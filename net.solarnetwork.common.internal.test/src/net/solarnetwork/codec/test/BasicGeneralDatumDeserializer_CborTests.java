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

package net.solarnetwork.codec.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import net.solarnetwork.codec.BasicGeneralDatumDeserializer;
import net.solarnetwork.codec.JsonUtils;
import net.solarnetwork.domain.datum.Datum;
import net.solarnetwork.domain.datum.DatumId;
import net.solarnetwork.domain.datum.DatumSamples;
import net.solarnetwork.domain.datum.GeneralDatum;

/**
 * Test cases for the {@link BasicGeneralDatumDeserializer} class using CBOR.
 *
 * @author matt
 * @version 1.0
 */
public class BasicGeneralDatumDeserializer_CborTests {

	private ObjectMapper mapper;

	private ObjectMapper createObjectMapper(JsonFactory jsonFactory) {
		ObjectMapper m = new ObjectMapper(jsonFactory);
		m.registerModule(JsonUtils.JAVA_TIME_MODULE);
		m.registerModule(JsonUtils.DATUM_MODULE);
		return m;
	}

	@Before
	public void setup() {
		mapper = createObjectMapper(new CBORFactory());
	}

	@Test
	public void deserialize_infinity() throws IOException {
		// GIVEN
		LocalDateTime date = LocalDateTime.of(2021, 8, 17, 14, 28, 12,
				(int) TimeUnit.MILLISECONDS.toNanos(345));
		Instant ts = date.toInstant(ZoneOffset.UTC);
		DatumSamples samples = new DatumSamples();
		samples.putInstantaneousSampleValue("a", Double.POSITIVE_INFINITY);
		GeneralDatum datum = new GeneralDatum(DatumId.nodeId(123L, "test.source", ts), samples);

		final byte[] cbor = mapper.writeValueAsBytes(datum);

		// WHEN
		Datum result = mapper.readValue(cbor, Datum.class);

		// THEN
		assertThat("GeneralDatum identity parsed", result, is(equalTo(datum)));
		assertThat("GeneralDatum samples parsed, INCLUDING Infinity", result.asSampleOperations(),
				is(equalTo(datum.asSampleOperations())));
	}

}
