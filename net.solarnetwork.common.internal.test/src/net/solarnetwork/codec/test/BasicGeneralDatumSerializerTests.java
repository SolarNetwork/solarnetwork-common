/* ==================================================================
 * BasicGeneralDatumSerializerTests.java - 17/08/2021 2:24:34 PM
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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import net.solarnetwork.codec.BasicGeneralDatumSerializer;
import net.solarnetwork.domain.datum.DatumSamples;
import net.solarnetwork.domain.datum.GeneralDatum;

/**
 * Test cases for the {@link BasicGeneralDatumSerializer} class.
 * 
 * @author matt
 * @version 1.0
 */
public class BasicGeneralDatumSerializerTests {

	private ObjectMapper mapper;

	private ObjectMapper createObjectMapper() {
		ObjectMapper m = new ObjectMapper();
		SimpleModule mod = new SimpleModule("Test");
		mod.addSerializer(GeneralDatum.class, BasicGeneralDatumSerializer.INSTANCE);
		m.registerModule(mod);
		return m;
	}

	@Before
	public void setup() {
		mapper = createObjectMapper();
	}

	@Test
	public void serializer_typical() throws IOException {
		// GIVEN
		LocalDateTime date = LocalDateTime.of(2021, 8, 17, 14, 28, 12,
				(int) TimeUnit.MILLISECONDS.toNanos(345));
		Instant ts = date.toInstant(ZoneOffset.UTC);
		DatumSamples s = new DatumSamples();
		s.putInstantaneousSampleValue("a", 1);
		s.putAccumulatingSampleValue("b", 2);
		s.putStatusSampleValue("c", "three");
		s.addTag("d");
		GeneralDatum datum = new GeneralDatum("test.source", ts, s);

		// WHEN
		String json = mapper.writeValueAsString(datum);

		// THEN
		// @formatter:off
		assertThat("JSON", json,
				is("{\"created\":\"2021-08-17 14:28:12.345Z\",\"sourceId\":\"test.source\""
						+ ",\"i\":{\"a\":1}"
						+ ",\"a\":{\"b\":2}"
						+ ",\"s\":{\"c\":\"three\"}"
						+ ",\"t\":[\"d\"]}"));
		// @formatter:on
	}

}
