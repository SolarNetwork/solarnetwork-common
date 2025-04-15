/* ==================================================================
 * DatumDateFunctionsTests.java - 6/08/2024 2:22:52 pm
 *
 * Copyright 2024 SolarNetwork.net Dev Team
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

package net.solarnetwork.domain.datum.test;

import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import net.solarnetwork.domain.datum.DatumDateFunctions;
import net.solarnetwork.util.NumberUtils;

/**
 * Test cases for the {@link DatumDateFunctions} interface.
 *
 * @author matt
 * @version 1.4
 */
public class DatumDateFunctionsTests implements DatumDateFunctions {

	@Test
	public void createDate() {
		assertThat("Date created", date(2024, 8, 6), is(equalTo(LocalDate.of(2024, 8, 6))));
	}

	@Test(expected = IllegalArgumentException.class)
	public void createDate_invalidDayOfMonth() {
		date(2024, 2, 31);
	}

	@Test
	public void createDateTime() {
		assertThat("Date created", date(2024, 8, 6, 13, 14),
				is(equalTo(LocalDateTime.of(2024, 8, 6, 13, 14))));
	}

	@Test(expected = IllegalArgumentException.class)
	public void createDateTime_invalidHourOfDay() {
		date(2024, 2, 31, 99, 0);
	}

	@Test
	public void createDateTimeWithSeconds() {
		assertThat("Date created", date(2024, 8, 6, 13, 14, 55),
				is(equalTo(LocalDateTime.of(2024, 8, 6, 13, 14, 55))));
	}

	@Test(expected = IllegalArgumentException.class)
	public void createDateTime_invalidSecondsOfMinute() {
		date(2024, 2, 31, 0, 0, 99);
	}

	@Test
	public void zonedDate() {
		// GIVEN
		LocalDate d = LocalDate.of(2024, 8, 6);

		// WHEN
		ZonedDateTime zd = dateTz(d);

		// THEN
		assertThat("Zoned date created as start-of-day from local date in system time zone", zd,
				is(equalTo(d.atStartOfDay(ZoneId.systemDefault()))));
	}

	@Test
	public void zonedDate_zone() {
		// GIVEN
		LocalDate d = LocalDate.of(2024, 8, 6);

		// WHEN
		String zoneId = "Pacific/Honolulu";
		ZonedDateTime zd = dateTz(d, zoneId);

		// THEN
		assertThat("Zoned date created as start-of-day from local date in given time zone", zd,
				is(equalTo(d.atStartOfDay(ZoneId.of(zoneId)))));
	}

	@Test(expected = IllegalArgumentException.class)
	public void zonedDate_invalidZone() {
		// GIVEN
		LocalDate d = LocalDate.of(2024, 8, 6);

		// WHEN
		dateTz(d, "Not/Azone");
	}

	@Test
	public void zonedDateTime() {
		// GIVEN
		LocalDateTime d = LocalDateTime.of(2024, 8, 6, 14, 15);

		// WHEN
		ZonedDateTime zd = dateTz(d);

		// THEN
		assertThat("Zoned date created as start-of-day from local date time in system time zone", zd,
				is(equalTo(d.atZone(ZoneId.systemDefault()))));
	}

	@Test
	public void zonedDateTime_zone() {
		// GIVEN
		LocalDateTime d = LocalDateTime.of(2024, 8, 6, 14, 15);

		// WHEN
		String zoneId = "Pacific/Honolulu";
		ZonedDateTime zd = dateTz(d, zoneId);

		// THEN
		assertThat("Zoned date created as start-of-day from local date time in given time zone", zd,
				is(equalTo(d.atZone(ZoneId.of(zoneId)))));
	}

	@Test(expected = IllegalArgumentException.class)
	public void zonedDateTime_invalidZone() {
		// GIVEN
		LocalDateTime d = LocalDateTime.of(2024, 8, 6, 14, 15);

		// WHEN
		dateTz(d, "Not/Azone");
	}

