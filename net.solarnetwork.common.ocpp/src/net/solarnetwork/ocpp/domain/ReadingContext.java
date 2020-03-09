/* ==================================================================
 * ReadingContext.java - 10/02/2020 11:01:00 am
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
 * The context for a sampled value.
 * 
 * @author matt
 * @version 1.0
 */
public enum ReadingContext implements CodedValue {

	Unknown(0),

	InterruptionBegin(1),

	InterruptionEnd(2),

	SampleClock(3),

	SamplePeriodic(4),

	TransactionBegin(5),

	TransactionEnd(6),

	Other(7),

	Trigger(8);

	private final byte code;

	private ReadingContext(int code) {
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
	public static ReadingContext forCode(int code) {
		final byte c = (byte) code;
		for ( ReadingContext v : values() ) {
			if ( v.code == c ) {
				return v;
			}
		}
		return ReadingContext.Unknown;
	}

}
