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

	Unknown(0),

	Available(1),
	/** Occupied: removed in 1.6. */
	Occupied(2),
	Faulted(3),
	Unavailable(4),
	Reserved(5),

	Preparing(6),
	Charging(7),
	SuspendedEV(8),
	SuspendedEVSE(9),
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
