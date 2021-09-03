/* ==================================================================
 * DayDatum.java - Oct 22, 2014 2:41:56 PM
 * 
 * Copyright 2007-2014 SolarNetwork.net Dev Team
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU  Public License as 
 * published by the Free Software Foundation; either version 2 of 
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 *  Public License for more details.
 * 
 * You should have received a copy of the GNU  Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
 * 02111-1307 USA
 * ==================================================================
 */

package net.solarnetwork.domain.datum;

import static net.solarnetwork.domain.datum.DatumSamplesType.Instantaneous;
import static net.solarnetwork.domain.datum.DatumSamplesType.Status;
import java.math.BigDecimal;
import java.time.LocalTime;
import net.solarnetwork.util.DateUtils;

/**
 * Solar day related datum.
 * 
 * @author matt
 * @version 2.0
 */
public interface DayDatum extends AtmosphericDatum {

	/**
	 * A status sample key for {@link DayDatum#getBriefOverview()} values.
	 */
	String BRIEF_OVERVIEW_KEY = "brief";

	/**
	 * An instantaneous sample key for {@link DayDatum#getSunriseTime()} values.
	 */
	String SUNRISE_KEY = "sunrise";

	/**
	 * An instantaneous sample key for {@link DayDatum#getSunsetTime()} values.
	 */
	String SUNSET_KEY = "sunset";

	/**
	 * An instantaneous sample key for {@link DayDatum#getMoonriseTime()}
	 * values.
	 */
	String MOONRISE_KEY = "moonrise";

	/**
	 * An instantaneous sample key for {@link DayDatum#getMoonsetTime()} values.
	 */
	String MOONSET_KEY = "moonset";

	/**
	 * An instantaneous sample key for {@link DayDatum#getTemperatureMaximum()}
	 * values.
	 */
	String TEMPERATURE_MAXIMUM_KEY = "tempMax";

	/**
	 * An instantaneous sample key for {@link DayDatum#getTemperatureMinimum()}
	 * values.
	 */
	String TEMPERATURE_MINIMUM_KEY = "tempMin";

	/**
	 * Get a brief textual description of the overall conditions, e.g. "Sunshine
	 * and some clouds. High 18C. Winds N at 10 to 15 km/h."
	 * 
	 * @return general overall conditions description
	 */
	default String getBriefOverview() {
		return asSampleOperations().getSampleString(DatumSamplesType.Status, BRIEF_OVERVIEW_KEY);
	}

	/**
	 * Get the sunrise time.
	 * 
	 * @return the sunrise
	 */
	default LocalTime getSunriseTime() {
		String time = asSampleOperations().getSampleString(Status, SUNRISE_KEY);
		if ( time == null ) {
			return null;
		}
		return net.solarnetwork.util.DateUtils.parseLocalTime(time);

	}

	/**
	 * Get the sunset time.
	 * 
	 * @return the sunset
	 */
	default LocalTime getSunsetTime() {
		String time = asSampleOperations().getSampleString(Status, SUNSET_KEY);
		if ( time == null ) {
			return null;
		}
		return DateUtils.parseLocalTime(time);
	}

	/**
	 * Get the moon rise time.
	 * 
	 * @return the moon rise
	 */
	default LocalTime getMoonriseTime() {
		String time = asSampleOperations().getSampleString(Status, MOONRISE_KEY);
		if ( time == null ) {
			return null;
		}
		return DateUtils.parseLocalTime(time);
	}

	/**
	 * Get the moon set time.
	 * 
	 * @return the moon set
	 */
	default LocalTime getMoonsetTime() {
		String time = asSampleOperations().getSampleString(Status, MOONSET_KEY);
		if ( time == null ) {
			return null;
		}
		return DateUtils.parseLocalTime(time);
	}

	/**
	 * Get the maximum temperature for the day.
	 * 
	 * @return the maximum temperature.
	 */
	default BigDecimal getTemperatureMinimum() {
		return asSampleOperations().getSampleBigDecimal(Instantaneous, TEMPERATURE_MINIMUM_KEY);
	}

	/**
	 * Get the maximum temperature for the day.
	 * 
	 * @return the maximum temperature.
	 */
	default BigDecimal getTemperatureMaximum() {
		return asSampleOperations().getSampleBigDecimal(Instantaneous, TEMPERATURE_MAXIMUM_KEY);
	}

}
