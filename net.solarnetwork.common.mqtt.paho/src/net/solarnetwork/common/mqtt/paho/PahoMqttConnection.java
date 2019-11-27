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
import java.util.Date;
import java.util.concurrent.CompletableFuture;
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
import org.springframework.scheduling.TaskScheduler;
import org.springframework.util.DigestUtils;
import net.solarnetwork.common.mqtt.BaseMqttConnection;
import net.solarnetwork.common.mqtt.BasicMqttConnectionConfig;
import net.solarnetwork.common.mqtt.MqttConnectReturnCode;
import net.solarnetwork.common.mqtt.MqttConnection;
import net.solarnetwork.common.mqtt.MqttConnectionConfig;
import net.solarnetwork.common.mqtt.MqttConnectionObserver;
import net.solarnetwork.common.mqtt.MqttMessageHandler;
import net.solarnetwork.common.mqtt.MqttQos;
import net.solarnetwork.common.mqtt.MqttStats;
import net.solarnetwork.common.mqtt.MqttStats.MqttStat;

/**
 * Implementation of {@link MqttConnection} based on the Paho framework.
 * 
 * @author matt
 * @version 1.0
 */
public class PahoMqttConnection extends BaseMqttConnection
		implements MqttCallbackExtended, IMqttMessageListener {

	private volatile IMqttAsyncClient client;

	private String persistencePath = "var/mqtt";

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
		super(executor, scheduler, connectionConfig, stats);
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
			if ( isClosed() ) {
				return;
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

	@Override
	protected synchronized Future<?> closeConnection() {
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

	/*---------------------
	 * Accessors
	 *------------------ */

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

}
