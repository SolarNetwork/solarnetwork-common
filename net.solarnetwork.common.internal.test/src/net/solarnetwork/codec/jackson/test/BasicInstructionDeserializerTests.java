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

package net.solarnetwork.codec.jackson.test;

import static org.assertj.core.api.BDDAssertions.from;
import static org.assertj.core.api.BDDAssertions.then;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import net.solarnetwork.codec.jackson.BasicInstructionDeserializer;
import net.solarnetwork.domain.BasicInstruction;
import net.solarnetwork.domain.BasicInstructionStatus;
import net.solarnetwork.domain.Instruction;
import net.solarnetwork.domain.InstructionStatus;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

/**
 * Test cases for the {@link BasicInstructionDeserializer} class.
 *
 * @author matt
 * @version 1.0
 */
public class BasicInstructionDeserializerTests {

	private static final Instant TEST_DATE = LocalDateTime
			.of(2021, 8, 11, 16, 45, 1, (int) TimeUnit.MILLISECONDS.toNanos(234))
			.toInstant(ZoneOffset.UTC);
	private static final String TEST_DATE_STRING = "2021-08-11 16:45:01.234Z";

	private static final Instant TEST_STATUS_DATE = LocalDateTime
			.of(2021, 8, 11, 16, 45, 2, (int) TimeUnit.MILLISECONDS.toNanos(345))
			.toInstant(ZoneOffset.UTC);
	private static final String TEST_STATUS_DATE_STRING = "2021-08-11 16:45:02.345Z";

	private ObjectMapper createObjectMapper() {
		SimpleModule mod = new SimpleModule("Test");
		mod.addDeserializer(Instruction.class, BasicInstructionDeserializer.INSTANCE);
		return JsonMapper.builder().addModule(mod).build();
	}

	private void thenInstructionEquals(String msg, Instruction instr, Instruction expected) {
		// @formatter:off
		then(instr)
			.as("%s Instruction provided", msg)
			.isNotNull()
			.as("%s ID matches", msg)
			.returns(expected.getId(), from(Instruction::getId))
			.as("%s topic matches", msg)
			.returns(expected.getTopic(), from(Instruction::getTopic))
			.as("%s instr date matches", msg)
			.returns(expected.getInstructionDate(), from(Instruction::getInstructionDate))
			.as("%s status matches", msg)
			.returns(expected.getStatus(), from(Instruction::getStatus))
			.as("%s parameters matches", msg)
			.returns(expected.getParameterMultiMap(), from(Instruction::getParameterMultiMap))
			;
		// @formatter:on
	}

	@Test
	public void deserialize_noParams_noStatus() throws IOException {
		// GIVEN
		ObjectMapper mapper = createObjectMapper();

		// @formatter:off
		String json = "{\n"
				+ "	\"id\" : 1,\n"
				+ "	\"topic\" : \"Mock/Topic\",\n"
				+ "	\"instructionDate\" : \"" +TEST_DATE_STRING + "\"\n"
				+ "}";
		// @formatter:on

		// WHEN
		Instruction result = mapper.readValue(json, Instruction.class);

		// THEN
		BasicInstruction expected = new BasicInstruction(1L, "Mock/Topic", TEST_DATE, null);
		thenInstructionEquals("Instruction without params", result, expected);
	}

	@Test
	public void deserialize_noParams_withStatus_noStatusDate() throws IOException {
		// GIVEN
		ObjectMapper mapper = createObjectMapper();

		// @formatter:off
		String json = "{\n"
				+ "	\"id\" : 1,\n"
				+ "	\"topic\" : \"Mock/Topic\",\n"
				+ "	\"instructionDate\" : \"" +TEST_DATE_STRING + "\",\n"
				+ " \"state\" : \"Completed\"\n"
				+ "}";
		// @formatter:on

		// WHEN
		Instruction result = mapper.readValue(json, Instruction.class);

		// THEN
		BasicInstructionStatus status = new BasicInstructionStatus(1L,
				InstructionStatus.InstructionState.Completed, null);
		BasicInstruction expected = new BasicInstruction(1L, "Mock/Topic", TEST_DATE, status);
		thenInstructionEquals("Instruction without params", result, expected);
	}

