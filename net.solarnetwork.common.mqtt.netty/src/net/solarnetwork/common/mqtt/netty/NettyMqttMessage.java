/* ==================================================================
 * NettyMqttMessage.java - 26/11/2019 3:07:26 pm
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

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.mqtt.MqttQoS;
import net.solarnetwork.common.mqtt.BasicMqttMessage;
import net.solarnetwork.common.mqtt.MqttMessage;

/**
 * Netty implementation of {@link MqttMessage}.
 * 
 * @author matt
 * @version 1.0
 */
public class NettyMqttMessage extends BasicMqttMessage {

	/**
	 * Constructor.
	 * 
	 * @param topic
	 *        the topic
	 * @param retained
	 *        the retained flag
	 * @param qos
	 *        the quality of service flag
	 * @param payload
	 *        the payload
	 */
	public NettyMqttMessage(String topic, boolean retained, MqttQoS qos, ByteBuf payload) {
		super(topic, retained, NettyMqttUtils.qosLevel(qos), bytes(payload));
	}

	private static byte[] bytes(ByteBuf payload) {
		byte[] result = new byte[(payload != null ? payload.readableBytes() : 0)];
		if ( result.length > 0 ) {
			payload.readBytes(result);
		}
		return result;
	}

}
