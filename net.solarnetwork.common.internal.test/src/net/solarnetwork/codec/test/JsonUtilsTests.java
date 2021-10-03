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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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
		assertThat("Instant serialized as SN milliseconds timestamp", json,
				equalTo("{\"ts\":\"2020-06-01 02:03:04.567Z\"}"));
	}

	@Test
	public void serialize_jsr310_Instant_epoch() throws IOException {
		// GIVEN
		Map<String, Object> props = new LinkedHashMap<>(2);
		Instant ts = LocalDateTime.of(2020, 6, 1, 2, 3, 4, (int) TimeUnit.MILLISECONDS.toNanos(567))
				.atOffset(ZoneOffset.UTC).toInstant();
		props.put("ts", ts);

		// WHEN
		ObjectMapper m = JsonUtils.createObjectMapper(null, JsonUtils.JAVA_TIMESTAMP_MODULE);
		m.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		String json = m.writeValueAsString(props);

		// THEN
		assertThat("Instant serialized as milliseconds epoch", json,
				equalTo(String.format("{\"ts\":%d}", ts.toEpochMilli())));
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
		assertThat("ZonedDateTime serialized as SN milliseconds timestamp", json,
				equalTo("{\"ts\":\"2020-06-01 09:03:04.567Z\"}"));
	}

	@Test
	public void serialize_jsr310_ZonedDateTime_epoch() throws IOException {
		// GIVEN
		Map<String, Object> props = new LinkedHashMap<>(2);
		ZonedDateTime ts = LocalDateTime
				.of(2020, 6, 1, 2, 3, 4, (int) TimeUnit.MILLISECONDS.toNanos(567))
				.atZone(ZoneId.of("America/Los_Angeles"));
		props.put("ts", ts);

		// WHEN
		ObjectMapper m = JsonUtils.createObjectMapper(null, JsonUtils.JAVA_TIMESTAMP_MODULE);
		m.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		String json = m.writeValueAsString(props);

		// THEN
		assertThat("ZonedDateTime serialized as millisecond epoch", json,
				equalTo(String.format("{\"ts\":%d}", ts.toInstant().toEpochMilli())));
	}

	@Test
	public void serialize_jsr310_LocalDateTime() {
		// GIVEN
		Map<String, Object> props = new LinkedHashMap<>(2);
		props.put("ts", LocalDateTime.of(2020, 6, 1, 2, 3, 4, (int) TimeUnit.MILLISECONDS.toNanos(567)));

		// WHEN
		String json = JsonUtils.getJSONString(props, null);

		// THEN
		assertThat("LocalDateTime serialized as SN milliseconds local timestamp", json,
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
		assertThat("LocalDate serialized as SN local date string", json,
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
		assertThat("LocalTime serialized as SN local time", json, equalTo("{\"ts\":\"02:03:04.567\"}"));
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

	@Test
	public void parse_jsr310_Instant() throws IOException {
		// GIVEN
		final Instant expected = LocalDateTime
				.of(2020, 6, 1, 2, 3, 4, (int) TimeUnit.MILLISECONDS.toNanos(567))
				.atOffset(ZoneOffset.UTC).toInstant();
		ObjectMapper m = JsonUtils.newObjectMapper();

		// WHEN
		Instant ts = m.readValue("\"2020-06-01 02:03:04.567Z\"", Instant.class);

		// THEN
		assertThat("Instant parsed from SN timestamp", ts, is(expected));
	}

	@Test
	public void parse_jsr310_Instant_epochMillis() throws IOException {
		// GIVEN
		final Instant expected = LocalDateTime
				.of(2020, 6, 1, 2, 3, 4, (int) TimeUnit.MILLISECONDS.toNanos(567))
				.atOffset(ZoneOffset.UTC).toInstant();
		ObjectMapper m = JsonUtils.newObjectMapper();

		// WHEN
		Instant ts = m.readValue(String.valueOf(expected.toEpochMilli()), Instant.class);

		// THEN
		assertThat("Instant parsed from epoch timestamp", ts, is(expected));
	}

	@Test
	public void parse_jsr310_ZonedDateTime() throws IOException {
		// GIVEN
		final ZoneId zone = ZoneId.of("America/Los_Angeles");
		final ZonedDateTime expected = LocalDateTime
				.of(2020, 6, 1, 2, 3, 4, (int) TimeUnit.MILLISECONDS.toNanos(567)).atZone(zone);
		ObjectMapper m = JsonUtils.newObjectMapper();

		// WHEN
		ZonedDateTime ts = m.readValue("\"2020-06-01 09:03:04.567Z\"", ZonedDateTime.class);

		// THEN
		assertThat("ZonedDateTime parsed from SN timestamp", ts.withZoneSameInstant(zone), is(expected));
	}

	@Test
	public void parse_jsr310_ZonedDateTime_epochMillis() throws IOException {
		// GIVEN
		final ZoneId zone = ZoneId.of("America/Los_Angeles");
		final ZonedDateTime expected = LocalDateTime
				.of(2020, 6, 1, 2, 3, 4, (int) TimeUnit.MILLISECONDS.toNanos(567)).atZone(zone);
		ObjectMapper m = JsonUtils.newObjectMapper();

		// WHEN
		ZonedDateTime ts = m.readValue(String.valueOf(expected.toInstant().toEpochMilli()),
				ZonedDateTime.class);

		// THEN
		assertThat("ZonedDateTime parsed from epoch timestamp", ts.withZoneSameInstant(zone),
				is(expected));
	}

}
