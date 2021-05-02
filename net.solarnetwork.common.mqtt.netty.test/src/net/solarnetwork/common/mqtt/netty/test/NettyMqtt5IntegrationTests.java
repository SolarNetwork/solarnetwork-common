/* ==================================================================
 * NettyMqtt5IntegrationTests.java - 2/05/2021 7:27:49 AM
 * 
 * Copyright 2021 SolarNetwork.net Dev Team
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
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import io.netty.util.CharsetUtil;
import net.solarnetwork.common.mqtt.BasicMqttConnectionConfig;
import net.solarnetwork.common.mqtt.BasicMqttMessage;
import net.solarnetwork.common.mqtt.BasicMqttProperty;
import net.solarnetwork.common.mqtt.MqttConnectReturnCode;
import net.solarnetwork.common.mqtt.MqttConnection;
import net.solarnetwork.common.mqtt.MqttMessage;
import net.solarnetwork.common.mqtt.MqttMessageHandler;
import net.solarnetwork.common.mqtt.MqttPropertyType;
import net.solarnetwork.common.mqtt.MqttQos;
import net.solarnetwork.common.mqtt.MqttStats;
import net.solarnetwork.common.mqtt.MqttVersion;
import net.solarnetwork.common.mqtt.netty.NettyMqttConnection;
import net.solarnetwork.test.SystemPropertyMatchTestRule;

/**
 * Test cases for Netty MQTT 5 integration.
 * 
 * @author matt
 * @version 1.0
 */
public class NettyMqtt5IntegrationTests {

	/** Only run when the {@code protoc-int} system property is defined. */
	@ClassRule
	public static SystemPropertyMatchTestRule PROFILE_RULE = new SystemPropertyMatchTestRule(
			"mqtt5-int");

	private static final Properties TEST_PROPS = loadTestProperties();

	private static final int TIMEOUT_SECS = 5;

	protected BasicMqttConnectionConfig config;
	private MqttConnection service;

	public static Properties loadTestProperties() {
		Properties p = new Properties();
		try {
			InputStream in = SystemPropertyMatchTestRule.class.getClassLoader()
					.getResourceAsStream("mqtt.properties");
			if ( in != null ) {
				p.load(in);
				in.close();
			}
		} catch ( IOException e ) {
			throw new RuntimeException(e);
		}
		return p;
	}

	@Before
	public void setup() throws Exception {
		config = new BasicMqttConnectionConfig();
		config.setServerUriValue(TEST_PROPS.getProperty("mqtt.url"));
		config.setClientId(TEST_PROPS.getProperty("mqtt.clientId"));
		config.setUsername(TEST_PROPS.getProperty("mqtt.username"));
		config.setPassword(TEST_PROPS.getProperty("mqtt.password"));
		config.setConnectTimeoutSeconds(1);
		config.setReconnectDelaySeconds(1);
		config.setVersion(MqttVersion.Mqtt5);
		config.setReconnect(false);
		config.setUid("Netty-Test");
		config.setStats(new MqttStats("Netty-Test", 5));
		config.setProperty(new BasicMqttProperty<Integer>(MqttPropertyType.TOPIC_ALIAS_MAXIMUM, 32));

		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setThreadNamePrefix("NettyMqtt-Scheduler-Test-");
		scheduler.initialize();
		NettyMqttConnection conn = new NettyMqttConnection(
				Executors.newCachedThreadPool(new CustomizableThreadFactory("NettyMqtt-Test-")),
				scheduler, config);
		conn.setWireLogging(true);
		this.service = conn;
	}

	@Test
	public void connect() throws Exception {
		// GIVEN

		// WHEN
		try {
			Future<MqttConnectReturnCode> f = service.open();
			MqttConnectReturnCode code = f.get(TIMEOUT_SECS, TimeUnit.SECONDS);

			// THEN
			assertThat("Opened connection", code, equalTo(MqttConnectReturnCode.Accepted));
		} finally {
			service.close();
		}
	}

	@Test
	public void publish_withAlias_first() throws Exception {
		// GIVEN
		Future<?> f = service.open();
		f.get(TIMEOUT_SECS, TimeUnit.SECONDS);

		// WHEN
		final String msg = "Hello, world.";
		MqttMessage mqttMsg = new BasicMqttMessage("test/foo", false, MqttQos.AtLeastOnce,
				msg.getBytes(CharsetUtil.UTF_8));
		f = service.publish(mqttMsg);
		f.get(TIMEOUT_SECS, TimeUnit.SECONDS);
	}

