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

package net.solarnetwork.codec.test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import net.solarnetwork.codec.JsonDateUtils;

/**
 * Test cases for the {@link JsonDateUtils} class.
 * 
 * @author matt
 * @version 1.0
 */
public class JsonDateUtilsTests {

	private ObjectMapper mapper;

	private ObjectMapper createObjectMapper() {
		ObjectMapper m = new ObjectMapper();
		m.disable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS);
		SimpleModule mod = new SimpleModule("Test");
		mod.addSerializer(Instant.class, JsonDateUtils.InstantSerializer.INSTANCE);
		mod.addSerializer(LocalDateTime.class, JsonDateUtils.LocalDateTimeSerializer.INSTANCE);
		mod.addSerializer(ZonedDateTime.class, JsonDateUtils.ZonedDateTimeSerializer.INSTANCE);
		mod.addDeserializer(Instant.class, JsonDateUtils.InstantDeserializer.INSTANCE);
		mod.addDeserializer(LocalDateTime.class, JsonDateUtils.LocalDateTimeDeserializer.INSTANCE);
		mod.addDeserializer(ZonedDateTime.class, JsonDateUtils.ZonedDateTimeDeserializer.INSTANCE);
		m.registerModule(mod);
		return m;
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
		assertThat("Instant parsed", result, is(equalTo(expected)));
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
		assertThat("Instant parsed as milliseconds", result, is(equalTo(expected)));
	}

	@Test
	public void deser_instant_nanos() throws IOException {
		// GIVEN
		Instant now = Instant.now();
		String json = String.format("%d.%09d", now.getEpochSecond(), now.getNano());

		// WHEN
		Instant result = mapper.readValue(json, Instant.class);

		// THEN
		assertThat("Instant parsed as seconds.nanoseconds", result, is(equalTo(now)));
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
		assertThat("LocalDateTime parsed", result, is(equalTo(expected)));
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
				.atZone(ZoneId.of("UTC"));
		assertThat("Instant parsed", result, is(equalTo(expected)));
	}

}
