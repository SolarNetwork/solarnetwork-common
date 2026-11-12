/* ==================================================================
 * ObjectMapperStreamDatumFilteredResultsProcessorTests.java - 1/05/2022 6:02:55 pm
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

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonMap;
import static net.solarnetwork.dao.StreamDatumFilteredResultsProcessor.METADATA_PROVIDER_ATTR;
import static net.solarnetwork.domain.datum.BasicObjectDatumStreamDataSet.dataSet;
import static net.solarnetwork.domain.datum.DatumProperties.propertiesOf;
import static net.solarnetwork.domain.datum.DatumPropertiesStatistics.statisticsOf;
import static net.solarnetwork.util.NumberUtils.decimalArray;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serial;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import net.solarnetwork.codec.jackson.JsonUtils;
import net.solarnetwork.codec.jackson.ObjectMapperStreamDatumFilteredResultsProcessor;
import net.solarnetwork.domain.datum.AggregateStreamDatum;
import net.solarnetwork.domain.datum.BasicAggregateStreamDatum;
import net.solarnetwork.domain.datum.BasicObjectDatumStreamDataSet;
import net.solarnetwork.domain.datum.BasicObjectDatumStreamMetadata;
import net.solarnetwork.domain.datum.BasicStreamDatum;
import net.solarnetwork.domain.datum.DatumProperties;
import net.solarnetwork.domain.datum.DatumPropertiesStatistics;
import net.solarnetwork.domain.datum.ObjectDatumKind;
import net.solarnetwork.domain.datum.ObjectDatumStreamMetadata;
import net.solarnetwork.domain.datum.ReadingStreamDatum;
import net.solarnetwork.domain.datum.StreamDatum;
import net.solarnetwork.util.ByteUtils;
import tools.jackson.core.exc.StreamWriteException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.cbor.CBORMapper;

/**
 * Test cases for the {@link ObjectMapperStreamDatumFilteredResultsProcessor}
 * class.
 *
 * <p>
 * Each test runs twice, generating JSON and then CBOR. The CBOR output is
 * converted to JSON for comparison, so both forms are compared to the same
 * expected JSON.
 * </p>
 *
 * @author matt
 * @version 1.0
 */
@RunWith(Parameterized.class)
public class ObjectMapperStreamDatumFilteredResultsProcessorTests {

	private static final MimeType CBOR_MIME_TYPE = MimeType.valueOf("application/cbor");

	/** A reading datum implementation for testing. */
	private static final class TestReadingStreamDatum extends BasicAggregateStreamDatum
			implements ReadingStreamDatum {

		@Serial
		private static final long serialVersionUID = 2817213429512637446L;

		private TestReadingStreamDatum(UUID streamId, Instant timestamp, DatumProperties properties,
				@Nullable Instant endTimestamp, DatumPropertiesStatistics statistics) {
			super(streamId, timestamp, properties, endTimestamp, statistics);
		}

	}

	@Parameter
	public boolean useCbor;

	/**
	 * Get the test parameters.
	 *
	 * @return the parameters: {@code false} for JSON, {@code true} for CBOR
	 */
	@Parameters(name = "cbor={0}")
	public static Collection<Object[]> params() {
		return asList(new Object[] { false }, new Object[] { true });
	}

	private ObjectMapper mapper;

	private ObjectMapper createObjectMapper() {
		return (useCbor ? CBORMapper.shared() : JsonMapper.shared());
	}

	@Before
	public void setup() {
		mapper = createObjectMapper();
	}

	private ObjectMapperStreamDatumFilteredResultsProcessor createProcessor(OutputStream out) {
		return createProcessor(out, null);
	}

	private ObjectMapperStreamDatumFilteredResultsProcessor createProcessor(OutputStream out,
			@Nullable Set<String> allowedPropertyNames) {
		return new ObjectMapperStreamDatumFilteredResultsProcessor(mapper.createGenerator(out),
				mapper._serializationContext(),
				(useCbor ? CBOR_MIME_TYPE : MimeTypeUtils.APPLICATION_JSON), allowedPropertyNames);
	}

	private ObjectDatumStreamMetadata nodeMeta(Long nodeId, String sourceId, String[] i, String[] a,
			String[] s) {
		return new BasicObjectDatumStreamMetadata(UUID.randomUUID(), "Pacific/Auckland",
				ObjectDatumKind.Node, nodeId, sourceId, i, a, s);
	}

