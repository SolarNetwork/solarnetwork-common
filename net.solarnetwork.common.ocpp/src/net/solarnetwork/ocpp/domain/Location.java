/* ==================================================================
 * Location.java - 10/02/2020 9:38:09 am
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

/**
 * Enumeration of OCPP location values.
 * 
 * @author matt
 * @version 1.0
 */
public enum Location {

	Body(1),

	Cable(2),

	EV(3),

	Inlet(4),

	Outlet(0);

	private final byte code;

	private Location(int code) {
		this.code = (byte) code;
	}

	/**
	 * Get the code value.
	 * 
	 * @return the code value
	 */
	public int codeValue() {
		return code & 0xFF;
	}

	/**
	 * Get an enumeration value for a code value.
	 * 
	 * @param code
	 *        the code
	 * @return the status, never {@literal null} and set to {@link #Outlet} if
	 *         not any other valid code
	 */
	public static Location forCode(int code) {
		switch (code) {
			case 1:
				return Body;

			case 2:
				return Cable;

			case 3:
				return EV;

			case 4:
				return Inlet;

			default:
				return Outlet;
		}
	}

}
