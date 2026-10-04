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
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import org.jspecify.annotations.Nullable;

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
 * <p>
 * <b>This class is not thread-safe.</b> If multiple threads access an instance
 * concurrently, and at least one of them modifies it, then all access must be
 * synchronized externally, including read-only methods like
 * {@link #getValue(int)}, {@link #forEachOrdered(IntShortBiConsumer)}, and
 * {@link #clone()}. A read concurrent with a modification can return the value
 * of a different key or throw an exception, not just return an outdated value.
 * </p>
 *
 * <p>
 * The iterators of this map's collection views, including those of
 * {@link #unsignedMap()}, and the {@code forEachOrdered()} methods are
 * <i>fail-fast</i>: if the map is structurally modified while iterating, in
 * any way except through the iterator's own {@code remove()} method, they
 * throw a {@link ConcurrentModificationException}. Adding or removing keys is
 * a structural modification; changing the value of an existing key is not.
 * Fail-fast behavior is best-effort, so it cannot be relied on to detect
 * unsynchronized concurrent modification.
 * </p>
 *
 * <p>
 * The entries of {@link #entrySet()}, and of the {@link #unsignedMap()} entry
 * set, are immutable snapshots: their {@link Map.Entry#setValue(Object)}
 * method throws an {@link UnsupportedOperationException}, and so does
 * {@link Map#replaceAll(java.util.function.BiFunction)}, which relies on it.
 * Change values with {@link #putValue(int, short)} instead.
 * </p>
 *
 * @author matt
 * @version 1.1
 * @since 1.58
 */
public class IntShortMap extends AbstractMap<Integer, Short>
		implements Map<Integer, Short>, IntShortOrderedIterable, Cloneable {

	/** The default initial capacity. */
	public static final int DEFAULT_INITIAL_CAPACITY = 16;

	/**
	 * The default value that causes {@code NoSuchElementException} to be thrown
	 * in {@link #getValue(int)}, when passed to {@link #IntShortMap(int, short)}.
	 *
	 * <p>
	 * This is also the 16-bit value {@code 0x8000}. To return that value for
	 * nonexistent keys instead, use {@link #IntShortMap(int, short, boolean)}.
	 * </p>
	 */
	public static final short VALUE_NO_SUCH_ELEMENT = Short.MIN_VALUE;

	private final short notFoundValue;
	private final boolean throwIfNotFound;
	private int[] keys;
	private short[] values;
	private int size;

	/** The count of structural modifications, for fail-fast iteration. */
	private int modCount;

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
	 *         if {@code initialCapacity} is negative
	 */
	public IntShortMap(int initialCapacity) {
		this(initialCapacity, (short) 0);
	}

	/**
	 * Constructor.
	 *
	 * <p>
	 * As {@link #VALUE_NO_SUCH_ELEMENT} means to throw an exception, this
	 * constructor cannot create a map that returns that value for nonexistent
	 * keys. Use {@link #IntShortMap(int, short, boolean)} for that.
	 * </p>
	 *
	 * @param initialCapacity
	 *        the initial capacity
	 * @param notFoundValue
	 *        the value to return in {@link #getValue(int)} if a key is not
	 *        found, or {@link #VALUE_NO_SUCH_ELEMENT} to throw a
	 *        {@link NoSuchElementException}
	 * @throws IllegalArgumentException
	 *         if {@code initialCapacity} is negative
	 */
	public IntShortMap(int initialCapacity, short notFoundValue) {
		this(initialCapacity, notFoundValue, notFoundValue == VALUE_NO_SUCH_ELEMENT);
	}

	/**
	 * Constructor.
	 *
	 * <p>
	 * Unlike {@link #IntShortMap(int, short)}, this treats every
	 * {@code notFoundValue} as a value to return, including
	 * {@link #VALUE_NO_SUCH_ELEMENT}.
	 * </p>
	 *
	 * @param initialCapacity
	 *        the initial capacity
	 * @param notFoundValue
	 *        the value to return in {@link #getValue(int)} if a key is not
	 *        found, when {@code throwIfNotFound} is {@literal false}
	 * @param throwIfNotFound
	 *        {@literal true} to throw a {@link NoSuchElementException} in
	 *        {@link #getValue(int)} if a key is not found, instead of returning
	 *        {@code notFoundValue}
	 * @throws IllegalArgumentException
	 *         if {@code initialCapacity} is negative
	 * @since 1.1
	 */
	public IntShortMap(int initialCapacity, short notFoundValue, boolean throwIfNotFound) {
		super();
		if ( initialCapacity < 0 ) {
			throw new IllegalArgumentException("The initial capacity must be 0 or more.");
		}
		this.notFoundValue = notFoundValue;
		this.throwIfNotFound = throwIfNotFound;
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

	/**
	 * Create a copy of this map.
	 *
	 * <p>
	 * The capacity of the copy is reduced to the size of this map, unless this
	 * map is empty.
	 * </p>
	 *
	 * @return the copy
	 */
	@Override
	public IntShortMap clone() {
		final IntShortMap m;
		try {
			m = (IntShortMap) super.clone();
		} catch ( CloneNotSupportedException e ) {
			// should not get here
			throw new RuntimeException(e);
		}
		final int len = (m.size > 0 ? m.size : m.keys.length);
		m.keys = Arrays.copyOf(m.keys, len);
		m.values = Arrays.copyOf(m.values, len);
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
	 * @throws ConcurrentModificationException
	 *         if this map is structurally modified while iterating, for example
	 *         by {@code action}
	 */
	@Override
	public void forEachOrdered(IntShortBiConsumer action) {
		Objects.requireNonNull(action);
		final int mc = modCount;
		for ( int i = 0; modCount == mc && i < size; i++ ) {
			action.accept(keys[i], values[i]);
		}
		if ( modCount != mc ) {
			throw new ConcurrentModificationException();
		}
	}

	/**
	 * Iterate over a range of key/value pairs in this map.
	 *
	 * <p>
	 * This method of iteration can be more efficient than iterating via
	 * {@link #entrySet()} because no intermediate {@code Set},
	 * {@code Iterator}, or {@code Entry} objects are created, and not primitive
	 * boxing occurs.
	 * </p>
	 *
	 * @param min
	 *        the minimum key value (inclusive)
	 * @param max
	 *        the maximum key value (exclusive)
	 * @param action
	 *        the consumer to handle the key/value pairs
	 * @throws ConcurrentModificationException
	 *         if this map is structurally modified while iterating, for example
	 *         by {@code action}
	 */
	@Override
	public void forEachOrdered(int min, int max, IntShortBiConsumer action) {
		Objects.requireNonNull(action);
		final int mc = modCount;
		int start = binarySearch(keys, 0, size, min);
		if ( start < 0 ) {
			start = -(start + 1);
		}
		for ( int i = start; modCount == mc && i < size && keys[i] < max; i++ ) {
			action.accept(keys[i], values[i]);
		}
		if ( modCount != mc ) {
			throw new ConcurrentModificationException();
		}
	}

	@Override
	public Set<Integer> keySet() {
		return new KeySet();
	}

	@Override
	public Collection<Short> values() {
		return new ValueCollection();
	}

	@Override
	public boolean containsValue(@Nullable Object value) {
		if ( !(value instanceof Short s) ) {
			return false;
		}
		final short v = s.shortValue();
		for ( int i = 0; i < size; i++ ) {
			if ( v == values[i] ) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean containsKey(@Nullable Object key) {
		return (key instanceof Integer k) && containsKey(k.intValue());
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
		modCount++;
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
	 * <p>
	 * The capacity is reduced to the size of this map.
	 * </p>
	 *
	 * @return {@literal true} if any capacity was freed
	 */
	public boolean compact() {
		if ( size >= keys.length ) {
			return false;
		}
		this.keys = Arrays.copyOf(keys, size);
		this.values = Arrays.copyOf(values, size);
		return true;
	}

	@Override
	public @Nullable Short get(@Nullable Object key) {
		return (key instanceof Integer k ? get(k.intValue()) : null);
	}

	/**
	 * Get the value for a given key.
	 *
	 * @param k
	 *        the key of the value to get
	 * @return the associated value, or {@code null} if {@code k} is not
	 *         present
	 */
	public @Nullable Short get(final int k) {
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
	 * @return the associated value, or the configured not-found value if
	 *         {@code k} is not present
	 * @throws NoSuchElementException
	 *         if {@code k} is not present and this map is configured to throw
	 *         an exception for nonexistent keys
	 */
	public short getValue(final int k) {
		final int idx = binarySearch(keys, 0, size, k);
		if ( idx >= 0 ) {
			return values[idx];
		}
		if ( throwIfNotFound ) {
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
	 * @return the previous value associated with {@code k}, or {@code null}
	 *         if none
	 */
	public @Nullable Short putValue(final int k, final int value) {
		return putValue(k, (short) value);
	}

	/**
	 * Put primitive values.
	 *
	 * @param k
	 *        the key
	 * @param value
	 *        the value
	 * @return the previous value associated with {@code k}, or {@code null}
	 *         if none
	 */
	public @Nullable Short putValue(final int k, final short value) {
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
			modCount++;
		}
		return prev;
	}

	@Override
	public @Nullable Short put(Integer key, Short value) {
		return putValue(key, value);
	}

	@Override
	public @Nullable Short remove(@Nullable Object key) {
		if ( !(key instanceof Integer k) ) {
			return null;
		}
		final int idx = binarySearch(keys, 0, size, k.intValue());
		if ( idx < 0 ) {
			return null;
		}
		final short prev = values[idx];
		removeKeyAtIndex(idx);
		return prev;
	}

	/**
	 * Get a view of this map with unsigned integer values.
	 *
	 * <p>
	 * Values put into the view must be between {@literal 0} and
	 * {@literal 65535}: {@link Map#put(Object, Object)} throws an
	 * {@link IllegalArgumentException} for any other value, rather than
	 * truncating it.
	 * </p>
	 *
	 * @return a new map, backed by this map's data, where the values are
	 *         returned as unsigned integers
	 */
	public Map<Integer, Integer> unsignedMap() {
		final Set<Entry<Integer, Integer>> entrySet = new UnsignedIntegerEntrySet();
		return new AbstractMap<Integer, Integer>() {

			@Override
			public boolean containsKey(@Nullable Object key) {
				return IntShortMap.this.containsKey(key);
			}

			@Override
			public @Nullable Integer get(@Nullable Object key) {
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
			public @Nullable Integer put(Integer key, Integer value) {
				final int v = value.intValue();
				if ( v < 0 || v > 0xFFFF ) {
					throw new IllegalArgumentException(
							"The value " + v + " must be between 0 and 65535.");
				}
				Short prev = IntShortMap.this.putValue(key, (short) v);
				return (prev != null ? Short.toUnsignedInt(prev) : null);
			}

			@Override
			public @Nullable Integer remove(@Nullable Object key) {
				Short prev = IntShortMap.this.remove(key);
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
		modCount++;
		System.arraycopy(keys, idx + 1, keys, idx, (size - idx));
		System.arraycopy(values, idx + 1, values, idx, (size - idx));
	}

	/**
	 * Base iterator over the array indexes of this map.
	 *
	 * @param <T>
	 *        the element type
	 */
	private abstract class IndexIterator<T> implements Iterator<T> {

		private int idx = 0;
		private int lastIdx = -1;
		private int expectedModCount = modCount;

		/**
		 * Get the element at an array index.
		 *
		 * @param i
		 *        the index
		 * @return the element
		 */
		protected abstract T element(int i);

		@Override
		public boolean hasNext() {
			return idx < size;
		}

		@Override
		public T next() {
			checkForComodification();
			if ( idx >= size ) {
				throw new NoSuchElementException();
			}
			lastIdx = idx++;
			return element(lastIdx);
		}

		@Override
		public void remove() {
			if ( lastIdx < 0 ) {
				throw new IllegalStateException();
			}
			checkForComodification();
			removeKeyAtIndex(lastIdx);
			idx = lastIdx;
			lastIdx = -1;
			expectedModCount = modCount;
		}

		private void checkForComodification() {
			if ( modCount != expectedModCount ) {
				throw new ConcurrentModificationException();
			}
		}

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

		@Override
		public boolean contains(@Nullable Object o) {
			return containsKey(o);
		}

		@Override
		public boolean remove(@Nullable Object o) {
			return IntShortMap.this.remove(o) != null;
		}

		@Override
		public void clear() {
			IntShortMap.this.clear();
		}

	}

	private final class KeyIterator extends IndexIterator<Integer> {

		@Override
		protected Integer element(int i) {
			return keys[i];
		}

	}

	private final class ValueCollection extends AbstractCollection<Short> {

		@Override
		public Iterator<Short> iterator() {
			return new ValueIterator();
		}

		@Override
		public int size() {
			return size;
		}

		@Override
		public boolean contains(@Nullable Object o) {
			return containsValue(o);
		}

		@Override
		public void clear() {
			IntShortMap.this.clear();
		}

	}

	private final class ValueIterator extends IndexIterator<Short> {

		@Override
		protected Short element(int i) {
			return values[i];
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
		public boolean contains(@Nullable Object o) {
			if ( !(o instanceof Entry<?, ?> e) ) {
				return false;
			}
			Short v = IntShortMap.this.get(e.getKey());
			return (v != null && Objects.equals(e.getValue(), v));
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
		public boolean remove(@Nullable Object o) {
			if ( !(o instanceof Entry<?, ?> e) || !contains(e) ) {
				return false;
			}
			return IntShortMap.this.remove(e.getKey()) != null;
		}

		private final class EntryIterator extends IndexIterator<Entry<Integer, Short>> {

			@Override
			protected Entry<Integer, Short> element(int i) {
				return new SimpleImmutableEntry<>(keys[i], values[i]);
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
		public boolean contains(@Nullable Object o) {
			if ( !(o instanceof Entry<?, ?> e) ) {
				return false;
			}
			Short v = IntShortMap.this.get(e.getKey());
			return (v != null && Objects.equals(e.getValue(), Short.toUnsignedInt(v)));
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
		public boolean remove(@Nullable Object o) {
			if ( !(o instanceof Entry<?, ?> e) || !contains(e) ) {
				return false;
			}
			return IntShortMap.this.remove(e.getKey()) != null;
		}

		private final class UnsignedEntryIterator extends IndexIterator<Entry<Integer, Integer>> {

			@Override
			protected Entry<Integer, Integer> element(int i) {
				return new SimpleImmutableEntry<>(keys[i], Short.toUnsignedInt(values[i]));
			}

		}

	}
}
