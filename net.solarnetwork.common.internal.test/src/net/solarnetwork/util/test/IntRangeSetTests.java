/* ==================================================================
 * IntRangeSetTests.java - 15/01/2020 1:40:24 pm
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

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static net.solarnetwork.util.IntRange.rangeOf;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.stream.StreamSupport;
import org.junit.Test;
import net.solarnetwork.util.IntRange;
import net.solarnetwork.util.IntRangeSet;

/**
 * Test cases for the {@link IntRangeSet} class.
 * 
 * @author matt
 * @version 1.0
 */
public class IntRangeSetTests {

	@Test
	public void add_one() {
		IntRangeSet s = new IntRangeSet();
		boolean result = s.add(1);
		assertThat("Set changed from mutation", result, equalTo(true));

		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Singleton range added", ranges, hasSize(1));
		assertThat("Added range", ranges.get(0), equalTo(rangeOf(1)));
	}

	@Test
	public void add_two_disjoint() {
		IntRangeSet s = new IntRangeSet();
		boolean result = s.add(1);
		assertThat("Set changed from 1st mutation", result, equalTo(true));
		result = s.add(10);
		assertThat("Set changed from 2nd mutation", result, equalTo(true));

		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Singleton ranges added", ranges, hasSize(2));
		assertThat("Added range 1", ranges.get(0), equalTo(rangeOf(1)));
		assertThat("Added range 2", ranges.get(1), equalTo(rangeOf(10, 10)));
	}

	@Test
	public void add_two_disjoint_before() {
		IntRangeSet s = new IntRangeSet();
		boolean result = s.add(10);
		assertThat("Set changed from 1st mutation", result, equalTo(true));
		result = s.add(1);
		assertThat("Set changed from 2nd mutation", result, equalTo(true));

		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Singleton ranges added", ranges, hasSize(2));
		assertThat("Added range 1", ranges.get(0), equalTo(rangeOf(1)));
		assertThat("Added range 2", ranges.get(1), equalTo(rangeOf(10, 10)));
	}

	@Test
	public void add_three_disjoint() {
		IntRangeSet s = new IntRangeSet();
		boolean result = s.add(1);
		assertThat("Set changed from 1st mutation", result, equalTo(true));
		result = s.add(10);
		assertThat("Set changed from 2nd mutation", result, equalTo(true));
		result = s.add(20);
		assertThat("Set changed from 3rd mutation", result, equalTo(true));

		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Singleton ranges added", ranges, hasSize(3));
		assertThat("Added range 1", ranges.get(0), equalTo(rangeOf(1)));
		assertThat("Added range 2", ranges.get(1), equalTo(rangeOf(10, 10)));
		assertThat("Added range 3", ranges.get(2), equalTo(rangeOf(20, 20)));
	}

	@Test
	public void add_three_disjoint_middle() {
		IntRangeSet s = new IntRangeSet();
		boolean result = s.add(1);
		assertThat("Set changed from 1st mutation", result, equalTo(true));
		result = s.add(20);
		assertThat("Set changed from 2nd mutation", result, equalTo(true));
		result = s.add(10);
		assertThat("Set changed from 3rd mutation", result, equalTo(true));

		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Singleton ranges added", ranges, hasSize(3));
		assertThat("Added range 1", ranges.get(0), equalTo(rangeOf(1)));
		assertThat("Added range 2", ranges.get(1), equalTo(rangeOf(10, 10)));
		assertThat("Added range 3", ranges.get(2), equalTo(rangeOf(20, 20)));
	}

	@Test
	public void add_merge_two_ranges() {
		IntRangeSet s = new IntRangeSet();
		boolean result = s.add(1);
		assertThat("Set changed from 1st mutation", result, equalTo(true));
		result = s.add(2);
		assertThat("Set changed from 2nd mutation", result, equalTo(true));
		result = s.add(4);
		assertThat("Set changed from 3rd mutation", result, equalTo(true));
		result = s.add(5);
		assertThat("Set changed from 4th mutation", result, equalTo(true));

		// at this point we should have 2 ranges [1..2][4..5] and by adding 3
		// we expect to end up with a single range [1..5]
		result = s.add(3);
		assertThat("Set changed from 5th mutation", result, equalTo(true));

		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Ended up with a single range", ranges, hasSize(1));
		assertThat("Final range", ranges.get(0), equalTo(rangeOf(1, 5)));
	}

	@Test
	public void add_expand_left() {
		IntRangeSet s = new IntRangeSet();
		boolean result = s.add(1);
		assertThat("Set changed from 1st mutation", result, equalTo(true));
		result = s.add(2);
		assertThat("Set changed from 2nd mutation", result, equalTo(true));
		result = s.add(9);
		assertThat("Set changed from 3rd mutation", result, equalTo(true));
		result = s.add(10);
		assertThat("Set changed from 4th mutation", result, equalTo(true));

		// at this point we should have 2 ranges [1..2][9..10] and by adding 8
		// we expect to end up with two ranges [1..2][8..10]
		result = s.add(8);
		assertThat("Set changed from 5th mutation", result, equalTo(true));

		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Ended up with two ranges", ranges, hasSize(2));
		assertThat("First range unchanged", ranges.get(0), equalTo(rangeOf(1, 2)));
		assertThat("Last range expanded left", ranges.get(1), equalTo(rangeOf(8, 10)));
	}

	@Test
	public void add_expand_left_first() {
		IntRangeSet s = new IntRangeSet();
		boolean result = s.add(1);
		assertThat("Set changed from 1st mutation", result, equalTo(true));
		result = s.add(2);
		assertThat("Set changed from 2nd mutation", result, equalTo(true));
		result = s.add(9);
		assertThat("Set changed from 3rd mutation", result, equalTo(true));
		result = s.add(10);
		assertThat("Set changed from 4th mutation", result, equalTo(true));

		// at this point we should have 2 ranges [1..2][9..10] and by adding 0
		// we expect to end up with two ranges [0..2][9..10]
		result = s.add(0);
		assertThat("Set changed from 5th mutation", result, equalTo(true));

		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Ended up with two ranges", ranges, hasSize(2));
		assertThat("First range unchanged", ranges.get(0), equalTo(rangeOf(0, 2)));
		assertThat("Last range expanded left", ranges.get(1), equalTo(rangeOf(9, 10)));
	}

