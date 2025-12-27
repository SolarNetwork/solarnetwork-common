/* ==================================================================
 * JsonDateUtilsTests.java - 11/08/2021 11:45:58 AM
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

package net.solarnetwork.codec.jackson.test;

import static org.assertj.core.api.BDDAssertions.then;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import net.solarnetwork.codec.jackson.JsonDateUtils;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * Test cases for the {@link JsonDateUtils} class.
 *
 * @author matt
 * @version 1.0
 */
public class JsonDateUtilsTests {

	private ObjectMapper mapper;

	private ObjectMapper createObjectMapper() {
		// @formatter:off
		return JsonMapper.builder()
				.disable(DateTimeFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
				.addModule(JsonDateUtils.JAVA_TIME_MODULE)
				.build();
		// @formatter:on
	}

	@Before
	public void setup() {
		mapper = createObjectMapper();
	}

	@Test
	public void deser_instant() throws IOException {
		// GIVEN
		String json = "\"2021-08-11 11:47:00.123Z\"";

		// WHEN
		Instant result = mapper.readValue(json, Instant.class);

		// THEN
		Instant expected = LocalDateTime
				.of(2021, 8, 11, 11, 47, 0, (int) TimeUnit.MILLISECONDS.toNanos(123))
				.toInstant(ZoneOffset.UTC);
		then(result).as("Instant parsed").isEqualTo(expected);
	}

	@Test
	public void deser_instant_t() throws IOException {
		// GIVEN
		String json = "\"2021-08-11T11:47:00.123Z\"";

		// WHEN
		Instant result = mapper.readValue(json, Instant.class);

		// THEN
		Instant expected = LocalDateTime
				.of(2021, 8, 11, 11, 47, 0, (int) TimeUnit.MILLISECONDS.toNanos(123))
				.toInstant(ZoneOffset.UTC);
		then(result).as("Instant parsed").isEqualTo(expected);
	}

	@Test
	public void deser_instant_t_offset_full() throws IOException {
		// GIVEN
		String json = "\"2021-08-11T11:47:00.123+12:00:00\"";

		// WHEN
		Instant result = mapper.readValue(json, Instant.class);

		// THEN
		Instant expected = LocalDateTime
				.of(2021, 8, 11, 11, 47, 0, (int) TimeUnit.MILLISECONDS.toNanos(123))
				.toInstant(ZoneOffset.ofHours(12));
		then(result).as("Instant parsed").isEqualTo(expected);
	}

	@Test
	public void deser_instant_t_offset_hhmm() throws IOException {
		// GIVEN
		String json = "\"2021-08-11T11:47:00.123+12:00\"";

		// WHEN
		Instant result = mapper.readValue(json, Instant.class);

		// THEN
		Instant expected = LocalDateTime
				.of(2021, 8, 11, 11, 47, 0, (int) TimeUnit.MILLISECONDS.toNanos(123))
				.toInstant(ZoneOffset.ofHours(12));
		then(result).as("Instant parsed").isEqualTo(expected);
	}

	@Test
	public void deser_instant_t_offset_hh_java11() throws IOException {
		// GIVEN
		String json = "\"2021-08-11T11:47:00.123+12\"";

		// WHEN
		Instant result = mapper.readValue(json, Instant.class);

		// THEN
		Instant expected = LocalDateTime
				.of(2021, 8, 11, 11, 47, 0, (int) TimeUnit.MILLISECONDS.toNanos(123))
				.toInstant(ZoneOffset.ofHours(12));
		then(result).as("Instant parsed").isEqualTo(expected);
	}

	@Test
	public void deser_instant_millis() throws IOException {
		// GIVEN
		long now = System.currentTimeMillis();
		String json = String.valueOf(now);

		// WHEN
		Instant result = mapper.readValue(json, Instant.class);

		// THEN
		Instant expected = Instant.ofEpochMilli(now);
		then(result).as("Millisecond epoch parsed").isEqualTo(expected);
	}

	@Test
	public void deser_instant_nanos() throws IOException {
		// GIVEN
		Instant now = Instant.now();
		String json = String.format("%d.%09d", now.getEpochSecond(), now.getNano());

		// WHEN
		Instant result = mapper.readValue(json, Instant.class);

		// THEN
		then(result).as("Seconds.nanoseconds epoch parsed").isEqualTo(now);
	}

	@Test
	public void deser_localDateTime() throws IOException {
		// GIVEN
		String json = "\"2021-08-11 11:47:00.123\"";

		// WHEN
		LocalDateTime result = mapper.readValue(json, LocalDateTime.class);

		// THEN
		LocalDateTime expected = LocalDateTime.of(2021, 8, 11, 11, 47, 0,
				(int) TimeUnit.MILLISECONDS.toNanos(123));
		then(result).as("LocalDateTime parsed").isEqualTo(expected);
	}

	@Test
	public void deser_localDateTime_t() throws IOException {
		// GIVEN
		String json = "\"2021-08-11T11:47:00.123\"";

		// WHEN
		LocalDateTime result = mapper.readValue(json, LocalDateTime.class);

		// THEN
		LocalDateTime expected = LocalDateTime.of(2021, 8, 11, 11, 47, 0,
				(int) TimeUnit.MILLISECONDS.toNanos(123));
		then(result).as("LocalDateTime parsed").isEqualTo(expected);
	}

	@Test
	public void deser_zonedDateTime() throws IOException {
		// GIVEN
		String json = "\"2021-08-11 11:47:00.123Z\"";

		// WHEN
		ZonedDateTime result = mapper.readValue(json, ZonedDateTime.class);

		// THEN
		ZonedDateTime expected = LocalDateTime
				.of(2021, 8, 11, 11, 47, 0, (int) TimeUnit.MILLISECONDS.toNanos(123))
				.atZone(ZoneOffset.UTC);
		then(result).as("ZonedDateTime parsed").isEqualTo(expected);
	}

	@Test
	public void deser_zonedDateTime_t() throws IOException {
		// GIVEN
		String json = "\"2021-08-11T11:47:00.123Z\"";

		// WHEN
		ZonedDateTime result = mapper.readValue(json, ZonedDateTime.class);

		// THEN
		ZonedDateTime expected = LocalDateTime
				.of(2021, 8, 11, 11, 47, 0, (int) TimeUnit.MILLISECONDS.toNanos(123))
				.atZone(ZoneId.of("Z"));
		then(result).as("ZonedDateTime parsed").isEqualTo(expected);
	}

}
