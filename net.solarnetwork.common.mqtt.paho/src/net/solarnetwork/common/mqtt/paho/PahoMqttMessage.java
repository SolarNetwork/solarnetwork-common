/* ==================================================================
 * PahoMqttMessage.java - 27/11/2019 9:57:03 am
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

package net.solarnetwork.common.mqtt.paho;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import net.solarnetwork.common.mqtt.BasicMqttMessage;
import net.solarnetwork.common.mqtt.MqttQos;

/**
 * Paho implementation of {@link net.solarnetwork.common.mqtt.MqttMessage}
 * 
 * @author matt
 * @version 1.0
 */
public class PahoMqttMessage extends BasicMqttMessage {

	/**
	 * Constructor.
	 * 
	 * @param topic
	 *        the topic
	 * @param msg
	 *        the Paho message
	 */
	public PahoMqttMessage(String topic, MqttMessage msg) {
		super(topic, msg.isRetained(), MqttQos.valueOf(msg.getQos()), msg.getPayload());
	}

}
