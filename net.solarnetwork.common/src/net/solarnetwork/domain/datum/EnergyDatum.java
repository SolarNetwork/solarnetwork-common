/* ==================================================================
 * EnergyDatum.java - Apr 1, 2014 5:02:29 PM
 * 
 * Copyright 2007-2014 SolarNetwork.net Dev Team
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

import static net.solarnetwork.domain.datum.DatumSamplesType.Accumulating;
import static net.solarnetwork.domain.datum.DatumSamplesType.Instantaneous;

/**
 * Standardized API for energy related datum to implement.
 * 
 * <p>
 * By "energy" we simply mean this datum represents information tracked during
 * the production or consumption of energy. For example current sensors can
 * provide approximate instantaneous watt readings, watt-hour meters can provide
 * accumulated Wh readings, and usually solar inverters can provide
 * instantaneous generated power and accumulated energy production readings.
 * </p>
 * 
 * @author matt
 * @version 2.0
 */
public interface EnergyDatum extends Datum {

	/**
	 * An accumulating sample key for {@link #getWattHourReading()} values.
	 */
	String WATT_HOUR_READING_KEY = "wattHours";

	/**
	 * An instantaneous sample key for {@link #getWatts()} values.
	 */
	String WATTS_KEY = "watts";

	/** A tag for "consumption" of energy. */
	String TAG_CONSUMPTION = "consumption";

	/** A tag for "generation" of energy. */
	String TAG_GENERATION = "power";

	/**
	 * Get a watt-hour reading.
	 * 
	 * <p>
	 * Generally this is an accumulating value and represents the overall energy
	 * used or produced since some reference date. Often the reference date if
	 * fixed, but it could also shift, for example shift to the last time a
	 * reading was taken.
	 * </p>
	 * 
	 * @return the watt hour reading, or {@literal null} if not available
	 */
	default Long getWattHourReading() {
		return asSampleOperations().getSampleLong(Accumulating, WATT_HOUR_READING_KEY);
	}

	/**
	 * Get a reverse watt-hour reading.
	 * 
	 * @return the reverse watt hour reading
	 */
	default Long getReverseWattHourReading() {
		return asSampleOperations().getSampleLong(Accumulating,
				WATT_HOUR_READING_KEY + REVERSE_ACCUMULATING_SUFFIX_KEY);
	}

	/**
	 * Get the instantaneous watts.
	 * 
	 * @return watts, or {@literal null} if not available
	 */
	default Integer getWatts() {
		return asSampleOperations().getSampleInteger(Instantaneous, WATTS_KEY);
	}

	/**
	 * Return {@literal true} if this datum is tagged with
	 * {@link #TAG_CONSUMPTION}.
	 * 
	 * @return {@literal true} if this datum has the consumption tag
	 */
	default boolean isConsumption() {
		return asSampleOperations().hasTag(TAG_CONSUMPTION);
	}

	/**
	 * Return {@literal true} if this datum is tagged with
	 * {@link #TAG_GENERATION}.
	 * 
	 * @return {@literal true} if this datum has the generation tag
	 */
	default boolean isGeneration() {
		return asSampleOperations().hasTag(TAG_GENERATION);
	}
}
