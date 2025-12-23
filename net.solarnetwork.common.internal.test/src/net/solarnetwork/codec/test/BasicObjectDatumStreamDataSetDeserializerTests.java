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

import static java.lang.String.format;
import static java.time.Instant.ofEpochMilli;
import static net.solarnetwork.util.NumberUtils.decimalArray;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.junit.Before;
import org.junit.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import net.solarnetwork.codec.BasicObjectDatumStreamDataSetDeserializer;
import net.solarnetwork.domain.datum.AggregateStreamDatum;
import net.solarnetwork.domain.datum.DatumSamplesType;
import net.solarnetwork.domain.datum.ObjectDatumKind;
import net.solarnetwork.domain.datum.ObjectDatumStreamDataSet;
import net.solarnetwork.domain.datum.ObjectDatumStreamMetadata;
import net.solarnetwork.domain.datum.StreamDatum;
import net.solarnetwork.util.ClassUtils;

/**
 * Test cases for the {@link BasicObjectDatumStreamDataSetDeserializer} class.
 *
 * @author matt
 * @version 1.2
 */
public class BasicObjectDatumStreamDataSetDeserializerTests {

	private ObjectMapper mapper;

	private ObjectMapper createObjectMapper() {
		ObjectMapper m = new ObjectMapper();
		SimpleModule mod = new SimpleModule("Test");
		mod.addDeserializer(ObjectDatumStreamDataSet.class,
				BasicObjectDatumStreamDataSetDeserializer.INSTANCE);
		m.registerModule(mod);
		return m;
	}

	@Before
	public void setup() {
		mapper = createObjectMapper();
	}

	@Test
	public void oneStream() throws IOException {
		// GIVEN
		UUID streamId = UUID.randomUUID();
		String json = format("{\"meta\":[{\"streamId\":\"%s\",", streamId)
				+ "\"zone\":\"Pacific/Auckland\",\"kind\":\"n\",\"objectId\":123,"
				+ "\"sourceId\":\"test/source\",\"i\":[\"a\",\"b\"],\"a\":[\"c\"],\"s\":[\"d\"]}],"
				+ "\"data\":[[0,1651197120000,1.23,2.34,3.45,\"foo\",\"a\"],"
				+ "[0,1651197121000,3.21,4.32,5.43,\"bar\"]]}";
		Instant start = Instant
				.from(LocalDateTime.of(2022, 4, 29, 13, 52).atZone(ZoneId.of("Pacific/Auckland")));

		// WHEN
		@SuppressWarnings("unchecked")
		ObjectDatumStreamDataSet<StreamDatum> result = mapper.readValue(json,
				ObjectDatumStreamDataSet.class);

		// THEN
		assertThat("Result parsed", result, is(notNullValue()));

		assertThat("Metadata parsed", result.metadataStreamIds(), containsInAnyOrder(streamId));
		ObjectDatumStreamMetadata meta = result.metadataForStreamId(streamId);
		assertThat("Stream ID parsed", meta.getStreamId(), is(streamId));
		assertThat("Time zone parsed", meta.getTimeZoneId(), is("Pacific/Auckland"));
		assertThat("Kind parsed", meta.getKind(), is(ObjectDatumKind.Node));
		assertThat("Object ID parsed", meta.getObjectId(), is(123L));
		assertThat("Source ID parsed", meta.getSourceId(), is("test/source"));
		assertThat("Instantaneous property names parsed",
				meta.propertyNamesForType(DatumSamplesType.Instantaneous),
				is(arrayContaining("a", "b")));
		assertThat("Accumulating property names parsed",
				meta.propertyNamesForType(DatumSamplesType.Accumulating), is(arrayContaining("c")));
		assertThat("Status property names parsed", meta.propertyNamesForType(DatumSamplesType.Status),
				is(arrayContaining("d")));

		List<StreamDatum> data = StreamSupport.stream(result.spliterator(), false)
				.collect(Collectors.toList());

		assertThat("Data parsed", data, hasSize(2));

		StreamDatum d = data.get(0);
		assertThat("Datum parsed", d, is(notNullValue()));
		assertThat("Datum timestamp", d.getTimestamp(), is(start));
		assertThat("Datum properties parsed", d.getProperties(), is(notNullValue()));
		assertThat("Datum instantaneous values", d.getProperties().getInstantaneous(),
				is(arrayContaining(decimalArray("1.23", "2.34"))));
		assertThat("Datum accumulating values", d.getProperties().getAccumulating(),
				is(arrayContaining(decimalArray("3.45"))));
		assertThat("Datum status values", d.getProperties().getStatus(), is(arrayContaining("foo")));
		assertThat("Datum tag values", d.getProperties().getTags(), is(arrayContaining("a")));

		d = data.get(1);
		assertThat("Datum parsed", d, is(notNullValue()));
		assertThat("Datum timestamp", d.getTimestamp(), is(start.plusSeconds(1)));
		assertThat("Datum properties parsed", d.getProperties(), is(notNullValue()));
		assertThat("Datum instantaneous values", d.getProperties().getInstantaneous(),
				is(arrayContaining(decimalArray("3.21", "4.32"))));
		assertThat("Datum accumulating values", d.getProperties().getAccumulating(),
				is(arrayContaining(decimalArray("5.43"))));
		assertThat("Datum status values", d.getProperties().getStatus(), is(arrayContaining("bar")));
		assertThat("Datum tag values", d.getProperties().getTags(), is(nullValue()));
	}

