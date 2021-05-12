/* ==================================================================
 * TemporalRangesTariff.java - 12/05/2021 8:39:25 AM
 * 
 * Copyright 2021 SolarNetwork.net Dev Team
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

import static java.util.stream.Collectors.toMap;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import net.solarnetwork.util.DateUtils;
import net.solarnetwork.util.IntRange;

/**
 * A tariff with time-based range rules.
 * 
 * <p>
 * The rules associated with this tariff are represented by a set of date ranges
 * that serve as the constraints that must be satisfied by a given date for the
 * rule to apply.
 * </p>
 */
public class TemporalRangesTariff implements Tariff {

	private final IntRange monthRange;
	private final IntRange dayOfMonthRange;
	private final IntRange dayOfWeekRange;
	private final IntRange minuteOfDayRange;
	private final Map<String, Rate> rates;

	/**
	 * Constructor.
	 * 
	 * @param monthRange
	 *        the month range (months are 1-12)
	 * @param dayOfMonthRange
	 *        the day of month range (1-31)
	 * @param dayOfWeekRange
	 *        the day of week range (1-7, with 1 = Monday, 7 = Sunday)
	 * @param minuteOfDayRange
	 *        the minute of day range (0-1440)
	 * @param rates
	 *        a list of rates associated with the tariff
	 */
	public TemporalRangesTariff(IntRange monthRange, IntRange dayOfMonthRange, IntRange dayOfWeekRange,
			IntRange minuteOfDayRange, List<Rate> rates) {
		super();
		this.monthRange = monthRange;
		this.dayOfMonthRange = dayOfMonthRange;
		this.dayOfWeekRange = dayOfWeekRange;
		this.minuteOfDayRange = minuteOfDayRange;
		this.rates = rates.stream()
				.collect(toMap(Rate::getId, Function.identity(), (k, v) -> v, LinkedHashMap::new));
	}

	/**
	 * Constructor.
	 * 
	 * @param monthRange
	 *        the month range (months are 1-12)
	 * @param dayOfMonthRange
	 *        the day of month range (1-31)
	 * @param dayOfWeekRange
	 *        the day of week range (1-7, with 1 = Monday, 7 = Sunday)
	 * @param minuteOfDayRange
	 *        the minute of day range (0-1440)
	 * @param rates
	 *        a list of rates associated with the tariff
	 * @param locale
	 *        the locale
	 */
	public TemporalRangesTariff(String monthRange, String dayOfMonthRange, String dayOfWeekRange,
			String minuteOfDayRange, List<Rate> rates, Locale locale) {
		super();
		this.monthRange = DateUtils.parseMonthRange(monthRange, locale);
		this.dayOfMonthRange = DateUtils.parseDayOfMonthRange(dayOfMonthRange, locale);
		this.dayOfWeekRange = DateUtils.parseDayOfWeekRange(dayOfWeekRange, locale);
		this.minuteOfDayRange = DateUtils.parseMinuteOfDayRange(minuteOfDayRange, locale);
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
	public IntRange rangeForField(ChronoField field) {
		switch (field) {
			case MONTH_OF_YEAR:
				return getMonthRange();

			case DAY_OF_MONTH:
				return getDayOfMonthRange();

			case DAY_OF_WEEK:
				return getDayOfWeekRange();

			case MINUTE_OF_DAY:
				return getMinuteOfDayRange();

			default:
				return null;
		}
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

	/**
	 * Get the month-of-year range.
	 * 
	 * @return the month range, from 1 - 12
	 */
	public IntRange getMonthRange() {
		return monthRange;
	}

	/**
	 * Get the day of month range.
	 * 
	 * @return the day range, from 1 - 31
	 */
	public IntRange getDayOfMonthRange() {
		return dayOfMonthRange;
	}

	/**
	 * Get the day-of-week range.
	 * 
	 * @return the weekday range, from 1-7 with Monday being 1
	 */
	public IntRange getDayOfWeekRange() {
		return dayOfWeekRange;
	}

	/**
	 * Get the minute-of-day range.
	 * 
	 * @return the range, from 0 - 1440
	 */
	public IntRange getMinuteOfDayRange() {
		return minuteOfDayRange;
	}

}
