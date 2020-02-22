/* ==================================================================
 * UnitOfMeasure.java - 10/02/2020 9:48:28 am
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
 * Enumeration of units of measure.
 * 
 * @author matt
 * @version 1.0
 */
public enum UnitOfMeasure {

	Unknown(0),

	Wh(1),

	kWh(2),

	varh(3),

	kvarh(4),

	W(5),

	kW(6),

	var(7),

	kvar(8),

	// removed in 1.6, use A
	Amp(9),

	// removed in 1.6, use V
	Volt(10),

	Celsius(11),

	A(12),

	Fahrenheit(13),

	VA(14),

	kVA(15),

	V(16),

	K(17),

	Percent(18);

	private final byte code;

	private UnitOfMeasure(int code) {
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
	 * @return the status, never {@literal null} and set to {@link #Unknown} if
	 *         not any other valid code
	 */
	public static UnitOfMeasure forCode(int code) {
		final byte c = (byte) code;
		for ( UnitOfMeasure v : values() ) {
			if ( v.code == c ) {
				return v;
			}
		}
		return UnitOfMeasure.Unknown;
	}
}
