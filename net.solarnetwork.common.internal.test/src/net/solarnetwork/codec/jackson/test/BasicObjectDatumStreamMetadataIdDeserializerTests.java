/* ==================================================================
 * BasicObjectDatumStreamMetadataDeserializerTests.java - 7/06/2021 9:39:11 AM
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
import net.solarnetwork.codec.jackson.BasicObjectDatumStreamMetadataIdDeserializer;
import net.solarnetwork.codec.test.JsonUtilsTests;
import net.solarnetwork.domain.datum.ObjectDatumKind;
import net.solarnetwork.domain.datum.ObjectDatumStreamMetadataId;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

/**
 * Test cases for the {@link BasicObjectDatumStreamMetadataIdDeserializer}
 * class.
 *
 * @author matt
 * @version 1.0
 */
public class BasicObjectDatumStreamMetadataIdDeserializerTests {

	private ObjectMapper mapper;

	private ObjectMapper createObjectMapper() {
		SimpleModule mod = new SimpleModule("Test");
		mod.addDeserializer(ObjectDatumStreamMetadataId.class,
				BasicObjectDatumStreamMetadataIdDeserializer.INSTANCE);
		return JsonMapper.builder().addModule(mod).build();
	}

	@Before
	public void setup() {
		mapper = createObjectMapper();
	}

	@Test
	public void deserialize_node() throws IOException {
		// GIVEN

		// WHEN
		ObjectDatumStreamMetadataId metaId = mapper.readValue(
				JsonUtilsTests.class.getResourceAsStream("node-stream-meta-id-01.json"),
				ObjectDatumStreamMetadataId.class);

		// THEN
		then(metaId).as("Metadata ID parsed")
				.isEqualTo(new ObjectDatumStreamMetadataId(ObjectDatumKind.Node, 123L, "foobar"));
	}

	@Test
	public void deserialize_location() throws IOException {
		// GIVEN

		// WHEN
		ObjectDatumStreamMetadataId metaId = mapper.readValue(
				JsonUtilsTests.class.getResourceAsStream("node-stream-meta-id-02.json"),
				ObjectDatumStreamMetadataId.class);

		// THEN
		then(metaId).as("Metadata ID parsed")
				.isEqualTo(new ObjectDatumStreamMetadataId(ObjectDatumKind.Location, 321L, "barfoo"));
	}

}
