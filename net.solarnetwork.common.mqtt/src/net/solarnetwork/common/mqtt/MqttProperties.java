/* ==================================================================
 * MqttProperties.java - 2/05/2021 8:21:08 AM
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

package net.solarnetwork.common.mqtt;

import java.util.List;

/**
 * API for MQTT properties.
 * 
 * @author matt
 * @version 1.0
 * @since 2.2
 */
public interface MqttProperties extends Iterable<MqttProperty<?>> {

	/**
	 * Get a specific property.
	 * 
	 * <p>
	 * If the property type supports multiple values, this will return the first
	 * available value.
	 * </p>
	 * 
	 * @param type
	 *        the property type
	 * @return the associated property, or {@literal null} if none available
	 */
	MqttProperty<?> getProperty(MqttPropertyType type);

	/**
	 * Get all available properties of a given type.
	 * 
	 * <p>
	 * This is useful for {@link MqttPropertyType#USER_PROPERTY}.
	 * </p>
	 * 
	 * @param type
	 *        the property type
	 * @return the associated properties, never {@literal null}
	 */
	List<? extends MqttProperty<?>> getAllProperties(MqttPropertyType type);

	/**
	 * Test if there are any properties configured.
	 * 
	 * @return {@literal true} if there are no properties configured
	 */
	boolean isEmpty();

	/**
	 * Mutable version of {@link MqttProperties}.
	 */
	interface MutableMqttProperties extends MqttProperties {

		/**
		 * Add a property.
		 * 
		 * <p>
		 * For multi-valued properties (like
		 * {@link MqttPropertyType#USER_PROPERTY}) this adds to the list of
		 * properties of that type. Otherwise this replaces any existing value
		 * for the given type.
		 * </p>
		 * 
		 * @param property
		 *        the property to add
		 */
		void addProperty(MqttProperty<?> property);

		/**
		 * Clear out all configured properties.
		 */
		void clear();

	}

}