	@Test
	public void datePlus_period() {
		// GIVEN
		LocalDate d = LocalDate.of(2024, 8, 6);

		// WHEN
		String p = "-P2Y";
		Temporal result = datePlus(d, p);

		// THEN
		assertThat("LocalDate returned with period added", result,
				is(equalTo(LocalDate.of(2022, 8, 6))));
	}

	@Test
	public void dateTimePlus_duration() {
		// GIVEN
		LocalDateTime d = LocalDateTime.of(2024, 8, 6, 14, 15);

		// WHEN
		String p = "-PT2H";
		Temporal result = datePlus(d, p);

		// THEN
		assertThat("LocalDateTime returned with period added", result,
				is(equalTo(LocalDateTime.of(2024, 8, 6, 12, 15))));
	}

	@Test(expected = IllegalArgumentException.class)
	public void dateTimePlus_invalid() {
		// GIVEN
		LocalDateTime d = LocalDateTime.of(2024, 8, 6, 14, 15);

		// WHEN
		String p = "foobar";
		datePlus(d, p);
	}

	@Test
	public void datePlus_units() {
		// GIVEN
		LocalDate d = LocalDate.of(2024, 8, 6);

		// WHEN
		Temporal result = datePlus(d, 1, "months");

		// THEN
		assertThat("LocalDate returned with period added", result,
				is(equalTo(LocalDate.of(2024, 9, 6))));
	}

	@Test(expected = IllegalArgumentException.class)
	public void datePlus_units_invalid() {
		// GIVEN
		LocalDate d = LocalDate.of(2024, 8, 6);

		// WHEN
		datePlus(d, 1, "foobar");
	}

	@Test
	public void parseZone() {
		// GIVEN
		final String zoneId = "Pacific/Auckland";

		// WHEN
		ZoneId zone = tz(zoneId);

		// THEN
		assertThat("ZoneId parsed", zone, is(equalTo(ZoneId.of(zoneId))));
	}

	@Test
	public void parseZoneOffset() {
		// GIVEN
		final String zoneId = "-10:00";

		// WHEN
		ZoneId zone = tz(zoneId);

		// THEN
		assertThat("ZoneId parsed", zone, is(equalTo(ZoneId.of(zoneId))));
	}

	@Test
	public void parseZone_null() {
		// WHEN
		ZoneId zone = tz(null);

		// THEN
		assertThat("System time zone returned", zone, is(equalTo(ZoneId.systemDefault())));
	}

	@Test(expected = IllegalArgumentException.class)
	public void parseZone_invalid() {
		// WHEN
		tz("Not/Azone");
	}

	@Test
	public void instant() {
		// GIVEN
		final Instant now = Instant.now();

		// WHEN
		Instant result = timestamp();

		// THEN
		assertThat("Timestamp returned", result, is(notNullValue()));
		assertThat("Result around 'now'", ChronoUnit.SECONDS.between(now, result),
				is(allOf(greaterThanOrEqualTo(0L), lessThanOrEqualTo(1L))));
	}

	@Test
	public void toInstant_date() {
		// GIVEN
		LocalDate d = LocalDate.of(2024, 8, 6);

		// WHEN
		Instant result = timestamp(d);

		// THEN
		assertThat("Instant returned for start of day in system time zone", result,
				is(equalTo(d.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant())));
	}

	@Test
	public void toInstant_date_zone() {
		// GIVEN
		LocalDate d = LocalDate.of(2024, 8, 6);
		ZoneId zone = ZoneId.of("Pacific/Honolulu");

		// WHEN
		Instant result = timestamp(d, zone);

		// THEN
		assertThat("Instant returned for start of day in given time zone", result,
				is(equalTo(d.atStartOfDay().atZone(zone).toInstant())));
	}

	@Test
	public void thisDay() {
		// GIVEN
		final LocalDate today = LocalDate.now();

		// WHEN
		LocalDate result = today();

		assertThat("Current local date returned", result, is(equalTo(today)));
	}

	@Test
	public void currTime() {
		// GIVEN
		final LocalDateTime now = LocalDateTime.now();

		// WHEN
		LocalDateTime result = now();

		// THEN
		assertThat("LocalDateTime returned", result, is(notNullValue()));
		assertThat("Result around 'now'", ChronoUnit.SECONDS.between(now, result),
				is(allOf(greaterThanOrEqualTo(0L), lessThanOrEqualTo(1L))));
	}

