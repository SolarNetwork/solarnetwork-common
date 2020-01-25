/* ==================================================================
 * ByteListTests.java - 25/01/2020 11:38:22 am
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

import static java.util.Arrays.asList;
import static net.solarnetwork.util.ByteUtils.objectArray;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.Test;
import net.solarnetwork.util.ByteList;

/**
 * Test cases for the {@link ByteList} class.
 * 
 * @author matt
 * @version 1.0
 */
public class ByteListTests {

	@Test
	public void add_one() {
		ByteList s = new ByteList();
		boolean result = s.add((byte) 1);
		assertThat("List changed from mutation", result, equalTo(true));

		Byte[] data = objectArray(s.toArrayValue());
		assertThat("Data added", data, arrayContaining((byte) 1));
	}

	@Test
	public void add_two() {
		ByteList s = new ByteList();
		boolean result = s.add((byte) 1);
		assertThat("List changed from 1st mutation", result, equalTo(true));
		result = s.add((byte) 10);
		assertThat("List changed from 2nd mutation", result, equalTo(true));

		Byte[] data = objectArray(s.toArrayValue());
		assertThat("Data", data, arrayContaining((byte) 1, (byte) 10));
	}

	@Test
	public void addAll_array() {
		ByteList s = new ByteList();
		s.addAll(new byte[] { (byte) 1, (byte) 2, (byte) 3 });
		Byte[] data = objectArray(s.toArrayValue());
		assertThat("Data", data, arrayContaining((byte) 1, (byte) 2, (byte) 3));
	}

	@Test
	public void addAll_array_two() {
		ByteList s = new ByteList();
		s.add((byte) 0);
		s.addAll(new byte[] { (byte) 1, (byte) 2, (byte) 3 });
		Byte[] data = objectArray(s.toArrayValue());
		assertThat("Data", data, arrayContaining((byte) 0, (byte) 1, (byte) 2, (byte) 3));
	}

	@Test
	public void addAll_singleton() {
		ByteList s = new ByteList();
		boolean result = s.addAll(asList((byte) 1));
		assertThat("List changed from mutation", result, equalTo(true));
		Byte[] data = objectArray(s.toArrayValue());
		assertThat("Data", data, arrayContaining((byte) 1));
	}

