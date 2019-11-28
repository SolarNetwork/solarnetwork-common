/* ==================================================================
 * ConnStartTester.java - 23/11/2019 1:26:47 pm
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

package net.solarnetwork.common.mqtt.paho.test;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.util.DigestUtils;

/**
 * Test MQTT connection.
 * 
 * <p>
 * Little program to test Paho's handling of trying to connect to non-existent
 * server.
 * </p>
 * 
 * @author matt
 * @version 1.0
 */
public class ConnStartTester implements MqttCallbackExtended {

	private static final long RETRY_CONNECT_DELAY = 2000L;
	private static final long MAX_CONNECT_DELAY_MS = 120000L;

	private static final Logger log = LoggerFactory.getLogger(ConnStartTester.class);

	private final ExecutorService executorService = Executors
			.newCachedThreadPool(new CustomizableThreadFactory("ConnStartTester-"));
	private final AtomicReference<IMqttAsyncClient> clientRef = new AtomicReference<>();

	private final String uid = UUID.randomUUID().toString();
	private final String username = "foo";
	private final String password = "bar";
	private final String clientId = "bim";
	private final String serverUri = "mqtts://localhost:28883";
	private final String persistencePath = "var/mqtt";
	private final long mqttTimeout = 5000L;
	private final boolean retryConnect = true;

	private Runnable connectThread;
	private MqttConnectOptions connOptions;

	public static void main(String[] args) {
		try {
			ConnStartTester t = new ConnStartTester();
			t.executorService.execute(new Runnable() {

				@Override
				public void run() {
					t.reconnect();
					//try {
					//	Thread.sleep(50L);
					//} catch ( InterruptedException e ) {
					// ignore
					//}
					t.reconnect();
				}

			});
		} catch ( Exception e ) {
			log.error("Connection error: {}", e.getMessage(), e);
		} finally {
			try {
				Thread.sleep(300000L);
			} catch ( InterruptedException e2 ) {
				// ignore
			}
		}
	}

	@Override
	public void connectionLost(Throwable cause) {
		log.info("Connection lost: {}", cause);
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		log.info("Message arrived on {}: {}", topic, message);
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		log.info("Delivery complete: {}", token);
	}

	@Override
	public void connectComplete(boolean reconnect, String serverURI) {
		log.info("{} complete to {}", (reconnect ? "Reconnect" : "Connect"), serverURI);
	}

	public synchronized void reconnect() {
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
							synchronized ( ConnStartTester.this ) {
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

				log.info("Connecting to MQTT server {} with clent {}", serverUri, client);
				clientRef.set(client);
				client.connect(connOptions).waitForCompletion(mqttTimeout);
			}
		} catch ( MqttException e ) {
			log.error("Error creating MQTT client: {}", e.toString());
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

}
