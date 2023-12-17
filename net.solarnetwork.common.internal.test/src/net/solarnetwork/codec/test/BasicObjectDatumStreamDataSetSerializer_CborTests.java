/* ==================================================================
 * BasicObjectDatumStreamDataSetSerializerTests.java - 29/04/2022 12:19:55 PM
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

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static net.solarnetwork.domain.datum.BasicObjectDatumStreamDataSet.dataSet;
import static net.solarnetwork.domain.datum.DatumProperties.propertiesOf;
import static net.solarnetwork.util.ByteUtils.objectArray;
import static net.solarnetwork.util.NumberUtils.decimalArray;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.is;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import net.solarnetwork.codec.BasicObjectDatumStreamDataSetSerializer;
import net.solarnetwork.domain.datum.BasicObjectDatumStreamDataSet;
import net.solarnetwork.domain.datum.BasicObjectDatumStreamMetadata;
import net.solarnetwork.domain.datum.BasicStreamDatum;
import net.solarnetwork.domain.datum.DatumProperties;
import net.solarnetwork.domain.datum.ObjectDatumKind;
import net.solarnetwork.domain.datum.ObjectDatumStreamDataSet;
import net.solarnetwork.domain.datum.ObjectDatumStreamMetadata;
import net.solarnetwork.domain.datum.StreamDatum;

/**
 * Test cases for the {@link BasicObjectDatumStreamDataSetSerializer} class.
 * 
 * @author matt
 * @version 1.0
 */
public class BasicObjectDatumStreamDataSetSerializer_CborTests {

	private ObjectMapper mapper;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private ObjectMapper createObjectMapper() {
		ObjectMapper m = new ObjectMapper(new CBORFactory());
		SimpleModule mod = new SimpleModule("Test");
		mod.addSerializer(ObjectDatumStreamDataSet.class,
				(JsonSerializer) BasicObjectDatumStreamDataSetSerializer.INSTANCE);
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
	public void oneStream() throws IOException {
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

		BasicObjectDatumStreamDataSet<StreamDatum> data = dataSet(asList(meta), asList(d1, d2));

		// WHEN
		Byte[] cbor = objectArray(mapper.writeValueAsBytes(data));

		// THEN
		assertThat("CBOR", cbor, is(arrayWithSize(206)));
	}

	@Test
	public void oneStream_missingData() throws IOException {
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

		BasicObjectDatumStreamDataSet<StreamDatum> data = dataSet(asList(meta), asList(d1, d2));

		// WHEN
		Byte[] cbor = objectArray(mapper.writeValueAsBytes(data));

		// THEN
		assertThat("CBOR", cbor, is(arrayWithSize(185)));
	}

	@Test
	public void emptyStream() throws IOException {
		// GIVEN
		BasicObjectDatumStreamDataSet<StreamDatum> data = dataSet(emptyList(), emptyList());

		// WHEN
		Byte[] cbor = objectArray(mapper.writeValueAsBytes(data));

		// THEN
		assertThat("CBOR", cbor, is(arrayWithSize(1)));

	}

	@Test
	public void multiStream() throws IOException {
		// GIVEN
		ObjectDatumStreamMetadata meta1 = nodeMeta(123L, "test/source/1", new String[] { "a", "b" },
				new String[] { "c" }, new String[] { "d" });
		ObjectDatumStreamMetadata meta2 = nodeMeta(123L, "test/source/2", new String[] { "aa", "bb" },
				new String[] { "cc" }, new String[] { "dd" });
		Instant start = Instant
				.from(LocalDateTime.of(2022, 4, 29, 13, 52).atZone(ZoneId.of("Pacific/Auckland")));

		StreamDatum d1_1 = new BasicStreamDatum(meta1.getStreamId(), start,
				propertiesOf(decimalArray("1.23", "2.34"), decimalArray("3.45"), new String[] { "foo" },
						new String[] { "a" }));

		StreamDatum d2_1 = new BasicStreamDatum(meta2.getStreamId(), start,
				propertiesOf(decimalArray("1.234", "2.345"), decimalArray("3.456"),
						new String[] { "fooo" }, new String[] { "aa" }));

		StreamDatum d1_2 = new BasicStreamDatum(meta1.getStreamId(), start.plusSeconds(1), propertiesOf(
				decimalArray("3.21", "4.32"), decimalArray("5.43"), new String[] { "bar" }, null));

		StreamDatum d2_2 = new BasicStreamDatum(meta2.getStreamId(), start.plusSeconds(1), propertiesOf(
				decimalArray("3.211", "4.321"), decimalArray("5.432"), new String[] { "barr" }, null));
		BasicObjectDatumStreamDataSet<StreamDatum> data = dataSet(asList(meta1, meta2),
				asList(d1_1, d2_1, d1_2, d2_2));

		// WHEN
		Byte[] cbor = objectArray(mapper.writeValueAsBytes(data));

		// THEN
		assertThat("CBOR", cbor, is(arrayWithSize(411)));
	}

}
