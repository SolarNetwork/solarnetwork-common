/* ==================================================================
 * ObjectDatumKind.java - 22/11/2020 9:51:48 pm
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

package net.solarnetwork.domain.datum;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * A datum object kind enumeration.
 *
 * @author matt
 * @version 1.1
 * @since 1.72
 */
public enum ObjectDatumKind {

	/** Node datum, with node ID and source ID. */
	Node('n'),

	/** Location datum, with location ID and source ID. */
	Location('l');

	private final String key;

	private ObjectDatumKind(char key) {
		this.key = String.valueOf(key);
	}

	/**
	 * Get the key.
	 *
	 * @return the key
	 */
	public char getKey() {
		return key.charAt(0);
	}

	/**
	 * Get a key value for this enum.
	 *
	 * @return the key as a string
	 * @since 1.1
	 */
	@JsonValue
	public String keyValue() {
		return key;
	}

	/**
	 * Get an enum instance for a key value.
	 *
	 * @param key
	 *        the key value
	 * @return the enum
	 * @throws IllegalArgumentException
	 *         if {@code key} is not supported
	 */
	public static ObjectDatumKind forKey(String key) {
		if ( key == null || key.isEmpty() ) {
			throw new IllegalArgumentException("Key must not be null.");
		}
		if ( key.length() == 1 ) {
			switch (key.charAt(0)) {
				case 'n':
					return Node;

				case 'l':
					return Location;

				default:
					throw new IllegalArgumentException("Invalid ObjectDatumKind value [" + key + "]");
			}
		}
		// try name() value for convenience
		return ObjectDatumKind.valueOf(key);
	}

	/**
	 * Get an enum instance for a name or key value.
	 *
	 * @param value
	 *        the enumeration name or key value, case-insensitve
	 * @return the enum, or {@literal null} if value is {@literal null} or empty
	 * @throws IllegalArgumentException
	 *         if {@code value} is not a valid value
	 * @since 1.1
	 */
	@JsonCreator
	public static ObjectDatumKind fromValue(String value) {
		if ( value == null || value.isEmpty() ) {
			return null;
		}
		final char key = value.length() == 1 ? Character.toLowerCase(value.charAt(0)) : 0;
		for ( ObjectDatumKind e : ObjectDatumKind.values() ) {
			if ( key == e.key.charAt(0) || value.equalsIgnoreCase(e.name()) ) {
				return e;
			}
		}
		throw new IllegalArgumentException("Unknown ObjectDatumKind value [" + value + "]");
	}

}
