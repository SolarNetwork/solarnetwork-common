/* ==================================================================
 * Half.java - 6/08/2021 11:19:06 AM
 * 
 * Copyright 2021 SolarNetwork.net Dev Team
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
 * 
 * Derived from android.util.Half:
 *
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ==================================================================
 */

package net.solarnetwork.util;

/**
 * An IEEE 754 half-precision (16-bit) floating point number.
 * 
 * @author matt
 * @version 1.0
 * @since 1.51
 * @see <a href=
 *      "https://en.wikipedia.org/wiki/Half-precision_floating-point_format">IEEE
 *      754 half-precision</a>
 */
public class Half extends Number implements Comparable<Half> {

	private static final long serialVersionUID = -8561082817144114958L;

	/**
	 * /** The number of bits used to represent a half-precision float value.
	 */
	public static final int SIZE = 16;
	/**
	 * Epsilon is the difference between 1.0 and the next value representable by
	 * a half-precision floating-point.
	 */
	public static final short EPSILON = (short) 0x1400;
	/**
	 * Maximum exponent a finite half-precision float may have.
	 */
	public static final int MAX_EXPONENT = 15;
	/**
	 * Minimum exponent a normalized half-precision float may have.
	 */
	public static final int MIN_EXPONENT = -14;
	/**
	 * Smallest negative value a half-precision float may have.
	 */
	public static final short LOWEST_VALUE = (short) 0xfbff;
	/**
	 * Maximum positive finite value a half-precision float may have.
	 */
	public static final short MAX_VALUE = (short) 0x7bff;
	/**
	 * Smallest positive normal value a half-precision float may have.
	 */
	public static final short MIN_NORMAL = (short) 0x0400;
	/**
	 * Smallest positive non-zero value a half-precision float may have.
	 */
	public static final short MIN_VALUE = (short) 0x0001;
	/**
	 * A Not-a-Number representation of a half-precision float.
	 */
	public static final short NaN = (short) 0x7e00;
	/**
	 * Negative infinity of type half-precision float.
	 */
	public static final short NEGATIVE_INFINITY = (short) 0xfc00;
	/**
	 * Negative 0 of type half-precision float.
	 */
	public static final short NEGATIVE_ZERO = (short) 0x8000;
	/**
	 * Positive infinity of type half-precision float.
	 */
	public static final short POSITIVE_INFINITY = (short) 0x7c00;
	/**
	 * Positive 0 of type half-precision float.
	 */
	public static final short POSITIVE_ZERO = (short) 0x0000;

	/** The value. */
	private final short mValue;

	/**
	 * Constructs a newly allocated {@code Half} object that represents the
	 * half-precision float type argument.
	 *
	 * @param value
	 *        The value to be represented by the {@code Half}
	 */
	public Half(short value) {
		mValue = value;
	}

	/**
	 * Constructs a newly allocated {@code Half} object that represents the
	 * argument converted to a half-precision float.
	 *
	 * @param value
	 *        The value to be represented by the {@code Half}
	 *
	 * @see #toHalf(float)
	 */
	public Half(float value) {
		mValue = toHalf(value);
	}

	/**
	 * Constructs a newly allocated {@code Half} object that represents the
	 * argument converted to a half-precision float.
	 *
	 * @param value
	 *        The value to be represented by the {@code Half}
	 *
	 * @see #toHalf(float)
	 */
	public Half(double value) {
		mValue = toHalf((float) value);
	}

	/**
	 * <p>
	 * Constructs a newly allocated {@code Half} object that represents the
	 * half-precision float value represented by the string. The string is
	 * converted to a half-precision float value as if by the
	 * {@link #valueOf(String)} method.
	 * </p>
	 *
	 * <p>
	 * Calling this constructor is equivalent to calling:
	 * </p>
	 * 
	 * <pre>
	 * new Half(Float.parseFloat(value))
	 * </pre>
	 *
	 * @param value
	 *        A string to be converted to a {@code Half}
	 * @throws NumberFormatException
	 *         if the string does not contain a parsable number
	 *
	 * @see Float#valueOf(java.lang.String)
	 * @see #toHalf(float)
	 */
	public Half(String value) throws NumberFormatException {
		mValue = toHalf(Float.parseFloat(value));
	}

