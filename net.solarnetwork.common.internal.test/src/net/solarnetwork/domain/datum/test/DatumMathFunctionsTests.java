/* ==================================================================
 * DatumMathFunctionsTests.java - 24/05/2022 3:17:52 pm
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

package net.solarnetwork.domain.datum.test;

import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import net.solarnetwork.domain.datum.DatumMathFunctions;

/**
 * Test cases for the {@link DatumMathFunctions} interface.
 *
 * @author matt
 * @version 1.5
 */
public class DatumMathFunctionsTests implements DatumMathFunctions {

	@Test
	public void integer_string() {
		assertThat("String integer parsed", integer("123"), is(equalTo(new BigInteger("123"))));
	}

	@Test
	public void integer_integer() {
		assertThat("Integer integer parsed", integer(123), is(equalTo(new BigInteger("123"))));
	}

	@Test
	public void integer_float() {
		assertThat("Float integer parsed", integer(123.0f), is(equalTo(new BigInteger("123"))));
	}

	@Test
	public void integer_double() {
		assertThat("Double integer parsed", integer(123.0), is(equalTo(new BigInteger("123"))));
	}

	@Test
	public void integer_decimal() {
		assertThat("Decimal integer parsed", integer(new BigDecimal("123.0000")),
				is(equalTo(new BigInteger("123"))));
	}

	@Test
	public void integer_nan() {
		assertThat("Decimal integer returns null", integer("Not a number"), is(nullValue()));
	}

	@Test
	public void integer_null() {
		assertThat("null integer returns null", integer(null), is(nullValue()));
	}

	@Test
	public void and() {
		assertThat("and computed", and(0b1010_1010, 0b1100_1100), is(BigInteger.valueOf(0b1000_1000)));
	}

	@Test
	public void and_null() {
		assertThat("null input returns null", and(null, 0b1100_1100), is(nullValue()));
		assertThat("null mask returns input", and(0b1010_1010, null),
				is(equalTo(BigInteger.valueOf(0b1010_1010))));
	}

	@Test
	public void or() {
		assertThat("or computed", or(0b1010_1010, 0b1100_1100), is(BigInteger.valueOf(0b1110_1110)));
	}

	@Test
	public void or_null() {
		assertThat("null input returns null", or(null, 0b1100_1100), is(nullValue()));
		assertThat("null mask returns input", or(0b1010_1010, null),
				is(equalTo(BigInteger.valueOf(0b1010_1010))));
	}

	@Test
	public void shiftRight() {
		assertThat("shift computed", shiftRight(0b1010_1010, 4), is(BigInteger.valueOf(0b1010)));
	}

	@Test
	public void shiftRight_null() {
		assertThat("null input returns null", shiftRight(null, 4), is(nullValue()));
		assertThat("null mask returns input", shiftRight(0b1010_1010, null),
				is(equalTo(BigInteger.valueOf(0b1010_1010))));
	}

	@Test
	public void shiftLeft() {
		assertThat("shift computed", shiftLeft(0b1010_1010, 4),
				is(BigInteger.valueOf(0b1010_1010_0000)));
	}

	@Test
	public void shiftLeft_null() {
		assertThat("null input returns null", shiftLeft(null, 4), is(nullValue()));
		assertThat("null mask returns input", shiftLeft(0b1010_1010, null),
				is(equalTo(BigInteger.valueOf(0b1010_1010))));
	}

	@Test
	public void narrow8() {
		assertThat("byte input returns byte", narrow8(integer("16")), is((byte) 16));
		assertThat("short input returns short", narrow8(integer("1024")), is((short) 1024));
	}

	@Test
	public void narrow16() {
		assertThat("byte input returns short", narrow16(integer("16")), is((short) 16));
		assertThat("short input returns short", narrow16(integer("1024")), is((short) 1024));
		assertThat("int input returns int", narrow16(integer("10241024")), is(10241024));
	}

	@Test
	public void narrow32() {
		assertThat("byte input returns int", narrow32(integer("16")), is(16));
		assertThat("short input returns int", narrow32(integer("1024")), is(1024));
		assertThat("int input returns int", narrow32(integer("10241024")), is(10241024));
		assertThat("long input returns long", narrow32(integer("1024102410241024")),
				is(1024102410241024L));
	}

	@Test
	public void narrow64() {
		assertThat("byte input returns int", narrow64(integer("16")), is(16L));
		assertThat("short input returns int", narrow64(integer("1024")), is(1024L));
		assertThat("int input returns int", narrow64(integer("10241024")), is(10241024L));
		assertThat("long input returns long", narrow64(integer("1024102410241024")),
				is(1024102410241024L));
		assertThat("integer input returns integer", narrow64(integer("1024102410241024102410241024")),
				is(new BigInteger("1024102410241024102410241024")));
	}

