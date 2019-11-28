/* ==================================================================
 * MqttConnectionObserver.java - 26/11/2019 2:45:47 pm
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
 * API for observing connection status changes.
 * 
 * @author matt
 * @version 1.0
 */
public interface MqttConnectionObserver {

	/**
	 * Callback invoked when a connection has been lost.
	 *
	 * @param connection
	 *        the connection that has lost connectivity with the MQTT server
	 * @param willReconnect
	 *        {@literal true} if the connection is configured to automatically
	 *        reconnect
	 * @param cause
	 *        the reason for the connection loss
	 */
	void onMqttServerConnectionLost(MqttConnection connection, boolean willReconnect, Throwable cause);

	/**
	 * Callback invoked when connectivity to a MQTT server has been established.
	 * 
	 * @param connection
	 *        the connection that has been established
	 * @param reconnected
	 *        {@literal true} if this was a reconnection to the MQTT server;
	 *        {@literal false} if this was the initial connection to the MQTT
	 *        server
	 */
	void onMqttServerConnectionEstablisehd(MqttConnection connection, boolean reconnected);

}