	@Test
	public void add_expand_right() {
		IntRangeSet s = new IntRangeSet();
		boolean result = s.add(1);
		assertThat("Set changed from 1st mutation", result, equalTo(true));
		result = s.add(2);
		assertThat("Set changed from 2nd mutation", result, equalTo(true));
		result = s.add(9);
		assertThat("Set changed from 3rd mutation", result, equalTo(true));
		result = s.add(10);
		assertThat("Set changed from 4th mutation", result, equalTo(true));

		// at this point we should have 2 ranges [1..2][9..10] and by adding 3
		// we expect to end up with two ranges [1..3][9..10]
		result = s.add(3);
		assertThat("Set changed from 5th mutation", result, equalTo(true));

		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Ended up with two ranges", ranges, hasSize(2));
		assertThat("First range unchanged", ranges.get(0), equalTo(rangeOf(1, 3)));
		assertThat("Last range expanded left", ranges.get(1), equalTo(rangeOf(9, 10)));
	}

	@Test
	public void add_expand_right_last() {
		IntRangeSet s = new IntRangeSet();
		boolean result = s.add(1);
		assertThat("Set changed from 1st mutation", result, equalTo(true));
		result = s.add(2);
		assertThat("Set changed from 2nd mutation", result, equalTo(true));
		result = s.add(9);
		assertThat("Set changed from 3rd mutation", result, equalTo(true));
		result = s.add(10);
		assertThat("Set changed from 4th mutation", result, equalTo(true));

		// at this point we should have 2 ranges [1..2][9..10] and by adding 11
		// we expect to end up with two ranges [1..2][9..11]
		result = s.add(11);
		assertThat("Set changed from 5th mutation", result, equalTo(true));

		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Ended up with two ranges", ranges, hasSize(2));
		assertThat("First range unchanged", ranges.get(0), equalTo(rangeOf(1, 2)));
		assertThat("Last range expanded left", ranges.get(1), equalTo(rangeOf(9, 11)));
	}

	@Test
	public void add_singleton_middle() {
		IntRangeSet s = new IntRangeSet();
		boolean result = s.add(1);
		assertThat("Set changed from 1st mutation", result, equalTo(true));
		result = s.add(2);
		assertThat("Set changed from 2nd mutation", result, equalTo(true));
		result = s.add(9);
		assertThat("Set changed from 3rd mutation", result, equalTo(true));
		result = s.add(10);
		assertThat("Set changed from 4th mutation", result, equalTo(true));

		// at this point we should have 3 ranges [1..2][9..10] and by adding 5
		// we expect to end up with three ranges [1..2][5..5][9..10]
		result = s.add(5);
		assertThat("Set changed from 5th mutation", result, equalTo(true));

		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Ended up with two ranges", ranges, hasSize(3));
		assertThat("First range unchanged", ranges.get(0), equalTo(rangeOf(1, 2)));
		assertThat("First range unchanged", ranges.get(1), equalTo(rangeOf(5)));
		assertThat("Last range expanded left", ranges.get(2), equalTo(rangeOf(9, 10)));
	}

	@Test
	public void add_two_unchanged() {
		IntRangeSet s = new IntRangeSet();
		boolean result = s.add(1);
		assertThat("Set changed from 1st mutation", result, equalTo(true));
		result = s.add(1);
		assertThat("Set unchanged from 2nd mutation", result, equalTo(false));

		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Singleton ranges added", ranges, hasSize(1));
		assertThat("Added range 1", ranges.get(0), equalTo(rangeOf(1)));
	}

	@Test
	public void add_two_adjacent_left() {
		IntRangeSet s = new IntRangeSet();
		boolean result = s.add(1);
		assertThat("Set changed from 1st mutation", result, equalTo(true));
		result = s.add(0);
		assertThat("Set changed from 2nd mutation", result, equalTo(true));

		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Singleton ranges added", ranges, hasSize(1));
		assertThat("Added range 1", ranges.get(0), equalTo(rangeOf(0, 1)));
	}

	@Test
	public void add_two_adjacent_right() {
		IntRangeSet s = new IntRangeSet();
		boolean result = s.add(1);
		assertThat("Set changed from 1st mutation", result, equalTo(true));
		result = s.add(2);
		assertThat("Set changed from 2nd mutation", result, equalTo(true));

		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Singleton ranges added", ranges, hasSize(1));
		assertThat("Added range 1", ranges.get(0), equalTo(rangeOf(1, 2)));
	}

	@Test
	public void addAll_singleton() {
		IntRangeSet s = new IntRangeSet();
		boolean result = s.addAll(asList(1));
		assertThat("Set changed from mutation", result, equalTo(true));
		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Ranges", ranges, contains(rangeOf(1)));
	}

	@Test
	public void addAll_oneRange() {
		IntRangeSet s = new IntRangeSet();
		boolean result = s.addAll(asList(1, 2, 3, 4, 5));
		assertThat("Set changed from mutation", result, equalTo(true));
		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Ranges", ranges, contains(rangeOf(1, 5)));
	}

	@Test
	public void addAll_twoRanges_randomInputOrder() {
		IntRangeSet s = new IntRangeSet();
		boolean result = s.addAll(asList(8, 5, 1, 9, 4, 3, 2, 5, 7));
		assertThat("Set changed from mutation", result, equalTo(true));
		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Ranges", ranges, contains(rangeOf(1, 5), rangeOf(7, 9)));
	}

	@Test
	public void size_empty() {
		IntRangeSet s = new IntRangeSet();
		assertThat("Empty size", s, hasSize(0));
	}

	@Test
	public void size_singleton() {
		IntRangeSet s = new IntRangeSet(rangeOf(1));
		assertThat("Singleton size", s, hasSize(1));
	}

