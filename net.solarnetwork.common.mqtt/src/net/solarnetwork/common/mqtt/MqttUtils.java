/* ==================================================================
 * MqttUtils.java - 18/11/2021 3:24:33 PM
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

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import net.solarnetwork.util.StringUtils;

/**
 * Utilities for MQTT.
 * 
 * @author matt
 * @version 1.0
 * @since 2.6
 */
public final class MqttUtils {

	private MqttUtils() {
		// can't construct me
	}

	/**
	 * Validate a topic name according to the MQTT 3.1.1 specification.
	 * 
	 * <p>
	 * This is suitable for validating a topic name for <b>publishing</b> to
	 * MQTT. It does not validate MQTT subscription filters.
	 * </p>
	 * 
	 * @param topic
	 *        the topic name to validate
	 * @throws IllegalArgumentException
	 *         if {@code topic} is not valid
	 */
	public static void validateTopicName(final String topic) {
		validateTopicName(topic, MqttVersion.Mqtt311);
	}

	/**
	 * Validate a topic name according to the MQTT specification.
	 * 
	 * <p>
	 * This is suitable for validating a topic name for <b>publishing</b> to
	 * MQTT. It does not validate MQTT subscription filters.
	 * </p>
	 * 
	 * @param topic
	 *        the topic name to validate
	 * @param version
	 *        the MQTT version to enforce
	 * @throws IllegalArgumentException
	 *         if {@code topic} is not valid
	 */
	public static void validateTopicName(final String topic, final MqttVersion version) {
		if ( topic == null || topic.isEmpty() ) {
			throw new IllegalArgumentException(
					"MQTT topic must be at least one character (MQTT-4.7.3-1).");
		} else if ( StringUtils.utf8length(topic) > 65535 ) {
			throw new IllegalArgumentException(
					"MQTT topic must not be longer than 65535 bytes (MQTT-4.7.3-3).");
		}
		final CharacterIterator itr = new StringCharacterIterator(topic);
		for ( char c = itr.first(); c != CharacterIterator.DONE; c = itr.next() ) {
			switch (c) {
				case '\0':
					throw new IllegalArgumentException(String.format(
							"MQTT topic must not include the null character, found at index %d (MQTT-4.7.3-2).",
							itr.getIndex()));

				case '#':
				case '+':
					throw new IllegalArgumentException(String.format(
							"MQTT topic must not include the subscription wildcard character '%c', found at index %d (MQTT-4.7.1-1).",
							c, itr.getIndex()));

			}
		}
	}

}
