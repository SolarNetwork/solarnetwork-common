/* ==================================================================
 * ChargePointErrorCode.java - 12/02/2020 1:11:54 pm
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
 * Enumeration of charge point error codes.
 * 
 * @author matt
 * @version 1.0
 */
public enum ChargePointErrorCode {

	Unknown(0),

	ConnectorLockFailure(1),
	HighTemperature(2),
	Mode3Error(3),
	NoError(4),
	PowerMeterFailure(5),
	PowerSwitchFailure(6),
	ReaderFailure(7),
	ResetFailure(8),
	GroundFailure(9),
	OverCurrentFailure(10),
	UnderVoltage(11),
	WeakSignal(12),
	OtherError(13),

	EVCommunicationError(14),
	InternalError(15),
	LocalListConflict(16),
	OverVoltage(17);

	private final byte code;

	private ChargePointErrorCode(int code) {
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
	public static ChargePointErrorCode forCode(int code) {
		final byte c = (byte) code;
		for ( ChargePointErrorCode v : values() ) {
			if ( v.code == c ) {
				return v;
			}
		}
		return ChargePointErrorCode.Unknown;
	}
}
