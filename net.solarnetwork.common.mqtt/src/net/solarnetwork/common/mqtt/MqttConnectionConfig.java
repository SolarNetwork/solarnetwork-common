/* ==================================================================
 * MqttConnectionConfig.java - 24/11/2019 12:32:05 pm
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

import java.util.UUID;
import net.solarnetwork.support.SSLService;

/**
 * API for MQTT connection configuration.
 * 
 * @author matt
 * @version 1.0
 */
public interface MqttConnectionConfig {

	/**
	 * The default MQTT port.
	 */
	int DEFAULT_PORT = 1883;

	/**
	 * The default MQTT port over SSL.
	 */
	int DEFAULT_PORT_SSL = 8883;

	/**
	 * Get the MQTT hostname to connect to.
	 * 
	 * @return the host name
	 */
	String getHost();

	/**
	 * Get the port to connect to.
	 * 
	 * @return the port
	 */
	int getPort();

	/**
	 * Get the MQTT protocol version.
	 * 
	 * @return the version
	 */
	MqttVersion getVersion();

	/**
	 * Get a SSL service to use for SSL-encrypted connections.
	 * 
	 * @return the SSL service, or {@literal null} for an unencrypted connection
	 */
	SSLService getSslService();

	/**
	 * Get the MQTT client ID.
	 * 
	 * @return the client ID
	 */
	String getClientId();

	/**
	 * Get the username.
	 * 
	 * @return the username
	 */
	String getUsername();

	/**
	 * Get the password.
	 * 
	 * @return the password
	 */
	String getPassword();

	/**
	 * Get the MQTT "clean session" flag.
	 * 
	 * @return {@literal true} to use a clean session
	 */
	boolean isCleanSession();

	/**
	 * Get a "last will" message.
	 * 
	 * @return the last will, or {@literal null}
	 */
	MqttMessage getLastWill();

	/**
	 * Get the maximum message payload size, in bytes.
	 * 
	 * @return the maximum message payload size
	 */
	int getMaximumMessageSize();

	/**
	 * Get a "keep alive" time, in seconds.
	 * 
	 * @return the keep alive time, in seconds
	 */
	int getKeepAliveSeconds();

	/**
	 * Get a "reconnect" flag.
	 * 
	 * @return {@literal true} to automatically reconnect
	 */
	boolean isReconnect();

	/**
	 * Get the reconnection delay, in seconds.
	 * 
	 * @return the delay, in seconds
	 */
	int getReconnectDelaySeconds();

	/**
	 * Generate a random client ID.
	 * 
	 * @param prefix
	 *        an optional prefix for the generated ID, or {@literal null} for
	 *        none
	 * @return the generated ID
	 */
	static String randomClientId(String prefix) {
		return (prefix != null ? prefix : "") + UUID.randomUUID().toString().replaceAll("-", "");
	}

}
