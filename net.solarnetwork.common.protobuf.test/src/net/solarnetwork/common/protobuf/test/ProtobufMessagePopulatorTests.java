/* ==================================================================
 * ProtobufMessagePopulatorTests.java - 25/04/2021 10:01:01 AM
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
import static org.hamcrest.Matchers.notNullValue;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import com.google.protobuf.Message;
import com.google.protobuf.TextFormat;
import net.solarnetwork.common.protobuf.ProtobufMessagePopulator;

/**
 * Test cases for the {@link ProtobufMessagePopulator} class.
 *
 * @author matt
 * @version 1.0
 */
public class ProtobufMessagePopulatorTests extends BaseProtocProtobufCompilerServiceTestSupport {

	@Test
	public void populateMessage() throws Exception {
		// GIVEN
		List<Resource> protos = Arrays.asList(new ClassPathResource("my-datum.proto", getClass()));
		ClassLoader cl = protocService.compileProtobufResources(protos, null);

		// WHEN
		Map<String, Object> data = new LinkedHashMap<>(4);
		data.put("voltage", 1.234);
		data.put("current", 2.345);
		data.put("status", "ERROR");
		data.put("location.lat", 1.2345);
		data.put("location.lon", 2.3456);

		ProtobufMessagePopulator p = new ProtobufMessagePopulator(cl, "sn.PowerDatum");
		p.setMessageProperties(data, false);

		// THEN
		Message m = p.build();
		assertThat("Data created", m, notNullValue());
		// @formatter:off
		assertThat("TXT message", TextFormat.printToString(m), equalTo(
				  "voltage: 1.234\n"
				+ "current: 2.345\n"
				+ "status: ERROR\n"
				+ "location {\n"
				+ "  lat: 1.2345\n"
				+ "  lon: 2.3456\n"
				+ "}\n"
				));
		// @formatter:on
	}

	@Test
	public void populateMessage_convertBigDecimal() throws Exception {
		// GIVEN
		List<Resource> protos = Arrays.asList(new ClassPathResource("my-datum.proto", getClass()));
		ClassLoader cl = protocService.compileProtobufResources(protos, null);

		// WHEN
		Map<String, Object> data = new LinkedHashMap<>(4);
		data.put("voltage", new BigDecimal("1.234"));

		ProtobufMessagePopulator p = new ProtobufMessagePopulator(cl, "sn.PowerDatum");
		p.setMessageProperties(data, false);

		// THEN
		Message m = p.build();
		assertThat("Data created", m, notNullValue());
		// @formatter:off
		assertThat("TXT message", TextFormat.printToString(m), equalTo(
				  "voltage: 1.234\n"
				));
		// @formatter:on
	}

	@Test(expected = IllegalArgumentException.class)
	public void populateMessage_convertError() throws Exception {
		// GIVEN
		List<Resource> protos = Arrays.asList(new ClassPathResource("my-datum.proto", getClass()));
		ClassLoader cl = protocService.compileProtobufResources(protos, null);

		// WHEN
		Map<String, Object> data = new LinkedHashMap<>(4);
		data.put("voltage", "not a number");

		ProtobufMessagePopulator p = new ProtobufMessagePopulator(cl, "sn.PowerDatum");
		p.setMessageProperties(data, false);
	}

	@Test
	public void populateMessage_convertError_ignore() throws Exception {
		// GIVEN
		List<Resource> protos = Arrays.asList(new ClassPathResource("my-datum.proto", getClass()));
		ClassLoader cl = protocService.compileProtobufResources(protos, null);

		// WHEN
		Map<String, Object> data = new LinkedHashMap<>(4);
		data.put("voltage", "not a number");
		data.put("current", 2.345);

		ProtobufMessagePopulator p = new ProtobufMessagePopulator(cl, "sn.PowerDatum");
		p.setMessageProperties(data, true);

		// THEN
		Message m = p.build();
		assertThat("Data created", m, notNullValue());
		// @formatter:off
		assertThat("TXT message", TextFormat.printToString(m), equalTo(
				  "current: 2.345\n"
				));
		// @formatter:on
	}

	@Test(expected = IllegalArgumentException.class)
	public void populateMessage_notAPropertyError() throws Exception {
		// GIVEN
		List<Resource> protos = Arrays.asList(new ClassPathResource("my-datum.proto", getClass()));
		ClassLoader cl = protocService.compileProtobufResources(protos, null);

		// WHEN
		Map<String, Object> data = new LinkedHashMap<>(4);
		data.put("notAProperty", 1.234);

		ProtobufMessagePopulator p = new ProtobufMessagePopulator(cl, "sn.PowerDatum");
		p.setMessageProperties(data, false);
	}

	@Test
	public void populateMessage_notAPropertyError_ignore() throws Exception {
		// GIVEN
		List<Resource> protos = Arrays.asList(new ClassPathResource("my-datum.proto", getClass()));
		ClassLoader cl = protocService.compileProtobufResources(protos, null);

		// WHEN
		Map<String, Object> data = new LinkedHashMap<>(4);
		data.put("notAProperty", 1.234);
		data.put("current", 2.345);

		ProtobufMessagePopulator p = new ProtobufMessagePopulator(cl, "sn.PowerDatum");
		p.setMessageProperties(data, true);

		// THEN
		Message m = p.build();
		assertThat("Data created", m, notNullValue());
		// @formatter:off
		assertThat("TXT message", TextFormat.printToString(m), equalTo(
				  "current: 2.345\n"
				));
		// @formatter:on
	}

}
