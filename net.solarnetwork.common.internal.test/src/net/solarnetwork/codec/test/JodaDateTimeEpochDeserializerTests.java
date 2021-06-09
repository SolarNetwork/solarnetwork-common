/* ==================================================================
 * JodaDateTimeEpochDeserializerTests.java - 6/11/2019 7:24:36 am
 * 
 * Copyright 2019 SolarNetwork.net Dev Team
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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import net.solarnetwork.codec.JodaDateTimeEpochDeserializer;

/**
 * Test cases for the {@link JodaDateTimeEpochDeserializer} class.
 * 
 * @author matt
 * @version 1.0
 */
public class JodaDateTimeEpochDeserializerTests {

	private ObjectMapper mapper;

	private ObjectMapper createObjectMapper() {
		ObjectMapper m = new ObjectMapper();
		SimpleModule mod = new SimpleModule("Test");
		mod.addDeserializer(DateTime.class, new JodaDateTimeEpochDeserializer());
		m.registerModule(mod);
		return m;
	}

	@Before
	public void setup() {
		mapper = createObjectMapper();
	}

	@Test
	public void deserializeInteger() throws Exception {
		long now = System.currentTimeMillis();
		DateTime result = mapper.readValue(String.valueOf(now), DateTime.class);
		assertThat("Parsed date", result, equalTo(new DateTime(now)));
	}

	@Test
	public void deserializeFloat() throws Exception {
		long now = System.currentTimeMillis();
		DateTime result = mapper.readValue(String.valueOf(now) + ".0", DateTime.class);
		assertThat("Parsed date", result, equalTo(new DateTime(now)));
	}

	@Test
	public void deserializeString() throws Exception {
		long now = System.currentTimeMillis();
		DateTime result = mapper.readValue(String.format("\"%d\"", now), DateTime.class);
		assertThat("Parsed date", result, equalTo(new DateTime(now)));
	}

	@Test(expected = JsonProcessingException.class)
	public void deserializeEmptyString() throws Exception {
		mapper.readValue("\"\"", DateTime.class);
	}

	@Test
	public void deserializeNull() throws Exception {
		DateTime result = mapper.readValue("null", DateTime.class);
		assertThat("Parsed null to null", result, nullValue());
	}

}
