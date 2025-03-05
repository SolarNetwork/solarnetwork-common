/* ==================================================================
 * TemporalRangeSetsTariff.java - 26/07/2024 10:36:18 am
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

package net.solarnetwork.domain.tariff;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static net.solarnetwork.util.DateUtils.parseRangeSet;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import net.solarnetwork.util.DateUtils;
import net.solarnetwork.util.IntRangeContainer;
import net.solarnetwork.util.IntRangeSet;

/**
 * A tariff with time-based range set rules.
 *
 * <p>
 * The rules associated with this tariff are represented by a set of date range
 * sets that serve as the constraints that must be satisfied by a given date for
 * the rule to apply.
 * </p>
 *
 * @author matt
 * @version 1.0
 * @since 3.16
 */
public class TemporalRangeSetsTariff implements Tariff, ChronoFieldsTariff {

	private final IntRangeSet monthRanges;
	private final IntRangeSet dayOfMonthRanges;
	private final IntRangeSet dayOfWeekRanges;
	private final IntRangeSet minuteOfDayRanges;
	private final Map<String, Rate> rates;

	/**
	 * Constructor.
	 *
	 * @param monthRanges
	 *        the month range (months are 1-12)
	 * @param dayOfMonthRanges
	 *        the day of month range (1-31)
	 * @param dayOfWeekRanges
	 *        the day of week range (1-7, with 1 = Monday, 7 = Sunday)
	 * @param minuteOfDayRanges
	 *        the minute of day range (0-1440)
	 * @param rates
	 *        a list of rates associated with the tariff
	 */
	public TemporalRangeSetsTariff(IntRangeSet monthRanges, IntRangeSet dayOfMonthRanges,
			IntRangeSet dayOfWeekRanges, IntRangeSet minuteOfDayRanges, List<Rate> rates) {
		super();
		this.monthRanges = monthRanges;
		this.dayOfMonthRanges = dayOfMonthRanges;
		this.dayOfWeekRanges = dayOfWeekRanges;
		this.minuteOfDayRanges = minuteOfDayRanges;
		this.rates = (rates == null ? emptyMap()
				: rates.stream()
						.collect(toMap(Rate::getId, identity(), (k, v) -> v, LinkedHashMap::new)));
	}

	/**
	 * Constructor.
	 *
	 * @param monthRanges
	 *        a comma-delimited list of month ranges (months are 1-12)
	 * @param dayOfMonthRanges
	 *        a comma-delimited list of day of month ranges (1-31)
	 * @param dayOfWeekRanges
	 *        a comma-delimited list of day of week ranges (1-7, with 1 =
	 *        Monday, 7 = Sunday)
	 * @param minuteOfDayRanges
	 *        a comma-delimited list of minute of day ranges (0-1440)
	 * @param rates
	 *        a list of rates associated with the tariff
	 * @param locale
	 *        the locale
	 */
	public TemporalRangeSetsTariff(String monthRanges, String dayOfMonthRanges, String dayOfWeekRanges,
			String minuteOfDayRanges, List<Rate> rates, Locale locale) {
		super();
		this.monthRanges = parseRangeSet(ChronoField.MONTH_OF_YEAR, monthRanges, locale);
		this.dayOfMonthRanges = parseRangeSet(ChronoField.DAY_OF_MONTH, dayOfMonthRanges, locale);
		this.dayOfWeekRanges = parseRangeSet(ChronoField.DAY_OF_WEEK, dayOfWeekRanges, locale);
		this.minuteOfDayRanges = parseRangeSet(ChronoField.MINUTE_OF_DAY, minuteOfDayRanges, locale);
		this.rates = rates.stream()
				.collect(toMap(Rate::getId, Function.identity(), (k, v) -> v, LinkedHashMap::new));
	}

	@Override
	public Map<String, Rate> getRates() {
		return rates;
	}

	/**
	 * Get a range for a given temporal field.
	 *
	 * @param field
	 *        the field to get the range for
	 * @return the associated range, or {@literal null} if the field is not
	 *         supported or the range is {@literal null}
	 */
	public IntRangeSet rangeSetForField(ChronoField field) {
		switch (field) {
			case MONTH_OF_YEAR:
				return getMonthRanges();

			case DAY_OF_MONTH:
				return getDayOfMonthRanges();

			case DAY_OF_WEEK:
				return getDayOfWeekRanges();

			case MINUTE_OF_DAY:
				return getMinuteOfDayRanges();

			default:
				return null;
		}
	}

	@Override
	public IntRangeContainer rangeForChronoField(ChronoField field) {
		return rangeSetForField(field);
	}

	/**
	 * Test if this rule applies according to a given
	 * {@code TemporalRangesTariffEvaluator}.
	 *
	 * @param evaluator
	 *        the evaluator to use
	 * @param dateTime
	 *        the date time
	 * @param parameters
	 *        the parameters
	 * @return the result of calling
	 *         {@link TemporalRangesTariffEvaluator#applies(TemporalRangesTariff, LocalDateTime, Map)}
	 *         with this object
	 */
	public boolean applies(TemporalRangesTariffEvaluator evaluator, LocalDateTime dateTime,
			Map<String, ?> parameters) {
		return evaluator.applies(this, dateTime, parameters);
	}

	@Override
	public String formatChronoField(ChronoField field, Locale locale, TextStyle style) {
		return DateUtils.formatRange(field, rangeSetForField(field), locale, style);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TemporalRangeSetsTariff{");
		if ( monthRanges != null ) {
			builder.append("months=");
			builder.append(monthRanges);
			builder.append(", ");
		}
		if ( dayOfMonthRanges != null ) {
			builder.append("days=");
			builder.append(dayOfMonthRanges);
			builder.append(", ");
		}
		if ( dayOfWeekRanges != null ) {
			builder.append("dows=");
			builder.append(dayOfWeekRanges);
			builder.append(", ");
		}
		if ( minuteOfDayRanges != null ) {
			builder.append("times=");
			builder.append(minuteOfDayRanges);
			builder.append(", ");
		}
		if ( rates != null ) {
			builder.append("rates={");
			builder.append(rates.values().stream().map(r -> format("%s=%s", r.getId(), r.getAmount()))
					.collect(joining(",")));
			builder.append("}");
		}
		builder.append("}");
		return builder.toString();
	}

	/**
	 * Get the month-of-year ranges.
	 *
	 * @return the month ranges, from 1 - 12
	 */
	public final IntRangeSet getMonthRanges() {
		return monthRanges;
	}

	/**
	 * Get the day of month ranges.
	 *
	 * @return the day ranges, from 1 - 31
	 */
	public final IntRangeSet getDayOfMonthRanges() {
		return dayOfMonthRanges;
	}

	/**
	 * Get the day-of-week ranges.
	 *
	 * @return the weekday ranges, from 1-7 with Monday being 1
	 */
	public final IntRangeSet getDayOfWeekRanges() {
		return dayOfWeekRanges;
	}

	/**
	 * Get the minute-of-day ranges.
	 *
	 * @return the minute-of-day ranges, from 0 - 1440
	 */
	public final IntRangeSet getMinuteOfDayRanges() {
		return minuteOfDayRanges;
	}

}
