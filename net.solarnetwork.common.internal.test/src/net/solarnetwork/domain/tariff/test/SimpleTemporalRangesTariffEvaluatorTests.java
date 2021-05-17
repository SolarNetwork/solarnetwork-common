/* ==================================================================
 * SimpleTemporalRangesTariffEvaluatorTests.java - 12/05/2021 6:06:18 PM
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
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;
import org.junit.Test;
import net.solarnetwork.domain.tariff.SimpleTariffRate;
import net.solarnetwork.domain.tariff.SimpleTemporalRangesTariffEvaluator;
import net.solarnetwork.domain.tariff.TemporalRangesTariff;

/**
 * Test cases for the {@link SimpleTemporalRangesTariffEvaluator} class.
 * 
 * @author matt
 * @version 1.0
 */
public class SimpleTemporalRangesTariffEvaluatorTests {

	private TemporalRangesTariff createTestTariff() {
		SimpleTariffRate r = new SimpleTariffRate("rate", BigDecimal.ONE);
		return new TemporalRangesTariff("Mar-Nov", null, "Mon-Fri", "00:00-20:30", asList(r),
				Locale.getDefault());
	}

	@Test
	public void rule_match() {
		// GIVEN
		SimpleTemporalRangesTariffEvaluator e = new SimpleTemporalRangesTariffEvaluator();
		TemporalRangesTariff t = createTestTariff();
		LocalDateTime date = LocalDateTime.of(2021, 5, 12, 18, 0);

		// WHEN
		boolean result = e.applies(t, date, null);

		// THEN
		assertThat("Rule matches", result, equalTo(true));
	}

	@Test
	public void rule_noMatch_month() {
		// GIVEN
		SimpleTemporalRangesTariffEvaluator e = new SimpleTemporalRangesTariffEvaluator();
		TemporalRangesTariff t = createTestTariff();
		LocalDateTime date = LocalDateTime.of(2021, 1, 12, 18, 0);

		// WHEN
		boolean result = e.applies(t, date, null);

		// THEN
		assertThat("Rule does not match", result, equalTo(false));
	}

	@Test
	public void rule_noMatch_dayOfWeek() {
		// GIVEN
		SimpleTemporalRangesTariffEvaluator e = new SimpleTemporalRangesTariffEvaluator();
		TemporalRangesTariff t = createTestTariff();
		LocalDateTime date = LocalDateTime.of(2021, 5, 9, 18, 0);

		// WHEN
		boolean result = e.applies(t, date, null);

		// THEN
		assertThat("Rule does not match", result, equalTo(false));
	}

	@Test
	public void rule_noMatch_hourOfDay() {
		// GIVEN
		SimpleTemporalRangesTariffEvaluator e = new SimpleTemporalRangesTariffEvaluator();
		TemporalRangesTariff t = createTestTariff();
		LocalDateTime date = LocalDateTime.of(2021, 5, 12, 21, 0);

		// WHEN
		boolean result = e.applies(t, date, null);

		// THEN
		assertThat("Rule does not match", result, equalTo(false));
	}

	@Test
	public void rule_noMatch_minuteOfDay() {
		// GIVEN
		SimpleTemporalRangesTariffEvaluator e = new SimpleTemporalRangesTariffEvaluator();
		TemporalRangesTariff t = createTestTariff();
		LocalDateTime date = LocalDateTime.of(2021, 5, 12, 20, 31);

		// WHEN
		boolean result = e.applies(t, date, null);

		// THEN
		assertThat("Rule does not match", result, equalTo(false));
	}

}