	/**
	 * Returns the half-precision value of this {@code Half} as a {@code short}
	 * containing the bit representation described in {@link Half}.
	 *
	 * @return The half-precision float value represented by this object
	 */
	public short halfValue() {
		return mValue;
	}

	/**
	 * Returns the value of this {@code Half} as a {@code byte} after a
	 * narrowing primitive conversion.
	 *
	 * @return The half-precision float value represented by this object
	 *         converted to type {@code byte}
	 */
	@Override
	public byte byteValue() {
		return (byte) toFloat(mValue);
	}

	/**
	 * Returns the value of this {@code Half} as a {@code short} after a
	 * narrowing primitive conversion.
	 *
	 * @return The half-precision float value represented by this object
	 *         converted to type {@code short}
	 */
	@Override
	public short shortValue() {
		return (short) toFloat(mValue);
	}

	/**
	 * Returns the value of this {@code Half} as a {@code int} after a narrowing
	 * primitive conversion.
	 *
	 * @return The half-precision float value represented by this object
	 *         converted to type {@code int}
	 */
	@Override
	public int intValue() {
		return (int) toFloat(mValue);
	}

	/**
	 * Returns the value of this {@code Half} as a {@code long} after a
	 * narrowing primitive conversion.
	 *
	 * @return The half-precision float value represented by this object
	 *         converted to type {@code long}
	 */
	@Override
	public long longValue() {
		return (long) toFloat(mValue);
	}

	/**
	 * Returns the value of this {@code Half} as a {@code float} after a
	 * widening primitive conversion.
	 *
	 * @return The half-precision float value represented by this object
	 *         converted to type {@code float}
	 */
	@Override
	public float floatValue() {
		return toFloat(mValue);
	}

	/**
	 * Returns the value of this {@code Half} as a {@code double} after a
	 * widening primitive conversion.
	 *
	 * @return The half-precision float value represented by this object
	 *         converted to type {@code double}
	 */
	@Override
	public double doubleValue() {
		return toFloat(mValue);
	}

	/**
	 * Returns true if this {@code Half} value represents a Not-a-Number, false
	 * otherwise.
	 *
	 * @return True if the value is a NaN, false otherwise
	 */
	public boolean isNaN() {
		return isNaN(mValue);
	}

	/**
	 * Compares this object against the specified object. The result is
	 * {@code true} if and only if the argument is not {@literal null} and is a
	 * {@code Half} object that represents the same half-precision value as the
	 * this object. Two half-precision values are considered to be the same if
	 * and only if the method {@link #halfToIntBits(short)} returns an identical
	 * {@code int} value for both.
	 *
	 * @param o
	 *        The object to compare
	 * @return True if the objects are the same, false otherwise
	 *
	 * @see #halfToIntBits(short)
	 */
	@Override
	public boolean equals(Object o) {
		return (o instanceof Half) && (halfToIntBits(((Half) o).mValue) == halfToIntBits(mValue));
	}

	/**
	 * Returns a hash code for this {@code Half} object. The result is the
	 * integer bit representation, exactly as produced by the method
	 * {@link #halfToIntBits(short)}, of the primitive half-precision float
	 * value represented by this {@code Half} object.
	 *
	 * @return A hash code value for this object
	 */
	@Override
	public int hashCode() {
		return hashCode(mValue);
	}

	/**
	 * Returns a string representation of the specified half-precision float
	 * value. See {@link #toString(short)} for more information.
	 *
	 * @return A string representation of this {@code Half} object
	 */

	@Override
	public String toString() {
		return toString(mValue);
	}

	/**
	 * <p>
	 * Compares the two specified half-precision float values. The following
	 * conditions apply during the comparison:
	 * </p>
	 *
	 * <ul>
	 * <li>{@link #NaN} is considered by this method to be equal to itself and
	 * greater than all other half-precision float values (including
	 * {@code #POSITIVE_INFINITY})</li>
	 * <li>{@link #POSITIVE_ZERO} is considered by this method to be greater
	 * than {@link #NEGATIVE_ZERO}.</li>
	 * </ul>
	 *
	 * @param h
	 *        The half-precision float value to compare to the half-precision
	 *        value represented by this {@code Half} object
	 *
	 * @return The value {@code 0} if {@code x} is numerically equal to
	 *         {@code y}; a value less than {@code 0} if {@code x} is
	 *         numerically less than {@code y}; and a value greater than
	 *         {@code 0} if {@code x} is numerically greater than {@code y}
	 */
	@Override
	public int compareTo(Half h) {
		return compare(mValue, h.mValue);
	}

