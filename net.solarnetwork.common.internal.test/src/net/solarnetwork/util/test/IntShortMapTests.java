/* ==================================================================
 * IntShortMapTests.java - 18/01/2020 8:41:45 am
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

package net.solarnetwork.util.test;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import org.junit.Test;
import net.solarnetwork.util.IntShortMap;

/**
 * Test cases for the {@link IntShortMap} class.
 * 
 * @author matt
 * @version 1.0
 */
public class IntShortMapTests {

	@Test
	public void construct() {
		IntShortMap m = new IntShortMap();
		assertThat("Capacity is initial", m.getCapacity(),
				equalTo(IntShortMap.DEFAULT_INITIAL_CAPACITY));
		m = new IntShortMap(64);
		assertThat("Capacity is initial", m.getCapacity(), equalTo(64));
	}

	private void assertRange(IntShortMap m, int from, int to) {
		int[] keys = m.keySet().stream().mapToInt(Integer::intValue).toArray();
		for ( int i = from, w = 0; i < to; i++, w++ ) {
			assertThat("Get value " + i, m.get(i), equalTo((short) i));
			assertThat("Key " + i, keys[w], equalTo(i));
		}
	}

	@Test
	public void put_initial() {
		IntShortMap m = new IntShortMap();
		Short prev = m.putValue(1, 2);
		assertThat("No previous value", prev, nullValue());
		assertThat("Size updated", m.size(), equalTo(1));
		assertThat("Get value", m.get(1), equalTo((short) 2));
	}

	@Test
	public void put_tail() {
		IntShortMap m = new IntShortMap();
		m.putValue(1, 1);
		Short prev = m.putValue(2, 2);
		assertThat("No previous value", prev, nullValue());
		assertThat("Size updated", m.size(), equalTo(2));
		assertRange(m, 1, 2);
	}

	@Test
	public void put_tail_pastCapacity() {
		IntShortMap m = new IntShortMap(2);
		m.putValue(1, 1);
		m.putValue(2, 2);
		Short prev = m.putValue(3, 3);
		assertThat("No previous value", prev, nullValue());
		assertThat("Size updated", m.size(), equalTo(3));
		assertThat("Capacity is expanded", m.getCapacity(), greaterThan(2));
		assertRange(m, 1, 3);
	}

	@Test
	public void put_replace() {
		IntShortMap m = new IntShortMap(2);
		m.putValue(1, 1);
		Short prev = m.putValue(1, 2);
		assertThat("Previous value", prev, equalTo((short) 1));
		assertThat("Size same", m.size(), equalTo(1));
		assertThat("Get value", m.get(1), equalTo((short) 2));
	}

	@Test
	public void put_head() {
		IntShortMap m = new IntShortMap();
		m.putValue(2, 2);
		Short prev = m.putValue(1, 1);
		assertThat("No previous value", prev, nullValue());
		assertThat("Size updated", m.size(), equalTo(2));
		assertRange(m, 1, 2);
	}

	@Test
	public void put_head_pastCapcity() {
		IntShortMap m = new IntShortMap(2);
		m.putValue(3, 3);
		m.putValue(2, 2);
		Short prev = m.putValue(1, 1);
		assertThat("No previous value", prev, nullValue());
		assertThat("Size updated", m.size(), equalTo(3));
		assertThat("Capacity is expanded", m.getCapacity(), greaterThan(2));
		assertRange(m, 1, 3);
	}

	@Test
	public void put_mid() {
		IntShortMap m = new IntShortMap(4);
		m.putValue(1, 1);
		m.putValue(3, 3);
		Short prev = m.putValue(2, 2);
		assertThat("No previous value", prev, nullValue());
		assertThat("Size updated", m.size(), equalTo(3));
		assertRange(m, 1, 3);
	}

	@Test
	public void put_mid2() {
		IntShortMap m = new IntShortMap(4);
		m.putValue(1, 1);
		m.putValue(3, 3);
		m.putValue(4, 4);
		Short prev = m.putValue(2, 2);
		assertThat("No previous value", prev, nullValue());
		assertThat("Size updated", m.size(), equalTo(4));
		assertRange(m, 1, 4);
	}

	@Test
	public void keySet_ordered() {
		IntShortMap m = new IntShortMap();
		m.putValue(1, 1);
		m.putValue(9, 9);
		m.putValue(5, 5);
		m.putValue(-8, -8);
		assertThat("Keys maintain order", m.keySet(), contains(-8, 1, 5, 9));
	}

	@Test
	public void clear_empty() {
		IntShortMap m = new IntShortMap();
		m.clear();
		assertThat("Size empty", m.size(), equalTo(0));
	}

