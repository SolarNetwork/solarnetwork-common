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

package net.solarnetwork.codec.jackson.test;

import static org.assertj.core.api.BDDAssertions.then;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import net.solarnetwork.codec.jackson.BasicObjectDatumStreamMetadataIdSerializer;
import net.solarnetwork.domain.datum.ObjectDatumKind;
import net.solarnetwork.domain.datum.ObjectDatumStreamMetadataId;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

/**
 * Test cases for the {@link BasicObjectDatumStreamMetadataIdSerializer} class.
 *
 * @author matt
 * @version 1.0
 */
public class BasicObjectDatumStreamMetadataIdSerializerTests {

	private ObjectMapper mapper;

	private ObjectMapper createObjectMapper() {
		SimpleModule mod = new SimpleModule("Test");
		mod.addSerializer(ObjectDatumStreamMetadataId.class,
				BasicObjectDatumStreamMetadataIdSerializer.INSTANCE);
		return JsonMapper.builder().addModule(mod).build();
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
		then(json).as("JSON").isEqualTo("{\"kind\":\"n\",\"objectId\":123,\"sourceId\":\"foobar\"}");
	}

	@Test
	public void serialize_location() throws IOException {
		// GIVEN
		ObjectDatumStreamMetadataId metaId = new ObjectDatumStreamMetadataId(ObjectDatumKind.Location,
				321L, "barfoo");

		// WHEN
		String json = mapper.writeValueAsString(metaId);

		// THEN
		then(json).as("JSON").isEqualTo("{\"kind\":\"l\",\"objectId\":321,\"sourceId\":\"barfoo\"}");
	}

}
