/* ==================================================================
 * JsonNodeUtilsTests.java - 8/04/2017 7:13:33 AM
 * 
 * Copyright 2017 SolarNetwork.net Dev Team
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import org.junit.Test;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.solarnetwork.util.JsonNodeUtils;

/**
 * Test cases for the {@link JsonNodeUtils} class.
 * 
 * @author matt
 * @version 1.0
 */
public class JsonNodeUtilsTests {

	private JsonNode parseJsonResource(String resource) {
		try {
			return new ObjectMapper().readTree(getClass().getResourceAsStream(resource));
		} catch ( IOException e ) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void parseBigDecimal() {
		JsonNode node = parseJsonResource("test-1.json");
		assertEquals("Parsed BigDecimal", new BigDecimal("-41.123456"),
				JsonNodeUtils.parseBigDecimalAttribute(node, "lat"));
	}

	@Test
	public void parseBigDecimalNullNode() {
		assertNull("Null node", JsonNodeUtils.parseBigDecimalAttribute(null, "lat"));
	}

	@Test
	public void parseBigDecimalNullValue() {
		JsonNode node = parseJsonResource("test-1.json");
		assertNull("Null attribute", JsonNodeUtils.parseBigDecimalAttribute(node, "no"));
	}

	@Test
	public void parseBigDecimalMissingValue() {
		JsonNode node = parseJsonResource("test-1.json");
		assertNull("Missing attribute", JsonNodeUtils.parseBigDecimalAttribute(node, "does_not_exist"));
	}

	@Test
	public void parseBigDecimalMalformedValue() {
		JsonNode node = parseJsonResource("test-1.json");
		assertNull("Malformed attribute", JsonNodeUtils.parseBigDecimalAttribute(node, "s"));
	}

	@Test
	public void parseInteger() {
		JsonNode node = parseJsonResource("test-1.json");
		assertEquals("Parsed Integer", Integer.valueOf(123),
				JsonNodeUtils.parseIntegerAttribute(node, "i"));
	}

	@Test
	public void parseIntegerNullNode() {
		assertNull("Null node", JsonNodeUtils.parseIntegerAttribute(null, "i"));
	}

	@Test
	public void parseIntegerNullValue() {
		JsonNode node = parseJsonResource("test-1.json");
		assertNull("Null attribute", JsonNodeUtils.parseIntegerAttribute(node, "no"));
	}

	@Test
	public void parseIntegerMissingValue() {
		JsonNode node = parseJsonResource("test-1.json");
		assertNull("Missing attribute", JsonNodeUtils.parseIntegerAttribute(node, "does_not_exist"));
	}

	@Test
	public void parseIntegerMalformedValue() {
		JsonNode node = parseJsonResource("test-1.json");
		assertNull("Malformed attribute", JsonNodeUtils.parseIntegerAttribute(node, "s"));
	}

	@Test
	public void parseIntegerStringValue() {
		JsonNode node = parseJsonResource("test-1.json");
		assertEquals("Integer string attribute", Integer.valueOf(456),
				JsonNodeUtils.parseIntegerAttribute(node, "is"));
	}

	@Test
	public void parseLong() {
		JsonNode node = parseJsonResource("test-1.json");
		assertEquals("Parsed Long", Long.valueOf(948457394876394876L),
				JsonNodeUtils.parseLongAttribute(node, "l"));
	}

	@Test
	public void parseLongNullNode() {
		assertNull("Null node", JsonNodeUtils.parseLongAttribute(null, "l"));
	}

	@Test
	public void parseLongNullValue() {
		JsonNode node = parseJsonResource("test-1.json");
		assertNull("Null attribute", JsonNodeUtils.parseLongAttribute(node, "no"));
	}

	@Test
	public void parseLongMissingValue() {
		JsonNode node = parseJsonResource("test-1.json");
		assertNull("Missing attribute", JsonNodeUtils.parseLongAttribute(node, "does_not_exist"));
	}

	@Test
	public void parseLongMalformedValue() {
		JsonNode node = parseJsonResource("test-1.json");
		assertNull("Malformed attribute", JsonNodeUtils.parseLongAttribute(node, "s"));
	}

	@Test
	public void parseLongStringValue() {
		JsonNode node = parseJsonResource("test-1.json");
		assertEquals("Long string attribute", Long.valueOf(993729384798127974L),
				JsonNodeUtils.parseLongAttribute(node, "ls"));
	}

	private SimpleDateFormat tsDateFormat() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss'Z'");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		return sdf;
	}

	@Test
	public void parseDate() throws ParseException {
		SimpleDateFormat sdf = tsDateFormat();
		JsonNode node = parseJsonResource("test-1.json");
		assertEquals("Parsed Date", sdf.parse("2017-04-08 12:00:00Z"),
				JsonNodeUtils.parseDateAttribute(node, "ts", sdf));
	}

	@Test
	public void parseDateNullNode() {
		SimpleDateFormat sdf = tsDateFormat();
		assertNull("Null node", JsonNodeUtils.parseDateAttribute(null, "ts", sdf));
	}

	@Test
	public void parseDateNullValue() {
		SimpleDateFormat sdf = tsDateFormat();
		JsonNode node = parseJsonResource("test-1.json");
		assertNull("Null attribute", JsonNodeUtils.parseDateAttribute(node, "no", sdf));
	}

	@Test
	public void parseDateMissingValue() {
		SimpleDateFormat sdf = tsDateFormat();
		JsonNode node = parseJsonResource("test-1.json");
		assertNull("Missing attribute", JsonNodeUtils.parseDateAttribute(node, "does_not_exist", sdf));
	}

	@Test
	public void parseDateMalformedValue() {
		SimpleDateFormat sdf = tsDateFormat();
		JsonNode node = parseJsonResource("test-1.json");
		assertNull("Malformed attribute", JsonNodeUtils.parseDateAttribute(node, "s", sdf));
	}

	@Test(expected = NullPointerException.class)
	public void parseDateNullDateFormat() {
		JsonNode node = parseJsonResource("test-1.json");
		JsonNodeUtils.parseDateAttribute(node, "s", null);
	}

	@Test
	public void parseString() {
		JsonNode node = parseJsonResource("test-1.json");
		assertEquals("Parsed String", "Hello", JsonNodeUtils.parseStringAttribute(node, "s"));
	}

	@Test
	public void parseStringNullNode() {
		assertNull("Null node", JsonNodeUtils.parseStringAttribute(null, "s"));
	}

	@Test
	public void parseStringNullValue() {
		JsonNode node = parseJsonResource("test-1.json");
		assertNull("Null attribute", JsonNodeUtils.parseStringAttribute(node, "no"));
	}

	@Test
	public void parseStringMissingValue() {
		JsonNode node = parseJsonResource("test-1.json");
		assertNull("Missing attribute", JsonNodeUtils.parseStringAttribute(node, "does_not_exist"));
	}

	@Test
	public void parseStringDecimalValue() {
		JsonNode node = parseJsonResource("test-1.json");
		assertEquals("Malformed attribute", "123.456", JsonNodeUtils.parseStringAttribute(node, "d"));
	}

}
