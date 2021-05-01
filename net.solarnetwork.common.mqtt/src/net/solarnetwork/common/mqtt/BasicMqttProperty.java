/* ==================================================================
 * BasicMqttProperty.java - 2/05/2021 8:38:37 AM
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

import java.util.Objects;

/**
 * Basic implementation of {@link MqttProperty}.
 * 
 * @author matt
 * @version 1.0
 * @since 2.2
 */
public class BasicMqttProperty<T> implements MqttProperty<T> {

	private final MqttPropertyType type;
	private final T value;

	/**
	 * Constructor.
	 * 
	 * @param type
	 *        the type
	 * @param value
	 *        the value
	 */
	public BasicMqttProperty(MqttPropertyType type, T value) {
		super();
		this.type = type;
		this.value = value;
	}

	@Override
	public MqttPropertyType getType() {
		return type;
	}

	@Override
	public T getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, value);
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( !(obj instanceof BasicMqttProperty) ) {
			return false;
		}
		BasicMqttProperty<?> other = (BasicMqttProperty<?>) obj;
		return type == other.type && Objects.equals(value, other.value);
	}

}
