/* ==================================================================
 * DatumMathFunctions.java - 28/02/2022 9:51:20 AM
 *
 * Copyright 2022 SolarNetwork.net Dev Team
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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import net.solarnetwork.util.NumberUtils;
import net.solarnetwork.util.StringUtils;

/**
 * API for datum-related math helper functions.
 *
 * @author matt
 * @version 1.5
 * @since 2.1
 */
public interface DatumMathFunctions {

	/**
	 * Apply a bitwise {@literal and} operation to an integer number.
	 *
	 * @param n
	 *        the integer number
	 * @param mask
	 *        the mask
	 * @return the result of {@code (n & mask)}, or {@code n} as an integer if
	 *         {@code mask} is {@literal null} or {@literal null} if {@code n}
	 *         cannot be converted to an integer
	 * @since 1.1
	 */
	default BigInteger and(Number n, Number mask) {
		BigInteger integer = integer(n);
		BigInteger m = integer(mask);
		return (integer != null && m != null ? integer.and(m) : integer);
	}

	/**
	 * Apply a bitwise {@literal not} operation to an integer number.
	 *
	 * @param n
	 *        the integer number
	 * @return the result of {@code (~n)}, or {@code n} as an integer or
	 *         {@literal null} if {@code n} cannot be converted to an integer
	 * @since 1.1
	 */
	default BigInteger not(Number n) {
		BigInteger integer = integer(n);
		return (integer != null ? integer.not() : integer);
	}

	/**
	 * Apply a bitwise {@literal and} operation to an integer number that has
	 * had a {@literal not} operation applied.
	 *
	 * @param n
	 *        the integer number
	 * @param mask
	 *        the mask
	 * @return the result of {@code (n & ~mask)}, or {@code n} as an integer if
	 *         {@code mask} is {@literal null} or {@literal null} if {@code n}
	 *         cannot be converted to an integer
	 * @since 1.1
	 */
	default BigInteger andNot(Number n, Number mask) {
		BigInteger integer = integer(n);
		BigInteger m = integer(mask);
		return (integer != null && m != null ? integer.andNot(m) : integer);
	}

	/**
	 * Apply a bitwise {@literal or} operation to an integer number.
	 *
	 * @param n
	 *        the integer number
	 * @param mask
	 *        the mask
	 * @return the result of {@code (n | mask)}, or {@code n} as an integer if
	 *         {@code mask} is {@literal null} or {@literal null} if {@code n}
	 *         cannot be converted to an integer
	 * @since 1.1
	 */
	default BigInteger or(Number n, Number mask) {
		BigInteger integer = integer(n);
		BigInteger m = integer(mask);
		return (integer != null && m != null ? integer.or(m) : integer);
	}

	/**
	 * Apply a bitwise {@literal xor} operation to an integer number.
	 *
	 * @param n
	 *        the integer number
	 * @param mask
	 *        the mask
	 * @return the result of {@code (n ^ mask)}, or {@code n} as an integer if
	 *         {@code mask} is {@literal null} or {@literal null} if {@code n}
	 *         cannot be converted to an integer
	 * @since 1.1
	 */
	default BigInteger xor(Number n, Number mask) {
		BigInteger integer = integer(n);
		BigInteger m = integer(mask);
		return (integer != null && m != null ? integer.xor(m) : integer);
	}

	/**
	 * Apply a bitwise right-shift operation to an integer number.
	 *
	 * @param n
	 *        the integer number
	 * @param count
	 *        the shift distance, in bits
	 * @return the result of {@code (n >> count)}, or {@code n} as an integer if
	 *         {@code count} is {@literal null} or {@literal null} if {@code n}
	 *         cannot be converted to an integer
	 * @since 1.1
	 */
	default BigInteger shiftRight(Number n, Number count) {
		BigInteger integer = integer(n);
		return (integer != null && count != null ? integer.shiftRight(count.intValue()) : integer);
	}

