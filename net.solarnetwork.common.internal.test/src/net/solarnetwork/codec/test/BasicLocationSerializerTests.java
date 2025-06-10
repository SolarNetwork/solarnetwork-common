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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import java.io.IOException;
import java.math.BigDecimal;
import org.junit.Before;
import org.junit.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import net.solarnetwork.codec.BasicLocationSerializer;
import net.solarnetwork.domain.BasicLocation;
import net.solarnetwork.domain.Identity;
import net.solarnetwork.domain.Location;

/**
 * Test cases for the {@link BasicLocationSerializer} class.
 *
 * @author matt
 * @version 1.0
 */
public class BasicLocationSerializerTests {

	private ObjectMapper mapper;

	private ObjectMapper createObjectMapper() {
		ObjectMapper m = new ObjectMapper();
		SimpleModule mod = new SimpleModule("Test");
		mod.addSerializer(Location.class, BasicLocationSerializer.INSTANCE);
		m.registerModule(mod);
		return m;
	}

	/**
	 * Test location that is also an identity.
	 */
	public static class IdentityBasicLocation extends BasicLocation implements Identity<Long> {

		private static final long serialVersionUID = -6977678277913241969L;

		private Long id = null;

		public IdentityBasicLocation(Long id, Location loc) {
			super(loc);
			this.id = id;
		}

		@Override
		public int compareTo(Long o) {
			if ( o == null && id == null ) {
				return 0;
			} else if ( o == null ) {
				return -1;
			} else if ( id == null ) {
				return 1;
			}
			return o.compareTo(id);
		}

		@Override
		public Long getId() {
			return id;
		}

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
		String json = mapper.writeValueAsString(l);

		// THEN
		assertThat("JSON", json,
				is(equalTo("{\"name\":\"Test\",\"country\":\"NZ\",\"region\":\"Wellington Region\""
						+ ",\"stateOrProvince\":\"Wellington State\",\"postalCode\":\"6011\""
						+ ",\"locality\":\"Wellington\",\"street\":\"123 Main Street\""
						+ ",\"lat\":1.23,\"lon\":2.34,\"el\":3.45,\"zone\":\"Pacific/Auckland\"}")));
	}

	@Test
	public void serialize_ident() throws IOException {
		// GIVEN
		BasicLocation l = new BasicLocation("Test", "NZ", "Wellington Region", "Wellington State",
				"Wellington", "6011", "123 Main Street", new BigDecimal("1.23"), new BigDecimal("2.34"),
				new BigDecimal("3.45"), "Pacific/Auckland");
		IdentityBasicLocation loc = new IdentityBasicLocation(123L, l);

		// WHEN
		String json = mapper.writeValueAsString(loc);

		// THEN
		assertThat("JSON", json, is(equalTo(
				"{\"id\":123,\"name\":\"Test\",\"country\":\"NZ\",\"region\":\"Wellington Region\""
						+ ",\"stateOrProvince\":\"Wellington State\",\"postalCode\":\"6011\""
						+ ",\"locality\":\"Wellington\",\"street\":\"123 Main Street\""
						+ ",\"lat\":1.23,\"lon\":2.34,\"el\":3.45,\"zone\":\"Pacific/Auckland\"}")));
	}

	@Test
	public void serialize_ident_noId() throws IOException {
		// GIVEN
		BasicLocation l = new BasicLocation("Test", "NZ", "Wellington Region", "Wellington State",
				"Wellington", "6011", "123 Main Street", new BigDecimal("1.23"), new BigDecimal("2.34"),
				new BigDecimal("3.45"), "Pacific/Auckland");
		IdentityBasicLocation loc = new IdentityBasicLocation(null, l);

		// WHEN
		String json = mapper.writeValueAsString(loc);

		// THEN
		assertThat("JSON", json,
				is(equalTo("{\"name\":\"Test\",\"country\":\"NZ\",\"region\":\"Wellington Region\""
						+ ",\"stateOrProvince\":\"Wellington State\",\"postalCode\":\"6011\""
						+ ",\"locality\":\"Wellington\",\"street\":\"123 Main Street\""
						+ ",\"lat\":1.23,\"lon\":2.34,\"el\":3.45,\"zone\":\"Pacific/Auckland\"}")));
	}

}
