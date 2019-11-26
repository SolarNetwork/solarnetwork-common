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
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import net.solarnetwork.common.mqtt.BasicMqttConnectionConfig;
import net.solarnetwork.common.mqtt.MqttConnectReturnCode;
import net.solarnetwork.common.mqtt.MqttConnection;
import net.solarnetwork.common.mqtt.MqttConnectionConfig;
import net.solarnetwork.common.mqtt.MqttConnectionObserver;
import net.solarnetwork.common.mqtt.MqttMessage;
import net.solarnetwork.common.mqtt.MqttMessageHandler;
import net.solarnetwork.common.mqtt.MqttQos;
import net.solarnetwork.common.mqtt.MqttStats;
import net.solarnetwork.common.mqtt.netty.client.MqttClient;
import net.solarnetwork.common.mqtt.netty.client.MqttClientCallback;
import net.solarnetwork.common.mqtt.netty.client.MqttClientConfig;
import net.solarnetwork.common.mqtt.netty.client.MqttConnectResult;
import net.solarnetwork.common.mqtt.netty.client.MqttLastWill;
import net.solarnetwork.domain.Identifiable;
import net.solarnetwork.domain.PingTest;
import net.solarnetwork.domain.PingTestResult;
import net.solarnetwork.settings.SettingsChangeObserver;
import net.solarnetwork.support.BasicIdentifiable;
import net.solarnetwork.support.CertificateException;
import net.solarnetwork.support.SSLService;

/**
 * Netty based implementation of {@link MqttConnection}.
 * 
 * @author matt
 * @version 1.0
 */