	private String resultJson(ByteArrayOutputStream out) throws IOException {
		if ( useCbor ) {
			return JsonUtils.getJSONString(mapper.readTree(out.toByteArray()));
		} else {
			return out.toString(ByteUtils.UTF8);
		}
	}

	@Test
	public void mimeType() throws IOException {
		// GIVEN
		BasicObjectDatumStreamDataSet<StreamDatum> data = dataSet(emptyList(), emptyList());

		try (ObjectMapperStreamDatumFilteredResultsProcessor processor = createProcessor(
				new ByteArrayOutputStream())) {
			processor.start(null, null, null, singletonMap(METADATA_PROVIDER_ATTR, data));

			// THEN
			then(processor.getMimeType()).as("MIME type is from constructor")
					.isEqualTo(useCbor ? CBOR_MIME_TYPE : MimeTypeUtils.APPLICATION_JSON);
		}
	}

	@Test
	public void start_noMetadataProvider() throws IOException {
		// GIVEN
		BasicObjectDatumStreamDataSet<StreamDatum> data = dataSet(emptyList(), emptyList());

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (ObjectMapperStreamDatumFilteredResultsProcessor processor = createProcessor(out)) {

			// THEN
			thenThrownBy(() -> processor.start(null, null, null, null))
					.as("Metadata provider is required").isInstanceOf(IllegalArgumentException.class);
			thenThrownBy(() -> processor.start(null, null, null, singletonMap("foo", "bar")))
					.as("Metadata provider is required in attributes")
					.isInstanceOf(IllegalArgumentException.class);

			processor.flush();
			then(out.toByteArray()).as("Nothing generated by failed start").isEmpty();

			// close() expects a successful start
			processor.start(null, null, null, singletonMap(METADATA_PROVIDER_ATTR, data));
		}
	}

	@Test
	public void noStreams() throws IOException {
		// GIVEN
		BasicObjectDatumStreamDataSet<StreamDatum> data = dataSet(emptyList(), emptyList());

		// WHEN
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (ObjectMapperStreamDatumFilteredResultsProcessor processor = createProcessor(out)) {
			processor.start(null, null, null, singletonMap(METADATA_PROVIDER_ATTR, data));
		}

		// THEN
		final String json = resultJson(out);
		then(json).as("Only success provided when no streams").isEqualTo("""
				{"success":true}""");
	}

	@Test
	public void oneStream_datum() throws IOException {
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
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (ObjectMapperStreamDatumFilteredResultsProcessor processor = createProcessor(out)) {
			processor.start(null, null, null, singletonMap(METADATA_PROVIDER_ATTR, data));
			processor.handleResultItem(d1);
			processor.handleResultItem(d2);
		}

		// THEN
		final String json = resultJson(out);
		then(json).as("Datum JSON").isEqualTo("""
				{"success":true,"meta":[\
				{"streamId":"%s","zone":"Pacific/Auckland","kind":"n","objectId":123,\
				"sourceId":"test/source","i":["a","b"],"a":["c"],"s":["d"]}\
				],"data":[\
				[0,1651197120000,1.23,2.34,3.45,"foo","a"],\
				[0,1651197121000,3.21,4.32,5.43,"bar"]\
				]}""".formatted(meta.getStreamId()));
	}

	@Test
	public void oneStream_datum_counts() throws IOException {
		// GIVEN
		ObjectDatumStreamMetadata meta = nodeMeta(123L, "test/source", new String[] { "a" }, null, null);
		Instant start = Instant
				.from(LocalDateTime.of(2022, 4, 29, 13, 52).atZone(ZoneId.of("Pacific/Auckland")));

		DatumProperties p1 = new DatumProperties();
		p1.setInstantaneous(decimalArray("1.23"));
		StreamDatum d1 = new BasicStreamDatum(meta.getStreamId(), start, p1);

		BasicObjectDatumStreamDataSet<StreamDatum> data = dataSet(asList(meta), asList(d1));

		// WHEN
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (ObjectMapperStreamDatumFilteredResultsProcessor processor = createProcessor(out)) {
			processor.start(10L, 5, 1, singletonMap(METADATA_PROVIDER_ATTR, data));
			processor.handleResultItem(d1);
		}

		// THEN
		final String json = resultJson(out);
		then(json).as("Result counts provided before metadata").isEqualTo("""
				{"success":true,"returnedResultCount":1,"startingOffset":5,"totalResultCount":10,\
				"meta":[\
				{"streamId":"%s","zone":"Pacific/Auckland","kind":"n","objectId":123,\
				"sourceId":"test/source","i":["a"]}\
				],"data":[\
				[0,1651197120000,1.23]\
				]}""".formatted(meta.getStreamId()));
	}