	/**
	 * Returns a hash code for a half-precision float value.
	 *
	 * @param h
	 *        The value to hash
	 *
	 * @return A hash code value for a half-precision float value
	 */
	public static int hashCode(short h) {
		return halfToIntBits(h);
	}

	/**
	 * <p>
	 * Compares the two specified half-precision float values. The following
	 * conditions apply during the comparison:
	 * </p>
	 *
	 * <ul>
	 * <li>{@link #NaN} is considered by this method to be equal to itself and
	 * greater than all other half-precision float values (including
	 * {@code #POSITIVE_INFINITY})</li>
	 * <li>{@link #POSITIVE_ZERO} is considered by this method to be greater
	 * than {@link #NEGATIVE_ZERO}.</li>
	 * </ul>
	 *
	 * @param x
	 *        The first half-precision float value to compare.
	 * @param y
	 *        The second half-precision float value to compare
	 *
	 * @return The value {@code 0} if {@code x} is numerically equal to
	 *         {@code y}, a value less than {@code 0} if {@code x} is
	 *         numerically less than {@code y}, and a value greater than
	 *         {@code 0} if {@code x} is numerically greater than {@code y}
	 */
	public static int compare(short x, short y) {
		return FP16.compare(x, y);
	}

	/**
	 * <p>
	 * Returns a representation of the specified half-precision float value
	 * according to the bit layout described in {@link Half}.
	 * </p>
	 *
	 * <p>
	 * Similar to {@link #halfToIntBits(short)}, this method collapses all
	 * possible Not-a-Number values to a single canonical Not-a-Number value
	 * defined by {@link #NaN}.
	 * </p>
	 *
	 * @param h
	 *        A half-precision float value
	 * @return The bits that represent the half-precision float value
	 *
	 * @see #halfToIntBits(short)
	 */
	public static short halfToShortBits(short h) {
		return (h & FP16.EXPONENT_SIGNIFICAND_MASK) > FP16.POSITIVE_INFINITY ? NaN : h;
	}

	/**
	 * <p>
	 * Returns a representation of the specified half-precision float value
	 * according to the bit layout described in {@link Half}.
	 * </p>
	 *
	 * <p>
	 * Unlike {@link #halfToRawIntBits(short)}, this method collapses all
	 * possible Not-a-Number values to a single canonical Not-a-Number value
	 * defined by {@link #NaN}.
	 * </p>
	 *
	 * @param h
	 *        A half-precision float value
	 * @return The bits that represent the half-precision float value
	 *
	 * @see #halfToRawIntBits(short)
	 * @see #halfToShortBits(short)
	 * @see #intBitsToHalf(int)
	 */
	public static int halfToIntBits(short h) {
		return (h & FP16.EXPONENT_SIGNIFICAND_MASK) > FP16.POSITIVE_INFINITY ? NaN : h & 0xffff;
	}

	/**
	 * <p>
	 * Returns a representation of the specified half-precision float value
	 * according to the bit layout described in {@link Half}.
	 * </p>
	 *
	 * <p>
	 * The argument is considered to be a representation of a half-precision
	 * float value according to the bit layout described in {@link Half}. The 16
	 * most significant bits of the returned value are set to 0.
	 * </p>
	 *
	 * @param h
	 *        A half-precision float value
	 * @return The bits that represent the half-precision float value
	 *
	 * @see #halfToIntBits(short)
	 * @see #intBitsToHalf(int)
	 */
	public static int halfToRawIntBits(short h) {
		return h & 0xffff;
	}

