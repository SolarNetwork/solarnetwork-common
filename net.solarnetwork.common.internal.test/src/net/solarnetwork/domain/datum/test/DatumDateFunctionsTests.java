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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import org.junit.Test;
import net.solarnetwork.domain.datum.DatumDateFunctions;

/**
 * Test cases for the {@link DatumDateFunctions} interface.
 *
 * @author matt
 * @version 1.0
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

		assertThat("ZonedDateTime returned", result, is(notNullValue()));
		assertThat("Result around 'now'", ChronoUnit.SECONDS.between(now, result),
				is(allOf(greaterThanOrEqualTo(0L), lessThanOrEqualTo(1L))));
	}

}