	@Test
	public void oneStream_datum_propertyRestricted() throws IOException {
		// GIVEN
		final ObjectDatumStreamMetadata meta = nodeMeta(123L, "test/source", new String[] { "a", "b" },
				new String[] { "c" }, new String[] { "d" });
		final Instant start = Instant
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

		final BasicObjectDatumStreamDataSet<StreamDatum> data = dataSet(asList(meta), asList(d1, d2));

		final Set<String> allowedProperties = Set.of("b", "c");

		// WHEN
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (ObjectMapperStreamDatumFilteredResultsProcessor processor = createProcessor(out,
				allowedProperties)) {
			processor.start(null, null, null, singletonMap(METADATA_PROVIDER_ATTR, data));
			processor.handleResultItem(d1);
			processor.handleResultItem(d2);
		}

		// THEN
		final String json = resultJson(out);
		then(json).as("Meta index, ts, restricted datum properties, and tags provided").isEqualTo("""
				{"success":true,"meta":[\
				{"streamId":"%s","zone":"Pacific/Auckland","kind":"n","objectId":123,\
				"sourceId":"test/source","i":["b"],"a":["c"]}\
				],"data":[\
				[0,1651197120000,2.34,3.45,"a"],\
				[0,1651197121000,4.32,5.43]\
				]}""".formatted(meta.getStreamId()));
	}

	@Test
	public void oneStream_datum_propertyRestricted_missingValue() throws IOException {
		// GIVEN
		final ObjectDatumStreamMetadata meta = nodeMeta(123L, "test/source", new String[] { "a", "b" },
				new String[] { "c" }, new String[] { "d" });
		final Instant start = Instant
				.from(LocalDateTime.of(2022, 4, 29, 13, 52).atZone(ZoneId.of("Pacific/Auckland")));

		DatumProperties p1 = new DatumProperties();
		p1.setInstantaneous(decimalArray("1.23", "2.34"));
		p1.setAccumulating(decimalArray("3.45"));
		p1.setStatus(new String[] { "foo" });
		p1.setTags(new String[] { "a" });
		StreamDatum d1 = new BasicStreamDatum(meta.getStreamId(), start, p1);

		// this datum has no "b" or "c" values
		DatumProperties p2 = new DatumProperties();
		p2.setInstantaneous(decimalArray("3.21"));
		p2.setStatus(new String[] { "bar" });
		StreamDatum d2 = new BasicStreamDatum(meta.getStreamId(), start.plusSeconds(1), p2);

		final BasicObjectDatumStreamDataSet<StreamDatum> data = dataSet(asList(meta), asList(d1, d2));

		final Set<String> allowedProperties = Set.of("b", "c");

		// WHEN
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (ObjectMapperStreamDatumFilteredResultsProcessor processor = createProcessor(out,
				allowedProperties)) {
			processor.start(null, null, null, singletonMap(METADATA_PROVIDER_ATTR, data));
			processor.handleResultItem(d1);
			processor.handleResultItem(d2);
		}

		// THEN
		final String json = resultJson(out);
		then(json).as("Missing restricted property values provided as null").isEqualTo("""
				{"success":true,"meta":[\
				{"streamId":"%s","zone":"Pacific/Auckland","kind":"n","objectId":123,\
				"sourceId":"test/source","i":["b"],"a":["c"]}\
				],"data":[\
				[0,1651197120000,2.34,3.45,"a"],\
				[0,1651197121000,null,null]\
				]}""".formatted(meta.getStreamId()));
	}

