/* ==================================================================
 * DatumDateFunctions.java - 6/08/2024 1:27:07 pm
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

package net.solarnetwork.domain.datum;

import static java.lang.String.format;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import net.solarnetwork.util.DateUtils;

/**
 * API for datum-related date helper functions.
 *
 * @author matt
 * @version 1.4
 * @since 3.17
 */
public interface DatumDateFunctions {

	/**
	 * Parse a time zone ID into a zone instance.
	 *
	 * @param zoneId
	 *        the zone ID to parse, or {@literal null} to resolve the system
	 *        time zone
	 * @return the zone, never {@literal null}
	 * @throws IllegalArgumentException
	 *         if {@code zoneId} cannot be parsed
	 */
	default ZoneId tz(String zoneId) {
		return DateUtils.tz(zoneId);
	}

	/**
	 * Get the local date right now in the system time zone.
	 *
	 * @return the local date
	 */
	default LocalDate today() {
		return LocalDate.now();
	}

	/**
	 * Get the local date right now in the given time zone.
	 *
	 * @param zoneId
	 *        the time zone, or {@literal null} to use the system time zone
	 * @return the local date
	 * @throws IllegalArgumentException
	 *         if {@code zoneId} cannot be parsed
	 */
	default LocalDate today(String zoneId) {
		return today(tz(zoneId));
	}

	/**
	 * Get the local date right now in the given time zone.
	 *
	 * @param zone
	 *        the time zone, or {@literal null} to use the system time zone
	 * @return the local date
	 */
	default LocalDate today(ZoneId zone) {
		if ( zone == null ) {
			return today();
		}
		return LocalDate.now(zone);
	}

	/**
	 * Get the start of the current day in the system time zone.
	 *
	 * @return the date and time
	 * @since 1.4
	 */
	default ZonedDateTime startOfDay() {
		return startOfDay((ZoneId) null);
	}

	/**
	 * Get the start of the current day in a given time zone.
	 *
	 * @param zoneId
	 *        the time zone, or {@literal null} to use the system time zone
	 * @return the date and time
	 * @throws IllegalArgumentException
	 *         if {@code zoneId} cannot be parsed
	 * @since 1.4
	 */
	default ZonedDateTime startOfDay(String zoneId) {
		return startOfDay(ZoneId.of(zoneId));
	}

	/**
	 * Get the start of the current day in a given time zone.
	 *
	 * @param zone
	 *        the time zone, or {@literal null} to use the system time zone
	 * @return the date and time
	 * @since 1.4
	 */
	default ZonedDateTime startOfDay(ZoneId zone) {
		if ( zone == null ) {
			zone = ZoneId.systemDefault();
		}
		return ZonedDateTime.now(zone).truncatedTo(ChronoUnit.DAYS);
	}

	/**
	 * Get the local date and time right now, in the system time zone.
	 *
	 * @return the local date and time
	 */
	default LocalDateTime now() {
		return LocalDateTime.now();
	}

	/**
	 * Get the local date and time right now in the given time zone.
	 *
	 * @param zoneId
	 *        the time zone, or {@literal null} to use the system time zone
	 * @return the local date and time
	 * @throws IllegalArgumentException
	 *         if {@code zoneId} cannot be parsed
	 */
	default LocalDateTime now(String zoneId) {
		return now(tz(zoneId));
	}

	/**
	 * Get the local date and time right now in the given time zone.
	 *
	 * @param zone
	 *        the time zone, or {@literal null} to use the system time zone
	 * @return the local date and time
	 */
	default LocalDateTime now(ZoneId zone) {
		if ( zone == null ) {
			return now();
		}
		return LocalDateTime.now(zone);
	}

	/**
	 * Get the date and time right now in the system time zone.
	 *
	 * @return the date and time
	 */
	default ZonedDateTime nowTz() {
		return ZonedDateTime.now();
	}

	/**
	 * Get the date and time right now in the system time zone.
	 *
	 * @param zoneId
	 *        the time zone, or {@literal null} to use the system time zone
	 * @return the date and time
	 * @throws IllegalArgumentException
	 *         if {@code zoneId} cannot be parsed
	 */
	default ZonedDateTime nowTz(String zoneId) {
		return nowTz(tz(zoneId));
	}

	/**
	 * Get the date and time right now in the system time zone.
	 *
	 * @param zone
	 *        the time zone, or {@literal null} to use the system time zone
	 * @return the date and time
	 */
	default ZonedDateTime nowTz(ZoneId zone) {
		if ( zone == null ) {
			return nowTz();
		}
		return ZonedDateTime.now(zone);
	}

	/**
	 * Get the local date and time from a zoned date and time.
	 *
	 * @param date
	 *        the zoned date and time to extract from
	 * @return the local date and time
	 * @since 1.1
	 */
	default LocalDateTime local(ZonedDateTime date) {
		if ( date == null ) {
			return null;
		}
		return date.toLocalDateTime();
	}