	@Test
	public void oneStream_missingData() throws IOException {
		// GIVEN
		UUID streamId = UUID.randomUUID();
		String json = format("{\"meta\":[{\"streamId\":\"%s\",", streamId)
				+ "\"zone\":\"Pacific/Auckland\",\"kind\":\"n\",\"objectId\":123,"
				+ "\"sourceId\":\"test/source\",\"i\":[\"a\",\"b\"],\"a\":[\"c\"],\"s\":[\"d\"]}],"
				+ "\"data\":[[0,1651197120000,null,null,3.45,\"foo\",\"a\"],"
				+ "[0,1651197121000,null,4.32,null,null]]}";
		Instant start = Instant
				.from(LocalDateTime.of(2022, 4, 29, 13, 52).atZone(ZoneId.of("Pacific/Auckland")));

		// WHEN
		@SuppressWarnings("unchecked")
		ObjectDatumStreamDataSet<StreamDatum> result = mapper.readValue(json,
				ObjectDatumStreamDataSet.class);

		// THEN
		List<StreamDatum> data = StreamSupport.stream(result.spliterator(), false)
				.collect(Collectors.toList());

		assertThat("Data parsed", data, hasSize(2));

		assertThat("Metadata parsed", result.metadataStreamIds(), containsInAnyOrder(streamId));
		ObjectDatumStreamMetadata meta = result.metadataForStreamId(streamId);
		assertThat("Stream ID parsed", meta.getStreamId(), is(streamId));
		assertThat("Time zone parsed", meta.getTimeZoneId(), is("Pacific/Auckland"));
		assertThat("Kind parsed", meta.getKind(), is(ObjectDatumKind.Node));
		assertThat("Object ID parsed", meta.getObjectId(), is(123L));
		assertThat("Source ID parsed", meta.getSourceId(), is("test/source"));
		assertThat("Instantaneous property names parsed",
				meta.propertyNamesForType(DatumSamplesType.Instantaneous),
				is(arrayContaining("a", "b")));
		assertThat("Accumulating property names parsed",
				meta.propertyNamesForType(DatumSamplesType.Accumulating), is(arrayContaining("c")));
		assertThat("Status property names parsed", meta.propertyNamesForType(DatumSamplesType.Status),
				is(arrayContaining("d")));

		StreamDatum d = data.get(0);
		assertThat("Datum parsed", d, is(notNullValue()));
		assertThat("Datum timestamp", d.getTimestamp(), is(start));
		assertThat("Datum properties parsed", d.getProperties(), is(notNullValue()));
		assertThat("Datum instantaneous values", d.getProperties().getInstantaneous(),
				is(arrayContaining(decimalArray(null, null))));
		assertThat("Datum accumulating values", d.getProperties().getAccumulating(),
				is(arrayContaining(decimalArray("3.45"))));
		assertThat("Datum status values", d.getProperties().getStatus(), is(arrayContaining("foo")));
		assertThat("Datum tag values", d.getProperties().getTags(), is(arrayContaining("a")));

		d = data.get(1);
		assertThat("Datum parsed", d, is(notNullValue()));
		assertThat("Datum timestamp", d.getTimestamp(), is(start.plusSeconds(1)));
		assertThat("Datum properties parsed", d.getProperties(), is(notNullValue()));
		assertThat("Datum instantaneous values", d.getProperties().getInstantaneous(),
				is(arrayContaining(decimalArray(null, "4.32"))));
		assertThat("Datum accumulating values", d.getProperties().getAccumulating(),
				is(arrayContaining(decimalArray((String) null))));
		assertThat("Datum status values", d.getProperties().getStatus(),
				is(arrayContaining((String) null)));
		assertThat("Datum tag values", d.getProperties().getTags(), is(nullValue()));
	}

