/* ==================================================================
 * Measurand.java - 10/02/2020 11:08:23 am
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
 * A type of sampled value.
 * 
 * @author matt
 * @version 1.0
 */
public enum Measurand {

	Unknown(0),
	CurrentExport(1),
	CurrentImport(2),
	CurrentOffered(17),
	EnergyActiveExportInterval(3),
	EnergyActiveExportRegister(4),
	EnergyActiveImportInterval(5),
	EnergyActiveImportRegister(6),
	EnergyReactiveExportInterval(7),
	EnergyReactiveExportRegister(8),
	EnergyReactiveImportInterval(9),
	EnergyReactiveImportRegister(10),
	Frequency(18),
	PowerActiveExport(11),
	PowerActiveImport(12),
	PowerFactor(19),
	PowerOffered(20),
	PowerReactiveExport(13),
	PowerReactiveImport(14),
	RPM(21),
	SoC(22),
	Temperature(15),
	Voltage(16);

	private final byte code;

	private Measurand(int code) {
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
	public static Measurand forCode(int code) {
		final byte c = (byte) code;
		for ( Measurand v : values() ) {
			if ( v.code == c ) {
				return v;
			}
		}
		return Measurand.Unknown;
	}

}
