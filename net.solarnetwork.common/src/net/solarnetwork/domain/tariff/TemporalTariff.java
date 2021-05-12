/* ==================================================================
 * TemporalTariff.java - 12/05/2021 10:31:33 AM
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

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * A tariff associated with a date.
 * 
 * @author matt
 * @version 1.0
 * @since 1.71
 */
public interface TemporalTariff extends Tariff {

	/**
	 * Get a date associated with this tariff.
	 * 
	 * @return the date, or {@literal null}
	 */
	LocalDate getDate();

	/**
	 * Get a time associated with this tariff.
	 * 
	 * @return the time, or {@literal null}
	 */
	LocalTime getTime();

}