	@Test
	public void emptyStream() throws IOException {
		// GIVEN
		String json = "{}";

		// WHEN
		@SuppressWarnings("unchecked")
		ObjectDatumStreamDataSet<StreamDatum> result = mapper.readValue(json,
				ObjectDatumStreamDataSet.class);

		// THEN
		assertThat("Result parsed", result, is(notNullValue()));
		assertThat("Result metadata is empty", result.metadataStreamIds(), is(empty()));
		assertThat("Result data is empty", result.getResults(), is(emptyIterable()));

	}

	@Test
	public void oneStream_aggregate() throws IOException {
		// GIVEN
		String json = ClassUtils.getResourceAsString("node-agg-stream-01.json", getClass());

		// WHEN
		@SuppressWarnings("unchecked")
		ObjectDatumStreamDataSet<StreamDatum> result = mapper.readValue(json,
				ObjectDatumStreamDataSet.class);

		// THEN
		assertThat("Result parsed", result, is(notNullValue()));

		final UUID streamId = UUID.fromString("5514f762-2361-4ec2-98ab-7e96807b3255");
		assertThat("Metadata parsed", result.metadataStreamIds(), containsInAnyOrder(streamId));
		ObjectDatumStreamMetadata meta = result.metadataForStreamId(streamId);
		assertThat("Stream ID parsed", meta.getStreamId(), is(streamId));
		assertThat("Time zone parsed", meta.getTimeZoneId(), is("America/New_York"));
		assertThat("Kind parsed", meta.getKind(), is(ObjectDatumKind.Node));
		assertThat("Object ID parsed", meta.getObjectId(), is(123L));
		assertThat("Source ID parsed", meta.getSourceId(), is("/pyrometer/1"));
		assertThat("Instantaneous property names parsed",
				meta.propertyNamesForType(DatumSamplesType.Instantaneous),
				is(arrayContaining("irradiance", "temperature")));
		assertThat("Accumulating property names parsed",
				meta.propertyNamesForType(DatumSamplesType.Accumulating),
				is(arrayContaining("irradianceHours")));
		assertThat("Status property names parsed", meta.propertyNamesForType(DatumSamplesType.Status),
				is(arrayContaining("state", "code")));

		List<StreamDatum> data = StreamSupport.stream(result.spliterator(), false)
				.collect(Collectors.toList());

		assertThat("Data parsed", data, hasSize(1));

		assertThat("Datum parsed as AggregateStreamDatum", data.get(0),
				is(instanceOf(AggregateStreamDatum.class)));
		AggregateStreamDatum d = (AggregateStreamDatum) data.get(0);
		assertThat("Datum timestamp", d.getTimestamp(), is(ofEpochMilli(1650945600000L)));
		assertThat("Datum end timestamp", d.getEndTimestamp(), is(ofEpochMilli(1651032000000L)));
		assertThat("Datum properties parsed", d.getProperties(), is(notNullValue()));
		assertThat("Datum instantaneous values", d.getProperties().getInstantaneous(),
				is(arrayContaining(decimalArray("3.6", "19.1"))));
		assertThat("Datum instantaneous stats", d.getStatistics().getInstantaneous(),
				is(arrayContaining(new BigDecimal[][] { decimalArray("3.6", "2", "0", "7.2"),
						decimalArray("19.1", "2", "18.1", "20.1"), })));
		assertThat("Datum accumulating values", d.getProperties().getAccumulating(),
				is(arrayContaining(decimalArray("1.422802"))));
		assertThat("Datum accumulating stats", d.getStatistics().getAccumulating(), is(arrayContaining(
				new BigDecimal[][] { decimalArray("1.422802", "1138.446687", "1139.869489") })));
		assertThat("Datum status values", d.getProperties().getStatus(),
				is(arrayContaining("Nominal", "S1")));
		assertThat("Datum tag values", d.getProperties().getTags(), is(arrayContaining("active")));
	}

