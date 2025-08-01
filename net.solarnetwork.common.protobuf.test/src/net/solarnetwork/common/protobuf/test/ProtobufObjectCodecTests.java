/* ==================================================================
 * ProtobufObjectCodecTests.java - 28/04/2021 9:31:57 AM
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

package net.solarnetwork.common.protobuf.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.notNullValue;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.codec.binary.Hex;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import net.solarnetwork.common.protobuf.ProtobufCompilerService;
import net.solarnetwork.common.protobuf.ProtobufObjectCodec;
import net.solarnetwork.util.ClassUtils;

/**
 * Test cases for the {@link ProtobufObjectCodec} class.
 *
 * @author matt
 * @version 2.0
 */
public class ProtobufObjectCodecTests extends BaseProtocProtobufCompilerServiceTestSupport {

	private ClassLoader protobufClassLoader;
	private ProtobufObjectCodec codec;

	@Override
	@Before
	public void setup() throws IOException {
		super.setup();

		List<Resource> protos = Collections
				.singletonList(new ClassPathResource("my-datum.proto", getClass()));
		protobufClassLoader = protocService.compileProtobufResources(protos, null);
		codec = new TestProtobufObjectCodec(protobufClassLoader);
		codec.setMessageClassName("sn.PowerDatum");
	}

	@Test
	public void encode() throws IOException {
		// GIVEN
		Map<String, Object> data = new LinkedHashMap<>(4);
		data.put("energy", 123);

		// WHEN
		byte[] result = codec.encodeAsBytes(data, null);

		// THEN
		String hex = Hex.encodeHexString(result);
		assertThat("Protobuf message encoded", hex, equalTo("187b"));
	}

	@Test
	public void decode() throws Exception {
		// GIVEN
		byte[] data = Hex.decodeHex("187b");

		// WHEN
		Object result = codec.decodeFromBytes(data, null);

		// THEN
		assertThat("Protobuf message decoded", result, notNullValue());
		Map<String, Object> props = ClassUtils.getSimpleBeanProperties(result, null);
		assertThat("Energy property decoded", props, hasEntry("energy", 123L));
	}

	private static final class TestProtobufObjectCodec extends ProtobufObjectCodec {

		private final ClassLoader protobufClassLoader;

		public TestProtobufObjectCodec(ClassLoader protobufClassLoader) {
			super(protobufClassLoader);
			this.protobufClassLoader = protobufClassLoader;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected Map<String, ?> convertToMap(Object obj, Map<String, ?> parameters) {
			return (Map<String, ?>) obj;
		}

		@Override
		protected ClassLoader compileProtobufResources(ProtobufCompilerService compiler)
				throws IOException {
			return protobufClassLoader;
		}

	}

}
