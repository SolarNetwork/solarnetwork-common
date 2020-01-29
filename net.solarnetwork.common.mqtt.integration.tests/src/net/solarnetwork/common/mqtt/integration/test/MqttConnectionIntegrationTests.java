/* ==================================================================
 * MqttConnectionIntegrationTests.java - 27/11/2019 11:39:02 am
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

package net.solarnetwork.common.mqtt.integration.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import io.moquette.interception.messages.InterceptConnectMessage;
import net.solarnetwork.common.mqtt.BasicMqttConnectionConfig;
import net.solarnetwork.common.mqtt.BasicMqttMessage;
import net.solarnetwork.common.mqtt.MqttConnection;
import net.solarnetwork.common.mqtt.MqttConnectionObserver;
import net.solarnetwork.common.mqtt.MqttMessage;
import net.solarnetwork.common.mqtt.MqttMessageHandler;
import net.solarnetwork.common.mqtt.MqttQos;
import net.solarnetwork.common.mqtt.ReconfigurableMqttConnection;
import net.solarnetwork.test.mqtt.MqttServerSupport;
import net.solarnetwork.test.mqtt.TestingInterceptHandler;

/**
 * Common integration tests for {@link MqttConnection} implementations.
 * 
 * <p>
 * Extending classes must call {@link #setService(MqttConnection)} before the
 * tests start.
 * </p>
 * 
 * @author matt
 * @version 1.0
 */
public abstract class MqttConnectionIntegrationTests extends MqttServerSupport {

	private static final String TEST_CLIENT_ID = "solarnet.test";
	private static final int TIMEOUT_SECS = 20;
	private static final Charset UTF8 = Charset.forName("UTF-8");

	protected BasicMqttConnectionConfig config;
	private MqttConnection service;

	public static final class CountDownConnectionObserver implements MqttConnectionObserver {

		private final CountDownLatch latch;
		private final AtomicInteger lostCounter;
		private final AtomicInteger estCounter;

		private CountDownConnectionObserver(CountDownLatch latch) {
			this(latch, null, null);
		}

		private CountDownConnectionObserver(CountDownLatch latch, AtomicInteger lostCounter,
				AtomicInteger estCounter) {
			this.latch = latch;
			this.lostCounter = lostCounter;
			this.estCounter = estCounter;
		}

		@Override
		public void onMqttServerConnectionLost(MqttConnection connection, boolean willReconnect,
				Throwable cause) {
			if ( lostCounter != null ) {
				lostCounter.incrementAndGet();
			}
		}

		@Override
		public void onMqttServerConnectionEstablisehd(MqttConnection connection, boolean reconnected) {
			if ( estCounter != null ) {
				estCounter.incrementAndGet();
			}
			latch.countDown();
		}
	}

	@Before
	public void setup() throws Exception {
		setupMqttServer();

		config = new BasicMqttConnectionConfig();
		config.setServerUriValue("mqtt://localhost:" + getMqttServerPort());
		config.setClientId(TEST_CLIENT_ID);
		config.setConnectTimeoutSeconds(1);
		config.setReconnectDelaySeconds(1);
	}

	/**
	 * Call to configure the {@link MqttConnection} to test.
	 * 
	 * @param conn
	 *        the connection
	 */
	protected void setService(MqttConnection conn) {
		this.service = conn;
	}

	@Test
	public void connectToServer() throws Exception {
		// given
		final String username = UUID.randomUUID().toString();
		final String password = UUID.randomUUID().toString();
		config.setUsername(username);
		config.setPassword(password);
		config.setReconnect(false);

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
		assertThat("Connect durable session", connMsg.isCleanSession(), equalTo(true));
	}