	/**
	 * Get the local date from a zoned date and time.
	 *
	 * @param date
	 *        the zoned date and time to extract from
	 * @return the local date
	 * @since 1.1
	 */
	default LocalDate localDate(ZonedDateTime date) {
		if ( date == null ) {
			return null;
		}
		return date.toLocalDate();
	}

	/**
	 * Get the local time from a zoned date and time.
	 *
	 * @param date
	 *        the zoned date and time to extract from
	 * @return the local time
	 * @since 1.1
	 */
	default LocalTime localTime(ZonedDateTime date) {
		if ( date == null ) {
			return null;
		}
		return date.toLocalTime();
	}

	/**
	 * Get the date and time right now as an {@link Instant}.
	 *
	 * @return the instant
	 */
	default Instant timestamp() {
		return Instant.now();
	}

	/**
	 * Get an {@link Instance} from a millisecond epoch date.
	 *
	 * @param date
	 *        the millisecond epoch date
	 * @return the instant, or {@literal null} if {@code date} is
	 *         {@literal null}
	 * @since 1.4
	 */
	default Instant timestamp(Long date) {
		if ( date == null ) {
			return null;
		}
		return Instant.ofEpochMilli(date);
	}

	/**
	 * Convert a {@link Temporal} into an {@link Instant} in the system time
	 * zone.
	 *
	 * @param date
	 *        the temporal to convert
	 * @return the converted instant, or {@literal null} if {code temporal} or
	 *         {@code zone} are {@literal null}
	 * @throws IllegalArgumentException
	 *         if the temporal type is not supported
	 */
	default Instant timestamp(Temporal date) {
		return DateUtils.timestamp(date);
	}

	/**
	 * Convert a {@link Temporal} into an {@link Instant} in the system time
	 * zone.
	 *
	 * @param date
	 *        the temporal to convert
	 * @param zoneId
	 *        the zone ID to use
	 * @return the converted instant, or {@literal null} if {code temporal} or
	 *         {@code zone} are {@literal null}
	 * @throws IllegalArgumentException
	 *         if the temporal type is not supported or {@code zoneId} is not
	 *         valid
	 */
	default Instant timestamp(Temporal date, String zoneId) {
		return DateUtils.timestamp(date, zoneId);
	}

	/**
	 * Convert a {@link Temporal} into an {@link Instant} in a given time zone.
	 *
	 * @param date
	 *        the temporal to convert
	 * @param zone
	 *        the time zone, or {@literal null} to use the system time zone
	 * @return the converted instant, or {@literal null} if {code temporal} or
	 *         {@code zone} are {@literal null}
	 * @throws IllegalArgumentException
	 *         if the temporal type is not supported
	 */
	default Instant timestamp(Temporal date, ZoneId zone) {
		return DateUtils.timestamp(date, zone);
	}

	/**
	 * Convert a {@link Temporal} into a Unix epoch milliseconds value.
	 *
	 * @param date
	 *        the date to get the epoch milliseconds for
	 * @return the epoch milliseconds, or {@code null} if {@code date} is
	 *         {@code null}
	 * @throws IllegalArgumentException
	 *         if the temporal type is not supported
	 * @since 1.4
	 */
	default Long epoch(Temporal date) {
		Instant ts = timestamp(date);
		return (ts != null ? ts.toEpochMilli() : null);
	}

	/**
	 * Convert a {@link Temporal} into a Unix epoch seconds value.
	 *
	 * @param date
	 *        the date to get the epoch seconds for
	 * @return the epoch seconds, or {@code null} if {@code date} is
	 *         {@code null}
	 * @throws IllegalArgumentException
	 *         if the temporal type is not supported
	 * @since 1.4
	 */
	default Long epochSecs(Temporal date) {
		Instant ts = timestamp(date);
		return (ts != null ? ts.getEpochSecond() : null);
	}

	/**
	 * Create a {@link LocalDate} instance.
	 *
	 * @param year
	 *        the calendar year
	 * @param month
	 *        the calendar month (1-12)
	 * @param day
	 *        the calendar day-of-month (1-31)
	 * @return the local date, not null
	 * @throws IllegalArgumentException
	 *         if the value of any field is out of range, or if the day-of-month
	 *         is invalid for the month-year
	 */
	default LocalDate date(int year, int month, int day) {
		try {
			return LocalDate.of(year, month, day);
		} catch ( DateTimeException e ) {
			throw new IllegalArgumentException(format("Cannot construct invalid date %d-%02d-%02d: %s",
					year, month, day, e.getMessage()));
		}
	}

