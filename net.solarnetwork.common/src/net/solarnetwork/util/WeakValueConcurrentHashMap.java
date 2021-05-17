/* ==================================================================
 * WeakValueConcurrentHashMap.java - 10/05/2021 10:29:13 AM
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

package net.solarnetwork.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A {@code ConcurrentMap} implementation with weak-referenced values.
 * 
 * <p>
 * The values stored in this map are only weakly-referenced. If a value is
 * garbage collected, then the associated key will remain in this map, but
 * methods that query for the value based on its key will return
 * {@literal null}.
 * </p>
 * 
 * @author matt
 * @version 1.0
 */
public class WeakValueConcurrentHashMap<K, V> extends AbstractMap<K, V> implements ConcurrentMap<K, V> {

	private final ConcurrentMap<K, WeakValue<V>> data;

	/**
	 * Creates a new, empty map with the default initial table size (16).
	 */
	public WeakValueConcurrentHashMap() {
		super();
		this.data = new ConcurrentHashMap<>();
	}

	/**
	 * Creates a new, empty map with an initial table size accommodating the
	 * specified number of elements without the need to dynamically resize.
	 *
	 * @param initialCapacity
	 *        The implementation performs internal sizing to accommodate this
	 *        many elements.
	 * @throws IllegalArgumentException
	 *         if the initial capacity of elements is negative
	 */
	public WeakValueConcurrentHashMap(int initialCapacity) {
		super();
		this.data = new ConcurrentHashMap<>(initialCapacity);
	}

	/**
	 * Creates a new map with the same mappings as the given map.
	 *
	 * @param m
	 *        the map
	 */
	public WeakValueConcurrentHashMap(Map<? extends K, ? extends V> m) {
		this();
		putAll(m);
	}

	/**
	 * Creates a new, empty map with an initial table size based on the given
	 * number of elements ({@code initialCapacity}) and initial table density
	 * ({@code loadFactor}).
	 *
	 * @param initialCapacity
	 *        the initial capacity. The implementation performs internal sizing
	 *        to accommodate this many elements, given the specified load
	 *        factor.
	 * @param loadFactor
	 *        the load factor (table density) for establishing the initial table
	 *        size
	 * @throws IllegalArgumentException
	 *         if the initial capacity of elements is negative or the load
	 *         factor is nonpositive
	 *
	 * @since 1.6
	 */
	public WeakValueConcurrentHashMap(int initialCapacity, float loadFactor) {
		this(initialCapacity, loadFactor, 1);
	}

	/**
	 * Creates a new, empty map with an initial table size based on the given
	 * number of elements ({@code initialCapacity}), table density
	 * ({@code loadFactor}), and number of concurrently updating threads
	 * ({@code concurrencyLevel}).
	 *
	 * @param initialCapacity
	 *        the initial capacity. The implementation performs internal sizing
	 *        to accommodate this many elements, given the specified load
	 *        factor.
	 * @param loadFactor
	 *        the load factor (table density) for establishing the initial table
	 *        size
	 * @param concurrencyLevel
	 *        the estimated number of concurrently updating threads. The
	 *        implementation may use this value as a sizing hint.
	 * @throws IllegalArgumentException
	 *         if the initial capacity is negative or the load factor or
	 *         concurrencyLevel are nonpositive
	 */
	public WeakValueConcurrentHashMap(int initialCapacity, float loadFactor, int concurrencyLevel) {
		super();
		this.data = new ConcurrentHashMap<>(initialCapacity, loadFactor, concurrencyLevel);
	}

	@Override
	public boolean containsValue(Object value) {
		Objects.requireNonNull(value, "The value must not be null.");
		return data.containsValue(new WeakValue<>(value));
	}

	@Override
	public boolean containsKey(Object key) {
		return data.containsKey(key);
	}

	@Override
	public V get(Object key) {
		WeakValue<V> v = data.get(key);
		return (v != null ? v.get() : null);
	}

	@Override
	public V put(K key, V value) {
		Objects.requireNonNull(value, "The value must not be null.");
		WeakValue<V> v = data.put(key, new WeakValue<>(value));
		return (v != null ? v.get() : null);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		if ( m == null ) {
			return;
		}
		for ( Map.Entry<? extends K, ? extends V> me : m.entrySet() ) {
			data.put(me.getKey(), new WeakValue<>(me.getValue()));
		}
	}

	@Override
	public V remove(Object key) {
		WeakValue<V> v = data.remove(key);
		return (v != null ? v.get() : null);
	}

	@Override
	public void clear() {
		data.clear();
	}

