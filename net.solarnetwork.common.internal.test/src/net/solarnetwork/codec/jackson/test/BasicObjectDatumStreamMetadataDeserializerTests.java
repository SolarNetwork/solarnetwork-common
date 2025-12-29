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

import static net.solarnetwork.domain.BasicLocation.locationOf;
import static org.assertj.core.api.BDDAssertions.then;
import java.io.IOException;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import net.solarnetwork.codec.jackson.BasicObjectDatumStreamMetadataDeserializer;
import net.solarnetwork.codec.test.JsonUtilsTests;
import net.solarnetwork.domain.BasicLocation;
import net.solarnetwork.domain.datum.BasicObjectDatumStreamMetadata;
import net.solarnetwork.domain.datum.ObjectDatumKind;
import net.solarnetwork.domain.datum.ObjectDatumStreamMetadata;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

/**
 * Test cases for the {@link BasicObjectDatumStreamMetadataDeserializer} class.
 *
 * @author matt
 * @version 1.0
 */
public class BasicObjectDatumStreamMetadataDeserializerTests {

	private ObjectMapper mapper;

	private ObjectMapper createObjectMapper() {
		SimpleModule mod = new SimpleModule("Test");
		mod.addDeserializer(ObjectDatumStreamMetadata.class,
				BasicObjectDatumStreamMetadataDeserializer.INSTANCE);
		return JsonMapper.builder().addModule(mod).build();
	}

	@Before
	public void setup() {
		mapper = createObjectMapper();
	}

	@Test
	public void deserialize_typical() throws IOException {
		// GIVEN

		// WHEN
		ObjectDatumStreamMetadata meta = mapper.readValue(
				JsonUtilsTests.class.getResourceAsStream("node-stream-meta-01.json"),
				ObjectDatumStreamMetadata.class);

		BasicLocation expectedLocation = locationOf("Test", "NZ", "Wellington Region", null,
				"Wellington", null, null, "Pacific/Auckland");

		BasicObjectDatumStreamMetadata expected = new BasicObjectDatumStreamMetadata(
				UUID.fromString("a66e3344-3791-4113-afff-22b44eb3c833"), "Pacific/Auckland",
				ObjectDatumKind.Node, 123L, "test.source", expectedLocation, new String[] { "watts" },
				new String[] { "wattHours" }, null, null);

		// THEN
		then(meta).as("Metadata parsed").isEqualTo(expected);
	}

	@Test
	public void deserialize_noLocation() throws IOException {
		// GIVEN

		// WHEN
		ObjectDatumStreamMetadata meta = mapper.readValue(
				JsonUtilsTests.class.getResourceAsStream("node-stream-meta-02.json"),
				ObjectDatumStreamMetadata.class);

		BasicObjectDatumStreamMetadata expected = new BasicObjectDatumStreamMetadata(
				UUID.fromString("a66e3344-3791-4113-afff-22b44eb3c833"), "Pacific/Auckland",
				ObjectDatumKind.Node, 123L, "test.source", null, new String[] { "watts" },
				new String[] { "wattHours" }, null, null);

		// THEN
		then(meta).as("Metadata parsed").isEqualTo(expected);
	}

}
