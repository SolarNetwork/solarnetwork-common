/* ==================================================================
 * IntShortMap.java - 17/01/2020 1:16:38 pm
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

package net.solarnetwork.util;

import static java.util.Arrays.binarySearch;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

/**
 * A map implementation optimized for sparse array like storage of integer keys
 * with associated short values.
 * 
 * <p>
 * This implementation is optimized for small sizes and appending keys in
 * ascending order. Random mutations trigger array copies that can slow
 * performance down considerably. Accessing values run in {@code O(log n)} time.
 * Keys are maintained in ascending order, so iteration occurs also in ascending
 * order.
 * </p>
 * 
 * @author matt
 * @version 1.0
 * @since 1.58
 */
public class IntShortMap extends AbstractMap<Integer, Short> implements Map<Integer, Short>, Cloneable {

	/** The default initial capacity. */
	public static final int DEFAULT_INITIAL_CAPACITY = 16;

	/**
	 * The default value that causes {@code NoSuchElementException} to be thrown
	 * in {@Link #getValue(int)}.
	 */
	public static final short VALUE_NO_SUCH_ELEMENT = Short.MIN_VALUE;

	private final int initialCapacity;
	private final short notFoundValue;
	private int[] keys;
	private short[] values;
	private int size;

	/**
	 * Default constructor.
	 * 
	 * <p>
	 * Defaults to returning {@literal 0} for nonexistent keys in
	 * {@link #getValue(int)}.
	 * </p>
	 */
	public IntShortMap() {
		this(DEFAULT_INITIAL_CAPACITY, (short) 0);
	}

	/**
	 * Constructor.
	 * 
	 * <p>
	 * Defaults to returning {@literal 0} for nonexistent keys in
	 * {@link #getValue(int)}.
	 * </p>
	 * 
	 * @param initialCapacity
	 *        the initial capacity
	 * @throws IllegalArgumentException
	 *         if {@code initialCapacity} is less than {@literal 1}
	 */
	public IntShortMap(int initialCapacity) {
		this(initialCapacity, (short) 0);
	}

	/**
	 * Constructor.
	 * 
	 * @param initialCapacity
	 *        the initial capacity
	 * @param notFoundValue
	 *        the value to return in {@link #getValue(int)} if a key is not
	 *        found, or {@link #VALUE_NO_SUCH_ELEMENT} to throw a
	 *        {@link NoSuchElementException}
	 * @throws IllegalArgumentException
	 *         if {@code initialCapacity} is less than {@literal 1}
	 */
	public IntShortMap(int initialCapacity, short notFoundValue) {
		super();
		if ( initialCapacity < 1 ) {
			throw new IllegalArgumentException("The initial capacity must be 1 or more.");
		}
		this.initialCapacity = initialCapacity;
		this.notFoundValue = notFoundValue;
		this.keys = new int[initialCapacity];
		this.values = new short[initialCapacity];
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder("{");
		final int len = size;
		for ( int i = 0; i < len; i++ ) {
			if ( i > 0 ) {
				buf.append(", ");
			}
			buf.append(keys[i]).append("=").append(values[i]);
		}
		buf.append("}");
		return buf.toString();
	}

	@Override
	public Object clone() {
		IntShortMap m = new IntShortMap(this.size > 0 ? this.size : this.getCapacity(),
				this.notFoundValue);
		System.arraycopy(keys, 0, m.keys, 0, size);
		System.arraycopy(values, 0, m.values, 0, size);
		m.size = this.size;
		return m;
	}

	@Override
	public Set<Entry<Integer, Short>> entrySet() {
		return new EntrySet();
	}

	@Override
	public int size() {
		return size;
	}

	/**
	 * Iterate over all key/value pairs in this map.
	 * 
	 * <p>
	 * This method of iteration can be more efficient than iterating via
	 * {@link #entrySet()} because no intermediate {@code Set},
	 * {@code Iterator}, or {@code Entry} objects are created, and not primitive
	 * boxing occurs.
	 * </p>
	 * 
	 * @param action
	 *        the consumer to handle the key/value pairs
	 */
	public void forEachOrdered(IntShortBiConsumer action) {
		Objects.requireNonNull(action);
		for ( int i = 0; i < size; i++ ) {
			action.accept(keys[i], values[i]);
		}
	}

	@Override
	public Set<Integer> keySet() {
		return new KeySet();
	}

	@Override
	public Collection<Short> values() {
		final int len = size;
		List<Short> l = new ArrayList<>(len);
		for ( int i = 0; i < len; i++ ) {
			l.add(values[i]);
		}
		return l;
	}