	@Test
	public void oneStream_datum_propertyRestricted_empty() throws IOException {
		// GIVEN
		final ObjectDatumStreamMetadata meta = nodeMeta(123L, "test/source", new String[] { "a", "b" },
				new String[] { "c" }, new String[] { "d" });
		final Instant start = Instant
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

		final BasicObjectDatumStreamDataSet<StreamDatum> data = dataSet(asList(meta), asList(d1, d2));

		final Set<String> allowedProperties = Set.of("foo");

		// WHEN
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (ObjectMapperStreamDatumFilteredResultsProcessor processor = createProcessor(out,
				allowedProperties)) {
			processor.start(null, null, null, singletonMap(METADATA_PROVIDER_ATTR, data));
			processor.handleResultItem(d1);
			processor.handleResultItem(d2);
		}

		// THEN
		final String json = resultJson(out);
		then(json).as("Meta index, ts, and tags provided").isEqualTo("""
				{"success":true,"meta":[\
				{"streamId":"%s","zone":"Pacific/Auckland","kind":"n","objectId":123,\
				"sourceId":"test/source"}\
				],"data":[\
				[0,1651197120000,"a"],\
				[0,1651197121000]\
				]}""".formatted(meta.getStreamId()));
	}

	@Test
	public void multiStream_datum() throws IOException {
		// GIVEN
		ObjectDatumStreamMetadata meta1 = nodeMeta(123L, "test/source/1", new String[] { "a", "b" },
				new String[] { "c" }, new String[] { "d" });
		ObjectDatumStreamMetadata meta2 = nodeMeta(123L, "test/source/2", new String[] { "e" }, null,
				null);
		Instant start = Instant
				.from(LocalDateTime.of(2022, 4, 29, 13, 52).atZone(ZoneId.of("Pacific/Auckland")));

		DatumProperties p1 = new DatumProperties();
		p1.setInstantaneous(decimalArray("1.1", "1.2"));
		p1.setAccumulating(decimalArray("1.3"));
		p1.setStatus(new String[] { "foo" });
		StreamDatum d1 = new BasicStreamDatum(meta1.getStreamId(), start, p1);

		DatumProperties p2 = new DatumProperties();
		p2.setInstantaneous(decimalArray("10.1"));
		p2.setTags(new String[] { "t1", "t2" });
		StreamDatum d2 = new BasicStreamDatum(meta2.getStreamId(), start, p2);

		DatumProperties p3 = new DatumProperties();
		p3.setInstantaneous(decimalArray("2.1", "2.2"));
		p3.setAccumulating(decimalArray("2.3"));
		StreamDatum d3 = new BasicStreamDatum(meta1.getStreamId(), start.plusSeconds(1), p3);

		BasicObjectDatumStreamDataSet<StreamDatum> data = dataSet(asList(meta1, meta2),
				asList(d1, d2, d3));

		// WHEN
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (ObjectMapperStreamDatumFilteredResultsProcessor processor = createProcessor(out)) {
			processor.start(null, null, null, singletonMap(METADATA_PROVIDER_ATTR, data));
			processor.handleResultItem(d1);
			processor.handleResultItem(d2);
			processor.handleResultItem(d3);
		}

		// THEN
		final String json = resultJson(out);
		then(json).as("Each datum refers to its stream's metadata index").isEqualTo("""
				{"success":true,"meta":[\
				{"streamId":"%s","zone":"Pacific/Auckland","kind":"n","objectId":123,\
				"sourceId":"test/source/1","i":["a","b"],"a":["c"],"s":["d"]},\
				{"streamId":"%s","zone":"Pacific/Auckland","kind":"n","objectId":123,\
				"sourceId":"test/source/2","i":["e"]}\
				],"data":[\
				[0,1651197120000,1.1,1.2,1.3,"foo"],\
				[1,1651197120000,10.1,"t1","t2"],\
				[0,1651197121000,2.1,2.2,2.3,null]\
				]}""".formatted(meta1.getStreamId(), meta2.getStreamId()));
	}

	@Test
	public void handleResultItem_unknownStream() throws IOException {
		// GIVEN
		ObjectDatumStreamMetadata meta = nodeMeta(123L, "test/source", new String[] { "a" }, null, null);
		BasicObjectDatumStreamDataSet<StreamDatum> data = dataSet(asList(meta), emptyList());

		DatumProperties p1 = new DatumProperties();
		p1.setInstantaneous(decimalArray("1.23"));
		StreamDatum d1 = new BasicStreamDatum(UUID.randomUUID(), Instant.now(), p1);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (ObjectMapperStreamDatumFilteredResultsProcessor processor = createProcessor(out)) {
			processor.start(null, null, null, singletonMap(METADATA_PROVIDER_ATTR, data));

			// THEN
			thenThrownBy(() -> processor.handleResultItem(d1))
					.as("Datum without metadata cannot be written")
					.isInstanceOf(StreamWriteException.class)
					.hasMessageContaining(d1.getStreamId().toString());
		}
	}

