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

package net.solarnetwork.codec.jackson.test;

import static java.lang.String.format;
import static java.time.Instant.ofEpochMilli;
import static net.solarnetwork.util.NumberUtils.decimalArray;
import static org.assertj.core.api.BDDAssertions.from;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenObject;
import static org.assertj.core.api.InstanceOfAssertFactories.collection;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.Before;
import org.junit.Test;
import net.solarnetwork.codec.jackson.BasicObjectDatumStreamDataSetDeserializer;
import net.solarnetwork.domain.datum.AggregateStreamDatum;
import net.solarnetwork.domain.datum.DatumProperties;
import net.solarnetwork.domain.datum.DatumPropertiesStatistics;
import net.solarnetwork.domain.datum.DatumSamplesType;
import net.solarnetwork.domain.datum.ObjectDatumKind;
import net.solarnetwork.domain.datum.ObjectDatumStreamDataSet;
import net.solarnetwork.domain.datum.ObjectDatumStreamMetadata;
import net.solarnetwork.domain.datum.StreamDatum;
import net.solarnetwork.util.ClassUtils;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

/**
 * Test cases for the {@link BasicObjectDatumStreamDataSetDeserializer} class.
 *
 * @author matt
 * @version 1.0
 */
public class BasicObjectDatumStreamDataSetDeserializerTests {

	private ObjectMapper mapper;

