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
import net.solarnetwork.util.IntRange;

/**
 * Simple implementation of {@link TemporalRangesTariffEvaluator}.
 * 
 * <p>
 * Each of the fields in the given rule are compared to the given date. If a
 * range exists for a given field, and the associated value of that field in the
 * given date is not within that range, {@literal false} is returned. All ranges
 * are treated as inclusive, except the {@code MINUTE_OF_DAY} range whose
 * maximum value is treated as an exclusive value.
 * </p>
 * 
 */
public final class SimpleTemporalRangesTariffEvaluator implements TemporalRangesTariffEvaluator {

	@Override
	public boolean applies(TemporalRangesTariff rule, LocalDateTime dateTime,
			Map<String, ?> parameters) {
		for ( ChronoField f : SimpleTemporalTariffSchedule.FIELDS ) {
			IntRange range = rule.rangeForField(f);
			if ( range != null ) {
				int v = dateTime.get(f);
				if ( !range.contains(v) || (f == ChronoField.MINUTE_OF_DAY && v == range.getMax()) ) {
					return false;
				}
			}
		}
		return true;
	}

}
