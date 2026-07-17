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

import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.Map.entry;
import static org.assertj.core.api.BDDAssertions.then;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import java.io.IOException;
import java.math.BigDecimal;
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
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import net.solarnetwork.codec.JsonUtils;
import net.solarnetwork.domain.BasicDeviceInfo;
import net.solarnetwork.domain.datum.GeneralDatumMetadata;
import net.solarnetwork.util.NumberUtils;

/**
 * Test cases for the {@link JsonUtils} class.
 *
 * @author matt
 * @version 2.3
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
	public void mapFromTree() throws IOException {
		// GIVEN
		final JsonNode input = JsonUtils.newObjectMapper().readTree("""
				{
					"a": 1,
					"b": "two",
					"c": 3.14159
				}
				""");

		// WHEN
		final Map<String, Number> result = JsonUtils.getMapFromTree(input, (k, v) -> {
			return k + "-xformed";
		}, (k, v) -> {
			return NumberUtils.bigDecimalForNumber(v.numberValue());
		});

		// THEN
		// @formatter:off
		then(result)
			.as("Map instance returned")
			.isNotNull()
			.as("Mapper null results are removed from map")
			.containsOnly(
				entry("a-xformed", new BigDecimal(1)),
				// b removed because value transform function returned null
				entry("c-xformed", new BigDecimal("3.14159"))
			)
			;
		// @formatter:on
	}

	@Test
	public void mapFromTree_nullInput() {
		// GIVEN
		final JsonNode input = null;

		// WHEN
		final Map<String, Number> result = JsonUtils.getMapFromTree(input, (k, v) -> {
			return k + "-xformed";
		}, (k, v) -> {
			return v.numberValue();
		});

		// THEN
		// @formatter:off
		then(result)
			.as("Null map instance returned from null input")
			.isNull()
			;
		// @formatter:on
	}

	@Test
	public void mapFromTree_emptyInput() throws IOException {
		// GIVEN
		final JsonNode input = JsonUtils.newObjectMapper().readTree("""
					{}
				""");
		;

		// WHEN
		final Map<String, Number> result = JsonUtils.getMapFromTree(input, (k, v) -> {
			return k + "-xformed";
		}, (k, v) -> {
			return v.numberValue();
		});

		// THEN
		// @formatter:off
		then(result)
			.as("Null map instance returned from empty input")
			.isNull()
			;
		// @formatter:on
	}

	@Test
	public void mapFromTree_nonObjectInput() throws IOException {
		// GIVEN
		final JsonNode input = JsonUtils.newObjectMapper().readTree("""
					"Not an object."
				""");
		;

		// WHEN
		final Map<String, Number> result = JsonUtils.getMapFromTree(input, (k, v) -> {
			return k + "-xformed";
		}, (k, v) -> {
			return v.numberValue();
		});

		// THEN
		// @formatter:off
		then(result)
			.as("Null map instance returned from empty input")
			.isNull()
			;
		// @formatter:on
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

	@Test
	public void parseNonEmptyStringAttribute_null() throws IOException {
		// GIVEN
		JsonNode n = JsonUtils.newObjectMapper().readTree("{\"a\":null}");

		// THEN
		assertThat("Null value resolves as null", JsonUtils.parseNonEmptyStringAttribute(n, "a"),
				is(nullValue()));
	}

	@Test
	public void parseNonEmptyStringAttribute_empty() throws IOException {
		// GIVEN
		JsonNode n = JsonUtils.newObjectMapper().readTree("{\"a\":\"\"}");

		// THEN
		assertThat("Empty value resolves as null", JsonUtils.parseNonEmptyStringAttribute(n, "a"),
				is(nullValue()));
	}

	@Test
	public void parseNonEmptyStringAttribute_blank() throws IOException {
		// GIVEN
		JsonNode n = JsonUtils.newObjectMapper().readTree("{\"a\":\" \"}");

		// THEN
		assertThat("Blank value resolves as-is", JsonUtils.parseNonEmptyStringAttribute(n, "a"),
				is(equalTo(" ")));
	}

	@Test
	public void parseNonEmptyStringAttribute() throws IOException {
		// GIVEN
		JsonNode n = JsonUtils.newObjectMapper().readTree("{\"a\":\"b\"}");

		// THEN
		assertThat("Non-empty value resolves as-is", JsonUtils.parseNonEmptyStringAttribute(n, "a"),
				is(equalTo("b")));
	}

	@Test
	public void parseNonEmptyStringAttribute_number() throws IOException {
		// GIVEN
		JsonNode n = JsonUtils.newObjectMapper().readTree("{\"a\":123}");

		// THEN
		assertThat("Number value resolves as string", JsonUtils.parseNonEmptyStringAttribute(n, "a"),
				is(equalTo("123")));
	}

	@Test
	public void parseLong() throws IOException {
		// GIVEN
		try (JsonParser p = JsonUtils.newObjectMapper().createParser("12345")) {

			// THEN
			assertThat("Long value parsed", JsonUtils.parseLong(p), is(equalTo(12345L)));
		}
	}

	@Test
	public void parseLong_stringValue() throws IOException {
		// GIVEN
		try (JsonParser p = JsonUtils.newObjectMapper().createParser("\"12345\"")) {

			// THEN
			assertThat("Long value parsed", JsonUtils.parseLong(p), is(equalTo(12345L)));
		}
	}

	@Test(expected = InvalidFormatException.class)
	public void parseLong_floatValue() throws IOException {
		// GIVEN
		try (JsonParser p = JsonUtils.newObjectMapper().createParser("\"12345.6789\"")) {

			// WHEN
			JsonUtils.parseLong(p);
		}
	}

	@Test
	public void parseTimestamp_null() throws IOException {
		// GIVEN
		final JsonNode input = null;

		// WHEN
		final Instant result = JsonUtils.parseTimestamp(input);

		// THEN
		then(result).as("Null input returns null").isNull();
	}

	@Test
	public void parseTimestamp_nonValueValue_object() throws IOException {
		// GIVEN
		final JsonNode input = JsonUtils.newObjectMapper().readTree("{}");

		// WHEN
		final Instant result = JsonUtils.parseTimestamp(input);

		// THEN
		then(result).as("Non-value JSON value returns null").isNull();
	}

	@Test
	public void parseTimestamp_nonValueValue_array() throws IOException {
		// GIVEN
		final JsonNode input = JsonUtils.newObjectMapper().readTree("[123345]");

		// WHEN
		final Instant result = JsonUtils.parseTimestamp(input);

		// THEN
		then(result).as("Non-value JSON value returns null").isNull();
	}

	@Test
	public void parseTimestamp_longValue() throws IOException {
		// GIVEN
		final Instant ts = Instant.now().truncatedTo(MILLIS);
		final JsonNode input = JsonUtils.newObjectMapper().readTree("%d".formatted(ts.toEpochMilli()));

		// WHEN
		final Instant result = JsonUtils.parseTimestamp(input);

		// THEN
		then(result).as("Millisecond epoch number parsed as Instant").isEqualTo(ts);
	}

	@Test
	public void parseTimestamp_textValue_iso() throws IOException {
		// GIVEN
		final Instant ts = Instant.now().truncatedTo(MILLIS);
		final JsonNode input = JsonUtils.newObjectMapper().readTree("\"%s\"".formatted(ts.toString()));

		// WHEN
		final Instant result = JsonUtils.parseTimestamp(input);

		// THEN
		then(result).as("ISO 8601 text parsed as Instant").isEqualTo(ts);
	}

	@Test
	public void parseTimestamp_textValue_sn() throws IOException {
		// GIVEN
		final Instant ts = Instant.now().truncatedTo(MILLIS);
		final JsonNode input = JsonUtils.newObjectMapper()
				.readTree("\"%s\"".formatted(ts.toString().replace('T', ' ')));

		// WHEN
		final Instant result = JsonUtils.parseTimestamp(input);

		// THEN
		then(result).as("ISO 8601 text parsed as Instant").isEqualTo(ts);
	}

	@Test
	public void readObject_null() throws IOException {
		// GIVEN
		try (JsonParser p = JsonUtils.newDatumObjectMapper().createParser("""
					{}
				""")) {

			// WHEN
			final GeneralDatumMetadata result = JsonUtils.readObject(p, null,
					GeneralDatumMetadata.class);

			// THEN
			then(result).as("Null input returns null").isNull();
		}
	}

	@Test
	public void readObject_nonObject() throws IOException {
		// GIVEN
		try (JsonParser p = JsonUtils.newDatumObjectMapper().createParser("""
				{"metadata":"Not an object"}
				""")) {
			final JsonNode tree = p.readValueAsTree();

			// WHEN
			final GeneralDatumMetadata result = JsonUtils.readObject(p, tree.path("metadata"),
					GeneralDatumMetadata.class);

			// THEN
			then(result).as("Non-object input returns null").isNull();
		}
	}

	@Test
	public void readObject_jsonNull() throws IOException {
		// GIVEN
		try (JsonParser p = JsonUtils.newDatumObjectMapper().createParser("""
				{"metadata":null}
				""")) {
			final JsonNode tree = p.readValueAsTree();

			// WHEN
			final GeneralDatumMetadata result = JsonUtils.readObject(p, tree.path("metadata"),
					GeneralDatumMetadata.class);

			// THEN
			then(result).as("Non-object input returns null").isNull();
		}
	}

	@Test
	public void readObject() throws IOException {
		// GIVEN
		try (JsonParser p = JsonUtils.newDatumObjectMapper().createParser("""
					{
					"metadata": {
						"m": {
							"foo": "bar"
						},
						"pm": {
							"thing": {
								"one": 2
							}
						}
					}
				}
				""")) {
			final JsonNode tree = p.readValueAsTree();

			// WHEN
			final GeneralDatumMetadata result = JsonUtils.readObject(p, tree.path("metadata"),
					GeneralDatumMetadata.class);

			// THEN
			GeneralDatumMetadata expected = new GeneralDatumMetadata();
			expected.putInfoValue("foo", "bar");
			expected.putInfoValue("thing", "one", 2);

			then(result).as("Tree parsed as object").isEqualTo(expected);
		}
	}

}
