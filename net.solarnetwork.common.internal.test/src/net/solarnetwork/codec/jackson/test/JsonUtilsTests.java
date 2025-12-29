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

package net.solarnetwork.codec.jackson.test;

import static org.assertj.core.api.BDDAssertions.catchThrowableOfType;
import static org.assertj.core.api.BDDAssertions.then;
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
import net.solarnetwork.codec.jackson.JsonDateUtils;
import net.solarnetwork.codec.jackson.JsonUtils;
import net.solarnetwork.domain.BasicDeviceInfo;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.exc.InvalidFormatException;
import tools.jackson.databind.json.JsonMapper;

/**
 * Test cases for the {@link JsonUtils} class.
 *
 * @author matt
 * @version 2.2
 */
public class JsonUtilsTests {

	@Test
	public void serialize_Instant() {
		// GIVEN
		Map<String, Object> props = new LinkedHashMap<>(2);
		props.put("ts", LocalDateTime.of(2020, 6, 1, 2, 3, 4, (int) TimeUnit.MILLISECONDS.toNanos(567))
				.atOffset(ZoneOffset.UTC).toInstant());

		// WHEN
		String json = JsonUtils.getJSONString(props, null);

		// THEN
		then(json).as("Instant serialized as SN milliseconds timestamp")
				.isEqualTo("{\"ts\":\"2020-06-01 02:03:04.567Z\"}");
	}

	private ObjectMapper msEpochMapper() {
		return JsonMapper.builder().addModule(JsonDateUtils.JAVA_TIMESTAMP_MODULE)
				.enable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
				.disable(DateTimeFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS).build();
	}

	@Test
	public void serialize_Instant_epoch() {
		// GIVEN
		Map<String, Object> props = new LinkedHashMap<>(2);
		Instant ts = LocalDateTime.of(2020, 6, 1, 2, 3, 4, (int) TimeUnit.MILLISECONDS.toNanos(567))
				.atOffset(ZoneOffset.UTC).toInstant();
		props.put("ts", ts);

		// WHEN
		ObjectMapper m = msEpochMapper();
		String json = m.writeValueAsString(props);

		// THEN
		then(json).as("Instant serialized as millisecond epoch")
				.isEqualTo(String.format("{\"ts\":%d}", ts.toEpochMilli()));
	}

	@Test
	public void serialize_ZonedDateTime() {
		// GIVEN
		Map<String, Object> props = new LinkedHashMap<>(2);
		props.put("ts", LocalDateTime.of(2020, 6, 1, 2, 3, 4, (int) TimeUnit.MILLISECONDS.toNanos(567))
				.atZone(ZoneId.of("America/Los_Angeles")));

		// WHEN
		String json = JsonUtils.getJSONString(props, null);

		// THEN
		then(json).as("Instant serialized as SN milliseconds timestamp")
				.isEqualTo("{\"ts\":\"2020-06-01 09:03:04.567Z\"}");
	}

	@Test
	public void serialize_ZonedDateTime_epoch() {
		// GIVEN
		Map<String, Object> props = new LinkedHashMap<>(2);
		ZonedDateTime ts = LocalDateTime
				.of(2020, 6, 1, 2, 3, 4, (int) TimeUnit.MILLISECONDS.toNanos(567))
				.atZone(ZoneId.of("America/Los_Angeles"));
		props.put("ts", ts);

		// WHEN
		ObjectMapper m = msEpochMapper();
		String json = m.writeValueAsString(props);

		// THEN
		then(json).as("Instant serialized as millisecond epoch")
				.isEqualTo(String.format("{\"ts\":%d}", ts.toInstant().toEpochMilli()));
	}

	@Test
	public void serialize_LocalDateTime() {
		// GIVEN
		Map<String, Object> props = new LinkedHashMap<>(2);
		props.put("ts", LocalDateTime.of(2020, 6, 1, 2, 3, 4, (int) TimeUnit.MILLISECONDS.toNanos(567)));

		// WHEN
		String json = JsonUtils.getJSONString(props, null);

		// THEN
		then(json).as("LocalDateTime serialized as SN milliseconds timestamp")
				.isEqualTo("{\"ts\":\"2020-06-01 02:03:04.567\"}");
	}

	@Test
	public void serialize_LocalDate() {
		// GIVEN
		Map<String, Object> props = new LinkedHashMap<>(2);
		props.put("ts", LocalDate.of(2020, 6, 1));

		// WHEN
		String json = JsonUtils.getJSONString(props, null);

		// THEN
		then(json).as("LocalDate serialized as SN local date string")
				.isEqualTo("{\"ts\":\"2020-06-01\"}");
	}

	@Test
	public void serialize_LocalTime() {
		// GIVEN
		Map<String, Object> props = new LinkedHashMap<>(2);
		props.put("ts", LocalTime.of(2, 3, 4, (int) TimeUnit.MILLISECONDS.toNanos(567)));

		// WHEN
		String json = JsonUtils.getJSONString(props, null);

		// THEN
		then(json).as("LocalTime serialized as SN local date string")
				.isEqualTo("{\"ts\":\"02:03:04.567\"}");
	}

	@Test
	public void stringMapFromObject() {
		// GIVEN
		BasicDeviceInfo info = BasicDeviceInfo.builder().withName("Super").withManufacturer("ACME")
				.withManufactureDate(LocalDate.of(2021, 7, 9)).build();

		// WHEN
		Map<String, Object> m = JsonUtils.getStringMapFromObject(info);

		// THEN
		// @formatter:off
		then(m)
			.as("Map created")
			.isNotNull()
			.as("Name property serialized")
			.containsEntry("name", "Super")
			.as("Manufacturer property serialized")
			.containsEntry("manufacturer", "ACME")
			.as("Manufacture date property serialized")
			.containsEntry("manufactureDate", "2021-07-09")
			;
		// /@formatter:on
	}