	private ObjectMapper createObjectMapper() {
		SimpleModule mod = new SimpleModule("Test");
		mod.addDeserializer(ObjectDatumStreamDataSet.class,
				BasicObjectDatumStreamDataSetDeserializer.INSTANCE);
		return JsonMapper.builder().addModule(mod).build();
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
		// @formatter:off
		thenObject(result)
			.as("Result parsed")
			.isNotNull()
			.extracting(r -> r.metadataForStreamId(streamId))
			.as("Stream meatadata parsed")
			.isNotNull()
			.as("Stream ID parsed")
			.returns(streamId, from(ObjectDatumStreamMetadata::getStreamId))
			.as("Time zone ID parsed")
			.returns("Pacific/Auckland", from(ObjectDatumStreamMetadata::getTimeZoneId))
			.as("Kind parsed")
			.returns(ObjectDatumKind.Node, from(ObjectDatumStreamMetadata::getKind))
			.as("Object ID parsed")
			.returns(123L, from(ObjectDatumStreamMetadata::getObjectId))
			.as("Source ID parsed")
			.returns("test/source", from(ObjectDatumStreamMetadata::getSourceId))
			.as("Instantaneous property names parsed")
			.returns(new String[] {"a", "b"}, from(m-> m.propertyNamesForType(DatumSamplesType.Instantaneous)))
			.as("Accumulating property names parsed")
			.returns(new String[] {"c"}, from(m-> m.propertyNamesForType(DatumSamplesType.Accumulating)))
			.as("Status property names parsed")
			.returns(new String[] {"d"}, from(m-> m.propertyNamesForType(DatumSamplesType.Status)))
			;
		thenObject(result)
			.as("Stream IDs provided")
			.returns(List.of(streamId), from(ObjectDatumStreamDataSet::metadataStreamIds))
			.asInstanceOf(InstanceOfAssertFactories.iterable(StreamDatum.class))
			.as("All datum parsed")
			.hasSize(2)
			.satisfies(l -> {
				then(l).element(0)
					.as("Datum parsed")
					.isNotNull()
					.as("Datum timestamp parsed")
					.returns(start, from(StreamDatum::getTimestamp))
					.as("Datum properties parsed")
					.extracting(StreamDatum::getProperties)
					.as("Instantaneous properties parsed")
					.returns(decimalArray("1.23", "2.34"), from(DatumProperties::getInstantaneous))
					.as("Accumualting properties parsed")
					.returns(decimalArray("3.45"), from(DatumProperties::getAccumulating))
					.as("Status properties parsed")
					.returns(new String[] {"foo"}, from(DatumProperties::getStatus))
					.as("Tags parsed")
					.returns(new String[] {"a"}, from(DatumProperties::getTags))
					;
				then(l).element(1)
					.as("Datum parsed")
					.isNotNull()
					.as("Datum timestamp parsed")
					.returns(start.plusSeconds(1), from(StreamDatum::getTimestamp))
					.as("Datum properties parsed")
					.extracting(StreamDatum::getProperties)
					.as("Instantaneous properties parsed")
					.returns(decimalArray("3.21", "4.32"), from(DatumProperties::getInstantaneous))
					.as("Accumualting properties parsed")
					.returns(decimalArray("5.43"), from(DatumProperties::getAccumulating))
					.as("Status properties parsed")
					.returns(new String[] {"bar"}, from(DatumProperties::getStatus))
					.as("Tags parsed")
					.returns(null, from(DatumProperties::getTags))
					;
			})
			;
		// @formatter:on
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
		// @formatter:off
		thenObject(result)
			.as("Result parsed")
			.isNotNull()
			.extracting(r -> r.metadataForStreamId(streamId))
			.as("Stream meatadata parsed")
			.isNotNull()
			.as("Stream ID parsed")
			.returns(streamId, from(ObjectDatumStreamMetadata::getStreamId))
			.as("Time zone ID parsed")
			.returns("Pacific/Auckland", from(ObjectDatumStreamMetadata::getTimeZoneId))
			.as("Kind parsed")
			.returns(ObjectDatumKind.Node, from(ObjectDatumStreamMetadata::getKind))
			.as("Object ID parsed")
			.returns(123L, from(ObjectDatumStreamMetadata::getObjectId))
			.as("Source ID parsed")
			.returns("test/source", from(ObjectDatumStreamMetadata::getSourceId))
			.as("Instantaneous property names parsed")
			.returns(new String[] {"a", "b"}, from(m-> m.propertyNamesForType(DatumSamplesType.Instantaneous)))
			.as("Accumulating property names parsed")
			.returns(new String[] {"c"}, from(m-> m.propertyNamesForType(DatumSamplesType.Accumulating)))
			.as("Status property names parsed")
			.returns(new String[] {"d"}, from(m-> m.propertyNamesForType(DatumSamplesType.Status)))
			;
		thenObject(result)
			.as("Stream IDs provided")
			.returns(List.of(streamId), from(ObjectDatumStreamDataSet::metadataStreamIds))
			.asInstanceOf(InstanceOfAssertFactories.iterable(StreamDatum.class))
			.as("All datum parsed")
			.hasSize(2)
			.satisfies(l -> {
				then(l).element(0)
					.as("Datum parsed")
					.isNotNull()
					.as("Datum timestamp parsed")
					.returns(start, from(StreamDatum::getTimestamp))
					.as("Datum properties parsed")
					.extracting(StreamDatum::getProperties)
					.as("Instantaneous properties parsed")
					.returns(decimalArray(null, null), from(DatumProperties::getInstantaneous))
					.as("Accumualting properties parsed")
					.returns(decimalArray("3.45"), from(DatumProperties::getAccumulating))
					.as("Status properties parsed")
					.returns(new String[] {"foo"}, from(DatumProperties::getStatus))
					.as("Tags parsed")
					.returns(new String[] {"a"}, from(DatumProperties::getTags))
					;
				then(l).element(1)
					.as("Datum parsed")
					.isNotNull()
					.as("Datum timestamp parsed")
					.returns(start.plusSeconds(1), from(StreamDatum::getTimestamp))
					.as("Datum properties parsed")
					.extracting(StreamDatum::getProperties)
					.as("Instantaneous properties parsed")
					.returns(decimalArray(null, "4.32"), from(DatumProperties::getInstantaneous))
					.as("Accumualting properties parsed")
					.returns(decimalArray((String)null), from(DatumProperties::getAccumulating))
					.as("Status properties parsed")
					.returns(new String[] {null}, from(DatumProperties::getStatus))
					.as("Tags parsed")
					.returns(null, from(DatumProperties::getTags))
					;
			})
			;
		// @formatter:on
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
		// @formatter:off
		thenObject(result)
			.as("Result parsed")
			.isNotNull()
			.extracting(ObjectDatumStreamDataSet::metadataStreamIds, collection(UUID.class))
			.as("Result metadata is empty")
			.isEmpty();
			;
		then(result)
			.as("Result data is empty")
			.isEmpty()
			;
		// @formatter:off
	}

