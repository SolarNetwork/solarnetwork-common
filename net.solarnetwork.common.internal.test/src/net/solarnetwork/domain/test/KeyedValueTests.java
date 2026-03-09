/* ==================================================================
 * KeyedValueTests.java - 10/03/2026 8:07:09 am
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

package net.solarnetwork.domain.test;

import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;
import net.solarnetwork.domain.KeyedValue;

/**
 * Test cases for the {@link KeyedValue} API.
 *
 * @author matt
 * @version 1.0
 */
public class KeyedValueTests {

	private enum TestKeyedValue implements KeyedValue {

		One("one"),

		Two("two"),

		Threee("three"),

		;

		private final String key;

		private TestKeyedValue(String key) {
			this.key = key;
		}

		@Override
		public String getKey() {
			return key;
		}

	}

	private record TestKeyed(String key) implements KeyedValue {

		@Override
		public String getKey() {
			return key;
		}

	}

	@Test
	public void enum_keyedValue() {
		for ( TestKeyedValue e : TestKeyedValue.values() ) {
			// @formatter:off
			then(KeyedValue.forKeyValue(e.getKey(), TestKeyedValue.class, null, false, false))
				.as("Key value %s decoded", e.getKey())
				.isEqualTo(e)
				;
			// @formatter:on
		}
	}

	@Test
	public void enum_keyedValue_caseInsensitive() {
		// @formatter:off
		then(KeyedValue.forKeyValue(TestKeyedValue.One.getKey().toUpperCase(), TestKeyedValue.class, null, false, false))
			.as("Upper case key value not decoded in case sensitive mode")
			.isNull()
			;

		then(KeyedValue.forKeyValue(TestKeyedValue.One.getKey().toUpperCase(), TestKeyedValue.class, null, true, false))
			.as("Upper case key value decoded in case insensitive mode")
			.isEqualTo(TestKeyedValue.One)
			;
		// @formatter:on
	}

	@Test
	public void enum_keyedValue_name() {
		// @formatter:off
		then(KeyedValue.forKeyValue(TestKeyedValue.One.name(), TestKeyedValue.class, null, false, false))
			.as("Name value not decoded without name matching")
			.isNull()
			;

		then(KeyedValue.forKeyValue(TestKeyedValue.One.name(), TestKeyedValue.class, null, false, true))
			.as("Name value not decoded without name matching")
			.isEqualTo(TestKeyedValue.One)
			;


		then(KeyedValue.forKeyValue(TestKeyedValue.One.name().toUpperCase(), TestKeyedValue.class, null, false, true))
			.as("Upper case name not decoded in case sensitive mode")
			.isNull()
			;

		then(KeyedValue.forKeyValue(TestKeyedValue.One.name().toUpperCase(), TestKeyedValue.class, null, true, true))
			.as("Upper case name decoded in case insensitive mode")
			.isEqualTo(TestKeyedValue.One)
			;
		// @formatter:on
	}

	@Test
	public void keyedValue() {
		// GIVEN
		// @formatter:off
		final TestKeyed[] values = new TestKeyed[] {
			new TestKeyed("a"),
			new TestKeyed("b"),
			new TestKeyed("c")
		};
		then(KeyedValue.forKeyValue("does not exist", values, null, false))
			.as("Non-existing key value not decoded")
			.isNull()
			;

		then(KeyedValue.forKeyValue("does not exist", values, values[2], false))
			.as("Non-existing key value not decoded and default value returned")
			.isSameAs(values[2])
			;

		then(KeyedValue.forKeyValue(values[0].key(), values, null, false))
			.as("Existing key value decoded")
			.isSameAs(values[0])
			;

		then(KeyedValue.forKeyValue(values[1].key().toUpperCase(), values, null, false))
			.as("Upper case key value not decoded in case sensitive mode")
			.isNull()
			;

		then(KeyedValue.forKeyValue(values[1].key().toUpperCase(), values, null, true))
			.as("Upper case key value decoded in case insensitive mode")
			.isSameAs(values[1])
			;
		// @formatter:on
	}

}
