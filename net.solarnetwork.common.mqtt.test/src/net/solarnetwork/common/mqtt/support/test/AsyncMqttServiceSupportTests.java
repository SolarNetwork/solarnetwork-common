/* ==================================================================
 * AsyncMqttServiceSupportTests.java - 3/11/2019 6:52:23 am
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

package net.solarnetwork.common.mqtt.support.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.moquette.interception.messages.InterceptConnectMessage;
import net.solarnetwork.common.mqtt.support.AsyncMqttServiceSupport;
import net.solarnetwork.common.mqtt.support.MqttStats;
import net.solarnetwork.support.SSLService;
import net.solarnetwork.test.CallingThreadExecutorService;
import net.solarnetwork.test.mqtt.MqttServerSupport;
import net.solarnetwork.test.mqtt.TestingInterceptHandler;
import net.solarnetwork.util.OptionalService;

/**
 * Test cases for the {@link AsyncMqttServiceSupport} class.
 * 
 * @author matt
 * @version 1.0
 */
public class AsyncMqttServiceSupportTests extends MqttServerSupport {

	private static final String TEST_CLIENT_ID = "solarnet.test";

	private static class TestService extends AsyncMqttServiceSupport {

		public TestService(ExecutorService executorService, OptionalService<SSLService> sslService,
				boolean retryConnect, MqttStats stats, String serverUri, String clientId) {
			super(executorService, sslService, retryConnect, stats, serverUri, clientId);
		}

	}

	private ObjectMapper objectMapper;
	private TestService service;

	@Before
	public void setup() {
		setupMqttServer();

		objectMapper = new ObjectMapper();
		objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

		String serverUri = "mqtt://localhost:" + getMqttServerPort();
		service = new TestService(new CallingThreadExecutorService(), null, false,
				new MqttStats("test.mqtt", 10), serverUri, TEST_CLIENT_ID);
	}

	@Override
	@After
	public void teardown() {
		super.teardown();
		//EasyMock.verify(dataCollectorBiz);
	}

	private void replayAll() {
		//EasyMock.replay(dataCollectorBiz);
	}

	@Test
	public void connectToServer() throws Exception {
		// given
		final String username = UUID.randomUUID().toString();
		final String password = UUID.randomUUID().toString();
		service.setUsername(username);
		service.setPassword(password);

		replayAll();

		// when
		service.init();

		stopMqttServer(); // to flush messages

		// then
		TestingInterceptHandler session = getTestingInterceptHandler();
		assertThat("Connected to broker", session.connectMessages, hasSize(1));

		InterceptConnectMessage connMsg = session.connectMessages.get(0);
		assertThat("Connect client ID", connMsg.getClientID(), equalTo(TEST_CLIENT_ID));
		assertThat("Connect username", connMsg.getUsername(), equalTo(username));
		assertThat("Connect password", connMsg.getPassword(), equalTo(password.getBytes()));
		assertThat("Connect durable session", connMsg.isCleanSession(), equalTo(false));
	}

	@Test
	public void connectToServerWithRetryEnabled() throws Exception {
		// given
		final String username = UUID.randomUUID().toString();
		final String password = UUID.randomUUID().toString();
		service.setUsername(username);
		service.setPassword(password);
		service.setRetryConnect(true);

		replayAll();

		// when
		service.init();

		// sleep for a bit to allow background thread to connect
		Thread.sleep(1000);

		stopMqttServer(); // to flush messages

		// then
		TestingInterceptHandler session = getTestingInterceptHandler();
		assertThat("Connected to broker", session.connectMessages, hasSize(1));

		InterceptConnectMessage connMsg = session.connectMessages.get(0);
		assertThat("Connect client ID", connMsg.getClientID(), equalTo(TEST_CLIENT_ID));
		assertThat("Connect username", connMsg.getUsername(), equalTo(username));
		assertThat("Connect password", connMsg.getPassword(), equalTo(password.getBytes()));
		assertThat("Connect durable session", connMsg.isCleanSession(), equalTo(false));
	}

	@Test
	public void connectToServerWithRetryEnabledFirstConnectFails() throws Exception {
		stopMqttServer(); // start shut down
		final int mqttPort = getFreePort();

		// given
		final String username = UUID.randomUUID().toString();
		final String password = UUID.randomUUID().toString();
		service.setUsername(username);
		service.setPassword(password);
		service.setRetryConnect(true);
		service.setServerUri("mqtt://localhost:" + mqttPort);

		replayAll();

		// when

		// start in bg thread, because of CallingThreadExecutorService use
		Thread initThread = new Thread(new Runnable() {

			@Override
			public void run() {
				service.init();
			}
		});
		initThread.start();

		// sleep for a bit to allow background thread to attempt first connect
		Thread.sleep(200);

		// bring up MQTT server now
		setupMqttServer(null, null, null, mqttPort);

		// sleep for a bit to allow background thread to attempt second connect
		initThread.join(3000);

		stopMqttServer(); // to flush messages

		// then
		TestingInterceptHandler session = getTestingInterceptHandler();
		assertThat("Connected to broker", session.connectMessages, hasSize(1));

		InterceptConnectMessage connMsg = session.connectMessages.get(0);
		assertThat("Connect client ID", connMsg.getClientID(), equalTo(TEST_CLIENT_ID));
		assertThat("Connect username", connMsg.getUsername(), equalTo(username));
		assertThat("Connect password", connMsg.getPassword(), equalTo(password.getBytes()));
		assertThat("Connect durable session", connMsg.isCleanSession(), equalTo(false));
	}

	@Test
	public void reconnectToServerAfterConfigChangeWithRetryEnabled() throws Exception {
		// given
		final String username = UUID.randomUUID().toString();
		final String password = UUID.randomUUID().toString();
		service.setUsername(username);
		service.setPassword(password);
		service.setRetryConnect(true);

		final TestingInterceptHandler session = getTestingInterceptHandler();

		replayAll();

		// when
		service.init();

		// sleep for a bit to allow background thread to connect
		Thread.sleep(1000);

		// stop server
		stopMqttServer();

		Thread.sleep(200);

		// start server on new port, update configuration
		setupMqttServer(Collections.singletonList(session), null, null, getFreePort());
		service.setClientId("test.client.2");
		service.setServerUri("mqtt://localhost:" + getMqttServerPort());
		service.init();

		// sleep for a bit to allow background thread to connect
		Thread.sleep(1000);

		// stop server to flush messages
		stopMqttServer();

		// then
		assertThat("Connected to broker", session.connectMessages, hasSize(2));

		InterceptConnectMessage connMsg = session.connectMessages.get(0);
		assertThat("Connect client ID", connMsg.getClientID(), equalTo(TEST_CLIENT_ID));
		assertThat("Connect username", connMsg.getUsername(), equalTo(username));
		assertThat("Connect password", connMsg.getPassword(), equalTo(password.getBytes()));
		assertThat("Connect durable session", connMsg.isCleanSession(), equalTo(false));

		connMsg = session.connectMessages.get(1);
		assertThat("Connect client ID", connMsg.getClientID(), equalTo("test.client.2"));
		assertThat("Connect username", connMsg.getUsername(), equalTo(username));
		assertThat("Connect password", connMsg.getPassword(), equalTo(password.getBytes()));
		assertThat("Connect durable session", connMsg.isCleanSession(), equalTo(false));
	}
}