	@Test
	public void interp() {
		assertThat("interpolated", interp(20, 10, 50, 0, 4), is(new BigDecimal("1")));
		assertThat("interpolated", interp(11, 3, 17, 12, 47), is(new BigDecimal("32")));
	}

	@Test
	public void exp() {
		assertThat("exp calculated", exp(2), is(Math.exp(2)));
	}

	@Test
	public void scaled_negative() {
		BigDecimal s = scaled(1, -4);
		assertThat("Scaled negative", s, equalTo(new BigDecimal("0.0001")));
	}

	@Test
	public void scaled_positive() {
		BigDecimal s = scaled(1, 4);
		assertThat("Scaled positive", s, equalTo(new BigDecimal("10000")));
	}

	@Test
	public void wholePart() {
		BigInteger bi = wholePart(new BigDecimal("123.12345"));
		assertThat("Result", bi, equalTo(new BigInteger("123")));
	}

	@Test
	public void wholePart_negative() {
		BigInteger bi = wholePart(new BigDecimal("-123.12345"));
		assertThat("Result", bi, equalTo(new BigInteger("-123")));
	}

	@Test
	public void fracPart() {
		BigInteger bi = fracPart(new BigDecimal("123.12345"), 9);
		assertThat("Result", bi, equalTo(new BigInteger("12345")));
	}

	@Test
	public void fracPart_negative() {
		BigInteger bi = fracPart(new BigDecimal("-123.12345"), 9);
		assertThat("Result", bi, equalTo(new BigInteger("-12345")));
	}

	@Test
	public void sqrt_double() {
		Number result = sqrt(4.0);
		assertThat("Square root of double value calcualted", result, is(equalTo(2.0)));
	}

	@Test
	public void sqrt_float() {
		Number result = sqrt(4.0f);
		assertThat("Square root of float value calcualted", result, is(equalTo(2.0)));
	}

	@Test
	public void sqrt_decimal() {
		Number result = sqrt(new BigDecimal("4"));
		assertThat("Square root of decimal value calcualted", result, is(equalTo(2.0)));
	}

	@Test
	public void cbrt_double() {
		Number result = cbrt(27.0);
		assertThat("Cube root of double value calcualted", result, is(equalTo(3.0)));
	}

	@Test
	public void cbrt_float() {
		Number result = cbrt(27.0f);
		assertThat("Cube root of float value calcualted", result, is(equalTo(3.0)));
	}

	@Test
	public void cbrt_decimal() {
		Number result = cbrt(new BigDecimal("27"));
		assertThat("Cube root of decimal value calcualted", result, is(equalTo(3.0)));
	}

	@Test
	public void pow_double() {
		Number result = pow(2.0, 3.0);
		assertThat("Power of double value calcualted", result, is(equalTo(8.0)));
	}

	@Test
	public void pow_float() {
		Number result = pow(2f, 3f);
		assertThat("Power of float value calcualted", result, is(equalTo(8.0)));
	}

	@Test
	public void pow_decimal() {
		Number result = pow(new BigDecimal("2.0"), 3);
		assertThat("Power of decimal value calcualted", result, is(equalTo(new BigDecimal("8.000"))));
	}

	@Test
	public void pow_integer_fractionalExponent() {
		Number result = pow(4, 0.5);
		assertThat("Power of integer value with fractional exponent calculated", result,
				is(equalTo(2.0)));
	}

	@Test
	public void pow_decimal_fractionalExponent() {
		Number result = pow(new BigDecimal("2"), new BigDecimal("0.5"));
		assertThat("Power of decimal value with fractional exponent calculated", result,
				is(equalTo(Math.pow(2.0, 0.5))));
	}

	@Test
	public void pow_integer_integralDecimalExponent() {
		Number result = pow(2, 3.0);
		assertThat("Power of integer value with whole decimal exponent calculated as decimal", result,
				is(equalTo(new BigDecimal("8"))));
	}

	@Test
	public void pow_integer_hugeExponent() {
		Number result = pow(2, 1e10);
		assertThat("Power of integer value with exponent too large for decimal calculated", result,
				is(equalTo(Double.POSITIVE_INFINITY)));
	}

	@Test
	public void pow_nullExponent() {
		Number result = pow(4, null);
		assertThat("Power with null exponent is null", result, is(nullValue()));
	}

	@Test
	public void rms() {
		List<Double> values = Arrays.asList(1.0, 2.0, 3.0);
		Number result = round(rms(values), 5);
		assertThat("RMS calcualted", result, is(equalTo(new BigDecimal("2.16025"))));
	}

