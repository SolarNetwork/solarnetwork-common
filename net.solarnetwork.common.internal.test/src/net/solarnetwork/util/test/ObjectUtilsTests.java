/* ==================================================================
 * ObjectUtilsTests.java - 7/10/2021 10:22:46 AM
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

package net.solarnetwork.util.test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import org.junit.Assert;
import org.junit.Test;
import net.solarnetwork.util.ObjectUtils;

/**
 * FIXME
 * 
 * <p>
 * TODO
 * </p>
 * 
 * @author matt
 * @version 1.0
 */
public class ObjectUtilsTests {

	@Test
	public void requireNonNullArgument_notNull() {
		// WHEN
		String arg = "foo";
		String result = ObjectUtils.requireNonNullArgument(arg, "fooBar");

		// THEN
		assertThat("Object should be returned when not null", result, is(sameInstance(arg)));
	}

	@Test
	public void requireNonNullArgument_null() {
		// WHEN
		try {
			ObjectUtils.requireNonNullArgument(null, "fooBar");
			Assert.fail("Should have thrown IllegalArgumentException");
		} catch ( IllegalArgumentException e ) {
			assertThat("Message should include argument name", e.getMessage(),
					is("The fooBar argument must not be null."));
		}
	}

}