	@Test
	public void oneStream_aggregate() throws IOException {
		// GIVEN
		ObjectDatumStreamMetadata meta = nodeMeta(123L, "test/source", new String[] { "a", "b" },
				new String[] { "c" }, new String[] { "d" });
		ZonedDateTime start = LocalDateTime.of(2022, 4, 29, 13, 52)
				.atZone(ZoneId.of("Pacific/Auckland"));

		// @formatter:off
		AggregateStreamDatum d1 = new BasicAggregateStreamDatum(meta.getStreamId(),
				start.minusMinutes(1).toInstant(),
				propertiesOf(
						decimalArray("1.23", "2.34"),
						decimalArray("3"),
						new String[] {"foo"},
						new String[] {"wham", "bam"}),
				null,
				statisticsOf(new BigDecimal[][] {
						decimalArray("10", "1.0", "2.0"),
						decimalArray("10", "2.0", "3.0")
					}, new BigDecimal[][] {
						decimalArray("30", "100", "130")
					}
				)
			);
		// @formatter:on

		BasicObjectDatumStreamDataSet<StreamDatum> data = dataSet(asList(meta), asList(d1));

		// WHEN
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (ObjectMapperStreamDatumFilteredResultsProcessor processor = createProcessor(out)) {
			processor.start(null, null, null, singletonMap(METADATA_PROVIDER_ATTR, data));
			processor.handleResultItem(d1);
		}

		// THEN
		final String json = resultJson(out);
		then(json).as("Meta index, ts range, and datum properties provided").isEqualTo("""
				{"success":true,"meta":[\
				{"streamId":"%s","zone":"Pacific/Auckland","kind":"n","objectId":123,\
				"sourceId":"test/source","i":["a","b"],"a":["c"],"s":["d"]}\
				],"data":[\
				[0,[1651197060000,null],[1.23,10,1.0,2.0],[2.34,10,2.0,3.0],[3,100,130],\
				"foo","wham","bam"]\
				]}""".formatted(meta.getStreamId()));
	}

	@Test
	public void oneStream_aggregate_endTimestamp() throws IOException {
		// GIVEN
		ObjectDatumStreamMetadata meta = nodeMeta(123L, "test/source", new String[] { "a" }, null, null);
		ZonedDateTime start = LocalDateTime.of(2022, 4, 29, 13, 0).atZone(ZoneId.of("Pacific/Auckland"));

		// @formatter:off
		AggregateStreamDatum d1 = new BasicAggregateStreamDatum(meta.getStreamId(),
				start.toInstant(),
				propertiesOf(decimalArray("1.23"), null, null, null),
				start.plusHours(1).toInstant(),
				statisticsOf(new BigDecimal[][] {
						decimalArray("10", "1.0", "2.0")
					}, null
				)
			);
		// @formatter:on

		BasicObjectDatumStreamDataSet<StreamDatum> data = dataSet(asList(meta), asList(d1));

		// WHEN
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (ObjectMapperStreamDatumFilteredResultsProcessor processor = createProcessor(out)) {
			processor.start(null, null, null, singletonMap(METADATA_PROVIDER_ATTR, data));
			processor.handleResultItem(d1);
		}

		// THEN
		final String json = resultJson(out);
		then(json).as("Ending timestamp only provided for readings, not other aggregates").isEqualTo("""
				{"success":true,"meta":[\
				{"streamId":"%s","zone":"Pacific/Auckland","kind":"n","objectId":123,\
				"sourceId":"test/source","i":["a"]}\
				],"data":[\
				[0,[1651194000000,null],[1.23,10,1.0,2.0]]\
				]}""".formatted(meta.getStreamId()));
	}

