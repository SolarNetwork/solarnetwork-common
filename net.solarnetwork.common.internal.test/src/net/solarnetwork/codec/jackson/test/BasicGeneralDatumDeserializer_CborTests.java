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
import net.solarnetwork.codec.jackson.BasicGeneralDatumSerializer;
import net.solarnetwork.codec.jackson.CborUtils;
import net.solarnetwork.domain.datum.Datum;
import net.solarnetwork.domain.datum.DatumId;
import net.solarnetwork.domain.datum.DatumSamples;
import net.solarnetwork.domain.datum.GeneralDatum;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.dataformat.cbor.CBORMapper;

/**
 * Test cases for the {@link BasicGeneralDatumDeserializer} class using CBOR.
 *
 * @author matt
 * @version 1.0
 */
public class BasicGeneralDatumDeserializer_CborTests {

	private ObjectMapper mapper;

	private ObjectMapper createObjectMapper() {
		SimpleModule mod = new SimpleModule("Test");
		mod.addDeserializer(Datum.class, BasicGeneralDatumDeserializer.INSTANCE);
		mod.addSerializer(Datum.class, BasicGeneralDatumSerializer.INSTANCE);
		return CBORMapper.builder(CborUtils.cborFactory()).addModule(mod).build();
	}

	@Before
	public void setup() {
		mapper = createObjectMapper();
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
		// @formatter:off
		then(result)
			.as("GeneralDatum identity parsed")
			.isEqualTo(datum)
			.as("GeneralDatum samples parsed")
			.returns(datum.asSampleOperations(), from(Datum::asSampleOperations))
			;
		// @formatter:off
	}

}
