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

package net.solarnetwork.util.test;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import java.io.Serializable;
import java.util.UUID;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import net.solarnetwork.util.DynamicServiceTracker;
import net.solarnetwork.util.test.TestServiceReference.SerializableServiceRef;

/**
 * Test cases for the {@link DynamicServiceTracker} class.
 * 
 * @author matt
 * @version 1.0
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
		// given
		SerializableServiceRef ref = new SerializableServiceRef(2, 10);
		ServiceReference<?>[] services = new ServiceReference<?>[] { new SerializableServiceRef(1, 0),
				ref, new SerializableServiceRef(3, 5), };
		expect(bundleContext.getServiceReferences(Serializable.class.getName(), null))
				.andReturn(services);
		UUID uuid = UUID.randomUUID();
		expect(bundleContext.<Serializable> getService(ref)).andReturn(uuid);

		// when
		replayAll();

		DynamicServiceTracker<UUID> tracker = new DynamicServiceTracker<UUID>();
		tracker.setBundleContext(bundleContext);
		tracker.setServiceClassName(Serializable.class.getName());

		UUID s = tracker.service();

		// then
		assertThat("Highest rank returned", s, sameInstance(uuid));
	}
}
