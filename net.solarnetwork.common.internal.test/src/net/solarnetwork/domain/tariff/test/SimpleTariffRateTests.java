/* ==================================================================
 * SimpleTariffRateTests.java - 12/05/2021 4:44:01 PM
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

package net.solarnetwork.domain.tariff.test;

import static org.junit.Assert.assertThat;
import java.math.BigDecimal;
import org.hamcrest.Matchers;
import org.junit.Test;
import net.solarnetwork.domain.tariff.SimpleTariffRate;

/**
 * Test cases for the {@link SimpleTariffRate} class.
 * 
 * @author matt
 * @version 1.0
 */
public class SimpleTariffRateTests {

	@Test
	public void idFromDescription() {
		// GIVEN
		SimpleTariffRate r = new SimpleTariffRate("TOU Rate", BigDecimal.ONE);

		// THEN
		assertThat("Simple ID created from description", r.getId(), Matchers.equalTo("tou-rate"));
	}

}