	@Test
	public void oneStream_aggregate_propertyRestricted() throws IOException {
		// GIVEN
		final ObjectDatumStreamMetadata meta = nodeMeta(123L, "test/source", new String[] { "a", "b" },
				new String[] { "c" }, new String[] { "d" });
		final ZonedDateTime start = LocalDateTime.of(2022, 4, 29, 13, 52)
				.atZone(ZoneId.of("Pacific/Auckland"));

		// @formatter:off
		final AggregateStreamDatum d1 = new BasicAggregateStreamDatum(meta.getStreamId(),
				start.minusMinutes(1).toInstant(),
				propertiesOf(
						decimalArray("1.23", "2.34"),
						decimalArray("3"),
						new String[] {"foo"},
						new String[] {"wham", "bam"}),
				null,
				statisticsOf(new BigDecimal[][] {
						decimalArray("10", "1.0", "2.0"),
						decimalArray("10", "2.0", "3.0")
					}, new BigDecimal[][] {
						decimalArray("30", "100", "130")
					}
				)
			);
		// @formatter:on

		final BasicObjectDatumStreamDataSet<StreamDatum> data = dataSet(asList(meta), asList(d1));

		final Set<String> allowedProperties = Set.of("b", "c");

		// WHEN
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (ObjectMapperStreamDatumFilteredResultsProcessor processor = createProcessor(out,
				allowedProperties)) {
			processor.start(null, null, null, singletonMap(METADATA_PROVIDER_ATTR, data));
			processor.handleResultItem(d1);
		}

		// THEN
		final String json = resultJson(out);
		then(json).as("Meta index, ts range, restricted datum properties, and tags provided")
				.isEqualTo("""
						{"success":true,"meta":[\
						{"streamId":"%s","zone":"Pacific/Auckland","kind":"n","objectId":123,\
						"sourceId":"test/source","i":["b"],"a":["c"]}\
						],"data":[\
						[0,[1651197060000,null],[2.34,10,2.0,3.0],[3,100,130],"wham","bam"]\
						]}""".formatted(meta.getStreamId()));
	}

	@Test
	public void oneStream_aggregate_propertyRestricted_empty() throws IOException {
		// GIVEN
		final ObjectDatumStreamMetadata meta = nodeMeta(123L, "test/source", new String[] { "a", "b" },
				new String[] { "c" }, new String[] { "d" });
		final ZonedDateTime start = LocalDateTime.of(2022, 4, 29, 13, 52)
				.atZone(ZoneId.of("Pacific/Auckland"));

		// @formatter:off
		final AggregateStreamDatum d1 = new BasicAggregateStreamDatum(meta.getStreamId(),
				start.minusMinutes(1).toInstant(),
				propertiesOf(
						decimalArray("1.23", "2.34"),
						decimalArray("3"),
						new String[] {"foo"},
						new String[] {"wham", "bam"}),
				null,
				statisticsOf(new BigDecimal[][] {
						decimalArray("10", "1.0", "2.0"),
						decimalArray("10", "2.0", "3.0")
					}, new BigDecimal[][] {
						decimalArray("30", "100", "130")
					}
				)
			);
		// @formatter:on

		final BasicObjectDatumStreamDataSet<StreamDatum> data = dataSet(asList(meta), asList(d1));

		final Set<String> allowedProperties = Set.of("foo");

		// WHEN
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (ObjectMapperStreamDatumFilteredResultsProcessor processor = createProcessor(out,
				allowedProperties)) {
			processor.start(null, null, null, singletonMap(METADATA_PROVIDER_ATTR, data));
			processor.handleResultItem(d1);
		}

		// THEN
		final String json = resultJson(out);
		then(json).as("Meta index, ts range, and tags provided").isEqualTo("""
				{"success":true,"meta":[\
				{"streamId":"%s","zone":"Pacific/Auckland","kind":"n","objectId":123,\
				"sourceId":"test/source"}\
				],"data":[\
				[0,[1651197060000,null],"wham","bam"]\
				]}""".formatted(meta.getStreamId()));
	}

	@Test
	public void oneStream_aggregate_missingAccumulatingStats() throws IOException {
		// GIVEN
		ObjectDatumStreamMetadata meta = nodeMeta(123L, "test/source", new String[] { "a", "b" },
				new String[] { "c" }, new String[] { "d" });
		ZonedDateTime start = LocalDateTime.of(2022, 4, 29, 13, 52)
				.atZone(ZoneId.of("Pacific/Auckland"));

		// @formatter:off
		AggregateStreamDatum d1 = new BasicAggregateStreamDatum(meta.getStreamId(),
				start.minusMinutes(1).toInstant(),
				propertiesOf(
						decimalArray("1.23", "2.34"),
						decimalArray("3"),
						new String[] {"foo"},
						new String[] {"wham", "bam"}),
				null,
				statisticsOf(new BigDecimal[][] {
						decimalArray("10", "1.0", "2.0"),
						decimalArray("10", "2.0", "3.0")
					}, null
				)
			);
		// @formatter:on

		BasicObjectDatumStreamDataSet<StreamDatum> data = dataSet(asList(meta), asList(d1));

		// WHEN
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (ObjectMapperStreamDatumFilteredResultsProcessor processor = createProcessor(out)) {
			processor.start(null, null, null, singletonMap(METADATA_PROVIDER_ATTR, data));
			processor.handleResultItem(d1);
		}

		// THEN
		final String json = resultJson(out);
		then(json).as(
				"Meta index, ts range, and datum properties provided (minus missing accumulating stats)")
				.isEqualTo("""
						{"success":true,"meta":[\
						{"streamId":"%s","zone":"Pacific/Auckland","kind":"n","objectId":123,\
						"sourceId":"test/source","i":["a","b"],"a":["c"],"s":["d"]}\
						],"data":[\
						[0,[1651197060000,null],[1.23,10,1.0,2.0],[2.34,10,2.0,3.0],[3],\
						"foo","wham","bam"]\
						]}""".formatted(meta.getStreamId()));
	}

