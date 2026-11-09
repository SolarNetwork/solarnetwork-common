/* ==================================================================
 * DynamicServiceTrackerTests.java - 7/06/2018 9:08:43 AM
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

package net.solarnetwork.common.osgi.service.test;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.easymock.EasyMock.expect;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.fail;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import net.solarnetwork.common.osgi.service.DynamicServiceTracker;
import net.solarnetwork.common.osgi.test.TestServiceReference.SerializableServiceRef;

/**
 * Test cases for the {@link DynamicServiceTracker} class.
 *
 * @author matt
 * @version 1.1
 */
public class DynamicServiceTrackerTests {

	private BundleContext bundleContext;

	@Before
	public void setup() {
		bundleContext = EasyMock.createMock(BundleContext.class);
	}

	@After
	public void teardown() {
		EasyMock.verify(bundleContext);
	}

	private void replayAll() {
		EasyMock.replay(bundleContext);
	}

	@Test
	public void highestRankWins() throws Exception {
		// GIVEN
		SerializableServiceRef ref = new SerializableServiceRef(2, 10);
		ServiceReference<?>[] services = new ServiceReference<?>[] { new SerializableServiceRef(1, 0),
				ref, new SerializableServiceRef(3, 5), };
		expect(bundleContext.getServiceReferences(Serializable.class.getName(), null))
				.andReturn(services);
		UUID uuid = UUID.randomUUID();
		expect(bundleContext.<Serializable> getService(ref)).andReturn(uuid);
		expect(bundleContext.ungetService(ref)).andReturn(true);

		// WHEN
		replayAll();

		DynamicServiceTracker<UUID> tracker = new DynamicServiceTracker<UUID>(bundleContext,
				Serializable.class);

		UUID s = tracker.service();

		// THEN
		assertThat("Highest rank returned", s, is(sameInstance(uuid)));
	}

	@Test
	public void sticky_firstTime() throws Exception {
		// GIVEN
		SerializableServiceRef ref = new SerializableServiceRef(1, 1);
		ServiceReference<?>[] services = new ServiceReference<?>[] { ref };
		expect(bundleContext.getServiceReferences(Serializable.class.getName(), null))
				.andReturn(services);
		UUID uuid = UUID.randomUUID();
		expect(bundleContext.<Serializable> getService(ref)).andReturn(uuid);
		expect(bundleContext.ungetService(ref)).andReturn(true);

		// WHEN
		replayAll();

		DynamicServiceTracker<UUID> tracker = new DynamicServiceTracker<UUID>(bundleContext,
				Serializable.class);
		tracker.setSticky(true);

		UUID s = tracker.service();

		// THEN
		assertThat("Service returned via ref", s, is(sameInstance(uuid)));
	}

	@Test
	public void sticky_secondTime() throws Exception {
		// GIVEN
		SerializableServiceRef ref = new SerializableServiceRef(1, 1);
		ServiceReference<?>[] services = new ServiceReference<?>[] { ref };
		expect(bundleContext.getServiceReferences(Serializable.class.getName(), null))
				.andReturn(services);
		UUID uuid = UUID.randomUUID();
		expect(bundleContext.<Serializable> getService(ref)).andReturn(uuid);
		expect(bundleContext.ungetService(ref)).andReturn(true);

		// WHEN
		replayAll();

		DynamicServiceTracker<UUID> tracker = new DynamicServiceTracker<UUID>(bundleContext,
				Serializable.class);
		tracker.setSticky(true);

		UUID s = tracker.service();
		UUID s2 = tracker.service();

		// THEN
		assertThat("Service returned via ref", s, is(sameInstance(uuid)));
		assertThat("Service returned via sticky cache", s2, is(sameInstance(uuid)));
	}

