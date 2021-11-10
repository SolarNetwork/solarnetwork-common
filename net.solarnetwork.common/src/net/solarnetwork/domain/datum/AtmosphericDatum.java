/* ==================================================================
 * AtmosphericDatum.java - Aug 26, 2014 1:52:01 PM
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

import static net.solarnetwork.domain.datum.DatumSamplesType.Instantaneous;
import static net.solarnetwork.domain.datum.DatumSamplesType.Status;
import java.math.BigDecimal;

/**
 * Standardized API for atmospheric related datum to implement.
 * 
 * @author matt
 * @version 1.1
 */
public interface AtmosphericDatum extends Datum {

	/**
	 * An instantaneous sample key for {@link AtmosphericDatum#getCO2()} values.
	 * 
	 * @since 1.1
	 */
	String CO2_KEY = "co2";

	/**
	 * An instantaneous sample key for {@link AtmosphericDatum#getLux()} values.
	 * 
	 * @since 1.1
	 */
	String LUX_KEY = "lux";

	/**
	 * An instantaneous sample key for {@link AtmosphericDatum#getTemperature()}
	 * values.
	 */
	String TEMPERATURE_KEY = "temp";

	/**
	 * An instantaneous sample key for {@link AtmosphericDatum#getHumidity()}
	 * values.
	 */
	String HUMIDITY_KEY = "humidity";

	/**
	 * An instantaneous sample key for {@link AtmosphericDatum#getDewPoint()}
	 * values.
	 */
	String DEW_POINT_KEY = "dew";

	/**
	 * An instantaneous sample key for
	 * {@link AtmosphericDatum#getAtmosphericPressure()} values.
	 */
	String ATMOSPHERIC_PRESSURE_KEY = "atm";

	/**
	 * A instantaneous sample key for {@link AtmosphericDatum#getVisibility()}
	 * values.
	 */
	String VISIBILITY_KEY = "visibility";

	/**
	 * A status sample key for {@link AtmosphericDatum#getSkyConditions()}
	 * values.
	 */
	String SKY_CONDITIONS_KEY = "sky";

	/**
	 * An status sample key for a bitmask of
	 * {@link net.solarnetwork.domain.SkyCondition#getCode()} values.
	 */
	String SKY_CONDITION_CODES_KEY = "skies";

	/**
	 * A status sample key for {@link AtmosphericDatum#getWindSpeed()} values.
	 */
	String WIND_SPEED_KEY = "wspeed";

	/**
	 * A status sample key for {@link AtmosphericDatum#getWindDirection()}
	 * values.
	 */
	String WIND_DIRECTION_KEY = "wdir";

	/**
	 * A status sample key for {@link AtmosphericDatum#getRain()} values.
	 */
	String RAIN_KEY = "rain";

	/**
	 * A status sample key for {@link AtmosphericDatum#getSnow()} values.
	 */
	String SNOW_KEY = "snow";

	/**
	 * A status sample key for {@link AtmosphericDatum#getIrradiance()} values.
	 */
	String IRRADIANCE_KEY = "irradiance";

	/** A tag for an "indoor" atmosphere sample. */
	String TAG_ATMOSPHERE_INDOOR = "indoor";

	/** A tag for an "outdoor" atmosphere sample. */
	String TAG_ATMOSPHERE_OUTDOOR = "outdoor";

	/**
	 * A tag for a forecast atmosphere sample, as opposed to an actual
	 * measurement.
	 */
	String TAG_FORECAST = "forecast";

	/**
	 * Get the instantaneous CO2 level, in parts-per-million.
	 * 
	 * @return the CO2, in parts-per-million
	 */
	default BigDecimal getCO2() {
		return asSampleOperations().getSampleBigDecimal(Instantaneous, CO2_KEY);
	}

	/**
	 * Get the instantaneous luminosity level, in lux.
	 * 
	 * @return the luminosity, in lux
	 */
	default BigDecimal getLux() {
		return asSampleOperations().getSampleBigDecimal(Instantaneous, LUX_KEY);
	}

	/**
	 * Get the instantaneous temperature, in degrees Celsius.
	 * 
	 * @return the temperature, in degrees Celsius
	 */
	default BigDecimal getTemperature() {
		return asSampleOperations().getSampleBigDecimal(Instantaneous, TEMPERATURE_KEY);
	}

	/**
	 * Get the instantaneous dew point, in degrees Celsius.
	 * 
	 * @return the dew point, in degrees Celsius
	 */
	default BigDecimal getDewPoint() {
		return asSampleOperations().getSampleBigDecimal(Instantaneous, DEW_POINT_KEY);
	}

	/**
	 * Get the instantaneous humidity, as an integer percentage (where 100
	 * represents 100%).
	 * 
	 * @return the humidity, as an integer percentage
	 */
	default Integer getHumidity() {
		return asSampleOperations().getSampleInteger(Instantaneous, HUMIDITY_KEY);
	}

	/**
	 * Get the instantaneous atmospheric pressure, in pascals.
	 * 
	 * @return the atmospheric pressure, in pascals
	 */
	default Integer getAtmosphericPressure() {
		return asSampleOperations().getSampleInteger(Instantaneous, ATMOSPHERIC_PRESSURE_KEY);
	}

	/**
	 * Get the instantaneous visibility, in meters.
	 * 
	 * @return visibility, in meters
	 */
	default Integer getVisibility() {
		return asSampleOperations().getSampleInteger(Instantaneous, VISIBILITY_KEY);
	}

	/**
	 * Get a textual description of the sky conditions, e.g. "clear", "cloudy",
	 * etc.
	 * 
	 * @return general sky conditions
	 */
	default String getSkyConditions() {
		return asSampleOperations().getSampleString(Status, SKY_CONDITIONS_KEY);
	}

	/**
	 * Get the wind speed, in meters / second.
	 * 
	 * @return wind speed
	 */
	default BigDecimal getWindSpeed() {
		return asSampleOperations().getSampleBigDecimal(Instantaneous, WIND_SPEED_KEY);
	}

	/**
	 * Get the wind direction, in degrees.
	 * 
	 * @return wind direction
	 */
	default Integer getWindDirection() {
		return asSampleOperations().getSampleInteger(Instantaneous, WIND_DIRECTION_KEY);
	}

	/**
	 * Get the rain accumulation, in millimeters.
	 * 
	 * @return rain accumulation
	 */
	default Integer getRain() {
		return asSampleOperations().getSampleInteger(Instantaneous, RAIN_KEY);
	}

	/**
	 * Get the snow accumulation, in millimeters.
	 * 
	 * @return snow accumulation
	 */
	default Integer getSnow() {
		return asSampleOperations().getSampleInteger(Instantaneous, SNOW_KEY);
	}

	/**
	 * Get the solar irradiance level, in watts / square meter.
	 * 
	 * @return irradiance level
	 */
	default BigDecimal getIrradiance() {
		return asSampleOperations().getSampleBigDecimal(Instantaneous, IRRADIANCE_KEY);
	}

}
