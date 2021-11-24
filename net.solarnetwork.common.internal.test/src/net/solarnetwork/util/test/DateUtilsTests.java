/* ==================================================================
 * DateUtilsTests.java - 12/02/2020 7:17:09 am
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

package net.solarnetwork.util.test;

import static java.lang.String.format;
import static java.time.format.TextStyle.FULL;
import static java.time.format.TextStyle.SHORT;
import static java.util.Locale.US;
import static net.solarnetwork.util.IntRange.rangeOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeThat;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import net.solarnetwork.util.DateUtils;
import net.solarnetwork.util.IntRange;

/**
 * Test cases for the {@link DateUtils} class.
 * 
 * @author matt
 * @version 1.2
 */
public class DateUtilsTests {

	@Test
	public void parseIsoDateOptTime_full() {
		LocalDateTime ts = DateUtils.ISO_DATE_OPT_TIME_OPT_MILLIS.parse("2020-02-01T20:12:34.567",
				LocalDateTime::from);
		assertThat(ts, equalTo(
				LocalDateTime.of(2020, 2, 1, 20, 12, 34, (int) TimeUnit.MILLISECONDS.toNanos(567))));
	}

	@Test
	public void parseIsoDateOptTime_noZone() {
		LocalDateTime ts = DateUtils.ISO_DATE_OPT_TIME_OPT_MILLIS.parse("2020-02-01T20:12:34",
				LocalDateTime::from);
		assertThat(ts, equalTo(LocalDateTime.of(2020, 2, 1, 20, 12, 34, 0)));
	}

	@Test
	public void parseIsoDateOptTime_noSeconds() {
		LocalDateTime ts = DateUtils.ISO_DATE_OPT_TIME_OPT_MILLIS.parse("2020-02-01T20:12",
				LocalDateTime::from);
		assertThat(ts, equalTo(LocalDateTime.of(2020, 2, 1, 20, 12, 0, 0)));
	}

	@Test
	public void parseIsoDateOptTimeAlt_full() {
		ZonedDateTime ts = DateUtils.ISO_DATE_OPT_TIME_ALT.parse("2020-02-01 20:12:34+12:00",
				ZonedDateTime::from);
		assertThat(ts, equalTo(ZonedDateTime.of(2020, 2, 1, 20, 12, 34, 0, ZoneOffset.ofHours(12))));
	}

	@Test
	public void parseIsoDateOptTimeAlt_noZone() {
		LocalDateTime ts = DateUtils.ISO_DATE_OPT_TIME_ALT.parse("2020-02-01 20:12:34",
				LocalDateTime::from);
		assertThat(ts, equalTo(LocalDateTime.of(2020, 2, 1, 20, 12, 34, 0)));
	}

	@Test
	public void parseIsoDateOptTimeAlt_noSeconds() {
		LocalDateTime ts = DateUtils.ISO_DATE_OPT_TIME_ALT.parse("2020-02-01 20:12",
				LocalDateTime::from);
		assertThat(ts, equalTo(LocalDateTime.of(2020, 2, 1, 20, 12, 0, 0)));
	}

	@Test
	public void parseIsoDateOptTimeAlt_dateWithZone() {
		// parsing a date+zone takes a slightly different approach
		TemporalAccessor ta = DateUtils.ISO_DATE_OPT_TIME_ALT.parse("2020-02-01+12:00");
		ZonedDateTime ts = LocalDate.from(ta).atStartOfDay(ZoneId.from(ta));
		assertThat(ts, equalTo(ZonedDateTime.of(2020, 2, 1, 0, 0, 0, 0, ZoneOffset.ofHours(12))));
	}

	@Test
	public void parseIsoDateOptTimeAlt_date() {
		LocalDate ts = DateUtils.ISO_DATE_OPT_TIME_ALT.parse("2020-02-01", LocalDate::from);
		assertThat(ts, equalTo(LocalDate.of(2020, 2, 1)));
	}

	final static double JAVA_VERS = Double.parseDouble(System.getProperty("java.specification.version"));

