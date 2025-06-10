/* ==================================================================
 * OptionalServiceTests.java - 24/09/2021 5:08:42 PM
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

package net.solarnetwork.service.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import org.junit.Test;
import net.solarnetwork.service.OptionalService;
import net.solarnetwork.service.OptionalServiceNotAvailableException;
import net.solarnetwork.service.StaticOptionalService;

/**
 * Test cases for the {@link OptionalService} API.
 * 
 * @author matt
 * @version 1.0
 */
public class OptionalServiceTests {

	@Test(expected = OptionalServiceNotAvailableException.class)
	public void resolveRequired_nullOptional() {
		// GIVEN
		String name = "My Service";

		// WHEN
		try {
			OptionalService.requiredService(null, name);
		} catch ( OptionalServiceNotAvailableException e ) {
			assertThat("Message contains service name", e.getMessage(), containsString(name));
			assertThat("Message reflects null input", e.getMessage(), containsString("not configured"));
			throw e;
		}
	}

	@Test(expected = OptionalServiceNotAvailableException.class)
	public void resolveRequired_nullService() {
		// GIVEN
		OptionalService<Object> optional = new StaticOptionalService<>(null);
		String name = "My Service";

		// WHEN
		try {
			OptionalService.requiredService(optional, name);
		} catch ( OptionalServiceNotAvailableException e ) {
			assertThat("Message contains service name", e.getMessage(), containsString(name));
			assertThat("Message reflects unresolved result", e.getMessage(),
					containsString("not available"));
			throw e;
		}
	}

	@Test(expected = OptionalServiceNotAvailableException.class)
	public void resolveRequired_nullServiceWithFilter() {
		// GIVEN
		StaticOptionalService<Object> optional = new StaticOptionalService<>(null);
		optional.setPropertyFilter("uid", "foo");
		String name = "My Service";

		// WHEN
		try {
			OptionalService.requiredService(optional, name);
		} catch ( OptionalServiceNotAvailableException e ) {
			assertThat("Message contains service name", e.getMessage(), containsString(name));
			assertThat("Message reflects unresolved result", e.getMessage(),
					containsString("not available"));
			assertThat("Message contains filter map", e.getMessage(),
					containsString("matching filter " + optional.getPropertyFilters().toString()));
			throw e;
		}
	}

}