	@Test
	public void sticky_gc() throws Exception {
		// GIVEN
		SerializableServiceRef ref = new SerializableServiceRef(1, 1);
		ServiceReference<?>[] services = new ServiceReference<?>[] { ref };
		expect(bundleContext.getServiceReferences(Serializable.class.getName(), null))
				.andReturn(services).times(2);
		String uuid = UUID.randomUUID().toString(); // don't hold ref to UUID so can be GC'ed
		expect(bundleContext.<Serializable> getService(ref)).andAnswer(new IAnswer<Serializable>() {

			@Override
			public Serializable answer() throws Throwable {
				return UUID.fromString(uuid); // again, no holder ref to UUID instance
			}
		}).times(2);
		expect(bundleContext.ungetService(ref)).andReturn(true).times(2);

		// WHEN
		replayAll();

		DynamicServiceTracker<UUID> tracker = new DynamicServiceTracker<UUID>(bundleContext,
				Serializable.class);
		tracker.setSticky(true);

		String s = tracker.service().toString();

		// force GC
		System.gc();

		String s2 = tracker.service().toString();

		// THEN
		assertThat("Service returned via ref", s, is(equalTo(uuid)));
		assertThat("Service returned via ref after GC", s2, is(equalTo(uuid)));
	}

	public static final class TestService implements Serializable {

		private static final long serialVersionUID = -4342981976789683287L;

		private String uid;
		private String capitalUid;

		public String getUid() {
			return uid;
		}

		public void setUid(String uid) {
			this.uid = uid;
		}

		public String getUID() {
			return capitalUid;
		}

		public void setUID(String uid) {
			capitalUid = uid;
		}

	}

	@Test
	public void caseInsensitivePropertyFilter() throws Exception {
		// GIVEN
		SerializableServiceRef ref = new SerializableServiceRef(1, 1);
		ServiceReference<?>[] services = new ServiceReference<?>[] { ref };
		expect(bundleContext.getServiceReferences(Serializable.class.getName(), null))
				.andReturn(services);
		TestService service = new TestService();
		expect(bundleContext.<Serializable> getService(ref)).andReturn(service);
		expect(bundleContext.ungetService(ref)).andReturn(true);

		DynamicServiceTracker<TestService> tracker = new DynamicServiceTracker<>(bundleContext,
				Serializable.class);

		// WHEN
		replayAll();

		service.uid = "foo";
		service.capitalUid = "bar";
		tracker.setPropertyFilter("uid", "no match here");
		tracker.setPropertyFilter("UID", "bar"); // last one set wins
		TestService s = tracker.service();

		// THEN
		assertThat("Service props case-insensitive", tracker.getPropertyFilters().keySet(),
				contains("UID"));
		assertThat("Service returned via ref", s, is(sameInstance(service)));
	}

	@Test
	public void requireFilter_noFilter() throws Exception {
		// GIVEN
		DynamicServiceTracker<TestService> tracker = new DynamicServiceTracker<>(bundleContext,
				Serializable.class);
		tracker.setRequirePropertyFilter(true);

		// WHEN
		replayAll();

		TestService s = tracker.service();

		// THEN
		assertThat("Service not returned because no filter configured", s, is(nullValue()));
	}

	@Test
	public void requireFilter_nullFilter() throws Exception {
		// GIVEN
		DynamicServiceTracker<TestService> tracker = new DynamicServiceTracker<>(bundleContext,
				Serializable.class);
		tracker.setRequirePropertyFilter(true);

		// WHEN
		replayAll();

		tracker.setPropertyFilter("uid", null);
		TestService s = tracker.service();

		// THEN
		assertThat("Service not returned because filter is null", s, is(nullValue()));
	}

	@Test
	public void requireFilter_emptyFilter() throws Exception {
		// GIVEN
		DynamicServiceTracker<TestService> tracker = new DynamicServiceTracker<>(bundleContext,
				Serializable.class);
		tracker.setRequirePropertyFilter(true);

		// WHEN
		replayAll();

		tracker.setPropertyFilter("uid", "");
		TestService s = tracker.service();

		// THEN
		assertThat("Service not returned because filter is empty", s, is(nullValue()));
	}

	@Test
	public void requireFilter_emptyFilter_ignoreEmptyDisabled() throws Exception {
		// GIVEN
		SerializableServiceRef ref = new SerializableServiceRef(1, 1);
		ServiceReference<?>[] services = new ServiceReference<?>[] { ref };
		expect(bundleContext.getServiceReferences(Serializable.class.getName(), null))
				.andReturn(services);
		TestService service = new TestService();
		expect(bundleContext.<Serializable> getService(ref)).andReturn(service);
		expect(bundleContext.ungetService(ref)).andReturn(true);

		DynamicServiceTracker<TestService> tracker = new DynamicServiceTracker<>(bundleContext,
				Serializable.class);
		tracker.setRequirePropertyFilter(true);
		tracker.setIgnoreEmptyPropertyFilterValues(false);

		// WHEN
		replayAll();

		tracker.setPropertyFilter("uid", "");
		TestService s = tracker.service();

		// THEN
		assertThat("Service not returned because empty filter does not match", s, is(nullValue()));
	}