	/**
	 * Create a {@link LocalDateTime} instance.
	 *
	 * @param year
	 *        the calendar year
	 * @param month
	 *        the calendar month (1-12)
	 * @param day
	 *        the calendar day-of-month (1-31)
	 * @param hour
	 *        the hour-of-day (0-23)
	 * @param minute
	 *        the minute-of-hour (0-59)
	 * @return the local date, not null
	 * @throws IllegalArgumentException
	 *         if the value of any field is out of range, or if the day-of-month
	 *         is invalid for the month-year
	 */
	default LocalDateTime date(int year, int month, int day, int hour, int minute) {
		try {
			return LocalDateTime.of(year, month, day, hour, minute);
		} catch ( DateTimeException e ) {
			throw new IllegalArgumentException(
					format("Cannot construct invalid date %d-%02d-%02d %02d:%02d: %s", year, month, day,
							hour, minute, e.getMessage()));
		}
	}

	/**
	 * Create a {@link LocalDateTime} instance.
	 *
	 * @param year
	 *        the calendar year
	 * @param month
	 *        the calendar month (1-12)
	 * @param day
	 *        the calendar day-of-month (1-31)
	 * @param hour
	 *        the hour-of-day (0-23)
	 * @param minute
	 *        the minute-of-hour (0-59)
	 * @param second
	 *        the second-of-minute (0-59)
	 * @return the local date, not null
	 * @throws IllegalArgumentException
	 *         if the value of any field is out of range, or if the day-of-month
	 *         is invalid for the month-year
	 */
	default LocalDateTime date(int year, int month, int day, int hour, int minute, int second) {
		try {
			return LocalDateTime.of(year, month, day, hour, minute, second);
		} catch ( DateTimeException e ) {
			throw new IllegalArgumentException(
					format("Cannot construct invalid date %d-%02d-%02d %02d:%02d:%02d: %s", year, month,
							day, hour, minute, e.getMessage()));
		}
	}

	/**
	 * Parse a {@link ChronoUnit} value.
	 *
	 * @param name
	 *        the value to case insensitively parse as a {@link ChronoUnit}
	 * @return the enum value
	 * @throws IllegalArgumentException
	 *         if {@code name} is not value, or {@literal null} if {@code name}
	 *         is {@literal null}
	 */
	default TemporalUnit chronoUnit(String name) {
		return DateUtils.chronoUnit(name);
	}

	/**
	 * Parse a temporal amount (period or duration).
	 *
	 * @param amount
	 *        the amount to add, in a form suitable for
	 *        {@link Period#parse(CharSequence)} or
	 *        {@link Duration#parse(CharSequence)}
	 * @return the parsed amount
	 * @throws IllegalArgumentException
	 *         if {@code amount} cannot be parsed as a {@link TemporalAmount}
	 */
	default TemporalAmount duration(String amount) {
		return DateUtils.duration(amount);
	}

	/**
	 * Truncate a date to a given unit.
	 *
	 * @param date
	 *        the date to truncate
	 * @param unit
	 *        the unit to truncate to
	 * @return the truncated date
	 * @throws IllegalArgumentException
	 *         if the date cannot be truncated to the given unit, or the unit
	 *         cannot be parsed
	 * @see #chronoUnit(String)
	 * @see #dateTruncate(Temporal, TemporalUnit)
	 */
	default Temporal dateTruncate(Temporal date, String unit) {
		return DateUtils.dateTruncate(date, unit);
	}

	/**
	 * Truncate a date to a given unit.
	 *
	 * @param date
	 *        the date to truncate
	 * @param unit
	 *        the unit to truncate to
	 * @return the truncated date
	 * @throws IllegalArgumentException
	 *         if the date cannot be truncated to the given unit
	 */
	default Temporal dateTruncate(Temporal date, TemporalUnit unit) {
		return DateUtils.dateTruncate(date, unit);
	}

	/**
	 * Round a date down by a given duration in the system default time zone.
	 *
	 * @param date
	 *        the date to round down (backwards)
	 * @param duration
	 *        the period to floor the date within; must be a value supported by
	 *        {@link #duration(String)}
	 * @return the floored date
	 * @throws IllegalArgumentException
	 *         if the date cannot be floored to the given duration, or the
	 *         duration cannot be parsed
	 * @see #duration(String)
	 * @see #dateFloor(Temporal, TemporalAmount, ZoneId)
	 * @since 1.4
	 */
	default Temporal dateFloor(Temporal date, String duration) {
		return dateFloor(date, duration(duration), ZoneId.systemDefault());
	}

	/**
	 * Round a date down by a given duration in the system default time zone.
	 *
	 * @param date
	 *        the date to round down (backwards)
	 * @param duration
	 *        the amount to floor the date within
	 * @param zone
	 *        the zone to use for {@link Period} amounts
	 * @return the floored date
	 * @throws IllegalArgumentException
	 *         if the date cannot be floored to the given duration
	 * @since 1.4
	 */
	default Temporal dateFloor(Temporal date, TemporalAmount duration) {
		return DateUtils.dateFloor(date, duration, ZoneId.systemDefault());
	}