	@Test
	public void oneStream_aggregate() throws IOException {
		// GIVEN
		String json = ClassUtils.getResourceAsString("node-agg-stream-01.json", net.solarnetwork.codec.test.JsonUtilsTests.class);

		// WHEN
		@SuppressWarnings("unchecked")
		ObjectDatumStreamDataSet<StreamDatum> result = mapper.readValue(json,
				ObjectDatumStreamDataSet.class);

		// THEN
		// @formatter:off
		final UUID streamId = UUID.fromString("5514f762-2361-4ec2-98ab-7e96807b3255");
		thenObject(result)
			.as("Result parsed")
			.isNotNull()
			.extracting(r -> r.metadataForStreamId(streamId))
			.as("Stream meatadata parsed")
			.isNotNull()
			.as("Stream ID parsed")
			.returns(streamId, from(ObjectDatumStreamMetadata::getStreamId))
			.as("Time zone ID parsed")
			.returns("America/New_York", from(ObjectDatumStreamMetadata::getTimeZoneId))
			.as("Kind parsed")
			.returns(ObjectDatumKind.Node, from(ObjectDatumStreamMetadata::getKind))
			.as("Object ID parsed")
			.returns(123L, from(ObjectDatumStreamMetadata::getObjectId))
			.as("Source ID parsed")
			.returns("/pyrometer/1", from(ObjectDatumStreamMetadata::getSourceId))
			.as("Instantaneous property names parsed")
			.returns(new String[] {"irradiance", "temperature"}, from(m-> m.propertyNamesForType(DatumSamplesType.Instantaneous)))
			.as("Accumulating property names parsed")
			.returns(new String[] {"irradianceHours"}, from(m-> m.propertyNamesForType(DatumSamplesType.Accumulating)))
			.as("Status property names parsed")
			.returns(new String[] {"state", "code"}, from(m-> m.propertyNamesForType(DatumSamplesType.Status)))
			;
		thenObject(result)
			.as("Stream IDs provided")
			.returns(List.of(streamId), from(ObjectDatumStreamDataSet::metadataStreamIds))
			.asInstanceOf(InstanceOfAssertFactories.iterable(StreamDatum.class))
			.as("All datum parsed")
			.hasSize(1)
			.satisfies(l -> {
				then(l).element(0)
					.as("Datum parsed")
					.isNotNull()
					.asInstanceOf(type(AggregateStreamDatum.class))
					.as("Datum timestamp parsed")
					.returns(ofEpochMilli(1650945600000L), from(AggregateStreamDatum::getTimestamp))
					.as("Datum end timestamp parsed")
					.returns(ofEpochMilli(1651032000000L), from(AggregateStreamDatum::getEndTimestamp))
					.satisfies(d -> {
						then(d.getProperties())
							.as("Instantaneous properties parsed")
							.returns(decimalArray("3.6", "19.1"), from(DatumProperties::getInstantaneous))
							.as("Accumualting properties parsed")
							.returns(decimalArray("1.422802"), from(DatumProperties::getAccumulating))
							.as("Status properties parsed")
							.returns(new String[] {"Nominal", "S1"}, from(DatumProperties::getStatus))
							.as("Tags parsed")
							.returns(new String[] {"active"}, from(DatumProperties::getTags))
							;
						then(d.getStatistics())
							.as("Instantaneous stats parsed")
							.returns(new BigDecimal[][] {
								decimalArray("3.6", "2", "0", "7.2"),
								decimalArray("19.1", "2", "18.1", "20.1"),
								}, from(DatumPropertiesStatistics::getInstantaneous)
							)
							.as("Accumulating stats parsed")
							.returns(new BigDecimal[][] {
								decimalArray("1.422802", "1138.446687", "1139.869489")
								}, from(DatumPropertiesStatistics::getAccumulating)
							)
							;
					})
					;
			})
			;
		// @formatter:on
	}