	@Test
	public void oneStream_aggregate_nullEndDate() throws IOException {
		// GIVEN
		String json = ClassUtils.getResourceAsString("node-agg-stream-02.json", getClass());

		// WHEN
		@SuppressWarnings("unchecked")
		ObjectDatumStreamDataSet<StreamDatum> result = mapper.readValue(json,
				ObjectDatumStreamDataSet.class);

		// THEN
		assertThat("Result parsed", result, is(notNullValue()));

		final UUID streamId = UUID.fromString("5514f762-2361-4ec2-98ab-7e96807b3255");
		assertThat("Metadata parsed", result.metadataStreamIds(), containsInAnyOrder(streamId));
		ObjectDatumStreamMetadata meta = result.metadataForStreamId(streamId);
		assertThat("Stream ID parsed", meta.getStreamId(), is(streamId));
		assertThat("Time zone parsed", meta.getTimeZoneId(), is("America/New_York"));
		assertThat("Kind parsed", meta.getKind(), is(ObjectDatumKind.Node));
		assertThat("Object ID parsed", meta.getObjectId(), is(123L));
		assertThat("Source ID parsed", meta.getSourceId(), is("/pyrometer/1"));
		assertThat("Instantaneous property names parsed",
				meta.propertyNamesForType(DatumSamplesType.Instantaneous),
				is(arrayContaining("irradiance", "temperature")));
		assertThat("Accumulating property names parsed",
				meta.propertyNamesForType(DatumSamplesType.Accumulating),
				is(arrayContaining("irradianceHours")));
		assertThat("Status property names parsed", meta.propertyNamesForType(DatumSamplesType.Status),
				is(arrayContaining("state", "code")));

		List<StreamDatum> data = StreamSupport.stream(result.spliterator(), false)
				.collect(Collectors.toList());

		assertThat("Data parsed", data, hasSize(1));

		assertThat("Datum parsed as AggregateStreamDatum", data.get(0),
				is(instanceOf(AggregateStreamDatum.class)));
		AggregateStreamDatum d = (AggregateStreamDatum) data.get(0);
		assertThat("Datum timestamp", d.getTimestamp(), is(ofEpochMilli(1650945600000L)));
		assertThat("Datum end timestamp", d.getEndTimestamp(), is(nullValue()));
		assertThat("Datum properties parsed", d.getProperties(), is(notNullValue()));
		assertThat("Datum instantaneous values", d.getProperties().getInstantaneous(),
				is(arrayContaining(decimalArray("3.6", "19.1"))));
		assertThat("Datum instantaneous stats", d.getStatistics().getInstantaneous(),
				is(arrayContaining(new BigDecimal[][] { decimalArray("3.6", "2", "0", "7.2"),
						decimalArray("19.1", "2", "18.1", "20.1"), })));
		assertThat("Datum accumulating values", d.getProperties().getAccumulating(),
				is(arrayContaining(decimalArray("1.422802"))));
		assertThat("Datum accumulating stats", d.getStatistics().getAccumulating(), is(arrayContaining(
				new BigDecimal[][] { decimalArray("1.422802", "1138.446687", "1139.869489") })));
		assertThat("Datum status values", d.getProperties().getStatus(),
				is(arrayContaining("Nominal", "S1")));
		assertThat("Datum tag values", d.getProperties().getTags(), is(arrayContaining("active")));
	}

