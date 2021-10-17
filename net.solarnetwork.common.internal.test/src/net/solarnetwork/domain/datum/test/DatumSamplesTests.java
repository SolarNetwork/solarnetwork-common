/* ==================================================================
 * DatumSamplesTests.java - 14/03/2018 2:42:40 PM
 * 
 * Copyright 2018 SolarNetwork.net Dev Team
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

import static net.solarnetwork.domain.datum.DatumSamplesType.Accumulating;
import static net.solarnetwork.domain.datum.DatumSamplesType.Instantaneous;
import static net.solarnetwork.domain.datum.DatumSamplesType.Status;
import static net.solarnetwork.domain.datum.DatumSamplesType.Tag;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.solarnetwork.domain.datum.DatumSamples;

/**
 * Test cases for the {@link DatumSamples} class.
 * 
 * @author matt
 * @version 1.0
 */
public class DatumSamplesTests {

	private static final String WATTS_PROP = "watts";
	private static final Integer TEST_WATTS = 231;
	private static final String WATT_HOURS_PROP = "watt_hours";
	private static final Long TEST_WATT_HOURS = 4123L;
	private static final String MSG_PROP = "msg";
	private static final String TEST_MSG = "Hello, world.";
	private static final String TEST_TAG = "test";

	private ObjectMapper objectMapper;