	@Test
	public void parseIsoDateOptTimeAlt_utc_full_java11() {
		assumeThat("Behavior in Java 11", JAVA_VERS, greaterThanOrEqualTo(11.0));
		// Behaviour changes from Java 8 -> 11 from bug JDK-8066982
		// https://bugs.java.com/bugdatabase/view_bug.do?bug_id=JDK-8066982
		ZonedDateTime ts = DateUtils.ISO_DATE_OPT_TIME_ALT_UTC.parse("2020-02-01 20:12:34+12:00",
				ZonedDateTime::from);
		assertThat(ts, equalTo(ZonedDateTime.of(2020, 2, 1, 20, 12, 34, 0, ZoneOffset.ofHours(12))
				.withZoneSameInstant(ZoneOffset.UTC)));

	}

	@Test
	public void parseIsoDateOptTimeAlt_utc_full_java8() {
		assumeThat("Behavior in Java 8", JAVA_VERS, lessThan(11.0));
		// Behaviour changes from Java 8 -> 11 from bug JDK-8066982
		// https://bugs.java.com/bugdatabase/view_bug.do?bug_id=JDK-8066982
		ZonedDateTime ts = DateUtils.ISO_DATE_OPT_TIME_ALT_UTC.parse("2020-02-01 20:12:34+12:00",
				ZonedDateTime::from);
		assertThat(ts, equalTo(ZonedDateTime.of(2020, 2, 1, 20, 12, 34, 0, ZoneOffset.UTC)));
	}

	@Test
	public void parseIsoDateOptTimeAlt_utc_noZone() {
		ZonedDateTime ts = DateUtils.ISO_DATE_OPT_TIME_ALT_UTC.parse("2020-02-01 20:12:34",
				ZonedDateTime::from);
		assertThat(ts, equalTo(ZonedDateTime.of(2020, 2, 1, 20, 12, 34, 0, ZoneOffset.UTC)));
	}

	@Test
	public void parseIsoDateOptTimeAlt_utc_date() {
		TemporalAccessor ta = DateUtils.ISO_DATE_OPT_TIME_ALT_UTC.parse("2020-02-01");
		ZonedDateTime ts = LocalDate.from(ta).atStartOfDay(ZoneId.from(ta));
		assertThat(ts, equalTo(ZonedDateTime.of(2020, 2, 1, 0, 0, 0, 0, ZoneOffset.UTC)));
	}

	@Test
	public void parseIsoDateOptTimeAlt_local_noZone() {
		ZonedDateTime ts = DateUtils.ISO_DATE_OPT_TIME_ALT_LOCAL.parse("2020-02-01 20:12:34",
				ZonedDateTime::from);
		assertThat(ts, equalTo(ZonedDateTime.of(2020, 2, 1, 20, 12, 34, 0, ZoneId.systemDefault())));
	}

	@Test
	public void parseIsoDateOptTimeAlt_local_date() {
		TemporalAccessor ta = DateUtils.ISO_DATE_OPT_TIME_ALT_LOCAL.parse("2020-02-01");
		ZonedDateTime ts = LocalDate.from(ta).atStartOfDay(ZoneId.from(ta));
		assertThat(ts, equalTo(ZonedDateTime.of(2020, 2, 1, 0, 0, 0, 0, ZoneId.systemDefault())));
	}

	@Test
	public void parseIsoDateOptTimeAlt_best_full() {
		TemporalAccessor ta = DateUtils.ISO_DATE_OPT_TIME_ALT.parseBest("2020-02-01 20:12:34+12:00",
				ZonedDateTime::from, LocalDateTime::from, LocalDate::from);
		assertThat("ZonedDateTime parsed", ta,
				equalTo(ZonedDateTime.of(2020, 2, 1, 20, 12, 34, 0, ZoneOffset.ofHours(12))));
	}

	@Test
	public void parseIsoDateOptTimeAlt_best_noZone() {
		TemporalAccessor ta = DateUtils.ISO_DATE_OPT_TIME_ALT.parseBest("2020-02-01 12:34",
				ZonedDateTime::from, LocalDateTime::from, LocalDate::from);
		assertThat("LocalDateTime parsed", ta, equalTo(LocalDateTime.of(2020, 2, 1, 12, 34)));
	}

	@Test
	public void parseIsoDateOptTimeAlt_best_date() {
		TemporalAccessor ta = DateUtils.ISO_DATE_OPT_TIME_ALT.parseBest("2020-02-01",
				ZonedDateTime::from, LocalDateTime::from, LocalDate::from);
		assertThat("LocalDate parsed", ta, equalTo(LocalDate.of(2020, 2, 1)));
	}

