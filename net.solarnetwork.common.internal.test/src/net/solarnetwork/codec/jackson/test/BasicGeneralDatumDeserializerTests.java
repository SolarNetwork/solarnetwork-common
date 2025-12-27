/* ==================================================================
 * BasicGeneralDatumDeserializerTests.java - 17/08/2021 2:50:32 PM
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

import static org.assertj.core.api.BDDAssertions.from;
import static org.assertj.core.api.BDDAssertions.then;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import net.solarnetwork.codec.jackson.BasicGeneralDatumDeserializer;
import net.solarnetwork.domain.datum.Datum;
import net.solarnetwork.domain.datum.DatumSamples;
import net.solarnetwork.domain.datum.GeneralDatum;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

/**
 * Test cases for the {@link BasicGeneralDatumDeserializer} class.
 *
 * @author matt
 * @version 1.0
 */
public class BasicGeneralDatumDeserializerTests {

	private ObjectMapper mapper;

	private ObjectMapper createObjectMapper() {
		SimpleModule mod = new SimpleModule("Test");
		mod.addDeserializer(Datum.class, BasicGeneralDatumDeserializer.INSTANCE);
		return JsonMapper.builder().addModule(mod).build();
	}

	@Before
	public void setup() {
		mapper = createObjectMapper();
	}

	@Test
	public void deserialize_typical() throws IOException {
		// GIVEN
		// @formatter:off
		final String json = "{\"created\":\"2021-08-17 14:28:12.345Z\",\"sourceId\":\"test.source\""
				+ ",\"i\":{\"a\":1}"
				+ ",\"a\":{\"b\":2}"
				+ ",\"s\":{\"c\":\"three\"}"
				+ ",\"t\":[\"d\"]}";
		// @formatter:on

		// WHEN
		Datum datum = mapper.readValue(json, Datum.class);

		// THEN
		LocalDateTime date = LocalDateTime.of(2021, 8, 17, 14, 28, 12,
				(int) TimeUnit.MILLISECONDS.toNanos(345));
		Instant ts = date.toInstant(ZoneOffset.UTC);
		DatumSamples s = new DatumSamples();
		s.putInstantaneousSampleValue("a", 1);
		s.putAccumulatingSampleValue("b", 2);
		s.putStatusSampleValue("c", "three");
		s.addTag("d");
		GeneralDatum expected = new GeneralDatum("test.source", ts, s);
		// @formatter:off
		then(datum)
			.as("GeneralDatum identity parsed")
			.isEqualTo(expected)
			.as("GeneralDatum samples parsed")
			.returns(expected.asSampleOperations(), from(Datum::asSampleOperations))
			;
		// @formatter:off
	}

	@Test
	public void deserialize_typical_isoTimestamp() throws IOException {
		// GIVEN
		// @formatter:off
		final String json = "{\"created\":\"2021-08-17T14:28:12.345Z\",\"sourceId\":\"test.source\""
				+ ",\"i\":{\"a\":1}"
				+ ",\"a\":{\"b\":2}"
				+ ",\"s\":{\"c\":\"three\"}"
				+ ",\"t\":[\"d\"]}";
		// @formatter:on

		// WHEN
		Datum datum = mapper.readValue(json, Datum.class);

		// THEN
		LocalDateTime date = LocalDateTime.of(2021, 8, 17, 14, 28, 12,
				(int) TimeUnit.MILLISECONDS.toNanos(345));
		Instant ts = date.toInstant(ZoneOffset.UTC);
		DatumSamples s = new DatumSamples();
		s.putInstantaneousSampleValue("a", 1);
		s.putAccumulatingSampleValue("b", 2);
		s.putStatusSampleValue("c", "three");
		s.addTag("d");
		GeneralDatum expected = new GeneralDatum("test.source", ts, s);
		// @formatter:off
		then(datum)
			.as("GeneralDatum identity parsed")
			.isEqualTo(expected)
			.as("GeneralDatum samples parsed")
			.returns(expected.asSampleOperations(), from(Datum::asSampleOperations))
			;
		// @formatter:off
	}

