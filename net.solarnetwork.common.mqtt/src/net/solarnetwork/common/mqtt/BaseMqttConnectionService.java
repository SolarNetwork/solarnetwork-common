/* ==================================================================
 * BaseMqttConnectionService.java - 27/11/2019 5:57:41 pm
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

package net.solarnetwork.common.mqtt;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.solarnetwork.domain.PingTest;
import net.solarnetwork.domain.PingTestResult;
import net.solarnetwork.support.BasicIdentifiable;

/**
 * An abstract service that uses a {@link MqttConnection}.
 * 
 * @author matt
 * @version 1.0
 */
public abstract class BaseMqttConnectionService extends BasicIdentifiable implements PingTest {

	/** The default value for the {@code publishQos} property. */
	public static final MqttQos DEFAULT_PUBLISH_QOS = MqttQos.AtLeastOnce;

	/** The default value for the {@code subscribeQos} property. */
	public static final MqttQos DEFAULT_SUBSCRIBE_QOS = MqttQos.AtLeastOnce;

	/** A class-level logger. */
	protected final Logger log = LoggerFactory.getLogger(getClass());

	private final MqttConnectionFactory connectionFactory;
	private final BasicMqttConnectionConfig mqttConfig;

	private MqttQos publishQos = DEFAULT_PUBLISH_QOS;
	private MqttQos subscribeQos = DEFAULT_SUBSCRIBE_QOS;
	private MqttConnection connection;

	/**
	 * Constructor.
	 * 
	 * @param connectionFactory
	 *        the factory to use for {@link MqttConnection} instances
	 * @param mqttStats
	 *        the optional statistics to use
	 */
	public BaseMqttConnectionService(MqttConnectionFactory connectionFactory, MqttStats mqttStats) {
		super();
		this.connectionFactory = connectionFactory;
		mqttConfig = new BasicMqttConnectionConfig();
		mqttConfig.setStats(mqttStats);
	}

	/**
	 * Call after properties are configured.
	 * 
	 * <p>
	 * This invokes {@link #startup()} without waiting for the result.
	 * </p>
	 */
	public synchronized void init() {
		startup();
	}

	/**
	 * Call after properties are configured.
	 * 
	 * @return a future that completes when the connection has been established
	 */
	public synchronized Future<?> startup() {
		if ( connection != null ) {
			return CompletableFuture.completedFuture(null);
		}
		connection = connectionFactory.createConnection(mqttConfig);
		if ( connection == null ) {
			CompletableFuture<Void> f = new CompletableFuture<>();
			f.completeExceptionally(
					new RuntimeException("Failed to obtain MQTT connection from factory."));
			return f;
		}
		connectionCreated(connection);
		try {
			return connection.open();
		} catch ( IOException e ) {
			throw new RuntimeException("Error opening MQTT connection to " + mqttConfig.getServerUri(),
					e);
		}
	}

	/**
	 * Callback to configure newly created MQTT connections.
	 * 
	 * <p>
	 * Extending classes can override this to do things like register as a
	 * message handler or connection observer. This implementation will
	 * automatically configure this object as the connection observer if it
	 * implements {@link MqttConnectionObserver}. It will also automatically
	 * configure this object as the message handler if it implements
	 * {@link MqttMessageHandler}.
	 * </p>
	 * 
	 * @param conn
	 *        the connection
	 */
	protected void connectionCreated(MqttConnection conn) {
		if ( this instanceof MqttConnectionObserver ) {
			conn.setConnectionObserver((MqttConnectionObserver) this);
		}
		if ( this instanceof MqttMessageHandler ) {
			conn.setMessageHandler((MqttMessageHandler) this);
		}
	}

	/**
	 * Call when no longer needed to release resources.
	 */
	public synchronized void shutdown() {
		if ( connection != null ) {
			try {
				connection.close();
			} catch ( IOException e ) {
				log.warn("Error closing MQTT connection to {}: {}", mqttConfig.getServerUri(),
						e.toString());
			} finally {
				connection = null;
			}
		}
	}

