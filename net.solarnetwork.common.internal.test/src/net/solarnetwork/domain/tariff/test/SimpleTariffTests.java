/* ==================================================================
 * SimpleTariffTests.java - 12/05/2021 5:17:02 PM
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

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import net.solarnetwork.domain.tariff.SimpleTariff;
import net.solarnetwork.domain.tariff.SimpleTariffRate;
import net.solarnetwork.domain.tariff.Tariff.Rate;

/**
 * Test cases for the {@link SimpleTariff} class.
 * 
 * @author matt
 * @version 1.0
 */
public class SimpleTariffTests {

	@Test
	public void constructFromCollection() {
		// GIVEN
		List<Rate> rateList = asList(new SimpleTariffRate("zero", BigDecimal.ZERO),
				new SimpleTariffRate("one", BigDecimal.ONE),
				new SimpleTariffRate("ten", BigDecimal.TEN));

		// WHEN
		SimpleTariff t = new SimpleTariff(rateList);
		Map<String, Rate> rates = t.getRates();

		// THEN
		assertThat("Rates created", rates, notNullValue());
		assertThat("Rate keys maintain order", rates.keySet(), contains("zero", "one", "ten"));
		assertThat("Rate zero", rates, hasEntry("zero", rateList.get(0)));
		assertThat("Rate one", rates, hasEntry("one", rateList.get(1)));
		assertThat("Rate ten", rates, hasEntry("ten", rateList.get(2)));
	}

}