	/**
	 * Apply a bitwise left-shift operation to an integer number.
	 *
	 * @param n
	 *        the integer number
	 * @param count
	 *        the shift distance, in bits
	 * @return the result of {@code (n << count)}, or {@code n} as an integer if
	 *         {@code count} is {@literal null} or {@literal null} if {@code n}
	 *         cannot be converted to an integer
	 * @since 1.1
	 */
	default BigInteger shiftLeft(Number n, Number count) {
		BigInteger integer = integer(n);
		return (integer != null && count != null ? integer.shiftLeft(count.intValue()) : integer);
	}

	/**
	 * Test if a bit is set on an integer number.
	 *
	 * @param n
	 *        the integer number
	 * @param bit
	 *        the bit to test
	 * @return the result of {@code ((n & (1 << bit)) != 0)}, or {@code n} as an
	 *         integer if {@code bit} is {@literal null} or {@literal null} if
	 *         {@code n} cannot be converted to an integer
	 * @since 1.1
	 */
	default boolean testBit(Number n, Number bit) {
		BigInteger integer = integer(n);
		return (integer != null && bit != null ? integer.testBit(bit.intValue()) : false);
	}

	/**
	 * Return a {@link BigDecimal} for a given value.
	 *
	 * @param value
	 *        the object to get as a {@link BigDecimal}
	 * @return the decimal instance, or {@literal null} if {@code value} is
	 *         {@literal null} or cannot be parsed as a decimal
	 */
	default BigDecimal decimal(Object value) {
		if ( value == null ) {
			return null;
		}
		Number n = null;
		if ( value instanceof Number ) {
			n = (Number) value;
		} else {
			n = StringUtils.numberValue(value.toString());
		}
		return NumberUtils.bigDecimalForNumber(n);
	}

	/**
	 * Return a {@link BigInteger} for a given value.
	 *
	 * @param value
	 *        the object to get as a {@link BigInteger}
	 * @return the integer instance, or {@literal null} if {@code value} is
	 *         {@literal null} or cannot be parsed as an integer
	 * @since 1.1
	 */
	default BigInteger integer(Object value) {
		if ( value == null ) {
			return null;
		}
		Number n = null;
		if ( value instanceof Number ) {
			n = (Number) value;
		} else {
			n = StringUtils.numberValue(value.toString());
		}
		return NumberUtils.bigIntegerForNumber(n);
	}

	/**
	 * Return the minimum between two number values.
	 *
	 * @param n1
	 *        the first number
	 * @param n2
	 *        the second number
	 * @return the minimum number, or {@literal null} if both arguments are
	 *         {@literal null}
	 */
	default Number min(Number n1, Number n2) {
		return NumberUtils.min(n1, n2);
	}

	/**
	 * Return the maximum between two number values.
	 *
	 * @param n1
	 *        the first number
	 * @param n2
	 *        the second number
	 * @return the maximum number, or {@literal null} if both arguments are
	 *         {@literal null}
	 */
	default Number max(Number n1, Number n2) {
		return NumberUtils.max(n1, n2);
	}

	/**
	 * Round positive numbers away from zero and negative numbers towards zero,
	 * to the nearest integer.
	 *
	 * @param n
	 *        the number to round
	 * @return the rounded number, or {@literal null} if {@code n} is
	 *         {@literal null}
	 */
	default Number ceil(Number n) {
		return NumberUtils.ceil(n, BigDecimal.ONE);
	}

	/**
	 * Round positive numbers away from zero and negative numbers towards zero,
	 * to the nearest integer multiple of a specific significance.
	 *
	 * @param n
	 *        the number to round
	 * @param significance
	 *        the multiple factor to round to
	 * @return the rounded number, or {@literal null} if {@code n} or
	 *         {@code significance} are {@literal null}
	 */
	default Number ceil(Number n, Number significance) {
		return NumberUtils.ceil(n, significance);
	}

	/**
	 * Round positive numbers towards zero and negative numbers away from zero,
	 * to the nearest integer multiple of a specific significance.
	 *
	 * @param n
	 *        the number to round
	 * @return the rounded number, or {@literal null} if {@code n} or
	 *         {@code significance} are {@literal null}
	 */
	default Number floor(Number n) {
		return NumberUtils.floor(n, BigDecimal.ONE);
	}