	@Test
	public void parseIsoAltTimestamp_full() {
		ZonedDateTime ts = DateUtils.parseIsoAltTimestamp("2020-02-01 20:12:34+12:00", ZoneOffset.UTC);
		assertThat(ts, equalTo(ZonedDateTime.of(2020, 2, 1, 20, 12, 34, 0, ZoneOffset.ofHours(12))));
	}

	@Test
	public void parseIsoAltTimestamp_noZone() {
		ZonedDateTime ts = DateUtils.parseIsoAltTimestamp("2020-02-01 20:12:34", ZoneOffset.UTC);
		assertThat(ts, equalTo(ZonedDateTime.of(2020, 2, 1, 20, 12, 34, 0, ZoneOffset.UTC)));
	}

	@Test
	public void parseIsoAltTimestamp_dateWithZone() {
		ZonedDateTime ts = DateUtils.parseIsoAltTimestamp("2020-02-01+12:00", ZoneOffset.UTC);
		assertThat(ts, equalTo(ZonedDateTime.of(2020, 2, 1, 0, 0, 0, 0, ZoneOffset.ofHours(12))));
	}

	@Test
	public void parseIsoAltTimestamp_date() {
		ZonedDateTime ts = DateUtils.parseIsoAltTimestamp("2020-02-01", ZoneOffset.UTC);
		assertThat(ts, equalTo(ZonedDateTime.of(2020, 2, 1, 0, 0, 0, 0, ZoneOffset.UTC)));
	}

	@Test
	public void parseDateRange_missingStart() {
		String[] inputs = new String[] { " - 3", "-3" };
		for ( int i = 0, len = inputs.length; i < len; i++ ) {
			try {
				DateUtils.parseRange(ChronoField.MONTH_OF_YEAR, inputs[i], null);
				fail(format("Missing start %d [%s] should fail to parse", i + 1, inputs[i]));
			} catch ( DateTimeException e ) {
				// expected
			}
		}
	}

	@Test
	public void parseDateRange_missingEnd() {
		String[] inputs = new String[] { "3 -", "3-" };
		for ( int i = 0, len = inputs.length; i < len; i++ ) {
			try {
				DateUtils.parseRange(ChronoField.MONTH_OF_YEAR, inputs[i], null);
				fail(format("Missing end %d [%s] should fail to parse", i + 1, inputs[i]));
			} catch ( DateTimeException e ) {
				// expected
			}
		}
	}

	@Test
	public void parseMonthRange() {
		String[] inputs = new String[] { "March-November", "Mar-Nov", "03-11", "3-11", " March  - Nov ",
				"Nov-Mar" };
		for ( int i = 0, len = inputs.length; i < len; i++ ) {
			IntRange r = DateUtils.parseMonthRange(inputs[i], null);
			assertThat(format("Month range %d [%s] parsed", i + 1, inputs[i]), r,
					equalTo(rangeOf(3, 11)));
		}
	}

	@Test
	public void parseMonthRange_singleton() {
		String[] inputs = new String[] { "March", "Mar", "03", "3", " March  ", "Mar-Mar" };
		for ( int i = 0, len = inputs.length; i < len; i++ ) {
			IntRange r = DateUtils.parseMonthRange(inputs[i], null);
			assertThat(format("Month singleton %d [%s] parsed", i + 1, inputs[i]), r,
					equalTo(rangeOf(3)));
		}
	}

	@Test(expected = DateTimeException.class)
	public void parseMonthRange_invalid() {
		DateUtils.parseMonthRange("March - Howdy", null);
	}

	@Test
	public void parseDayOfMonthRange() {
		String[] inputs = new String[] { "7-21", "7 - 21", "07-21", "07 - 21", " 7 - 21 ", " 07  - 21 ",
				"21-7" };
		for ( int i = 0, len = inputs.length; i < len; i++ ) {
			IntRange r = DateUtils.parseDayOfMonthRange(inputs[i], null);
			assertThat(format("DayOfMonth range %d [%s] parsed", i + 1, inputs[i]), r,
					equalTo(rangeOf(7, 21)));
		}
	}