	@Override
	public V putIfAbsent(K key, V value) {
		Objects.requireNonNull(value, "The value must not be null.");
		WeakReference<V> v = data.putIfAbsent(key, new WeakValue<>(value));
		return (v != null ? v.get() : null);
	}

	@Override
	public boolean remove(Object key, Object value) {
		Objects.requireNonNull(value, "The value must not be null.");
		return data.remove(key, new WeakValue<>(value));
	}

	@Override
	public boolean replace(K key, V oldValue, V newValue) {
		Objects.requireNonNull(oldValue, "The newValue must not be null.");
		Objects.requireNonNull(newValue, "The newValue must not be null.");
		return data.replace(key, new WeakValue<>(oldValue), new WeakValue<>(newValue));
	}

	@Override
	public V replace(K key, V value) {
		Objects.requireNonNull(value, "The value must not be null.");
		WeakValue<V> v = data.replace(key, new WeakValue<>(value));
		return (v != null ? v.get() : null);
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		return new EntrySet<>(data.entrySet());
	}

	@Override
	public int size() {
		return data.size();
	}

	@Override
	public boolean isEmpty() {
		return data.isEmpty();
	}

	@Override
	public Set<K> keySet() {
		return data.keySet();
	}

	@Override
	public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
		Objects.requireNonNull(mappingFunction, "A mapping function is required.");
		WeakValue<V> r = data.computeIfAbsent(key, k -> {
			V mapped = mappingFunction.apply(k);
			return (mapped != null ? new WeakValue<>(mapped) : null);
		});
		return (r != null ? r.get() : null);
	}

	@Override
	public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		Objects.requireNonNull(remappingFunction, "A remapping function is required.");
		WeakValue<V> r = data.computeIfPresent(key, (k, v) -> {
			V old = (v != null ? v.get() : null);
			V mapped = remappingFunction.apply(k, old);
			return (mapped != null ? new WeakValue<>(mapped) : null);
		});
		return (r != null ? r.get() : null);
	}

	@Override
	public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		Objects.requireNonNull(remappingFunction, "A remapping function is required.");
		WeakValue<V> r = data.compute(key, (k, v) -> {
			V old = (v != null ? v.get() : null);
			V mapped = remappingFunction.apply(k, old);
			return (mapped != null ? new WeakValue<>(mapped) : null);
		});
		return (r != null ? r.get() : null);
	}

	private static final class EntrySet<K, V> extends AbstractSet<Map.Entry<K, V>> {

		private final Set<Entry<K, WeakValue<V>>> set;

		private EntrySet(Set<Entry<K, WeakValue<V>>> set) {
			super();
			this.set = set;
		}

		@Override
		public Iterator<Entry<K, V>> iterator() {
			return new EntryIterator(set.iterator());
		}

		@Override
		public int size() {
			return set.size();
		}

		private final class EntryIterator implements Iterator<Entry<K, V>> {

			private final Iterator<Entry<K, WeakValue<V>>> itr;

			private EntryIterator(Iterator<Entry<K, WeakValue<V>>> itr) {
				super();
				this.itr = itr;
			}

			@Override
			public boolean hasNext() {
				return itr.hasNext();
			}

			@Override
			public Entry<K, V> next() {
				Entry<K, WeakValue<V>> e = itr.next();
				return (e != null ? new WeakEntry(e) : null);
			}
		}

		private final class WeakEntry implements Entry<K, V> {

			private final Entry<K, WeakValue<V>> e;

			private WeakEntry(Entry<K, WeakValue<V>> e) {
				super();
				this.e = e;
			}

			@Override
			public K getKey() {
				return e.getKey();
			}

			@Override
			public V getValue() {
				WeakValue<V> v = e.getValue();
				return (v != null ? v.get() : null);
			}

			@Override
			public V setValue(V value) {
				WeakValue<V> v = e.setValue(new WeakValue<>(value));
				return (v != null ? v.get() : null);
			}

		}

	}

	private static class WeakValue<T> extends WeakReference<T> {

		private WeakValue(T referent, ReferenceQueue<? super T> q) {
			super(referent, q);
		}

		private WeakValue(T referent) {
			super(referent);
		}

		@Override
		public int hashCode() {
			T v = get();
			return (v != null ? v.hashCode() : super.hashCode());
		}

		@Override
		public boolean equals(Object obj) {
			@SuppressWarnings("unchecked")
			WeakValue<T> o = (WeakValue<T>) obj;
			T v = get();
			return (v != null ? v.equals(o.get()) : super.equals(obj));
		}

	}

}
