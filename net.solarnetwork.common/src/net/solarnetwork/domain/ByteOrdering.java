/* ==================================================================
 * ByteOrdering.java - 25/09/2019 8:34:10 pm
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
 * Enumeration of endian types.
 * 
 * @author matt
 * @version 1.0
 * @since 1.54
 */
public enum ByteOrdering {

	/** Big endian. */
	BigEndian('b', "Most to least significant"),

	/** Little endian. */
	LittleEndian('l', "Least to most signifiant");

	private char code;
	private String description;

	private ByteOrdering(char code, String description) {
		this.code = code;
		this.description = description;
	}

	/**
	 * Get a code for this type.
	 * 
	 * @return the code
	 */
	public char getCode() {
		return code;
	}

	/**
	 * Get the description for this type.
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Get an enum instance for a code value.
	 * 
	 * @param code
	 *        the code
	 * @return the enum
	 * @throws IllegalArgumentException
	 *         if {@code code} is not a valid value
	 */
	public static ByteOrdering forCode(char code) {
		for ( ByteOrdering e : ByteOrdering.values() ) {
			if ( code == e.code ) {
				return e;
			}
		}

		throw new IllegalArgumentException("Unknown ByteOrdering code [" + code + "]");
	}

}
