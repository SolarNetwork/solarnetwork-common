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
 * @version 1.1
 */
public enum UnitOfMeasure implements CodedValue {

	/** Unknown. */
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
	Percent(18),

	/** Arbitrary strength unit (signal strength). */
	ASU(19),

	/** Bytes. */
	B(20),

	/** Decibel. */
	dB(21),

	/** Power relative to 1mW. */
	dBm(22),

	/** Degrees (angle/rotation). */
	Deg(23),

	/** Hertz (frequency). */
	Hz(24),

	/** Lux (light intensity). */
	lx(25),

	/** Meter. */
	m(26),

	/** Meters per second-squared (acceleration). */
	ms2(27),

	/** Newtons (force). */
	N(28),

	/** Ohm (impedance). */
	Ohm(29),

	/** Kilo pascal (pressure). */
	kPa(30),

	/** Relative humidity percent. */
	RH(31),

	/** Revolutions per minute. */
	RPM(32),

	/** Seconds. */
	s(33),

	/** Kilo volt-ampere hours. */
	kVAh(34),

	;

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
