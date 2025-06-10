/* ==================================================================
 * IntRangeTests.java - 15/01/2020 1:40:13 pm
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

package net.solarnetwork.util.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;
import org.junit.Test;
import net.solarnetwork.util.IntRange;

/**
 * Test cases for the {@link IntRange} class.
 * 
 * @author matt
 * @version 1.0
 */
public class IntRangeTests {

	@Test
	public void construct() {
		IntRange r = new IntRange(1, 2);
		assertThat("Min set", r.getMin(), equalTo(1));
		assertThat("Max set", r.getMax(), equalTo(2));
	}

	@Test
	public void constructFlippedRange() {
		IntRange r = new IntRange(2, 1);
		assertThat("Min set flipped", r.getMin(), equalTo(1));
		assertThat("Max set flipped", r.getMax(), equalTo(2));
	}

	@Test
	public void constructSingltonRange() {
		IntRange r = new IntRange(1, 1);
		assertThat("Min set", r.getMin(), equalTo(1));
		assertThat("Max set", r.getMax(), equalTo(1));
	}

	@Test
	public void createSingleton() {
		IntRange r = IntRange.rangeOf(1);
		assertThat("Min set", r.getMin(), equalTo(1));
		assertThat("Max set", r.getMax(), equalTo(1));
	}

	@Test
	public void create() {
		IntRange r = IntRange.rangeOf(1, 2);
		assertThat("Min set", r.getMin(), equalTo(1));
		assertThat("Max set", r.getMax(), equalTo(2));
	}

	@Test
	public void isSingleton() {
		IntRange r = IntRange.rangeOf(1);
		assertThat("Singleton", r.isSingleton(), equalTo(true));

		r = IntRange.rangeOf(1, 2);
		assertThat("Singleton", r.isSingleton(), equalTo(false));
	}

	@Test
	public void equals() {
		IntRange r1 = new IntRange(1, 2);
		IntRange r2 = new IntRange(1, 2);
		assertThat("Ranges equal", r1, equalTo(r2));
	}

	@Test
	public void hashCode_consistent() {
		IntRange r1 = new IntRange(1, 2);
		IntRange r2 = new IntRange(1, 2);
		assertThat("Ranges equal", r1.hashCode(), equalTo(r2.hashCode()));
	}

	@Test
	public void compare_lessThan() {
		IntRange r1 = new IntRange(1, 2);
		IntRange r2 = new IntRange(2, 2);
		assertThat("Ranges compare", r1.compareTo(r2), equalTo(-1));
		assertThat("Ranges compare inverse", r2.compareTo(r1), equalTo(1));
	}

	@Test
	public void compare_greaterThan() {
		IntRange r1 = new IntRange(2, 2);
		IntRange r2 = new IntRange(1, 2);
		assertThat("Ranges compare", r1.compareTo(r2), equalTo(1));
		assertThat("Ranges compare inverse", r2.compareTo(r1), equalTo(-1));
	}

	@Test
	public void compare_equal() {
		IntRange r1 = new IntRange(1, 2);
		IntRange r2 = new IntRange(1, 2);
		assertThat("Ranges compare", r1.compareTo(r2), equalTo(0));
		assertThat("Ranges compare inverse", r2.compareTo(r1), equalTo(0));
	}

	@Test
	public void compare_equalStart() {
		IntRange r1 = new IntRange(1, 2);
		IntRange r2 = new IntRange(1, 3);
		assertThat("Ranges compare only based on min", r1.compareTo(r2), equalTo(0));
		assertThat("Ranges compare only based on min inverse", r2.compareTo(r1), equalTo(0));
	}

	@Test
	public void compare_within() {
		IntRange r1 = new IntRange(1, 10);
		IntRange r2 = new IntRange(3, 6);
		assertThat("Ranges compare only based on min", r1.compareTo(r2), equalTo(-1));
		assertThat("Ranges compare only based on min inverse", r2.compareTo(r1), equalTo(1));
	}

	@Test
	public void length() {
		IntRange r = new IntRange(1, 10);
		assertThat("Length", r.length(), equalTo(10));
	}

	@Test
	public void length_singleton() {
		IntRange r = new IntRange(1, 1);
		assertThat("Length singlton", r.length(), equalTo(1));
	}

	@Test
	public void contains() {
		IntRange r = new IntRange(1, 10);
		assertThat("Contains min", r.contains(1), equalTo(true));
		assertThat("Contains max", r.contains(10), equalTo(true));
		for ( int i = 2; i < 10; i++ ) {
			assertThat("Contains inner value " + i, r.contains(i), equalTo(true));
		}
		assertThat("Does not contain < min", r.contains(0), equalTo(false));
		assertThat("Does not contain > max", r.contains(11), equalTo(false));
	}

	@Test
	public void containsAll() {
		IntRange r = new IntRange(1, 10);
		assertThat("Contains min singleton", r.containsAll(new IntRange(1, 1)), equalTo(true));
		assertThat("Contains max singleton", r.containsAll(new IntRange(10, 10)), equalTo(true));
		assertThat("Contains same", r.containsAll(new IntRange(1, 10)), equalTo(true));
		assertThat("Contains head", r.containsAll(new IntRange(1, 5)), equalTo(true));
		assertThat("Contains tail", r.containsAll(new IntRange(5, 10)), equalTo(true));
		assertThat("Contains middle", r.containsAll(new IntRange(4, 6)), equalTo(true));

		assertThat("Does not contain left", r.containsAll(new IntRange(-1, 0)), equalTo(false));
		assertThat("Does not contain right", r.containsAll(new IntRange(11, 12)), equalTo(false));
		assertThat("Does not contain left overlap", r.containsAll(new IntRange(0, 1)), equalTo(false));
		assertThat("Does not contain right overlap", r.containsAll(new IntRange(10, 11)),
				equalTo(false));
		assertThat("Does not contain larger", r.containsAll(new IntRange(0, 11)), equalTo(false));
	}