	@Test
	public void list_requireFilter_noFilter() throws Exception {
		// GIVEN
		DynamicServiceTracker<TestService> tracker = new DynamicServiceTracker<>(bundleContext,
				Serializable.class);
		tracker.setRequirePropertyFilter(true);

		// WHEN
		replayAll();

		Iterable<TestService> s = tracker.services();

		// THEN
		List<TestService> l = stream(s.spliterator(), false).collect(toList());
		assertThat("Service not returned because no filter configured", l, hasSize(0));
	}

	@Test
	public void list_requireFilter_nullFilter() throws Exception {
		// GIVEN
		DynamicServiceTracker<TestService> tracker = new DynamicServiceTracker<>(bundleContext,
				Serializable.class);
		tracker.setRequirePropertyFilter(true);

		// WHEN
		replayAll();

		tracker.setPropertyFilter("uid", null);
		Iterable<TestService> s = tracker.services();

		// THEN
		List<TestService> l = stream(s.spliterator(), false).collect(toList());
		assertThat("Service not returned because filter is null", l, hasSize(0));
	}

	@Test
	public void list_requireFilter_emptyFilter() throws Exception {
		// GIVEN
		DynamicServiceTracker<TestService> tracker = new DynamicServiceTracker<>(bundleContext,
				Serializable.class);
		tracker.setRequirePropertyFilter(true);

		// WHEN
		replayAll();

		tracker.setPropertyFilter("uid", "");
		Iterable<TestService> s = tracker.services();

		// THEN
		List<TestService> l = stream(s.spliterator(), false).collect(toList());
		assertThat("Service not returned because filter is empty", l, hasSize(0));
	}

	@Test
	public void list_requireFilter_emptyFilter_ignoreEmptyDisabled() throws Exception {
		// GIVEN
		SerializableServiceRef ref = new SerializableServiceRef(1, 1);
		ServiceReference<?>[] services = new ServiceReference<?>[] { ref };
		expect(bundleContext.getServiceReferences(Serializable.class.getName(), null))
				.andReturn(services);
		TestService service = new TestService();
		expect(bundleContext.<Serializable> getService(ref)).andReturn(service);
		expect(bundleContext.ungetService(ref)).andReturn(true);

		DynamicServiceTracker<TestService> tracker = new DynamicServiceTracker<>(bundleContext,
				Serializable.class);
		tracker.setRequirePropertyFilter(true);
		tracker.setIgnoreEmptyPropertyFilterValues(false);

		// WHEN
		replayAll();

		tracker.setPropertyFilter("uid", "");
		Iterable<TestService> s = tracker.services();

		// THEN
		List<TestService> l = stream(s.spliterator(), false).collect(toList());
		assertThat("Service not returned because empty filter does not match", l, hasSize(0));
	}

	@Test
	public void list_matchingAndNotMatching() throws Exception {
		// GIVEN
		SerializableServiceRef ref1 = new SerializableServiceRef(1, 1);
		SerializableServiceRef ref2 = new SerializableServiceRef(2, 0);
		ServiceReference<?>[] services = new ServiceReference<?>[] { ref1, ref2 };
		expect(bundleContext.getServiceReferences(Serializable.class.getName(), null))
				.andReturn(services);
		TestService service1 = new TestService();
		service1.uid = "foo";
		expect(bundleContext.<Serializable> getService(ref1)).andReturn(service1);
		expect(bundleContext.ungetService(ref1)).andReturn(true);
		TestService service2 = new TestService();
		service2.uid = "bar";
		expect(bundleContext.<Serializable> getService(ref2)).andReturn(service2);
		expect(bundleContext.ungetService(ref2)).andReturn(true);

		DynamicServiceTracker<TestService> tracker = new DynamicServiceTracker<>(bundleContext,
				Serializable.class);

		// WHEN
		replayAll();

		tracker.setPropertyFilter("uid", "bar");
		Iterable<TestService> s = tracker.services();

		// THEN
		List<TestService> l = stream(s.spliterator(), false).collect(toList());
		assertThat("Only matching service returned, all services released", l, contains(service2));
	}

