/* ==================================================================
 * JodaDateUtils.java - 21/11/2020 10:17:54 am
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

package net.solarnetwork.util;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.joda.time.DateTimeZone;

/**
 * Utility methods for Joda dates.
 * 
 * @author matt
 * @version 1.0
 */
public final class JodaDateUtils {

	private static final int NANO_PER_MILLI = 1000000;

	private JodaDateUtils() {
		// don't construct me
	}

	/**
	 * Convert a Joda {@code DateTime} into a Java {@code ZonedDateTime}.
	 * 
	 * @param joda
	 *        the {@code org.joda.time} date
	 * @return the equivalent {@code java.time} date, or {@literal null} if
	 *         {@code joda} is {@literal null}
	 */
	public static ZonedDateTime fromJoda(org.joda.time.DateTime joda) {
		if ( joda == null ) {
			return null;
		}
		org.joda.time.DateTimeZone jzone = joda.getZone();
		ZoneId zone = jzone != null ? ZoneId.of(jzone.getID()) : ZoneId.systemDefault();
		return ZonedDateTime.of(joda.getYear(), joda.getMonthOfYear(), joda.getDayOfMonth(),
				joda.getHourOfDay(), joda.getMinuteOfHour(), joda.getSecondOfMinute(),
				joda.getMillisOfSecond() * NANO_PER_MILLI, zone);
	}

	/**
	 * Convert a Joda {@code DateTime} into a Java {@code Instant}.
	 * 
	 * @param joda
	 *        the {@code org.joda.time} date
	 * @return the equivalent {@code java.time} date, or {@literal null} if
	 *         {@code joda} is {@literal null}
	 * @see #fromJoda(org.joda.time.DateTime)
	 */
	public static Instant fromJodaToInstant(org.joda.time.DateTime joda) {
		ZonedDateTime zdt = fromJoda(joda);
		return (zdt != null ? zdt.toInstant() : null);
	}

	/**
	 * Convert a {@link ZonedDateTime} to a Joda {@code DateTime}.
	 * 
	 * @param date
	 *        the {@code java.time} date
	 * @return the equivalent {@code org.joda.time} date, or {@literal null} if
	 *         {@code date} is {@literal null}
	 */
	public static org.joda.time.DateTime toJoda(ZonedDateTime date) {
		if ( date == null ) {
			return null;
		}
		return toJoda(date.toInstant(), date != null ? date.getZone() : (ZoneId) null);
	}

	/**
	 * Convert an {@link Instant} to a Joda {@code DateTime}.
	 * 
	 * @param date
	 *        the {@code java.time} date
	 * @param zone
	 *        the zone
	 * @return the equivalent {@code org.joda.time} date, or {@literal null} if
	 *         {@code date} is {@literal null}
	 */
	public static org.joda.time.DateTime toJoda(Instant date, ZoneId zone) {
		if ( date == null ) {
			return null;
		}
		return toJoda(date, zone != null ? zone.getId() : null);
	}

	/**
	 * Convert an {@link Instant} to a Joda {@code DateTime}.
	 * 
	 * @param date
	 *        the {@code java.time} date
	 * @return the equivalent {@code org.joda.time} date, or {@literal null} if
	 *         {@code date} is {@literal null}
	 */
	public static org.joda.time.DateTime toJoda(Instant date, String timeZoneId) {
		if ( date == null ) {
			return null;
		}
		// ZoneOffset.UTC returns an ID of "Z" which Joda does not recognize
		org.joda.time.DateTimeZone jzone = (timeZoneId != null
				? "Z".equals(timeZoneId) ? DateTimeZone.UTC
						: org.joda.time.DateTimeZone.forID(timeZoneId)
				: DateTimeZone.getDefault());
		return new org.joda.time.DateTime(date.toEpochMilli(), jzone);
	}

	/**
	 * Convert a Joda {@code LocalDate} into a Java {@link LocalDate}.
	 * 
	 * @param joda
	 *        the {@code org.joda.time} date
	 * @return the equivalent {@code java.time} date, or {@literal null} if
	 *         {@code joda} is {@literal null}
	 */
	public static LocalDate fromJoda(org.joda.time.LocalDate joda) {
		if ( joda == null ) {
			return null;
		}
		return LocalDate.of(joda.getYear(), joda.getMonthOfYear(), joda.getDayOfMonth());
	}