	@Test
	public void adjacent() {
		IntRange r1 = new IntRange(1, 2);
		IntRange r2 = new IntRange(3, 4);
		assertThat("Ranges adjacent", r1.adjacentTo(r2), equalTo(true));
		assertThat("Ranges adjacent inverse", r2.adjacentTo(r1), equalTo(true));
	}

	@Test
	public void adjacent_gap() {
		IntRange r1 = new IntRange(1, 2);
		IntRange r2 = new IntRange(4, 5);
		assertThat("Gapped ranges not adjacent", r1.adjacentTo(r2), equalTo(false));
		assertThat("Gapped ranges not adjacent inverse", r2.adjacentTo(r1), equalTo(false));
	}

	@Test
	public void adjacent_overlap() {
		IntRange r1 = new IntRange(1, 2);
		IntRange r2 = new IntRange(2, 3);
		assertThat("Overlapping ranges not adjacent", r1.adjacentTo(r2), equalTo(false));
		assertThat("Overlapping ranges not adjacent inverse", r2.adjacentTo(r1), equalTo(false));
	}

	@Test
	public void intersects_overlap() {
		IntRange r1 = new IntRange(1, 3);
		IntRange r2 = new IntRange(2, 5);
		assertThat("Overlapping ranges intersect", r1.intersects(r2), equalTo(true));
		assertThat("Overlapping ranges intersect inverse", r2.intersects(r1), equalTo(true));
	}

	@Test
	public void intersect_gap() {
		IntRange r1 = new IntRange(1, 2);
		IntRange r2 = new IntRange(4, 5);
		assertThat("Gapped ranges do not intersect", r1.intersects(r2), equalTo(false));
		assertThat("Gapped ranges do not intersect inverse", r2.intersects(r1), equalTo(false));
	}

	@Test
	public void intersect_touch() {
		IntRange r1 = new IntRange(1, 2);
		IntRange r2 = new IntRange(2, 3);
		assertThat("Touching ranges intersect", r1.intersects(r2), equalTo(true));
		assertThat("Touching ranges intersect inverse", r2.intersects(r1), equalTo(true));
	}

	@Test
	public void canMerge_adjacent() {
		IntRange r1 = new IntRange(1, 2);
		IntRange r2 = new IntRange(3, 4);
		assertThat("Adjacent ranges can merge", r1.canMergeWith(r2), equalTo(true));
		assertThat("Adjacent ranges can merge inverse", r2.canMergeWith(r1), equalTo(true));
	}

	@Test
	public void canMerge_overlap() {
		IntRange r1 = new IntRange(1, 3);
		IntRange r2 = new IntRange(2, 5);
		assertThat("Overlapping ranges can merge", r1.canMergeWith(r2), equalTo(true));
		assertThat("Overlapping ranges can merge inverse", r2.canMergeWith(r1), equalTo(true));
	}

	@Test
	public void canMerge_subset() {
		IntRange r1 = new IntRange(1, 10);
		IntRange r2 = new IntRange(3, 6);
		assertThat("Subset ranges can merge", r1.canMergeWith(r2), equalTo(true));
		assertThat("Subset ranges can merge inverse", r2.canMergeWith(r1), equalTo(true));
	}

	@Test
	public void canMerge_gap() {
		IntRange r1 = new IntRange(1, 2);
		IntRange r2 = new IntRange(4, 5);
		assertThat("Gapped ranges cannot merge", r1.canMergeWith(r2), equalTo(false));
		assertThat("Gapped ranges cannot merge inverse", r2.canMergeWith(r1), equalTo(false));
	}

	@Test
	public void merge_adjacent() {
		IntRange r1 = new IntRange(1, 2);
		IntRange r2 = new IntRange(3, 4);
		assertThat("Adjacent ranges merge", r1.mergeWith(r2), equalTo(new IntRange(1, 4)));
		assertThat("Adjacent ranges merge inverse", r2.mergeWith(r1), equalTo(new IntRange(1, 4)));

	}

	@Test
	public void merge_overlap() {
		IntRange r1 = new IntRange(1, 3);
		IntRange r2 = new IntRange(2, 5);
		assertThat("Adjacent ranges merge", r1.mergeWith(r2), equalTo(new IntRange(1, 5)));
		assertThat("Adjacent ranges merge inverse", r2.mergeWith(r1), equalTo(new IntRange(1, 5)));

	}

	@Test
	public void merge_subset() {
		IntRange r1 = new IntRange(1, 10);
		IntRange r2 = new IntRange(3, 6);
		assertThat("Subset ranges merge to larger range", r1.mergeWith(r2), sameInstance(r1));
		assertThat("Subset ranges merge to larger range inverse", r2.mergeWith(r1), sameInstance(r1));

	}

	@Test(expected = IllegalArgumentException.class)
	public void merge_gap() {
		IntRange r1 = new IntRange(1, 2);
		IntRange r2 = new IntRange(4, 5);
		r1.mergeWith(r2);

	}

	@Test(expected = IllegalArgumentException.class)
	public void merge_gap_inverse() {
		IntRange r1 = new IntRange(1, 2);
		IntRange r2 = new IntRange(4, 5);
		r2.mergeWith(r1);
	}

}
