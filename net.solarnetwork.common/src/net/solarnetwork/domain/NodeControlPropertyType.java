/* ==================================================================
 * NodeControlPropertyType.java - Sep 28, 2011 4:12:34 PM
 * 
 * Copyright 2007-2011 SolarNetwork.net Dev Team
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
 * Enumeration of supported node component property types.
 * 
 * @author matt
 * @version 1.1
 */
public enum NodeControlPropertyType {

	/** A boolean on/off toggle control property. */
	Boolean('b'),

	/** A floating point decimal control property. */
	Float('f'),

	/** A whole number control property. */
	Integer('i'),

	/** A percentage control property. */
	Percent('p'),

	/** A string control property. */
	String('s');

	private final char key;

	private NodeControlPropertyType(char key) {
		this.key = key;
	}

	/**
	 * Get a key value for this enum.
	 * 
	 * @return the key
	 * @since 1.1
	 */
	public char getKey() {
		return key;
	}

	/**
	 * Get an enum instance for a key value.
	 * 
	 * @param key
	 *        the key
	 * @return the enum
	 * @throws IllegalArgumentException
	 *         if {@code key} is not a valid value
	 * @since 1.1
	 */
	public static NodeControlPropertyType forKey(char key) {
		for ( NodeControlPropertyType e : NodeControlPropertyType.values() ) {
			if ( key == e.key ) {
				return e;
			}
		}
		throw new IllegalArgumentException("Unknown NodeControlPropertyType key [" + key + "]");
	}
}
