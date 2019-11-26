/* ==================================================================
 * PahoMqttConnection.java - 27/11/2019 7:12:52 am
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

package net.solarnetwork.common.mqtt.paho;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.util.DigestUtils;
import net.solarnetwork.common.mqtt.BasicMqttConnectionConfig;
import net.solarnetwork.common.mqtt.MqttConnectReturnCode;
import net.solarnetwork.common.mqtt.MqttConnection;
import net.solarnetwork.common.mqtt.MqttConnectionConfig;
import net.solarnetwork.common.mqtt.MqttConnectionObserver;
import net.solarnetwork.common.mqtt.MqttMessageHandler;
import net.solarnetwork.common.mqtt.MqttQos;
import net.solarnetwork.common.mqtt.MqttStats;
import net.solarnetwork.common.mqtt.MqttStats.MqttStat;
import net.solarnetwork.common.mqtt.ReconfigurableMqttConnection;
import net.solarnetwork.domain.Identifiable;
import net.solarnetwork.domain.PingTest;
import net.solarnetwork.domain.PingTestResult;
import net.solarnetwork.settings.SettingsChangeObserver;
import net.solarnetwork.support.BasicIdentifiable;

/**
 * Implementation of {@link MqttConnection} based on the Paho framework.
 * 
 * @author matt
 * @version 1.0
 */
