/* ==================================================================
 * KeyCodedValue.java - 10/03/2026 7:33:15 am
 *
 * Copyright 2026 SolarNetwork.net Dev Team
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

import org.jspecify.annotations.Nullable;

/**
 * API for something that has a "key" value.
 *
 * <p>
 * This can be used in enumerations to provide a consistent way to exchange
 * enumerated values with characters that do not depend on the ordinal position
 * (or string value) of the enum.
 * </p>
 *
 * @author matt
 * @version 1.0
 */
public interface KeyedValue {

	/**
	 * Get the coded value.
	 *
	 * @return the code
	 */
	String getKey();

	/**
	 * Convert a key value into an enum value.
	 *
	 * @param <T>
	 *        the value type
	 * @param key
	 *        the key to get an enum value for
	 * @param clazz
	 *        the class of an enumeration of {@link KeyedValue} objects
	 * @param defaultValue
	 *        a default value to return if no matching code is found
	 * @param caseInsensitive
	 *        {@code true} to use case-insensitive matching
	 * @param matchName
	 *        {@code true} to allow matching enum name values in addition to key
	 *        values
	 * @return the first enumeration value, in ordinal order, that has the given
	 *         key value or matching name, or {@code defaultValue} if not found
	 */
	static <T extends Enum<T> & KeyedValue> @Nullable T forKeyValue(@Nullable String key, Class<T> clazz,
			@Nullable T defaultValue, boolean caseInsensitive, boolean matchName) {
		if ( key != null ) {
			for ( T v : clazz.getEnumConstants() ) {
				if ( caseInsensitive ? key.equalsIgnoreCase(v.getKey()) : key.equals(v.getKey()) ) {
					return v;
				} else if ( matchName
						&& (caseInsensitive ? key.equalsIgnoreCase(v.name()) : key.equals(v.name())) ) {
					return v;
				}
			}
		}
		return defaultValue;
	}

	/**
	 * Convert a key value into an enum value.
	 *
	 * @param <T>
	 *        the value type
	 * @param key
	 *        the key to get an enum value for
	 * @param values
	 *        the values to search through
	 * @param defaultValue
	 *        a default value to return if no matching code is found
	 * @param caseInsensitive
	 *        {@code true} to use case-insensitive matching
	 * @return the first value, in array order, that has the given key value, or
	 *         {@code defaultValue} if not found
	 */
	static <T extends KeyedValue> @Nullable T forKeyValue(@Nullable String key, T[] values,
			@Nullable T defaultValue, boolean caseInsensitive) {
		if ( key != null ) {
			for ( T v : values ) {
				if ( caseInsensitive ? key.equalsIgnoreCase(v.getKey()) : key.equals(v.getKey()) ) {
					return v;
				}
			}
		}
		return defaultValue;
	}

}