	@Override
	public boolean containsValue(Object value) {
		final short v = (Short) value;
		for ( int i = 0; i < size; i++ ) {
			if ( v == values[i] ) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean containsKey(Object key) {
		return containsKey(((Integer) key).intValue());
	}

	/**
	 * Test if a primitive key value exists in this map.
	 * 
	 * @param k
	 *        the key to test
	 * @return {@literal true} if the key exists in this map
	 */
	public boolean containsKey(final int k) {
		final int idx = Arrays.binarySearch(keys, 0, size, k);
		return idx >= 0;
	}

	@Override
	public void clear() {
		size = 0;
	}

	/**
	 * Get the current capacity.
	 * 
	 * @return the capacity
	 */
	public int getCapacity() {
		return keys.length;
	}

	/**
	 * Free up excess capacity.
	 * 
	 * @return {@literal true} if any capacity was freed
	 */
	public boolean compact() {
		if ( keys.length < initialCapacity || size == keys.length ) {
			return false;
		}
		int[] newKeys = new int[size];
		short[] newValues = new short[size];
		System.arraycopy(keys, 0, newKeys, 0, size);
		System.arraycopy(values, 0, newValues, 0, size);
		this.keys = newKeys;
		this.values = newValues;
		return true;
	}

	@Override
	public Short get(Object key) {
		final int k = (Integer) key;
		return get(k);
	}

	/**
	 * Get the value for a given key.
	 * 
	 * @param k
	 *        the key of the value to get
	 * @return the associated value, or {@literal null} if {@code k} is not
	 *         present
	 */
	public Short get(final int k) {
		final int idx = binarySearch(keys, 0, size, k);
		if ( idx >= 0 ) {
			return values[idx];
		}
		return null;
	}

	/**
	 * Get the value for a given key.
	 * 
	 * @param k
	 *        the key of the value to get
	 * @return the associated value
	 * @throws NoSuchEl
	 */
	public short getValue(final int k) {
		final int idx = binarySearch(keys, 0, size, k);
		if ( idx >= 0 ) {
			return values[idx];
		}
		if ( notFoundValue == VALUE_NO_SUCH_ELEMENT ) {
			throw new NoSuchElementException();
		}
		return notFoundValue;
	}

	/**
	 * Get primitive values.
	 * 
	 * @param k
	 *        the key
	 * @param value
	 *        the value, which will be down-cast to a short
	 * @return the previous value associated with {@code k}, or {@literal null}
	 *         if none
	 */
	public Short putValue(final int k, final int value) {
		return putValue(k, (short) value);
	}

	/**
	 * Put primitive values.
	 * 
	 * @param k
	 *        the key
	 * @param value
	 *        the value
	 * @return the previous value associated with {@code k}, or {@literal null}
	 *         if none
	 */
	public Short putValue(final int k, final short value) {
		// find position to insert key at; if larger than highest key, we can insert at end
		final int idx = (size == 0 || k > keys[size - 1] ? -size - 1 : binarySearch(keys, 0, size, k));

		Short prev = null;
		if ( idx >= 0 && size > 0 ) {
			// key already present, so replace value
			prev = values[idx];
			values[idx] = value;
		} else {
			// key not present; insert, expanding capacity if necessary
			final int p = -(idx + 1);
			if ( size >= keys.length ) {
				// expand capacity by 50%
				expandCapacity();
			}
			if ( p < size ) {
				// have to insert into middle of array, so shift higher slots right
				System.arraycopy(keys, p, keys, p + 1, (size - p));
				System.arraycopy(values, p, values, p + 1, (size - p));
			}
			keys[p] = k;
			values[p] = value;
			size++;
		}
		return prev;
	}

	@Override
	public Short put(Integer key, Short value) {
		return putValue(key, value);
	}

	/**
	 * Get a view of this map with unsigned integer values.
	 * 
	 * @return a new map, backed by this map's data, where the values are
	 *         returned as unsigned integers
	 */
	public Map<Integer, Integer> unsignedMap() {
		final Set<Entry<Integer, Integer>> entrySet = new UnsignedIntegerEntrySet();
		return new AbstractMap<Integer, Integer>() {

			@Override
			public boolean containsKey(Object key) {
				return IntShortMap.this.containsKey(key);
			}

			@Override
			public Integer get(Object key) {
				Integer result = null;
				Short v = IntShortMap.this.get(key);
				if ( v != null ) {
					result = Short.toUnsignedInt(v);
				}
				return result;
			}

			@Override
			public Set<Entry<Integer, Integer>> entrySet() {
				return entrySet;
			}

			@Override
			public Integer put(Integer key, Integer value) {
				Short v = value.shortValue();
				Short prev = IntShortMap.this.put(key, v);
				return (prev != null ? Short.toUnsignedInt(prev) : null);
			}

		};
	}

	private void expandCapacity() {
		final int oldLen = keys.length;
		final int newLen = oldLen + oldLen / 2 + 1;
		int[] newKeys = new int[newLen];
		System.arraycopy(keys, 0, newKeys, 0, oldLen);
		short[] newValues = new short[newLen];
		System.arraycopy(values, 0, newValues, 0, oldLen);
		this.keys = newKeys;
		this.values = newValues;
	}

	private void removeKeyAtIndex(int idx) {
		if ( idx < 0 || idx >= size ) {
			throw new IndexOutOfBoundsException();
		}
		size--;
		System.arraycopy(keys, idx + 1, keys, idx, (size - idx));
		System.arraycopy(values, idx + 1, values, idx, (size - idx));
	}

	private final class KeySet extends AbstractSet<Integer> {

		@Override
		public Iterator<Integer> iterator() {
			return new KeyIterator();
		}

		@Override
		public int size() {
			return size;
		}

	}

	private final class KeyIterator implements Iterator<Integer> {

		private int idx;

		private KeyIterator() {
			super();
			this.idx = 0;
		}

		@Override
		public boolean hasNext() {
			return idx < size;
		}

		@Override
		public Integer next() {
			if ( idx >= size ) {
				throw new NoSuchElementException();
			}
			return keys[idx++];
		}

		@Override
		public void remove() {
			removeKeyAtIndex(--idx);
		}

	}

	private final class EntrySet extends AbstractSet<Entry<Integer, Short>> {

		@Override
		public Iterator<Entry<Integer, Short>> iterator() {
			return new EntryIterator();
		}

		@Override
		public int size() {
			return size;
		}

		@Override
		public boolean contains(Object o) {
			if ( !(o instanceof Entry) ) {
				return false;
			}
			@SuppressWarnings("unchecked")
			Entry<Integer, Short> e = (Entry<Integer, Short>) o;
			Short v = IntShortMap.this.get(e.getKey());
			return Objects.equals(e.getValue(), v);
		}

		@Override
		public void clear() {
			IntShortMap.this.clear();
		}

		@Override
		public boolean isEmpty() {
			return IntShortMap.this.isEmpty();
		}

		@Override
		public boolean remove(Object o) {
			if ( !(o instanceof Entry) ) {
				return false;
			}
			@SuppressWarnings("unchecked")
			Entry<Integer, Short> e = (Entry<Integer, Short>) o;
			Short v = IntShortMap.this.get(e.getKey());
			if ( Objects.equals(e.getValue(), v) ) {
				return IntShortMap.this.remove(e.getKey()) != null;
			}
			return false;
		}

		private final class EntryIterator implements Iterator<Entry<Integer, Short>> {

			private int idx;

			private EntryIterator() {
				super();
				this.idx = 0;
			}

			@Override
			public boolean hasNext() {
				return idx < size;
			}

			@Override
			public Entry<Integer, Short> next() {
				final int i = idx++;
				return new SimpleImmutableEntry<>(keys[i], values[i]);
			}

			@Override
			public void remove() {
				removeKeyAtIndex(--idx);
			}

		}

	}

	private final class UnsignedIntegerEntrySet extends AbstractSet<Entry<Integer, Integer>> {

		@Override
		public Iterator<Entry<Integer, Integer>> iterator() {
			return new UnsignedEntryIterator();
		}

		@Override
		public int size() {
			return size;
		}

		@Override
		public boolean contains(Object o) {
			if ( !(o instanceof Entry) ) {
				return false;
			}
			@SuppressWarnings("unchecked")
			Entry<Integer, Integer> e = (Entry<Integer, Integer>) o;
			Short v = IntShortMap.this.get(e.getKey());
			return Objects.equals(e.getValue().shortValue(), v);
		}

		@Override
		public void clear() {
			IntShortMap.this.clear();
		}

		@Override
		public boolean isEmpty() {
			return IntShortMap.this.isEmpty();
		}

		@Override
		public boolean remove(Object o) {
			if ( !(o instanceof Entry) ) {
				return false;
			}
			@SuppressWarnings("unchecked")
			Entry<Integer, Integer> e = (Entry<Integer, Integer>) o;
			Short v = IntShortMap.this.get(e.getKey());
			if ( Objects.equals(e.getValue().shortValue(), v) ) {
				return IntShortMap.this.remove(e.getKey()) != null;
			}
			return false;
		}

		private final class UnsignedEntryIterator implements Iterator<Entry<Integer, Integer>> {

			private int idx;

			private UnsignedEntryIterator() {
				super();
				this.idx = 0;
			}

			@Override
			public boolean hasNext() {
				return idx < size;
			}

			@Override
			public Entry<Integer, Integer> next() {
				final int i = idx++;
				return new SimpleImmutableEntry<>(keys[i], Short.toUnsignedInt(values[i]));
			}

			@Override
			public void remove() {
				removeKeyAtIndex(--idx);
			}

		}

	}
}
