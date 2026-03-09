/* ==================================================================
 * KeyCodedValueTests.java - 10/03/2026 8:11:25 am
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
import net.solarnetwork.domain.KeyCodedValue;

/**
 * Test cases for the {@link KeyCodedValue} API.
 *
 * @author matt
 * @version 1.0
 */
public class KeyCodedValueTests {

	private enum TestKeyCodedValue implements KeyCodedValue {

		One('o'),

		Two('t'),

		Threee('T'),

		;

		private final char key;

		private TestKeyCodedValue(char key) {
			this.key = key;
		}

		@Override
		public char getKeyCode() {
			return key;
		}

	}

	@Test
	public void keyedValue() {
		for ( TestKeyCodedValue e : TestKeyCodedValue.values() ) {
			// @formatter:off
			then(KeyCodedValue.forKeyCode(e.getKeyCode(), TestKeyCodedValue.class, null))
				.as("Key value %s decoded", e.getKeyCode())
				.isEqualTo(e)
				;
			// @formatter:on
		}
	}

	@Test
	public void keyedValue_default() {
		// @formatter:off
		then(KeyCodedValue.forKeyCode('0', TestKeyCodedValue.class, TestKeyCodedValue.One))
			.as("Non-existing key value results in given default")
			.isEqualTo(TestKeyCodedValue.One)
			;
		// @formatter:on
	}

}
