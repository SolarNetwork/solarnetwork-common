/* ==================================================================
 * MqttChannelHandlerTests.java - 30/09/2026 11:55:00 am
 *
 * Copyright 2026 SolarNetwork.net Dev Team
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

package net.solarnetwork.common.mqtt.netty.client;

import static java.nio.charset.StandardCharsets.UTF_8;
import static net.solarnetwork.util.ObjectUtils.nonnull;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CopyOnWriteArrayList;
import org.jspecify.annotations.Nullable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPubAckMessage;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttPublishVariableHeader;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.ImmediateEventExecutor;
import io.netty.util.concurrent.Promise;
import net.solarnetwork.common.mqtt.MqttMessageHandler;

/**
 * Test cases for the {@link MqttChannelHandler} class.
 *
 * @author matt
 * @version 1.1
 */
public class MqttChannelHandlerTests {

	private static final String TEST_TOPIC = "test/topic";

	private MqttClientImpl client;
	private EmbeddedChannel channel;

	@Before
	public void setup() {
		client = new MqttClientImpl(new MqttClientConfig(), null);
		Promise<MqttConnectResult> connectFuture = new DefaultPromise<>(ImmediateEventExecutor.INSTANCE);
		channel = new EmbeddedChannel(new MqttChannelHandler(client, connectFuture));
		// drain the CONNECT sent when the channel became active
		channel.readOutbound();
	}

	@After
	public void teardown() {
		channel.finishAndReleaseAll();
	}

	private void subscribe(MqttMessageHandler handler) {
		client.getSubscriptions().computeIfAbsent(TEST_TOPIC, k -> new CopyOnWriteArrayList<>())
				.add(new MqttSubscription(TEST_TOPIC, handler, false));
	}

