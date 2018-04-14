/* ==================================================================
 * GeneralSourceMetadataTest.java - Oct 21, 2014 1:45:17 PM
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
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import net.solarnetwork.domain.GeneralDatumMetadata;
import net.solarnetwork.domain.GeneralSourceMetadata;

/**
 * Test cases for {@link GeneralSourceMetadata}.
 * 
 * @author matt
 * @version 1.1
 */
public class GeneralSourceMetadataTest {

	private static final String TEST_SOURCE_ID = "test.source";

	private ObjectMapper objectMapper;

	@Before
	public void setup() {
		objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(Include.NON_NULL);
		objectMapper.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true);

		SimpleModule module = new SimpleModule("TestModule", new Version(1, 0, 0, null, null, null));
		module.addSerializer(new net.solarnetwork.util.JodaDateTimeSerializer());
		objectMapper.registerModule(module);
	}

	private GeneralSourceMetadata getTestInstance() {
		GeneralSourceMetadata result = new GeneralSourceMetadata();
		result.setCreated(testDate());
		result.setSourceId(TEST_SOURCE_ID);

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
				"{\"created\":\"2014-10-20 23:00:00.000Z\",\"sourceId\":\"test.source\",\"m\":{\"currency\":\"NZD\"}"
						+ ",\"pm\":{\"amount\":{\"units\":\"MWh\"}}" + ",\"t\":[\"price\"]}",
				json);
	}

	@Test
	public void deserializeJson() throws Exception {
		String json = "{\"created\":1413846000000,\"updated\":1413846000000,\"sourceId\":\"test.source\",\"m\":{\"currency\":\"NZD\"}"
				+ ",\"pm\":{\"amount\":{\"units\":\"MWh\"}}" + ",\"t\":[\"price\"]}";
		GeneralSourceMetadata meta = objectMapper.readValue(json, GeneralSourceMetadata.class);

		Assert.assertNotNull(meta);
		Assert.assertEquals(testDate().getMillis(), meta.getCreated().getMillis());
		Assert.assertEquals(testDate().getMillis(), meta.getUpdated().getMillis());
		Assert.assertEquals("test.source", meta.getSourceId());
		Assert.assertEquals("NZD", meta.getMeta().getInfoString("currency"));
		Assert.assertEquals("MWh", meta.getMeta().getInfoString("amount", "units"));
		Assert.assertTrue(meta.getMeta().hasTag("price"));
	}

}
