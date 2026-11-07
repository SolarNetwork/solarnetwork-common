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
import static org.assertj.core.api.BDDAssertions.thenExceptionOfType;
import static org.assertj.core.api.BDDAssertions.thenIllegalArgumentException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.hamcrest.Matchers;
import org.junit.Test;
import net.solarnetwork.domain.BitDataType;
import net.solarnetwork.util.ByteUtils;

/**
 * Test cases for the {@link ByteUtils} class.
 * 
 * @author matt
 * @version 1.2
 */
public class ByteUtilsTests {

	@Test
	public void encodeHexString_null() {
		byte[] b = new byte[0];
		String r = ByteUtils.encodeHexString(b, 0, b.length, false);
		assertThat("Empty range encodes empty string", r, equalTo(""));
	}

	@Test
	public void encodeHexString_basic() {
		byte[] b = new byte[] { (byte) 0x11, (byte) 0x99, (byte) 0xFF };
		String r = ByteUtils.encodeHexString(b, 0, b.length, false);
		assertThat("Bytes encode to hex string", r, equalTo("1199FF"));
	}

	@Test
	public void encodeHexString_basicLowerCase() {
		byte[] b = new byte[] { (byte) 0x11, (byte) 0x99, (byte) 0xFF };
		String r = ByteUtils.encodeHexString(b, 0, b.length, false, true);
		assertThat("Bytes encode to hex string", r, equalTo("1199ff"));
	}

	@Test
	public void encodeHexString_basicWithSpace() {
		byte[] b = new byte[] { (byte) 0x11, (byte) 0x99, (byte) 0xFF };
		String r = ByteUtils.encodeHexString(b, 0, b.length, true);
		assertThat("Bytes encode to hex string", r, equalTo("11 99 FF"));
	}

	@Test
	public void encodeHexString_subset() {
		byte[] b = new byte[] { (byte) 0x11, (byte) 0x99, (byte) 0xFF };
		String r = ByteUtils.encodeHexString(b, 1, b.length, false);
		assertThat("Bytes subrange encode to hex string", r, equalTo("99FF"));
	}

	@Test
	public void encodeHexString_subsetWithSpace() {
		byte[] b = new byte[] { (byte) 0x11, (byte) 0x99, (byte) 0xFF };
		String r = ByteUtils.encodeHexString(b, 1, b.length, true);
		assertThat("Bytes subrange encode to hex string", r, equalTo("99 FF"));
	}

	@Test
	public void encodeHexString_toIndexPastEnd() {
		byte[] b = new byte[] { (byte) 0x11, (byte) 0x99, (byte) 0xFF };
		String r = ByteUtils.encodeHexString(b, 1, 5, true);
		assertThat("Bytes subrange up to end of data encode to hex string", r, equalTo("99 FF"));
	}

	@Test
	public void encodeHexString_toIndexMax() {
		byte[] b = new byte[] { (byte) 0x11, (byte) 0x99, (byte) 0xFF };
		String r = ByteUtils.encodeHexString(b, 0, Integer.MAX_VALUE, false);
		assertThat("Bytes up to end of data encode to hex string", r, equalTo("1199FF"));
	}

	@Test
	public void decodeHexString_basic() {
		// GIVEN
		String s = "1F2B01";

		// WHEN
		byte[] b = ByteUtils.decodeHexString(s);

		// THEN
		assertThat("Decoded bytes",
				Arrays.equals(b, new byte[] { (byte) 0x1F, (byte) 0x2B, (byte) 0x01 }), equalTo(true));
	}

	@Test
	public void decodeHexString_oddLength() {
		// GIVEN
		String s = "F2B01";

		// WHEN
		byte[] b = ByteUtils.decodeHexString(s);

		// THEN
		assertThat("Decoded bytes",
				Arrays.equals(b, new byte[] { (byte) 0x0F, (byte) 0x2B, (byte) 0x01 }), equalTo(true));
	}

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
	public void encodeInt8_null() {
		byte[] dest = new byte[] { (byte) 0x7F };
		ByteUtils.encodeInt8(null, dest, 0);
		assertThat("Null encoded as zero", dest, equalTo(new byte[] { (byte) 0 }));
	}

	@Test
	public void encodeUnsignedInt8() {
		byte[] dest = new byte[1];
		ByteUtils.encodeUnsignedInt8(200, dest, 0);
		assertThat("Encoded value", dest, equalTo(new byte[] { (byte) 0xC8 }));
	}

