/* ==================================================================
 * SimpleActionMessageQueueTests.java - 12/02/2020 11:09:12 am
 * 
 * Copyright 2020 SolarNetwork.net Dev Team
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

package net.solarnetwork.ocpp.service.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.junit.Before;
import org.junit.Test;
import net.solarnetwork.ocpp.domain.Action;
import net.solarnetwork.ocpp.domain.BasicActionMessage;
import net.solarnetwork.ocpp.domain.ChargePointIdentity;
import net.solarnetwork.ocpp.domain.PendingActionMessage;
import net.solarnetwork.ocpp.service.SimpleActionMessageQueue;

/**
 * Test cases for the {@link SimpleActionMessageQueue} class.
 * 
 * @author matt
 * @version 1.0
 */
public class SimpleActionMessageQueueTests {

	private Map<ChargePointIdentity, Deque<PendingActionMessage>> map;
	private SimpleActionMessageQueue amq;

	private enum Work implements Action {

		LazeAbout;

		@Override
		public String getName() {
			return name();
		}
	}

	@Before
	public void setup() {
		map = new ConcurrentHashMap<>(8, 0.7f, 2);
		amq = new SimpleActionMessageQueue(map);
	}

	private ChargePointIdentity createClientId() {
		return createClientId(UUID.randomUUID().toString());
	}

	private ChargePointIdentity createClientId(String identifier) {
		return new ChargePointIdentity(identifier, UUID.randomUUID().toString());
	}

	@Test
	public void addPending() {
		// given
		ChargePointIdentity clientId = createClientId();
		PendingActionMessage msg = new PendingActionMessage(
				new BasicActionMessage<Object>(clientId, Work.LazeAbout, Boolean.TRUE));

		// when
		amq.addPendingMessage(msg);

		// then
		assertThat("Queue added to map", map, hasKey(clientId));
		assertThat("Message added to queue", map.get(clientId), contains(msg));
	}

	@Test
	public void addPending_multi() {
		// given
		ChargePointIdentity clientId = createClientId();

		// when
		PendingActionMessage msg = new PendingActionMessage(
				new BasicActionMessage<Object>(clientId, Work.LazeAbout, Boolean.TRUE));
		amq.addPendingMessage(msg);

		PendingActionMessage msg2 = new PendingActionMessage(
				new BasicActionMessage<Object>(clientId, Work.LazeAbout, Boolean.FALSE));
		amq.addPendingMessage(msg2);

		// then
		assertThat("Queue added to map", map, hasKey(clientId));
		assertThat("Message added to queue", map.get(clientId), contains(msg, msg2));
	}

	@Test
	public void addPending_multiClients() {
		// given
		ChargePointIdentity clientId1 = createClientId();
		ChargePointIdentity clientId2 = createClientId();

		// when
		PendingActionMessage msg = new PendingActionMessage(
				new BasicActionMessage<Object>(clientId1, Work.LazeAbout, Boolean.TRUE));
		amq.addPendingMessage(msg);

		PendingActionMessage msg2 = new PendingActionMessage(
				new BasicActionMessage<Object>(clientId2, Work.LazeAbout, Boolean.FALSE));
		amq.addPendingMessage(msg2);

		// then
		assertThat("Queue added to map", map.keySet(), containsInAnyOrder(clientId1, clientId2));
		assertThat("Message added to queue 1", map.get(clientId1), contains(msg));
		assertThat("Message added to queue 2", map.get(clientId2), contains(msg2));
	}

	@Test
	public void getQueue_none() {
		Deque<PendingActionMessage> q = amq.pendingMessageQueue(createClientId());
		assertThat("Queue created", q, allOf(notNullValue(), hasSize(0)));
	}

	@Test
	public void getQueue_clientNotFound() {
		// given
		ChargePointIdentity clientId1 = createClientId();
		PendingActionMessage msg = new PendingActionMessage(
				new BasicActionMessage<Object>(clientId1, Work.LazeAbout, Boolean.TRUE));
		amq.addPendingMessage(msg);

		// when
		ChargePointIdentity clientId2 = createClientId();
		Deque<PendingActionMessage> q = amq.pendingMessageQueue(clientId2);
		assertThat("Queue created", q, allOf(notNullValue(), hasSize(0)));
		assertThat("Two queues exist", map.keySet(), containsInAnyOrder(clientId1, clientId2));
		assertThat("Other queue still has item", map.get(clientId1), hasSize(1));
	}

	@Test
	public void getQueue() {
		// given
		ChargePointIdentity clientId = createClientId();
		PendingActionMessage msg = new PendingActionMessage(
				new BasicActionMessage<Object>(clientId, Work.LazeAbout, Boolean.TRUE));
		amq.addPendingMessage(msg);

		// when
		Deque<PendingActionMessage> q = amq.pendingMessageQueue(clientId);
		assertThat("Client queue available", q, sameInstance(map.get(clientId)));
	}

