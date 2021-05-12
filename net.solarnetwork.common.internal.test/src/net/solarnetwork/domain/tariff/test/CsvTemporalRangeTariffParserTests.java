/* ==================================================================
 * CsvTemporalRangeTariffParserTests.java - 12/05/2021 9:36:03 PM
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

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;
import org.junit.Test;
import net.solarnetwork.domain.tariff.CsvTemporalRangeTariffParser;
import net.solarnetwork.domain.tariff.SimpleTariffRate;
import net.solarnetwork.domain.tariff.Tariff.Rate;
import net.solarnetwork.domain.tariff.TemporalRangesTariff;

/**
 * Test cases for the {@link CsvTemporalRangeTariffParser} class.
 * 
 * @author matt
 * @version 1.0
 */
public class CsvTemporalRangeTariffParserTests {

	/**
	 * Create a tariff where the rates names are given "column" names like in a
	 * spreadsheet, starting with "E".
	 * 
	 * @param locale
	 *        the locale, or {@literal null} to use the system default
	 * @param data
	 *        the month, day, day of week, time, rate1[, rateN...]
	 * @return the tariff
	 */
	public static TemporalRangesTariff spreadsheetColumnRates(Locale locale, String... data) {
		locale = (locale != null ? locale : Locale.getDefault());
		List<Rate> rates = IntStream.range(4, data.length).mapToObj(
				i -> new SimpleTariffRate(Character.toString((char) ('A' + i)), new BigDecimal(data[i])))
				.collect(toList());
		return new TemporalRangesTariff(data[0], data[1], data[2], data[3], rates, locale);
	}

	/**
	 * Create a tariff where the rates names are given "column" names like in a
	 * spreadsheet, starting with "E".
	 * 
	 * @param data
	 *        the month, day, day of week, time, rate1[, rateN...]
	 * @return the tariff
	 */
	public static TemporalRangesTariff spreadsheetColumnRates(String... data) {
		return spreadsheetColumnRates(null, data);
	}

	public static void assertTemporalRangeTariff(String msg, TemporalRangesTariff actual,
			TemporalRangesTariff expected) {
		assertThat(msg + " tariff exists", actual, notNullValue());
		assertThat(msg + " month range", actual.getMonthRange(), equalTo(expected.getMonthRange()));
		assertThat(msg + " day of month range", actual.getDayOfMonthRange(),
				equalTo(expected.getDayOfMonthRange()));
		assertThat(msg + " day of week range", actual.getDayOfWeekRange(),
				equalTo(expected.getDayOfWeekRange()));
		assertThat(msg + " minute of day range", actual.getMinuteOfDayRange(),
				equalTo(expected.getMinuteOfDayRange()));
		assertThat(msg + " has no rates", actual.getRates(), equalTo(expected.getRates()));
	}

	public static void assertTemporalRangeTariff(String msg, TemporalRangesTariff actual,
			String... data) {
		assertTemporalRangeTariff(msg, actual, spreadsheetColumnRates(data));
	}

	@Test
	public void parse_example() throws IOException {
		// GIVEN
		CsvTemporalRangeTariffParser p = new CsvTemporalRangeTariffParser();

		// WHEN
		try (InputStreamReader r = new InputStreamReader(
				getClass().getResourceAsStream("test-tariffs.csv"), "UTF-8")) {
			List<TemporalRangesTariff> tariffs = p.parseTariffs(r);

			assertThat("Tariffs parsed", tariffs, hasSize(4));
			assertTemporalRangeTariff("Row 1", tariffs.get(0), "January-December", null, "Mon-Fri",
					"0-8", "10.48");
			assertTemporalRangeTariff("Row 2", tariffs.get(1), "January-December", null, "Mon-Fri",
					"8-24", "11.00");
			assertTemporalRangeTariff("Row 3", tariffs.get(2), "January-December", null, "Sat-Sun",
					"0-8", "9.19");
			assertTemporalRangeTariff("Row 4", tariffs.get(3), "January-December", null, "Sat-Sun",
					"8-24", "11.21");
		}
	}

}
