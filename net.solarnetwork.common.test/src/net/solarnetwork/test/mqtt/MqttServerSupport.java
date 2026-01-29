/* ==================================================================
 * MqttServerSupport.java - 8/06/2018 2:20:24 PM
 *
 * Copyright 2018 SolarNetwork.net Dev Team
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

package net.solarnetwork.test.mqtt;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.After;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.moquette.BrokerConstants;
import io.moquette.broker.Server;
import io.moquette.broker.config.MemoryConfig;
import io.moquette.broker.security.IAuthenticator;
import io.moquette.broker.security.IAuthorizatorPolicy;
import io.moquette.interception.InterceptHandler;

/**
 * Support for MQTT client integration with an embedded MQTT server.
 *
 * <p>
 * Unit tests can extend this class directly.
 * </p>
 *
 * @author matt
 * @version 1.1
 */
public class MqttServerSupport {

	/** A class-level logger. */
	protected final Logger log = LoggerFactory.getLogger(getClass());

	/** The MQTT server. */
	protected Server mqttServer;

	/** The MQTT client. */
	protected MqttClient mqttClient;

	private Properties mqttServerProperties;
	private TestingInterceptHandler testingHandler;

	/**
	 * Constructor.
	 */
	public MqttServerSupport() {
		super();
	}

	/**
	 * Find and return an unused IP port.
	 *
	 * @return the unused port
	 * @throws RuntimeException
	 *         if no unused port can be found
	 */
	protected static final int getFreePort() {
		try (ServerSocket ss = new ServerSocket(0)) {
			ss.setReuseAddress(true);
			return ss.getLocalPort();
		} catch ( IOException e ) {
			throw new RuntimeException("Unable to find unused port");
		}
	}

	/**
	 * Create basic MQTT properties.
	 *
	 * <p>
	 * The {@link BrokerConstants#PORT_PROPERTY_NAME} and
	 * {@link BrokerConstants#HOST_PROPERTY_NAME} values will be populated.
	 * </p>
	 *
	 * @param port
	 *        the port to use
	 * @return the server properties
	 */
	protected Properties createMqttServerProperties(int port) {
		if ( port < 1 ) {
			port = getFreePort();
		}
		Properties p = new Properties();
		p.setProperty(BrokerConstants.PORT_PROPERTY_NAME, String.valueOf(port));
		p.setProperty(BrokerConstants.HOST_PROPERTY_NAME, "127.0.0.1");
		p.setProperty(BrokerConstants.ENABLE_TELEMETRY_NAME, Boolean.FALSE.toString());
		return p;
	}

	/**
	 * Setup an embedded MQTT server.
	 *
	 * @param handlers
	 *        the handlers
	 * @param authenticator
	 *        the authenticator
	 * @param authorizator
	 *        the authorizator
	 * @param port
	 *        the listen port
	 */
	public void setupMqttServer(List<InterceptHandler> handlers, IAuthenticator authenticator,
			IAuthorizatorPolicy authorizator, int port) {
		testingHandler = null;
		if ( handlers == null ) {
			testingHandler = new TestingInterceptHandler();
			handlers = Collections.singletonList(testingHandler);
		}
		Server s = new Server();
		Properties p = createMqttServerProperties(port);
		log.debug("Starting MQTT server with props {}", p);
		s.startServer(new MemoryConfig(p), handlers, null, authenticator, authorizator);
		mqttServer = s;
		mqttServerProperties = p;
	}

	/**
	 * Setup an embedded MQTT server.
	 */
	public void setupMqttServer() {
		setupMqttServer(null, null, null, 0);
	}

	/**
	 * Get the port the MQTT server is listing on.
	 *
	 * @return the port
	 */
	public int getMqttServerPort() {
		if ( mqttServerProperties == null ) {
			return 1883;
		}
		String port = mqttServerProperties.getProperty(BrokerConstants.PORT_PROPERTY_NAME, "1883");
		return Integer.parseInt(port);
	}

	/**
	 * Get the default testing handler, if no specific handlers have been passed
	 * to
	 * {@link #setupMqttServer(List, IAuthenticator, IAuthorizatorPolicy, int)}.
	 *
	 * @return the testing handler
	 */
	public TestingInterceptHandler getTestingInterceptHandler() {
		return testingHandler;
	}

	/**
	 * Shut down the embedded MQTT server.
	 */
	public void stopMqttServer() {
		if ( mqttServer != null ) {
			mqttServer.stopServer();
			mqttServer = null;
			mqttServerProperties = null;
		}
	}

	/**
	 * Get the embedded MQTT server.
	 *
	 * @return the server
	 */
	public Server getServer() {
		return mqttServer;
	}

	/**
	 * Get an MQTT client.
	 *
	 * @return the client
	 */
	public IMqttClient getClient() {
		return mqttClient;
	}

	/**
	 * Setup a MQTT client.
	 *
	 * @param clientId
	 *        the client ID
	 * @param callback
	 *        the callback
	 */
	public void setupMqttClient(String clientId, MqttCallback callback) {
		if ( mqttClient != null ) {
			try {
				mqttClient.close(true);
			} catch ( MqttException e ) {
				log.info("Exception closing MQTT client: {}", e.getMessage());
			}
		}
		int port = getMqttServerPort();
		try {
			MemoryPersistence persistence = new MemoryPersistence();
			MqttClient client = new MqttClient("tcp://127.0.0.1:" + port, clientId, persistence);
			client.setCallback(callback);
			MqttConnectOptions connOptions = new MqttConnectOptions();
			connOptions.setCleanSession(false);
			connOptions.setAutomaticReconnect(false);
			client.connect(connOptions);
			mqttClient = client;
		} catch ( MqttException e ) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Perform cleanup (stop the MQTT server).
	 */
	@After
	public void teardown() {
		stopMqttServer();
	}

}
