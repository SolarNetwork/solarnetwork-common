/* ==================================================================
 * BasicObjectDatumStreamMetadataSerializerTests.java - 7/06/2021 9:57:01 AM
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
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.is;
import java.io.IOException;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import net.solarnetwork.codec.BasicObjectDatumStreamMetadataSerializer;
import net.solarnetwork.domain.BasicLocation;
import net.solarnetwork.domain.datum.BasicObjectDatumStreamMetadata;
import net.solarnetwork.domain.datum.ObjectDatumKind;
import net.solarnetwork.domain.datum.ObjectDatumStreamMetadata;

/**
 * Test cases for the {@link BasicObjectDatumStreamMetadataSerializer} class.
 * 
 * @author matt
 * @version 1.0
 */
public class BasicObjectDatumStreamMetadataSerializer_CborTests {

	private ObjectMapper mapper;

	private ObjectMapper createObjectMapper() {
		ObjectMapper m = new ObjectMapper(new CBORFactory());
		SimpleModule mod = new SimpleModule("Test");
		mod.addSerializer(ObjectDatumStreamMetadata.class,
				BasicObjectDatumStreamMetadataSerializer.INSTANCE);
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
		BasicLocation l = BasicLocation.locationOf("NZ", "Wellington Region", "Pacific/Auckland");

		BasicObjectDatumStreamMetadata meta = new BasicObjectDatumStreamMetadata(UUID.randomUUID(),
				"Pacific/Auckland", ObjectDatumKind.Node, 123L, "test.source", l,
				new String[] { "watts" }, new String[] { "wattHours" }, new String[] { "state" }, null);

		// WHEN
		Byte[] cbor = objectArray(mapper.writeValueAsBytes(meta));

		// THEN
		assertThat("CBOR", cbor, is(arrayWithSize(208)));
	}

	@Test
	public void serialize_noLocation() throws IOException {
		// GIVEN
		BasicObjectDatumStreamMetadata meta = new BasicObjectDatumStreamMetadata(UUID.randomUUID(),
				"Pacific/Auckland", ObjectDatumKind.Node, 123L, "test.source", null,
				new String[] { "watts" }, new String[] { "wattHours" }, new String[] { "state" }, null);

		// WHEN
		Byte[] cbor = objectArray(mapper.writeValueAsBytes(meta));

		// THEN
		assertThat("CBOR", cbor, is(arrayWithSize(140)));
	}

}