	/**
	 * <p>
	 * Returns the half-precision float value corresponding to a given bit
	 * representation.
	 * </p>
	 *
	 * <p>
	 * The argument is considered to be a representation of a half-precision
	 * float value according to the bit layout described in {@link Half}. The 16
	 * most significant bits of the argument are ignored.
	 * </p>
	 *
	 * @param bits
	 *        An integer
	 * @return The half-precision float value with the same bit pattern
	 */
	public static short intBitsToHalf(int bits) {
		return (short) (bits & 0xffff);
	}

	/**
	 * Returns the first parameter with the sign of the second parameter. This
	 * method treats NaNs as having a sign.
	 *
	 * @param magnitude
	 *        A half-precision float value providing the magnitude of the result
	 * @param sign
	 *        A half-precision float value providing the sign of the result
	 * @return A value with the magnitude of the first parameter and the sign of
	 *         the second parameter
	 */
	public static short copySign(short magnitude, short sign) {
		return (short) ((sign & FP16.SIGN_MASK) | (magnitude & FP16.EXPONENT_SIGNIFICAND_MASK));
	}

	/**
	 * Returns the absolute value of the specified half-precision float. Special
	 * values are handled in the following ways:
	 * <ul>
	 * <li>If the specified half-precision float is NaN, the result is NaN</li>
	 * <li>If the specified half-precision float is zero (negative or positive),
	 * the result is positive zero (see {@link #POSITIVE_ZERO})</li>
	 * <li>If the specified half-precision float is infinity (negative or
	 * positive), the result is positive infinity (see
	 * {@link #POSITIVE_INFINITY})</li>
	 * </ul>
	 *
	 * @param h
	 *        A half-precision float value
	 * @return The absolute value of the specified half-precision float
	 */
	public static short abs(short h) {
		return (short) (h & FP16.EXPONENT_SIGNIFICAND_MASK);
	}

	/**
	 * Returns the closest integral half-precision float value to the specified
	 * half-precision float value. Special values are handled in the following
	 * ways:
	 * <ul>
	 * <li>If the specified half-precision float is NaN, the result is NaN</li>
	 * <li>If the specified half-precision float is infinity (negative or
	 * positive), the result is infinity (with the same sign)</li>
	 * <li>If the specified half-precision float is zero (negative or positive),
	 * the result is zero (with the same sign)</li>
	 * </ul>
	 *
	 * <p class=note>
	 * <strong>Note:</strong> Unlike the identically named
	 * <code class=prettyprint>int java.lang.Math.round(float)</code> method,
	 * this returns a Half value stored in a short, <strong>not</strong> an
	 * actual short integer result.
	 *
	 * @param h
	 *        A half-precision float value
	 * @return The value of the specified half-precision float rounded to the
	 *         nearest half-precision float value
	 */
	public static short round(short h) {
		return FP16.rint(h);
	}

	/**
	 * Returns the smallest half-precision float value toward negative infinity
	 * greater than or equal to the specified half-precision float value.
	 * Special values are handled in the following ways:
	 * <ul>
	 * <li>If the specified half-precision float is NaN, the result is NaN</li>
	 * <li>If the specified half-precision float is infinity (negative or
	 * positive), the result is infinity (with the same sign)</li>
	 * <li>If the specified half-precision float is zero (negative or positive),
	 * the result is zero (with the same sign)</li>
	 * </ul>
	 *
	 * @param h
	 *        A half-precision float value
	 * @return The smallest half-precision float value toward negative infinity
	 *         greater than or equal to the specified half-precision float value
	 */
	public static short ceil(short h) {
		return FP16.ceil(h);
	}

	/**
	 * Returns the largest half-precision float value toward positive infinity
	 * less than or equal to the specified half-precision float value. Special
	 * values are handled in the following ways:
	 * <ul>
	 * <li>If the specified half-precision float is NaN, the result is NaN</li>
	 * <li>If the specified half-precision float is infinity (negative or
	 * positive), the result is infinity (with the same sign)</li>
	 * <li>If the specified half-precision float is zero (negative or positive),
	 * the result is zero (with the same sign)</li>
	 * </ul>
	 *
	 * @param h
	 *        A half-precision float value
	 * @return The largest half-precision float value toward positive infinity
	 *         less than or equal to the specified half-precision float value
	 */
	public static short floor(short h) {
		return FP16.floor(h);
	}

