/* ==================================================================
 * ProtobufMessagePopulator.java - 25/04/2021 8:15:22 AM
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

package net.solarnetwork.common.protobuf;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;

/**
 * FIXME
 * 
 * <p>
 * TODO
 * </p>
 * 
 * @author matt
 * @version 1.0
 */
public class ProtobufMessagePopulator {

	private static final Logger log = LoggerFactory.getLogger(ProtobufMessagePopulator.class);

	private final ClassLoader classLoader;
	private final String propertyPath;
	private final Message.Builder messageBuilder;

	private Map<String, ProtobufMessagePopulator> nestedPopulatorMap = null;

	/**
	 * Constructor.
	 * 
	 * @param classLoader
	 *        the class loader to use
	 * @param messageClassName
	 *        the root protobuf {@link Message} class name
	 * @throws IllegalArgumentException
	 *         if any error occurs obtaining a {@link Builder} instance for the
	 *         given {@code messageClassName}
	 */
	public ProtobufMessagePopulator(ClassLoader classLoader, String messageClassName) {
		super();
		this.classLoader = classLoader;
		try {
			this.messageBuilder = builderForMessageClassName(messageClassName);
		} catch ( ClassNotFoundException | NoSuchMethodException | SecurityException
				| IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
			throw new IllegalArgumentException(
					String.format("Unable to create a protobuf Builder for class [%s]: %s",
							messageClassName, e.getMessage()),
					e);
		}
		this.propertyPath = "";
	}

	/**
	 * Constructor.
	 * 
	 * @param classLoader
	 *        the class loader to use
	 * @param messageBuilder
	 *        the builder to use
	 * @param propertyPath
	 *        the property path of this builder
	 * @throws IllegalArgumentException
	 *         if any error occurs obtaining a {@link Builder} instance for the
	 *         given {@code messageClassName}
	 */
	public ProtobufMessagePopulator(ClassLoader classLoader, Message.Builder messageBuilder,
			String propertyPath) {
		super();
		this.classLoader = classLoader;
		this.messageBuilder = messageBuilder;
		this.propertyPath = propertyPath;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Class<? extends Message> loadMessageClass(String className) throws ClassNotFoundException {
		return (Class) classLoader.loadClass(className);
	}

	private Builder builderForMessageClassName(String className)
			throws ClassNotFoundException, NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class<? extends Message> clazz = loadMessageClass(className);
		Method m = clazz.getMethod("newBuilder");
		return (Message.Builder) m.invoke(null);
	}

	/**
	 * Perform a batch update from a Map.
	 * 
	 * @param o
	 *        the
	 * @param values
	 *        Map to take properties from. Contains property value objects,
	 *        keyed by property name
	 * @throws IllegalArgumentException
	 *         if there is no such property or if the property isn't writable
	 */
	public void setMessageProperties(Map<String, ?> values, boolean ignoreErrors) {
		for ( Map.Entry<String, ?> me : values.entrySet() ) {
			setMessageProperty(me.getKey(), me.getValue(), ignoreErrors);
		}
	}

	public void setMessageProperty(final String key, final Object value, final boolean ignoreErrors) {
		final int dotIdx = key.indexOf('.');
		if ( dotIdx == 0 ) {
			// skip property name starting with dot
			log.trace("Ignoring property name with leading '.': {}", key);
			return;
		} else if ( dotIdx + 1 >= key.length() ) {
			// skip property name ending with dot
			log.trace("Ignoring property name with trailing '.': {}", key);
			return;
		} else if ( dotIdx > 0 ) {
			// nested property
			final String nestedPropName = key.substring(0, dotIdx);
			final FieldDescriptor nestedFieldDesc = descriptorForPropertyName(nestedPropName);
			if ( nestedFieldDesc == null ) {
				if ( ignoreErrors ) {
					log.trace("Ignoring unknown field [{}] in message class [{}]", nestedPropName,
							messageBuilder.getDescriptorForType().getFullName());
					return;
				}
				throw new IllegalArgumentException(
						String.format("Field [%s] not found in message class [%s]", nestedPropName,
								messageBuilder.getDescriptorForType().getFullName()));
			}
			if ( nestedPopulatorMap == null ) {
				nestedPopulatorMap = new HashMap<>(4);
			}
			ProtobufMessagePopulator nestedPopulator = nestedPopulatorMap.computeIfAbsent(nestedPropName,
					k -> {
						final Message.Builder nestedBuilder = messageBuilder
								.getFieldBuilder(nestedFieldDesc);
						final String fullNestedPropName = (propertyPath.isEmpty() ? nestedPropName
								: String.format("%s.%s", propertyPath, nestedPropName));
						return new ProtobufMessagePopulator(classLoader, nestedBuilder,
								fullNestedPropName);
					});
			final String nestedPropPath = key.substring(dotIdx + 1);
			nestedPopulator.setMessageProperty(nestedPropPath, value, ignoreErrors);
		} else {
			FieldDescriptor fieldDesc = descriptorForPropertyName(key);
			if ( fieldDesc == null ) {
				if ( ignoreErrors ) {
					log.trace("Ignoring unknown field [{}] in message class [{}]", key,
							messageBuilder.getDescriptorForType().getFullName());
					return;
				}
				throw new IllegalArgumentException(
						String.format("Field [%s] not found in message class [%s]", key,
								messageBuilder.getDescriptorForType().getFullName()));
			}
			try {
				messageBuilder.setField(fieldDesc, value);
			} catch ( RuntimeException e ) {
				Throwable t = e;
				while ( t.getCause() != null ) {
					t = t.getCause();
				}
				throw new IllegalArgumentException(String.format(
						"Error setting field [%s] in message class [%s] to value [%s]: %s", key,
						messageBuilder.getDescriptorForType().getFullName(), value, t.toString()), e);
			}
		}
	}

	private FieldDescriptor descriptorForPropertyName(String propName) {
		Descriptor desc = messageBuilder.getDescriptorForType();
		for ( FieldDescriptor f : desc.getFields() ) {
			if ( propName.equals(f.getName()) ) {
				return f;
			}
		}
		return null;
	}

	public Message.Builder getMessageBuilder() {
		return messageBuilder;
	}

	public Message build() {
		return messageBuilder.build();
	}

}
