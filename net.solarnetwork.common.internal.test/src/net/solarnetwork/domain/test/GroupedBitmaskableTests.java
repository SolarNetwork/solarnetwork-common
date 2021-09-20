/* ==================================================================
 * GroupedBitmaskableTests.java - 22/04/2020 2:59:53 pm
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

package net.solarnetwork.domain.test;

import static java.util.Arrays.asList;
import static net.solarnetwork.domain.GroupedBitmaskable.groupBitmaskValue;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.junit.Test;
import net.solarnetwork.domain.GroupedBitmaskable;

/**
 * Test cases for {@link GroupedBitmaskable}.
 * 
 * @author matt
 * @version 1.0
 */
public class GroupedBitmaskableTests {

	private static interface Group extends GroupedBitmaskable {

	}

	private static enum G1 implements Group {

		V1,
		V2,
		V3;

		@Override
		public int bitmaskBitOffset() {
			return ordinal();
		}

		@Override
		public int getGroupIndex() {
			return 0;
		}

		@Override
		public int getGroupSize() {
			return 8;
		}

	}

	private static enum G2 implements Group {

		V4,
		V5,
		V6;

		@Override
		public int bitmaskBitOffset() {
			return ordinal();
		}

		@Override
		public int getGroupIndex() {
			return 1;
		}

		@Override
		public int getGroupSize() {
			return 8;
		}

	}

	@Test
	public void groupBitmaskValue_0() {
		// GIVEN
		Set<Group> set = new HashSet<>(8);
		set.addAll(asList(G1.V1, G1.V3, G2.V4));

		// WHEN
		int value = groupBitmaskValue(set, 0);

		// THEN
		assertThat("Extracted group 0 bitmask", value, equalTo(0b101));
	}

	@Test
	public void groupBitmaskValue_1() {
		// GIVEN
		Set<Group> set = new HashSet<>(8);
		set.addAll(asList(G1.V1, G1.V3, G2.V4));

		// WHEN
		int value = groupBitmaskValue(set, 1);

		// THEN
		assertThat("Extracted group 1 bitmask", value, equalTo(0b1));
	}

	@Test
	public void sortedGroupBitmaskableSet() {
		// GIVEN
		Set<Group> set = new HashSet<>(8);
		set.addAll(asList(G1.V3, G1.V1, G2.V4, G2.V6, G2.V5));

		// WHEN
		SortedSet<Group> sorted = new TreeSet<>(GroupedBitmaskable.SORT_BY_OVERALL_INDEX);
		sorted.addAll(set);

		// THEN
		assertThat("Items sorted by group then index", sorted,
				contains(G1.V1, G1.V3, G2.V4, G2.V5, G2.V6));
	}

	@Test
	public void overallBitmaskValue() {
		// GIVEN
		Set<Group> set = new HashSet<>(8);
		set.addAll(asList(G1.V1, G1.V3, G2.V4));

		// WHEN
		BitSet result = GroupedBitmaskable.overallBitmaskValue(set);

		// THEN
		BitSet expected = new BitSet();
		expected.set(0);
		expected.set(2);
		expected.set(8);
		assertThat("Extracted overall bitset", result, equalTo(expected));
	}

	@Test
	public void overallBitmaskValue_empty() {
		// GIVEN

		// WHEN
		BitSet result = GroupedBitmaskable.overallBitmaskValue(Collections.emptySet());

		// THEN
		assertThat("Extracted overall bitset", result, nullValue());
	}
}
