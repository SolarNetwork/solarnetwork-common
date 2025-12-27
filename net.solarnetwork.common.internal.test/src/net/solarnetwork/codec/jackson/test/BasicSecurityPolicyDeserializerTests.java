/* ==================================================================
 * SecurityPolicyDeserializerTests.java - 27/09/2025 1:45:20 pm
 *
 * Copyright 2025 SolarNetwork.net Dev Team
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
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import net.solarnetwork.codec.jackson.BasicSecurityPolicyDeserializer;
import net.solarnetwork.domain.LocationPrecision;
import net.solarnetwork.domain.SecurityPolicy;
import net.solarnetwork.domain.datum.Aggregation;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

/**
 * Test cases for the {@link BasicSecurityPolicyDeserializer} class.
 *
 * @author matt
 * @version 1.0
 */
public class BasicSecurityPolicyDeserializerTests {

	private ObjectMapper mapper;

	private ObjectMapper createObjectMapper() {
		SimpleModule mod = new SimpleModule("Test");
		mod.addDeserializer(SecurityPolicy.class, BasicSecurityPolicyDeserializer.INSTANCE);
		return JsonMapper.builder().addModule(mod).build();
	}

	@Before
	public void setup() {
		mapper = createObjectMapper();
	}

	@Test
	public void full() throws IOException {
		// GIVEN
		final long now = System.currentTimeMillis();
		final String json = """
				{
					"nodeIds": [1,2,3],
					"sourceIds": ["a", "b", "c"],
					"minAggregation": "Hour",
					"aggregations": ["Hour", "Day"],
					"minLocationPrecision": "PostalCode",
					"locationPrecisions": ["PostalCode", "TimeZone"],
					"nodeMetadataPaths": ["p", "q"],
					"userMetadataPaths": ["r" ,"s"],
					"apiPaths": ["t", "v"],
					"notAfter": %d,
					"refreshAllowed": true
				}
				""".formatted(now);

		// WHEN
		SecurityPolicy result = mapper.readValue(json, SecurityPolicy.class);

		// THEN
		// @formatter:off
		then(result)
			.as("JSON parsed")
			.isNotNull()
			.as("Node IDs parsed")
			.returns(Set.of(1L, 2L, 3L), from(SecurityPolicy::getNodeIds))
			.as("Source IDs parsed")
			.returns(Set.of("a", "b", "c"), from(SecurityPolicy::getSourceIds))
			.as("Min agg parsed")
			.returns(Aggregation.Hour, from(SecurityPolicy::getMinAggregation))
			.as("Min loc precision parsed")
			.returns(LocationPrecision.PostalCode, from(SecurityPolicy::getMinLocationPrecision))
			.as("Node metadata paths parsed")
			.returns(Set.of("p", "q"), from(SecurityPolicy::getNodeMetadataPaths))
			.as("User metadata paths parsed")
			.returns(Set.of("r", "s"), from(SecurityPolicy::getUserMetadataPaths))
			.as("API paths parsed")
			.returns(Set.of("t", "v"), from(SecurityPolicy::getApiPaths))
			.as("Not after parsed")
			.returns(Instant.ofEpochMilli(now), from(SecurityPolicy::getNotAfter))
			.as("Refresh allowed parsed")
			.returns(true, from(SecurityPolicy::getRefreshAllowed))
			;
		// @formatter:on
	}

	@Test
	public void nonMinSets() throws IOException {
		// GIVEN
		final long now = System.currentTimeMillis();
		final String json = """
				{
					"nodeIds": [1,2,3],
					"sourceIds": ["a", "b", "c"],
					"aggregations": ["Hour", "Day"],
					"locationPrecisions": ["PostalCode", "TimeZone"],
					"nodeMetadataPaths": ["p", "q"],
					"userMetadataPaths": ["r" ,"s"],
					"apiPaths": ["t", "v"],
					"notAfter": %d,
					"refreshAllowed": true
				}
				""".formatted(now);

		// WHEN
		SecurityPolicy result = mapper.readValue(json, SecurityPolicy.class);

		// THEN
		// @formatter:off
		then(result)
			.as("JSON parsed")
			.isNotNull()
			.as("Node IDs parsed")
			.returns(Set.of(1L, 2L, 3L), from(SecurityPolicy::getNodeIds))
			.as("Source IDs parsed")
			.returns(Set.of("a", "b", "c"), from(SecurityPolicy::getSourceIds))
			.as("Min agg not given")
			.returns(null, from(SecurityPolicy::getMinAggregation))
			.as("Aggs parsed")
			.returns(Set.of(Aggregation.Hour, Aggregation.Day), from(SecurityPolicy::getAggregations))
			.as("Min loc not given")
			.returns(null, from(SecurityPolicy::getMinLocationPrecision))
			.as("Location precisions parsed")
			.returns(Set.of(LocationPrecision.PostalCode, LocationPrecision.TimeZone), from(SecurityPolicy::getLocationPrecisions))
			.as("Node metadata paths parsed")
			.returns(Set.of("p", "q"), from(SecurityPolicy::getNodeMetadataPaths))
			.as("User metadata paths parsed")
			.returns(Set.of("r", "s"), from(SecurityPolicy::getUserMetadataPaths))
			.as("API paths parsed")
			.returns(Set.of("t", "v"), from(SecurityPolicy::getApiPaths))
			.as("Not after parsed")
			.returns(Instant.ofEpochMilli(now), from(SecurityPolicy::getNotAfter))
			.as("Refresh allowed parsed")
			.returns(true, from(SecurityPolicy::getRefreshAllowed))
			;
		// @formatter:on
	}

}
