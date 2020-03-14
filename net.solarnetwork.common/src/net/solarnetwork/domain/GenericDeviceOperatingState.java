/* ==================================================================
 * GenericDeviceOperatingState.java - 18/02/2019 11:11:39 am
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

import java.util.Objects;
import java.util.stream.IntStream;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * A generic device operating state used when a vendor-specific implementation
 * is not known.
 * 
 * @author matt
 * @version 1.0
 * @since 1.50
 */
public class GenericDeviceOperatingState implements Bitmaskable {

	private final int code;

	/**
	 * Constructor.
	 * 
	 * @param code
	 *        the code
	 */
	public GenericDeviceOperatingState(int code) {
		super();
		this.code = code;
	}

	@Override
	public int bitmaskBitOffset() {
		return code - 1;
	}

	/**
	 * Get the code value.
	 * 
	 * @return the code
	 */
	@JsonValue
	public int getCode() {
		return code;
	}

	/**
	 * Get an array of all possible values.
	 * 
	 * @return array of values
	 */
	public static GenericDeviceOperatingState[] values() {
		return IntStream.range(1, 33).mapToObj(i -> new GenericDeviceOperatingState(i))
				.toArray(GenericDeviceOperatingState[]::new);
	}

	@Override
	public int hashCode() {
		return Objects.hash(code);
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( obj == null ) {
			return false;
		}
		if ( !(obj instanceof GenericDeviceOperatingState) ) {
			return false;
		}
		GenericDeviceOperatingState other = (GenericDeviceOperatingState) obj;
		return code == other.code;
	}

}
