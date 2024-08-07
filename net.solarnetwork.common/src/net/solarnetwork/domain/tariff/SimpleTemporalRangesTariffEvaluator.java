/* ==================================================================
 * SimpleTemporalRangesTariffEvaluator.java - 12/05/2021 9:30:30 AM
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

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.Map;
import net.solarnetwork.service.support.BasicIdentifiable;
import net.solarnetwork.util.IntRangeContainer;

/**
 * Simple implementation of {@link TemporalTariffEvaluator} with support for
 * both {@link TemporalRangesTariff} and {@link TemporalRangeSetsTariff}.
 *
 * <p>
 * Each of the fields in the given rule are compared to the given date. If a
 * range exists for a given field, and the associated value of that field in the
 * given date is not within that range, {@literal false} is returned. All ranges
 * are treated as inclusive, except the {@code MINUTE_OF_DAY} range whose
 * maximum value is treated as an exclusive value.
 * </p>
 *
 * @author matt
 * @version 1.1
 * @since 1.71
 */
public final class SimpleTemporalRangesTariffEvaluator extends BasicIdentifiable
		implements TemporalRangesTariffEvaluator {

	/** A static default instance. */
	public static final TemporalRangesTariffEvaluator DEFAULT_EVALUATOR = new SimpleTemporalRangesTariffEvaluator();

	/**
	 * Constructor.
	 */
	public SimpleTemporalRangesTariffEvaluator() {
		super();
	}

	@Override
	public boolean applies(TemporalRangesTariff rule, LocalDateTime dateTime,
			Map<String, ?> parameters) {
		return appliesInternal(rule, dateTime, parameters);
	}

	@Override
	public boolean applies(Tariff rule, LocalDateTime date, Map<String, ?> parameters) {
		if ( rule instanceof ChronoFieldsTariff ) {
			return appliesInternal((ChronoFieldsTariff) rule, date, parameters);
		}
		return false;
	}

	private boolean appliesInternal(ChronoFieldsTariff rule, LocalDateTime dateTime,
			Map<String, ?> parameters) {
		for ( ChronoField f : SimpleTemporalTariffSchedule.FIELDS ) {
			IntRangeContainer ranges = rule.rangeForChronoField(f);
			if ( ranges != null ) {
				int v = dateTime.get(f);
				if ( !ranges.contains(v) || (f == ChronoField.MINUTE_OF_DAY && v == ranges.max()) ) {
					return false;
				}
			}
		}
		return true;
	}

}
