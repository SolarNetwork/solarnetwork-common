/* ==================================================================
 * ObjectDatumStreamDataSerializerTests.java - 29/04/2022 12:19:55 PM
 * 
 * Copyright 2022 SolarNetwork.net Dev Team
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

import static java.lang.String.format;
import static net.solarnetwork.util.NumberUtils.decimalArray;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import net.solarnetwork.codec.ObjectDatumStreamDataSerializer;
import net.solarnetwork.domain.datum.BasicObjectDatumStreamMetadata;
import net.solarnetwork.domain.datum.BasicStreamDatum;
import net.solarnetwork.domain.datum.DatumProperties;
import net.solarnetwork.domain.datum.ObjectDatumKind;
import net.solarnetwork.domain.datum.ObjectDatumStreamData;
import net.solarnetwork.domain.datum.ObjectDatumStreamMetadata;
import net.solarnetwork.domain.datum.StreamDatum;

/**
 * Test cases for the {@link ObjectDatumStreamDataSerializer} class.
 * 
 * @author matt
 * @version 1.0
 */
public class ObjectDatumStreamDataSerializerTests {

	private ObjectMapper mapper;

	private ObjectMapper createObjectMapper() {
		ObjectMapper m = new ObjectMapper();
		SimpleModule mod = new SimpleModule("Test");
		mod.addSerializer(ObjectDatumStreamData.class, ObjectDatumStreamDataSerializer.INSTANCE);
		m.registerModule(mod);
		return m;
	}

	@Before
	public void setup() {
		mapper = createObjectMapper();
	}

	private ObjectDatumStreamMetadata nodeMeta(Long nodeId, String sourceId, String[] i, String[] a,
			String[] s) {
		return new BasicObjectDatumStreamMetadata(UUID.randomUUID(), "Pacific/Auckland",
				ObjectDatumKind.Node, nodeId, sourceId, i, a, s);
	}

	@Test
	public void serialize_typical() throws IOException {
		// GIVEN
		ObjectDatumStreamMetadata meta = nodeMeta(123L, "test/source", new String[] { "a", "b" },
				new String[] { "c" }, new String[] { "d" });
		Instant start = Instant
				.from(LocalDateTime.of(2022, 4, 29, 13, 52).atZone(ZoneId.of("Pacific/Auckland")));

		DatumProperties p1 = new DatumProperties();
		p1.setInstantaneous(decimalArray("1.23", "2.34"));
		p1.setAccumulating(decimalArray("3.45"));
		p1.setStatus(new String[] { "foo" });
		p1.setTags(new String[] { "a" });
		StreamDatum d1 = new BasicStreamDatum(meta.getStreamId(), start, p1);

		DatumProperties p2 = new DatumProperties();
		p2.setInstantaneous(decimalArray("3.21", "4.32"));
		p2.setAccumulating(decimalArray("5.43"));
		p2.setStatus(new String[] { "bar" });
		StreamDatum d2 = new BasicStreamDatum(meta.getStreamId(), start.plusSeconds(1), p2);

		ObjectDatumStreamData data = new ObjectDatumStreamData(meta, Arrays.asList(d1, d2));

		// WHEN
		String json = mapper.writeValueAsString(data);

		// THEN
		assertThat("JSON", json, is(format("{\"meta\":{\"streamId\":\"%s\",", meta.getStreamId())
				+ "\"zone\":\"Pacific/Auckland\",\"kind\":\"n\",\"objectId\":123,"
				+ "\"sourceId\":\"test/source\",\"i\":[\"a\",\"b\"],\"a\":[\"c\"],\"s\":[\"d\"]},"
				+ "\"data\":[[1651197120000,1.23,2.34,3.45,\"foo\",\"a\"],"
				+ "[1651197121000,3.21,4.32,5.43,\"bar\"]]}"));
	}

	@Test
	public void serialize_missingData() throws IOException {
		// GIVEN
		ObjectDatumStreamMetadata meta = nodeMeta(123L, "test/source", new String[] { "a", "b" },
				new String[] { "c" }, new String[] { "d" });
		Instant start = Instant
				.from(LocalDateTime.of(2022, 4, 29, 13, 52).atZone(ZoneId.of("Pacific/Auckland")));

		DatumProperties p1 = new DatumProperties();
		p1.setAccumulating(decimalArray("3.45"));
		p1.setStatus(new String[] { "foo" });
		p1.setTags(new String[] { "a" });
		StreamDatum d1 = new BasicStreamDatum(meta.getStreamId(), start, p1);

		DatumProperties p2 = new DatumProperties();
		p2.setInstantaneous(decimalArray(null, "4.32"));
		StreamDatum d2 = new BasicStreamDatum(meta.getStreamId(), start.plusSeconds(1), p2);

		ObjectDatumStreamData data = new ObjectDatumStreamData(meta, Arrays.asList(d1, d2));

		// WHEN
		String json = mapper.writeValueAsString(data);

		// THEN
		assertThat("JSON", json, is(format("{\"meta\":{\"streamId\":\"%s\",", meta.getStreamId())
				+ "\"zone\":\"Pacific/Auckland\",\"kind\":\"n\",\"objectId\":123,"
				+ "\"sourceId\":\"test/source\",\"i\":[\"a\",\"b\"],\"a\":[\"c\"],\"s\":[\"d\"]},"
				+ "\"data\":[[1651197120000,null,null,3.45,\"foo\",\"a\"],"
				+ "[1651197121000,null,4.32,null,null]]}"));
	}

}
