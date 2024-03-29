/* ==================================================================
 * ChargingProfilePurpose.java - 18/02/2020 4:12:53 pm
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
 * An enumeration of purpose types for a charging profile.
 *
 * @author matt
 * @version 1.1
 */
public enum ChargingProfilePurpose implements CodedValue {

	/** An unknown purpose. */
	Unknown(0),

	/**
	 * Configuration for the maximum power or current available for an entire
	 * Charge Point.
	 */
	ChargePointMaxProfile(1),

	/**
	 * Default profile for the Charge Point to use when a new transaction is
	 * started, unless the transaction specifies a specific profile to use.
	 */
	TxDefaultProfile(2),

	/** Transaction profile. */
	TxProfile(3),

	/**
	 * Additional constraints that will be incorporated into a local power
	 * schedule. Only valid for a Charging Station.
	 *
	 * @since 1.1
	 */
	ChargingStationExternalConstraints(4),;

	private final byte code;

	private ChargingProfilePurpose(int code) {
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
	public static ChargingProfilePurpose forCode(int code) {
		switch (code) {
			case 1:
				return ChargePointMaxProfile;

			case 2:
				return TxDefaultProfile;

			case 3:
				return TxProfile;

			case 4:
				return ChargingStationExternalConstraints;

			default:
				return Unknown;
		}
	}

}
