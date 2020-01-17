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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
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
}
