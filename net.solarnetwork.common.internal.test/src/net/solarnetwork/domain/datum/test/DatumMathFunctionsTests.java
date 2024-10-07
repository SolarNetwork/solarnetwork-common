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
 * @version 1.3
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
	public void rms() {
		List<Double> values = Arrays.asList(1.0, 2.0, 3.0);
		Number result = round(rms(values), 5);
		assertThat("RMS calcualted", result, is(equalTo(new BigDecimal("2.16025"))));
	}

}