	@Test
	public void rms_null() {
		Number result = round(rms(null), 5);
		assertThat("RMS is null for null input", result, is(nullValue()));
	}

	@Test
	public void rms_empty() {
		Number result = round(rms(emptyList()), 5);
		assertThat("RMS is null for empty input", result, is(nullValue()));
	}

	@Test
	public void rms_nullElement() {
		List<Integer> values = Arrays.asList(3, 4, null);
		Number result = round(rms(values), 5);
		assertThat("RMS calculated ignoring null elements", result,
				is(equalTo(new BigDecimal("3.53553"))));
	}

	@Test
	public void rms_allNullElements() {
		List<Integer> values = Arrays.asList(null, null);
		Number result = rms(values);
		assertThat("RMS is null when all elements are null", result, is(nullValue()));
	}

	@Test
	public void rms_nan() {
		List<Number> values = Arrays.asList(3, Double.NaN, 4, Float.NaN);
		Number result = round(rms(values), 5);
		assertThat("RMS calculated ignoring NaN elements", result,
				is(equalTo(new BigDecimal("3.53553"))));
	}

	@Test
	public void rms_infinite() {
		List<Number> values = Arrays.asList(3, Double.NEGATIVE_INFINITY, 4);
		Number result = rms(values);
		assertThat("RMS is positive infinity when an element is infinite", result,
				is(equalTo(Double.POSITIVE_INFINITY)));
	}

	@Test
	public void sum() {
		List<Double> values = Arrays.asList(1.0, 2.0, 3.0);
		Number result = sum(values);
		assertThat("Sum calcualted", result, is(equalTo(new BigDecimal("6.0"))));
	}

	@Test
	public void sum_null() {
		Number result = sum(null);
		assertThat("Sum is null for null input", result, is(nullValue()));
	}

	@Test
	public void sum_empty() {
		Number result = sum(emptyList());
		assertThat("Sum is null for empty input", result, is(nullValue()));
	}

	@Test
	public void sum_nan() {
		List<Number> values = Arrays.asList(1.0, Double.NaN, 2.0, Float.NaN);
		Number result = sum(values);
		assertThat("Sum calculated ignoring NaN elements", result, is(equalTo(new BigDecimal("3.0"))));
	}

	@Test
	public void sum_infinite() {
		assertThat("Sum is positive infinity with a positive infinite element",
				sum(Arrays.asList(1.0, Double.POSITIVE_INFINITY, Double.NaN)),
				is(equalTo(Double.POSITIVE_INFINITY)));
		assertThat("Sum is negative infinity with a negative infinite element",
				sum(Arrays.asList(1.0, Float.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY)),
				is(equalTo(Double.NEGATIVE_INFINITY)));
	}

	@Test
	public void sum_oppositeInfinite() {
		List<Number> values = Arrays.asList(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, 5);
		Number result = sum(values);
		assertThat("Sum is NaN with both positive and negative infinite elements", result,
				is(equalTo(Double.NaN)));
	}

	@Test
	public void avg() {
		List<Double> values = Arrays.asList(1.0, 2.0, 3.0);
		Number result = avg(values);
		assertThat("Average calcualted", result, is(equalTo(new BigDecimal("2.0"))));
	}

	@Test
	public void avg_overflow() {
		List<Double> values = Arrays.asList(2.0, 2.0, 3.0);
		Number result = avg(values);
		assertThat("Average calcualted with rounding to 12 places", result,
				is(equalTo(new BigDecimal("2.333333333333"))));
	}

	@Test
	public void avg_null() {
		Number result = avg(null);
		assertThat("Average is null for null input", result, is(nullValue()));
	}

	@Test
	public void avg_empty() {
		Number result = avg(emptyList());
		assertThat("Average is null for empty input", result, is(nullValue()));
	}

	@Test
	public void avg_nullElement() {
		List<Double> values = Arrays.asList(1.0, null, 3.0);
		Number result = avg(values);
		assertThat("Average calculated ignoring null elements", result,
				is(equalTo(new BigDecimal("2.0"))));
	}

	@Test
	public void avg_singleNullElement() {
		List<Double> values = Arrays.asList((Double) null);
		Number result = avg(values);
		assertThat("Average is null when the only element is null", result, is(nullValue()));
	}

	@Test
	public void avg_allNullElements() {
		List<Double> values = Arrays.asList(null, null);
		Number result = avg(values);
		assertThat("Average is null when all elements are null", result, is(nullValue()));
	}

	@Test
	public void avg_nan() {
		List<Number> values = Arrays.asList(1.0, Double.NaN, 3.0);
		Number result = avg(values);
		assertThat("Average calculated ignoring NaN elements", result,
				is(equalTo(new BigDecimal("2.0"))));
	}

