/* ==================================================================
 * BasicStreamDatumArrayDeserializerTests.java - 4/06/2021 6:06:37 PM
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

package net.solarnetwork.util.test;

import static java.time.Instant.ofEpochMilli;
import static net.solarnetwork.util.NumberUtils.decimalArray;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import net.solarnetwork.domain.datum.StreamDatum;
import net.solarnetwork.util.BasicStreamDatumArrayDeserializer;

/**
 * Test cases for the {@link BasicStreamDatumArrayDeserializer} class.
 * 
 * @author matt
 * @version 1.0
 */
public class BasicStreamDatumArrayDeserializerTests {

	private ObjectMapper mapper;

	private ObjectMapper createObjectMapper() {
		ObjectMapper m = new ObjectMapper();
		SimpleModule mod = new SimpleModule("Test");
		mod.addDeserializer(StreamDatum.class, BasicStreamDatumArrayDeserializer.INSTANCE);
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
		final String json = "[1234567890,-39146522915747961,-8199130050457739548"
				+ ",[1.23,2.34],[3.45],[\"foo\",\"bar\"],[\"a\"]]";

		// WHEN
		StreamDatum d = mapper.readValue(json, StreamDatum.class);

		assertThat("Datum parsed", d, is(notNullValue()));
		assertThat("Datum timestamp", d.getTimestamp(), is(equalTo(ofEpochMilli(1234567890L))));
		assertThat("Instantaneous values", d.getProperties().getInstantaneous(),
				is(arrayContaining(decimalArray("1.23", "2.34"))));
		assertThat("Accumulating values", d.getProperties().getAccumulating(),
				is(arrayContaining(decimalArray("3.45"))));
		assertThat("Status values", d.getProperties().getStatus(),
				is(arrayContaining(new String[] { "foo", "bar" })));
		assertThat("Tags values", d.getProperties().getTags(),
				is(arrayContaining(new String[] { "a" })));
	}

	@Test
	public void deserialize_sparse() throws IOException {
		// GIVEN
		final String json = "[1234567890,-39146522915747961,-8199130050457739548"
				+ ",null,[3.45],null,null]";

		// WHEN
		StreamDatum d = mapper.readValue(json, StreamDatum.class);

		assertThat("Datum parsed", d, is(notNullValue()));
		assertThat("Datum timestamp", d.getTimestamp(), is(equalTo(ofEpochMilli(1234567890L))));
		assertThat("Instantaneous values", d.getProperties().getInstantaneous(), is(nullValue()));
		assertThat("Accumulating values", d.getProperties().getAccumulating(),
				is(arrayContaining(decimalArray("3.45"))));
		assertThat("Status values", d.getProperties().getStatus(), is(nullValue()));
		assertThat("Tags values", d.getProperties().getTags(), is(nullValue()));
	}

}