	/**
	 * Round a date down by a given duration.
	 *
	 * @param date
	 *        the date to round down (backwards)
	 * @param duration
	 *        the period to floor the date within; must be a value supported by
	 *        {@link #duration(String)}
	 * @param zoneId
	 *        the zone IDto use for {@link Period} amounts; must be supported by
	 *        {@link #tz(String)}
	 * @return the floored date
	 * @throws IllegalArgumentException
	 *         if the date cannot be floored to the given duration, or the
	 *         duration cannot be parsed
	 * @see #tz(String)
	 * @see #duration(String)
	 * @see #dateFloor(Temporal, TemporalAmount ZoneId)
	 * @since 1.4
	 */
	default Temporal dateFloor(Temporal date, String duration, String zoneId) {
		return dateFloor(date, duration(duration), tz(zoneId));
	}

	/**
	 * Round a date down by a given duration.
	 *
	 * @param date
	 *        the date to round down (backwards)
	 * @param duration
	 *        the period to floor the date within; must be a value supported by
	 *        {@link #duration(String)}
	 * @param zone
	 *        the zone to use for {@link Period} amounts
	 * @return the floored date
	 * @throws IllegalArgumentException
	 *         if the date cannot be floored to the given duration, or the
	 *         duration cannot be parsed
	 * @see #duration(String)
	 * @see #dateFloor(Temporal, TemporalAmount, ZoneId)
	 * @since 1.4
	 */
	default Temporal dateFloor(Temporal date, String duration, ZoneId zone) {
		return dateFloor(date, duration(duration), zone);
	}

	/**
	 * Round a date down by a given duration.
	 *
	 * @param date
	 *        the date to round down (backwards)
	 * @param duration
	 *        the period to floor the date within
	 * @param zoneId
	 *        the zone IDto use for {@link Period} amounts; must be supported by
	 *        {@link #tz(String)}
	 * @return the floored date
	 * @throws IllegalArgumentException
	 *         if the date cannot be floored to the given duration, or the
	 *         duration cannot be parsed
	 * @see #tz(String)
	 * @see #dateFloor(Temporal, TemporalAmount, ZoneId)
	 * @since 1.4
	 */
	default Temporal dateFloor(Temporal date, TemporalAmount duration, String zoneId) {
		return dateFloor(date, duration, tz(zoneId));
	}

	/**
	 * Round a date down by a given duration.
	 *
	 * @param date
	 *        the date to round down (backwards)
	 * @param duration
	 *        the amount to floor the date within
	 * @param zone
	 *        the zone to use for {@link Period} amounts
	 * @return the floored date
	 * @throws IllegalArgumentException
	 *         if the date cannot be floored to the given duration
	 * @since 1.4
	 */
	default Temporal dateFloor(Temporal date, TemporalAmount duration, ZoneId zone) {
		return DateUtils.dateFloor(date, duration, zone);
	}

	/**
	 * Create a {@link ZonedDateTime} from a date in the system time zone.
	 *
	 * @param date
	 *        the date
	 * @return the zoned date, or {@literal null} if {@code date} is
	 *         {@literal null}
	 */
	default ZonedDateTime dateTz(LocalDate date) {
		return dateTz(date, ZoneId.systemDefault());
	}

	/**
	 * Create a {@link ZonedDateTime} from a date.
	 *
	 * @param date
	 *        the date
	 * @param zoneId
	 *        the time zone
	 * @return the zoned date, or {@literal null} if {@code date} is
	 *         {@literal null}
	 * @throws IllegalArgumentException
	 *         if {@code zoneId} is not valid
	 */
	default ZonedDateTime dateTz(LocalDate date, String zoneId) {
		return dateTz(date, tz(zoneId));
	}

	/**
	 * Create a {@link ZonedDateTime} from a date.
	 *
	 * @param date
	 *        the date
	 * @param zone
	 *        the time zone, or {@literal null} to use the system time zone
	 * @return the zoned date, or {@literal null} if {@code date} is
	 *         {@literal null}
	 */
	default ZonedDateTime dateTz(LocalDate date, ZoneId zone) {
		if ( date == null ) {
			return null;
		}
		if ( zone == null ) {
			zone = ZoneId.systemDefault();
		}
		return date.atStartOfDay(zone);
	}

	/**
	 * Create a {@link ZonedDateTime} from a date in the system time zone.
	 *
	 * @param date
	 *        the date
	 * @return the zoned date, or {@literal null} if {@code date} is
	 *         {@literal null}
	 */
	default ZonedDateTime dateTz(LocalDateTime date) {
		if ( date == null ) {
			return null;
		}
		return date.atZone(ZoneId.systemDefault());
	}

