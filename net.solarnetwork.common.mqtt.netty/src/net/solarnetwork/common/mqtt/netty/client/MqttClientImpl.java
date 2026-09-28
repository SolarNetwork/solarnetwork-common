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

import static net.solarnetwork.util.ObjectUtils.requireNonNullArgument;
import static net.solarnetwork.util.ObjectUtils.requireNonNullProperty;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
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
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttProperties;
import io.netty.handler.codec.mqtt.MqttProperties.MqttProperty;
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
 * <p>
 * All the connection state maintained here is mutated both from the Netty event
 * loop (when server packets arrive, and when the channel closes) and from
 * application threads (when {@link #on(String, MqttMessageHandler, MqttQoS)},
 * {@link #off(String, MqttMessageHandler)}, or
 * {@link #publish(String, ByteBuf, MqttQoS, boolean, net.solarnetwork.common.mqtt.MqttProperties)}
 * are called), so it is held in concurrent collections.
 * </p>
 *
 * @version 1.4
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

	/**
	 * The maximum number of seconds to wait for a {@literal DISCONNECT} message
	 * to be written before forcing the channel closed.
	 *
	 * @since 1.4
	 */
	public static final int DISCONNECT_TIMEOUT_SECS = 5;

	private static final Logger log = LoggerFactory.getLogger(MqttClientImpl.class);

	private final Set<String> serverSubscriptions = ConcurrentHashMap.newKeySet();
	private final ConcurrentMap<Integer, MqttPendingUnsubscription> pendingServerUnsubscribes = new ConcurrentHashMap<>(
			8, 0.7f, 2);
	private final ConcurrentMap<Integer, MqttIncomingQos2Publish> qos2PendingIncomingPublishes = new ConcurrentHashMap<>(
			8, 0.7f, 2);
	private final ConcurrentMap<Integer, MqttPendingPublish> pendingPublishes = new ConcurrentHashMap<>(
			16, 0.7f, 2);
	private final ConcurrentMap<String, List<MqttSubscription>> subscriptions = new ConcurrentHashMap<>(
			8, 0.7f, 2);
	private final ConcurrentMap<Integer, MqttPendingSubscription> pendingSubscriptions = new ConcurrentHashMap<>(
			8, 0.7f, 2);
	private final Set<String> pendingSubscribeTopics = ConcurrentHashMap.newKeySet();
	private final ConcurrentMap<MqttMessageHandler, List<MqttSubscription>> handlerToSubscription = new ConcurrentHashMap<>(
			8, 0.7f, 2);
	private final AtomicInteger nextMessageId = new AtomicInteger(0);
	private final MqttTopicAliases clientAliases = new BasicMqttTopicAliases(0);

	private final MqttClientConfig clientConfig;

	private final @Nullable MqttMessageHandler defaultHandler;

	private volatile @Nullable EventLoopGroup eventLoop;

	private volatile @Nullable Channel channel;

	private volatile boolean disconnected = false;
	private volatile boolean reconnect = false;
	private volatile boolean wireLogging = false;
	private volatile @Nullable String host;
	private volatile int port;
	private volatile @Nullable MqttClientCallback callback;
	private volatile boolean publishRetransmit = false;
	private volatile int pendingAbortTimeoutMinutes = 60;

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
	 * @param defaultHandler
	 *        an optional default handler
	 * @throws IllegalArgumentException
	 *         if {@code clientConfig} is {@code null}
	 */
	public MqttClientImpl(MqttClientConfig clientConfig, @Nullable MqttMessageHandler defaultHandler) {
		this.clientConfig = requireNonNullArgument(clientConfig, "clientConfig");
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

	private synchronized EventLoopGroup getOrCreateEventLoop() {
		final EventLoopGroup loop = getEventLoop();
		if ( loop != null ) {
			return loop;
		}
		EventLoopGroup el = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
		setEventLoop(el);
		return el;
	}

	private Future<MqttConnectResult> connect(String host, int port, boolean reconnect) {
		final EventLoopGroup eventLoop = getOrCreateEventLoop();
		this.host = host;
		this.port = port;
		Promise<MqttConnectResult> connectFuture = new DefaultPromise<>(eventLoop.next());
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.group(eventLoop);
		bootstrap.channel(clientConfig.getChannelClass());
		bootstrap.remoteAddress(host, port);
		bootstrap.handler(
				new MqttChannelInitializer(connectFuture, host, port, clientConfig.getSslContext()));
		ChannelFuture future = bootstrap.connect();

		future.addListener((ChannelFutureListener) f -> {
			if ( f.isSuccess() ) {
				final Channel ch = f.channel();
				MqttClientImpl.this.channel = ch;
				ch.closeFuture().addListener((ChannelFutureListener) channelFuture -> {
					final ChannelClosedException e = new ChannelClosedException("Channel is closed!");
					try {
						channelClosed(e);
					} catch ( RuntimeException t ) {
						log.warn("Error releasing state of closed MQTT connection {}: {}",
								getServerUri(), t.toString(), t);
					}

					// only report the loss if we did not close the connection ourselves; note
					// isConnected() can never be true here, as the channel has just closed
					final MqttClientCallback cb = callback;
					if ( cb != null && !isDisconnected() ) {
						try {
							cb.connectionLost(e);
						} catch ( Throwable t ) {
							// ignore
						}
					}
					scheduleConnectIfRequired(host, port, true);
				});
			} else {
				scheduleConnectIfRequired(host, port, reconnect);
			}
		});
		return connectFuture;
	}

	/**
	 * Release all connection-scoped state after the channel has closed.
	 *
	 * <p>
	 * Any in-flight operation is settled here, rather than simply discarded, so
	 * callers waiting on a publish, subscribe, or unsubscribe future are not
	 * left waiting for a response that can never arrive. All retransmission
	 * timers are stopped, and all retained publish payloads released.
	 * </p>
	 *
	 * @param cause
	 *        the reason the channel closed
	 * @since 1.4
	 */
	private void channelClosed(Throwable cause) {
		for ( Iterator<MqttPendingSubscription> itr = pendingSubscriptions.values().iterator(); itr
				.hasNext(); ) {
			MqttPendingSubscription pending = itr.next();
			itr.remove();
			pending.stop();
			pending.getFuture().tryFailure(cause);
		}
		for ( Iterator<MqttPendingUnsubscription> itr = pendingServerUnsubscribes.values().iterator(); itr
				.hasNext(); ) {
			MqttPendingUnsubscription pending = itr.next();
			itr.remove();
			pending.stop();
			pending.getFuture().tryFailure(cause);
		}
		for ( Iterator<MqttPendingPublish> itr = pendingPublishes.values().iterator(); itr.hasNext(); ) {
			MqttPendingPublish pending = itr.next();
			itr.remove();
			pending.stop();
			pending.getFuture().tryFailure(cause);
			pending.releasePayload();
		}
		for ( Iterator<MqttIncomingQos2Publish> itr = qos2PendingIncomingPublishes.values()
				.iterator(); itr.hasNext(); ) {
			MqttIncomingQos2Publish pending = itr.next();
			itr.remove();
			pending.stop();
			final ByteBuf payload = pending.getIncomingPublish().payload();
			if ( payload.refCnt() > 0 ) {
				payload.release();
			}
		}
		serverSubscriptions.clear();
		subscriptions.clear();
		pendingSubscribeTopics.clear();
		handlerToSubscription.clear();
		clientAliases.setMaximumAliasCount(0); // also clears
	}

	private void scheduleConnectIfRequired(String host, int port, boolean reconnect) {
		if ( clientConfig.isReconnect() && !disconnected ) {
			if ( reconnect ) {
				this.reconnect = true;
			}
			final EventLoopGroup eventLoop = getEventLoop();
			if ( eventLoop != null ) {
				eventLoop.schedule((Runnable) () -> connect(host, port, reconnect),
						clientConfig.getReconnectDelay(), TimeUnit.SECONDS);
			}
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
				itr.remove();
				pending.stop();
				pending.getFuture().tryFailure(new TimeoutException(
						"Failed to publish message within " + timeoutMins + " minutes"));
				pending.releasePayload();
			}
		}
	}

	@Override
	public @Nullable URI getServerUri() {
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
	public @Nullable EventLoopGroup getEventLoop() {
		return eventLoop;
	}

	@Override
	public void setEventLoop(EventLoopGroup eventLoop) {
		this.eventLoop = requireNonNullArgument(eventLoop, "eventLoop");
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
		final EventLoopGroup eventLoop = requireEventLoop();
		Promise<Void> future = new DefaultPromise<>(eventLoop.next());
		List<MqttSubscription> subs = this.handlerToSubscription.get(handler);
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
		this.handlerToSubscription.computeIfPresent(handler, (k, v) -> {
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
		final EventLoopGroup eventLoop = requireEventLoop();
		Promise<Void> future = new DefaultPromise<>(eventLoop.next());
		final List<MqttSubscription> topicSubs = this.subscriptions.get(topic);
		Set<MqttSubscription> subscriptions = (topicSubs != null ? new LinkedHashSet<>(topicSubs)
				: Collections.emptySet());
		for ( MqttSubscription subscription : subscriptions ) {
			final List<MqttSubscription> handSubs = this.handlerToSubscription
					.get(subscription.getHandler());
			if ( handSubs != null ) {
				for ( MqttSubscription handSub : handSubs ) {
					this.subscriptions.computeIfPresent(topic, (k, v) -> {
						if ( v != null ) {
							v.remove(handSub);
						}
						return v;
					});
				}
			}
			this.handlerToSubscription.computeIfPresent(subscription.getHandler(), (k, v) -> {
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
			net.solarnetwork.common.mqtt.@Nullable MqttProperties properties) {
		Promise<Void> future = new DefaultPromise<>(requireEventLoop().next());
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
				p.add(new MqttProperties.IntegerProperty(MqttProperties.TOPIC_ALIAS, a));
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
			if ( channelFuture.cause() != null ) {
				// the channel was not active, so the message never reached the encoder and
				// nothing has released the payload on its behalf: leave `sent` false so both
				// outstanding references are released here
				this.pendingPublishes.remove(pendingPublish.getMessageId());
				future.tryFailure(channelFuture.cause());
				pendingPublish.releasePayload();
				return future;
			}
			pendingPublish.setSent(true);
		}
		if ( pendingPublish.isSent() && pendingPublish.getQos() == MqttQoS.AT_MOST_ONCE ) {
			this.pendingPublishes.remove(pendingPublish.getMessageId());
			pendingPublish.getFuture().trySuccess(null); //We don't get an ACK for QOS 0
			pendingPublish.releasePayload();
		} else if ( pendingPublish.isSent() && retransmit ) {
			pendingPublish.startPublishRetransmissionTimer(requireEventLoop().next(),
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
	public CompletableFuture<Void> disconnect() {
		disconnected = true;
		CompletableFuture<Void> result = new CompletableFuture<>();
		final Channel ch = this.channel;
		if ( ch == null ) {
			result.complete(null);
			return result;
		}
		this.reconnect = false;

		// the result completes when the channel actually closes, however that comes about
		ch.closeFuture().addListener((ChannelFutureListener) closeFuture -> {
			if ( closeFuture.isSuccess() ) {
				result.complete(null);
			} else {
				result.completeExceptionally(closeFuture.cause());
			}
		});

		MqttMessage message = new MqttMessage(
				new MqttFixedHeader(MqttMessageType.DISCONNECT, false, MqttQoS.AT_MOST_ONCE, false, 0));
		final ChannelFuture cf = this.sendAndFlushPacket(ch, message);
		if ( cf == null ) {
			ch.close();
			return result;
		}
		cf.addListener((ChannelFutureListener) f -> ch.close());

		// a DISCONNECT written to a half-open connection, or to a TLS channel whose handshake
		// never completed, can stay pending forever; never let that hold the channel open
		try {
			ch.eventLoop().schedule(() -> {
				if ( ch.isOpen() ) {
					log.info("Timeout sending DISCONNECT to MQTT server {}; closing channel.",
							getServerUri());
					ch.close();
				}
			}, DISCONNECT_TIMEOUT_SECS, TimeUnit.SECONDS);
		} catch ( RejectedExecutionException e ) {
			// event loop already shutting down; it will close the channel itself
		}
		return result;
	}

	@Override
	public boolean isDisconnected() {
		return disconnected == true;
	}

	@Override
	public void setCallback(@Nullable MqttClientCallback callback) {
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

	@Nullable
	ChannelFuture sendAndFlushPacket(final Object message) {
		return sendAndFlushPacket(this.channel, message);
	}

	@Nullable
	ChannelFuture sendAndFlushPacket(final @Nullable Channel ch, final Object message) {
		if ( ch == null ) {
			return null;
		}
		if ( ch.isActive() ) {
			return ch.writeAndFlush(message);
		}
		return ch.newFailedFuture(new ChannelClosedException("Channel is closed!"));
	}

	private MqttMessageIdVariableHeader getNewMessageId() {
		final int nextId = this.nextMessageId.accumulateAndGet(1, (c, d) -> {
			return (c < 0xFFFF ? c + 1 : 1);
		});
		return MqttMessageIdVariableHeader.from(nextId);
	}

	private Channel requireChannel() {
		return requireNonNullProperty(this.channel, "Channel");
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
			l = (CopyOnWriteArrayList<MqttSubscription>) this.handlerToSubscription
					.computeIfAbsent(handler, k -> new CopyOnWriteArrayList<>());
			l.addIfAbsent(subscription);
			return requireChannel().newSucceededFuture();
		}

		Promise<Void> future = new DefaultPromise<>(requireEventLoop().next());
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

		pendingSubscription.startRetransmitTimer(requireEventLoop().next(), this::sendAndFlushPacket);

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
			pendingUnsubscription.startRetransmissionTimer(requireEventLoop().next(),
					this::sendAndFlushPacket);

			this.sendAndFlushPacket(message);
		} else {
			promise.setSuccess(null);
		}
	}

	ConcurrentMap<Integer, MqttPendingSubscription> getPendingSubscriptions() {
		return pendingSubscriptions;
	}

	ConcurrentMap<String, List<MqttSubscription>> getSubscriptions() {
		return subscriptions;
	}

	Set<String> getPendingSubscribeTopics() {
		return pendingSubscribeTopics;
	}

	ConcurrentMap<MqttMessageHandler, List<MqttSubscription>> getHandlerToSubscription() {
		return handlerToSubscription;
	}

	Set<String> getServerSubscriptions() {
		return serverSubscriptions;
	}

	ConcurrentMap<Integer, MqttPendingUnsubscription> getPendingServerUnsubscribes() {
		return pendingServerUnsubscribes;
	}

	ConcurrentMap<Integer, MqttPendingPublish> getPendingPublishes() {
		return pendingPublishes;
	}

	ConcurrentMap<Integer, MqttIncomingQos2Publish> getQos2PendingIncomingPublishes() {
		return qos2PendingIncomingPublishes;
	}

	private class MqttChannelInitializer extends ChannelInitializer<SocketChannel> {

		private final Promise<MqttConnectResult> connectFuture;
		private final String host;
		private final int port;
		private final @Nullable SslContext sslContext;

		public MqttChannelInitializer(Promise<MqttConnectResult> connectFuture, String host, int port,
				@Nullable SslContext sslContext) {
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

	@Nullable
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
