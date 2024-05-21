/* ==================================================================
 * MqttBasicCount.java - 11/06/2018 7:43:25 PM
 *
 * Copyright 2018 SolarNetwork.net Dev Team
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
 * Basic counted fields for MQTT statistics.
 *
 * @since 5.0
 */
public enum MqttBasicCount {

	/** The number of connection attempts. */
	ConnectionAttempts("connection attempts"),

	/** The number of successful connection attempts. */
	ConnectionSuccess("connections made"),

	/** The number of failed connection attempts. */
	ConnectionFail("connections failed"),

	/** The number of connections lost. */
	ConnectionLost("connections lost"),

	/** The number of messages received. */
	MessagesReceived("messages received"),

	/** The number of messages delivered. */
	MessagesDelivered("messages delivered"),

	/** The number of messages that failed to be delivered. */
	MessagesDeliveredFail("failed message deliveries"),

	/** The number of bytes in all messages received. */
	PayloadBytesReceived("payload bytes received"),

	/** The number of bytes in all messages delivered. */
	PayloadBytesDelivered("payload bytes sent");

	private final String description;

	private MqttBasicCount(String description) {
		this.description = description;
	}

	/**
	 * Get a description of the count.
	 *
	 * @return a description
	 */
	public String getDescription() {
		return description;
	}

}
