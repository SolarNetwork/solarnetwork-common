/* ==================================================================
 * DynamicServiceProxyTests.java - 9/11/2026 10:12:31 AM
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

package net.solarnetwork.common.osgi.service.test;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import java.util.Collections;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Supplier;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.wiring.BundleWiring;
import net.solarnetwork.common.osgi.service.DynamicServiceProxy;
import net.solarnetwork.common.osgi.test.TestServiceReference;
import net.solarnetwork.service.FilterableService;

/**
 * Test cases for the {@link DynamicServiceProxy} class.
 *
 * @author matt
 * @version 1.0
 */
public class DynamicServiceProxyTests {

	private BundleContext bundleContext;
	private Bundle bundle;
	private BundleWiring bundleWiring;

	@Before
	public void setup() {
		bundleContext = EasyMock.createMock(BundleContext.class);
		bundle = EasyMock.createMock(Bundle.class);
		bundleWiring = EasyMock.createMock(BundleWiring.class);
	}

	@After
	public void teardown() {
		EasyMock.verify(bundleContext, bundle, bundleWiring);
	}

	private void replayAll() {
		EasyMock.replay(bundleContext, bundle, bundleWiring);
	}

	private void expectProxyClassLoader() {
		expect(bundleContext.getBundle()).andReturn(bundle);
		expect(bundle.adapt(BundleWiring.class)).andReturn(bundleWiring);
		expect(bundleWiring.getClassLoader()).andReturn(getClass().getClassLoader());
	}

	@Test
	public void invoke_releaseServiceAfterCall() throws Throwable {
		// GIVEN
		TestServiceReference<CharSequence> ref = new TestServiceReference<>(1, 0);
		expect(bundleContext.getServiceReferences(CharSequence.class.getName(), null))
				.andReturn(new ServiceReference<?>[] { ref });
		expect(bundleContext.getService(ref)).andReturn("foo");
		expect(bundleContext.ungetService(ref)).andReturn(true);

		// WHEN
		replayAll();
		DynamicServiceProxy<CharSequence> proxy = new DynamicServiceProxy<>(bundleContext,
				CharSequence.class);
		Object result = proxy.invoke(null, CharSequence.class.getMethod("length"), null);

		// THEN
		assertThat("Method invoked on service", result, is(equalTo(3)));
	}

	@Test
	public void invoke_releaseServiceAfterCall_exception() throws Throwable {
		// GIVEN
		TestServiceReference<CharSequence> ref = new TestServiceReference<>(1, 0);
		expect(bundleContext.getServiceReferences(CharSequence.class.getName(), null))
				.andReturn(new ServiceReference<?>[] { ref });
		expect(bundleContext.getService(ref)).andReturn("foo");
		expect(bundleContext.ungetService(ref)).andReturn(true);

		// WHEN
		replayAll();
		DynamicServiceProxy<CharSequence> proxy = new DynamicServiceProxy<>(bundleContext,
				CharSequence.class);
		try {
			proxy.invoke(null, CharSequence.class.getMethod("charAt", int.class), new Object[] { 10 });
			fail("Service exception should be thrown");
		} catch ( IndexOutOfBoundsException e ) {
			// THEN
			// expected, and service released
		}
	}

	@Test
	public void invoke_releaseNonMatchingService() throws Throwable {
		// GIVEN
		TestServiceReference<CharSequence> ref1 = new TestServiceReference<>(1, 1);
		TestServiceReference<CharSequence> ref2 = new TestServiceReference<>(2, 0);
		expect(bundleContext.getServiceReferences(CharSequence.class.getName(), null))
				.andReturn(new ServiceReference<?>[] { ref2, ref1 });
		expect(bundleContext.getService(ref1)).andReturn("");
		expect(bundleContext.ungetService(ref1)).andReturn(true);
		expect(bundleContext.getService(ref2)).andReturn("foo");
		expect(bundleContext.ungetService(ref2)).andReturn(true);

		// WHEN
		replayAll();
		DynamicServiceProxy<CharSequence> proxy = new DynamicServiceProxy<>(bundleContext,
				CharSequence.class);
		proxy.setPropertyFilters(Collections.singletonMap("empty", false));
		Object result = proxy.invoke(null, CharSequence.class.getMethod("length"), null);

		// THEN
		assertThat("Method invoked on matching service", result, is(equalTo(3)));
	}

	public static final class ThrowingCharSequence implements CharSequence {

		@Override
		public int length() {
			return 0;
		}

		@Override
		public char charAt(int index) {
			throw new IndexOutOfBoundsException(index);
		}

		@Override
		public CharSequence subSequence(int start, int end) {
			return this;
		}

		@Override
		public boolean isEmpty() {
			throw new UnsupportedOperationException("Test getter failure");
		}

	}