	@Test
	public void publish_withAlias_second() throws Exception {
		// GIVEN
		Future<?> f = service.open();
		f.get(TIMEOUT_SECS, TimeUnit.SECONDS);

		// WHEN
		MqttMessage mqttMsg = new BasicMqttMessage("test/foo", false, MqttQos.AtLeastOnce,
				"Hello, world.".getBytes(CharsetUtil.UTF_8));
		f = service.publish(mqttMsg);
		f.get(TIMEOUT_SECS, TimeUnit.SECONDS);

		mqttMsg = new BasicMqttMessage("test/foo", false, MqttQos.AtLeastOnce,
				"HELLO, WORLD!".getBytes(CharsetUtil.UTF_8));
		f = service.publish(mqttMsg);
		f.get(TIMEOUT_SECS, TimeUnit.SECONDS);
	}

	@Test
	public void subscribe_withAlias_first() throws Exception {
		// GIVEN
		service.open().get(TIMEOUT_SECS, TimeUnit.SECONDS);

		final BlockingQueue<MqttMessage> messages = new LinkedBlockingQueue<>();
		service.setMessageHandler(new MqttMessageHandler() {

			@Override
			public void onMqttMessage(MqttMessage message) {
				messages.add(message);
			}
		});

		// WHEN
		Future<?> f = service.subscribe("test/foo", MqttQos.AtLeastOnce, null);
		f.get(TIMEOUT_SECS, TimeUnit.SECONDS);

		final String msg = "Hello, world.";
		final MqttMessage tx = new BasicMqttMessage("test/foo", false, MqttQos.AtLeastOnce,
				msg.getBytes(CharsetUtil.UTF_8));
		f = service.publish(tx);
		f.get(TIMEOUT_SECS, TimeUnit.SECONDS);

		// THEN
		MqttMessage rx = messages.poll(TIMEOUT_SECS, TimeUnit.SECONDS);
		assertThat("Message topic", rx.getTopic(), equalTo(tx.getTopic()));
		assertThat("Message QoS", rx.getQosLevel(), equalTo(MqttQos.AtLeastOnce));
		assertThat("Message payload", new String(rx.getPayload(), CharsetUtil.UTF_8), equalTo(msg));
	}

	@Test
	public void subscribe_withAlias_second() throws Exception {
		// GIVEN
		service.open().get(TIMEOUT_SECS, TimeUnit.SECONDS);

		final BlockingQueue<MqttMessage> messages = new LinkedBlockingQueue<>();
		service.setMessageHandler(new MqttMessageHandler() {

			@Override
			public void onMqttMessage(MqttMessage message) {
				messages.add(message);
			}
		});

		// WHEN
		Future<?> f = service.subscribe("test/foo", MqttQos.AtLeastOnce, null);
		f.get(TIMEOUT_SECS, TimeUnit.SECONDS);

		final String msg = "Hello, world.";
		final MqttMessage tx = new BasicMqttMessage("test/foo", false, MqttQos.AtLeastOnce,
				msg.getBytes(CharsetUtil.UTF_8));
		f = service.publish(tx);
		f.get(TIMEOUT_SECS, TimeUnit.SECONDS);

		// give a little time for broker to publish to subscriber
		Thread.sleep(50);

		final MqttMessage tx2 = new BasicMqttMessage("test/foo", false, MqttQos.AtLeastOnce,
				msg.toUpperCase().getBytes(CharsetUtil.UTF_8));
		f = service.publish(tx2);
		f.get(TIMEOUT_SECS, TimeUnit.SECONDS);

		// THEN
		MqttMessage rx = messages.poll(TIMEOUT_SECS, TimeUnit.SECONDS);
		assertThat("Message topic", rx.getTopic(), equalTo(tx.getTopic()));
		assertThat("Message QoS", rx.getQosLevel(), equalTo(MqttQos.AtLeastOnce));
		assertThat("Message payload", new String(rx.getPayload(), CharsetUtil.UTF_8), equalTo(msg));

		rx = messages.poll(TIMEOUT_SECS, TimeUnit.SECONDS);
		assertThat("Message topic", rx.getTopic(), equalTo(tx2.getTopic()));
		assertThat("Message QoS", rx.getQosLevel(), equalTo(MqttQos.AtLeastOnce));
		assertThat("Message payload", new String(rx.getPayload(), CharsetUtil.UTF_8),
				equalTo(msg.toUpperCase()));
	}

