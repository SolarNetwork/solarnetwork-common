/* ==================================================================
 * ChargingProfileKind.java - 18/02/2020 4:14:31 pm
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
 * Enumeration of charging profile schedule kinds.
 * 
 * @author matt
 * @version 1.0
 */
public enum ChargingProfileKind implements CodedValue {

	/** Unknown kind. */
	Unknown(0),

	/**
	 * Schedule periods are relative to a fixed point in time defined in the
	 * schedule.
	 */
	Absolute(1),

	/** The schedule restarts periodically at the first schedule period. */
	Recurring(2),

	/**
	 * Schedule periods are relative to a situation-specific start point (such
	 * as the start of a transaction) that is determined by the charge point.
	 */
	Relative(3);

	private final byte code;

	private ChargingProfileKind(int code) {
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
	public static ChargingProfileKind forCode(int code) {
		switch (code) {
			case 1:
				return Absolute;

			case 2:
				return Recurring;

			case 3:
				return Relative;

			default:
				return Unknown;
		}
	}

}
