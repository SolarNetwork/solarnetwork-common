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
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

/**
 * Utility methods for dealing with collections.
 * 
 * @author matt
 * @version 1.0
 * @since 1.58
 */
public final class CollectionUtils {

	/**
	 * Create integer ranges from a set of integers to produce a reduced set of
	 * ranges that cover all integers in the source set.
	 * 
	 * <p>
	 * The resulting ranges are guaranteed to be equal to, <b>or a super set
	 * of<b>, the given source set. Thus the resulting ranges could include
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
	 * <ul>
	 * 
	 * <p>
	 * then calling this method like {@code minimizeIntRanges(s, 32)} would
	 * return a new set with these ranges:
	 * </p>
	 * 
	 * <ul>
	 * <li>0-28</li>
	 * <li>404-418</li>
	 * <ul>
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
		IntRange last = IntRange.rangeOf(a, b);
		if ( result.isEmpty() || !result.get(result.size() - 1).equals(last) ) {
			result.add(last);
		}
		return result;
	}

}