	/**
	 * Create a {@link ZonedDateTime} from a date.
	 *
	 * @param date
	 *        the date
	 * @param zoneId
	 *        the time zone
	 * @return the zoned date, or {@literal null} if {@code date} is
	 *         {@literal null}
	 * @throws IllegalArgumentException
	 *         if {@code zoneId} is not valid
	 */
	default ZonedDateTime dateTz(LocalDateTime date, String zoneId) {
		return dateTz(date, tz(zoneId));
	}

	/**
	 * Create a {@link ZonedDateTime} from a date.
	 *
	 * @param date
	 *        the date
	 * @param zone
	 *        the time zone, or {@literal null} to use the system time zone
	 * @return the zoned date, or {@literal null} if {@code date} is
	 *         {@literal null}
	 */
	default ZonedDateTime dateTz(LocalDateTime date, ZoneId zone) {
		if ( date == null ) {
			return null;
		}
		if ( zone == null ) {
			zone = ZoneId.systemDefault();
		}
		return date.atZone(zone);
	}

	/**
	 * Create a {@link ZonedDateTime} from a millisecond epoch date for the
	 * system time zone.
	 *
	 * @param date
	 *        the date
	 * @return the zoned date, or {@literal null} if {@code date} is
	 *         {@literal null}
	 * @throws IllegalArgumentException
	 *         if {@code zoneId} is not valid
	 * @since 1.4
	 */
	default ZonedDateTime dateTz(Long date) {
		return dateTz(timestamp(date), ZoneId.systemDefault());
	}

	/**
	 * Create a {@link ZonedDateTime} from a millisecond epoch date.
	 *
	 * @param date
	 *        the date
	 * @param zoneId
	 *        the time zone
	 * @return the zoned date, or {@literal null} if {@code date} is
	 *         {@literal null}
	 * @throws IllegalArgumentException
	 *         if {@code zoneId} is not valid
	 * @since 1.4
	 */
	default ZonedDateTime dateTz(Long date, String zoneId) {
		return dateTz(timestamp(date), tz(zoneId));
	}

	/**
	 * Create a {@link ZonedDateTime} from a millisecond epoch date.
	 *
	 * @param date
	 *        the date
	 * @param zone
	 *        the time zone, or {@literal null} to use the system time zone
	 * @return the zoned date, or {@literal null} if {@code date} is
	 *         {@literal null}
	 * @since 1.4
	 */
	default ZonedDateTime dateTz(Long date, ZoneId zone) {
		return dateTz(timestamp(date), zone);
	}

	/**
	 * Create a {@link ZonedDateTime} in the system time zone from an
	 * {@link Instant}.
	 *
	 * @param date
	 *        the date
	 * @return the zoned date, or {@literal null} if {@code date} is
	 *         {@literal null}
	 * @throws IllegalArgumentException
	 *         if {@code zoneId} is not valid
	 * @since 1.4
	 */
	default ZonedDateTime dateTz(Instant date) {
		return dateTz(date, ZoneId.systemDefault());
	}

	/**
	 * Create a {@link ZonedDateTime} from an {@link Instant}.
	 *
	 * @param date
	 *        the date
	 * @param zoneId
	 *        the time zone
	 * @return the zoned date, or {@literal null} if {@code date} is
	 *         {@literal null}
	 * @throws IllegalArgumentException
	 *         if {@code zoneId} is not valid
	 * @since 1.4
	 */
	default ZonedDateTime dateTz(Instant date, String zoneId) {
		return dateTz(date, tz(zoneId));
	}

	/**
	 * Create a {@link ZonedDateTime} from an {@link Instant}.
	 *
	 * @param date
	 *        the date
	 * @param zone
	 *        the time zone, or {@literal null} to use the system time zone
	 * @return the zoned date, or {@literal null} if {@code date} is
	 *         {@literal null}
	 * @since 1.4
	 */
	default ZonedDateTime dateTz(Instant date, ZoneId zone) {
		if ( date == null ) {
			return null;
		}
		if ( zone == null ) {
			zone = ZoneId.systemDefault();
		}
		return date.atZone(zone);
	}

	/**
	 * Add an ISO 8601 period or duration to a date.
	 *
	 * @param date
	 *        the date to add to
	 * @param amount
	 *        the amount to add, in a form suitable for
	 *        {@link Period#parse(CharSequence)} or
	 *        {@link Duration#parse(CharSequence)}
	 * @return the new adjusted date
	 * @throws IllegalArgumentException
	 *         if {@code amount} cannot be parsed as a {@link TemporalAmount} or
	 *         {@code amount} cannot be added to {@code date}
	 * @see #duration(String)
	 */
	default Temporal datePlus(Temporal date, String amount) {
		return DateUtils.datePlus(date, amount);
	}

