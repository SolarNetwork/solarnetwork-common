/* ==================================================================
 * ByteUtilsTests.java - 25/09/2019 8:32:16 pm
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

package net.solarnetwork.util.test;

import static net.solarnetwork.domain.ByteOrdering.BigEndian;
import static net.solarnetwork.domain.ByteOrdering.LittleEndian;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import org.hamcrest.Matchers;
import org.junit.Test;
import net.solarnetwork.util.ByteUtils;

/**
 * Test cases for the {@link ByteUtils} class.
 * 
 * @author matt
 * @version 1.0
 */
public class ByteUtilsTests {

	@Test
	public void encodeInt8() {
		byte[] dest = new byte[1];
		ByteUtils.encodeInt8((byte) -56, dest, 0);
		assertThat("Encoded value", Arrays.equals(dest, new byte[] { (byte) -56 }), equalTo(true));
	}

	@Test
	public void encodeInt8_offset() {
		byte[] dest = new byte[] { (byte) 0, (byte) 0, (byte) 0 };
		ByteUtils.encodeInt8((byte) -56, dest, 2);
		assertThat("Encoded value", Arrays.equals(dest, new byte[] { (byte) 0, (byte) 0, (byte) -56 }),
				equalTo(true));
	}

	@Test
	public void encodeInt16() {
		byte[] dest = new byte[2];
		ByteUtils.encodeInt16((short) -29917, dest, 0, BigEndian);
		assertThat("Encoded value", Arrays.equals(dest, new byte[] { (byte) 0x8B, (byte) 0x23 }),
				equalTo(true));
	}

	@Test
	public void encodeInt16_offset() {
		byte[] dest = new byte[] { (byte) 0, (byte) 0, (byte) 0 };
		ByteUtils.encodeInt16((short) -29917, dest, 1, BigEndian);
		assertThat("Encoded value",
				Arrays.equals(dest, new byte[] { (byte) 0, (byte) 0x8B, (byte) 0x23 }), equalTo(true));
	}

	@Test
	public void encodeInt16_LittleEndian() {
		byte[] dest = new byte[2];
		ByteUtils.encodeInt16((short) -29917, dest, 0, LittleEndian);
		assertThat("Encoded value", Arrays.equals(dest, new byte[] { (byte) 0x23, (byte) 0x8B }),
				equalTo(true));
	}

	@Test
	public void encodeInt16_LittleEndian_offset() {
		byte[] dest = new byte[] { (byte) 0, (byte) 0, (byte) 0 };
		ByteUtils.encodeInt16((short) -29917, dest, 1, LittleEndian);
		assertThat("Encoded value",
				Arrays.equals(dest, new byte[] { (byte) 0, (byte) 0x23, (byte) 0x8B }), equalTo(true));
	}

	@Test
	public void encodeUnsignedInt16() {
		byte[] dest = new byte[2];
		ByteUtils.encodeUnsignedInt16(35619, dest, 0, BigEndian);
		assertThat("Encoded value", Arrays.equals(dest, new byte[] { (byte) 0x8B, (byte) 0x23 }),
				equalTo(true));
	}

	@Test
	public void encodeUnsignedInt16_offset() {
		byte[] dest = new byte[] { (byte) 0, (byte) 0, (byte) 0 };
		ByteUtils.encodeUnsignedInt16(35619, dest, 1, BigEndian);
		assertThat("Encoded value",
				Arrays.equals(dest, new byte[] { (byte) 0, (byte) 0x8B, (byte) 0x23 }), equalTo(true));
	}

	@Test
	public void encodeUnsignedInt16_LittleEndian() {
		byte[] dest = new byte[2];
		ByteUtils.encodeUnsignedInt16(35619, dest, 0, LittleEndian);
		assertThat("Encoded value", Arrays.equals(dest, new byte[] { (byte) 0x23, (byte) 0x8B }),
				equalTo(true));
	}

	@Test
	public void encodeUnsignedInt16_LittleEndian_offset() {
		byte[] dest = new byte[] { (byte) 0, (byte) 0, (byte) 0 };
		ByteUtils.encodeUnsignedInt16(35619, dest, 1, LittleEndian);
		assertThat("Encoded value",
				Arrays.equals(dest, new byte[] { (byte) 0, (byte) 0x23, (byte) 0x8B }), equalTo(true));
	}

