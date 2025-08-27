/* ==================================================================
 * BasicInstructionDeserializerTests.java - 11/08/2021 10:12:05 AM
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import net.solarnetwork.codec.BasicInstructionStatusDeserializer;
import net.solarnetwork.domain.BasicInstructionStatus;
import net.solarnetwork.domain.InstructionStatus;

/**
 * Test cases for the {@link BasicInstructionStatusDeserializer} class.
 *
 * @author matt
 * @version 1.1
 */
public class BasicInstructionStatusDeserializerTests {

	private static final Instant TEST_STATUS_DATE = LocalDateTime
			.of(2021, 8, 11, 16, 45, 2, (int) TimeUnit.MILLISECONDS.toNanos(345))
			.toInstant(ZoneOffset.UTC);
	private static final String TEST_STATUS_DATE_STRING = "2021-08-11 16:45:02.345Z";

	private ObjectMapper createObjectMapper() {
		ObjectMapper m = new ObjectMapper();
		SimpleModule mod = new SimpleModule("Test");
		mod.addDeserializer(InstructionStatus.class, BasicInstructionStatusDeserializer.INSTANCE);
		m.registerModule(mod);
		return m;
	}

	private void assertInstructionStatusEquals(String msg, InstructionStatus instr,
			InstructionStatus expected) {
		assertThat(msg + " not null", instr, is(notNullValue()));
		assertThat(msg + " ID matches", instr.getInstructionId(),
				is(equalTo(expected.getInstructionId())));
		assertThat(msg + " status date matches", instr.getStatusDate(),
				is(equalTo(expected.getStatusDate())));
		assertThat(msg + " state matches", instr.getInstructionState(),
				is(equalTo(expected.getInstructionState())));
		assertThat(msg + " result parameters match", instr.getResultParameters(),
				is(equalTo(expected.getResultParameters())));
	}

	@Test
	public void deserialize_withStatus_noStatusDate() throws IOException {
		// GIVEN
		ObjectMapper mapper = createObjectMapper();

		// @formatter:off
		String json = "{\n"
				+ "	\"instructionId\" : 1,\n"
				+ " \"state\" : \"Completed\"\n"
				+ "}";
		// @formatter:on

		// WHEN
		InstructionStatus result = mapper.readValue(json, InstructionStatus.class);

		// THEN
		BasicInstructionStatus expected = new BasicInstructionStatus(1L,
				InstructionStatus.InstructionState.Completed, null);
		assertInstructionStatusEquals("Status", result, expected);
	}

	@Test
	public void deserialize_withStatus_withStatusDate() throws IOException {
		// GIVEN
		ObjectMapper mapper = createObjectMapper();

		// @formatter:off
		String json = "{\n"
				+ "	\"instructionId\" : 1,\n"
				+ " \"state\" : \"Completed\",\n"
				+ "	\"statusDate\" : \"" +TEST_STATUS_DATE_STRING + "\"\n"
				+ "}";
		// @formatter:on

		// WHEN
		InstructionStatus result = mapper.readValue(json, InstructionStatus.class);

		// THEN
		BasicInstructionStatus expected = new BasicInstructionStatus(1L,
				InstructionStatus.InstructionState.Completed, TEST_STATUS_DATE);
		assertInstructionStatusEquals("Status with date", result, expected);
	}

	@Test
	public void deserialize_altId_withStatus_withStatusDate() throws IOException {
		// GIVEN
		ObjectMapper mapper = createObjectMapper();

		// @formatter:off
		String json = "{\n"
				+ "	\"id\" : 1,\n"
				+ " \"state\" : \"Completed\",\n"
				+ "	\"statusDate\" : \"" +TEST_STATUS_DATE_STRING + "\"\n"
				+ "}";
		// @formatter:on

		// WHEN
		InstructionStatus result = mapper.readValue(json, InstructionStatus.class);

		// THEN
		BasicInstructionStatus expected = new BasicInstructionStatus(1L,
				InstructionStatus.InstructionState.Completed, TEST_STATUS_DATE);
		assertInstructionStatusEquals("Status with date", result, expected);
	}

	@Test
	public void deserialize_altIdDuplicated_withStatus_withStatusDate() throws IOException {
		// GIVEN
		ObjectMapper mapper = createObjectMapper();

		// @formatter:off
		String json = "{\n"
				+ "	\"id\" : 1,\n"
				+ "	\"instructionId\" : 2,\n"
				+ " \"state\" : \"Completed\",\n"
				+ "	\"statusDate\" : \"" +TEST_STATUS_DATE_STRING + "\"\n"
				+ "}";
		// @formatter:on

		// WHEN
		InstructionStatus result = mapper.readValue(json, InstructionStatus.class);

		// THEN
		BasicInstructionStatus expected = new BasicInstructionStatus(2L,
				InstructionStatus.InstructionState.Completed, TEST_STATUS_DATE);
		assertInstructionStatusEquals("Status with date", result, expected);
	}

	@Test
	public void deserialize_withStatus_withStatusDate_withResultParams() throws IOException {
		// GIVEN
		ObjectMapper mapper = createObjectMapper();

		// @formatter:off
		String json = "{\n"
				+ "	\"instructionId\" : 1,\n"
				+ "	\"topic\" : \"Mock/Topic\",\n"
				+ " \"state\" : \"Completed\",\n"
				+ "	\"statusDate\" : \"" +TEST_STATUS_DATE_STRING + "\",\n"
				+ " \"resultParameters\" : {\n"
					+ " \"status\" : \"bar\"\n"
				+ "}\n"
				+ "}";
		// @formatter:on

		// WHEN
		InstructionStatus result = mapper.readValue(json, InstructionStatus.class);

		// THEN
		BasicInstructionStatus expected = new BasicInstructionStatus(1L,
				InstructionStatus.InstructionState.Completed, TEST_STATUS_DATE,
				Collections.singletonMap("status", "bar"));
		assertInstructionStatusEquals("Status with result params", result, expected);
	}

	@Test
	public void deserialize_withStatus_withStatusDate_withComplexResultParams() throws IOException {
		// GIVEN
		ObjectMapper mapper = createObjectMapper();

		// @formatter:off
		String json = "{\n"
				+ "	\"instructionId\" : 1,\n"
				+ "	\"topic\" : \"Mock/Topic\",\n"
				+ " \"state\" : \"Completed\",\n"
				+ "	\"statusDate\" : \"" +TEST_STATUS_DATE_STRING + "\",\n"
				+ " \"resultParameters\" : {\n"
					+ " \"status\" : \"404\"\n"
					+ ",\"list\" : [3,2,1]\n"
					+ ",\"obj\": {\n"
						+ " \"foo\" : \"bar\"\n"
						+ ",\"n\" : 123\n"
					+ "}"
				+ "}\n"
				+ "}";
		// @formatter:on

		// WHEN
		InstructionStatus result = mapper.readValue(json, InstructionStatus.class);

		// THEN
		Map<String, Object> expectedResultParams = new LinkedHashMap<>(4);
		expectedResultParams.put("status", "404");
		expectedResultParams.put("list", Arrays.asList(3, 2, 1));
		Map<String, Object> nested = new LinkedHashMap<>(2);
		nested.put("foo", "bar");
		nested.put("n", 123);
		expectedResultParams.put("obj", nested);
		BasicInstructionStatus expected = new BasicInstructionStatus(1L,
				InstructionStatus.InstructionState.Completed, TEST_STATUS_DATE, expectedResultParams);
		assertInstructionStatusEquals("Status with result params", result, expected);
	}

}
