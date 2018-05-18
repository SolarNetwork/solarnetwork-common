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
 * @version 1.1
 * @since 1.42
 */
public final class NumberUtils {

	private static final int[] CRC16_TABLE = {
			// @formatter:off
			0x0000, 0xC0C1, 0xC181, 0x0140, 0xC301, 0x03C0, 0x0280, 0xC241,
			0xC601, 0x06C0, 0x0780, 0xC741, 0x0500, 0xC5C1, 0xC481, 0x0440,
			0xCC01, 0x0CC0, 0x0D80, 0xCD41, 0x0F00, 0xCFC1, 0xCE81, 0x0E40,
			0x0A00, 0xCAC1, 0xCB81, 0x0B40, 0xC901, 0x09C0, 0x0880, 0xC841,
			0xD801, 0x18C0, 0x1980, 0xD941, 0x1B00, 0xDBC1, 0xDA81, 0x1A40,
			0x1E00, 0xDEC1, 0xDF81, 0x1F40, 0xDD01, 0x1DC0, 0x1C80, 0xDC41,
			0x1400, 0xD4C1, 0xD581, 0x1540, 0xD701, 0x17C0, 0x1680, 0xD641,
			0xD201, 0x12C0, 0x1380, 0xD341, 0x1100, 0xD1C1, 0xD081, 0x1040,
			0xF001, 0x30C0, 0x3180, 0xF141, 0x3300, 0xF3C1, 0xF281, 0x3240,
			0x3600, 0xF6C1, 0xF781, 0x3740, 0xF501, 0x35C0, 0x3480, 0xF441,
			0x3C00, 0xFCC1, 0xFD81, 0x3D40, 0xFF01, 0x3FC0, 0x3E80, 0xFE41,
			0xFA01, 0x3AC0, 0x3B80, 0xFB41, 0x3900, 0xF9C1, 0xF881, 0x3840,
			0x2800, 0xE8C1, 0xE981, 0x2940, 0xEB01, 0x2BC0, 0x2A80, 0xEA41,
			0xEE01, 0x2EC0, 0x2F80, 0xEF41, 0x2D00, 0xEDC1, 0xEC81, 0x2C40,
			0xE401, 0x24C0, 0x2580, 0xE541, 0x2700, 0xE7C1, 0xE681, 0x2640,
			0x2200, 0xE2C1, 0xE381, 0x2340, 0xE101, 0x21C0, 0x2080, 0xE041,
			0xA001, 0x60C0, 0x6180, 0xA141, 0x6300, 0xA3C1, 0xA281, 0x6240,
			0x6600, 0xA6C1, 0xA781, 0x6740, 0xA501, 0x65C0, 0x6480, 0xA441,
			0x6C00, 0xACC1, 0xAD81, 0x6D40, 0xAF01, 0x6FC0, 0x6E80, 0xAE41,
			0xAA01, 0x6AC0, 0x6B80, 0xAB41, 0x6900, 0xA9C1, 0xA881, 0x6840,
			0x7800, 0xB8C1, 0xB981, 0x7940, 0xBB01, 0x7BC0, 0x7A80, 0xBA41,
			0xBE01, 0x7EC0, 0x7F80, 0xBF41, 0x7D00, 0xBDC1, 0xBC81, 0x7C40,
			0xB401, 0x74C0, 0x7580, 0xB541, 0x7700, 0xB7C1, 0xB681, 0x7640,
			0x7200, 0xB2C1, 0xB381, 0x7340, 0xB101, 0x71C0, 0x7080, 0xB041,
			0x5000, 0x90C1, 0x9181, 0x5140, 0x9301, 0x53C0, 0x5280, 0x9241,
			0x9601, 0x56C0, 0x5780, 0x9741, 0x5500, 0x95C1, 0x9481, 0x5440,
			0x9C01, 0x5CC0, 0x5D80, 0x9D41, 0x5F00, 0x9FC1, 0x9E81, 0x5E40,
			0x5A00, 0x9AC1, 0x9B81, 0x5B40, 0x9901, 0x59C0, 0x5880, 0x9841,
			0x8801, 0x48C0, 0x4980, 0x8941, 0x4B00, 0x8BC1, 0x8A81, 0x4A40,
			0x4E00, 0x8EC1, 0x8F81, 0x4F40, 0x8D01, 0x4DC0, 0x4C80, 0x8C41,
			0x4400, 0x84C1, 0x8581, 0x4540, 0x8701, 0x47C0, 0x4680, 0x8641,
			0x8201, 0x42C0, 0x4380, 0x8341, 0x4100, 0x81C1, 0x8081, 0x4040,
			// @formatter:on
	};

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

	/**
	 * Calculate the CRC-16 checksum value from a set of data.
	 * 
	 * <p>
	 * Adapted from https://introcs.cs.princeton.edu/java/61data/CRC16.java.
	 * </p>
	 * 
	 * @param data
	 *        the data to checksum
	 * @param offset
	 *        the offset within the data to start
	 * @param length
	 *        the length of data to checksum
	 * @return the checksum value
	 * @since 1.1
	 */
	public static int crc16(byte[] data, int offset, int length) {
		int crc = 0x0000;
		if ( data != null && data.length >= offset + length ) {
			for ( int i = offset; i < data.length && i < offset + length; i++ ) {
				byte b = data[i];
				crc = (crc >>> 8) ^ CRC16_TABLE[(crc ^ b) & 0xFF];
			}
		}
		return crc;
	}

}
