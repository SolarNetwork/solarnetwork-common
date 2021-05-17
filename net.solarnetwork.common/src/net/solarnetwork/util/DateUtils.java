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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQueries;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Date and time utilities.
 * 
 * @author matt
 * @version 1.2
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

	/**
	 * A range delimited pattern.
	 * 
	 * @since 1.2
	 */
	public static final Pattern RANGE_DELIMITER = Pattern.compile("\\s*-\\s*");

	private static String[] splitRange(String range) {
		if ( range == null ) {
			return null;
		}
		range = range.trim();
		Matcher m = RANGE_DELIMITER.matcher(range);
		if ( m.find() && m.end() < range.length() ) {
			String a = range.substring(0, m.start());
			String b = range.substring(m.end(), range.length());
			return new String[] { a, b };
		}
		return new String[] { range, range };
	}

	private static IntRange parseRange(String[] r, Locale locale, TemporalField field, TextStyle style)
			throws DateTimeException {
		DateTimeFormatter f = new DateTimeFormatterBuilder().parseCaseInsensitive().parseLenient()
				.appendText(field, style).toFormatter(locale);
		TemporalAccessor a = f.parse(r[0]);
		if ( r[0].equals(r[1]) ) {
			return IntRange.rangeOf(a.get(field));
		}
		TemporalAccessor b = f.parse(r[1]);
		return IntRange.rangeOf(a.get(field), b.get(field));
	}

	/**
	 * Parse a time range.
	 * 
	 * <p>
	 * The range can be specified using names, abbreviations, or numbers. The
	 * range of allowed numbers varies by field.
	 * </p>
	 * 
	 * @param field
	 *        the time field to parse
	 * @param range
	 *        the range string to parse into a time range
	 * @param locale
	 *        the locale to parse the range as
	 * @return the range
	 * @throws DateTimeException
	 *         if any parsing error occurs
	 * @since 1.2
	 */
	public static IntRange parseRange(TemporalField field, String range, Locale locale)
			throws DateTimeException {
		if ( range == null ) {
			return null;
		}
		if ( locale == null ) {
			locale = Locale.getDefault();
		}
		return parseRange(splitRange(range), locale, field, TextStyle.FULL);
	}

	/**
	 * Parse a month range.
	 * 
	 * <p>
	 * The range can be specified using month names, abbreviations, or numbers.
	 * Months are numbered from {@literal 1-12}.
	 * </p>
	 * 
	 * @param range
	 *        the range string to parse into a month range
	 * @param locale
	 *        the locale to parse the range as
	 * @return the range, with a minimum of {@literal 1} and maximum of
	 *         {@literal 12}
	 * @throws DateTimeException
	 *         if any parsing error occurs
	 * @since 1.2
	 */
	public static IntRange parseMonthRange(String range, Locale locale) throws DateTimeException {
		return parseRange(ChronoField.MONTH_OF_YEAR, range, locale);
	}

	/**
	 * Parse a day of month range.
	 * 
	 * <p>
	 * The range can be specified using day numbers. Days are numbered from
	 * {@literal 1-31}.
	 * </p>
	 * 
	 * @param range
	 *        the range string to parse into a day of month range
	 * @param locale
	 *        the locale to parse the range as
	 * @return the range, with a minimum of {@literal 1} and maximum of
	 *         {@literal 31}
	 * @throws DateTimeException
	 *         if any parsing error occurs
	 * @since 1.2
	 */
	public static IntRange parseDayOfMonthRange(String range, Locale locale) throws DateTimeException {
		return parseRange(ChronoField.DAY_OF_MONTH, range, locale);
	}

	/**
	 * Parse a day of week range.
	 * 
	 * <p>
	 * The range can be specified using weekday names, abbreviations, or
	 * numbers. Weekdays are numbered from {@literal 1-7} with Monday starting
	 * at {@literal 1}.
	 * </p>
	 * 
	 * @param range
	 *        the range string to parse into a day of week range
	 * @param locale
	 *        the locale to parse the range as
	 * @return the range, with a minimum of {@literal 1} and maximum of
	 *         {@literal 7}
	 * @throws DateTimeException
	 *         if any parsing error occurs
	 * @since 1.2
	 */
	public static IntRange parseDayOfWeekRange(String range, Locale locale) throws DateTimeException {
		return parseRange(ChronoField.DAY_OF_WEEK, range, locale);
	}

	/**
	 * Parse a hour of day range.
	 * 
	 * <p>
	 * The range can be specified using numbers. Hours of 0 - 24 are allowed.
	 * </p>
	 * 
	 * @param range
	 *        the range string to parse into a day of week range
	 * @param locale
	 *        the locale to parse the range as
	 * @return the range, with a minimum of {@literal 1} and maximum of
	 *         {@literal 24}
	 * @throws DateTimeException
	 *         if any parsing error occurs
	 * @since 1.2
	 */
	public static IntRange parseMinuteOfDayRange(String range, Locale locale) throws DateTimeException {
		return parseMinuteOfDayRange(range, locale, false);
	}

	/**
	 * Parse a minute of day range.
	 * 
	 * <p>
	 * The range can be specified using ISO local time strings or numbers. For
	 * strings, the format {@literal HH:MM} is used.
	 * </p>
	 * 
	 * <p>
	 * For numbers, the values are assumed to be whole hours and values between
	 * 0 - 24 are allowed.
	 * </p>
	 * 
	 * @param range
	 *        the range string to parse into a day of week range
	 * @param locale
	 *        the locale to parse the range as
	 * @param fix24
	 *        if {@literal true} then the value {@literal 24} will be changed to
	 *        {@code 23:59}, otherwise {@literal 24} will be left as-is
	 * @return the range, with a minimum of {@literal 0} and maximum of
	 *         {@literal 1439}
	 * @throws DateTimeException
	 *         if any parsing error occurs
	 * @since 1.2
	 */
	public static IntRange parseMinuteOfDayRange(String range, Locale locale, boolean fix24)
			throws DateTimeException {
		String[] r = splitRange(range);
		if ( r == null ) {
			return null;
		}
		int[] n = new int[2];
		try {
			for ( int i = 0; i < 2; i++ ) {
				String s = r[i];
				int h = 0;
				int m = 0;
				int idx = s.indexOf(':');
				if ( idx > 0 ) {
					h = Integer.parseInt(s.substring(0, idx));
					m = Integer.parseInt(s.substring(idx + 1));
				} else {
					h = Integer.parseInt(r[i]);
				}
				if ( h < 0 || h > 24 ) {
					throw new DateTimeException("Hour of day out of range: " + h);
				}
				if ( fix24 && h == 24 ) {
					h = 23;
					if ( m == 0 ) {
						m = 59;
					}
				}
				if ( m < 0 || m > 59 ) {
					throw new DateTimeException("Minute of hour out of range: " + m);
				}
				n[i] = h * 60 + m;
			}
		} catch ( NumberFormatException e ) {
			throw new DateTimeParseException("Invalid hour of day range", range, 0, e);
		}
		return IntRange.rangeOf(n[0], n[1]);
	}

	/** The number of standard minutes in a standard 24-hour day. */
	public static final int MINUTES_PER_DAY = 60 * 24;

	private static String formatMinuteOfDay(final int moh) {
		int h = moh / 60;
		int m = moh - h * 60;
		return String.format("%02d:%02d", h, m);
	}

	private static String formatRange(IntRange range, Locale locale, ChronoField field, TextStyle style)
			throws DateTimeException {
		StringBuilder buf = new StringBuilder();

		if ( field == ChronoField.MINUTE_OF_DAY ) {
			if ( range.getMin() < 0 || range.getMax() > MINUTES_PER_DAY ) {
				throw new DateTimeException(
						"The start minute of hour is out of range: " + range.getMin());
			} else if ( range.getMax() < 0 || range.getMax() > MINUTES_PER_DAY ) {
				throw new DateTimeException("The end minute of hour is out of range: " + range.getMax());
			}
			// manually handle this so we support both hour and HH:MM and allow 24
			if ( style == TextStyle.SHORT && range.getMin() % 60 == 0 && range.getMax() % 60 == 0 ) {
				// use hour-hour style
				buf.append(range.getMin() / 60);
				if ( range.getMax() != range.getMin() ) {
					buf.append('-').append(range.getMax() / 60);
				}
			} else {
				// use HH:MM
				buf.append(formatMinuteOfDay(range.getMin()));
				if ( range.getMax() != range.getMin() ) {
					buf.append('-').append(formatMinuteOfDay(range.getMax()));
				}
			}
		} else {
			DateTimeFormatter f = new DateTimeFormatterBuilder().appendText(field, style)
					.toFormatter(locale);
			LocalDateTime t = LocalDateTime.now();
			if ( field == ChronoField.DAY_OF_WEEK ) {
				t = t.with(TemporalAdjusters.nextOrSame(DayOfWeek.of(range.getMin())));
			} else {
				t = t.with(field, range.getMin());
			}

			f.formatTo(t, buf);
			if ( range.getMax() != range.getMin() ) {
				buf.append('-');
				if ( field == ChronoField.DAY_OF_WEEK ) {
					t = t.with(TemporalAdjusters.nextOrSame(DayOfWeek.of(range.getMax())));
				} else {
					t = t.with(field, range.getMax());
				}
				f.formatTo(t, buf);
			}
		}

		return buf.toString();
	}

	/**
	 * Format a time range.
	 * 
	 * <p>
	 * The range can be specified using names, abbreviations, or numbers. The
	 * range of allowed numbers varies by field.
	 * </p>
	 * 
	 * @param field
	 *        the time field to format
	 * @param range
	 *        the range to format into a string
	 * @param locale
	 *        the locale to format the range as, or {@literal null} to use the
	 *        system default
	 * @param style
	 *        the formatting style
	 * @return the range string
	 * @throws DateTimeException
	 *         if any formatting error occurs
	 * @since 1.2
	 */
	public static String formatRange(ChronoField field, IntRange range, Locale locale, TextStyle style)
			throws DateTimeException {
		if ( range == null ) {
			return null;
		}
		if ( locale == null ) {
			locale = Locale.getDefault();
		}
		return formatRange(range, locale, field, style);
	}

}
