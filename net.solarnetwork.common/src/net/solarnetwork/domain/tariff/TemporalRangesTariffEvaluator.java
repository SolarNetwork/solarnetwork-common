/* ==================================================================
 * TemporalRangesTariffEvaluator.java - 12/05/2021 8:39:25 AM
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
import java.util.Map;
import net.solarnetwork.domain.Identifiable;

/**
 * API for a function that can evaluate a given {@link TemporalRangesTariff}
 * rule to see if it applies based on an input date and parameter map.
 */
public interface TemporalRangesTariffEvaluator extends Identifiable {

	/**
	 * Test if a rule applies to a given date and set of parameters.
	 * 
	 * @param rule
	 *        the rule
	 * @param date
	 *        the date
	 * @param parameters
	 *        the parameters
	 * @return {@literal true} if the tariff applies
	 */
	public boolean applies(TemporalRangesTariff rule, LocalDateTime date, Map<String, ?> parameters);
}