	@Test
	public void parseDayOfMonthRange_singleton() {
		String[] inputs = new String[] { "07", "7", " 7", " 07", " 7 ", " 07 ", "07-07" };
		for ( int i = 0, len = inputs.length; i < len; i++ ) {
			IntRange r = DateUtils.parseDayOfMonthRange(inputs[i], null);
			assertThat(format("DayOfMonth singleton %d [%s] parsed", i + 1, inputs[i]), r,
					equalTo(rangeOf(7)));
		}
	}

	@Test(expected = DateTimeException.class)
	public void parseDayOfMonthRange_invalid() {
		DateUtils.parseDayOfMonthRange("1-100", null);
	}

	@Test
	public void parseDayOfWeekRange() {
		String[] inputs = new String[] { "Tuesday - Saturday", "Tue - Sat", "2-6", "02 - 06",
				" Tue - 6 ", " Tue  - Sat ", "Sat-Tue" };
		for ( int i = 0, len = inputs.length; i < len; i++ ) {
			IntRange r = DateUtils.parseDayOfWeekRange(inputs[i], null);
			assertThat(format("DayOfWeek range %d [%s] parsed", i + 1, inputs[i]), r,
					equalTo(rangeOf(2, 6)));
		}
	}

	@Test
	public void parseDayOfWeekRange_singleton() {
		String[] inputs = new String[] { "Tuesday", "Tue", "02", "2", " 2 ", " 02 ", "2 ", "Tue-Tue" };
		for ( int i = 0, len = inputs.length; i < len; i++ ) {
			IntRange r = DateUtils.parseDayOfWeekRange(inputs[i], null);
			assertThat(format("DayOfWeek singleton %d [%s] parsed", i + 1, inputs[i]), r,
					equalTo(rangeOf(2)));
		}
	}

	@Test(expected = DateTimeException.class)
	public void parseDayOfWeekRange_invalid() {
		DateUtils.parseDayOfWeekRange("1-10", null);
	}

	@Test
	public void parseMinuteOfDayRange() {
		String[] inputs = new String[] { "00 - 24", "0 - 24", "0-24", " 0 - 24 ", "0 - 24 ", "24-0" };
		for ( int i = 0, len = inputs.length; i < len; i++ ) {
			IntRange r = DateUtils.parseMinuteOfDayRange(inputs[i], null, false);
			assertThat(format("MinuteOfDay range %d [%s] parsed", i + 1, inputs[i]), r,
					equalTo(rangeOf(0, 24 * 60)));
		}
		inputs = new String[] { "0-1", "4-8", "8-24", " 2 - 22 ", "23-0" };
		IntRange[] expected = new IntRange[] { rangeOf(0, 1 * 60), rangeOf(4 * 60, 8 * 60),
				rangeOf(8 * 60, 24 * 60), rangeOf(2 * 60, 22 * 60), rangeOf(0 * 60, 23 * 60) };
		for ( int i = 0, len = inputs.length; i < len; i++ ) {
			IntRange r = DateUtils.parseMinuteOfDayRange(inputs[i], null, false);
			assertThat(format("MinuteOfDay range %d [%s] parsed", i + 1, inputs[i]), r,
					equalTo(expected[i]));
		}
	}

	@Test
	public void parseMinuteOfDayRange_singleton() {
		String[] inputs = new String[] { "24", " 24 ", "24-24" };
		for ( int i = 0, len = inputs.length; i < len; i++ ) {
			IntRange r = DateUtils.parseMinuteOfDayRange(inputs[i], null, false);
			assertThat(format("MinuteOfDay singleton %d [%s] parsed", i + 1, inputs[i]), r,
					equalTo(rangeOf(24 * 60)));
		}
	}

	@Test(expected = DateTimeException.class)
	public void parseMinuteOfDayRange_invalid() {
		DateUtils.parseMinuteOfDayRange("1 - 25", null);
	}