public class PahoMqttConnection extends BasicIdentifiable
		implements MqttConnection, ReconfigurableMqttConnection, MqttCallbackExtended,
		IMqttMessageListener, SettingsChangeObserver, Identifiable, PingTest {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final Executor executor;
	private final TaskScheduler scheduler;
	private final BasicMqttConnectionConfig connectionConfig;
	private final MqttStats stats;

	private volatile MqttMessageHandler messageHandler;
	private volatile MqttConnectionObserver connectionObserver;
	private volatile IMqttAsyncClient client;

	private String persistencePath = "var/mqtt";
	private boolean closed;

	private CompletableFuture<MqttConnectReturnCode> connectFuture;
	private CompletableFuture<Void> reconfigureFuture;

	/**
	 * Constructor.
	 */
	public PahoMqttConnection(Executor executor, TaskScheduler scheduler) {
		this(executor, scheduler, new BasicMqttConnectionConfig(), null);
	}

	/**
	 * Constructor.
	 */
	public PahoMqttConnection(Executor executor, TaskScheduler scheduler,
			MqttConnectionConfig connectionConfig, MqttStats stats) {
		super();
		this.executor = executor;
		this.scheduler = scheduler;
		this.closed = false;
		this.connectionConfig = connectionConfig instanceof BasicMqttConnectionConfig
				? (BasicMqttConnectionConfig) connectionConfig
				: new BasicMqttConnectionConfig(connectionConfig);
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

	@Override
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
				synchronized ( PahoMqttConnection.this ) {
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
			synchronized ( PahoMqttConnection.this ) {
				if ( closed ) {
					return;
				}
			}
			if ( reconnectDelay < connectionConfig.getReconnectDelaySeconds() * 30000L ) {
				int step = (Math.max(1, connectionConfig.getReconnectDelaySeconds() / 2));
				reconnectDelay += (step * 1000L);
			}
			Throwable t = null;
			IMqttToken tok = null;
			MqttConnectOptions config = null;
			try {
				config = createClientConfig(connectionConfig);
			} catch ( RuntimeException e ) {
				log.warn("Invalid {} MQTT configuration: {}", getUid(), e.toString(), e);
				t = e;
			}
			if ( config != null ) {
				MqttAsyncClient client = null;
				try {
					client = createClient(getUid(), connectionConfig.getServerUri(),
							connectionConfig.getClientId());
					client.setCallback(PahoMqttConnection.this);
					log.info("Connecting to MQTT server {}...", connectionConfig.getServerUri());
					tok = client.connect(config);
					tok.waitForCompletion(
							TimeUnit.SECONDS.toMillis(connectionConfig.getConnectTimeoutSeconds()));
					if ( tok.getException() != null ) {
						t = tok.getException();
					} else if ( tok.isComplete() ) {
						log.info("Connected to MQTT server {}", connectionConfig.getServerUri());
						connectComplete(client, tok, null);
						return;
					}
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
				MqttStats s = PahoMqttConnection.this.stats;
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
				connectComplete(null, tok, t);
			}
		}

		private MqttAsyncClient createClient(String uid, URI serverUri, String clientId)
				throws MqttException {
			if ( uid == null || uid.isEmpty() || serverUri == null || clientId == null
					|| clientId.isEmpty() || persistencePath == null || persistencePath.isEmpty() ) {
				log.info("Server URI and/or client ID not configured, cannot connect to MQTT server.");
				return null;
			}

			int port = serverUri.getPort();
			String scheme = serverUri.getScheme();
			boolean useSsl = (port == 8883 || "mqtts".equalsIgnoreCase(scheme)
					|| "ssl".equalsIgnoreCase(scheme));
			String connUri = (useSsl ? "ssl" : "tcp") + "://" + serverUri.getHost()
					+ (port > 0 ? ":" + serverUri.getPort() : "");

			Path p = Paths.get(persistencePath, DigestUtils.md5DigestAsHex(uid.getBytes()));
			if ( !Files.isDirectory(p) ) {
				try {
					Files.createDirectories(p);
				} catch ( IOException e ) {
					throw new RuntimeException(
							"Unable to create MQTT persistance directory [" + p + "]: " + e.getMessage(),
							e);
				}
			}
			MqttDefaultFilePersistence persistence = new MqttDefaultFilePersistence(p.toString());
			MqttAsyncClient c = null;
			c = new MqttAsyncClient(connUri, clientId, persistence);
			c.setCallback(PahoMqttConnection.this);
			return c;
		}

		private void connectComplete(MqttAsyncClient client, IMqttToken result, Throwable t) {
			final MqttConnectReturnCode code = returnCode(result);
			synchronized ( PahoMqttConnection.this ) {
				PahoMqttConnection.this.client = client;
				if ( connectFuture != null ) {
					if ( t != null ) {
						connectFuture.completeExceptionally(t);
					} else {
						connectFuture.complete(code);
						MqttStats s = PahoMqttConnection.this.stats;
						if ( s != null ) {
							s.incrementAndGet(MqttStats.BasicCounts.ConnectionSuccess);
						}
						MqttConnectionObserver observer = PahoMqttConnection.this.connectionObserver;
						if ( observer != null ) {
							observer.onMqttServerConnectionEstablisehd(PahoMqttConnection.this, false);
						}
					}
				}
			}
		}
	}

	private MqttConnectReturnCode returnCode(IMqttToken token) {
		if ( token == null ) {
			return null;
		}
		MqttException e = token.getException();
		if ( e == null && token.isComplete() ) {
			return MqttConnectReturnCode.Accepted;
		} else if ( e == null ) {
			return null;
		}
		switch (e.getReasonCode()) {
			case MqttException.REASON_CODE_FAILED_AUTHENTICATION:
				return MqttConnectReturnCode.BadCredentials;

			case MqttException.REASON_CODE_INVALID_CLIENT_ID:
				return MqttConnectReturnCode.ClientIdRejected;

			case MqttException.REASON_CODE_NOT_AUTHORIZED:
				return MqttConnectReturnCode.NotAuthorized;

			case MqttException.REASON_CODE_BROKER_UNAVAILABLE:
				return MqttConnectReturnCode.ServerUnavailable;

			case MqttException.REASON_CODE_INVALID_PROTOCOL_VERSION:
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

	private MqttConnectOptions createClientConfig(final MqttConnectionConfig connConfig) {
		if ( connConfig == null ) {
			return null;
		}

		if ( connConfig.getServerUri() == null || connConfig.getClientId() == null
				|| connConfig.getClientId().isEmpty() ) {
			log.info("Server URI and/or client ID not configured, cannot connect to MQTT server.");
			return null;
		}

		final MqttConnectOptions config = new MqttConnectOptions();

		config.setCleanSession(connConfig.isCleanSession());
		config.setAutomaticReconnect(connConfig.isReconnect());
		config.setConnectionTimeout(connConfig.getConnectTimeoutSeconds());
		config.setKeepAliveInterval(connConfig.getKeepAliveSeconds());
		if ( connConfig.getUsername() != null && !connConfig.getUsername().isEmpty() ) {
			config.setUserName(connConfig.getUsername());
		}
		if ( connConfig.getPassword() != null && !connConfig.getPassword().isEmpty() ) {
			config.setPassword(connConfig.getPassword().toCharArray());
		}

		if ( connConfig.getSslService() != null ) {
			config.setSocketFactory(connConfig.getSslService().getSSLSocketFactory());
		}

		if ( connConfig.getLastWill() != null ) {
			config.setWill(connConfig.getLastWill().getTopic(), connConfig.getLastWill().getPayload(),
					connConfig.getLastWill().getQosLevel().getValue(),
					connConfig.getLastWill().isRetained());
		}

		switch (connConfig.getVersion()) {
			case Mqtt31:
				config.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
				break;

			default:
				config.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
		}
		return config;
	}

	@Override
	public void close() throws IOException {
		IMqttAsyncClient c;
		synchronized ( this ) {
			c = this.client;
			closed = true;
		}
		if ( c != null ) {
			String serverUri = null;
			try {
				serverUri = client.getServerURI();
			} catch ( RuntimeException e ) {
				URI uri = connectionConfig.getServerUri();
				if ( uri != null ) {
					serverUri = uri.toString();
				}
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

	private Future<?> closeClient(final IMqttAsyncClient c) {
		CompletableFuture<Void> result = new CompletableFuture<>();
		executor.execute(new Runnable() {

			@Override
			public void run() {
				try {
					if ( c.isConnected() ) {
						log.info("Disconnecting MQTT connection to {} with client {}", c.getServerURI(),
								c);
						c.disconnect().waitForCompletion(
								TimeUnit.SECONDS.toMillis(connectionConfig.getConnectTimeoutSeconds()));
					} else {
						log.debug("Not connected to MQTT @ {}, no need to shut down client {}",
								c.getServerURI(), client);
					}
				} catch ( MqttException e ) {
					log.warn("Error disconnecting MQTT connection to {} with client {}: {}",
							c.getServerURI(), c, e.toString());
					try {
						c.disconnectForcibly();
					} catch ( MqttException e2 ) {
						// ignore
					}
				} finally {
					try {
						log.info("Closing MQTT connection to {} with client {}", c.getServerURI(), c);
						c.close();
						result.complete(null);
					} catch ( MqttException e ) {
						log.warn("Error closing MQTT connection to {} with client {}: {}",
								c.getServerURI(), c, e.toString());
						result.completeExceptionally(e);
					} finally {
						synchronized ( PahoMqttConnection.this ) {
							client = null;
						}
					}
				}
			}
		});
		return result;
	}

	private synchronized Future<?> closeConnection() {
		final IMqttAsyncClient c = this.client;
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
	public final void deliveryComplete(IMqttDeliveryToken token) {
		if ( token != null ) {
			if ( token.isComplete() ) {
				stats.incrementAndGet(MqttStats.BasicCounts.MessagesDelivered);
			} else if ( token.getException() != null ) {
				stats.incrementAndGet(MqttStats.BasicCounts.MessagesDeliveredFail);
			}
		}
	}

	@Override
	public final void connectComplete(boolean reconnect, String serverURI) {
		if ( reconnect ) {
			onSuccessfulReconnect();
		}
	}

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
		final IMqttAsyncClient c;
		synchronized ( this ) {
			if ( closed ) {
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

	@Override
	public synchronized boolean isClosed() {
		return closed;
	}

	@Override
	public final void messageArrived(String topic, MqttMessage message) throws Exception {
		log.trace("SolarIn MQTT message arrived on {}", topic);
		MqttStats s = this.stats;
		if ( s != null ) {
			s.incrementAndGet(MqttStats.BasicCounts.MessagesReceived);
		}
		MqttMessageHandler handler = this.messageHandler;
		if ( handler != null ) {
			handler.onMqttMessage(new PahoMqttMessage(topic, message));
		}
	}

	@Override
	public Future<?> publish(net.solarnetwork.common.mqtt.MqttMessage message) {
		if ( message == null ) {
			return CompletableFuture.completedFuture(null);
		}
		CompletableFuture<Void> f = new CompletableFuture<>();
		IMqttAsyncClient c = this.client;
		if ( c == null ) {
			f.completeExceptionally(new IOException("Not connected to MQTT server."));
			return f;
		}
		final MqttStats s = this.stats;
		try {
			c.publish(message.getTopic(), message.getPayload(), message.getQosLevel().getValue(),
					message.isRetained(), null,
					new CompletableMqttActionListener(f, MqttStats.BasicCounts.MessagesDelivered,
							MqttStats.BasicCounts.MessagesDeliveredFail));
		} catch ( MqttException e ) {
			if ( s != null ) {
				s.incrementAndGet(MqttStats.BasicCounts.MessagesDeliveredFail);
			}
			f.completeExceptionally(e);
		}
		return f;
	}

	private final class StatsMessageHandler implements IMqttMessageListener {

		private final MqttMessageHandler delegate;

		private StatsMessageHandler(MqttMessageHandler delegate) {
			super();
			this.delegate = delegate;
		}

		@Override
		public void messageArrived(String topic, MqttMessage message) throws Exception {
			MqttStats s = PahoMqttConnection.this.stats;
			if ( s != null ) {
				s.incrementAndGet(MqttStats.BasicCounts.MessagesReceived);
			}
			delegate.onMqttMessage(new PahoMqttMessage(topic, message));
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

	private final class CompletableMqttActionListener implements IMqttActionListener {

		private final CompletableFuture<Void> f;
		private final MqttStat statSuccess;
		private final MqttStat statFailure;

		private CompletableMqttActionListener(CompletableFuture<Void> future) {
			this(future, null, null);
		}

		private CompletableMqttActionListener(CompletableFuture<Void> future, MqttStat statSuccess,
				MqttStat statFailure) {
			this.f = future;
			this.statSuccess = statSuccess;
			this.statFailure = statFailure;
		}

		@Override
		public void onSuccess(IMqttToken asyncActionToken) {
			if ( stats != null && statSuccess != null ) {
				stats.incrementAndGet(statSuccess);
			}
			f.complete(null);
		}

		@Override
		public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
			if ( stats != null && statFailure != null ) {
				stats.incrementAndGet(statFailure);
			}
			f.completeExceptionally(exception);
		}

	}

	@Override
	public Future<?> subscribe(String topic, MqttQos qosLevel, MqttMessageHandler handler) {
		IMqttAsyncClient c = this.client;
		CompletableFuture<Void> f = new CompletableFuture<>();
		if ( c == null ) {
			f.completeExceptionally(new IOException("Not connected to MQTT server."));
			return f;
		}
		try {
			c.subscribe(topic, qosLevel.getValue(), null, new CompletableMqttActionListener(f),
					handler != null ? new StatsMessageHandler(handler) : this);
		} catch ( MqttException e ) {
			f.completeExceptionally(e);
		}
		return f;
	}

	@Override
	public Future<?> unsubscribe(String topic, MqttMessageHandler handler) {
		IMqttAsyncClient c = this.client;
		CompletableFuture<Void> f = new CompletableFuture<>();
		if ( c == null ) {
			f.completeExceptionally(new IOException("Not connected to MQTT server."));
			return f;
		}
		// must wrap `handler` with StatsMessageHandler so equals() works with what
		// was passed to c.on() in the #subscribe() method above
		try {
			c.unsubscribe(topic, null, new CompletableMqttActionListener(f));
		} catch ( MqttException e ) {
			f.completeExceptionally(e);
		}
		return f;
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
		IMqttAsyncClient client = this.client;
		boolean healthy = (client != null && client.isConnected());
		String serverUri = (client != null ? client.getServerURI()
				: connectionConfig.getServerUri() != null ? connectionConfig.getServerUri().toString()
						: null);
		String msg = (healthy ? "Connected to " + serverUri
				: client != null ? "Not connected" : "No client available");
		Map<String, Object> props = Collections.singletonMap("serverUri", serverUri);
		PingTestResult result = new PingTestResult(healthy, msg, props);
		return result;
	}

	/*---------------------
	 * Accessors
	 *------------------ */

	@Override
	public void setUid(String uid) {
		super.setUid(uid);
		if ( uid == null ) {
			throw new IllegalArgumentException("uid value must not be null");
		}
		this.stats.setUid(uid);
	}

	/**
	 * Get the connection configuration.
	 * 
	 * @return the configuration, never {@literal null}
	 */
	public BasicMqttConnectionConfig getConnectionConfig() {
		return connectionConfig;
	}

	/**
	 * Set the path to store persisted MQTT data.
	 * 
	 * <p>
	 * This directory will be created if it does not already exist.
	 * </p>
	 * 
	 * @param persistencePath
	 *        the path to set; defaults to {@literal var/mqtt}
	 */
	public void setPersistencePath(String persistencePath) {
		this.persistencePath = persistencePath;
	}

	/**
	 * Get the executor service.
	 * 
	 * @return the executorService the executor service
	 */
	public Executor getExecutor() {
		return executor;
	}

	/**
	 * Get the statistics.
	 * 
	 * @return the stats the statistics
	 */
	public MqttStats getStats() {
		return stats;
	}

}