	@Test
	public void addAll_list() {
		ByteList s = new ByteList();
		boolean result = s.addAll(asList((byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5));
		assertThat("List changed from mutation", result, equalTo(true));
		Byte[] data = objectArray(s.toArrayValue());
		assertThat("Data", data, arrayContaining((byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5));
	}

	@Test
	public void add_expandCapacity() {
		ByteList s = new ByteList(2);
		assertThat("Initial capacity", s.getCapacity(), equalTo(2));
		Byte[] expected = new Byte[100];
		for ( int i = 0; i < 100; i++ ) {
			s.add((byte) i);
			expected[i] = (byte) i;
		}
		assertThat("Final capacity", s.getCapacity(), equalTo(128));
		Byte[] data = objectArray(s.toArrayValue());
		assertThat("Data", data, arrayContaining(expected));

	}

	@Test
	public void size_empty() {
		ByteList s = new ByteList();
		assertThat("Empty size", s, hasSize(0));
	}

	@Test
	public void size_singleton() {
		ByteList s = new ByteList(new byte[] { 1 });
		assertThat("Singleton size", s, hasSize(1));
	}

	@Test
	public void size_list() {
		ByteList s = new ByteList(new byte[] { 1, 2, 3 });
		assertThat("One range size", s, hasSize(3));
	}

	@Test
	public void iterate() {
		ByteList s = new ByteList(new byte[] { 1, 2, 4, 5 });
		Byte[] data = s.stream().toArray(Byte[]::new);
		assertThat("Iterated data", data, arrayContaining((byte) 1, (byte) 2, (byte) 4, (byte) 5));
	}

	@Test(expected = NoSuchElementException.class)
	public void iterate_beyond() {
		ByteList s = new ByteList(new byte[] { 1, 2 });
		Iterator<Byte> itr = s.iterator();
		for ( int i = 0; i < 2; i++ ) {
			itr.next();
		}
		assertThat("No more elements available", itr.hasNext(), equalTo(false));
		itr.next();
	}

	@Test
	public void forEachOrdered() {
		ByteList s = new ByteList(new byte[] { 1, 2, 4, 5 });
		List<Byte> data = new ArrayList<>(4);
		s.forEachOrdered(data::add);
		assertThat("Iterator values", data, contains((byte) 1, (byte) 2, (byte) 4, (byte) 5));
	}

	@Test
	public void forEachOrdered_empty() {
		ByteList s = new ByteList();
		List<Byte> data = new ArrayList<>(4);
		s.forEachOrdered(data::add);
		assertThat("Iterator values", data, hasSize(0));
	}

	@Test
	public void forEachOrdered_range_head() {
		ByteList s = new ByteList(new byte[] { 1, 2, 4, 5 });
		List<Byte> data = new ArrayList<>(4);
		s.forEachOrdered(0, 3, data::add);
		assertThat("Iterator values", data, contains((byte) 1, (byte) 2, (byte) 4));
	}

	@Test
	public void forEachOrdered_range_tail() {
		ByteList s = new ByteList(new byte[] { 1, 2, 4, 5 });
		List<Byte> data = new ArrayList<>(4);
		s.forEachOrdered(1, 4, data::add);
		assertThat("Iterator values", data, contains((byte) 2, (byte) 4, (byte) 5));
	}

	@Test
	public void clear() {
		ByteList s = new ByteList(new byte[] { 1, 2, 4, 5 });
		s.clear();
		assertThat("No data remains", objectArray(s.toArrayValue()), arrayWithSize(0));
	}

	@Test(expected = ArrayIndexOutOfBoundsException.class)
	public void remove_empty() {
		ByteList s = new ByteList();
		s.remove(1);
	}

	@Test
	public void remove_singleton_only() {
		ByteList s = new ByteList(new byte[] { 1 });
		Byte result = s.remove(0);
		assertThat("Removed value", result, equalTo((byte) 1));
		assertThat("Final list empty", objectArray(s.toArrayValue()), arrayWithSize(0));
	}

	@Test
	public void remove_singleton_first() {
		ByteList s = new ByteList(new byte[] { 1, 2, 3 });
		Byte result = s.remove(0);
		assertThat("Removed value", result, equalTo((byte) 1));
		assertThat("Final list", objectArray(s.toArrayValue()), arrayContaining((byte) 2, (byte) 3));
	}

	@Test
	public void remove_singleton_last() {
		ByteList s = new ByteList(new byte[] { 1, 2, 3 });
		Byte result = s.remove(2);
		assertThat("Removed value", result, equalTo((byte) 3));
		assertThat("Final list", objectArray(s.toArrayValue()), arrayContaining((byte) 1, (byte) 2));
	}

	@Test
	public void remove_singleton_middle() {
		ByteList s = new ByteList(new byte[] { 1, 2, 3 });
		Byte result = s.remove(1);
		assertThat("Removed value", result, equalTo((byte) 2));
		assertThat("Final list", objectArray(s.toArrayValue()), arrayContaining((byte) 1, (byte) 3));
	}

	@Test
	public void remove_range_first() {
		ByteList s = new ByteList(new byte[] { 1, 2, 3 });
		s.remove(0, 2);
		assertThat("Final list", objectArray(s.toArrayValue()), arrayContaining((byte) 3));
	}

	@Test
	public void remove_range_last() {
		ByteList s = new ByteList(new byte[] { 1, 2, 3 });
		s.remove(1, 2);
		assertThat("Final list", objectArray(s.toArrayValue()), arrayContaining((byte) 1));
	}

	@Test
	public void remove_range_middle() {
		ByteList s = new ByteList(new byte[] { 1, 2, 3, 4 });
		s.remove(1, 2);
		assertThat("Final list", objectArray(s.toArrayValue()), arrayContaining((byte) 1, (byte) 4));
	}

}
