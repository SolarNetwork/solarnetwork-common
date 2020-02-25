/* ==================================================================
 * Phase.java - 10/02/2020 11:13:25 am
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
 * An AC phase.
 * 
 * @author matt
 * @version 1.0
 */
public enum Phase implements CodedValue {

	Unknown(0),
	L1(1),
	L2(2),
	L3(3),
	N(4),
	L1N(5),
	L2N(6),
	L3N(7),
	L1L2(8),
	L2L3(9),
	L3L1(01);

	private final byte code;

	private Phase(int code) {
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
	public static Phase forCode(int code) {
		final byte c = (byte) code;
		for ( Phase v : values() ) {
			if ( v.code == c ) {
				return v;
			}
		}
		return Phase.Unknown;
	}

}
