/* ==================================================================
 * NumberUtilsTests.java - 15/03/2018 2:54:58 PM
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

package net.solarnetwork.util.test;

import static org.assertj.core.api.BDDAssertions.then;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.BitSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.hamcrest.Matchers;
import org.junit.Test;
import net.solarnetwork.util.NumberUtils;

/**
 * Unit tests for the {@link NumberUtils} class.
 *
 * @author matt
 * @version 1.9
 */
public class NumberUtilsTests {

	@Test
	public void unsignedByte() {
		assertThat("Min value", NumberUtils.unsigned((byte) 0x00), equalTo((short) 0));
		assertThat("Mid value", NumberUtils.unsigned((byte) 0xAB), equalTo((short) 171));
		assertThat("Mid value", NumberUtils.unsigned((byte) -85), equalTo((short) 171));
		assertThat("Max value", NumberUtils.unsigned((byte) 0xFF), equalTo((short) 255));
	}

	@Test
	public void unsignedByteValues() {
		byte[] vals = new byte[] { (byte) 0x00, (byte) 0xAB, (byte) 0xFF };
		short[] act = NumberUtils.unsigned(vals);
		assertThat(act.length, equalTo(3));
		assertThat(act[0], equalTo((short) 0));
		assertThat(act[1], equalTo((short) 171));
		assertThat(act[2], equalTo((short) 255));
	}

	@Test
	public void unsignedValuesNullInput() {
		short[] act = NumberUtils.unsigned(null);
		assertThat(act, Matchers.nullValue());
	}

	@Test
	public void unsignedValuesEmptyInput() {
		short[] act = NumberUtils.unsigned(new byte[0]);
		assertThat(act.length, equalTo(0));
	}

	@Test
	public void unsignedNumberByte() {
		assertThat(NumberUtils.unsigned(Byte.valueOf((byte) 0xAB)),
				equalTo((Number) Short.valueOf((short) 171)));
	}

	@Test
	public void unsignedNumberShort() {
		assertThat(NumberUtils.unsignedNumber(Short.valueOf((short) 0xABCD)),
				equalTo((Number) Integer.valueOf(43981)));
	}

	@Test
	public void unsignedNumberInteger() {
		assertThat(NumberUtils.unsignedNumber(Integer.valueOf(0xFEDCABCD)),
				equalTo((Number) Long.valueOf(4275874765L)));
	}

	@Test
	public void unsignedNumberLong() {
		assertThat(NumberUtils.unsignedNumber(Long.valueOf(0xFEDCABCDFEDCABCDL)),
				equalTo((Number) new BigInteger("18364742281742560205")));
	}

	@Test
	public void bigDecimalForByte() {
		assertThat(NumberUtils.bigDecimalForNumber((byte) 12), equalTo(new BigDecimal("12")));
	}

	@Test
	public void bigDecimalForShort() {
		assertThat(NumberUtils.bigDecimalForNumber((short) 1234), equalTo(new BigDecimal("1234")));
	}

	@Test
	public void bigDecimalForInteger() {
		assertThat(NumberUtils.bigDecimalForNumber(123456), equalTo(new BigDecimal("123456")));
	}

	@Test
	public void bigDecimalForLong() {
		assertThat(NumberUtils.bigDecimalForNumber(123456789123L),
				equalTo(new BigDecimal("123456789123")));
	}

	@Test
	public void bigDecimalForFloat() {
		assertThat(NumberUtils.bigDecimalForNumber(123.123f), equalTo(new BigDecimal("123.123")));
	}

	@Test
	public void bigDecimalForDouble() {
		assertThat(NumberUtils.bigDecimalForNumber(123.123456), equalTo(new BigDecimal("123.123456")));
	}

	@Test
	public void calcualteCrcSimple() throws UnsupportedEncodingException {
		byte[] bytes = "123456789".getBytes("US-ASCII");
		int result = NumberUtils.crc16(bytes, 0, 9);
		assertThat("CRC", result, equalTo(0xBB3D));
	}

	@Test
	public void calcualteCrcOffsetPrefix() throws UnsupportedEncodingException {
		byte[] bytes = "0000123456789".getBytes("US-ASCII");
		int result = NumberUtils.crc16(bytes, 4, 9);
		assertThat("CRC", result, equalTo(0xBB3D));
	}

	@Test
	public void calcualteCrcOffsetMiddle() throws UnsupportedEncodingException {
		byte[] bytes = "00001234567890000".getBytes("US-ASCII");
		int result = NumberUtils.crc16(bytes, 4, 9);
		assertThat("CRC", result, equalTo(0xBB3D));
	}

	@Test
	public void calcualteCrcOffsetSuffix() throws UnsupportedEncodingException {
		byte[] bytes = "1234567890000".getBytes("US-ASCII");
		int result = NumberUtils.crc16(bytes, 0, 9);
		assertThat("CRC", result, equalTo(0xBB3D));
	}

	@Test
	public void calcualteCrcNotEnoughData() throws UnsupportedEncodingException {
		byte[] bytes = "123".getBytes("US-ASCII");
		int result = NumberUtils.crc16(bytes, 0, 9);
		assertThat("CRC", result, equalTo(0x0000));
	}

	@Test
	public void calculateCrcEmptyPacket() {
		byte[] bytes = new byte[] { 0x02, 0x05, 0x01, 0x00, 0x01, 0x01 };
		int result = NumberUtils.crc16(bytes, 1, bytes.length - 1);
		assertThat("CRC", result, equalTo(0xAC0D));
	}

	@Test
	public void fractionalPartFromNull() {
		BigInteger bi = NumberUtils.fractionalPartToInteger(null);
		assertThat("Result", bi, equalTo(BigInteger.ZERO));
	}

	@Test
	public void fractionalPartFromSmallDecimal() {
		BigInteger bi = NumberUtils.fractionalPartToInteger(new BigDecimal("123.12345"));
		assertThat("Result", bi, equalTo(new BigInteger("12345")));
	}

	@Test
	public void fractionalPartFromSmallDecimalNegative() {
		BigInteger bi = NumberUtils.fractionalPartToInteger(new BigDecimal("-123.12345"));
		assertThat("Result", bi, equalTo(new BigInteger("-12345")));
	}

	@Test
	public void fractionalPartFromScaledDecimal() {
		BigInteger bi = NumberUtils.fractionalPartToInteger(new BigDecimal("123.12345"), 9);
		assertThat("Result", bi, equalTo(new BigInteger("12345")));
	}

	@Test
	public void fractionalPartFromScaledDecimalTruncated() {
		BigInteger bi = NumberUtils.fractionalPartToInteger(new BigDecimal("123.123456789123456789"), 9);
		assertThat("Result", bi, equalTo(new BigInteger("123456789")));
	}

	@Test
	public void fractionalPartFromScaledDecimalTruncatedNegative() {
		BigInteger bi = NumberUtils.fractionalPartToInteger(new BigDecimal("-123.123456789123456789"),
				9);
		assertThat("Result", bi, equalTo(new BigInteger("-123456789")));
	}

	@Test
	public void fractionalPartScaledRounded() {
		BigInteger bi = NumberUtils.fractionalPartToInteger(new BigDecimal("3.99999999999999"), 9);
		assertThat("Result", bi, equalTo(new BigInteger(String.valueOf("999999999"))));
	}

	@Test
	public void fractionalPartScaledRoundedNegative() {
		BigInteger bi = NumberUtils.fractionalPartToInteger(new BigDecimal("-3.99999999999999"), 9);
		assertThat("Result", bi, equalTo(new BigInteger(String.valueOf("-999999999"))));
	}

	@Test
	public void offset_nullValue() {
		Number n = NumberUtils.offset(null, BigDecimal.ZERO);
		assertThat("Result", n, nullValue());
	}

	@Test
	public void offset_nullOffset() {
		BigDecimal v = new BigDecimal("1.23");
		Number n = NumberUtils.offset(v, null);
		assertThat("Result unchanged from null offset", n, sameInstance(v));
	}

	@Test
	public void offset_zeroOffset() {
		BigDecimal v = new BigDecimal("1.23");
		Number n = NumberUtils.offset(v, BigDecimal.ZERO);
		assertThat("Result unchanged from 0 offset", n, sameInstance(v));
	}