	@Test
	public void oneStream_reading() throws IOException {
		// GIVEN
		ObjectDatumStreamMetadata meta = nodeMeta(123L, "test/source", new String[] { "a", "b" },
				new String[] { "c" }, new String[] { "d" });
		ZonedDateTime start = LocalDateTime.of(2022, 4, 29, 13, 52)
				.atZone(ZoneId.of("Pacific/Auckland"));

		// @formatter:off
		ReadingStreamDatum d1 = new TestReadingStreamDatum(meta.getStreamId(),
				start.minusMinutes(1).toInstant(),
				propertiesOf(
						decimalArray("1.23", "2.34"),
						decimalArray("3"), null, null),
				start.plusHours(1).minusMinutes(1).toInstant(),
				statisticsOf(new BigDecimal[][] {
					decimalArray("10", "1.0", "2.0"),
					decimalArray("10", "2.0", "3.0")
				}, new BigDecimal[][] {
					decimalArray("30", "100", "130")
				}));
		// @formatter:on

		BasicObjectDatumStreamDataSet<StreamDatum> data = dataSet(asList(meta), asList(d1));

		// WHEN
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (ObjectMapperStreamDatumFilteredResultsProcessor processor = createProcessor(out)) {
			processor.start(null, null, null, singletonMap(METADATA_PROVIDER_ATTR, data));
			processor.handleResultItem(d1);
		}

		// THEN
		final String json = resultJson(out);
		then(json).as("Meta index, ts range, and datum properties provided").isEqualTo("""
				{"success":true,"meta":[\
				{"streamId":"%s","zone":"Pacific/Auckland","kind":"n","objectId":123,\
				"sourceId":"test/source","i":["a","b"],"a":["c"],"s":["d"]}\
				],"data":[\
				[0,[1651197060000,1651200660000],[1.23,10,1.0,2.0],[2.34,10,2.0,3.0],[30,100,130],\
				null]\
				]}""".formatted(meta.getStreamId()));
	}

	@Test
	public void oneStream_reading_propertyRestricted() throws IOException {
		// GIVEN
		final ObjectDatumStreamMetadata meta = nodeMeta(123L, "test/source", new String[] { "a", "b" },
				new String[] { "c" }, new String[] { "d" });
		final ZonedDateTime start = LocalDateTime.of(2022, 4, 29, 13, 52)
				.atZone(ZoneId.of("Pacific/Auckland"));

		// @formatter:off
		final ReadingStreamDatum d1 = new TestReadingStreamDatum(meta.getStreamId(),
				start.minusMinutes(1).toInstant(),
				propertiesOf(
						decimalArray("1.23", "2.34"),
						decimalArray("3"), null, null),
				start.plusHours(1).minusMinutes(1).toInstant(),
				statisticsOf(new BigDecimal[][] {
					decimalArray("10", "1.0", "2.0"),
					decimalArray("10", "2.0", "3.0")
				}, new BigDecimal[][] {
					decimalArray("30", "100", "130")
				}));
		// @formatter:on

		final BasicObjectDatumStreamDataSet<StreamDatum> data = dataSet(asList(meta), asList(d1));

		final Set<String> allowedProperties = Set.of("b", "c");

		// WHEN
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (ObjectMapperStreamDatumFilteredResultsProcessor processor = createProcessor(out,
				allowedProperties)) {
			processor.start(null, null, null, singletonMap(METADATA_PROVIDER_ATTR, data));
			processor.handleResultItem(d1);
		}

		// THEN
		final String json = resultJson(out);
		then(json).as("Meta index, ts range, and restricted datum properties provided").isEqualTo("""
				{"success":true,"meta":[\
				{"streamId":"%s","zone":"Pacific/Auckland","kind":"n","objectId":123,\
				"sourceId":"test/source","i":["b"],"a":["c"]}\
				],"data":[\
				[0,[1651197060000,1651200660000],[2.34,10,2.0,3.0],[30,100,130]]\
				]}""".formatted(meta.getStreamId()));
	}

