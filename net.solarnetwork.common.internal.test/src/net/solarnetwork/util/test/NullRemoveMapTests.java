/* ==================================================================
 * NullRemoveMapTests.java - 27/11/2024 3:35:09 pm
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

package net.solarnetwork.util.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;
import net.solarnetwork.util.NullRemoveMap;

/**
 * Test cases for the {@link NullRemoveMap}.
 *
 * @author matt
 * @version 1.0
 */
public class NullRemoveMapTests {

	@Test
	public void populate() {
		// GIVEN
		final Map<String, Object> storage = new LinkedHashMap<>(4);
		final NullRemoveMap<String, Object> map = new NullRemoveMap<>(storage);

		// WHEN
		map.put("a", 1);
		map.put("b", 2);
		map.put("c", 3);

		// THEN
		assertThat("Keys populated on delegate map", storage.keySet(), contains("a", "b", "c"));
		assertThat("Values populated on delegate map", storage.values(), contains(1, 2, 3));

		assertThat("Keys provided via delegate map", map.keySet(), contains("a", "b", "c"));
		assertThat("Values provided delegate map", map.values(), contains(1, 2, 3));
	}

	@Test
	public void putNull() {
		// GIVEN
		final Map<String, Object> storage = new LinkedHashMap<>(4);
		storage.put("a", 1);
		storage.put("b", 2);
		storage.put("c", 3);
		final NullRemoveMap<String, Object> map = new NullRemoveMap<>(storage);

		// WHEN
		map.put("b", null);

		// THEN
		assertThat("Key removed from delegate map", storage.keySet(), contains("a", "c"));
		assertThat("Key removed from delegate map", storage.values(), contains(1, 3));

		assertThat("Keys provided via delegate map", map.keySet(), contains("a", "c"));
		assertThat("Values provided delegate map", map.values(), contains(1, 3));
	}

}
