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
import static org.assertj.core.api.BDDAssertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.util.FileCopyUtils;
import net.solarnetwork.domain.tariff.ChronoFieldsTariff;
import net.solarnetwork.domain.tariff.CsvTemporalRangeTariffParser;
import net.solarnetwork.domain.tariff.SimpleTariffRate;
import net.solarnetwork.domain.tariff.Tariff;
import net.solarnetwork.domain.tariff.Tariff.Rate;
import net.solarnetwork.domain.tariff.TemporalRangeSetsTariff;
import net.solarnetwork.util.StringUtils;

/**
 * Test cases for the {@link CsvTemporalRangeTariffParser} class.
 *
 * @author matt
 * @version 1.2
 */
public class CsvTemporalRangeTariffParserTests {

	private String stringResource(String resource) {
		try {
			return FileCopyUtils.copyToString(
					new InputStreamReader(getClass().getResourceAsStream(resource), "UTF-8"));
		} catch ( Exception e ) {
			throw new RuntimeException(e);
		}
	}

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
	public static ChronoFieldsTariff spreadsheetColumnRates(Locale locale, boolean preserveCase,
			String... data) {
		locale = (locale != null ? locale : Locale.getDefault());

		List<Rate> rates = IntStream.range(4, data.length).mapToObj(i -> {
			String name = Character.toString((char) ('A' + i));
			String id = StringUtils.simpleIdValue(name, preserveCase);
			return new SimpleTariffRate(id, name, new BigDecimal(data[i]));
		}).collect(toList());
		return new TemporalRangeSetsTariff(data[0], data[1], data[2], data[3], rates, locale);
	}

	/**
	 * Create a tariff where the rates names are given "column" names like in a
	 * spreadsheet, starting with "E".
	 *
	 * @param data
	 *        the month, day, day of week, time, rate1[, rateN...]
	 * @return the tariff
	 */
	public static ChronoFieldsTariff spreadsheetColumnRates(String... data) {
		return spreadsheetColumnRates(null, false, data);
	}

	/**
	 * Create a tariff where the rates names are given "column" names like in a
	 * spreadsheet, starting with "E".
	 *
	 * @param data
	 *        the month, day, day of week, time, rate1[, rateN...]
	 * @return the tariff
	 */
	public static ChronoFieldsTariff spreadsheetColumnRates(boolean preserveCase, String... data) {
		return spreadsheetColumnRates(null, preserveCase, data);
	}

	public static void assertTemporalRangeTariff(String msg, ChronoFieldsTariff actual,
			ChronoFieldsTariff expected) {
		assertThat(msg + " tariff exists", actual, notNullValue());
		assertThat(msg + " month range", actual.rangeForChronoField(ChronoField.MONTH_OF_YEAR),
				equalTo(expected.rangeForChronoField(ChronoField.MONTH_OF_YEAR)));
		assertThat(msg + " day of month range", actual.rangeForChronoField(ChronoField.DAY_OF_MONTH),
				equalTo(expected.rangeForChronoField(ChronoField.DAY_OF_MONTH)));
		assertThat(msg + " day of week range", actual.rangeForChronoField(ChronoField.DAY_OF_WEEK),
				equalTo(expected.rangeForChronoField(ChronoField.DAY_OF_WEEK)));
		assertThat(msg + " minute of day range", actual.rangeForChronoField(ChronoField.MINUTE_OF_DAY),
				equalTo(expected.rangeForChronoField(ChronoField.MINUTE_OF_DAY)));
		assertThat(msg + " has rates", actual.getRates(), equalTo(expected.getRates()));
	}

	public static void assertTemporalRangeTariff(String msg, Tariff actual, String... data) {
		assertTemporalRangeTariff(msg, false, actual, data);
	}

	public static void assertTemporalRangeTariff(String msg, boolean preserveCase, Tariff actual,
			String... data) {
		assertThat(msg + " is a ChronoFieldsTariff", actual,
				Matchers.instanceOf(ChronoFieldsTariff.class));
		assertTemporalRangeTariff(msg, (ChronoFieldsTariff) actual,
				spreadsheetColumnRates(preserveCase, data));
	}

