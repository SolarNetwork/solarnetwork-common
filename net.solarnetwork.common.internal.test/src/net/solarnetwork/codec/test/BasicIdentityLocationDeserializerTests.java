/* ==================================================================
 * BasicLocationDeserializerTests.java - 6/06/2021 3:13:41 PM
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
import java.io.IOException;
import java.math.BigDecimal;
import org.junit.Before;
import org.junit.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import net.solarnetwork.codec.BasicIdentityLocationDeserializer;
import net.solarnetwork.domain.BasicIdentityLocation;
import net.solarnetwork.domain.BasicLocation;

/**
 * Test cases for the {@link BasicIdentityLocationDeserializer} class.
 *
 * @author matt
 * @version 1.0
 */
public class BasicIdentityLocationDeserializerTests {

	private ObjectMapper mapper;

	private ObjectMapper createObjectMapper() {
		ObjectMapper m = new ObjectMapper();
		SimpleModule mod = new SimpleModule("Test");
		mod.addDeserializer(BasicIdentityLocation.class, BasicIdentityLocationDeserializer.INSTANCE);
		m.registerModule(mod);
		return m;
	}

	@Before
	public void setup() {
		mapper = createObjectMapper();
	}

	@Test
	public void deserialize_withoutId() throws IOException {
		// GIVEN
		final String json = "{\"name\":\"Test\",\"country\":\"NZ\",\"region\":\"Wellington Region\""
				+ ",\"stateOrProvince\":\"Wellington State\",\"postalCode\":\"6011\""
				+ ",\"locality\":\"Wellington\",\"street\":\"123 Main Street\""
				+ ",\"lat\":1.23,\"lon\":2.34,\"el\":3.45,\"zone\":\"Pacific/Auckland\"}";

		// WHEN
		BasicIdentityLocation l = mapper.readValue(json, BasicIdentityLocation.class);

		BasicIdentityLocation expected = new BasicIdentityLocation(null,
				new BasicLocation("Test", "NZ", "Wellington Region", "Wellington State", "Wellington",
						"6011", "123 Main Street", new BigDecimal("1.23"), new BigDecimal("2.34"),
						new BigDecimal("3.45"), "Pacific/Auckland"));
		assertThat("Location parsed", l, is(equalTo(expected)));
	}

	@Test
	public void deserialize_withId() throws IOException {
		// GIVEN
		final String json = "{\"id\":123,\"name\":\"Test\",\"country\":\"NZ\",\"region\":\"Wellington Region\""
				+ ",\"stateOrProvince\":\"Wellington State\",\"postalCode\":\"6011\""
				+ ",\"locality\":\"Wellington\",\"street\":\"123 Main Street\""
				+ ",\"lat\":1.23,\"lon\":2.34,\"el\":3.45,\"zone\":\"Pacific/Auckland\"}";

		// WHEN
		BasicIdentityLocation l = mapper.readValue(json, BasicIdentityLocation.class);

		BasicIdentityLocation expected = new BasicIdentityLocation(123L,
				new BasicLocation("Test", "NZ", "Wellington Region", "Wellington State", "Wellington",
						"6011", "123 Main Street", new BigDecimal("1.23"), new BigDecimal("2.34"),
						new BigDecimal("3.45"), "Pacific/Auckland"));
		assertThat("Location parsed", l, is(equalTo(expected)));
	}

}
