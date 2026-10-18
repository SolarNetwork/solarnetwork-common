/* ==================================================================
 * GeneralDatumMetadataSerializerTests.java - 19 Oct 2026 7:44:00 am
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

import static org.assertj.core.api.BDDAssertions.then;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import net.solarnetwork.codec.jackson.GeneralDatumMetadataSerializer;
import net.solarnetwork.domain.datum.GeneralDatumMetadata;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

/**
 * Test cases for the {@link GeneralDatumMetadataSerializer} class.
 *
 * @author matt
 * @version 1.0
 */
public class GeneralDatumMetadataSerializerTests {

	private ObjectMapper mapper;

	private ObjectMapper createObjectMapper() {
		SimpleModule mod = new SimpleModule("Test");
		mod.addSerializer(GeneralDatumMetadata.class, GeneralDatumMetadataSerializer.INSTANCE);
		return JsonMapper.builder().addModule(mod).build();
	}

	@Before
	public void setup() {
		mapper = createObjectMapper();
	}

	@Test
	public void serialize_typical() throws IOException {
		// GIVEN
		final GeneralDatumMetadata meta = new GeneralDatumMetadata();
		meta.setM(Map.of("a", "b"));
		meta.setPm(Map.of("c", Map.of("d", "e")));
		meta.setT(new LinkedHashSet<>(List.of("f", "g")));

		// WHEN
		String json = mapper.writeValueAsString(meta);

		// THEN
		// @formatter:off
		then(json)
			.isEqualToIgnoringWhitespace(
				"""
				{
					"m": {"a": "b"},
					"pm": {
						"c": {"d": "e"}
					},
					"t": ["f", "g"]
				}
				"""
			)
			;
		// @formatter:on
	}

	@Test
	public void serialize_onlyM() throws IOException {
		// GIVEN
		final GeneralDatumMetadata meta = new GeneralDatumMetadata();
		meta.setM(Map.of("a", "b"));

		// WHEN
		String json = mapper.writeValueAsString(meta);

		// THEN
		// @formatter:off
		then(json)
			.isEqualToIgnoringWhitespace(
				"""
				{
					"m": {"a": "b"}
				}
				"""
			)
			;
		// @formatter:on
	}

	@Test
	public void serialize_onlyPm() throws IOException {
		// GIVEN
		final GeneralDatumMetadata meta = new GeneralDatumMetadata();
		meta.setPm(Map.of("c", Map.of("d", "e")));

		// WHEN
		String json = mapper.writeValueAsString(meta);

		// THEN
		// @formatter:off
		then(json)
			.isEqualToIgnoringWhitespace(
				"""
				{
					"pm": {
						"c": {"d": "e"}
					}
				}
				"""
			)
			;
		// @formatter:on
	}

	@Test
	public void serialize_onlyT() throws IOException {
		// GIVEN
		final GeneralDatumMetadata meta = new GeneralDatumMetadata();
		meta.setT(new LinkedHashSet<>(List.of("f", "g")));

		// WHEN
		String json = mapper.writeValueAsString(meta);

		// THEN
		// @formatter:off
		then(json)
			.isEqualToIgnoringWhitespace(
				"""
				{
					"t": ["f", "g"]
				}
				"""
			)
			;
		// @formatter:on
	}

}