	@Test
	public void oneStream_aggregate_nullEndDate() throws IOException {
		// GIVEN
		String json = ClassUtils.getResourceAsString("node-agg-stream-02.json",
				net.solarnetwork.codec.test.JsonUtilsTests.class);

		// WHEN
		@SuppressWarnings("unchecked")
		ObjectDatumStreamDataSet<StreamDatum> result = mapper.readValue(json,
				ObjectDatumStreamDataSet.class);

		// THEN
		// @formatter:off
		final UUID streamId = UUID.fromString("5514f762-2361-4ec2-98ab-7e96807b3255");
		thenObject(result)
			.as("Result parsed")
			.isNotNull()
			.extracting(r -> r.metadataForStreamId(streamId))
			.as("Stream meatadata parsed")
			.isNotNull()
			.as("Stream ID parsed")
			.returns(streamId, from(ObjectDatumStreamMetadata::getStreamId))
			.as("Time zone ID parsed")
			.returns("America/New_York", from(ObjectDatumStreamMetadata::getTimeZoneId))
			.as("Kind parsed")
			.returns(ObjectDatumKind.Node, from(ObjectDatumStreamMetadata::getKind))
			.as("Object ID parsed")
			.returns(123L, from(ObjectDatumStreamMetadata::getObjectId))
			.as("Source ID parsed")
			.returns("/pyrometer/1", from(ObjectDatumStreamMetadata::getSourceId))
			.as("Instantaneous property names parsed")
			.returns(new String[] {"irradiance", "temperature"}, from(m-> m.propertyNamesForType(DatumSamplesType.Instantaneous)))
			.as("Accumulating property names parsed")
			.returns(new String[] {"irradianceHours"}, from(m-> m.propertyNamesForType(DatumSamplesType.Accumulating)))
			.as("Status property names parsed")
			.returns(new String[] {"state", "code"}, from(m-> m.propertyNamesForType(DatumSamplesType.Status)))
			;
		thenObject(result)
			.as("Stream IDs provided")
			.returns(List.of(streamId), from(ObjectDatumStreamDataSet::metadataStreamIds))
			.asInstanceOf(InstanceOfAssertFactories.iterable(StreamDatum.class))
			.as("All datum parsed")
			.hasSize(1)
			.satisfies(l -> {
				then(l).element(0)
					.as("Datum parsed")
					.isNotNull()
					.asInstanceOf(type(AggregateStreamDatum.class))
					.as("Datum timestamp parsed")
					.returns(ofEpochMilli(1650945600000L), from(AggregateStreamDatum::getTimestamp))
					.as("Datum end timestamp is null")
					.returns(null, from(AggregateStreamDatum::getEndTimestamp))
					.satisfies(d -> {
						then(d.getProperties())
							.as("Instantaneous properties parsed")
							.returns(decimalArray("3.6", "19.1"), from(DatumProperties::getInstantaneous))
							.as("Accumualting properties parsed")
							.returns(decimalArray("1.422802"), from(DatumProperties::getAccumulating))
							.as("Status properties parsed")
							.returns(new String[] {"Nominal", "S1"}, from(DatumProperties::getStatus))
							.as("Tags parsed")
							.returns(new String[] {"active"}, from(DatumProperties::getTags))
							;
						then(d.getStatistics())
							.as("Instantaneous stats parsed")
							.returns(new BigDecimal[][] {
								decimalArray("3.6", "2", "0", "7.2"),
								decimalArray("19.1", "2", "18.1", "20.1"),
								}, from(DatumPropertiesStatistics::getInstantaneous)
							)
							.as("Accumulating stats parsed")
							.returns(new BigDecimal[][] {
								decimalArray("1.422802", "1138.446687", "1139.869489")
								}, from(DatumPropertiesStatistics::getAccumulating)
							)
							;
					})
					;
			})
			;
		// @formatter:on
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
		// @formatter:off
		thenObject(result)
			.as("Result parsed")
			.isNotNull()
			.returns(List.of(streamId1, streamId2), from(ObjectDatumStreamDataSet::metadataStreamIds))
			.satisfies(r -> {
				then(r.metadataForStreamId(streamId1))
					.as("Stream meatadata parsed")
					.isNotNull()
					.as("Stream ID parsed")
					.returns(streamId1, from(ObjectDatumStreamMetadata::getStreamId))
					.as("Time zone ID parsed")
					.returns("Pacific/Auckland", from(ObjectDatumStreamMetadata::getTimeZoneId))
					.as("Kind parsed")
					.returns(ObjectDatumKind.Node, from(ObjectDatumStreamMetadata::getKind))
					.as("Object ID parsed")
					.returns(123L, from(ObjectDatumStreamMetadata::getObjectId))
					.as("Source ID parsed")
					.returns("test/source/1", from(ObjectDatumStreamMetadata::getSourceId))
					.as("Instantaneous property names parsed")
					.returns(new String[] {"a", "b"}, from(m-> m.propertyNamesForType(DatumSamplesType.Instantaneous)))
					.as("Accumulating property names parsed")
					.returns(new String[] {"c"}, from(m-> m.propertyNamesForType(DatumSamplesType.Accumulating)))
					.as("Status property names parsed")
					.returns(new String[] {"d"}, from(m-> m.propertyNamesForType(DatumSamplesType.Status)))
					;
				then(r.metadataForStreamId(streamId2))
					.as("Stream meatadata parsed")
					.isNotNull()
					.as("Stream ID parsed")
					.returns(streamId2, from(ObjectDatumStreamMetadata::getStreamId))
					.as("Time zone ID parsed")
					.returns("Pacific/Auckland", from(ObjectDatumStreamMetadata::getTimeZoneId))
					.as("Kind parsed")
					.returns(ObjectDatumKind.Node, from(ObjectDatumStreamMetadata::getKind))
					.as("Object ID parsed")
					.returns(123L, from(ObjectDatumStreamMetadata::getObjectId))
					.as("Source ID parsed")
					.returns("test/source/2", from(ObjectDatumStreamMetadata::getSourceId))
					.as("Instantaneous property names parsed")
					.returns(new String[] {"aa", "bb"}, from(m-> m.propertyNamesForType(DatumSamplesType.Instantaneous)))
					.as("Accumulating property names parsed")
					.returns(new String[] {"cc"}, from(m-> m.propertyNamesForType(DatumSamplesType.Accumulating)))
					.as("Status property names parsed")
					.returns(new String[] {"dd"}, from(m-> m.propertyNamesForType(DatumSamplesType.Status)))
					;
			})
			;
		then(result)
			.as("All datum parsed")
			.hasSize(4)
			.satisfies(l -> {
				then(l).element(0)
					.as("Datum parsed")
					.isNotNull()
					.as("Stream ID parsed")
					.returns(streamId1, StreamDatum::getStreamId)
					.as("Datum timestamp parsed")
					.returns(start, from(StreamDatum::getTimestamp))
					.as("Datum properties parsed")
					.extracting(StreamDatum::getProperties)
					.as("Instantaneous properties parsed")
					.returns(decimalArray("1.23", "2.34"), from(DatumProperties::getInstantaneous))
					.as("Accumualting properties parsed")
					.returns(decimalArray("3.45"), from(DatumProperties::getAccumulating))
					.as("Status properties parsed")
					.returns(new String[] {"foo"}, from(DatumProperties::getStatus))
					.as("Tags parsed")
					.returns(new String[] {"a"}, from(DatumProperties::getTags))
					;
				then(l).element(1)
					.as("Datum parsed")
					.isNotNull()
					.as("Stream ID parsed")
					.returns(streamId2, StreamDatum::getStreamId)
					.as("Datum timestamp parsed")
					.returns(start, from(StreamDatum::getTimestamp))
					.as("Datum properties parsed")
					.extracting(StreamDatum::getProperties)
					.as("Instantaneous properties parsed")
					.returns(decimalArray("1.234", "2.345"), from(DatumProperties::getInstantaneous))
					.as("Accumualting properties parsed")
					.returns(decimalArray("3.456"), from(DatumProperties::getAccumulating))
					.as("Status properties parsed")
					.returns(new String[] {"fooo"}, from(DatumProperties::getStatus))
					.as("Tags parsed")
					.returns(new String[] {"aa"}, from(DatumProperties::getTags))
					;
				then(l).element(2)
					.as("Datum parsed")
					.isNotNull()
					.as("Stream ID parsed")
					.returns(streamId1, StreamDatum::getStreamId)
					.as("Datum timestamp parsed")
					.returns(start.plusSeconds(1), from(StreamDatum::getTimestamp))
					.as("Datum properties parsed")
					.extracting(StreamDatum::getProperties)
					.as("Instantaneous properties parsed")
					.returns(decimalArray("3.21", "4.32"), from(DatumProperties::getInstantaneous))
					.as("Accumualting properties parsed")
					.returns(decimalArray("5.43"), from(DatumProperties::getAccumulating))
					.as("Status properties parsed")
					.returns(new String[] {"bar"}, from(DatumProperties::getStatus))
					.as("Tags parsed")
					.returns(null, from(DatumProperties::getTags))
					;
				then(l).element(3)
					.as("Datum parsed")
					.isNotNull()
					.as("Stream ID parsed")
					.returns(streamId2, StreamDatum::getStreamId)
					.as("Datum timestamp parsed")
					.returns(start.plusSeconds(1), from(StreamDatum::getTimestamp))
					.as("Datum properties parsed")
					.extracting(StreamDatum::getProperties)
					.as("Instantaneous properties parsed")
					.returns(decimalArray("3.211", "4.321"), from(DatumProperties::getInstantaneous))
					.as("Accumualting properties parsed")
					.returns(decimalArray("5.432"), from(DatumProperties::getAccumulating))
					.as("Status properties parsed")
					.returns(new String[] {"barr"}, from(DatumProperties::getStatus))
					.as("Tags parsed")
					.returns(null, from(DatumProperties::getTags))
					;
			})
			;
		// @formatter:on
	}