	@Test
	public void currTimeTz() {
		// GIVEN
		final ZonedDateTime now = ZonedDateTime.now();

		// WHEN
		ZonedDateTime result = nowTz();

		// THEN
		assertThat("ZonedDateTime returned", result, is(notNullValue()));
		assertThat("Result around 'now'", ChronoUnit.SECONDS.between(now, result),
				is(allOf(greaterThanOrEqualTo(0L), lessThanOrEqualTo(1L))));
	}

	@Test
	public void parseLocalDate() {
		// WHEN
		LocalDate result = date("2024-11-20");

		// THEN
		assertThat("Local date string parsed", result, is(equalTo(LocalDate.of(2024, 11, 20))));
	}

	@Test
	public void parseLocalTime() {
		// WHEN
		LocalTime result = time("15:09");

		// THEN
		assertThat("Local time string parsed", result, is(equalTo(LocalTime.of(15, 9))));
	}

	@Test
	public void parseLocalTime_withSecs() {
		// WHEN
		LocalTime result = time("15:09:45");

		// THEN
		assertThat("Local time string with seconds parsed", result,
				is(equalTo(LocalTime.of(15, 9, 45))));
	}

	@Test
	public void parseTimestamp_date() {
		// WHEN
		Instant result = timestamp("2024-11-20");

		// THEN
		assertThat("Date timestamp parsed", result, is(equalTo(
				LocalDateTime.of(2024, 11, 20, 0, 0).atZone(ZoneId.systemDefault()).toInstant())));
	}

	@Test
	public void parseTimestamp_dateTime() {
		// WHEN
		Instant result = timestamp("2024-11-20T12:34:56.789");

		// THEN
		assertThat("Date timestamp parsed", result,
				is(equalTo(LocalDateTime
						.of(2024, 11, 20, 12, 34, 56, (int) TimeUnit.MILLISECONDS.toNanos(789))
						.atZone(ZoneId.systemDefault()).toInstant())));
	}

	@Test
	public void parseTimestamp_dateTimeZone() {
		// WHEN
		Instant result = timestamp("2024-11-20T12:34:56.789+12:00");

		// THEN
		assertThat("Date timestamp parsed", result,
				is(equalTo(LocalDateTime
						.of(2024, 11, 20, 12, 34, 56, (int) TimeUnit.MILLISECONDS.toNanos(789))
						.atZone(ZoneOffset.ofHours(12)).toInstant())));
	}

	@Test
	public void parseTimestamp_dateAndZone() {
		// WHEN
		Instant result = timestamp("2024-11-20Z");

		// THEN
		assertThat("Date timestamp parsed", result,
				is(equalTo(LocalDateTime.of(2024, 11, 20, 0, 0).atZone(ZoneOffset.UTC).toInstant())));
	}

	@Test
	public void durBetween_LocalDateTime() {
		// GIVEN
		LocalDateTime d1 = LocalDateTime.of(2024, 8, 6, 14, 15);
		LocalDateTime d2 = LocalDateTime.of(2024, 8, 6, 14, 20);

		// WHEN
		Duration result = durationBetween(d1, d2);

		// THEN
		assertThat("Duration between two LocalDateTime returned", result,
				is(equalTo(Duration.ofMinutes(5))));
	}

	@Test
	public void secsBetween_LocalDateTime() {
		// GIVEN
		LocalDateTime d1 = LocalDateTime.of(2024, 8, 6, 14, 15);
		LocalDateTime d2 = LocalDateTime.of(2024, 8, 6, 14, 20);

		// WHEN
		long result = secondsBetween(d1, d2);

		// THEN
		assertThat("Seconds between two LocalDateTime returned", result,
				is(equalTo(Duration.between(d1, d2).getSeconds())));
	}

	@Test
	public void minsBetween_LocalDateTime() {
		// GIVEN
		LocalDateTime d1 = LocalDateTime.of(2024, 8, 6, 14, 15);
		LocalDateTime d2 = LocalDateTime.of(2024, 8, 6, 14, 20);

		// WHEN
		long result = minutesBetween(d1, d2);

		// THEN
		assertThat("Minutes between two LocalDateTime returned", result,
				is(equalTo(Duration.between(d1, d2).toMinutes())));
	}