	@Test
	public void addPendingAndThen() {
		// given
		ChargePointIdentity clientId = createClientId();
		PendingActionMessage msg = new PendingActionMessage(
				new BasicActionMessage<Object>(clientId, Work.LazeAbout, Boolean.TRUE));

		// when
		AtomicBoolean invoked = new AtomicBoolean(false);
		amq.addPendingMessage(msg, q -> {
			invoked.set(true);
			assertThat("Queue provided to consumer", q, notNullValue());
			assertThat("Queue has message added", q, contains(msg));
			assertThat("Queue added to map", q, sameInstance(map.get(clientId)));
		});

		// then
		assertThat("Consumer invoked", invoked.get(), equalTo(true));
	}

	@Test
	public void pollMessage() {
		// given
		ChargePointIdentity clientId = createClientId();
		PendingActionMessage msg = new PendingActionMessage(
				new BasicActionMessage<Object>(clientId, Work.LazeAbout, Boolean.TRUE));
		amq.addPendingMessage(msg);

		// when
		PendingActionMessage result = amq.pollPendingMessage(clientId, msg.getMessage().getMessageId());

		// then
		assertThat("Message polled", result, sameInstance(msg));
		assertThat("Message removed from queue", map.get(clientId), hasSize(0));
	}

	@Test
	public void pollMessage_noClient() {
		// given
		ChargePointIdentity clientId = createClientId();
		PendingActionMessage msg = new PendingActionMessage(
				new BasicActionMessage<Object>(clientId, Work.LazeAbout, Boolean.TRUE));
		amq.addPendingMessage(msg);

		// when
		PendingActionMessage result = amq.pollPendingMessage(createClientId(),
				msg.getMessage().getMessageId());

		// then
		assertThat("Message not polled", result, nullValue());
		assertThat("Message removed from queue", map.get(clientId), contains(msg));
	}

	@Test
	public void pollMessage_noMessageId() {
		// given
		ChargePointIdentity clientId = createClientId();
		PendingActionMessage msg = new PendingActionMessage(
				new BasicActionMessage<Object>(clientId, Work.LazeAbout, Boolean.TRUE));
		amq.addPendingMessage(msg);

		// when
		PendingActionMessage result = amq.pollPendingMessage(clientId, "foo");

		// then
		assertThat("Message not polled", result, nullValue());
		assertThat("Message removed from queue", map.get(clientId), contains(msg));
	}

	@Test
	public void poll() {
		// given
		ChargePointIdentity clientId = createClientId();
		PendingActionMessage msg = new PendingActionMessage(
				new BasicActionMessage<Object>(clientId, Work.LazeAbout, Boolean.TRUE));
		amq.addPendingMessage(msg);

		// when
		PendingActionMessage result = amq.pollPendingMessage(clientId);

		// then
		assertThat("Message polled", result, sameInstance(msg));
		assertThat("Message removed from queue", map.get(clientId), hasSize(0));
	}

	@Test
	public void poll_noClient() {
		// given
		ChargePointIdentity clientId = createClientId();
		PendingActionMessage msg = new PendingActionMessage(
				new BasicActionMessage<Object>(clientId, Work.LazeAbout, Boolean.TRUE));
		amq.addPendingMessage(msg);

		// when
		PendingActionMessage result = amq.pollPendingMessage(createClientId());

		// then
		assertThat("Message not polled", result, nullValue());
		assertThat("Message removed from queue", map.get(clientId), contains(msg));
	}

	@Test
	public void poll_noMessageId() {
		// given
		ChargePointIdentity clientId = createClientId();

		// create empty queue
		amq.pendingMessageQueue(clientId);

		// when
		PendingActionMessage result = amq.pollPendingMessage(clientId);

		// then
		assertThat("Message not polled", result, nullValue());
		assertThat("Message removed from queue", map.get(clientId), hasSize(0));
	}

	@Test
	public void allQueues_multiClient() {
		// given
		ChargePointIdentity clientId1 = createClientId("A");
		ChargePointIdentity clientId2 = createClientId("B");

		PendingActionMessage msg = new PendingActionMessage(
				new BasicActionMessage<Object>(clientId1, Work.LazeAbout, Boolean.TRUE));
		amq.addPendingMessage(msg);

		PendingActionMessage msg2 = new PendingActionMessage(
				new BasicActionMessage<Object>(clientId2, Work.LazeAbout, Boolean.FALSE));
		amq.addPendingMessage(msg2);

		// when
		Iterable<Entry<ChargePointIdentity, Deque<PendingActionMessage>>> set = amq.allQueues();

		// then
		assertThat("Entry iterable available", set, notNullValue());

		List<Entry<ChargePointIdentity, Deque<PendingActionMessage>>> list = StreamSupport
				.stream(set.spliterator(), false).sorted((l, r) -> l.getKey().compareTo(r.getKey()))
				.collect(Collectors.toList());

		assertThat("Message added to queue 1", list, hasSize(2));

		Entry<ChargePointIdentity, Deque<PendingActionMessage>> e1 = list.get(0);
		assertThat("Client 1 entry", e1.getKey(), equalTo(clientId1));
		assertThat("Client 1 queue", e1.getValue(), contains(msg));

		Entry<ChargePointIdentity, Deque<PendingActionMessage>> e2 = list.get(1);
		assertThat("Client 2 entry", e2.getKey(), equalTo(clientId2));
		assertThat("Client 2 queue", e2.getValue(), contains(msg2));
	}
}
