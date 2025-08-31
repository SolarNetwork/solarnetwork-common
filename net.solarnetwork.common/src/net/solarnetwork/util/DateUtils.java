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

import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MILLI_OF_SECOND;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalUnit;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.util.StringUtils;

/**
 * Date and time utilities.
 *
 * @author matt
 * @version 2.7
 * @since 1.59
 */
public final class DateUtils {

	private DateUtils() {
		// not available
	}

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
				.parseDefaulting(ChronoField.ERA, ChronoField.ERA.range().getMaximum())
				.optionalStart()
				.appendLiteral(' ')
				.append(DateTimeFormatter.ISO_TIME)
				.toFormatter()
				.withChronology(IsoChronology.INSTANCE);
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
	 * Date and time formatter using the ISO 8601 style but with an optional
	 * time component.
	 *
	 * <p>
	 * This supports patterns like:
	 * </p>
	 * <ul>
	 * <li>{@literal 2020-02-01T20:12:34+12:00}</li>
	 * <li>{@literal 2020-02-01T20:12:34}</li>
	 * <li>{@literal 2020-02-01T20:12}</li>
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
	 * <code>TemporalAccessor ta = DateUtils.ISO_DATE_OPT_TIME.parse("2020-02-01+12:00");
	 * ZonedDateTime ts = LocalDate.from(ta).atStartOfDay(ZoneId.from(ta));</code>
	 * </pre>
	 *
	 * @since 2.2
	 */
	public static final DateTimeFormatter ISO_DATE_OPT_TIME;
	static {
		// @formatter:off
		ISO_DATE_OPT_TIME = new DateTimeFormatterBuilder()
				.append(DateTimeFormatter.ISO_DATE)
				.parseDefaulting(ChronoField.ERA, ChronoField.ERA.range().getMaximum())
				.optionalStart()
				.appendLiteral('T')
				.append(DateTimeFormatter.ISO_TIME)
				.toFormatter()
				.withChronology(IsoChronology.INSTANCE);
		// @formatter:on
	}

	/**
	 * Date and time formatter based on {@link #ISO_DATE_OPT_TIME} with a UTC
	 * time zone offset applied.
	 *
	 * @since 2.2
	 */
	public static final DateTimeFormatter ISO_DATE_OPT_TIME_UTC = ISO_DATE_OPT_TIME
			.withZone(ZoneOffset.UTC);

	/**
	 * Date and time formatter based on {@link #ISO_DATE_OPT_TIME} with the
	 * local system default time zone applied.
	 *
	 * @since 2.2
	 */
	public static final DateTimeFormatter ISO_DATE_OPT_TIME_LOCAL = ISO_DATE_OPT_TIME
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
				.parseDefaulting(ChronoField.ERA, ChronoField.ERA.range().getMaximum())
				.appendLiteral(' ')
				.append(DateTimeFormatter.ISO_TIME)
				.toFormatter()
				.withChronology(IsoChronology.INSTANCE);
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
	 * Local time formatted for standard local time values like
	 * {@literal HH:mm}.
	 *
	 * @since 1.4
	 * @see #LOCAL_TIME_FULL
	 */
	public static final DateTimeFormatter LOCAL_TIME;
	static {
		// @formatter:off
		LOCAL_TIME = new DateTimeFormatterBuilder()
				.appendValue(HOUR_OF_DAY, 2)
				.appendLiteral(':')
				.appendValue(MINUTE_OF_HOUR, 2)
				.toFormatter();
		// @formatter:on
	}

	/**
	 * Local time formatted for standard local time values like
	 * {@literal yyyy-MM-dd}.
	 *
	 * <p>
	 * This is just an alias for {@link DateTimeFormatter#ISO_LOCAL_DATE}.
	 * </p>
	 *
	 * @since 2.0
	 */
	public static final DateTimeFormatter LOCAL_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

