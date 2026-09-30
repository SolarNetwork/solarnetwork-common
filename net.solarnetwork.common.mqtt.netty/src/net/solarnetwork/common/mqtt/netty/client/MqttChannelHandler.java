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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
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
import net.solarnetwork.common.mqtt.MqttMessageHandler;
import net.solarnetwork.common.mqtt.MqttTopicAliases;
import net.solarnetwork.common.mqtt.NoOpMqttTopicAliases;
import net.solarnetwork.common.mqtt.netty.NettyMqttMessage;
import net.solarnetwork.service.RemoteServiceException;

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

		// the CONNACK can never arrive now, so fail the connect rather than leaving the caller
		// to wait out its full connect timeout
		this.connectFuture.tryFailure(new ChannelClosedException("Channel closed before CONNACK."));

		log.debug("Clearing topic aliases for server (max {}) and client (max {})",
				serverAliases.getMaximumAliasCount(), client.getTopicAliases().getMaximumAliasCount());

		// force the client/server alias max to 0 so aliases will not be generated until after we
		// have a new connection; the handleConack() method will restore the client max
		serverAliases.setMaximumAliasCount(0);
		client.getTopicAliases().setMaximumAliasCount(0);
	}

	/**
	 * Deliver an incoming PUBLISH message to the handlers subscribed to its
	 * topic.
	 *
	 * <p>
	 * A handler that throws has not accepted the message. The exception is
	 * contained here rather than allowed to reach
	 * {@link #exceptionCaught(ChannelHandlerContext, Throwable)}, which closes
	 * the channel: the connection stays up and keeps processing messages, and
	 * only the acknowledgement is withheld.
	 * </p>
	 *
	 * @param message
	 *        the message to deliver
	 * @return a stage that completes once every handler has accepted the
	 *         message, and so it may be acknowledged, or completes exceptionally
	 *         if any did not
	 */
	private CompletableFuture<Void> invokeHandlersForIncomingPublish(MqttPublishMessage message) {
		boolean handlerInvoked = false;
		final List<CompletionStage<?>> results = new ArrayList<>(4);

		// decode topic alias if provided
		final String msgTopic = message.variableHeader().topicName();
		final MqttProperties props = message.variableHeader().properties();
		Integer topicAlias = null;
		if ( props != null ) {
			@SuppressWarnings("rawtypes")
			MqttProperty prop = props.getProperty(MqttProperties.TOPIC_ALIAS);
			if ( prop instanceof MqttProperties.IntegerProperty ) {
				topicAlias = ((MqttProperties.IntegerProperty) prop).value();
			}
		}
		final String topic;
		try {
			if ( topicAlias != null ) {
				final String aliased = serverAliases.aliasedTopic(msgTopic, topicAlias);
				if ( aliased == null ) {
					throw new IllegalStateException("Could not resolve topic alias " + topicAlias);
				}
				topic = aliased;
				if ( log.isDebugEnabled() ) {
					log.debug("Received message {} resolved topic [{}] with alias {} as [{}]",
							message.variableHeader().packetId(), msgTopic, topicAlias, topic);
				}
			} else {
				topic = msgTopic;
			}
		} catch ( RuntimeException e ) {
			// the topic cannot be resolved, so no handler can ever process this
			// message: report it as handled, because withholding the acknowledgement
			// would only stall the subscription on a message that cannot be retried
			log.warn("Discarding unresolvable incoming message on topic [{}]: {}", msgTopic,
					e.toString(), e);
			return CompletableFuture.completedFuture(null);
		}

		// iterate in place: the map is concurrent and each value is a CopyOnWriteArrayList,
		// so no defensive copy is needed even though a `once` subscription removes itself
		// below. Each subscription is held under its own topic key, so none can be seen twice.
		for ( CopyOnWriteArrayList<MqttSubscription> topicSubs : this.client.getSubscriptions()
				.values() ) {
			for ( MqttSubscription subscription : topicSubs ) {
				if ( !subscription.matches(topic) ) {
					continue;
				}
				if ( subscription.isOnce() && subscription.isCalled() ) {
					continue;
				}
				handlerInvoked = true;
				results.add(invokeHandler(subscription.getHandler(), topic, message));
				subscription.setCalled(true);
				if ( subscription.isOnce() ) {
					this.client.off(subscription.getTopic(), subscription.getHandler());
				}
			}
		}
		if ( !handlerInvoked && client.getDefaultHandler() != null ) {
			results.add(invokeHandler(client.getDefaultHandler(), topic, message));
		}
		if ( results.isEmpty() ) {
			return CompletableFuture.completedFuture(null);
		}
		return CompletableFuture.allOf(results.stream().map(CompletionStage::toCompletableFuture)
				.toArray(CompletableFuture[]::new));
	}

	/**
	 * Pass a message to one handler.
	 *
	 * <p>
	 * The message is copied out of the payload buffer before the handler is
	 * called, and the reader index restored, so the handler may keep the message
	 * after this method returns and a handler that consumed the buffer cannot
	 * corrupt the read for the handlers that follow.
	 * </p>
	 *
	 * @param handler
	 *        the handler to invoke
	 * @param topic
	 *        the resolved message topic
	 * @param message
	 *        the message to pass
	 * @return a stage that completes when the handler has accepted the message
	 */
	private CompletionStage<?> invokeHandler(MqttMessageHandler handler, String topic,
			MqttPublishMessage message) {
		final NettyMqttMessage msg;
		message.payload().markReaderIndex();
		try {
			// NettyMqttMessage copies the payload, so msg outlives this call
			msg = new NettyMqttMessage(topic, message.fixedHeader().isRetain(),
					message.fixedHeader().qosLevel(), message.payload());
		} finally {
			message.payload().resetReaderIndex();
		}
		CompletionStage<?> result;
		try {
			result = handler.onMqttMessageAsync(msg);
			if ( result == null ) {
				result = CompletableFuture.completedFuture(null);
			}
		} catch ( RuntimeException e ) {
			// an implementation may throw rather than return a failed stage
			result = CompletableFuture.failedFuture(e);
		}
		return result.whenComplete((r, e) -> {
			if ( e != null ) {
				log.warn("Error handling incoming message on topic [{}]: {}", topic, e.toString(), e);
			}
		});
	}

	private void handleConack(Channel channel, MqttConnAckMessage message) {
		MqttProperties props = message.variableHeader().properties();
		int maxPublishTopicAliases = 0;
		if ( props != null ) {
			@SuppressWarnings("rawtypes")
			MqttProperty prop = props.getProperty(MqttProperties.TOPIC_ALIAS_MAXIMUM);
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

				this.connectFuture.trySuccess(new MqttConnectResult(true,
						MqttConnectReturnCode.CONNECTION_ACCEPTED, channel.closeFuture()));

				for ( MqttPendingSubscription pending : this.client.getPendingSubscriptions()
						.values() ) {
					if ( !pending.isSent() ) {
						channel.write(pending.getSubscribeMessage());
						pending.setSent(true);
					}
				}

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
				this.connectFuture.trySuccess(new MqttConnectResult(false,
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
			this.client.getSubscriptions()
					.computeIfAbsent(pendingSubscription.getTopic(), k -> new CopyOnWriteArrayList<>())
					.addIfAbsent(subscription);
			this.client.getHandlerToSubscription()
					.computeIfAbsent(handler.getHandler(), k -> new CopyOnWriteArrayList<>())
					.addIfAbsent(subscription);
		}
		this.client.getPendingSubscribeTopics().remove(pendingSubscription.getTopic());

		this.client.getServerSubscriptions().add(pendingSubscription.getTopic());

		pendingSubscription.getFuture().trySuccess(null);
	}

	private void handlePublish(Channel channel, MqttPublishMessage message) {
		switch (message.fixedHeader().qosLevel()) {
			case AT_MOST_ONCE:
				// nothing to acknowledge, so the handler outcome cannot be acted on
				invokeHandlersForIncomingPublish(message);
				break;

			case AT_LEAST_ONCE: {
				// capture these now: the message is released once this returns, which can
				// happen before a handler completes
				final int packetId = message.variableHeader().packetId();
				final String topicName = message.variableHeader().topicName();
				invokeHandlersForIncomingPublish(message).whenComplete((r, ex) -> {
					if ( packetId == -1 ) {
						return;
					}
					if ( ex != null ) {
						// withhold the acknowledgement so the message stays unacknowledged
						// and the broker redelivers it, which is the guarantee QOS 1 gives
						log.warn(
								"Not acknowledging message {} on topic [{}], which a handler did not accept, so the broker can redeliver it.",
								packetId, topicName);
						return;
					}
					MqttFixedHeader ackHeader = new MqttFixedHeader(MqttMessageType.PUBACK, false,
							MqttQoS.AT_MOST_ONCE, false, 0);
					// safe from any thread: Netty schedules onto the channel's event loop
					channel.writeAndFlush(new MqttPubAckMessage(ackHeader,
							MqttMessageIdVariableHeader.from(packetId)));
				});
				break;
			}

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

					incomingQos2Publish.startPubrecRetransmitTimer(this.client.requireEventLoop().next(),
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
		this.client.getPendingServerUnsubscribes().remove(message.variableHeader().messageId());
		unsubscription.getFuture().trySuccess(null);
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
		if ( MqttPubackReasonCode.isError(reasonCode) ) {
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
			pendingPublish.getFuture().tryFailure(ex);
		} else {
			if ( reasonCode != (byte) 0 && log.isDebugEnabled() ) {
				// a non-zero success code, such as 0x10 "no matching subscribers"
				log.debug("PUBACK reason code {} on message {} to {}", Byte.toUnsignedInt(reasonCode),
						message.variableHeader().messageId(), client.getServerUri());
			}
			String topic = pendingPublish.getMessage().variableHeader().topicName();
			if ( topic != null && !topic.isEmpty()
					&& client.getTopicAliases().getMaximumAliasCount() > 0 ) {
				// confirm alias
				client.getTopicAliases().confirmTopicAlias(topic);
			}
			pendingPublish.getFuture().trySuccess(null);
		}
		pendingPublish.releasePayload();
	}

	private void handlePubrec(Channel channel, MqttMessage message) {
		final MqttMessageIdVariableHeader variableHeader = (MqttMessageIdVariableHeader) message
				.variableHeader();
		final MqttPendingPublish pendingPublish = this.client.getPendingPublishes()
				.get(variableHeader.messageId());
		if ( pendingPublish == null ) {
			log.warn("Pending publish state not found for message {}", variableHeader.messageId());
			return;
		}
		pendingPublish.onPubackReceived();

		MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBREL, false,
				MqttQoS.AT_LEAST_ONCE, false, 0);
		MqttMessage pubrelMessage = new MqttMessage(fixedHeader, variableHeader);
		channel.writeAndFlush(pubrelMessage);

		pendingPublish.setPubrelMessage(pubrelMessage);
		pendingPublish.startPubrelRetransmissionTimer(this.client.requireEventLoop().next(),
				this.client::sendAndFlushPacket);
	}

	private void handlePubrel(Channel channel, MqttMessage message) {
		MqttIncomingQos2Publish incomingQos2Publish = this.client.getQos2PendingIncomingPublishes()
				.remove(((MqttMessageIdVariableHeader) message.variableHeader()).messageId());
		if ( incomingQos2Publish != null ) {
			incomingQos2Publish.onPubrelReceived();
			try {
				// the handler outcome cannot be acted on here: the PUBREC has already been
				// sent, so the broker will not send the PUBLISH again, and withholding the
				// PUBCOMP would only stall the flow rather than cause a redelivery
				this.invokeHandlersForIncomingPublish(incomingQos2Publish.getIncomingPublish());
			} finally {
				// release the reference retained when the PUBLISH arrived
				incomingQos2Publish.getIncomingPublish().payload().release();
			}
		}
		MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBCOMP, false,
				MqttQoS.AT_MOST_ONCE, false, 0);
		MqttMessageIdVariableHeader variableHeader = MqttMessageIdVariableHeader
				.from(((MqttMessageIdVariableHeader) message.variableHeader()).messageId());
		channel.writeAndFlush(new MqttMessage(fixedHeader, variableHeader));
	}

	private void handlePubcomp(MqttMessage message) {
		final MqttMessageIdVariableHeader variableHeader = (MqttMessageIdVariableHeader) message
				.variableHeader();
		final MqttPendingPublish pendingPublish = this.client.getPendingPublishes()
				.get(variableHeader.messageId());
		if ( pendingPublish == null ) {
			log.warn("Pending publish state not found for message {}",
					((MqttMessageIdVariableHeader) message.variableHeader()).messageId());
			return;
		}
		this.client.getPendingPublishes().remove(variableHeader.messageId());
		pendingPublish.getFuture().trySuccess(null);
		pendingPublish.releasePayload();
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
		// the connection is not usable after an unhandled error, for example a failed TLS
		// handshake, so close it rather than leaving it open until an idle timeout
		this.connectFuture.tryFailure(cause);
		ctx.close();
	}

}