	@Test
	public void avg_onlyNan() {
		List<Number> values = Arrays.asList(Double.NaN, Float.NaN);
		Number result = avg(values);
		assertThat("Average is null when all elements are NaN", result, is(nullValue()));
	}

	@Test
	public void avg_infinite() {
		assertThat("Average is negative infinity with a negative infinite element",
				avg(Arrays.asList(1.0, Double.NEGATIVE_INFINITY, 3.0)),
				is(equalTo(Double.NEGATIVE_INFINITY)));
		assertThat("Average is NaN with both positive and negative infinite elements",
				avg(Arrays.asList(Double.POSITIVE_INFINITY, 1.0, Float.NEGATIVE_INFINITY)),
				is(equalTo(Double.NaN)));
	}

	@Test
	public void min() {
		List<Double> values = Arrays.asList(1.0, 2.0, 3.0);
		Number result = min(values);
		assertThat("Sum calcualted", result, is(equalTo(new BigDecimal("1.0"))));
	}

	@Test
	public void min_null() {
		Number result = min(null);
		assertThat("Minimum is null for null input", result, is(nullValue()));
	}

	@Test
	public void min_empty() {
		Number result = min(emptyList());
		assertThat("Minimum is null for empty input", result, is(nullValue()));
	}

	@Test
	public void min_nan() {
		List<Number> values = Arrays.asList(2.0, Double.NaN, 1.0);
		Number result = min(values);
		assertThat("Min found ignoring NaN elements", result, is(equalTo(new BigDecimal("1.0"))));
	}

	@Test
	public void min_infinite() {
		assertThat("Min is negative infinity with a negative infinite element",
				min(Arrays.asList(1.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)),
				is(equalTo(Double.NEGATIVE_INFINITY)));
		assertThat("Min ignores positive infinity when there are finite elements",
				min(Arrays.asList(Double.POSITIVE_INFINITY, 1.0)), is(equalTo(new BigDecimal("1.0"))));
		assertThat("Min is positive infinity when there are no finite elements",
				min(Arrays.asList(Float.POSITIVE_INFINITY, Double.NaN)),
				is(equalTo(Double.POSITIVE_INFINITY)));
	}

	@Test
	public void max() {
		List<Double> values = Arrays.asList(1.0, 2.0, 3.0);
		Number result = max(values);
		assertThat("Sum calcualted", result, is(equalTo(new BigDecimal("3.0"))));
	}

	@Test
	public void max_null() {
		Number result = max(null);
		assertThat("Maximum is null for null input", result, is(nullValue()));
	}

	@Test
	public void max_empty() {
		Number result = max(emptyList());
		assertThat("Maximum is null for empty input", result, is(nullValue()));
	}

	@Test
	public void max_nan() {
		List<Number> values = Arrays.asList(1.0, Float.NaN, 2.0);
		Number result = max(values);
		assertThat("Max found ignoring NaN elements", result, is(equalTo(new BigDecimal("2.0"))));
	}

	@Test
	public void max_infinite() {
		assertThat("Max is positive infinity with a positive infinite element",
				max(Arrays.asList(1.0, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)),
				is(equalTo(Double.POSITIVE_INFINITY)));
		assertThat("Max ignores negative infinity when there are finite elements",
				max(Arrays.asList(Double.NEGATIVE_INFINITY, 1.0)), is(equalTo(new BigDecimal("1.0"))));
		assertThat("Max is negative infinity when there are no finite elements",
				max(Arrays.asList(Float.NEGATIVE_INFINITY, Double.NaN)),
				is(equalTo(Double.NEGATIVE_INFINITY)));
	}

	@Test
	public void significanceRounding_nonFinite() {
		assertThat("Ceiling of NaN is NaN", ceil(Double.NaN), is(equalTo(Double.NaN)));
		assertThat("Floor of infinity is infinity", floor(Double.POSITIVE_INFINITY),
				is(equalTo(Double.POSITIVE_INFINITY)));
		assertThat("Ceiling of NaN to significance is NaN", ceil(Float.NaN, 0.5),
				is(equalTo(Float.NaN)));
		assertThat("Floor of infinity to significance is infinity", floor(Double.NEGATIVE_INFINITY, 0.5),
				is(equalTo(Double.NEGATIVE_INFINITY)));
		assertThat("Up of NaN to significance is NaN", up(Double.NaN, 0.5), is(equalTo(Double.NaN)));
		assertThat("Down of infinity to significance is infinity", down(Float.POSITIVE_INFINITY, 0.5),
				is(equalTo(Float.POSITIVE_INFINITY)));
		assertThat("Multiple round of NaN is NaN", mround(Double.NaN, 0.5), is(equalTo(Double.NaN)));
	}

}