	@Test
	public void multiStream() throws IOException {
		// GIVEN
		UUID streamId1 = UUID.randomUUID();
		UUID streamId2 = UUID.randomUUID();
		String json = format("{\"meta\":[{\"streamId\":\"%s\",", streamId1)
				+ "\"zone\":\"Pacific/Auckland\",\"kind\":\"n\",\"objectId\":123,"
				+ "\"sourceId\":\"test/source/1\",\"i\":[\"a\",\"b\"],\"a\":[\"c\"],\"s\":[\"d\"]},"
				+ format("{\"streamId\":\"%s\",", streamId2)
				+ "\"zone\":\"Pacific/Auckland\",\"kind\":\"n\",\"objectId\":123,"
				+ "\"sourceId\":\"test/source/2\",\"i\":[\"aa\",\"bb\"],\"a\":[\"cc\"],\"s\":[\"dd\"]}],\"data\":["
				+ "[0,1651197120000,1.23,2.34,3.45,\"foo\",\"a\"],"
				+ "[1,1651197120000,1.234,2.345,3.456,\"fooo\",\"aa\"],"
				+ "[0,1651197121000,3.21,4.32,5.43,\"bar\"],"
				+ "[1,1651197121000,3.211,4.321,5.432,\"barr\"]" + "]}";
		Instant start = Instant
				.from(LocalDateTime.of(2022, 4, 29, 13, 52).atZone(ZoneId.of("Pacific/Auckland")));

		@SuppressWarnings("unchecked")
		ObjectDatumStreamDataSet<StreamDatum> result = mapper.readValue(json,
				ObjectDatumStreamDataSet.class);

		// THEN
		List<StreamDatum> data = StreamSupport.stream(result.spliterator(), false)
				.collect(Collectors.toList());

		assertThat("Data parsed", data, hasSize(4));

		assertThat("Metadata parsed", result.metadataStreamIds(),
				containsInAnyOrder(streamId1, streamId2));
		ObjectDatumStreamMetadata meta1 = result.metadataForStreamId(streamId1);
		assertThat("Stream ID parsed", meta1.getStreamId(), is(streamId1));
		assertThat("Time zone parsed", meta1.getTimeZoneId(), is("Pacific/Auckland"));
		assertThat("Kind parsed", meta1.getKind(), is(ObjectDatumKind.Node));
		assertThat("Object ID parsed", meta1.getObjectId(), is(123L));
		assertThat("Source ID parsed", meta1.getSourceId(), is("test/source/1"));
		assertThat("Instantaneous property names parsed",
				meta1.propertyNamesForType(DatumSamplesType.Instantaneous),
				is(arrayContaining("a", "b")));
		assertThat("Accumulating property names parsed",
				meta1.propertyNamesForType(DatumSamplesType.Accumulating), is(arrayContaining("c")));
		assertThat("Status property names parsed", meta1.propertyNamesForType(DatumSamplesType.Status),
				is(arrayContaining("d")));

		ObjectDatumStreamMetadata meta2 = result.metadataForStreamId(streamId2);
		assertThat("Stream ID parsed", meta2.getStreamId(), is(streamId2));
		assertThat("Time zone parsed", meta2.getTimeZoneId(), is("Pacific/Auckland"));
		assertThat("Kind parsed", meta2.getKind(), is(ObjectDatumKind.Node));
		assertThat("Object ID parsed", meta2.getObjectId(), is(123L));
		assertThat("Source ID parsed", meta2.getSourceId(), is("test/source/2"));
		assertThat("Instantaneous property names parsed",
				meta2.propertyNamesForType(DatumSamplesType.Instantaneous),
				is(arrayContaining("aa", "bb")));
		assertThat("Accumulating property names parsed",
				meta2.propertyNamesForType(DatumSamplesType.Accumulating), is(arrayContaining("cc")));
		assertThat("Status property names parsed", meta2.propertyNamesForType(DatumSamplesType.Status),
				is(arrayContaining("dd")));

		StreamDatum d = data.get(0);
		assertThat("Datum parsed", d, is(notNullValue()));
		assertThat("Datum timestamp", d.getTimestamp(), is(start));
		assertThat("Datum properties parsed", d.getProperties(), is(notNullValue()));
		assertThat("Datum instantaneous values", d.getProperties().getInstantaneous(),
				is(arrayContaining(decimalArray("1.23", "2.34"))));
		assertThat("Datum accumulating values", d.getProperties().getAccumulating(),
				is(arrayContaining(decimalArray("3.45"))));
		assertThat("Datum status values", d.getProperties().getStatus(), is(arrayContaining("foo")));
		assertThat("Datum tag values", d.getProperties().getTags(), is(arrayContaining("a")));

		d = data.get(1);
		assertThat("Datum parsed", d, is(notNullValue()));
		assertThat("Datum timestamp", d.getTimestamp(), is(start));
		assertThat("Datum properties parsed", d.getProperties(), is(notNullValue()));
		assertThat("Datum instantaneous values", d.getProperties().getInstantaneous(),
				is(arrayContaining(decimalArray("1.234", "2.345"))));
		assertThat("Datum accumulating values", d.getProperties().getAccumulating(),
				is(arrayContaining(decimalArray("3.456"))));
		assertThat("Datum status values", d.getProperties().getStatus(), is(arrayContaining("fooo")));
		assertThat("Datum tag values", d.getProperties().getTags(), is(arrayContaining("aa")));

		d = data.get(2);
		assertThat("Datum parsed", d, is(notNullValue()));
		assertThat("Datum timestamp", d.getTimestamp(), is(start.plusSeconds(1)));
		assertThat("Datum properties parsed", d.getProperties(), is(notNullValue()));
		assertThat("Datum instantaneous values", d.getProperties().getInstantaneous(),
				is(arrayContaining(decimalArray("3.21", "4.32"))));
		assertThat("Datum accumulating values", d.getProperties().getAccumulating(),
				is(arrayContaining(decimalArray("5.43"))));
		assertThat("Datum status values", d.getProperties().getStatus(), is(arrayContaining("bar")));
		assertThat("Datum tag values", d.getProperties().getTags(), is(nullValue()));

		d = data.get(3);
		assertThat("Datum parsed", d, is(notNullValue()));
		assertThat("Datum timestamp", d.getTimestamp(), is(start.plusSeconds(1)));
		assertThat("Datum properties parsed", d.getProperties(), is(notNullValue()));
		assertThat("Datum instantaneous values", d.getProperties().getInstantaneous(),
				is(arrayContaining(decimalArray("3.211", "4.321"))));
		assertThat("Datum accumulating values", d.getProperties().getAccumulating(),
				is(arrayContaining(decimalArray("5.432"))));
		assertThat("Datum status values", d.getProperties().getStatus(), is(arrayContaining("barr")));
		assertThat("Datum tag values", d.getProperties().getTags(), is(nullValue()));
	}

