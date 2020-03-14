/* ==================================================================
 * ChargePointStatus.java - 12/02/2020 1:06:58 pm
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
 * Charge point status enumeration.
 * 
 * @author matt
 * @version 1.0
 */
public enum ChargePointStatus implements CodedValue {

	/** The connector status it not known. */
	Unknown(0),

	/** The connector is available for a new user. */
	Available(1),

	/** See {@link Charging} - removed in 1.6. */
	Occupied(2),

	/**
	 * When a Charge Point or connector has reported an error and is not
	 * available for energy delivery.
	 */
	Faulted(3),

	/**
	 * When a connector becomes unavailable as the result of a Change
	 * Availability command or an event upon which the Charge Point transitions
	 * to unavailable at its discretion.
	 */
	Unavailable(4),

	/**
	 * When a connector becomes reserved as a result of a ReserveNow command.
	 */
	Reserved(5),

	/**
	 * When a connector becomes no longer available for a new user but there is
	 * no ongoing Transaction (yet). Typically a connector is in preparing state
	 * when a user presents a tag, inserts a cable or a vehicle occupies the
	 * parking bay.
	 */
	Preparing(6),

	/**
	 * When the contactor of a connector closes, allowing the vehicle to charge.
	 */
	Charging(7),

	/**
	 * When the EV is connected to the EVSE and the EVSE is offering energy but
	 * the EV is not taking any energy.
	 */
	SuspendedEV(8),

	/**
	 * When the EV is connected to the EVSE but the EVSE is not offering energy
	 * to the EV, e.g. due to a smart charging restriction, local supply power
	 * constraints, or as the result of StartTransaction.conf indicating that
	 * charging is not allowed etc.
	 */
	SuspendedEVSE(9),

	/**
	 * When a Transaction has stopped at a connector, but the connector is not
	 * yet available for a new user, e.g. the cable has not been removed or the
	 * vehicle has not left the parking bay.
	 */
	Finishing(10);

	private final byte code;

	private ChargePointStatus(int code) {
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
	public static ChargePointStatus forCode(int code) {
		final byte c = (byte) code;
		for ( ChargePointStatus v : values() ) {
			if ( v.code == c ) {
				return v;
			}
		}
		return ChargePointStatus.Unknown;
	}

}