	@Test
	public void hoursBetween_LocalDateTime() {
		// GIVEN
		LocalDateTime d1 = LocalDateTime.of(2024, 8, 6, 14, 15);
		LocalDateTime d2 = LocalDateTime.of(2024, 8, 6, 16, 15);

		// WHEN
		long result = hoursBetween(d1, d2);

		// THEN
		assertThat("Hours between two LocalDateTime returned", result,
				is(equalTo(Duration.between(d1, d2).toHours())));
	}

	@Test
	public void daysBetween_LocalDateTime() {
		// GIVEN
		LocalDateTime d1 = LocalDateTime.of(2024, 8, 6, 14, 15);
		LocalDateTime d2 = LocalDateTime.of(2024, 8, 8, 14, 15);

		// WHEN
		long result = daysBetween(d1, d2);

		// THEN
		assertThat("Days between two LocalDateTime returned", result,
				is(equalTo(Duration.between(d1, d2).toDays())));
	}

	@Test
	public void hoursBetween_LocalDate() {
		// GIVEN
		LocalDate d1 = LocalDate.of(2024, 8, 6);
		LocalDate d2 = LocalDate.of(2024, 8, 8);

		// WHEN
		long result = hoursBetween(d1, d2);

		// THEN
		assertThat("Hours between two LocalDate returned", result, is(equalTo(48L)));
	}

	@Test
	public void daysBetween_LocalDate() {
		// GIVEN
		LocalDate d1 = LocalDate.of(2024, 8, 6);
		LocalDate d2 = LocalDate.of(2024, 8, 8);

		// WHEN
		long result = daysBetween(d1, d2);

		// THEN
		assertThat("Days between two LocalDate returned", result, is(equalTo(2L)));
	}

	@Test
	public void minutesBetween_LocalTime() {
		// GIVEN
		LocalTime d1 = LocalTime.of(10, 10);
		LocalTime d2 = LocalTime.of(11, 11);

		// WHEN
		long result = minutesBetween(d1, d2);

		// THEN
		assertThat("Minutes between two LocalTime returned", result, is(equalTo(61L)));
	}

	@Test
	public void hoursBetween_LocalTime() {
		// GIVEN
		LocalTime d1 = LocalTime.of(10, 10);
		LocalTime d2 = LocalTime.of(11, 11);

		// WHEN
		long result = hoursBetween(d1, d2);

		// THEN
		assertThat("Hours between two LocalTime returned", result, is(equalTo(1L)));
	}

	@Test
	public void monthsBetween_LocalDate_withinMonth() {
		// GIVEN
		LocalDate d1 = LocalDate.of(2024, 9, 4);
		LocalDate d2 = LocalDate.of(2024, 9, 10);

		// WHEN
		double result = monthsBetween(d1, d2);

		// THEN
		assertThat("Months between two LocalDate returned", result, is(equalTo(0.2)));
	}

	@Test
	public void monthsBetween_LocalDate_withinMonth_reverse() {
		// GIVEN
		LocalDate d1 = LocalDate.of(2024, 9, 4);
		LocalDate d2 = LocalDate.of(2024, 9, 10);

		// WHEN
		double result = monthsBetween(d2, d1);

		// THEN
		assertThat("Months between two LocalDate returned", result, is(equalTo(-0.2)));
	}

	@Test
	public void monthsBetween_LocalDate_onBoundaries() {
		// GIVEN
		LocalDate d1 = LocalDate.of(2024, 8, 1);
		LocalDate d2 = LocalDate.of(2024, 9, 1);

		// WHEN
		double result = monthsBetween(d1, d2);

		// THEN
		assertThat("Months between two LocalDate returned", result, is(equalTo(1.0)));
	}

