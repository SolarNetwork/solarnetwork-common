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
import static java.util.Map.entry;
import static org.assertj.core.api.BDDAssertions.then;
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
 * @version 1.2
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
		// @formatter:off
		then(rates)
			.as("Expected rate mappings for all rates in given list")
			.containsOnly(
					entry("zero", rateList.get(0)),
					entry("one", rateList.get(1)),
					entry("ten", rateList.get(2))
					)
			;
		// @formatter:on
	}

	@Test
	public void rateShortcut() {
		// GIVEN
		List<Rate> rateList = asList(new SimpleTariffRate("zero", BigDecimal.ZERO),
				new SimpleTariffRate("one", BigDecimal.ONE),
				new SimpleTariffRate("ten", BigDecimal.TEN));

		// WHEN
		SimpleTariff t = new SimpleTariff(rateList);

		// THEN
		// @formatter:off
		then(t.rate("zero"))
			.as("Rate shortcut returns instance")
			.isSameAs(rateList.get(0))
			;
		then(t.rate("does not exist"))
			.as("Rate shortcut for non-existing ID returns null")
			.isNull()
			;
		then(t.rate())
			.as("Rate shortcut returns first instance")
			.isSameAs(rateList.get(0))
			;
		// @formatter:on
	}

	@Test
	public void amountShortcut() {
		// GIVEN
		List<Rate> rateList = asList(new SimpleTariffRate("zero", BigDecimal.ZERO),
				new SimpleTariffRate("one", BigDecimal.ONE),
				new SimpleTariffRate("ten", BigDecimal.TEN));

		// WHEN
		SimpleTariff t = new SimpleTariff(rateList);

		// THEN
		// @formatter:off
		then(t.amount("zero"))
			.as("Amount shortcut returns instance")
			.isSameAs(rateList.get(0).getAmount())
			;
		then(t.amount("does not exist"))
			.as("Amount shortcut for non-existing ID returns null")
			.isNull()
			;
		then(t.amount())
			.as("Amount for first rate returned")
			.isSameAs(rateList.get(0).getAmount())
			;
		// @formatter:on
	}

	@Test
	public void emptyTariffShortcuts() {
		// WHEN
		SimpleTariff t = new SimpleTariff(List.of());

		// THEN
		// @formatter:off
		then(t.rate("does not exist"))
			.as("Rate shortcut for non-existing ID returns null")
			.isNull()
			;
		then(t.rate())
			.as("Rate for first returns null")
			.isNull()
			;

		then(t.amount("does not exist"))
			.as("Amount shortcut for non-existing ID returns null")
			.isNull()
			;
		then(t.amount())
			.as("Amount for first rate rerturns null")
			.isNull()
			;
		// @formatter:on
	}

}
