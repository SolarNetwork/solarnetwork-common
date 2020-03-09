/* ==================================================================
 * FallbackOptionalServiceTests.java - 9/03/2020 2:36:48 pm
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

package net.solarnetwork.util.test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import net.solarnetwork.util.FallbackOptionalService;
import net.solarnetwork.util.StaticOptionalService;

/**
 * Test cases for the {@link FallbackOptionalService} class.
 * 
 * @author matt
 * @version 1.0
 */
public class FallbackOptionalServiceTests {

	@Test
	public void firstIsAvailable() {
		// given
		final Object o1 = new Object();
		FallbackOptionalService<Object> s = new FallbackOptionalService<>(
				asList(new StaticOptionalService<>(o1), new StaticOptionalService<>(null)));

		// when
		Object o = s.service();

		// then
		assertThat("First service returned because non-null", o, sameInstance(o1));
	}

	@Test
	public void secondIsAvailable() {
		// given
		final Object o1 = new Object();
		FallbackOptionalService<Object> s = new FallbackOptionalService<>(
				asList(new StaticOptionalService<>(null), new StaticOptionalService<>(o1)));

		// when
		Object o = s.service();

		// then
		assertThat("Second service returned because non-null", o, sameInstance(o1));
	}

	@Test
	public void noOptionalServices() {
		// given
		FallbackOptionalService<Object> s = new FallbackOptionalService<>(emptyList());

		// when
		Object o = s.service();

		// then
		assertThat("Null returned because no optional services available", o, nullValue());
	}

	@Test
	public void noServices() {
		// given
		FallbackOptionalService<Object> s = new FallbackOptionalService<>(
				asList(new StaticOptionalService<>(null), new StaticOptionalService<>(null)));

		// when
		Object o = s.service();

		// then
		assertThat("Null returned because no service available", o, nullValue());
	}
}