	@Test
	public void encodeUnsignedInt8_null() {
		byte[] dest = new byte[] { (byte) 0x7F };
		ByteUtils.encodeUnsignedInt8(null, dest, 0);
		assertThat("Null encoded as zero", dest, equalTo(new byte[] { (byte) 0 }));
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
	public void parseBytes_truncated() {
		byte[] data = new byte[] { (byte) 0x01, (byte) 0x03, (byte) 0x05, (byte) 0x07 };
		byte[] result = ByteUtils.parseBytes(data, 2, 4, BigEndian);
		assertThat("Result truncated to available bytes", result,
				equalTo(new byte[] { (byte) 0x05, (byte) 0x07 }));
	}

	@Test
	public void parseBytes_offsetPastEnd() {
		byte[] data = new byte[] { (byte) 0x01, (byte) 0x03, (byte) 0x05, (byte) 0x07 };
		byte[] result = ByteUtils.parseBytes(data, 6, 2, BigEndian);
		assertThat("Offset past end of data returns empty result", result, equalTo(new byte[0]));
	}

	@Test
	public void parseBytes_LittleEndian_offsetPastEnd() {
		byte[] data = new byte[] { (byte) 0x01, (byte) 0x03, (byte) 0x05, (byte) 0x07 };
		byte[] result = ByteUtils.parseBytes(data, 6, 2, LittleEndian);
		assertThat("Offset past end of data returns empty result", result, equalTo(new byte[0]));
	}

	@Test
	public void parseBytes_lengthOverflow() {
		byte[] data = new byte[] { (byte) 0x01, (byte) 0x03, (byte) 0x05, (byte) 0x07 };
		byte[] result = ByteUtils.parseBytes(data, 2, Integer.MAX_VALUE, BigEndian);
		assertThat("Result truncated to available bytes", result,
				equalTo(new byte[] { (byte) 0x05, (byte) 0x07 }));
	}

	@Test
	public void parseUnsignedInteger_offsetPastEnd() {
		byte[] data = new byte[] { (byte) 0x01, (byte) 0x03, (byte) 0x05, (byte) 0x07 };
		BigInteger result = ByteUtils.parseUnsignedInteger(data, 6, 2, BigEndian);
		assertThat("Offset past end of data parses as zero", result, equalTo(BigInteger.ZERO));
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

	@Test
	public void parseAsciiString_padded() {
		byte[] data = "12\0\0".getBytes(StandardCharsets.US_ASCII);
		assertThat("Trailing NUL padding ignored",
				ByteUtils.parseDecimalCharacterString(data, 0, data.length, BigEndian, ByteUtils.ASCII),
				equalTo(new BigDecimal("12")));

		data = " 12.5 ".getBytes(StandardCharsets.US_ASCII);
		assertThat("Whitespace padding ignored",
				ByteUtils.parseDecimalCharacterString(data, 0, data.length, BigEndian, ByteUtils.ASCII),
				equalTo(new BigDecimal("12.5")));

		data = "21\0\0".getBytes(StandardCharsets.US_ASCII);
		assertThat("NUL padding leading after little-endian reversal ignored", ByteUtils
				.parseDecimalCharacterString(data, 0, data.length, LittleEndian, ByteUtils.ASCII),
				equalTo(new BigDecimal("12")));
	}

	@Test
	public void parseAsciiString_invalid() {
		final byte[] text = "abc".getBytes(StandardCharsets.US_ASCII);
		final byte[] nul = new byte[4];
		// @formatter:off
		thenExceptionOfType(NumberFormatException.class)
			.as("Non-numeric text throws")
			.isThrownBy(() -> ByteUtils.parseDecimalCharacterString(text, 0, text.length,
					BigEndian, ByteUtils.ASCII))
			;
		thenExceptionOfType(NumberFormatException.class)
			.as("Only padding throws")
			.isThrownBy(() -> ByteUtils.parseDecimalCharacterString(nul, 0, nul.length,
					BigEndian, ByteUtils.ASCII))
			;
		// @formatter:on
	}

	@Test
	public void testObjectArray() {
		byte[] data = new byte[] { 1, 2, 3, 4, 5, 0 };
		Byte[] result = ByteUtils.objectArray(data);
		assertThat("Bytes turned to objects", result,
				arrayContaining((byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5, (byte) 0));
	}

	@Test
	public void testByteArray() {
		Byte[] data = new Byte[] { 1, 2, 3, 4, 5, 0 };
		byte[] result = ByteUtils.byteArray(data);
		for ( int i = 0; i < data.length; i++ ) {
			assertThat("Byte turned to primitive " + i, result[i], equalTo(data[i]));
		}
	}

	@Test
	public void testHumanReadableByteCount() {
		// GIVEN
		final Long[] inputs = new Long[] { 0L, 27L, 999L, 1000L, 1023L, 1024L, 1728L, 110592L, 7077888L,
				452984832L, 28991029248L, 1855425871872L, 9223372036854775807L };

		// WHEN
		final String[] result = Arrays.stream(inputs).map(n -> ByteUtils.humanReadableByteCount(n))
				.toArray(String[]::new);

		// THEN
		assertThat("Counts converted to formatted strings", result,
				arrayContaining("0 B", "27 B", "999 B", "1000 B", "1023 B", "1.0 KiB", "1.7 KiB",
						"108.0 KiB", "6.8 MiB", "432.0 MiB", "27.0 GiB", "1.7 TiB", "8.0 EiB"));
	}

	/** Test data with a leading pad byte so parsing starts at offset 1. */
	private static final byte[] PARSE_DATA = new byte[] { (byte) 0x00, (byte) 0x81, (byte) 0x02,
			(byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08 };

	private static ByteBuffer parseDataBuffer(ByteOrder order) {
		return ByteBuffer.wrap(PARSE_DATA, 1, PARSE_DATA.length - 1).slice().order(order);
	}

	@Test
	public void encodeHexUpperCase() {
		char[] dest = new char[] { 'x', 'x', 'x' };
		char[] result = ByteUtils.encodeHexUpperCase((byte) 0xAB, dest, 1);
		assertThat("Destination returned", result == dest, equalTo(true));
		assertThat("Upper case hex encoded at index", new String(dest), equalTo("xAB"));
	}

	@Test
	public void encodeHexLowerCase() {
		char[] dest = new char[] { 'x', 'x', 'x' };
		char[] result = ByteUtils.encodeHexLowerCase((byte) 0xAB, dest, 0);
		assertThat("Destination returned", result == dest, equalTo(true));
		assertThat("Lower case hex encoded at index", new String(dest), equalTo("abx"));
	}

	@Test
	public void encodeHexString_outOfRange() {
		byte[] b = new byte[] { (byte) 0x11, (byte) 0x99, (byte) 0xFF };
		assertThat("Null data", ByteUtils.encodeHexString(null, 0, 1, false), equalTo(""));
		assertThat("Negative from", ByteUtils.encodeHexString(b, -1, 2, false), equalTo(""));
		assertThat("From past end", ByteUtils.encodeHexString(b, 3, 4, false), equalTo(""));
		assertThat("Negative to", ByteUtils.encodeHexString(b, 0, -1, false), equalTo(""));
		assertThat("To equal to from", ByteUtils.encodeHexString(b, 1, 1, false), equalTo(""));
		assertThat("To before from", ByteUtils.encodeHexString(b, 2, 1, false), equalTo(""));
	}

	@Test
	public void decodeHexString_null() {
		assertThat("Null decodes to empty bytes", ByteUtils.decodeHexString(null), equalTo(new byte[0]));
	}

	@Test
	public void decodeHexPadStart_nullOrEmpty() {
		assertThat("Null decodes to empty bytes", ByteUtils.decodeHexPadStart(null),
				equalTo(new byte[0]));
		assertThat("Empty decodes to empty bytes", ByteUtils.decodeHexPadStart(new char[0]),
				equalTo(new byte[0]));
	}

	@Test
	public void decodeHexString_invalid() {
		// @formatter:off
		thenIllegalArgumentException()
			.as("Non-hex characters throw")
			.isThrownBy(() -> ByteUtils.decodeHexString("zz"))
			.withMessageContaining("index 0")
			;
		thenIllegalArgumentException()
			.as("Non-hex second character of a pair throws")
			.isThrownBy(() -> ByteUtils.decodeHexString("1g"))
			.withMessageContaining("index 1")
			;
		thenIllegalArgumentException()
			.as("Non-hex odd-length first character throws")
			.isThrownBy(() -> ByteUtils.decodeHexString("g12"))
			.withMessageContaining("index 0")
			;
		thenIllegalArgumentException()
			.as("Whitespace throws")
			.isThrownBy(() -> ByteUtils.decodeHexPadStart("12 34".toCharArray()))
			.withMessageContaining("index 2")
			;
		// @formatter:on
	}

	@Test
	public void encode_null() {
		byte[] dest = new byte[8];
		Arrays.fill(dest, (byte) 0x7F);
		ByteUtils.encodeInt16(null, dest, 0, BigEndian);
		ByteUtils.encodeUnsignedInt16(null, dest, 2, BigEndian);
		ByteUtils.encodeInt32(null, dest, 4, BigEndian);
		assertThat("Null 16 and 32 bit values encoded as zero", dest, equalTo(new byte[8]));

		Arrays.fill(dest, (byte) 0x7F);
		ByteUtils.encodeUnsignedInt32(null, dest, 0, BigEndian);
		assertThat("Null unsigned 32 bit value encoded as zero", Arrays.copyOf(dest, 4),
				equalTo(new byte[4]));

		Arrays.fill(dest, (byte) 0x7F);
		ByteUtils.encodeInt64((Number) null, dest, 0, BigEndian);
		assertThat("Null 64 bit value encoded as zero", dest, equalTo(new byte[8]));

		Arrays.fill(dest, (byte) 0x7F);
		ByteUtils.encodeUnsignedInt64((Number) null, dest, 0, BigEndian);
		assertThat("Null unsigned 64 bit value encoded as zero", dest, equalTo(new byte[8]));
	}

	@Test
	public void encodeInt64_number() {
		byte[] dest = new byte[8];
		ByteUtils.encodeInt64((Number) 0x0102030405060708L, dest, 0, LittleEndian);
		assertThat("Number encoded", dest, equalTo(new byte[] { (byte) 0x08, (byte) 0x07, (byte) 0x06,
				(byte) 0x05, (byte) 0x04, (byte) 0x03, (byte) 0x02, (byte) 0x01 }));
	}

	@Test
	public void parseUnsignedInt64_zero() {
		assertThat("All zero bytes", ByteUtils.parseUnsignedInt64((byte) 0, (byte) 0, (byte) 0, (byte) 0,
				(byte) 0, (byte) 0, (byte) 0, (byte) 0), equalTo(BigInteger.ZERO));
		assertThat("Only lowest byte", ByteUtils.parseUnsignedInt64((byte) 0, (byte) 0, (byte) 0,
				(byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0xFF), equalTo(BigInteger.valueOf(255)));
	}

	@Test
	public void parseFloat64NaN() {
		long bits = Double.doubleToRawLongBits(Double.NaN);
		Double result = ByteUtils.parseFloat64((byte) (bits >>> 56), (byte) (bits >>> 48),
				(byte) (bits >>> 40), (byte) (bits >>> 32), (byte) (bits >>> 24), (byte) (bits >>> 16),
				(byte) (bits >>> 8), (byte) bits);
		assertThat("Double value is NaN", result, nullValue());
	}

	@Test
	public void parseBytes_null() {
		assertThat("Null data parses to empty bytes", ByteUtils.parseBytes(null, 0, 2, BigEndian),
				equalTo(new byte[0]));
	}

	@Test
	public void parseInteger() {
		byte[] data = new byte[] { (byte) 0xFF, (byte) 0xFE };
		assertThat("Signed big endian", ByteUtils.parseInteger(data, 0, 2, BigEndian),
				equalTo(BigInteger.valueOf(-2)));
		assertThat("Signed little endian", ByteUtils.parseInteger(data, 0, 2, LittleEndian),
				equalTo(BigInteger.valueOf((short) 0xFEFF)));
	}

	@Test
	public void parseInteger_offsetPastEnd() {
		byte[] data = new byte[] { (byte) 0xFF, (byte) 0xFE };
		assertThat("No bytes parse as zero", ByteUtils.parseInteger(data, 4, 2, BigEndian),
				equalTo(BigInteger.ZERO));
	}

	@Test
	public void parseNumber_nullData() {
		assertThat("Null data", ByteUtils.parseNumber(BitDataType.Int16, null, 0, BigEndian),
				nullValue());
	}

	@Test
	public void parseNumber_bit() {
		assertThat("Zero bit", ByteUtils.parseNumber(BitDataType.Bit, PARSE_DATA, 0, BigEndian),
				equalTo(0));
		assertThat("Non-zero bit", ByteUtils.parseNumber(BitDataType.Bit, PARSE_DATA, 1, BigEndian),
				equalTo(1));
		assertThat("Non-zero boolean",
				ByteUtils.parseNumber(BitDataType.Boolean, PARSE_DATA, 1, BigEndian), equalTo(1));
	}

	@Test
	public void parseNumber_8bit() {
		assertThat("Int8", ByteUtils.parseNumber(BitDataType.Int8, PARSE_DATA, 1, BigEndian),
				equalTo((byte) 0x81));
		assertThat("UInt8", ByteUtils.parseNumber(BitDataType.UInt8, PARSE_DATA, 1, BigEndian),
				equalTo((short) 0x81));
	}

	@Test
	public void parseNumber_16bit() {
		short be = parseDataBuffer(ByteOrder.BIG_ENDIAN).getShort();
		short le = parseDataBuffer(ByteOrder.LITTLE_ENDIAN).getShort();
		assertThat("Int16 big endian",
				ByteUtils.parseNumber(BitDataType.Int16, PARSE_DATA, 1, BigEndian), equalTo(be));
		assertThat("Int16 little endian",
				ByteUtils.parseNumber(BitDataType.Int16, PARSE_DATA, 1, LittleEndian), equalTo(le));
		assertThat("UInt16 big endian",
				ByteUtils.parseNumber(BitDataType.UInt16, PARSE_DATA, 1, BigEndian),
				equalTo(Short.toUnsignedInt(be)));
		assertThat("UInt16 little endian",
				ByteUtils.parseNumber(BitDataType.UInt16, PARSE_DATA, 1, LittleEndian),
				equalTo(Short.toUnsignedInt(le)));
	}

	@Test
	public void parseNumber_32bit() {
		int be = parseDataBuffer(ByteOrder.BIG_ENDIAN).getInt();
		int le = parseDataBuffer(ByteOrder.LITTLE_ENDIAN).getInt();
		assertThat("Int32 big endian",
				ByteUtils.parseNumber(BitDataType.Int32, PARSE_DATA, 1, BigEndian), equalTo(be));
		assertThat("Int32 little endian",
				ByteUtils.parseNumber(BitDataType.Int32, PARSE_DATA, 1, LittleEndian), equalTo(le));
		assertThat("UInt32 big endian",
				ByteUtils.parseNumber(BitDataType.UInt32, PARSE_DATA, 1, BigEndian),
				equalTo(Integer.toUnsignedLong(be)));
		assertThat("UInt32 little endian",
				ByteUtils.parseNumber(BitDataType.UInt32, PARSE_DATA, 1, LittleEndian),
				equalTo(Integer.toUnsignedLong(le)));
	}

	@Test
	public void parseNumber_64bit() {
		long be = parseDataBuffer(ByteOrder.BIG_ENDIAN).getLong();
		long le = parseDataBuffer(ByteOrder.LITTLE_ENDIAN).getLong();
		assertThat("Int64 big endian",
				ByteUtils.parseNumber(BitDataType.Int64, PARSE_DATA, 1, BigEndian), equalTo(be));
		assertThat("Int64 little endian",
				ByteUtils.parseNumber(BitDataType.Int64, PARSE_DATA, 1, LittleEndian), equalTo(le));
		assertThat("UInt64 big endian",
				ByteUtils.parseNumber(BitDataType.UInt64, PARSE_DATA, 1, BigEndian),
				equalTo(new BigInteger(Long.toUnsignedString(be))));
		assertThat("UInt64 little endian",
				ByteUtils.parseNumber(BitDataType.UInt64, PARSE_DATA, 1, LittleEndian),
				equalTo(new BigInteger(Long.toUnsignedString(le))));
	}

	@Test
	public void parseNumber_float() {
		byte[] data = new byte[13];
		ByteBuffer.wrap(data, 1, 12).putFloat(1.5f).putDouble(-2.25);
		assertThat("Float32 big endian", ByteUtils.parseNumber(BitDataType.Float32, data, 1, BigEndian),
				equalTo(1.5f));
		assertThat("Float64 big endian", ByteUtils.parseNumber(BitDataType.Float64, data, 5, BigEndian),
				equalTo(-2.25));

		ByteBuffer.wrap(data, 1, 12).order(ByteOrder.LITTLE_ENDIAN).putFloat(1.5f).putDouble(-2.25);
		assertThat("Float32 little endian",
				ByteUtils.parseNumber(BitDataType.Float32, data, 1, LittleEndian), equalTo(1.5f));
		assertThat("Float64 little endian",
				ByteUtils.parseNumber(BitDataType.Float64, data, 5, LittleEndian), equalTo(-2.25));
	}

	@Test
	public void parseNumber_variableLengthIntegers() {
		short be = parseDataBuffer(ByteOrder.BIG_ENDIAN).getShort();
		assertThat("Integer", ByteUtils.parseNumber(BitDataType.Integer, PARSE_DATA, 1, 2, BigEndian),
				equalTo(BigInteger.valueOf(be)));
		assertThat("UnsignedInteger",
				ByteUtils.parseNumber(BitDataType.UnsignedInteger, PARSE_DATA, 1, 2, BigEndian),
				equalTo(BigInteger.valueOf(Short.toUnsignedInt(be))));
		assertThat("Bytes", ByteUtils.parseNumber(BitDataType.Bytes, PARSE_DATA, 1, 2, BigEndian),
				equalTo(BigInteger.valueOf(Short.toUnsignedInt(be))));
	}

	@Test
	public void parseNumber_strings() {
		byte[] data = "x1024".getBytes(StandardCharsets.US_ASCII);
		assertThat("ASCII", ByteUtils.parseNumber(BitDataType.StringAscii, data, 1, 4, BigEndian),
				equalTo(new BigDecimal("1024")));
		assertThat("UTF-8", ByteUtils.parseNumber(BitDataType.StringUtf8, data, 1, 4, BigEndian),
				equalTo(new BigDecimal("1024")));
	}

	@Test
	public void parseNumber_notEnoughData() {
		final int last = PARSE_DATA.length - 1;
		assertThat("Bit",
				ByteUtils.parseNumber(BitDataType.Bit, PARSE_DATA, PARSE_DATA.length, BigEndian),
				nullValue());
		assertThat("Int8",
				ByteUtils.parseNumber(BitDataType.Int8, PARSE_DATA, PARSE_DATA.length, BigEndian),
				nullValue());
		assertThat("UInt8",
				ByteUtils.parseNumber(BitDataType.UInt8, PARSE_DATA, PARSE_DATA.length, BigEndian),
				nullValue());
		assertThat("Int16", ByteUtils.parseNumber(BitDataType.Int16, PARSE_DATA, last, BigEndian),
				nullValue());
		assertThat("UInt16", ByteUtils.parseNumber(BitDataType.UInt16, PARSE_DATA, last, BigEndian),
				nullValue());
		assertThat("Int32", ByteUtils.parseNumber(BitDataType.Int32, PARSE_DATA, last - 2, BigEndian),
				nullValue());
		assertThat("UInt32", ByteUtils.parseNumber(BitDataType.UInt32, PARSE_DATA, last - 2, BigEndian),
				nullValue());
		assertThat("Float32",
				ByteUtils.parseNumber(BitDataType.Float32, PARSE_DATA, last - 2, BigEndian),
				nullValue());
		assertThat("Int64", ByteUtils.parseNumber(BitDataType.Int64, PARSE_DATA, 2, BigEndian),
				nullValue());
		assertThat("UInt64", ByteUtils.parseNumber(BitDataType.UInt64, PARSE_DATA, 2, BigEndian),
				nullValue());
		assertThat("Float64", ByteUtils.parseNumber(BitDataType.Float64, PARSE_DATA, 2, BigEndian),
				nullValue());
		for ( BitDataType t : new BitDataType[] { BitDataType.Integer, BitDataType.UnsignedInteger,
				BitDataType.Bytes, BitDataType.StringAscii, BitDataType.StringUtf8 } ) {
			assertThat(t.name(), ByteUtils.parseNumber(t, PARSE_DATA, last, 2, BigEndian), nullValue());
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void parseNumber_fixedLength_variableLengthType() {
		ByteUtils.parseNumber(BitDataType.Integer, PARSE_DATA, 0, BigEndian);
	}

	@Test
	public void objectArray_null() {
		assertThat("Null input", ByteUtils.objectArray(null), nullValue());
	}

	@Test
	public void byteArray_null() {
		assertThat("Null input", ByteUtils.byteArray(null), nullValue());
	}

	@Test
	public void byteArray_nullElements() {
		Byte[] data = new Byte[] { 1, null, 3 };
		assertThat("Default null value", ByteUtils.byteArray(data), equalTo(new byte[] { 1, 0, 3 }));
		assertThat("Explicit null value", ByteUtils.byteArray(data, (byte) -1),
				equalTo(new byte[] { 1, -1, 3 }));
	}

	@Test
	public void humanReadableByteCount_negative() {
		assertThat("Negative small count", ByteUtils.humanReadableByteCount(-27), equalTo("-27 B"));
		assertThat("Minimum count", ByteUtils.humanReadableByteCount(Long.MIN_VALUE),
				equalTo(String.format("%.1f %ciB", -8191 / 1024.0, 'E')));
	}

}
