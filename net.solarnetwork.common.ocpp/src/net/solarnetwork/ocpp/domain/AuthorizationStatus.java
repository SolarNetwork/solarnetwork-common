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
 * @version 1.0
 */
public enum AuthorizationStatus implements CodedValue {

	None(0),

	Accepted(1),

	Blocked(2),

	Expired(3),

	Invalid(4),

	ConcurrentTx(5);

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

			default:
				return None;
		}
	}
}
