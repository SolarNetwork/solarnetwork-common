/* ==================================================================
 * BasicStreamDatumArrayDeserializerTests.java - 4/06/2021 6:06:37 PM
 *
 * Copyright 2021 SolarNetwork.net Dev Team
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

import static java.time.Instant.ofEpochMilli;
import static net.solarnetwork.util.NumberUtils.decimalArray;
import static org.assertj.core.api.BDDAssertions.catchThrowableOfType;
import static org.assertj.core.api.BDDAssertions.from;
import static org.assertj.core.api.BDDAssertions.then;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import net.solarnetwork.codec.jackson.BasicStreamDatumArrayDeserializer;
import net.solarnetwork.domain.datum.DatumProperties;
import net.solarnetwork.domain.datum.StreamDatum;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.exc.InvalidFormatException;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

/**
 * Test cases for the {@link BasicStreamDatumArrayDeserializer} class.
 *
 * @author matt
 * @version 1.0
 */
public class BasicStreamDatumArrayDeserializerTests {

	private ObjectMapper mapper;

	private ObjectMapper createObjectMapper() {
		SimpleModule mod = new SimpleModule("Test");
		mod.addDeserializer(StreamDatum.class, BasicStreamDatumArrayDeserializer.INSTANCE);
		return JsonMapper.builder().addModule(mod).build();
	}

	@Before
	public void setup() {
		mapper = createObjectMapper();
	}

	@Test
	public void deserialize_typical() throws IOException {
		// GIVEN
		final String json = "[1234567890,-39146522915747961,-8199130050457739548"
				+ ",[1.23,2.34],[3.45],[\"foo\",\"bar\"],[\"a\"]]";

		// WHEN
		StreamDatum d = mapper.readValue(json, StreamDatum.class);

		// THEN
		// @formatter:off
		then(d)
			.as("Datum parsed")
			.isNotNull()
			.as("Timestamp")
			.returns(ofEpochMilli(1234567890L), from(StreamDatum::getTimestamp))
			.as("Datum properties parsed")
			.extracting(StreamDatum::getProperties)
			.as("Instantaneous properties parsed")
			.returns(decimalArray("1.23", "2.34"), from(DatumProperties::getInstantaneous))
			.as("Accumualting properties parsed")
			.returns(decimalArray("3.45"), from(DatumProperties::getAccumulating))
			.as("Status properties parsed")
			.returns(new String[] {"foo", "bar"}, from(DatumProperties::getStatus))
			.as("Tags parsed")
			.returns(new String[] {"a"}, from(DatumProperties::getTags))
			;
		// @formatter:on
	}

	@Test
	public void deserialize_sparse() throws IOException {
		// GIVEN
		final String json = "[1234567890,-39146522915747961,-8199130050457739548"
				+ ",null,[3.45],null,null]";

		// WHEN
		StreamDatum d = mapper.readValue(json, StreamDatum.class);

		// THEN
		// @formatter:off
		then(d)
			.as("Datum parsed")
			.isNotNull()
			.as("Timestamp")
			.returns(ofEpochMilli(1234567890L), from(StreamDatum::getTimestamp))
			.as("Datum properties parsed")
			.extracting(StreamDatum::getProperties)
			.as("Instantaneous properties parsed")
			.returns(null, from(DatumProperties::getInstantaneous))
			.as("Accumualting properties parsed")
			.returns(decimalArray("3.45"), from(DatumProperties::getAccumulating))
			.as("Status properties parsed")
			.returns(null, from(DatumProperties::getStatus))
			.as("Tags parsed")
			.returns(null, from(DatumProperties::getTags))
			;
		// @formatter:on
	}

	@Test
	public void deserialize_stringNumber() throws IOException {
		// GIVEN
		final String json = "[1234567890,-39146522915747961,-8199130050457739548"
				+ ",null,[\"3.45\"],null,null]";

		// WHEN
		StreamDatum d = mapper.readValue(json, StreamDatum.class);

		// THEN
		// @formatter:off
		then(d)
			.as("Datum parsed")
			.isNotNull()
			.as("Datum properties parsed")
			.extracting(StreamDatum::getProperties)
			.as("Accumualting properties parsed")
			.returns(decimalArray("3.45"), from(DatumProperties::getAccumulating))
			;
		// @formatter:on
	}

	@Test
	public void deserialize_numberString() throws IOException {
		// GIVEN
		final String json = "[1234567890,-39146522915747961,-8199130050457739548"
				+ ",null,null,[123],null]";

		// WHEN
		StreamDatum d = mapper.readValue(json, StreamDatum.class);

		// THEN
		// @formatter:off
		then(d)
			.as("Datum parsed")
			.isNotNull()
			.as("Datum properties parsed")
			.extracting(StreamDatum::getProperties)
			.as("Status properties parsed")
			.returns(new String[] {"123"}, from(DatumProperties::getStatus))
			;
		// @formatter:on
	}

	@Test
	public void deserialize_arrayNullNumberValue() throws IOException {
		// GIVEN
		final String json = "[1234567890,-39146522915747961,-8199130050457739548"
				+ ",null,[123,null,456],null,null]";

		// WHEN
		StreamDatum d = mapper.readValue(json, StreamDatum.class);

		// THEN
		// @formatter:off
		then(d)
			.as("Datum parsed")
			.isNotNull()
			.as("Datum properties parsed")
			.extracting(StreamDatum::getProperties)
			.as("Accumulating properties parsed")
			.returns(decimalArray("123", null, "456"), from(DatumProperties::getAccumulating))
			;
		// @formatter:on
	}

	@Test
	public void deserialize_arrayNullStringValue() throws IOException {
		// GIVEN
		final String json = "[1234567890,-39146522915747961,-8199130050457739548"
				+ ",null,null,[\"one\",null,\"two\"],null]";

		// WHEN
		StreamDatum d = mapper.readValue(json, StreamDatum.class);

		// THEN
		// @formatter:off
		then(d)
			.as("Datum parsed")
			.isNotNull()
			.as("Datum properties parsed")
			.extracting(StreamDatum::getProperties)
			.as("Status properties parsed")
			.returns(new String[] { "one", null, "two" }, from(DatumProperties::getStatus))
			;
		// @formatter:on
	}

	@Test
	public void deserialize_notANumber() throws IOException {
		// GIVEN
		final String json = "[1234567890,-39146522915747961,-8199130050457739548"
				+ ",null,[\"howdy\"],null,null]";

		// WHEN
		catchThrowableOfType(InvalidFormatException.class, () -> {
			mapper.readValue(json, StreamDatum.class);
		});
	}

}
