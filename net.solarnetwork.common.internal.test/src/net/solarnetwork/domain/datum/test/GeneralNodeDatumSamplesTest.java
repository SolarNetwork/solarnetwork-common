/* ==================================================================
 * GeneralNodeDatumSamplesTest.java - Aug 29, 2014 1:09:38 PM
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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.solarnetwork.domain.datum.GeneralNodeDatumSamples;

/**
 * Test cases for {@link GeneralNodeDatumSamples}.
 * 
 * @author matt
 * @version 1.1
 */
public class GeneralNodeDatumSamplesTest {

	private ObjectMapper objectMapper;

	@Before
	public void setup() {
		objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(Include.NON_NULL);
		objectMapper.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true);
	}

	private GeneralNodeDatumSamples getTestInstance() {
		GeneralNodeDatumSamples samples = new GeneralNodeDatumSamples();

		Map<String, Number> instants = new HashMap<String, Number>(2);
		instants.put("watts", 231);
		samples.setInstantaneous(instants);

		Map<String, Number> accum = new HashMap<String, Number>(2);
		accum.put("watt_hours", 4123);
		samples.setAccumulating(accum);

		Map<String, Object> status = new HashMap<String, Object>(2);
		status.put("msg", "Hello, world.");
		samples.setStatus(status);

		samples.addTag("test");

		return samples;
	}

	@Test
	public void serializeJson() throws Exception {
		String json = objectMapper.writeValueAsString(getTestInstance());
		Assert.assertEquals(
				"{\"i\":{\"watts\":231},\"a\":{\"watt_hours\":4123},\"s\":{\"msg\":\"Hello, world.\"},\"t\":[\"test\"]}",
				json);
	}

	@Test
	public void deserializeJson() throws Exception {
		String json = "{\"i\":{\"watts\":89, \"temp\":21.2},\"s\":{\"ploc\":2502287},\"t\":[\"test\"]}";
		GeneralNodeDatumSamples samples = objectMapper.readValue(json, GeneralNodeDatumSamples.class);
		Assert.assertNotNull(samples);
		Assert.assertEquals(Integer.valueOf(89), samples.getInstantaneousSampleInteger("watts"));
		Assert.assertEquals(Long.valueOf(2502287), samples.getStatusSampleLong("ploc"));
		Assert.assertEquals(new BigDecimal("21.2"), samples.getInstantaneousSampleBigDecimal("temp"));
	}

	@Test
	public void removeAccumulatingKey() {
		GeneralNodeDatumSamples meta = getTestInstance();
		meta.putAccumulatingSampleValue("watt_hours", null);
		meta.putStatusSampleValue("does.not.exist", null);
		Assert.assertNull(meta.getAccumulatingSampleInteger("watt_hours"));
	}

	@Test
	public void removeInstantaneousKey() {
		GeneralNodeDatumSamples meta = getTestInstance();
		meta.putInstantaneousSampleValue("watts", null);
		meta.putInstantaneousSampleValue("does.not.exist", null);
		Assert.assertNull(meta.getInstantaneousSampleInteger("watts"));
	}

	@Test
	public void removeStatusKey() {
		GeneralNodeDatumSamples meta = getTestInstance();
		meta.putStatusSampleValue("msg", null);
		meta.putStatusSampleValue("does.not.exist", null);
		Assert.assertNull(meta.getStatusSampleString("msg"));
	}

}