	@Test
	public void connectToServerWithoutCleanSession() throws Exception {
		// given
		final String username = UUID.randomUUID().toString();
		final String password = UUID.randomUUID().toString();
		config.setUsername(username);
		config.setPassword(password);
		config.setReconnect(false);
		config.setCleanSession(false);

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
		assertThat("Connect durable session", connMsg.isCleanSession(), equalTo(true));
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
		assertThat("Connect durable session", connMsg.isCleanSession(), equalTo(true));
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

		// when
		service.open().get(TIMEOUT_SECS, TimeUnit.SECONDS);

		// stop server
		stopMqttServer();

		Thread.sleep(200);

		// start server on new port, update configuration
		setupMqttServer(Collections.singletonList(session), null, null, getFreePort());
		config.setClientId("test.client.2");
		config.setServerUriValue("mqtt://localhost:" + getMqttServerPort());

		if ( service instanceof ReconfigurableMqttConnection ) {
			Future<?> f = ((ReconfigurableMqttConnection) service).reconfigure();
			f.get(TIMEOUT_SECS * 2, TimeUnit.SECONDS);
		}

		// stop server to flush messages
		stopMqttServer();

		// then
		assertThat("Connected to broker", session.connectMessages, hasSize(2));

		InterceptConnectMessage connMsg = session.connectMessages.get(0);
		assertThat("Connect client ID", connMsg.getClientID(), equalTo(TEST_CLIENT_ID));
		assertThat("Connect username", connMsg.getUsername(), equalTo(username));
		assertThat("Connect password", connMsg.getPassword(), equalTo(password.getBytes()));
		assertThat("Connect durable session", connMsg.isCleanSession(), equalTo(true));

		connMsg = session.connectMessages.get(1);
		assertThat("Connect client ID", connMsg.getClientID(), equalTo("test.client.2"));
		assertThat("Connect username", connMsg.getUsername(), equalTo(username));
		assertThat("Connect password", connMsg.getPassword(), equalTo(password.getBytes()));
		assertThat("Connect durable session", connMsg.isCleanSession(), equalTo(true));
	}

	@Test
	public void reconnectToServerAfterConnectionDroppedWithRetryEnabled() throws Exception {
		// given
		final String username = UUID.randomUUID().toString();
		final String password = UUID.randomUUID().toString();
		config.setUsername(username);
		config.setPassword(password);
		config.setReconnect(true);

		final TestingInterceptHandler session = getTestingInterceptHandler();

		// when
		final CountDownLatch connectLatch = new CountDownLatch(2);
		service.setConnectionObserver(new CountDownConnectionObserver(connectLatch));

		service.open().get(TIMEOUT_SECS, TimeUnit.SECONDS);

		// stop server
		stopMqttServer();

		Thread.sleep(200);

		// start server on new port, update configuration
		setupMqttServer(Collections.singletonList(session), null, null, config.getPort());

		// chill for a while for auto-reconnect
		boolean reconnected = connectLatch.await(TIMEOUT_SECS, TimeUnit.SECONDS);
		assertThat("Reconnected", reconnected, equalTo(true));

		// stop server to flush messages
		stopMqttServer();

		// then
		assertThat("Connected to broker", session.connectMessages, hasSize(2));

		InterceptConnectMessage connMsg = session.connectMessages.get(0);
		assertThat("Connect client ID", connMsg.getClientID(), equalTo(TEST_CLIENT_ID));
		assertThat("Connect username", connMsg.getUsername(), equalTo(username));
		assertThat("Connect password", connMsg.getPassword(), equalTo(password.getBytes()));
		assertThat("Connect durable session", connMsg.isCleanSession(), equalTo(true));

		connMsg = session.connectMessages.get(1);
		assertThat("Connect client ID", connMsg.getClientID(), equalTo(TEST_CLIENT_ID));
		assertThat("Connect username", connMsg.getUsername(), equalTo(username));
		assertThat("Connect password", connMsg.getPassword(), equalTo(password.getBytes()));
		assertThat("Connect durable session", connMsg.isCleanSession(), equalTo(true));
	}

	@Test
	public void publish() throws Exception {
		// given
		final String username = UUID.randomUUID().toString();
		final String password = UUID.randomUUID().toString();
		config.setUsername(username);
		config.setPassword(password);
		config.setReconnect(false);

		// when
		service.open().get(TIMEOUT_SECS, TimeUnit.SECONDS);

		final String msg = "Hello, world.";
		service.publish(new BasicMqttMessage("foo", false, MqttQos.AtLeastOnce, msg.getBytes(UTF8)))
				.get(TIMEOUT_SECS, TimeUnit.SECONDS);

		stopMqttServer(); // to flush messages

		// then
		TestingInterceptHandler session = getTestingInterceptHandler();
		assertThat("Published a message", session.publishMessages, hasSize(1));

		String result = session.getPublishPayloadStringAtIndex(0);
		assertThat("Published message payload", result, equalTo(msg));
	}

