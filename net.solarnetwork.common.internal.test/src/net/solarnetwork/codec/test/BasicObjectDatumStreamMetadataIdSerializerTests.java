/* ==================================================================
 * BasicObjectDatumStreamMetadataSerializerTests.java - 7/06/2021 9:57:01 AM
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
import org.junit.Before;
import org.junit.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import net.solarnetwork.codec.BasicObjectDatumStreamMetadataIdSerializer;
import net.solarnetwork.domain.datum.ObjectDatumKind;
import net.solarnetwork.domain.datum.ObjectDatumStreamMetadataId;

/**
 * Test cases for the {@link BasicObjectDatumStreamMetadataIdSerializer} class.
 * 
 * @author matt
 * @version 1.0
 */
public class BasicObjectDatumStreamMetadataIdSerializerTests {

	private ObjectMapper mapper;

	private ObjectMapper createObjectMapper() {
		ObjectMapper m = new ObjectMapper();
		SimpleModule mod = new SimpleModule("Test");
		mod.addSerializer(ObjectDatumStreamMetadataId.class,
				BasicObjectDatumStreamMetadataIdSerializer.INSTANCE);
		m.registerModule(mod);
		return m;
	}

	@Before
	public void setup() {
		mapper = createObjectMapper();
	}

	@Test
	public void serialize_node() throws IOException {
		// GIVEN
		ObjectDatumStreamMetadataId metaId = new ObjectDatumStreamMetadataId(ObjectDatumKind.Node, 123L,
				"foobar");

		// WHEN
		String json = mapper.writeValueAsString(metaId);

		// THEN
		assertThat("JSON", json,
				is(equalTo("{\"kind\":\"n\",\"objectId\":123,\"sourceId\":\"foobar\"}")));
	}

	@Test
	public void serialize_location() throws IOException {
		// GIVEN
		ObjectDatumStreamMetadataId metaId = new ObjectDatumStreamMetadataId(ObjectDatumKind.Location,
				321L, "barfoo");

		// WHEN
		String json = mapper.writeValueAsString(metaId);

		// THEN
		assertThat("JSON", json,
				is(equalTo("{\"kind\":\"l\",\"objectId\":321,\"sourceId\":\"barfoo\"}")));
	}

}
