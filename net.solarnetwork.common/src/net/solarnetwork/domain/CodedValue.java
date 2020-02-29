/* ==================================================================
 * CodedValue.java - 25/02/2020 7:25:22 pm
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

package net.solarnetwork.domain;

/**
 * API for something that has a "code" value.
 * 
 * <p>
 * This can be used in enumerations to provide a consistent way to exchange
 * enumerated values with integers that do not depend on the ordinal position
 * (or string value) of the enum.
 * </p>
 * 
 * @author matt
 * @version 1.0
 * @since 1.59
 */
public interface CodedValue {

	/**
	 * Get the coded value.
	 * 
	 * @return the code
	 */
	int getCode();

	/**
	 * Convert a code value into an enum value.
	 * 
	 * @param <T>
	 *        the value type
	 * @param code
	 *        the code to get an enum value for
	 * @param clazz
	 *        the class of an enumeration of {@link CodedValue} objects
	 * @param defaultValue
	 *        a default value to return if no matching code is found
	 * @return the first enumeration value, in ordinal order, that has the given
	 *         code value, or {@code defaultValue} if not found
	 */
	static <T extends Enum<T> & CodedValue> T forCodeValue(int code, Class<T> clazz, T defaultValue) {
		return forCodeValue(code, clazz.getEnumConstants(), defaultValue);
	}

	/**
	 * Convert a code value into an enum value.
	 * 
	 * @param <T>
	 *        the value type
	 * @param code
	 *        the code to get an enum value for
	 * @param values
	 *        the values to search through
	 * @param defaultValue
	 *        a default value to return if no matching code is found
	 * @return the first value, in array order, that has the given code value,
	 *         or {@code defaultValue} if not found
	 */
	static <T extends CodedValue> T forCodeValue(int code, T[] values, T defaultValue) {
		for ( T v : values ) {
			if ( code == v.getCode() ) {
				return v;
			}
		}
		return defaultValue;
	}

}
