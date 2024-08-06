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
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;

/**
 * API for datum-related date helper functions.
 *
 * @author matt
 * @version 1.0
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
		if ( zoneId == null ) {
			return ZoneId.systemDefault();
		}
		try {
			return ZoneId.of(zoneId);
		} catch ( DateTimeException e ) {
			throw new IllegalArgumentException(
					format("Invalid time zone [%s]: %s", zoneId, e.getMessage()));
		}
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
	 * Get the date and time right now as an {@link Instant}.
	 *
	 * @return the instant
	 */
	default Instant timestamp() {
		return Instant.now();
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
		return timestamp(date, ZoneId.systemDefault());
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
		return timestamp(date, tz(zoneId));
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
		if ( date == null ) {
			return null;
		}
		if ( zone == null ) {
			zone = ZoneId.systemDefault();
		}
		if ( date instanceof Instant ) {
			return (Instant) date;
		}
		if ( date instanceof LocalDate ) {
			return ((LocalDate) date).atStartOfDay().atZone(zone).toInstant();
		} else if ( date instanceof LocalDateTime ) {
			return ((LocalDateTime) date).atZone(zone).toInstant();
		} else if ( date instanceof ZonedDateTime ) {
			return ((ZonedDateTime) date).toInstant();
		} else if ( date instanceof OffsetDateTime ) {
			return ((OffsetDateTime) date).toInstant();
		} else if ( date instanceof YearMonth ) {
			return ((YearMonth) date).atDay(1).atStartOfDay().atZone(zone).toInstant();
		} else if ( date instanceof Year ) {
			return ((Year) date).atMonth(1).atDay(1).atStartOfDay().atZone(zone).toInstant();
		}
		throw new IllegalArgumentException("Unsupported temporal type [" + date.getClass() + "]");
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
		if ( name == null ) {
			return null;
		}
		try {
			return ChronoUnit.valueOf(name.toUpperCase());
		} catch ( IllegalArgumentException e ) {
			throw new IllegalArgumentException(format("Invalid chrono unit [%s].", name));
		}
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
		TemporalAmount amt = null;
		try {
			amt = Period.parse(amount);
		} catch ( DateTimeParseException e ) {
			// ignore, try duration
			try {
				amt = Duration.parse(amount);
			} catch ( DateTimeParseException e2 ) {
				throw new IllegalArgumentException(
						"Unable to parse [" + amount + "] as a ISO 8601 period or duration value.");
			}
		}
		return amt;
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
		return dateTruncate(date, chronoUnit(unit));
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
		if ( date == null || unit == null ) {
			return date;
		}
		try {
			if ( date instanceof ZonedDateTime ) {
				return ((ZonedDateTime) date).truncatedTo(unit);
			} else if ( date instanceof Instant ) {
				return ((Instant) date).truncatedTo(unit);
			} else if ( date instanceof OffsetDateTime ) {
				return ((OffsetDateTime) date).truncatedTo(unit);
			} else if ( date instanceof LocalDateTime ) {
				return ((LocalDateTime) date).truncatedTo(unit);
			} else if ( date instanceof LocalDate ) {
				if ( unit.equals(ChronoUnit.MONTHS) ) {
					return ((LocalDate) date).with(TemporalAdjusters.firstDayOfMonth());
				} else if ( unit.equals(ChronoUnit.YEARS) ) {
					return ((LocalDate) date).with(TemporalAdjusters.firstDayOfYear());
				}
			}
		} catch ( DateTimeException e ) {
			throw new IllegalArgumentException(
					format("Cannot truncate date %s to %s: %s", date, unit, e.getMessage()));
		}
		throw new IllegalArgumentException(
				format("Cannot trundate date type %s to %s: unsupported type.", date.getClass(), unit));
	}

	/**
	 * Create an {@link ZonedDateTime} from a date in the system time zone.
	 *
	 * @param date
	 *        the date
	 * @return the zoned date, or {@literal null} if {@code date} is
	 *         {@literal null}
	 */
	default ZonedDateTime dateTz(LocalDate date) {
		if ( date == null ) {
			return null;
		}
		return date.atStartOfDay(ZoneId.systemDefault());
	}

	/**
	 * Create an {@link Instant} from a date.
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
	 * Create an {@link Instant} from a date.
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
	 * Create an {@link Instant} from a date in the system time zone.
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
	 * Create an {@link Instant} from a date.
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
	 * Create an {@link Instant} from a date.
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
		return datePlus(date, duration(amount));
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
		if ( date == null || amount == null ) {
			return date;
		}
		try {
			return date.plus(amount);
		} catch ( DateTimeException e ) {
			throw new IllegalArgumentException(
					format("Unable to add [%s] to [%s]: %s", amount, date, e.getMessage()));
		}
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
		return datePlus(date, amount, chronoUnit(unit));
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
		if ( date == null || unit == null || amount == 0 ) {
			return date;
		}
		try {
			return date.plus(amount, unit);
		} catch ( DateTimeException e ) {
			throw new IllegalArgumentException(
					format("Unable to add %d %s to [%s]: %s", unit, date, e.getMessage()));
		}
	}

}
