/* ==================================================================
 * DatumSamplesType.java - 20/12/2017 1:44:58 PM
 *
 * Copyright 2017 SolarNetwork.net Dev Team
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
 * A property type for a datum property.
 *
 * @author matt
 * @version 1.1
 * @since 2.0
 */
public enum DatumSamplesType {

	/** Instantaneous property */
	Instantaneous('i'),

	/** Accumulating property */
	Accumulating('a'),

	/** Status property. */
	Status('s'),

	/** Tag property. */
	Tag('t');

	private final String type;

	private DatumSamplesType(char type) {
		this.type = String.valueOf(type);
	}

	/**
	 * Get a key value for this enum.
	 *
	 * @return the key
	 */
	public char toKey() {
		return type.charAt(0);
	}

	/**
	 * Get a key value for this enum.
	 *
	 * @return the key as a string
	 * @since 1.1
	 */
	@JsonValue
	public String keyValue() {
		return type;
	}

	/**
	 * Get an enum instance for a key value.
	 *
	 * @param key
	 *        the key
	 * @return the enum
	 * @throws IllegalArgumentException
	 *         if {@code key} is not a valid value
	 */
	public static DatumSamplesType valueOf(char key) {
		for ( DatumSamplesType e : DatumSamplesType.values() ) {
			if ( key == e.type.charAt(0) ) {
				return e;
			}
		}
		throw new IllegalArgumentException("Unknown DatumSamplesType key [" + key + "]");
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
	public static DatumSamplesType fromValue(String value) {
		if ( value == null || value.isEmpty() ) {
			return null;
		}
		final char key = value.length() == 1 ? Character.toLowerCase(value.charAt(0)) : 0;
		for ( DatumSamplesType e : DatumSamplesType.values() ) {
			if ( key == e.type.charAt(0) || value.equalsIgnoreCase(e.name()) ) {
				return e;
			}
		}
		throw new IllegalArgumentException("Unknown DatumSamplesType value [" + value + "]");
	}

}