	@Test
	public void offset() {
		Number n = NumberUtils.offset(new BigDecimal("1.23"), new BigDecimal("2.0"));
		assertThat("Result offset", n, equalTo(new BigDecimal("3.23")));
	}

	@Test
	public void multiplied_nullValue() {
		Number n = NumberUtils.multiplied(null, BigDecimal.ZERO);
		assertThat("Result", n, nullValue());
	}

	@Test
	public void multiplied_nullMultiple() {
		BigDecimal v = new BigDecimal("1.23");
		Number n = NumberUtils.multiplied(v, null);
		assertThat("Result unchanged from null multiple", n, sameInstance(v));
	}

	@Test
	public void multiplied_zeroMultiple() {
		Number n = NumberUtils.multiplied(new BigDecimal("1.23"), BigDecimal.ZERO);
		assertThat("Result zero from 0 multiple", n, equalTo(new BigDecimal("0.00")));
	}

	@Test
	public void multiplied_oneMultiple() {
		BigDecimal v = new BigDecimal("1.23");
		Number n = NumberUtils.multiplied(v, BigDecimal.ONE);
		assertThat("Result unchanged from 1 multiple", n, sameInstance(v));
	}

	@Test
	public void multiplied() {
		Number n = NumberUtils.multiplied(new BigDecimal("1.23"), new BigDecimal("2"));
		assertThat("Result multiplied", n, equalTo(new BigDecimal("2.46")));
	}

	@Test
	public void maximumDecimalScale_nullValue() {
		Number n = NumberUtils.maximumDecimalScale(null, 0);
		assertThat("Result", n, nullValue());
	}

	@Test
	public void maximumDecimalScale_negativeScale() {
		BigDecimal v = new BigDecimal("1.23");
		Number n = NumberUtils.maximumDecimalScale(v, -1);
		assertThat("Result unchanged from negative maximumDecimalScale", n, sameInstance(v));
	}

	@Test
	public void maximumDecimalScale_zeroScale() {
		Number n = NumberUtils.maximumDecimalScale(new BigDecimal("1.23"), 0);
		assertThat("Result rounded to whole number", n, equalTo(new BigDecimal("1")));
	}

	@Test
	public void maximumDecimalScale_scaleRoundedUp() {
		Number n = NumberUtils.maximumDecimalScale(new BigDecimal("1.28"), 1);
		assertThat("Result rounded up", n, equalTo(new BigDecimal("1.3")));
	}

	@Test
	public void bitSetToInteger_empty() {
		BigInteger n = NumberUtils.bigIntegerForBitSet(new BitSet());
		assertThat("Empty BitSet converted", n, equalTo(BigInteger.ZERO));
	}

	@Test
	public void bitSetToInteger_null() {
		BigInteger n = NumberUtils.bigIntegerForBitSet(null);
		assertThat("Null BitSet converted", n, equalTo(BigInteger.ZERO));
	}

	@Test
	public void bitSetToInteger() {
		BitSet bs = new BitSet();
		bs.set(0);
		bs.set(17);
		BigInteger n = NumberUtils.bigIntegerForBitSet(bs);
		assertThat("BitSet converted", n, equalTo(new BigInteger("131073")));
	}

	@Test
	public void integerToBitSet_null() {
		BitSet bs = NumberUtils.bitSetForBigInteger(null);
		assertThat("Null integer converted", bs, equalTo(new BitSet()));
	}

	@Test
	public void integerToBitSet() {
		BitSet bs = NumberUtils.bitSetForBigInteger(new BigInteger("131073"));
		BitSet expected = new BitSet();
		expected.set(0);
		expected.set(17);
		assertThat("Integer converted", bs, equalTo(expected));
	}

	@Test
	public void scaled_negative() {
		BigDecimal s = NumberUtils.scaled(1, -4);
		assertThat("Scaled negative", s, equalTo(new BigDecimal("0.0001")));
	}

	@Test
	public void scaled_positive() {
		BigDecimal s = NumberUtils.scaled(1, 4);
		assertThat("Scaled positive", s, equalTo(new BigDecimal("10000")));
	}

	@Test
	public void atomicInt_getAndIncrementWithWrap_noWrap() {
		// GIVEN
		AtomicInteger n = new AtomicInteger(0);

		// WHEN
		int result1 = NumberUtils.getAndIncrementWithWrap(n, -1);
		int result2 = NumberUtils.getAndIncrementWithWrap(n, -1);

		// THEN
		assertThat("Got value without wrapping", result1, is(0));
		assertThat("Got value without wrapping", result2, is(1));
	}

	@Test
	public void atomicInt_getAndIncrementWithWrap_wrap() {
		// GIVEN
		AtomicInteger n = new AtomicInteger(Integer.MAX_VALUE);

		// WHEN
		int result1 = NumberUtils.getAndIncrementWithWrap(n, -1);
		int result2 = NumberUtils.getAndIncrementWithWrap(n, -1);

		// THEN
		assertThat("Got value without wrapping", result1, is(Integer.MAX_VALUE));
		assertThat("Got value with wrapping", result2, is(-1));
	}

	@Test
	public void atomicLong_getAndIncrementWithWrap_noWrap() {
		// GIVEN
		AtomicLong n = new AtomicLong(0L);

		// WHEN
		long result1 = NumberUtils.getAndIncrementWithWrap(n, -1L);
		long result2 = NumberUtils.getAndIncrementWithWrap(n, -1L);

		// THEN
		assertThat("Got value without wrapping", result1, is(0L));
		assertThat("Got value without wrapping", result2, is(1L));
	}

	@Test
	public void atomicLong_getAndIncrementWithWrap_wrap() {
		// GIVEN
		AtomicLong n = new AtomicLong(Long.MAX_VALUE);

		// WHEN
		long result1 = NumberUtils.getAndIncrementWithWrap(n, -1L);
		long result2 = NumberUtils.getAndIncrementWithWrap(n, -1L);

		// THEN
		assertThat("Got value without wrapping", result1, is(Long.MAX_VALUE));
		assertThat("Got value with wrapping", result2, is(-1L));
	}

	@Test
	public void testHumanReadableCount() {
		// GIVEN
		final Long[] inputs = new Long[] { 0L, 27L, 999L, 1000L, 1023L, 1024L, 1728L, 110592L, 7077888L,
				452984832L, 28991029248L, 1855425871872L, 9223372036854775807L };

		// WHEN
		final String[] result = Arrays.stream(inputs).map(n -> NumberUtils.humanReadableCount(n))
				.toArray(String[]::new);

		// THEN
		assertThat("Counts converted to formatted strings", result,
				arrayContaining("0 B", "27 B", "999 B", "1.0 kB", "1.0 kB", "1.0 kB", "1.7 kB",
						"110.6 kB", "7.1 MB", "453.0 MB", "29.0 GB", "1.9 TB", "9.2 EB"));
	}

	@Test
	public void min_short() {
		assertThat("Short input returns Integer", NumberUtils.min((short) 1, (short) 2), is(1));
		assertThat("Short input returns Integer", NumberUtils.min((short) 2, (short) 1), is(1));
	}

	@Test
	public void min_int() {
		assertThat("Int input returns Integer", NumberUtils.min(1, 2), is(1));
		assertThat("Int input returns Integer", NumberUtils.min(2, 1), is(1));
	}

	@Test
	public void min_long() {
		assertThat("Long input returns Long", NumberUtils.min(1L, 2L), is(1L));
		assertThat("Long input returns Long", NumberUtils.min(2L, 1L), is(1L));
	}

	@Test
	public void min_bigInteger() {
		assertThat("BigInteger input returns BigInteger",
				NumberUtils.min(new BigInteger("1"), new BigInteger("2")), is(new BigInteger("1")));
		assertThat("BigInteger input returns BigInteger",
				NumberUtils.min(new BigInteger("2"), new BigInteger("1")), is(new BigInteger("1")));
	}

	@Test
	public void min_bigDecimal() {
		assertThat("BigDecimal input returns BigDecimal",
				NumberUtils.min(new BigDecimal("1.1"), new BigDecimal("2.2")),
				is(new BigDecimal("1.1")));
		assertThat("BigDecimal input returns BigDecimal",
				NumberUtils.min(new BigDecimal("2.2"), new BigDecimal("1.1")),
				is(new BigDecimal("1.1")));
	}