	@Test
	public void parseMinuteOfDayRange_fix24() {
		String[] inputs = new String[] { "00 - 24", "0 - 24", "0-24", " 0 - 24 ", "0 - 24 ", "24-0" };
		for ( int i = 0, len = inputs.length; i < len; i++ ) {
			IntRange r = DateUtils.parseMinuteOfDayRange(inputs[i], null, true);
			assertThat(format("MinuteOfDay range %d [%s] parsed", i + 1, inputs[i]), r,
					equalTo(rangeOf(0, 23 * 60 + 59)));
		}
		inputs = new String[] { "0-1", "4-8", "8-24", " 2 - 22 ", "23-0" };
		IntRange[] expected = new IntRange[] { rangeOf(0, 1 * 60), rangeOf(4 * 60, 8 * 60),
				rangeOf(8 * 60, 23 * 60 + 59), rangeOf(2 * 60, 22 * 60), rangeOf(0, 23 * 60) };
		for ( int i = 0, len = inputs.length; i < len; i++ ) {
			IntRange r = DateUtils.parseMinuteOfDayRange(inputs[i], null, true);
			assertThat(format("MinuteOfDay range %d [%s] parsed", i + 1, inputs[i]), r,
					equalTo(expected[i]));
		}
	}

	@Test
	public void parseMinuteOfDayRange_fix24_singleton() {
		String[] inputs = new String[] { "24", " 24 ", "24-24" };
		for ( int i = 0, len = inputs.length; i < len; i++ ) {
			IntRange r = DateUtils.parseMinuteOfDayRange(inputs[i], null, true);
			assertThat(format("MinuteOfDay singleton %d [%s] parsed", i + 1, inputs[i]), r,
					equalTo(rangeOf(23 * 60 + 59)));
		}
	}

	@Test
	public void parseMinuteOfDayRange_iso() {
		String[] inputs = new String[] { "00:30-24:00", "00:30 - 24:00", "00:30-24:00",
				"24:00 - 00:30" };
		for ( int i = 0, len = inputs.length; i < len; i++ ) {
			IntRange r = DateUtils.parseMinuteOfDayRange(inputs[i], null, false);
			assertThat(format("MinuteOfDay range %d [%s] parsed", i + 1, inputs[i]), r,
					equalTo(rangeOf(30, 1440)));
		}
		inputs = new String[] { "0-1", "4-8", "8-24", " 2 - 22 ", "23-0" };
		IntRange[] expected = new IntRange[] { rangeOf(0, 60), rangeOf(4 * 60, 8 * 60),
				rangeOf(8 * 60, 24 * 60), rangeOf(2 * 60, 22 * 60), rangeOf(0, 23 * 60) };
		for ( int i = 0, len = inputs.length; i < len; i++ ) {
			IntRange r = DateUtils.parseMinuteOfDayRange(inputs[i], null, false);
			assertThat(format("MinuteOfDay range %d [%s] parsed", i + 1, inputs[i]), r,
					equalTo(expected[i]));
		}
	}

	@Test
	public void parseMinuteOfDayRange_singleton_iso() {
		String[] inputs = new String[] { "24:00", " 24:00 ", "24:00-24:00" };
		for ( int i = 0, len = inputs.length; i < len; i++ ) {
			IntRange r = DateUtils.parseMinuteOfDayRange(inputs[i], null, false);
			assertThat(format("MinuteOfDay singleton %d [%s] parsed", i + 1, inputs[i]), r,
					equalTo(rangeOf(1440)));
		}
	}

	@Test(expected = DateTimeException.class)
	public void parseMinuteOfDayRange_invalid_iso() {
		DateUtils.parseMinuteOfDayRange("01:00 - 25:00", null);
	}

	@Test
	public void parseMinuteOfDayRange_iso_fix24() {
		String[] inputs = new String[] { "00:30 - 24:00", "00:30 - 24:00", "0:30-24:00",
				" 00:30 - 24:00 ", "00:30 - 24:00 ", "24:00-00:30" };
		for ( int i = 0, len = inputs.length; i < len; i++ ) {
			IntRange r = DateUtils.parseMinuteOfDayRange(inputs[i], null, true);
			assertThat(format("MinuteOfDay range %d [%s] parsed", i + 1, inputs[i]), r,
					equalTo(rangeOf(30, 1439)));
		}
		inputs = new String[] { "02:40-04:20", "04:04-08:08", "08:30-24:00", " 02:20 - 22:22 ",
				"23:30-00:10" };
		IntRange[] expected = new IntRange[] { rangeOf(2 * 60 + 40, 4 * 60 + 20),
				rangeOf(4 * 60 + 4, 8 * 60 + 8), rangeOf(8 * 60 + 30, 23 * 60 + 59),
				rangeOf(2 * 60 + 20, 22 * 60 + 22), rangeOf(10, 23 * 60 + 30) };
		for ( int i = 0, len = inputs.length; i < len; i++ ) {
			IntRange r = DateUtils.parseMinuteOfDayRange(inputs[i], null, true);
			assertThat(format("MinuteOfDay range %d [%s] parsed", i + 1, inputs[i]), r,
					equalTo(expected[i]));
		}
	}