	@Test
	public void clear() {
		IntShortMap m = new IntShortMap();
		for ( int i = 0; i < 8; i++ ) {
			m.putValue(i, i);
		}
		assertThat("Size before clear", m.size(), equalTo(8));
		assertThat("Keys", m.keySet(), contains(0, 1, 2, 3, 4, 5, 6, 7));
		m.clear();
		assertThat("Size after clear", m.size(), equalTo(0));
		assertThat("Keys", m.keySet(), hasSize(0));
	}

	@Test
	public void entrySet_empty() {
		IntShortMap m = new IntShortMap();
		Set<Map.Entry<Integer, Short>> s = m.entrySet();
		assertThat("Empty entry set", s, hasSize(0));
	}

	@Test
	public void entrySet() {
		IntShortMap m = new IntShortMap();
		m.putValue(1, 2);
		m.putValue(2, 3);
		m.putValue(3, 4);
		m.putValue(4, 5);
		Set<Map.Entry<Integer, Short>> s = m.entrySet();
		Iterator<Map.Entry<Integer, Short>> itr = s.iterator();
		for ( int i = 1; i < 5; i++ ) {
			Map.Entry<Integer, Short> e = itr.next();
			assertThat("Entry key", e.getKey(), equalTo(i));
			assertThat("Entry value", e.getValue(), equalTo((short) (i + 1)));
		}
		assertThat("No more entries", itr.hasNext(), equalTo(false));
	}

	@Test
	public void entrySet_iterator_remove_first() {
		IntShortMap m = new IntShortMap(4);
		m.putValue(1, 2);
		m.putValue(2, 3);
		m.putValue(3, 4);
		Set<Map.Entry<Integer, Short>> s = m.entrySet();
		Iterator<Map.Entry<Integer, Short>> itr = s.iterator();
		for ( int i = 0; i < 3; i++ ) {
			itr.next();
			if ( i == 0 ) {
				itr.remove();
			}
		}
		assertThat("No more entries", itr.hasNext(), equalTo(false));
		assertThat("Size decreased", m.size(), equalTo(2));
		assertThat("Map entry removed", m, allOf(hasEntry(2, (short) 3), hasEntry(3, (short) 4)));
	}

	@Test
	public void entrySet_iterator_remove_mid() {
		IntShortMap m = new IntShortMap(4);
		m.putValue(1, 2);
		m.putValue(2, 3);
		m.putValue(3, 4);
		Set<Map.Entry<Integer, Short>> s = m.entrySet();
		Iterator<Map.Entry<Integer, Short>> itr = s.iterator();
		for ( int i = 0; i < 3; i++ ) {
			itr.next();
			if ( i == 1 ) {
				itr.remove();
			}
		}
		assertThat("No more entries", itr.hasNext(), equalTo(false));
		assertThat("Size decreased", m.size(), equalTo(2));
		assertThat("Map entry removed", m, allOf(hasEntry(1, (short) 2), hasEntry(3, (short) 4)));
	}

	@Test
	public void entrySet_iterator_remove_last() {
		IntShortMap m = new IntShortMap(4);
		m.putValue(1, 2);
		m.putValue(2, 3);
		m.putValue(3, 4);
		Set<Map.Entry<Integer, Short>> s = m.entrySet();
		Iterator<Map.Entry<Integer, Short>> itr = s.iterator();
		for ( int i = 0; i < 3; i++ ) {
			itr.next();
			if ( i == 2 ) {
				itr.remove();
			}
		}
		assertThat("No more entries", itr.hasNext(), equalTo(false));
		assertThat("Size decreased", m.size(), equalTo(2));
		assertThat("Map entry removed", m, allOf(hasEntry(1, (short) 2), hasEntry(2, (short) 3)));
	}

	@Test
	public void entrySet_iterator_remove_multi() {
		IntShortMap m = new IntShortMap(4);
		m.putValue(1, 2);
		m.putValue(2, 3);
		m.putValue(3, 4);
		m.putValue(4, 5);
		Set<Map.Entry<Integer, Short>> s = m.entrySet();
		Iterator<Map.Entry<Integer, Short>> itr = s.iterator();
		for ( int i = 0; i < 4; i++ ) {
			itr.next();
			if ( i >= 1 && i <= 2 ) {
				itr.remove();
			}
		}
		assertThat("No more entries", itr.hasNext(), equalTo(false));
		assertThat("Size decreased", m.size(), equalTo(2));
		assertThat("Map entry removed", m, allOf(hasEntry(1, (short) 2), hasEntry(4, (short) 5)));
	}

