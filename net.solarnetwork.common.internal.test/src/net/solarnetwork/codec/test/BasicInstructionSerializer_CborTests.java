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

package net.solarnetwork.codec.test;

import static net.solarnetwork.util.ByteUtils.objectArray;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.is;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.commons.codec.binary.Hex;
import org.junit.Before;
import org.junit.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import net.solarnetwork.codec.BasicInstructionSerializer;
import net.solarnetwork.domain.BasicInstruction;
import net.solarnetwork.domain.BasicInstructionStatus;
import net.solarnetwork.domain.Instruction;
import net.solarnetwork.domain.InstructionStatus;

/**
 * Test cases for the {@link BasicInstructionSerializer} class.
 *
 * @author matt
 * @version 1.0
 */
public class BasicInstructionSerializer_CborTests {

	private static final Instant TEST_DATE = LocalDateTime
			.of(2021, 8, 11, 16, 45, 1, (int) TimeUnit.MILLISECONDS.toNanos(234))
			.toInstant(ZoneOffset.UTC);

	private static final Instant TEST_STATUS_DATE = LocalDateTime
			.of(2021, 8, 11, 16, 45, 2, (int) TimeUnit.MILLISECONDS.toNanos(345))
			.toInstant(ZoneOffset.UTC);

	private ObjectMapper mapper;

	private ObjectMapper createObjectMapper() {
		ObjectMapper m = new ObjectMapper(new CBORFactory());
		SimpleModule mod = new SimpleModule("Test");
		mod.addSerializer(Instruction.class, BasicInstructionSerializer.INSTANCE);
		m.registerModule(mod);
		return m;
	}

	@Before
	public void setup() {
		mapper = createObjectMapper();
	}

	@Test
	public void serialize_noParams() throws IOException {
		// GIVEN
		BasicInstruction instr = new BasicInstruction(1L, "Mock/Test", TEST_DATE, null);

		// WHEN
		Byte[] cbor = objectArray(mapper.writeValueAsBytes(instr));

		// THEN
		assertThat("CBOR", cbor, is(arrayWithSize(63)));
	}

	@Test
	public void serialize_noParams_withStatus_noResultParams() throws IOException {
		// GIVEN
		BasicInstructionStatus status = new BasicInstructionStatus(1L,
				InstructionStatus.InstructionState.Completed, TEST_STATUS_DATE);
		BasicInstruction instr = new BasicInstruction(1L, "Mock/Test", TEST_DATE, status);

		// WHEN
		Byte[] cbor = objectArray(mapper.writeValueAsBytes(instr));

		// THEN
		assertThat("CBOR", cbor, is(arrayWithSize(116)));
	}

	@Test
	public void serialize_noParams_withStatus_withResultParam() throws IOException {
		// GIVEN
		BasicInstructionStatus status = new BasicInstructionStatus(1L,
				InstructionStatus.InstructionState.Completed, TEST_STATUS_DATE,
				Collections.singletonMap("status", "404"));
		BasicInstruction instr = new BasicInstruction(1L, "Mock/Test", TEST_DATE, status);

		// WHEN
		Byte[] cbor = objectArray(mapper.writeValueAsBytes(instr));

		// THEN
		assertThat("CBOR", cbor, is(arrayWithSize(146)));
	}

	@Test
	public void serialize_withParam() throws IOException {
		// GIVEN
		BasicInstruction instr = new BasicInstruction(1L, "Mock/Test", TEST_DATE, null);
		instr.addParameter("foo", "bar");

		// WHEN
		Byte[] cbor = objectArray(mapper.writeValueAsBytes(instr));

		// THEN
		assertThat("CBOR", cbor, is(arrayWithSize(92)));
	}

	@Test
	public void serialize_withParams() throws IOException {
		// GIVEN
		BasicInstruction instr = new BasicInstruction(1L, "Mock/Test", TEST_DATE, null);
		instr.addParameter("foo", "bar");
		instr.addParameter("bim", "bam");

		// WHEN
		Byte[] cbor = objectArray(mapper.writeValueAsBytes(instr));

		// THEN
		assertThat("CBOR", cbor, is(arrayWithSize(113)));
	}

	@Test
	public void serialize_withParams_duplicate() throws IOException {
		// GIVEN
		BasicInstruction instr = new BasicInstruction(1L, "Mock/Test", TEST_DATE, null);
		instr.addParameter("foo", "f1");
		instr.addParameter("foo", "f2");

		// WHEN
		Byte[] cbor = objectArray(mapper.writeValueAsBytes(instr));

		// THEN
		assertThat("CBOR", cbor, is(arrayWithSize(111)));
	}

	@Test
	public void serialize_withComplexResultParams() throws Exception {
		// GIVEN
		Map<String, Object> resultParams = new LinkedHashMap<>(4);
		resultParams.put("status", "404");
		resultParams.put("list", Arrays.asList(1, 2, 3));
		resultParams.put("obj", Collections.singletonMap("n", 1));
		BasicInstructionStatus status = new BasicInstructionStatus(1L,
				InstructionStatus.InstructionState.Completed, TEST_STATUS_DATE, resultParams);
		BasicInstruction instr = new BasicInstruction(1L, "Mock/Test", TEST_DATE, status);
		instr.addParameter("foo", "bar");

		// WHEN
		byte[] cbor = mapper.writeValueAsBytes(instr);

		// THEN
		Byte[] result = objectArray(cbor);
		Byte[] expected = objectArray(Hex.decodeHex(
		// @formatter:off
				  "a76269640165746f706963694d6f636b2f546573746f696e737472756374696f6e446174"
				+ "657818323032312d30382d31312031363a34353a30312e3233345a66706172616d7381bf"
				+ "646e616d6563666f6f6576616c756563626172ff65737461746569436f6d706c65746564"
				+ "6a737461747573446174657818323032312d30382d31312031363a34353a30322e333435"
				+ "5a70726573756c74506172616d6574657273bf6673746174757363343034646c69737483"
				+ "010203636f626abf616e01ffff"
				// @formatter:on
		));
		assertThat("CBOR", result, is(arrayContaining(expected)));

	}

}