	@Test
	public void parseMinuteOfDayRange_iso_fix24_singleton() {
		String[] inputs = new String[] { "24:00", " 24:00 ", "24:00-24:00" };
		for ( int i = 0, len = inputs.length; i < len; i++ ) {
			IntRange r = DateUtils.parseMinuteOfDayRange(inputs[i], null, true);
			assertThat(format("MinuteOfDay singleton %d [%s] parsed", i + 1, inputs[i]), r,
					equalTo(rangeOf(1439)));
		}
	}

	@Test
	public void parseIsoDateOptTimeOptMillisAlt_localDateTime_withMillis() {
		LocalDateTime ts = DateUtils.ISO_DATE_OPT_TIME_OPT_MILLIS_ALT.parse("2020-02-01 20:12:34.567",
				LocalDateTime::from);
		assertThat(ts, equalTo(
				LocalDateTime.of(2020, 2, 1, 20, 12, 34, (int) TimeUnit.MILLISECONDS.toNanos(567))));
	}

	@Test
	public void parseIsoDateOptTimeOptMillisAlt_localDateTime_noMillis() {
		LocalDateTime ts = DateUtils.ISO_DATE_OPT_TIME_OPT_MILLIS_ALT.parse("2020-02-01 20:12:34",
				LocalDateTime::from);
		assertThat(ts, equalTo(LocalDateTime.of(2020, 2, 1, 20, 12, 34, 0)));
	}

	@Test
	public void parseIsoDateOptTimeOptMillisAlt_localDateTime_noSeconds() {
		LocalDateTime ts = DateUtils.ISO_DATE_OPT_TIME_OPT_MILLIS_ALT.parse("2020-02-01 20:12",
				LocalDateTime::from);
		assertThat(ts, equalTo(LocalDateTime.of(2020, 2, 1, 20, 12, 0, 0)));
	}

	@Test
	public void parseIsoDateOptTimeOptMillisAlt_localDateTime_noTime() {
		LocalDateTime ts = DateUtils.ISO_DATE_OPT_TIME_OPT_MILLIS_ALT.parse("2020-02-01",
				LocalDateTime::from);
		assertThat(ts, equalTo(LocalDateTime.of(2020, 2, 1, 0, 0, 0, 0)));
	}

	@Test
	public void formatMonthRange() {
		String range = DateUtils.formatRange(ChronoField.MONTH_OF_YEAR, rangeOf(2, 11), US, SHORT);
		assertThat("Formatted range", range, equalTo("Feb-Nov"));
	}

	@Test(expected = DateTimeException.class)
	public void formatMonthRange_invalid() {
		DateUtils.formatRange(ChronoField.MONTH_OF_YEAR, rangeOf(2, 15), US, SHORT);
	}

	@Test
	public void formatDayOfMonthRange() {
		String range = DateUtils.formatRange(ChronoField.DAY_OF_MONTH, rangeOf(2, 11), US, SHORT);
		assertThat("Formatted range", range, equalTo("2-11"));
	}

	@Test(expected = DateTimeException.class)
	public void formatDayOfMonthRange_invalid() {
		DateUtils.formatRange(ChronoField.DAY_OF_MONTH, rangeOf(2, 55), US, SHORT);
	}

	@Test
	public void formatDayOfWeekRange() {
		String range = DateUtils.formatRange(ChronoField.DAY_OF_WEEK, rangeOf(2, 6), US, SHORT);
		assertThat("Formatted range", range, equalTo("Tue-Sat"));
	}

	@Test(expected = DateTimeException.class)
	public void formatDayOfWeekRange_invalid() {
		DateUtils.formatRange(ChronoField.DAY_OF_WEEK, rangeOf(2, 55), US, SHORT);
	}

	@Test
	public void formatMinuteOfDayRange_hours() {
		String range = DateUtils.formatRange(ChronoField.MINUTE_OF_DAY, rangeOf(2 * 60, 18 * 60), US,
				SHORT);
		assertThat("Formatted range", range, equalTo("2-18"));
	}