	@Test
	public void invoke_propertyGetterThrows_skipAndRelease() throws Throwable {
		// GIVEN
		TestServiceReference<CharSequence> ref1 = new TestServiceReference<>(1, 1);
		TestServiceReference<CharSequence> ref2 = new TestServiceReference<>(2, 0);
		expect(bundleContext.getServiceReferences(CharSequence.class.getName(), null))
				.andReturn(new ServiceReference<?>[] { ref1, ref2 });
		expect(bundleContext.getService(ref1)).andReturn(new ThrowingCharSequence());
		expect(bundleContext.ungetService(ref1)).andReturn(true);
		expect(bundleContext.getService(ref2)).andReturn("foo");
		expect(bundleContext.ungetService(ref2)).andReturn(true);

		// WHEN
		replayAll();
		DynamicServiceProxy<CharSequence> proxy = new DynamicServiceProxy<>(bundleContext,
				CharSequence.class);
		proxy.setPropertyFilters(Collections.singletonMap("empty", false));
		Object result = proxy.invoke(null, CharSequence.class.getMethod("length"), null);

		// THEN
		assertThat("Service with failing getter skipped and released, next match invoked", result,
				is(equalTo(3)));
	}

	public static final class GroupsCharSequence implements CharSequence {

		@Override
		public int length() {
			return 0;
		}

		@Override
		public char charAt(int index) {
			throw new IndexOutOfBoundsException(index);
		}

		@Override
		public CharSequence subSequence(int start, int end) {
			return this;
		}

		public SortedSet<String> getGroups() {
			return new TreeSet<>(Collections.singleton("a"));
		}

	}

	@Test
	public void invoke_filterComparisonThrows_release() throws Throwable {
		// GIVEN
		TestServiceReference<CharSequence> ref = new TestServiceReference<>(1, 0);
		expect(bundleContext.getServiceReferences(CharSequence.class.getName(), null))
				.andReturn(new ServiceReference<?>[] { ref });
		expect(bundleContext.getService(ref)).andReturn(new GroupsCharSequence());
		expect(bundleContext.ungetService(ref)).andReturn(true);

		// WHEN
		replayAll();
		DynamicServiceProxy<CharSequence> proxy = new DynamicServiceProxy<>(bundleContext,
				CharSequence.class);
		// a number cannot be compared to the service's sorted set of strings
		proxy.setPropertyFilters(Collections.singletonMap("groups", 1));
		try {
			proxy.invoke(null, CharSequence.class.getMethod("length"), null);
			fail("Filter comparison exception should be thrown");
		} catch ( ClassCastException e ) {
			// THEN
			// expected, and service released
		}
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void invoke_nonPublicServiceClass() throws Throwable {
		// GIVEN
		TestServiceReference<Supplier> ref = new TestServiceReference<>(1, 0);
		expect(bundleContext.getServiceReferences(Supplier.class.getName(), null))
				.andReturn(new ServiceReference<?>[] { ref });
		// the class of a lambda is not public
		Supplier<String> service = () -> "foo";
		expect(bundleContext.getService(ref)).andReturn(service);
		expect(bundleContext.ungetService(ref)).andReturn(true);

		// WHEN
		replayAll();
		DynamicServiceProxy<Supplier> proxy = new DynamicServiceProxy<>(bundleContext, Supplier.class);
		Object result = proxy.invoke(null, Supplier.class.getMethod("get"), null);

		// THEN
		assertThat("Method invoked on non-public service class", result, is(equalTo("foo")));
	}

	/**
	 * A service API with a method that matches a {@link DynamicServiceProxy}
	 * method.
	 */
	public interface ServiceFilterProvider {

		String getServiceFilter();

	}

	@Test
	public void invoke_serviceMethodMatchesProxyMethod() throws Throwable {
		// GIVEN
		TestServiceReference<ServiceFilterProvider> ref = new TestServiceReference<>(1, 0);
		expect(bundleContext.getServiceReferences(ServiceFilterProvider.class.getName(), null))
				.andReturn(new ServiceReference<?>[] { ref });
		ServiceFilterProvider service = () -> "(foo=bar)";
		expect(bundleContext.getService(ref)).andReturn(service);
		expect(bundleContext.ungetService(ref)).andReturn(true);

		// WHEN
		replayAll();
		DynamicServiceProxy<ServiceFilterProvider> proxy = new DynamicServiceProxy<>(bundleContext,
				ServiceFilterProvider.class);
		Object result = proxy.invoke(null, ServiceFilterProvider.class.getMethod("getServiceFilter"),
				null);

		// THEN
		assertThat("Method invoked on service, not proxy", result, is(equalTo("(foo=bar)")));
	}

	@Test
	public void getObject_equals() throws Exception {
		// GIVEN
		expectProxyClassLoader();

		// WHEN
		replayAll();
		DynamicServiceProxy<CharSequence> proxy = new DynamicServiceProxy<>(bundleContext,
				CharSequence.class);
		CharSequence obj = proxy.getObject();

		// THEN
		assertThat("Proxy equals itself", obj, is(equalTo(obj)));
		assertThat("Proxy does not equal other object", obj.equals("foo"), is(false));
	}

	@Test
	public void getObject_filterableService() throws Exception {
		// GIVEN
		expectProxyClassLoader();

		// WHEN
		replayAll();
		DynamicServiceProxy<CharSequence> proxy = new DynamicServiceProxy<>(bundleContext,
				CharSequence.class);
		FilterableService obj = (FilterableService) proxy.getObject();
		obj.setPropertyFilter("foo", "bar");

		// THEN
		assertThat("Filter set on proxy", proxy.getPropertyFilters(), is(equalTo(Map.of("foo", "bar"))));
		assertThat("Filter value from proxy", obj.getPropertyValue("foo"), is(equalTo("bar")));
	}

}
