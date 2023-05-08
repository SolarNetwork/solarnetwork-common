/* ==================================================================
 * BasicRegistrationReceiptTests.java - 1/05/2023 7:18:34 am
 * 
 * Copyright 2023 SolarNetwork.net Dev Team
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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import org.junit.Test;
import net.solarnetwork.domain.BasicRegistrationReceipt;

/**
 * Test cases for the {@link BasicRegistrationReceipt} class.
 * 
 * @author matt
 * @version 1.0
 */
public class BasicRegistrationReceiptTests {

	@Test
	public void usernameUrlComponent_withPlus() {
		// GIVEN
		BasicRegistrationReceipt r = new BasicRegistrationReceipt("foo+bar@example.com", "abc123");

		// WHEN
		String result = r.getUsernameURLComponent();

		// THEN
		assertThat("Username encoded with +", result, is(equalTo("foo%2Bbar%40example.com")));
	}

}
