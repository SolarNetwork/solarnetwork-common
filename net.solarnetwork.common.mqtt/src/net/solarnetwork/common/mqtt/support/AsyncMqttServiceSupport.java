/* ==================================================================
 * AsyncMqttServiceSupport.java - 2/11/2019 6:29:58 pm
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

package net.solarnetwork.common.mqtt.support;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.DigestUtils;
import net.solarnetwork.domain.Identifiable;
import net.solarnetwork.domain.PingTest;
import net.solarnetwork.domain.PingTestResult;
import net.solarnetwork.settings.SettingsChangeObserver;
import net.solarnetwork.support.SSLService;
import net.solarnetwork.util.OptionalService;

/**
 * Helper base class for asynchronous MQTT client based services.
 * 
 * @author matt
 * @version 1.0
 * @since 1.1
 */
public abstract class AsyncMqttServiceSupport
		implements MqttCallbackExtended, SettingsChangeObserver, Identifiable, PingTest {

	/** The default value for the {@code mqttTimeout} property. */
	public static final long DEFAULT_MQTT_TIMEOUT = 10000;

	/** A class-level logger. */
	protected final Logger log = LoggerFactory.getLogger(getClass());

	private static final long RETRY_CONNECT_DELAY = 2000L;
	private static final long MAX_CONNECT_DELAY_MS = 120000L;

	private static final long RETRY_SUBSCRIBE_DELAY = 1000L;
	private static final long MAX_SUBSCRIBE_DELAY_MS = 60000L;

	private final ExecutorService executorService;
	private final OptionalService<SSLService> sslServiceRef;
	private final AtomicReference<IMqttAsyncClient> clientRef;
	private final MqttStats stats;
	private MqttConnectOptions connOptions;

	private String uid = UUID.randomUUID().toString();
	private String groupUid;
	private String displayName;

	private boolean retryConnect;
	private String serverUri;
	private String clientId;
	private String username;
	private String password;
	private String persistencePath = "var/mqtt";
	private int subscribeQos = 1;
	private int publishQos = 0;
	private long mqttTimeout = DEFAULT_MQTT_TIMEOUT;
	private boolean publishOnly;

	private Runnable connectThread = null;

	/**
	 * Constructor.
	 * 
	 * @param executorService
	 *        task service
	 * @param sslService
	 *        SSL service
	 * @param retryConnect
	 *        {@literal true} to keep retrying to connect to MQTT server
	 * @param stats
	 *        the MQTT stats tracker
	 */
	public AsyncMqttServiceSupport(ExecutorService executorService,
			OptionalService<SSLService> sslService, boolean retryConnect, MqttStats stats) {
		this(executorService, sslService, retryConnect, stats, null, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param executorService
	 *        task service
	 * @param sslService
	 *        SSL service
	 * @param retryConnect
	 *        {@literal true} to keep retrying to connect to MQTT server
	 * @param stats
	 *        the MQTT stats tracker
	 * @param serverUri
	 *        MQTT URI to connect to
	 * @param clientId
	 *        the MQTT client ID
	 */
	public AsyncMqttServiceSupport(ExecutorService executorService,
			OptionalService<SSLService> sslService, boolean retryConnect, MqttStats stats,
			String serverUri, String clientId) {
		super();
		assert executorService != null;
		this.executorService = executorService;
		this.sslServiceRef = sslService;
		this.serverUri = serverUri;
		this.clientId = clientId;
		this.retryConnect = retryConnect;
		this.stats = stats;
		this.clientRef = new AtomicReference<IMqttAsyncClient>();
	}

	/**
	 * Immediately connect.
	 */
	public synchronized void init() {
		configurationChanged(null);
	}

	@Override
	public synchronized void configurationChanged(Map<String, Object> properties) {
		if ( retryConnect ) {
			if ( connectThread != null ) {
				return;
			}
			this.connOptions = null;
			MqttConnectOptions newConnOptions = new MqttConnectOptions();
			Runnable connector = new Runnable() {

				final AtomicLong sleep = new AtomicLong(0);

				@Override
				public void run() {
					final long sleepMs = sleep.get();
					if ( sleepMs > 0 ) {
						try {
							Thread.sleep(sleepMs);
						} catch ( InterruptedException e ) {
							// ignore
						}
					}
					try {
						IMqttAsyncClient client = setupClient(newConnOptions);
						if ( client != null ) {
							synchronized ( AsyncMqttServiceSupport.this ) {
								connectThread = null;
								connOptions = newConnOptions;
							}
							return;
						} else if ( serverUri == null || serverUri.isEmpty() || clientId == null
								|| clientId.isEmpty() ) {
							// not configured yet
							log.info("{} MQTT configuration incomplete, will not connect.", uid);
							return;
						}
					} catch ( RuntimeException e ) {
						// ignore
					}
					long delay = sleep.accumulateAndGet(sleep.get() / RETRY_CONNECT_DELAY, (c, s) -> {
						long d = (s * 2) * RETRY_CONNECT_DELAY;
						if ( d == 0 ) {
							d = RETRY_CONNECT_DELAY;
						}
						if ( d > MAX_CONNECT_DELAY_MS ) {
							d = MAX_CONNECT_DELAY_MS;
						}
						return d;
					});
					log.info("Failed to connect to MQTT server {}, will try again in {}s", serverUri,
							MILLISECONDS.toSeconds(delay));
					executorService.execute(this);
				}
			};
			connectThread = connector;
			executorService.execute(connector);
		} else {
			this.connOptions = null;
			MqttConnectOptions newConnOptions = new MqttConnectOptions();
			IMqttAsyncClient client = setupClient(newConnOptions);
			if ( client != null ) {
				connOptions = newConnOptions;
			}
		}
	}

	/**
	 * Close down the service.
	 */
	public synchronized void close() {
		shutdownClient(clientRef.get());
	}

	/**
	 * Get the MQTT client, if available.
	 * 
	 * @return the MQTT client if available, otherwise {@literal null}
	 */
	protected final IMqttAsyncClient client() {
		return clientRef.get();
	}

	private synchronized void shutdownClient(IMqttAsyncClient client) {
		if ( client == null ) {
			return;
		}
		if ( this.connOptions != null ) {
			this.connOptions.setAutomaticReconnect(false);
		}
		try {
			if ( client.isConnected() ) {
				log.info("Disconnecting MQTT connection to {} with client {}", client.getServerURI(),
						client);
				client.disconnect().waitForCompletion(mqttTimeout);
			} else {
				log.debug("Not connected to MQTT @ {}, no need to shut down client {}",
						client.getServerURI(), client);
			}
		} catch ( MqttException e ) {
			log.warn("Error disconnecting MQTT connection to {} with client {}: {}",
					client.getServerURI(), client, e.toString());
			try {
				client.disconnectForcibly();
			} catch ( MqttException e2 ) {
				// ignore
			}
		} finally {
			try {
				log.info("Closing MQTT connection to {} with client {}", client.getServerURI(), client);
				client.close();
			} catch ( MqttException e ) {
				log.warn("Error closing MQTT connection to {} with client {}: {}", client.getServerURI(),
						client, e.toString());
			} finally {
				clientRef.compareAndSet(client, null);
			}
		}
	}

	private synchronized IMqttAsyncClient setupClient(MqttConnectOptions connOptions) {
		IMqttAsyncClient client = null;
		shutdownClient(clientRef.get());
		try {
			client = createClient(uid, serverUri, clientId, persistencePath);
			if ( client != null ) {
				connOptions.setCleanSession(false);
				connOptions.setAutomaticReconnect(true);
				connOptions.setConnectionTimeout((int) MILLISECONDS.toSeconds(mqttTimeout));
				if ( username != null && !username.isEmpty() ) {
					connOptions.setUserName(username);
				}
				if ( password != null && !password.isEmpty() ) {
					connOptions.setPassword(password.toCharArray());
				}

				final SSLService sslService = (sslServiceRef != null ? sslServiceRef.service() : null);
				if ( sslService != null ) {
					connOptions.setSocketFactory(sslService.getSSLSocketFactory());
				}

				log.info("Connecting to MQTT server {} with clent {}", serverUri, client);
				clientRef.set(client);
				client.connect(connOptions).waitForCompletion(mqttTimeout);

				if ( !publishOnly ) {
					subscribeToTopics(client);
				}
			}
		} catch ( MqttException e ) {
			log.error("Error creating MQTT client: {}", e.toString());
			shutdownClient(client);
			client = null;
		}
		return client;
	}

	private synchronized IMqttAsyncClient createClient(String uid, String serverUri, String clientId,
			String persistencePath) throws MqttException {
		if ( uid == null || uid.isEmpty() || serverUri == null || serverUri.isEmpty() || clientId == null
				|| clientId.isEmpty() || persistencePath == null || persistencePath.isEmpty() ) {
			log.info("Server URI and/or client ID not configured, cannot connect to MQTT server.");
			return null;
		}
		URI uri;
		try {
			uri = new URI(serverUri);
		} catch ( URISyntaxException e1 ) {
			log.error("Invalid MQTT URL: " + serverUri);
			return null;
		}

		int port = uri.getPort();
		String scheme = uri.getScheme();
		boolean useSsl = (port == 8883 || "mqtts".equalsIgnoreCase(scheme)
				|| "ssl".equalsIgnoreCase(scheme));
		String connUri = (useSsl ? "ssl" : "tcp") + "://" + uri.getHost()
				+ (port > 0 ? ":" + uri.getPort() : "");

		Path p = Paths.get(persistencePath, DigestUtils.md5DigestAsHex(uid.getBytes()));
		if ( !Files.isDirectory(p) ) {
			try {
				Files.createDirectories(p);
			} catch ( IOException e ) {
				throw new RuntimeException(
						"Unable to create MQTT persistance directory [" + p + "]: " + e.getMessage(), e);
			}
		}
		MqttDefaultFilePersistence persistence = new MqttDefaultFilePersistence(p.toString());
		MqttAsyncClient c = null;
		c = new MqttAsyncClient(connUri, clientId, persistence);
		c.setCallback(this);
		return c;
	}

	/**
	 * Extending classes can override to subscribe to topics when the connection
	 * is established.
	 * 
	 * <p>
	 * Extending classes can choose to override this and subscribe to topics
	 * when the connection is established. For example:
	 * </p>
	 * 
	 * <pre>
	 * <code>
	 * IMqttToken token = client.subscribe(datumTopics, subscribeQos);
	 * token.waitForCompletion(mqttTimeout);
	 * </code>
	 * </pre>
	 * 
	 * @param client
	 *        the client to subscribe with
	 * @throws MqttException
	 *         if any error occurs
	 */
	protected void subscribeToTopics(IMqttAsyncClient client) throws MqttException {
		// nothing to do here
	}

	/**
	 * Examine an exception to determine if the connection should be forcibly
	 * closed and then reopened.
	 * 
	 * @param cause
	 *        the exception to examine
	 * @return the exception that should trigger connection re-establishment, or
	 *         {@literal null} if the connection should be left alone
	 */
	protected Throwable exceptionToForceConnectionReestablishment(Throwable cause) {
		return null;
	}

	@Override
	public void connectionLost(Throwable cause) {
		final IMqttAsyncClient client = clientRef.get();
		log.info("Connection lost to MQTT server @ {} with client {}: {}",
				(client != null ? client.getServerURI() : "N/A"), client, cause.toString());
		final Throwable restablishThrowable = exceptionToForceConnectionReestablishment(cause);
		if ( restablishThrowable != null ) {
			// shutdown and re-connect
			executorService.execute(new Runnable() {

				@Override
				public void run() {
					try {
						// pause just a bit to give connectionLost chance to complete
						Thread.sleep(200);
					} catch ( InterruptedException e ) {
						// just continue
					}
					log.info(
							"Re-establishing connection to MQTT server @ {} with client {} after error [{}]",
							(client != null ? client.getServerURI() : "N/A"), client,
							restablishThrowable.getMessage());
					close();
					try {
						// pause just a bit to give connection chance to close
						Thread.sleep(200);
					} catch ( InterruptedException e ) {
						// just continue
					}
					init();
				}
			});
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
		deliveryCompleteInternal(token);
	}

	protected void deliveryCompleteInternal(IMqttDeliveryToken token) {
		// extending classes can override
	}

	@Override
	public final void connectComplete(boolean reconnect, String serverURI) {
		log.info("{} to MQTT server @ {} with client {}", (reconnect ? "Reconnected" : "Connected"),
				serverURI, clientRef.get());
		stats.incrementAndGet(MqttStats.BasicCounts.ConnectionSuccess);
		if ( reconnect ) {
			// re-subscribe
			executorService.execute(new Runnable() {

				final AtomicLong sleep = new AtomicLong(0);

				@Override
				public void run() {
					final IMqttAsyncClient client = clientRef.get();
					if ( client == null || !client.isConnected() ) {
						return;
					}
					final long sleepMs = sleep.get();
					try {
						Thread.sleep(sleepMs + 200);
					} catch ( InterruptedException e ) {
						// ignore
					}
					try {
						if ( !publishOnly ) {
							subscribeToTopics(client);
						}
					} catch ( MqttException e ) {
						if ( e.getReasonCode() == MqttException.REASON_CODE_CLIENT_NOT_CONNECTED ) {
							// stop trying to subscribe
							log.error(
									"Error subscribing to topics on MQTT server @ {} ({}), will not try again.",
									serverUri, e.toString(), e);
							return;
						}
						long delay = sleep.accumulateAndGet(sleep.get() / RETRY_SUBSCRIBE_DELAY,
								(c, s) -> {
									long d = (s * 2) * RETRY_SUBSCRIBE_DELAY;
									if ( d == 0 ) {
										d = RETRY_SUBSCRIBE_DELAY;
									}
									if ( d > MAX_SUBSCRIBE_DELAY_MS ) {
										d = MAX_SUBSCRIBE_DELAY_MS;
									}
									return d;
								});
						log.error(
								"Error subscribing to topics on MQTT server @ {} ({}), will try again in {}s",
								serverUri, e.toString(), MILLISECONDS.toSeconds(delay), e);
						executorService.execute(this);
					}
				}
			});
		}
		connectionCompleteInternal(reconnect, serverURI);
	}

	protected void connectionCompleteInternal(boolean reconnect, String serverURI) {
		// extending classes can override
	}

	@Override
	public final void messageArrived(String topic, MqttMessage message) throws Exception {
		log.trace("SolarIn MQTT message arrived on {}", topic);
		stats.incrementAndGet(MqttStats.BasicCounts.MessagesReceived);
		messageArrivedInternal(topic, message);
	}

	protected void messageArrivedInternal(String topic, MqttMessage message) throws Exception {
		// extending classes can override
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
		IMqttAsyncClient client = clientRef.get();
		boolean healthy = (client != null && client.isConnected());
		String msg = (healthy ? "Connected to " + serverUri
				: client != null ? "Not connected" : "No client available");
		Map<String, Object> props = Collections.singletonMap("serverUri", serverUri);
		PingTestResult result = new PingTestResult(healthy, msg, props);
		return result;
	}

	@Override
	public String getUid() {
		return uid;
	}

	/*---------------------
	 * Accessors
	 *------------------ */

	/**
	 * Set the service unique ID.
	 * 
	 * @param uid
	 *        the unique ID
	 */
	public void setUid(String uid) {
		if ( uid == null ) {
			throw new IllegalArgumentException("uid value must not be null");
		}
		this.uid = uid;
		this.stats.setUid(uid);
	}

	@Override
	public String getGroupUid() {
		return groupUid;
	}

	/**
	 * Set the service group unique ID.
	 * 
	 * @param groupUid
	 *        the group ID
	 */
	public void setGroupUid(String groupUid) {
		this.groupUid = groupUid;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Set the service display name.
	 * 
	 * @param displayName
	 *        the display name
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * Set the MQTT server URI to connect to.
	 * 
	 * <p>
	 * This should be in the form <code>mqtt[s]://host:port</code>.
	 * </p>
	 * 
	 * @param serverUri
	 *        the URI to connect to
	 */
	public void setServerUri(String serverUri) {
		this.serverUri = serverUri;
	}

	/**
	 * Set the MQTT client ID to use.
	 * 
	 * @param clientId
	 *        the client ID
	 */
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	/**
	 * Set the MQTT username to authenticate as.
	 * 
	 * @param username
	 *        the username
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Set the MQTT password to authenticate with.
	 * 
	 * @param password
	 *        the password
	 */
	public void setPassword(String password) {
		this.password = password;
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
	 * Set flag to retry connecting during startup.
	 * 
	 * <p>
	 * This affects how the {@link #init()} method handles connecting to the
	 * MQTT broker. When {@literal true} then the connection will happen in a
	 * background thread, and this service will keep re-trying to connect if the
	 * connection fails. It will delay re-try attempts in an increasing fashion,
	 * up to a limit of 2 minutes. When this property is {@literal false} then
	 * the connection will happen in the calling thread and this service will
	 * <b>not</b> attempt to re-try the connection if the connection fails.
	 * </p>
	 * 
	 * <p>
	 * Note that once connected to a broker, this service <b>will</b>
	 * automatically re-connect to that broker if the connection closes for any
	 * reason.
	 * </p>
	 * 
	 * @param retryConnect
	 *        {@literal true} to re-try connecting to the MQTT broker during
	 *        startup if the connection attempt fails, {@literal false} to only
	 *        try once
	 */
	public void setRetryConnect(boolean retryConnect) {
		this.retryConnect = retryConnect;
	}

	/**
	 * Set the statistic log frequency.
	 * 
	 * @param frequency
	 *        the statistic log frequency
	 */
	public void setStatLogFrequency(int frequency) {
		if ( frequency < 1 ) {
			frequency = 1;
		}
		stats.setLogFrequency(frequency);
	}

	/**
	 * Get the MQTT QoS to use on subscription topics.
	 * 
	 * @param subscribeQos
	 *        the subscription QoS; defaults to {@literal 1}
	 */
	public int getSubscribeQos() {
		return subscribeQos;
	}

	/**
	 * The MQTT QoS to use on subscription topics.
	 * 
	 * @param subscribeQos
	 *        the subscription QoS
	 */
	public void setSubscribeQos(int subscribeQos) {
		this.subscribeQos = subscribeQos;
	}

	/**
	 * Get the MQTT QoS to use to publish to instruction topics.
	 * 
	 * @return the publish QoS; defaults to {@literal 0}
	 */
	public int getPublishQos() {
		return publishQos;
	}

	/**
	 * The MQTT QoS to use to publish to instruction topics.
	 * 
	 * <p>
	 * Note this defaults to {@literal 0} because a node might not actually be
	 * using MQTT, and instructions that aren't transitioned from their initial
	 * {@literal Queuing } state to something else after a set period of time
	 * will automatically be transitioned to {@literal Queued} so the bulk
	 * instruction process will pick up the instruction.
	 * </p>
	 * 
	 * @param publishQos
	 *        the publish QoS; defaults to {@literal 0}
	 */
	public void setPublishQos(int publishQos) {
		this.publishQos = publishQos;
	}

	/**
	 * A timeout, in milliseconds, to wait for MQTT operations to complete in.
	 * 
	 * @param mqttTimeout
	 *        the timeout, or less than 1 to wait forever; defaults to
	 *        {@link #DEFAULT_MQTT_TIMEOUT}
	 */
	public void setMqttTimeout(long mqttTimeout) {
		this.mqttTimeout = mqttTimeout;
	}

	/**
	 * Set the "publish only" mode.
	 * 
	 * <p>
	 * In "publish only" mode the collector does not subscribe to any topics. It
	 * will only publish node instructions that arrive via
	 * {@link #didQueueNodeInstruction(NodeInstruction, Long)}.
	 * </p>
	 * 
	 * @param publishOnly
	 *        if {@literal true} then do <b>not</b> subscribe to any topics
	 */
	public void setPublishOnly(boolean publishOnly) {
		this.publishOnly = publishOnly;
	}

	/**
	 * Get the MQTT timeout.
	 * 
	 * @return the timeout, in milliseconds
	 */
	public long getMqttTimeout() {
		return mqttTimeout;
	}

	/**
	 * Get the executor service.
	 * 
	 * @return the executorService the executor service
	 */
	public ExecutorService getExecutorService() {
		return executorService;
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