	/**
	 * Add an ISO 8601 period or duration to a date.
	 *
	 * @param date
	 *        the date to add to
	 * @param amount
	 *        the amount to add
	 * @return the new adjusted date
	 * @throws IllegalArgumentException
	 *         if {@code amount} cannot be added to {@code date}
	 * @see #duration(String)
	 */
	default Temporal datePlus(Temporal date, TemporalAmount amount) {
		return DateUtils.datePlus(date, amount);
	}

	/**
	 * Add a chronological unit to a date.
	 *
	 * @param date
	 *        the date to add to
	 * @param amount
	 *        the amount to add
	 * @param unit
	 *        a {@link ChronoUnit} value for the time unit (case insensitive)
	 * @return the new adjusted date
	 * @throws IllegalArgumentException
	 *         if {@code unit} cannot be parsed as a {@link ChronoUnit}
	 */
	default Temporal datePlus(Temporal date, long amount, String unit) {
		return DateUtils.datePlus(date, amount, unit);
	}

	/**
	 * Add a chronological unit to a date.
	 *
	 * @param date
	 *        the date to add to
	 * @param amount
	 *        the amount to add
	 * @param unit
	 *        the unit
	 * @return the new adjusted date, or {@literal null} if {@code date} or
	 *         {@code unit} are {@literal null}
	 * @throws IllegalArgumentException
	 *         if {@code unit} cannot be added to {@code date}
	 */
	default Temporal datePlus(Temporal date, long amount, TemporalUnit unit) {
		return DateUtils.datePlus(date, amount, unit);
	}

	/**
	 * Parse a {@code YYYY-MM-DD} string into a local date object.
	 *
	 * @param value
	 *        the date string to parse, in {@code YYYY-MM-DD} form
	 * @return the parsed date, or {@literal null} if {@code value} is
	 *         {@code null} or empty
	 * @throws IllegalArgumentException
	 *         if {@code value} cannot be parsed
	 * @since 1.2
	 */
	default LocalDate date(String value) {
		if ( value == null || value.isEmpty() ) {
			return null;
		}
		try {
			return DateUtils.parseLocalDate(value);
		} catch ( DateTimeException e ) {
			throw new IllegalArgumentException(
					format("Cannot parse date from [%s], should be in YYYY-MM-DD form: %s", value,
							e.getMessage()));
		}
	}

	/**
	 * Parse a {@code HH:mm} string into a local time object.
	 *
	 * @param value
	 *        the time string to parse, in {@code HH:mm} form
	 * @return the parsed time, or {@literal null} if {@code value} is
	 *         {@code null} or empty
	 * @throws IllegalArgumentException
	 *         if {@code value} cannot be parsed
	 * @since 1.2
	 */
	default LocalTime time(String value) {
		if ( value == null || value.isEmpty() ) {
			return null;
		}
		try {
			return DateUtils.parseLocalTime(value);
		} catch ( DateTimeException e ) {
			throw new IllegalArgumentException(format(
					"Cannot parse time from [%s], should be in HH:mm form: %s", value, e.getMessage()));
		}
	}

	/**
	 * Parse an ISO-8601 timestamp.
	 *
	 * <p>
	 * This method handles input values that both include or omit a time zone
	 * offset. If a time zone offset is not provided in the input, then
	 * {@code defaultZone} will be used to get a final result.
	 * </p>
	 *
	 * @param value
	 *        the timestamp string to parse, in ISO-8601 form
	 * @return the parsed time, or {@literal null} if {@code value} is
	 *         {@code null} or empty
	 * @throws IllegalArgumentException
	 *         if {@code value} cannot be parsed
	 * @since 1.2
	 */
	default Instant timestamp(String value) {
		if ( value == null || value.isEmpty() ) {
			return null;
		}
		try {
			ZonedDateTime dt = DateUtils.parseIsoTimestamp(value, ZoneId.systemDefault());
			if ( dt == null ) {
				throw new DateTimeException("Failed to parse timestamp.");
			}
			return dt.toInstant();
		} catch ( DateTimeException e ) {
			throw new IllegalArgumentException(
					format("Cannot parse timestamp from [%s], should be in YYYY-MM-DDTHH:mm:ss form: %s",
							value, e.getMessage()));
		}
	}

	/**
	 * Calculate the duration between two dates.
	 *
	 * @param date1
	 *        the first date
	 * @param date2
	 *        the second date
	 * @return the duration between the two dates, or {@code null} if either
	 *         argument is {@code null}
	 * @throws IllegalArgumentException
	 *         if the duration cannot be calculated
	 * @since 1.2
	 */
	default Duration durationBetween(Temporal date1, Temporal date2) {
		if ( date1 == null || date2 == null ) {
			return null;
		}
		try {
			return Duration.between(date1, date2);
		} catch ( DateTimeException e ) {
			throw new IllegalArgumentException(
					format("Cannot calculate the duration between [%s] and [%s]: %s", date1, date2,
							e.getMessage()));
		}
	}

