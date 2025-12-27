/* ==================================================================
 * BasicStreamDatumArraySerializerTests.java - 4/06/2021 5:29:39 PM
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

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static net.solarnetwork.util.NumberUtils.decimalArray;
import static org.assertj.core.api.BDDAssertions.then;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;
import java.util.function.Function;
import org.junit.Before;
import org.junit.Test;
import net.solarnetwork.codec.jackson.BasicStreamDatumArraySerializer;
import net.solarnetwork.domain.datum.BasicStreamDatum;
import net.solarnetwork.domain.datum.DatumProperties;
import net.solarnetwork.domain.datum.StreamDatum;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

/**
 * Test cases for the {@link BasicStreamDatumArraySerializer} class.
 *
 * @author matt
 * @version 1.0
 */
public class BasicStreamDatumArraySerializerTests {

	private ObjectMapper mapper;

	private static Function<String, String> QUOTER = s -> {
		return "\"" + s + "\"";
	};

	private ObjectMapper createObjectMapper() {
		SimpleModule mod = new SimpleModule("Test");
		mod.addSerializer(StreamDatum.class, BasicStreamDatumArraySerializer.INSTANCE);
		return JsonMapper.builder().addModule(mod).build();
	}

	@Before
	public void setup() {
		mapper = createObjectMapper();
	}

	private void thenStreamDatumArray(String msg, String json, StreamDatum expected) {
		DatumProperties p = expected.getProperties();
		// @formatter:off
		then(json)
			.as("%s JSON generated", msg)
			.isNotNull()
			.as("%s JSON", msg)
			.isEqualTo((format("[%d,%d,%d,[%s],[%s],[%s],[%s]]",
				expected.getTimestamp().toEpochMilli(),
				expected.getStreamId().getMostSignificantBits(),
				expected.getStreamId().getLeastSignificantBits(),
				Arrays.stream(p.getInstantaneous()).map(Object::toString).collect(joining(",")),
				Arrays.stream(p.getAccumulating()).map(Object::toString).collect(joining(",")),
				Arrays.stream(p.getStatus()).map(QUOTER).collect(joining(",")),
				Arrays.stream(p.getTags()).map(QUOTER).collect(joining(","))
				)));
		// @formatter:on
	}

	@Test
	public void serialize_typical() throws IOException {
		// GIVEN
		DatumProperties p = new DatumProperties();
		p.setInstantaneous(decimalArray("1.23", "2.34"));
		p.setAccumulating(decimalArray("3.45"));
		p.setStatus(new String[] { "foo", "bar" });
		p.setTags(new String[] { "a", "b" });
		BasicStreamDatum d = new BasicStreamDatum(UUID.randomUUID(), Instant.now(), p);

		// WHEN
		String json = mapper.writeValueAsString(d);

		// THEN
		thenStreamDatumArray("Typical", json, d);
	}

	@Test
	public void serialize_sparse() throws IOException {
		// GIVEN
		DatumProperties p = new DatumProperties();
		p.setAccumulating(decimalArray("3.45"));
		BasicStreamDatum d = new BasicStreamDatum(UUID.randomUUID(), Instant.now(), p);

		// WHEN
		String json = mapper.writeValueAsString(d);

		// THEN
		// @formatter:off
		then(json)
			.as("Sparse JSON")
			.isEqualTo(format("[%d,%d,%d,null,[%s],null,null]",
				d.getTimestamp().toEpochMilli(),
				d.getStreamId().getMostSignificantBits(),
				d.getStreamId().getLeastSignificantBits(),
				Arrays.stream(p.getAccumulating()).map(Object::toString).collect(joining(","))
				));
		// @formatter:on
	}

	@Test
	public void serialize_arrayNullNumberValue() throws IOException {
		// GIVEN
		DatumProperties p = new DatumProperties();
		p.setAccumulating(decimalArray("1.23", null, "2.34"));
		BasicStreamDatum d = new BasicStreamDatum(UUID.randomUUID(), Instant.now(), p);

		// WHEN
		String json = mapper.writeValueAsString(d);

		// THEN
		// @formatter:off
		then(json)
			.as("Sparse JSON")
			.isEqualTo(format("[%d,%d,%d,null,[%s],null,null]",
				d.getTimestamp().toEpochMilli(),
				d.getStreamId().getMostSignificantBits(),
				d.getStreamId().getLeastSignificantBits(),
				Arrays.stream(p.getAccumulating()).map(e-> e != null ? e.toString() : "null")
					.collect(joining(","))
				));
		// @formatter:on
	}

	@Test
	public void serialize_arrayNullStringValue() throws IOException {
		// GIVEN
		DatumProperties p = new DatumProperties();
		p.setStatus(new String[] { "one", null, "two" });
		BasicStreamDatum d = new BasicStreamDatum(UUID.randomUUID(), Instant.now(), p);

		// WHEN
		String json = mapper.writeValueAsString(d);

		// THEN
		// @formatter:off
		then(json)
			.as("Sparse JSON")
			.isEqualTo(format("[%d,%d,%d,null,null,[%s],null]",
				d.getTimestamp().toEpochMilli(),
				d.getStreamId().getMostSignificantBits(),
				d.getStreamId().getLeastSignificantBits(),
				Arrays.stream(p.getStatus()).map(e-> e != null ? "\"" + e.toString() + "\"" : "null")
					.collect(joining(","))
				));
		// @formatter:on
	}

}