	/**
	 * Local time formatted for standard local time values like
	 * {@literal HH:mm:ss}.
	 *
	 * <p>
	 * This is just an alias for {@link DateTimeFormatter#ISO_LOCAL_TIME}.
	 * </p>
	 *
	 * @since 2.0
	 * @see #LOCAL_TIME
	 */
	public static final DateTimeFormatter LOCAL_TIME_FULL = DateTimeFormatter.ISO_LOCAL_TIME;

	/**
	 * Format for a long date and short time, for display purposes.
	 *
	 * @since 1.5
	 */
	public static final DateTimeFormatter DISPLAY_DATE_LONG_TIME_SHORT = DateTimeFormatter
			.ofLocalizedDateTime(FormatStyle.LONG, FormatStyle.SHORT);

	/**
	 * Date and time formatter using the ISO 8601 style for local time with
	 * optional seconds, but with an optional milliseconds of second fraction
	 * component (instead of nanoseconds of second).
	 *
	 * <p>
	 * This supports patterns like:
	 * </p>
	 * <ul>
	 * <li>{@literal 20:12:34.567}</li>
	 * <li>{@literal 20:12:34}</li>
	 * <li>{@literal 20:12}</li>
	 * </ul>
	 *
	 * @since 2.0
	 */
	public static final DateTimeFormatter ISO_LOCAL_TIME_OPT_MILLIS;
	static {
		// @formatter:off
		ISO_LOCAL_TIME_OPT_MILLIS = new DateTimeFormatterBuilder()
                .appendValue(HOUR_OF_DAY, 2)
                .appendLiteral(':')
                .appendValue(MINUTE_OF_HOUR, 2)
                .optionalStart()
                .appendLiteral(':')
                .appendValue(SECOND_OF_MINUTE, 2)
                .optionalStart()
                .appendFraction(MILLI_OF_SECOND, 0, 3, true)
                .toFormatter()
				.withChronology(IsoChronology.INSTANCE);
    	// @formatter:on
	}

	/**
	 * Date and time formatter using the ISO 8601 style but with an optional
	 * time component using an optional milliseconds of second component, and a
	 * space character for the date/time separator instead of {@literal T}.
	 *
	 * <p>
	 * This supports patterns like:
	 * </p>
	 * <ul>
	 * <li>{@literal 2020-02-01 20:12:34.567}</li>
	 * <li>{@literal 2020-02-01 20:12:34}</li>
	 * <li>{@literal 2020-02-01 20:12}</li>
	 * <li>{@literal 2020-02-01}</li>
	 * </ul>
	 *
	 * @since 2.0
	 */
	public static final DateTimeFormatter ISO_DATE_OPT_TIME_OPT_MILLIS_ALT;
	static {
		// @formatter:off
		ISO_DATE_OPT_TIME_OPT_MILLIS_ALT = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .append(DateTimeFormatter.ISO_LOCAL_DATE)
				.parseDefaulting(ChronoField.ERA, ChronoField.ERA.range().getMaximum())
				.optionalStart()
				.appendLiteral(' ')
				.append(ISO_LOCAL_TIME_OPT_MILLIS)
				.optionalEnd()
				.parseDefaulting(ChronoField.HOUR_OF_DAY, ChronoField.HOUR_OF_DAY.range().getMinimum())
				.parseDefaulting(ChronoField.MINUTE_OF_HOUR, ChronoField.MINUTE_OF_HOUR.range().getMinimum())
				.parseDefaulting(ChronoField.SECOND_OF_MINUTE, ChronoField.SECOND_OF_MINUTE.range().getMinimum())
				.parseDefaulting(ChronoField.MILLI_OF_SECOND, ChronoField.MILLI_OF_SECOND.range().getMinimum())
				.toFormatter()
				.withChronology(IsoChronology.INSTANCE);
		// @formatter:on
	}

