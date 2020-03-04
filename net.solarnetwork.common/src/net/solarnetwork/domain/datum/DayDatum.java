/* ==================================================================
 * DayDatum.java - Oct 22, 2014 2:41:56 PM
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

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * Solar day related datum.
 * 
 * @author matt
 * @version 1.0
 */
public interface DayDatum extends Datum {

	/**
	 * A {@link net.solarnetwork.domain.GeneralDatumSamples} instantaneous
	 * sample key for {@link DayDatum#getSunriseTime()} values.
	 */
	static final String SUNRISE_KEY = "sunrise";

	/**
	 * A {@link net.solarnetwork.domain.GeneralDatumSamples} instantaneous
	 * sample key for {@link DayDatum#getSunsetTime()} values.
	 */
	static final String SUNSET_KEY = "sunset";

	/**
	 * A {@link net.solarnetwork.domain.GeneralDatumSamples} instantaneous
	 * sample key for {@link DayDatum#getMoonriseTime()} values.
	 */
	static final String MOONRISE_KEY = "moonrise";

	/**
	 * A {@link net.solarnetwork.domain.GeneralDatumSamples} instantaneous
	 * sample key for {@link DayDatum#getMoonsetTime()} values.
	 */
	static final String MOONSET_KEY = "moonset";

	/**
	 * A {@link net.solarnetwork.domain.GeneralDatumSamples} instantaneous
	 * sample key for {@link DayDatum#getTemperatureMaximum()} values.
	 */
	static final String TEMPERATURE_MAXIMUM_KEY = "tempMax";

	/**
	 * A {@link net.solarnetwork.domain.GeneralDatumSamples} instantaneous
	 * sample key for {@link DayDatum#getTemperatureMinimum()} values.
	 */
	static final String TEMPERATURE_MINIMUM_KEY = "tempMin";

	/**
	 * A {@link net.solarnetwork.domain.GeneralDatumSamples} status sample key
	 * for {@link DayDatum#getSkyConditions()} values.
	 */
	static final String SKY_CONDITIONS_KEY = "sky";

	/**
	 * A {@link net.solarnetwork.domain.GeneralDatumSamples} status sample key
	 * for a bitmask of {@link net.solarnetwork.domain.SkyCondition#getCode()}
	 * values.
	 */
	String SKY_CONDITION_CODES_KEY = "skies";

	/**
	 * A {@link net.solarnetwork.domain.GeneralDatumSamples} status sample key
	 * for {@link DayDatum#getBriefOverview()} values.
	 */
	static final String BRIEF_OVERVIEW_KEY = "brief";

	/**
	 * A {@link net.solarnetwork.domain.GeneralDatumSamples} status sample key
	 * for {@link AtmosphericDatum#getWindSpeed()} values.
	 */
	static final String WIND_SPEED_KEY = "wspeed";

	/**
	 * A {@link net.solarnetwork.domain.GeneralDatumSamples} status sample key
	 * for {@link AtmosphericDatum#getWindDirection()} values.
	 */
	static final String WIND_DIRECTION_KEY = "wdir";

	/**
	 * A {@link net.solarnetwork.domain.GeneralDatumSamples} status sample key
	 * for {@link AtmosphericDatum#getRain()} values.
	 */
	static final String RAIN_KEY = "rain";

	/**
	 * A {@link net.solarnetwork.domain.GeneralDatumSamples} status sample key
	 * for {@link AtmosphericDatum#getSnow()} values.
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
	LocalTime getSunriseTime();

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
	BigDecimal getTemperatureMaximum();

	/**
	 * Get a textual description of the sky conditions, e.g. "clear", "cloudy",
	 * etc.
	 * 
	 * @return general sky conditions
	 */
	String getSkyConditions();

	/**
	 * Get a brief textual description of the overall conditions, e.g. "Sunshine
	 * and some clouds. High 18C. Winds N at 10 to 15 km/h."
	 * 
	 * @return general overall conditions description
	 */
	String getBriefOverview();

	/**
	 * Get the wind speed, in meters / second.
	 * 
	 * @return wind speed
	 */
	BigDecimal getWindSpeed();

	/**
	 * Get the wind direction, in degrees.
	 * 
	 * @return wind direction
	 */
	Integer getWindDirection();

	/**
	 * Get the rain accumulation, in millimeters.
	 * 
	 * @return rain accumulation
	 */
	Integer getRain();

	/**
	 * Get the snow accumulation, in millimeters.
	 * 
	 * @return snow accumulation
	 */
	Integer getSnow();

}
