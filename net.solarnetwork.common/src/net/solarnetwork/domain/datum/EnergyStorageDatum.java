/* ==================================================================
 * EnergyStorageDatum.java - 16/02/2016 7:37:54 pm
 * 
 * Copyright 2007-2016 SolarNetwork.net Dev Team
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

package net.solarnetwork.domain.datum;

import static net.solarnetwork.domain.datum.DatumSamplesType.Instantaneous;

/**
 * Standardized API for energy storage system related datum to implement.
 * 
 * @author matt
 * @version 2.0
 */
public interface EnergyStorageDatum extends Datum {

	/**
	 * An instantaneous sample key for {@link #getAvailableEnergyPercentage()}
	 * values.
	 */
	String PERCENTAGE_KEY = "percent";

	/**
	 * An instantaneous sample key for {@link #getAvailableEnergy()} values.
	 */
	String AVAILABLE_WATT_HOURS_KEY = "availWattHours";

	/**
	 * Get the percentage of energy capacity available in the storage.
	 * 
	 * <p>
	 * This value, multiplied by {@link #getAvailableEnergy()}, represents the
	 * total energy capacity of the storage.
	 * </p>
	 * 
	 * @return The available energy as a percentage of the total capacity of the
	 *         storage.
	 */
	default Float getAvailableEnergyPercentage() {
		return asSampleOperations().getSampleFloat(Instantaneous, PERCENTAGE_KEY);
	}

	/**
	 * Get the available energy of the storage system, in Wh.
	 * 
	 * @return The available energy of the storage.
	 */
	default Long getAvailableEnergy() {
		return asSampleOperations().getSampleLong(Instantaneous, AVAILABLE_WATT_HOURS_KEY);
	}

}
