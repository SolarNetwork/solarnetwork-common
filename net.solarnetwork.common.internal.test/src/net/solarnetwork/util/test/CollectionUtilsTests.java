/* ==================================================================
 * CollectionUtilsTests.java - 17/01/2020 10:41:39 am
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
import static java.util.Collections.emptySet;
import static net.solarnetwork.util.IntRange.rangeOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.junit.Test;
import net.solarnetwork.domain.KeyValuePair;
import net.solarnetwork.util.CollectionUtils;
import net.solarnetwork.util.IntRange;
import net.solarnetwork.util.IntRangeSet;

/**
 * Test cases for the {@link CollectionUtils} class.
 *
 * @author matt
 * @version 1.2
 */
public class CollectionUtilsTests {

	@Test
	public void coverintgIntRanges_empty() {
		List<IntRange> result = CollectionUtils.coveringIntRanges(new IntRangeSet(), 64);
		assertThat("Resulting ranges", result, hasSize(0));
	}

	@Test
	public void coveringIntRanges_reduced() {
		IntRangeSet set = new IntRangeSet(rangeOf(0, 1), rangeOf(3, 5), rangeOf(100, 101));
		List<IntRange> result = CollectionUtils.coveringIntRanges(set, 64);
		assertThat("Resulting ranges", result, contains(rangeOf(0, 5), rangeOf(100, 101)));
	}

	@Test
	public void coveringIntRanges_reduced2() {
		IntRangeSet set = new IntRangeSet(rangeOf(1), rangeOf(3, 5), rangeOf(7), rangeOf(9), rangeOf(32),
				rangeOf(60, 72), rangeOf(100, 101));
		List<IntRange> result = CollectionUtils.coveringIntRanges(set, 64);
		assertThat("Resulting ranges", result, contains(rangeOf(1, 64), rangeOf(65, 101)));
	}

	@Test
	public void coveringIntRanges_toSingletons() {
		IntRangeSet set = new IntRangeSet(rangeOf(0, 1), rangeOf(100, 101));
		List<IntRange> result = CollectionUtils.coveringIntRanges(set, 1);
		assertThat("Resulting ranges", result,
				contains(rangeOf(0), rangeOf(1), rangeOf(100), rangeOf(101)));
	}

	@Test
	public void coveringIntRanges_nochange() {
		IntRangeSet set = new IntRangeSet(rangeOf(0, 5), rangeOf(100, 101));
		List<IntRange> result = CollectionUtils.coveringIntRanges(set, 64);
		assertThat("Resulting ranges", result, contains(rangeOf(0, 5), rangeOf(100, 101)));
	}

	@Test
	public void coveringIntRanges_toOneRange() {
		IntRangeSet set = new IntRangeSet(rangeOf(1, 2),
				rangeOf(Integer.MAX_VALUE - 1, Integer.MAX_VALUE));
		List<IntRange> result = CollectionUtils.coveringIntRanges(set, Integer.MAX_VALUE);
		assertThat("Resulting ranges", result, contains(rangeOf(1, Integer.MAX_VALUE)));
	}

	@Test
	public void coveringIntRanges_javaDocExample() {
		IntRangeSet set = new IntRangeSet(rangeOf(0, 1), rangeOf(3, 5), rangeOf(20, 28),
				rangeOf(404, 406), rangeOf(412, 418));
		List<IntRange> result = CollectionUtils.coveringIntRanges(set, 64);
		assertThat("Resulting ranges", result, contains(rangeOf(0, 28), rangeOf(404, 418)));
	}

	@Test
	public void coveringIntRanges_treeSet_javaDocExample() {
		IntRangeSet set = new IntRangeSet(rangeOf(0, 1), rangeOf(3, 5), rangeOf(20, 28),
				rangeOf(404, 406), rangeOf(412, 418));
		SortedSet<Integer> treeSet = new TreeSet<>(set);
		List<IntRange> result = CollectionUtils.coveringIntRanges(treeSet, 64);
		assertThat("Resulting ranges", result, contains(rangeOf(0, 28), rangeOf(404, 418)));
	}

