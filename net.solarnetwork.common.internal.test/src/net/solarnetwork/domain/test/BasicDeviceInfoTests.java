/* ==================================================================
 * BasicDeviceInfoTests.java - 9/07/2021 7:02:22 AM
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

package net.solarnetwork.domain.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import java.time.LocalDate;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.solarnetwork.domain.BasicDeviceInfo;

/**
 * Test cases for the {@link BasicDeviceInfo} class.
 *
 * @author matt
 * @version 1.1
 */
public class BasicDeviceInfoTests {

	private ObjectMapper objectMapper;

	@Before
	public void setup() {
		objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(Include.NON_NULL);
		objectMapper.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true);
		objectMapper.registerModule(new JavaTimeModule());
	}

	private BasicDeviceInfo getTestInstance() {
		return BasicDeviceInfo.builder().withName("Test").withManufacturer("ACME").withModelName("Blamo")
				.withVersion("2.0").withSerialNumber("1234567890")
				.withManufactureDate(LocalDate.of(2021, 7, 9)).withDeviceAddress("1.2.3.4").build();
	}

	@Test
	public void serializeJson() throws Exception {
		String json = objectMapper.writeValueAsString(getTestInstance());
		assertThat(json, is(equalTo(
				"{\"name\":\"Test\",\"manufacturer\":\"ACME\",\"modelName\":\"Blamo\",\"version\":\"2.0\","
						+ "\"serialNumber\":\"1234567890\",\"manufactureDate\":\"2021-07-09\",\"deviceAddress\":\"1.2.3.4\"}")));
	}

	@Test
	public void serializeJson_withNameplateRatings() throws Exception {
		// GIVEN
		BasicDeviceInfo info = BasicDeviceInfo.builderFrom(getTestInstance())
				.withNameplateRatings(Collections.singletonMap("foo", "bar")).build();

		// WHEN
		String json = objectMapper.writeValueAsString(info);

		// THEN
		assertThat(json, is(equalTo(
				"{\"name\":\"Test\",\"manufacturer\":\"ACME\",\"modelName\":\"Blamo\",\"version\":\"2.0\","
						+ "\"serialNumber\":\"1234567890\",\"manufactureDate\":\"2021-07-09\",\"deviceAddress\":\"1.2.3.4\","
						+ "\"nameplateRatings\":{\"foo\":\"bar\"}}")));
	}

	@Test
	public void deserializeJson_withNameplateRatings() throws Exception {
		// GIVEN
		BasicDeviceInfo info = BasicDeviceInfo.builderFrom(getTestInstance())
				.withNameplateRatings(Collections.singletonMap("foo", "bar")).build();

		// WHEN
		String json = objectMapper.writeValueAsString(info);
		BasicDeviceInfo result = objectMapper.readValue(json, BasicDeviceInfo.class);

		// THEN
		assertThat(result, is(equalTo(info)));
	}

	@Test
	public void builder_isEmpty_allNull() {
		BasicDeviceInfo.Builder b = BasicDeviceInfo.builder();
		assertThat("Builder is empty", b.isEmpty(), is(equalTo(true)));
	}

	@Test
	public void builder_isEmpty_allStringsEmpty() {
		BasicDeviceInfo.Builder b = BasicDeviceInfo.builder().withName("").withManufacturer("")
				.withModelName("").withVersion("").withSerialNumber("").withDeviceAddress("");
		assertThat("Builder is empty", b.isEmpty(), is(equalTo(true)));
	}

	@Test
	public void builder_isEmpty_withName() {
		BasicDeviceInfo.Builder b = BasicDeviceInfo.builder().withName("Test");
		assertThat("Builder not empty", b.isEmpty(), is(equalTo(false)));
	}

	@Test
	public void builder_isEmpty_withManufacturer() {
		BasicDeviceInfo.Builder b = BasicDeviceInfo.builder().withManufacturer("Test");
		assertThat("Builder not empty", b.isEmpty(), is(equalTo(false)));
	}

	@Test
	public void builder_isEmpty_withModelName() {
		BasicDeviceInfo.Builder b = BasicDeviceInfo.builder().withModelName("Test");
		assertThat("Builder not empty", b.isEmpty(), is(equalTo(false)));
	}

	@Test
	public void builder_isEmpty_withVersion() {
		BasicDeviceInfo.Builder b = BasicDeviceInfo.builder().withVersion("Test");
		assertThat("Builder not empty", b.isEmpty(), is(equalTo(false)));
	}

	@Test
	public void builder_isEmpty_withSerialNumber() {
		BasicDeviceInfo.Builder b = BasicDeviceInfo.builder().withSerialNumber("Test");
		assertThat("Builder not empty", b.isEmpty(), is(equalTo(false)));
	}

	@Test
	public void builder_isEmpty_withManufactureDate() {
		BasicDeviceInfo.Builder b = BasicDeviceInfo.builder()
				.withManufactureDate(LocalDate.of(2021, 7, 9));
		assertThat("Builder not empty", b.isEmpty(), is(equalTo(false)));
	}

	@Test
	public void builder_isEmpty_withDeviceAddress() {
		BasicDeviceInfo.Builder b = BasicDeviceInfo.builder().withDeviceAddress("Test");
		assertThat("Builder not empty", b.isEmpty(), is(equalTo(false)));
	}

}