	/**
	 * Round positive numbers towards zero and negative numbers away from zero,
	 * to the nearest integer multiple of a specific significance.
	 *
	 * @param n
	 *        the number to round
	 * @param significance
	 *        the multiple factor to round to
	 * @return the rounded number, or {@literal null} if {@code n} or
	 *         {@code significance} are {@literal null}
	 */
	default Number floor(Number n, Number significance) {
		return NumberUtils.floor(n, significance);
	}

	/**
	 * Round a number towards zero to the nearest integer.
	 *
	 * <p>
	 * This method is a shortcut for calling {@code roundUp(n, 0)}.
	 * </p>
	 *
	 * @param n
	 *        the number to round
	 * @return the rounded number, or {@literal null} if {@code n} is
	 *         {@literal null}
	 * @see #roundUp(Number, Number)
	 */
	default Number up(Number n) {
		return NumberUtils.roundUp(n, 0);
	}

	/**
	 * Round a number towards zero to the nearest integer multiple of a specific
	 * significance.
	 *
	 * @param n
	 *        the number to round
	 * @param significance
	 *        the multiple factor to round to
	 * @return the rounded number, or {@literal null} if {@code n} or
	 *         {@code significance} are {@literal null}
	 */
	default Number up(Number n, Number significance) {
		return NumberUtils.up(n, significance);
	}

	/**
	 * Round a number towards zero to the nearest integer.
	 *
	 * <p>
	 * This method is a shortcut for calling {@code roundDown(n, 0)}.
	 * </p>
	 *
	 * @param n
	 *        the number to round
	 * @return the rounded number, or {@literal null} if {@code n} is
	 *         {@literal null}
	 * @see #roundDown(Number, Number)
	 */
	default Number down(Number n) {
		return NumberUtils.roundDown(n, 0);
	}

	/**
	 * Round a number towards zero to the nearest integer multiple of a specific
	 * significance.
	 *
	 * <p>
	 * This method rounds using the {@link RoundingMode#DOWN} mode.
	 * </p>
	 *
	 * @param n
	 *        the number to round
	 * @param significance
	 *        the multiple factor to round to
	 * @return the rounded number, or {@literal null} if {@code n} or
	 *         {@code significance} are {@literal null}
	 */
	default Number down(Number n, Number significance) {
		return NumberUtils.down(n, significance);
	}

	/**
	 * Round a number to the nearest integer multiple of a specific
	 * significance.
	 *
	 * @param n
	 *        the number to round
	 * @param significance
	 *        the multiple factor to round to
	 * @return the rounded number, or {@literal null} if {@code n} or
	 *         {@code significance} are {@literal null}
	 */
	default Number mround(Number n, Number significance) {
		return NumberUtils.mround(n, significance);
	}

	/**
	 * Round a number to the nearest integer.
	 *
	 * <p>
	 * This is a shortcut for calling {@code round(n, 0)}.
	 * </p>
	 *
	 * @param n
	 *        the number to round
	 * @return the rounded number, or {@literal null} if {@code n} is
	 *         {@literal null}
	 */
	default Number round(Number n) {
		return NumberUtils.round(n, 0);
	}

	/**
	 * Round a number to a maximum number of decimal digits using the
	 * {@link RoundingMode#HALF_UP} mode.
	 *
	 * @param n
	 *        the number to round
	 * @param digits
	 *        the maximum number of decimal digits
	 * @return the rounded number, or {@literal null} if {@code n} or
	 *         {@code digits} is {@literal null}
	 */
	default Number round(Number n, Number digits) {
		return NumberUtils.round(n, digits);
	}

	/**
	 * Round a number away from zero to a maximum number of decimal digits.
	 *
	 * @param n
	 *        the number to round
	 * @param digits
	 *        the maximum number of decimal digits
	 * @return the rounded number, or {@literal null} if {@code n} or
	 *         {@code digits} is {@literal null}
	 */
	default Number roundUp(Number n, Number digits) {
		return NumberUtils.roundUp(n, digits);
	}

