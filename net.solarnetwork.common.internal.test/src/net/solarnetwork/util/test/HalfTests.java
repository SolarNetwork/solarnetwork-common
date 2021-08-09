/* ==================================================================
 * HalfTests.java - 7/08/2021 3:35:11 PM
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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import net.solarnetwork.util.Half;

/**
 * Test cases for the {@link Half} class.
 * 
 * @author matt
 * @version 1.0
 */
public class HalfTests {

	@Test
	public void valueOf_string_ok() {
		Half h = Half.valueOf("1.23");
		assertThat("Half parsed", h.halfValue(), equalTo((short) 0x3CEC));
	}

	@Test
	public void valueOf_half_ok() {
		Half h = Half.valueOf((short) 0x4DDA);
		assertThat("Half round trip", h.halfValue(), equalTo((short) 0x4DDA));
		assertThat("Half string", h.toString(), equalTo("23.40625"));
	}

}
