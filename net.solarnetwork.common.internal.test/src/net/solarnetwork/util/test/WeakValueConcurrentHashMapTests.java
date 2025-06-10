/* ==================================================================
 * WeakValueConcurrentHashMapTests.java - 10/05/2021 11:08:00 AM
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

package net.solarnetwork.util.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import org.junit.Test;
import net.solarnetwork.util.WeakValueConcurrentHashMap;

/**
 * Test cases for the {@link WeakValueConcurrentHashMap} class.
 * 
 * @author matt
 * @version 1.0
 */
public class WeakValueConcurrentHashMapTests {

	@Test
	public void get_empty() {
		// GIVEN
		ConcurrentMap<String, UUID> m = new WeakValueConcurrentHashMap<>();

		// WHEN
		UUID v = m.get("foo");

		// THEN
		assertThat("No value in empty map", v, nullValue());
	}

	@Test
	public void put() {
		// GIVEN
		String uuid = UUID.randomUUID().toString();
		ConcurrentMap<String, UUID> m = new WeakValueConcurrentHashMap<>();

		// WHEN
		UUID v = m.put("foo", UUID.fromString(uuid));

		// THEN
		assertThat("No existing value in empty map", v, nullValue());
	}

	@Test
	public void put_get() {
		// GIVEN
		UUID id = UUID.randomUUID(); // strong ref, no gc
		ConcurrentMap<String, UUID> m = new WeakValueConcurrentHashMap<>();

		// WHEN
		m.put("foo", id);
		UUID v = m.get("foo");

		// THEN
		assertThat("Existing value found in map", v, sameInstance(id));
	}

	@Test
	public void put_gc_get() {
		// GIVEN
		String uuid = UUID.randomUUID().toString();
		ConcurrentMap<String, UUID> m = new WeakValueConcurrentHashMap<>();

		// WHEN
		m.put("foo", UUID.fromString(uuid));
		System.gc();
		UUID v = m.get("foo");

		// THEN
		assertThat("Weak ref value reclaimed after GC", v, nullValue());
	}

}
