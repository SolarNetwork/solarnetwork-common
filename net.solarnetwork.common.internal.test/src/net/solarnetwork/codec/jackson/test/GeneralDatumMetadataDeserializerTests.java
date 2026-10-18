/* ==================================================================
 * GeneralDatumMetadataDeserializerTests.java - 19 Oct 2026 7:59:19 am
 *
 * Copyright 2026 SolarNetwork.net Dev Team
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import net.solarnetwork.codec.jackson.GeneralDatumMetadataDeserializer;
import net.solarnetwork.domain.datum.GeneralDatumMetadata;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

/**
 * Test cases for the {@link GeneralDatumMetadataDeserializer} class.
 *
 * @author matt
 * @version 1.0
 */
public class GeneralDatumMetadataDeserializerTests {

	private ObjectMapper mapper;

	private ObjectMapper createObjectMapper() {
		SimpleModule mod = new SimpleModule("Test");
		mod.addDeserializer(GeneralDatumMetadata.class, GeneralDatumMetadataDeserializer.INSTANCE);
		return JsonMapper.builder().addModule(mod).build();
	}

	@Before
	public void setup() {
		mapper = createObjectMapper();
	}

	@Test
	public void deserialize_typical() throws IOException {
		// GIVEN
		final String json = """
					{
					"m": {"a": "b"},
					"pm": {
						"c": {"d": "e"}
					},
					"t": ["f", "g"]
				}
				""";

		// WHEN
		GeneralDatumMetadata d = mapper.readValue(json, GeneralDatumMetadata.class);

		// @formatter:off
		then(d)
			.as("JSON parsed")
			.isNotNull()
			.as("M parsed")
			.returns(Map.of("a", "b"), from(GeneralDatumMetadata::getM))
			.as("Pm parsed")
			.returns(Map.of("c", Map.of("d", "e")), from(GeneralDatumMetadata::getPm))
			.as("T parsed")
			.returns(new LinkedHashSet<>(List.of("f", "g")), from(GeneralDatumMetadata::getT))
			;
		// @formatter:on
	}

	@Test
	public void deserialize_onlyM() throws IOException {
		// GIVEN
		final String json = """
					{
					"m": {"a": "b"}
				}
				""";

		// WHEN
		GeneralDatumMetadata d = mapper.readValue(json, GeneralDatumMetadata.class);

		// @formatter:off
		then(d)
			.as("JSON parsed")
			.isNotNull()
			.as("M parsed")
			.returns(Map.of("a", "b"), from(GeneralDatumMetadata::getM))
			.as("Pm null")
			.returns(null, from(GeneralDatumMetadata::getPm))
			.as("T null")
			.returns(null, from(GeneralDatumMetadata::getT))
			;
		// @formatter:on
	}

	@Test
	public void deserialize_nullValues() throws IOException {
		// GIVEN
		final String json = """
					{
					"m": null,
					"pm": null,
					"t": null
				}
				""";

		// WHEN
		GeneralDatumMetadata d = mapper.readValue(json, GeneralDatumMetadata.class);

		// @formatter:off
		then(d)
			.as("JSON parsed")
			.isNotNull()
			.as("M null")
			.returns(null, from(GeneralDatumMetadata::getM))
			.as("Pm null")
			.returns(null, from(GeneralDatumMetadata::getPm))
			.as("T null")
			.returns(null, from(GeneralDatumMetadata::getT))
			;
		// @formatter:on
	}

	@Test
	public void deserialize_nonMapPmValue() throws IOException {
		// GIVEN
		final String json = """
					{
					"pm": {
						"a": "b",
						"c": {"d": "e"}
					}
				}
				""";

		// WHEN
		GeneralDatumMetadata d = mapper.readValue(json, GeneralDatumMetadata.class);

		// @formatter:off
		then(d)
			.as("JSON parsed")
			.isNotNull()
			.as("M null")
			.returns(null, from(GeneralDatumMetadata::getM))
			.as("Pm parsed, ignorning non-map top-level value")
			.returns(Map.of("c", Map.of("d", "e")), from(GeneralDatumMetadata::getPm))
			.as("T null")
			.returns(null, from(GeneralDatumMetadata::getT))
			;
		// @formatter:on
	}

}
