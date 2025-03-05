/* ==================================================================
 * IdentifiableConfigurationTests.java - 3/10/2024 6:56:49 am
 *
 * Copyright 2024 SolarNetwork.net Dev Team
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

package net.solarnetwork.service.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;
import net.solarnetwork.domain.BasicIdentifiableConfiguration;
import net.solarnetwork.service.IdentifiableConfiguration;

/**
 * Test cases for the {@link IdentifiableConfiguration} class.
 *
 * @author matt
 * @version 1.0
 */
public class IdentifiableConfigurationTests {

	private static Map<String, Object> testProps() {
		final Map<String, Object> props = new LinkedHashMap<>(4);
		props.put("foo", "bar");
		props.put("long", 123L);
		props.put("int", 234);
		props.put("float", 34.5f);
		props.put("double", 45.6);
		props.put("num", "567");
		return props;
	}

	@Test
	public void serviceProperty_string() {
		// GIVEN
		final Map<String, Object> props = testProps();
		BasicIdentifiableConfiguration conf = new BasicIdentifiableConfiguration();
		conf.setServiceProps(props);

		// THEN
		assertThat("String returned directly", conf.serviceProperty("foo", String.class),
				is(sameInstance(props.get("foo"))));
		assertThat("Number converted to string", conf.serviceProperty("long", String.class),
				is(equalTo("123")));
	}

	@Test
	public void serviceProperty_number() {
		// GIVEN
		final Map<String, Object> props = testProps();
		BasicIdentifiableConfiguration conf = new BasicIdentifiableConfiguration();
		conf.setServiceProps(props);

		// THEN
		assertThat("Long returned directly", conf.serviceProperty("long", Long.class),
				is(sameInstance(props.get("long"))));
		assertThat("String converted to long", conf.serviceProperty("num", Long.class),
				is(equalTo(567L)));
		assertThat("Int converted to long", conf.serviceProperty("int", Long.class), is(equalTo(234L)));
		assertThat("NaN string results in null", conf.serviceProperty("foo", Long.class),
				is(nullValue()));
	}

}
