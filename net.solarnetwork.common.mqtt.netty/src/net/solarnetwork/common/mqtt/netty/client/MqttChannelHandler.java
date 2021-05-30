/**
 * Copyright 2019 SolarNetwork.net Dev Team
 * Copyright © 2016-2019 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.solarnetwork.common.mqtt.netty.client;

import static java.util.stream.Collectors.toList;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.MqttConnAckMessage;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import io.netty.handler.codec.mqtt.MqttConnectPayload;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttConnectVariableHeader;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttProperties;
import io.netty.handler.codec.mqtt.MqttProperties.MqttProperty;
import io.netty.handler.codec.mqtt.MqttPubAckMessage;
import io.netty.handler.codec.mqtt.MqttPubReplyMessageVariableHeader;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttSubAckMessage;
import io.netty.handler.codec.mqtt.MqttUnsubAckMessage;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.Promise;
import net.solarnetwork.common.mqtt.BasicMqttTopicAliases;
import net.solarnetwork.common.mqtt.MqttTopicAliases;
import net.solarnetwork.common.mqtt.NoOpMqttTopicAliases;
import net.solarnetwork.common.mqtt.netty.NettyMqttMessage;
import net.solarnetwork.support.RemoteServiceException;

final class MqttChannelHandler extends SimpleChannelInboundHandler<MqttMessage> {

	private static final Logger log = LoggerFactory.getLogger(MqttChannelHandler.class);

	private final MqttClientImpl client;
	private final Promise<MqttConnectResult> connectFuture;
	private final MqttTopicAliases serverAliases;

	MqttChannelHandler(MqttClientImpl client, Promise<MqttConnectResult> connectFuture) {
		this.client = client;
		this.connectFuture = connectFuture;
		this.serverAliases = (client.getClientConfig().getProtocolVersion().protocolLevel() > (byte) 4
				? new BasicMqttTopicAliases(0)
				: new NoOpMqttTopicAliases());
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, MqttMessage msg) throws Exception {
		switch (msg.fixedHeader().messageType()) {
			case CONNACK:
				handleConack(ctx.channel(), (MqttConnAckMessage) msg);
				break;
			case SUBACK:
				handleSubAck((MqttSubAckMessage) msg);
				break;
			case PUBLISH:
				handlePublish(ctx.channel(), (MqttPublishMessage) msg);
				break;
			case UNSUBACK:
				handleUnsuback((MqttUnsubAckMessage) msg);
				break;
			case PUBACK:
				handlePuback((MqttPubAckMessage) msg);
				break;
			case PUBREC:
				handlePubrec(ctx.channel(), msg);
				break;
			case PUBREL:
				handlePubrel(ctx.channel(), msg);
				break;
			case PUBCOMP:
				handlePubcomp(msg);
				break;
			default:
				// nothing
		}
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);

		MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.CONNECT, false,
				MqttQoS.AT_MOST_ONCE, false, 0);
		MqttClientConfig config = this.client.getClientConfig();

		// @formatter:off
		MqttConnectVariableHeader variableHeader = new MqttConnectVariableHeader(
				config.getProtocolVersion().protocolName(), // Protocol Name
				config.getProtocolVersion().protocolLevel(), // Protocol Level
				config.getUsername() != null, // Has Username
				config.getPassword() != null, // Has Password
				config.getLastWill() != null && config.getLastWill().isRetain(), // Will Retain
				config.getLastWill() != null // Will QOS
						? config.getLastWill().getQos().value()
						: 0,
				config.getLastWill() != null, // Has Will
				config.isCleanSession(), // Clean Session
				config.getTimeoutSeconds(), // Timeout
				config.getConnectionProperties()
		);
		MqttConnectPayload payload = new MqttConnectPayload(config.getClientId(),
				config.getLastWill() != null
						? config.getLastWill().getTopic()
						: null,
				config.getLastWill() != null 
						? config.getLastWill().getMessage().getBytes(CharsetUtil.UTF_8) 
						: null,
				config.getUsername(),
				config.getPassword() != null
						? config.getPassword().getBytes(CharsetUtil.UTF_8)
						: null);
		// @formatter:on
		ctx.channel().writeAndFlush(new MqttConnectMessage(fixedHeader, variableHeader, payload));
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		log.debug("Clearing topic aliases for server (max {}) and client (max {})",
				serverAliases.getMaximumAliasCount(), client.getTopicAliases().getMaximumAliasCount());

		// force the client/server alias max to 0 so aliases will not be generated until after we
		// have a new connection; the handleConack() method will restore the client max
		serverAliases.setMaximumAliasCount(0);
		client.getTopicAliases().setMaximumAliasCount(0);
	}

	private void invokeHandlersForIncomingPublish(MqttPublishMessage message) {
		boolean handlerInvoked = false;

		// decode topic alias if provided
		final String msgTopic = message.variableHeader().topicName();
		MqttProperties props = message.variableHeader().properties();
		Integer topicAlias = null;
		if ( props != null ) {
			@SuppressWarnings("rawtypes")
			MqttProperty prop = props.getProperty(MqttProperties.MqttPropertyType.TOPIC_ALIAS.value());
			if ( prop instanceof MqttProperties.IntegerProperty ) {
				topicAlias = ((MqttProperties.IntegerProperty) prop).value();
			}
		}

		final String topic = serverAliases.aliasedTopic(msgTopic, topicAlias);
		if ( log.isDebugEnabled() && topicAlias != null ) {
			log.debug("Received message {} resolved topic [{}] with alias {} as [{}]",
					message.variableHeader().packetId(), msgTopic, topicAlias, topic);
		}

		for ( MqttSubscription subscription : new LinkedHashSet<>(this.client.getSubscriptions().values()
				.stream().flatMap(List::stream).collect(toList())) ) {
			if ( subscription.matches(topic) ) {
				if ( subscription.isOnce() && subscription.isCalled() ) {
					continue;
				}
				message.payload().markReaderIndex();
				subscription.setCalled(true);
				subscription.getHandler()
						.onMqttMessage(new NettyMqttMessage(topic, message.fixedHeader().isRetain(),
								message.fixedHeader().qosLevel(), message.payload()));
				if ( subscription.isOnce() ) {
					this.client.off(subscription.getTopic(), subscription.getHandler());
				}
				message.payload().resetReaderIndex();
				handlerInvoked = true;
			}
		}
		if ( !handlerInvoked && client.getDefaultHandler() != null ) {
			client.getDefaultHandler()
					.onMqttMessage(new NettyMqttMessage(topic, message.fixedHeader().isRetain(),
							message.fixedHeader().qosLevel(), message.payload()));
		}
		message.payload().release();
	}

	private void handleConack(Channel channel, MqttConnAckMessage message) {
		MqttProperties props = message.variableHeader().properties();
		int maxPublishTopicAliases = 0;
		if ( props != null ) {
			@SuppressWarnings("rawtypes")
			MqttProperty prop = props
					.getProperty(MqttProperties.MqttPropertyType.TOPIC_ALIAS_MAXIMUM.value());
			if ( prop instanceof MqttProperties.IntegerProperty ) {
				Integer max = ((MqttProperties.IntegerProperty) prop).value();
				if ( max != null ) {
					maxPublishTopicAliases = max.intValue();
				}
			}
		}

		switch (message.variableHeader().connectReturnCode()) {
			case CONNECTION_ACCEPTED:
				// enforce the server-requested maximum topic alias count when publishing from client
				client.getTopicAliases().setMaximumAliasCount(maxPublishTopicAliases);

				// enforce the client-requested maximum topic alias count when subscribing from the server
				final int maxSubscribeTopicAliases = (client.getClientConfig().getProtocolVersion()
						.protocolLevel() > (byte) 4 ? client.getClientConfig().getMaximumTopicAliases()
								: 0);
				serverAliases.setMaximumAliasCount(maxSubscribeTopicAliases);

				log.debug("MQTT connection {} allowable topic aliases for server: {}; client: {}",
						client.getServerUri(), maxSubscribeTopicAliases, maxPublishTopicAliases);

				this.connectFuture.setSuccess(new MqttConnectResult(true,
						MqttConnectReturnCode.CONNECTION_ACCEPTED, channel.closeFuture()));

				this.client.getPendingSubscriptions().entrySet().stream()
						.filter((e) -> !e.getValue().isSent()).forEach((e) -> {
							channel.write(e.getValue().getSubscribeMessage());
							e.getValue().setSent(true);
						});

				this.client.getPendingPublishes().forEach((id, publish) -> {
					if ( publish.isSent() )
						return;
					channel.write(publish.getMessage());
					publish.setSent(true);
					if ( publish.getQos() == MqttQoS.AT_MOST_ONCE ) {
						publish.getFuture().setSuccess(null); //We don't get an ACK for QOS 0
						this.client.getPendingPublishes().remove(publish.getMessageId());
					}
				});
				channel.flush();
				if ( this.client.isReconnect() ) {
					this.client.onSuccessfulReconnect();
				}
				break;

			case CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD:
			case CONNECTION_REFUSED_IDENTIFIER_REJECTED:
			case CONNECTION_REFUSED_NOT_AUTHORIZED:
			case CONNECTION_REFUSED_SERVER_UNAVAILABLE:
			case CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION:
			case CONNECTION_REFUSED_BAD_AUTHENTICATION_METHOD:
			case CONNECTION_REFUSED_BAD_USERNAME_OR_PASSWORD:
			case CONNECTION_REFUSED_BANNED:
			case CONNECTION_REFUSED_CLIENT_IDENTIFIER_NOT_VALID:
			case CONNECTION_REFUSED_CONNECTION_RATE_EXCEEDED:
			case CONNECTION_REFUSED_IMPLEMENTATION_SPECIFIC:
			case CONNECTION_REFUSED_MALFORMED_PACKET:
			case CONNECTION_REFUSED_NOT_AUTHORIZED_5:
			case CONNECTION_REFUSED_PACKET_TOO_LARGE:
			case CONNECTION_REFUSED_PAYLOAD_FORMAT_INVALID:
			case CONNECTION_REFUSED_PROTOCOL_ERROR:
			case CONNECTION_REFUSED_QOS_NOT_SUPPORTED:
			case CONNECTION_REFUSED_QUOTA_EXCEEDED:
			case CONNECTION_REFUSED_RETAIN_NOT_SUPPORTED:
			case CONNECTION_REFUSED_SERVER_BUSY:
			case CONNECTION_REFUSED_SERVER_MOVED:
			case CONNECTION_REFUSED_SERVER_UNAVAILABLE_5:
			case CONNECTION_REFUSED_TOPIC_NAME_INVALID:
			case CONNECTION_REFUSED_UNSPECIFIED_ERROR:
			case CONNECTION_REFUSED_UNSUPPORTED_PROTOCOL_VERSION:
			case CONNECTION_REFUSED_USE_ANOTHER_SERVER:
				this.connectFuture.setSuccess(new MqttConnectResult(false,
						message.variableHeader().connectReturnCode(), channel.closeFuture()));
				channel.close();
				// Don't start reconnect logic here
				break;
		}
	}

	private void handleSubAck(MqttSubAckMessage message) {
		MqttPendingSubscription pendingSubscription = this.client.getPendingSubscriptions()
				.remove(message.variableHeader().messageId());
		if ( pendingSubscription == null ) {
			return;
		}
		pendingSubscription.onSubackReceived();
		for ( MqttPendingSubscription.MqttPendingHandler handler : pendingSubscription.getHandlers() ) {
			MqttSubscription subscription = new MqttSubscription(pendingSubscription.getTopic(),
					handler.getHandler(), handler.isOnce());
			CopyOnWriteArrayList<MqttSubscription> l = (CopyOnWriteArrayList<MqttSubscription>) this.client
					.getSubscriptions()
					.computeIfAbsent(pendingSubscription.getTopic(), k -> new CopyOnWriteArrayList<>());
			l.addIfAbsent(subscription);
			l = (CopyOnWriteArrayList<MqttSubscription>) this.client.getHandlerToSubscribtion()
					.computeIfAbsent(handler.getHandler(), k -> new CopyOnWriteArrayList<>());
			l.addIfAbsent(subscription);
		}
		this.client.getPendingSubscribeTopics().remove(pendingSubscription.getTopic());

		this.client.getServerSubscriptions().add(pendingSubscription.getTopic());

		if ( !pendingSubscription.getFuture().isDone() ) {
			pendingSubscription.getFuture().setSuccess(null);
		}
	}

	private void handlePublish(Channel channel, MqttPublishMessage message) {
		switch (message.fixedHeader().qosLevel()) {
			case AT_MOST_ONCE:
				invokeHandlersForIncomingPublish(message);
				break;

			case AT_LEAST_ONCE:
				invokeHandlersForIncomingPublish(message);
				if ( message.variableHeader().packetId() != -1 ) {
					MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBACK, false,
							MqttQoS.AT_MOST_ONCE, false, 0);
					MqttMessageIdVariableHeader variableHeader = MqttMessageIdVariableHeader
							.from(message.variableHeader().packetId());
					channel.writeAndFlush(new MqttPubAckMessage(fixedHeader, variableHeader));
				}
				break;

			case EXACTLY_ONCE:
				if ( message.variableHeader().packetId() != -1 ) {
					MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBREC, false,
							MqttQoS.AT_MOST_ONCE, false, 0);
					MqttMessageIdVariableHeader variableHeader = MqttMessageIdVariableHeader
							.from(message.variableHeader().packetId());
					MqttMessage pubrecMessage = new MqttMessage(fixedHeader, variableHeader);

					MqttIncomingQos2Publish incomingQos2Publish = new MqttIncomingQos2Publish(message,
							pubrecMessage);
					this.client.getQos2PendingIncomingPublishes()
							.put(message.variableHeader().packetId(), incomingQos2Publish);
					message.payload().retain();
					incomingQos2Publish.startPubrecRetransmitTimer(this.client.getEventLoop().next(),
							this.client::sendAndFlushPacket);

					channel.writeAndFlush(pubrecMessage);
				}
				break;

			default:
				// ignore
		}
	}

	private void handleUnsuback(MqttUnsubAckMessage message) {
		MqttPendingUnsubscription unsubscription = this.client.getPendingServerUnsubscribes()
				.get(message.variableHeader().messageId());
		if ( unsubscription == null ) {
			return;
		}
		unsubscription.onUnsubackReceived();
		this.client.getServerSubscriptions().remove(unsubscription.getTopic());
		unsubscription.getFuture().setSuccess(null);
		this.client.getPendingServerUnsubscribes().remove(message.variableHeader().messageId());
	}

	private void handlePuback(MqttPubAckMessage message) {
		MqttPendingPublish pendingPublish = this.client.getPendingPublishes()
				.get(message.variableHeader().messageId());
		if ( pendingPublish == null ) {
			return;
		}
		pendingPublish.onPubackReceived();
		this.client.getPendingPublishes().remove(message.variableHeader().messageId());
		byte reasonCode = 0;
		if ( message.variableHeader() instanceof MqttPubReplyMessageVariableHeader ) {
			MqttPubReplyMessageVariableHeader rep = (MqttPubReplyMessageVariableHeader) message
					.variableHeader();
			reasonCode = rep.reasonCode();
		}
		if ( reasonCode != (byte) 0 ) {
			MqttPubackReasonCode r = null;
			try {
				r = MqttPubackReasonCode.forCode(reasonCode);
			} catch ( IllegalArgumentException e ) {
				// ignore
			}
			String msg = (r != null
					? String.format("Unsuccessful PUBACK reason code %d (%s) on message %d",
							Byte.toUnsignedInt(reasonCode), r, message.variableHeader().messageId())
					: String.format("Unsuccessful PUBACK reason code %d on message %d",
							Byte.toUnsignedInt(reasonCode), message.variableHeader().messageId()));
			RemoteServiceException ex = new RemoteServiceException(msg);
			pendingPublish.getFuture().setFailure(ex);
		} else {
			String topic = pendingPublish.getMessage().variableHeader().topicName();
			if ( topic != null && !topic.isEmpty()
					&& client.getTopicAliases().getMaximumAliasCount() > 0 ) {
				// confirm alias
				client.getTopicAliases().confirmTopicAlias(topic);
			}
			pendingPublish.getFuture().setSuccess(null);
		}
		pendingPublish.getPayload().release();
	}

	private void handlePubrec(Channel channel, MqttMessage message) {
		MqttPendingPublish pendingPublish = this.client.getPendingPublishes()
				.get(((MqttMessageIdVariableHeader) message.variableHeader()).messageId());
		pendingPublish.onPubackReceived();

		MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBREL, false,
				MqttQoS.AT_LEAST_ONCE, false, 0);
		MqttMessageIdVariableHeader variableHeader = (MqttMessageIdVariableHeader) message
				.variableHeader();
		MqttMessage pubrelMessage = new MqttMessage(fixedHeader, variableHeader);
		channel.writeAndFlush(pubrelMessage);

		pendingPublish.setPubrelMessage(pubrelMessage);
		pendingPublish.startPubrelRetransmissionTimer(this.client.getEventLoop().next(),
				this.client::sendAndFlushPacket);
	}

	private void handlePubrel(Channel channel, MqttMessage message) {
		if ( this.client.getQos2PendingIncomingPublishes()
				.containsKey(((MqttMessageIdVariableHeader) message.variableHeader()).messageId()) ) {
			MqttIncomingQos2Publish incomingQos2Publish = this.client.getQos2PendingIncomingPublishes()
					.get(((MqttMessageIdVariableHeader) message.variableHeader()).messageId());
			this.invokeHandlersForIncomingPublish(incomingQos2Publish.getIncomingPublish());
			incomingQos2Publish.onPubrelReceived();
			this.client.getQos2PendingIncomingPublishes()
					.remove(incomingQos2Publish.getIncomingPublish().variableHeader().packetId());
		}
		MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBCOMP, false,
				MqttQoS.AT_MOST_ONCE, false, 0);
		MqttMessageIdVariableHeader variableHeader = MqttMessageIdVariableHeader
				.from(((MqttMessageIdVariableHeader) message.variableHeader()).messageId());
		channel.writeAndFlush(new MqttMessage(fixedHeader, variableHeader));
	}

	private void handlePubcomp(MqttMessage message) {
		MqttMessageIdVariableHeader variableHeader = (MqttMessageIdVariableHeader) message
				.variableHeader();
		MqttPendingPublish pendingPublish = this.client.getPendingPublishes()
				.get(variableHeader.messageId());
		pendingPublish.getFuture().setSuccess(null);
		this.client.getPendingPublishes().remove(variableHeader.messageId());
		pendingPublish.getPayload().release();
		pendingPublish.onPubcompReceived();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		if ( log.isWarnEnabled() && !client.isDisconnected() ) {
			if ( cause instanceof IOException ) {
				log.warn("Communication problem in MQTT connection {}: {}", client.getServerUri(),
						cause.getMessage());
			} else {
				log.warn("Exception in MQTT connection {}: {}", client.getServerUri(), cause.toString(),
						cause);
			}
		}
	}

}
