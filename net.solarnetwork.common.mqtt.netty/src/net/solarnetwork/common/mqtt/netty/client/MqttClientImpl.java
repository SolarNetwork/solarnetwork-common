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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttProperties;
import io.netty.handler.codec.mqtt.MqttProperties.MqttProperty;
import io.netty.handler.codec.mqtt.MqttProperties.MqttPropertyType;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttPublishVariableHeader;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttSubscribeMessage;
import io.netty.handler.codec.mqtt.MqttSubscribePayload;
import io.netty.handler.codec.mqtt.MqttTopicSubscription;
import io.netty.handler.codec.mqtt.MqttUnsubscribeMessage;
import io.netty.handler.codec.mqtt.MqttUnsubscribePayload;
import io.netty.handler.codec.mqtt.MqttVersion;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;
import net.solarnetwork.common.mqtt.BasicMqttTopicAliases;
import net.solarnetwork.common.mqtt.MqttMessageHandler;
import net.solarnetwork.common.mqtt.MqttTopicAliases;
import net.solarnetwork.domain.KeyValuePair;

/**
 * Represents an MqttClientImpl connected to a single MQTT server. Will try to
 * keep the connection going at all times.
 * 
 * @version 1.2
 */
final class MqttClientImpl implements MqttClient {

	/**
	 * A multiplication factor applied to the configured
	 * {@code IdleStateHandler} read timeout, in relation to its configured
	 * write timeout.
	 * 
	 * @since 1.2
	 */
	public static final int READ_TIMEOUT_FACTOR = 2;

	private static final Logger log = LoggerFactory.getLogger(MqttClientImpl.class);

	private final Set<String> serverSubscriptions = new HashSet<>();
	private final IntObjectHashMap<MqttPendingUnsubscription> pendingServerUnsubscribes = new IntObjectHashMap<>();
	private final IntObjectHashMap<MqttIncomingQos2Publish> qos2PendingIncomingPublishes = new IntObjectHashMap<>();
	private final ConcurrentMap<Integer, MqttPendingPublish> pendingPublishes = new ConcurrentHashMap<>(
			16, 0.7f, 2);
	private final MultiValueMap<String, MqttSubscription> subscriptions = new LinkedMultiValueMap<>();
	private final IntObjectHashMap<MqttPendingSubscription> pendingSubscriptions = new IntObjectHashMap<>();
	private final Set<String> pendingSubscribeTopics = new HashSet<>();
	private final MultiValueMap<MqttMessageHandler, MqttSubscription> handlerToSubscribtion = new LinkedMultiValueMap<>();
	private final AtomicInteger nextMessageId = new AtomicInteger(0);
	private final MqttTopicAliases clientAliases = new BasicMqttTopicAliases(0);

	private final MqttClientConfig clientConfig;

	private final MqttMessageHandler defaultHandler;

	private EventLoopGroup eventLoop;

	private volatile Channel channel;

	private volatile boolean disconnected = false;
	private volatile boolean reconnect = false;
	private boolean wireLogging = false;
	private String host;
	private int port;
	private MqttClientCallback callback;
	private boolean publishRetransmit = false;
	private int pendingAbortTimeoutMinutes = 60;

	/**
	 * Construct the MqttClientImpl with default config
	 */
	public MqttClientImpl(MqttMessageHandler defaultHandler) {
		this.clientConfig = new MqttClientConfig();
		this.defaultHandler = defaultHandler;
	}

	/**
	 * Construct the MqttClientImpl with additional config. This config can also
	 * be changed using the {@link #getClientConfig()} function
	 *
	 * @param clientConfig
	 *        The config object to use while looking for settings
	 */
	public MqttClientImpl(MqttClientConfig clientConfig, MqttMessageHandler defaultHandler) {
		this.clientConfig = clientConfig;
		this.defaultHandler = defaultHandler;
	}

	/**
	 * Connect to the specified hostname/ip. By default uses port 1883. If you
	 * want to change the port number, see {@link #connect(String, int)}
	 *
	 * @param host
	 *        The ip address or host to connect to
	 * @return A future which will be completed when the connection is opened
	 *         and we received an CONNACK
	 */
	@Override
	public Future<MqttConnectResult> connect(String host) {
		return connect(host, 1883);
	}

