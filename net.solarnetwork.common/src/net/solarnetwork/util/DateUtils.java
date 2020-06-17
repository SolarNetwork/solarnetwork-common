/* ==================================================================
 * DateUtils.java - 12/02/2020 7:04:10 am
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

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;

/**
 * Date and time utilities.
 * 
 * @author matt
 * @version 1.1
 * @since 1.59
 */
public final class DateUtils {

	/**
	 * Date and time formatter using the ISO 8601 style but with an optional
	 * time component and a space character for the date/time separator instead
	 * of {@literal T}.
	 * 
	 * <p>
	 * This supports patterns like:
	 * </p>
	 * <ul>
	 * <li>{@literal 2020-02-01 20:12:34+12:00}</li>
	 * <li>{@literal 2020-02-01 20:12:34}</li>
	 * <li>{@literal 2020-02-01 20:12}</li>
	 * <li>{@literal 2020-02-01+12:00}</li>
	 * <li>{@literal 2020-02-01}</li>
	 * </ul>
	 * 
	 * <p>
	 * Note that parsing the date + zone form like {@literal 2020-02-01+12:00}
	 * requires using a {@link java.time.temporal.TemporalAccessor}, like:
	 * </p>
	 * 
	 * <pre>
	 * <code>TemporalAccessor ta = DateUtils.ISO_DATE_OPT_TIME_ALT.parse("2020-02-01+12:00");
	 * ZonedDateTime ts = LocalDate.from(ta).atStartOfDay(ZoneId.from(ta));</code>
	 * </pre>
	 */
	public static final DateTimeFormatter ISO_DATE_OPT_TIME_ALT;
	static {
		// @formatter:off
		ISO_DATE_OPT_TIME_ALT = new DateTimeFormatterBuilder()
				.append(DateTimeFormatter.ISO_DATE)
				.optionalStart()
				.appendLiteral(' ')
				.append(DateTimeFormatter.ISO_TIME)
				.toFormatter();
		// @formatter:on
	}

	/**
	 * Date and time formatter based on {@link #ISO_DATE_OPT_TIME_ALT} with a
	 * UTC time zone offset applied.
	 */
	public static final DateTimeFormatter ISO_DATE_OPT_TIME_ALT_UTC = ISO_DATE_OPT_TIME_ALT
			.withZone(ZoneOffset.UTC);

	/**
	 * Date and time formatter based on {@link #ISO_DATE_OPT_TIME_ALT} with the
	 * local system default time zone applied.
	 */
	public static final DateTimeFormatter ISO_DATE_OPT_TIME_ALT_LOCAL = ISO_DATE_OPT_TIME_ALT
			.withZone(ZoneId.systemDefault());

	/**
	 * Date and time formatter using the ISO 8601 style but with a space
	 * character for the date/time separator instead of {@literal T}.
	 * 
	 * <p>
	 * This supports patterns like:
	 * </p>
	 * <ul>
	 * <li>{@literal 2020-02-01 20:12:34+12:00}</li>
	 * <li>{@literal 2020-02-01 20:12:34}</li>
	 * <li>{@literal 2020-02-01 20:12}</li>
	 * </ul>
	 * 
	 * @since 1.1
	 */
	public static final DateTimeFormatter ISO_DATE_TIME_ALT;
	static {
		// @formatter:off
		ISO_DATE_TIME_ALT = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .append(DateTimeFormatter.ISO_LOCAL_DATE)
				.appendLiteral(' ')
				.append(DateTimeFormatter.ISO_TIME)
				.toFormatter();
		// @formatter:on
	}

	/**
	 * Date and time formatter based on {@link #ISO_DATE_TIME_ALT} with a UTC
	 * time zone offset applied.
	 * 
	 * @since 1.1
	 */
	public static final DateTimeFormatter ISO_DATE_TIME_ALT_UTC = ISO_DATE_TIME_ALT
			.withZone(ZoneOffset.UTC);

	/**
	 * Parse an ISO-8601 alternate timestamp using a given formatter.
	 * 
	 * <p>
	 * This method handles input values that both include or omit a time zone
	 * offset. If a time zone offset is not provided in the input, then
	 * {@code defaultZone} will be used to get a final result.
	 * </p>
	 * 
	 * @param value
	 *        the date time string to parse
	 * @param defaultZone
	 *        a default time zone to use if one is not available in
	 *        {@code value}
	 * @return the parsed date, or {@literal null} if it cannot be parsed for
	 *         any reason
	 */
	public static ZonedDateTime parseIsoAltTimestamp(final String value, final ZoneId defaultZone) {
		ZonedDateTime result = null;
		try {
			// try with full time zone from value first
			result = ISO_DATE_OPT_TIME_ALT.parse(value, ZonedDateTime::from);
		} catch ( DateTimeException e ) {
			// try date + zone approach
			try {
				TemporalAccessor ta = ISO_DATE_OPT_TIME_ALT.parse(value);
				ZoneId zone = ta.query(TemporalQueries.zone());
				if ( zone == null ) {
					zone = defaultZone;
				}
				try {
					result = LocalDateTime.from(ta).atZone(zone);
				} catch ( DateTimeException e3 ) {
					try {
						result = LocalDate.from(ta).atStartOfDay(zone);
					} catch ( DateTimeException e4 ) {
						// :-(
					}
				}
			} catch ( DateTimeException e2 ) {
				// nadda
			}
		}
		return result;
	}

}