	/**
	 * Round a number towards zero to a maximum number of decimal digits.
	 *
	 * @param n
	 *        the number to round
	 * @param digits
	 *        the maximum number of decimal digits
	 * @return the rounded number, or {@literal null} if {@code n} or
	 *         {@code digits} is {@literal null}
	 */
	default Number roundDown(Number n, Number digits) {
		return NumberUtils.roundDown(n, digits);
	}

	/**
	 * Narrow a number to the smallest possible number type that can exactly
	 * represent the given number.
	 *
	 * <p>
	 * If {@code n} cannot be narrowed then {@code n} is returned.
	 * </p>
	 *
	 * @param n
	 *        the number to narrow
	 * @param minBytePower
	 *        a minimum power-of-two byte size to narrow to; to; for example
	 *        {@literal 1} would narrow to at most a {@link Short}, {@literal 2}
	 *        to at most an {@link Integer} or {@link Float}, {@literal 3} to at
	 *        most a {@link Long} or {@link Double}
	 * @return the (possibly) narrowed number, or {@literal null} if {@code n}
	 *         is {@literal null}
	 * @since 1.1
	 */
	default Number narrow(Number n, Number minBytePower) {
		return NumberUtils.narrow(n, minBytePower.intValue());
	}

	/**
	 * Narrow a number to at minimum an 8-bit value that can exactly represent
	 * the given number.
	 *
	 * <p>
	 * If {@code n} cannot be narrowed then {@code n} is returned.
	 * </p>
	 *
	 * @param n
	 *        the number to narrow
	 * @return the (possibly) narrowed number, or {@literal null} if {@code n}
	 *         is {@literal null}
	 * @since 1.1
	 */
	default Number narrow8(Number n) {
		return NumberUtils.narrow(n, 0);
	}

	/**
	 * Narrow a number to at minimum a 16-bit value that can exactly represent
	 * the given number.
	 *
	 * <p>
	 * If {@code n} cannot be narrowed then {@code n} is returned.
	 * </p>
	 *
	 * @param n
	 *        the number to narrow
	 * @return the (possibly) narrowed number, or {@literal null} if {@code n}
	 *         is {@literal null}
	 * @since 1.1
	 */
	default Number narrow16(Number n) {
		return NumberUtils.narrow(n, 1);
	}

	/**
	 * Narrow a number to at minimum a 32-bit value that can exactly represent
	 * the given number.
	 *
	 * <p>
	 * If {@code n} cannot be narrowed then {@code n} is returned.
	 * </p>
	 *
	 * @param n
	 *        the number to narrow
	 * @return the (possibly) narrowed number, or {@literal null} if {@code n}
	 *         is {@literal null}
	 * @since 1.1
	 */
	default Number narrow32(Number n) {
		return NumberUtils.narrow(n, 2);
	}

	/**
	 * Narrow a number to at minimum a 64-bit value that can exactly represent
	 * the given number.
	 *
	 * <p>
	 * If {@code n} cannot be narrowed then {@code n} is returned.
	 * </p>
	 *
	 * @param n
	 *        the number to narrow
	 * @return the (possibly) narrowed number, or {@literal null} if {@code n}
	 *         is {@literal null}
	 * @since 1.1
	 */
	default Number narrow64(Number n) {
		return NumberUtils.narrow(n, 3);
	}

	/**
	 * Compute the sum a group of numbers.
	 *
	 * @param set
	 *        the numbers to sum; if {@literal null} or empty then
	 *        {@literal null} will be returned
	 * @return the sum of {@code set}
	 */
	default Number sum(Collection<? extends Number> set) {
		BigDecimal result = null;
		if ( set != null && !set.isEmpty() ) {
			result = BigDecimal.ZERO;
			for ( Number n : set ) {
				if ( n == null ) {
					continue;
				}
				result = result.add(NumberUtils.bigDecimalForNumber(n));
			}
		}
		return result;
	}