	@Test
	public void min_mixed() {
		assertThat("Mixed input returns BigDecimal", NumberUtils.min(1, new BigDecimal("2.2")),
				is(new BigDecimal("1")));
		assertThat("Mixed input returns BigDecimal", NumberUtils.min(new BigDecimal("2.2"), 1),
				is(new BigDecimal("1")));
	}

	@Test
	public void max_short() {
		assertThat("Short input returns Integer", NumberUtils.max((short) 1, (short) 2), is(2));
		assertThat("Short input returns Integer", NumberUtils.max((short) 2, (short) 1), is(2));
	}

	@Test
	public void max_int() {
		assertThat("Int input returns Integer", NumberUtils.max(1, 2), is(2));
		assertThat("Int input returns Integer", NumberUtils.max(2, 1), is(2));
	}

	@Test
	public void max_long() {
		assertThat("Long input returns Long", NumberUtils.max(1L, 2L), is(2L));
		assertThat("Long input returns Long", NumberUtils.max(2L, 1L), is(2L));
	}

	@Test
	public void max_bigInteger() {
		assertThat("BigInteger input returns BigInteger",
				NumberUtils.max(new BigInteger("1"), new BigInteger("2")), is(new BigInteger("2")));
		assertThat("BigInteger input returns BigInteger",
				NumberUtils.max(new BigInteger("2"), new BigInteger("1")), is(new BigInteger("2")));
	}

	@Test
	public void max_bigDecimal() {
		assertThat("BigDecimal input returns BigDecimal",
				NumberUtils.max(new BigDecimal("1.1"), new BigDecimal("2.2")),
				is(new BigDecimal("2.2")));
		assertThat("BigDecimal input returns BigDecimal",
				NumberUtils.max(new BigDecimal("2.2"), new BigDecimal("1.1")),
				is(new BigDecimal("2.2")));
	}

	@Test
	public void max_mixed() {
		assertThat("Mixed input returns BigDecimal", NumberUtils.max(1, new BigDecimal("2.2")),
				is(new BigDecimal("2.2")));
		assertThat("Mixed input returns BigDecimal", NumberUtils.max(new BigDecimal("2.2"), 1),
				is(new BigDecimal("2.2")));
	}

	@Test
	public void floor_1() {
		assertThat("positive exact", NumberUtils.floor(1.0, 1), is(new BigDecimal("1")));
		assertThat("positive left", NumberUtils.floor(1.2, 1), is(new BigDecimal("1")));
		assertThat("positive even", NumberUtils.floor(1.5, 1), is(new BigDecimal("1")));
		assertThat("positive right", NumberUtils.floor(1.7, 1), is(new BigDecimal("1")));

		assertThat("negative exact", NumberUtils.floor(-1.0, 1), is(new BigDecimal("-1")));
		assertThat("negative left", NumberUtils.floor(-1.2, 1), is(new BigDecimal("-2")));
		assertThat("negative even", NumberUtils.floor(-1.5, 1), is(new BigDecimal("-2")));
		assertThat("negative right", NumberUtils.floor(-1.7, 1), is(new BigDecimal("-2")));
	}

	@Test
	public void ceil_1() {
		assertThat("positive exact", NumberUtils.ceil(1.0, 1), is(new BigDecimal("1")));
		assertThat("positive left", NumberUtils.ceil(1.2, 1), is(new BigDecimal("2")));
		assertThat("positive even", NumberUtils.ceil(1.5, 1), is(new BigDecimal("2")));
		assertThat("positive right", NumberUtils.ceil(1.7, 1), is(new BigDecimal("2")));

		assertThat("negative exact", NumberUtils.ceil(-1.0, 1), is(new BigDecimal("-1")));
		assertThat("negative left", NumberUtils.ceil(-1.2, 1), is(new BigDecimal("-1")));
		assertThat("negative even", NumberUtils.ceil(-1.5, 1), is(new BigDecimal("-1")));
		assertThat("negative right", NumberUtils.ceil(-1.7, 1), is(new BigDecimal("-1")));
	}

	@Test
	public void down_1() {
		assertThat("positive exact", NumberUtils.down(1.0, 1), is(new BigDecimal("1")));
		assertThat("positive left", NumberUtils.down(1.2, 1), is(new BigDecimal("1")));
		assertThat("positive even", NumberUtils.down(1.5, 1), is(new BigDecimal("1")));
		assertThat("positive right", NumberUtils.down(1.7, 1), is(new BigDecimal("1")));

		assertThat("negative exact", NumberUtils.down(-1.0, 1), is(new BigDecimal("-1")));
		assertThat("negative left", NumberUtils.down(-1.2, 1), is(new BigDecimal("-1")));
		assertThat("negative even", NumberUtils.down(-1.5, 1), is(new BigDecimal("-1")));
		assertThat("negative right", NumberUtils.down(-1.7, 1), is(new BigDecimal("-1")));
	}

	@Test
	public void up_1() {
		assertThat("positive exact", NumberUtils.up(1.0, 1), is(new BigDecimal("1")));
		assertThat("positive left", NumberUtils.up(1.2, 1), is(new BigDecimal("2")));
		assertThat("positive even", NumberUtils.up(1.5, 1), is(new BigDecimal("2")));
		assertThat("positive right", NumberUtils.up(1.7, 1), is(new BigDecimal("2")));

		assertThat("negative exact", NumberUtils.up(-1.0, 1), is(new BigDecimal("-1")));
		assertThat("negative left", NumberUtils.up(-1.2, 1), is(new BigDecimal("-2")));
		assertThat("negative even", NumberUtils.up(-1.5, 1), is(new BigDecimal("-2")));
		assertThat("negative right", NumberUtils.up(-1.7, 1), is(new BigDecimal("-2")));
	}

	@Test
	public void mround_1() {
		assertThat("positive exact", NumberUtils.mround(1.0, 1), is(new BigDecimal("1")));
		assertThat("positive left", NumberUtils.mround(1.2, 1), is(new BigDecimal("1")));
		assertThat("positive even", NumberUtils.mround(1.5, 1), is(new BigDecimal("2")));
		assertThat("positive right", NumberUtils.mround(1.7, 1), is(new BigDecimal("2")));

		assertThat("negative exact", NumberUtils.mround(-1.0, 1), is(new BigDecimal("-1")));
		assertThat("negative left", NumberUtils.mround(-1.2, 1), is(new BigDecimal("-1")));
		assertThat("negative even", NumberUtils.mround(-1.5, 1), is(new BigDecimal("-2")));
		assertThat("negative right", NumberUtils.mround(-1.7, 1), is(new BigDecimal("-2")));
	}

	@Test
	public void floor_3() {
		assertThat("positive zero", NumberUtils.floor(0.0, 3), is(new BigDecimal("0")));
		assertThat("positive left", NumberUtils.floor(1.2, 3), is(new BigDecimal("0")));
		assertThat("positive even", NumberUtils.floor(1.5, 3), is(new BigDecimal("0")));
		assertThat("positive right", NumberUtils.floor(2.7, 3), is(new BigDecimal("0")));
		assertThat("positive exact", NumberUtils.floor(3.0, 3), is(new BigDecimal("3")));

		assertThat("negative zero", NumberUtils.floor(-0.0, 3), is(new BigDecimal("-0")));
		assertThat("negative left", NumberUtils.floor(-1.2, 3), is(new BigDecimal("-3")));
		assertThat("negative even", NumberUtils.floor(-1.5, 3), is(new BigDecimal("-3")));
		assertThat("negative right", NumberUtils.floor(-2.7, 3), is(new BigDecimal("-3")));
		assertThat("negative exact", NumberUtils.floor(-3.0, 3), is(new BigDecimal("-3")));
	}

