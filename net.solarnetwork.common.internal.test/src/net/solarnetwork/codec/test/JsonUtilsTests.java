/* ==================================================================
 * JsonUtilsTests.java - 16/06/2020 9:35:25 am
 * 
 * Copyright 2020 SolarNetwork.net Dev Team
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

package net.solarnetwork.codec.test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import net.solarnetwork.codec.JsonUtils;
import net.solarnetwork.domain.BasicDeviceInfo;

/**
 * Test cases for the {@link JsonUtils} class.
 * 
 * @author matt
 * @version 2.0
 */
public class JsonUtilsTests {

	@Test
	public void serialize_jsr310_Instant() {
		// GIVEN
		Map<String, Object> props = new LinkedHashMap<>(2);
		props.put("ts", LocalDateTime.of(2020, 6, 1, 2, 3, 4, (int) TimeUnit.MILLISECONDS.toNanos(567))
				.atOffset(ZoneOffset.UTC).toInstant());

		// WHEN
		String json = JsonUtils.getJSONString(props, null);

		// THEN
		assertThat("Instant serialized as milliseconds timestamp", json,
				equalTo("{\"ts\":\"2020-06-01 02:03:04.567Z\"}"));
	}

	@Test
	public void serialize_jsr310_ZonedDateTime() {
		// GIVEN
		Map<String, Object> props = new LinkedHashMap<>(2);
		props.put("ts", LocalDateTime.of(2020, 6, 1, 2, 3, 4, (int) TimeUnit.MILLISECONDS.toNanos(567))
				.atZone(ZoneId.of("America/Los_Angeles")));

		// WHEN
		String json = JsonUtils.getJSONString(props, null);

		// THEN
		assertThat("ZonedDateTime serialized as milliseconds timestamp", json,
				equalTo("{\"ts\":\"2020-06-01 09:03:04.567Z\"}"));
	}

	@Test
	public void serialize_jsr310_LocalDateTime() {
		// GIVEN
		Map<String, Object> props = new LinkedHashMap<>(2);
		props.put("ts", LocalDateTime.of(2020, 6, 1, 2, 3, 4, (int) TimeUnit.MILLISECONDS.toNanos(567)));

		// WHEN
		String json = JsonUtils.getJSONString(props, null);

		// THEN
		assertThat("LocalDateTime serialized as milliseconds timestamp", json,
				equalTo("{\"ts\":\"2020-06-01 02:03:04.567\"}"));
	}

	@Test
	public void serialize_jsr310_LocalDate() {
		// GIVEN
		Map<String, Object> props = new LinkedHashMap<>(2);
		props.put("ts", LocalDate.of(2020, 6, 1));

		// WHEN
		String json = JsonUtils.getJSONString(props, null);

		// THEN
		assertThat("LocalDate serialized as milliseconds timestamp", json,
				equalTo("{\"ts\":\"2020-06-01\"}"));
	}

	@Test
	public void serialize_jsr310_LocalTime() {
		// GIVEN
		Map<String, Object> props = new LinkedHashMap<>(2);
		props.put("ts", LocalTime.of(2, 3, 4, (int) TimeUnit.MILLISECONDS.toNanos(567)));

		// WHEN
		String json = JsonUtils.getJSONString(props, null);

		// THEN
		assertThat("LocalDate serialized as milliseconds timestamp", json,
				equalTo("{\"ts\":\"02:03:04.567\"}"));
	}

	@Test
	public void stringMapFromObject() {
		// GIVEN
		BasicDeviceInfo info = BasicDeviceInfo.builder().withName("Super").withManufacturer("ACME")
				.withManufactureDate(LocalDate.of(2021, 7, 9)).build();

		// WHEN
		Map<String, Object> m = JsonUtils.getStringMapFromObject(info);

		// THEN
		assertThat("Map created", m, notNullValue());
		assertThat("Name property serialized", m, hasEntry("name", "Super"));
		assertThat("Manufacturer property serialized", m, hasEntry("manufacturer", "ACME"));
		assertThat("Manufacture date property serialized", m, hasEntry("manufactureDate", "2021-07-09"));
	}

	@Test
	public void stringMapFromObject_null() {
		// GIVEN

		// WHEN
		Map<String, Object> m = JsonUtils.getStringMapFromObject(null);

		// THEN
		assertThat("Map not created", m, nullValue());
	}

}
