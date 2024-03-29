/* ==================================================================
 * AuthorizationStatus.java - 9/02/2020 5:15:55 pm
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
 * Status of an authorization.
 *
 * @author matt
 * @version 1.1
 */
public enum AuthorizationStatus implements CodedValue {

	/** None. */
	None(0),

	/** Accpeted. */
	Accepted(1),

	/** Blocked. */
	Blocked(2),

	/** Expired. */
	Expired(3),

	/** Invalid. */
	Invalid(4),

	/** Concurrent transaction. */
	ConcurrentTx(5),

	/**
	 * Identifier is valid, but EV Driver doesn’t have enough credit to start
	 * charging. Not allowed for charging.
	 *
	 * @since 1.1
	 */
	NoCredit(6),

	/**
	 * Identifier is valid, but not allowed to charge at this type of EVSE.
	 *
	 * @since 1.1
	 */
	NotAllowedTypeEVSE(7),

	/**
	 * Identifier is valid, but not allowed to charge at this location.
	 *
	 * @since 1.1
	 */
	NotAtThisLocation(8),

	/**
	 * Identifier is valid, but not allowed to charge at this location at this
	 * time.
	 *
	 * @since 1.1
	 */
	NotAtThisTime(9),

	/**
	 * Identifier is unknown. Not allowed for charging.
	 *
	 * @since 1.1
	 */
	Unknown(10),

	;

	private final byte code;

	private AuthorizationStatus(int code) {
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
	 * @return the status, never {@literal null} and set to {@link #None} if not
	 *         any other valid code
	 */
	public static AuthorizationStatus forCode(int code) {
		switch (code) {
			case 1:
				return Accepted;

			case 2:
				return Blocked;

			case 3:
				return Expired;

			case 4:
				return Invalid;

			case 5:
				return ConcurrentTx;

			case 6:
				return NoCredit;

			case 7:
				return NotAllowedTypeEVSE;

			case 8:
				return NotAtThisLocation;

			case 9:
				return NotAtThisTime;

			case 10:
				return Unknown;

			default:
				return None;
		}
	}
}
