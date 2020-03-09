/* ==================================================================
 * ChargingScheduleRecurrency.java - 18/02/2020 4:19:37 pm
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
 * An enumeration of recurrency types.
 * 
 * @author matt
 * @version 1.0
 */
public enum ChargingScheduleRecurrency implements CodedValue {

	/** Unknown kind. */
	Unknown(0),

	/**
	 * The schedule restarts every 24 hours, at the same time as in the schedule
	 * start date.
	 */
	Daily(1),

	/**
	 * The schedule restarts every 7 days, at the same time and day-of-the-week
	 * as in schedule start date.
	 */
	Weekly(2);

	private final byte code;

	private ChargingScheduleRecurrency(int code) {
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
	public static ChargingScheduleRecurrency forCode(int code) {
		switch (code) {
			case 1:
				return Daily;

			case 2:
				return Weekly;

			default:
				return Unknown;
		}
	}

}