	@Test
	public void oneStream_aggregate_missingAccumulatingStats() throws IOException {
		// GIVEN
		String json = ClassUtils.getResourceAsString("node-agg-stream-03.json",
				net.solarnetwork.codec.test.JsonUtilsTests.class);

		// WHEN
		@SuppressWarnings("unchecked")
		ObjectDatumStreamDataSet<StreamDatum> result = mapper.readValue(json,
				ObjectDatumStreamDataSet.class);

		// THEN
		// @formatter:off
		final UUID streamId = UUID.fromString("5514f762-2361-4ec2-98ab-7e96807b3255");
		thenObject(result)
			.as("Result parsed")
			.isNotNull()
			.as("Stream IDs provided")
			.returns(List.of(streamId), from(ObjectDatumStreamDataSet::metadataStreamIds))
			.extracting(r -> r.metadataForStreamId(streamId))
			.as("Stream meatadata parsed")
			.isNotNull()
			.as("Stream ID parsed")
			.returns(streamId, from(ObjectDatumStreamMetadata::getStreamId))
			.as("Time zone ID parsed")
			.returns("America/New_York", from(ObjectDatumStreamMetadata::getTimeZoneId))
			.as("Kind parsed")
			.returns(ObjectDatumKind.Node, from(ObjectDatumStreamMetadata::getKind))
			.as("Object ID parsed")
			.returns(123L, from(ObjectDatumStreamMetadata::getObjectId))
			.as("Source ID parsed")
			.returns("/pyrometer/1", from(ObjectDatumStreamMetadata::getSourceId))
			.as("Instantaneous property names parsed")
			.returns(new String[] {"irradiance", "temperature"}, from(m-> m.propertyNamesForType(DatumSamplesType.Instantaneous)))
			.as("Accumulating property names parsed")
			.returns(new String[] {"irradianceHours"}, from(m-> m.propertyNamesForType(DatumSamplesType.Accumulating)))
			.as("Status property names parsed")
			.returns(new String[] {"state", "code"}, from(m-> m.propertyNamesForType(DatumSamplesType.Status)))
			;
		then(result)
			.as("All datum parsed")
			.hasSize(1)
			.satisfies(l -> {
				then(l).element(0)
					.as("Datum parsed")
					.isNotNull()
					.asInstanceOf(type(AggregateStreamDatum.class))
					.as("Datum timestamp parsed")
					.returns(ofEpochMilli(1650945600000L), from(AggregateStreamDatum::getTimestamp))
					.as("Datum end timestamp is null")
					.returns(ofEpochMilli(1651032000000L), from(AggregateStreamDatum::getEndTimestamp))
					.satisfies(d -> {
						then(d.getProperties())
							.as("Instantaneous properties parsed")
							.returns(decimalArray("3.6", "19.1"), from(DatumProperties::getInstantaneous))
							.as("Accumualting properties parsed")
							.returns(decimalArray("1.422802"), from(DatumProperties::getAccumulating))
							.as("Status properties parsed")
							.returns(new String[] {"Nominal", "S1"}, from(DatumProperties::getStatus))
							.as("Tags parsed")
							.returns(new String[] {"active"}, from(DatumProperties::getTags))
							;
						then(d.getStatistics())
							.as("Instantaneous stats parsed")
							.returns(new BigDecimal[][] {
								decimalArray("3.6", "2", "0", "7.2"),
								decimalArray("19.1", "2", "18.1", "20.1"),
								}, from(DatumPropertiesStatistics::getInstantaneous)
							)
							.as("Accumulating stats parsed (missing)")
							.returns(new BigDecimal[][] {
								decimalArray("1.422802", null, null)
								}, from(DatumPropertiesStatistics::getAccumulating)
							)
							;
					})
					;
			})
			;
		// @formatter:on
	}

}
