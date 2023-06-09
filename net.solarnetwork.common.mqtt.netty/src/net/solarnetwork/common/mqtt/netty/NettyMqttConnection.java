/* ==================================================================
 * NettyMqttConnection.java - 25/11/2019 7:27:40 am
 * 
 * Copyright 2019 SolarNetwork.net Dev Team
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation; either version 2 of 
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
 * 02111-1307 USA
 * ==================================================================
 */

package net.solarnetwork.common.mqtt.netty;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import io.netty.buffer.Unpooled;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttVersion;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.util.concurrent.GenericFutureListener;
import net.solarnetwork.common.mqtt.BaseMqttConnection;
import net.solarnetwork.common.mqtt.BasicMqttConnectionConfig;
import net.solarnetwork.common.mqtt.MqttConnectReturnCode;
import net.solarnetwork.common.mqtt.MqttConnection;
import net.solarnetwork.common.mqtt.MqttConnectionConfig;
import net.solarnetwork.common.mqtt.MqttConnectionObserver;
import net.solarnetwork.common.mqtt.MqttMessage;
import net.solarnetwork.common.mqtt.MqttMessageHandler;
import net.solarnetwork.common.mqtt.MqttProperty;
import net.solarnetwork.common.mqtt.MqttPropertyType;
import net.solarnetwork.common.mqtt.MqttQos;
import net.solarnetwork.common.mqtt.MqttStats;
import net.solarnetwork.common.mqtt.MqttUtils;
import net.solarnetwork.common.mqtt.WireLoggingSupport;
import net.solarnetwork.common.mqtt.netty.client.ChannelClosedException;
import net.solarnetwork.common.mqtt.netty.client.MqttClient;
import net.solarnetwork.common.mqtt.netty.client.MqttClientCallback;
import net.solarnetwork.common.mqtt.netty.client.MqttClientConfig;
import net.solarnetwork.common.mqtt.netty.client.MqttConnectResult;
import net.solarnetwork.common.mqtt.netty.client.MqttLastWill;
import net.solarnetwork.service.CertificateException;
import net.solarnetwork.service.SSLService;

/**
 * Netty based implementation of {@link MqttConnection}.
 * 
 * @author matt
 * @version 2.1
 */
