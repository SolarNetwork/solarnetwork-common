/* ==================================================================
 * ChargeSessionEndReason.java - 14/02/2020 3:38:17 pm
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

package net.solarnetwork.ocpp.domain;

import net.solarnetwork.domain.CodedValue;

/**
 * Enumeration of charging session end reasons.
 * 
 * @author matt
 * @version 1.0
 */
public enum ChargeSessionEndReason implements CodedValue {

	/** Unknown. */
	Unknown(0),

	/** Emergency stop button was used. */
	EmergencyStop(1),

	/**
	 * The disconnecting of a cable, or vehicle moved away from inductive charge
	 * unit.
	 */
	EVDisconnected(2),

	/** A hard reset command was received. */
	HardReset(3),

	/**
	 * Stopped locally on request of the user at the Charge Point. This is a
	 * regular termination of a transaction. Examples: presenting an RFID tag,
	 * pressing a button to stop.
	 */
	Local(4),

	/** Any other reason. */
	Other(5),

	/** Complete loss of power. */
	PowerLoss(6),

	/**
	 * A locally initiated reset/reboot occurred (for instance watchdog kicked
	 * in).
	 */
	Reboot(7),

	/**
	 * Stopped remotely on request of the user. This is a regular termination of
	 * a transaction. Examples: termination using a smartphone app, exceeding a
	 * (non local) prepaid credit.
	 */
	Remote(8),

	/** A soft reset command was received. */
	SoftReset(9),

	/** An unlock connector command was received. */
	UnlockCommand(10),

	/**
	 * The transaction was stopped because of the authorization status in a
	 * start transaction.
	 */
	DeAuthorized(11);

	private final byte code;

	private ChargeSessionEndReason(int code) {
		this.code = (byte) code;
	}

	/**
	 * Get the code value.
	 * 
	 * @return the code value
	 */
	@Override
	public int getCode() {
		return code & 0xFF;
	}

	/**
	 * Get an enumeration value for a code value.
	 * 
	 * @param code
	 *        the code
	 * @return the status, never {@literal null} and set to {@link #Unknown} if
	 *         not any other valid code
	 */
	public static ChargeSessionEndReason forCode(int code) {
		final byte c = (byte) code;
		for ( ChargeSessionEndReason v : values() ) {
			if ( v.code == c ) {
				return v;
			}
		}
		return ChargeSessionEndReason.Unknown;
	}

}