	@Test
	public void monthsBetween_LocalDate_onStartBoundaries() {
		// GIVEN
		LocalDate d1 = LocalDate.of(2024, 8, 1);
		LocalDate d2 = LocalDate.of(2024, 9, 15);

		// WHEN
		double result = monthsBetween(d1, d2);

		// THEN
		assertThat("Months between two LocalDate returned", NumberUtils.round(result, 2),
				is(equalTo(new BigDecimal("1.47"))));
	}

	@Test
	public void monthsBetween_LocalDate_onEndBoundaries() {
		// GIVEN
		LocalDate d1 = LocalDate.of(2024, 8, 15);
		LocalDate d2 = LocalDate.of(2024, 10, 1);

		// WHEN
		double result = monthsBetween(d1, d2);

		// THEN
		assertThat("Months between two LocalDate returned", NumberUtils.round(result, 2),
				is(equalTo(new BigDecimal("1.55"))));
	}

	@Test
	public void yearsBetween_LocalDate_withinYear() {
		// GIVEN
		LocalDate d1 = LocalDate.of(2024, 2, 1);
		LocalDate d2 = LocalDate.of(2024, 10, 1);

		// WHEN
		double result = yearsBetween(d1, d2);

		// THEN
		assertThat("Years between two LocalDate returned", NumberUtils.round(result, 2),
				is(equalTo(new BigDecimal("0.66"))));
	}

	@Test
	public void yearsBetween_LocalDate_withinYear_reverse() {
		// GIVEN
		LocalDate d1 = LocalDate.of(2024, 2, 1);
		LocalDate d2 = LocalDate.of(2024, 10, 1);

		// WHEN
		double result = yearsBetween(d2, d1);

		// THEN
		assertThat("Years between two LocalDate returned", NumberUtils.round(result, 2),
				is(equalTo(new BigDecimal("-0.66"))));
	}

	@Test
	public void yearsBetween_LocalDate_onBoundaries() {
		// GIVEN
		LocalDate d1 = LocalDate.of(2024, 1, 1);
		LocalDate d2 = LocalDate.of(2025, 1, 1);

		// WHEN
		double result = yearsBetween(d1, d2);

		// THEN
		assertThat("Years between two LocalDate returned", result, is(equalTo(1.0)));
	}

	@Test
	public void yearsBetween_LocalDate_onStartBoundaries() {
		// GIVEN
		LocalDate d1 = LocalDate.of(2024, 1, 1);
		LocalDate d2 = LocalDate.of(2025, 6, 15);

		// WHEN
		double result = yearsBetween(d1, d2);

		// THEN
		assertThat("Years between two LocalDate returned", NumberUtils.round(result, 2),
				is(equalTo(new BigDecimal("1.45"))));
	}

	@Test
	public void yearsBetween_LocalDate_onEndBoundaries() {
		// GIVEN
		LocalDate d1 = LocalDate.of(2024, 6, 15);
		LocalDate d2 = LocalDate.of(2026, 1, 1);

		// WHEN
		double result = yearsBetween(d1, d2);

		// THEN
		assertThat("Years between two LocalDate returned", NumberUtils.round(result, 2),
				is(equalTo(new BigDecimal("1.55"))));
	}

	@Test(expected = IllegalArgumentException.class)
	public void formatDate_localDate_invalidPattern() {
		// WHEN
		formatDate(LocalDate.of(2025, 3, 14), "wtf");
	}

	@Test
	public void formatDate_localDate_yyyyMMdd() {
		// WHEN
		String result = formatDate(LocalDate.of(2025, 3, 14), "yyyy-MM-dd");

		// THEN
		assertThat("Date formatted", result, is(equalTo("2025-03-14")));
	}

	@Test
	public void formatDate_localDate_yyyyMMdd_zone() {
		// WHEN
		String result = formatDate(LocalDate.of(2025, 3, 14), "yyyy-MM-dd",
				ZoneId.of("America/Los_Angeles"));

		// THEN
		assertThat("Date formatted", result, is(equalTo("2025-03-14")));
	}

	@Test(expected = IllegalArgumentException.class)
	public void formatDate_localTime_invalidPattern() {
		// WHEN
		formatDate(LocalTime.of(15, 13, 22, 123456789), "wtf");
	}