	@Test
	public void oneStream_reading_propertyRestricted_empty() throws IOException {
		// GIVEN
		final ObjectDatumStreamMetadata meta = nodeMeta(123L, "test/source", new String[] { "a", "b" },
				new String[] { "c" }, new String[] { "d" });
		final ZonedDateTime start = LocalDateTime.of(2022, 4, 29, 13, 52)
				.atZone(ZoneId.of("Pacific/Auckland"));

		// @formatter:off
		final ReadingStreamDatum d1 = new TestReadingStreamDatum(meta.getStreamId(),
				start.minusMinutes(1).toInstant(),
				propertiesOf(
						decimalArray("1.23", "2.34"),
						decimalArray("3"), null, null),
				start.plusHours(1).minusMinutes(1).toInstant(),
				statisticsOf(new BigDecimal[][] {
					decimalArray("10", "1.0", "2.0"),
					decimalArray("10", "2.0", "3.0")
				}, new BigDecimal[][] {
					decimalArray("30", "100", "130")
				}));
		// @formatter:on

		final BasicObjectDatumStreamDataSet<StreamDatum> data = dataSet(asList(meta), asList(d1));

		final Set<String> allowedProperties = Set.of("foo");

		// WHEN
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (ObjectMapperStreamDatumFilteredResultsProcessor processor = createProcessor(out,
				allowedProperties)) {
			processor.start(null, null, null, singletonMap(METADATA_PROVIDER_ATTR, data));
			processor.handleResultItem(d1);
		}

		// THEN
		final String json = resultJson(out);
		then(json).as("Meta index, and ts range provided").isEqualTo("""
				{"success":true,"meta":[\
				{"streamId":"%s","zone":"Pacific/Auckland","kind":"n","objectId":123,\
				"sourceId":"test/source"}\
				],"data":[\
				[0,[1651197060000,1651200660000]]\
				]}""".formatted(meta.getStreamId()));
	}

	@Test
	public void oneStream_reading_missingAccumulatingStats() throws IOException {
		// GIVEN
		ObjectDatumStreamMetadata meta = nodeMeta(123L, "test/source", new String[] { "a", "b" },
				new String[] { "c" }, new String[] { "d" });
		ZonedDateTime start = LocalDateTime.of(2022, 4, 29, 13, 52)
				.atZone(ZoneId.of("Pacific/Auckland"));

		// @formatter:off
		ReadingStreamDatum d1 = new TestReadingStreamDatum(meta.getStreamId(),
				start.minusMinutes(1).toInstant(),
				propertiesOf(
						decimalArray("1.23", "2.34"),
						decimalArray("3"), null, null),
				start.plusHours(1).minusMinutes(1).toInstant(),
				statisticsOf(new BigDecimal[][] {
					decimalArray("10", "1.0", "2.0"),
					decimalArray("10", "2.0", "3.0")
				}, null));
		// @formatter:on

		BasicObjectDatumStreamDataSet<StreamDatum> data = dataSet(asList(meta), asList(d1));

		// WHEN
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (ObjectMapperStreamDatumFilteredResultsProcessor processor = createProcessor(out)) {
			processor.start(null, null, null, singletonMap(METADATA_PROVIDER_ATTR, data));
			processor.handleResultItem(d1);
		}

		// THEN
		final String json = resultJson(out);
		then(json).as(
				"Meta index, ts range, and datum properties provided, without accumulating as stats missing")
				.isEqualTo("""
						{"success":true,"meta":[\
						{"streamId":"%s","zone":"Pacific/Auckland","kind":"n","objectId":123,\
						"sourceId":"test/source","i":["a","b"],"a":["c"],"s":["d"]}\
						],"data":[\
						[0,[1651197060000,1651200660000],[1.23,10,1.0,2.0],[2.34,10,2.0,3.0],null,null]\
						]}""".formatted(meta.getStreamId()));
	}

}
