/* ==================================================================
 * ByteUtils.java - 25/09/2019 8:17:46 pm
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

package net.solarnetwork.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import net.solarnetwork.domain.BitDataType;
import net.solarnetwork.domain.ByteOrdering;

/**
 * Utilities for working with bytes.
 * 
 * <p>
 * Some routines have been adapted from Apache Commons Codec's
 * {@literal Hex.java} class.
 * </p>
 * 
 * @author matt
 * @version 1.1
 * @since 1.54
 */
public final class ByteUtils {

	/** The UTF-8 character set name. */
	public static final String UTF8_CHARSET = "UTF-8";

	/** The UTF-8 character set. */
	public static final Charset UTF8 = Charset.forName(UTF8_CHARSET);

	/** The ASCII character set name. */
	public static final String ASCII_CHARSET = "US-ASCII";

	/** The ASCII character set. */
	public static final Charset ASCII = Charset.forName(ASCII_CHARSET);

	/**
	 * The ISO-8859-1 (ISO-LATIN-1) character set name.
	 * 
	 * @since 1.1
	 */
	public static final String LATIN1_CHARSET = "ISO-8859-1";

	/**
	 * The ISO-8859-1 (ISO-LATIN-1) character set.
	 * 
	 * @since 1.1
	 */
	public static final Charset LATIN1 = Charset.forName(LATIN1_CHARSET);

