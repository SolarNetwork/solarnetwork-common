/* ==================================================================
 * MqttPubackReasonCode.java - 28/05/2021 6:34:52 PM
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

package net.solarnetwork.common.mqtt.netty.client;

/**
 * MQTT {@literal PUBACK} reason codes.
 * 
 * @author matt
 * @version 1.0
 */
public enum MqttPubackReasonCode {

	Success((byte) 0x00),

	NoSubscribers((byte) 0x10),

	UnspecifiedError((byte) 0x80),

	ImplementationSpecificError((byte) 0x83),

	NotAuthorized((byte) 0x87),

	TopicNameInvalid((byte) 0x90),

	PacketIdentifierInUse((byte) 0x91),

	QuotaExceeded((byte) 0x97),

	PayloadFormatInvalid((byte) 0x99),

	;

	private final byte code;

	private MqttPubackReasonCode(byte code) {
		this.code = code;
	}

	/**
	 * Get the code value.
	 * 
	 * @return the code
	 */
	public byte getCode() {
		return code;
	}

	/**
	 * Get an enum value for a code.
	 * 
	 * @param code
	 *        the code value
	 * @return the enum instance
	 * @throws IllegalArgumentException
	 *         if {@literal code} is not supported
	 */
	public static MqttPubackReasonCode forCode(byte code) {
		switch (code) {
			case (byte) 0x00:
				return Success;
			case (byte) 0x10:
				return NoSubscribers;
			case (byte) 0x80:
				return UnspecifiedError;
			case (byte) 0x83:
				return ImplementationSpecificError;
			case (byte) 0x87:
				return NotAuthorized;
			case (byte) 0x90:
				return TopicNameInvalid;
			case (byte) 0x91:
				return PacketIdentifierInUse;
			case (byte) 0x97:
				return QuotaExceeded;
			case (byte) 0x99:
				return PayloadFormatInvalid;

			default:
				throw new IllegalArgumentException("Unsupported reason code: " + code);
		}
	}

}