	@Test
	public void formatDate_localTime_HHmmss() {
		// WHEN
		String result = formatDate(LocalTime.of(15, 13, 22, 123456789), "HH:mm:ss");

		// THEN
		assertThat("Date formatted", result, is(equalTo("15:13:22")));
	}

	@Test
	public void formatDate_localTime_HHmmssSSS() {
		// WHEN
		String result = formatDate(LocalTime.of(15, 13, 22, 123456789), "HH:mm:ss.SSS");

		// THEN
		assertThat("Date formatted", result, is(equalTo("15:13:22.123")));
	}

	@Test
	public void formatDate_localTime_HHmmssn() {
		// WHEN
		String result = formatDate(LocalTime.of(15, 13, 22, 123456789), "HH:mm:ss.n");

		// THEN
		assertThat("Date formatted", result, is(equalTo("15:13:22.123456789")));
	}

	@Test
	public void formatDate_localTime_HHmmssS_trailingZeros() {
		// WHEN
		String result = formatDate(LocalTime.of(15, 13, 22, 123000000), "HH:mm:ss.SSSSSSSSS");

		// THEN
		assertThat("Date formatted", result, is(equalTo("15:13:22.123000000")));
	}

	@Test
	public void formatDate_localTime_HHmmss_zone() {
		// WHEN
		String result = formatDate(LocalTime.of(15, 13, 22, 123456789), "HH:mm:ss",
				ZoneId.of("America/Los_Angeles"));

		// THEN
		assertThat("Date formatted", result, is(equalTo("15:13:22")));
	}

	@Test(expected = IllegalArgumentException.class)
	public void formatDate_localDateTime_invalidPattern() {
		// WHEN
		formatDate(LocalDateTime.of(2025, 3, 14, 15, 13, 22, 123456789), "wtf");
	}

	@Test
	public void formatDate_localDateTime_yyyyMMddHHmmss() {
		// WHEN
		String result = formatDate(LocalDateTime.of(2025, 3, 14, 15, 13, 22, 123456789),
				"yyyy-MM-dd'T'HH:mm:ss");

		// THEN
		assertThat("Date time formatted", result, is(equalTo("2025-03-14T15:13:22")));
	}

	@Test
	public void formatDate_localDateTime_yyyyMMddHHmmss_zone() {
		// WHEN
		String result = formatDate(LocalDateTime.of(2025, 3, 14, 15, 13, 22, 123456789),
				"yyyy-MM-dd'T'HH:mm:ss", ZoneId.of("America/Los_Angeles"));

		// THEN
		assertThat("Date time formatted", result, is(equalTo("2025-03-14T15:13:22")));
	}

	@Test
	public void formatDate_offsetDateTime_yyyyMMddHHmmss() {
		// GIVEN
		Temporal date = LocalDateTime.of(2025, 3, 14, 15, 13, 22, 123456789).atOffset(UTC);

		// WHEN
		String result = formatDate(date, "yyyy-MM-dd'T'HH:mm:ss");

		// THEN
		assertThat("Date time formatted", result,
				is(equalTo(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
						.withChronology(IsoChronology.INSTANCE).withZone(ZoneId.systemDefault())
						.format(date))));
	}

	@Test
	public void formatDate_offsetDateTime_yyyyMMddHHmmss_zone() {
		// GIVEN
		Temporal date = LocalDateTime.of(2025, 3, 14, 15, 13, 22, 123456789).atOffset(UTC);

		// WHEN
		String result = formatDate(date, "yyyy-MM-dd'T'HH:mm:ss", ZoneId.of("America/Los_Angeles"));

		// THEN
		assertThat("Date time formatted", result,
				is(equalTo(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
						.withChronology(IsoChronology.INSTANCE)
						.withZone(ZoneId.of("America/Los_Angeles")).format(date))));
	}

	@Test
	public void formatDate_offsetDateTime_yyyyMMddHHmmss_zone_sameZone() {
		// GIVEN
		Temporal date = LocalDateTime.of(2025, 3, 14, 15, 13, 22, 123456789).atOffset(UTC);

		// WHEN
		String result = formatDate(date, "yyyy-MM-dd'T'HH:mm:ss", UTC);

		// THEN
		assertThat("Date time formatted", result, is(equalTo("2025-03-14T15:13:22")));
	}

