/* ==================================================================
 * BitmaskableTests.java - 12/08/2021 10:27:06 AM
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

package net.solarnetwork.domain.test;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.junit.Test;
import net.solarnetwork.domain.Bitmaskable;

/**
 * Test cases for the {@link Bitmaskable} interface.
 * 
 * @author matt
 * @version 1.0
 */
public class BitmaskableTests {

	private static enum B1 implements Bitmaskable {

		V1,
		V2,
		V3;

		@Override
		public int bitmaskBitOffset() {
			return ordinal();
		}

	}

	private static enum B2 implements Bitmaskable {

		V1,
		V2,
		V3,
		V4,
		V5,
		V6;

		@Override
		public int bitmaskBitOffset() {
			return ordinal();
		}

	}

	@Test
	public void sortedBitmaskableSet() {
		// GIVEN
		Set<Bitmaskable> set = new HashSet<>(8);
		set.addAll(asList(B1.V3, B1.V1, B2.V4, B2.V6, B2.V5, B1.V2));

		// WHEN
		SortedSet<Bitmaskable> sorted = new TreeSet<>(Bitmaskable.SORT_BY_TYPE);
		sorted.addAll(set);

		// THEN
		assertThat("Items sorted by type then index", sorted,
				contains(B1.V1, B1.V2, B1.V3, B2.V4, B2.V5, B2.V6));
	}

}
