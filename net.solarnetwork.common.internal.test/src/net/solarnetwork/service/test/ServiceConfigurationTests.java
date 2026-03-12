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

import static org.assertj.core.api.BDDAssertions.then;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
	public void servicePropertyNumber() {
		// GIVEN
		final Map<String, Object> props = testProps();

		final String aLargeInteger = "987654321098765432109876543210987654321";
		props.put("large-integer", aLargeInteger);
		final String aLargeDecimal = "987654321098765432109876543210.987654321";
		props.put("large-decimal", aLargeDecimal);

		final BigDecimal aDefault = new BigDecimal("1.234");

		// WHEN
		ServiceConfiguration conf = new BasicIdentifiableConfiguration(null, null, props);

		// THEN
		// @formatter:off
		then(conf.servicePropertyNumber("long", null))
			.as("Long instance returned as 32-bit int")
			.isEqualTo(((Long)props.get("long")).intValue())
			;
		then(conf.servicePropertyNumber("num", null))
			.as("String number parsed to 32-bit Integer")
			.isEqualTo(567)
			;
		then(conf.servicePropertyNumber("empty", aDefault))
			.as("Empty string value returns default value")
			.isSameAs(aDefault)
			;
		then(conf.servicePropertyNumber("foo", null))
			.as("Non-number string returns null default value")
			.isNull()
			;
		then(conf.servicePropertyNumber("foo", aDefault))
			.as("Non-number string returns default value")
			.isSameAs(aDefault)
			;
		then(conf.servicePropertyNumber("key does not exist", aDefault))
			.as("Non-existing key returns default value")
			.isSameAs(aDefault)
			;
		then(conf.servicePropertyNumber("large-integer", null))
			.as("Large integer returned as BigInteger (not narrowed to 32-bit)")
			.isEqualTo(new BigInteger(aLargeInteger))
			;
		then(conf.servicePropertyNumber("large-decimal", null))
			.as("Large decimal returned as BigDecimal (not narrowed to 32-bit)")
			.isEqualTo(new BigDecimal(aLargeDecimal))
			;
		// @formatter:on
	}

	@Test
	public void servicePropertyDuration() {
		// GIVEN
		final Map<String, Object> props = testProps();

		final Duration aDuration = Duration.ofHours(1);
		props.put("duration-inst", aDuration);
		props.put("duration", aDuration.toString());

		final Duration aDefault = Duration.ofSeconds(1);

		// WHEN
		ServiceConfiguration conf = new BasicIdentifiableConfiguration(null, null, props);

		// THEN
		// @formatter:off
		then(conf.servicePropertyDuration("long", null))
			.as("Number instance parsed as seconds")
			.isEqualTo(Duration.ofSeconds((long)props.get("long")))
			;
		then(conf.servicePropertyDuration("num", null))
			.as("String number parsed as seconds")
			.isEqualTo(Duration.ofSeconds(Long.valueOf((String)props.get("num"))))
			;
		then(conf.servicePropertyDuration("empty", aDefault))
			.as("Empty string value returns default value")
			.isSameAs(aDefault)
			;
		then(conf.servicePropertyDuration("foo", aDefault))
			.as("Non-duration non-number string returns default value")
			.isSameAs(aDefault)
			;
		then(conf.servicePropertyDuration("key does not exist", aDefault))
			.as("Non-existing key returns default value")
			.isSameAs(aDefault)
			;
		then(conf.servicePropertyDuration("duration-inst", null))
			.as("Duration instance returned directly")
			.isSameAs(aDuration)
			;
		then(conf.servicePropertyDuration("duration", null))
			.as("Duration string parsed as duration")
			.isEqualTo(aDuration)
			;
		// @formatter:on
	}

	@Test
	public void servicePropertyTimestamp() {
		// GIVEN
		final Map<String, Object> props = testProps();

		final Instant aTimestamp = Instant.now();
		props.put("timestamp-inst", aTimestamp);
		props.put("timestampms", aTimestamp.toEpochMilli());
		props.put("timestamp", aTimestamp.toString());

		final Instant aDefault = aTimestamp.truncatedTo(ChronoUnit.DAYS).minusSeconds(1);

		// WHEN
		ServiceConfiguration conf = new BasicIdentifiableConfiguration(null, null, props);

		// THEN
		// @formatter:off
		then(conf.servicePropertyTimestamp("timestamp-inst", null))
			.as("Timestamp instance returned directly")
			.isSameAs(aTimestamp)
			;
		then(conf.servicePropertyTimestamp("timestampms", null))
			.as("Number instance parsed as millisecond epoch")
			.isEqualTo(aTimestamp.truncatedTo(ChronoUnit.MILLIS))
			;
		then(conf.servicePropertyTimestamp("timestamp", null))
			.as("String parsed as ISO timestamp")
			.isEqualTo(aTimestamp)
			;
		then(conf.servicePropertyTimestamp("empty", aDefault))
			.as("Empty string value returns default value")
			.isSameAs(aDefault)
			;
		then(conf.servicePropertyTimestamp("foo", aDefault))
			.as("Non-timestamp non-number string returns default value")
			.isSameAs(aDefault)
			;
		then(conf.servicePropertyTimestamp("key does not exist", aDefault))
			.as("Non-existing key returns default value")
			.isSameAs(aDefault)
			;
		// @formatter:on
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
