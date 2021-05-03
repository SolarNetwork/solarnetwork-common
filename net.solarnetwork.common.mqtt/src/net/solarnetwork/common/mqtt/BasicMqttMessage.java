/* ==================================================================
 * BasicMqttMessage.java - 26/11/2019 4:39:09 pm
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
 * Basic implementation of {@link MqttMessage}.
 * 
 * @author matt
 * @version 1.0
 */
public class BasicMqttMessage implements MqttMessage {

	private final String topic;
	private final boolean retained;
	private final MqttQos qosLevel;
	private final byte[] payload;
	private final MqttProperties properties;

	/**
	 * Constructor.
	 * 
	 * @param topic
	 *        the topic
	 * @param retained
	 *        the retained flag
	 * @param qosLevel
	 *        the quality of service flag
	 * @param payload
	 *        the payload
	 */
	public BasicMqttMessage(String topic, boolean retained, MqttQos qosLevel, byte[] payload) {
		this(topic, retained, qosLevel, payload, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param topic
	 *        the topic
	 * @param retained
	 *        the retained flag
	 * @param qosLevel
	 *        the quality of service flag
	 * @param payload
	 *        the payload
	 * @param properties
	 *        the properties
	 * @since 1.1
	 */
	public BasicMqttMessage(String topic, boolean retained, MqttQos qosLevel, byte[] payload,
			MqttProperties properties) {
		super();
		this.topic = topic;
		this.retained = retained;
		this.qosLevel = qosLevel;
		this.payload = payload;
		this.properties = properties;
	}

	@Override
	public String getTopic() {
		return topic;
	}

	@Override
	public boolean isRetained() {
		return retained;
	}

	@Override
	public MqttQos getQosLevel() {
		return qosLevel;
	}

	@Override
	public byte[] getPayload() {
		return payload;
	}

	@Override
	public MqttProperties getProperties() {
		return properties;
	}

}