	@Test
	public void sticky_configurationChanged_propertyFilter() throws Exception {
		// GIVEN
		SerializableServiceRef ref1 = new SerializableServiceRef(1, 0);
		SerializableServiceRef ref2 = new SerializableServiceRef(2, 0);
		ServiceReference<?>[] services = new ServiceReference<?>[] { ref1, ref2 };
		expect(bundleContext.getServiceReferences(Serializable.class.getName(), null))
				.andReturn(services).times(2);
		TestService service1 = new TestService();
		service1.uid = "foo";
		expect(bundleContext.<Serializable> getService(ref1)).andReturn(service1).times(2);
		expect(bundleContext.ungetService(ref1)).andReturn(true).times(2);
		TestService service2 = new TestService();
		service2.uid = "bar";
		expect(bundleContext.<Serializable> getService(ref2)).andReturn(service2);
		expect(bundleContext.ungetService(ref2)).andReturn(true);

		// WHEN
		replayAll();

		DynamicServiceTracker<TestService> tracker = new DynamicServiceTracker<>(bundleContext,
				Serializable.class);
		tracker.setSticky(true);
		tracker.setPropertyFilter("uid", "foo");
		TestService s1 = tracker.service();
		TestService s2 = tracker.service();

		// change the filter map directly, as a propertyFilters['uid'] setting does
		tracker.getPropertyFilters().put("uid", "bar");
		tracker.configurationChanged(Map.of("propertyFilters['uid']", "bar"));
		TestService s3 = tracker.service();

		// THEN
		assertThat("Service returned via ref", s1, is(sameInstance(service1)));
		assertThat("Service returned via sticky cache", s2, is(sameInstance(service1)));
		assertThat("Service resolved again after configuration change", s3, is(sameInstance(service2)));
	}

	@Test
	public void sticky_configurationChanged_whileNotSticky() throws Exception {
		// GIVEN
		SerializableServiceRef ref1 = new SerializableServiceRef(1, 0);
		SerializableServiceRef ref2 = new SerializableServiceRef(2, 0);
		ServiceReference<?>[] services = new ServiceReference<?>[] { ref1, ref2 };
		expect(bundleContext.getServiceReferences(Serializable.class.getName(), null))
				.andReturn(services).times(2);
		TestService service1 = new TestService();
		service1.uid = "foo";
		expect(bundleContext.<Serializable> getService(ref1)).andReturn(service1).times(2);
		expect(bundleContext.ungetService(ref1)).andReturn(true).times(2);
		TestService service2 = new TestService();
		service2.uid = "bar";
		expect(bundleContext.<Serializable> getService(ref2)).andReturn(service2);
		expect(bundleContext.ungetService(ref2)).andReturn(true);

		// WHEN
		replayAll();

		DynamicServiceTracker<TestService> tracker = new DynamicServiceTracker<>(bundleContext,
				Serializable.class);
		tracker.setSticky(true);
		tracker.setPropertyFilter("uid", "foo");
		TestService s1 = tracker.service();

		tracker.setSticky(false);
		tracker.getPropertyFilters().put("uid", "bar");
		tracker.configurationChanged(Map.of("propertyFilters['uid']", "bar"));
		tracker.setSticky(true);
		TestService s2 = tracker.service();

		// THEN
		assertThat("Service returned via ref", s1, is(sameInstance(service1)));
		assertThat("Service resolved again after configuration change while not sticky", s2,
				is(sameInstance(service2)));
	}

