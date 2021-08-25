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

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * Solar day related datum.
 * 
 * @author matt
 * @version 2.0
 */
public interface DayDatum extends Datum {

	/**
	 * A {@link DatumSamples} instantaneous sample key for
	 * {@link DayDatum#getSunriseTime()} values.
	 */
	static final String SUNRISE_KEY = "sunrise";

	/**
	 * A {@link DatumSamples} instantaneous sample key for
	 * {@link DayDatum#getSunsetTime()} values.
	 */
	static final String SUNSET_KEY = "sunset";

	/**
	 * A {@link DatumSamples} instantaneous sample key for
	 * {@link DayDatum#getMoonriseTime()} values.
	 */
	static final String MOONRISE_KEY = "moonrise";

	/**
	 * A {@link DatumSamples} instantaneous sample key for
	 * {@link DayDatum#getMoonsetTime()} values.
	 */
	static final String MOONSET_KEY = "moonset";

	/**
	 * A {@link DatumSamples} instantaneous sample key for
	 * {@link DayDatum#getTemperatureMaximum()} values.
	 */
	static final String TEMPERATURE_MAXIMUM_KEY = "tempMax";

	/**
	 * A {@link DatumSamples} instantaneous sample key for
	 * {@link DayDatum#getTemperatureMinimum()} values.
	 */
	static final String TEMPERATURE_MINIMUM_KEY = "tempMin";

	/**
	 * A {@link DatumSamples} status sample key for
	 * {@link DayDatum#getSkyConditions()} values.
	 */
	static final String SKY_CONDITIONS_KEY = "sky";

	/**
	 * A {@link DatumSamples} status sample key for a bitmask of
	 * {@link net.solarnetwork.domain.SkyCondition#getCode()} values.
	 */
	String SKY_CONDITION_CODES_KEY = "skies";

	/**
	 * A {@link DatumSamples} status sample key for
	 * {@link DayDatum#getBriefOverview()} values.
	 */
	static final String BRIEF_OVERVIEW_KEY = "brief";

	/**
	 * A {@link DatumSamples} status sample key for
	 * {@link AtmosphericDatum#getWindSpeed()} values.
	 */
	static final String WIND_SPEED_KEY = "wspeed";

	/**
	 * A {@link DatumSamples} status sample key for
	 * {@link AtmosphericDatum#getWindDirection()} values.
	 */
	static final String WIND_DIRECTION_KEY = "wdir";

	/**
	 * A {@link DatumSamples} status sample key for
	 * {@link AtmosphericDatum#getRain()} values.
	 */
	static final String RAIN_KEY = "rain";

	/**
	 * A {@link DatumSamples} status sample key for
	 * {@link AtmosphericDatum#getSnow()} values.
	 */
	static final String SNOW_KEY = "snow";

	/**
	 * A tag for a forecast day sample, as opposed to an actual measurement.
	 */
	static final String TAG_FORECAST = "forecast";

	/**
	 * Get the sunrise time.
	 * 
	 * @return the sunrise
	 */
	default LocalTime getSunriseTime() {
		String time = asSampleOperations().getSampleString(DatumSamplesType.Status, SUNRISE_KEY);
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
	LocalTime getSunsetTime();

	/**
	 * Get the sunrise time.
	 * 
	 * @return the moonrise
	 */
	LocalTime getMoonriseTime();

	/**
	 * Get the moonset time.
	 * 
	 * @return the moonset
	 */
	LocalTime getMoonsetTime();

	/**
	 * Get the minimum temperature for the day.
	 * 
	 * @return The minimum temperature.
	 */
	BigDecimal getTemperatureMinimum();

	/**
	 * Get the maximum temperature for the day.
	 * 
	 * @return The maximum temperature.
	 */
	default BigDecimal getTemperatureMaximum() {
		return asSampleOperations().getSampleBigDecimal(DatumSamplesType.Instantaneous,
				TEMPERATURE_MAXIMUM_KEY);
	}

	/**
	 * Get a textual description of the sky conditions, e.g. "clear", "cloudy",
	 * etc.
	 * 
	 * @return general sky conditions
	 */
	default String getSkyConditions() {
		return asSampleOperations().getSampleString(DatumSamplesType.Status, SKY_CONDITIONS_KEY);
	}

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
	 * Get the wind speed, in meters / second.
	 * 
	 * @return wind speed
	 */
	default BigDecimal getWindSpeed() {
		return asSampleOperations().getSampleBigDecimal(DatumSamplesType.Instantaneous, WIND_SPEED_KEY);
	}

	/**
	 * Get the wind direction, in degrees.
	 * 
	 * @return wind direction
	 */
	default Integer getWindDirection() {
		return asSampleOperations().getSampleInteger(DatumSamplesType.Instantaneous, WIND_DIRECTION_KEY);
	}

	/**
	 * Get the rain accumulation, in millimeters.
	 * 
	 * @return rain accumulation
	 */
	default Integer getRain() {
		return asSampleOperations().getSampleInteger(DatumSamplesType.Instantaneous, RAIN_KEY);
	}

	/**
	 * Get the snow accumulation, in millimeters.
	 * 
	 * @return snow accumulation
	 */
	default Integer getSnow() {
		return asSampleOperations().getSampleInteger(DatumSamplesType.Instantaneous, SNOW_KEY);
	}

}