	@Test
	public void deserialize_noParams_withStatus_withStatusDate() throws IOException {
		// GIVEN
		ObjectMapper mapper = createObjectMapper();

		// @formatter:off
		String json = "{\n"
				+ "	\"id\" : 1,\n"
				+ "	\"topic\" : \"Mock/Topic\",\n"
				+ "	\"instructionDate\" : \"" +TEST_DATE_STRING + "\",\n"
				+ " \"state\" : \"Completed\",\n"
				+ "	\"statusDate\" : \"" +TEST_STATUS_DATE_STRING + "\"\n"
				+ "}";
		// @formatter:on

		// WHEN
		Instruction result = mapper.readValue(json, Instruction.class);

		// THEN
		BasicInstructionStatus status = new BasicInstructionStatus(1L,
				InstructionStatus.InstructionState.Completed, TEST_STATUS_DATE);
		BasicInstruction expected = new BasicInstruction(1L, "Mock/Topic", TEST_DATE, status);
		thenInstructionEquals("Instruction without params", result, expected);
	}

	@Test
	public void deserialize_noParams_withStatus_withStatusDate_withResultParams() throws IOException {
		// GIVEN
		ObjectMapper mapper = createObjectMapper();

		// @formatter:off
		String json = "{\n"
				+ "	\"id\" : 1,\n"
				+ "	\"topic\" : \"Mock/Topic\",\n"
				+ "	\"instructionDate\" : \"" +TEST_DATE_STRING + "\",\n"
				+ " \"state\" : \"Completed\",\n"
				+ "	\"statusDate\" : \"" +TEST_STATUS_DATE_STRING + "\",\n"
				+ " \"resultParameters\" : {\n"
					+ " \"status\" : \"bar\"\n"
				+ "}\n"
				+ "}";
		// @formatter:on

		// WHEN
		Instruction result = mapper.readValue(json, Instruction.class);

		// THEN
		BasicInstructionStatus status = new BasicInstructionStatus(1L,
				InstructionStatus.InstructionState.Completed, TEST_STATUS_DATE,
				Collections.singletonMap("status", "bar"));
		BasicInstruction expected = new BasicInstruction(1L, "Mock/Topic", TEST_DATE, status);
		thenInstructionEquals("Instruction without params with result params", result, expected);
	}

	@Test
	public void deserialize_withParams_noStatus() throws IOException {
		// GIVEN
		ObjectMapper mapper = createObjectMapper();

		// @formatter:off
		String json = "{\n"
				+ "	\"id\" : 1,\n"
				+ "	\"topic\" : \"Mock/Topic\",\n"
				+ "	\"instructionDate\" : \"" +TEST_DATE_STRING + "\",\n"
				+ "	\"parameters\" : [\n"
				+ "	  { \"name\" : \"foo\", \"value\" : \"bar\" }\n"
				+ "	]\n"
				+ "}";
		// @formatter:on

		// WHEN
		Instruction result = mapper.readValue(json, Instruction.class);

		// THEN
		BasicInstruction expected = new BasicInstruction(1L, "Mock/Topic", TEST_DATE, null);
		expected.addParameter("foo", "bar");
		thenInstructionEquals("Local instruction with params", result, expected);
	}

	@Test
	public void deserialize_noId_noParams_noStatus() throws IOException {
		// GIVEN
		ObjectMapper mapper = createObjectMapper();

		// @formatter:off
		String json = "{\n"
				+ "	\"topic\" : \"Mock/Topic\",\n"
				+ "	\"instructionDate\" : \"" +TEST_DATE_STRING + "\"\n"
				+ "}";
		// @formatter:on

		// WHEN
		Instruction result = mapper.readValue(json, Instruction.class);

		// THEN
		BasicInstruction expected = new BasicInstruction(null, "Mock/Topic", TEST_DATE, null);
		thenInstructionEquals("Instruction without ID or params", result, expected);
	}

	@Test
	public void deserialize_withStatus_withStatusDate_withParameters_withResultParams()
			throws IOException {
		// GIVEN
		ObjectMapper mapper = createObjectMapper();

		// @formatter:off
		String json = """
				{
					"id" : 1,
					"topic" : "Mock/Topic",
					"instructionDate" : "%s",
					"state" : "Completed",
					"statusDate" : "%s",
					"parameters" : {
						"a" : "b"
					},
					"resultParameters" : {
						"status" : "bar"
					}
				}
				""".formatted(TEST_DATE_STRING, TEST_STATUS_DATE_STRING);
		// @formatter:on

		// WHEN
		Instruction result = mapper.readValue(json, Instruction.class);

		// THEN
		BasicInstructionStatus status = new BasicInstructionStatus(1L,
				InstructionStatus.InstructionState.Completed, TEST_STATUS_DATE,
				Collections.singletonMap("status", "bar"));
		BasicInstruction expected = new BasicInstruction(1L, "Mock/Topic", TEST_DATE, status);
		expected.addParameter("a", "b");
		thenInstructionEquals("Instruction without params with result params", result, expected);
	}
}