	@Test
	public void sticky_configurationChanged_serviceFilter() throws Exception {
		// GIVEN
		SerializableServiceRef ref1 = new SerializableServiceRef(1, 0);
		expect(bundleContext.getServiceReferences(Serializable.class.getName(), null))
				.andReturn(new ServiceReference<?>[] { ref1 });
		TestService service1 = new TestService();
		expect(bundleContext.<Serializable> getService(ref1)).andReturn(service1);
		expect(bundleContext.ungetService(ref1)).andReturn(true);

		final String filter = "(foo=bar)";
		SerializableServiceRef ref2 = new SerializableServiceRef(2, 0);
		expect(bundleContext.getServiceReferences(Serializable.class.getName(), filter))
				.andReturn(new ServiceReference<?>[] { ref2 });
		TestService service2 = new TestService();
		expect(bundleContext.<Serializable> getService(ref2)).andReturn(service2);
		expect(bundleContext.ungetService(ref2)).andReturn(true);

		// WHEN
		replayAll();

		DynamicServiceTracker<TestService> tracker = new DynamicServiceTracker<>(bundleContext,
				Serializable.class);
		tracker.setSticky(true);
		TestService s1 = tracker.service();
		tracker.setServiceFilter(filter);
		tracker.configurationChanged(Map.of("serviceFilter", filter));
		TestService s2 = tracker.service();

		// THEN
		assertThat("Service returned via ref", s1, is(sameInstance(service1)));
		assertThat("Service resolved again after configuration change", s2, is(sameInstance(service2)));
	}

	public static final class ThrowingService implements Serializable {

		private static final long serialVersionUID = 3069377004717433226L;

		public String getUid() {
			throw new UnsupportedOperationException("Test getter failure");
		}

	}

	@Test
	public void propertyGetterThrows_skipAndRelease() throws Exception {
		// GIVEN
		SerializableServiceRef ref1 = new SerializableServiceRef(1, 1);
		SerializableServiceRef ref2 = new SerializableServiceRef(2, 0);
		ServiceReference<?>[] services = new ServiceReference<?>[] { ref1, ref2 };
		expect(bundleContext.getServiceReferences(Serializable.class.getName(), null))
				.andReturn(services);
		expect(bundleContext.<Serializable> getService(ref1)).andReturn(new ThrowingService());
		expect(bundleContext.ungetService(ref1)).andReturn(true);
		TestService service2 = new TestService();
		service2.uid = "foo";
		expect(bundleContext.<Serializable> getService(ref2)).andReturn(service2);
		expect(bundleContext.ungetService(ref2)).andReturn(true);

		DynamicServiceTracker<TestService> tracker = new DynamicServiceTracker<>(bundleContext,
				Serializable.class);

		// WHEN
		replayAll();

		tracker.setPropertyFilter("uid", "foo");
		TestService s = tracker.service();

		// THEN
		assertThat("Service with failing getter skipped and released, next match returned", s,
				is(sameInstance(service2)));
	}

	public static final class GroupsService implements Serializable {

		private static final long serialVersionUID = 5107268441232415416L;

		public SortedSet<String> getGroups() {
			return new TreeSet<>(Set.of("a"));
		}

	}

	@Test
	public void filterComparisonThrows_release() throws Exception {
		// GIVEN
		SerializableServiceRef ref = new SerializableServiceRef(1, 0);
		expect(bundleContext.getServiceReferences(Serializable.class.getName(), null))
				.andReturn(new ServiceReference<?>[] { ref });
		expect(bundleContext.<Serializable> getService(ref)).andReturn(new GroupsService());
		expect(bundleContext.ungetService(ref)).andReturn(true);

		DynamicServiceTracker<GroupsService> tracker = new DynamicServiceTracker<>(bundleContext,
				Serializable.class);

		// WHEN
		replayAll();

		// a number cannot be compared to the service's sorted set of strings
		tracker.setPropertyFilter("groups", 1);
		try {
			tracker.service();
			fail("Filter comparison exception should be thrown");
		} catch ( ClassCastException e ) {
			// THEN
			// expected, and service released
		}
	}

	@Test
	public void list_filterComparisonThrows_release() throws Exception {
		// GIVEN
		SerializableServiceRef ref = new SerializableServiceRef(1, 0);
		expect(bundleContext.getServiceReferences(Serializable.class.getName(), null))
				.andReturn(new ServiceReference<?>[] { ref });
		expect(bundleContext.<Serializable> getService(ref)).andReturn(new GroupsService());
		expect(bundleContext.ungetService(ref)).andReturn(true);

		DynamicServiceTracker<GroupsService> tracker = new DynamicServiceTracker<>(bundleContext,
				Serializable.class);

		// WHEN
		replayAll();

		// a number cannot be compared to the service's sorted set of strings
		tracker.setPropertyFilter("groups", 1);
		try {
			tracker.services();
			fail("Filter comparison exception should be thrown");
		} catch ( ClassCastException e ) {
			// THEN
			// expected, and service released
		}
	}

}