	@Test
	public void ceil_3() {
		assertThat("positive zero", NumberUtils.ceil(0.0, 3), is(new BigDecimal("0")));
		assertThat("positive left", NumberUtils.ceil(1.2, 3), is(new BigDecimal("3")));
		assertThat("positive even", NumberUtils.ceil(1.5, 3), is(new BigDecimal("3")));
		assertThat("positive right", NumberUtils.ceil(2.7, 3), is(new BigDecimal("3")));
		assertThat("positive exact", NumberUtils.ceil(3.0, 3), is(new BigDecimal("3")));

		assertThat("negative zero", NumberUtils.ceil(-0.0, 3), is(new BigDecimal("-0")));
		assertThat("negative left", NumberUtils.ceil(-1.2, 3), is(new BigDecimal("-0")));
		assertThat("negative even", NumberUtils.ceil(-1.5, 3), is(new BigDecimal("-0")));
		assertThat("negative right", NumberUtils.ceil(-2.7, 3), is(new BigDecimal("-0")));
		assertThat("negative exact", NumberUtils.ceil(-3.0, 3), is(new BigDecimal("-3")));
	}

	@Test
	public void down_3() {
		assertThat("positive zero", NumberUtils.down(0.0, 3), is(new BigDecimal("0")));
		assertThat("positive left", NumberUtils.down(1.2, 3), is(new BigDecimal("0")));
		assertThat("positive even", NumberUtils.down(1.5, 3), is(new BigDecimal("0")));
		assertThat("positive right", NumberUtils.down(2.7, 3), is(new BigDecimal("0")));
		assertThat("positive exact", NumberUtils.down(3.0, 3), is(new BigDecimal("3")));

		assertThat("negative zero", NumberUtils.down(-0.0, 3), is(new BigDecimal("-0")));
		assertThat("negative left", NumberUtils.down(-1.2, 3), is(new BigDecimal("-0")));
		assertThat("negative even", NumberUtils.down(-1.5, 3), is(new BigDecimal("-0")));
		assertThat("negative right", NumberUtils.down(-2.7, 3), is(new BigDecimal("-0")));
		assertThat("negative exact", NumberUtils.down(-3.0, 3), is(new BigDecimal("-3")));
	}

	@Test
	public void up_3() {
		assertThat("positive zero", NumberUtils.up(0.0, 3), is(new BigDecimal("0")));
		assertThat("positive left", NumberUtils.up(1.2, 3), is(new BigDecimal("3")));
		assertThat("positive even", NumberUtils.up(1.5, 3), is(new BigDecimal("3")));
		assertThat("positive right", NumberUtils.up(2.7, 3), is(new BigDecimal("3")));
		assertThat("positive exact", NumberUtils.up(3.0, 3), is(new BigDecimal("3")));

		assertThat("negative zero", NumberUtils.up(-0.0, 3), is(new BigDecimal("-0")));
		assertThat("negative left", NumberUtils.up(-1.2, 3), is(new BigDecimal("-3")));
		assertThat("negative even", NumberUtils.up(-1.5, 3), is(new BigDecimal("-3")));
		assertThat("negative right", NumberUtils.up(-2.7, 3), is(new BigDecimal("-3")));
		assertThat("negative exact", NumberUtils.up(-3.0, 3), is(new BigDecimal("-3")));
	}

	@Test
	public void mround_3() {
		assertThat("positive zero", NumberUtils.mround(0.0, 3), is(new BigDecimal("0")));
		assertThat("positive left", NumberUtils.mround(1.2, 3), is(new BigDecimal("0")));
		assertThat("positive even", NumberUtils.mround(1.5, 3), is(new BigDecimal("3")));
		assertThat("positive right", NumberUtils.mround(2.7, 3), is(new BigDecimal("3")));
		assertThat("positive exact", NumberUtils.mround(3.0, 3), is(new BigDecimal("3")));

		assertThat("negative zero", NumberUtils.mround(-0.0, 3), is(new BigDecimal("-0")));
		assertThat("negative left", NumberUtils.mround(-1.2, 3), is(new BigDecimal("-0")));
		assertThat("negative even", NumberUtils.mround(-1.5, 3), is(new BigDecimal("-3")));
		assertThat("negative right", NumberUtils.mround(-2.7, 3), is(new BigDecimal("-3")));
		assertThat("negative exact", NumberUtils.mround(-3.0, 3), is(new BigDecimal("-3")));
	}

	@Test
	public void mround_singleRounding() {
		// @formatter:off
		then(NumberUtils.mround(2.99, 2))
			.as("2.99 / 2 = 1.495 rounds to 1, not up to 1.50 then 2")
			.isEqualTo(new BigDecimal("2"))
			;
		then(NumberUtils.mround(new BigDecimal("2.4999"), 5))
			.as("2.4999 / 5 = 0.49998 rounds to 0, not up to 0.5000 then 1")
			.isEqualTo(new BigDecimal("0"))
			;
		then(NumberUtils.mround(new BigDecimal("-2.4999"), 5))
			.as("-2.4999 / 5 = -0.49998 rounds to 0, not down to -0.5000 then -1")
			.isEqualTo(new BigDecimal("0"))
			;
		// @formatter:on
	}

	@Test
	public void mround_negativeScale() {
		// GIVEN
		// BigDecimal.valueOf() gives "round" double values from 1E7 a negative scale
		final Double reading = 15000000.0;

		// @formatter:off
		then(NumberUtils.mround(reading, 1000))
			.as("Value that is already a multiple is unchanged")
			.isEqualTo(new BigDecimal("15000000"))
			;
		then(NumberUtils.mround(1.0E7, 3))
			.as("Rounded to nearest multiple")
			.isEqualTo(new BigDecimal("9999999"))
			;
		then(NumberUtils.floor(new BigDecimal("1E+3"), 300))
			.as("Floor to multiple")
			.isEqualTo(new BigDecimal("900"))
			;
		then(NumberUtils.ceil(new BigDecimal("1E+3"), 300))
			.as("Ceiling to multiple")
			.isEqualTo(new BigDecimal("1200"))
			;
		then(NumberUtils.down(new BigDecimal("-1E+3"), 300))
			.as("Rounded towards zero to multiple")
			.isEqualTo(new BigDecimal("-900"))
			;
		then(NumberUtils.up(new BigDecimal("-1E+3"), 300))
			.as("Rounded away from zero to multiple")
			.isEqualTo(new BigDecimal("-1200"))
			;
		// @formatter:on
	}

	@Test
	public void mround_zeroSignificance() {
		// @formatter:off
		then(NumberUtils.mround(12.34, 0))
			.as("Zero significance rounds to zero, like spreadsheet MROUND")
			.isEqualTo(BigDecimal.ZERO)
			;
		then(NumberUtils.mround(-12.34, 0.0))
			.as("Zero significance with a decimal scale rounds to zero")
			.isEqualTo(BigDecimal.ZERO)
			;
		then(NumberUtils.floor(12.34, 0))
			.as("Floor with zero significance rounds to zero")
			.isEqualTo(BigDecimal.ZERO)
			;
		then(NumberUtils.ceil(-12.34, 0))
			.as("Ceiling with zero significance rounds to zero")
			.isEqualTo(BigDecimal.ZERO)
			;
		// @formatter:on
	}

	@Test
	public void round_0() {
		assertThat("positive zero", NumberUtils.round(new BigDecimal("0.0"), 0),
				is(new BigDecimal("0")));
		assertThat("positive left", NumberUtils.round(new BigDecimal("1.2"), 0),
				is(new BigDecimal("1")));
		assertThat("positive even", NumberUtils.round(new BigDecimal("1.5"), 0),
				is(new BigDecimal("2")));
		assertThat("positive right", NumberUtils.round(new BigDecimal("2.7"), 0),
				is(new BigDecimal("3")));
		assertThat("positive exact", NumberUtils.round(new BigDecimal("3.0"), 0),
				is(new BigDecimal("3")));

		assertThat("negative zero", NumberUtils.round(new BigDecimal("-0.0"), 0),
				is(new BigDecimal("-0")));
		assertThat("negative left", NumberUtils.round(new BigDecimal("-1.2"), 0),
				is(new BigDecimal("-1")));
		assertThat("negative even", NumberUtils.round(new BigDecimal("-1.5"), 0),
				is(new BigDecimal("-2")));
		assertThat("negative right", NumberUtils.round(new BigDecimal("-2.7"), 0),
				is(new BigDecimal("-3")));
		assertThat("negative exact", NumberUtils.round(new BigDecimal("-3.0"), 0),
				is(new BigDecimal("-3")));
	}