	private static final char[] DIGITS_UPPER = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A',
			'B', 'C', 'D', 'E', 'F' };
	private static final char[] DIGITS_LOWER = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a',
			'b', 'c', 'd', 'e', 'f' };

	/**
	 * Encode a single byte as hex characters.
	 * 
	 * @param b
	 *        the byte to encode
	 * @param toDigits
	 *        the hex alphabet to use
	 * @param dest
	 *        the destination character buffer to write the hex encoding to
	 * @param destIndex
	 *        the index within {@code dest} to write the hex encoding at, along
	 *        with {@code destIndex + 1}
	 * @return the {@code dest} array
	 */
	public static char[] encodeHex(final byte b, final char[] toDigits, final char[] dest,
			int destIndex) {
		dest[destIndex] = toDigits[(0xF0 & b) >>> 4];
		dest[destIndex + 1] = toDigits[0x0F & b];
		return dest;
	}

	/**
	 * Encode a single byte as upper-case hex characters.
	 * 
	 * @param b
	 *        the byte to encode
	 * @param dest
	 *        the destination character buffer to write the hex encoding to
	 * @param destIndex
	 *        the index within {@code dest} to write the hex encoding at, along
	 *        with {@code destIndex + 1}
	 * @return the {@code dest} array
	 */
	public static char[] encodeHexUpperCase(final byte b, final char[] dest, int destIndex) {
		return encodeHex(b, DIGITS_UPPER, dest, destIndex);
	}

	/**
	 * Encode a single byte as lower-case hex characters.
	 * 
	 * @param b
	 *        the byte to encode
	 * @param dest
	 *        the destination character buffer to write the hex encoding to
	 * @param destIndex
	 *        the index within {@code dest} to write the hex encoding at, along
	 *        with {@code destIndex + 1}
	 * @return the {@code dest} array
	 */
	public static char[] encodeHexLowerCase(final byte b, final char[] dest, int destIndex) {
		return encodeHex(b, DIGITS_LOWER, dest, destIndex);
	}

	/*- maybe someday
	private static char[] encodeHex(final byte[] data, final char[] toDigits) {
		final int l = data.length;
		final char[] out = new char[l << 1];
		for ( int i = 0, j = 0; i < l; i++ ) {
			encodeHex(data[i], toDigits, out, j);
		}
		return out;
	}
	*/

	/**
	 * Encode a byte array into a hex-encoded upper-case string.
	 * 
	 * @param data
	 *        the data to encode as hex strings
	 * @param fromIndex
	 *        the starting index within {@code data} to encode (inclusive)
	 * @param toIndex
	 *        the ending index within {@code data} to encode (exclusive)
	 * @param space
	 *        {@literal true} to add a single space character between each hex
	 *        pair
	 * @return the string, never {@literal null}
	 */
	public static String encodeHexString(final byte[] data, final int fromIndex, final int toIndex,
			final boolean space) {
		if ( data == null || data.length < 1 || fromIndex < 0 || fromIndex >= data.length || toIndex < 0
				|| toIndex <= fromIndex ) {
			return "";
		}
		StringBuilder hexData = new StringBuilder(
				2 * (toIndex - fromIndex) + (space ? (toIndex - fromIndex) : 0));
		char[] buffer = new char[2];
		for ( int i = fromIndex; i < toIndex; i++ ) {
			if ( space && i > fromIndex ) {
				hexData.append(' ');
			}
			hexData.append(encodeHex(data[i], DIGITS_UPPER, buffer, 0));
		}
		return hexData.toString();
	}

	/**
	 * Encode a byte array into a hex-encoded upper-case string.
	 * 
	 * @param data
	 *        the data to encode as hex strings
	 * @param fromIndex
	 *        the starting index within {@code data} to encode (inclusive)
	 * @param toIndex
	 *        the ending index within {@code data} to encode (exclusive)
	 * @param space
	 *        {@literal true} to add a single space character between each hex
	 * @param lowerCase
	 *        {@literal true} to use lower case, {@literal false} for upper case
	 *        pair
	 * @return the string, never {@literal null}
	 */
	public static String encodeHexString(final byte[] data, final int fromIndex, final int toIndex,
			final boolean space, final boolean lowerCase) {
		if ( data == null || data.length < 1 || fromIndex < 0 || fromIndex >= data.length || toIndex < 0
				|| toIndex <= fromIndex ) {
			return "";
		}
		final char[] digits = (lowerCase ? DIGITS_LOWER : DIGITS_UPPER);
		StringBuilder hexData = new StringBuilder(
				2 * (toIndex - fromIndex) + (space ? (toIndex - fromIndex) : 0));
		char[] buffer = new char[2];
		for ( int i = fromIndex; i < toIndex; i++ ) {
			if ( space && i > fromIndex ) {
				hexData.append(' ');
			}
			hexData.append(encodeHex(data[i], digits, buffer, 0));
		}
		return hexData.toString();
	}

	/**
	 * Convert a hex-encoded string to a byte array.
	 * 
	 * <p>
	 * If the string does not have an even number of characters, a {@literal 0}
	 * will be inserted at the start of the string.
	 * </p>
	 * 
	 * @param s
	 *        the string to decode
	 * @return the bytes, never {@literal null}
	 * @see #decodeHexStringPadStart(String)
	 */
	public static byte[] decodeHexString(String s) {
		if ( s == null ) {
			return new byte[0];
		}
		return decodeHexPadStart(s.toCharArray());
	}

	/**
	 * Convert a hex-encoded string to a byte array.
	 * 
	 * <p>
	 * If the string does not have an even number of characters, a {@literal 0}
	 * will be inserted at the start of the string.
	 * </p>
	 * 
	 * @param chars
	 *        the characters to decode
	 * @return the bytes, never {@literal null}
	 */
	public static byte[] decodeHexPadStart(final char[] chars) {
		if ( chars == null || chars.length < 1 ) {
			return new byte[0];
		}
		final int len = chars.length;
		final boolean even = (len & 0x01) == 0;
		final byte[] data = new byte[(even ? len : len + 1) / 2];
		int i = 0;
		int j = 0;
		if ( !even ) {
			data[i] = (byte) (Character.digit(chars[j], 16) & 0xFF);
			i++;
			j++;
		}
		for ( ; j < len; i++ ) {
			int n = Character.digit(chars[j], 16) << 4;
			j++;
			n |= Character.digit(chars[j], 16);
			j++;
			data[i] = (byte) (n & 0xFF);
		}
		return data;
	}

	/**
	 * Encode an 8-bit signed integer value into a raw byte value.
	 * 
	 * @param n
	 *        the number to encode
	 * @param dest
	 *        the destination to encode the number to
	 * @param offset
	 *        the offset within {@code dest} to encode the number to
	 * @throws ArrayIndexOutOfBoundsException
	 *         if {@code dest} is not long enough to hold the number's byte
	 *         value
	 */
	public static void encodeInt8(final Number n, byte[] dest, int offset) {
		dest[offset] = (n != null ? n.byteValue() : 0);
	}

	/**
	 * Encode an 8-bit unsigned integer value into a raw byte value.
	 * 
	 * @param n
	 *        the number to encode
	 * @param dest
	 *        the destination to encode the number to
	 * @param offset
	 *        the offset within {@code dest} to encode the number to
	 * @throws ArrayIndexOutOfBoundsException
	 *         if {@code dest} is not long enough to hold the number's byte
	 *         value
	 */
	public static void encodeUnsignedInt8(final Number n, byte[] dest, int offset) {
		dest[offset] = (n != null ? (byte) (n.shortValue() & (short) 0xFF) : 0);
	}

	/**
	 * Encode a 16-bit signed integer value into a raw byte value.
	 * 
	 * @param n
	 *        the number to encode
	 * @param dest
	 *        the destination to encode the number to
	 * @param offset
	 *        the offset within {@code dest} to encode the number to
	 * @param byteOrder
	 *        the byte order to encode into {@code dest}
	 * @throws ArrayIndexOutOfBoundsException
	 *         if {@code dest} is not long enough to hold the number's byte
	 *         value
	 */
	public static void encodeInt16(final Number n, byte[] dest, int offset, ByteOrdering byteOrder) {
		short s = (n != null ? n.shortValue() : (short) 0);
		if ( byteOrder == ByteOrdering.BigEndian ) {
			dest[offset] = (byte) ((s >> 8) & (short) 0xFF);
			dest[offset + 1] = (byte) (s & (short) 0xFF);
		} else {
			dest[offset + 1] = (byte) ((s >> 8) & (short) 0xFF);
			dest[offset] = (byte) (s & (short) 0xFF);
		}
	}

	/**
	 * Encode a 16-bit unsigned integer value into a raw byte value.
	 * 
	 * @param n
	 *        the number to encode
	 * @param dest
	 *        the destination to encode the number to
	 * @param offset
	 *        the offset within {@code dest} to encode the number to
	 * @param byteOrder
	 *        the byte order to encode into {@code dest}
	 * @throws ArrayIndexOutOfBoundsException
	 *         if {@code dest} is not long enough to hold the number's byte
	 *         value
	 */
	public static void encodeUnsignedInt16(final Number n, byte[] dest, int offset,
			ByteOrdering byteOrder) {
		int s = (n != null ? n.shortValue() : (short) 0);
		if ( byteOrder == ByteOrdering.BigEndian ) {
			dest[offset] = (byte) ((s >>> 8) & (short) 0xFF);
			dest[offset + 1] = (byte) (s & (short) 0xFF);
		} else {
			dest[offset + 1] = (byte) ((s >> 8) & (short) 0xFF);
			dest[offset] = (byte) (s & (short) 0xFF);
		}
	}

	/**
	 * Encode a 32-bit signed integer value into a raw byte value.
	 * 
	 * @param n
	 *        the number to encode
	 * @param dest
	 *        the destination to encode the number to
	 * @param offset
	 *        the offset within {@code dest} to encode the number to
	 * @param byteOrder
	 *        the byte order to encode into {@code dest}
	 * @throws ArrayIndexOutOfBoundsException
	 *         if {@code dest} is not long enough to hold the number's byte
	 *         value
	 */
	public static void encodeInt32(final Number n, byte[] dest, int offset, ByteOrdering byteOrder) {
		int s = (n != null ? n.intValue() : 0);
		if ( byteOrder == ByteOrdering.BigEndian ) {
			dest[offset] = (byte) ((s >> 24) & 0xFF);
			dest[offset + 1] = (byte) ((s >> 16) & 0xFF);
			dest[offset + 2] = (byte) ((s >> 8) & 0xFF);
			dest[offset + 3] = (byte) (s & 0xFF);
		} else {
			dest[offset + 3] = (byte) ((s >> 24) & 0xFF);
			dest[offset + 2] = (byte) ((s >> 16) & 0xFF);
			dest[offset + 1] = (byte) ((s >> 8) & 0xFF);
			dest[offset] = (byte) (s & 0xFF);
		}
	}

	/**
	 * Encode a 32-bit unsigned integer value into a raw byte value.
	 * 
	 * @param n
	 *        the number to encode
	 * @param dest
	 *        the destination to encode the number to
	 * @param offset
	 *        the offset within {@code dest} to encode the number to
	 * @param byteOrder
	 *        the byte order to encode into {@code dest}
	 * @throws ArrayIndexOutOfBoundsException
	 *         if {@code dest} is not long enough to hold the number's byte
	 *         value
	 */
	public static void encodeUnsignedInt32(final Number n, byte[] dest, int offset,
			ByteOrdering byteOrder) {
		int s = (n != null ? n.intValue() : 0);
		if ( byteOrder == ByteOrdering.BigEndian ) {
			dest[offset] = (byte) ((s >>> 24) & 0xFF);
			dest[offset + 1] = (byte) ((s >>> 16) & 0xFF);
			dest[offset + 2] = (byte) ((s >>> 8) & 0xFF);
			dest[offset + 3] = (byte) (s & 0xFF);
		} else {
			dest[offset + 3] = (byte) ((s >>> 24) & 0xFF);
			dest[offset + 2] = (byte) ((s >>> 16) & 0xFF);
			dest[offset + 1] = (byte) ((s >>> 8) & 0xFF);
			dest[offset] = (byte) (s & 0xFF);
		}
	}

	/**
	 * Encode a 64-bit signed integer value into a raw byte value.
	 * 
	 * @param n
	 *        the number to encode
	 * @param dest
	 *        the destination to encode the number to
	 * @param offset
	 *        the offset within {@code dest} to encode the number to
	 * @param byteOrder
	 *        the byte order to encode into {@code dest}
	 * @throws ArrayIndexOutOfBoundsException
	 *         if {@code dest} is not long enough to hold the number's byte
	 *         value
	 */
	public static void encodeInt64(final Number n, byte[] dest, int offset, ByteOrdering byteOrder) {
		long s = (n != null ? n.longValue() : 0L);
		if ( byteOrder == ByteOrdering.BigEndian ) {
			dest[offset] = (byte) ((s >> 56) & 0xFFL);
			dest[offset + 1] = (byte) ((s >> 48) & 0xFFL);
			dest[offset + 2] = (byte) ((s >> 40) & 0xFFL);
			dest[offset + 3] = (byte) ((s >> 32) & 0xFFL);
			dest[offset + 4] = (byte) ((s >> 24) & 0xFFL);
			dest[offset + 5] = (byte) ((s >> 16) & 0xFFL);
			dest[offset + 6] = (byte) ((s >> 8) & 0xFFL);
			dest[offset + 7] = (byte) (s & 0xFFL);
		} else {
			dest[offset + 7] = (byte) ((s >> 56) & 0xFFL);
			dest[offset + 6] = (byte) ((s >> 48) & 0xFFL);
			dest[offset + 5] = (byte) ((s >> 40) & 0xFFL);
			dest[offset + 4] = (byte) ((s >> 32) & 0xFFL);
			dest[offset + 3] = (byte) ((s >> 24) & 0xFFL);
			dest[offset + 2] = (byte) ((s >> 16) & 0xFFL);
			dest[offset + 1] = (byte) ((s >> 8) & 0xFFL);
			dest[offset] = (byte) (s & 0xFFL);
		}
	}

	/**
	 * Encode a 64-bit unsigned integer value into a raw byte value.
	 * 
	 * @param n
	 *        the number to encode
	 * @param dest
	 *        the destination to encode the number to
	 * @param offset
	 *        the offset within {@code dest} to encode the number to
	 * @param byteOrder
	 *        the byte order to encode into {@code dest}
	 * @throws ArrayIndexOutOfBoundsException
	 *         if {@code dest} is not long enough to hold the number's byte
	 *         value
	 */
	public static void encodeUnsignedInt64(final Number n, byte[] dest, int offset,
			ByteOrdering byteOrder) {
		long s = (n != null ? n.longValue() : 0L);
		if ( byteOrder == ByteOrdering.BigEndian ) {
			dest[offset] = (byte) ((s >>> 56) & 0xFF);
			dest[offset + 1] = (byte) ((s >>> 48) & 0xFFL);
			dest[offset + 2] = (byte) ((s >>> 40) & 0xFFL);
			dest[offset + 3] = (byte) ((s >>> 32) & 0xFFL);
			dest[offset + 4] = (byte) ((s >>> 24) & 0xFFL);
			dest[offset + 5] = (byte) ((s >>> 16) & 0xFFL);
			dest[offset + 6] = (byte) ((s >>> 8) & 0xFFL);
			dest[offset + 7] = (byte) (s & 0xFF);
		} else {
			dest[offset + 7] = (byte) ((s >>> 56) & 0xFFL);
			dest[offset + 6] = (byte) ((s >>> 48) & 0xFFL);
			dest[offset + 5] = (byte) ((s >>> 40) & 0xFFL);
			dest[offset + 4] = (byte) ((s >>> 32) & 0xFFL);
			dest[offset + 3] = (byte) ((s >>> 24) & 0xFFL);
			dest[offset + 2] = (byte) ((s >>> 16) & 0xFFL);
			dest[offset + 1] = (byte) ((s >>> 8) & 0xFFL);
			dest[offset] = (byte) (s & 0xFFL);
		}
	}

	/**
	 * Parse a number from raw byte data.
	 * 
	 * <p>
	 * This method is suitable for fixed-length data types only.
	 * </p>
	 * 
	 * @param dataType
	 *        the desired data type
	 * @param data
	 *        an array of byte values
	 * @param offset
	 *        an offset within {@code data} to start reading from
	 * @param byteOrder
	 *        the byte order of {@code data}
	 * @return the parsed number, or {@literal null} if {@code data} is
	 *         {@literal null} or not long enough for the requested data type
	 * @throws IllegalArgumentException
	 *         if {@code dataType} is not supported
	 */
	public static Number parseNumber(final BitDataType dataType, final byte[] data, final int offset,
			final ByteOrdering byteOrder) {
		if ( dataType.isVariableLength() ) {
			throw new IllegalArgumentException("Variable length data types not supported.");
		}
		return parseNumber(dataType, data, offset, 0, byteOrder);
	}

	/**
	 * Parse a number from raw byte data.
	 * 
	 * @param dataType
	 *        the desired data type
	 * @param data
	 *        an array of byte values
	 * @param offset
	 *        an offset within {@code data} to start reading from
	 * @param length
	 *        for types of variable length, the number of bytes to consume; for
	 *        types of fixed length this value is ignored
	 * @param byteOrder
	 *        the byte order of {@code data}
	 * @return the parsed number, or {@literal null} if {@code data} is
	 *         {@literal null} or not long enough for the requested data type
	 * @throws IllegalArgumentException
	 *         if {@code dataType} is not supported
	 */
	public static Number parseNumber(final BitDataType dataType, final byte[] data, final int offset,
			final int length, final ByteOrdering byteOrder) {
		Number result = null;
		switch (dataType) {
			case Bit:
			case Boolean:
				if ( offset < data.length ) {
					result = data[offset] == 0 ? 0 : 1;
				}
				break;

			case Float32:
				if ( offset + 3 < data.length ) {
					if ( byteOrder == ByteOrdering.BigEndian ) {
						result = parseFloat32(data[offset], data[offset + 1], data[offset + 2],
								data[offset + 3]);
					} else {
						result = parseFloat32(data[offset + 3], data[offset + 2], data[offset + 1],
								data[offset]);
					}
				}
				break;

			case Float64:
				if ( offset + 7 < data.length ) {
					if ( byteOrder == ByteOrdering.BigEndian ) {
						result = parseFloat64(data[offset], data[offset + 1], data[offset + 2],
								data[offset + 3], data[offset + 4], data[offset + 5], data[offset + 6],
								data[offset + 7]);
					} else {
						result = parseFloat64(data[offset + 7], data[offset + 6], data[offset + 5],
								data[offset + 4], data[offset + 3], data[offset + 2], data[offset + 1],
								data[offset]);
					}
				}
				break;

			case Int8:
				if ( offset < data.length ) {
					result = parseInt8(data[offset]);
				}
				break;

			case UInt8:
				if ( offset < data.length ) {
					result = parseUnsignedInt8(data[offset]);
				}
				break;

			case Int16:
				if ( offset + 1 < data.length ) {
					if ( byteOrder == ByteOrdering.BigEndian ) {
						result = parseInt16(data[offset], data[offset + 1]);
					} else {
						result = parseInt16(data[offset + 1], data[offset]);
					}
				}
				break;

			case UInt16:
				if ( offset + 1 < data.length ) {
					if ( byteOrder == ByteOrdering.BigEndian ) {
						result = parseUnsignedInt16(data[offset], data[offset + 1]);
					} else {
						result = parseUnsignedInt16(data[offset + 1], data[offset]);
					}
				}
				break;

			case Int32:
				if ( offset + 3 < data.length ) {
					if ( byteOrder == ByteOrdering.BigEndian ) {
						result = parseInt32(data[offset], data[offset + 1], data[offset + 2],
								data[offset + 3]);
					} else {
						result = parseInt32(data[offset + 3], data[offset + 2], data[offset + 1],
								data[offset]);
					}
				}
				break;

			case UInt32:
				if ( offset + 3 < data.length ) {
					if ( byteOrder == ByteOrdering.BigEndian ) {
						result = parseUnsignedInt32(data[offset], data[offset + 1], data[offset + 2],
								data[offset + 3]);
					} else {
						result = parseUnsignedInt32(data[offset + 3], data[offset + 2], data[offset + 1],
								data[offset]);
					}
				}
				break;

			case Int64:
				if ( offset + 7 < data.length ) {
					if ( byteOrder == ByteOrdering.BigEndian ) {
						result = parseInt64(data[offset], data[offset + 1], data[offset + 2],
								data[offset + 3], data[offset + 4], data[offset + 5], data[offset + 6],
								data[offset + 7]);
					} else {
						result = parseInt64(data[offset + 7], data[offset + 6], data[offset + 5],
								data[offset + 4], data[offset + 3], data[offset + 2], data[offset + 1],
								data[offset]);
					}
				}
				break;

			case UInt64:
				if ( offset + 7 < data.length ) {
					if ( byteOrder == ByteOrdering.BigEndian ) {
						result = parseUnsignedInt64(data[offset], data[offset + 1], data[offset + 2],
								data[offset + 3], data[offset + 4], data[offset + 5], data[offset + 6],
								data[offset + 7]);
					} else {
						result = parseUnsignedInt64(data[offset + 7], data[offset + 6], data[offset + 5],
								data[offset + 4], data[offset + 3], data[offset + 2], data[offset + 1],
								data[offset]);
					}
				}
				break;

			case Bytes:
			case UnsignedInteger:
				if ( offset + length <= data.length ) {
					result = parseUnsignedInteger(data, offset, length, byteOrder);
				}
				break;

			case Integer:
				if ( offset + length <= data.length ) {
					result = parseInteger(data, offset, length, byteOrder);
				}
				break;

			case StringAscii:
				if ( offset + length <= data.length ) {
					result = parseDecimalCharacterString(data, offset, length, byteOrder, ASCII);
				}
				break;

			case StringUtf8:
				if ( offset + length <= data.length ) {
					result = parseDecimalCharacterString(data, offset, length, byteOrder, UTF8);
				}
				break;

			default:
				throw new IllegalArgumentException(
						"Data type " + dataType + " cannot be converted into a number.");

		}
		return result;
	}

	/**
	 * Parse an 8-bit signed integer value from a raw byte value.
	 * 
	 * @param b
	 *        bits 7-0
	 * @return the parsed integer, never {@literal null}
	 */
	public static Byte parseInt8(final byte b) {
		return b;
	}

	/**
	 * Parse an 8-bit unsigned integer value from a raw byte value.
	 * 
	 * <p>
	 * <b>Note</b> a {@code Short} is returned to support unsigned 8-bit values.
	 * </p>
	 * 
	 * @param b
	 *        bits 7-0
	 * @return the parsed integer, never {@literal null}
	 */
	public static Short parseUnsignedInt8(final byte b) {
		return (short) (b & (short) 0xFF);
	}

	/**
	 * Parse a 16-bit signed integer value from a raw byte value.
	 * 
	 * @param hi
	 *        bits 15-8
	 * @param lo
	 *        bits 7-0
	 * @return the parsed integer, never {@literal null}
	 */
	public static Short parseInt16(final byte hi, final byte lo) {
		return (short) (((hi & 0xFF) << 8) | lo & 0xFF);
	}

	/**
	 * Parse a 16-bit unsigned integer value from a raw byte value.
	 * 
	 * <p>
	 * <b>Note</b> a {@code Integer} is returned to support unsigned 16-bit
	 * values.
	 * </p>
	 * 
	 * @param hi
	 *        bits 15-8
	 * @param lo
	 *        bits 7-0
	 * @return the parsed integer, never {@literal null}
	 */
	public static Integer parseUnsignedInt16(final byte hi, final byte lo) {
		return (int) (((hi & 0xFF) << 8) | lo & 0xFF);
	}

	/**
	 * Parse a 32-bit signed integer value from raw byte values.
	 * 
	 * @param d
	 *        bits 31-24
	 * @param c
	 *        bits 23-16
	 * @param b
	 *        bits 15-8
	 * @param a
	 *        bits 7-0
	 * @return the parsed integer, never {@literal null}
	 */
	public static Integer parseInt32(final byte d, final byte c, final byte b, final byte a) {
		return (((d & 0xFF) << 24) | ((c & 0xFF) << 16) | ((b & 0xFF) << 8) | (a & 0xFF));
	}

	/**
	 * Parse a 32-bit unsigned integer value from raw byte values.
	 * 
	 * <p>
	 * <b>Note</b> a {@code Long} is returned to support unsigned 32-bit values.
	 * </p>
	 * 
	 * @param d
	 *        bits 31-24
	 * @param c
	 *        bits 23-16
	 * @param b
	 *        bits 15-8
	 * @param a
	 *        bits 7-0
	 * @return the parsed integer, never {@literal null}
	 */
	public static Long parseUnsignedInt32(final byte d, final byte c, final byte b, final byte a) {
		return (long) (((d & 0xFFL) << 24) | ((c & 0xFFL) << 16) | ((b & 0xFFL) << 8) | (a & 0xFFL));
	}

	/**
	 * Parse a 64-bit signed integer value from raw byte values.
	 * 
	 * @param h
	 *        bits 63-56
	 * @param g
	 *        bits 55-48
	 * @param f
	 *        bits 47-40
	 * @param e
	 *        bits 39-32
	 * @param d
	 *        bits 31-24
	 * @param c
	 *        bits 23-16
	 * @param b
	 *        bits 15-8
	 * @param a
	 *        bits 7-0
	 * @return the parsed integer, never {@literal null}
	 */
	public static Long parseInt64(final byte h, final byte g, final byte f, final byte e, final byte d,
			final byte c, final byte b, final byte a) {
		return (long) (((h & 0xFFL) << 56) | ((g & 0xFFL) << 48) | ((f & 0xFFL) << 40)
				| ((e & 0xFFL) << 32) | ((d & 0xFFL) << 24) | ((c & 0xFFL) << 16) | ((b & 0xFFL) << 8)
				| (a & 0xFFL));
	}

	/**
	 * Construct an 64-bit unsigned integer from raw byte values.
	 * 
	 * <p>
	 * <b>Note</b> a {@code BigInteger} is returned to support unsigned 64-bit
	 * values.
	 * </p>
	 * 
	 * @param h
	 *        bits 63-56
	 * @param g
	 *        bits 55-48
	 * @param f
	 *        bits 47-40
	 * @param e
	 *        bits 39-32
	 * @param d
	 *        bits 31-24
	 * @param c
	 *        bits 23-16
	 * @param b
	 *        bits 15-8
	 * @param a
	 *        bits 7-0
	 * @return the parsed integer, never {@literal null}
	 */
	public static BigInteger parseUnsignedInt64(final byte h, final byte g, final byte f, final byte e,
			final byte d, final byte c, final byte b, final byte a) {
		int sign = (h == 0 && g == 0 && f == 0 && e == 0 && d == 0 && c == 0 && b == 0 && a == 0 ? 0
				: 1);
		return new BigInteger(sign, new byte[] { h, g, f, e, d, c, b, a });
	}

	/**
	 * Parse an IEEE-754 32-bit float value from raw byte values.
	 * 
	 * @param d
	 *        bits 31-24
	 * @param c
	 *        bits 23-16
	 * @param b
	 *        bits 15-8
	 * @param a
	 *        bits 7-0
	 * @return the parsed float, or {@literal null} if not available or parsed
	 *         float is {@code NaN}
	 */
	public static Float parseFloat32(final byte d, final byte c, final byte b, final byte a) {
		Integer int32 = parseInt32(d, c, b, a);
		Float result = Float.intBitsToFloat(int32.intValue());
		if ( result.isNaN() ) {
			result = null;
		}
		return result;
	}

	/**
	 * Parse an IEEE-754 64-bit floating point value from raw byte values.
	 * 
	 * @param h
	 *        bits 63-56
	 * @param g
	 *        bits 55-48
	 * @param f
	 *        bits 47-40
	 * @param e
	 *        bits 39-32
	 * @param d
	 *        bits 31-24
	 * @param c
	 *        bits 23-16
	 * @param b
	 *        bits 15-8
	 * @param a
	 *        bits 7-0
	 * @return the parsed float, or {@literal null} if the result is {@code NaN}
	 */
	public static Double parseFloat64(final byte h, final byte g, final byte f, final byte e,
			final byte d, final byte c, final byte b, final byte a) {
		Long l = parseInt64(h, g, f, e, d, c, b, a);
		Double result = Double.longBitsToDouble(l);
		if ( result.isNaN() ) {
			result = null;
		}
		return result;
	}

	/**
	 * Parse any number of byte values as a series of bytes.
	 * 
	 * @param data
	 *        the data
	 * @param offset
	 *        the byte offset to start from
	 * @param length
	 *        the number of bytes to consume
	 * @param byteOrder
	 *        the byte order of {@code data}
	 * @return the parsed bytes, never {@literal null}
	 */
	public static byte[] parseBytes(byte[] data, int offset, int length, final ByteOrdering byteOrder) {
		final int len = (data == null || data.length < 1 ? 0
				: offset + length <= data.length ? length : data.length - offset);
		byte[] bytes = new byte[len];
		if ( len > 0 ) {
			if ( byteOrder == ByteOrdering.BigEndian ) {
				System.arraycopy(data, offset, bytes, 0, len);
			} else {
				for ( int i = offset + len - 1, p = 0; p < len; i--, p++ ) {
					bytes[p] = data[i];
				}
			}
		}
		return bytes;
	}

	/**
	 * Parse any number of byte values into an signed {@link BigInteger}.
	 * 
	 * @param data
	 *        the data to parse
	 * @param offset
	 *        the offset within {@code data} to start reading from
	 * @param length
	 *        the number of bytes to consume
	 * @param byteOrder
	 *        the byte order of {@code data}
	 * @return the integer value
	 */
	public static BigInteger parseInteger(byte[] data, int offset, int length, ByteOrdering byteOrder) {
		byte[] bytes = parseBytes(data, offset, length, byteOrder);
		BigInteger result;
		try {
			result = new BigInteger(bytes);
		} catch ( NumberFormatException e ) {
			result = BigInteger.ZERO;
		}
		return result;
	}

	/**
	 * Parse any number of byte values into an unsigned {@link BigInteger}.
	 * 
	 * @param data
	 *        the data to parse
	 * @param offset
	 *        the offset within {@code data} to start reading from
	 * @param length
	 *        the number of bytes to consume
	 * @param byteOrder
	 *        the byte order of {@code data}
	 * @return the integer value
	 */
	public static BigInteger parseUnsignedInteger(byte[] data, int offset, int length,
			ByteOrdering byteOrder) {
		byte[] bytes = parseBytes(data, offset, length, byteOrder);
		boolean zero = true;
		for ( int i = 0; i < bytes.length; i++ ) {
			if ( bytes[i] != 0 ) {
				zero = false;
				break;
			}
		}
		BigInteger result;
		try {
			result = new BigInteger(zero ? 0 : 1, bytes);
		} catch ( NumberFormatException e ) {
			result = BigInteger.ZERO;
		}
		return result;
	}

	/**
	 * Parse any number of byte values representing characters into a decimal
	 * string.
	 * 
	 * <p>
	 * This method interprets the {@code data} bytes as a string encoded as
	 * {@code charset}, whose string value contains a string decimal number. For
	 * example, a big-endian ASCII encoded byte array of
	 * {@literal 0x31 0x30 0x32 0x34} is interpreted as the string
	 * {@literal "1024"} which is then parsed as a {@link BigDecimal}.
	 * </p>
	 * 
	 * @param data
	 *        the data to parse
	 * @param offset
	 *        the offset within {@code data} to start reading from
	 * @param length
	 *        the number of bytes to consume
	 * @param byteOrder
	 *        the byte order of {@code data}
	 * @param charset
	 *        the character set to interpret the bytes as
	 * @return
	 */
	public static BigDecimal parseDecimalCharacterString(final byte[] data, final int offset,
			final int length, final ByteOrdering byteOrder, final Charset charset) {
		byte[] bytes = parseBytes(data, offset, length, byteOrder);
		String s = new String(bytes, charset);
		return new BigDecimal(s);
	}

	/**
	 * Convert an array of bytes to Byte objects.
	 * 
	 * @param array
	 * @param array
	 *        the array to convert
	 * @return the converted array, or {@literal null} if {@code array} is
	 *         {@literal null}
	 * @since 1.1
	 */
	public static Byte[] objectArray(byte[] array) {
		if ( array == null ) {
			return null;
		}
		final int count = array.length;
		final Byte[] result = new Byte[count];
		for ( int i = 0; i < count; i++ ) {
			result[i] = array[i];
		}
		return result;
	}

	/**
	 * Convert an array of bytes to Byte objects.
	 * 
	 * <p>
	 * {@literal 0} will be used for any {@literal null} object values.
	 * </p>
	 * 
	 * @param array
	 *        the array to convert
	 * @return the converted array, or {@literal null} if {@code array} is
	 *         {@literal null}
	 * @since 1.1
	 */
	public static byte[] byteArray(Byte[] array) {
		return byteArray(array, (byte) 0);
	}

	/**
	 * Convert an array of bytes to Byte objects.
	 * 
	 * @param array
	 *        the array to convert
	 * @param nullValue
	 *        the byte value to use for {@literal null} Byte values
	 * @return the converted array, or {@literal null} if {@code array} is
	 *         {@literal null}
	 * @since 1.1
	 */
	public static byte[] byteArray(Byte[] array, byte nullValue) {
		if ( array == null ) {
			return null;
		}
		final int count = array.length;
		final byte[] result = new byte[count];
		for ( int i = 0; i < count; i++ ) {
			result[i] = array[i] != null ? array[i].byteValue() : nullValue;
		}
		return result;
	}

}
