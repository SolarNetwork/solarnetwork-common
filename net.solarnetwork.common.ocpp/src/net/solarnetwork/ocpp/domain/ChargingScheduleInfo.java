/* ==================================================================
 * ChargingScheduleInfo.java - 18/02/2020 3:17:19 pm
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

package net.solarnetwork.ocpp.domain;

import static net.solarnetwork.util.ObjectUtils.requireNonNullArgument;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import net.solarnetwork.domain.Differentiable;
import net.solarnetwork.util.DateUtils;

/**
 * Information about a charging schedule.
 *
 * @author matt
 * @version 1.0
 */
public class ChargingScheduleInfo implements Differentiable<ChargingScheduleInfo> {

	private @Nullable Duration duration;
	private @Nullable Instant start;
	private UnitOfMeasure rateUnit;
	private @Nullable BigDecimal minRate;
	private @Nullable List<ChargingSchedulePeriodInfo> periods;

	/**
	 * Constructor.
	 *
	 * @param rateUnit
	 *        the rate unit
	 * @throws IllegalArgumentException
	 *         if {@code rateUnit} is {@code null}
	 */
	public ChargingScheduleInfo(UnitOfMeasure rateUnit) {
		super();
		this.rateUnit = requireNonNullArgument(rateUnit, "rateUnit");
	}

	/**
	 * Constructor.
	 *
	 * @param duration
	 *        the schedule duration
	 * @param start
	 *        the schedule start time
	 * @param rateUnit
	 *        the rate unit
	 * @param minRate
	 *        the minimum charge rate
	 * @throws IllegalArgumentException
	 *         if {@code rateUnit} is {@code null}
	 */
	public ChargingScheduleInfo(@Nullable Duration duration, @Nullable Instant start,
			UnitOfMeasure rateUnit, @Nullable BigDecimal minRate) {
		this(rateUnit);
		this.duration = duration;
		this.start = start;
		this.minRate = minRate;
	}

	/**
	 * Copy constructor.
	 *
	 * @param other
	 *        the info to copy
	 */
	public ChargingScheduleInfo(ChargingScheduleInfo other) {
		this(other.duration, other.start, other.rateUnit, other.minRate);
		if ( other.periods != null ) {
			setPeriods(new ArrayList<>(other.periods));
		}
	}

	/**
	 * Test if the properties of another entity are the same as in this
	 * instance.
	 *
	 * @param other
	 *        the other entity to compare to
	 * @return {@literal true} if the properties of this instance are equal to
	 *         the other
	 */
	public boolean isSameAs(@Nullable ChargingScheduleInfo other) {
		if ( other == null ) {
			return false;
		}
		// @formatter:off
		return Objects.equals(duration, other.duration)
				&& Objects.equals(start, other.start)
				&& Objects.equals(rateUnit, other.rateUnit)
				&& Objects.equals(minRate, other.minRate)
				&& Objects.equals(periods, other.periods);
		// @formatter:on
	}

