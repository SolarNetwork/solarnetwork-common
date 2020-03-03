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

import net.solarnetwork.domain.CodedValue;

/**
 * Enumeration of charge point error codes.
 * 
 * @author matt
 * @version 1.0
 */
public enum ChargePointErrorCode implements CodedValue {

	/** Unknown error code. */
	Unknown(0),

	/** Failure to lock or unlock the connector. */
	ConnectorLockFailure(1),

	/** Temperature inside Charge Point is too high. */
	HighTemperature(2),

	/** EV communication error, see {@link #EVCommunicationError}. */
	Mode3Error(3),

	/** No error to report. */
	NoError(4),

	/** Failure to read electrical/energy/power meter. */
	PowerMeterFailure(5),

	/** Failure to control power switch. */
	PowerSwitchFailure(6),

	/** Failure with idTag reader. */
	ReaderFailure(7),

	/** Unable to perform a reset. */
	ResetFailure(8),

	/** Ground fault circuit interrupter has been activated. */
	GroundFailure(9),

	/** Over current protection device has tripped. */
	OverCurrentFailure(10),

	/** Voltage has dropped below an acceptable level. */
	UnderVoltage(11),

	/** Wireless communication device reports a weak signal. */
	WeakSignal(12),

	/** Other type of error. More information in {@code vendorErrorCode}. */
	OtherError(13),

	/**
	 * Communication failure with the vehicle, might be Mode 3 or other
	 * communication protocol problem.
	 */
	EVCommunicationError(14),

	/** Error in internal hard- or software component. */
	InternalError(15),

	/**
	 * The authorization information received from the Central System is in
	 * conflict with the LocalAuthorizationList.
	 */
	LocalListConflict(16),

	/** Voltage has risen above an acceptable level. */
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