	/**
	 * Compute the average (mean) of a group of numbers.
	 *
	 * @param set
	 *        the numbers to average; if {@literal null} or empty then
	 *        {@literal null} will be returned
	 * @return the average of {@code set}
	 */
	default Number avg(Collection<? extends Number> set) {
		BigDecimal sum = (BigDecimal) sum(set);
		if ( sum == null ) {
			return null;
		}
		if ( set.size() == 1 ) {
			return sum;
		}
		int nonNullCount = 0;
		for ( Number n : set ) {
			if ( n != null ) {
				nonNullCount += 1;
			}
		}
		BigDecimal count = new BigDecimal(nonNullCount);
		try {
			return sum.divide(count);
		} catch ( ArithmeticException e ) {
			// try with rounding
			return sum.divide(count, 12, RoundingMode.HALF_UP);
		}
	}

	/**
	 * Find the maximum value in a group of numbers.
	 *
	 * @param set
	 *        the numbers to find the maximum in; if {@literal null} or empty
	 *        then {@literal null} will be returned
	 * @return the maximum of {@code set}
	 */
	default Number max(Collection<? extends Number> set) {
		if ( set == null ) {
			return null;
		}
		BigDecimal max = null;
		for ( Number n : set ) {
			BigDecimal d = decimal(n);
			if ( d == null ) {
				continue;
			}
			if ( max == null || d.compareTo(max) > 0 ) {
				max = d;
			}
		}
		return max;
	}

	/**
	 * Find the minimum value in a group of numbers.
	 *
	 * @param set
	 *        the numbers to find the minimum in; if {@literal null} or empty
	 *        then {@literal null} will be returned
	 * @return the minimum of {@code set}
	 */
	default Number min(Collection<? extends Number> set) {
		if ( set == null ) {
			return null;
		}
		BigDecimal min = null;
		for ( Number n : set ) {
			BigDecimal d = decimal(n);
			if ( d == null ) {
				continue;
			}
			if ( min == null || d.compareTo(min) < 0 ) {
				min = d;
			}
		}
		return min;
	}

	/**
	 * Perform linear interpolation of a value over an input range on an output
	 * range.
	 *
	 * <p>
	 * No error checking is performed on the input values.
	 * </p>
	 *
	 * @param x
	 *        the value to interpolate
	 * @param x1
	 *        the minimum range of the {@code x} value
	 * @param x2
	 *        the maximum range of the {@code x} value
	 * @param y1
	 *        the minimum output range to interpolate on
	 * @param y2
	 *        the maximum output range to interpolate on
	 * @return an interpolated value <em>y</em> in the range {@code y1} -
	 *         {@code y2}
	 * @since 1.2
	 */
	default Number interp(Number x, Number x1, Number x2, Number y1, Number y2) {
		return NumberUtils.linearInterpolate(x, x1, x2, y1, y2);
	}

	/**
	 * Perform linear interpolation of a value over an input range on an output
	 * range.
	 *
	 * <p>
	 * No error checking is performed on the input values.
	 * </p>
	 *
	 * @param x
	 *        the value to interpolate
	 * @param x1
	 *        the minimum range of the {@code x} value
	 * @param x2
	 *        the maximum range of the {@code x} value
	 * @param y1
	 *        the minimum output range to interpolate on
	 * @param y2
	 *        the maximum output range to interpolate on
	 * @param scale
	 *        the decimal scale
	 * @return an interpolated value <em>y</em> in the range {@code y1} -
	 *         {@code y2}
	 * @since 1.2
	 */
	default Number interp(Number x, Number x1, Number x2, Number y1, Number y2, int scale) {
		return NumberUtils.linearInterpolate(x, x1, x2, y1, y2, scale);
	}

	/**
	 * Returns Euler's number <i>e</i> raised to the power of {@code n}.
	 *
	 * @param n
	 *        the value to raise <i>e</i> to
	 * @return the calculated value, or {@literal null} if {@code n} is
	 *         {@literal null}
	 * @since 1.2
	 */
	default Number exp(Number n) {
		if ( n == null ) {
			return null;
		}
		return Math.exp(n.doubleValue());
	}

	/**
	 * Calculate the square root of a number.
	 *
	 * @param n
	 *        the number to calculate the square root of
	 * @return the square root of {@code n}, or {@literal null} if {@code n} is
	 *         {@literal null}
	 * @since 1.4
	 */
	default Number sqrt(Number n) {
		if ( n == null ) {
			return null;
		}
		// NOTE: when jump to Java 11 minimum can test for BigDecimal
		//       and then use BigDecimal.sqrt(MathContext.DECIMAL64)
		return Math.sqrt(n.doubleValue());
	}

