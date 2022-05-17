/* ==================================================================
 * BasicLocationTests.java - 18/05/2022 10:05:06 am
 * 
 * Copyright 2022 SolarNetwork.net Dev Team
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

package net.solarnetwork.domain.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import java.math.BigDecimal;
import org.junit.Test;
import net.solarnetwork.domain.BasicLocation;

/**
 * Test cases for the {@link BasicLocation} class.
 * 
 * @author matt
 * @version 1.0
 */
public class BasicLocationTests {

	@Test
	public void isSameAs_decimals() {
		// GIVEN
		BasicLocation loc1 = new BasicLocation(null, null, null, null, null, null, null,
				new BigDecimal("1.234"), new BigDecimal("2.345"), new BigDecimal("3.456"), null);
		BasicLocation loc2 = new BasicLocation(null, null, null, null, null, null, null,
				new BigDecimal("1.234000"), new BigDecimal("2.345000"), new BigDecimal("3.456000"),
				null);

		// THEN
		assertThat("Decimals compared without scale", loc1.equals(loc2), is(true));
	}

}