	@Test
	public void round_3() {
		assertThat("positive zero", NumberUtils.round(new BigDecimal("0.0"), 3),
				is(new BigDecimal("0.0")));
		assertThat("positive left", NumberUtils.round(new BigDecimal("1.1232"), 3),
				is(new BigDecimal("1.123")));
		assertThat("positive even", NumberUtils.round(new BigDecimal("1.1235"), 3),
				is(new BigDecimal("1.124")));
		assertThat("positive right", NumberUtils.round(new BigDecimal("1.1237"), 3),
				is(new BigDecimal("1.124")));
		assertThat("positive exact", NumberUtils.round(new BigDecimal("1.1230"), 3),
				is(new BigDecimal("1.123")));

		assertThat("negative zero", NumberUtils.round(new BigDecimal("-0.0"), 3),
				is(new BigDecimal("-0.0")));
		assertThat("negative left", NumberUtils.round(new BigDecimal("-1.1232"), 3),
				is(new BigDecimal("-1.123")));
		assertThat("negative even", NumberUtils.round(new BigDecimal("-1.1235"), 3),
				is(new BigDecimal("-1.124")));
		assertThat("negative right", NumberUtils.round(new BigDecimal("-1.1237"), 3),
				is(new BigDecimal("-1.124")));
		assertThat("negative exact", NumberUtils.round(new BigDecimal("-1.1230"), 3),
				is(new BigDecimal("-1.123")));
	}

	@Test
	public void roundup_0() {
		assertThat("positive zero", NumberUtils.roundUp(new BigDecimal("0.0"), 0),
				is(new BigDecimal("0")));
		assertThat("positive left", NumberUtils.roundUp(new BigDecimal("1.2"), 0),
				is(new BigDecimal("2")));
		assertThat("positive even", NumberUtils.roundUp(new BigDecimal("1.5"), 0),
				is(new BigDecimal("2")));
		assertThat("positive right", NumberUtils.roundUp(new BigDecimal("2.7"), 0),
				is(new BigDecimal("3")));
		assertThat("positive exact", NumberUtils.roundUp(new BigDecimal("3.0"), 0),
				is(new BigDecimal("3")));

		assertThat("negative zero", NumberUtils.roundUp(new BigDecimal("-0.0"), 0),
				is(new BigDecimal("-0")));
		assertThat("negative left", NumberUtils.roundUp(new BigDecimal("-1.2"), 0),
				is(new BigDecimal("-2")));
		assertThat("negative even", NumberUtils.roundUp(new BigDecimal("-1.5"), 0),
				is(new BigDecimal("-2")));
		assertThat("negative right", NumberUtils.roundUp(new BigDecimal("-2.7"), 0),
				is(new BigDecimal("-3")));
		assertThat("negative exact", NumberUtils.roundUp(new BigDecimal("-3.0"), 0),
				is(new BigDecimal("-3")));
	}

	@Test
	public void roundup_3() {
		assertThat("positive zero", NumberUtils.roundUp(new BigDecimal("0.0"), 3),
				is(new BigDecimal("0.0")));
		assertThat("positive left", NumberUtils.roundUp(new BigDecimal("1.1232"), 3),
				is(new BigDecimal("1.124")));
		assertThat("positive even", NumberUtils.roundUp(new BigDecimal("1.1235"), 3),
				is(new BigDecimal("1.124")));
		assertThat("positive right", NumberUtils.roundUp(new BigDecimal("1.1237"), 3),
				is(new BigDecimal("1.124")));
		assertThat("positive exact", NumberUtils.roundUp(new BigDecimal("1.1230"), 3),
				is(new BigDecimal("1.123")));

		assertThat("negative zero", NumberUtils.roundUp(new BigDecimal("-0.0"), 3),
				is(new BigDecimal("-0.0")));
		assertThat("negative left", NumberUtils.roundUp(new BigDecimal("-1.1232"), 3),
				is(new BigDecimal("-1.124")));
		assertThat("negative even", NumberUtils.roundUp(new BigDecimal("-1.1235"), 3),
				is(new BigDecimal("-1.124")));
		assertThat("negative right", NumberUtils.roundUp(new BigDecimal("-1.1237"), 3),
				is(new BigDecimal("-1.124")));
		assertThat("negative exact", NumberUtils.roundUp(new BigDecimal("-1.1230"), 3),
				is(new BigDecimal("-1.123")));
	}

	@Test
	public void rounddown_0() {
		assertThat("positive zero", NumberUtils.roundDown(new BigDecimal("0.0"), 0),
				is(new BigDecimal("0")));
		assertThat("positive left", NumberUtils.roundDown(new BigDecimal("1.2"), 0),
				is(new BigDecimal("1")));
		assertThat("positive even", NumberUtils.roundDown(new BigDecimal("1.5"), 0),
				is(new BigDecimal("1")));
		assertThat("positive right", NumberUtils.roundDown(new BigDecimal("2.7"), 0),
				is(new BigDecimal("2")));
		assertThat("positive exact", NumberUtils.roundDown(new BigDecimal("3.0"), 0),
				is(new BigDecimal("3")));

		assertThat("negative zero", NumberUtils.roundDown(new BigDecimal("-0.0"), 0),
				is(new BigDecimal("-0")));
		assertThat("negative left", NumberUtils.roundDown(new BigDecimal("-1.2"), 0),
				is(new BigDecimal("-1")));
		assertThat("negative even", NumberUtils.roundDown(new BigDecimal("-1.5"), 0),
				is(new BigDecimal("-1")));
		assertThat("negative right", NumberUtils.roundDown(new BigDecimal("-2.7"), 0),
				is(new BigDecimal("-2")));
		assertThat("negative exact", NumberUtils.roundDown(new BigDecimal("-3.0"), 0),
				is(new BigDecimal("-3")));
	}

	@Test
	public void rounddown_3() {
		assertThat("positive zero", NumberUtils.roundDown(new BigDecimal("0.0"), 3),
				is(new BigDecimal("0.0")));
		assertThat("positive left", NumberUtils.roundDown(new BigDecimal("1.1232"), 3),
				is(new BigDecimal("1.123")));
		assertThat("positive even", NumberUtils.roundDown(new BigDecimal("1.1235"), 3),
				is(new BigDecimal("1.123")));
		assertThat("positive right", NumberUtils.roundDown(new BigDecimal("1.1237"), 3),
				is(new BigDecimal("1.123")));
		assertThat("positive exact", NumberUtils.roundDown(new BigDecimal("1.1230"), 3),
				is(new BigDecimal("1.123")));

		assertThat("negative zero", NumberUtils.roundDown(new BigDecimal("-0.0"), 3),
				is(new BigDecimal("-0.0")));
		assertThat("negative left", NumberUtils.roundDown(new BigDecimal("-1.1232"), 3),
				is(new BigDecimal("-1.123")));
		assertThat("negative even", NumberUtils.roundDown(new BigDecimal("-1.1235"), 3),
				is(new BigDecimal("-1.123")));
		assertThat("negative right", NumberUtils.roundDown(new BigDecimal("-1.1237"), 3),
				is(new BigDecimal("-1.123")));
		assertThat("negative exact", NumberUtils.roundDown(new BigDecimal("-1.1230"), 3),
				is(new BigDecimal("-1.123")));
	}

	@Test
	public void narrowBigInteger() {
		assertThat("narrowed to byte", NumberUtils.narrow(new BigInteger("7"), 0), is((byte) 7));
		assertThat("narrowed to short", NumberUtils.narrow(new BigInteger("723"), 0), is((short) 723));
		assertThat("narrowed to int", NumberUtils.narrow(new BigInteger("72356789"), 0), is(72356789));
		assertThat("narrowed to long", NumberUtils.narrow(new BigInteger("7235678972356789"), 0),
				is(7235678972356789L));
		BigInteger n = new BigInteger("72356789723567897235678972356789");
		assertThat("too big to narrow", NumberUtils.narrow(n, 0), is(sameInstance(n)));
	}

