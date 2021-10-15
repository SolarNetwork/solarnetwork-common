/* ==================================================================
 * CollectionUtils.java - 17/01/2020 10:20:16 am
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

/**
 * Utility methods for dealing with collections.
 * 
 * @author matt
 * @version 1.1
 * @since 1.58
 */
public final class CollectionUtils {

	/**
	 * Create integer ranges from a set of integers to produce a reduced set of
	 * ranges that cover all integers in the source set.
	 * 
	 * <p>
	 * The resulting ranges are guaranteed to be equal to, <b>or a super set
	 * of</b>, the given source set. Thus the resulting ranges could include
	 * <b>more</b> values than the source set. The resulting ranges will not
	 * intersect any other but could be adjacent. They will also be ordered in
	 * ascending order.
	 * </p>
	 * 
	 * <p>
	 * This can be useful when you want to coalesce many discreet ranges into a
	 * smaller number of larger ranges, such as when querying a device for data
	 * in address registers, to reduce the number of overall requests required
	 * to read the complete set of registers. Instead of reading many small
	 * ranges of registers, fewer large ranges of registers can be requested.
	 * For example, if the a set {@code s} passed to this method contained these
	 * ranges:
	 * </p>
	 * 
	 * <ul>
	 * <li>0-1</li>
	 * <li>3-5</li>
	 * <li>20-28</li>
	 * <li>404-406</li>
	 * <li>412-418</li>
	 * </ul>
	 * 
	 * <p>
	 * then calling this method like {@code minimizeIntRanges(s, 32)} would
	 * return a new set with these ranges:
	 * </p>
	 * 
	 * <ul>
	 * <li>0-28</li>
	 * <li>404-418</li>
	 * </ul>
	 * 
	 * @param set
	 *        the set of integers to reduce
	 * @param maxRangeLength
	 *        the maximum length of any combined range in the resulting set
	 * @return a new set of ranges possibly combined, or {@code set} if there
	 *         are less than two ranges to start with, or {@literal null} if
	 *         {@code set} is {@literal null}
	 * @throws IllegalArgumentException
	 *         if {@code maxRangeLength} is less than {@literal 1}
	 */
	public static List<IntRange> coveringIntRanges(SortedSet<Integer> set, int maxRangeLength) {
		if ( maxRangeLength < 1 ) {
			throw new IllegalArgumentException("Max range length must be greater than 0.");
		}
		if ( set == null ) {
			return null;
		}
		List<IntRange> result = new ArrayList<>();
		if ( set.isEmpty() ) {
			return Collections.emptyList();
		}
		IntRange last;
		if ( set instanceof IntOrderedIterable ) {
			final int min = set.first();
			final int[] meta = new int[] { min, min };
			((IntOrderedIterable) set).forEachOrdered(v -> {
				int d = v - meta[0];
				if ( d >= maxRangeLength ) {
					result.add(IntRange.rangeOf(meta[0], meta[1]));
					meta[0] = v;
					meta[1] = v;
				} else {
					meta[1] = v;
				}
			});
			last = IntRange.rangeOf(meta[0], meta[1]);
		} else {
			Iterator<Integer> itr = set.iterator();
			int v = itr.next();
			int a = v;
			int b = a;
			while ( itr.hasNext() ) {
				v = itr.next();
				int d = v - a;
				if ( d >= maxRangeLength ) {
					result.add(IntRange.rangeOf(a, b));
					a = v;
					b = v;
				} else {
					b = v;
				}
			}
			last = IntRange.rangeOf(a, b);
		}
		if ( result.isEmpty() || !result.get(result.size() - 1).equals(last) ) {
			result.add(last);
		}
		return result;
	}

	/**
	 * Convert a dictionary to a map.
	 * 
	 * <p>
	 * This creates a new {@link Map} and copies all the key-value pairs from
	 * the given {@link Dictionary} into it.
	 * </p>
	 * 
	 * @param <K>
	 *        the key type
	 * @param <V>
	 *        the value type
	 * @param dict
	 *        the dictionary to convert
	 * @return the new map instance, or {@literal null} if {@code dict} is
	 *         {@literal null}
	 */
	public static <K, V> Map<K, V> mapForDictionary(Dictionary<K, V> dict) {
		if ( dict == null ) {
			return null;
		}
		Map<K, V> result = new HashMap<>(dict.size());
		Enumeration<K> keyEnum = dict.keys();
		while ( keyEnum.hasMoreElements() ) {
			K key = keyEnum.nextElement();
			if ( key != null ) {
				result.put(key, dict.get(key));
			}
		}
		return result;
	}

	/**
	 * Convert a map to a dictionary.
	 * 
	 * <p>
	 * This creates a new {@link Dictionary} and copies all the key-value pairs
	 * from the given {@link Map} into it.
	 * </p>
	 * 
	 * @param <K>
	 *        the key type
	 * @param <V>
	 *        the value type
	 * @param map
	 *        the map to convert
	 * @return the new dictionary instance, or {@literal null} if {@code map} is
	 *         {@literal null}
	 */
	public static <K, V> Dictionary<K, V> dictionaryForMap(Map<K, V> map) {
		if ( map == null ) {
			return null;
		}
		return new Hashtable<>(map);
	}

}
