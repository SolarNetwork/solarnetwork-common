/* ==================================================================
 * MqttConnectReturnCode.java - 25/11/2019 7:59:39 am
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
 * Enumeration of MQTTconnection result codes.
 * 
 * @author matt
 * @version 1.0
 */
public enum MqttConnectReturnCode {

	Accepted((byte) 0x0),

	UnacceptableProtocolVersion((byte) 0x1),

	ClientIdRejected((byte) 0x02),

	ServerUnavailable((byte) 0x03),

	BadCredentials((byte) 0x04),

	NotAuthorized((byte) 0x05);

	private final byte byteValue;

	private MqttConnectReturnCode(byte byteValue) {
		this.byteValue = byteValue;
	}

	public byte byteValue() {
		return byteValue;
	}

	/**
	 * Get an enumeration for a connect return code value.
	 * 
	 * @param value
	 *        the return code value
	 * @return the associated enum
	 * @throws IllegalArgumentException
	 *         if {@code value} is not supported
	 */
	public static MqttConnectReturnCode valueOf(int value) {
		return valueOf((byte) (value & 0xFF));
	}

	/**
	 * Get an enumeration for a connect return code value.
	 * 
	 * @param value
	 *        the return code value
	 * @return the associated enum
	 * @throws IllegalArgumentException
	 *         if {@code value} is not supported
	 */
	public static MqttConnectReturnCode valueOf(byte value) {
		switch (value) {
			case 0x0:
				return Accepted;

			case 0x1:
				return UnacceptableProtocolVersion;

			case 0x2:
				return ClientIdRejected;

			case 0x3:
				return ServerUnavailable;

			case 0x4:
				return BadCredentials;

			case 0x5:
				return NotAuthorized;
		}
		throw new IllegalArgumentException("Connect return code value not supported: " + (value & 0xFF));
	}

}