	/**
	 * Calculate the cube root of a number.
	 *
	 * @param n
	 *        the number to calculate the cube root of
	 * @return the cube root of {@code n}, or {@literal null} if {@code n} is
	 *         {@literal null}
	 * @since 1.4
	 */
	default Number cbrt(Number n) {
		if ( n == null ) {
			return null;
		}
		return Math.cbrt(n.doubleValue());
	}

	/**
	 * Returns the value of {@code n} raised to the power of {@code p}.
	 *
	 * @param n
	 *        the number to raise
	 * @param e
	 *        the power to raise to
	 * @return {@code n} raised to the power of {@code p}, or {@literal null} if
	 *         any argument is {@literal null}
	 * @since 1.4
	 */
	default Number pow(Number n, Number e) {
		if ( n == null || e == null ) {
			return n;
		}
		if ( n instanceof Double || n instanceof Float ) {
			return Math.pow(n.doubleValue(), e.doubleValue());
		}
		BigDecimal d = NumberUtils.bigDecimalForNumber(n);
		return d.pow(e.intValue(), MathContext.DECIMAL64);
	}

	/**
	 * Scale a number by a power of 10.
	 *
	 * @param n
	 *        the number to scale
	 * @param scale
	 *        the power of 10 to scale by; a negative value shifts the decimal
	 *        point left this many places; a positive value shifts the decimal
	 *        point right this many places
	 * @return the scaled value
	 * @since 1.3
	 */
	default BigDecimal scaled(Number n, int scale) {
		return NumberUtils.scaled(n, scale);
	}

	/**
	 * Get the whole part of a number as a {@link BigInteger}.
	 *
	 * <p>
	 * The whole portion of the number is returned without any rounding from the
	 * fractional part of the number.
	 * </p>
	 *
	 * @param n
	 *        the number to extract the whole part from
	 * @return the whole part as an integer, or zero if {@code n} is
	 *         {@literal null}
	 * @since 1.3
	 */
	default BigInteger wholePart(Number n) {
		return NumberUtils.wholePartToInteger(decimal(n));
	}

	/**
	 * Get the fractional part of a number as a {@link BigInteger}.
	 *
	 * <p>
	 * The maximum scale of the number is preserved in the returned value.
	 * </p>
	 *
	 * @param n
	 *        the number to extract the fractional part from
	 * @return the fractional part as an integer, or zero if {@code n} is
	 *         {@literal null}
	 * @since 1.3
	 */
	default BigInteger fracPart(Number n) {
		return NumberUtils.fractionalPartToInteger(decimal(n));
	}

	/**
	 * Get the fractional part of a number as a {@link BigInteger} with a
	 * maximum scale.
	 *
	 * <p>
	 * If the fractional part must be rounded, the {@link RoundingMode#DOWN}
	 * method will be used to truncate the value to keep it within the desired
	 * scale.
	 * </p>
	 *
	 * @param n
	 *        the number to extract the fractional part from
	 * @param scale
	 *        the maximum power-of-10 scale
	 * @return the fractional part as an integer, or zero if {@code n} is
	 *         {@literal null}
	 * @since 1.3
	 */
	default BigInteger fracPart(Number n, int scale) {
		return NumberUtils.fractionalPartToInteger(decimal(n), scale);
	}

	/**
	 * Compute the root-mean-squared a group of numbers.
	 *
	 * @param set
	 *        the numbers to calculate the RMS for; if {@literal null} then
	 *        {@literal null} will be returned
	 * @return the RMS of {@code set}
	 */
	default Number rms(Collection<? extends Number> set) {
		if ( set == null || set.isEmpty() ) {
			return null;
		}
		List<Number> squares = set.stream().map(n -> pow(n, 2)).collect(Collectors.toList());
		Number sum = sum(squares);
		return sqrt(NumberUtils.bigDecimalForNumber(sum).divide(new BigDecimal(squares.size()),
				MathContext.DECIMAL64));
	}

}
