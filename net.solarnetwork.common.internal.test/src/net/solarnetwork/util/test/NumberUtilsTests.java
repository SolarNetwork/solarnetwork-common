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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.hamcrest.Matchers;
import org.junit.Test;
import net.solarnetwork.util.NumberUtils;

/**
 * Unit tests for the {@link NumberUtils} class.
 * 
 * @author matt
 * @version 1.0
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

}
