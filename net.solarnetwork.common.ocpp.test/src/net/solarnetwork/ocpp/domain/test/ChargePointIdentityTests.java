/* ==================================================================
 * ChargePointIdentityTests.java - 18/11/2020 4:33:55 pm
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

package net.solarnetwork.ocpp.domain.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import net.solarnetwork.ocpp.domain.ChargePointIdentity;

/**
 * Test cases for the {@link ChargePointIdentity} class.
 * 
 * @author matt
 * @version 1.0
 */
public class ChargePointIdentityTests {

	@Test
	public void equals() {
		// GIVEN
		ChargePointIdentity a = new ChargePointIdentity("a", Long.valueOf(1L));
		ChargePointIdentity b = new ChargePointIdentity("a", Long.valueOf(1L));

		// THEN
		assertThat("Objects are equal when identifier and userIdentifier are equal", a, equalTo(b));
	}

	@Test
	public void equals_integerUserIdentiferConvertedToLong() {
		// GIVEN
		ChargePointIdentity a = new ChargePointIdentity("a", Integer.valueOf(1));
		ChargePointIdentity b = new ChargePointIdentity("a", Long.valueOf(1L));

		// THEN
		assertThat("Integer userIdentifier converted to Long", a.getUserIdentifier(), equalTo(1L));
		assertThat("Integer userIdentifier compared as Long", a, equalTo(b));
	}

}