	@Test
	public void formatDate_zonedDateTime_yyyyMMddHHmmss() {
		// GIVEN
		Temporal date = LocalDateTime.of(2025, 3, 14, 15, 13, 22, 123456789)
				.atZone(ZoneId.of("Pacific/Auckland"));

		// WHEN
		String result = formatDate(date, "yyyy-MM-dd'T'HH:mm:ss");

		// THEN
		assertThat("Date time formatted", result,
				is(equalTo(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
						.withChronology(IsoChronology.INSTANCE).withZone(ZoneId.systemDefault())
						.format(date))));
	}

	@Test
	public void formatDate_zonedDateTime_yyyyMMddHHmmss_zone() {
		// GIVEN
		Temporal date = LocalDateTime.of(2025, 3, 14, 15, 13, 22, 123456789)
				.atZone(ZoneId.of("Pacific/Auckland"));

		// WHEN
		String result = formatDate(date, "yyyy-MM-dd'T'HH:mm:ss", ZoneId.of("America/Los_Angeles"));

		// THEN
		assertThat("Date time formatted", result,
				is(equalTo(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
						.withChronology(IsoChronology.INSTANCE)
						.withZone(ZoneId.of("America/Los_Angeles")).format(date))));
	}

	@Test
	public void timestamp_epoch() {
		// GIVEN
		long ts = System.currentTimeMillis();

		// WHEN
		Instant result = timestamp(ts);

		// THEN
		assertThat("Instant created from epoch milliseconds", result.toEpochMilli(), is(equalTo(ts)));
	}

	@Test
	public void timestamp_epoch_null() {
		// WHEN
		Instant result = timestamp((Long) null);

		// THEN
		assertThat("Null returned from null input", result, is(nullValue()));
	}

	@Test
	public void dateTz_epoch() {
		// GIVEN
		long ts = System.currentTimeMillis();

		// WHEN
		ZonedDateTime result = dateTz(ts);

		// THEN
		assertThat("ZonedDateTime created from epoch milliseconds with system time zone", result,
				is(equalTo(Instant.ofEpochMilli(ts).atZone(ZoneId.systemDefault()))));
	}

	@Test
	public void dateTz_epoch_null() {
		// WHEN
		ZonedDateTime result = dateTz((Long) null);

		// THEN
		assertThat("Null returned from null input", result, is(nullValue()));
	}

	@Test
	public void dateTz_epoch_zone() {
		// GIVEN
		long ts = System.currentTimeMillis();
		ZoneId zone = ZoneId.of("America/Los_Angeles");

		// WHEN
		ZonedDateTime result = dateTz(ts, zone);

		// THEN
		assertThat("ZonedDateTime created from epoch milliseconds in given time zone", result,
				is(equalTo(Instant.ofEpochMilli(ts).atZone(zone))));
	}

	@Test
	public void dateTz_epoch_zoneId() {
		// GIVEN
		long ts = System.currentTimeMillis();
		String zoneId = "America/Los_Angeles";

		// WHEN
		ZonedDateTime result = dateTz(ts, zoneId);

		// THEN
		assertThat("ZonedDateTime created from epoch milliseconds in given time zone ID", result,
				is(equalTo(Instant.ofEpochMilli(ts).atZone(ZoneId.of(zoneId)))));
	}

	@Test
	public void dateTz_instant() {
		// GIVEN
		Instant ts = Instant.now();

		// WHEN
		ZonedDateTime result = dateTz(ts);

		// THEN
		assertThat("ZonedDateTime created from Instant with system time zone", result,
				is(equalTo(ts.atZone(ZoneId.systemDefault()))));
	}

	@Test
	public void dateTz_instant_null() {
		// WHEN
		ZonedDateTime result = dateTz((Instant) null);

		// THEN
		assertThat("Null returned from null input", result, is(nullValue()));
	}