	@Override
	public boolean differsFrom(@Nullable ChargingScheduleInfo other) {
		return !isSameAs(other);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ChargingScheduleInfo{");
		if ( duration != null ) {
			builder.append("duration=");
			builder.append(duration);
			builder.append(", ");
		}
		if ( start != null ) {
			builder.append("start=");
			builder.append(start);
			builder.append(", ");
		}
		if ( rateUnit != null ) {
			builder.append("rateUnit=");
			builder.append(rateUnit);
			builder.append(", ");
		}
		if ( minRate != null ) {
			builder.append("minRate=");
			builder.append(minRate);
			builder.append(", ");
		}
		if ( periods != null ) {
			builder.append("periods=");
			builder.append(periods);
		}
		builder.append("}");
		return builder.toString();
	}

	/**
	 * Get the duration.
	 *
	 * @return the duration
	 */
	public final @Nullable Duration getDuration() {
		return duration;
	}

	/**
	 * Set the duration.
	 *
	 * @param duration
	 *        the duration to set
	 */
	public final void setDuration(@Nullable Duration duration) {
		this.duration = duration;
	}

	/**
	 * Get the duration, as seconds.
	 *
	 * @return the duration seconds
	 */
	public final int getDurationSeconds() {
		Duration d = getDuration();
		return (d != null ? (int) d.getSeconds() : 0);
	}

	/**
	 * Set the duration, as seconds.
	 *
	 * <p>
	 * If {@code seconds} is less than {@literal 1} then a {@code null}
	 * {@link Duration} will be set.
	 * </p>
	 *
	 * @param seconds
	 *        the duration
	 */
	public final void setDurationSeconds(int seconds) {
		setDuration(seconds > 0 ? Duration.ofSeconds(seconds) : null);
	}

	/**
	 * Get the start time.
	 *
	 * @return the start
	 */
	public final @Nullable Instant getStart() {
		return start;
	}

	/**
	 * Set the start time.
	 *
	 * @param start
	 *        the start to set
	 */
	public final void setStart(@Nullable Instant start) {
		this.start = start;
	}

	/**
	 * Get the start date as a formatted instant.
	 *
	 * @return the date, as an ISO 8601 formatted string, or {@code null} if
	 *         {@code start} is {@code null}
	 */
	public final @Nullable String getStartValue() {
		Instant ts = getStart();
		return (ts != null ? DateUtils.ISO_DATE_OPT_TIME_ALT_LOCAL.format(ts) : null);
	}

	/**
	 * Set the start date as an ISO 8601 formatted timestamp.
	 *
	 * @param value
	 *        the date string
	 */
	public final void setStartValue(@Nullable String value) {
		Instant ts = null;
		if ( value != null ) {
			ZonedDateTime date = DateUtils.parseIsoAltTimestamp(value, ZoneId.systemDefault());
			if ( date != null ) {
				ts = date.toInstant();
			}
		}
		setStart(ts);
	}

	/**
	 * Get the charging rate unit to use for the configured
	 * {@link #getPeriods()}.
	 *
	 * @return the unit, never {@code null}
	 */
	public final UnitOfMeasure getRateUnit() {
		return rateUnit;
	}

	/**
	 * Set the charging rate unit to use for the configured
	 * {@link #getPeriods()}.
	 *
	 * @param rateUnit
	 *        the unit to set
	 * @throws IllegalArgumentException
	 *         if any argument is {@code null}
	 */
	public final void setRateUnit(UnitOfMeasure rateUnit) {
		this.rateUnit = requireNonNullArgument(rateUnit, "rateUnit");
	}

	/**
	 * Get the charging rate unit, as a code value.
	 *
	 * @return the rate code
	 */
	public final int getRateUnitCode() {
		return getRateUnit().getCode();
	}

	/**
	 * Set the rate unit as a code value.
	 *
	 * @param code
	 *        the code value
	 */
	public final void setRateUnitCode(int code) {
		setRateUnit(UnitOfMeasure.forCode(code));
	}

	/**
	 * Get the minimum rate.
	 *
	 * @return the rate
	 */
	public final @Nullable BigDecimal getMinRate() {
		return minRate;
	}

	/**
	 * Set the minimum rate.
	 *
	 * @param minRate
	 *        the rate to set
	 */
	public final void setMinRate(@Nullable BigDecimal minRate) {
		if ( minRate != null && minRate.scale() != 1 ) {
			minRate = minRate.setScale(1, RoundingMode.HALF_UP);
		}
		this.minRate = minRate;
	}

	/**
	 * Add a period.
	 *
	 * @param period
	 *        the period to add
	 */
	public final void addPeriod(ChargingSchedulePeriodInfo period) {
		List<ChargingSchedulePeriodInfo> list = getPeriods();
		if ( list == null ) {
			list = new ArrayList<>(4);
			setPeriods(list);
		}
		list.add(period);
	}

	/**
	 * Get the periods list.
	 *
	 * @return the periods
	 */
	public final @Nullable List<ChargingSchedulePeriodInfo> getPeriods() {
		return periods;
	}

	/**
	 * Set the periods list.
	 *
	 * @param periods
	 *        the periods to set
	 */
	public final void setPeriods(@Nullable List<ChargingSchedulePeriodInfo> periods) {
		this.periods = periods;
	}

	/**
	 * Get the count of entity entities.
	 *
	 * @return the configuration count
	 */
	public final int getPeriodsCount() {
		List<ChargingSchedulePeriodInfo> periods = getPeriods();
		return (periods != null ? periods.size() : 0);
	}

	/**
	 * Adjust the number of configured entity entities.
	 *
	 * @param count
	 *        the desired number of elements
	 */
	public final void setPeriodsCount(int count) {
		List<ChargingSchedulePeriodInfo> infos = getPeriods();
		int currCount = (infos != null ? infos.size() : 0);
		if ( currCount == count || infos == null ) {
			return;
		}
		while ( currCount < count ) {
			if ( infos == null ) {
				infos = new ArrayList<>(count);
				setPeriods(infos);
			}
			infos.add(new ChargingSchedulePeriodInfo());
			currCount++;
		}
		while ( currCount > count ) {
			infos.remove(--currCount);
		}
	}
}