	/**
	 * Get the MQTT connection.
	 * 
	 * @return the connection
	 */
	protected MqttConnection connection() {
		return connection;
	}

	@Override
	public String getPingTestId() {
		return getClass().getName();
	}

	@Override
	public long getPingTestMaximumExecutionMilliseconds() {
		return 10000L;
	}

	@Override
	public Result performPingTest() throws Exception {
		MqttConnection conn = null;
		synchronized ( this ) {
			conn = this.connection;
		}
		if ( conn != null ) {
			if ( conn instanceof PingTest ) {
				return ((PingTest) conn).performPingTest();
			}
			boolean healthy = conn.isEstablished();
			URI serverUri = mqttConfig.getServerUri();
			String msg = (healthy ? "Connected to " + serverUri : "Not connected");
			Map<String, Object> props = Collections.singletonMap("serverUri", serverUri);
			PingTestResult result = new PingTestResult(healthy, msg, props);
			return result;
		}
		return new PingTestResult(false, "No MQTT connection available.");
	}

	/**
	 * Get the MQTT QOS to use when publishing.
	 * 
	 * @return the QOS, never {@literal null}
	 */
	public MqttQos getPublishQos() {
		return publishQos;
	}

	/**
	 * Set the MQTT QOS to use when publishing.
	 * 
	 * @param publishQos
	 *        the QOS
	 * @throws IllegalArgumentException
	 *         if {@code publishQos} is {@literal null}
	 */
	public void setPublishQos(MqttQos publishQos) {
		if ( publishQos == null ) {
			throw new IllegalArgumentException("The publishQos value must not be null.");
		}
		this.publishQos = publishQos;
	}

	/**
	 * Get the MQTT QOS value to use when publishing.
	 * 
	 * @return the QOS value
	 */
	public int getPublishQosValue() {
		return getPublishQos().getValue();
	}

	/**
	 * Set the MQTT QOS value to use when publishing.
	 * 
	 * @param value
	 *        the QOS value
	 */
	public void setPublishQosValue(int value) {
		try {
			setPublishQos(MqttQos.valueOf(value));
		} catch ( IllegalArgumentException e ) {
			setPublishQos(MqttQos.AtLeastOnce);
		}
	}

	/**
	 * Get the MQTT QOS to use when subscribing.
	 * 
	 * @return the QOS, never {@literal null}
	 */
	public MqttQos getSubscribeQos() {
		return subscribeQos;
	}

	/**
	 * Set the MQTT QOS to use when subscribing.
	 * 
	 * @param subscribeQos
	 *        the QOS
	 * @throws IllegalArgumentException
	 *         if {@code subscribeQos} is {@literal null}
	 */
	public void setSubscribeQos(MqttQos subscribeQos) {
		if ( subscribeQos == null ) {
			throw new IllegalArgumentException("The subscribeQos value must not be null.");
		}
		this.subscribeQos = subscribeQos;
	}

	/**
	 * Get the MQTT QOS value to use when subscribing.
	 * 
	 * @return the QOS value
	 */
	public int getSubscribeQosValue() {
		return getSubscribeQos().getValue();
	}

	/**
	 * Set the MQTT QOS value to use when subscribing.
	 * 
	 * @param value
	 *        the QOS value
	 */
	public void setSubscribeQosValue(int value) {
		try {
			setSubscribeQos(MqttQos.valueOf(value));
		} catch ( IllegalArgumentException e ) {
			setSubscribeQos(MqttQos.AtLeastOnce);
		}
	}

	/**
	 * Get the MQTT statistics.
	 * 
	 * @return the statistics
	 */
	public MqttStats getMqttStats() {
		return mqttConfig.getStats();
	}

	/**
	 * Get the MQTT configuration.
	 * 
	 * @return the configuration, never {@literal null}
	 */
	public BasicMqttConnectionConfig getMqttConfig() {
		return mqttConfig;
	}

	@Override
	public void setUid(String uid) {
		super.setUid(uid);
		mqttConfig.setUid(uid);
	}

}