	/**
	 * Connect to the specified hostname/ip using the specified port
	 *
	 * @param host
	 *        The ip address or host to connect to
	 * @param port
	 *        The tcp port to connect to
	 * @return A future which will be completed when the connection is opened
	 *         and we received an CONNACK
	 */
	@Override
	public Future<MqttConnectResult> connect(String host, int port) {
		return connect(host, port, false);
	}

	private Future<MqttConnectResult> connect(String host, int port, boolean reconnect) {
		if ( this.eventLoop == null ) {
			NioEventLoopGroup el = new NioEventLoopGroup();
			setEventLoop(el);
		}
		this.host = host;
		this.port = port;
		Promise<MqttConnectResult> connectFuture = new DefaultPromise<>(this.eventLoop.next());
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.group(this.eventLoop);
		bootstrap.channel(clientConfig.getChannelClass());
		bootstrap.remoteAddress(host, port);
		bootstrap.handler(
				new MqttChannelInitializer(connectFuture, host, port, clientConfig.getSslContext()));
		ChannelFuture future = bootstrap.connect();

		future.addListener((ChannelFutureListener) f -> {
			if ( f.isSuccess() ) {
				MqttClientImpl.this.channel = f.channel();
				MqttClientImpl.this.channel.closeFuture()
						.addListener((ChannelFutureListener) channelFuture -> {
							if ( isConnected() ) {
								return;
							}
							ChannelClosedException e = new ChannelClosedException("Channel is closed!");
							if ( callback != null ) {
								try {
									callback.connectionLost(e);
								} catch ( Throwable t ) {
									// ignore
								}
							}
							pendingSubscriptions.clear();
							serverSubscriptions.clear();
							subscriptions.clear();
							pendingServerUnsubscribes.clear();
							qos2PendingIncomingPublishes.clear();
							pendingPublishes.clear();
							pendingSubscribeTopics.clear();
							handlerToSubscribtion.clear();
							clientAliases.setMaximumAliasCount(0); // also clears
							scheduleConnectIfRequired(host, port, true);
						});
			} else {
				scheduleConnectIfRequired(host, port, reconnect);
			}
		});
		return connectFuture;
	}

	private void scheduleConnectIfRequired(String host, int port, boolean reconnect) {
		if ( clientConfig.isReconnect() && !disconnected ) {
			if ( reconnect ) {
				this.reconnect = true;
			}
			eventLoop.schedule((Runnable) () -> connect(host, port, reconnect),
					clientConfig.getReconnectDelay(), TimeUnit.SECONDS);
		}
	}

	private void cleanup() {
		final int timeoutMins = getPendingAbortTimeoutMinutes();
		final long timeout = TimeUnit.MINUTES.toMillis(timeoutMins);
		if ( timeout < 1 ) {
			return;
		}
		final long now = System.currentTimeMillis();
		for ( Iterator<MqttPendingPublish> itr = getPendingPublishes().values().iterator(); itr
				.hasNext(); ) {
			MqttPendingPublish pending = itr.next();
			if ( pending.getDate() + timeout < now ) {
				log.warn("Timeout on pending publish message {}: aborting publish.",
						pending.getMessageId());
				pending.stop();
				pending.getFuture().setFailure(new TimeoutException(
						"Failed to publish message within " + timeoutMins + " minutes"));
				pending.getPayload().release();
				itr.remove();
			}
		}
	}

	@Override
	public URI getServerUri() {
		String host = this.host;
		if ( host == null || host.isEmpty() ) {
			return null;
		}
		StringBuilder buf = new StringBuilder("mqtt");
		if ( clientConfig.getSslContext() != null ) {
			buf.append("s");
		}
		buf.append("://").append(host).append(":").append(port);
		try {
			return new URI(buf.toString());
		} catch ( URISyntaxException e ) {
			throw new IllegalArgumentException("Bad URI syntax from [" + buf + "]", e);
		}
	}

	@Override
	public boolean isConnected() {
		return !disconnected && channel != null && channel.isActive();
	}

	@Override
	public Future<MqttConnectResult> reconnect() {
		if ( host == null ) {
			throw new IllegalStateException("Cannot reconnect. Call connect() first");
		}
		return connect(host, port);
	}

	@Override
	public EventLoopGroup getEventLoop() {
		return eventLoop;
	}