	@Test
	public void subscribe_withAlias_second_afterReconnect() throws Exception {
		// GIVEN
		service.open().get(TIMEOUT_SECS, TimeUnit.SECONDS);

		final BlockingQueue<MqttMessage> messages = new LinkedBlockingQueue<>();
		service.setMessageHandler(new MqttMessageHandler() {

			@Override
			public void onMqttMessage(MqttMessage message) {
				messages.add(message);
			}
		});

		// WHEN
		Future<?> f = service.subscribe("test/foo", MqttQos.AtLeastOnce, null);
		f.get(TIMEOUT_SECS, TimeUnit.SECONDS);

		final String msg = "Hello, world.";
		final MqttMessage tx = new BasicMqttMessage("test/foo", false, MqttQos.AtLeastOnce,
				msg.getBytes(CharsetUtil.UTF_8));
		f = service.publish(tx);
		f.get(TIMEOUT_SECS, TimeUnit.SECONDS);

		// give a little time for broker to publish to subscriber
		Thread.sleep(50);

		service.close();
		Thread.sleep(50);
		service.open().get(TIMEOUT_SECS, TimeUnit.SECONDS);
		f = service.subscribe("test/foo", MqttQos.AtLeastOnce, null);
		f.get(TIMEOUT_SECS, TimeUnit.SECONDS);

		final MqttMessage tx2 = new BasicMqttMessage("test/foo", false, MqttQos.AtLeastOnce,
				msg.toUpperCase().getBytes(CharsetUtil.UTF_8));
		f = service.publish(tx2);
		f.get(TIMEOUT_SECS, TimeUnit.SECONDS);

		// THEN
		MqttMessage rx = messages.poll(TIMEOUT_SECS, TimeUnit.SECONDS);
		assertThat("Message received", rx, notNullValue());
		assertThat("Message topic", rx.getTopic(), equalTo(tx.getTopic()));
		assertThat("Message QoS", rx.getQosLevel(), equalTo(MqttQos.AtLeastOnce));
		assertThat("Message payload", new String(rx.getPayload(), CharsetUtil.UTF_8), equalTo(msg));

		rx = messages.poll(TIMEOUT_SECS, TimeUnit.SECONDS);
		assertThat("Message received", rx, notNullValue());
		assertThat("Message topic", rx.getTopic(), equalTo(tx2.getTopic()));
		assertThat("Message QoS", rx.getQosLevel(), equalTo(MqttQos.AtLeastOnce));
		assertThat("Message payload", new String(rx.getPayload(), CharsetUtil.UTF_8),
				equalTo(msg.toUpperCase()));
	}

	@Test
	public void subscribe_withAlias_twoTopics_first() throws Exception {
		// GIVEN
		service.open().get(TIMEOUT_SECS, TimeUnit.SECONDS);

		final BlockingQueue<MqttMessage> messages = new LinkedBlockingQueue<>();
		service.setMessageHandler(new MqttMessageHandler() {

			@Override
			public void onMqttMessage(MqttMessage message) {
				messages.add(message);
			}
		});

		// WHEN
		Future<?> f = service.subscribe("test/foo", MqttQos.AtLeastOnce, null);
		f.get(TIMEOUT_SECS, TimeUnit.SECONDS);

		f = service.subscribe("test/bar", MqttQos.AtLeastOnce, null);
		f.get(TIMEOUT_SECS, TimeUnit.SECONDS);

		final String msg = "Hello, world.";
		final MqttMessage tx = new BasicMqttMessage("test/foo", false, MqttQos.AtLeastOnce,
				msg.getBytes(CharsetUtil.UTF_8));
		service.publish(tx).get(TIMEOUT_SECS, TimeUnit.SECONDS);

		// give a little time for broker to publish to subscriber
		Thread.sleep(50);

		final String msg2 = "Goodbye, world.";
		final MqttMessage tx2 = new BasicMqttMessage("test/bar", false, MqttQos.AtLeastOnce,
				msg2.getBytes(CharsetUtil.UTF_8));
		service.publish(tx2).get(TIMEOUT_SECS, TimeUnit.SECONDS);

		// THEN
		MqttMessage rx = messages.poll(TIMEOUT_SECS, TimeUnit.SECONDS);
		assertThat("Message topic", rx.getTopic(), equalTo(tx.getTopic()));
		assertThat("Message QoS", rx.getQosLevel(), equalTo(MqttQos.AtLeastOnce));
		assertThat("Message payload", new String(rx.getPayload(), CharsetUtil.UTF_8), equalTo(msg));

		rx = messages.poll(TIMEOUT_SECS, TimeUnit.SECONDS);
		assertThat("Message topic", rx.getTopic(), equalTo(tx2.getTopic()));
		assertThat("Message QoS", rx.getQosLevel(), equalTo(MqttQos.AtLeastOnce));
		assertThat("Message payload", new String(rx.getPayload(), CharsetUtil.UTF_8), equalTo(msg2));
	}

