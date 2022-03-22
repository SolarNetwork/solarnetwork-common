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
import java.math.RoundingMode;
import java.util.Collection;
import net.solarnetwork.util.NumberUtils;
import net.solarnetwork.util.StringUtils;

/**
 * API for datum-related math helper functions.
 * 
 * @author matt
 * @version 1.0
 * @since 2.1
 */
public interface DatumMathFunctions {

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
	 * Compute the sum a group of numbers.
	 * 
	 * @param set
	 *        the numbers to sum; if {@literal null} then {@literal null} will
	 *        be returned
	 * @return the sum of {@code set}
	 */
	default Number sum(Collection<? extends Number> set) {
		BigDecimal result = null;
		if ( set != null ) {
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

}
