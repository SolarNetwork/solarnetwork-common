/* ==================================================================
 * NettyMqttConnectionTests.java - 26/11/2019 9:46:40 am
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

package net.solarnetwork.common.mqtt.netty.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import io.moquette.interception.messages.InterceptConnectMessage;
import net.solarnetwork.common.mqtt.BasicMqttConnectionConfig;
import net.solarnetwork.common.mqtt.BasicMqttMessage;
import net.solarnetwork.common.mqtt.MqttMessage;
import net.solarnetwork.common.mqtt.MqttMessageHandler;
import net.solarnetwork.common.mqtt.MqttQos;
import net.solarnetwork.common.mqtt.MqttStats;
import net.solarnetwork.common.mqtt.netty.NettyMqttConnection;
import net.solarnetwork.test.mqtt.MqttServerSupport;
import net.solarnetwork.test.mqtt.TestingInterceptHandler;

/**
 * Test cases for the {@link NettyMqttConnection} class.
 * 
 * @author matt
 * @version 1.0
 */
public class NettyMqttConnectionTests extends MqttServerSupport {

	private static final String TEST_CLIENT_ID = "solarnet.test";
	private static final int TIMEOUT_SECS = 10;

	private BasicMqttConnectionConfig config;
	private NettyMqttConnection service;

