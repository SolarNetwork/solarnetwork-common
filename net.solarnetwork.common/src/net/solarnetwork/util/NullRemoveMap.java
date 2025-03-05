/* ==================================================================
 * NullRemoveMap.java - 27/11/2024 3:19:19 pm
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

package net.solarnetwork.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Map implementation that delegates to another Map instance while translating
 * all put operations with {@code null} values to remove operations.
 *
 * <p>
 * Calls like {@code put("a", null)} will remove {@code "a"} from the underlying
 * map.
 * </p>
 *
 * @param <K>
 *        the key type
 * @param <V>
 *        the value type
 * @author matt
 * @version 1.0
 * @since 3.28
 */
public class NullRemoveMap<K, V> implements Map<K, V> {

	private final Map<K, V> delegate;

	/**
	 * Constructor.
	 *
	 * @param delegate
	 *        the delegate map
	 * @throws IllegalArgumentException
	 *         if any argument is {@code null}
	 */
	public NullRemoveMap(Map<K, V> delegate) {
		super();
		this.delegate = ObjectUtils.requireNonNullArgument(delegate, "delegate");
	}

	@Override
	public String toString() {
		return delegate.toString();
	}

	@Override
	public int size() {
		return delegate.size();
	}

	@Override
	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		if ( key == null ) {
			return false;
		}
		return delegate.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return delegate.containsValue(value);
	}

	@Override
	public V get(Object key) {
		if ( key == null ) {
			return null;
		}
		return delegate.get(key);
	}

	@Override
	public V put(K key, V value) {
		if ( value == null ) {
			return delegate.remove(key);
		}
		return delegate.put(key, value);
	}

	@Override
	public V remove(Object key) {
		if ( key == null ) {
			return null;
		}
		return delegate.remove(key);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		if ( m == null ) {
			return;
		}
		for ( Entry<? extends K, ? extends V> e : m.entrySet() ) {
			put(e.getKey(), e.getValue());
		}
	}

	@Override
	public void clear() {
		delegate.clear();
	}

	@Override
	public Set<K> keySet() {
		return delegate.keySet();
	}

	@Override
	public Collection<V> values() {
		return delegate.values();
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		return delegate.entrySet();
	}

	@Override
	public boolean equals(Object o) {
		return delegate.equals(o);
	}

	@Override
	public int hashCode() {
		return delegate.hashCode();
	}

	@Override
	public V getOrDefault(Object key, V defaultValue) {
		if ( key == null ) {
			return defaultValue;
		}
		return delegate.getOrDefault(key, defaultValue);
	}

	@Override
	public void forEach(BiConsumer<? super K, ? super V> action) {
		delegate.forEach(action);
	}

	@Override
	public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
		delegate.replaceAll(function);
	}

	@Override
	public V putIfAbsent(K key, V value) {
		if ( value == null ) {
			return null;
		}
		return delegate.putIfAbsent(key, value);
	}

	@Override
	public boolean remove(Object key, Object value) {
		if ( value == null ) {
			return false;
		}
		return delegate.remove(key, value);
	}

	@Override
	public boolean replace(K key, V oldValue, V newValue) {
		if ( newValue == null ) {
			return remove(key, oldValue);
		}
		return delegate.replace(key, oldValue, newValue);
	}

	@Override
	public V replace(K key, V value) {
		if ( value == null ) {
			return remove(key);
		}
		return delegate.replace(key, value);
	}

	@Override
	public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
		return delegate.computeIfAbsent(key, mappingFunction);
	}

	@Override
	public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		return delegate.computeIfPresent(key, remappingFunction);
	}

	@Override
	public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		return delegate.compute(key, remappingFunction);
	}

	@Override
	public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
		return delegate.merge(key, value, remappingFunction);
	}

}