	/**
	 * Calculate an amount of a given {@code ChronoUnit} between two dates.
	 *
	 * @param unit
	 *        the desired unit
	 * @param date1
	 *        the first date
	 * @param date2
	 *        the second date
	 * @return the number of {@code unit} units between the two dates, or
	 *         {@code 0} if either argument is {@code null}
	 * @throws IllegalArgumentException
	 *         if the duration cannot be calculated
	 * @since 1.2
	 */
	static long between(ChronoUnit unit, Temporal date1, Temporal date2) {
		if ( date1 == null || date2 == null ) {
			return 0;
		}
		if ( unit.isTimeBased() && date1 instanceof LocalDate ) {
			// try converting to something with time, at start of day
			date1 = ((LocalDate) date1).atStartOfDay();
		}
		if ( unit.isTimeBased() && date2 instanceof LocalDate ) {
			// try converting to something with time, at start of day
			date2 = ((LocalDate) date2).atStartOfDay();
		}
		try {
			return unit.between(date1, date2);
		} catch ( DateTimeException e ) {
			throw new IllegalArgumentException(
					format("Cannot calculate number of %s between [%s] and [%s]: %s", unit, date1, date2,
							e.getMessage()));
		}
	}

	/**
	 * Calculate the number of seconds between two dates.
	 *
	 * @param date1
	 *        the first date
	 * @param date2
	 *        the second date
	 * @return the number of seconds between the two dates, or {@code 0} if
	 *         either argument is {@code null}
	 * @throws IllegalArgumentException
	 *         if the duration cannot be calculated
	 * @since 1.2
	 */
	default long secondsBetween(Temporal date1, Temporal date2) {
		return between(ChronoUnit.SECONDS, date1, date2);
	}

	/**
	 * Calculate the number of minutes between two dates.
	 *
	 * @param date1
	 *        the first date
	 * @param date2
	 *        the second date
	 * @return the number of minutes between the two dates, or {@code 0} if
	 *         either argument is {@code null}
	 * @throws IllegalArgumentException
	 *         if the duration cannot be calculated
	 * @since 1.2
	 */
	default long minutesBetween(Temporal date1, Temporal date2) {
		return between(ChronoUnit.MINUTES, date1, date2);
	}

	/**
	 * Calculate the number of hours between two dates.
	 *
	 * @param date1
	 *        the first date
	 * @param date2
	 *        the second date
	 * @return the number of hours between the two dates, or {@code 0} if either
	 *         argument is {@code null}
	 * @throws IllegalArgumentException
	 *         if the duration cannot be calculated
	 * @since 1.2
	 */
	default long hoursBetween(Temporal date1, Temporal date2) {
		return between(ChronoUnit.HOURS, date1, date2);
	}

	/**
	 * Calculate the number of days between two dates.
	 *
	 * @param date1
	 *        the first date
	 * @param date2
	 *        the second date
	 * @return the number of days between the two dates, or {@code 0} if either
	 *         argument is {@code null}
	 * @throws IllegalArgumentException
	 *         if the duration cannot be calculated
	 * @since 1.2
	 */
	default long daysBetween(Temporal date1, Temporal date2) {
		return between(ChronoUnit.DAYS, date1, date2);
	}

	/**
	 * Calculate the fractional number of months between two dates, with second
	 * resolution.
	 *
	 * @param date1
	 *        the first date
	 * @param date2
	 *        the second date
	 * @return the number of months between the two dates, or {@code 0} if
	 *         either argument is {@code null}
	 * @throws IllegalArgumentException
	 *         if the duration cannot be calculated
	 * @since 1.2
	 */
	@SuppressWarnings("unchecked")
	default double monthsBetween(Temporal date1, Temporal date2) {
		if ( date1 == null || date2 == null ) {
			return 0.0;
		}

		long sign = 1;
		double f1 = 0.0;
		long wholeMonths = 0;
		double f2 = 0.0;

		if ( ((Comparable<Temporal>) date1).compareTo(date2) > 0 ) {
			// swap date order and negate result
			Temporal t = date1;
			date1 = date2;
			date2 = t;
			sign = -1;
		}

		// calculate starting fraction of first start month
		Temporal date1NextMonthStart = date1.with(TemporalAdjusters.firstDayOfNextMonth());

		if ( ((Comparable<Temporal>) date1NextMonthStart).compareTo(date2) > 0 ) {
			// within a single month
			f1 = (double) between(ChronoUnit.SECONDS, date1, date2)
					/ (double) between(ChronoUnit.SECONDS,
							date1.with(TemporalAdjusters.firstDayOfMonth()), date1NextMonthStart);
		} else {
			f1 = (double) between(ChronoUnit.SECONDS, date1, date1NextMonthStart)
					/ (double) between(ChronoUnit.SECONDS,
							date1.with(TemporalAdjusters.firstDayOfMonth()), date1NextMonthStart);

			Temporal date2MonthStart = date2.with(TemporalAdjusters.firstDayOfMonth());

			// calculate whole months between month boundaries
			wholeMonths = Math.max(date1NextMonthStart.until(date2MonthStart, ChronoUnit.MONTHS), 0);

			f2 = (double) between(ChronoUnit.SECONDS, date2MonthStart, date2)
					/ (double) between(ChronoUnit.SECONDS, date2MonthStart,
							date2MonthStart.with(TemporalAdjusters.firstDayOfNextMonth()));
		}

		return sign * (f1 + wholeMonths + f2);
	}

