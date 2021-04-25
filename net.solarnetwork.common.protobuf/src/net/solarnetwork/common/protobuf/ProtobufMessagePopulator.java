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
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;
import com.google.protobuf.ProtocolMessageEnum;

/**
 * Helper class for dynamically populating properties on Protobuf
 * {@link Message} objects.
 * 
 * <p>
 * This class is <b>not</b> thread safe.
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
	 * <p>
	 * Use this constructor to dynamically instantiate a {@link Message}
	 * instance via the provided class name.
	 * </p>
	 * 
	 * @param classLoader
	 *        the class loader to use
	 * @param messageClassName
	 *        the root Protobuf {@link Message} class name
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
	 *        the property path of this builder, or {@literal null} or an empty
	 *        string if this is a top-level object
	 * @throws IllegalArgumentException
	 *         if any error occurs obtaining a {@link Builder} instance for the
	 *         given {@code messageClassName}
	 */
	public ProtobufMessagePopulator(ClassLoader classLoader, Message.Builder messageBuilder,
			String propertyPath) {
		super();
		this.classLoader = classLoader;
		this.messageBuilder = messageBuilder;
		this.propertyPath = (propertyPath != null ? propertyPath : "");
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
	 * @param values
	 *        Map of property name keys to associated values
	 * @param ignoreErrors
	 *        {@literal true} to ignore errors such as unknown property or type
	 *        conversion exceptions
	 * @throws IllegalArgumentException
	 *         if {@code ignoreErrors} is {@literal false} and there is no such
	 *         property or if the property isn't writable
	 * @see #setMessageProperty(String, Object, boolean)
	 */
	public void setMessageProperties(Map<String, ?> values, boolean ignoreErrors) {
		for ( Map.Entry<String, ?> me : values.entrySet() ) {
			setMessageProperty(me.getKey(), me.getValue(), ignoreErrors);
		}
	}

	/**
	 * Set a single property value.
	 * 
	 * <p>
	 * The {@code key} value supports nested object paths using a {@literal .}
	 * delimiter. For example the key {@literal location.lat} would set the
	 * {@literal lat} property of the {@literal location} object.
	 * </p>
	 * 
	 * @param key
	 *        the property key
	 * @param value
	 *        the property value
	 * @param ignoreErrors
	 *        {@literal true} to ignore errors such as unknown property or type
	 *        conversion exceptions
	 * @see #convertFieldValueIfNecessary(Object, FieldDescriptor)
	 */
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
				final Object fieldValue = convertFieldValueIfNecessary(value, fieldDesc);
				messageBuilder.setField(fieldDesc, fieldValue);
			} catch ( RuntimeException e ) {
				Throwable t = e;
				while ( t.getCause() != null ) {
					t = t.getCause();
				}
				String msg = String.format(
						"Error setting field [%s] in message class [%s] to value [%s]: %s", key,
						messageBuilder.getDescriptorForType().getFullName(), value, t.toString());
				if ( !ignoreErrors ) {
					throw new IllegalArgumentException(msg, e);
				} else {
					log.debug(msg);
				}
			}
		}
	}

	/**
	 * Convert a value to one suitable for a given field descriptor.
	 * 
	 * <p>
	 * The {@literal value} will be converted if necessary to match the message
	 * field type. The following conversions are performed for a given message
	 * field type:
	 * </p>
	 * 
	 * <dl>
	 * <dt>BOOLEAN</dt>
	 * <dd>
	 * <p>
	 * If not already a {@code java.lang.Boolean} then:
	 * </p>
	 * <ol>
	 * <li>If the value is a {@code java.lang.Number} then convert its
	 * {@code int} value of {@literal 0} to {@literal false} and any other value
	 * to {@literal true}.</li>
	 * <li>Otherwise convert the value to a string and convert {@literal "1"},
	 * {@literal "true"}, {@literal "t"}, {@literal "yes"}, and {@literal "y"}
	 * to {@literal true} and any other value to {@literal false}.</li>
	 * </ol>
	 * </dd>
	 * <dt>BYTE_STRING</dt>
	 * <dd>
	 * <p>
	 * If not already a {@code com.google.protobuf.ByteString} then:
	 * </p>
	 * <ol>
	 * <li>If the value is a {@code byte[]} or {@code java.nio.ByteBuffer} then
	 * convert directly to a {@code ByteString}.</li>
	 * <li>Otherwise convert the value to a string and convert to UTF-8
	 * bytes.</li>
	 * </ol>
	 * </dd>
	 * <dt>DOUBLE</dt>
	 * <dd>
	 * <p>
	 * If not already a {@code java.lang.Double} then:
	 * </p>
	 * <ol>
	 * <li>If the value is a {@code java.lang.Number} then return
	 * {@link java.lang.Number#doubleValue()}.</li>
	 * <li>Otherwise convert the value to a string and return
	 * {@link java.lang.Double#valueOf(String)}.</li>
	 * </ol>
	 * </dd>
	 * <dt>ENUM</dt>
	 * <dd>
	 * <p>
	 * If not already a
	 * {@code com.google.protobuf.Descriptors.EnumValueDescriptor} then:
	 * </p>
	 * <ol>
	 * <li>If the value is a {@code com.google.protobuf.ProtocolMessageEnum}
	 * then return {@link ProtocolMessageEnum#getValueDescriptor()}.</li>
	 * <li>If the value is a {@code java.lang.Number} then return
	 * {@link EnumDescriptor#findValueByNumber(int)}.</li>
	 * <li>Otherwise convert the value to a string and return
	 * {@link EnumDescriptor#findValueByName(String)}.</li>
	 * </ol>
	 * </dd>
	 * <dt>FLOAT</dt>
	 * <dd>
	 * <p>
	 * If not already a {@code java.lang.Float} then:
	 * </p>
	 * <ol>
	 * <li>If the value is a {@code java.lang.Number} then return
	 * {@link java.lang.Number#floatValue()}.</li>
	 * <li>Otherwise convert the value to a string and return
	 * {@link java.lang.Float#valueOf(String)}.</li>
	 * </ol>
	 * </dd>
	 * <dt>INT</dt>
	 * <dd>
	 * <p>
	 * If not already a {@code java.lang.Integer} then:
	 * </p>
	 * <ol>
	 * <li>If the value is a {@code java.lang.Number} then return
	 * {@link java.lang.Number#intValue()}.</li>
	 * <li>Otherwise convert the value to a string and return
	 * {@link java.lang.Integer#valueOf(String)}.</li>
	 * </ol>
	 * </dd>
	 * <dt>LONG</dt>
	 * <dd>
	 * <p>
	 * If not already a {@code java.lang.Long} then:
	 * </p>
	 * <ol>
	 * <li>If the value is a {@code java.lang.Number} then return
	 * {@link java.lang.Number#longValue()}.</li>
	 * <li>Otherwise convert the value to a string and return
	 * {@link java.lang.Long#valueOf(String)}.</li>
	 * </ol>
	 * </dd>
	 * <dt>MESSAGE</dt>
	 * <dd>No additional processing is done.</dd>
	 * <dt>STRING</dt>
	 * <dd>If the value is not already a string, then return
	 * {@link Object#toString()}.
	 * </dl>
	 * 
	 * @param value
	 *        the value to convert if necessary to suit the given descriptor
	 * 
	 * @param fieldDesc
	 *        the field descriptor
	 * @return the value
	 * @throws IllegalArgumentException
	 *         if {@code value} cannot be converted to the given field
	 *         descriptor
	 */
	public static Object convertFieldValueIfNecessary(final Object value,
			final FieldDescriptor fieldDesc) {
		if ( value == null ) {
			return null;
		}
		final JavaType fieldType = fieldDesc.getJavaType();
		switch (fieldType) {
			case BOOLEAN:
				if ( !(value instanceof Boolean) ) {
					// if a number, use 0 for false
					if ( value instanceof Number ) {
						return (((Number) value).intValue() != 0);
					}
					String s = value.toString().toLowerCase();
					return ("1".equals(s) || "t".equals(s) || "true".equals(s) || "y".equals(s)
							|| "yes".equals(s));
				}
				break;

			case BYTE_STRING:
				if ( !(value instanceof ByteString) ) {
					if ( value instanceof byte[] ) {
						return ByteString.copyFrom((byte[]) value);
					} else if ( value instanceof ByteBuffer ) {
						return ByteString.copyFrom((ByteBuffer) value);
					}
					return ByteString.copyFromUtf8(value.toString());
				}
				break;

			case DOUBLE:
				if ( !(value instanceof Double) ) {
					if ( value instanceof Number ) {
						return ((Number) value).doubleValue();
					}
					return Double.valueOf(value.toString());
				}
				break;

			case ENUM:
				if ( !(value instanceof EnumValueDescriptor) ) {
					EnumDescriptor enumDesc = fieldDesc.getEnumType();
					if ( value instanceof ProtocolMessageEnum ) {
						return ((ProtocolMessageEnum) value).getValueDescriptor();
					} else if ( value instanceof Number ) {
						return enumDesc.findValueByNumber(((Number) value).intValue());
					}
					return enumDesc.findValueByName(value.toString());
				}
				break;

			case FLOAT:
				if ( !(value instanceof Float) ) {
					if ( value instanceof Number ) {
						return ((Number) value).floatValue();
					}
					return Float.valueOf(value.toString());
				}
				break;

			case INT:
				if ( !(value instanceof Integer) ) {
					if ( value instanceof Number ) {
						return ((Number) value).intValue();
					}
					return Integer.valueOf(value.toString());
				}
				break;

			case LONG:
				if ( !(value instanceof Long) ) {
					if ( value instanceof Number ) {
						return ((Number) value).longValue();
					}
					return Long.valueOf(value.toString());
				}
				break;

			case MESSAGE:
				// nothing here
				break;

			case STRING:
				if ( !(value instanceof String) ) {
					return value.toString();
				}
				break;
		}
		return value;
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