	@Override
	public void setEventLoop(EventLoopGroup eventLoop) {
		this.eventLoop = eventLoop;
		eventLoop.scheduleWithFixedDelay(this::cleanup, 30, 30, TimeUnit.MINUTES);
	}

	@Override
	public Future<Void> on(String topic, MqttMessageHandler handler) {
		return on(topic, handler, MqttQoS.AT_MOST_ONCE);
	}

	@Override
	public Future<Void> on(String topic, MqttMessageHandler handler, MqttQoS qos) {
		return createSubscription(topic, handler, false, qos);
	}

	@Override
	public Future<Void> once(String topic, MqttMessageHandler handler) {
		return once(topic, handler, MqttQoS.AT_MOST_ONCE);
	}

	@Override
	public Future<Void> once(String topic, MqttMessageHandler handler, MqttQoS qos) {
		return createSubscription(topic, handler, true, qos);
	}

	@Override
	public Future<Void> off(String topic, MqttMessageHandler handler) {
		Promise<Void> future = new DefaultPromise<>(this.eventLoop.next());
		List<MqttSubscription> subs = this.handlerToSubscribtion.get(handler);
		if ( subs != null ) {
			for ( MqttSubscription subscription : new ArrayList<>(subs) ) {
				if ( topic.equals(subscription.getTopic()) ) {
					this.subscriptions.computeIfPresent(topic, (k, v) -> {
						if ( v != null ) {
							v.remove(subscription);
						}
						return v;
					});
					subs.remove(subscription);
				}
			}
		}
		this.handlerToSubscribtion.computeIfPresent(handler, (k, v) -> {
			if ( v != null && v.isEmpty() ) {
				v = null;
			}
			return v;
		});
		this.checkSubscribtions(topic, future);
		return future;
	}

	@Override
	public Future<Void> off(String topic) {
		Promise<Void> future = new DefaultPromise<>(this.eventLoop.next());
		Set<MqttSubscription> subscriptions = new LinkedHashSet<>(this.subscriptions.get(topic));
		for ( MqttSubscription subscription : subscriptions ) {
			for ( MqttSubscription handSub : this.handlerToSubscribtion
					.get(subscription.getHandler()) ) {
				this.subscriptions.computeIfPresent(topic, (k, v) -> {
					if ( v != null ) {
						v.remove(handSub);
					}
					return v;
				});
			}
			this.handlerToSubscribtion.computeIfPresent(subscription.getHandler(), (k, v) -> {
				if ( v != null ) {
					v.remove(subscription);
				}
				return v;
			});
		}
		this.checkSubscribtions(topic, future);
		return future;
	}

	@Override
	public Future<Void> publish(String topic, ByteBuf payload) {
		return publish(topic, payload, MqttQoS.AT_MOST_ONCE, false, null);
	}

	@Override
	public Future<Void> publish(String topic, ByteBuf payload, MqttQoS qos) {
		return publish(topic, payload, qos, false, null);
	}

	@Override
	public Future<Void> publish(String topic, ByteBuf payload, boolean retain) {
		return publish(topic, payload, MqttQoS.AT_MOST_ONCE, retain, null);
	}

	@Override
	public Future<Void> publish(String topic, ByteBuf payload, MqttQoS qos, boolean retain) {
		return publish(topic, payload, qos, retain, null);
	}

	@Override
	public Future<Void> publish(String topic, ByteBuf payload, MqttQoS qos, boolean retain,
			net.solarnetwork.common.mqtt.MqttProperties properties) {
		Promise<Void> future = new DefaultPromise<>(this.eventLoop.next());
		MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBLISH, false, qos, retain,
				0);

		MqttProperties props = MqttProperties.NO_PROPERTIES;
		if ( properties != null && !properties.isEmpty() ) {
			props = new MqttProperties();
			copyProperties(properties, props);
		}

		// use topic alias if possible
		if ( this.clientConfig.getProtocolVersion().protocolLevel() >= MqttVersion.MQTT_5
				.protocolLevel() ) {
			final MqttProperties p = (props == MqttProperties.NO_PROPERTIES ? new MqttProperties()
					: props);
			topic = this.clientAliases.topicAlias(topic, a -> {
				p.add(new MqttProperties.IntegerProperty(MqttPropertyType.TOPIC_ALIAS.value(), a));
			});
			props = p;
		}

