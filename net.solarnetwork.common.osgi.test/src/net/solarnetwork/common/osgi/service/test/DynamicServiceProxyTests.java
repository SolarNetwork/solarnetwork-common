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
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import net.solarnetwork.common.osgi.service.DynamicServiceProxy;
import net.solarnetwork.common.osgi.test.TestServiceReference;

/**
 * Test cases for the {@link DynamicServiceProxy} class.
 *
 * @author matt
 * @version 1.0
 */
public class DynamicServiceProxyTests {

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

}