	@Before
	public void setup() {
		objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(Include.NON_NULL);
		objectMapper.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true);
	}

	@Test
	public void createSampleValueInstantaneous() {
		DatumSamples s = new DatumSamples();
		s.putSampleValue(Instantaneous, WATTS_PROP, TEST_WATTS);
		assertThat(s.getI().keySet(), hasSize(1));
		assertThat(s.getI(), hasEntry(WATTS_PROP, (Number) TEST_WATTS));
	}

	@Test
	public void getNonExistingFirstSampleValueInstantaneous() {
		DatumSamples s = new DatumSamples();
		assertThat(s.getSampleInteger(Instantaneous, WATTS_PROP), nullValue());
	}

	@Test
	public void getNonExistingSampleValueInstantaneous() {
		DatumSamples s = new DatumSamples();
		s.putSampleValue(Instantaneous, WATTS_PROP, TEST_WATTS);
		assertThat(s.getSampleInteger(Instantaneous, "foo"), nullValue());
	}

	@Test
	public void getSampleValueInstantaneous() {
		DatumSamples s = new DatumSamples();
		s.putSampleValue(Instantaneous, WATTS_PROP, TEST_WATTS);
		assertThat(s.getSampleInteger(Instantaneous, WATTS_PROP), equalTo(TEST_WATTS));
	}

	@Test
	public void replaceSampleValueInstantaneous() {
		DatumSamples s = new DatumSamples();
		s.putSampleValue(Instantaneous, WATTS_PROP, TEST_WATTS);
		s.putSampleValue(Instantaneous, WATTS_PROP, 1);
		assertThat(s.getI().keySet(), hasSize(1));
		assertThat(s.getI(), hasEntry(WATTS_PROP, (Number) 1));
	}

	@Test
	public void addSampleValueInstantaneous() {
		DatumSamples s = new DatumSamples();
		s.putSampleValue(Instantaneous, WATTS_PROP, TEST_WATTS);
		s.putSampleValue(Instantaneous, "foo", 1);
		assertThat(s.getI().keySet(), hasSize(2));
		assertThat(s.getI(), hasEntry(WATTS_PROP, (Number) TEST_WATTS));
		assertThat(s.getI(), hasEntry("foo", (Number) 1));
	}

	@Test
	public void removeSampleValueInstantaneous() {
		DatumSamples s = new DatumSamples();
		s.putSampleValue(Instantaneous, WATTS_PROP, TEST_WATTS);
		s.putSampleValue(Instantaneous, WATTS_PROP, null);
		assertThat(s.getI().keySet(), hasSize(0));
	}

	@Test
	public void removeNonExistingFirstSampleValueInstantaneous() {
		DatumSamples s = new DatumSamples();
		s.putSampleValue(Instantaneous, WATTS_PROP, null);
		assertThat(s.getI(), nullValue());
	}

	@Test
	public void removeNonExistingSampleValueInstantaneous() {
		DatumSamples s = new DatumSamples();
		s.putSampleValue(Instantaneous, WATTS_PROP, TEST_WATTS);
		s.putSampleValue(Instantaneous, "foo", null);
		assertThat(s.getI().keySet(), hasSize(1));
		assertThat(s.getI(), hasEntry(WATTS_PROP, (Number) TEST_WATTS));
	}

	@Test
	public void createSampleValueAccumulating() {
		DatumSamples s = new DatumSamples();
		s.putSampleValue(Accumulating, WATT_HOURS_PROP, TEST_WATT_HOURS);
		assertThat(s.getA().keySet(), hasSize(1));
		assertThat(s.getA(), hasEntry(WATT_HOURS_PROP, (Number) TEST_WATT_HOURS));
	}

	@Test
	public void getNonExistingFirstSampleValueAccumulating() {
		DatumSamples s = new DatumSamples();
		assertThat(s.getSampleLong(Accumulating, WATT_HOURS_PROP), nullValue());
	}

	@Test
	public void getNonExistingSampleValueAccumulating() {
		DatumSamples s = new DatumSamples();
		s.putSampleValue(Accumulating, WATT_HOURS_PROP, TEST_WATT_HOURS);
		assertThat(s.getSampleLong(Accumulating, "foo"), nullValue());
	}

	@Test
	public void getSampleValueAccumulating() {
		DatumSamples s = new DatumSamples();
		s.putSampleValue(Accumulating, WATT_HOURS_PROP, TEST_WATT_HOURS);
		assertThat(s.getSampleLong(Accumulating, WATT_HOURS_PROP), equalTo(TEST_WATT_HOURS));
	}

	@Test
	public void replaceSampleValueAccumulating() {
		DatumSamples s = new DatumSamples();
		s.putSampleValue(Accumulating, WATT_HOURS_PROP, TEST_WATT_HOURS);
		s.putSampleValue(Accumulating, WATT_HOURS_PROP, 1);
		assertThat(s.getA().keySet(), hasSize(1));
		assertThat(s.getA(), hasEntry(WATT_HOURS_PROP, (Number) 1));
	}

	@Test
	public void addSampleValueAccumulating() {
		DatumSamples s = new DatumSamples();
		s.putSampleValue(Accumulating, WATT_HOURS_PROP, TEST_WATT_HOURS);
		s.putSampleValue(Accumulating, "foo", 1);
		assertThat(s.getA().keySet(), hasSize(2));
		assertThat(s.getA(), hasEntry(WATT_HOURS_PROP, (Number) TEST_WATT_HOURS));
		assertThat(s.getA(), hasEntry("foo", (Number) 1));
	}

	@Test
	public void removeSampleValueAccumulating() {
		DatumSamples s = new DatumSamples();
		s.putSampleValue(Accumulating, WATT_HOURS_PROP, TEST_WATT_HOURS);
		s.putSampleValue(Accumulating, WATT_HOURS_PROP, null);
		assertThat(s.getA().keySet(), hasSize(0));
	}

	@Test
	public void removeNonExistingFirstSampleValueAccumulating() {
		DatumSamples s = new DatumSamples();
		s.putSampleValue(Accumulating, WATT_HOURS_PROP, null);
		assertThat(s.getA(), nullValue());
	}

	@Test
	public void removeNonExistingSampleValueAccumulating() {
		DatumSamples s = new DatumSamples();
		s.putSampleValue(Accumulating, WATT_HOURS_PROP, TEST_WATT_HOURS);
		s.putSampleValue(Accumulating, "foo", null);
		assertThat(s.getA().keySet(), hasSize(1));
		assertThat(s.getA(), hasEntry(WATT_HOURS_PROP, (Number) TEST_WATT_HOURS));
	}

	@Test
	public void createSampleValueStatus() {
		DatumSamples s = new DatumSamples();
		s.putSampleValue(Status, MSG_PROP, TEST_MSG);
		assertThat(s.getS().keySet(), hasSize(1));
		assertThat(s.getS(), hasEntry(MSG_PROP, (Object) TEST_MSG));
	}

	@Test
	public void getNonExistingFirstSampleValueStatus() {
		DatumSamples s = new DatumSamples();
		assertThat(s.getSampleString(Status, MSG_PROP), nullValue());
	}

	@Test
	public void getNonExistingSampleValueStatus() {
		DatumSamples s = new DatumSamples();
		s.putSampleValue(Status, MSG_PROP, TEST_MSG);
		assertThat(s.getSampleString(Status, "foo"), nullValue());
	}

	@Test
	public void getSampleValueStatus() {
		DatumSamples s = new DatumSamples();
		s.putSampleValue(Status, MSG_PROP, TEST_MSG);
		assertThat(s.getSampleString(Status, MSG_PROP), equalTo(TEST_MSG));
	}

	@Test
	public void replaceSampleValueStatus() {
		DatumSamples s = new DatumSamples();
		s.putSampleValue(Status, MSG_PROP, TEST_MSG);
		s.putSampleValue(Status, MSG_PROP, "bar");
		assertThat(s.getS().keySet(), hasSize(1));
		assertThat(s.getS(), hasEntry(MSG_PROP, (Object) "bar"));
	}

	@Test
	public void addSampleValueStatus() {
		DatumSamples s = new DatumSamples();
		s.putSampleValue(Status, MSG_PROP, TEST_MSG);
		s.putSampleValue(Status, "foo", "bar");
		assertThat(s.getS().keySet(), hasSize(2));
		assertThat(s.getS(), hasEntry(MSG_PROP, (Object) TEST_MSG));
		assertThat(s.getS(), hasEntry("foo", (Object) "bar"));
	}

	@Test
	public void removeSampleValueStatus() {
		DatumSamples s = new DatumSamples();
		s.putSampleValue(Status, MSG_PROP, TEST_MSG);
		s.putSampleValue(Status, MSG_PROP, null);
		assertThat(s.getS().keySet(), hasSize(0));
	}

	@Test
	public void removeNonExistingFirstSampleValueStatus() {
		DatumSamples s = new DatumSamples();
		s.putSampleValue(Status, MSG_PROP, null);
		assertThat(s.getS(), nullValue());
	}

	@Test
	public void removeNonExistingSampleValueStatus() {
		DatumSamples s = new DatumSamples();
		s.putSampleValue(Status, MSG_PROP, TEST_MSG);
		s.putSampleValue(Status, "foo", null);
		assertThat(s.getS().keySet(), hasSize(1));
		assertThat(s.getS(), hasEntry(MSG_PROP, (Object) TEST_MSG));
	}

	@Test
	public void createSampleValueTag() {
		DatumSamples s = new DatumSamples();
		s.putSampleValue(Tag, TEST_TAG, TEST_TAG);
		assertThat(s.getT(), hasSize(1));
		assertThat(s.getT(), contains(TEST_TAG));
	}

	@Test
	public void setSampleDataTag() {
		DatumSamples s = new DatumSamples();
		s.setSampleData(Tag, Collections.singletonMap(TEST_TAG, TEST_TAG));
		assertThat(s.getTags(), hasSize(1));
		assertThat(s.getTags(), containsInAnyOrder(TEST_TAG));
	}

	@Test
	public void getNonExistingFirstSampleValueTag() {
		DatumSamples s = new DatumSamples();
		assertThat(s.getSampleString(Tag, TEST_TAG), nullValue());
	}

	@Test
	public void getNonExistingSampleValueTag() {
		DatumSamples s = new DatumSamples();
		s.putSampleValue(Tag, TEST_TAG, TEST_TAG);
		assertThat(s.getSampleString(Tag, "foo"), nullValue());
	}

	@Test
	public void getSampleValueTag() {
		DatumSamples s = new DatumSamples();
		s.putSampleValue(Tag, TEST_TAG, TEST_TAG);
		assertThat(s.getSampleString(Tag, TEST_TAG), equalTo(TEST_TAG));
	}

	@Test
	public void replaceSampleValueTag() {
		DatumSamples s = new DatumSamples();
		s.putSampleValue(Tag, TEST_TAG, TEST_TAG);
		s.putSampleValue(Tag, TEST_TAG, "bar");
		assertThat(s.getT(), hasSize(1));
		assertThat(s.getT(), contains("bar"));
	}

	@Test
	public void addSampleValueTag() {
		DatumSamples s = new DatumSamples();
		s.putSampleValue(Tag, TEST_TAG, TEST_TAG);
		s.putSampleValue(Tag, "foo", "foo");
		assertThat(s.getT(), hasSize(2));
		assertThat(s.getT(), containsInAnyOrder(TEST_TAG, "foo"));
	}

	@Test
	public void removeSampleValueTag() {
		DatumSamples s = new DatumSamples();
		s.putSampleValue(Tag, TEST_TAG, TEST_TAG);
		s.putSampleValue(Tag, TEST_TAG, null);
		assertThat(s.getT(), hasSize(0));
	}

	@Test
	public void removeNonExistingFirstSampleValueTag() {
		DatumSamples s = new DatumSamples();
		s.putSampleValue(Tag, TEST_TAG, null);
		assertThat(s.getT(), nullValue());
	}

	@Test
	public void removeNonExistingSampleValueTag() {
		DatumSamples s = new DatumSamples();
		s.putSampleValue(Tag, TEST_TAG, TEST_TAG);
		s.putSampleValue(Tag, "foo", null);
		assertThat(s.getT(), hasSize(1));
		assertThat(s.getT(), contains(TEST_TAG));
	}

	private DatumSamples getTestInstance() {
		DatumSamples samples = new DatumSamples();

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
		assertThat("JSON", json, is(
				"{\"i\":{\"watts\":231},\"a\":{\"watt_hours\":4123},\"s\":{\"msg\":\"Hello, world.\"},\"t\":[\"test\"]}"));
	}

	@Test
	public void deserializeJson() throws Exception {
		String json = "{\"i\":{\"watts\":89, \"temp\":21.2},\"s\":{\"ploc\":2502287},\"t\":[\"test\"]}";
		DatumSamples samples = objectMapper.readValue(json, DatumSamples.class);
		assertThat("JSON parsed", samples, is(notNullValue()));
		assertThat("Instantaneous integer property", samples.getInstantaneousSampleInteger("watts"),
				is(89));
		assertThat("Status property", samples.getStatusSampleLong("ploc"), is(2502287L));
		assertThat("Instantaneous decimal property", samples.getInstantaneousSampleBigDecimal("temp"),
				is(new BigDecimal("21.2")));
	}

}
