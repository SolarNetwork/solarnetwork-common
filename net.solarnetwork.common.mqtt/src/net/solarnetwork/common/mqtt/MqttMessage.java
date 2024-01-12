/* ==================================================================
 * MqttMessage.java - 23/11/2019 5:20:17 pm
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
 * MQTT message.
 * 
 * @author matt
 * @version 1.1
 */
public interface MqttMessage {

	/**
	 * Get the message topic.
	 * 
	 * @return the topic
	 */
	String getTopic();

	/**
	 * Get the retained flag.
	 * 
	 * @return {@literal true} if the message has the "retain" flag set
	 */
	boolean isRetained();

	/**
	 * Get the quality of service level.
	 * 
	 * @return the QoS level
	 */
	MqttQos getQosLevel();

	/**
	 * Get the message payload.
	 * 
	 * @return the payload
	 */
	byte[] getPayload();

	/**
	 * Get the message properties.
	 * 
	 * @return the properties, or {@literal null}
	 * @since 1.1
	 */
	MqttProperties getProperties();

}
