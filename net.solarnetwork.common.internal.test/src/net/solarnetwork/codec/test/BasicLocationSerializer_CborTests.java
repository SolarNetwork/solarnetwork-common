/* ==================================================================
 * BasicLocationSerializerTests.java - 6/06/2021 3:06:44 PM
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
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import java.math.BigDecimal;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import net.solarnetwork.codec.BasicLocationSerializer;
import net.solarnetwork.codec.test.BasicLocationSerializerTests.IdentityBasicLocation;
import net.solarnetwork.domain.BasicLocation;
import net.solarnetwork.domain.Location;

/**
 * Test cases for the {@link BasicLocationSerializer} class.
 *
 * @author matt
 * @version 1.0
 */
public class BasicLocationSerializer_CborTests {

	private ObjectMapper mapper;

	private ObjectMapper createObjectMapper() {
		ObjectMapper m = new ObjectMapper(new CBORFactory());
		SimpleModule mod = new SimpleModule("Test");
		mod.addSerializer(Location.class, BasicLocationSerializer.INSTANCE);
		m.registerModule(mod);
		return m;
	}

	@Before
	public void setup() {
		mapper = createObjectMapper();
	}

	@Test
	public void serialize_typical() throws IOException {
		// GIVEN
		BasicLocation l = new BasicLocation("Test", "NZ", "Wellington Region", "Wellington State",
				"Wellington", "6011", "123 Main Street", new BigDecimal("1.23"), new BigDecimal("2.34"),
				new BigDecimal("3.45"), "Pacific/Auckland");

		// WHEN
		Byte[] cbor = objectArray(mapper.writeValueAsBytes(l));

		// THEN
		assertThat("CBOR", cbor, is(arrayWithSize(188)));
	}

	@Test
	public void serialize_subset() throws IOException {
		// GIVEN
		BasicLocation l = new BasicLocation("Test", "NZ", "Wellington Region", "Wellington State",
				"Wellington", "6011", null, null, null, null, "Pacific/Auckland");

		// WHEN
		Byte[] cbor = objectArray(mapper.writeValueAsBytes(l));

		// THEN
		assertThat("CBOR", cbor, is(arrayWithSize(138)));
	}

	@Test
	public void serialize_ident() throws IOException {
		// GIVEN
		BasicLocation l = new BasicLocation("Test", "NZ", "Wellington Region", "Wellington State",
				"Wellington", "6011", "123 Main Street", new BigDecimal("1.23"), new BigDecimal("2.34"),
				new BigDecimal("3.45"), "Pacific/Auckland");
		IdentityBasicLocation loc = new IdentityBasicLocation(123L, l);

		// WHEN
		Byte[] cbor = objectArray(mapper.writeValueAsBytes(loc));

		// THEN
		assertThat("CBOR", cbor, is(arrayWithSize(193)));
	}

	@Test
	public void serialize_ident_noId() throws IOException {
		// GIVEN
		BasicLocation l = new BasicLocation("Test", "NZ", "Wellington Region", "Wellington State",
				"Wellington", "6011", "123 Main Street", new BigDecimal("1.23"), new BigDecimal("2.34"),
				new BigDecimal("3.45"), "Pacific/Auckland");
		IdentityBasicLocation loc = new IdentityBasicLocation(null, l);

		// WHEN
		Byte[] cbor = objectArray(mapper.writeValueAsBytes(loc));

		// THEN
		assertThat("CBOR", cbor, is(Matchers.equalTo(objectArray(mapper.writeValueAsBytes(l)))));
	}
}
