/* ==================================================================
 * BigDecimalStringSerializerTests.java - 15/01/2020 10:04:41 am
 * 
 * Copyright 2020 SolarNetwork.net Dev Team
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
import java.math.BigDecimal;
import org.junit.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.module.SimpleModule;
import net.solarnetwork.codec.BigDecimalStringSerializer;

/**
 * Test cases for the {@link BigDecimalStringSerializer} class.
 * 
 * @author matt
 * @version 1.0
 */
public class BigDecimalStringSerializerTests {

	private ObjectMapper createObjectMapper() {
		ObjectMapper m = new ObjectMapper();
		SimpleModule mod = new SimpleModule("Test");
		mod.addSerializer(BigDecimal.class, new BigDecimalStringSerializer());
		m.registerModule(mod);
		return m;
	}

	@Test
	public void serialize() throws Exception {
		final ObjectMapper m = createObjectMapper();
		final String num = "1.23456";
		BigDecimal d = new BigDecimal(num);
		String result = m.writeValueAsString(d);
		assertThat("Serialized BigDecimal as string", result, equalTo(String.format("\"%s\"", num)));
	}

	@Test
	public void serializeWithoutSerializer() throws Exception {
		final ObjectMapper m = new ObjectMapper();
		final String num = "1.23456";
		BigDecimal d = new BigDecimal(num);
		String result = m.writeValueAsString(d);
		assertThat("Serialized BigDecimal as number when not configured", result, equalTo(num));
	}

	public final class TestObj {

		private final BigDecimal d;

		private TestObj(BigDecimal d) {
			super();
			this.d = d;
		}

		@JsonSerialize(using = BigDecimalStringSerializer.class)
		public BigDecimal getD() {
			return d;
		}
	}

	@Test
	public void serializeUsingAnnotation() throws Exception {
		final ObjectMapper m = new ObjectMapper();
		final String num = "1.23456";
		final TestObj o = new TestObj(new BigDecimal(num));
		String result = m.writeValueAsString(o);
		assertThat("Serialized BigDecimal field as string", result,
				equalTo(String.format("{\"d\":\"%s\"}", num)));
	}
}
