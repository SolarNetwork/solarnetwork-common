/* ==================================================================
 * MqttQos.java - 23/11/2019 5:27:14 pm
 * 
 * Copyright 2019 SolarNetwork.net Dev Team
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

/**
 * MQTT quality of service enumeration.
 * 
 * @author matt
 * @version 1.0
 */
public enum MqttQos {

	/** At most once ({literal 0}). */
	AtMostOnce(0),

	/** At least once ({literal 1}). */
	AtLeastOnce(1),

	/** Exactly once ({literal 2}). */
	ExactlyOnce(2);

	private final int value;

	private MqttQos(int value) {
		this.value = value;
	}

	/**
	 * Get the MQTT QoS value.
	 * 
	 * @return the QoS value
	 */
	public int getValue() {
		return value;
	}

	/**
	 * Get an enumeration for a QoS value.
	 * 
	 * @param value
	 *        the QoS value
	 * @return the associated enum
	 * @throws IllegalArgumentException
	 *         if {@code value} is not supported
	 */
	public static MqttQos valueOf(int value) {
		switch (value) {
			case 0:
				return MqttQos.AtMostOnce;

			case 1:
				return MqttQos.AtLeastOnce;

			case 2:
				return MqttQos.ExactlyOnce;

			default:
				throw new IllegalArgumentException("QoS value not supported: " + value);

		}
	}

}