	@Test
	public void deserialize_typical_location() throws IOException {
		// GIVEN
		// @formatter:off
		final String json = "{\"created\":\"2021-08-17 14:28:12.345Z\",\"sourceId\":\"test.source\""
				+ ",\"locationId\":123"
				+ ",\"i\":{\"a\":1}"
				+ ",\"a\":{\"b\":2}"
				+ ",\"s\":{\"c\":\"three\"}"
				+ ",\"t\":[\"d\"]}";
		// @formatter:on

		// WHEN
		Datum datum = mapper.readValue(json, Datum.class);

		// THEN
		LocalDateTime date = LocalDateTime.of(2021, 8, 17, 14, 28, 12,
				(int) TimeUnit.MILLISECONDS.toNanos(345));
		Instant ts = date.toInstant(ZoneOffset.UTC);
		DatumSamples s = new DatumSamples();
		s.putInstantaneousSampleValue("a", 1);
		s.putAccumulatingSampleValue("b", 2);
		s.putStatusSampleValue("c", "three");
		s.addTag("d");
		GeneralDatum expected = GeneralDatum.locationDatum(123L, "test.source", ts, s);
		// @formatter:off
		then(datum)
			.as("GeneralDatum identity parsed")
			.isEqualTo(expected)
			.as("GeneralDatum samples parsed")
			.returns(expected.asSampleOperations(), from(Datum::asSampleOperations))
			;
		// @formatter:off
	}

	@Test
	public void deserialize_nested() throws IOException {
		// GIVEN
		LocalDateTime date = LocalDateTime.of(2021, 8, 17, 14, 28, 12,
				(int) TimeUnit.MILLISECONDS.toNanos(345));
		Instant ts = date.toInstant(ZoneOffset.UTC);
		// @formatter:off
		final String json = "{\"created\":"+ts.toEpochMilli()+",\"sourceId\":\"test.source\""
				+ ",\"samples\":{"
				+ "\"i\":{\"a\":1}"
				  + ",\"a\":{\"b\":2}"
				  + ",\"s\":{\"c\":\"three\"}"
				  + ",\"t\":[\"d\"]"
				+ "}}";
		// @formatter:on

		// WHEN
		Datum datum = mapper.readValue(json, Datum.class);

		// THEN
		DatumSamples s = new DatumSamples();
		s.putInstantaneousSampleValue("a", 1);
		s.putAccumulatingSampleValue("b", 2);
		s.putStatusSampleValue("c", "three");
		s.addTag("d");
		GeneralDatum expected = new GeneralDatum("test.source", ts, s);
		// @formatter:off
		then(datum)
			.as("GeneralDatum identity parsed")
			.isEqualTo(expected)
			.as("GeneralDatum samples parsed")
			.returns(expected.asSampleOperations(), from(Datum::asSampleOperations))
			;
		// @formatter:off
	}

	@Test
	public void deserialize_nested_location() throws IOException {
		// GIVEN
		LocalDateTime date = LocalDateTime.of(2021, 8, 17, 14, 28, 12,
				(int) TimeUnit.MILLISECONDS.toNanos(345));
		Instant ts = date.toInstant(ZoneOffset.UTC);
		// @formatter:off
		final String json = "{\"created\":"+ts.toEpochMilli()+",\"sourceId\":\"test.source\""
				+ ",\"locationId\":123"
				+ ",\"samples\":{"
				+ "\"i\":{\"a\":1}"
				  + ",\"a\":{\"b\":2}"
				  + ",\"s\":{\"c\":\"three\"}"
				  + ",\"t\":[\"d\"]"
				+ "}}";
		// @formatter:on

		// WHEN
		Datum datum = mapper.readValue(json, Datum.class);

		// THEN
		DatumSamples s = new DatumSamples();
		s.putInstantaneousSampleValue("a", 1);
		s.putAccumulatingSampleValue("b", 2);
		s.putStatusSampleValue("c", "three");
		s.addTag("d");
		GeneralDatum expected = GeneralDatum.locationDatum(123L, "test.source", ts, s);
		// @formatter:off
		then(datum)
			.as("GeneralDatum identity parsed")
			.isEqualTo(expected)
			.as("GeneralDatum samples parsed")
			.returns(expected.asSampleOperations(), from(Datum::asSampleOperations))
			;
		// @formatter:off
	}

	@Test
	public void deserialize_infinity() throws IOException {
		// GIVEN
		final String json = "{\"created\":\"2021-08-17 14:28:12.345Z\",\"nodeId\":123"
				+ ",\"sourceId\":\"test.source\"" + ",\"i\":{\"a\":\"Infinity\"}}";

		// WHEN
		Datum datum = mapper.readValue(json, Datum.class);

		// THEN
		Instant ts = Instant.parse("2021-08-17T14:28:12.345Z");

		DatumSamples s = new DatumSamples();
		GeneralDatum expected = GeneralDatum.nodeDatum(123L, "test.source", ts, s);
		// @formatter:off
		then(datum)
			.as("GeneralDatum identity parsed")
			.isEqualTo(expected)
			.as("GeneralDatum samples parsed")
			.returns(expected.asSampleOperations(), from(Datum::asSampleOperations))
			;
		// @formatter:off
	}

}
