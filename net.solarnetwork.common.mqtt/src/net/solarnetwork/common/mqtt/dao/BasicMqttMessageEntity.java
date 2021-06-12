/* ==================================================================
 * BasicMqttMessageEntity.java - 11/06/2021 4:16:32 PM
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

package net.solarnetwork.common.mqtt.dao;

import java.time.Instant;
import net.solarnetwork.common.mqtt.MqttMessage;
import net.solarnetwork.common.mqtt.MqttProperties;
import net.solarnetwork.common.mqtt.MqttQos;
import net.solarnetwork.dao.BasicLongEntity;

/**
 * Basic implementation of {@link MqttMessageEntity}.
 * 
 * <p>
 * Note that the {@link MqttMessage#getProperties()} property is <b>not</b>
 * supported.
 * </p>
 * 
 * @author matt
 * @version 1.0
 * @since 2.5
 */
public class BasicMqttMessageEntity extends BasicLongEntity implements MqttMessageEntity {

	private static final long serialVersionUID = 5975450598082657522L;

	private final String destination;
	private final String topic;
	private final boolean retained;
	private final MqttQos qos;
	private final byte[] payload;

	/**
	 * Default constructor.
	 */
	public BasicMqttMessageEntity() {
		this(null, null, null, null, false, null, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *        the primary key
	 * @param created
	 *        the created date
	 * @param destination
	 *        the destination
	 * @param message
	 *        the message
	 */
	public BasicMqttMessageEntity(Long id, Instant created, String destination, MqttMessage message) {
		this(id, created, destination, message.getTopic(), message.isRetained(), message.getQosLevel(),
				message.getPayload());
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *        the primary key
	 * @param created
	 *        the created date
	 * @param destination
	 *        the destination
	 * @param topic
	 *        the topic
	 * @param retained
	 *        the retained flag
	 * @param qos
	 *        the QoS setting
	 * @param payload
	 *        the message payload
	 */
	public BasicMqttMessageEntity(Long id, Instant created, String destination, String topic,
			boolean retained, MqttQos qos, byte[] payload) {
		super(id, created);
		this.destination = destination;
		this.topic = topic;
		this.retained = retained;
		this.qos = qos;
		this.payload = payload;
	}

	/**
	 * Create a new entity from a message.
	 * 
	 * @param destination
	 *        the destination
	 * @param message
	 *        the message
	 * @return the entity
	 */
	public static BasicMqttMessageEntity forMessage(String destination, MqttMessage message) {
		return new BasicMqttMessageEntity(null, Instant.now(), destination, message);
	}

	@Override
	public BasicMqttMessageEntity clone() {
		return (BasicMqttMessageEntity) super.clone();
	}

	/**
	 * Create a copy with a specific ID.
	 * 
	 * @param id
	 *        the ID to assign
	 * @return the new entity
	 */
	public BasicMqttMessageEntity withId(Long id) {
		return new BasicMqttMessageEntity(id, getCreated(), destination, this);
	}

	@Override
	public String getDestination() {
		return destination;
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
		return qos;
	}

	@Override
	public byte[] getPayload() {
		return payload;
	}

	@Override
	public MqttProperties getProperties() {
		return null;
	}

}
