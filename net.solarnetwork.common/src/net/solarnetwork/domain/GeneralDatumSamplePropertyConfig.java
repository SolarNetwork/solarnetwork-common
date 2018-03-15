/* ==================================================================
 * GeneralDatumSamplePropertyConfig.java - 14/03/2018 3:45:22 PM
 * 
 * Copyright 2018 SolarNetwork.net Dev Team
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

package net.solarnetwork.domain;

/**
 * Join some sort of configuration with a sample property key and type.
 * 
 * @param V
 *        the configuration type
 * @author matt
 * @version 1.0
 * @since 1.42
 */
public class GeneralDatumSamplePropertyConfig<V> {

	private String propertyKey;
	private GeneralDatumSamplesType propertyType;
	private V config;

	/**
	 * Default constructor.
	 */
	public GeneralDatumSamplePropertyConfig() {
		super();
	}

	/**
	 * Construct with values.
	 * 
	 * @param propertyKey
	 *        the sample property key to use
	 * @param propertyType
	 *        the sample property type to use
	 * @param config
	 *        the configuration to use
	 */
	public GeneralDatumSamplePropertyConfig(String propertyKey, GeneralDatumSamplesType propertyType,
			V config) {
		super();
		setPropertyKey(propertyKey);
		setPropertyType(propertyType);
		setConfig(config);
	}

	/**
	 * Get the sample property key.
	 * 
	 * <p>
	 * This value represents a key in a
	 * {@link GeneralDatumSamples#getSampleData(GeneralDatumSamplesType)} map.
	 * </p>
	 * 
	 * @return the sample property key
	 */
	public String getPropertyKey() {
		return propertyKey;
	}

	/**
	 * Set the sample property key.
	 * 
	 * <p>
	 * This value represents a key in a
	 * {@link GeneralDatumSamples#getSampleData(GeneralDatumSamplesType)} map.
	 * </p>
	 * 
	 * @param propertyKey
	 *        the sample property key
	 */
	public void setPropertyKey(String propertyKey) {
		this.propertyKey = propertyKey;
	}

	/**
	 * Get the sample property type.
	 * 
	 * <p>
	 * This value represents the type to use in a
	 * {@link GeneralDatumSamples#getSampleData(GeneralDatumSamplesType)} map.
	 * </p>
	 * 
	 * @return the sample property type
	 */
	public GeneralDatumSamplesType getPropertyType() {
		return propertyType;
	}

	/**
	 * Set the sample property type.
	 * 
	 * <p>
	 * This value represents the type to use in a
	 * {@link GeneralDatumSamples#getSampleData(GeneralDatumSamplesType)} map.
	 * </p>
	 * 
	 * @param propertyType
	 */
	public void setPropertyType(GeneralDatumSamplesType propertyType) {
		this.propertyType = propertyType;
	}

	/**
	 * Get the property type key.
	 * 
	 * <p>
	 * This returns the configured {@link #getPropertyType()}
	 * {@link GeneralDatumSamplesType#toKey()} value as a string.
	 * </p>
	 * 
	 * @return the property type key
	 */
	public String getPropertyTypeKey() {
		GeneralDatumSamplesType type = getPropertyType();
		if ( type == null ) {
			return null;
		}
		return Character.toString(type.toKey());
	}

	/**
	 * Set the property type via a key value.
	 * 
	 * <p>
	 * This uses the first character of {@code key} as a
	 * {@link GeneralDatumSamplesType} key value to call
	 * {@link #setPropertyType(GeneralDatumSamplesType)}.
	 * </p>
	 * 
	 * @param key
	 *        the datum property type key to set
	 */
	public void setPropertyTypeKey(String key) {
		if ( key == null || key.length() < 1 ) {
			return;
		}
		setPropertyType(GeneralDatumSamplesType.valueOf(key.charAt(0)));
	}

	/**
	 * Get the configuration object.
	 * 
	 * @return the configuration to associate with the sample property key and
	 *         type
	 */
	public V getConfig() {
		return config;
	}

	/**
	 * Set the configuration object.
	 * 
	 * @param config
	 *        the configuration to associate with the sample property key and
	 *        type
	 */
	public void setConfig(V config) {
		this.config = config;
	}

}