	@Test(expected = IllegalArgumentException.class)
	public void coveringIntRanges_zeroMax() {
		CollectionUtils.coveringIntRanges(new IntRangeSet(), 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void coveringIntRanges_negativeMax() {
		CollectionUtils.coveringIntRanges(new IntRangeSet(), -1);
	}

	@Test
	public void mapForDict_null() {
		// WHEN
		Map<String, Object> m = CollectionUtils.mapForDictionary(null);

		// THEN
		assertThat("Null map returned for null dictionay", m, is(nullValue()));
	}

	@Test
	public void mapForDict_basic() {
		// GIVEN
		Hashtable<String, Object> dict = new Hashtable<>();
		dict.put("foo", "bar");
		dict.put("n", 1);

		// WHEN
		Map<String, Object> m = CollectionUtils.mapForDictionary(dict);

		// THEN
		assertThat("Map created with same size", m.keySet(), hasSize(dict.size()));
		for ( Entry<String, Object> e : dict.entrySet() ) {
			assertThat(String.format("Map contains copied property %s", e.getKey()), m,
					hasEntry(e.getKey(), e.getValue()));
		}
	}

	@Test
	public void sensitiveNames_null() {
		assertThat("Null input results in empty set", CollectionUtils.sensitiveNamesToMask(null),
				hasSize(0));
	}

	@Test
	public void sensitiveNames_empty() {
		assertThat("Empty input results in empty set",
				CollectionUtils.sensitiveNamesToMask(Collections.emptySet()), hasSize(0));
	}

	@Test
	public void sensitiveNames_noMatches() {
		assertThat("Non-matching input results in empty set",
				CollectionUtils.sensitiveNamesToMask(new HashSet<>(Arrays.asList("foo", "bar"))),
				hasSize(0));
	}

	@Test
	public void sensitiveNames_match() {
		// @formatter:off
		String[] input = new String[] {
				"secret",
				"SeCrEt",
				"a string with secretThingy in it",
				"pass",
				"password",
				"myPassword",
		};
		// @formatter:on
		for ( String s : input ) {
			assertThat(String.format("'%s' input results in '%<s' set", s),
					CollectionUtils.sensitiveNamesToMask(new HashSet<>(Arrays.asList("foo", s))),
					containsInAnyOrder(s));
		}
	}

	@Test
	public void filteredSubset_emptySubset() {
		// GIVEN
		Set<Integer> superSet = new HashSet<>(Arrays.asList(1, 2, 3));

		// WHEN
		Set<Integer> result = CollectionUtils.filteredSubset(superSet, emptySet(), LinkedHashSet::new);

		// THEN
		assertThat("Super set returned", result, is(sameInstance(superSet)));
	}

	@Test
	public void filteredSubset_emptySuperset() {
		// GIVEN
		Set<Integer> superSet = Collections.emptySet();

		// WHEN
		Set<Integer> result = CollectionUtils.filteredSubset(superSet, emptySet(), LinkedHashSet::new);

		// THEN
		assertThat("Super set returned", result, is(sameInstance(superSet)));
	}

	@Test
	public void filteredSubset_nullSuperset() {
		// WHEN
		Set<Integer> result = CollectionUtils.filteredSubset(null, emptySet(), LinkedHashSet::new);

		// THEN
		assertThat("Super set returned", result, is(nullValue()));
	}

	@Test
	public void filteredSubset_validSubset() {
		// GIVEN
		Set<Integer> superSet = new HashSet<>(asList(1, 2, 3));

		// WHEN
		final Set<Integer> subSet = new HashSet<>(asList(1, 2));
		Set<Integer> result = CollectionUtils.filteredSubset(superSet, subSet, LinkedHashSet::new);

		// THEN
		assertThat("Sub set returned", result, is(sameInstance(subSet)));
	}

	@Test
	public void filteredSubset_restrictedSubset() {
		// GIVEN
		Set<Integer> superSet = new HashSet<>(asList(1, 2, 3));

		// WHEN
		final Set<Integer> subSet = new HashSet<>(asList(1, 2, 4, 8, 16));
		Set<Integer> result = CollectionUtils.filteredSubset(superSet, subSet, LinkedHashSet::new);

		// THEN
		assertThat("Restricted sub set returned", result, containsInAnyOrder(1, 2));
	}

	@Test
	public void filteredSubset_completelyRestrictedSubset() {
		// GIVEN
		Set<Integer> superSet = new HashSet<>(asList(1, 2, 3));

		// WHEN
		final Set<Integer> subSet = new HashSet<>(asList(4, 8, 16));
		Set<Integer> result = CollectionUtils.filteredSubset(superSet, subSet, LinkedHashSet::new);

		// THEN
		assertThat("Super set returned", result, is(sameInstance(superSet)));
	}

	@Test
	public void sort_null() {
		// WHEN
		Collection<String> result = CollectionUtils.sort(null);

		// THEN
		assertThat("Null result returned for null input collection", result, is(nullValue()));
	}

	@Test
	public void sort_empty() {
		// GIVEN
		List<String> input = Collections.emptyList();

		// WHEN
		Collection<String> result = CollectionUtils.sort(input);

		// THEN
		assertThat("Input returned for empty input collection", result, is(sameInstance(input)));
	}

	@Test
	public void sort_singleton() {
		// GIVEN
		List<String> input = Collections.singletonList("a");

		// WHEN
		Collection<String> result = CollectionUtils.sort(input);

		// THEN
		assertThat("Input returned for singleton input collection", result, is(sameInstance(input)));
	}

	@Test
	public void sort_natural() {
		// GIVEN
		List<String> input = Arrays.asList("b", "d", "c", "a");

		// WHEN
		Collection<String> result = CollectionUtils.sort(input);

		// THEN
		assertThat("Natural sorted result for input collection", result, contains("a", "b", "c", "d"));
	}

	@Test
	public void sort_natural_reverse() {
		// GIVEN
		List<String> input = Arrays.asList("b", "d", "c", "a");

		// WHEN
		Collection<String> result = CollectionUtils.sort(input, true);

		// THEN
		assertThat("Natural sorted result for input collection", result, contains("d", "c", "b", "a"));
	}

	@Test
	public void sort_natural_notComparable() {
		// GIVEN
		List<Object> input = Arrays.asList(new Object(), new Object());

		// WHEN
		Collection<Object> result = CollectionUtils.sort(input, true);

		// THEN
		assertThat("Input returned for non-Comparable input collection", result,
				is(sameInstance(input)));
	}

	@Test
	public void sort_prop() {
		// GIVEN
		// @formatter:off
		List<KeyValuePair> input = Arrays.asList(
				new KeyValuePair("b", "3"),
				new KeyValuePair("d", "1"),
				new KeyValuePair("c", "2"),
				new KeyValuePair("a", "4")
				);
		// @formatter:on

		// WHEN
		Collection<KeyValuePair> result = CollectionUtils.sort(input, "key");

		// THEN
		// @formatter:off
		assertThat("Sorted result for input collection", result, contains(
				new KeyValuePair("a", "4"),
				new KeyValuePair("b", "3"),
				new KeyValuePair("c", "2"),
				new KeyValuePair("d", "1")));
		// @formatter:on
	}

	@Test
	public void sort_prop_reverse() {
		// GIVEN
		// @formatter:off
		List<KeyValuePair> input = Arrays.asList(
				new KeyValuePair("b", "3"),
				new KeyValuePair("d", "1"),
				new KeyValuePair("c", "2"),
				new KeyValuePair("a", "4")
				);
		// @formatter:on

		// WHEN
		Collection<KeyValuePair> result = CollectionUtils.sort(input, true, "value");

		// THEN
		// @formatter:off
		assertThat("Sorted result for input collection", result, contains(
				new KeyValuePair("a", "4"),
				new KeyValuePair("b", "3"),
				new KeyValuePair("c", "2"),
				new KeyValuePair("d", "1")));
		// @formatter:on
	}

	@Test
	public void sort_props() {
		// GIVEN
		// @formatter:off
		List<KeyValuePair> input = Arrays.asList(
				new KeyValuePair("a", "3"),
				new KeyValuePair("a", "1"),
				new KeyValuePair("b", "9"),
				new KeyValuePair("b", "5")
				);
		// @formatter:on

		// WHEN
		Collection<KeyValuePair> result = CollectionUtils.sort(input, "key", "value");

		// THEN
		// @formatter:off
		assertThat("Sorted result for input collection", result, contains(
				new KeyValuePair("a", "1"),
				new KeyValuePair("a", "3"),
				new KeyValuePair("b", "5"),
				new KeyValuePair("b", "9")));
		// @formatter:on
	}

	@SuppressWarnings("unchecked")
	@Test
	public void sort_maps() {
		// GIVEN
		// @formatter:off
		List<Map<String, String>> input = Arrays.asList(
				Collections.singletonMap("a", "3"),
				Collections.singletonMap("a", "1"),
				Collections.singletonMap("a", "9"),
				Collections.singletonMap("a", "5")
				);
		// @formatter:on

		// WHEN
		Collection<Map<String, String>> result = CollectionUtils.sort(input, "a");

		// THEN
		// @formatter:off
		assertThat("Sorted result for input collection", result, contains(
				Collections.singletonMap("a", "1"),
				Collections.singletonMap("a", "3"),
				Collections.singletonMap("a", "5"),
				Collections.singletonMap("a", "9")
				));
		// @formatter:on
	}

}