	@Test
	public void oneStream_aggregate_missingAccumulatingStats() throws IOException {
		// GIVEN
		String json = ClassUtils.getResourceAsString("node-agg-stream-03.json", getClass());

		// WHEN
		@SuppressWarnings("unchecked")
		ObjectDatumStreamDataSet<StreamDatum> result = mapper.readValue(json,
				ObjectDatumStreamDataSet.class);

		// THEN
		assertThat("Result parsed", result, is(notNullValue()));

		final UUID streamId = UUID.fromString("5514f762-2361-4ec2-98ab-7e96807b3255");
		assertThat("Metadata parsed", result.metadataStreamIds(), containsInAnyOrder(streamId));
		ObjectDatumStreamMetadata meta = result.metadataForStreamId(streamId);
		assertThat("Stream ID parsed", meta.getStreamId(), is(streamId));
		assertThat("Time zone parsed", meta.getTimeZoneId(), is("America/New_York"));
		assertThat("Kind parsed", meta.getKind(), is(ObjectDatumKind.Node));
		assertThat("Object ID parsed", meta.getObjectId(), is(123L));
		assertThat("Source ID parsed", meta.getSourceId(), is("/pyrometer/1"));
		assertThat("Instantaneous property names parsed",
				meta.propertyNamesForType(DatumSamplesType.Instantaneous),
				is(arrayContaining("irradiance", "temperature")));
		assertThat("Accumulating property names parsed",
				meta.propertyNamesForType(DatumSamplesType.Accumulating),
				is(arrayContaining("irradianceHours")));
		assertThat("Status property names parsed", meta.propertyNamesForType(DatumSamplesType.Status),
				is(arrayContaining("state", "code")));

		List<StreamDatum> data = StreamSupport.stream(result.spliterator(), false)
				.collect(Collectors.toList());

		assertThat("Data parsed", data, hasSize(1));

		assertThat("Datum parsed as AggregateStreamDatum", data.get(0),
				is(instanceOf(AggregateStreamDatum.class)));
		AggregateStreamDatum d = (AggregateStreamDatum) data.get(0);
		assertThat("Datum timestamp", d.getTimestamp(), is(ofEpochMilli(1650945600000L)));
		assertThat("Datum end timestamp", d.getEndTimestamp(), is(ofEpochMilli(1651032000000L)));
		assertThat("Datum properties parsed", d.getProperties(), is(notNullValue()));
		assertThat("Datum instantaneous values", d.getProperties().getInstantaneous(),
				is(arrayContaining(decimalArray("3.6", "19.1"))));
		assertThat("Datum instantaneous stats", d.getStatistics().getInstantaneous(),
				is(arrayContaining(new BigDecimal[][] { decimalArray("3.6", "2", "0", "7.2"),
						decimalArray("19.1", "2", "18.1", "20.1"), })));
		assertThat("Datum accumulating values", d.getProperties().getAccumulating(),
				is(arrayContaining(decimalArray("1.422802"))));
		assertThat("Datum accumulating stats (missing)", d.getStatistics().getAccumulating(),
				is(arrayContaining(new BigDecimal[][] { decimalArray("1.422802", null, null) })));
		assertThat("Datum status values", d.getProperties().getStatus(),
				is(arrayContaining("Nominal", "S1")));
		assertThat("Datum tag values", d.getProperties().getTags(), is(arrayContaining("active")));
	}

}
