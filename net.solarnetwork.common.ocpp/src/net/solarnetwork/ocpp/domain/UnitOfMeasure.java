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

import net.solarnetwork.domain.CodedValue;

/**
 * Enumeration of units of measure.
 * 
 * @author matt
 * @version 1.0
 */
public enum UnitOfMeasure implements CodedValue {

	/** Unknonw. */
	Unknown(0),

	/** Watt hours. */
	Wh(1),

	/** Kilo-watt hours. */
	kWh(2),

	/** Volt-amps reactive hours. */
	varh(3),

	/** Kilo-volt-amps reactive hours. */
	kvarh(4),

	/** Watts. */
	W(5),

	/** Kilowatts. */
	kW(6),

	/** Volt-amps reactive. */
	var(7),

	/** Kilo-volt-amps reactive. */
	kvar(8),

	/** Amperes, removed in 1.6, use A. */
	Amp(9),

	/** Volts, removed in 1.6, use V. */
	Volt(10),

	/** Celsius. */
	Celsius(11),

	/** Amperes. */
	A(12),

	/** Fahrenheit. */
	Fahrenheit(13),

	/** Volt-amperes. */
	VA(14),

	/** Kilo-volt-amperes. */
	kVA(15),

	/** Volts. */
	V(16),

	/** Kelvin. */
	K(17),

	/** Percent. */
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
