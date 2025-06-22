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
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import net.solarnetwork.codec.BasicInstructionDeserializer;
import net.solarnetwork.domain.BasicInstruction;
import net.solarnetwork.domain.BasicInstructionStatus;
import net.solarnetwork.domain.Instruction;
import net.solarnetwork.domain.InstructionStatus;

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

	private ObjectMapper createObjectMapper(JsonDeserializer<Instruction> deserializer) {
		ObjectMapper m = new ObjectMapper();
		SimpleModule mod = new SimpleModule("Test");
		mod.addDeserializer(Instruction.class, deserializer);
		m.registerModule(mod);
		return m;
	}

	private void assertInstructionEquals(String msg, Instruction instr, Instruction expected) {
		assertThat(msg + " not null", instr, is(notNullValue()));
		assertThat(msg + " ID matches", instr.getId(), is(equalTo(expected.getId())));
		assertThat(msg + " topic matches", instr.getTopic(), is(equalTo(expected.getTopic())));
		assertThat(msg + " instr date matches", instr.getInstructionDate(),
				is(equalTo(expected.getInstructionDate())));
		assertThat(msg + " status matches", instr.getStatus(), is(equalTo(expected.getStatus())));
		assertThat(msg + " parameters match", instr.getParameterMultiMap(),
				is(equalTo(expected.getParameterMultiMap())));
	}

	@Test
	public void deserialize_noParams_noStatus() throws IOException {
		// GIVEN
		ObjectMapper mapper = createObjectMapper(BasicInstructionDeserializer.INSTANCE);

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
		assertInstructionEquals("Instruction without params", result, expected);
	}

	@Test
	public void deserialize_noParams_withStatus_noStatusDate() throws IOException {
		// GIVEN
		ObjectMapper mapper = createObjectMapper(BasicInstructionDeserializer.INSTANCE);

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
		assertInstructionEquals("Instruction without params", result, expected);
	}

	@Test
	public void deserialize_noParams_withStatus_withStatusDate() throws IOException {
		// GIVEN
		ObjectMapper mapper = createObjectMapper(BasicInstructionDeserializer.INSTANCE);

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
		assertInstructionEquals("Instruction without params", result, expected);
	}

	@Test
	public void deserialize_noParams_withStatus_withStatusDate_withResultParams() throws IOException {
		// GIVEN
		ObjectMapper mapper = createObjectMapper(BasicInstructionDeserializer.INSTANCE);

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
		assertInstructionEquals("Instruction without params with result params", result, expected);
	}

	@Test
	public void deserialize_withParams_noStatus() throws IOException {
		// GIVEN
		ObjectMapper mapper = createObjectMapper(BasicInstructionDeserializer.INSTANCE);

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
		assertInstructionEquals("Local instruction with params", result, expected);
	}

	@Test
	public void deserialize_noId_noParams_noStatus() throws IOException {
		// GIVEN
		ObjectMapper mapper = createObjectMapper(BasicInstructionDeserializer.INSTANCE);

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
		assertInstructionEquals("Instruction without ID or params", result, expected);
	}

}
