/* ==================================================================
 * JsonUtils_CborTests.java - 25/07/2025 7:41:13 am
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

package net.solarnetwork.codec.test;

import java.io.IOException;
import java.util.HexFormat;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import net.solarnetwork.codec.JsonUtils;

/**
 * Test cases for {@link JsonUtils} using CBOR.
 *
 * @author matt
 * @version 1.0
 */
public class JsonUtils_CborTests {

	private static final Logger log = LoggerFactory.getLogger(JsonUtils_CborTests.class);

	private ObjectMapper mapper;

	private ObjectMapper createObjectMapper(JsonFactory jsonFactory) {
		ObjectMapper m = new ObjectMapper(jsonFactory);
		return m;
	}

	@Before
	public void setup() {
		mapper = createObjectMapper(new CBORFactory());
	}

	@Test(expected = NumberFormatException.class)
	public void parseDecimal_Infinity() throws IOException {
		// GIVEN
		byte[] cbor = mapper.writeValueAsBytes(Double.POSITIVE_INFINITY);
		String cborHex = HexFormat.of().formatHex(cbor);
		log.debug("Infinity CBOR: {}", cborHex);
		try (JsonParser p = mapper.createParser(cbor)) {

			// THEN
			JsonUtils.parseDecimal(p);
		}
	}

}
