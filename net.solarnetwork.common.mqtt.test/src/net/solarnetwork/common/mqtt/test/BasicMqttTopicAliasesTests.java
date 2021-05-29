/* ==================================================================
 * BasicMqttTopicAliasesTests.java - 30/05/2021 10:21:27 AM
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

package net.solarnetwork.common.mqtt.test;

import static java.lang.String.format;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Before;
import org.junit.Test;
import net.solarnetwork.common.mqtt.BasicMqttTopicAliases;
import net.solarnetwork.common.mqtt.MqttProperties;

/**
 * Test cases for the {@link BasicMqttTopicAliases} class.
 * 
 * @author matt
 * @version 1.0
 */
public class BasicMqttTopicAliasesTests {

	private ConcurrentMap<String, Integer> topicAliases;
	private ConcurrentMap<Integer, String> aliasedTopics;
	private ConcurrentMap<Integer, MqttProperties> properties;
	private BasicMqttTopicAliases aliases;

	@Before
	public void setup() {
		topicAliases = new ConcurrentHashMap<>(4);
		aliasedTopics = new ConcurrentHashMap<>(4);
		properties = new ConcurrentHashMap<>(4);
		aliases = new BasicMqttTopicAliases(0, topicAliases, aliasedTopics, properties);
	}

	private void assertAliasMapping(String msg, String topic, Integer alias) {
		assertAliasMapping(msg, topic, alias, alias);
	}

	private void assertAliasMapping(String msg, String topic, Integer alias, Integer aliased) {
		assertThat(format("%s topic alias mapping exists", msg), topicAliases, hasEntry(topic, alias));
		assertThat(format("%s aliased topic mapping exists", msg), aliasedTopics,
				hasEntry(aliased, topic));
	}

	private void assertAliasCount(int count) {
		assertThat("Topic alias mapping exists", topicAliases.keySet(), hasSize(count));
		assertThat("Aliased topic mapping exists", aliasedTopics.keySet(), hasSize(count));
	}

	@Test
	public void addAlias_noneAllowed() {
		// GIVEN
		String t = "foo";

		// WHEN
		String topic = aliases.topicAlias(t, a -> {
			fail("Should not provide alias");
		});

		// THEN
		assertThat("Topic not aliased", topic, is(equalTo(t)));
		assertAliasCount(0);
	}

	@Test
	public void addAlias_firstTime() {
		// GIVEN
		aliases.setMaximumAliasCount(1);
		String t = "foo";

		// WHEN
		AtomicInteger alias = new AtomicInteger();
		String topic = aliases.topicAlias(t, a -> {
			alias.set(a);
		});

		// THEN
		assertThat("Topic not aliased", topic, is(equalTo(t)));
		assertThat("Unconfirmed alias provided", alias.get(), is(equalTo(1)));
		assertAliasCount(1);
		assertAliasMapping("Unconfirmed alias", t, -1, 1);
	}

	@Test
	public void addAlias_secondTime_unconfirmed() {
		// GIVEN
		aliases.setMaximumAliasCount(1);
		String t = "foo";
		aliases.topicAlias(t, a -> {
			assertThat("Unconfirmed alias provided", a, is(equalTo(1)));
		});

		// WHEN
		AtomicInteger alias = new AtomicInteger();
		String topic = aliases.topicAlias(t, a -> {
			alias.set(a);
		});

		// THEN
		assertThat("Topic not aliased", topic, is(equalTo(t)));
		assertThat("Unconfirmed alias provided", alias.get(), is(equalTo(1)));
		assertAliasCount(1);
	}

	@Test
	public void addAlias_secondTime_confirmed() {
		// GIVEN
		aliases.setMaximumAliasCount(1);
		String t = "foo";
		aliases.topicAlias(t, a -> {
			assertThat("Unconfirmed alias provided", a, is(equalTo(1)));
		});

		// WHEN
		aliases.confirmTopicAlias(t);
		AtomicInteger alias = new AtomicInteger();
		String topic = aliases.topicAlias(t, a -> {
			alias.set(a);
		});

		// THEN
		assertThat("Topic is aliased", topic, is(equalTo("")));
		assertAliasCount(1);
		assertThat("Alias provided", alias.get(), is(equalTo(1)));
		assertAliasMapping("Confirmed alias", t, 1);
	}

	@Test
	public void addAlias_clear() {
		// GIVEN
		aliases.setMaximumAliasCount(1);
		String t = "foo";
		aliases.topicAlias(t, a -> {
			assertThat("Unconfirmed alias provided", a, is(equalTo(1)));
		});
		aliases.confirmTopicAlias(t);

		// WHEN
		aliases.clear();

		// THEN
		assertAliasCount(0);
		assertThat("Max count unchanged", aliases.getMaximumAliasCount(), is(equalTo(1)));
	}

	@Test
	public void addAlias_setMaximumZeroClears() {
		// GIVEN
		aliases.setMaximumAliasCount(1);
		String t = "foo";
		aliases.topicAlias(t, a -> {
			assertThat("Unconfirmed alias provided", a, is(equalTo(1)));
		});
		aliases.confirmTopicAlias(t);

		// WHEN
		aliases.setMaximumAliasCount(0);

		// THEN
		assertAliasCount(0);
		assertThat("Max count reset to 0", aliases.getMaximumAliasCount(), is(equalTo(0)));
	}
}