	/**
	 * Returns the truncated half-precision float value of the specified
	 * half-precision float value. Special values are handled in the following
	 * ways:
	 * <ul>
	 * <li>If the specified half-precision float is NaN, the result is NaN</li>
	 * <li>If the specified half-precision float is infinity (negative or
	 * positive), the result is infinity (with the same sign)</li>
	 * <li>If the specified half-precision float is zero (negative or positive),
	 * the result is zero (with the same sign)</li>
	 * </ul>
	 *
	 * @param h
	 *        A half-precision float value
	 * @return The truncated half-precision float value of the specified
	 *         half-precision float value
	 */
	public static short trunc(short h) {
		return FP16.trunc(h);
	}

	/**
	 * Returns the smaller of two half-precision float values (the value closest
	 * to negative infinity). Special values are handled in the following ways:
	 * <ul>
	 * <li>If either value is NaN, the result is NaN</li>
	 * <li>{@link #NEGATIVE_ZERO} is smaller than {@link #POSITIVE_ZERO}</li>
	 * </ul>
	 *
	 * @param x
	 *        The first half-precision value
	 * @param y
	 *        The second half-precision value
	 * @return The smaller of the two specified half-precision values
	 */
	public static short min(short x, short y) {
		return FP16.min(x, y);
	}

	/**
	 * Returns the larger of two half-precision float values (the value closest
	 * to positive infinity). Special values are handled in the following ways:
	 * <ul>
	 * <li>If either value is NaN, the result is NaN</li>
	 * <li>{@link #POSITIVE_ZERO} is greater than {@link #NEGATIVE_ZERO}</li>
	 * </ul>
	 *
	 * @param x
	 *        The first half-precision value
	 * @param y
	 *        The second half-precision value
	 *
	 * @return The larger of the two specified half-precision values
	 */
	public static short max(short x, short y) {
		return FP16.max(x, y);
	}

	/**
	 * Returns true if the first half-precision float value is less (smaller
	 * toward negative infinity) than the second half-precision float value. If
	 * either of the values is NaN, the result is false.
	 *
	 * @param x
	 *        The first half-precision value
	 * @param y
	 *        The second half-precision value
	 *
	 * @return True if x is less than y, false otherwise
	 */
	public static boolean less(short x, short y) {
		return FP16.less(x, y);
	}

	/**
	 * Returns true if the first half-precision float value is less (smaller
	 * toward negative infinity) than or equal to the second half-precision
	 * float value. If either of the values is NaN, the result is false.
	 *
	 * @param x
	 *        The first half-precision value
	 * @param y
	 *        The second half-precision value
	 *
	 * @return True if x is less than or equal to y, false otherwise
	 */
	public static boolean lessEquals(short x, short y) {
		return FP16.lessEquals(x, y);
	}

	/**
	 * Returns true if the first half-precision float value is greater (larger
	 * toward positive infinity) than the second half-precision float value. If
	 * either of the values is NaN, the result is false.
	 *
	 * @param x
	 *        The first half-precision value
	 * @param y
	 *        The second half-precision value
	 *
	 * @return True if x is greater than y, false otherwise
	 */
	public static boolean greater(short x, short y) {
		return FP16.greater(x, y);
	}

	/**
	 * Returns true if the first half-precision float value is greater (larger
	 * toward positive infinity) than or equal to the second half-precision
	 * float value. If either of the values is NaN, the result is false.
	 *
	 * @param x
	 *        The first half-precision value
	 * @param y
	 *        The second half-precision value
	 *
	 * @return True if x is greater than y, false otherwise
	 */
	public static boolean greaterEquals(short x, short y) {
		return FP16.greaterEquals(x, y);
	}

	/**
	 * Returns true if the two half-precision float values are equal. If either
	 * of the values is NaN, the result is false. {@link #POSITIVE_ZERO} and
	 * {@link #NEGATIVE_ZERO} are considered equal.
	 *
	 * @param x
	 *        The first half-precision value
	 * @param y
	 *        The second half-precision value
	 *
	 * @return True if x is equal to y, false otherwise
	 */
	public static boolean equals(short x, short y) {
		return FP16.equals(x, y);
	}