	/**
	 * Convert an {@link LocalDate} to a Joda {@code LocalDate}.
	 * 
	 * @param date
	 *        the {@code java.time} date
	 * @return the equivalent {@code org.joda.time} date, or {@literal null} if
	 *         {@code date} is {@literal null}
	 */
	public static org.joda.time.LocalDate toJoda(LocalDate date) {
		if ( date == null ) {
			return null;
		}
		return new org.joda.time.LocalDate(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
	}

	/**
	 * Convert a Joda {@code LocalTime} into a Java {@link LocalTime}.
	 * 
	 * @param joda
	 *        the {@code org.joda.time} date
	 * @return the equivalent {@code java.time} date, or {@literal null} if
	 *         {@code joda} is {@literal null}
	 */
	public static LocalTime fromJoda(org.joda.time.LocalTime joda) {
		if ( joda == null ) {
			return null;
		}
		return LocalTime.of(joda.getHourOfDay(), joda.getMinuteOfHour(), joda.getSecondOfMinute(),
				joda.getMillisOfSecond() * NANO_PER_MILLI);
	}

	/**
	 * Convert an {@link LocalTime} to a Joda {@code LocalTime}.
	 * 
	 * @param date
	 *        the {@code java.time} date
	 * @return the equivalent {@code org.joda.time} date, or {@literal null} if
	 *         {@code date} is {@literal null}
	 */
	public static org.joda.time.LocalTime toJoda(LocalTime date) {
		if ( date == null ) {
			return null;
		}
		return new org.joda.time.LocalTime(date.getHour(), date.getMinute(), date.getSecond(),
				date.getNano() / NANO_PER_MILLI);
	}

	/**
	 * Convert a Joda {@code LocalDateTime} into a Java {@code LocalDateTime}.
	 * 
	 * @param joda
	 *        the {@code org.joda.time} date
	 * @return the equivalent {@code java.time} date, or {@literal null} if
	 *         {@code joda} is {@literal null}
	 */
	public static LocalDateTime fromJoda(org.joda.time.LocalDateTime joda) {
		if ( joda == null ) {
			return null;
		}
		return LocalDateTime.of(joda.getYear(), joda.getMonthOfYear(), joda.getDayOfMonth(),
				joda.getHourOfDay(), joda.getMinuteOfHour(), joda.getSecondOfMinute(),
				joda.getMillisOfSecond() * NANO_PER_MILLI);
	}

	/**
	 * Convert an {@link LocalDateTime} to a Joda {@code DateTime}.
	 * 
	 * @param date
	 *        the {@code java.time} date
	 * @return the equivalent {@code org.joda.time} date, or {@literal null} if
	 *         {@code date} is {@literal null}
	 */
	public static org.joda.time.LocalDateTime toJoda(LocalDateTime date) {
		if ( date == null ) {
			return null;
		}
		return new org.joda.time.LocalDateTime(date.getYear(), date.getMonthValue(),
				date.getDayOfMonth(), date.getHour(), date.getMinute(), date.getSecond(),
				date.getNano() / NANO_PER_MILLI);
	}

	/**
	 * Convert a Joda {@code Period} into a Java {@code Period}.
	 * 
	 * @param joda
	 *        the {@code org.joda.time} date
	 * @return the equivalent {@code java.time} date, or {@literal null} if
	 *         {@code joda} is {@literal null}
	 */
	public static Period fromJoda(org.joda.time.Period joda) {
		if ( joda == null ) {
			return null;
		}
		return Period.of(joda.getYears(), joda.getMonths(), joda.getDays());
	}

	/**
	 * Convert an {@link Period} to a Joda {@code Period}.
	 * 
	 * @param date
	 *        the {@code java.time} date
	 * @return the equivalent {@code org.joda.time} date, or {@literal null} if
	 *         {@code date} is {@literal null}
	 */
	public static org.joda.time.Period toJoda(Period date) {
		if ( date == null ) {
			return null;
		}
		return new org.joda.time.Period(date.getYears(), date.getMonths(), 0, date.getDays(), 0, 0, 0,
				0);
	}

	/**
	 * Convert a Joda {@code Duration} into a Java {@code Duration}.
	 * 
	 * @param joda
	 *        the {@code org.joda.time} date
	 * @return the equivalent {@code java.time} date, or {@literal null} if
	 *         {@code joda} is {@literal null}
	 */
	public static Duration fromJoda(org.joda.time.Duration joda) {
		if ( joda == null ) {
			return null;
		}
		return Duration.ofMillis(joda.getMillis());
	}

	/**
	 * Convert an {@link Duration} to a Joda {@code Duration}.
	 * 
	 * @param date
	 *        the {@code java.time} date
	 * @return the equivalent {@code org.joda.time} date, or {@literal null} if
	 *         {@code date} is {@literal null}
	 */
	public static org.joda.time.Duration toJoda(Duration date) {
		if ( date == null ) {
			return null;
		}
		return new org.joda.time.Duration(date.toMillis());
	}

}
