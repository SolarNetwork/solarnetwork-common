/* ==================================================================
 * DeviceOperatingState.java - 18/02/2019 10:19:30 am
 * 
 * Copyright 2019 SolarNetwork.net Dev Team
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

package net.solarnetwork.domain;

/**
 * An enumeration of standardized device operating states.
 * 
 * @author matt
 * @version 1.0
 * @since 1.50
 */
public enum DeviceOperatingState implements Bitmaskable {

	/** An unknown state. */
	Unknown(0),

	/** Normal operating state. */
	Normal(1),

	/** A startup/initializing state. */
	Starting(2),

	/** A standby/low power state. */
	Standby(3),

	/** A shutdown/off state. */
	Shutdown(4),

	/** A faulty state. */
	Fault(5),

	/** A disabled state. */
	Disabled(6),

	/** A recovery state. */
	Recovery(7),

	/** An overridden state. */
	Override(8);

	private final int code;

	private DeviceOperatingState(int code) {
		this.code = code;
	}

	/**
	 * Get the code for this condition.
	 * 
	 * @return the code
	 */
	public int getCode() {
		return code;
	}

	@Override
	public int bitmaskBitOffset() {
		return code - 1;
	}

	/**
	 * Get an enum for a code value.
	 * 
	 * @param code
	 *        the code to get an enum for
	 * @return the enum with the given {@code code}, or {@literal null} if
	 *         {@code code} is {@literal 0}
	 * @throws IllegalArgumentException
	 *         if {@code code} is not supported
	 */
	public static DeviceOperatingState forCode(int code) {
		if ( code == 0 ) {
			return null;
		}
		for ( DeviceOperatingState c : values() ) {
			if ( code == c.code ) {
				return c;
			}
		}
		throw new IllegalArgumentException("DeviceOperatingState code [" + code + "] not supported");
	}
}