	@Test
	public void dateTz_instant_zone() {
		// GIVEN
		Instant ts = Instant.now();
		ZoneId zone = ZoneId.of("America/Los_Angeles");

		// WHEN
		ZonedDateTime result = dateTz(ts, zone);

		// THEN
		assertThat("ZonedDateTime created from Instant in given time zone", result,
				is(equalTo(ts.atZone(zone))));
	}

	@Test
	public void dateTz_instant_zoneId() {
		// GIVEN
		Instant ts = Instant.now();
		String zoneId = "America/Los_Angeles";

		// WHEN
		ZonedDateTime result = dateTz(ts, zoneId);

		// THEN
		assertThat("ZonedDateTime created from Instant in given time zone ID", result,
				is(equalTo(ts.atZone(ZoneId.of(zoneId)))));
	}

	@Test
	public void startOfDay_default() {
		// WHEN
		ZonedDateTime result = startOfDay();

		// THEN
		assertThat("ZonedDateTime at midnight today in system time zone", result,
				is(equalTo(ZonedDateTime.now().truncatedTo(DAYS))));
	}

	@Test
	public void startOfDay_zoneId() {
		// GIVEN
		final String zoneId = "America/Los_Angeles";

		// WHEN
		ZonedDateTime result = startOfDay(zoneId);

		// THEN
		assertThat("ZonedDateTime at midnight today in given time zone", result,
				is(equalTo(ZonedDateTime.now(ZoneId.of(zoneId)).truncatedTo(DAYS))));
	}

	@Test
	public void startOfDay_zone() {
		// GIVEN
		final ZoneId zone = ZoneId.of("America/Los_Angeles");

		// WHEN
		ZonedDateTime result = startOfDay(zone);

		// THEN
		assertThat("ZonedDateTime at midnight today in given time zone", result,
				is(equalTo(ZonedDateTime.now(zone).truncatedTo(DAYS))));
	}

	@Test
	public void epoch_Instant() {
		//GIVEN
		final Instant date = Instant.now();

		// WHEN
		Long result = epoch(date);

		// THEN
		assertThat("Epoch for Instant", result, is(equalTo(date.toEpochMilli())));
	}

	@Test
	public void epochSecs_Instant() {
		//GIVEN
		final Instant date = Instant.now();

		// WHEN
		Long result = epochSecs(date);

		// THEN
		assertThat("Epoch for Instant", result, is(equalTo(date.getEpochSecond())));
	}

	@Test
	public void epoch_ZonedDateTime() {
		//GIVEN
		final ZonedDateTime date = ZonedDateTime.now(ZoneId.of("America/Los_Angeles"));

		// WHEN
		Long result = epoch(date);

		// THEN
		assertThat("Epoch for ZonedDateTime", result, is(equalTo(date.toInstant().toEpochMilli())));
	}

	@Test
	public void epochSecs_ZonedDateTime() {
		//GIVEN
		final ZonedDateTime date = ZonedDateTime.now(ZoneId.of("America/Los_Angeles"));

		// WHEN
		Long result = epochSecs(date);

		// THEN
		assertThat("Epoch for ZonedDateTime", result, is(equalTo(date.toInstant().getEpochSecond())));
	}

	@Test
	public void epoch_LocalDate() {
		//GIVEN
		final LocalDate date = LocalDate.now();

		// WHEN
		Long result = epoch(date);

		// THEN
		assertThat("Epoch for LocalDate is midnight in system time zone", result,
				is(equalTo(date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())));
	}

	@Test
	public void epochSecs_LocalDate() {
		//GIVEN
		final LocalDate date = LocalDate.now();

		// WHEN
		Long result = epochSecs(date);

		// THEN
		assertThat("Epoch for LocalDate is midnight in system time zone", result,
				is(equalTo(date.atStartOfDay(ZoneId.systemDefault()).toInstant().getEpochSecond())));
	}

	@Test
	public void dateFloor_Instant() {
		// GIVEN
		Instant date = Instant.now();

		// WHEN
		Temporal result = dateFloor(date, Duration.ofMinutes(15));

		// THEN
		assertThat("Floored Instant in system time zone", result, is(equalTo(date.truncatedTo(MINUTES)
				.minus(date.atZone(ZoneId.systemDefault()).getMinute() % 15, MINUTES))));
	}

}
