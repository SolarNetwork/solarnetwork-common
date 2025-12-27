/* ==================================================================
 * BasicInstructionSerializerTests.java - 11/08/2021 4:13:17 PM
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

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.BDDAssertions.then;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import net.solarnetwork.codec.jackson.BasicInstructionStatusSerializer;
import net.solarnetwork.domain.BasicInstructionStatus;
import net.solarnetwork.domain.InstructionStatus;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

/**
 * Test cases for the {@link BasicInstructionStatusSerializer} class.
 *
 * @author matt
 * @version 1.0
 */
public class BasicInstructionStatusSerializerTests {

	private static final Instant TEST_STATUS_DATE = LocalDateTime
			.of(2021, 8, 11, 16, 45, 2, (int) TimeUnit.MILLISECONDS.toNanos(345))
			.toInstant(ZoneOffset.UTC);
	private static final String TEST_STATUS_DATE_STRING = "2021-08-11 16:45:02.345Z";

	private ObjectMapper mapper;

	private ObjectMapper createObjectMapper() {
		SimpleModule mod = new SimpleModule("Test");
		mod.addSerializer(InstructionStatus.class, BasicInstructionStatusSerializer.INSTANCE);
		return JsonMapper.builder().addModule(mod).build();
	}

	@Before
	public void setup() {
		mapper = createObjectMapper();
	}

	@Test
	public void serialize_noResultParams() throws IOException {
		// GIVEN
		BasicInstructionStatus status = new BasicInstructionStatus(1L,
				InstructionStatus.InstructionState.Completed, TEST_STATUS_DATE);

		// WHEN
		String json = mapper.writeValueAsString(status);

		// THEN
		// @formatter:off
		then(json)
			.as("JSON")
			.isEqualTo(
				"{\"instructionId\":1"
				+ ",\"state\":\"Completed\""
				+ ",\"statusDate\":\"" + TEST_STATUS_DATE_STRING + "\""
				+ "}");
		// @formatter:on
	}

	@Test
	public void serialize_withResultParam() throws IOException {
		// GIVEN
		BasicInstructionStatus status = new BasicInstructionStatus(1L,
				InstructionStatus.InstructionState.Completed, TEST_STATUS_DATE,
				Collections.singletonMap("status", "404"));

		// WHEN
		String json = mapper.writeValueAsString(status);

		// THEN
		// @formatter:off
		then(json)
			.as("JSON")
			.isEqualTo(
				"{\"instructionId\":1"
				+ ",\"state\":\"Completed\""
				+ ",\"statusDate\":\"" + TEST_STATUS_DATE_STRING + "\""
				+ ",\"resultParameters\":{"
				    + "\"status\":\"404\""
				+ "}"
				+ "}");
		// @formatter:on
	}

	@Test
	public void serialize_nestedResultParams() throws IOException {
		// GIVEN
		BasicInstructionStatus status = new BasicInstructionStatus(1L,
				InstructionStatus.InstructionState.Completed, TEST_STATUS_DATE,
				Collections.singletonMap("status", Arrays.asList(
						new BasicInstructionStatus(null, InstructionStatus.InstructionState.Completed,
								TEST_STATUS_DATE, singletonMap("message", "Instr 1")),
						new BasicInstructionStatus(null, InstructionStatus.InstructionState.Declined,
								TEST_STATUS_DATE, singletonMap("message", "Instr 2")))));

		// WHEN
		String json = mapper.writeValueAsString(status);

		// THEN
		// @formatter:off
		then(json)
			.as("JSON")
			.isEqualTo(String.format(
				"{\"instructionId\":1"
				+ ",\"state\":\"Completed\""
				+ ",\"statusDate\":\"%s\""
				+ ",\"resultParameters\":{"
				    + "\"status\":["
				    	+ "{\"state\":\"Completed\",\"statusDate\":\"%s\",\"resultParameters\":"
				    		+"{\"message\":\"Instr 1\"}},"
					    + "{\"state\":\"Declined\",\"statusDate\":\"%s\",\"resultParameters\":"
				    		+"{\"message\":\"Instr 2\"}}"
				    + "]"
				+ "}"
				+ "}", TEST_STATUS_DATE_STRING, TEST_STATUS_DATE_STRING, TEST_STATUS_DATE_STRING));
		// @formatter:on
	}

}
