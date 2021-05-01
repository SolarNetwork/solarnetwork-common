/* ==================================================================
 * BasicMutableMqttProperties.java - 2/05/2021 8:34:30 AM
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

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import net.solarnetwork.common.mqtt.MqttProperties.MutableMqttProperties;
import net.solarnetwork.domain.KeyValuePair;
import net.solarnetwork.util.UnionIterator;

/**
 * Basic implementation of {@link MutableMqttProperties}.
 * 
 * @author matt
 * @version 1.0
 * @since 2.2
 */
public class BasicMutableMqttProperties implements MutableMqttProperties {

	private final Map<MqttPropertyType, MqttProperty<?>> properties;
	private final List<MqttProperty<KeyValuePair>> userProperties;
	private final List<MqttProperty<Integer>> subscriptionIds;

	/**
	 * Constructor.
	 */
	public BasicMutableMqttProperties() {
		super();
		this.properties = new ConcurrentSkipListMap<>();
		this.userProperties = new CopyOnWriteArrayList<>();
		this.subscriptionIds = new CopyOnWriteArrayList<>();
	}

	@Override
	public MqttProperty<?> getProperty(MqttPropertyType type) {
		switch (type) {
			case USER_PROPERTY:
				return userProperties.iterator().next();

			case SUBSCRIPTION_IDENTIFIER:
				return subscriptionIds.iterator().next();

			default:
				return properties.get(type);
		}
	}

	@Override
	public List<? extends MqttProperty<?>> getAllProperties(MqttPropertyType type) {
		switch (type) {
			case USER_PROPERTY:
				return userProperties;

			case SUBSCRIPTION_IDENTIFIER:
				return subscriptionIds;

			default:
				MqttProperty<?> prop = properties.get(type);
				return (prop != null ? Collections.singletonList(prop) : Collections.emptyList());
		}
	}

	@Override
	public boolean isEmpty() {
		return properties.isEmpty() && userProperties.isEmpty() && subscriptionIds.isEmpty();
	}

	@Override
	public void clear() {
		properties.clear();
		userProperties.clear();
		subscriptionIds.clear();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Iterator<MqttProperty<?>> iterator() {
		Iterator<MqttProperty<?>> propsItr = properties.values().iterator();
		Iterator<MqttProperty<?>> userPropsItr = (Iterator) userProperties.iterator();
		Iterator<MqttProperty<?>> subsItr = (Iterator) subscriptionIds.iterator();
		List<Iterator<MqttProperty<?>>> itrs = Arrays.asList(propsItr, userPropsItr, subsItr);
		return new UnionIterator<MqttProperty<?>>(itrs);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addProperty(MqttProperty<?> property) {
		if ( property == null || property.getType() == null ) {
			return;
		}
		switch (property.getType()) {
			case USER_PROPERTY:
				if ( property.getValue() instanceof KeyValuePair ) {
					userProperties.add((MqttProperty<KeyValuePair>) property);
				}
				break;

			case SUBSCRIPTION_IDENTIFIER:
				if ( property.getValue() instanceof Integer ) {
					subscriptionIds.add((MqttProperty<Integer>) property);
				}
				break;

			default:
				properties.put(property.getType(), property);
		}
	}

}
