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
 * @version 1.5
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
		Number n = NumberUtils.offset(new BigDecimal("1.23"), null);
		assertThat("Result unchanged from null offset", n, sameInstance(n));
	}

	@Test
	public void offset_zeroOffset() {
		Number n = NumberUtils.offset(new BigDecimal("1.23"), BigDecimal.ZERO);
		assertThat("Result unchanged from 0 offset", n, sameInstance(n));
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
		Number n = NumberUtils.multiplied(new BigDecimal("1.23"), null);
		assertThat("Result unchanged from null multiple", n, sameInstance(n));
	}

	@Test
	public void multiplied_zeroMultiple() {
		Number n = NumberUtils.multiplied(new BigDecimal("1.23"), BigDecimal.ZERO);
		assertThat("Result unchanged from 0 multiple", n, sameInstance(n));
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
		Number n = NumberUtils.maximumDecimalScale(new BigDecimal("1.23"), -1);
		assertThat("Result unchanged from negative maximumDecimalScale", n, sameInstance(n));
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
		assertThat("positive zero", NumberUtils.roundup(new BigDecimal("0.0"), 0),
				is(new BigDecimal("0")));
		assertThat("positive left", NumberUtils.roundup(new BigDecimal("1.2"), 0),
				is(new BigDecimal("2")));
		assertThat("positive even", NumberUtils.roundup(new BigDecimal("1.5"), 0),
				is(new BigDecimal("2")));
		assertThat("positive right", NumberUtils.roundup(new BigDecimal("2.7"), 0),
				is(new BigDecimal("3")));
		assertThat("positive exact", NumberUtils.roundup(new BigDecimal("3.0"), 0),
				is(new BigDecimal("3")));

		assertThat("negative zero", NumberUtils.roundup(new BigDecimal("-0.0"), 0),
				is(new BigDecimal("-0")));
		assertThat("negative left", NumberUtils.roundup(new BigDecimal("-1.2"), 0),
				is(new BigDecimal("-2")));
		assertThat("negative even", NumberUtils.roundup(new BigDecimal("-1.5"), 0),
				is(new BigDecimal("-2")));
		assertThat("negative right", NumberUtils.roundup(new BigDecimal("-2.7"), 0),
				is(new BigDecimal("-3")));
		assertThat("negative exact", NumberUtils.roundup(new BigDecimal("-3.0"), 0),
				is(new BigDecimal("-3")));
	}

	@Test
	public void roundup_3() {
		assertThat("positive zero", NumberUtils.roundup(new BigDecimal("0.0"), 3),
				is(new BigDecimal("0.0")));
		assertThat("positive left", NumberUtils.roundup(new BigDecimal("1.1232"), 3),
				is(new BigDecimal("1.124")));
		assertThat("positive even", NumberUtils.roundup(new BigDecimal("1.1235"), 3),
				is(new BigDecimal("1.124")));
		assertThat("positive right", NumberUtils.roundup(new BigDecimal("1.1237"), 3),
				is(new BigDecimal("1.124")));
		assertThat("positive exact", NumberUtils.roundup(new BigDecimal("1.1230"), 3),
				is(new BigDecimal("1.123")));

		assertThat("negative zero", NumberUtils.roundup(new BigDecimal("-0.0"), 3),
				is(new BigDecimal("-0.0")));
		assertThat("negative left", NumberUtils.roundup(new BigDecimal("-1.1232"), 3),
				is(new BigDecimal("-1.124")));
		assertThat("negative even", NumberUtils.roundup(new BigDecimal("-1.1235"), 3),
				is(new BigDecimal("-1.124")));
		assertThat("negative right", NumberUtils.roundup(new BigDecimal("-1.1237"), 3),
				is(new BigDecimal("-1.124")));
		assertThat("negative exact", NumberUtils.roundup(new BigDecimal("-1.1230"), 3),
				is(new BigDecimal("-1.123")));
	}

	@Test
	public void rounddown_0() {
		assertThat("positive zero", NumberUtils.rounddown(new BigDecimal("0.0"), 0),
				is(new BigDecimal("0")));
		assertThat("positive left", NumberUtils.rounddown(new BigDecimal("1.2"), 0),
				is(new BigDecimal("1")));
		assertThat("positive even", NumberUtils.rounddown(new BigDecimal("1.5"), 0),
				is(new BigDecimal("1")));
		assertThat("positive right", NumberUtils.rounddown(new BigDecimal("2.7"), 0),
				is(new BigDecimal("2")));
		assertThat("positive exact", NumberUtils.rounddown(new BigDecimal("3.0"), 0),
				is(new BigDecimal("3")));

		assertThat("negative zero", NumberUtils.rounddown(new BigDecimal("-0.0"), 0),
				is(new BigDecimal("-0")));
		assertThat("negative left", NumberUtils.rounddown(new BigDecimal("-1.2"), 0),
				is(new BigDecimal("-1")));
		assertThat("negative even", NumberUtils.rounddown(new BigDecimal("-1.5"), 0),
				is(new BigDecimal("-1")));
		assertThat("negative right", NumberUtils.rounddown(new BigDecimal("-2.7"), 0),
				is(new BigDecimal("-2")));
		assertThat("negative exact", NumberUtils.rounddown(new BigDecimal("-3.0"), 0),
				is(new BigDecimal("-3")));
	}

	@Test
	public void rounddown_3() {
		assertThat("positive zero", NumberUtils.rounddown(new BigDecimal("0.0"), 3),
				is(new BigDecimal("0.0")));
		assertThat("positive left", NumberUtils.rounddown(new BigDecimal("1.1232"), 3),
				is(new BigDecimal("1.123")));
		assertThat("positive even", NumberUtils.rounddown(new BigDecimal("1.1235"), 3),
				is(new BigDecimal("1.123")));
		assertThat("positive right", NumberUtils.rounddown(new BigDecimal("1.1237"), 3),
				is(new BigDecimal("1.123")));
		assertThat("positive exact", NumberUtils.rounddown(new BigDecimal("1.1230"), 3),
				is(new BigDecimal("1.123")));

		assertThat("negative zero", NumberUtils.rounddown(new BigDecimal("-0.0"), 3),
				is(new BigDecimal("-0.0")));
		assertThat("negative left", NumberUtils.rounddown(new BigDecimal("-1.1232"), 3),
				is(new BigDecimal("-1.123")));
		assertThat("negative even", NumberUtils.rounddown(new BigDecimal("-1.1235"), 3),
				is(new BigDecimal("-1.123")));
		assertThat("negative right", NumberUtils.rounddown(new BigDecimal("-1.1237"), 3),
				is(new BigDecimal("-1.123")));
		assertThat("negative exact", NumberUtils.rounddown(new BigDecimal("-1.1230"), 3),
				is(new BigDecimal("-1.123")));
	}

}