	@Test
	public void entrySet_iterator_remove_all() {
		IntShortMap m = new IntShortMap(4);
		m.putValue(1, 2);
		m.putValue(2, 3);
		m.putValue(3, 4);
		Set<Map.Entry<Integer, Short>> s = m.entrySet();
		Iterator<Map.Entry<Integer, Short>> itr = s.iterator();
		for ( int i = 0; i < 3; i++ ) {
			itr.next();
			itr.remove();
		}
		assertThat("No more entries", itr.hasNext(), equalTo(false));
		assertThat("Size decreased", m.size(), equalTo(0));
	}

	@Test
	public void containsKey() {
		IntShortMap m = new IntShortMap(4);
		m.putValue(1, 2);
		m.putValue(2, 3);
		m.putValue(3, 4);
		for ( int i = 1; i <= 3; i++ ) {
			assertThat("Contains key " + i, m.containsKey(i), equalTo(true));
		}
		assertThat("Missing key", m.containsKey(0), equalTo(false));
		assertThat("Missing key", m.containsKey(9), equalTo(false));
	}

	@Test
	public void containsValue() {
		IntShortMap m = new IntShortMap(4);
		m.putValue(1, 2);
		m.putValue(2, 3);
		m.putValue(3, 4);
		for ( int i = 2; i <= 4; i++ ) {
			assertThat("Contains value " + i, m.containsValue((short) i), equalTo(true));
		}
		assertThat("Missing key", m.containsValue((short) 0), equalTo(false));
		assertThat("Missing key", m.containsValue((short) 9), equalTo(false));
	}

	@Test
	public void forEachOrdered_empty() {
		IntShortMap m = new IntShortMap();
		m.forEachOrdered((k, v) -> {
			throw new RuntimeException("Should not be here.");
		});
	}

	@Test
	public void forEachOrdered() {
		IntShortMap m = new IntShortMap();
		m.putValue(1, 2);
		m.putValue(7, 8);
		m.putValue(3, 4);
		List<Integer> keys = new ArrayList<Integer>(3);
		List<Short> vals = new ArrayList<>(3);
		m.forEachOrdered((k, v) -> {
			keys.add(k);
			vals.add(v);
		});
		assertThat("Consumed keys", keys, contains(1, 3, 7));
		assertThat("Consumed values", vals, contains((short) 2, (short) 4, (short) 8));
	}

	@Test
	public void getValue_found() {
		IntShortMap m = new IntShortMap();
		m.putValue(1, 2);
		assertThat("Found value", m.getValue(1), equalTo((short) 2));
	}

	@Test(expected = NoSuchElementException.class)
	public void getValue_notfound() {
		IntShortMap m = new IntShortMap();
		m.putValue(1, 2);
		m.getValue(2);
	}

	@Test
	public void clone_empty() {
		IntShortMap m1 = new IntShortMap(8);
		IntShortMap m2 = (IntShortMap) m1.clone();
		assertThat("New instance created", m2, not(sameInstance(m1)));
		assertThat("New instance capacity same", m2.getCapacity(), equalTo(m1.getCapacity()));
		assertThat("New instance size same", m2.size(), equalTo(m1.size()));
	}

	@Test
	public void clone_contents() {
		IntShortMap m1 = new IntShortMap(8);
		m1.putValue(1, 1);
		m1.putValue(2, 2);
		m1.putValue(9, 9);
		IntShortMap m2 = (IntShortMap) m1.clone();
		assertThat("New instance created", m2, not(sameInstance(m1)));
		assertThat("New instance capacity compacted", m2.getCapacity(), equalTo(3));
		assertThat("New instance size same", m2.size(), equalTo(m1.size()));
		assertThat("Contents same", m2.entrySet(), equalTo(m1.entrySet()));
	}

	@Test
	public void clone_mutation() {
		IntShortMap m1 = new IntShortMap(8);
		m1.putValue(1, 1);
		m1.putValue(2, 2);
		m1.putValue(3, 3);
		IntShortMap m2 = (IntShortMap) m1.clone();
		m2.putValue(1, 99);
		m2.putValue(4, 100);
		assertThat("New instance created", m2, not(sameInstance(m1)));
		assertThat("New instance size", m2.size(), equalTo(m1.size() + 1));
		assertRange(m1, 1, 3);
		assertThat("Clone value mutated", m2.get(1), equalTo((short) 99));
		assertThat("Clone value unchanged", m2.get(2), equalTo((short) 2));
		assertThat("Clone value unchanged", m2.get(3), equalTo((short) 3));
		assertThat("Clone value added", m2.get(4), equalTo((short) 100));
	}
}
