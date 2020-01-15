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

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import java.util.Iterator;
import java.util.List;
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
		assertThat("Added range", ranges.get(0), equalTo(new IntRange(1, 1)));
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
		assertThat("Added range 1", ranges.get(0), equalTo(new IntRange(1, 1)));
		assertThat("Added range 2", ranges.get(1), equalTo(new IntRange(10, 10)));
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
		assertThat("Added range 1", ranges.get(0), equalTo(new IntRange(1, 1)));
		assertThat("Added range 2", ranges.get(1), equalTo(new IntRange(10, 10)));
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
		assertThat("Added range 1", ranges.get(0), equalTo(new IntRange(1, 1)));
		assertThat("Added range 2", ranges.get(1), equalTo(new IntRange(10, 10)));
		assertThat("Added range 3", ranges.get(2), equalTo(new IntRange(20, 20)));
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
		assertThat("Added range 1", ranges.get(0), equalTo(new IntRange(1, 1)));
		assertThat("Added range 2", ranges.get(1), equalTo(new IntRange(10, 10)));
		assertThat("Added range 3", ranges.get(2), equalTo(new IntRange(20, 20)));
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
		assertThat("Final range", ranges.get(0), equalTo(new IntRange(1, 5)));
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
		assertThat("First range unchanged", ranges.get(0), equalTo(new IntRange(1, 2)));
		assertThat("Last range expanded left", ranges.get(1), equalTo(new IntRange(8, 10)));
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
		assertThat("First range unchanged", ranges.get(0), equalTo(new IntRange(0, 2)));
		assertThat("Last range expanded left", ranges.get(1), equalTo(new IntRange(9, 10)));
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
		assertThat("First range unchanged", ranges.get(0), equalTo(new IntRange(1, 3)));
		assertThat("Last range expanded left", ranges.get(1), equalTo(new IntRange(9, 10)));
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
		assertThat("First range unchanged", ranges.get(0), equalTo(new IntRange(1, 2)));
		assertThat("Last range expanded left", ranges.get(1), equalTo(new IntRange(9, 11)));
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
		assertThat("First range unchanged", ranges.get(0), equalTo(new IntRange(1, 2)));
		assertThat("First range unchanged", ranges.get(1), equalTo(new IntRange(5, 5)));
		assertThat("Last range expanded left", ranges.get(2), equalTo(new IntRange(9, 10)));
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
		assertThat("Added range 1", ranges.get(0), equalTo(new IntRange(1, 1)));
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
		assertThat("Added range 1", ranges.get(0), equalTo(new IntRange(0, 1)));
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
		assertThat("Added range 1", ranges.get(0), equalTo(new IntRange(1, 2)));
	}

	@Test
	public void size_empty() {
		IntRangeSet s = new IntRangeSet();
		assertThat("Empty size", s, hasSize(0));
	}

	@Test
	public void addRange_initial() {
		IntRangeSet s = new IntRangeSet();
		boolean result = s.addRange(1, 2);
		assertThat("Set changed from 1st mutation", result, equalTo(true));
		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Singleton ranges added", ranges, hasSize(1));
		assertThat("Added range 1", ranges.get(0), equalTo(new IntRange(1, 2)));
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
		assertThat("Added range 1", ranges.get(0), equalTo(new IntRange(1, 2)));
		assertThat("Added range 2", ranges.get(1), equalTo(new IntRange(9, 10)));
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
		assertThat("Expanded right to range", ranges.get(0), equalTo(new IntRange(1, 4)));
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
		assertThat("Expanded right to range", ranges.get(0), equalTo(new IntRange(1, 4)));
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
		assertThat("Expanded left to range", ranges.get(0), equalTo(new IntRange(1, 4)));
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
		assertThat("Expanded left to range", ranges.get(0), equalTo(new IntRange(1, 4)));
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
		assertThat("Added range 1", ranges.get(0), equalTo(new IntRange(1, 2)));
		assertThat("Added range 2", ranges.get(1), equalTo(new IntRange(9, 10)));
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
		assertThat("Added range 1", ranges.get(0), equalTo(new IntRange(1, 2)));
		assertThat("Added range 2", ranges.get(1), equalTo(new IntRange(4, 5)));
		assertThat("Added range 2", ranges.get(2), equalTo(new IntRange(9, 10)));
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
		assertThat("Added range 1", ranges.get(0), equalTo(new IntRange(1, 3)));
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
		assertThat("Final range", ranges.get(0), equalTo(new IntRange(1, 6)));
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
		assertThat("Final range", ranges.get(0), equalTo(new IntRange(1, 6)));
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
		assertThat("Final range", ranges.get(0), equalTo(new IntRange(1, 6)));
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
		assertThat("Final range", ranges.get(0), equalTo(new IntRange(1, 6)));
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
		assertThat("Final range", ranges.get(0), equalTo(new IntRange(1, 10)));
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
		assertThat("First range", ranges.get(0), equalTo(new IntRange(0, 7)));
		assertThat("Last range", ranges.get(1), equalTo(new IntRange(9, 10)));
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
		assertThat("First range", ranges.get(0), equalTo(new IntRange(1, 2)));
		assertThat("Last range", ranges.get(1), equalTo(new IntRange(4, 11)));
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
		assertThat("First range", ranges.get(0), equalTo(new IntRange(1, 2)));
		assertThat("Last range", ranges.get(1), equalTo(new IntRange(4, 11)));
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
		assertThat("First range", ranges.get(0), equalTo(new IntRange(0, 11)));
	}

	@Test
	public void construct_withRanges() {
		IntRangeSet s = new IntRangeSet(new IntRange(1, 2), new IntRange(4, 5));
		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Constructed with ranges", ranges, hasSize(2));
		assertThat("Range 1", ranges.get(0), equalTo(new IntRange(1, 2)));
		assertThat("Range 2", ranges.get(1), equalTo(new IntRange(4, 5)));
	}

	@Test
	public void construct_withRanges_merge() {
		IntRangeSet s = new IntRangeSet(new IntRange(1, 2), new IntRange(4, 5), new IntRange(3, 3));
		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Constructed with ranges merges to single", ranges, hasSize(1));
		assertThat("Range 1", ranges.get(0), equalTo(new IntRange(1, 5)));
	}

	@Test
	public void iterate() {
		IntRangeSet s = new IntRangeSet(new IntRange(1, 2), new IntRange(4, 5));
		Integer[] data = StreamSupport.stream(s.spliterator(), false).toArray(Integer[]::new);
		assertThat("Iterator size", data.length, equalTo(4));
		assertThat("Iterator values", data, arrayContaining(1, 2, 4, 5));
	}

	@Test(expected = NoSuchElementException.class)
	public void iterate_beyond() {
		IntRangeSet s = new IntRangeSet(new IntRange(1, 2));
		Iterator<Integer> itr = s.iterator();
		for ( int i = 0; i < 2; i++ ) {
			itr.next();
		}
		assertThat("No more elements available", itr.hasNext(), equalTo(false));
		itr.next();
	}

}