public class NettyMqttConnection extends BaseMqttConnection
		implements MqttMessageHandler, MqttClientCallback, WireLoggingSupport {

	/** The {@code ioThreadCount} property default value. */
	public static final int DEFAULT_IO_THREAD_COUNT = 2;

	/** The {@code wireLogging} property default value. */
	public static final boolean DEFAULT_WIRE_LOGGING = false;

	private int ioThreadCount = DEFAULT_IO_THREAD_COUNT;
	private boolean wireLogging = DEFAULT_WIRE_LOGGING;

	private volatile MqttClient client;

	/**
	 * Constructor.
	 * 
	 * @param executor
	 *        the executor to use
	 * @param scheduler
	 *        the scheduler to use
	 */
	public NettyMqttConnection(Executor executor, TaskScheduler scheduler) {
		this(executor, scheduler, new BasicMqttConnectionConfig());
	}

	/**
	 * Constructor.
	 * 
	 * @param executor
	 *        the executor to use
	 * @param scheduler
	 *        the scheduler to use
	 * @param connectionConfig
	 *        the config to use
	 */
	public NettyMqttConnection(Executor executor, TaskScheduler scheduler,
			MqttConnectionConfig connectionConfig) {
		super(executor, scheduler, connectionConfig);
		this.ioThreadCount = DEFAULT_IO_THREAD_COUNT;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder("NettyMqttConnection{");
		BasicMqttConnectionConfig config = getConnectionConfig();
		if ( config != null ) {
			buf.append("uid=");
			buf.append(config.getUid());
			buf.append(", clientId=");
			buf.append(config.getClientId());
			if ( config.getUsername() != null ) {
				buf.append(", username=");
				buf.append(config.getUsername());
			}
			buf.append(", uri=");
			buf.append(config.getServerUri());
		}
		buf.append('}');
		return buf.toString();
	}

	private final class ConnectTask implements Runnable {

		private final CompletableFuture<MqttConnectReturnCode> connectFuture;
		private final ConnectScheduledTask scheduledTask;
		private long reconnectDelay = 0;

		private ConnectTask(CompletableFuture<MqttConnectReturnCode> connectFuture,
				ConnectScheduledTask scheduledTask) {
			super();
			this.connectFuture = connectFuture;
			this.scheduledTask = scheduledTask;
		}

		@Override
		public void run() {
			synchronized ( NettyMqttConnection.this ) {
				if ( isClosed() || connectFuture != connectFuture() ) {
					connectFuture.completeExceptionally(new RuntimeException("Connect cancelled."));
					return;
				}
			}
			if ( reconnectDelay < connectionConfig.getReconnectDelaySeconds() * 30000L ) {
				int step = (Math.max(1, connectionConfig.getReconnectDelaySeconds() / 2));
				reconnectDelay += (step * 1000L);
			}
			Throwable t = null;
			MqttConnectResult r = null;
			MqttClientConfig config = null;
			try {
				config = createClientConfig(connectionConfig);
			} catch ( RuntimeException e ) {
				log.warn("Invalid {} MQTT configuration: {}", getUid(), e.toString(), e);
				t = e;
			}
			if ( config != null ) {
				MqttClient client = null;
				MqttStats s = connectionConfig.getStats();
				try {
					client = MqttClient.create(config, NettyMqttConnection.this);
					client.setWireLogging(wireLogging || connectionConfig.isWireLoggingEnabled());
					client.setCallback(NettyMqttConnection.this);
					client.setEventLoop(new NioEventLoopGroup(ioThreadCount,
							new CustomizableThreadFactory("MQTT-" + getUid() + "-")));
					if ( s != null ) {
						s.incrementAndGet(MqttStats.BasicCounts.ConnectionAttempts);
					}
					log.info("Connecting to MQTT server {}...", connectionConfig.getServerUri());
					Future<MqttConnectResult> f = client.connect(connectionConfig.getHost(),
							connectionConfig.getPort());
					r = f.get(connectionConfig.getConnectTimeoutSeconds(), TimeUnit.SECONDS);
					if ( r.isSuccess() ) {
						log.info("Connected to MQTT server {}", connectionConfig.getServerUri());
						connectComplete(client, r, null);
						return;
					}
					t = new RuntimeException("Server refused connection: " + r.getReturnCode());
				} catch ( Exception e ) {
					t = e;
					if ( client != null ) {
						try {
							closeClient(client).get(connectionConfig.getConnectTimeoutSeconds(),
									TimeUnit.SECONDS);
						} catch ( Exception e2 ) {
							// ignore
						}
					}
				}
				if ( s != null ) {
					s.incrementAndGet(MqttStats.BasicCounts.ConnectionFail);
				}
				if ( connectionConfig.isReconnect() ) {
					log.info("Failed to connect to MQTT server {} ({}), will try again in {}s",
							connectionConfig.getServerUri(),
							t instanceof TimeoutException ? "timeout" : t.getMessage(),
							String.format("%.01f", (reconnectDelay / 1000.0)));
				} else {
					log.info("Failed to connect to MQTT server {} (), will not try again.",
							connectionConfig.getServerUri(),
							t instanceof TimeoutException ? "timeout" : t.getMessage());
				}
			} else {
				log.info("{} MQTT configuration incomplete, will not connect.", getUid());
			}
			if ( connectionConfig.isReconnect() && config != null ) {
				scheduler.schedule(scheduledTask, new Date(System.currentTimeMillis() + reconnectDelay));
			} else {
				connectComplete(null, r, t);
			}
		}

		private void connectComplete(MqttClient client, MqttConnectResult result, Throwable t) {
			synchronized ( NettyMqttConnection.this ) {
				NettyMqttConnection.this.client = client;
				if ( connectFuture != null ) {
					if ( t != null ) {
						connectFuture.completeExceptionally(t);
					} else {
						MqttConnectReturnCode code = result != null ? returnCode(result.getReturnCode())
								: null;
						connectFuture.complete(code);
						MqttStats s = connectionConfig.getStats();
						if ( s != null ) {
							s.incrementAndGet(MqttStats.BasicCounts.ConnectionSuccess);
						}
						MqttConnectionObserver observer = NettyMqttConnection.this.connectionObserver;
						if ( observer != null ) {
							executor.execute(new ConnectionEstablishedTask(false, observer));
						}
					}
				}
			}
		}
	}

	private MqttConnectReturnCode returnCode(io.netty.handler.codec.mqtt.MqttConnectReturnCode other) {
		if ( other == null ) {
			return null;
		}
		switch (other) {
			case CONNECTION_ACCEPTED:
				return MqttConnectReturnCode.Accepted;

			case CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD:
				return MqttConnectReturnCode.BadCredentials;

			case CONNECTION_REFUSED_IDENTIFIER_REJECTED:
				return MqttConnectReturnCode.ClientIdRejected;

			case CONNECTION_REFUSED_NOT_AUTHORIZED:
				return MqttConnectReturnCode.NotAuthorized;

			case CONNECTION_REFUSED_SERVER_UNAVAILABLE:
				return MqttConnectReturnCode.ServerUnavailable;

			case CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION:
				return MqttConnectReturnCode.UnacceptableProtocolVersion;

			default:
				return null;
		}

	}

	// this task runs in the TaskScheduler, which we don't want to block
	private final class ConnectScheduledTask implements Runnable {

		private final ConnectTask task;

		private ConnectScheduledTask(CompletableFuture<MqttConnectReturnCode> future) {
			super();
			this.task = new ConnectTask(future, this);
		}

		@Override
		public void run() {
			executor.execute(task);
		}

	}

	@Override
	protected Runnable createConnectScheduledTask(CompletableFuture<MqttConnectReturnCode> future) {
		return new ConnectScheduledTask(future);
	}

	private MqttClientConfig createClientConfig(final MqttConnectionConfig connConfig) {
		if ( connConfig == null ) {
			return null;
		}

		if ( connConfig.getServerUri() == null || connConfig.getClientId() == null
				|| connConfig.getClientId().isEmpty() ) {
			log.info("Server URI and/or client ID not configured, cannot connect to MQTT server.");
			return null;
		}

		final MqttClientConfig config;
		if ( connConfig.isUseSsl() ) {
			config = new MqttClientConfig(createSslContext(connConfig.getSslService()));
		} else {
			config = new MqttClientConfig();
		}
		config.setCleanSession(connConfig.isCleanSession());
		config.setClientId(connConfig.getClientId());
		if ( connConfig.getLastWill() != null ) {
			MqttMessage msg = connConfig.getLastWill();
			MqttLastWill lwt = new MqttLastWill(msg.getTopic(),
					new String(msg.getPayload(), Charset.forName("UTF-8")), msg.isRetained(),
					MqttQoS.valueOf(msg.getQosLevel().getValue()));
			config.setLastWill(lwt);
		}
		config.setMaxBytesInMessage(connConfig.getMaximumMessageSize());
		config.setPassword(connConfig.getPassword());
		switch (connConfig.getVersion()) {
			case Mqtt31:
				config.setProtocolVersion(MqttVersion.MQTT_3_1);
				break;

			case Mqtt5:
				config.setProtocolVersion(MqttVersion.MQTT_5);
				MqttProperty<Integer> maxTopicAliases = connConfig
						.getProperty(MqttPropertyType.TOPIC_ALIAS_MAXIMUM);
				if ( maxTopicAliases != null && maxTopicAliases.getValue() != null ) {
					config.setMaximumTopicAliases(maxTopicAliases.getValue().intValue());
				}
				break;

			default:
				config.setProtocolVersion(MqttVersion.MQTT_3_1_1);
		}
		config.setReconnect(false); // only switch AFTER connect
		config.setReconnectDelay(connConfig.getReconnectDelaySeconds());
		config.setTimeoutSeconds(connConfig.getKeepAliveSeconds());
		config.setReadTimeoutSeconds(connConfig.getReadTimeoutSeconds());
		config.setWriteTimeoutSeconds(connConfig.getWriteTimeoutSeconds());
		config.setUsername(connConfig.getUsername());
		return config;
	}

	private SslContext createSslContext(SSLService sslService) {
		try {
			SslContextBuilder builder = SslContextBuilder.forClient();
			if ( sslService != null ) {
				TrustManagerFactory tmf = sslService.getTrustManagerFactory();
				if ( tmf != null ) {
					builder.trustManager(tmf);
				}
				KeyManagerFactory kmf = sslService.getKeyManagerFactory();
				if ( kmf != null ) {
					builder.keyManager(kmf);
				}
			}
			return builder.build();
		} catch ( SSLException e ) {
			throw new CertificateException(
					"Error configuring SSL for MQTT connection: " + e.getMessage(), e);
		}
	}

	private Future<?> closeClient(final MqttClient c) {
		CompletableFuture<Void> result = new CompletableFuture<>();
		executor.execute(new Runnable() {

			@Override
			public void run() {
				try {
					c.disconnect().get(connectionConfig.getConnectTimeoutSeconds(), TimeUnit.SECONDS);
					result.complete(null);
				} catch ( Exception e ) {
					result.completeExceptionally(e);
				} finally {
					EventLoopGroup g = c.getEventLoop();
					if ( g != null ) {
						g.shutdownGracefully();
					}
				}
			}
		});
		return result;
	}

	@Override
	protected synchronized Future<?> closeConnection() {
		final MqttClient c = this.client;
		if ( c != null ) {
			try {
				return closeClient(c);
			} finally {
				client = null;
			}
		}
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public void connectionLost(Throwable cause) {
		String msg = (cause instanceof ChannelClosedException ? "closed"
				: cause != null ? cause.toString() : "unknown cause");
		log.warn("Connection lost to MQTT server {}: {}", connectionConfig.getServerUri(), msg);
		MqttStats s = connectionConfig.getStats();
		if ( s != null ) {
			s.incrementAndGet(MqttStats.BasicCounts.ConnectionLost);
		}
		MqttConnectionObserver observer = this.connectionObserver;
		if ( observer != null ) {
			// bump to another thread so MQTT processing not affected by observer execution time
			executor.execute(new ConnectionLostTask(cause, observer));
		}
		if ( !isClosed() && connectionConfig.isReconnect() ) {
			log.info("Resetting connection to MQTT server {} to schedule reconnect",
					connectionConfig.getServerUri());
			reconfigure();
		}
	}

	private final class ConnectionLostTask implements Runnable {

		private final Throwable cause;
		private final MqttConnectionObserver observer;

		private ConnectionLostTask(Throwable cause, MqttConnectionObserver observer) {
			this.cause = cause;
			this.observer = observer;
		}

		@Override
		public void run() {
			try {
				observer.onMqttServerConnectionLost(NettyMqttConnection.this,
						connectionConfig.isReconnect(), cause);
			} catch ( Throwable t ) {
				Throwable root = t;
				while ( root.getCause() != null ) {
					root = root.getCause();
				}
				log.error("Unhandled {} exception on connection loss observer {}",
						root.getClass().getSimpleName(), observer, t);
			}
		}

	}

	@Override
	public void onSuccessfulReconnect() {
		log.warn("Reconnected to MQTT server {}", connectionConfig.getServerUri());
		MqttStats s = connectionConfig.getStats();
		if ( s != null ) {
			s.incrementAndGet(MqttStats.BasicCounts.ConnectionSuccess);
		}
		MqttConnectionObserver observer = this.connectionObserver;
		if ( observer != null ) {
			// bump to another thread so MQTT processing not affected by observer execution time
			executor.execute(new ConnectionEstablishedTask(true, observer));
		}
	}

	private final class ConnectionEstablishedTask implements Runnable {

		private final boolean reconnected;
		private final MqttConnectionObserver observer;

		private ConnectionEstablishedTask(boolean reconnected, MqttConnectionObserver observer) {
			this.reconnected = reconnected;
			this.observer = observer;
		}

		@Override
		public void run() {
			try {
				observer.onMqttServerConnectionEstablished(NettyMqttConnection.this, reconnected);
			} catch ( Throwable t ) {
				Throwable root = t;
				while ( root.getCause() != null ) {
					root = root.getCause();
				}
				log.error("Unhandled {} exception on connection establishment observer {}",
						root.getClass().getSimpleName(), observer, t);
			}
		}

	}

	@Override
	public boolean isEstablished() {
		final MqttClient c;
		synchronized ( this ) {
			if ( isClosed() ) {
				return false;
			}
			c = this.client;
		}
		if ( c == null ) {
			return false;
		}
		synchronized ( c ) {
			return c.isConnected();
		}
	}

	private final class MessageHandlerTask implements Runnable {

		private final MqttMessage message;
		private final MqttMessageHandler handler;

		private MessageHandlerTask(MqttMessage message, MqttMessageHandler handler) {
			this.message = message;
			this.handler = handler;
		}

		@Override
		public void run() {
			try {
				handler.onMqttMessage(message);
			} catch ( Exception e ) {
				Throwable root = e;
				while ( root.getCause() != null ) {
					root = root.getCause();
				}
				log.error("Unhandled exception in MQTT message handler {} on topic {}: {}", handler,
						message.getTopic(), root.getMessage(), e);
			}
		}

	}

	@Override
	public void onMqttMessage(MqttMessage message) {
		MqttStats s = connectionConfig.getStats();
		if ( s != null && message != null ) {
			s.incrementAndGet(MqttStats.BasicCounts.MessagesReceived);
			byte[] payload = message.getPayload();
			if ( payload != null && payload.length > 0 ) {
				s.addAndGet(MqttStats.BasicCounts.PayloadBytesReceived, payload.length);
			}
		}
		MqttMessageHandler handler = this.messageHandler;
		if ( handler != null ) {
			// bump to another thread so MQTT processing not affected by handler execution time
			executor.execute(new MessageHandlerTask(message, handler));
		}
	}

	@Override
	public Future<?> publish(MqttMessage message) {
		if ( message == null ) {
			return CompletableFuture.completedFuture(null);
		}
		try {
			MqttUtils.validateTopicName(message.getTopic(), getConnectionConfig().getVersion());
		} catch ( IllegalArgumentException e ) {
			CompletableFuture<Void> f = new CompletableFuture<>();
			f.completeExceptionally(e);
			return f;
		}

		MqttClient c = this.client;
		if ( c == null ) {
			CompletableFuture<Void> f = new CompletableFuture<>();
			f.completeExceptionally(new IOException("Not connected to MQTT server."));
			return f;
		}
		final byte[] payload = message.getPayload();
		io.netty.util.concurrent.Future<Void> f = c.publish(message.getTopic(),
				Unpooled.wrappedBuffer(payload), NettyMqttUtils.qos(message.getQosLevel()),
				message.isRetained(), message.getProperties());

		final MqttStats s = connectionConfig.getStats();
		if ( s != null ) {
			f.addListener(new GenericFutureListener<io.netty.util.concurrent.Future<? super Void>>() {

				@Override
				public void operationComplete(io.netty.util.concurrent.Future<? super Void> future)
						throws Exception {
					if ( future.isSuccess() ) {
						s.incrementAndGet(MqttStats.BasicCounts.MessagesDelivered);
						if ( payload != null && payload.length > 0 ) {
							s.addAndGet(MqttStats.BasicCounts.PayloadBytesDelivered, payload.length);
						}
					} else {
						s.incrementAndGet(MqttStats.BasicCounts.MessagesDeliveredFail);
					}
				}
			});
		}

		return f;
	}

	private final class StatsMessageHandler implements MqttMessageHandler {

		private final MqttMessageHandler delegate;

		private StatsMessageHandler(MqttMessageHandler delegate) {
			super();
			this.delegate = delegate;
		}

		@Override
		public void onMqttMessage(MqttMessage message) {
			MqttStats s = connectionConfig.getStats();
			if ( s != null ) {
				s.incrementAndGet(MqttStats.BasicCounts.MessagesReceived);
			}
			// bump to another thread so MQTT processing not affected by handler execution time
			executor.execute(new MessageHandlerTask(message, delegate));
		}

		// hashCode & equals are funny here so that the delegate is used
		// and when subscribe/unsubscribe are called everything works out

		@Override
		public int hashCode() {
			return delegate.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if ( obj instanceof StatsMessageHandler ) {
				obj = ((StatsMessageHandler) obj).delegate;
			}
			return delegate.equals(obj);
		}

	}

	@Override
	public Future<?> subscribe(String topic, MqttQos qosLevel, MqttMessageHandler handler) {
		MqttClient c = this.client;
		if ( c == null ) {
			CompletableFuture<Void> f = new CompletableFuture<>();
			f.completeExceptionally(new IOException("Not connected to MQTT server."));
			return f;
		}
		return c.on(topic, handler != null ? new StatsMessageHandler(handler) : this,
				NettyMqttUtils.qos(qosLevel));
	}

	@Override
	public Future<?> unsubscribe(String topic, MqttMessageHandler handler) {
		MqttClient c = this.client;
		if ( c == null ) {
			CompletableFuture<Void> f = new CompletableFuture<>();
			f.completeExceptionally(new IOException("Not connected to MQTT server."));
			return f;
		}
		// must wrap `handler` with StatsMessageHandler so equals() works with what
		// was passed to c.on() in the #subscribe() method above
		return c.off(topic, handler != null ? new StatsMessageHandler(handler) : this);
	}

	/*---------------------
	 * Accessors
	 *------------------ */

	/**
	 * Get the IO thread count.
	 * 
	 * @return the number of IO threads to manage; defaults to
	 *         {@link #DEFAULT_IO_THREAD_COUNT}
	 */
	public int getIoThreadCount() {
		return ioThreadCount;
	}

	/**
	 * Set the IO thread count.
	 * 
	 * @param ioThreadCount
	 *        the count to set
	 * @throws IllegalArgumentException
	 *         if {@code ioThreadCount} is less than {@literal 0}
	 */
	public void setIoThreadCount(int ioThreadCount) {
		if ( ioThreadCount < 0 ) {
			throw new IllegalArgumentException("The ioThreadCount value must be >= 0");
		}
		this.ioThreadCount = ioThreadCount;
	}

	@Override
	public boolean isWireLoggingEnabled() {
		return wireLogging;
	}

	@Override
	public void setWireLoggingEnabled(boolean wireLogging) {
		this.wireLogging = wireLogging;
	}

}
