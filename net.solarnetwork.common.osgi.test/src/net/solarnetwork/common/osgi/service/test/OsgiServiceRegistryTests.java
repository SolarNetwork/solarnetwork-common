/* ==================================================================
 * OsgiServiceRegistryTests.java - 9/11/2026 10:31:07 AM
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
import static org.hamcrest.Matchers.contains;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import net.solarnetwork.common.osgi.service.OsgiServiceRegistry;
import net.solarnetwork.common.osgi.test.TestServiceReference.SerializableServiceRef;

/**
 * Test cases for the {@link OsgiServiceRegistry} class.
 *
 * @author matt
 * @version 1.0
 */
public class OsgiServiceRegistryTests {

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
	public void services_filter_releaseServices() throws Exception {
		// GIVEN
		final String filter = "(foo=bar)";
		SerializableServiceRef ref1 = new SerializableServiceRef(1, 0);
		SerializableServiceRef ref2 = new SerializableServiceRef(2, 0);
		SerializableServiceRef ref3 = new SerializableServiceRef(3, 0);
		expect(bundleContext.getAllServiceReferences(null, filter))
				.andReturn(new ServiceReference<?>[] { ref1, ref2, ref3 });
		UUID uuid1 = UUID.randomUUID();
		UUID uuid3 = UUID.randomUUID();
		expect(bundleContext.<Serializable> getService(ref1)).andReturn(uuid1);
		expect(bundleContext.ungetService(ref1)).andReturn(true);
		expect(bundleContext.<Serializable> getService(ref2)).andReturn(null);
		expect(bundleContext.<Serializable> getService(ref3)).andReturn(uuid3);
		expect(bundleContext.ungetService(ref3)).andReturn(true);

		// WHEN
		replayAll();
		OsgiServiceRegistry registry = new OsgiServiceRegistry(bundleContext);
		Collection<?> result = registry.services(filter);

		// THEN
		assertThat("Available services returned", result, contains(uuid1, uuid3));
	}

	@Test
	public void services_class_releaseServices() throws Exception {
		// GIVEN
		SerializableServiceRef ref1 = new SerializableServiceRef(1, 0);
		SerializableServiceRef ref2 = new SerializableServiceRef(2, 0);
		SerializableServiceRef ref3 = new SerializableServiceRef(3, 0);
		expect(bundleContext.getServiceReferences(Serializable.class, null))
				.andReturn(Arrays.asList(ref1, ref2, ref3));
		UUID uuid1 = UUID.randomUUID();
		UUID uuid3 = UUID.randomUUID();
		expect(bundleContext.getService(ref1)).andReturn(uuid1);
		expect(bundleContext.ungetService(ref1)).andReturn(true);
		expect(bundleContext.getService(ref2)).andReturn(null);
		expect(bundleContext.getService(ref3)).andReturn(uuid3);
		expect(bundleContext.ungetService(ref3)).andReturn(true);

		// WHEN
		replayAll();
		OsgiServiceRegistry registry = new OsgiServiceRegistry(bundleContext);
		Collection<Serializable> result = registry.services(Serializable.class, null);

		// THEN
		assertThat("Available services returned", result, contains(uuid1, uuid3));
	}

}