		final boolean retransmit = (publishRetransmit && qos != MqttQoS.AT_MOST_ONCE
				|| qos == MqttQoS.EXACTLY_ONCE);
		MqttPublishVariableHeader variableHeader = new MqttPublishVariableHeader(topic,
				getNewMessageId().messageId(), props);
		MqttPublishMessage message = new MqttPublishMessage(fixedHeader, variableHeader, payload);
		MqttPendingPublish pendingPublish = new MqttPendingPublish(variableHeader.packetId(), future,
				payload.retain(), message, qos, retransmit);

		// immediately stash pending in case response comes immediately
		this.pendingPublishes.put(pendingPublish.getMessageId(), pendingPublish);
		ChannelFuture channelFuture = this.sendAndFlushPacket(message);

		if ( channelFuture != null ) {
			pendingPublish.setSent(true);
			if ( channelFuture.cause() != null ) {
				future.setFailure(channelFuture.cause());
				this.pendingPublishes.remove(pendingPublish.getMessageId());
				payload.release();
				return future;
			}
		}
		if ( pendingPublish.isSent() && pendingPublish.getQos() == MqttQoS.AT_MOST_ONCE ) {
			pendingPublish.getFuture().setSuccess(null); //We don't get an ACK for QOS 0
			this.pendingPublishes.remove(pendingPublish.getMessageId());
			payload.release();
		} else if ( pendingPublish.isSent() && retransmit ) {
			pendingPublish.startPublishRetransmissionTimer(this.eventLoop.next(),
					this::sendAndFlushPacket);
		}
		return future;
	}

	private void copyProperties(net.solarnetwork.common.mqtt.MqttProperties properties,
			MqttProperties props) {
		if ( properties == null ) {
			return;
		}
		for ( net.solarnetwork.common.mqtt.MqttProperty<?> p : properties ) {
			MqttProperty<?> prop = null;
			Class<?> valueType = p.getType().getValueType();
			if ( Integer.class.isAssignableFrom(valueType) ) {
				prop = new MqttProperties.IntegerProperty(p.getType().getKey(), (Integer) p.getValue());
			} else if ( String.class.isAssignableFrom(valueType) ) {
				prop = new MqttProperties.StringProperty(p.getType().getKey(), p.getValue().toString());
			} else if ( byte[].class.isAssignableFrom(valueType) ) {
				prop = new MqttProperties.BinaryProperty(p.getType().getKey(), (byte[]) p.getValue());
			} else if ( KeyValuePair.class.isAssignableFrom(valueType) ) {
				KeyValuePair kp = (KeyValuePair) p.getValue();
				prop = new MqttProperties.UserProperty(kp.getKey(), kp.getValue());
			}
			if ( prop != null ) {
				props.add(prop);
			}
		}
	}

	@Override
	public MqttClientConfig getClientConfig() {
		return clientConfig;
	}

	@Override
	public java.util.concurrent.Future<?> disconnect() {
		disconnected = true;
		CompletableFuture<Void> result = new CompletableFuture<>();
		if ( this.channel != null ) {
			this.reconnect = false;
			MqttMessage message = new MqttMessage(new MqttFixedHeader(MqttMessageType.DISCONNECT, false,
					MqttQoS.AT_MOST_ONCE, false, 0));
			this.sendAndFlushPacket(message)
					.addListener(future1 -> channel.close().addListener(closeFuture -> {
						if ( closeFuture.isSuccess() ) {
							result.complete(null);
						} else {
							result.completeExceptionally(closeFuture.cause());
						}
					}));
		} else {
			result.complete(null);
		}
		return result;
	}

	@Override
	public boolean isDisconnected() {
		return disconnected == true;
	}

	@Override
	public void setCallback(MqttClientCallback callback) {
		this.callback = callback;
	}

	///////////////////////////////////////////// PRIVATE API /////////////////////////////////////////////

	public boolean isReconnect() {
		return reconnect;
	}

	public void onSuccessfulReconnect() {
		if ( callback != null ) {
			callback.onSuccessfulReconnect();
		}
	}

	ChannelFuture sendAndFlushPacket(Object message) {
		if ( this.channel == null ) {
			return null;
		}
		if ( this.channel.isActive() ) {
			return this.channel.writeAndFlush(message);
		}
		return this.channel.newFailedFuture(new ChannelClosedException("Channel is closed!"));
	}

	private MqttMessageIdVariableHeader getNewMessageId() {
		final int nextId = this.nextMessageId.accumulateAndGet(1, (c, d) -> {
			return (c < 0xFFFF ? c + 1 : 1);
		});
		return MqttMessageIdVariableHeader.from(nextId);
	}

	private Future<Void> createSubscription(String topic, MqttMessageHandler handler, boolean once,
			MqttQoS qos) {
		if ( this.pendingSubscribeTopics.contains(topic) ) {
			Optional<Map.Entry<Integer, MqttPendingSubscription>> subscriptionEntry = this.pendingSubscriptions
					.entrySet().stream().filter((e) -> e.getValue().getTopic().equals(topic)).findAny();
			if ( subscriptionEntry.isPresent() ) {
				subscriptionEntry.get().getValue().addHandler(handler, once);
				return subscriptionEntry.get().getValue().getFuture();
			}
		}
		if ( this.serverSubscriptions.contains(topic) ) {
			MqttSubscription subscription = new MqttSubscription(topic, handler, once);
			CopyOnWriteArrayList<MqttSubscription> l = (CopyOnWriteArrayList<MqttSubscription>) this.subscriptions
					.computeIfAbsent(topic, k -> new CopyOnWriteArrayList<>());
			l.addIfAbsent(subscription);
			l = (CopyOnWriteArrayList<MqttSubscription>) this.handlerToSubscribtion
					.computeIfAbsent(handler, k -> new CopyOnWriteArrayList<>());
			l.addIfAbsent(subscription);
			return this.channel.newSucceededFuture();
		}

		Promise<Void> future = new DefaultPromise<>(this.eventLoop.next());
		MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.SUBSCRIBE, false,
				MqttQoS.AT_LEAST_ONCE, false, 0);
		MqttTopicSubscription subscription = new MqttTopicSubscription(topic, qos);
		MqttMessageIdVariableHeader variableHeader = getNewMessageId();
		MqttSubscribePayload payload = new MqttSubscribePayload(Collections.singletonList(subscription));
		MqttSubscribeMessage message = new MqttSubscribeMessage(fixedHeader, variableHeader, payload);

		final MqttPendingSubscription pendingSubscription = new MqttPendingSubscription(future, topic,
				message);
		pendingSubscription.addHandler(handler, once);
		this.pendingSubscriptions.put(variableHeader.messageId(), pendingSubscription);
		this.pendingSubscribeTopics.add(topic);
		pendingSubscription.setSent(this.sendAndFlushPacket(message) != null); //If not sent, we will send it when the connection is opened

		pendingSubscription.startRetransmitTimer(this.eventLoop.next(), this::sendAndFlushPacket);

		return future;
	}

	private void checkSubscribtions(String topic, Promise<Void> promise) {
		if ( !(this.subscriptions.containsKey(topic) && this.subscriptions.get(topic).size() != 0)
				&& this.serverSubscriptions.contains(topic) ) {
			MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.UNSUBSCRIBE, false,
					MqttQoS.AT_LEAST_ONCE, false, 0);
			MqttMessageIdVariableHeader variableHeader = getNewMessageId();
			MqttUnsubscribePayload payload = new MqttUnsubscribePayload(
					Collections.singletonList(topic));
			MqttUnsubscribeMessage message = new MqttUnsubscribeMessage(fixedHeader, variableHeader,
					payload);

			MqttPendingUnsubscription pendingUnsubscription = new MqttPendingUnsubscription(promise,
					topic, message);
			this.pendingServerUnsubscribes.put(variableHeader.messageId(), pendingUnsubscription);
			pendingUnsubscription.startRetransmissionTimer(this.eventLoop.next(),
					this::sendAndFlushPacket);

			this.sendAndFlushPacket(message);
		} else {
			promise.setSuccess(null);
		}
	}

	IntObjectHashMap<MqttPendingSubscription> getPendingSubscriptions() {
		return pendingSubscriptions;
	}

	MultiValueMap<String, MqttSubscription> getSubscriptions() {
		return subscriptions;
	}

	Set<String> getPendingSubscribeTopics() {
		return pendingSubscribeTopics;
	}

	MultiValueMap<MqttMessageHandler, MqttSubscription> getHandlerToSubscribtion() {
		return handlerToSubscribtion;
	}

	Set<String> getServerSubscriptions() {
		return serverSubscriptions;
	}

	IntObjectHashMap<MqttPendingUnsubscription> getPendingServerUnsubscribes() {
		return pendingServerUnsubscribes;
	}

	ConcurrentMap<Integer, MqttPendingPublish> getPendingPublishes() {
		return pendingPublishes;
	}

	IntObjectHashMap<MqttIncomingQos2Publish> getQos2PendingIncomingPublishes() {
		return qos2PendingIncomingPublishes;
	}

	private class MqttChannelInitializer extends ChannelInitializer<SocketChannel> {

		private final Promise<MqttConnectResult> connectFuture;
		private final String host;
		private final int port;
		private final SslContext sslContext;

		public MqttChannelInitializer(Promise<MqttConnectResult> connectFuture, String host, int port,
				SslContext sslContext) {
			this.connectFuture = connectFuture;
			this.host = host;
			this.port = port;
			this.sslContext = sslContext;
		}

		@Override
		protected void initChannel(SocketChannel ch) throws Exception {
			if ( sslContext != null ) {
				ch.pipeline().addLast(sslContext.newHandler(ch.alloc(), host, port));
			}
			if ( wireLogging ) {
				ch.pipeline().addLast(new LoggingHandler("net.solarnetwork.mqtt." + host + ":" + port));
			}
			ch.pipeline().addLast("mqttDecoder", new MqttDecoder(clientConfig.getMaxBytesInMessage()));
			ch.pipeline().addLast("mqttEncoder", MqttEncoder.INSTANCE);

			final int timeout = MqttClientImpl.this.clientConfig.getTimeoutSeconds();
			final int readTimeout = MqttClientImpl.this.clientConfig.getReadTimeoutSeconds();
			final int writeTimeout = MqttClientImpl.this.clientConfig.getWriteTimeoutSeconds();
			if ( readTimeout != 0 || writeTimeout != 0 ) {
				ch.pipeline().addLast("idleStateHandler",
						new IdleStateHandler(
								readTimeout >= 0 ? readTimeout : timeout * READ_TIMEOUT_FACTOR,
								writeTimeout >= 0 ? writeTimeout : timeout, 0));
			}
			ch.pipeline().addLast("mqttPingHandler", new MqttPingHandler(timeout, readTimeout != 0));
			ch.pipeline().addLast("mqttHandler",
					new MqttChannelHandler(MqttClientImpl.this, connectFuture));
		}
	}

	MqttMessageHandler getDefaultHandler() {
		return defaultHandler;
	}

	@Override
	public void setWireLogging(boolean wireLogging) {
		this.wireLogging = wireLogging;
	}

	@Override
	public MqttTopicAliases getTopicAliases() {
		return clientAliases;
	}

	/**
	 * Get the "publish retransmit" toggle.
	 * 
	 * @return {@literal true} if published messages should automatically get
	 *         re-published on failure; defaults to {@literal false}
	 * @since 1.1
	 */
	public boolean isPublishRetransmit() {
		return publishRetransmit;
	}

	/**
	 * Set the "publish retransmit" toggle.
	 * 
	 * @param {@literal true} if published messages should automatically get
	 * re-published on failure
	 * @since 1.1
	 */
	public void setPublishRetransmit(boolean publishRetransmit) {
		this.publishRetransmit = publishRetransmit;
	}

	/**
	 * Get the minimum timeout to hold on to pending messages.
	 * 
	 * @return the pending abort timeout, in minutes; defaults to {@literal 60}
	 * @since 1.1
	 */
	public int getPendingAbortTimeoutMinutes() {
		return pendingAbortTimeoutMinutes;
	}

	/**
	 * Set the minimum timeout to hold on to pending messages.
	 * 
	 * @param the
	 *        pending abort timeout, in minutes
	 * @since 1.1
	 */
	public void setPendingAbortTimeoutMinutes(int pendingAbortTimeoutMinutes) {
		this.pendingAbortTimeoutMinutes = pendingAbortTimeoutMinutes;
	}

}