	@Test
	public void subscribe_withAlias_twoTopics_second() throws Exception {
		// GIVEN
		service.open().get(TIMEOUT_SECS, TimeUnit.SECONDS);

		final BlockingQueue<MqttMessage> messages = new LinkedBlockingQueue<>();
		service.setMessageHandler(new MqttMessageHandler() {

			@Override
			public void onMqttMessage(MqttMessage message) {
				messages.add(message);
			}
		});

		// WHEN
		Future<?> f = service.subscribe("test/foo", MqttQos.AtLeastOnce, null);
		f.get(TIMEOUT_SECS, TimeUnit.SECONDS);

		f = service.subscribe("test/bar", MqttQos.AtLeastOnce, null);
		f.get(TIMEOUT_SECS, TimeUnit.SECONDS);

		final String msg = "Hello, world.";
		final MqttMessage tx = new BasicMqttMessage("test/foo", false, MqttQos.AtLeastOnce,
				msg.getBytes(CharsetUtil.UTF_8));
		service.publish(tx).get(TIMEOUT_SECS, TimeUnit.SECONDS);

		// give a little time for broker to publish to subscriber
		Thread.sleep(50);

		final String msg2 = "Goodbye, world.";
		final MqttMessage tx2 = new BasicMqttMessage("test/bar", false, MqttQos.AtLeastOnce,
				msg2.getBytes(CharsetUtil.UTF_8));
		service.publish(tx2).get(TIMEOUT_SECS, TimeUnit.SECONDS);

		// give a little time for broker to publish to subscriber
		Thread.sleep(50);

		final MqttMessage tx3 = new BasicMqttMessage("test/foo", false, MqttQos.AtLeastOnce,
				msg.toUpperCase().getBytes(CharsetUtil.UTF_8));
		service.publish(tx3).get(TIMEOUT_SECS, TimeUnit.SECONDS);

		// give a little time for broker to publish to subscriber
		Thread.sleep(50);

		final MqttMessage tx4 = new BasicMqttMessage("test/bar", false, MqttQos.AtLeastOnce,
				msg2.toUpperCase().getBytes(CharsetUtil.UTF_8));
		service.publish(tx4).get(TIMEOUT_SECS, TimeUnit.SECONDS);

		// THEN
		MqttMessage rx = messages.poll(TIMEOUT_SECS, TimeUnit.SECONDS);
		assertThat("Message topic", rx.getTopic(), equalTo(tx.getTopic()));
		assertThat("Message QoS", rx.getQosLevel(), equalTo(MqttQos.AtLeastOnce));
		assertThat("Message payload", new String(rx.getPayload(), CharsetUtil.UTF_8), equalTo(msg));

		rx = messages.poll(TIMEOUT_SECS, TimeUnit.SECONDS);
		assertThat("Message topic", rx.getTopic(), equalTo(tx2.getTopic()));
		assertThat("Message QoS", rx.getQosLevel(), equalTo(MqttQos.AtLeastOnce));
		assertThat("Message payload", new String(rx.getPayload(), CharsetUtil.UTF_8), equalTo(msg2));

		rx = messages.poll(TIMEOUT_SECS, TimeUnit.SECONDS);
		assertThat("Message topic", rx.getTopic(), equalTo(tx3.getTopic()));
		assertThat("Message QoS", rx.getQosLevel(), equalTo(MqttQos.AtLeastOnce));
		assertThat("Message payload", new String(rx.getPayload(), CharsetUtil.UTF_8),
				equalTo(msg.toUpperCase()));

		rx = messages.poll(TIMEOUT_SECS, TimeUnit.SECONDS);
		assertThat("Message topic", rx.getTopic(), equalTo(tx4.getTopic()));
		assertThat("Message QoS", rx.getQosLevel(), equalTo(MqttQos.AtLeastOnce));
		assertThat("Message payload", new String(rx.getPayload(), CharsetUtil.UTF_8),
				equalTo(msg2.toUpperCase()));
	}

}
