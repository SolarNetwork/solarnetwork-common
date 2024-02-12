/* ==================================================================
 * MessageType.java - 31/01/2020 6:46:22 am
 * 
 * Copyright 2020 SolarNetwork.net Dev Team
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

package net.solarnetwork.ocpp.json;

/**
 * An enumeration of OCPP JSON message types.
 * 
 * @author matt
 * @version 1.0
 */
public enum MessageType {

	/** A message request, client to server. */
	Call(2),

	/** A message response, server to client. */
	CallResult(3),

	/** A message error response, server to client. */
	CallError(4);

	private int number;

	private MessageType(int number) {
		this.number = (short) number;
	}

	/**
	 * Get the message type number value.
	 * 
	 * @return the number value
	 */
	public int getNumber() {
		return number;
	}

	/**
	 * Get an enumeration for a number value.
	 * 
	 * @param number
	 *        the message type number to get the associated enumeration for
	 * @return the enumeration value
	 * @throws IllegalArgumentException
	 *         if {@code number} is not a valid value
	 */
	public static MessageType forNumber(int number) {
		switch (number) {
			case 2:
				return Call;

			case 3:
				return CallResult;

			case 4:
				return CallError;

			default:
				throw new IllegalArgumentException("Message type number " + number + " not supported.");
		}
	}

}
