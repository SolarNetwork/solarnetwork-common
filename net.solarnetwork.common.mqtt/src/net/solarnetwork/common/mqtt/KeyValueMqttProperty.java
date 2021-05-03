/* ==================================================================
 * KeyValueMqttProperty.java - 2/05/2021 8:38:11 AM
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

import net.solarnetwork.domain.KeyValuePair;

/**
 * A "key value" MQTT property.
 * 
 * <p>
 * Can be used for user properties, for example.
 * </p>
 * 
 * @author matt
 * @version 1.0
 * @since 2.2
 */
public class KeyValueMqttProperty extends BasicMqttProperty<KeyValuePair> {

	/**
	 * Constructor.
	 * 
	 * @param type
	 *        the type
	 * @param value
	 *        the value
	 */
	public KeyValueMqttProperty(MqttPropertyType type, KeyValuePair value) {
		super(type, value);
	}

	/**
	 * Create a new instance out of a key and value.
	 * 
	 * @param type
	 *        the property type
	 * @param key
	 *        the key
	 * @param value
	 *        the value
	 * @return the new instance
	 */
	public static KeyValueMqttProperty forKeyValue(MqttPropertyType type, String key, String value) {
		return new KeyValueMqttProperty(type, new KeyValuePair(key, value));
	}

	/**
	 * Convenience method to create a new {@link MqttPropertyType#USER_PROPERTY}
	 * type instance.
	 * 
	 * @param key
	 *        the key
	 * @param value
	 *        the value
	 * @return the new instance
	 */
	public static KeyValueMqttProperty userProperty(String key, String value) {
		return forKeyValue(MqttPropertyType.USER_PROPERTY, key, value);
	}

}
