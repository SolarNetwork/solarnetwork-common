/* ==================================================================
 * CborUtilsTests.java - 24/09/2025 4:13:44 pm
 *
 * Copyright 2025 SolarNetwork.net Dev Team
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

package net.solarnetwork.codec.jackson.test;

import static net.solarnetwork.util.ByteUtils.objectArray;
import static org.assertj.core.api.BDDAssertions.then;
import java.io.IOException;
import java.math.BigInteger;
import org.junit.Test;
import net.solarnetwork.codec.jackson.CborUtils;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.cbor.CBORMapper;

/**
 * Test cases for the {@link CborUtils} class.
 *
 * @author matt
 * @version 1.0
 */
public class CborUtilsTests {

	// see https://github.com/FasterXML/jackson-dataformats-binary/issues/431
	@Test
	public void encodeNegativeBigInteger() throws IOException {
		// GIVEN
		ObjectMapper mapper = CBORMapper.builder(CborUtils.cborFactory()).build();

		// WHEN
		Byte[] cbor = objectArray(mapper.writeValueAsBytes(BigInteger.ONE.negate()));

		// THEN
		then(cbor).as("CBOR -1 BigInteger")
				.containsExactly(objectArray(new byte[] { (byte) 0xC3, (byte) 0x41, (byte) 0x0 }));
	}

	// see https://github.com/FasterXML/jackson-dataformats-binary/issues/431
	@Test
	public void decodeNegativeBigInteger() throws IOException {
		// GIVEN
		ObjectMapper mapper = CBORMapper.builder(CborUtils.cborFactory()).build();

		// WHEN
		BigInteger result = mapper.readValue(new byte[] { (byte) 0xC3, (byte) 0x41, (byte) 0x0 },
				BigInteger.class);

		// THEN
		then(result).as("CBOR -1 BigInteger").isEqualTo(BigInteger.ONE.negate());
	}

}
