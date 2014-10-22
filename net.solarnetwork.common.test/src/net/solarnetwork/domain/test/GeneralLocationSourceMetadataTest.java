/* ==================================================================
 * GeneralLocationSourceMetadataTest.java - Oct 21, 2014 2:11:26 PM
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

package net.solarnetwork.domain.test;

import java.util.HashMap;
import java.util.Map;
import net.solarnetwork.domain.GeneralDatumMetadata;
import net.solarnetwork.domain.GeneralLocationSourceMetadata;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test cases for the {@link GeneralLocationSourceMetadata} class.
 * 
 * @author matt
 * @version 1.0
 */
public class GeneralLocationSourceMetadataTest {

	private static final String TEST_SOURCE_ID = "test.source";
	private static final Long TEST_LOC_ID = -1L;

	private ObjectMapper objectMapper;

	@Before
	public void setup() {
		objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(Inclusion.NON_NULL);
		objectMapper.setDeserializationConfig(objectMapper.getDeserializationConfig().with(
				DeserializationConfig.Feature.USE_BIG_DECIMAL_FOR_FLOATS));

	}

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

	private DateTime testDate() {
		return new DateTime(2014, 10, 21, 12, 0, 0);
	}

	@Test
	public void serializeJson() throws Exception {
		String json = objectMapper.writeValueAsString(getTestInstance());
		Assert.assertEquals(
				"{\"created\":1413846000000,\"locationId\":-1,\"sourceId\":\"test.source\",\"m\":{\"currency\":\"NZD\"}"
						+ ",\"pm\":{\"amount\":{\"units\":\"MWh\"}}" + ",\"t\":[\"price\"]}", json);
	}

	@Test
	public void deserializeJson() throws Exception {
		String json = "{\"created\":1413846000000,\"updated\":1413846000000,\"locationId\":-1,\"sourceId\":\"test.source\",\"m\":{\"currency\":\"NZD\"}"
				+ ",\"pm\":{\"amount\":{\"units\":\"MWh\"}}" + ",\"t\":[\"price\"]}";
		GeneralLocationSourceMetadata meta = objectMapper.readValue(json,
				GeneralLocationSourceMetadata.class);

		Assert.assertNotNull(meta);
		Assert.assertEquals(testDate().getMillis(), meta.getCreated().getMillis());
		Assert.assertEquals(testDate().getMillis(), meta.getUpdated().getMillis());
		Assert.assertEquals(-1L, meta.getLocationId().longValue());
		Assert.assertEquals("test.source", meta.getSourceId());
		Assert.assertEquals("NZD", meta.getMeta().getInfoString("currency"));
		Assert.assertEquals("MWh", meta.getMeta().getInfoString("amount", "units"));
		Assert.assertTrue(meta.getMeta().hasTag("price"));
	}

}
