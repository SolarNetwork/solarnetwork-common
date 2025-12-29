/* ==================================================================
 * GeneralLocationSourceMetadataTests.java - Oct 21, 2014 2:11:26 PM
 *
 * Copyright 2007-2014 SolarNetwork.net Dev Team
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

package net.solarnetwork.domain.datum.test;

import static org.assertj.core.api.BDDAssertions.from;
import static org.assertj.core.api.BDDAssertions.then;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import net.solarnetwork.codec.jackson.JsonUtils;
import net.solarnetwork.domain.datum.GeneralDatumMetadata;
import net.solarnetwork.domain.datum.GeneralLocationSourceMetadata;

/**
 * Test cases for the {@link GeneralLocationSourceMetadata} class.
 *
 * @author matt
 * @version 1.0
 */
public class GeneralLocationSourceMetadata_Jackson3Tests {

	private static final String TEST_SOURCE_ID = "test.source";
	private static final Long TEST_LOC_ID = -1L;

	private GeneralLocationSourceMetadata getTestInstance() {
		GeneralLocationSourceMetadata result = new GeneralLocationSourceMetadata();
		result.setCreated(testDate());
		result.setSourceId(TEST_SOURCE_ID);
		result.setLocationId(TEST_LOC_ID);

		GeneralDatumMetadata meta = new GeneralDatumMetadata();
		result.setMeta(meta);

		Map<String, Object> info = new HashMap<String, Object>(2);
		info.put("currency", "NZD");
		meta.setInfo(info);

		Map<String, Map<String, Object>> propInfo = new HashMap<String, Map<String, Object>>(2);
		Map<String, Object> amount = new HashMap<String, Object>(2);
		amount.put("units", "MWh");
		propInfo.put("amount", amount);
		meta.setPropertyInfo(propInfo);

		meta.addTag("price");

		return result;
	}

	private Instant testDate() {
		return LocalDateTime.of(2014, 10, 21, 12, 0, 0).atZone(ZoneId.systemDefault()).toInstant();
	}

	@Test
	public void serializeJson() throws Exception {
		String json = JsonUtils.JSON_OBJECT_MAPPER.writeValueAsString(getTestInstance());
		then(json).isEqualTo(
				"{\"created\":\"2014-10-20 23:00:00Z\",\"locationId\":-1,\"sourceId\":\"test.source\",\"m\":{\"currency\":\"NZD\"}"
						+ ",\"pm\":{\"amount\":{\"units\":\"MWh\"}}" + ",\"t\":[\"price\"]}");
	}

	@Test
	public void deserializeJson() throws Exception {
		String json = "{\"created\":\"2014-10-20 23:00:00Z\",\"updated\":\"2014-10-20 23:00:00Z\",\"locationId\":-1,\"sourceId\":\"test.source\",\"m\":{\"currency\":\"NZD\"}"
				+ ",\"pm\":{\"amount\":{\"units\":\"MWh\"}}" + ",\"t\":[\"price\"]}";
		GeneralLocationSourceMetadata meta = JsonUtils.JSON_OBJECT_MAPPER.readValue(json,
				GeneralLocationSourceMetadata.class);

		// @formatter:off
		then(meta)
			.isNotNull()
			.as("Created")
			.returns(testDate(), from(GeneralLocationSourceMetadata::getCreated))
			.as("Updated")
			.returns(testDate(), from(GeneralLocationSourceMetadata::getUpdated))
			.as("Location ID")
			.returns(-1L, from(GeneralLocationSourceMetadata::getLocationId))
			.as("Source ID")
			.returns("test.source", from(GeneralLocationSourceMetadata::getSourceId))
			.extracting(GeneralLocationSourceMetadata::getMeta)
			.as("Currency")
			.returns("NZD", from(m -> m.getInfoString("currency")))
			.as("Amount/Units")
			.returns("MWh", from(m -> m.getInfoString("amount", "units")))
			.as("Price tag")
			.returns(true, from(m -> m.hasTag("price")))
			;
		// @formatter:on
	}

}