	@Test
	public void parse_example() throws IOException {
		// GIVEN
		CsvTemporalRangeTariffParser p = new CsvTemporalRangeTariffParser();

		// WHEN
		try (InputStreamReader r = new InputStreamReader(
				getClass().getResourceAsStream("test-tariffs-01.csv"), "UTF-8")) {
			List<ChronoFieldsTariff> tariffs = p.parseTariffs(r);

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

	@Test
	public void parse_example_preserveCase() throws IOException {
		// GIVEN
		CsvTemporalRangeTariffParser p = new CsvTemporalRangeTariffParser(Locale.getDefault(), true);

		// WHEN
		try (InputStreamReader r = new InputStreamReader(
				getClass().getResourceAsStream("test-tariffs-01.csv"), "UTF-8")) {
			List<ChronoFieldsTariff> tariffs = p.parseTariffs(r);

			assertThat("Tariffs parsed", tariffs, hasSize(4));
			assertTemporalRangeTariff("Row 1", true, tariffs.get(0), "January-December", null, "Mon-Fri",
					"0-8", "10.48");
			assertTemporalRangeTariff("Row 2", true, tariffs.get(1), "January-December", null, "Mon-Fri",
					"8-24", "11.00");
			assertTemporalRangeTariff("Row 3", true, tariffs.get(2), "January-December", null, "Sat-Sun",
					"0-8", "9.19");
			assertTemporalRangeTariff("Row 4", true, tariffs.get(3), "January-December", null, "Sat-Sun",
					"8-24", "11.21");
		}
	}

	@Test
	public void format_hours() throws IOException {
		// GIVEN
		// @formatter:off
		List<ChronoFieldsTariff> tariffs = Arrays.asList(
				spreadsheetColumnRates("January-December", null, "Mon-Fri", "0-8", "10.48", "1.23"),
				spreadsheetColumnRates("January-December", null, "Mon-Fri", "8-24", "11.00", "2.34"),
				spreadsheetColumnRates("January-December", null, "Sat-Sun", "0-8", "9.19", "3.45", "4.56"),
				spreadsheetColumnRates("January-December", null, "Sat-Sun", "8-24", "11.21")
				);
		// @formatter:on

		StringWriter w = new StringWriter(4096);

		// WHEN
		new CsvTemporalRangeTariffParser().formatCsv(tariffs, w);

		// THEN
		String csv = w.toString();
		assertThat("Formatted CSV", csv, equalTo(stringResource("test-tariffs-02.csv")));
	}

	@Test
	public void format_times() throws IOException {
		// GIVEN
		// @formatter:off
		List<ChronoFieldsTariff> tariffs = Arrays.asList(
				spreadsheetColumnRates("January-December", null, "Mon-Fri", "00:00-08:30", "10.48", "1.23"),
				spreadsheetColumnRates("January-December", null, "Mon-Fri", "08:30-24:00", "11.00", "2.34"),
				spreadsheetColumnRates("January-December", null, "Sat-Sun", "00:00-08:30", "9.19", "3.45", "4.56"),
				spreadsheetColumnRates("January-December", null, "Sat-Sun", "08:30-24:00", "11.21")
				);
		// @formatter:on

		StringWriter w = new StringWriter(4096);

		// WHEN
		new CsvTemporalRangeTariffParser().formatCsv(tariffs, w);

		// THEN
		String csv = w.toString();
		assertThat("Formatted CSV", csv, equalTo(stringResource("test-tariffs-03.csv")));
	}

	@Test
	public void parse_wholeHours() throws IOException {
		// GIVEN
		CsvTemporalRangeTariffParser p = new CsvTemporalRangeTariffParser();

		// WHEN
		try (InputStreamReader r = new InputStreamReader(
				getClass().getResourceAsStream("test-tariffs-04.csv"), "UTF-8")) {
			List<ChronoFieldsTariff> tariffs = p.parseTariffs(r);

			assertThat("Tariffs parsed", tariffs, hasSize(4));
			assertTemporalRangeTariff("Row 1", tariffs.get(0), "January-December", null, null, "0-1",
					"10.48");
			assertTemporalRangeTariff("Row 2", tariffs.get(1), "January-December", null, null, "1-2",
					"11.00");
			assertTemporalRangeTariff("Row 3", tariffs.get(2), "January-December", null, null, "22-23",
					"9.19");
			assertTemporalRangeTariff("Row 4", tariffs.get(3), "January-December", null, null, "23-24",
					"11.21");
		}
	}

	@Test
	public void parse_strict_descendingRejected() {
		// GIVEN
		CsvTemporalRangeTariffParser p = new CsvTemporalRangeTariffParser(Locale.ENGLISH, false, true);
		final String csv = "Month,Day,Weekday,Time,E\n,,Mon-Fri,07:00-18:00,1\n,,,22:00-06:00,1\n";

		// WHEN
		Throwable t = catchThrowable(() -> p.parseTariffs(new StringReader(csv)));

		// THEN
		// @formatter:off
		then(t)
			.as("Strict parser rejects a descending range")
			.isInstanceOf(IllegalArgumentException.class)
			.as("Error names the row and range")
			.hasMessage("Error parsing date range value in CSV row 3: Time range [22:00-06:00] is descending")
			;
		// @formatter:on
	}

	@Test
	public void parse_strict_multiRangeDescendingRejected() {
		// GIVEN
		CsvTemporalRangeTariffParser p = new CsvTemporalRangeTariffParser(Locale.ENGLISH, false, true);
		final String csv = "Month,Day,Weekday,Time,E\n,,\"Mon-Wed,Sat-Fri\",,1\n";

		// WHEN
		Throwable t = catchThrowable(() -> p.parseTariffs(new StringReader(csv)));

		// THEN
		// @formatter:off
		then(t)
			.as("Strict parser rejects a descending range")
			.isInstanceOf(IllegalArgumentException.class)
			.as("Error names the row and range")
			.hasMessage("Error parsing date range value in CSV row 2: Range [Sat-Fri] is descending")
			;
		// @formatter:on
	}

	@Test
	public void parse_strict_valid() throws IOException {
		// GIVEN
		CsvTemporalRangeTariffParser p = new CsvTemporalRangeTariffParser(Locale.ENGLISH, false, true);
		final String csv = "Month,Day,Weekday,Time,E\n,,Mon-Fri,07:00-18:00,1\nDec,24-26,,8,1\n";

		// WHEN
		List<ChronoFieldsTariff> tariffs = p.parseTariffs(new StringReader(csv));

		// THEN
		// @formatter:off
		then(p.isStrict())
			.as("Strict mode enabled")
			.isTrue()
			;
		then(tariffs)
			.as("Tariffs parsed")
			.hasSize(2)
			;
		// @formatter:on
		assertTemporalRangeTariff("Row 1", tariffs.get(0), null, null, "Mon-Fri", "07:00-18:00", "1");
		assertTemporalRangeTariff("Row 2 whole hour widened", tariffs.get(1), "December", "24-26", null,
				"8-9", "1");
	}

	@Test
	public void parse_lenient_descendingReordered() throws IOException {
		// GIVEN
		CsvTemporalRangeTariffParser p = new CsvTemporalRangeTariffParser(Locale.ENGLISH, false);
		final String csv = "Month,Day,Weekday,Time,E\n,,Fri-Mon,22:00-06:00,1\n";

		// WHEN
		List<ChronoFieldsTariff> tariffs = p.parseTariffs(new StringReader(csv));

		// THEN
		// @formatter:off
		then(p.isStrict())
			.as("Strict mode disabled")
			.isFalse()
			;
		then(tariffs)
			.as("Tariffs parsed")
			.hasSize(1)
			;
		// @formatter:on
		assertTemporalRangeTariff("Row 1 reordered", tariffs.get(0), null, null, "Mon-Fri",
				"06:00-22:00", "1");
	}

	@Test
	public void parse_strict_oneMinuteGapRejected() {
		// GIVEN
		CsvTemporalRangeTariffParser p = new CsvTemporalRangeTariffParser(Locale.ENGLISH, false, true);
		final String csv = "Month,Day,Weekday,Time,E\n,,,\"06:00-09:00,09:01-12:00\",1\n";

		// WHEN
		Throwable t = catchThrowable(() -> p.parseTariffs(new StringReader(csv)));

		// THEN
		// @formatter:off
		then(t)
			.as("Strict parser rejects ranges one minute apart")
			.isInstanceOf(IllegalArgumentException.class)
			.as("Error names the row and ranges")
			.hasMessage("Error parsing date range value in CSV row 2: Time ranges [06:00-09:00] and [09:01-12:00] are one minute apart and would merge")
			;
		// @formatter:on
	}

}
