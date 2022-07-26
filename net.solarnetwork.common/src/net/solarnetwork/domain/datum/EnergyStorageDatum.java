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
import static net.solarnetwork.domain.datum.DatumSamplesType.Status;

/**
 * Standardized API for energy storage system related datum to implement.
 * 
 * @author matt
 * @version 2.1
 */
public interface EnergyStorageDatum extends Datum {

	/**
	 * An instantaneous sample key for {@link #getAvailableEnergyPercentage()}
	 * values.
	 * 
	 * @since 2.1
	 */
	String STATE_OF_CHARGE_PERCENTAGE_KEY = "soc";

	/**
	 * An instantaneous sample key for {@link #getAvailableEnergyPercentage()}
	 * values.
	 * 
	 * @deprecated since 2.1 use {@link #STATE_OF_CHARGE_PERCENTAGE_KEY}
	 */
	@Deprecated
	String PERCENTAGE_KEY = "percent";

	/**
	 * An instantaneous sample key for {@link #getStateOfHealthPercentage()}
	 * values.
	 * 
	 * @since 2.1
	 */
	String STATE_OF_HEALTH_PERCENTAGE_KEY = "soh";

	/**
	 * An instantaneous sample key for {@link #getAvailableEnergy()} values.
	 */
	String AVAILABLE_WATT_HOURS_KEY = "availWattHours";

	/**
	 * An status sample key for {@link #getEnergyCapacity()} values.
	 * 
	 * @since 2.1
	 */
	String CAPACITY_WATT_HOURS_KEY = "capacityWattHours";

	/**
	 * Get the percentage of energy capacity available in the storage.
	 * 
	 * <p>
	 * The {@link #getAvailableEnergy()} value divided by this value, represents
	 * the total energy capacity of the storage.
	 * </p>
	 * 
	 * @return the available energy as a percentage of the total capacity of the
	 *         storage
	 */
	default Float getAvailableEnergyPercentage() {
		return asSampleOperations().getSampleFloat(Instantaneous, STATE_OF_CHARGE_PERCENTAGE_KEY);
	}

	/**
	 * Get a percentage of storage "health" in terms of practical total capacity
	 * right now versus theoretical total capacity when the storage was
	 * manufactured.
	 * 
	 * @return the total energy capacity now as a percentage of the theoretical
	 *         total capacity of the storage when manufactured
	 * @since 2.1
	 */
	default Float getStateOfHealthPercentage() {
		return asSampleOperations().getSampleFloat(Instantaneous, STATE_OF_HEALTH_PERCENTAGE_KEY);
	}

	/**
	 * Get the available energy of the storage system, in Wh.
	 * 
	 * @return the available energy of the storage
	 */
	default Long getAvailableEnergy() {
		return asSampleOperations().getSampleLong(Instantaneous, AVAILABLE_WATT_HOURS_KEY);
	}

	/**
	 * Get the energy capacity of the storage system, in Wh.
	 * 
	 * @return the energy capacity of the storage
	 * @since 2.1
	 */
	default Long getEnergyCapacity() {
		return asSampleOperations().getSampleLong(Status, CAPACITY_WATT_HOURS_KEY);
	}

}