	private void publishInbound(int packetId, MqttQoS qos) {
		MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBLISH, false, qos, false, 0);
		MqttPublishVariableHeader variableHeader = new MqttPublishVariableHeader(TEST_TOPIC, packetId);
		channel.writeInbound(new MqttPublishMessage(fixedHeader, variableHeader,
				Unpooled.copiedBuffer("hello", UTF_8)));
	}

	private @Nullable MqttPubAckMessage readPubackOutbound() {
		Object msg;
		while ( (msg = channel.readOutbound()) != null ) {
			if ( msg instanceof MqttPubAckMessage ) {
				return (MqttPubAckMessage) msg;
			}
		}
		return null;
	}

	@Test
	public void qos1_handled_acknowledged() {
		// GIVEN
		final List<MqttMessage> handled = new ArrayList<>(1);
		subscribe(msg -> handled.add(null));

		// WHEN
		publishInbound(42, MqttQoS.AT_LEAST_ONCE);

		// THEN
		assertThat("Handler invoked", handled.size(), is(equalTo(1)));
		MqttPubAckMessage puback = readPubackOutbound();
		assertThat("Message acknowledged after the handler accepted it", puback, is(notNullValue()));
		assertThat("Acknowledged the delivered packet",
				nonnull(puback, "PubAck").variableHeader().messageId(), is(equalTo(42)));
	}

	@Test
	public void qos1_handlerThrew_notAcknowledged() {
		// GIVEN
		subscribe(msg -> {
			throw new RuntimeException("boom!");
		});

		// WHEN
		publishInbound(42, MqttQoS.AT_LEAST_ONCE);

		// THEN
		assertThat("Message not acknowledged, so the broker can redeliver it", readPubackOutbound(),
				is(nullValue()));
		assertThat("Channel still open, so messages keep being processed", channel.isActive(),
				is(equalTo(true)));
	}

	@Test
	public void qos1_handlerThrew_laterMessageStillProcessed() {
		// GIVEN
		final List<Integer> seen = new ArrayList<>(2);
		subscribe(msg -> {
			seen.add(seen.size());
			if ( seen.size() < 2 ) {
				throw new RuntimeException("boom!");
			}
		});

		// WHEN
		publishInbound(42, MqttQoS.AT_LEAST_ONCE);
		publishInbound(43, MqttQoS.AT_LEAST_ONCE);

		// THEN
		assertThat("Both messages delivered to the handler", seen.size(), is(equalTo(2)));
		MqttPubAckMessage puback = readPubackOutbound();
		assertThat("Second message acknowledged", puback, is(notNullValue()));
		assertThat("Only the message the handler accepted was acknowledged",
				nonnull(puback, "PubAck").variableHeader().messageId(), is(equalTo(43)));
		assertThat("No other acknowledgement sent", readPubackOutbound(), is(nullValue()));
	}

	@Test
	public void qos1_oneHandlerThrew_othersStillInvoked() {
		// GIVEN
		final List<String> seen = new ArrayList<>(2);
		client.getSubscriptions().computeIfAbsent(TEST_TOPIC, k -> new CopyOnWriteArrayList<>())
				.add(new MqttSubscription(TEST_TOPIC, msg -> {
					seen.add("first");
					throw new RuntimeException("boom!");
				}, false));
		client.getSubscriptions().get(TEST_TOPIC)
				.add(new MqttSubscription(TEST_TOPIC, msg -> seen.add("second"), false));

		// WHEN
		publishInbound(42, MqttQoS.AT_LEAST_ONCE);

		// THEN
		assertThat("Both handlers invoked despite the first throwing", seen,
				is(equalTo(List.of("first", "second"))));
		assertThat("Message not acknowledged, because one handler did not accept it",
				readPubackOutbound(), is(nullValue()));
	}

	@Test
	public void qos0_handlerThrew_channelStaysOpen() {
		// GIVEN
		subscribe(msg -> {
			throw new RuntimeException("boom!");
		});

		// WHEN
		publishInbound(-1, MqttQoS.AT_MOST_ONCE);

		// THEN
		assertThat("Nothing to acknowledge at QOS 0", readPubackOutbound(), is(nullValue()));
		assertThat("Channel still open", channel.isActive(), is(equalTo(true)));
	}

	@Test
	public void qos1_asyncHandler_acknowledgedOnlyWhenStageCompletes() {
		// GIVEN
		final CompletableFuture<Void> work = new CompletableFuture<>();
		subscribe(new MqttMessageHandler() {

			@Override
			public void onMqttMessage(net.solarnetwork.common.mqtt.MqttMessage message) {
				throw new UnsupportedOperationException("async handler");
			}

			@Override
			public CompletionStage<?> onMqttMessageAsync(
					net.solarnetwork.common.mqtt.MqttMessage message) {
				return work;
			}

		});

		// WHEN
		publishInbound(42, MqttQoS.AT_LEAST_ONCE);

		// THEN
		assertThat("Not acknowledged while the handler is still working", readPubackOutbound(),
				is(nullValue()));

		work.complete(null);
		channel.runPendingTasks();

		MqttPubAckMessage puback = readPubackOutbound();
		assertThat("Acknowledged once the handler completed", puback, is(notNullValue()));
		assertThat("Acknowledged the delivered packet",
				nonnull(puback, "PubAck").variableHeader().messageId(), is(equalTo(42)));
	}

	@Test
	public void qos1_asyncHandlerFailed_notAcknowledged() {
		// GIVEN
		final CompletableFuture<Void> work = new CompletableFuture<>();
		subscribe(new MqttMessageHandler() {

			@Override
			public void onMqttMessage(net.solarnetwork.common.mqtt.MqttMessage message) {
				throw new UnsupportedOperationException("async handler");
			}

			@Override
			public CompletionStage<?> onMqttMessageAsync(
					net.solarnetwork.common.mqtt.MqttMessage message) {
				return work;
			}

		});

		// WHEN
		publishInbound(42, MqttQoS.AT_LEAST_ONCE);
		work.completeExceptionally(new RuntimeException("boom!"));
		channel.runPendingTasks();

		// THEN
		assertThat("Not acknowledged, so the broker can redeliver it", readPubackOutbound(),
				is(nullValue()));
		assertThat("Channel still open", channel.isActive(), is(equalTo(true)));
	}

	@Test
	public void qos1_asyncHandler_completedOnAnotherThread_acknowledged() throws Exception {
		// GIVEN
		final CompletableFuture<Void> work = new CompletableFuture<>();
		subscribe(new MqttMessageHandler() {

			@Override
			public void onMqttMessage(net.solarnetwork.common.mqtt.MqttMessage message) {
				throw new UnsupportedOperationException("async handler");
			}

			@Override
			public CompletionStage<?> onMqttMessageAsync(
					net.solarnetwork.common.mqtt.MqttMessage message) {
				return work;
			}

		});

		// WHEN
		publishInbound(42, MqttQoS.AT_LEAST_ONCE);

		// complete from a worker thread, as a handler using an executor would
		Thread worker = new Thread(() -> work.complete(null), "test-worker");
		worker.start();
		worker.join(5_000);

		channel.runPendingTasks();

		// THEN
		MqttPubAckMessage puback = readPubackOutbound();
		assertThat("Acknowledged from the completing worker thread", puback, is(notNullValue()));
		assertThat("Acknowledged the delivered packet",
				nonnull(puback, "PubAck").variableHeader().messageId(), is(equalTo(42)));
	}

	@Test
	public void qos1_asyncHandlerReturnedNull_acknowledged() {
		// GIVEN
		subscribe(new MqttMessageHandler() {

			@Override
			public void onMqttMessage(net.solarnetwork.common.mqtt.MqttMessage message) {
				throw new UnsupportedOperationException("async handler");
			}

			@SuppressWarnings("NullAway")
			@Override
			public CompletionStage<?> onMqttMessageAsync(
					net.solarnetwork.common.mqtt.MqttMessage message) {
				return null;
			}

		});

		// WHEN
		publishInbound(42, MqttQoS.AT_LEAST_ONCE);
		channel.runPendingTasks();

		// THEN
		assertThat("A null stage is treated as accepted", readPubackOutbound(), is(notNullValue()));
	}

}
