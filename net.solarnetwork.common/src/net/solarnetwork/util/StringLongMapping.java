/* ==================================================================
 * StringLongMap.java - 6/05/2026 11:20:56 am
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

package net.solarnetwork.util;

import java.util.HashMap;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * A simple hash map of strings to longs, not synchronized, with a fixed load
 * factor.
 *
 * <p>
 * Adapted from {@code org.apache.batik.css.engine.StringIntMap}, by Stephane
 * Hillion.
 * </p>
 *
 * @author matt
 * @version 1.0
 * @since 4.36
 */
public class StringLongMapping implements CountTracker, Cloneable {

	/**
	 * The underlying data.
	 */
	private Entry[] table;

	/**
	 * The number of entries.
	 */
	private int count;

	/**
	 * Constructor.
	 *
	 * <p>
	 * A default capacity of {@code 16} will be used.
	 * </p>
	 */
	public StringLongMapping() {
		this(16);
	}

	/**
	 * Constructor.
	 *
	 * @param capacity
	 *        the capacity of the mapping
	 */
	public StringLongMapping(int capacity) {
		// the table is set to 75% of the requested size
		table = new Entry[(capacity - (capacity >> 2)) + 1];
	}

	/**
	 * Constructor.
	 *
	 * @param data
	 *        the initial data to populate in the mapping
	 */
	public StringLongMapping(Map<String, Long> data) {
		this(data.size());
		for ( Map.Entry<String, Long> e : data.entrySet() ) {
			putCount(e.getKey(), e.getValue());
		}
	}

	@Override
	public long getCount(String key, final long notFoundValue) {
		final Entry e = entry(key);
		return (e != null ? e.value : notFoundValue);
	}

	private @Nullable Entry entry(String key) {
		final int hash = key.hashCode() & 0x7FFFFFFF;
		final int index = hash % table.length;

		for ( Entry e = table[index]; e != null; e = e.next ) {
			if ( (e.hash == hash) && e.key.equals(key) ) {
				return e;
			}
		}

		return null;
	}

	@Override
	public void putCount(String key, long value) {
		int hash = key.hashCode() & 0x7FFFFFFF;
		int index = hash % table.length;

		for ( Entry e = table[index]; e != null; e = e.next ) {
			if ( (e.hash == hash) && e.key.equals(key) ) {
				e.value = value;
				return;
			}
		}

		// The key is not in the hash table
		int len = table.length;
		if ( count++ >= (len - (len >> 2)) ) {
			// more than 75% loaded: grow
			rehash();
			index = hash % table.length;
		}

		Entry e = new Entry(hash, key, value, table[index]);
		table[index] = e;
	}

	@Override
	public void addCount(String key, long amount) {
		final Entry e = entry(key);
		if ( e != null ) {
			e.value += amount;
		} else {
			putCount(key, amount);
		}
	}

	/**
	 * Create a {@link Map} from this mapping.
	 *
	 * @return the map
	 */
	@Override
	public Map<String, Long> toMap() {
		var result = new HashMap<String, Long>(count);
		populateMap(result);
		return result;
	}

	/**
	 * Populate a {@link Map} with the values in this mapping.
	 *
	 * @param map
	 *        the map to populate
	 */
	public void populateMap(Map<String, Long> map) {
		for ( Entry e : table ) {
			if ( e == null ) {
				continue;
			}
			map.put(e.key, e.value);
			for ( Entry next = e.next; next != null; next = next.next ) {
				map.put(next.key, next.value);
			}
		}
	}

	/**
	 * Rehash the table
	 */
	private void rehash() {
		Entry[] oldTable = table;

		table = new Entry[oldTable.length * 2 + 1];

		for ( int i = oldTable.length - 1; i >= 0; i-- ) {
			for ( Entry old = oldTable[i]; old != null; ) {
				Entry e = old;
				old = old.next;

				int index = e.hash % table.length;
				e.next = table[index];
				table[index] = e;
			}
		}
	}

	@Override
	public StringLongMapping clone() {
		return new StringLongMapping(toMap());
	}

	/**
	 * To manage collisions
	 */
	private static class Entry {

		/**
		 * The hash code.
		 */
		private final int hash;

		/**
		 * The key.
		 */
		private final String key;

		/**
		 * The value.
		 */
		private long value;

		/**
		 * The next entry - changed, when table[] is reordered.
		 */
		private Entry next;

		/**
		 * Creates a new entry
		 */
		private Entry(int hash, String key, long value, Entry next) {
			this.hash = hash;
			this.key = key;
			this.value = value;
			this.next = next;
		}
	}

}
