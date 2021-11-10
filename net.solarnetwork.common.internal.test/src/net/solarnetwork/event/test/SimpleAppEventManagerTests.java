/* ==================================================================
 * SimpleAppEventManagerTests.java - 9/11/2021 11:09:11 AM
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

package net.solarnetwork.event.test;

import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import net.solarnetwork.event.AppEvent;
import net.solarnetwork.event.AppEventHandler;
import net.solarnetwork.event.BasicAppEvent;
import net.solarnetwork.event.SimpleAppEventManager;

/**
 * Test cases for the {@link SimpleAppEventManager} class.
 * 
 * @author matt
 * @version 1.0
 */
public class SimpleAppEventManagerTests {

	private ExecutorService executor;
	private SimpleAppEventManager manager;

	@Before
	public void setup() {
		executor = Executors.newWorkStealingPool();
		manager = new SimpleAppEventManager(executor);
	}

	@After
	public void teardown() {
		if ( !executor.isShutdown() ) {
			executor.shutdownNow();
		}
	}

	private static class CapturingAppEventHandler implements AppEventHandler {

		private final List<AppEvent> events;

		public CapturingAppEventHandler(List<AppEvent> events) {
			super();
			this.events = events;
		}

		@Override
		public void handleEvent(AppEvent event) {
			events.add(event);
		}

	}

	@Test
	public void postEvent_simple() throws Exception {
		// GIVEN
		List<AppEvent> events = new ArrayList<>(1);
		manager.registerEventHandler(new CapturingAppEventHandler(events), "foo/**");

		// WHEN
		BasicAppEvent evt = new BasicAppEvent("foo/bar", singletonMap("foo", "bar"));
		manager.postEvent(evt);

		// THEN
		executor.shutdown();
		executor.awaitTermination(2, TimeUnit.SECONDS);

		assertThat("Event handled", events, hasSize(1));
		assertThat("Event same instance", events.get(0), is(sameInstance(evt)));
	}

	@Test
	public void postEvent_multipleHandlers() throws Exception {
		// GIVEN
		List<AppEvent> events1 = new ArrayList<>(1);
		List<AppEvent> events2 = new ArrayList<>(1);
		manager.registerEventHandler(new CapturingAppEventHandler(events1), "foo/**");
		manager.registerEventHandler(new CapturingAppEventHandler(events2), "**");

		// WHEN
		BasicAppEvent evt = new BasicAppEvent("foo/bar", singletonMap("foo", "bar"));
		manager.postEvent(evt);

		// THEN
		executor.shutdown();
		executor.awaitTermination(2, TimeUnit.SECONDS);

		assertThat("Event handled by handler 1", events1, hasSize(1));
		assertThat("Event same instance in handler 1", events1.get(0), is(sameInstance(evt)));
		assertThat("Event handled by handler 2", events2, hasSize(1));
		assertThat("Event same instance in handler 2", events2.get(0), is(sameInstance(evt)));
	}

	@Test
	public void postEvent_multipleHandlers_topicFilter() throws Exception {
		// GIVEN
		List<AppEvent> events1 = new ArrayList<>(1);
		List<AppEvent> events2 = new ArrayList<>(1);
		manager.registerEventHandler(new CapturingAppEventHandler(events1), "foo/**");
		manager.registerEventHandler(new CapturingAppEventHandler(events2), "**");

		// WHEN
		BasicAppEvent evt = new BasicAppEvent("bar/foo", singletonMap("bar", "foo"));
		manager.postEvent(evt);

		// THEN
		executor.shutdown();
		executor.awaitTermination(2, TimeUnit.SECONDS);

		assertThat("No event handled by handler 1 because topic doesn't match", events1, hasSize(0));
		assertThat("Event handled by handler 2", events2, hasSize(1));
		assertThat("Event same instance in handler 2", events2.get(0), is(sameInstance(evt)));
	}

}
