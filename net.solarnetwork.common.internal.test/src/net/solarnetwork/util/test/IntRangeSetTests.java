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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.TreeSet;
import java.util.stream.StreamSupport;
import org.junit.Test;
import net.solarnetwork.util.IntRange;
import net.solarnetwork.util.IntRangeSet;

/**
 * Test cases for the {@link IntRangeSet} class.
 *
 * @author matt
 * @version 1.1
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
	public void addAll_farApart() {
		IntRangeSet s = new IntRangeSet();
		boolean result = s.addAll(asList(2_000_000_000, -2_000_000_000));
		assertThat("Set changed from mutation", result, equalTo(true));
		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Values more than Integer.MAX_VALUE apart are not merged", ranges,
				contains(rangeOf(-2_000_000_000), rangeOf(2_000_000_000)));
	}

	@Test
	public void addAll_minAndMaxValues() {
		IntRangeSet s = new IntRangeSet();
		boolean result = s.addAll(asList(Integer.MAX_VALUE, Integer.MIN_VALUE));
		assertThat("Set changed from mutation", result, equalTo(true));
		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Ranges", ranges, contains(rangeOf(Integer.MIN_VALUE), rangeOf(Integer.MAX_VALUE)));
	}

	@Test
	public void addRange_maxValue_afterMinValue() {
		IntRangeSet s = new IntRangeSet();
		s.add(Integer.MIN_VALUE);
		boolean result = s.addRange(Integer.MAX_VALUE, Integer.MAX_VALUE);
		assertThat("Set changed from mutation", result, equalTo(true));
		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("MAX_VALUE not merged with MIN_VALUE", ranges,
				contains(rangeOf(Integer.MIN_VALUE), rangeOf(Integer.MAX_VALUE)));
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
	public void size_rangeLongerThanMaxValue() {
		IntRangeSet s = new IntRangeSet(rangeOf(0, Integer.MAX_VALUE));
		assertThat("Size capped at MAX_VALUE", s.size(), equalTo(Integer.MAX_VALUE));
	}

	@Test
	public void size_allValues() {
		IntRangeSet s = new IntRangeSet(rangeOf(Integer.MIN_VALUE, Integer.MAX_VALUE));
		assertThat("Size capped at MAX_VALUE", s.size(), equalTo(Integer.MAX_VALUE));
	}

	@Test
	public void size_rangesSumLongerThanMaxValue() {
		IntRangeSet s = new IntRangeSet(rangeOf(-2_000_000_000, -1), rangeOf(1, 2_000_000_000));
		assertThat("Size capped at MAX_VALUE", s.size(), equalTo(Integer.MAX_VALUE));
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

	@Test(expected = ConcurrentModificationException.class)
	public void forEachOrdered_concurrentModification() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 5));
		s.forEachOrdered(v -> {
			if ( v == 2 ) {
				s.remove(5);
			}
		});
	}

	@Test(expected = ConcurrentModificationException.class)
	public void forEachOrdered_concurrentModification_atMaxValue() {
		IntRangeSet s = new IntRangeSet(rangeOf(Integer.MAX_VALUE - 1, Integer.MAX_VALUE));
		s.forEachOrdered(v -> {
			if ( v == Integer.MAX_VALUE ) {
				s.remove(Integer.MAX_VALUE - 1);
			}
		});
	}

	@Test(expected = ConcurrentModificationException.class)
	public void forEachOrdered_range_concurrentModification() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 5));
		s.forEachOrdered(1, 10, v -> {
			if ( v == 2 ) {
				s.remove(5);
			}
		});
	}

	@Test(expected = ConcurrentModificationException.class)
	public void forEachOrdered_range_concurrentModification_atMaxValue() {
		IntRangeSet s = new IntRangeSet(rangeOf(Integer.MAX_VALUE - 2, Integer.MAX_VALUE - 1));
		s.forEachOrdered(0, Integer.MAX_VALUE, v -> {
			if ( v == Integer.MAX_VALUE - 1 ) {
				s.remove(Integer.MAX_VALUE - 2);
			}
		});
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

	@Test(expected = NoSuchElementException.class)
	public void first_empty() {
		new IntRangeSet().first();
	}

	@Test
	public void min_empty() {
		IntRangeSet s = new IntRangeSet();
		assertThat("Empty min", s.min(), nullValue());
	}

	@Test
	public void min_twoRanges() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3), rangeOf(5, 9));
		assertThat("Two ranges min", s.min(), equalTo(1));
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

	@Test(expected = NoSuchElementException.class)
	public void last_empty() {
		new IntRangeSet().last();
	}

	@Test
	public void max_empty() {
		IntRangeSet s = new IntRangeSet();
		assertThat("Empty max", s.max(), nullValue());
	}

	@Test
	public void max_twoRanges() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3), rangeOf(5, 9));
		assertThat("Two ranges max", s.max(), equalTo(9));
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
		@SuppressWarnings({ "unlikely-arg-type", "CollectionIncompatibleType" })
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
		@SuppressWarnings({ "unlikely-arg-type", "CollectionIncompatibleType" })
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

	@Test(expected = NoSuchElementException.class)
	public void reverseSet_empty_first() {
		new IntRangeSet().descendingSet().first();
	}

	@Test(expected = NoSuchElementException.class)
	public void reverseSet_empty_last() {
		new IntRangeSet().descendingSet().last();
	}

	@Test
	public void reverseSet_comparator() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 2), rangeOf(4, 5));
		Comparator<? super Integer> cmp = s.descendingSet().comparator();
		assertThat("Comparator provided for reverse order", cmp, notNullValue());
		assertThat("Comparator orders greater values first", cmp.compare(5, 1), lessThan(0));
	}

	@Test
	public void reverseSet_copyToTreeSet() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 2), rangeOf(4, 5));
		TreeSet<Integer> t = new TreeSet<>(s.descendingSet());
		assertThat("Copy iterates in descending order", t, contains(5, 4, 2, 1));
		for ( int v : asList(1, 2, 4, 5) ) {
			assertThat("Copy contains " + v, t.contains(v), equalTo(true));
		}
	}

	@Test
	public void reverseSet_streamSorted() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 2), rangeOf(4, 5));
		List<Integer> data = s.descendingSet().stream().sorted().collect(toList());
		assertThat("Stream sorted into natural order", data, contains(1, 2, 4, 5));
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

	@Test
	public void immutableCopy_ranges_remove() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 2), rangeOf(4, 5));
		IntRangeSet c = s.immutableCopy();
		Iterator<IntRange> itr = c.ranges().iterator();
		itr.next();
		try {
			itr.remove();
			fail("Should not be able to remove range from immutable set");
		} catch ( UnsupportedOperationException e ) {
			// expected
		}
		assertThat("Immutable copy unchanged", c, contains(1, 2, 4, 5));
	}

	@Test
	public void ranges_remove() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 2), rangeOf(4, 5));
		Iterator<IntRange> itr = s.ranges().iterator();
		itr.next();
		try {
			itr.remove();
			fail("Should not be able to remove range via ranges()");
		} catch ( UnsupportedOperationException e ) {
			// expected
		}
		assertThat("Set unchanged", s, contains(1, 2, 4, 5));
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

	@Test
	public void containsImpl() {
		// GIVEN
		IntRangeSet set = new IntRangeSet(rangeOf(2), rangeOf(5, 10), rangeOf(99));

		// THEN
		assertThat("Does not contain value", set.contains(1), is(false));
		assertThat("Contains value", set.contains(2), is(true));
		for ( int i = 5; i <= 10; i++ ) {
			assertThat("Contains value in range", set.contains(i), is(true));
		}
		assertThat("Does not contain value", set.contains(11), is(false));
		assertThat("Contains value", set.contains(99), is(true));
	}

	@Test
	public void containsImpl_empty() {
		// GIVEN
		IntRangeSet set = new IntRangeSet();

		// THEN
		assertThat("Does not contain value", set.contains(1), is(false));
		assertThat("Does not contain value", set.contains(11), is(false));
	}

	@SuppressWarnings({ "unlikely-arg-type", "CollectionIncompatibleType" })
	@Test
	public void containsImpl_nonInteger() {
		// GIVEN
		IntRangeSet set = new IntRangeSet(rangeOf(2));

		// THEN
		assertThat("Does not contain value", set.contains("a"), is(false));
	}

	@Test
	public void subSet() {
		// GIVEN
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3), rangeOf(5, 9));

		// WHEN
		NavigableSet<Integer> v = s.subSet(2, true, 7, false);

		// THEN
		assertThat("View values", v, contains(2, 3, 5, 6));
		assertThat("View size", v, hasSize(4));
		assertThat("View first", v.first(), equalTo(2));
		assertThat("View last", v.last(), equalTo(6));
		assertThat("View contains value in range", v.contains(5), is(true));
		assertThat("View does not contain value below range", v.contains(1), is(false));
		assertThat("View does not contain value above range", v.contains(7), is(false));
		assertThat("View descending values", v.descendingSet(), contains(6, 5, 3, 2));
		List<Integer> data = new ArrayList<>();
		v.descendingIterator().forEachRemaining(data::add);
		assertThat("View descending iterator", data, contains(6, 5, 3, 2));
		assertThat("View equals set of same values", v, equalTo(new HashSet<>(asList(2, 3, 5, 6))));
	}

	@Test
	public void subSet_exclusiveInclusive() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3), rangeOf(5, 9));
		assertThat("Default bounds", s.subSet(2, 7), contains(2, 3, 5, 6));
		assertThat("Exclusive bounds", s.subSet(2, false, 7, false), contains(3, 5, 6));
		assertThat("Inclusive bounds", s.subSet(2, true, 7, true), contains(2, 3, 5, 6, 7));
		assertThat("Empty exclusive bounds", s.subSet(2, false, 2, false), hasSize(0));
		assertThat("Single inclusive bound", s.subSet(2, true, 2, true), contains(2));
	}

	@Test(expected = IllegalArgumentException.class)
	public void subSet_fromGreaterThanTo() {
		new IntRangeSet(rangeOf(1, 9)).subSet(5, 4);
	}

	@Test
	public void headSet() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3), rangeOf(5, 9));
		assertThat("Default exclusive", s.headSet(5), contains(1, 2, 3));
		assertThat("Inclusive", s.headSet(5, true), contains(1, 2, 3, 5));
		assertThat("Below all", s.headSet(1), hasSize(0));
		assertThat("Above all", s.headSet(100), contains(1, 2, 3, 5, 6, 7, 8, 9));
	}

	@Test
	public void tailSet() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3), rangeOf(5, 9));
		assertThat("Default inclusive", s.tailSet(3), contains(3, 5, 6, 7, 8, 9));
		assertThat("Exclusive", s.tailSet(3, false), contains(5, 6, 7, 8, 9));
		assertThat("Above all", s.tailSet(9, false), hasSize(0));
		assertThat("Below all", s.tailSet(-100), contains(1, 2, 3, 5, 6, 7, 8, 9));
	}

	@Test
	public void subSet_navigation() {
		// GIVEN
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3), rangeOf(5, 9));

		// WHEN
		NavigableSet<Integer> v = s.subSet(2, true, 7, true);

		// THEN
		assertThat("Lower within range", v.lower(5), equalTo(3));
		assertThat("Lower at range start", v.lower(2), nullValue());
		assertThat("Lower above range", v.lower(100), equalTo(7));
		assertThat("Floor within range", v.floor(4), equalTo(3));
		assertThat("Floor below range", v.floor(1), nullValue());
		assertThat("Floor above range", v.floor(9), equalTo(7));
		assertThat("Ceiling within range", v.ceiling(4), equalTo(5));
		assertThat("Ceiling above range", v.ceiling(8), nullValue());
		assertThat("Ceiling below range", v.ceiling(-100), equalTo(2));
		assertThat("Higher within range", v.higher(3), equalTo(5));
		assertThat("Higher at range end", v.higher(7), nullValue());
		assertThat("Higher below range", v.higher(1), equalTo(2));
	}

	@Test(expected = NoSuchElementException.class)
	public void subSet_empty_first() {
		new IntRangeSet(rangeOf(1, 3), rangeOf(5, 9)).subSet(4, 5).first();
	}

	@Test(expected = NoSuchElementException.class)
	public void subSet_empty_last() {
		new IntRangeSet(rangeOf(1, 3), rangeOf(5, 9)).subSet(4, 5).last();
	}

	@Test
	public void subSet_writeThrough() {
		// GIVEN
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3), rangeOf(5, 9));
		NavigableSet<Integer> v = s.subSet(2, true, 7, false);

		// WHEN
		boolean added = v.add(4);
		boolean removed = v.remove(5);
		boolean removedOutOfRange = v.remove(1);

		// THEN
		assertThat("Added in range", added, is(true));
		assertThat("Removed in range", removed, is(true));
		assertThat("Did not remove out of range", removedOutOfRange, is(false));
		assertThat("Changes written to backing set", s, contains(1, 2, 3, 4, 6, 7, 8, 9));
		assertThat("View values", v, contains(2, 3, 4, 6));
	}

	@Test
	public void subSet_backingSetChangesVisible() {
		// GIVEN
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3), rangeOf(5, 9));
		NavigableSet<Integer> v = s.subSet(2, true, 7, false);

		// WHEN
		s.add(4);
		s.remove(2);

		// THEN
		assertThat("View shows backing set changes", v, contains(3, 4, 5, 6));
	}

	@Test(expected = IllegalArgumentException.class)
	public void subSet_add_outOfRange() {
		new IntRangeSet(rangeOf(1, 9)).subSet(2, 7).add(7);
	}

	@Test
	public void subSet_clear() {
		// GIVEN
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3), rangeOf(5, 9));

		// WHEN
		s.subSet(2, 7).clear();

		// THEN
		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Range values removed from backing set", ranges, contains(rangeOf(1), rangeOf(7, 9)));
	}

	@Test
	public void subSet_clear_withinRange() {
		// GIVEN
		IntRangeSet s = new IntRangeSet(rangeOf(1, 9));

		// WHEN
		s.subSet(3, 7).clear();

		// THEN
		List<IntRange> ranges = stream(s.ranges().spliterator(), false).collect(toList());
		assertThat("Range split by clear", ranges, contains(rangeOf(1, 2), rangeOf(7, 9)));
	}

	@Test
	public void subSet_removeAll() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 9));
		boolean result = s.subSet(3, 7).removeAll(asList(1, 4, 5));
		assertThat("Set changed", result, is(true));
		assertThat("Only values in range removed", s, contains(1, 2, 3, 6, 7, 8, 9));
	}

	@Test
	public void subSet_poll() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3), rangeOf(5, 9));
		NavigableSet<Integer> v = s.subSet(2, true, 7, false);
		assertThat("Poll first", v.pollFirst(), equalTo(2));
		assertThat("Poll last", v.pollLast(), equalTo(6));
		assertThat("Polled values removed from backing set", s, contains(1, 3, 5, 7, 8, 9));
		assertThat("Poll empty view", s.subSet(10, true, 20, false).pollFirst(), nullValue());
	}

	@Test
	public void subSet_nested() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 9));
		NavigableSet<Integer> v = s.subSet(2, true, 8, false);
		assertThat("Nested subSet", v.subSet(3, 5), contains(3, 4));
		assertThat("Nested headSet at exclusive bound", v.headSet(8, false), contains(2, 3, 4, 5, 6, 7));
		assertThat("Nested tailSet", v.tailSet(6), contains(6, 7));
	}

	@Test(expected = IllegalArgumentException.class)
	public void subSet_nested_outOfRange() {
		new IntRangeSet(rangeOf(1, 9)).subSet(2, true, 8, false).headSet(8, true);
	}

	@Test(expected = IllegalArgumentException.class)
	public void subSet_nested_outOfRange_belowExclusive() {
		new IntRangeSet(rangeOf(1, 9)).subSet(2, true, 8, false).tailSet(1, false);
	}

	@Test
	public void subSet_extremeValues() {
		// GIVEN
		IntRangeSet s = new IntRangeSet(rangeOf(Integer.MIN_VALUE, Integer.MAX_VALUE));

		// THEN
		assertThat("Tail at MAX_VALUE", s.tailSet(Integer.MAX_VALUE - 1),
				contains(Integer.MAX_VALUE - 1, Integer.MAX_VALUE));
		assertThat("Tail after MAX_VALUE", s.tailSet(Integer.MAX_VALUE, false), hasSize(0));
		assertThat("Head at MIN_VALUE descending",
				s.headSet(Integer.MIN_VALUE + 1, true).descendingSet(),
				contains(Integer.MIN_VALUE + 1, Integer.MIN_VALUE));
		assertThat("Head before MIN_VALUE", s.headSet(Integer.MIN_VALUE, false), hasSize(0));
		assertThat("Higher than MAX_VALUE", s.tailSet(0, true).higher(Integer.MAX_VALUE), nullValue());
		assertThat("Lower than MIN_VALUE", s.headSet(0, false).lower(Integer.MIN_VALUE), nullValue());
	}

	@Test
	public void reverseSet_subSets() {
		// GIVEN
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3), rangeOf(5, 9));

		// WHEN
		NavigableSet<Integer> d = s.descendingSet();

		// THEN
		assertThat("Descending headSet", d.headSet(5), contains(9, 8, 7, 6));
		assertThat("Descending headSet inclusive", d.headSet(5, true), contains(9, 8, 7, 6, 5));
		assertThat("Descending tailSet", d.tailSet(5), contains(5, 3, 2, 1));
		assertThat("Descending tailSet exclusive", d.tailSet(5, false), contains(3, 2, 1));
		assertThat("Descending subSet", d.subSet(8, 2), contains(8, 7, 6, 5, 3));
		assertThat("Descending subSet ascending again", d.subSet(8, true, 2, false).descendingSet(),
				contains(3, 5, 6, 7, 8));
	}

	@Test(expected = IllegalArgumentException.class)
	public void reverseSet_subSet_fromLessThanTo() {
		new IntRangeSet(rangeOf(1, 9)).descendingSet().subSet(2, 8);
	}

	private static List<IntRange> rangeList(IntRangeSet s) {
		return stream(s.ranges().spliterator(), false).collect(toList());
	}

	@Test
	public void iterator_remove() {
		// GIVEN
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3), rangeOf(5, 9));

		// WHEN
		List<Integer> data = new ArrayList<>();
		for ( Iterator<Integer> itr = s.iterator(); itr.hasNext(); ) {
			int v = itr.next();
			data.add(v);
			if ( v % 2 == 0 ) {
				itr.remove();
			}
		}

		// THEN
		assertThat("All values iterated", data, contains(1, 2, 3, 5, 6, 7, 8, 9));
		assertThat("Even values removed", rangeList(s),
				contains(rangeOf(1), rangeOf(3), rangeOf(5), rangeOf(7), rangeOf(9)));
	}

	@Test
	public void iterator_remove_all() {
		// GIVEN
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3), rangeOf(5), rangeOf(7, 9));

		// WHEN
		List<Integer> data = new ArrayList<>();
		for ( Iterator<Integer> itr = s.iterator(); itr.hasNext(); ) {
			data.add(itr.next());
			itr.remove();
		}

		// THEN
		assertThat("All values iterated", data, contains(1, 2, 3, 5, 7, 8, 9));
		assertThat("All values removed", s, hasSize(0));
	}

	@Test
	public void iterator_remove_rangeEnds() {
		// GIVEN
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3), rangeOf(5, 7));

		// WHEN
		List<Integer> data = new ArrayList<>();
		for ( Iterator<Integer> itr = s.iterator(); itr.hasNext(); ) {
			int v = itr.next();
			data.add(v);
			if ( v == 3 || v == 5 ) {
				itr.remove();
			}
		}

		// THEN
		assertThat("All values iterated", data, contains(1, 2, 3, 5, 6, 7));
		assertThat("Range end values removed", rangeList(s), contains(rangeOf(1, 2), rangeOf(6, 7)));
	}

	@Test
	public void descendingIterator_remove() {
		// GIVEN
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3), rangeOf(5, 9));

		// WHEN
		List<Integer> data = new ArrayList<>();
		for ( Iterator<Integer> itr = s.descendingIterator(); itr.hasNext(); ) {
			int v = itr.next();
			data.add(v);
			if ( v % 2 == 0 ) {
				itr.remove();
			}
		}

		// THEN
		assertThat("All values iterated", data, contains(9, 8, 7, 6, 5, 3, 2, 1));
		assertThat("Even values removed", rangeList(s),
				contains(rangeOf(1), rangeOf(3), rangeOf(5), rangeOf(7), rangeOf(9)));
	}

	@Test
	public void descendingIterator_remove_all() {
		// GIVEN
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3), rangeOf(5), rangeOf(7, 9));

		// WHEN
		List<Integer> data = new ArrayList<>();
		for ( Iterator<Integer> itr = s.descendingIterator(); itr.hasNext(); ) {
			data.add(itr.next());
			itr.remove();
		}

		// THEN
		assertThat("All values iterated", data, contains(9, 8, 7, 5, 3, 2, 1));
		assertThat("All values removed", s, hasSize(0));
	}

	@Test(expected = IllegalStateException.class)
	public void iterator_remove_beforeNext() {
		new IntRangeSet(rangeOf(1, 3)).iterator().remove();
	}

	@Test
	public void iterator_remove_twice() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3));
		Iterator<Integer> itr = s.iterator();
		itr.next();
		itr.remove();
		try {
			itr.remove();
			fail("Should not be able to remove twice");
		} catch ( IllegalStateException e ) {
			// expected
		}
		assertThat("Only one value removed", s, contains(2, 3));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void iterator_remove_immutable() {
		Iterator<Integer> itr = new IntRangeSet(rangeOf(1, 3)).immutableCopy().iterator();
		itr.next();
		itr.remove();
	}

	@Test(expected = ConcurrentModificationException.class)
	public void iterator_concurrentModification() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 5));
		Iterator<Integer> itr = s.iterator();
		itr.next();
		s.remove(5);
		itr.next();
	}

	@Test(expected = ConcurrentModificationException.class)
	public void descendingIterator_concurrentModification() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 5));
		Iterator<Integer> itr = s.descendingIterator();
		itr.next();
		s.remove(1);
		itr.next();
	}

	@Test
	public void retainAll() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 3), rangeOf(5, 9));
		boolean result = s.retainAll(asList(2, 6, 7, 100));
		assertThat("Set changed", result, is(true));
		assertThat("Only given values retained", rangeList(s), contains(rangeOf(2), rangeOf(6, 7)));
	}

	@Test
	public void removeIf() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 9));
		boolean result = s.removeIf(v -> v > 3 && v < 7);
		assertThat("Set changed", result, is(true));
		assertThat("Matching values removed", rangeList(s), contains(rangeOf(1, 3), rangeOf(7, 9)));
	}

	@Test
	public void reverseSet_removeIf() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 9));
		boolean result = s.descendingSet().removeIf(v -> v % 3 == 0);
		assertThat("Set changed", result, is(true));
		assertThat("Matching values removed", s, contains(1, 2, 4, 5, 7, 8));
	}

	@Test
	public void subSet_removeIf() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 9));
		boolean result = s.subSet(3, true, 7, false).removeIf(v -> v % 2 == 0);
		assertThat("Set changed", result, is(true));
		assertThat("Only matching values in range removed", s, contains(1, 2, 3, 5, 7, 8, 9));
	}

	@Test
	public void subSet_retainAll() {
		IntRangeSet s = new IntRangeSet(rangeOf(1, 9));
		boolean result = s.subSet(3, true, 7, false).retainAll(asList(1, 5));
		assertThat("Set changed", result, is(true));
		assertThat("Only values in range removed", s, contains(1, 2, 5, 7, 8, 9));
	}

	@Test
	public void headSet_iterator_remove_atBound() {
		// GIVEN
		IntRangeSet s = new IntRangeSet(rangeOf(1, 9));

		// WHEN
		List<Integer> data = new ArrayList<>();
		for ( Iterator<Integer> itr = s.headSet(5, true).iterator(); itr.hasNext(); ) {
			int v = itr.next();
			data.add(v);
			if ( v == 5 ) {
				itr.remove();
			}
		}

		// THEN
		assertThat("View values iterated", data, contains(1, 2, 3, 4, 5));
		assertThat("Bound value removed", rangeList(s), contains(rangeOf(1, 4), rangeOf(6, 9)));
	}

	@Test
	public void tailSet_descendingIterator_remove_atBound() {
		// GIVEN
		IntRangeSet s = new IntRangeSet(rangeOf(1, 9));

		// WHEN
		List<Integer> data = new ArrayList<>();
		for ( Iterator<Integer> itr = s.tailSet(5, true).descendingIterator(); itr.hasNext(); ) {
			int v = itr.next();
			data.add(v);
			if ( v == 5 ) {
				itr.remove();
			}
		}

		// THEN
		assertThat("View values iterated", data, contains(9, 8, 7, 6, 5));
		assertThat("Bound value removed", rangeList(s), contains(rangeOf(1, 4), rangeOf(6, 9)));
	}

}
