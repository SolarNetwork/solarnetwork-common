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
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import net.solarnetwork.codec.ObjectDatumStreamDataDeserializer;
import net.solarnetwork.domain.datum.DatumSamplesType;
import net.solarnetwork.domain.datum.ObjectDatumKind;
import net.solarnetwork.domain.datum.ObjectDatumStreamData;
import net.solarnetwork.domain.datum.ObjectDatumStreamMetadata;
import net.solarnetwork.domain.datum.StreamDatum;

/**
 * Test cases for the {@link ObjectDatumStreamDataDeserializer} class.
 * 
 * @author matt
 * @version 1.0
 */
public class ObjectDatumStreamDataDeserializerTests {

	private ObjectMapper mapper;

	private ObjectMapper createObjectMapper() {
		ObjectMapper m = new ObjectMapper();
		SimpleModule mod = new SimpleModule("Test");
		mod.addDeserializer(ObjectDatumStreamData.class, ObjectDatumStreamDataDeserializer.INSTANCE);
		m.registerModule(mod);
		return m;
	}

	@Before
	public void setup() {
		mapper = createObjectMapper();
	}

	@Test
	public void deserialize_typical() throws IOException {
		// GIVEN
		UUID streamId = UUID.randomUUID();
		String json = format("{\"meta\":{\"streamId\":\"%s\",", streamId)
				+ "\"zone\":\"Pacific/Auckland\",\"kind\":\"n\",\"objectId\":123,"
				+ "\"sourceId\":\"test/source\",\"i\":[\"a\",\"b\"],\"a\":[\"c\"],\"s\":[\"d\"]},"
				+ "\"data\":[[1651197120000,1.23,2.34,3.45,\"foo\",\"a\"],"
				+ "[1651197121000,3.21,4.32,5.43,\"bar\"]]}";
		Instant start = Instant
				.from(LocalDateTime.of(2022, 4, 29, 13, 52).atZone(ZoneId.of("Pacific/Auckland")));

		// WHEN
		ObjectDatumStreamData result = mapper.readValue(json, ObjectDatumStreamData.class);

		// THEN
		assertThat("Result parsed", result, is(notNullValue()));

		assertThat("Metadata parsed", result.getMetadata(), is(notNullValue()));
		ObjectDatumStreamMetadata meta = result.getMetadata();
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

		assertThat("Data parsed", result.getData(), hasSize(2));

		StreamDatum d = result.getData().get(0);
		assertThat("Datum parsed", d, is(notNullValue()));
		assertThat("Datum timestamp", d.getTimestamp(), is(start));
		assertThat("Datum properties parsed", d.getProperties(), is(notNullValue()));
		assertThat("Datum instantaneous values", d.getProperties().getInstantaneous(),
				is(arrayContaining(decimalArray("1.23", "2.34"))));
		assertThat("Datum accumulating values", d.getProperties().getAccumulating(),
				is(arrayContaining(decimalArray("3.45"))));
		assertThat("Datum status values", d.getProperties().getStatus(), is(arrayContaining("foo")));
		assertThat("Datum tag values", d.getProperties().getTags(), is(arrayContaining("a")));

		d = result.getData().get(1);
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
	public void deserialize_missingData() throws IOException {
		// GIVEN
		UUID streamId = UUID.randomUUID();
		String json = format("{\"meta\":{\"streamId\":\"%s\",", streamId)
				+ "\"zone\":\"Pacific/Auckland\",\"kind\":\"n\",\"objectId\":123,"
				+ "\"sourceId\":\"test/source\",\"i\":[\"a\",\"b\"],\"a\":[\"c\"],\"s\":[\"d\"]},"
				+ "\"data\":[[1651197120000,null,null,3.45,\"foo\",\"a\"],"
				+ "[1651197121000,null,4.32,null,null]]}";
		Instant start = Instant
				.from(LocalDateTime.of(2022, 4, 29, 13, 52).atZone(ZoneId.of("Pacific/Auckland")));

		// WHEN
		ObjectDatumStreamData result = mapper.readValue(json, ObjectDatumStreamData.class);

		// THEN
		assertThat("Result parsed", result, is(notNullValue()));

		assertThat("Metadata parsed", result.getMetadata(), is(notNullValue()));
		ObjectDatumStreamMetadata meta = result.getMetadata();
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

		assertThat("Data parsed", result.getData(), hasSize(2));

		StreamDatum d = result.getData().get(0);
		assertThat("Datum parsed", d, is(notNullValue()));
		assertThat("Datum timestamp", d.getTimestamp(), is(start));
		assertThat("Datum properties parsed", d.getProperties(), is(notNullValue()));
		assertThat("Datum instantaneous values", d.getProperties().getInstantaneous(),
				is(arrayContaining(decimalArray(null, null))));
		assertThat("Datum accumulating values", d.getProperties().getAccumulating(),
				is(arrayContaining(decimalArray("3.45"))));
		assertThat("Datum status values", d.getProperties().getStatus(), is(arrayContaining("foo")));
		assertThat("Datum tag values", d.getProperties().getTags(), is(arrayContaining("a")));

		d = result.getData().get(1);
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
}
