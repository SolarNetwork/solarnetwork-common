/* ==================================================================
 * GroupedBitmaskable.java - 22/04/2020 2:33:35 pm
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

package net.solarnetwork.domain;

import java.util.BitSet;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

/**
 * API for a {@link Bitmaskable} that is grouped by an index value into
 * like-sized groupings.
 * 
 * @author matt
 * @version 1.0
 * @since 1.61
 */
public interface GroupedBitmaskable extends Bitmaskable {

	/**
	 * A comparator of {@link GroupedBitmaskable} by their overall index, in
	 * ascending order.
	 */
	final Comparator<GroupedBitmaskable> SORT_BY_OVERALL_INDEX = new SortByOverallIndex();

	/**
	 * Get the group index.
	 * 
	 * @return the group index
	 */
	int getGroupIndex();

	/**
	 * Get the size of each group.
	 * 
	 * @return the size of each group
	 */
	int getGroupSize();

	/**
	 * Get the overall bitmask number within all groups, starting from
	 * {@literal 0}.
	 * 
	 * @return the fault number
	 */
	default int getOverallIndex() {
		return (getGroupIndex() * getGroupSize()) + bitmaskBitOffset();
	}

	/**
	 * Get a group's bitmask value from a set of grouped bitmaskables.
	 * 
	 * @param values
	 *        the set of grouped bitmaskables to extract the group's bitmask
	 *        value from
	 * @param group
	 *        the bitmask group to extract
	 * @return the bitmask value for the given group
	 */
	public static int groupBitmaskValue(Set<? extends GroupedBitmaskable> values, int group) {
		if ( values == null || values.isEmpty() ) {
			return 0;
		}
		Set<GroupedBitmaskable> set = new HashSet<>(16);
		for ( GroupedBitmaskable f : values ) {
			if ( group == f.getGroupIndex() ) {
				set.add(f);
			}
		}
		if ( set.isEmpty() ) {
			return 0;
		}
		return Bitmaskable.bitmaskValue(set);
	}

	/**
	 * Get the overall bitmask values from a set of grouped bitmaskables.
	 * 
	 * @param values
	 *        the set of grouped bitmaskables to extract the overall bitmask
	 *        value from
	 * @return the bitset of overall enabled bits, or {@literal null} if no bits
	 *         are set
	 */
	public static BitSet overallBitmaskValue(Set<? extends GroupedBitmaskable> values) {
		if ( values == null || values.isEmpty() ) {
			return null;
		}
		BitSet set = new BitSet();
		for ( GroupedBitmaskable f : values ) {
			set.set(f.getOverallIndex());
		}
		if ( set.isEmpty() ) {
			return null;
		}
		return set;
	}

	/**
	 * A comparator of {@link GroupedBitmaskable} that compares overall index
	 * values.
	 */
	final class SortByOverallIndex implements Comparator<GroupedBitmaskable> {

		@Override
		public int compare(GroupedBitmaskable o1, GroupedBitmaskable o2) {
			if ( o1 == o2 ) {
				return 0;
			} else if ( o1 == null ) {
				return -1;
			} else if ( o2 == null ) {
				return 1;
			}
			int n1 = o1.getOverallIndex();
			int n2 = o2.getOverallIndex();
			return Integer.compare(n1, n2);
		}

	}

}