	/**
	 * Calculate the fractional number of months between two dates, with second
	 * resolution.
	 *
	 * @param date1
	 *        the first date
	 * @param date2
	 *        the second date
	 * @return the number of months between the two dates, or {@code 0} if
	 *         either argument is {@code null}
	 * @throws IllegalArgumentException
	 *         if the duration cannot be calculated
	 * @since 1.2
	 */
	@SuppressWarnings("unchecked")
	default double yearsBetween(Temporal date1, Temporal date2) {
		if ( date1 == null || date2 == null ) {
			return 0.0;
		}

		long sign = 1;
		double f1 = 0.0;
		long wholeYears = 0;
		double f2 = 0.0;

		if ( ((Comparable<Temporal>) date1).compareTo(date2) > 0 ) {
			// swap date order and negate result
			Temporal t = date1;
			date1 = date2;
			date2 = t;
			sign = -1;
		}

		// calculate starting fraction of first start month
		Temporal date1NextYearStart = date1.with(TemporalAdjusters.firstDayOfNextYear());

		if ( ((Comparable<Temporal>) date1NextYearStart).compareTo(date2) > 0 ) {
			// within a single month
			f1 = (double) between(ChronoUnit.SECONDS, date1, date2)
					/ (double) between(ChronoUnit.SECONDS,
							date1.with(TemporalAdjusters.firstDayOfYear()), date1NextYearStart);
		} else {
			f1 = (double) between(ChronoUnit.SECONDS, date1, date1NextYearStart)
					/ (double) between(ChronoUnit.SECONDS,
							date1.with(TemporalAdjusters.firstDayOfYear()), date1NextYearStart);

			Temporal date2YearStart = date2.with(TemporalAdjusters.firstDayOfYear());

			// calculate whole months between month boundaries
			wholeYears = Math.max(date1NextYearStart.until(date2YearStart, ChronoUnit.YEARS), 0);

			f2 = (double) between(ChronoUnit.SECONDS, date2YearStart, date2)
					/ (double) between(ChronoUnit.SECONDS, date2YearStart,
							date2YearStart.with(TemporalAdjusters.firstDayOfNextYear()));
		}

		return sign * (f1 + wholeYears + f2);
	}

	/**
	 * Format a date using a pattern and the default system time zone.
	 *
	 * @param date
	 *        the date to format
	 * @param pattern
	 *        the pattern, as supported by
	 *        {@link DateTimeFormatterBuilder#appendPattern(String)}
	 * @return the formatted date
	 * @throws IllegalArgumentException
	 *         if any error occurs formatting the date
	 * @since 1.3
	 * @see #formatDate(Temporal, String, ZoneId)
	 */
	default String formatDate(Temporal date, String pattern) {
		return formatDate(date, pattern, null);
	}

	/**
	 * Format a date using a pattern and optional time zone.
	 *
	 * @param date
	 *        the date to format
	 * @param pattern
	 *        the pattern, as supported by
	 *        {@link DateTimeFormatterBuilder#appendPattern(String)}
	 * @param zone
	 *        the time zone to use, or {@code null} to use the default system
	 *        time zone
	 * @return the formatted date
	 * @throws IllegalArgumentException
	 *         if any error occurs formatting the date
	 * @since 1.3
	 */
	default String formatDate(Temporal date, String pattern, ZoneId zone) {
		if ( date == null || pattern == null || pattern.isEmpty() ) {
			return null;
		}
		DateTimeFormatter fmt = DateTimeFormatter.ofPattern(pattern)
				.withChronology(IsoChronology.INSTANCE);
		fmt = fmt.withZone(zone != null ? zone : ZoneId.systemDefault());
		try {
			return fmt.format(date);
		} catch ( DateTimeException e ) {
			throw new IllegalArgumentException(
					format("Cannot format date [%s] with pattern [%s] and zone [%s]: %s", date, pattern,
							zone, e.getMessage()));
		}
	}

}