	@Test
	public void narrowBigDecimal() {
		assertThat("narrowed to byte", NumberUtils.narrow(new BigDecimal("7"), 0), is((byte) 7));
		assertThat("narrowed to short", NumberUtils.narrow(new BigDecimal("723"), 0), is((short) 723));
		assertThat("narrowed to int", NumberUtils.narrow(new BigDecimal("72356789"), 0), is(72356789));
		assertThat("narrowed to float", NumberUtils.narrow(BigDecimal.valueOf(123.00005), 0),
				is((float) 123.00005));
		assertThat("narrowed to long", NumberUtils.narrow(new BigDecimal("7235678972356789"), 0),
				is(7235678972356789L));
		assertThat("narrowed to double", NumberUtils.narrow(BigDecimal.valueOf(723567897.2356789), 0),
				is(723567897.2356789));
		BigDecimal n = new BigDecimal("72356789723567897235678972356789");
		assertThat("too big to narrow", NumberUtils.narrow(n, 0), is(sameInstance(n)));
	}

	@Test
	public void narrowShort() {
		assertThat("narrowed to byte", NumberUtils.narrow((short) 11, 0), is((byte) 11));

		Short n = (short) 11;
		assertThat("min power disallows narrowing", NumberUtils.narrow(n, 1), is(sameInstance(n)));

		n = (short) 12345;
		assertThat("too big to narrow", NumberUtils.narrow(n, 0), is(sameInstance(n)));
	}

	@Test
	public void narrowInteger() {
		assertThat("narrowed to byte", NumberUtils.narrow(11, 0), is((byte) 11));
		assertThat("narrowed to short", NumberUtils.narrow(12345, 0), is((short) 12345));

		Integer n = 11;
		assertThat("min power constrains narrowing to short", NumberUtils.narrow(n, 1), is((short) 11));
		assertThat("min power disallows narrowing", NumberUtils.narrow(n, 2), is(sameInstance(n)));

		n = 123456789;
		assertThat("too big to narrow", NumberUtils.narrow(n, 0), is(sameInstance(n)));
	}

	@Test
	public void narrowLong() {
		assertThat("narrowed to byte", NumberUtils.narrow(11L, 0), is((byte) 11));
		assertThat("narrowed to short", NumberUtils.narrow(12345L, 0), is((short) 12345));
		assertThat("narrowed to int", NumberUtils.narrow(123456789L, 0), is(123456789));

		Long n = 11L;
		assertThat("min power constrains narrowing to short", NumberUtils.narrow(n, 1), is((short) 11));
		assertThat("min power constrains narrowing to int", NumberUtils.narrow(n, 2), is(11));
		assertThat("min power disallows narrowing", NumberUtils.narrow(n, 3), is(sameInstance(n)));

		n = 1234567890123789L;
		assertThat("too big to narrow", NumberUtils.narrow(n, 0), is(sameInstance(n)));
	}

	@Test
	public void narrowFloat() {
		Float f = 123.0f;
		assertThat("float cannot be narrowed", NumberUtils.narrow(f, 0), is(sameInstance(f)));
	}

	@Test
	public void narrowDouble() {
		assertThat("narrowed to float", NumberUtils.narrow(123.0, 0), is(123.0f));
		Number n = 1238909809.190298093;
		assertThat("too big to narrow", NumberUtils.narrow(n, 0), is(sameInstance(n)));
	}

	@Test
	public void narrowDouble_minBytePower() {
		// GIVEN
		final Double exactFloat = 1.5;
		final Double inexactFloat = 0.1;

		// @formatter:off
		then(NumberUtils.narrow(exactFloat, 2))
			.as("Min power 2 narrows to float")
			.isEqualTo(1.5f)
			;
		then(NumberUtils.narrow(exactFloat, 3))
			.as("Min power 3 keeps 64-bit double, not narrowed to float")
			.isSameAs(exactFloat)
			;
		then(NumberUtils.narrow(exactFloat, 4))
			.as("Min power above double size keeps double, not narrowed to float")
			.isSameAs(exactFloat)
			;
		then(NumberUtils.narrow(inexactFloat, 4))
			.as("Min power above double size keeps double, not widened to BigDecimal")
			.isSameAs(inexactFloat)
			;
		// @formatter:on
	}

	@Test
	public void linearInterp() {
		assertThat("interpolated simple", NumberUtils.linearInterpolate(20, 10, 50, 0, 4),
				is(new BigDecimal("1")));
		assertThat("interpolated using multiplication before division",
				NumberUtils.linearInterpolate(11, 3, 17, 12, 47), is(new BigDecimal("32")));
		assertThat("interpolated to fraction", NumberUtils.linearInterpolate(13, 1, 99, 0, 10),
				is(new BigDecimal("1.224489795918")));
	}

	@Test
	public void convertNumber_int() {
		final Integer n = 123;
		assertThat("Integer converts as-is", NumberUtils.convertNumber(n, Integer.class),
				is(sameInstance(n)));
		assertThat("Long converts", NumberUtils.convertNumber(234L, Integer.class), is(equalTo(234)));
		assertThat("Double converts", NumberUtils.convertNumber(34.5, Integer.class), is(equalTo(34)));
		assertThat("Float converts", NumberUtils.convertNumber(45.6f, Integer.class), is(equalTo(45)));
		assertThat("BigDecimal converts",
				NumberUtils.convertNumber(new BigDecimal("56.7"), Integer.class), is(equalTo(56)));
		assertThat("BigInteger converts", NumberUtils.convertNumber(new BigInteger("67"), Integer.class),
				is(equalTo(67)));
	}

	@Test
	public void convertNumber_long() {
		final Long n = 123L;
		assertThat("Long converts as-is", NumberUtils.convertNumber(n, Long.class), is(sameInstance(n)));
		assertThat("Integer converts", NumberUtils.convertNumber(234, Long.class), is(equalTo(234L)));
		assertThat("Double converts", NumberUtils.convertNumber(34.5, Long.class), is(equalTo(34L)));
		assertThat("Float converts", NumberUtils.convertNumber(45.6f, Long.class), is(equalTo(45L)));
		assertThat("BigDecimal converts", NumberUtils.convertNumber(new BigDecimal("56.7"), Long.class),
				is(equalTo(56L)));
		assertThat("BigInteger converts", NumberUtils.convertNumber(new BigInteger("67"), Long.class),
				is(equalTo(67L)));
	}

	@Test
	public void convertNumber_bigDecimal() {
		final BigDecimal n = new BigDecimal("123");
		assertThat("BigDecimal converts as-is", NumberUtils.convertNumber(n, BigDecimal.class),
				is(sameInstance(n)));
		assertThat("Integer converts", NumberUtils.convertNumber(234, BigDecimal.class),
				is(equalTo(new BigDecimal("234"))));
		assertThat("Long converts", NumberUtils.convertNumber(123L, BigDecimal.class),
				is(equalTo(new BigDecimal("123"))));
		assertThat("Double converts", NumberUtils.convertNumber(34.5, BigDecimal.class),
				is(equalTo(new BigDecimal("34.5"))));
		assertThat("Float converts", NumberUtils.convertNumber(45.6f, BigDecimal.class),
				is(equalTo(new BigDecimal("45.6"))));
		assertThat("BigDecimal converts",
				NumberUtils.convertNumber(new BigDecimal("56.7"), BigDecimal.class),
				is(equalTo(new BigDecimal("56.7"))));
	}

	@Test
	public void convertNumber_bigInteger() {
		final BigInteger n = new BigInteger("123");
		assertThat("BigInteger converts as-is", NumberUtils.convertNumber(n, BigInteger.class),
				is(sameInstance(n)));
		assertThat("Integer converts", NumberUtils.convertNumber(234, BigInteger.class),
				is(equalTo(new BigInteger("234"))));
		assertThat("Long converts", NumberUtils.convertNumber(123L, BigInteger.class),
				is(equalTo(new BigInteger("123"))));
		assertThat("Double converts", NumberUtils.convertNumber(34.5, BigInteger.class),
				is(equalTo(new BigInteger("34"))));
		assertThat("Float converts", NumberUtils.convertNumber(45.6f, BigInteger.class),
				is(equalTo(new BigInteger("45"))));
		assertThat("BigDecimal converts",
				NumberUtils.convertNumber(new BigDecimal("56.7"), BigInteger.class),
				is(equalTo(new BigInteger("56"))));
	}