	@Test
	public void formatMinuteOfDayRange_hours_full() {
		String range = DateUtils.formatRange(ChronoField.MINUTE_OF_DAY, rangeOf(2 * 60, 18 * 60), US,
				FULL);
		assertThat("Formatted range", range, equalTo("02:00-18:00"));
	}

	@Test
	public void formatMinuteOfDayRange_minutes() {
		String range = DateUtils.formatRange(ChronoField.MINUTE_OF_DAY, rangeOf(30, 18 * 60), US, SHORT);
		assertThat("Formatted range", range, equalTo("00:30-18:00"));
	}

	@Test(expected = DateTimeException.class)
	public void formatMinuteOfDayRange_invalid() {
		DateUtils.formatRange(ChronoField.MINUTE_OF_DAY, rangeOf(0, 99 * 60), US, SHORT);
	}

	@Test
	public void formatHoursMinutesSeconds_seconds() {
		String s = DateUtils.formatHoursMinutesSeconds(TimeUnit.SECONDS.toMillis(34));
		assertThat("Seconds formatted", s, is("00:34"));
	}

	@Test
	public void formatHoursMinutesSeconds_60seconds() {
		String s = DateUtils.formatHoursMinutesSeconds(TimeUnit.SECONDS.toMillis(60));
		assertThat("60 seconds formatted", s, is("01:00"));
	}

	@Test
	public void formatHoursMinutesSeconds_minutes() {
		String s = DateUtils.formatHoursMinutesSeconds(
				TimeUnit.MINUTES.toMillis(12) + TimeUnit.SECONDS.toMillis(34));
		assertThat("Minutes formatted", s, is("12:34"));
	}

	@Test
	public void formatHoursMinutesSeconds_60minutes() {
		String s = DateUtils.formatHoursMinutesSeconds(TimeUnit.MINUTES.toMillis(60));
		assertThat("60 minutes formatted", s, is("01:00:00"));
	}

	@Test
	public void formatHoursMinutesSeconds_hours() {
		String s = DateUtils.formatHoursMinutesSeconds(TimeUnit.HOURS.toMillis(8)
				+ TimeUnit.MINUTES.toMillis(12) + TimeUnit.SECONDS.toMillis(34));
		assertThat("Hours formatted", s, is("08:12:34"));
	}

	@Test
	public void formatHoursMinutesSeconds_24hours() {
		String s = DateUtils.formatHoursMinutesSeconds(TimeUnit.HOURS.toMillis(24));
		assertThat("24 hours formatted", s, is("1d 00:00:00"));
	}

	@Test
	public void formatHoursMinutesSeconds_days() {
		String s = DateUtils
				.formatHoursMinutesSeconds(TimeUnit.DAYS.toMillis(4) + TimeUnit.HOURS.toMillis(8)
						+ TimeUnit.MINUTES.toMillis(12) + TimeUnit.SECONDS.toMillis(34));
		assertThat("Hours formatted", s, is("4d 08:12:34"));
	}

	@Test
	public void parseLocalTime_hhmm() {
		LocalTime d = DateUtils.parseLocalTime("09:30");
		assertThat("Time parsed", d, is(LocalTime.of(9, 30)));
	}

	@Test
	public void formatLocalTime_hhmm() {
		String s = DateUtils.format(LocalTime.of(9, 30));
		assertThat("Time formatted", s, is("09:30"));
	}

	@Test
	public void parseLocalTime_24hhmm() {
		LocalTime d = DateUtils.parseLocalTime("19:30");
		assertThat("Time parsed", d, is(LocalTime.of(19, 30)));
	}

	@Test
	public void formatLocalTime_24hhmm() {
		String s = DateUtils.format(LocalTime.of(19, 30));
		assertThat("Time formatted", s, is("19:30"));
	}

	@Test
	public void parseLocalDate() {
		LocalDate d = DateUtils.parseLocalDate("2021-08-26");
		assertThat("Date parsed", d, is(LocalDate.of(2021, 8, 26)));
	}

	@Test
	public void formatLocalDate() {
		String s = DateUtils.format(LocalDate.of(2021, 8, 26));
		assertThat("Time formatted", s, is("2021-08-26"));
	}

}
