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

import static java.util.Collections.singletonMap;
import static net.solarnetwork.util.ByteUtils.objectArray;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.is;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import net.solarnetwork.codec.BasicInstructionStatusSerializer;
import net.solarnetwork.domain.BasicInstructionStatus;
import net.solarnetwork.domain.InstructionStatus;

/**
 * Test cases for the {@link BasicInstructionStatusSerializer} class.
 *
 * @author matt
 * @version 1.0
 */
public class BasicInstructionStatusSerializer_CborTests {

	private static final Instant TEST_STATUS_DATE = LocalDateTime
			.of(2021, 8, 11, 16, 45, 2, (int) TimeUnit.MILLISECONDS.toNanos(345))
			.toInstant(ZoneOffset.UTC);

	private ObjectMapper mapper;

	private ObjectMapper createObjectMapper() {
		ObjectMapper m = new ObjectMapper(new CBORFactory());
		SimpleModule mod = new SimpleModule("Test");
		mod.addSerializer(InstructionStatus.class, BasicInstructionStatusSerializer.INSTANCE);
		m.registerModule(mod);
		return m;
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
		Byte[] cbor = objectArray(mapper.writeValueAsBytes(status));

		// THEN
		assertThat("CBOR", cbor, is(arrayWithSize(69)));
	}

	@Test
	public void serialize_withResultParam() throws IOException {
		// GIVEN
		BasicInstructionStatus status = new BasicInstructionStatus(1L,
				InstructionStatus.InstructionState.Completed, TEST_STATUS_DATE,
				Collections.singletonMap("status", "404"));

		// WHEN
		byte[] raw = mapper.writeValueAsBytes(status);
		//String hex = org.apache.commons.codec.binary.Hex.encodeHexString(raw);
		Byte[] cbor = objectArray(raw);

		// THEN
		assertThat("CBOR", cbor, is(arrayWithSize(99)));
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
		byte[] raw = mapper.writeValueAsBytes(status);
		//String hex = org.apache.commons.codec.binary.Hex.encodeHexString(raw);
		Byte[] cbor = objectArray(raw);

		// THEN
		assertThat("CBOR", cbor, is(arrayWithSize(273)));
	}

}
