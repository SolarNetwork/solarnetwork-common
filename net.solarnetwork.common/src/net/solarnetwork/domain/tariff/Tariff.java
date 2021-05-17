/* ==================================================================
 * Tariff.java - 12/05/2021 8:39:25 AM
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * A tariff.
 * 
 * @author matt
 * @version 1.0
 * @since 1.71
 */
public interface Tariff {

	/**
	 * A tariff rate.
	 */
	interface Rate {

		/**
		 * Get a unique ID for this rate.
		 * 
		 * <p>
		 * This is meant to uniquely identify the type of rate, so the
		 * description can be localized.
		 * </p>
		 * 
		 * @return a unique ID, never {@literal null}
		 */
		String getId();

		/**
		 * Get a non-localized description of the rate.
		 * 
		 * @return the description
		 */
		String getDescription();

		/**
		 * Get the rate amount.
		 * 
		 * @return the amount, never {@literal null}
		 */
		BigDecimal getAmount();

	}

	/**
	 * Get the rates that apply with this tariff.
	 * 
	 * @return the rates, as a mapping of rate IDs to associated rates, never
	 *         {@literal null}
	 */
	Map<String, Rate> getRates();

	/**
	 * Create a temporal tariff.
	 * 
	 * @param dateTime
	 *        the date time for the tariff
	 * @return the new temporal tariff
	 */
	default TemporalTariff toTemporalTariff(LocalDateTime dateTime) {
		return new SimpleTemporalTariff(dateTime, this);
	}

}