	@Test
	public void size_oneRange() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3));
		assertThat("One range size", s, hasSize(3));
	}

	@Test
	public void size_twoRanges() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3), rangeOf(5, 9));
		assertThat("Two ranges size", s, hasSize(8));
	}

	@Test
	public void size_threeRanges() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3), rangeOf(5, 9), rangeOf(100, 199));
		assertThat("Two ranges size", s, hasSize(108));
	}

	@Test
	public void addRange_initial() {
		IntRangeSet s = new IntRangeSet();
		boolean result = s.addRange(1, 2);
		assertThat("Set changed from 1st mutation", result, equalTo(true));
		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Singleton ranges added", ranges, hasSize(1));
		assertThat("Added range 1", ranges.get(0), equalTo(rangeOf(1, 2)));
	}

	@Test
	public void addRange_insert_end() {
		IntRangeSet s = new IntRangeSet();
		boolean result = s.addRange(1, 2);
		assertThat("Set changed from 1st mutation", result, equalTo(true));

		result = s.addRange(9, 10);
		assertThat("Set changed from 2nd mutation", result, equalTo(true));

		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Singleton ranges added", ranges, hasSize(2));
		assertThat("Added range 1", ranges.get(0), equalTo(rangeOf(1, 2)));
		assertThat("Added range 2", ranges.get(1), equalTo(rangeOf(9, 10)));
	}

	@Test
	public void addRange_expand_right_adjacent() {
		IntRangeSet s = new IntRangeSet();
		boolean result = s.addRange(1, 2);
		assertThat("Set changed from 1st mutation", result, equalTo(true));

		result = s.addRange(3, 4);
		assertThat("Set changed from 2nd mutation", result, equalTo(true));

		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Ranges merged", ranges, hasSize(1));
		assertThat("Expanded right to range", ranges.get(0), equalTo(rangeOf(1, 4)));
	}

	@Test
	public void addRange_expand_right_overlap() {
		IntRangeSet s = new IntRangeSet();
		boolean result = s.addRange(1, 3);
		assertThat("Set changed from 1st mutation", result, equalTo(true));

		result = s.addRange(2, 4);
		assertThat("Set changed from 2nd mutation", result, equalTo(true));

		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Ranges merged", ranges, hasSize(1));
		assertThat("Expanded right to range", ranges.get(0), equalTo(rangeOf(1, 4)));
	}

	@Test
	public void addRange_expand_left_adjacent() {
		IntRangeSet s = new IntRangeSet();
		boolean result = s.addRange(3, 4);
		assertThat("Set changed from 1st mutation", result, equalTo(true));

		result = s.addRange(1, 2);
		assertThat("Set changed from 2nd mutation", result, equalTo(true));

		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Ranges merged", ranges, hasSize(1));
		assertThat("Expanded left to range", ranges.get(0), equalTo(rangeOf(1, 4)));
	}

	@Test
	public void addRange_expand_left_overlap() {
		IntRangeSet s = new IntRangeSet();
		boolean result = s.addRange(2, 4);
		assertThat("Set changed from 1st mutation", result, equalTo(true));

		result = s.addRange(1, 3);
		assertThat("Set changed from 2nd mutation", result, equalTo(true));

		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Ranges merged", ranges, hasSize(1));
		assertThat("Expanded left to range", ranges.get(0), equalTo(rangeOf(1, 4)));
	}

	@Test
	public void addRange_insert_first() {
		IntRangeSet s = new IntRangeSet();
		boolean result = s.addRange(9, 10);
		assertThat("Set changed from 1st mutation", result, equalTo(true));

		result = s.addRange(1, 2);
		assertThat("Set changed from 2nd mutation", result, equalTo(true));

		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Singleton ranges added", ranges, hasSize(2));
		assertThat("Added range 1", ranges.get(0), equalTo(rangeOf(1, 2)));
		assertThat("Added range 2", ranges.get(1), equalTo(rangeOf(9, 10)));
	}

	@Test
	public void addRange_insert_middle() {
		IntRangeSet s = new IntRangeSet();
		boolean result = s.addRange(1, 2);
		assertThat("Set changed from 1st mutation", result, equalTo(true));

		result = s.addRange(9, 10);
		assertThat("Set changed from 2nd mutation", result, equalTo(true));

		result = s.addRange(4, 5);
		assertThat("Set changed from 3rd mutation", result, equalTo(true));

		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Singleton ranges added", ranges, hasSize(3));
		assertThat("Added range 1", ranges.get(0), equalTo(rangeOf(1, 2)));
		assertThat("Added range 2", ranges.get(1), equalTo(rangeOf(4, 5)));
		assertThat("Added range 2", ranges.get(2), equalTo(rangeOf(9, 10)));
	}

	@Test
	public void addRange_unchanged() {
		IntRangeSet s = new IntRangeSet();
		boolean result = s.addRange(1, 3);
		assertThat("Set changed from 1st mutation", result, equalTo(true));

		result = s.addRange(1, 2);
		assertThat("Set unchanged from 2nd mutation", result, equalTo(false));

		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Initial range only", ranges, hasSize(1));
		assertThat("Added range 1", ranges.get(0), equalTo(rangeOf(1, 3)));
	}

	@Test
	public void addRange_merge_ranges_adjacent() {
		IntRangeSet s = new IntRangeSet();
		boolean result = s.addRange(1, 2);
		assertThat("Set changed from 1st mutation", result, equalTo(true));
		result = s.addRange(5, 6);
		assertThat("Set changed from 2nd mutation", result, equalTo(true));

		// at this point we should have 2 ranges [1..2][5..6] and by adding [3..4]
		// we expect to end up with a single range [1..6]
		result = s.addRange(3, 4);
		assertThat("Set changed from 3rd mutation", result, equalTo(true));

		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Ended up with a single range", ranges, hasSize(1));
		assertThat("Final range", ranges.get(0), equalTo(rangeOf(1, 6)));
	}

	@Test
	public void addRange_merge_ranges_overlap_left() {
		IntRangeSet s = new IntRangeSet();
		boolean result = s.addRange(1, 2);
		assertThat("Set changed from 1st mutation", result, equalTo(true));
		result = s.addRange(5, 6);
		assertThat("Set changed from 2nd mutation", result, equalTo(true));

		// at this point we should have 2 ranges [1..2][5..6] and by adding [2..4]
		// we expect to end up with a single range [1..6]
		result = s.addRange(2, 4);
		assertThat("Set changed from 3rd mutation", result, equalTo(true));

		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Ended up with a single range", ranges, hasSize(1));
		assertThat("Final range", ranges.get(0), equalTo(rangeOf(1, 6)));
	}

	@Test
	public void addRange_merge_ranges_overlap_right() {
		IntRangeSet s = new IntRangeSet();
		boolean result = s.addRange(1, 2);
		assertThat("Set changed from 1st mutation", result, equalTo(true));
		result = s.addRange(5, 6);
		assertThat("Set changed from 2nd mutation", result, equalTo(true));

		// at this point we should have 2 ranges [1..2][5..6] and by adding [3..5]
		// we expect to end up with a single range [1..6]
		result = s.addRange(3, 5);
		assertThat("Set changed from 3rd mutation", result, equalTo(true));

		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Ended up with a single range", ranges, hasSize(1));
		assertThat("Final range", ranges.get(0), equalTo(rangeOf(1, 6)));
	}

	@Test
	public void addRange_merge_ranges_overlap_left_right() {
		IntRangeSet s = new IntRangeSet();
		boolean result = s.addRange(1, 2);
		assertThat("Set changed from 1st mutation", result, equalTo(true));
		result = s.addRange(5, 6);
		assertThat("Set changed from 2nd mutation", result, equalTo(true));

		// at this point we should have 2 ranges [1..2][5..6] and by adding [2..5]
		// we expect to end up with a single range [1..6]
		result = s.addRange(2, 5);
		assertThat("Set changed from 3rd mutation", result, equalTo(true));

		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Ended up with a single range", ranges, hasSize(1));
		assertThat("Final range", ranges.get(0), equalTo(rangeOf(1, 6)));
	}

	@Test
	public void addRange_merge_ranges_overlap_multiple_all() {
		IntRangeSet s = new IntRangeSet();
		boolean result = s.addRange(1, 2);
		assertThat("Set changed from 1st mutation", result, equalTo(true));
		result = s.addRange(5, 6);
		assertThat("Set changed from 2nd mutation", result, equalTo(true));
		result = s.addRange(9, 10);
		assertThat("Set changed from 3rd mutation", result, equalTo(true));

		// at this point we should have ranges [1..2][5..6][9..10] and by adding [2..9]
		// we expect to end up with a single range [1..10]
		result = s.addRange(2, 9);
		assertThat("Set changed from 4rd mutation", result, equalTo(true));

		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Ended up with a single range", ranges, hasSize(1));
		assertThat("Final range", ranges.get(0), equalTo(rangeOf(1, 10)));
	}

	@Test
	public void addRange_merge_ranges_overlap_multiple_first() {
		IntRangeSet s = new IntRangeSet();
		boolean result = s.addRange(1, 2);
		assertThat("Set changed from 1st mutation", result, equalTo(true));
		result = s.addRange(5, 6);
		assertThat("Set changed from 2nd mutation", result, equalTo(true));
		result = s.addRange(9, 10);
		assertThat("Set changed from 3rd mutation", result, equalTo(true));

		// at this point we should have ranges [1..2][5..6][9..10] and by adding [2..9]
		// we expect to end up with a single range [1..10]
		result = s.addRange(0, 7);
		assertThat("Set changed from 4rd mutation", result, equalTo(true));

		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Ended up with merged ranges", ranges, hasSize(2));
		assertThat("First range", ranges.get(0), equalTo(rangeOf(0, 7)));
		assertThat("Last range", ranges.get(1), equalTo(rangeOf(9, 10)));
	}

	@Test
	public void addRange_merge_ranges_overlap_multiple_end() {
		IntRangeSet s = new IntRangeSet();
		boolean result = s.addRange(1, 2);
		assertThat("Set changed from 1st mutation", result, equalTo(true));
		result = s.addRange(5, 6);
		assertThat("Set changed from 2nd mutation", result, equalTo(true));
		result = s.addRange(9, 10);
		assertThat("Set changed from 3rd mutation", result, equalTo(true));

		// at this point we should have ranges [1..2][5..6][9..10] and by adding [2..9]
		// we expect to end up with a single range [1..10]
		result = s.addRange(4, 11);
		assertThat("Set changed from 4rd mutation", result, equalTo(true));

		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Ended up with merged ranges", ranges, hasSize(2));
		assertThat("First range", ranges.get(0), equalTo(rangeOf(1, 2)));
		assertThat("Last range", ranges.get(1), equalTo(rangeOf(4, 11)));
	}

	@Test
	public void addRange_merge_ranges_overlap_multiple_larger() {
		IntRangeSet s = new IntRangeSet();
		boolean result = s.addRange(1, 2);
		assertThat("Set changed from 1st mutation", result, equalTo(true));
		result = s.addRange(5, 6);
		assertThat("Set changed from 2nd mutation", result, equalTo(true));
		result = s.addRange(9, 10);
		assertThat("Set changed from 3rd mutation", result, equalTo(true));

		// at this point we should have ranges [1..2][5..6][9..10] and by adding [4..11]
		// we expect to end up with [1..2][4..11]
		result = s.addRange(4, 11);
		assertThat("Set changed from 4rd mutation", result, equalTo(true));

		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Ended up with merged ranges", ranges, hasSize(2));
		assertThat("First range", ranges.get(0), equalTo(rangeOf(1, 2)));
		assertThat("Last range", ranges.get(1), equalTo(rangeOf(4, 11)));
	}

	@Test
	public void addRange_merge_ranges_overlap_multiple_superGreedy() {
		IntRangeSet s = new IntRangeSet();
		boolean result = s.addRange(1, 2);
		assertThat("Set changed from 1st mutation", result, equalTo(true));
		result = s.addRange(5, 6);
		assertThat("Set changed from 2nd mutation", result, equalTo(true));
		result = s.addRange(9, 10);
		assertThat("Set changed from 3rd mutation", result, equalTo(true));

		// at this point we should have ranges [1..2][5..6][9..10] and by adding [4..11]
		// we expect to end up with [0..11]
		result = s.addRange(0, 11);
		assertThat("Set changed from 4rd mutation", result, equalTo(true));

		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Ended up with a single range", ranges, hasSize(1));
		assertThat("First range", ranges.get(0), equalTo(rangeOf(0, 11)));
	}

	@Test
	public void construct_withRanges() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 2), rangeOf(4, 5));
		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Constructed with ranges", ranges, hasSize(2));
		assertThat("Range 1", ranges.get(0), equalTo(rangeOf(1, 2)));
		assertThat("Range 2", ranges.get(1), equalTo(rangeOf(4, 5)));
	}

	@Test
	public void construct_withRanges_merge() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 2), rangeOf(4, 5), rangeOf(3));
		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Constructed with ranges merges to single", ranges, hasSize(1));
		assertThat("Range 1", ranges.get(0), equalTo(rangeOf(1, 5)));
	}

	@Test
	public void iterate() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 2), rangeOf(4, 5));
		Integer[] data = StreamSupport.stream(s.spliterator(), false).toArray(Integer[]::new);
		assertThat("Iterator size", data.length, equalTo(4));
		assertThat("Iterator values", data, arrayContaining(1, 2, 4, 5));
	}

	@Test(expected = NoSuchElementException.class)
	public void iterate_beyond() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 2));
		Iterator<Integer> itr = s.iterator();
		for ( int i = 0; i < 2; i++ ) {
			itr.next();
		}
		assertThat("No more elements available", itr.hasNext(), equalTo(false));
		itr.next();
	}

	@Test
	public void iterateReverse() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 2), rangeOf(4, 5));
		List<Integer> data = new ArrayList<Integer>(4);
		s.descendingIterator().forEachRemaining(data::add);
		assertThat("Iterator size", data.size(), equalTo(4));
		assertThat("Iterator values", data, contains(5, 4, 2, 1));
	}

	@Test(expected = NoSuchElementException.class)
	public void iterateReverse_beyond() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 2));
		Iterator<Integer> itr = s.descendingIterator();
		for ( int i = 0; i < 2; i++ ) {
			itr.next();
		}
		assertThat("No more elements available", itr.hasNext(), equalTo(false));
		itr.next();
	}

	@Test
	public void forEachOrdered() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 2), rangeOf(4, 5));
		List<Integer> data = new ArrayList<>(4);
		s.forEachOrdered(data::add);
		assertThat("Iterator values", data, contains(1, 2, 4, 5));
	}

	@Test
	public void forEachOrdered_empty() {
		IntRangeSet s = new IntRangeSet();
		List<Integer> data = new ArrayList<>(4);
		s.forEachOrdered(data::add);
		assertThat("Iterator values", data, hasSize(0));
	}

	@Test
	public void forEachOrdered_range_head_onExistingKeys() {
		IntRangeSet m = new IntRangeSet(rangeOf(1, 2), rangeOf(4, 7), rangeOf(9, 10));
		List<Integer> data = new ArrayList<>(4);
		m.forEachOrdered(1, 7, data::add);
		assertThat("Consumed values", data, contains(1, 2, 4, 5, 6));
	}

	@Test
	public void forEachOrdered_range_head_onNonexistingKeys() {
		IntRangeSet m = new IntRangeSet(rangeOf(1, 2), rangeOf(4, 7), rangeOf(9, 10));
		List<Integer> data = new ArrayList<>(4);
		m.forEachOrdered(-1, 5, data::add);
		assertThat("Consumed values", data, contains(1, 2, 4));
	}

	@Test
	public void forEachOrdered_range_tail_onExistingKeys() {
		IntRangeSet m = new IntRangeSet(rangeOf(1, 2), rangeOf(4, 7), rangeOf(9, 10));
		List<Integer> data = new ArrayList<>(4);
		m.forEachOrdered(7, 11, data::add);
		assertThat("Consumed values", data, contains(7, 9, 10));
	}

	@Test
	public void forEachOrdered_range_tail_onNonexistingKeys() {
		IntRangeSet m = new IntRangeSet(rangeOf(1, 2), rangeOf(4, 7), rangeOf(9, 10));
		List<Integer> data = new ArrayList<>(4);
		m.forEachOrdered(8, 11, data::add);
		assertThat("Consumed values", data, contains(9, 10));
	}

	@Test
	public void clear() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 2), rangeOf(4, 5));
		assertThat("Ranges present", stream(s.ranges().spliterator(), false).collect(toList()),
				hasSize(2));
		s.clear();
		assertThat("Ranges removed", stream(s.ranges().spliterator(), false).collect(toList()),
				hasSize(0));
	}

	@Test
	public void first_empty() {
		IntRangeSet s = new IntRangeSet();
		assertThat("Empty first", s.first(), nullValue());
	}

	@Test
	public void first_singleton() {
		IntRangeSet s = new IntRangeSet(rangeOf(1));
		assertThat("Singleton first", s.first(), equalTo(1));
	}

	@Test
	public void first_oneRange() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3));
		assertThat("One range first", s.first(), equalTo(1));
	}

	@Test
	public void first_twoRanges() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3), rangeOf(5, 9));
		assertThat("Two ranges first", s.first(), equalTo(1));
	}

	@Test
	public void last_empty() {
		IntRangeSet s = new IntRangeSet();
		assertThat("Empty last", s.last(), nullValue());
	}

	@Test
	public void last_singleton() {
		IntRangeSet s = new IntRangeSet(rangeOf(1));
		assertThat("Singleton last", s.last(), equalTo(1));
	}

	@Test
	public void last_oneRange() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3));
		assertThat("One range last", s.last(), equalTo(3));
	}

	@Test
	public void last_twoRanges() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3), rangeOf(5, 9));
		assertThat("Two ranges last", s.last(), equalTo(9));
	}

	@Test
	public void lower_empty() {
		IntRangeSet s = new IntRangeSet();
		assertThat("Empty lower", s.lower(1), nullValue());
	}

	@Test
	public void lower_singleton_first() {
		IntRangeSet s = new IntRangeSet(rangeOf(1));
		assertThat("Singleton lower", s.lower(1), nullValue());
	}

	@Test
	public void lower_singleton_left() {
		IntRangeSet s = new IntRangeSet(rangeOf(1));
		assertThat("Singleton lower", s.lower(0), nullValue());
	}

	@Test
	public void lower_singleton_right() {
		IntRangeSet s = new IntRangeSet(rangeOf(1));
		assertThat("Singleton lower", s.lower(2), equalTo(1));
	}

	@Test
	public void lower_oneRange_first() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3));
		assertThat("One range lower", s.lower(1), nullValue());
	}

	@Test
	public void lower_oneRange_left() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3));
		assertThat("One range lower", s.lower(0), nullValue());
	}

	@Test
	public void lower_oneRange_right() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3));
		assertThat("One range lower", s.lower(4), equalTo(3));
	}

	@Test
	public void lower_oneRange_within() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3));
		assertThat("One range lower", s.lower(2), equalTo(1));
	}

	@Test
	public void lower_twoRanges_first() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3), rangeOf(5, 9));
		assertThat("Two ranges lower", s.lower(1), nullValue());
	}

	@Test
	public void lower_twoRanges_left() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3), rangeOf(5, 9));
		assertThat("Two ranges lower", s.lower(0), nullValue());
	}

	@Test
	public void lower_twoRanges_right() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3), rangeOf(5, 9));
		assertThat("Two ranges lower", s.lower(10), equalTo(9));
	}

	@Test
	public void lower_twoRanges_within_first() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3), rangeOf(5, 9));
		assertThat("Two ranges lower", s.lower(2), equalTo(1));
	}

	@Test
	public void lower_twoRanges_within_last() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3), rangeOf(5, 9));
		assertThat("Two ranges lower", s.lower(8), equalTo(7));
	}

	@Test
	public void lower_twoRanges_between() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3), rangeOf(5, 9));
		assertThat("Two ranges lower", s.lower(4), equalTo(3));
	}

	@Test
	public void floor_empty() {
		IntRangeSet s = new IntRangeSet();
		assertThat("Empty floor", s.floor(1), nullValue());
	}

	@Test
	public void floor_singleton_first() {
		IntRangeSet s = new IntRangeSet(rangeOf(1));
		assertThat("Singleton floor", s.floor(1), equalTo(1));
	}

	@Test
	public void floor_singleton_left() {
		IntRangeSet s = new IntRangeSet(rangeOf(1));
		assertThat("Singleton floor", s.floor(0), nullValue());
	}

	@Test
	public void floor_singleton_right() {
		IntRangeSet s = new IntRangeSet(rangeOf(1));
		assertThat("Singleton floor", s.floor(2), equalTo(1));
	}

	@Test
	public void floor_oneRange_first() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3));
		assertThat("One range floor", s.floor(1), equalTo(1));
	}

	@Test
	public void floor_oneRange_last() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3));
		assertThat("One range floor", s.floor(3), equalTo(3));
	}

	@Test
	public void floor_oneRange_left() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3));
		assertThat("One range floor", s.floor(0), nullValue());
	}

	@Test
	public void floor_oneRange_right() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3));
		assertThat("One range floor", s.floor(4), equalTo(3));
	}

	@Test
	public void floor_oneRange_within() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3));
		assertThat("One range floor", s.floor(2), equalTo(2));
	}

	@Test
	public void floor_twoRanges_first() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3), rangeOf(5, 9));
		assertThat("Two ranges floor", s.floor(1), equalTo(1));
	}

	@Test
	public void floor_twoRanges_left() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3), rangeOf(5, 9));
		assertThat("Two ranges floor", s.floor(0), nullValue());
	}

	@Test
	public void floor_twoRanges_right() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3), rangeOf(5, 9));
		assertThat("Two ranges floor", s.floor(10), equalTo(9));
	}

	@Test
	public void floor_twoRanges_within_first() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3), rangeOf(5, 9));
		assertThat("Two ranges floor", s.floor(2), equalTo(2));
	}

	@Test
	public void floor_twoRanges_within_last() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3), rangeOf(5, 9));
		assertThat("Two ranges floor", s.floor(8), equalTo(8));
	}

	@Test
	public void floor_twoRanges_between() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3), rangeOf(5, 9));
		assertThat("Two ranges floor", s.floor(4), equalTo(3));
	}

	@Test
	public void higher_empty() {
		IntRangeSet s = new IntRangeSet();
		assertThat("Empty higher", s.higher(1), nullValue());
	}

	@Test
	public void higher_singleton_first() {
		IntRangeSet s = new IntRangeSet(rangeOf(1));
		assertThat("Singleton higher", s.higher(1), nullValue());
	}

	@Test
	public void higher_singleton_left() {
		IntRangeSet s = new IntRangeSet(rangeOf(1));
		assertThat("Singleton higher", s.higher(0), equalTo(1));
	}

	@Test
	public void higher_singleton_right() {
		IntRangeSet s = new IntRangeSet(rangeOf(1));
		assertThat("Singleton higher", s.higher(2), nullValue());
	}

	@Test
	public void higher_oneRange_first() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3));
		assertThat("One range higher", s.higher(1), equalTo(2));
	}

	@Test
	public void higher_oneRange_left() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3));
		assertThat("One range higher", s.higher(0), equalTo(1));
	}

	@Test
	public void higher_oneRange_right() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3));
		assertThat("One range higher", s.higher(4), nullValue());
	}

	@Test
	public void higher_oneRange_within() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3));
		assertThat("One range higher", s.higher(2), equalTo(3));
	}

	@Test
	public void higher_twoRanges_first() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3), rangeOf(5, 9));
		assertThat("Two ranges higher", s.higher(1), equalTo(2));
	}

	@Test
	public void higher_twoRanges_left() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3), rangeOf(5, 9));
		assertThat("Two ranges higher", s.higher(0), equalTo(1));
	}

	@Test
	public void higher_twoRanges_right() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3), rangeOf(5, 9));
		assertThat("Two ranges higher", s.higher(10), nullValue());
	}

	@Test
	public void higher_twoRanges_within_first() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3), rangeOf(5, 9));
		assertThat("Two ranges higher", s.higher(2), equalTo(3));
	}

	@Test
	public void higher_twoRanges_within_last() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3), rangeOf(5, 9));
		assertThat("Two ranges higher", s.higher(8), equalTo(9));
	}

	@Test
	public void higher_twoRanges_between() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3), rangeOf(5, 9));
		assertThat("Two ranges higher", s.higher(4), equalTo(5));
	}

	@Test
	public void ceiling_empty() {
		IntRangeSet s = new IntRangeSet();
		assertThat("Empty ceiling", s.ceiling(1), nullValue());
	}

	@Test
	public void ceiling_singleton_first() {
		IntRangeSet s = new IntRangeSet(rangeOf(1));
		assertThat("Singleton ceiling", s.ceiling(1), equalTo(1));
	}

	@Test
	public void ceiling_singleton_left() {
		IntRangeSet s = new IntRangeSet(rangeOf(1));
		assertThat("Singleton ceiling", s.ceiling(0), equalTo(1));
	}

	@Test
	public void ceiling_singleton_right() {
		IntRangeSet s = new IntRangeSet(rangeOf(1));
		assertThat("Singleton ceiling", s.ceiling(2), nullValue());
	}

	@Test
	public void ceiling_oneRange_first() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3));
		assertThat("One range ceiling", s.ceiling(1), equalTo(1));
	}

	@Test
	public void ceiling_oneRange_last() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3));
		assertThat("One range ceiling", s.ceiling(3), equalTo(3));
	}

	@Test
	public void ceiling_oneRange_left() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3));
		assertThat("One range ceiling", s.ceiling(0), equalTo(1));
	}

	@Test
	public void ceiling_oneRange_right() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3));
		assertThat("One range ceiling", s.ceiling(4), nullValue());
	}

	@Test
	public void ceiling_oneRange_within() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3));
		assertThat("One range ceiling", s.ceiling(2), equalTo(2));
	}

	@Test
	public void ceiling_twoRanges_first() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3), rangeOf(5, 9));
		assertThat("Two ranges ceiling", s.ceiling(1), equalTo(1));
	}

	@Test
	public void ceiling_twoRanges_left() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3), rangeOf(5, 9));
		assertThat("Two ranges ceiling", s.ceiling(0), equalTo(1));
	}

	@Test
	public void ceiling_twoRanges_right() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3), rangeOf(5, 9));
		assertThat("Two ranges ceiling", s.ceiling(10), nullValue());
	}

	@Test
	public void ceiling_twoRanges_within_first() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3), rangeOf(5, 9));
		assertThat("Two ranges ceiling", s.ceiling(2), equalTo(2));
	}

	@Test
	public void ceiling_twoRanges_within_last() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3), rangeOf(5, 9));
		assertThat("Two ranges ceiling", s.ceiling(8), equalTo(8));
	}

	@Test
	public void ceiling_twoRanges_between() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3), rangeOf(5, 9));
		assertThat("Two ranges ceiling", s.ceiling(4), equalTo(5));
	}

	@Test
	public void remove_empty() {
		IntRangeSet s = new IntRangeSet();
		boolean result = s.remove(1);
		assertThat("No change on empty set", result, equalTo(false));
	}

	@Test
	public void remove_nonInteger() {
		IntRangeSet s = new IntRangeSet(rangeOf(1));
		@SuppressWarnings("unlikely-arg-type")
		boolean result = s.remove("1");
		assertThat("No change on non-Integer argument", result, equalTo(false));
	}

	@Test
	public void remove_singleton_only() {
		IntRangeSet s = new IntRangeSet(rangeOf(1));
		boolean result = s.remove(1);
		assertThat("Set mutated", result, equalTo(true));
		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Final range set empty", ranges, hasSize(0));
	}

	@Test
	public void remove_singleton_first() {
		IntRangeSet s = new IntRangeSet(rangeOf(1), rangeOf(3));
		boolean result = s.remove(1);
		assertThat("Set mutated", result, equalTo(true));
		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Final range set", ranges, contains(rangeOf(3)));
	}

	@Test
	public void remove_singleton_last() {
		IntRangeSet s = new IntRangeSet(rangeOf(1), rangeOf(3));
		boolean result = s.remove(3);
		assertThat("Set mutated", result, equalTo(true));
		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Final range set", ranges, contains(rangeOf(1)));
	}

	@Test
	public void remove_singleton_middle() {
		IntRangeSet s = new IntRangeSet(rangeOf(1), rangeOf(3), rangeOf(5));
		boolean result = s.remove(3);
		assertThat("Set mutated", result, equalTo(true));
		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Final range set", ranges, contains(rangeOf(1), rangeOf(5)));
	}

	@Test
	public void remove_oneRange_first() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3));
		boolean result = s.remove(1);
		assertThat("Set mutated", result, equalTo(true));
		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Final range set", ranges, contains(rangeOf(2, 3)));
	}

	@Test
	public void remove_oneRange_last() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3));
		boolean result = s.remove(3);
		assertThat("Set mutated", result, equalTo(true));
		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Final range set", ranges, contains(rangeOf(1, 2)));
	}

	@Test
	public void remove_oneRange_middle() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3));
		boolean result = s.remove(2);
		assertThat("Set mutated", result, equalTo(true));
		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Final range set", ranges, contains(rangeOf(1), rangeOf(3)));
	}

	@Test
	public void remove_oneRange_middleLarge() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 10));
		boolean result = s.remove(5);
		assertThat("Set mutated", result, equalTo(true));
		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Final range set", ranges, contains(rangeOf(1, 4), rangeOf(6, 10)));
	}

	@Test
	public void removeAll_empty() {
		IntRangeSet s = new IntRangeSet();
		boolean result = s.removeAll(asList(1, 2));
		assertThat("No change on empty set", result, equalTo(false));
	}

	@Test
	public void removeAll_nonInteger() {
		IntRangeSet s = new IntRangeSet(rangeOf(1));
		@SuppressWarnings("unlikely-arg-type")
		boolean result = s.removeAll(asList("1", "2"));
		assertThat("No change on non-Integer argument", result, equalTo(false));
	}

	@Test
	public void removeAll_singleton_only() {
		IntRangeSet s = new IntRangeSet(rangeOf(1));
		boolean result = s.removeAll(asList(1, 2));
		assertThat("Set mutated", result, equalTo(true));
		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Final range set empty", ranges, hasSize(0));
	}

	@Test
	public void removeAll_singleton_first() {
		IntRangeSet s = new IntRangeSet(rangeOf(1), rangeOf(3));
		boolean result = s.removeAll(asList(1, 2));
		assertThat("Set mutated", result, equalTo(true));
		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Final range set", ranges, contains(rangeOf(3)));
	}

	@Test
	public void removeAll_singleton_last() {
		IntRangeSet s = new IntRangeSet(rangeOf(1), rangeOf(3));
		boolean result = s.removeAll(asList(3, 4));
		assertThat("Set mutated", result, equalTo(true));
		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Final range set", ranges, contains(rangeOf(1)));
	}

	@Test
	public void removeAll_singleton_middle() {
		IntRangeSet s = new IntRangeSet(rangeOf(1), rangeOf(3), rangeOf(5));
		boolean result = s.removeAll(asList(2, 3, 4));
		assertThat("Set mutated", result, equalTo(true));
		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Final range set", ranges, contains(rangeOf(1), rangeOf(5)));
	}

	@Test
	public void removeAll_oneRange_first() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3));
		boolean result = s.removeAll(asList(1, 2));
		assertThat("Set mutated", result, equalTo(true));
		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Final range set", ranges, contains(rangeOf(3)));
	}

	@Test
	public void removeAll_oneRange_last() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3));
		boolean result = s.removeAll(asList(2, 3));
		assertThat("Set mutated", result, equalTo(true));
		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Final range set", ranges, contains(rangeOf(1)));
	}

	@Test
	public void removeAll_oneRange_middle() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 5));
		boolean result = s.removeAll(asList(2, 3));
		assertThat("Set mutated", result, equalTo(true));
		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Final range set", ranges, contains(rangeOf(1), rangeOf(4, 5)));
	}

	@Test
	public void removeAll_oneRange_middleLarge() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 10));
		boolean result = s.removeAll(asList(2, 4, 5, 8));
		assertThat("Set mutated", result, equalTo(true));
		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Final range set", ranges,
				contains(rangeOf(1), rangeOf(3), rangeOf(6, 7), rangeOf(9, 10)));
	}

	@Test
	public void pollFirst_empty() {
		IntRangeSet s = new IntRangeSet();
		Integer result = s.pollFirst();
		assertThat("No result when empty", result, nullValue());
	}

	@Test
	public void pollFirst_singleton_onlyRemaining() {
		IntRangeSet s = new IntRangeSet(rangeOf(1));
		Integer result = s.pollFirst();
		assertThat("Polled value", result, equalTo(1));
		assertThat("Set size reduced", s, hasSize(0));
	}

	@Test
	public void pollFirst_singleton_first() {
		IntRangeSet s = new IntRangeSet(rangeOf(1), rangeOf(3));
		Integer result = s.pollFirst();
		assertThat("Polled value", result, equalTo(1));
		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Final range set", ranges, contains(rangeOf(3)));
	}

	@Test
	public void pollFirst_oneRange() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 2));
		Integer result = s.pollFirst();
		assertThat("Polled value", result, equalTo(1));
		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Final range set", ranges, contains(rangeOf(2)));
	}

	@Test
	public void pollFirst_twoRanges() {
		IntRangeSet s = new IntRangeSet(rangeOf(2, 3), rangeOf(5, 6));
		Integer result = s.pollFirst();
		assertThat("Polled value", result, equalTo(2));
		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Final range set", ranges, contains(rangeOf(3), rangeOf(5, 6)));
	}

	@Test
	public void pollLast_empty() {
		IntRangeSet s = new IntRangeSet();
		Integer result = s.pollLast();
		assertThat("No result when empty", result, nullValue());
	}

	@Test
	public void pollLast_singleton_onlyRemaining() {
		IntRangeSet s = new IntRangeSet(rangeOf(1));
		Integer result = s.pollLast();
		assertThat("Polled value", result, equalTo(1));
		assertThat("Set size reduced", s, hasSize(0));
	}

	@Test
	public void pollLast_singleton_last() {
		IntRangeSet s = new IntRangeSet(rangeOf(1), rangeOf(3));
		Integer result = s.pollLast();
		assertThat("Polled value", result, equalTo(3));
		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Final range set", ranges, contains(rangeOf(1)));
	}

	@Test
	public void pollLast_oneRange() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 2));
		Integer result = s.pollLast();
		assertThat("Polled value", result, equalTo(2));
		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Final range set", ranges, contains(rangeOf(1)));
	}

	@Test
	public void pollLast_twoRanges() {
		IntRangeSet s = new IntRangeSet(rangeOf(2, 3), rangeOf(5, 6));
		Integer result = s.pollLast();
		assertThat("Polled value", result, equalTo(6));
		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Final range set", ranges, contains(rangeOf(2, 3), rangeOf(5)));
	}

	@Test
	public void reverseSet_empty() {
		IntRangeSet s = new IntRangeSet();
		NavigableSet<Integer> d = s.descendingSet();
		assertThat("Empty descending set created", d, hasSize(0));
	}

	@Test
	public void reverseSet_empty_add() {
		IntRangeSet s = new IntRangeSet();
		NavigableSet<Integer> d = s.descendingSet();
		d.addAll(asList(1, 2, 4, 5));
		List<Integer> data = new ArrayList<Integer>(4);
		d.iterator().forEachRemaining(data::add);
		assertThat("Iterator size", data.size(), equalTo(4));
		assertThat("Iterator values", data, contains(5, 4, 2, 1));
	}

	@Test
	public void reverseSet_singleton() {
		IntRangeSet s = new IntRangeSet(rangeOf(1));
		NavigableSet<Integer> d = s.descendingSet();
		assertThat("Empty descending set created", d, hasSize(1));
		List<Integer> data = new ArrayList<Integer>(1);
		d.iterator().forEachRemaining(data::add);
		assertThat("Iterator size", data.size(), equalTo(1));
		assertThat("Iterator values", data, contains(1));
	}

	@Test
	public void reverseSet_oneRange() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 2));
		NavigableSet<Integer> d = s.descendingSet();
		assertThat("Empty descending set size", d, hasSize(2));
		List<Integer> data = new ArrayList<Integer>(2);
		d.iterator().forEachRemaining(data::add);
		assertThat("Iterator size", data.size(), equalTo(2));
		assertThat("Iterator values", data, contains(2, 1));
		assertThat("First value reversed", d.first(), equalTo(2));
		assertThat("Last value reversed", d.last(), equalTo(1));

		data = new ArrayList<Integer>(2);
		s.iterator().forEachRemaining(data::add);
		assertThat("Mutations go to backing set", data, contains(1, 2));
	}

	@Test
	public void reverseSet_twoRanges() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 2), rangeOf(4, 5));
		NavigableSet<Integer> d = s.descendingSet();
		assertThat("Empty descending set size", d, hasSize(4));
		List<Integer> data = new ArrayList<Integer>(4);
		d.iterator().forEachRemaining(data::add);
		assertThat("Iterator size", data.size(), equalTo(4));
		assertThat("Iterator values", data, contains(5, 4, 2, 1));
		assertThat("First value reversed", d.first(), equalTo(5));
		assertThat("Last value reversed", d.last(), equalTo(1));

		data = new ArrayList<Integer>(2);
		s.iterator().forEachRemaining(data::add);
		assertThat("Mutations go to backing set", data, contains(1, 2, 4, 5));
	}

	@Test
	public void immutableCopy() {
		IntRangeSet s = new IntRangeSet();
		s.add(1);
		IntRangeSet c = s.immutableCopy();
		s.add(2);
		assertThat("Original set mutated", s, hasSize(2));
		assertThat("Immutable copied at point in time", c, hasSize(1));
		assertThat("Immutable copy contains original", c, contains(1));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void immutableCopy_add() {
		IntRangeSet s = new IntRangeSet();
		s.add(1);
		IntRangeSet c = s.immutableCopy();
		c.add(2);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void immutableCopy_addRange() {
		IntRangeSet s = new IntRangeSet();
		s.add(1);
		IntRangeSet c = s.immutableCopy();
		c.addRange(2, 3);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void immutableCopy_remove() {
		IntRangeSet s = new IntRangeSet();
		s.add(1);
		IntRangeSet c = s.immutableCopy();
		c.remove(1);
	}

	@Test
	public void nonOrderedInputOrderedAndCombined() {
		IntRangeSet set = new IntRangeSet();
		set.addRange(3109, 3109 + 2);
		set.addRange(3059, 3059 + 2);
		set.addRange(3009, 3009 + 2);
		set.addRange(3035, 3035 + 2);
		set.addRange(3203, 3203 + 4);
		set.addRange(3207, 3207 + 4);
		List<IntRange> ranges = stream(set.ranges().spliterator(), false).collect(toList());
		assertThat("Ranges", ranges, contains(rangeOf(3009, 3011), rangeOf(3035, 3037),
				rangeOf(3059, 3061), rangeOf(3109, 3111), rangeOf(3203, 3211)));
	}

}