	@Before
	public void setup() throws Exception {
		setupMqttServer();

		config = new BasicMqttConnectionConfig();
		config.setServerUriValue("mqtt://localhost:" + getMqttServerPort());
		config.setClientId(TEST_CLIENT_ID);
		config.setConnectTimeoutSeconds(1);
		config.setReconnectDelaySeconds(1);
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setThreadNamePrefix("NettyMqtt-Scheduler-Test-");
		scheduler.initialize();
		service = new NettyMqttConnection(
				Executors.newCachedThreadPool(new CustomizableThreadFactory("NettyMqtt-Test-")),
				scheduler, config, new MqttStats("TEST", 5));
		service.setUid("Test Conn");
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
		config.setUsername(username);
		config.setPassword(password);
		config.setReconnect(false);

		replayAll();

		// when
		service.open().get(TIMEOUT_SECS, TimeUnit.SECONDS);

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
		config.setUsername(username);
		config.setPassword(password);
		config.setReconnect(true);

		replayAll();

		// when
		service.open().get(TIMEOUT_SECS, TimeUnit.SECONDS);

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
		config.setUsername(username);
		config.setPassword(password);
		config.setReconnect(true);
		config.setReconnectDelaySeconds(1);
		config.setServerUriValue("mqtt://localhost:" + mqttPort);

		replayAll();

		// when

		Thread initThread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					service.open().get(TIMEOUT_SECS, TimeUnit.DAYS);
				} catch ( InterruptedException | ExecutionException | TimeoutException
						| IOException e ) {
					log.info("Connection to MQTT failed: " + e.toString());
				}
			}
		});
		initThread.start();

		// sleep for a bit to allow background thread to attempt first connect
		Thread.sleep(2000);

		// bring up MQTT server now
		setupMqttServer(null, null, null, mqttPort);

		// wait for connection to finish
		initThread.join();

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
		config.setUsername(username);
		config.setPassword(password);
		config.setReconnect(true);
		config.setReconnectDelaySeconds(1);

		final TestingInterceptHandler session = getTestingInterceptHandler();

		replayAll();

		// when
		service.open().get(TIMEOUT_SECS, TimeUnit.SECONDS);

		// stop server
		stopMqttServer();

		Thread.sleep(200);

		// start server on new port, update configuration
		setupMqttServer(Collections.singletonList(session), null, null, getFreePort());
		config.setClientId("test.client.2");
		config.setServerUriValue("mqtt://localhost:" + getMqttServerPort());

		Future<?> f = service.reconfigure();
		f.get(TIMEOUT_SECS, TimeUnit.SECONDS);

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

	@Test
	public void reconnectToServerAfterConnectionDropped() throws Exception {
		// given
		final String username = UUID.randomUUID().toString();
		final String password = UUID.randomUUID().toString();
		config.setUsername(username);
		config.setPassword(password);
		config.setReconnect(true);

		final TestingInterceptHandler session = getTestingInterceptHandler();

		replayAll();

		// when
		service.open().get(TIMEOUT_SECS, TimeUnit.SECONDS);

		// stop server
		stopMqttServer();

		Thread.sleep(200);

		// start server on new port, update configuration
		setupMqttServer(Collections.singletonList(session), null, null, config.getPort());

		// chill for a while for auto-reconnect
		Thread.sleep(5000);

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
		assertThat("Connect client ID", connMsg.getClientID(), equalTo(TEST_CLIENT_ID));
		assertThat("Connect username", connMsg.getUsername(), equalTo(username));
		assertThat("Connect password", connMsg.getPassword(), equalTo(password.getBytes()));
		assertThat("Connect durable session", connMsg.isCleanSession(), equalTo(false));
	}

	private static final Charset UTF8 = Charset.forName("UTF-8");

	@Test
	public void publish() throws Exception {
		// given
		final String username = UUID.randomUUID().toString();
		final String password = UUID.randomUUID().toString();
		config.setUsername(username);
		config.setPassword(password);
		config.setReconnect(false);

		replayAll();

		// when
		service.open().get(TIMEOUT_SECS, TimeUnit.SECONDS);

		final String msg = "Hello, world.";
		service.publish(new BasicMqttMessage("foo", false, MqttQos.AtLeastOnce, msg.getBytes(UTF8)))
				.get(TIMEOUT_SECS, TimeUnit.SECONDS);

		stopMqttServer(); // to flush messages

		// then
		TestingInterceptHandler session = getTestingInterceptHandler();
		assertThat("Connected to broker", session.publishMessages, hasSize(1));

		String result = session.getPublishPayloadStringAtIndex(0);
		assertThat("Published message payload", result, equalTo(msg));
	}

	@Test
	public void subscribeWithChannelHandler() throws Exception {
		// given
		final String username = UUID.randomUUID().toString();
		final String password = UUID.randomUUID().toString();
		config.setUsername(username);
		config.setPassword(password);
		config.setReconnect(false);

		replayAll();

		// when
		service.open().get(TIMEOUT_SECS, TimeUnit.SECONDS);

		final List<MqttMessage> messages = new ArrayList<>(2);
		service.setMessageHandler(new MqttMessageHandler() {

			@Override
			public void onMqttMessage(MqttMessage message) {
				messages.add(message);
			}
		});

		Future<?> f = service.subscribe("foo", MqttQos.AtLeastOnce, null);
		f.get(TIMEOUT_SECS, TimeUnit.SECONDS);

		final String msg = "Hello, world.";
		final MqttMessage tx = new BasicMqttMessage("foo", false, MqttQos.AtLeastOnce,
				msg.getBytes(UTF8));
		f = service.publish(tx);
		f.get(TIMEOUT_SECS, TimeUnit.SECONDS);

		// give a little time for broker to publish to subscriber
		Thread.sleep(300);

		stopMqttServer(); // to flush messages

		// then
		assertThat("Message received", messages, hasSize(1));
		MqttMessage rx = messages.get(0);
		assertThat("Message topic", rx.getTopic(), equalTo(tx.getTopic()));
		assertThat("Message QoS", rx.getQosLevel(), equalTo(MqttQos.AtLeastOnce));
		assertThat("Message payload", new String(rx.getPayload(), UTF8), equalTo(msg));
	}

	@Test
	public void subscribeWithSubscriptionHandler() throws Exception {
		// given
		final String username = UUID.randomUUID().toString();
		final String password = UUID.randomUUID().toString();
		config.setUsername(username);
		config.setPassword(password);
		config.setReconnect(false);

		replayAll();

		// when
		service.open().get(TIMEOUT_SECS, TimeUnit.SECONDS);

		final List<MqttMessage> messages = new ArrayList<>(2);
		Future<?> f = service.subscribe("foo", MqttQos.AtLeastOnce, new MqttMessageHandler() {

			@Override
			public void onMqttMessage(MqttMessage message) {
				messages.add(message);
			}
		});
		f.get(TIMEOUT_SECS, TimeUnit.SECONDS);

		final String msg = "Hello, world.";
		final MqttMessage tx = new BasicMqttMessage("foo", false, MqttQos.AtLeastOnce,
				msg.getBytes(UTF8));
		service.publish(tx).get(TIMEOUT_SECS, TimeUnit.SECONDS);

		// give a little time for broker to publish to subscriber
		Thread.sleep(300);

		stopMqttServer(); // to flush messages

		// then
		assertThat("Message received", messages, hasSize(1));
		MqttMessage rx = messages.get(0);
		assertThat("Message topic", rx.getTopic(), equalTo(tx.getTopic()));
		assertThat("Message QoS", rx.getQosLevel(), equalTo(MqttQos.AtLeastOnce));
		assertThat("Message payload", new String(rx.getPayload(), UTF8), equalTo(msg));
	}

	@Test
	public void subscribeWithChannelAndSubscriptionHandler() throws Exception {
		// given
		final String username = UUID.randomUUID().toString();
		final String password = UUID.randomUUID().toString();
		config.setUsername(username);
		config.setPassword(password);
		config.setReconnect(false);

		replayAll();

		// when
		service.open().get(TIMEOUT_SECS, TimeUnit.SECONDS);

		final List<MqttMessage> messages = new ArrayList<>(2);

		AtomicBoolean b = new AtomicBoolean(false);
		service.setMessageHandler(new MqttMessageHandler() {

			@Override
			public void onMqttMessage(MqttMessage message) {
				messages.add(message);
				b.set(true);
			}
		});

		service.subscribe("foo", MqttQos.AtLeastOnce, new MqttMessageHandler() {

			@Override
			public void onMqttMessage(MqttMessage message) {
				messages.add(message);
			}
		}).get(TIMEOUT_SECS, TimeUnit.SECONDS);

		final String msg = "Hello, world.";
		final MqttMessage tx = new BasicMqttMessage("foo", false, MqttQos.AtLeastOnce,
				msg.getBytes(UTF8));
		service.publish(tx).get(TIMEOUT_SECS, TimeUnit.SECONDS);

		// give a little time for broker to publish to subscriber
		Thread.sleep(300);

		stopMqttServer(); // to flush messages

		// then
		assertThat("Message received", messages, hasSize(1));
		assertThat("Message received on subscription handler", b.get(), equalTo(false));
		MqttMessage rx = messages.get(0);
		assertThat("Message topic", rx.getTopic(), equalTo(tx.getTopic()));
		assertThat("Message QoS", rx.getQosLevel(), equalTo(MqttQos.AtLeastOnce));
		assertThat("Message payload", new String(rx.getPayload(), UTF8), equalTo(msg));
	}

	@Test
	public void unsubscribeWithChannelHandler() throws Exception {
		// given
		final String username = UUID.randomUUID().toString();
		final String password = UUID.randomUUID().toString();
		config.setUsername(username);
		config.setPassword(password);
		config.setReconnect(false);

		replayAll();

		// when
		service.open().get(TIMEOUT_SECS, TimeUnit.SECONDS);

		final List<MqttMessage> messages = new ArrayList<>(2);
		service.setMessageHandler(new MqttMessageHandler() {

			@Override
			public void onMqttMessage(MqttMessage message) {
				messages.add(message);
			}
		});

		Future<?> f = service.subscribe("foo", MqttQos.AtLeastOnce, null);
		f.get(TIMEOUT_SECS, TimeUnit.SECONDS);

		final String msg = "Hello, world.";
		final MqttMessage tx = new BasicMqttMessage("foo", false, MqttQos.AtLeastOnce,
				msg.getBytes(UTF8));
		f = service.publish(tx);
		f.get(TIMEOUT_SECS, TimeUnit.SECONDS);

		// unsubscribe
		f = service.unsubscribe("foo", null);
		f.get(TIMEOUT_SECS, TimeUnit.SECONDS);

		// publish again
		f = service.publish(tx);
		f.get(TIMEOUT_SECS, TimeUnit.SECONDS);

		// give a little time for broker to publish to subscriber
		Thread.sleep(300);

		stopMqttServer(); // to flush messages

		// then
		assertThat("Message received", messages, hasSize(1));
		MqttMessage rx = messages.get(0);
		assertThat("Message topic", rx.getTopic(), equalTo(tx.getTopic()));
		assertThat("Message QoS", rx.getQosLevel(), equalTo(MqttQos.AtLeastOnce));
		assertThat("Message payload", new String(rx.getPayload(), UTF8), equalTo(msg));
	}

}
