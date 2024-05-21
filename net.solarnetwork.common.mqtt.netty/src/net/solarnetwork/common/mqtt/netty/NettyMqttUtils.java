/* ==================================================================
 * NettyMqttUtils.java - 26/11/2019 3:35:35 pm
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

package net.solarnetwork.common.mqtt.netty;

import io.netty.handler.codec.mqtt.MqttQoS;
import net.solarnetwork.common.mqtt.MqttQos;

/**
 * Utility helper methods for Netty MQTT.
 *
 * @author matt
 * @version 1.0
 */
public final class NettyMqttUtils {

	private NettyMqttUtils() {
		// not available
	}

	/**
	 * Get a {@link MqttQos} for a {@link MqttQoS}.
	 *
	 * @param qos
	 *        the quality of service to convert
	 * @return the converted quality of service
	 */
	public static MqttQos qosLevel(MqttQoS qos) {
		if ( qos == null ) {
			return MqttQos.AtLeastOnce;
		}
		switch (qos) {
			case EXACTLY_ONCE:
				return MqttQos.ExactlyOnce;

			case AT_LEAST_ONCE:
				return MqttQos.AtLeastOnce;

			default:
				return MqttQos.AtMostOnce;
		}
	}

	/**
	 * Get a {@link MqttQoS} for a {@link MqttQos}.
	 *
	 * @param qosLevel
	 *        the quality of service to convert
	 * @return the converted quality of service
	 */
	public static MqttQoS qos(MqttQos qosLevel) {
		if ( qosLevel == null ) {
			return MqttQoS.AT_LEAST_ONCE;
		}
		switch (qosLevel) {
			case ExactlyOnce:
				return MqttQoS.EXACTLY_ONCE;

			case AtLeastOnce:
				return MqttQoS.AT_LEAST_ONCE;

			default:
				return MqttQoS.AT_MOST_ONCE;
		}
	}
}
