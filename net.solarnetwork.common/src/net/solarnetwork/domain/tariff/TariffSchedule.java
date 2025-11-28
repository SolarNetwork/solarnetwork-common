/* ==================================================================
 * TariffSchedule.java - 12/05/2021 8:38:44 AM
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
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * API for a tariff schedule, that can resolve a tariff based on a date.
 *
 * @author matt
 * @version 1.2
 * @since 1.71
 */
public interface TariffSchedule {

	/**
	 * Resolve a tariff without any parameters.
	 *
	 * @param dateTime
	 *        the date to resolve a tariff for
	 * @return the tariff, or {@code null} if no tariff applies
	 * @since 1.2
	 */
	default Tariff resolveTariff(LocalDateTime dateTime) {
		return resolveTariff(dateTime, null);
	}

	/**
	 * Resolve a tariff.
	 *
	 * @param dateTime
	 *        the date to resolve a tariff for
	 * @param parameters
	 *        optional parameters
	 * @return the tariff, or {@code null} if no tariff applies
	 */
	Tariff resolveTariff(LocalDateTime dateTime, Map<String, ?> parameters);

	/**
	 * Get all available rules in the schedule.
	 *
	 * @return the rules, or an empty collection if none available
	 * @since 1.1
	 */
	default Collection<? extends Tariff> rules() {
		return Collections.emptyList();
	}

}