	@Test
	public void stringMapFromObject_null() {
		// GIVEN

		// WHEN
		Map<String, Object> m = JsonUtils.getStringMapFromObject(null);

		// THEN
		then(m).as("Map not created").isNull();
	}

	@Test
	public void parse_Instant() {
		// GIVEN
		final Instant expected = LocalDateTime
				.of(2020, 6, 1, 2, 3, 4, (int) TimeUnit.MILLISECONDS.toNanos(567))
				.atOffset(ZoneOffset.UTC).toInstant();

		// WHEN
		Instant ts = JsonUtils.JSON_OBJECT_MAPPER.readValue("\"2020-06-01 02:03:04.567Z\"",
				Instant.class);

		// THEN
		then(ts).as("Instant parsed from SN timestamp").isEqualTo(expected);
	}

	@Test
	public void parse_Instant_epochMillis() {
		// GIVEN
		final Instant expected = LocalDateTime
				.of(2020, 6, 1, 2, 3, 4, (int) TimeUnit.MILLISECONDS.toNanos(567))
				.atOffset(ZoneOffset.UTC).toInstant();

		// WHEN
		Instant ts = JsonUtils.JSON_OBJECT_MAPPER.readValue(String.valueOf(expected.toEpochMilli()),
				Instant.class);

		// THEN
		then(ts).as("Instant parsed from epoch timestamp").isEqualTo(expected);
	}

	@Test
	public void parse_ZonedDateTime() {
		// GIVEN
		final ZoneId zone = ZoneId.of("America/Los_Angeles");
		final ZonedDateTime expected = LocalDateTime
				.of(2020, 6, 1, 2, 3, 4, (int) TimeUnit.MILLISECONDS.toNanos(567)).atZone(zone);

		// WHEN
		ZonedDateTime ts = JsonUtils.JSON_OBJECT_MAPPER.readValue("\"2020-06-01 09:03:04.567Z\"",
				ZonedDateTime.class);

		// THEN
		then(ts.withZoneSameInstant(zone)).as("ZonedDateTime parsed from SN timestamp")
				.isEqualTo(expected);
	}

	@Test
	public void parse_ZonedDateTime_epochMillis() {
		// GIVEN
		final ZoneId zone = ZoneId.of("America/Los_Angeles");
		final ZonedDateTime expected = LocalDateTime
				.of(2020, 6, 1, 2, 3, 4, (int) TimeUnit.MILLISECONDS.toNanos(567)).atZone(zone);

		// WHEN
		ZonedDateTime ts = JsonUtils.JSON_OBJECT_MAPPER
				.readValue(String.valueOf(expected.toInstant().toEpochMilli()), ZonedDateTime.class);

		// THEN
		then(ts.withZoneSameInstant(zone)).as("ZonedDateTime parsed from epoch timestamp")
				.isEqualTo(expected);
	}

	@Test
	public void parseNonEmptyStringAttribute_null() {
		// GIVEN
		JsonNode n = JsonUtils.JSON_OBJECT_MAPPER.readTree("{\"a\":null}");

		// THEN
		then(JsonUtils.parseNonEmptyStringAttribute(n, "a")).as("Null value resolves as null").isNull();
	}

	@Test
	public void parseNonEmptyStringAttribute_empty() {
		// GIVEN
		JsonNode n = JsonUtils.JSON_OBJECT_MAPPER.readTree("{\"a\":\"\"}");

		// THEN
		then(JsonUtils.parseNonEmptyStringAttribute(n, "a")).as("Empty value resolves as null").isNull();
	}

	@Test
	public void parseNonEmptyStringAttribute_blank() {
		// GIVEN
		JsonNode n = JsonUtils.JSON_OBJECT_MAPPER.readTree("{\"a\":\" \"}");

		// THEN
		then(JsonUtils.parseNonEmptyStringAttribute(n, "a")).as("Blank value resolves as-is")
				.isEqualTo(" ");
	}

	@Test
	public void parseNonEmptyStringAttribute() {
		// GIVEN
		JsonNode n = JsonUtils.JSON_OBJECT_MAPPER.readTree("{\"a\":\"b\"}");

		// THEN
		then(JsonUtils.parseNonEmptyStringAttribute(n, "a")).as("Non-empty value resolves as-is")
				.isEqualTo("b");
	}

	@Test
	public void parseNonEmptyStringAttribute_number() {
		// GIVEN
		JsonNode n = JsonUtils.JSON_OBJECT_MAPPER.readTree("{\"a\":123}");

		// THEN
		then(JsonUtils.parseNonEmptyStringAttribute(n, "a")).as("Number value resolves as string")
				.isEqualTo("123");
	}

	@Test
	public void parseLong() {
		// GIVEN
		try (JsonParser p = JsonUtils.JSON_OBJECT_MAPPER.createParser("12345")) {

			// THEN
			then(JsonUtils.parseLong(p)).as("Long value parsed").isEqualTo(12345L);
		}
	}

	@Test
	public void parseLong_stringValue() {
		// GIVEN
		try (JsonParser p = JsonUtils.JSON_OBJECT_MAPPER.createParser("\"12345\"")) {

			// THEN
			then(JsonUtils.parseLong(p)).as("Long value parsed").isEqualTo(12345L);
		}
	}

	@Test
	public void parseLong_floatValue() {
		// GIVEN
		try (JsonParser p = JsonUtils.JSON_OBJECT_MAPPER.createParser("\"12345.6789\"")) {

			// WHEN
			catchThrowableOfType(InvalidFormatException.class, () -> JsonUtils.parseLong(p));
		}
	}

}
