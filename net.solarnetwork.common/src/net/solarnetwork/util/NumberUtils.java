/* ==================================================================
 * NumberUtils.java - 15/03/2018 2:49:15 PM
 * 
 * Copyright 2018 SolarNetwork.net Dev Team
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

package net.solarnetwork.util;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Utilities for dealing with numbers.
 * 
 * @author matt
 * @version 1.0
 * @since 1.42
 */
public final class NumberUtils {

	private NumberUtils() {
		// do not construct me
	}

	/**
	 * Convert a signed byte into an unsigned short value.
	 * 
	 * <p>
	 * The returned short will have a value between 0 and 255.
	 * </p>
	 * 
	 * @param data
	 *        the byte
	 * @return the unsigned value
	 */
	public static short unsigned(byte data) {
		return (short) (data & 0xFF);
	}

	/**
	 * Convert signed bytes into unsigned short values.
	 * 
	 * <p>
	 * The returned shorts will have values between 0 and 255.
	 * </p>
	 * 
	 * @param data
	 *        the bytes
	 * @return the unsigned values, or {@literal null} if {@code data} is
	 *         {@literal null}
	 */
	public static short[] unsigned(byte[] data) {
		// convert bytes into "unsigned" integer values, i.e. 0..255
		if ( data == null ) {
			return null;
		}
		short[] unsigned = new short[data.length];
		for ( int i = 0; i < data.length; i++ ) {
			unsigned[i] = (short) (data[i] & 0xFF);
		}
		return unsigned;
	}

	/**
	 * Convert a signed whole number into an unsigned equivalent.
	 * 
	 * <p>
	 * This method attempts to return the next-largest data type for the
	 * unsigned conversion, e.g. an unsigned int will be returned for a signed
	 * short.
	 * </p>
	 * 
	 * @param value
	 *        the signed whole number to convert
	 * @return the unsigned value
	 */
	public static Number unsignedNumber(Number value) {
		if ( value instanceof Byte ) {
			return Byte.toUnsignedInt(value.byteValue());
		} else if ( value instanceof Short ) {
			return Short.toUnsignedInt(value.shortValue());
		} else if ( value instanceof Integer ) {
			return Integer.toUnsignedLong(value.intValue());
		}
		return new BigInteger(Long.toUnsignedString(value.longValue()));
	}

	/**
	 * Get a {@link BigDecimal} for a number.
	 * 
	 * <p>
	 * If {@code value} is already a {@link BigDecimal} it will be returned
	 * directly. Otherwise a new {@link BigDecimal} instance will be created out
	 * of {@code value}.
	 * </p>
	 * 
	 * @param value
	 *        the number to get a {@code BigDecimal} version of
	 * @return the {@code BigDecimal} version of {@code value}
	 */
	public static BigDecimal bigDecimalForNumber(Number value) {
		BigDecimal v = null;
		if ( value instanceof BigDecimal ) {
			v = (BigDecimal) value;
		} else if ( value instanceof Long ) {
			v = new BigDecimal(value.longValue());
		} else if ( value instanceof Integer || value instanceof Short ) {
			v = new BigDecimal(value.intValue());
		} else if ( value instanceof Double ) {
			v = BigDecimal.valueOf(value.doubleValue());
		} else {
			// note Float falls through to here per recommended way of converting that to BigDecimal
			v = new BigDecimal(value.toString());
		}
		return v;
	}

}