	@Test
	public void convertNumber_bigInteger_large() {
		final BigInteger expected = BigInteger.TEN.pow(30);
		// @formatter:off
		then(NumberUtils.convertNumber(new BigDecimal("1E+30"), BigInteger.class))
			.as("BigDecimal larger than long converts")
			.isEqualTo(expected)
			;
		then(NumberUtils.convertNumber(new BigDecimal("-1.5E+30"), BigInteger.class))
			.as("Negative BigDecimal larger than long converts")
			.isEqualTo(expected.multiply(BigInteger.valueOf(-15)).divide(BigInteger.TEN))
			;
		then(NumberUtils.convertNumber(1e30, BigInteger.class))
			.as("Double larger than long converts")
			.isEqualTo(expected)
			;
		then(NumberUtils.convertNumber(1e30f, BigInteger.class))
			.as("Float larger than long converts")
			.isEqualTo(expected)
			;
		// @formatter:on
	}

	@Test
	public void convertNumber_bigInteger_nonFinite() {
		// @formatter:off
		then(NumberUtils.convertNumber(Double.NaN, BigInteger.class))
			.as("NaN converts like long")
			.isEqualTo(BigInteger.ZERO)
			;
		then(NumberUtils.convertNumber(Double.POSITIVE_INFINITY, BigInteger.class))
			.as("Positive infinity converts like long")
			.isEqualTo(BigInteger.valueOf(Long.MAX_VALUE))
			;
		then(NumberUtils.convertNumber(Float.NEGATIVE_INFINITY, BigInteger.class))
			.as("Negative infinity converts like long")
			.isEqualTo(BigInteger.valueOf(Long.MIN_VALUE))
			;
		// @formatter:on
	}

	@Test
	public void convertNumber_short() {
		final Short n = 123;
		// @formatter:off
		then(NumberUtils.convertNumber(n, Short.class))
			.as("Short converts as-is")
			.isSameAs(n)
			;
		then(NumberUtils.convertNumber(234, Short.class))
			.as("Integer converts")
			.isEqualTo((short) 234)
			;
		then(NumberUtils.convertNumber(34.5, Short.class))
			.as("Double converts")
			.isEqualTo((short) 34)
			;
		then(NumberUtils.convertNumber(new BigDecimal("56.7"), Short.class))
			.as("BigDecimal converts")
			.isEqualTo((short) 56)
			;
		// @formatter:on
	}

	@Test
	public void convertNumber_byte() {
		final Byte n = 12;
		// @formatter:off
		then(NumberUtils.convertNumber(n, Byte.class))
			.as("Byte converts as-is")
			.isSameAs(n)
			;
		then(NumberUtils.convertNumber(23, Byte.class))
			.as("Integer converts")
			.isEqualTo((byte) 23)
			;
		then(NumberUtils.convertNumber(new BigInteger("34"), Byte.class))
			.as("BigInteger converts")
			.isEqualTo((byte) 34)
			;
		// @formatter:on
	}

	@Test
	public void parseNumber_withType() {
		// @formatter:off
		then(NumberUtils.parseNumber("123", Integer.class))
			.as("Integer parsed")
			.isEqualTo(123)
			;
		then(NumberUtils.parseNumber("123", Long.class))
			.as("Long parsed")
			.isEqualTo(123L)
			;
		then(NumberUtils.parseNumber("12.5", Float.class))
			.as("Float parsed")
			.isEqualTo(12.5f)
			;
		then(NumberUtils.parseNumber("12.5", Double.class))
			.as("Double parsed")
			.isEqualTo(12.5)
			;
		then(NumberUtils.parseNumber("12.50", BigDecimal.class))
			.as("BigDecimal parsed")
			.isEqualTo(new BigDecimal("12.50"))
			;
		then(NumberUtils.parseNumber("123", BigInteger.class))
			.as("BigInteger parsed")
			.isEqualTo(new BigInteger("123"))
			;
		then(NumberUtils.parseNumber("123", Short.class))
			.as("Short parsed")
			.isEqualTo((short) 123)
			;
		then(NumberUtils.parseNumber("-12", Byte.class))
			.as("Byte parsed")
			.isEqualTo((byte) -12)
			;
		// @formatter:on
	}

	@Test
	public void parseNumber_int() {
		assertThat("Int parsed", NumberUtils.parseNumber("123"), is(equalTo(123)));
		assertThat("Negative int parsed", NumberUtils.parseNumber("-123"), is(equalTo(-123)));

		assertThat("Long parsed", NumberUtils.parseNumber("1234567890123456789"),
				is(equalTo(1234567890123456789L)));
		assertThat("Negative long parsed", NumberUtils.parseNumber("-1234567890123456789"),
				is(equalTo(-1234567890123456789L)));

		assertThat("Float parsed", NumberUtils.parseNumber("12.3"), is(equalTo(12.3f)));
		assertThat("Negative float parsed", NumberUtils.parseNumber("-12.3"), is(equalTo(-12.3f)));

		assertThat("Double parsed", NumberUtils.parseNumber("12.34567890123"),
				is(equalTo(12.34567890123)));
		assertThat("Negative double parsed", NumberUtils.parseNumber("-12.34567890123"),
				is(equalTo(-12.34567890123)));
	}

	@Test
	public void parseNumber_long() {
		assertThat("Long parsed", NumberUtils.parseNumber("1234567890123456789"),
				is(equalTo(1234567890123456789L)));
		assertThat("Negative long parsed", NumberUtils.parseNumber("-1234567890123456789"),
				is(equalTo(-1234567890123456789L)));
	}

	@Test
	public void parseNumber_float() {
		assertThat("Float parsed", NumberUtils.parseNumber("12.3"), is(equalTo(12.3f)));
		assertThat("Negative float parsed", NumberUtils.parseNumber("-12.3"), is(equalTo(-12.3f)));
	}

	@Test
	public void parseNumber_double() {
		assertThat("Double parsed", NumberUtils.parseNumber("12.34567890123"),
				is(equalTo(12.34567890123)));
		assertThat("Negative double parsed", NumberUtils.parseNumber("-12.34567890123"),
				is(equalTo(-12.34567890123)));
	}

	@Test
	public void parseNumber_BigInteger() {
		assertThat("BigInteger parsed",
				NumberUtils.parseNumber("123456789012345678901234567890123456789"),
				is(equalTo(new BigInteger("123456789012345678901234567890123456789"))));
		assertThat("Negative BigInteger parsed",
				NumberUtils.parseNumber("-123456789012345678901234567890123456789"),
				is(equalTo(new BigInteger("-123456789012345678901234567890123456789"))));
	}

	@Test
	public void parseNumber_BigDecimal() {
		assertThat("BigDecimal parsed", NumberUtils.parseNumber(
				"123456789012345678901234567890123456789.123456789012345678901234567890123456789"),
				is(equalTo(new BigDecimal(
						"123456789012345678901234567890123456789.123456789012345678901234567890123456789"))));
		assertThat("Negative BigDecimal parsed", NumberUtils.parseNumber(
				"-123456789012345678901234567890123456789.123456789012345678901234567890123456789"),
				is(equalTo(new BigDecimal(
						"-123456789012345678901234567890123456789.123456789012345678901234567890123456789"))));
	}

	@Test
	public void parseNumber_hex() {
		then(NumberUtils.parseNumber("0x123abc1234567890123456789")).as("Hex parsed into BigIneteger")
				.isEqualTo(new BigInteger("123abc1234567890123456789", 16));
		then(NumberUtils.parseNumber("0X123ABC1234567890123456789"))
				.as("Upper case hex parsed into BigIneteger")
				.isEqualTo(new BigInteger("123abc1234567890123456789", 16));
		then(NumberUtils.parseNumber("123abc1234567890123456789")).as("Unprefixed hex parsed as null")
				.isNull();
		then(NumberUtils.parseNumber("123ABC1234567890123456789"))
				.as("Unprefixed upper case hex parsed as null").isNull();
	}

	@Test
	public void compare_nulls() {
		// @formatter:off
		then(NumberUtils.compare(null, null))
			.as("Two nulls are equal")
			.isZero()
			;
		then(NumberUtils.compare(null, 1))
			.as("Null sorts before number")
			.isNegative()
			;
		then(NumberUtils.compare(1, null))
			.as("Number sorts after null")
			.isPositive()
			;
		// @formatter:on
	}

