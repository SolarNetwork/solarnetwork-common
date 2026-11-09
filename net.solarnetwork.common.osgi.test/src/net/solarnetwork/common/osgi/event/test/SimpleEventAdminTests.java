/* ==================================================================
 * SimpleEventAdminTests.java - 10/11/2026 7:52:57 AM
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

package net.solarnetwork.common.osgi.event.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import net.solarnetwork.common.osgi.event.SimpleEventAdmin;

/**
 * Test cases for the {@link SimpleEventAdmin} class.
 *
 * @author matt
 * @version 1.0
 */
public class SimpleEventAdminTests {

	@Test
	public void registerEventHandler_concurrent() throws Exception {
		// many topics per trial, so the registering threads race on each
		final String[] topics = new String[100];
		for ( int i = 0; i < topics.length; i++ ) {
			topics[i] = "a/" + i;
		}
		final ExecutorService pool = Executors.newFixedThreadPool(2);
		try {
			for ( int i = 0; i < 200; i++ ) {
				// GIVEN
				final SimpleEventAdmin admin = new SimpleEventAdmin(Runnable::run);
				final CyclicBarrier barrier = new CyclicBarrier(2);
				final AtomicInteger count = new AtomicInteger();
				final EventHandler handler1 = e -> count.incrementAndGet();
				final EventHandler handler2 = e -> count.incrementAndGet();

				// WHEN
				Future<?> f1 = pool.submit(() -> {
					barrier.await();
					admin.registerEventHandler(handler1, topics);
					return null;
				});
				Future<?> f2 = pool.submit(() -> {
					barrier.await();
					admin.registerEventHandler(handler2, topics);
					return null;
				});
				f1.get();
				f2.get();

				// THEN
				for ( String topic : topics ) {
					admin.sendEvent(new Event(topic, Map.of()));
				}
				assertThat("Both handlers registered for every topic in trial " + i, count.get(),
						is(2 * topics.length));
			}
		} finally {
			pool.shutdownNow();
		}
	}

	@Test
	public void sendEvent_handlerThrows() {
		// GIVEN
		SimpleEventAdmin admin = new SimpleEventAdmin(Runnable::run);
		List<String> received = new ArrayList<>();
		admin.registerEventHandler(e -> {
			throw new RuntimeException("Test handler failure");
		}, "a/b");
		admin.registerEventHandler(e -> received.add(e.getTopic()), "a/b");

		// WHEN
		admin.sendEvent(new Event("a/b", Map.of()));

		// THEN
		assertThat("Event delivered to handler after failed handler", received, contains("a/b"));
	}

	@Test
	public void postEvent_handlerThrows() {
		// GIVEN
		// run handlers on the calling thread
		SimpleEventAdmin admin = new SimpleEventAdmin(Runnable::run);
		List<String> received = new ArrayList<>();
		admin.registerEventHandler(e -> {
			throw new RuntimeException("Test handler failure");
		}, "a/b");
		admin.registerEventHandler(e -> received.add(e.getTopic()), "a/b");

		// WHEN
		admin.postEvent(new Event("a/b", Map.of()));

		// THEN
		assertThat("Event delivered to handler after failed handler", received, contains("a/b"));
	}

	@Test
	public void sendEvent_handlerMatchesTwoTopics() {
		// GIVEN
		SimpleEventAdmin admin = new SimpleEventAdmin(Runnable::run);
		AtomicInteger count = new AtomicInteger();
		admin.registerEventHandler(e -> count.incrementAndGet(), "a/*", "a/b");

		// WHEN
		admin.sendEvent(new Event("a/b", Map.of()));

		// THEN
		assertThat("Event delivered to handler once", count.get(), is(1));
	}

	@Test
	public void postEvent_handlerMatchesTwoTopics() {
		// GIVEN
		AtomicInteger executions = new AtomicInteger();
		SimpleEventAdmin admin = new SimpleEventAdmin(r -> {
			executions.incrementAndGet();
			r.run();
		});
		AtomicInteger count = new AtomicInteger();
		admin.registerEventHandler(e -> count.incrementAndGet(), "a/*", "a/b");

		// WHEN
		admin.postEvent(new Event("a/b", Map.of()));

		// THEN
		assertThat("Event delivered to handler once", count.get(), is(1));
		assertThat("One delivery task executed", executions.get(), is(1));
	}

}
