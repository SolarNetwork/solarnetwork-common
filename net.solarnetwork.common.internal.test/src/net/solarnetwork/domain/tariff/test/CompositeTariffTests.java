/* ==================================================================
 * CompositeTariffTests.java - 12/05/2021 5:24:17 PM
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
import net.solarnetwork.domain.tariff.CompositeTariff;
import net.solarnetwork.domain.tariff.SimpleTariff;
import net.solarnetwork.domain.tariff.SimpleTariffRate;
import net.solarnetwork.domain.tariff.Tariff.Rate;

/**
 * Test cases for the {@link CompositeTariff} class.
 * 
 * @author matt
 * @version 1.0
 */
public class CompositeTariffTests {

	@Test
	public void construct() {
		// GIVEN
		// @formatter:off
		List<Rate> rateList = asList(
				new SimpleTariffRate("zero", BigDecimal.ZERO),
				new SimpleTariffRate("one", BigDecimal.ONE),
				new SimpleTariffRate("ten", BigDecimal.TEN),
				new SimpleTariffRate("zero", new BigDecimal("0.01")),
				new SimpleTariffRate("0-1", new BigDecimal("0.1"))
				);
		// @formatter:on

		SimpleTariff t1 = new SimpleTariff(asList(rateList.get(0), rateList.get(1)));
		SimpleTariff t2 = new SimpleTariff(asList(rateList.get(2)));
		SimpleTariff t3 = new SimpleTariff(asList(rateList.get(3), rateList.get(4)));

		// WHEN
		CompositeTariff t = new CompositeTariff(asList(t1, t2, t3));
		Map<String, Rate> rates = t.getRates();

		// THEN
		assertThat("Rates created", rates, notNullValue());
		assertThat("Rate keys maintain order", rates.keySet(), contains("zero", "one", "ten", "0-1"));
		assertThat("Rate zero", rates, hasEntry("zero", rateList.get(0)));
		assertThat("Rate one", rates, hasEntry("one", rateList.get(1)));
		assertThat("Rate ten", rates, hasEntry("ten", rateList.get(2)));
		// rate 3 is duplicate and skipped
		assertThat("Rate 0-1", rates, hasEntry("0-1", rateList.get(4)));
	}

}
