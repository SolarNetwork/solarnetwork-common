/* ==================================================================
 * BitDataType.java - 24/09/2019 8:54:44 pm
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
 * An enumeration of common bit-centric data types.
 * 
 * @author matt
 * @version 1.0
 * @since 1.54
 */
public enum BitDataType {

	/** An individual bit. */
	Bit(1, "bit", false, "Bit (on/off)"),

	/** Boolean byte. */
	Boolean("B", 1, false, "Boolean (true/false)"),

	/** 32-bit floating point. */
	Float32("f32", 4, true, "32-bit floating point (4 bytes)"),

	/** 64-bit floating point. */
	Float64("f64", 8, true, "64-bit floating point (8 bytes)"),

	/** Signed 8-bit integer. */
	Int8("i8", 1, true, "8-bit signed integer (1 byte)"),

	/** Unsigned 16-bit integer. */
	UInt8("u8", 1, "8-bit unsigned integer (1 byte)"),

	/** Signed 16-bit integer. */
	Int16("i16", 2, true, "16-bit signed integer (2 bytes)"),

	/** Unsigned 16-bit integer. */
	UInt16("u16", 2, "16-bit unsigned integer (2 bytes)"),

	/** Signed 32-bit integer. */
	Int32("i32", 4, true, "32-bit signed integer (4 bytes)"),

	/** Unsigned 32-bit integer. */
	UInt32("u32", 4, "32-bit unsigned integer (4 bytes)"),

	/** Signed 64-bit integer. */
	Int64("i64", 8, true, "64-bit signed integer (8 bytes)"),

	/** Unsigned 64-bit integer. */
	UInt64("u64", 8, "64-bit unsigned integer (8 bytes)"),

	/**
	 * Arbitrary bit-length signed integer, where most-significant bit
	 * represents sign.
	 */
	Integer("I", -1, true, "Integer"),

	/** Arbitrary bit-length unsigned integer. */
	UnsignedInteger("U", -1, "Unsigned integer"),

	/** Raw bytes. */
	Bytes("b", -1, "Bytes", false),

	/** Bytes interpreted as a UTF-8 encoded string. */
	StringUtf8("s", -1, "String (ASCII)", false),

	/** Bytes interpreted as an ASCII encoded string. */
	StringAscii("a", -1, "String (UTF-8)", false);

	private final String key;
	private final int bitLength;
	private final int byteLength;
	private final boolean number;
	private final boolean signed;
	private final String description;

	private BitDataType(int bitLength, String key, boolean signed, String description) {
		this.key = key;
		this.bitLength = bitLength;
		this.byteLength = bitLength + (8 - (bitLength % 8));
		this.number = true;
		this.signed = signed;
		this.description = description;
	}

	private BitDataType(String key, int byteLength, String description) {
		this(key, byteLength, false, description);
	}

	private BitDataType(String key, int byteLength, boolean signed, String description) {
		this.key = key;
		this.byteLength = byteLength;
		this.bitLength = byteLength * 8;
		this.number = true;
		this.signed = signed;
		this.description = description;
	}

	private BitDataType(String key, int byteLength, String description, boolean number) {
		this.key = key;
		this.bitLength = (byteLength < 0 ? -1 : byteLength * 8);
		this.byteLength = byteLength;
		this.number = number;
		this.signed = false;
		this.description = description;
	}

	/**
	 * Get the key value for this enum.
	 * 
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Get the number of bits this data type requires.
	 * 
	 * @return the number of bits, or {@literal -1} for an unknown length (for
	 *         example for strings)
	 */
	public int getBitLength() {
		return bitLength;
	}

	/**
	 * Test if this type has a bit length that is not an even multiple of
	 * {@literal 8}.
	 * 
	 * @return {@literal true} if {@link #getBitLength()} is not evenly
	 *         divisible by {@literal 8}
	 */
	public boolean isFractionalByteLength() {
		return bitLength % 8 != 0;
	}

	/**
	 * Get the number of bytes this data type requires.
	 * 
	 * <p>
	 * For types where {@link #getBitLength()} is not an even multiple of
	 * {@literal 8}, the returned value will be rounded <b>up</b> to the next
	 * multiple of {@literal 8}.
	 * </p>
	 * 
	 * @return the number of bytes, or {@literal -1} for an unknown length (for
	 *         example for strings)
	 */
	public int getByteLength() {
		return byteLength;
	}

	/**
	 * Test if the type represents a number.
	 * 
	 * @return {@literal true} if the type is a number type, {@literal false}
	 *         otherwise (such as bytes or a string)
	 */
	public boolean isNumber() {
		return number;
	}

	/**
	 * Get signed flag.
	 * 
	 * @return {@literal true} if the data type represents a signed number,
	 *         {@literal false} for unsigned (or not a number)
	 */
	public boolean isSigned() {
		return signed;
	}

	/**
	 * Test if the type has a variable length.
	 * 
	 * <p>
	 * A type like {@link #StringUtf8} has a variable length, while
	 * {@link #Int32} is fixed.
	 * </p>
	 * 
	 * @return {@literal true} if the type has a variable number of bytes,
	 *         {@literal false} if a fixed number
	 */
	public boolean isVariableLength() {
		return byteLength < 0;
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
	public static BitDataType forKey(String key) {
		for ( BitDataType e : BitDataType.values() ) {
			if ( key.equals(e.key) ) {
				return e;
			}
		}

		throw new IllegalArgumentException("Unknown BitDataType key [" + key + "]");
	}

	/**
	 * Get a friendly description for this data type.
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

}
