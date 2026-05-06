/* ==================================================================
 * StringLongMappingTests.java - 6/05/2026 11:46:14 am
 *
 * Copyright 2026 SolarNetwork.net Dev Team
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

package net.solarnetwork.util.test;

import static net.solarnetwork.test.CommonTestUtils.randomInt;
import static net.solarnetwork.test.CommonTestUtils.randomLong;
import static net.solarnetwork.test.CommonTestUtils.randomString;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenNullPointerException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.Test;
import net.solarnetwork.test.CommonTestUtils;
import net.solarnetwork.util.StringLongMapping;

/**
 * Test cases for the {@link StringLongMapping} class.
 *
 * @author matt
 * @version 1.0
 */
public class StringLongMappingTests {

	@Test
	public void get_empty() {
		// GIVEN
		final String key = randomString();

		// WHEN
		final var mapping = new StringLongMapping(2);

		// THEN
		// @formatter:off
		then(mapping.getCount(key))
			.as("Empty map returns default 0")
			.isEqualTo(0L)
			;

		then(mapping.getCount(key, Long.MIN_VALUE))
			.as("Empty map returns given default")
			.isEqualTo(Long.MIN_VALUE)
			;

		then(mapping.toMap())
			.as("Empty map generated from empty mapping")
			.isEmpty();;
			;
		// @formatter:on
	}

	@Test
	public void put_get() {
		// GIVEN
		final String key = randomString();
		final long count = randomLong();

		// WHEN
		final var mapping = new StringLongMapping(2);
		mapping.putCount(key, count);

		// THEN
		// @formatter:off
		then(mapping.getCount(key))
			.as("Get returns value previously put")
			.isEqualTo(count)
			;

		then(mapping.getCount(""))
			.as("Non-existent key returns default value")
			.isEqualTo(0L)
			;

		then(mapping.toMap())
			.as("Map generated from entries")
			.containsExactlyInAnyOrderEntriesOf(Map.of(key, count));
			;
		// @formatter:on
	}

	@Test
	public void put_null() {
		// WHEN
		final var mapping = new StringLongMapping(2);

		thenNullPointerException().isThrownBy(() -> {
			mapping.putCount(null, 1L);
		});
	}

	@Test
	public void get_null() {
		// WHEN
		final var mapping = new StringLongMapping(2);

		thenNullPointerException().isThrownBy(() -> {
			mapping.getCount(null);
		});
	}

	@Test
	public void putMany_toMap() {
		// GIVEN
		final int count = CommonTestUtils.RNG.nextInt(40) + 10;
		final Map<String, Long> data = new HashMap<>(count);
		for ( int i = 0; i < count; i++ ) {
			data.put(randomString(), randomLong());
		}

		// WHEN
		final var mapping = new StringLongMapping(count);
		for ( Entry<String, Long> e : data.entrySet() ) {
			mapping.putCount(e.getKey(), e.getValue());
		}

		// THEN
		// @formatter:off
		for ( Entry<String, Long> e : data.entrySet() ) {
			then(mapping.getCount(e.getKey(), Long.MIN_VALUE))
				.as("Get for key returns expected value")
				.isEqualTo(data.get(e.getKey()))
				;
		}

		then(mapping.toMap())
			.as("Map generated from entries")
			.containsExactlyInAnyOrderEntriesOf(data);
			;
		// @formatter:on
	}

	@Test
	public void constructFromData_toMap() {
		// GIVEN
		final int count = CommonTestUtils.RNG.nextInt(40) + 10;
		final Map<String, Long> data = new HashMap<>(count);
		for ( int i = 0; i < count; i++ ) {
			data.put(randomString(), randomLong());
		}

		// WHEN
		final var mapping = new StringLongMapping(data);

		// THEN
		// @formatter:off
		for ( Entry<String, Long> e : data.entrySet() ) {
			then(mapping.getCount(e.getKey(), Long.MIN_VALUE))
				.as("Get for key returns expected value")
				.isEqualTo(data.get(e.getKey()))
				;
		}

		then(mapping.toMap())
			.as("Map generated from entries")
			.containsExactlyInAnyOrderEntriesOf(data);
			;
		// @formatter:on
	}

	@Test
	public void add() {
		// GIVEN
		final String key = randomString();
		final var mapping = new StringLongMapping(Map.of(key, 1L));

		// WHEN
		final long increment = randomInt();
		mapping.addCount(key, increment);

		// THEN
		// @formatter:off
		then(mapping.getCount(key))
			.as("Get returns value previously put + increment")
			.isEqualTo(increment + 1L)
			;
		// @formatter:on
	}

	@Test
	public void add_notKnown() {
		// GIVEN
		final String key = randomString();
		final var mapping = new StringLongMapping(1);

		// WHEN
		final long increment = randomInt();
		mapping.addCount(key, increment);

		// THEN
		// @formatter:off
		then(mapping.getCount(key))
			.as("Get returns 0 + increment")
			.isEqualTo(increment)
			;
		// @formatter:on
	}

	@Test
	public void increment() {
		// GIVEN
		final String key = randomString();
		final var mapping = new StringLongMapping(Map.of(key, 1L));

		// WHEN
		mapping.incrementCount(key);

		// THEN
		// @formatter:off
		then(mapping.getCount(key))
			.as("Get returns value previously put + 1")
			.isEqualTo(2L)
			;
		// @formatter:on
	}

	@Test
	public void increment_notKnown() {
		// GIVEN
		final String key = randomString();
		final var mapping = new StringLongMapping(1);

		// WHEN
		mapping.incrementCount(key);

		// THEN
		// @formatter:off
		then(mapping.getCount(key))
			.as("Get returns 0 + 1")
			.isEqualTo(1L)
			;
		// @formatter:on
	}

}
