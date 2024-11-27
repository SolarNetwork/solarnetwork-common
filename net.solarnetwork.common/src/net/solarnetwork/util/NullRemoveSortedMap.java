/* ==================================================================
 * NullRemoveSortedMap.java - 27/11/2024 3:27:44 pm
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
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

/**
 * Extension of {@link NullRemoveMap} for {@link SortedMap}.
 *
 * @param <K>
 *        the key type
 * @param <V>
 *        the value type
 * @author matt
 * @version 1.0
 * @since 3.28
 */
public class NullRemoveSortedMap<K, V> extends NullRemoveMap<K, V> implements SortedMap<K, V> {

	private final SortedMap<K, V> delegate;

	/**
	 * Constructor.
	 *
	 * @param delegate
	 *        the delegate map
	 * @throws IllegalArgumentException
	 *         if any argument is {@code null}
	 */
	public NullRemoveSortedMap(SortedMap<K, V> delegate) {
		super(delegate);
		this.delegate = ObjectUtils.requireNonNullArgument(delegate, "delegate");
	}

	@Override
	public Comparator<? super K> comparator() {
		return delegate.comparator();
	}

	@Override
	public SortedMap<K, V> subMap(K fromKey, K toKey) {
		return delegate.subMap(fromKey, toKey);
	}

	@Override
	public SortedMap<K, V> headMap(K toKey) {
		return delegate.headMap(toKey);
	}

	@Override
	public SortedMap<K, V> tailMap(K fromKey) {
		return delegate.tailMap(fromKey);
	}

	@Override
	public K firstKey() {
		return delegate.firstKey();
	}

	@Override
	public K lastKey() {
		return delegate.lastKey();
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
	public Set<Map.Entry<K, V>> entrySet() {
		return delegate.entrySet();
	}

}
