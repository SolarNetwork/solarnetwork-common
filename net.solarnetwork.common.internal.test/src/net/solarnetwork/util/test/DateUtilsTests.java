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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAccessor;
import org.junit.Test;
import net.solarnetwork.util.DateUtils;

/**
 * Test cases for the {@link DateUtils} class.
 * 
 * @author matt
 * @version 1.0
 */
public class DateUtilsTests {

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
		assumeThat("Behavior in Java 11", JAVA_VERS, lessThan(11.0));
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

}
