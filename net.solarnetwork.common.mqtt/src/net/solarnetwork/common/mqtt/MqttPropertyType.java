/* ==================================================================
 * MqttPropertyType.java - 2/05/2021 8:22:28 AM
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
 * Enumeration of MQTT property types.
 * 
 * <p>
 * Adapted from the Netty MQTT codec.
 * </p>
 * 
 * @author matt
 * @version 1.0
 * @since 2.2
 */
public enum MqttPropertyType {

	// single byte properties
	PAYLOAD_FORMAT_INDICATOR(0x01, Integer.class, 1),
	REQUEST_PROBLEM_INFORMATION(0x17, Integer.class, 1),
	REQUEST_RESPONSE_INFORMATION(0x19, Integer.class, 1),
	MAXIMUM_QOS(0x24, Integer.class, 1),
	RETAIN_AVAILABLE(0x25, Integer.class, 1),
	WILDCARD_SUBSCRIPTION_AVAILABLE(0x28, Integer.class, 1),
	SUBSCRIPTION_IDENTIFIER_AVAILABLE(0x29, Integer.class, 1),
	SHARED_SUBSCRIPTION_AVAILABLE(0x2A, Integer.class, 1),

	// two bytes properties
	SERVER_KEEP_ALIVE(0x13, Integer.class, 2),
	RECEIVE_MAXIMUM(0x21, Integer.class, 2),
	TOPIC_ALIAS_MAXIMUM(0x22, Integer.class, 2),
	TOPIC_ALIAS(0x23, Integer.class, 2),

	// four bytes properties
	PUBLICATION_EXPIRY_INTERVAL(0x02, Integer.class, 2),
	SESSION_EXPIRY_INTERVAL(0x11, Integer.class, 2),
	WILL_DELAY_INTERVAL(0x18, Integer.class, 2),
	MAXIMUM_PACKET_SIZE(0x27, Integer.class, 2),

	// Variable Byte Integer
	SUBSCRIPTION_IDENTIFIER(0x0B, Integer.class),

	// UTF-8 Encoded String properties
	CONTENT_TYPE(0x03, String.class),
	RESPONSE_TOPIC(0x08, String.class),
	ASSIGNED_CLIENT_IDENTIFIER(0x12, String.class),
	AUTHENTICATION_METHOD(0x15, String.class),
	RESPONSE_INFORMATION(0x1A, String.class),
	SERVER_REFERENCE(0x1C, String.class),
	REASON_STRING(0x1F, String.class),
	USER_PROPERTY(0x26, KeyValuePair.class),

	// Binary Data
	CORRELATION_DATA(0x09, byte[].class),
	AUTHENTICATION_DATA(0x16, byte[].class);

	private static final MqttPropertyType[] VALUES;

	static {
		VALUES = new MqttPropertyType[43];
		for ( MqttPropertyType v : values() ) {
			VALUES[v.key] = v;
		}
	}

	private final Integer key;
	private final Class<?> valueType;
	private final int length;

	private MqttPropertyType(int key, Class<?> valueType) {
		this(key, valueType, -1);
	}

	private MqttPropertyType(int key, Class<?> valueType, int length) {
		this.key = key;
		this.valueType = valueType;
		this.length = length;
	}

	/**
	 * Get the key value.
	 * 
	 * @return the key
	 */
	public Integer getKey() {
		return key;
	}

	/**
	 * Get the expected value type for this property type.
	 * 
	 * @return the value type, never {@literal null}
	 */
	public Class<?> getValueType() {
		return valueType;
	}

	/**
	 * Get the expected encoding byte length for this property type.
	 * 
	 * @return the length in bytes, or {@literal -1} for variable length
	 */
	public int getLength() {
		return length;
	}

	/**
	 * Get an enum value for a key.
	 * 
	 * @param key
	 *        the key to get the enum for
	 * @return the enum
	 * @throws IllegalArgumentException
	 *         if {@code key} is not a valid value
	 */
	public static MqttPropertyType valueOf(int key) {
		MqttPropertyType t = null;
		try {
			t = VALUES[key];
		} catch ( ArrayIndexOutOfBoundsException e ) {
			// ignore this
		}
		if ( t == null ) {
			throw new IllegalArgumentException("Unknown MQTT property key: " + key);
		}
		return t;
	}

}
