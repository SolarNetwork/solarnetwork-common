/* ==================================================================
 * SimpleLocationTests.java - 18/05/2022 10:05:06 am
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
import net.solarnetwork.domain.SimpleLocation;

/**
 * Test cases for the {@link SimpleLocation} class.
 * 
 * @author matt
 * @version 1.0
 */
public class SimpleLocationTests {

	@Test
	public void isSameAs_decimals() {
		// GIVEN
		SimpleLocation loc1 = new SimpleLocation();
		loc1.setLatitude(new BigDecimal("1.234"));
		loc1.setLongitude(new BigDecimal("2.345"));
		loc1.setElevation(new BigDecimal("3.456"));
		SimpleLocation loc2 = new SimpleLocation();
		loc2.setLatitude(new BigDecimal("1.234000"));
		loc2.setLongitude(new BigDecimal("2.345000"));
		loc2.setElevation(new BigDecimal("3.456000"));

		// THEN
		assertThat("Decimals compared without scale", loc1.equals(loc2), is(true));
	}

}