	/**
	 * Returns the sign of the specified half-precision float.
	 *
	 * @param h
	 *        A half-precision float value
	 * @return 1 if the value is positive, -1 if the value is negative
	 */
	public static int getSign(short h) {
		return (h & FP16.SIGN_MASK) == 0 ? 1 : -1;
	}

	/**
	 * Returns the unbiased exponent used in the representation of the specified
	 * half-precision float value. if the value is NaN or infinite, this* method
	 * returns {@link #MAX_EXPONENT} + 1. If the argument is 0 or a subnormal
	 * representation, this method returns {@link #MIN_EXPONENT} - 1.
	 *
	 * @param h
	 *        A half-precision float value
	 * @return The unbiased exponent of the specified value
	 */
	public static int getExponent(short h) {
		return ((h >>> FP16.EXPONENT_SHIFT) & FP16.SHIFTED_EXPONENT_MASK) - FP16.EXPONENT_BIAS;
	}

	/**
	 * Returns the significand, or mantissa, used in the representation of the
	 * specified half-precision float value.
	 *
	 * @param h
	 *        A half-precision float value
	 * @return The significand, or significand, of the specified vlaue
	 */
	public static int getSignificand(short h) {
		return h & FP16.SIGNIFICAND_MASK;
	}

	/**
	 * Returns true if the specified half-precision float value represents
	 * infinity, false otherwise.
	 *
	 * @param h
	 *        A half-precision float value
	 * @return True if the value is positive infinity or negative infinity,
	 *         false otherwise
	 */
	public static boolean isInfinite(short h) {
		return FP16.isInfinite(h);
	}

	/**
	 * Returns true if the specified half-precision float value represents a
	 * Not-a-Number, false otherwise.
	 *
	 * @param h
	 *        A half-precision float value
	 * @return True if the value is a NaN, false otherwise
	 */
	public static boolean isNaN(short h) {
		return FP16.isNaN(h);
	}

	/**
	 * Returns true if the specified half-precision float value is normalized
	 * (does not have a subnormal representation). If the specified value is
	 * {@link #POSITIVE_INFINITY}, {@link #NEGATIVE_INFINITY},
	 * {@link #POSITIVE_ZERO}, {@link #NEGATIVE_ZERO}, NaN or any subnormal
	 * number, this method returns false.
	 *
	 * @param h
	 *        A half-precision float value
	 * @return True if the value is normalized, false otherwise
	 */
	public static boolean isNormalized(short h) {
		return FP16.isNormalized(h);
	}

	/**
	 * <p>
	 * Converts the specified half-precision float value into a single-precision
	 * float value. The following special cases are handled:
	 * </p>
	 * <ul>
	 * <li>If the input is {@link #NaN}, the returned value is
	 * {@link Float#NaN}</li>
	 * <li>If the input is {@link #POSITIVE_INFINITY} or
	 * {@link #NEGATIVE_INFINITY}, the returned value is respectively
	 * {@link Float#POSITIVE_INFINITY} or {@link Float#NEGATIVE_INFINITY}</li>
	 * <li>If the input is 0 (positive or negative), the returned value is
	 * +/-0.0f</li>
	 * <li>Otherwise, the returned value is a normalized single-precision float
	 * value</li>
	 * </ul>
	 *
	 * @param h
	 *        The half-precision float value to convert to single-precision
	 * @return A normalized single-precision float value
	 */
	public static float toFloat(short h) {
		return FP16.toFloat(h);
	}

	/**
	 * <p>
	 * Converts the specified single-precision float value into a half-precision
	 * float value. The following special cases are handled:
	 * </p>
	 * <ul>
	 * <li>If the input is NaN (see {@link Float#isNaN(float)}), the returned
	 * value is {@link #NaN}</li>
	 * <li>If the input is {@link Float#POSITIVE_INFINITY} or
	 * {@link Float#NEGATIVE_INFINITY}, the returned value is respectively
	 * {@link #POSITIVE_INFINITY} or {@link #NEGATIVE_INFINITY}</li>
	 * <li>If the input is 0 (positive or negative), the returned value is
	 * {@link #POSITIVE_ZERO} or {@link #NEGATIVE_ZERO}</li>
	 * <li>If the input is a less than {@link #MIN_VALUE}, the returned value is
	 * flushed to {@link #POSITIVE_ZERO} or {@link #NEGATIVE_ZERO}</li>
	 * <li>If the input is a less than {@link #MIN_NORMAL}, the returned value
	 * is a denorm half-precision float</li>
	 * <li>Otherwise, the returned value is rounded to the nearest representable
	 * half-precision float value</li>
	 * </ul>
	 *
	 * @param f
	 *        The single-precision float value to convert to half-precision
	 * @return A half-precision float value
	 */
	public static short toHalf(float f) {
		return FP16.toHalf(f);
	}

