/* ==================================================================
 * ServiceConfigurationTests.java - 17/10/2024 8:01:04 am
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import net.solarnetwork.domain.BasicIdentifiableConfiguration;
import net.solarnetwork.service.ServiceConfiguration;

/**
 * Test cases for the {@link ServiceConfiguration} API.
 *
 * @author matt
 * @version 1.1
 */
public class ServiceConfigurationTests {

	private static Map<String, Object> testProps() {
		final Map<String, Object> props = new LinkedHashMap<>(4);
		props.put("foo", "bar");
		props.put("long", 123L);
		props.put("int", 234);
		props.put("float", 34.5f);
		props.put("double", 45.6);
		props.put("num", "567");
		props.put("empty", "");
		return props;
	}

	@Test
	public void serviceProperty_string() {
		// GIVEN
		final Map<String, Object> props = testProps();
		ServiceConfiguration conf = new BasicIdentifiableConfiguration(null, null, props);

		// THEN
		assertThat("Has string property", conf.hasServiceProperty("foo", String.class), is(true));
		assertThat("String returned directly", conf.serviceProperty("foo", String.class),
				is(sameInstance(props.get("foo"))));
		assertThat("Has convertable string property", conf.hasServiceProperty("long", String.class),
				is(true));
		assertThat("Number converted to string", conf.serviceProperty("long", String.class),
				is(equalTo("123")));

		assertThat("Does not have empty untyped string property", conf.hasServiceProperty("empty"),
				is(false));
		assertThat("Does not have empty string typed property",
				conf.hasServiceProperty("empty", String.class), is(false));
		assertThat("Empty string property resolves to null", conf.serviceProperty("empty", String.class),
				is(nullValue()));
	}

	@Test
	public void serviceProperty_number() {
		// GIVEN
		final Map<String, Object> props = testProps();
		ServiceConfiguration conf = new BasicIdentifiableConfiguration(null, null, props);

		// THEN
		assertThat("Has long property", conf.hasServiceProperty("long", Long.class), is(true));
		assertThat("Long returned directly", conf.serviceProperty("long", Long.class),
				is(sameInstance(props.get("long"))));
		assertThat("Has converted long property", conf.hasServiceProperty("num", Long.class), is(true));
		assertThat("String converted to long", conf.serviceProperty("num", Long.class),
				is(equalTo(567L)));
		assertThat("Has converted int to long property", conf.hasServiceProperty("int", Long.class),
				is(true));
		assertThat("Int converted to long", conf.serviceProperty("int", Long.class), is(equalTo(234L)));

		assertThat("Does have untyped property", conf.hasServiceProperty("foo"));
		assertThat("Does not have NaN converted property", conf.hasServiceProperty("foo", Long.class),
				is(false));
		assertThat("NaN string results in null", conf.serviceProperty("foo", Long.class),
				is(nullValue()));
	}

	@Test
	public void serviceProperty_stringMap_map() {
		// GIVEN
		final Map<String, String> map = Collections.singletonMap("the", "map");
		final Map<String, Object> props = testProps();
		props.put("m", map);
		ServiceConfiguration conf = new BasicIdentifiableConfiguration(null, null, props);

		// THEN
		assertThat("Map returned directly", conf.servicePropertyStringMap("m"), is(sameInstance(map)));
		assertThat("Null returned for non-existing property",
				conf.servicePropertyStringMap("does.not.exist"), is(nullValue()));
		assertThat("Empty map returned for not-a-map property", conf.servicePropertyStringMap("long"),
				is(equalTo(Collections.emptyMap())));
	}

	@Test
	public void serviceProperty_stringMap_stringEncoding() {
		// GIVEN
		final Map<String, Object> props = testProps();
		props.put("m", "the=map, see=how");
		ServiceConfiguration conf = new BasicIdentifiableConfiguration(null, null, props);

		// THEN
		Map<String, String> expected = new HashMap<>(2);
		expected.put("the", "map");
		expected.put("see", "how");
		assertThat("Map derived from string", conf.servicePropertyStringMap("m"), is(equalTo(expected)));
	}

	@Test
	public void serviceProperty_stringList_list() {
		// GIVEN
		final List<String> list = Arrays.asList("the", "list");
		final Map<String, Object> props = testProps();
		props.put("l", list);
		ServiceConfiguration conf = new BasicIdentifiableConfiguration(null, null, props);

		// THEN
		assertThat("List returned directly", conf.servicePropertyStringList("l"),
				is(sameInstance(list)));
		assertThat("Null returned for non-existing property",
				conf.servicePropertyStringList("does.not.exist"), is(nullValue()));
		assertThat("List returned for not-a-list property", conf.servicePropertyStringList("long"),
				is(equalTo(Arrays.asList("123"))));
	}

	@Test
	public void serviceProperty_stringList_stringEncoding() {
		// GIVEN
		final Map<String, Object> props = testProps();
		props.put("l", "the, list");
		ServiceConfiguration conf = new BasicIdentifiableConfiguration(null, null, props);

		// THEN
		assertThat("List derived from string", conf.servicePropertyStringList("l"),
				is(equalTo(Arrays.asList("the", "list"))));
	}

}