	private void publishConcurrently(final MqttQos qos) throws Exception {
		// given
		final String username = UUID.randomUUID().toString();
		final String password = UUID.randomUUID().toString();
		config.setUsername(username);
		config.setPassword(password);
		config.setReconnect(false);

		final int numThreads = 4, msgCount = numThreads * 10;
		ExecutorService executor = Executors.newFixedThreadPool(numThreads,
				new CustomizableThreadFactory("MQTT-Int-Pub-"));

		// when
		service.open().get(TIMEOUT_SECS, TimeUnit.SECONDS);

		final String msg = "Hello, world: %d";
		final List<Future<?>> publishFutures = new ArrayList<>(msgCount);
		try {
			for ( int i = 0; i < msgCount; i++ ) {
				final int count = i + 1;
				executor.submit(new Runnable() {

					@Override
					public void run() {
						Future<?> f = service.publish(new BasicMqttMessage("foo", false, qos,
								String.format(msg, count).getBytes(UTF8)));
						publishFutures.add(f);
					}
				});
			}
		} finally {
			executor.shutdown();
		}
		executor.awaitTermination(1, TimeUnit.MINUTES);

		final long giveUpAt = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(1);
		while ( !publishFutures.isEmpty() && (System.currentTimeMillis() < giveUpAt) ) {
			for ( Iterator<Future<?>> itr = publishFutures.iterator(); itr.hasNext(); ) {
				Future<?> f = itr.next();
				if ( f.isDone() ) {
					itr.remove();
				}
			}
			if ( !publishFutures.isEmpty() ) {
				log.debug("Waiting for {} message publications to complete...", publishFutures.size());
				Thread.sleep(400L);
			}
		}

		stopMqttServer(); // to flush messages

		// then
		assertThat("All messages completed publishing", publishFutures, hasSize(0));

		TestingInterceptHandler session = getTestingInterceptHandler();
		assertThat("Published " + msgCount + " messages", session.publishMessages, hasSize(msgCount));

		String result = session.getPublishPayloadStringAtIndex(0);
		assertThat("Published message payload", result, startsWith("Hello, world: "));
	}

	@Test
	public void publishConcurrently_qos1() throws Exception {
		publishConcurrently(MqttQos.AtLeastOnce);
	}

	@Test
	public void publishConcurrently_qos2() throws Exception {
		publishConcurrently(MqttQos.ExactlyOnce);
	}

