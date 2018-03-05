/* ===================================================================
 * PropertySerializerRegistrar.java
 * 
 * Created Sep 24, 2009 2:23:43 PM
 * 
 * Copyright (c) 2009 Solarnetwork.net Dev Team.
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
 * ===================================================================
 */

package net.solarnetwork.util;

import java.util.Map;

/**
 * A registrar of {@link PropertySerializer} implementations mapped to specific
 * object types or property names.
 * 
 * <p>
 * The configurable properties of this class are:
 * </p>
 * 
 * <dl class="class-properties">
 * <dt>propertySerializers</dt>
 * <dd>A property name mapping to {@link PropertySerializer} implementations for
 * serializing those properties with.</dd>
 * 
 * <dt>classSerializers</dt>
 * <dd>A property type mapping to {@link PropertySerializer} implementations for
 * serializing those properties with.</dd>
 * </dl>
 *
 * @author matt
 * @version 1.0
 */
public class PropertySerializerRegistrar {

	private Map<String, PropertySerializer> propertySerializers = null;
	private Map<String, PropertySerializer> classSerializers = null;

	/**
	 * Return a configured {@link PropertySerializer} for either a specific
	 * property name or property type.
	 * 
	 * <p>
	 * The {@code propertySerializers} mappings are consulted first (using the
	 * passed in {@code propertyName} value), and if no match is found there the
	 * {@code classSerializers} mappings are consulted (using the passed in
	 * {@code propertyType} value). If no match is found, <em>null</em> is
	 * returned.
	 * </p>
	 * 
	 * @param propertyName
	 *        the name of the property to serialize
	 * @param propertyType
	 *        the type of property to serialize
	 * @return configured PropertySerializer, or <em>null</em> if none found
	 */
	public PropertySerializer serializerFor(String propertyName, Class<?> propertyType) {
		if ( propertyName != null && propertySerializers != null
				&& propertySerializers.containsKey(propertyName) ) {
			return propertySerializers.get(propertyName);
		}
		if ( propertyType != null && classSerializers != null
				&& classSerializers.containsKey(propertyType.getName()) ) {
			return classSerializers.get(propertyType.getName());
		}
		return null;
	}

	/**
	 * Attempt to serialize a property using a configured
	 * {@link PropertySerializer}, returning {@code propertyValue} if no
	 * matching serializer configured.
	 * 
	 * <p>
	 * This method calls {@link #serializerFor(String, Class)}, and if that
	 * returns a {@link PropertySerializer} the {@code bean},
	 * {@code propertyName}, and {@code propertyValue} will be passed to
	 * {@link PropertySerializer#serialize(Object, String, Object)} and the
	 * result of that returned from this method.
	 * </p>
	 * 
	 * <p>
	 * If no matching serializer is found, {@code propertyValue} is returned
	 * unchanged.
	 * </p>
	 * 
	 * @param propertyName
	 *        the name of the property being serialized
	 * @param propertyType
	 *        the type of property being serialized
	 * @param bean
	 *        the object the property is being serialized from
	 * @param propertyValue
	 *        the value of the property to serialize
	 * @return serialized value
	 */
	public Object serializeProperty(String propertyName, Class<?> propertyType, Object bean,
			Object propertyValue) {
		PropertySerializer ser = serializerFor(propertyName, propertyType);
		if ( ser == null ) {
			return propertyValue;
		}
		return ser.serialize(bean, propertyName, propertyValue);
	}

	/**
	 * @return the propertySerializers
	 */
	public Map<String, PropertySerializer> getPropertySerializers() {
		return propertySerializers;
	}

	/**
	 * @param propertySerializers
	 *        the propertySerializers to set
	 */
	public void setPropertySerializers(Map<String, PropertySerializer> propertySerializers) {
		this.propertySerializers = propertySerializers;
	}

	/**
	 * @return the classSerializers
	 */
	public Map<String, PropertySerializer> getClassSerializers() {
		return classSerializers;
	}

	/**
	 * @param classSerializers
	 *        the classSerializers to set
	 */
	public void setClassSerializers(Map<String, PropertySerializer> classSerializers) {
		this.classSerializers = classSerializers;
	}

}
