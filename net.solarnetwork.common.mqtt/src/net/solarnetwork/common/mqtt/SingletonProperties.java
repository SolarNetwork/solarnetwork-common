/* ==================================================================
 * SingletonProperties.java - 2/05/2021 2:11:22 PM
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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Helper class for a singleton property {@link MqttProperties} instance.
 * 
 * @author matt
 * @version 1.0
 * @since 2.2
 */
public class SingletonProperties implements MqttProperties {

	private final MqttProperty<?> property;
	private final List<MqttProperty<?>> list;

	/**
	 * Constructor.
	 * 
	 * @param property
	 *        the property
	 */
	public SingletonProperties(MqttProperty<?> property) {
		super();
		this.property = property;
		this.list = Collections.singletonList(property);
	}

	/**
	 * Create a singleton instance from a property type and value.
	 * 
	 * @param <T>
	 *        the property value type
	 * @param type
	 *        the property type
	 * @param value
	 *        the value
	 * @return the singleton properties instance
	 */
	public static <T> MqttProperties property(MqttPropertyType type, T value) {
		return new SingletonProperties(new BasicMqttProperty<T>(type, value));
	}

	@Override
	public Iterator<MqttProperty<?>> iterator() {
		return list.iterator();
	}

	@Override
	public MqttProperty<?> getProperty(MqttPropertyType type) {
		return (type == property.getType() ? property : null);
	}

	@Override
	public List<? extends MqttProperty<?>> getAllProperties(MqttPropertyType type) {
		return (type == property.getType() ? list : null);
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

}
