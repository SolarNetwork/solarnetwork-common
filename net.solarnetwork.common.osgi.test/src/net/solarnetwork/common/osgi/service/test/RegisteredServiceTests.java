/* ==================================================================
 * RegisteredServiceTests.java - 10/11/2026 7:40:00 AM
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

import static org.easymock.EasyMock.expectLastCall;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import java.util.Map;
import org.easymock.EasyMock;
import org.junit.Test;
import org.osgi.framework.ServiceRegistration;
import net.solarnetwork.common.osgi.service.RegisteredService;

/**
 * Test cases for the {@link RegisteredService} class.
 *
 * @author matt
 * @version 1.0
 */
@SuppressWarnings({ "deprecation", "removal" })
public class RegisteredServiceTests {

	@Test
	public void areMapsSame_objectArray() {
		// WHEN
		boolean result = RegisteredService.areMapsSame(Map.of("a", new String[] { "1", "2" }),
				Map.of("a", new String[] { "1", "2" }));

		// THEN
		assertThat("Equal object arrays are same", result, is(true));
	}

	@Test
	public void areMapsSame_primitiveArray() {
		// WHEN
		boolean result = RegisteredService.areMapsSame(Map.of("a", new int[] { 1, 2 }),
				Map.of("a", new int[] { 1, 2 }));

		// THEN
		assertThat("Equal primitive arrays are same", result, is(true));
	}

	@Test
	public void areMapsSame_primitiveArray_notEqual() {
		// WHEN
		boolean result = RegisteredService.areMapsSame(Map.of("a", new int[] { 1, 2 }),
				Map.of("a", new int[] { 1, 3 }));

		// THEN
		assertThat("Different primitive arrays are not same", result, is(false));
	}

	@Test
	public void areMapsSame_differentArrayTypes() {
		// WHEN
		boolean result = RegisteredService.areMapsSame(Map.of("a", new String[] { "1" }),
				Map.of("a", new int[] { 1 }));

		// THEN
		assertThat("Arrays of different types are not same", result, is(false));
	}

	@Test
	public void unregister_alreadyUnregistered() {
		// GIVEN
		ServiceRegistration<?> reg1 = EasyMock.createMock(ServiceRegistration.class);
		ServiceRegistration<?> reg2 = EasyMock.createMock(ServiceRegistration.class);

		// the first registration is already gone, e.g. because its bundle stopped
		reg1.unregister();
		expectLastCall().andThrow(new IllegalStateException("Service already unregistered"));
		reg2.unregister();

		RegisteredService<String> service = new RegisteredService<>("foo", Map.of());
		service.addReg(reg1);
		service.addReg(reg2);

		// WHEN
		EasyMock.replay(reg1, reg2);
		service.unregister();

		// THEN
		// the second registration is unregistered too
		EasyMock.verify(reg1, reg2);
	}

}
