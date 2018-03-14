/* ==================================================================
 * GeneralDatumSamplesTests.java - 14/03/2018 2:42:40 PM
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

package net.solarnetwork.domain.test;

import static net.solarnetwork.domain.GeneralDatumSamplesType.Accumulating;
import static net.solarnetwork.domain.GeneralDatumSamplesType.Instantaneous;
import static net.solarnetwork.domain.GeneralDatumSamplesType.Status;
import static net.solarnetwork.domain.GeneralDatumSamplesType.Tag;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.solarnetwork.domain.GeneralDatumSamples;

/**
 * Test cases for the {@link GeneralDatumSamples} class.
 * 
 * @author matt
 * @version 1.0
 */
public class GeneralDatumSamplesTests {

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
		GeneralDatumSamples s = new GeneralDatumSamples();
		s.putSampleValue(Instantaneous, WATTS_PROP, TEST_WATTS);
		assertThat(s.getI().keySet(), hasSize(1));
		assertThat(s.getI(), hasEntry(WATTS_PROP, (Number) TEST_WATTS));
	}

	@Test
	public void getNonExistingFirstSampleValueInstantaneous() {
		GeneralDatumSamples s = new GeneralDatumSamples();
		assertThat(s.getSampleInteger(Instantaneous, WATTS_PROP), nullValue());
	}

	@Test
	public void getNonExistingSampleValueInstantaneous() {
		GeneralDatumSamples s = new GeneralDatumSamples();
		s.putSampleValue(Instantaneous, WATTS_PROP, TEST_WATTS);
		assertThat(s.getSampleInteger(Instantaneous, "foo"), nullValue());
	}

	@Test
	public void getSampleValueInstantaneous() {
		GeneralDatumSamples s = new GeneralDatumSamples();
		s.putSampleValue(Instantaneous, WATTS_PROP, TEST_WATTS);
		assertThat(s.getSampleInteger(Instantaneous, WATTS_PROP), equalTo(TEST_WATTS));
	}

	@Test
	public void replaceSampleValueInstantaneous() {
		GeneralDatumSamples s = new GeneralDatumSamples();
		s.putSampleValue(Instantaneous, WATTS_PROP, TEST_WATTS);
		s.putSampleValue(Instantaneous, WATTS_PROP, 1);
		assertThat(s.getI().keySet(), hasSize(1));
		assertThat(s.getI(), hasEntry(WATTS_PROP, (Number) 1));
	}

	@Test
	public void addSampleValueInstantaneous() {
		GeneralDatumSamples s = new GeneralDatumSamples();
		s.putSampleValue(Instantaneous, WATTS_PROP, TEST_WATTS);
		s.putSampleValue(Instantaneous, "foo", 1);
		assertThat(s.getI().keySet(), hasSize(2));
		assertThat(s.getI(), hasEntry(WATTS_PROP, (Number) TEST_WATTS));
		assertThat(s.getI(), hasEntry("foo", (Number) 1));
	}

	@Test
	public void removeSampleValueInstantaneous() {
		GeneralDatumSamples s = new GeneralDatumSamples();
		s.putSampleValue(Instantaneous, WATTS_PROP, TEST_WATTS);
		s.putSampleValue(Instantaneous, WATTS_PROP, null);
		assertThat(s.getI().keySet(), hasSize(0));
	}

	@Test
	public void removeNonExistingFirstSampleValueInstantaneous() {
		GeneralDatumSamples s = new GeneralDatumSamples();
		s.putSampleValue(Instantaneous, WATTS_PROP, null);
		assertThat(s.getI(), nullValue());
	}

	@Test
	public void removeNonExistingSampleValueInstantaneous() {
		GeneralDatumSamples s = new GeneralDatumSamples();
		s.putSampleValue(Instantaneous, WATTS_PROP, TEST_WATTS);
		s.putSampleValue(Instantaneous, "foo", null);
		assertThat(s.getI().keySet(), hasSize(1));
		assertThat(s.getI(), hasEntry(WATTS_PROP, (Number) TEST_WATTS));
	}

	@Test
	public void createSampleValueAccumulating() {
		GeneralDatumSamples s = new GeneralDatumSamples();
		s.putSampleValue(Accumulating, WATT_HOURS_PROP, TEST_WATT_HOURS);
		assertThat(s.getA().keySet(), hasSize(1));
		assertThat(s.getA(), hasEntry(WATT_HOURS_PROP, (Number) TEST_WATT_HOURS));
	}

	@Test
	public void getNonExistingFirstSampleValueAccumulating() {
		GeneralDatumSamples s = new GeneralDatumSamples();
		assertThat(s.getSampleLong(Accumulating, WATT_HOURS_PROP), nullValue());
	}

	@Test
	public void getNonExistingSampleValueAccumulating() {
		GeneralDatumSamples s = new GeneralDatumSamples();
		s.putSampleValue(Accumulating, WATT_HOURS_PROP, TEST_WATT_HOURS);
		assertThat(s.getSampleLong(Accumulating, "foo"), nullValue());
	}

	@Test
	public void getSampleValueAccumulating() {
		GeneralDatumSamples s = new GeneralDatumSamples();
		s.putSampleValue(Accumulating, WATT_HOURS_PROP, TEST_WATT_HOURS);
		assertThat(s.getSampleLong(Accumulating, WATT_HOURS_PROP), equalTo(TEST_WATT_HOURS));
	}

	@Test
	public void replaceSampleValueAccumulating() {
		GeneralDatumSamples s = new GeneralDatumSamples();
		s.putSampleValue(Accumulating, WATT_HOURS_PROP, TEST_WATT_HOURS);
		s.putSampleValue(Accumulating, WATT_HOURS_PROP, 1);
		assertThat(s.getA().keySet(), hasSize(1));
		assertThat(s.getA(), hasEntry(WATT_HOURS_PROP, (Number) 1));
	}

	@Test
	public void addSampleValueAccumulating() {
		GeneralDatumSamples s = new GeneralDatumSamples();
		s.putSampleValue(Accumulating, WATT_HOURS_PROP, TEST_WATT_HOURS);
		s.putSampleValue(Accumulating, "foo", 1);
		assertThat(s.getA().keySet(), hasSize(2));
		assertThat(s.getA(), hasEntry(WATT_HOURS_PROP, (Number) TEST_WATT_HOURS));
		assertThat(s.getA(), hasEntry("foo", (Number) 1));
	}

	@Test
	public void removeSampleValueAccumulating() {
		GeneralDatumSamples s = new GeneralDatumSamples();
		s.putSampleValue(Accumulating, WATT_HOURS_PROP, TEST_WATT_HOURS);
		s.putSampleValue(Accumulating, WATT_HOURS_PROP, null);
		assertThat(s.getA().keySet(), hasSize(0));
	}

	@Test
	public void removeNonExistingFirstSampleValueAccumulating() {
		GeneralDatumSamples s = new GeneralDatumSamples();
		s.putSampleValue(Accumulating, WATT_HOURS_PROP, null);
		assertThat(s.getA(), nullValue());
	}

	@Test
	public void removeNonExistingSampleValueAccumulating() {
		GeneralDatumSamples s = new GeneralDatumSamples();
		s.putSampleValue(Accumulating, WATT_HOURS_PROP, TEST_WATT_HOURS);
		s.putSampleValue(Accumulating, "foo", null);
		assertThat(s.getA().keySet(), hasSize(1));
		assertThat(s.getA(), hasEntry(WATT_HOURS_PROP, (Number) TEST_WATT_HOURS));
	}

	@Test
	public void createSampleValueStatus() {
		GeneralDatumSamples s = new GeneralDatumSamples();
		s.putSampleValue(Status, MSG_PROP, TEST_MSG);
		assertThat(s.getS().keySet(), hasSize(1));
		assertThat(s.getS(), hasEntry(MSG_PROP, (Object) TEST_MSG));
	}

	@Test
	public void getNonExistingFirstSampleValueStatus() {
		GeneralDatumSamples s = new GeneralDatumSamples();
		assertThat(s.getSampleString(Status, MSG_PROP), nullValue());
	}

	@Test
	public void getNonExistingSampleValueStatus() {
		GeneralDatumSamples s = new GeneralDatumSamples();
		s.putSampleValue(Status, MSG_PROP, TEST_MSG);
		assertThat(s.getSampleString(Status, "foo"), nullValue());
	}

	@Test
	public void getSampleValueStatus() {
		GeneralDatumSamples s = new GeneralDatumSamples();
		s.putSampleValue(Status, MSG_PROP, TEST_MSG);
		assertThat(s.getSampleString(Status, MSG_PROP), equalTo(TEST_MSG));
	}

	@Test
	public void replaceSampleValueStatus() {
		GeneralDatumSamples s = new GeneralDatumSamples();
		s.putSampleValue(Status, MSG_PROP, TEST_MSG);
		s.putSampleValue(Status, MSG_PROP, "bar");
		assertThat(s.getS().keySet(), hasSize(1));
		assertThat(s.getS(), hasEntry(MSG_PROP, (Object) "bar"));
	}

	@Test
	public void addSampleValueStatus() {
		GeneralDatumSamples s = new GeneralDatumSamples();
		s.putSampleValue(Status, MSG_PROP, TEST_MSG);
		s.putSampleValue(Status, "foo", "bar");
		assertThat(s.getS().keySet(), hasSize(2));
		assertThat(s.getS(), hasEntry(MSG_PROP, (Object) TEST_MSG));
		assertThat(s.getS(), hasEntry("foo", (Object) "bar"));
	}

	@Test
	public void removeSampleValueStatus() {
		GeneralDatumSamples s = new GeneralDatumSamples();
		s.putSampleValue(Status, MSG_PROP, TEST_MSG);
		s.putSampleValue(Status, MSG_PROP, null);
		assertThat(s.getS().keySet(), hasSize(0));
	}

	@Test
	public void removeNonExistingFirstSampleValueStatus() {
		GeneralDatumSamples s = new GeneralDatumSamples();
		s.putSampleValue(Status, MSG_PROP, null);
		assertThat(s.getS(), nullValue());
	}

	@Test
	public void removeNonExistingSampleValueStatus() {
		GeneralDatumSamples s = new GeneralDatumSamples();
		s.putSampleValue(Status, MSG_PROP, TEST_MSG);
		s.putSampleValue(Status, "foo", null);
		assertThat(s.getS().keySet(), hasSize(1));
		assertThat(s.getS(), hasEntry(MSG_PROP, (Object) TEST_MSG));
	}

	@Test
	public void createSampleValueTag() {
		GeneralDatumSamples s = new GeneralDatumSamples();
		s.putSampleValue(Tag, TEST_TAG, TEST_TAG);
		assertThat(s.getT(), hasSize(1));
		assertThat(s.getT(), contains(TEST_TAG));
	}

	@Test
	public void getNonExistingFirstSampleValueTag() {
		GeneralDatumSamples s = new GeneralDatumSamples();
		assertThat(s.getSampleString(Tag, TEST_TAG), nullValue());
	}

	@Test
	public void getNonExistingSampleValueTag() {
		GeneralDatumSamples s = new GeneralDatumSamples();
		s.putSampleValue(Tag, TEST_TAG, TEST_TAG);
		assertThat(s.getSampleString(Tag, "foo"), nullValue());
	}

	@Test
	public void getSampleValueTag() {
		GeneralDatumSamples s = new GeneralDatumSamples();
		s.putSampleValue(Tag, TEST_TAG, TEST_TAG);
		assertThat(s.getSampleString(Tag, TEST_TAG), equalTo(TEST_TAG));
	}

	@Test
	public void replaceSampleValueTag() {
		GeneralDatumSamples s = new GeneralDatumSamples();
		s.putSampleValue(Tag, TEST_TAG, TEST_TAG);
		s.putSampleValue(Tag, TEST_TAG, "bar");
		assertThat(s.getT(), hasSize(1));
		assertThat(s.getT(), contains("bar"));
	}

	@Test
	public void addSampleValueTag() {
		GeneralDatumSamples s = new GeneralDatumSamples();
		s.putSampleValue(Tag, TEST_TAG, TEST_TAG);
		s.putSampleValue(Tag, "foo", "foo");
		assertThat(s.getT(), hasSize(2));
		assertThat(s.getT(), containsInAnyOrder(TEST_TAG, "foo"));
	}

	@Test
	public void removeSampleValueTag() {
		GeneralDatumSamples s = new GeneralDatumSamples();
		s.putSampleValue(Tag, TEST_TAG, TEST_TAG);
		s.putSampleValue(Tag, TEST_TAG, null);
		assertThat(s.getT(), hasSize(0));
	}

	@Test
	public void removeNonExistingFirstSampleValueTag() {
		GeneralDatumSamples s = new GeneralDatumSamples();
		s.putSampleValue(Tag, TEST_TAG, null);
		assertThat(s.getT(), nullValue());
	}

	@Test
	public void removeNonExistingSampleValueTag() {
		GeneralDatumSamples s = new GeneralDatumSamples();
		s.putSampleValue(Tag, TEST_TAG, TEST_TAG);
		s.putSampleValue(Tag, "foo", null);
		assertThat(s.getT(), hasSize(1));
		assertThat(s.getT(), contains(TEST_TAG));
	}

}