	/**
	 * Returns a {@code Half} instance representing the specified half-precision
	 * float value.
	 *
	 * @param h
	 *        A half-precision float value
	 * @return a {@code Half} instance representing {@code h}
	 */
	public static Half valueOf(short h) {
		return new Half(h);
	}

	/**
	 * Returns a {@code Half} instance representing the specified float value.
	 *
	 * @param f
	 *        A float value
	 * @return a {@code Half} instance representing {@code f}
	 */
	public static Half valueOf(float f) {
		return new Half(f);
	}

	/**
	 * Returns a {@code Half} instance representing the specified string value.
	 * Calling this method is equivalent to calling
	 * <code>toHalf(Float.parseString(h))</code>. See
	 * {@link Float#valueOf(String)} for more information on the format of the
	 * string representation.
	 *
	 * @param s
	 *        The string to be parsed
	 * @return a {@code Half} instance representing {@code h}
	 * @throws NumberFormatException
	 *         if the string does not contain a parsable half-precision float
	 *         value
	 */
	public static Half valueOf(String s) {
		return new Half(s);
	}

	/**
	 * Returns the half-precision float value represented by the specified
	 * string. Calling this method is equivalent to calling
	 * <code>toHalf(Float.parseString(h))</code>. See
	 * {@link Float#valueOf(String)} for more information on the format of the
	 * string representation.
	 *
	 * @param s
	 *        The string to be parsed
	 * @return A half-precision float value represented by the string
	 * @throws NumberFormatException
	 *         if the string does not contain a parsable half-precision float
	 *         value
	 */
	public static short parseHalf(String s) throws NumberFormatException {
		return toHalf(Float.parseFloat(s));
	}

	/**
	 * Returns a string representation of the specified half-precision float
	 * value. Calling this method is equivalent to calling
	 * <code>Float.toString(toFloat(h))</code>. See
	 * {@link Float#toString(float)} for more information on the format of the
	 * string representation.
	 *
	 * @param h
	 *        A half-precision float value
	 * @return A string representation of the specified value
	 */
	public static String toString(short h) {
		return Float.toString(toFloat(h));
	}

	/**
	 * <p>
	 * Returns a hexadecimal string representation of the specified
	 * half-precision float value. If the value is a NaN, the result is
	 * <code>"NaN"</code>, otherwise the result follows this format:
	 * </p>
	 * <ul>
	 * <li>If the sign is positive, no sign character appears in the result</li>
	 * <li>If the sign is negative, the first character is <code>'-'</code></li>
	 * <li>If the value is inifinity, the string is <code>"Infinity"</code></li>
	 * <li>If the value is 0, the string is <code>"0x0.0p0"</code></li>
	 * <li>If the value has a normalized representation, the exponent and
	 * significand are represented in the string in two fields. The significand
	 * starts with <code>"0x1."</code> followed by its lowercase hexadecimal
	 * representation. Trailing zeroes are removed unless all digits are 0, then
	 * a single zero is used. The significand representation is followed by the
	 * exponent, represented by <code>"p"</code>, itself followed by a decimal
	 * string of the unbiased exponent</li>
	 * <li>If the value has a subnormal representation, the significand starts
	 * with <code>"0x0."</code> followed by its lowercase hexadecimal
	 * representation. Trailing zeroes are removed unless all digits are 0, then
	 * a single zero is used. The significand representation is followed by the
	 * exponent, represented by <code>"p-14"</code></li>
	 * </ul>
	 *
	 * @param h
	 *        A half-precision float value
	 * @return A hexadecimal string representation of the specified value
	 */
	public static String toHexString(short h) {
		return FP16.toHexString(h);
	}

}