	/**
	 * Date and time formatter based on
	 * {@link #ISO_DATE_OPT_TIME_OPT_MILLIS_ALT} with a UTC time zone offset
	 * applied.
	 *
	 * @since 2.0
	 */
	public static final DateTimeFormatter ISO_DATE_OPT_TIME_OPT_MILLIS_ALT_UTC;
	static {
		ISO_DATE_OPT_TIME_OPT_MILLIS_ALT_UTC = ISO_DATE_OPT_TIME_OPT_MILLIS_ALT.withZone(ZoneOffset.UTC);
	}

	/**
	 * Date and time formatter using the ISO 8601 style but with an optional
	 * time component using an optional milliseconds of second component.
	 *
	 * <p>
	 * This supports patterns like:
	 * </p>
	 * <ul>
	 * <li>{@literal 2020-02-01T20:12:34.567}</li>
	 * <li>{@literal 2020-02-01T20:12:34}</li>
	 * <li>{@literal 2020-02-01T20:12}</li>
	 * <li>{@literal 2020-02-01}</li>
	 * </ul>
	 *
	 * @since 2.1
	 */
	public static final DateTimeFormatter ISO_DATE_OPT_TIME_OPT_MILLIS;
	static {
		// @formatter:off
		ISO_DATE_OPT_TIME_OPT_MILLIS = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .append(DateTimeFormatter.ISO_LOCAL_DATE)
				.parseDefaulting(ChronoField.ERA, ChronoField.ERA.range().getMaximum())
				.optionalStart()
				.appendLiteral('T')
				.append(ISO_LOCAL_TIME_OPT_MILLIS)
				.optionalEnd()
				.parseDefaulting(ChronoField.HOUR_OF_DAY, ChronoField.HOUR_OF_DAY.range().getMinimum())
				.parseDefaulting(ChronoField.MINUTE_OF_HOUR, ChronoField.MINUTE_OF_HOUR.range().getMinimum())
				.parseDefaulting(ChronoField.SECOND_OF_MINUTE, ChronoField.SECOND_OF_MINUTE.range().getMinimum())
				.parseDefaulting(ChronoField.MILLI_OF_SECOND, ChronoField.MILLI_OF_SECOND.range().getMinimum())
				.toFormatter()
				.withChronology(IsoChronology.INSTANCE);
		// @formatter:on
	}

	/**
	 * Date and time formatter based on {@link #ISO_DATE_OPT_TIME_OPT_MILLIS}
	 * with a UTC time zone offset applied.
	 *
	 * @since 2.1
	 */
	public static final DateTimeFormatter ISO_DATE_OPT_TIME_OPT_MILLIS_UTC;
	static {
		ISO_DATE_OPT_TIME_OPT_MILLIS_UTC = ISO_DATE_OPT_TIME_OPT_MILLIS.withZone(ZoneOffset.UTC);
	}

	/**
	 * Parse an ISO-8601 alternate timestamp.
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
		return parseIsoTimestamp(ISO_DATE_OPT_TIME_ALT, value, defaultZone);
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
	 *        the date time string to parse
	 * @param defaultZone
	 *        a default time zone to use if one is not available in
	 *        {@code value}
	 * @return the parsed date, or {@literal null} if it cannot be parsed for
	 *         any reason
	 * @since 2.2
	 */
	public static ZonedDateTime parseIsoTimestamp(final String value, final ZoneId defaultZone) {
		return parseIsoTimestamp(ISO_DATE_OPT_TIME, value, defaultZone);
	}

