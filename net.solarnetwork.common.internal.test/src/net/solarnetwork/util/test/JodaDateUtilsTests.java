/* ==================================================================
 * JodaDateUtilsTests.java - 21/11/2020 10:22:30 am
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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.junit.Test;
import net.solarnetwork.util.JodaDateUtils;

/**
 * Test cases for the {@link JodaDateUtils} class.
 * 
 * @author matt
 * @version 1.0
 */
public class JodaDateUtilsTests {

	private static final String TEST_TZ = "America/Chicago";

	@Test
	public void convertDateTime() {
		// GIVEN
		org.joda.time.DateTime joda = new org.joda.time.DateTime(2020, 1, 1, 12, 34, 56, 789,
				org.joda.time.DateTimeZone.forID(TEST_TZ));

		// THEN
		assertThat("Joda date converted", JodaDateUtils.fromJoda(joda),
				equalTo(ZonedDateTime.of(2020, 1, 1, 12, 34, 56, 789000000, ZoneId.of(TEST_TZ))));
	}

	@Test
	public void convertLocalDate() {
		// GIVEN
		org.joda.time.LocalDate joda = new org.joda.time.LocalDate(2020, 1, 1);

		// THEN
		assertThat("Joda date converted", JodaDateUtils.fromJoda(joda),
				equalTo(LocalDate.of(2020, 1, 1)));
	}

	@Test
	public void convertLocalTime() {
		// GIVEN
		org.joda.time.LocalTime joda = new org.joda.time.LocalTime(12, 34, 56, 789);

		// THEN
		assertThat("Joda date converted", JodaDateUtils.fromJoda(joda),
				equalTo(LocalTime.of(12, 34, 56, 789000000)));
	}

	@Test
	public void convertLocalDateTime() {
		// GIVEN
		org.joda.time.LocalDateTime joda = new org.joda.time.LocalDateTime(2020, 1, 1, 12, 34, 56, 789);

		// THEN
		assertThat("Joda date converted", JodaDateUtils.fromJoda(joda),
				equalTo(LocalDateTime.of(2020, 1, 1, 12, 34, 56, 789000000)));
	}

	@Test
	public void convertPeriod() {
		// GIVEN
		org.joda.time.Period joda = org.joda.time.Period.years(1).withMonths(2).withDays(3);

		// THEN
		assertThat("Joda date converted", JodaDateUtils.fromJoda(joda), equalTo(Period.of(1, 2, 3)));
	}

	@Test
	public void convertDuration() {
		// GIVEN
		org.joda.time.Duration joda = org.joda.time.Duration.standardHours(1);

		// THEN
		assertThat("Joda date converted", JodaDateUtils.fromJoda(joda), equalTo(Duration.ofHours(1)));
	}
}