	@Test
	public void subscribeWithChannelHandler() throws Exception {
		// given
		final String username = UUID.randomUUID().toString();
		final String password = UUID.randomUUID().toString();
		config.setUsername(username);
		config.setPassword(password);
		config.setReconnect(false);

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
	public void subscribeWithChannelAndSubscriptionHandler() throws Exception {
		// given
		final String username = UUID.randomUUID().toString();
		final String password = UUID.randomUUID().toString();
		config.setUsername(username);
		config.setPassword(password);
		config.setReconnect(false);

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

	@Test
	public void unsubscribeWithSubscriptionHandler() throws Exception {
		// given
		final String username = UUID.randomUUID().toString();
		final String password = UUID.randomUUID().toString();
		config.setUsername(username);
		config.setPassword(password);
		config.setReconnect(false);

		// when
		service.open().get(TIMEOUT_SECS, TimeUnit.SECONDS);

		final List<MqttMessage> messages = new ArrayList<>(2);
		MqttMessageHandler msgHandler = new MqttMessageHandler() {

			@Override
			public void onMqttMessage(MqttMessage message) {
				messages.add(message);
			}
		};
		Future<?> f = service.subscribe("foo", MqttQos.AtLeastOnce, msgHandler);
		f.get(TIMEOUT_SECS, TimeUnit.SECONDS);

		final String msg = "Hello, world.";
		final MqttMessage tx = new BasicMqttMessage("foo", false, MqttQos.AtLeastOnce,
				msg.getBytes(UTF8));
		f = service.publish(tx);
		f.get(TIMEOUT_SECS, TimeUnit.SECONDS);

		// unsubscribe
		f = service.unsubscribe("foo", msgHandler);
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

	@Test
	public void subscribeMultiWithChandleHandler() throws Exception {
		// given
		final String username = UUID.randomUUID().toString();
		final String password = UUID.randomUUID().toString();
		config.setUsername(username);
		config.setPassword(password);
		config.setReconnect(false);

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

		f = service.subscribe("bar", MqttQos.AtLeastOnce, null);
		f.get(TIMEOUT_SECS, TimeUnit.SECONDS);

		final String msg = "Hello, world.";
		final MqttMessage tx = new BasicMqttMessage("foo", false, MqttQos.AtLeastOnce,
				msg.getBytes(UTF8));
		service.publish(tx).get(TIMEOUT_SECS, TimeUnit.SECONDS);

		// give a little time for broker to publish to subscriber
		Thread.sleep(200);

		final String msg2 = "Goodbye, world.";
		final MqttMessage tx2 = new BasicMqttMessage("bar", false, MqttQos.AtLeastOnce,
				msg2.getBytes(UTF8));
		service.publish(tx2).get(TIMEOUT_SECS, TimeUnit.SECONDS);

		// give a little time for broker to publish to subscriber
		Thread.sleep(300);

		stopMqttServer(); // to flush messages

		// then
		assertThat("Message received", messages, hasSize(2));
		MqttMessage rx = messages.get(0);
		assertThat("Message topic", rx.getTopic(), equalTo(tx.getTopic()));
		assertThat("Message QoS", rx.getQosLevel(), equalTo(MqttQos.AtLeastOnce));
		assertThat("Message payload", new String(rx.getPayload(), UTF8), equalTo(msg));

		rx = messages.get(1);
		assertThat("Message topic", rx.getTopic(), equalTo(tx2.getTopic()));
		assertThat("Message QoS", rx.getQosLevel(), equalTo(MqttQos.AtLeastOnce));
		assertThat("Message payload", new String(rx.getPayload(), UTF8), equalTo(msg2));
	}

	@Test
	public void unsubscribeMultiWithChandleHandler() throws Exception {
		// given
		final String username = UUID.randomUUID().toString();
		final String password = UUID.randomUUID().toString();
		config.setUsername(username);
		config.setPassword(password);
		config.setReconnect(false);

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

		f = service.subscribe("bar", MqttQos.AtLeastOnce, null);
		f.get(TIMEOUT_SECS, TimeUnit.SECONDS);

		final String msg = "Hello, world.";
		final MqttMessage tx = new BasicMqttMessage("foo", false, MqttQos.AtLeastOnce,
				msg.getBytes(UTF8));
		service.publish(tx).get(TIMEOUT_SECS, TimeUnit.SECONDS);

		// give a little time for broker to publish to subscriber
		Thread.sleep(200);

		final String msg2 = "Goodbye, world.";
		final MqttMessage tx2 = new BasicMqttMessage("bar", false, MqttQos.AtLeastOnce,
				msg2.getBytes(UTF8));
		service.publish(tx2).get(TIMEOUT_SECS, TimeUnit.SECONDS);

		// give a little time for broker to publish to subscriber
		Thread.sleep(200);

		// unsubscribe
		f = service.unsubscribe("foo", null);
		f.get(TIMEOUT_SECS, TimeUnit.SECONDS);

		// publish again
		f = service.publish(tx);
		f.get(TIMEOUT_SECS, TimeUnit.SECONDS);

		f = service.publish(tx2);
		f.get(TIMEOUT_SECS, TimeUnit.SECONDS);

		// give a little time for broker to publish to subscriber
		Thread.sleep(300);

		stopMqttServer(); // to flush messages

		// then
		assertThat("Message received", messages, hasSize(3));
		MqttMessage rx = messages.get(0);
		assertThat("Message topic", rx.getTopic(), equalTo(tx.getTopic()));
		assertThat("Message QoS", rx.getQosLevel(), equalTo(MqttQos.AtLeastOnce));
		assertThat("Message payload", new String(rx.getPayload(), UTF8), equalTo(msg));

		rx = messages.get(1);
		assertThat("Message topic", rx.getTopic(), equalTo(tx2.getTopic()));
		assertThat("Message QoS", rx.getQosLevel(), equalTo(MqttQos.AtLeastOnce));
		assertThat("Message payload", new String(rx.getPayload(), UTF8), equalTo(msg2));

		rx = messages.get(2);
		assertThat("Message topic", rx.getTopic(), equalTo(tx2.getTopic()));
		assertThat("Message QoS", rx.getQosLevel(), equalTo(MqttQos.AtLeastOnce));
		assertThat("Message payload", new String(rx.getPayload(), UTF8), equalTo(msg2));
	}

	@Test
	public void reconnectToServerAfterConnectionDroppedWithRetryEnabledAfterSubscribe()
			throws Exception {
		// given
		final String username = UUID.randomUUID().toString();
		final String password = UUID.randomUUID().toString();
		config.setUsername(username);
		config.setPassword(password);
		config.setReconnect(true);

		final TestingInterceptHandler session = getTestingInterceptHandler();

		// when
		final CountDownLatch connectLatch = new CountDownLatch(2);
		service.setConnectionObserver(new CountDownConnectionObserver(connectLatch));

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
		service.publish(tx).get(TIMEOUT_SECS, TimeUnit.SECONDS);

		// give a little time for broker to publish to subscriber
		Thread.sleep(200);

		// stop server
		stopMqttServer();

		Thread.sleep(200);

		// start server on new port, update configuration
		setupMqttServer(Collections.singletonList(session), null, null, config.getPort());

		// chill for a while for auto-reconnect
		boolean reconnected = connectLatch.await(TIMEOUT_SECS, TimeUnit.SECONDS);
		assertThat("Reconnected", reconnected, equalTo(true));

		final String msg2 = "Goodbye, world.";
		final MqttMessage tx2 = new BasicMqttMessage("foo", false, MqttQos.AtLeastOnce,
				msg2.getBytes(UTF8));
		f = service.publish(tx2);
		f.get(TIMEOUT_SECS, TimeUnit.SECONDS);

		// stop server to flush messages
		stopMqttServer();

		// then
		assertThat("Connected to broker", session.connectMessages, hasSize(2));

		InterceptConnectMessage connMsg = session.connectMessages.get(0);
		assertThat("Connect client ID", connMsg.getClientID(), equalTo(TEST_CLIENT_ID));
		assertThat("Connect username", connMsg.getUsername(), equalTo(username));
		assertThat("Connect password", connMsg.getPassword(), equalTo(password.getBytes()));
		assertThat("Connect durable session", connMsg.isCleanSession(), equalTo(true));

		connMsg = session.connectMessages.get(1);
		assertThat("Connect client ID", connMsg.getClientID(), equalTo(TEST_CLIENT_ID));
		assertThat("Connect username", connMsg.getUsername(), equalTo(username));
		assertThat("Connect password", connMsg.getPassword(), equalTo(password.getBytes()));
		assertThat("Connect durable session", connMsg.isCleanSession(), equalTo(true));

		// 2nd message lost, as subscription not restored
		assertThat("Message received", messages, hasSize(1));
		MqttMessage rx = messages.get(0);
		assertThat("Message topic", rx.getTopic(), equalTo(tx.getTopic()));
		assertThat("Message QoS", rx.getQosLevel(), equalTo(MqttQos.AtLeastOnce));
		assertThat("Message payload", new String(rx.getPayload(), UTF8), equalTo(msg));
	}

	@Test
	public void reconnectToServerAfterConnectionDroppedWithRetryEnabledAfterSubscribeWithObserver()
			throws Exception {
		// given
		final String username = UUID.randomUUID().toString();
		final String password = UUID.randomUUID().toString();
		config.setUsername(username);
		config.setPassword(password);
		config.setReconnect(true);

		final TestingInterceptHandler session = getTestingInterceptHandler();

		// when
		final AtomicInteger lostCounter = new AtomicInteger(0);
		final AtomicInteger estCounter = new AtomicInteger(0);
		final CountDownLatch connectLatch = new CountDownLatch(2);
		service.setConnectionObserver(new MqttConnectionObserver() {

			@Override
			public void onMqttServerConnectionLost(MqttConnection connection, boolean willReconnect,
					Throwable cause) {
				lostCounter.incrementAndGet();
			}

			@Override
			public void onMqttServerConnectionEstablisehd(MqttConnection connection,
					boolean reconnected) {
				estCounter.incrementAndGet();
				connectLatch.countDown();
				service.subscribe("foo", MqttQos.AtLeastOnce, null);
			}
		});

		service.open().get(TIMEOUT_SECS, TimeUnit.SECONDS);

		final List<MqttMessage> messages = new ArrayList<>(2);
		service.setMessageHandler(new MqttMessageHandler() {

			@Override
			public void onMqttMessage(MqttMessage message) {
				messages.add(message);
			}
		});

		final String msg = "Hello, world.";
		final MqttMessage tx = new BasicMqttMessage("foo", false, MqttQos.AtLeastOnce,
				msg.getBytes(UTF8));
		service.publish(tx).get(TIMEOUT_SECS, TimeUnit.SECONDS);

		// give a little time for broker to publish to subscriber
		Thread.sleep(200);

		// stop server
		stopMqttServer();

		Thread.sleep(200);

		// start server on new port, update configuration
		setupMqttServer(Collections.singletonList(session), null, null, config.getPort());

		// chill for a while for auto-reconnect
		boolean reconnected = connectLatch.await(TIMEOUT_SECS, TimeUnit.SECONDS);
		assertThat("Reconnected", reconnected, equalTo(true));

		final String msg2 = "Goodbye, world.";
		final MqttMessage tx2 = new BasicMqttMessage("foo", false, MqttQos.AtLeastOnce,
				msg2.getBytes(UTF8));
		Future<?> f = service.publish(tx2);
		f.get(TIMEOUT_SECS, TimeUnit.SECONDS);

		Thread.sleep(200);

		// stop server to flush messages
		service.setConnectionObserver(null);
		stopMqttServer();

		// then
		assertThat("Connected to broker", session.connectMessages, hasSize(2));

		InterceptConnectMessage connMsg = session.connectMessages.get(0);
		assertThat("Connect client ID", connMsg.getClientID(), equalTo(TEST_CLIENT_ID));
		assertThat("Connect username", connMsg.getUsername(), equalTo(username));
		assertThat("Connect password", connMsg.getPassword(), equalTo(password.getBytes()));
		assertThat("Connect durable session", connMsg.isCleanSession(), equalTo(true));

		connMsg = session.connectMessages.get(1);
		assertThat("Connect client ID", connMsg.getClientID(), equalTo(TEST_CLIENT_ID));
		assertThat("Connect username", connMsg.getUsername(), equalTo(username));
		assertThat("Connect password", connMsg.getPassword(), equalTo(password.getBytes()));
		assertThat("Connect durable session", connMsg.isCleanSession(), equalTo(true));

		// observer invoked
		assertThat("Connection established callback called", estCounter.get(), equalTo(2));
		assertThat("Connection lost callback called", lostCounter.get(), equalTo(1));

		// 2nd message NOT lost, as subscription was restored by observer
		assertThat("Message received", messages, hasSize(2));
		MqttMessage rx = messages.get(0);
		assertThat("Message topic", rx.getTopic(), equalTo(tx.getTopic()));
		assertThat("Message QoS", rx.getQosLevel(), equalTo(MqttQos.AtLeastOnce));
		assertThat("Message payload", new String(rx.getPayload(), UTF8), equalTo(msg));

		rx = messages.get(1);
		assertThat("Message topic", rx.getTopic(), equalTo(tx2.getTopic()));
		assertThat("Message QoS", rx.getQosLevel(), equalTo(MqttQos.AtLeastOnce));
		assertThat("Message payload", new String(rx.getPayload(), UTF8), equalTo(msg2));
	}

}