	/**
	 * Parse an ISO-8601 alternate timestamp using a given formatter.
	 *
	 * <p>
	 * This method handles input values that both include or omit a time zone
	 * offset. If a time zone offset is not provided in the input, then
	 * {@code defaultZone} will be used to get a final result.
	 * </p>
	 *
	 * @param formatter
	 *        the formatter, which must support {@link ZonedDateTime} parsing
	 * @param value
	 *        the date time string to parse
	 * @param defaultZone
	 *        a default time zone to use if one is not available in
	 *        {@code value}
	 * @return the parsed date, or {@literal null} if it cannot be parsed for
	 *         any reason
	 * @since 2.2
	 */
	public static ZonedDateTime parseIsoTimestamp(final DateTimeFormatter formatter, final String value,
			final ZoneId defaultZone) {
		ZonedDateTime result = null;
		try {
			// try with full time zone from value first
			result = formatter.parse(value, ZonedDateTime::from);
		} catch ( DateTimeException e ) {
			// try date + zone approach
			try {
				TemporalAccessor ta = formatter.parse(value);
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
		if ( ChronoField.MINUTE_OF_DAY.equals(field) ) {
			return parseMinuteOfDayRange(range, locale);
		}
		return parseRange(splitRange(range), locale, field, TextStyle.FULL);
	}

	/**
	 * Parse a time range set.
	 *
	 * <p>
	 * A comma range delimiter will be used.
	 * </p>
	 *
	 * @param field
	 *        the time field to parse
	 * @param ranges
	 *        the range set string to parse into a time range set
	 * @param locale
	 *        the locale to parse the ranges as
	 * @return the range set, or {@literal null} if {@code ranges} is
	 *         {@literal null} or empty
	 * @throws DateTimeException
	 *         if any parsing error occurs
	 * @since 2.3
	 */
	public static IntRangeSet parseRangeSet(TemporalField field, String ranges, Locale locale) {
		return parseRangeSet(field, ranges, locale, ",");
	}

	/**
	 * Parse a time range set.
	 *
	 * @param field
	 *        the time field to parse
	 * @param ranges
	 *        the range set string to parse into a time range set
	 * @param locale
	 *        the locale to parse the ranges as
	 * @param rangeDelim
	 *        the delimiter to use between ranges
	 * @return the range set, or {@literal null} if {@code ranges} is
	 *         {@literal null} or empty
	 * @throws DateTimeException
	 *         if any parsing error occurs
	 * @since 2.3
	 */
	public static IntRangeSet parseRangeSet(TemporalField field, String ranges, Locale locale,
			String rangeDelim) {
		if ( ranges == null ) {
			return null;
		}
		if ( locale == null ) {
			locale = Locale.getDefault();
		}
		String[] rangeComponents = StringUtils.delimitedListToStringArray(ranges, rangeDelim);
		if ( rangeComponents.length < 1 ) {
			return null;
		}
		IntRangeSet result = new IntRangeSet();
		for ( String range : rangeComponents ) {
			IntRange r = parseRange(field, range.trim(), locale);
			if ( r != null ) {
				result.addRange(r);
			}
		}
		return result;
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
			LocalDateTime t = LocalDateTime.of(2024, 1, 1, 0, 0); // Jan 1 on Mon, leap year
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

	/**
	 * Format a time range set.
	 *
	 * <p>
	 * The range can be specified using names, abbreviations, or numbers. The
	 * range of allowed numbers varies by field.
	 * </p>
	 *
	 * <p>
	 * A comma will be used for the range delimiter.
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
	 * @since 2.3
	 * @see #formatRange(ChronoField, IntRangeContainer, Locale, TextStyle,
	 *      String)
	 */
	public static String formatRange(ChronoField field, IntRangeContainer range, Locale locale,
			TextStyle style) throws DateTimeException {
		return formatRange(field, range, locale, style, ",");
	}

	/**
	 * Format a time range set.
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
	 * @param rangeDelim
	 *        the delimiter to use between ranges
	 * @return the range string
	 * @throws DateTimeException
	 *         if any formatting error occurs
	 * @since 2.3
	 */
	public static String formatRange(ChronoField field, IntRangeContainer range, Locale locale,
			TextStyle style, String rangeDelim) throws DateTimeException {
		if ( range == null ) {
			return null;
		}
		if ( locale == null ) {
			locale = Locale.getDefault();
		}

		StringBuilder buf = new StringBuilder();
		for ( IntRange r : range.ranges() ) {
			if ( buf.length() > 0 ) {
				buf.append(rangeDelim);
			}
			String s = formatRange(r, locale, field, style);
			if ( s != null ) {
				buf.append(s);
			}
		}
		return buf.toString();
	}

	/**
	 * Get a formatted "x days hh:mm:ss" style string from a millisecond
	 * duration.
	 *
	 * <p>
	 * The output pattern is like: {@literal Dd HH:MM:SS} where {@literal D} is
	 * the number of whole days, {@literal HH} the number of hours,
	 * {@literal MM} the number of minutes, and {@literal SS} the number of
	 * seconds.
	 * </p>
	 *
	 * <p>
	 * The returned string will be shortened to exclude the day and hour
	 * components if those values are {@literal 0}.
	 * </p>
	 *
	 * <p>
	 * Examples of output are:
	 * </p>
	 *
	 * <pre>
	 * 3d 12:34:56
	 * 23:45:43
	 * 34:56
	 * 00:01
	 * </pre>
	 *
	 * @param milliseconds
	 *        the millisecond duration
	 * @return the formatted string
	 */
	public static String formatHoursMinutesSeconds(final long milliseconds) {
		long totalSeconds = milliseconds / 1000L;
		long hours = (int) (totalSeconds / 3600L);
		int minutes = (int) ((totalSeconds % 3600L) / 60);
		int seconds = (int) (totalSeconds % 60);
		if ( hours > 23 ) {
			int days = (int) hours / 24;
			hours -= days * 24;
			return String.format("%dd %02d:%02d:%02d", days, hours, minutes, seconds);
		} else if ( hours > 0 ) {
			return String.format("%02d:%02d:%02d", hours, minutes, seconds);
		}
		return String.format("%02d:%02d", minutes, seconds);
	}

	/**
	 * Parse a standard local time value, in {@code HH:mm} form.
	 *
	 * @param value
	 *        the time value
	 * @return the LocalTime object
	 */
	public static LocalTime parseLocalTime(String value) {
		return LOCAL_TIME_FULL.parse(value, LocalTime::from);
	}

	/**
	 * Format a standard local time value, in {@code HH:mm} form.
	 *
	 * @param value
	 *        the LocalTime to format
	 * @return the formatted value
	 */
	public static String format(LocalTime value) {
		return LOCAL_TIME.format(value);
	}

	/**
	 * Parse a standard local date value, in {@code yyyy-MM-dd} form.
	 *
	 * @param value
	 *        the date value
	 * @return the LocalDate object
	 */
	public static LocalDate parseLocalDate(String value) {
		return DateTimeFormatter.ISO_LOCAL_DATE.parse(value, LocalDate::from);
	}

	/**
	 * Format a standard local date value, in {@code yyyy-MM-dd} form.
	 *
	 * @param value
	 *        the LocalDate to format
	 * @return the formatted value
	 */
	public static String format(LocalDate value) {
		return DateTimeFormatter.ISO_LOCAL_DATE.format(value);
	}

	/**
	 * Format an instant for display in the local (system) time zone.
	 *
	 * @param timestamp
	 *        the instant
	 * @return the formatted date
	 * @since 1.5
	 */
	public static String formatForLocalDisplay(Instant timestamp) {
		return DISPLAY_DATE_LONG_TIME_SHORT.format(timestamp.atZone(ZoneId.systemDefault()));
	}

	/**
	 * Parse a time zone ID into a zone instance.
	 *
	 * @param zoneId
	 *        the zone ID to parse, or {@literal null} to resolve the system
	 *        time zone
	 * @return the zone, never {@literal null}
	 * @throws IllegalArgumentException
	 *         if {@code zoneId} cannot be parsed
	 * @since 2.4
	 */
	public static ZoneId tz(String zoneId) {
		if ( zoneId == null ) {
			return ZoneId.systemDefault();
		}
		try {
			return ZoneId.of(zoneId);
		} catch ( DateTimeException e ) {
			throw new IllegalArgumentException(
					String.format("Invalid time zone [%s]: %s", zoneId, e.getMessage()));
		}
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
	 * @since 2.4
	 */
	public static Instant timestamp(Temporal date) {
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
	 * @since 2.4
	 */
	public static Instant timestamp(Temporal date, String zoneId) {
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
	 * @since 2.4
	 */
	public static Instant timestamp(Temporal date, ZoneId zone) {
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
	 * Parse a {@link ChronoUnit} value.
	 *
	 * @param name
	 *        the value to case insensitively parse as a {@link ChronoUnit}
	 * @return the enum value
	 * @throws IllegalArgumentException
	 *         if {@code name} is not value, or {@literal null} if {@code name}
	 *         is {@literal null}
	 * @since 2.4
	 */
	public static TemporalUnit chronoUnit(String name) {
		if ( name == null ) {
			return null;
		}
		try {
			return ChronoUnit.valueOf(name.toUpperCase());
		} catch ( IllegalArgumentException e ) {
			throw new IllegalArgumentException(String.format("Invalid chrono unit [%s].", name));
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
	 * @since 2.4
	 */
	public static TemporalAmount duration(String amount) {
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
	 * @since 2.4
	 */
	public static Temporal dateTruncate(Temporal date, String unit) {
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
	 * @since 2.4
	 */
	public static Temporal dateTruncate(Temporal date, TemporalUnit unit) {
		if ( date == null || unit == null ) {
			return date;
		}
		try {
			Temporal result = null;
			if ( unit.equals(ChronoUnit.WEEKS) ) {
				result = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
			} else if ( unit.equals(ChronoUnit.MONTHS) ) {
				result = date.with(TemporalAdjusters.firstDayOfMonth());
			} else if ( unit.equals(ChronoUnit.YEARS) ) {
				result = date.with(TemporalAdjusters.firstDayOfYear());
			}

			if ( result != null ) {
				// if date supports time, truncate that to start of day
				if ( result.isSupported(ChronoField.NANO_OF_DAY) ) {
					return result.with(ChronoField.NANO_OF_DAY, 0);
				}
				return result;
			}

			if ( date instanceof ZonedDateTime ) {
				return ((ZonedDateTime) date).truncatedTo(unit);
			} else if ( date instanceof Instant ) {
				return ((Instant) date).truncatedTo(unit);
			} else if ( date instanceof OffsetDateTime ) {
				return ((OffsetDateTime) date).truncatedTo(unit);
			} else if ( date instanceof LocalDateTime ) {
				return ((LocalDateTime) date).truncatedTo(unit);
			}
		} catch ( DateTimeException e ) {
			throw new IllegalArgumentException(
					String.format("Cannot truncate date %s to %s: %s", date, unit, e.getMessage()));
		}
		throw new IllegalArgumentException(String
				.format("Cannot trundate date type %s to %s: unsupported type.", date.getClass(), unit));
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
	 * @since 2.4
	 */
	public static Temporal datePlus(Temporal date, String amount) {
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
	 * @since 2.4
	 */
	public static Temporal datePlus(Temporal date, TemporalAmount amount) {
		if ( date == null || amount == null ) {
			return date;
		}
		try {
			return date.plus(amount);
		} catch ( DateTimeException e ) {
			throw new IllegalArgumentException(
					String.format("Unable to add [%s] to [%s]: %s", amount, date, e.getMessage()));
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
	 * @since 2.4
	 */
	public static Temporal datePlus(Temporal date, long amount, String unit) {
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
	 * @since 2.4
	 */
	public static Temporal datePlus(Temporal date, long amount, TemporalUnit unit) {
		if ( date == null || unit == null || amount == 0 ) {
			return date;
		}
		try {
			return date.plus(amount, unit);
		} catch ( DateTimeException e ) {
			throw new IllegalArgumentException(
					String.format("Unable to add %d %s to [%s]: %s", unit, date, e.getMessage()));
		}
	}

	/**
	 * Convert an {@link Instant} to the same type a given {@link Temporal}.
	 *
	 * @param date
	 *        the date to convert
	 * @param type
	 *        the type to convert the date to
	 * @param zone
	 *        the time zone to use for zoned conversions
	 * @return the converted date, or {@code date} if {@code type} is not
	 *         supported
	 * @since 2.7
	 */
	public static Temporal convert(Instant date, Temporal type, ZoneId zone) {
		if ( type instanceof ZonedDateTime ) {
			return date.atZone(zone);
		} else if ( type instanceof OffsetDateTime ) {
			return date.atOffset(((OffsetDateTime) type).getOffset());
		} else if ( type instanceof LocalDateTime ) {
			return date.atZone(zone).toLocalDateTime();
		} else if ( type instanceof LocalDate ) {
			return date.atZone(zone).toLocalDate();
		} else if ( type instanceof LocalTime ) {
			return date.atZone(zone).toLocalTime();
		}
		return date;
	}

	/**
	 * Round a date down by a given duration.
	 *
	 * @param date
	 *        the date to round down (backwards)
	 * @param amount
	 *        the amount to floor the date within
	 * @param zone
	 *        the time zone to use for {@link Period} based amounts
	 * @return the floored date, or {@code date} if {@code amount} is not a
	 *         supported amount type ({@link Period} or {@link Duration})
	 * @throws IllegalArgumentException
	 *         if the date cannot be floored to the given duration
	 * @since 2.7
	 */
	public static Temporal dateFloor(Temporal date, TemporalAmount amount, ZoneId zone) {
		if ( amount instanceof Period ) {
			return dateFloor(date, (Period) amount, zone);
		} else if ( amount instanceof Duration ) {
			return dateFloor(date, (Duration) amount, zone);
		}
		return date;
	}

	/**
	 * Round a date down by a given duration.
	 *
	 * @param date
	 *        the date to round down (backwards)
	 * @param amount
	 *        the amount to floor the date within
	 * @param zone
	 *        the time zone to use for {@link Period} based amounts
	 * @return the floored date
	 * @throws IllegalArgumentException
	 *         if the date cannot be floored to the given duration
	 * @since 2.7
	 */
	public static Temporal dateFloor(Temporal date, Duration amount, ZoneId zone) {
		Instant ts = timestamp(date, zone);
		Instant floored = Clock.tick(Clock.fixed(ts, zone), amount).instant();
		return convert(floored, date, zone);
	}

	/**
	 * Round a date down by a given period.
	 *
	 * <p>
	 * Only the following periods are supported:
	 * </p>
	 *
	 * <ul>
	 * <li>1 year</li>
	 * <li>1 month</li>
	 * <li>1 week</li>
	 * <li>1 day</li>
	 * </ul>
	 *
	 * @param date
	 *        the date to round down (backwards)
	 * @param amount
	 *        the amount to floor the date within
	 * @param zone
	 *        the time zone to use for {@link Period} based amounts
	 * @return the floored date
	 * @throws IllegalArgumentException
	 *         if the date cannot be floored to the given duration
	 * @since 2.7
	 */
	public static Temporal dateFloor(Temporal date, Period amount, ZoneId zone) {
		Instant ts = timestamp(date, zone);
		TemporalAdjuster adj = amount.getYears() > 0 ? TemporalAdjusters.firstDayOfYear()
				: amount.getMonths() > 0 ? TemporalAdjusters.firstDayOfMonth()
						: amount.getDays() > 6 ? TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)
								: null;
		ZonedDateTime d = ts.atZone(zone);
		if ( adj != null ) {
			d = d.with(adj);
		}
		Instant floored = d.toInstant().truncatedTo(ChronoUnit.DAYS);
		return convert(floored, date, zone);
	}

}