	@Test
	public void encodeInt32() {
		byte[] dest = new byte[4];
		ByteUtils.encodeInt32(-299178798, dest, 0, BigEndian);
		assertThat("Encoded value",
				Arrays.equals(dest, new byte[] { (byte) 0xEE, (byte) 0x2A, (byte) 0xE4, (byte) 0xD2 }),
				equalTo(true));
	}

	@Test
	public void encodeInt32_offset() {
		byte[] dest = new byte[] { (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0 };
		ByteUtils.encodeInt32(-299178798, dest, 1, BigEndian);
		assertThat("Encoded value",
				Arrays.equals(dest,
						new byte[] { (byte) 0, (byte) 0xEE, (byte) 0x2A, (byte) 0xE4, (byte) 0xD2 }),
				equalTo(true));
	}

	@Test
	public void encodeInt32_LittleEndian() {
		byte[] dest = new byte[4];
		ByteUtils.encodeInt32(-299178798, dest, 0, LittleEndian);
		assertThat("Encoded value",
				Arrays.equals(dest, new byte[] { (byte) 0xD2, (byte) 0xE4, (byte) 0x2A, (byte) 0xEE }),
				equalTo(true));
	}

	@Test
	public void encodeInt32_LittleEndian_offset() {
		byte[] dest = new byte[] { (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0 };
		ByteUtils.encodeInt32(3995788498L, dest, 1, LittleEndian);
		assertThat("Encoded value",
				Arrays.equals(dest,
						new byte[] { (byte) 0, (byte) 0xD2, (byte) 0xE4, (byte) 0x2A, (byte) 0xEE }),
				equalTo(true));
	}

	@Test
	public void encodeUnsignedInt32() {
		byte[] dest = new byte[4];
		ByteUtils.encodeUnsignedInt32(3995788498L, dest, 0, BigEndian);
		assertThat("Encoded value",
				Arrays.equals(dest, new byte[] { (byte) 0xEE, (byte) 0x2A, (byte) 0xE4, (byte) 0xD2 }),
				equalTo(true));
	}

	@Test
	public void encodeUnsignedInt32_offset() {
		byte[] dest = new byte[] { (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0 };
		ByteUtils.encodeUnsignedInt32(3995788498L, dest, 1, BigEndian);
		assertThat("Encoded value",
				Arrays.equals(dest,
						new byte[] { (byte) 0, (byte) 0xEE, (byte) 0x2A, (byte) 0xE4, (byte) 0xD2 }),
				equalTo(true));
	}

	@Test
	public void encodeUnsignedInt32_LittleEndian() {
		byte[] dest = new byte[4];
		ByteUtils.encodeUnsignedInt32(3995788498L, dest, 0, LittleEndian);
		assertThat("Encoded value",
				Arrays.equals(dest, new byte[] { (byte) 0xD2, (byte) 0xE4, (byte) 0x2A, (byte) 0xEE }),
				equalTo(true));
	}

	@Test
	public void encodeUnsignedInt32_LittleEndian_offset() {
		byte[] dest = new byte[] { (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0 };
		ByteUtils.encodeUnsignedInt32(3995788498L, dest, 1, LittleEndian);
		assertThat("Encoded value",
				Arrays.equals(dest,
						new byte[] { (byte) 0, (byte) 0xD2, (byte) 0xE4, (byte) 0x2A, (byte) 0xEE }),
				equalTo(true));
	}

	@Test
	public void encodeInt64() {
		byte[] dest = new byte[8];
		ByteUtils.encodeInt64(-1091497730748786224L, dest, 0, BigEndian);
		assertThat(
				"Encoded value", Arrays
						.equals(dest,
								new byte[] { (byte) 0xF0, (byte) 0xDA, (byte) 0x38, (byte) 0x98,
										(byte) 0xCD, (byte) 0x92, (byte) 0xD9, (byte) 0xD0 }),
				equalTo(true));
	}

	@Test
	public void encodeInt64_offset() {
		byte[] dest = new byte[] { (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
				(byte) 0, (byte) 0 };
		ByteUtils.encodeInt64(-1091497730748786224L, dest, 1, BigEndian);
		assertThat("Encoded value",
				Arrays.equals(dest, new byte[] { (byte) 0, (byte) 0xF0, (byte) 0xDA, (byte) 0x38,
						(byte) 0x98, (byte) 0xCD, (byte) 0x92, (byte) 0xD9, (byte) 0xD0 }),
				equalTo(true));
	}

	@Test
	public void encodeInt64_LittleEndian() {
		byte[] dest = new byte[8];
		ByteUtils.encodeInt64(-1091497730748786224L, dest, 0, LittleEndian);
		assertThat(
				"Encoded value", Arrays
						.equals(dest,
								new byte[] { (byte) 0xD0, (byte) 0xD9, (byte) 0x92, (byte) 0xCD,
										(byte) 0x98, (byte) 0x38, (byte) 0xDA, (byte) 0xF0 }),
				equalTo(true));
	}

	@Test
	public void encodeInt64_LittleEndian_offset() {
		byte[] dest = new byte[] { (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
				(byte) 0, (byte) 0 };
		ByteUtils.encodeInt64(-1091497730748786224L, dest, 1, LittleEndian);
		assertThat("Encoded value",
				Arrays.equals(dest, new byte[] { (byte) 0, (byte) 0xD0, (byte) 0xD9, (byte) 0x92,
						(byte) 0xCD, (byte) 0x98, (byte) 0x38, (byte) 0xDA, (byte) 0xF0 }),
				equalTo(true));
	}

	@Test
	public void encodeUnsignedInt64() {
		byte[] dest = new byte[8];
		ByteUtils.encodeUnsignedInt64(new BigInteger("17355246342960765392"), dest, 0, BigEndian);
		assertThat(
				"Encoded value", Arrays
						.equals(dest,
								new byte[] { (byte) 0xF0, (byte) 0xDA, (byte) 0x38, (byte) 0x98,
										(byte) 0xCD, (byte) 0x92, (byte) 0xD9, (byte) 0xD0 }),
				equalTo(true));
	}

	@Test
	public void encodeUnsignedInt64_offset() {
		byte[] dest = new byte[] { (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
				(byte) 0, (byte) 0 };
		ByteUtils.encodeUnsignedInt64(new BigInteger("17355246342960765392"), dest, 1, BigEndian);
		assertThat("Encoded value",
				Arrays.equals(dest, new byte[] { (byte) 0, (byte) 0xF0, (byte) 0xDA, (byte) 0x38,
						(byte) 0x98, (byte) 0xCD, (byte) 0x92, (byte) 0xD9, (byte) 0xD0 }),
				equalTo(true));
	}

	@Test
	public void encodeUnsignedInt64_LittleEndian() {
		byte[] dest = new byte[8];
		ByteUtils.encodeUnsignedInt64(new BigInteger("17355246342960765392"), dest, 0, LittleEndian);
		assertThat(
				"Encoded value", Arrays
						.equals(dest,
								new byte[] { (byte) 0xD0, (byte) 0xD9, (byte) 0x92, (byte) 0xCD,
										(byte) 0x98, (byte) 0x38, (byte) 0xDA, (byte) 0xF0 }),
				equalTo(true));
	}

	@Test
	public void encodeUnsignedInt64_LittleEndian_offset() {
		byte[] dest = new byte[] { (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
				(byte) 0, (byte) 0 };
		ByteUtils.encodeUnsignedInt64(new BigInteger("17355246342960765392"), dest, 1, LittleEndian);
		assertThat("Encoded value",
				Arrays.equals(dest, new byte[] { (byte) 0, (byte) 0xD0, (byte) 0xD9, (byte) 0x92,
						(byte) 0xCD, (byte) 0x98, (byte) 0x38, (byte) 0xDA, (byte) 0xF0 }),
				equalTo(true));
	}

	@Test
	public void parseFloat32() {
		Float result = ByteUtils.parseFloat32((byte) 0x40, (byte) 0x3F, (byte) 0xA7, (byte) 0xF6);
		assertThat(result, notNullValue());
		assertThat("Float bytes", Float.floatToIntBits(result), Matchers.equalTo(0x403FA7F6));
		assertThat("Float value", result.doubleValue(), closeTo(2.994626, 0.000001));
	}

	@Test
	public void parseFloat32NaN() {
		Float result = ByteUtils.parseFloat32((byte) 0xFF, (byte) 0xC0, (byte) 0x00, (byte) 0x00);
		assertThat("Float value is NaN", result, nullValue());
	}

	@Test
	public void parseFloat64() {
		Double result = ByteUtils.parseFloat64((byte) 0xC1, (byte) 0x4E, (byte) 0x0D, (byte) 0xF3,
				(byte) 0x0D, (byte) 0x0B, (byte) 0x6D, (byte) 0xE3);
		assertThat(result, notNullValue());
		assertThat("Double bytes", Double.doubleToLongBits(result),
				Matchers.equalTo(0xC14E0DF30D0B6DE3L));
		assertThat("Float value", result.doubleValue(), closeTo(-3939302.10191129292, 0.0000000000001));
	}

	@Test
	public void parseInt8() {
		assertThat(ByteUtils.parseInt8((byte) 0xCF), equalTo((byte) -49));
	}

	@Test
	public void parseUnsignedInt8() {
		assertThat(ByteUtils.parseUnsignedInt8((byte) 0xCF), equalTo((short) 207));
	}

	@Test
	public void parseInt16() {
		assertThat(ByteUtils.parseInt16((byte) 0x8A, (byte) 0xBC), equalTo((short) -30020));
	}

	@Test
	public void parseUnsignedInt16() {
		assertThat(ByteUtils.parseUnsignedInt16((byte) 0x8A, (byte) 0xBC), equalTo(35516));
	}

	@Test
	public void parseInt32() {
		assertThat(ByteUtils.parseInt32((byte) 0xFF, (byte) 0x44, (byte) 0x1C, (byte) 0x6F),
				equalTo(-12313489));
	}

	@Test
	public void parseUnsignedInt32() {
		assertThat(ByteUtils.parseUnsignedInt32((byte) 0xFF, (byte) 0x44, (byte) 0x1C, (byte) 0x6F),
				equalTo(4282653807L));
	}

	@Test
	public void parseInt64() {
		assertThat(ByteUtils.parseInt64((byte) 0x8A, (byte) 0xBC, (byte) 0x00, (byte) 0x39, (byte) 0xFF,
				(byte) 0x44, (byte) 0x1C, (byte) 0x6F), equalTo(-8449878551758103441L));
	}

	@Test
	public void parseUnsignedInt64() {
		assertThat(
				ByteUtils.parseUnsignedInt64((byte) 0x8A, (byte) 0xBC, (byte) 0x00, (byte) 0x39,
						(byte) 0xFF, (byte) 0x44, (byte) 0x1C, (byte) 0x6F),
				equalTo(new BigInteger("9996865521951448175")));
	}

	@Test
	public void parseUnsignedIntegerWayLargerThan64() {
		byte[] data = new byte[] { (byte) 0x00, (byte) 0x32, (byte) 0x85, (byte) 0x86, (byte) 0x61,
				(byte) 0x6F, (byte) 0x58, (byte) 0x66, (byte) 0xFF, (byte) 0x00, (byte) 0x17,
				(byte) 0x55, (byte) 0x86, (byte) 0x68, (byte) 0x16, (byte) 0xFE, (byte) 0x2F,
				(byte) 0x58, (byte) 0x66, (byte) 0x85, (byte) 0x86, (byte) 0x6B, (byte) 0x58,
				(byte) 0x66 };
		BigInteger bint = ByteUtils.parseUnsignedInteger(data, 0, data.length, BigEndian);
		assertThat(bint,
				equalTo(new BigInteger("00328586616F5866FF001755866816FE2F586685866B5866", 16)));
	}

	@Test
	public void parseUnsignedIntegerWayLargerThan64LeastSignificantWordOrder() {
		byte[] data = new byte[] { (byte) 0x66, (byte) 0x58, (byte) 0x6B, (byte) 0x86, (byte) 0x85,
				(byte) 0x66, (byte) 0x58, (byte) 0x2F, (byte) 0xFE, (byte) 0x16, (byte) 0x68,
				(byte) 0x86, (byte) 0x55, (byte) 0x17, (byte) 0x00, (byte) 0xFF, (byte) 0x66,
				(byte) 0x58, (byte) 0x6F, (byte) 0x61, (byte) 0x86, (byte) 0x85, (byte) 0x32,
				(byte) 0x00 };
		BigInteger bint = ByteUtils.parseUnsignedInteger(data, 0, data.length, LittleEndian);
		assertThat(bint,
				equalTo(new BigInteger("00328586616F5866FF001755866816FE2F586685866B5866", 16)));
	}

	@Test
	public void parseUnsignedIntegerWayLargerThan64NoLeadingZeros() {
		byte[] data = new byte[] { (byte) 0xDC, (byte) 0x32, (byte) 0x85, (byte) 0x86, (byte) 0x61,
				(byte) 0x6F, (byte) 0x58, (byte) 0x66, (byte) 0xFF, (byte) 0x00, (byte) 0x17,
				(byte) 0x55, (byte) 0x86, (byte) 0x68, (byte) 0x16, (byte) 0xFE, (byte) 0x2F,
				(byte) 0x58, (byte) 0x66, (byte) 0x85, (byte) 0x86, (byte) 0x6B, (byte) 0x58,
				(byte) 0x66 };
		BigInteger bint = ByteUtils.parseUnsignedInteger(data, 0, data.length, BigEndian);
		assertThat(bint,
				equalTo(new BigInteger("DC328586616F5866FF001755866816FE2F586685866B5866", 16)));
	}

	@Test
	public void parseUnsignedIntegerWayLargerThan64NoLeadingZerosLeastSignificantWordOrder() {
		byte[] data = new byte[] { (byte) 0x66, (byte) 0x58, (byte) 0x6B, (byte) 0x86, (byte) 0x85,
				(byte) 0x66, (byte) 0x58, (byte) 0x2F, (byte) 0xFE, (byte) 0x16, (byte) 0x68,
				(byte) 0x86, (byte) 0x55, (byte) 0x17, (byte) 0x00, (byte) 0xFF, (byte) 0x66,
				(byte) 0x58, (byte) 0x6F, (byte) 0x61, (byte) 0x86, (byte) 0x85, (byte) 0x32,
				(byte) 0xDC };
		BigInteger bint = ByteUtils.parseUnsignedInteger(data, 0, data.length, LittleEndian);
		assertThat(bint,
				equalTo(new BigInteger("DC328586616F5866FF001755866816FE2F586685866B5866", 16)));
	}

	@Test
	public void parseBytes() {
		byte[] data = new byte[] { (byte) 0x01, (byte) 0x03, (byte) 0x05, (byte) 0x07, (byte) 0x09,
				(byte) 0x0b, (byte) 0x0d, (byte) 0x00 };
		byte[] result = ByteUtils.parseBytes(data, 0, data.length, BigEndian);
		assertThat(Arrays.equals(result, data), equalTo(true));
	}

	@Test
	public void parseBytes_slice() {
		byte[] data = new byte[] { (byte) 0x01, (byte) 0x03, (byte) 0x05, (byte) 0x07, (byte) 0x09,
				(byte) 0x0b, (byte) 0x0d, (byte) 0x00 };
		byte[] result = ByteUtils.parseBytes(data, 2, 3, BigEndian);
		assertThat(Arrays.equals(result, new byte[] { (byte) 0x05, (byte) 0x07, (byte) 0x09 }),
				equalTo(true));
	}

	@Test
	public void parseBytes_LittleEndian() {
		byte[] data = new byte[] { (byte) 0x01, (byte) 0x03, (byte) 0x05, (byte) 0x07, (byte) 0x09,
				(byte) 0x0b, (byte) 0x0d, (byte) 0x00 };
		byte[] result = ByteUtils.parseBytes(data, 0, data.length, LittleEndian);
		assertThat(
				"Result bytes reversed", Arrays
						.equals(result,
								new byte[] { (byte) 0x00, (byte) 0x0d, (byte) 0x0b, (byte) 0x09,
										(byte) 0x07, (byte) 0x05, (byte) 0x03, (byte) 0x01 }),
				equalTo(true));
	}

	@Test
	public void parseBytes_LittleEndian_slice() {
		byte[] data = new byte[] { (byte) 0x01, (byte) 0x03, (byte) 0x05, (byte) 0x07, (byte) 0x09,
				(byte) 0x0b, (byte) 0x0d, (byte) 0x00 };
		byte[] result = ByteUtils.parseBytes(data, 3, 4, LittleEndian);
		assertThat("Result bytes reversed",
				Arrays.equals(result, new byte[] { (byte) 0x0d, (byte) 0x0b, (byte) 0x09, (byte) 0x07 }),
				equalTo(true));
	}

	@Test
	public void parseAsciiString() {
		byte[] data = new byte[] { (byte) 0x31, (byte) 0x30, (byte) 0x32, (byte) 0x34 };
		BigDecimal dec = ByteUtils.parseDecimalCharacterString(data, 0, data.length, BigEndian,
				ByteUtils.ASCII);
		assertThat(dec, equalTo(new BigDecimal("1024")));
	}

	@Test
	public void parseAsciiString_LittleEndian() {
		byte[] data = new byte[] { (byte) 0x31, (byte) 0x30, (byte) 0x32, (byte) 0x34 };
		BigDecimal dec = ByteUtils.parseDecimalCharacterString(data, 0, data.length, LittleEndian,
				ByteUtils.ASCII);
		assertThat(dec, equalTo(new BigDecimal("4201")));
	}

}