	@Test
	public void compare_sameType() {
		// GIVEN
		final Integer n = 1000;

		// THEN
		// @formatter:off
		then(NumberUtils.compare(n, n))
			.as("Same instance is equal")
			.isZero()
			;
		then(NumberUtils.compare(Integer.valueOf(1000), Integer.valueOf(1000)))
			.as("Integer instances with same value are equal")
			.isZero()
			;
		then(NumberUtils.compare(1, 2))
			.as("Smaller Integer sorts first")
			.isNegative()
			;
		then(NumberUtils.compare(2, 1))
			.as("Larger Integer sorts last")
			.isPositive()
			;
		then(NumberUtils.compare(1.5, 2.5))
			.as("Smaller Double sorts first")
			.isNegative()
			;
		then(NumberUtils.compare(new BigDecimal("1.0"), new BigDecimal("1.00")))
			.as("BigDecimal values compared ignoring scale")
			.isZero()
			;
		// @formatter:on
	}

	@Test
	public void compare_mixedTypes() {
		// @formatter:off
		then(NumberUtils.compare(1, 2L))
			.as("Smaller Integer sorts before larger Long")
			.isNegative()
			;
		then(NumberUtils.compare(2L, 1))
			.as("Larger Long sorts after smaller Integer")
			.isPositive()
			;
		then(NumberUtils.compare(1, new BigDecimal("1.0")))
			.as("Integer and BigDecimal with same value are equal")
			.isZero()
			;
		then(NumberUtils.compare(new BigInteger("123456789012345678901234567890"), Long.MAX_VALUE))
			.as("BigInteger larger than any Long sorts after Long")
			.isPositive()
			;
		then(NumberUtils.compare(0.1f, 0.1))
			.as("Float and Double with same decimal value are equal")
			.isZero()
			;
		then(NumberUtils.compare((short) 3, (byte) 2))
			.as("Larger Short sorts after smaller Byte")
			.isPositive()
			;
		// @formatter:on
	}

	@Test
	public void compare_notComparable() {
		// @formatter:off
		then(NumberUtils.compare(new AtomicInteger(1), new AtomicInteger(2)))
			.as("Smaller AtomicInteger sorts first, compared by value")
			.isNegative()
			;
		then(NumberUtils.compare(new AtomicInteger(2), new AtomicInteger(2)))
			.as("AtomicInteger instances with same value are equal")
			.isZero()
			;
		then(NumberUtils.compare(new AtomicLong(2), 1))
			.as("Larger AtomicLong sorts after smaller Integer")
			.isPositive()
			;
		// @formatter:on
	}

	@Test
	public void compare_nan() {
		// @formatter:off
		then(NumberUtils.compare(Double.NaN, 1))
			.as("Double NaN sorts after Integer")
			.isPositive()
			;
		then(NumberUtils.compare(1, Double.NaN))
			.as("Integer sorts before Double NaN")
			.isNegative()
			;
		then(NumberUtils.compare(Float.NaN, new BigDecimal("1e400")))
			.as("Float NaN sorts after BigDecimal larger than any Double")
			.isPositive()
			;
		then(NumberUtils.compare(Float.NaN, Double.NaN))
			.as("Float NaN and Double NaN are equal")
			.isZero()
			;
		then(NumberUtils.compare(Double.NaN, Float.POSITIVE_INFINITY))
			.as("Double NaN sorts after Float positive infinity")
			.isPositive()
			;
		// @formatter:on
	}

	@Test
	public void compare_infinity() {
		// @formatter:off
		then(NumberUtils.compare(Double.POSITIVE_INFINITY, Long.MAX_VALUE))
			.as("Double positive infinity sorts after Long")
			.isPositive()
			;
		then(NumberUtils.compare(Double.NEGATIVE_INFINITY, Long.MIN_VALUE))
			.as("Double negative infinity sorts before Long")
			.isNegative()
			;
		then(NumberUtils.compare(new BigDecimal("1e400"), Double.POSITIVE_INFINITY))
			.as("BigDecimal larger than any Double sorts before Double positive infinity")
			.isNegative()
			;
		then(NumberUtils.compare(new BigDecimal("-1e400"), Float.NEGATIVE_INFINITY))
			.as("BigDecimal smaller than any Double sorts after Float negative infinity")
			.isPositive()
			;
		then(NumberUtils.compare(Float.POSITIVE_INFINITY, Double.POSITIVE_INFINITY))
			.as("Float and Double positive infinity are equal")
			.isZero()
			;
		then(NumberUtils.compare(Float.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY))
			.as("Float negative infinity sorts before Double positive infinity")
			.isNegative()
			;
		// @formatter:on
	}

	@Test
	public void abs_null() {
		then(NumberUtils.abs(null)).as("Null input returns null").isNull();
	}

	@Test
	public void abs_integer() {
		// @formatter:off
		then(NumberUtils.abs(-5))
			.as("Negative Integer returns positive Integer")
			.isEqualTo(5)
			;
		then(NumberUtils.abs(5))
			.as("Positive Integer returned as-is")
			.isEqualTo(5)
			;
		then(NumberUtils.abs(Integer.MIN_VALUE))
			.as("Integer minimum value returns positive Long, outside Integer range")
			.isEqualTo(2147483648L)
			;
		// @formatter:on
	}

	@Test
	public void abs_long() {
		// @formatter:off
		then(NumberUtils.abs(-5L))
			.as("Negative Long returns positive Long")
			.isEqualTo(5L)
			;
		then(NumberUtils.abs(Long.MIN_VALUE))
			.as("Long minimum value returns positive BigInteger, outside Long range")
			.isEqualTo(new BigInteger("9223372036854775808"))
			;
		// @formatter:on
	}

	@Test
	public void abs_float() {
		// @formatter:off
		then(NumberUtils.abs(-1.5f))
			.as("Negative Float returns positive Float")
			.isEqualTo(1.5f)
			;
		then(NumberUtils.abs(-0.0f))
			.as("Negative zero Float returns positive zero Float")
			.isEqualTo(0.0f)
			;
		then(NumberUtils.abs(Float.NEGATIVE_INFINITY))
			.as("Float negative infinity returns positive infinity")
			.isEqualTo(Float.POSITIVE_INFINITY)
			;
		then(NumberUtils.abs(Float.NaN))
			.as("Float NaN returns NaN")
			.isEqualTo(Float.NaN)
			;
		// @formatter:on
	}

	@Test
	public void abs_double() {
		// @formatter:off
		then(NumberUtils.abs(-1.5))
			.as("Negative Double returns positive Double")
			.isEqualTo(1.5)
			;
		then(NumberUtils.abs(-0.0))
			.as("Negative zero Double returns positive zero Double")
			.isEqualTo(0.0)
			;
		then(NumberUtils.abs(Double.NEGATIVE_INFINITY))
			.as("Double negative infinity returns positive infinity")
			.isEqualTo(Double.POSITIVE_INFINITY)
			;
		then(NumberUtils.abs(Double.NaN))
			.as("Double NaN returns NaN")
			.isEqualTo(Double.NaN)
			;
		// @formatter:on
	}

	@Test
	public void abs_bigNumbers() {
		// @formatter:off
		then(NumberUtils.abs(new BigInteger("-123456789012345678901234567890")))
			.as("Negative BigInteger returns positive BigInteger")
			.isEqualTo(new BigInteger("123456789012345678901234567890"))
			;
		then(NumberUtils.abs(new BigDecimal("-1.50")))
			.as("Negative BigDecimal returns positive BigDecimal with same scale")
			.isEqualTo(new BigDecimal("1.50"))
			;
		// @formatter:on
	}

	@Test
	public void abs_otherTypes() {
		// @formatter:off
		then(NumberUtils.abs((short) -3))
			.as("Negative Short returns positive Integer")
			.isEqualTo(3)
			;
		then(NumberUtils.abs(Short.MIN_VALUE))
			.as("Short minimum value returns positive Integer, outside Short range")
			.isEqualTo(32768)
			;
		then(NumberUtils.abs((byte) -3))
			.as("Negative Byte returns positive Integer")
			.isEqualTo(3)
			;
		then(NumberUtils.abs(new AtomicInteger(-3)))
			.as("Negative AtomicInteger returns positive value")
			.hasToString("3")
			;
		then(NumberUtils.abs(new AtomicLong(-3)))
			.as("Negative AtomicLong returns positive value")
			.hasToString("3")
			;
		// @formatter:on
	}

}