public class NettyMqttConnection extends BasicIdentifiable implements MqttConnection, MqttMessageHandler,
		MqttClientCallback, SettingsChangeObserver, Identifiable, PingTest {

	/** The {@code ioThreadCount} property default value. */
	public static final int DEFAULT_IO_THREAD_COUNT = 2;

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final Executor executor;
	private final TaskScheduler scheduler;
	private final BasicMqttConnectionConfig connectionConfig;
	private final MqttStats stats;
	private int ioThreadCount = DEFAULT_IO_THREAD_COUNT;

	private volatile MqttMessageHandler messageHandler;
	private volatile MqttConnectionObserver connectionObserver;
	private volatile MqttClient client;

	private boolean closed;

	private CompletableFuture<MqttConnectReturnCode> connectFuture;
	private CompletableFuture<Void> reconfigureFuture;

	/**
	 * Constructor.
	 */
	public NettyMqttConnection(Executor executor, TaskScheduler scheduler) {
		this(executor, scheduler, new BasicMqttConnectionConfig(), null);
	}

	/**
	 * Constructor.
	 */
	public NettyMqttConnection(Executor executor, TaskScheduler scheduler,
			MqttConnectionConfig connectionConfig, MqttStats stats) {
		super();
		this.executor = executor;
		this.scheduler = scheduler;
		this.closed = false;
		this.connectionConfig = connectionConfig instanceof BasicMqttConnectionConfig
				? (BasicMqttConnectionConfig) connectionConfig
				: new BasicMqttConnectionConfig(connectionConfig);
		this.ioThreadCount = DEFAULT_IO_THREAD_COUNT;
		this.stats = stats;
	}

	/**
	 * Initialize after all properties have been configured.
	 */
	public synchronized void init() {
		try {
			open();
		} catch ( IOException e ) {
			// ignore;
		}
	}

	@Override
	public synchronized void configurationChanged(Map<String, Object> properties) {
		reconfigure();
	}

	/**
	 * Reconfigure the connection, closing any active connection in order to
	 * re-establish a new one based on the current configuration.
	 * 
	 * @return a future that completes when the
	 */
	public synchronized Future<?> reconfigure() {
		if ( reconfigureFuture != null ) {
			return reconfigureFuture;
		}
		if ( connectFuture != null ) {
			if ( !connectFuture.isDone() ) {
				try {
					log.info(
							"Cancelling scheduled connection to {} MQTT server from configuration change",
							getUid());
					connectFuture.cancel(true);
				} catch ( Exception e ) {
					// ignore
				}
			}
			connectFuture = null;
		}
		final CompletableFuture<Void> f = new CompletableFuture<>();
		reconfigureFuture = f;
		executor.execute(new Runnable() {

			@Override
			public void run() {
				Throwable t = null;
				try {
					try {
						closeConnection().get(connectionConfig.getConnectTimeoutSeconds(),
								TimeUnit.SECONDS);
					} catch ( Exception e ) {
						// ignore
					}
					try {
						open().get(connectionConfig.getConnectTimeoutSeconds(), TimeUnit.SECONDS);
					} catch ( Exception e ) {
						t = e;
					}
				} finally {
					complete(t);
				}
			}

			private void complete(Throwable t) {
				synchronized ( NettyMqttConnection.this ) {
					reconfigureFuture = null;
				}
				if ( t != null ) {
					f.completeExceptionally(t);

				} else {
					f.complete(null);
				}
			}

		});
		return f;
	}

	private final class ConnectTask implements Runnable {

		private final ConnectScheduledTask scheduledTask;
		private long reconnectDelay = 0;

		private ConnectTask(ConnectScheduledTask scheduledTask) {
			super();
			this.scheduledTask = scheduledTask;
		}

		@Override
		public void run() {
			synchronized ( NettyMqttConnection.this ) {
				if ( closed ) {
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
				try {
					client = MqttClient.create(config, NettyMqttConnection.this);
					client.setCallback(NettyMqttConnection.this);
					client.setEventLoop(new NioEventLoopGroup(ioThreadCount,
							new CustomizableThreadFactory("MQTT-" + getUid() + "-")));
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
				MqttStats s = NettyMqttConnection.this.stats;
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
						if ( connectionConfig.isReconnect() ) {
							client.getClientConfig().setReconnect(true);
						}
						MqttConnectReturnCode code = result != null ? returnCode(result.getReturnCode())
								: null;
						connectFuture.complete(code);
						MqttStats s = NettyMqttConnection.this.stats;
						if ( s != null ) {
							s.incrementAndGet(MqttStats.BasicCounts.ConnectionSuccess);
						}
						MqttConnectionObserver observer = NettyMqttConnection.this.connectionObserver;
						if ( observer != null ) {
							observer.onMqttServerConnectionEstablisehd(NettyMqttConnection.this, false);
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

		private final ConnectTask task = new ConnectTask(this);

		@Override
		public void run() {
			executor.execute(task);
		}

	}

	@Override
	public synchronized Future<MqttConnectReturnCode> open() throws IOException {
		if ( connectFuture != null ) {
			return connectFuture;
		}
		if ( client != null ) {
			return CompletableFuture.completedFuture(null);
		}
		CompletableFuture<MqttConnectReturnCode> f = new CompletableFuture<>();
		this.connectFuture = f;
		scheduler.schedule(new ConnectScheduledTask(), new Date(System.currentTimeMillis() + 200L));
		return f;
	}

	private MqttClientConfig createClientConfig(final MqttConnectionConfig connConfig) {
		if ( connConfig == null ) {
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

			default:
				config.setProtocolVersion(MqttVersion.MQTT_3_1_1);
		}
		config.setReconnect(false); // only switch AFTER connect
		config.setReconnectDelay(connConfig.getReconnectDelaySeconds());
		config.setTimeoutSeconds(connConfig.getKeepAliveSeconds());
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

	@Override
	public void close() throws IOException {
		MqttClient c;
		synchronized ( this ) {
			c = this.client;
			closed = true;
		}
		if ( c != null ) {
			URI serverUri = null;
			try {
				serverUri = client.getServerUri();
			} catch ( RuntimeException e ) {
				serverUri = connectionConfig.getServerUri();
			}
			try {
				closeConnection().get(connectionConfig.getConnectTimeoutSeconds(), TimeUnit.SECONDS);
			} catch ( ExecutionException e ) {
				log.warn("Error closing connection to MQTT server {}", serverUri);
				throw new IOException("Error closing connection to MQTT server " + serverUri, e);
			} catch ( TimeoutException | InterruptedException e ) {
				log.warn("Timeout closing connection to MQTT server {}", serverUri);
			}
		}
	}

	private Future<?> closeClient(final MqttClient c) {
		CompletableFuture<Void> result = new CompletableFuture<>();
		executor.execute(new Runnable() {

			@Override
			public void run() {
				try {
					c.disconnect().get(connectionConfig.getConnectTimeoutSeconds(), TimeUnit.SECONDS);
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

	private synchronized Future<?> closeConnection() {
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
		String msg = (cause != null ? cause.toString() : "unknown cause");
		log.warn("Connection lost to MQTT server {}: {}", connectionConfig.getServerUri(), msg);
		MqttStats s = this.stats;
		if ( s != null ) {
			s.incrementAndGet(MqttStats.BasicCounts.ConnectionLost);
		}
		MqttConnectionObserver observer = this.connectionObserver;
		if ( observer != null ) {
			observer.onMqttServerConnectionLost(this, connectionConfig.isReconnect(), cause);
		}
	}

	@Override
	public void onSuccessfulReconnect() {
		log.warn("Reconnected to MQTT server {}", connectionConfig.getServerUri());
		MqttStats s = this.stats;
		if ( s != null ) {
			s.incrementAndGet(MqttStats.BasicCounts.ConnectionSuccess);
		}
		MqttConnectionObserver observer = this.connectionObserver;
		if ( observer != null ) {
			observer.onMqttServerConnectionEstablisehd(this, true);
		}
	}

	@Override
	public boolean isEstablished() {
		final MqttClient client;
		synchronized ( this ) {
			if ( closed ) {
				return false;
			}
			client = client();
		}
		if ( client == null ) {
			return false;
		}
		synchronized ( client ) {
			return client.isConnected();
		}
	}

	@Override
	public synchronized boolean isClosed() {
		return closed;
	}

	private MqttClient client() {
		return client;
	}

	@Override
	public void onMqttMessage(MqttMessage message) {
		MqttStats s = NettyMqttConnection.this.stats;
		if ( s != null ) {
			s.incrementAndGet(MqttStats.BasicCounts.MessagesReceived);
		}
		MqttMessageHandler handler = this.messageHandler;
		if ( handler != null ) {
			handler.onMqttMessage(message);
		}
	}

	@Override
	public Future<?> publish(MqttMessage message) {
		if ( message == null ) {
			return CompletableFuture.completedFuture(null);
		}
		MqttClient c = this.client;
		if ( c == null ) {
			CompletableFuture<Void> f = new CompletableFuture<>();
			f.completeExceptionally(new IOException("Not connected to MQTT server."));
			return f;
		}
		io.netty.util.concurrent.Future<Void> f = c.publish(message.getTopic(),
				Unpooled.wrappedBuffer(message.getPayload()), NettyMqttUtils.qos(message.getQosLevel()),
				message.isRetained());

		final MqttStats s = this.stats;
		if ( s != null ) {
			f.addListener(new GenericFutureListener<io.netty.util.concurrent.Future<? super Void>>() {

				@Override
				public void operationComplete(io.netty.util.concurrent.Future<? super Void> future)
						throws Exception {
					if ( future.isSuccess() ) {
						s.incrementAndGet(MqttStats.BasicCounts.MessagesDelivered);
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
			MqttStats s = NettyMqttConnection.this.stats;
			if ( s != null ) {
				s.incrementAndGet(MqttStats.BasicCounts.MessagesReceived);
			}
			delegate.onMqttMessage(message);
		}

		@Override
		public int hashCode() {
			return delegate.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
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
		return c.off(topic, handler != null ? handler : this);
	}

	@Override
	public void setMessageHandler(MqttMessageHandler handler) {
		this.messageHandler = handler;
	}

	@Override
	public void setConnectionObserver(MqttConnectionObserver observer) {
		this.connectionObserver = observer;
	}

	/*---------------------
	 * Ping test support
	 *------------------ */

	@Override
	public String getPingTestId() {
		return getClass().getName() + "-" + getUid();
	}

	@Override
	public String getPingTestName() {
		return "MQTT Service";
	}

	@Override
	public long getPingTestMaximumExecutionMilliseconds() {
		return 10000;
	}

	@Override
	public PingTest.Result performPingTest() throws Exception {
		boolean healthy = isEstablished();
		URI serverUri = connectionConfig.getServerUri();
		String msg = (healthy ? "Connected to " + serverUri
				: client != null ? "Not connected" : "No client available");
		Map<String, Object> props = Collections.singletonMap("serverUri", serverUri);
		PingTestResult result = new PingTestResult(healthy, msg, props);
		return result;
	}

	/*---------------------
	 * Accessors
	 *------------------ */

	/**
	 * Get the connection configuration.
	 * 
	 * @return the configuration, never {@literal null}
	 */
	public BasicMqttConnectionConfig getConnectionConfig() {
		return connectionConfig;
	}

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

}
