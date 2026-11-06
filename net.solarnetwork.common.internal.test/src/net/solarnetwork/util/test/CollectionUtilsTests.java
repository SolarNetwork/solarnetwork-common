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
import static java.util.Map.entry;
import static net.solarnetwork.test.CommonTestUtils.randomFloat;
import static net.solarnetwork.test.CommonTestUtils.randomLong;
import static net.solarnetwork.util.IntRange.rangeOf;
import static org.assertj.core.api.BDDAssertions.then;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;
import org.junit.Test;
import net.solarnetwork.domain.KeyValuePair;
import net.solarnetwork.util.CollectionUtils;
import net.solarnetwork.util.IntRange;
import net.solarnetwork.util.IntRangeSet;
import net.solarnetwork.util.NumberUtils;
import net.solarnetwork.util.StringUtils;

/**
 * Test cases for the {@link CollectionUtils} class.
 *
 * @author matt
 * @version 1.7
 */
public class CollectionUtilsTests {

	private static final String PROP_1 = "p1";
	private static final String PROP_2 = "p2";
	private static final String PROP_3 = "p3";

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

	@Test
	public void coveringIntRanges_extremes() {
		IntRangeSet set = new IntRangeSet(rangeOf(Integer.MIN_VALUE), rangeOf(Integer.MAX_VALUE));
		List<IntRange> result = CollectionUtils.coveringIntRanges(set, 64);
		assertThat("Values further apart than Integer.MAX_VALUE not combined", result,
				contains(rangeOf(Integer.MIN_VALUE), rangeOf(Integer.MAX_VALUE)));
	}

	@Test
	public void coveringIntRanges_treeSet_extremes() {
		SortedSet<Integer> treeSet = new TreeSet<>(asList(Integer.MIN_VALUE, Integer.MAX_VALUE));
		List<IntRange> result = CollectionUtils.coveringIntRanges(treeSet, 64);
		assertThat("Values further apart than Integer.MAX_VALUE not combined", result,
				contains(rangeOf(Integer.MIN_VALUE), rangeOf(Integer.MAX_VALUE)));
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

	@Test
	public void transformMapValues() {
		// GIVEN
		Map<String, String> input = new LinkedHashMap<>(8);
		input.put("a", "1");
		input.put("b", "2");
		input.put("c", "3");
		input.put("d", "4");

		Set<String> transformKeys = new LinkedHashSet<>(Arrays.asList("a", "c"));

		// WHEN
		Map<String, String> result = CollectionUtils.transformMap(input, transformKeys, (s) -> {
			return s + "-xformed";
		});

		// THEN
		assertThat("Map returned", result, is(notNullValue()));
		assertThat("Map is different instance from input", result, is(not(sameInstance(input))));
		assertThat("Map returned with same size as input", result.keySet(), hasSize(input.size()));
		assertThat("Map returned with same keys as input", result.keySet(),
				contains(input.keySet().toArray(new String[input.size()])));
		assertThat("Key a transfomed becacuse in transformKeys", result, hasEntry("a", "1-xformed"));
		assertThat("Key b not transfomed becacuse not in transformKeys", result,
				hasEntry("b", input.get("b")));
		assertThat("Key c transfomed becacuse in transformKeys", result, hasEntry("c", "3-xformed"));
		assertThat("Key d not transfomed becacuse not in transformKeys", result,
				hasEntry("d", input.get("d")));
	}

	@Test
	public void transformMapValues_noTransforms() {
		// GIVEN
		Map<String, String> input = new LinkedHashMap<>(8);
		input.put("a", "1");
		input.put("b", "2");
		input.put("c", "3");
		input.put("d", "4");

		Set<String> transformKeys = new LinkedHashSet<>(Arrays.asList("other"));

		// WHEN
		Map<String, String> result = CollectionUtils.transformMap(input, transformKeys, (s) -> {
			return s + "-xformed";
		});

		// THEN
		assertThat("Map returned is same as input because no keys needed transforming", result,
				is(sameInstance(input)));
	}

	@Test
	public void transformMapValues_nullsRemoved() {
		// GIVEN
		Map<String, String> input = new LinkedHashMap<>(8);
		input.put("a", "1");
		input.put("b", "2");
		input.put("c", "3");
		input.put("d", "4");

		Set<String> transformKeys = new LinkedHashSet<>(Arrays.asList("a", "c"));

		// WHEN
		Map<String, String> result = CollectionUtils.transformMap(input, transformKeys, (s) -> {
			return ("1".equals(s) ? s + "-xformed" : null);
		});

		// THEN
		// @formatter:off
		then(result)
			.as("Map instance returned")
			.isNotNull()
			.as("Different instance returned")
			.isNotSameAs(input)
			.as("Mapper null results are removed from map")
			.containsOnly(
				entry("a", "1-xformed"), // transformed because in transformKeys
				entry("b", "2"),         // not transformed
				                         // c removed because mapper function returned null
				entry("d", "4")          // not transformed
			)
			;
		// @formatter:on
	}

	@Test
	public void transformMapValues_nonStringKeys() {
		// GIVEN
		Map<Integer, String> input = new LinkedHashMap<>(4);
		input.put(1, "1");
		input.put(2, "2");

		// WHEN
		Map<Integer, String> result = CollectionUtils.transformMap(input, Set.of(2),
				(s) -> s + "-xformed");

		// THEN
		// @formatter:off
		then(result)
			.as("Value for non-string key transformed")
			.containsOnly(
				entry(1, "1"),
				entry(2, "2-xformed")
			)
			;
		// @formatter:on
	}

	@Test
	public void transformMapValues_nullKey() {
		// GIVEN
		Map<String, String> input = new LinkedHashMap<>(4);
		input.put(null, "1");
		input.put("a", "2");

		// WHEN
		Map<String, String> result = CollectionUtils.transformMap(input, Set.of("a"),
				(s) -> s + "-xformed");

		// THEN
		// @formatter:off
		then(result)
			.as("Null key left as-is, other key transformed")
			.hasSize(2)
			.containsEntry(null, "1")
			.containsEntry("a", "2-xformed")
			;
		// @formatter:on
	}

	@Test
	public void transformMap() {
		// GIVEN
		final Map<String, Object> input = new LinkedHashMap<>(8);
		input.put("a", 1);
		input.put("b", "two");
		input.put("c", new BigDecimal(3.14159));

		// WHEN
		final Map<String, Number> result = CollectionUtils.transformMap(input, null, (k, v) -> {
			return k + "-xformed";
		}, (k, v) -> {
			Number val = (v instanceof Number n ? n : NumberUtils.parseNumber(v.toString()));
			return (val != null ? val.doubleValue() * 2.0 : null);
		});

		// THEN
		// @formatter:off
		then(result)
			.as("Map instance returned")
			.isNotNull()
			.as("Mapper null results are removed from map")
			.containsOnly(
				entry("a-xformed", 2.0),
				// b removed because value transform function returned null
				entry("c-xformed", 6.28318)
			)
			;
		// @formatter:on
	}

	@Test
	public void transformMap_selectedKeys() {
		// GIVEN
		final Map<String, Object> input = new LinkedHashMap<>(8);
		input.put("a", 1);
		input.put("b", "two");
		input.put("c", new BigDecimal(3.14159));

		// WHEN
		final Map<String, Number> result = CollectionUtils.transformMap(input, Set.of("b", "c"),
				(k, v) -> {
					return k + "-xformed";
				}, (k, v) -> {
					Number val = (v instanceof Number n ? n : NumberUtils.parseNumber(v.toString()));
					return (val != null ? val.doubleValue() * 2.0 : null);
				});

		// THEN
		// @formatter:off
		then(result)
			.as("Map instance returned")
			.isNotNull()
			.as("Mapper null results are removed from map")
			.containsOnly(
				// a removed because not in selected keys
				// b removed because value transform function returned null
				entry("c-xformed", 6.28318)
			)
			;
		// @formatter:on
	}

	@Test
	public void transformMap_nullInput() {
		// GIVEN
		final Map<String, String> input = null;

		// WHEN
		final Map<String, Number> result = CollectionUtils.transformMap(input, null, (k, v) -> k,
				(k, v) -> {
					return NumberUtils.parseNumber(v);
				});

		// THEN
		// @formatter:off
		then(result)
			.as("Null returned for null input")
			.isNull()
			;
		// @formatter:on
	}

	@Test
	public void transformMap_emptyInput() {
		// GIVEN
		final Map<String, String> input = Map.of();

		// WHEN
		final Map<String, Number> result = CollectionUtils.transformMap(input, null, (k, v) -> k,
				(k, v) -> {
					return NumberUtils.parseNumber(v);
				});

		// THEN
		// @formatter:off
		then(result)
			.as("Null returned for empty input")
			.isNull()
			;
		// @formatter:on
	}

	@Test(expected = NumberFormatException.class)
	public void getMapBigDecimal_Infinity() {
		Map<String, Number> map = new LinkedHashMap<>();
		map.put("num", Double.POSITIVE_INFINITY);

		// WHEN
		CollectionUtils.getMapBigDecimal("num", map);
	}

	@Test
	public void differsNumerically_bothEmpty() {
		// GIVEN
		Map<String, Object> m1 = Collections.emptyMap();
		Map<String, Object> m2 = Collections.emptyMap();

		// THEN
		then(CollectionUtils.differsNumerically(m1, m2)).as("Both empty are not different").isFalse();
	}

	@Test
	public void differsNumerically_nonEqualValue() {
		// GIVEN
		Map<String, Object> m1 = Map.of(PROP_1, 1);
		Map<String, Object> m2 = Map.of(PROP_1, 2);

		// THEN
		then(CollectionUtils.differsNumerically(m1, m2)).as("Property value differs").isTrue();
	}

	@Test
	public void differsNumerically_comparableValue_decimal() {
		// GIVEN
		Map<String, Object> m1 = Map.of(PROP_1, new BigDecimal("1.0"));
		Map<String, Object> m2 = Map.of(PROP_1, new BigDecimal("1"));

		// THEN
		then(CollectionUtils.differsNumerically(m1, m2)).as("Property values are comparably equal")
				.isFalse();
	}

	@Test
	public void differsNumerically_comparableValue_double() {
		// GIVEN
		Map<String, Object> m1 = Map.of(PROP_1, 1.0);
		Map<String, Object> m2 = Map.of(PROP_1, (double) 1);

		// THEN
		then(CollectionUtils.differsNumerically(m1, m2)).as("Property values are comparably equal")
				.isFalse();
	}

	@Test
	public void differsNumerically_comparableValue_misMatchTypes() {
		// GIVEN
		Map<String, Object> m1 = Map.of(PROP_1, 1.0);
		Map<String, Object> m2 = Map.of(PROP_1, 1.0f);

		// THEN
		then(CollectionUtils.differsNumerically(m1, m2)).as("Property values of different types differ")
				.isTrue();
	}

	@Test
	public void differsNumerically_comparableValue_misMatchTypes_mapped() {
		// GIVEN
		Map<String, Object> m1 = Map.of(PROP_1, 1);
		Map<String, Object> m2 = Map.of(PROP_1, 1f);

		// THEN
		then(CollectionUtils.differsNumerically(m1, m2, StringUtils::numberValue,
				NumberUtils::bigDecimalForNumber)).as(
						"Property values of different types mapped to BigDecimal are comparably equal")
						.isFalse();
	}

	@Test
	public void differsNumerically_equalValue() {
		// GIVEN
		Map<String, Object> m1 = Map.of(PROP_1, 1);
		Map<String, Object> m2 = Map.of(PROP_1, 1);

		// THEN
		then(CollectionUtils.differsNumerically(m1, m2)).as("Property value equal").isFalse();
	}

	@Test
	public void differsNumerically_oneNullMap() {
		// GIVEN
		Map<String, Object> m1 = Map.of(PROP_1, 1);
		Map<String, Object> m2 = Map.of();

		// THEN
		then(CollectionUtils.differsNumerically(m1, m2)).as("One null one not differ").isTrue();
		then(CollectionUtils.differsNumerically(m2, m1)).as("One null one not differ (reverse)")
				.isTrue();
	}

	@Test
	public void differsNumerically_oneNullOneEmptyMap() {
		// GIVEN
		Map<String, Object> m1 = Map.of();
		Map<String, Object> m2 = null;

		// THEN
		then(CollectionUtils.differsNumerically(m1, m2)).as("One null one empty equal").isFalse();
		then(CollectionUtils.differsNumerically(m2, m1)).as("One null one empty equal (reverse)")
				.isFalse();
	}

	@Test
	public void differsNumerically_withEqualMulti() {
		// GIVEN
		Map<String, Object> m1 = Map.of(PROP_1, 1, PROP_2, 2);
		Map<String, Object> m2 = Map.of(PROP_1, 1, PROP_2, 2);

		// THEN
		then(CollectionUtils.differsNumerically(m1, m2)).as("All properties equal").isFalse();
	}

	@Test
	public void differsNumerically_withDifferentMulti() {
		// GIVEN
		Map<String, Object> m1 = Map.of(PROP_1, 1, PROP_2, 2);
		Map<String, Object> m2 = Map.of(PROP_1, 1, PROP_2, 22);

		// THEN
		then(CollectionUtils.differsNumerically(m1, m2)).as("Different prop values differ").isTrue();
	}

	@Test
	public void differsNumerically_withEqualStrings() {
		// GIVEN
		Map<String, Object> m1 = Map.of(PROP_1, 1, PROP_3, "3");
		Map<String, Object> m2 = Map.of(PROP_1, 1, PROP_3, "3");

		// THEN
		then(CollectionUtils.differsNumerically(m1, m2)).as("Status values parsed as numbers are equal")
				.isFalse();
	}

	@Test
	public void differsNumerically_withDifferentStrings() {
		// GIVEN
		Map<String, Object> m1 = Map.of(PROP_1, 1, PROP_3, "3");
		Map<String, Object> m2 = Map.of(PROP_1, 1, PROP_3, "33");

		// THEN
		then(CollectionUtils.differsNumerically(m1, m2)).as("Status values parsed as numbers differ")
				.isTrue();
	}

	@Test
	public void differsNumerically_withDifferentStrings_oneNotNumber() {
		// GIVEN
		Map<String, Object> m1 = Map.of(PROP_1, 1, PROP_3, "3");
		Map<String, Object> m2 = Map.of(PROP_1, 1, PROP_3, "3a");

		// THEN
		then(CollectionUtils.differsNumerically(m1, m2))
				.as("Status values parsed as numbers, one NaN, differ").isTrue();
	}

	@Test
	public void differsNumerically_withDifferentNonNumberStrings() {
		// GIVEN
		Map<String, Object> m1 = Map.of(PROP_1, "a");
		Map<String, Object> m2 = Map.of(PROP_1, "b");

		// THEN
		then(CollectionUtils.differsNumerically(m1, m2)).as("Non-number values differ").isTrue();
		then(CollectionUtils.differsNumerically(m1, Map.of(PROP_1, "a"))).as("Non-number values equal")
				.isFalse();
	}

	@Test
	public void differsNumerically_mapped_stringAndLong() {
		// GIVEN
		Map<String, Object> m1 = Map.of(PROP_1, "1", PROP_2, "1000");
		Map<String, Object> m2 = Map.of(PROP_1, 1L, PROP_2, 1000L);

		// THEN
		then(CollectionUtils.differsNumerically(m1, m2, StringUtils::numberValue, NumberUtils::narrow32))
				.as("String and Long values narrowed to equal Integer values").isFalse();
	}

	@Test
	public void differsNumerically_mapped_intAndLong() {
		// GIVEN
		Map<String, Object> m1 = Map.of(PROP_1, 1, PROP_2, 1000);
		Map<String, Object> m2 = Map.of(PROP_1, 1L, PROP_2, 1000L);

		// THEN
		then(CollectionUtils.differsNumerically(m1, m2, StringUtils::numberValue, NumberUtils::narrow32))
				.as("Integer and Long values narrowed to equal Integer values").isFalse();
	}

	@Test
	public void mapProperty_shortAndByte() {
		// GIVEN
		final Short aDefault = (short) -1;
		// @formatter:off
		final Map<String, Object> m = Map.ofEntries(
				entry("short", (short) 1),
				entry("int", 2),
				entry("string-num", "3"),
				entry("string-invalid", "this is foobar")
			);
		// @formatter:on

		// THEN
		// @formatter:off
		then(CollectionUtils.mapProperty("short", Short.class, aDefault, m))
			.as("Short instance returned directly")
			.isEqualTo((short) 1)
			;
		then(CollectionUtils.mapProperty("int", Short.class, aDefault, m))
			.as("Integer converted to Short")
			.isEqualTo((short) 2)
			;
		then(CollectionUtils.mapProperty("string-num", Short.class, aDefault, m))
			.as("String number parsed as Short")
			.isEqualTo((short) 3)
			;
		then(CollectionUtils.mapProperty("string-invalid", Short.class, aDefault, m))
			.as("Invalid string returns default value")
			.isSameAs(aDefault)
			;
		then(CollectionUtils.mapProperty("string-num", Byte.class, null, m))
			.as("String number parsed as Byte")
			.isEqualTo((byte) 3)
			;
		// @formatter:on
	}

	@Test
	public void mapPropertyStringList_emptyCollection() {
		// GIVEN
		// @formatter:off
		final Map<String, Object> m = Map.ofEntries(
				entry("list", List.of()),
				entry("set", Set.of())
			);
		// @formatter:on

		// THEN
		// @formatter:off
		then(CollectionUtils.mapPropertyStringList("list", m))
			.as("Empty list resolves to empty list")
			.isEmpty()
			;
		then(CollectionUtils.mapPropertyStringList("set", m))
			.as("Empty set resolves to empty list")
			.isEmpty()
			;
		// @formatter:on
	}

	@Test
	public void mapPropertyStringList_set() {
		// GIVEN
		// @formatter:off
		final Map<String, Object> m = Map.ofEntries(
				entry("strings", new LinkedHashSet<>(List.of("x", "y"))),
				entry("numbers", new LinkedHashSet<>(List.of(1, 2)))
			);
		// @formatter:on

		// THEN
		// @formatter:off
		then(CollectionUtils.mapPropertyStringList("strings", m))
			.as("Set values resolved in iteration order")
			.containsExactly("x", "y")
			;
		then(CollectionUtils.mapPropertyStringList("numbers", m))
			.as("Set values converted to strings")
			.containsExactly("1", "2")
			;
		// @formatter:on
	}

	@Test
	public void mapPropertyStringList_stringList() {
		// GIVEN
		final List<String> list = List.of("a", "b");

		// THEN
		// @formatter:off
		then(CollectionUtils.mapPropertyStringList("list", Map.of("list", list)))
			.as("String list returned as-is")
			.isSameAs(list)
			;
		// @formatter:on
	}

	@Test
	public void mapPropertyStringList_mixedList() {
		// GIVEN
		final List<Object> middle = new ArrayList<>(List.of("a", 1, "b"));
		// @formatter:off
		final Map<String, Object> m = Map.ofEntries(
				entry("middle", middle),
				entry("last", List.of("a", "b", 2)),
				entry("several", List.of("a", 1, 2.5, "b", 3L))
			);
		// @formatter:on

		// THEN
		// @formatter:off
		then(CollectionUtils.mapPropertyStringList("middle", m))
			.as("Non-string value within list converted to string")
			.containsExactly("a", "1", "b")
			;
		then(middle)
			.as("Source list not modified")
			.containsExactly("a", 1, "b")
			;
		then(CollectionUtils.mapPropertyStringList("last", m))
			.as("Non-string value at end of list converted to string")
			.containsExactly("a", "b", "2")
			;
		then(CollectionUtils.mapPropertyStringList("several", m))
			.as("All non-string values converted to strings")
			.containsExactly("a", "1", "2.5", "b", "3")
			;
		// @formatter:on
	}

	@Test
	public void mapPropertyStringList_nulls() {
		// GIVEN
		// @formatter:off
		final Map<String, Object> m = Map.ofEntries(
				entry("middle", Arrays.asList("a", null, "b")),
				entry("last", Arrays.asList("a", "b", null)),
				entry("afterCopy", Arrays.asList("a", 1, null, "b")),
				entry("mixed", Arrays.asList("a", null, 1, null, "b")),
				entry("leading", Arrays.asList(null, "a", 1)),
				entry("allNull", Arrays.asList(null, null)),
				entry("stringArray", new String[] { "a", null, "b" })
			);
		// @formatter:on

		// THEN
		// @formatter:off
		then(CollectionUtils.mapPropertyStringList("middle", m))
			.as("Null within string list skipped")
			.containsExactly("a", "b")
			;
		then(CollectionUtils.mapPropertyStringList("last", m))
			.as("Null at end of string list skipped")
			.containsExactly("a", "b")
			;
		then(CollectionUtils.mapPropertyStringList("afterCopy", m))
			.as("Null after non-string value skipped")
			.containsExactly("a", "1", "b")
			;
		then(CollectionUtils.mapPropertyStringList("mixed", m))
			.as("All nulls skipped and non-string values converted to strings")
			.containsExactly("a", "1", "b")
			;
		then(CollectionUtils.mapPropertyStringList("leading", m))
			.as("Leading null skipped")
			.containsExactly("a", "1")
			;
		then(CollectionUtils.mapPropertyStringList("allNull", m))
			.as("List of only nulls resolves to empty list")
			.isEmpty()
			;
		then(CollectionUtils.mapPropertyStringList("stringArray", m))
			.as("Null within string array skipped")
			.containsExactly("a", "b")
			;
		// @formatter:on
	}

	@Test
	public void mapPropertyDuration() {
		// GIVEN
		final Duration aDefault = Duration.ofSeconds(1);
		final Duration anInstance = Duration.ofHours(1);
		final Long aLong = 1234567890123456789L;
		final String aStringNum = "12345";
		final String aStringInvalid = "this is foobar";
		// @formatter:off
		final Map<String, Object> m = Map.ofEntries(
				entry("long", aLong),
				entry("string-num", aStringNum),
				entry("string-empty", ""),
				entry("string-invalid", aStringInvalid),
				entry("inst", anInstance),
				entry("string", anInstance.toString())
			);
		// @formatter:on

		// THEN
		// @formatter:off
		then(CollectionUtils.mapPropertyDuration(null, aDefault, m))
			.as("Null key returns default")
			.isSameAs(aDefault)
			;
		then(CollectionUtils.mapPropertyDuration("long", aDefault, null))
			.as("Null map returns default")
			.isSameAs(aDefault)
			;
		then(CollectionUtils.mapPropertyDuration("long", aDefault, m))
			.as("Number instance parsed as seconds")
			.isEqualTo(Duration.ofSeconds(aLong))
			;
		then(CollectionUtils.mapPropertyDuration("string-num", aDefault, m))
			.as("String number parsed as seconds")
			.isEqualTo(Duration.ofSeconds(Long.valueOf(aStringNum)))
			;
		then(CollectionUtils.mapPropertyDuration("string-empty", aDefault, m))
			.as("Empty string value returns default value")
			.isSameAs(aDefault)
			;
		then(CollectionUtils.mapPropertyDuration("string-invalid", aDefault, m))
			.as("Invalid string returns default value")
			.isSameAs(aDefault)
			;
		then(CollectionUtils.mapPropertyDuration("key does not exist", aDefault, m))
			.as("Non-existing key returns default value")
			.isSameAs(aDefault)
			;
		then(CollectionUtils.mapPropertyDuration("inst", aDefault, m))
			.as("Instance returned directly")
			.isSameAs(anInstance)
			;
		then(CollectionUtils.mapPropertyDuration("string", aDefault, m))
			.as("String parsed as instance")
			.isEqualTo(anInstance)
			;
		// @formatter:on
	}

	@Test
	public void mapPropertyInstant() {
		// GIVEN
		final Instant aDefault = Instant.now().truncatedTo(ChronoUnit.HOURS).minusSeconds(1);
		final Instant anInsance = Instant.now();
		final Long aLong = 1234567890123456L;
		final String aStringNum = "1234567890123456";
		final String aStringInvalid = "this is foobar";
		// @formatter:off
		final Map<String, Object> m = Map.ofEntries(
				entry("long", aLong),
				entry("string-num", aStringNum),
				entry("string-empty", ""),
				entry("string-invalid", aStringInvalid),
				entry("inst", anInsance),
				entry("string", anInsance.toString())
			);
		// @formatter:on

		// THEN
		// @formatter:off
		then(CollectionUtils.mapPropertyTimestamp(null, aDefault, m))
			.as("Null key returns default")
			.isSameAs(aDefault)
			;
		then(CollectionUtils.mapPropertyTimestamp("long", aDefault, null))
			.as("Null map returns default")
			.isSameAs(aDefault)
			;
		then(CollectionUtils.mapPropertyTimestamp("long", aDefault, m))
			.as("Number instance parsed as millisecond epoch")
			.isEqualTo(Instant.ofEpochMilli(aLong))
			;
		then(CollectionUtils.mapPropertyTimestamp("string-num", aDefault, m))
			.as("String number parsed as millisecond epoch")
			.isEqualTo(Instant.ofEpochMilli(Long.valueOf(aStringNum)))
			;
		then(CollectionUtils.mapPropertyTimestamp("string-empty", aDefault, m))
			.as("Empty string value returns default value")
			.isSameAs(aDefault)
			;
		then(CollectionUtils.mapPropertyTimestamp("string-invalid", aDefault, m))
			.as("Invalid string returns default value")
			.isSameAs(aDefault)
			;
		then(CollectionUtils.mapPropertyTimestamp("key does not exist", aDefault, m))
			.as("Non-existing key returns default value")
			.isSameAs(aDefault)
			;
		then(CollectionUtils.mapPropertyTimestamp("inst", aDefault, m))
			.as("Instance returned directly")
			.isSameAs(anInsance)
			;
		then(CollectionUtils.mapPropertyTimestamp("string", aDefault, m))
			.as("String parsed as instance")
			.isEqualTo(anInsance)
			;
		// @formatter:on
	}

	@Test
	public void servicePropertyNumber() {
		// GIVEN
		final Number aDefault = new BigDecimal("1.234");
		final Float anInstance = randomFloat();
		final Long aLong = randomLong();
		final String aStringNum = String.valueOf(randomLong());
		final String aStringInvalid = "this is foobar";
		final String aLargeInteger = "987654321098765432109876543210987654321";
		final String aLargeDecimal = "987654321098765432109876543210.987654321";
		// @formatter:off
		final Map<String, Object> m = Map.ofEntries(
				entry("long", aLong),
				entry("string-num", aStringNum),
				entry("string-empty", ""),
				entry("string-invalid", aStringInvalid),
				entry("inst", anInstance),
				entry("string", anInstance.toString()),
				entry("large-integer", aLargeInteger),
				entry("large-decimal", aLargeDecimal)
			);
		// @formatter:on

		// THEN
		// @formatter:off
		then(CollectionUtils.mapPropertyNumber(null, aDefault, m))
			.as("Null key returns default")
			.isSameAs(aDefault)
			;
		then(CollectionUtils.mapPropertyNumber("long", aDefault, null))
			.as("Null map returns default")
			.isSameAs(aDefault)
			;
		then(CollectionUtils.mapPropertyNumber("long", aDefault, m))
			.as("Number instance parsed as millisecond epoch")
			.isEqualTo(aLong)
			;
		then(CollectionUtils.mapPropertyNumber("string-num", aDefault, m))
			.as("String number parsed as number")
			.isEqualTo(Long.valueOf(aStringNum))
			;
		then(CollectionUtils.mapPropertyNumber("string-empty", aDefault, m))
			.as("Empty string value returns default value")
			.isSameAs(aDefault)
			;
		then(CollectionUtils.mapPropertyNumber("string-invalid", aDefault, m))
			.as("Invalid string returns default value")
			.isSameAs(aDefault)
			;
		then(CollectionUtils.mapPropertyNumber("key does not exist", aDefault, m))
			.as("Non-existing key returns default value")
			.isSameAs(aDefault)
			;
		then(CollectionUtils.mapPropertyNumber("inst", aDefault, m))
			.as("Instance returned directly")
			.isSameAs(anInstance)
			;
		then(CollectionUtils.mapPropertyNumber("string", aDefault, m))
			.as("String parsed as instance")
			.isEqualTo(anInstance)
			;
		then(CollectionUtils.mapPropertyNumber("large-integer", aDefault, m))
			.as("Large integer returned as BigInteger (not narrowed to 32-bit)")
			.isEqualTo(new BigInteger(aLargeInteger))
			;
		then(CollectionUtils.mapPropertyNumber("large-decimal", aDefault, m))
			.as("Large decimal returned as BigDecimal (not narrowed to 32-bit)")
			.isEqualTo(new BigDecimal(aLargeDecimal))
			;
		// @formatter:on
	}

	@Test
	public void coveringIntRanges_null() {
		then(CollectionUtils.coveringIntRanges(null, 1)).as("Null input").isNull();
	}

	@Test
	public void dictionaryForMap() {
		// GIVEN
		Map<String, Integer> map = Map.of("a", 1, "b", 2);

		// WHEN
		Dictionary<String, Integer> result = CollectionUtils.dictionaryForMap(map);

		// THEN
		then(result).as("Dictionary created").isNotNull();
		then(CollectionUtils.mapForDictionary(result)).as("Dictionary has all map entries")
				.isEqualTo(map);
		then(CollectionUtils.dictionaryForMap(null)).as("Null input").isNull();
	}

	/** Map with values of various types for testing the getMap* methods. */
	private static final Map<String, Object> GET_MAP_DATA = Map.of("short", (short) 1, "int", 2, "long",
			3L, "float", 4.5f, "double", 5.5, "decimal", new BigDecimal("6.5"), "bigint",
			BigInteger.valueOf(7), "str", "8", "decstr", "8.5", "bad", "not a number");

	@Test
	public void getMapShort() {
		then(CollectionUtils.getMapShort("short", null)).as("Null map").isNull();
		then(CollectionUtils.getMapShort("nope", GET_MAP_DATA)).as("Missing key").isNull();
		then(CollectionUtils.getMapShort("short", GET_MAP_DATA)).as("Short").isEqualTo((short) 1);
		then(CollectionUtils.getMapShort("int", GET_MAP_DATA)).as("Other number").isEqualTo((short) 2);
		then(CollectionUtils.getMapShort("str", GET_MAP_DATA)).as("Parsed string").isEqualTo((short) 8);
		then(CollectionUtils.getMapShort("bad", GET_MAP_DATA)).as("Invalid string").isNull();
	}

	@Test
	public void getMapInteger() {
		then(CollectionUtils.getMapInteger("int", GET_MAP_DATA)).as("Integer").isEqualTo(2);
		then(CollectionUtils.getMapInteger("long", GET_MAP_DATA)).as("Other number").isEqualTo(3);
		then(CollectionUtils.getMapInteger("str", GET_MAP_DATA)).as("Parsed string").isEqualTo(8);
		then(CollectionUtils.getMapInteger("bad", GET_MAP_DATA)).as("Invalid string").isNull();
	}

	@Test
	public void getMapLong() {
		then(CollectionUtils.getMapLong("long", GET_MAP_DATA)).as("Long").isEqualTo(3L);
		then(CollectionUtils.getMapLong("int", GET_MAP_DATA)).as("Other number").isEqualTo(2L);
		then(CollectionUtils.getMapLong("str", GET_MAP_DATA)).as("Parsed string").isEqualTo(8L);
		then(CollectionUtils.getMapLong("bad", GET_MAP_DATA)).as("Invalid string").isNull();
	}

	@Test
	public void getMapFloat() {
		then(CollectionUtils.getMapFloat("float", null)).as("Null map").isNull();
		then(CollectionUtils.getMapFloat("nope", GET_MAP_DATA)).as("Missing key").isNull();
		then(CollectionUtils.getMapFloat("float", GET_MAP_DATA)).as("Float").isEqualTo(4.5f);
		then(CollectionUtils.getMapFloat("double", GET_MAP_DATA)).as("Other number").isEqualTo(5.5f);
		then(CollectionUtils.getMapFloat("decstr", GET_MAP_DATA)).as("Parsed string").isEqualTo(8.5f);
		then(CollectionUtils.getMapFloat("bad", GET_MAP_DATA)).as("Invalid string").isNull();
	}

	@Test
	public void getMapDouble() {
		then(CollectionUtils.getMapDouble("double", null)).as("Null map").isNull();
		then(CollectionUtils.getMapDouble("nope", GET_MAP_DATA)).as("Missing key").isNull();
		then(CollectionUtils.getMapDouble("double", GET_MAP_DATA)).as("Double").isEqualTo(5.5);
		then(CollectionUtils.getMapDouble("float", GET_MAP_DATA)).as("Other number").isEqualTo(4.5);
		then(CollectionUtils.getMapDouble("decstr", GET_MAP_DATA)).as("Parsed string").isEqualTo(8.5);
		then(CollectionUtils.getMapDouble("bad", GET_MAP_DATA)).as("Invalid string").isNull();
	}

	@Test
	public void getMapBigDecimal() {
		then(CollectionUtils.getMapBigDecimal("decimal", null)).as("Null map").isNull();
		then(CollectionUtils.getMapBigDecimal("decstr", GET_MAP_DATA)).as("Parsed string")
				.isEqualTo(new BigDecimal("8.5"));
		then(CollectionUtils.getMapBigDecimal("bad", GET_MAP_DATA)).as("Invalid string").isNull();
	}

	@Test
	public void getMapBigInteger() {
		then(CollectionUtils.getMapBigInteger("bigint", null)).as("Null map").isNull();
		then(CollectionUtils.getMapBigInteger("nope", GET_MAP_DATA)).as("Missing key").isNull();
		then(CollectionUtils.getMapBigInteger("bigint", GET_MAP_DATA)).as("BigInteger")
				.isEqualTo(BigInteger.valueOf(7));
		then(CollectionUtils.getMapBigInteger("decimal", GET_MAP_DATA)).as("Other number")
				.isEqualTo(BigInteger.valueOf(6));
		then(CollectionUtils.getMapBigInteger("str", GET_MAP_DATA)).as("Parsed string")
				.isEqualTo(BigInteger.valueOf(8));
		then(CollectionUtils.getMapBigInteger("bad", GET_MAP_DATA)).as("Invalid string").isNull();
	}

	@Test
	public void mapProperty_defaults() {
		final Integer def = -1;
		then(CollectionUtils.mapProperty(null, Integer.class, def, GET_MAP_DATA)).as("Null key")
				.isEqualTo(def);
		then(CollectionUtils.mapProperty("int", Integer.class, def, null)).as("Null map").isEqualTo(def);
		then(CollectionUtils.mapProperty("nope", Integer.class, def, GET_MAP_DATA)).as("Missing key")
				.isEqualTo(def);
		then(CollectionUtils.mapProperty("bad", Integer.class, def, GET_MAP_DATA))
				.as("Unparsable number").isEqualTo(def);
		then(CollectionUtils.mapProperty("int", Instant.class, Instant.EPOCH, GET_MAP_DATA))
				.as("Unsupported conversion").isEqualTo(Instant.EPOCH);
	}

	@Test
	public void mapProperty_strings() {
		then(CollectionUtils.mapProperty("int", String.class, "def", GET_MAP_DATA))
				.as("Value converted to string").isEqualTo("2");
		then(CollectionUtils.mapProperty("e", String.class, "def", Map.of("e", "")))
				.as("Empty string is null, not default").isNull();
		then(CollectionUtils.mapProperty("str", Integer.class, null, GET_MAP_DATA))
				.as("String parsed as number").isEqualTo(8);
	}

	@Test
	public void mapPropertyStringMap() {
		then(CollectionUtils.mapPropertyStringMap(null, Map.of("a", "b=c"))).as("Null key").isNull();
		then(CollectionUtils.mapPropertyStringMap("a", null)).as("Null map").isNull();
		then(CollectionUtils.mapPropertyStringMap("a", Map.of())).as("Missing key").isNull();
		then(CollectionUtils.mapPropertyStringMap("a", Map.of("a", "b=c,d=e")))
				.as("String parsed as map").containsOnly(entry("b", "c"), entry("d", "e"));
		then(CollectionUtils.mapPropertyStringMap("a", Map.of("a", Map.of("b", "c")))).as("Map returned")
				.containsOnly(entry("b", "c"));
	}

	@Test
	public void mapPropertyStringList_types() {
		then(CollectionUtils.mapPropertyStringList(null, Map.of("a", "b"))).as("Null key").isNull();
		then(CollectionUtils.mapPropertyStringList("a", null)).as("Null map").isNull();
		then(CollectionUtils.mapPropertyStringList("a", Map.of())).as("Missing key").isNull();
		then(CollectionUtils.mapPropertyStringList("a", Map.of("a", List.of("b", "c"))))
				.as("String list").containsExactly("b", "c");
		then(CollectionUtils.mapPropertyStringList("a", Map.of("a", List.of(1, 2))))
				.as("Non-string list converted").containsExactly("1", "2");
		then(CollectionUtils.mapPropertyStringList("a", Map.of("a", new String[] { "b", "c" })))
				.as("String array").containsExactly("b", "c");
		then(CollectionUtils.mapPropertyStringList("a", Map.of("a", new Object[] { 1, null, "c" })))
				.as("Object array, nulls skipped").containsExactly("1", "c");
		then(CollectionUtils.mapPropertyStringList("a", Map.of("a", "b, c"))).as("Delimited string")
				.containsExactly("b", "c");
	}

	@Test
	public void mapPropertyNumber_noParser() {
		then(CollectionUtils.mapPropertyNumber("a", 5, null, null, Map.of("a", "1")))
				.as("String not parsed without parser").isEqualTo(5);
		then(CollectionUtils.mapPropertyNumber("a", 5, null, null, Map.of("a", 1)))
				.as("Number returned without mapper").isEqualTo(1);
		then(CollectionUtils.mapPropertyNumber("a", 5, null, n -> null, Map.of("a", 1)))
				.as("Default returned when mapper returns null").isEqualTo(5);
	}

	@Test
	public void valuesMatching_skipNullAndEmpty() {
		// GIVEN
		Set<String> set = new HashSet<>(asList(null, "", "foo", "bar"));

		// WHEN
		Set<String> result = CollectionUtils.valuesMatching(set, Pattern.compile("o"));

		// THEN
		then(result).as("Null and empty values skipped").containsOnly("foo");
	}

	@Test
	public void filteredSubset_nullSubset() {
		// GIVEN
		Set<String> superSet = Set.of("a", "b");

		// WHEN
		Set<String> result = CollectionUtils.filteredSubset(superSet, null, HashSet::new);

		// THEN
		then(result).as("Null subset returns superset").isSameAs(superSet);
	}

	@Test
	public void sort_props_nullsAndEqual() {
		// GIVEN
		Map<String, Object> m1 = new HashMap<>(Map.of("a", 2, "id", 1));
		Map<String, Object> m2 = new HashMap<>(Map.of("id", 2));
		Map<String, Object> m3 = new HashMap<>(Map.of("a", 1, "id", 3));
		Map<String, Object> m4 = new HashMap<>(Map.of("a", 2, "id", 4));

		// WHEN
		Collection<Map<String, Object>> result = CollectionUtils.sort(asList(m1, m2, m3, m4), "a");

		// THEN
		then(result).as("Null values first, equal values keep their order").containsExactly(m2, m3, m1,
				m4);
	}

	@Test
	public void sort_props_nullLast() {
		// GIVEN
		Map<String, Object> m1 = new HashMap<>(Map.of("a", 2));
		Map<String, Object> m2 = new HashMap<>();

		// WHEN
		Collection<Map<String, Object>> result = CollectionUtils.sort(asList(m1, m2), "a");

		// THEN
		then(result).as("Null value sorted before non-null").containsExactly(m2, m1);
	}

	@Test
	public void transformMapValues_nothingToTransform() {
		final Map<String, String> map = Map.of("a", "1");
		then(CollectionUtils.transformMap((Map<String, String>) null, Set.of("a"), v -> v))
				.as("Null map").isNull();
		then(CollectionUtils.transformMap(Map.<String, String> of(), Set.of("a"), v -> v))
				.as("Empty map").isEmpty();
		then(CollectionUtils.transformMap(map, null, v -> v)).as("Null keys").isSameAs(map);
		then(CollectionUtils.transformMap(map, Set.<String> of(), v -> v)).as("Empty keys")
				.isSameAs(map);
		then(CollectionUtils.transformMap(map, Set.of("b"), v -> v)).as("Key not in map").isSameAs(map);
	}

	@Test
	public void transformMap_skipNullKeysAndErrors() {
		// GIVEN
		Map<String, String> map = new LinkedHashMap<>();
		map.put("a", "1");
		map.put("b", "2");
		map.put("c", "not a number");

		// WHEN
		Map<String, Integer> result = CollectionUtils.transformMap(map, null,
				(k, v) -> "b".equals(k) ? null : k.toUpperCase(), (k, v) -> Integer.valueOf(v));

		// THEN
		then(result).as("Null keys and transform errors skipped").containsOnly(entry("A", 1));
	}

	@Test
	public void transformMap_emptyResult() {
		// WHEN
		Map<String, String> result = CollectionUtils.transformMap(Map.of("a", "1"), null, (k, v) -> null,
				(k, v) -> v);

		// THEN
		then(result).as("Empty result returned as null").isNull();
	}

	@Test
	public void differsNumerically_sameSizeDifferentKeys() {
		then(CollectionUtils.differsNumerically(Map.of("a", 1), Map.of("b", 1)))
				.as("Different keys differ").isTrue();
	}

	@Test
	public void differsNumerically_nullValues() {
		// GIVEN
		Map<String, Object> m1 = new HashMap<>();
		m1.put("a", null);
		Map<String, Object> m2 = new HashMap<>();
		m2.put("a", null);

		// THEN
		then(CollectionUtils.differsNumerically(m1, m2)).as("Both null values equal").isFalse();
		then(CollectionUtils.differsNumerically(m1, Map.of("a", 1))).as("Null and number differ")
				.isTrue();
		then(CollectionUtils.differsNumerically(Map.of("a", 1), m1)).as("Number and null differ")
				.isTrue();
	}

	@Test
	public void differsNumerically_noParser() {
		then(CollectionUtils.differsNumerically(Map.of("a", "x"), Map.of("a", "x"), null, null))
				.as("Equal strings without parser").isFalse();
		then(CollectionUtils.differsNumerically(Map.of("a", "1"), Map.of("a", "2"), null, null))
				.as("Different strings without parser").isTrue();
		then(CollectionUtils.differsNumerically(Map.of("a", "1"), Map.of("a", 1), null, null))
				.as("String and number without parser").isTrue();
	}

	/** A nested map for testing the valueAtPath methods. */
	private static final Map<String, Object> PATH_DATA = Map.of("a",
			Map.of("b", Map.of("c", 1), "e", Map.of()), "s", "str");

	@Test
	public void valueAtPath_keys() {
		then(CollectionUtils.valueAtPath(new String[] { "a", "b", "c" }, PATH_DATA)).as("Nested value")
				.isEqualTo(1);
		then(CollectionUtils.valueAtPath(new String[] { "", "a", "", "b", "c" }, PATH_DATA))
				.as("Empty keys skipped").isEqualTo(1);
		then(CollectionUtils.valueAtPath(new String[] { "x", "a" }, 1, PATH_DATA))
				.as("Starting index skips prefix keys")
				.isEqualTo(Map.of("b", Map.of("c", 1), "e", Map.of()));
		then(CollectionUtils.valueAtPath(new String[] { "a" }, 1, PATH_DATA))
				.as("Starting index past end returns data").isSameAs(PATH_DATA);
		then(CollectionUtils.valueAtPath(new String[] { "s", "x" }, PATH_DATA))
				.as("Path through non-map value").isNull();
		then(CollectionUtils.valueAtPath(new String[] { "a", "e", "x" }, PATH_DATA))
				.as("Path through empty map").isNull();
	}

	@Test
	public void valueAtPath_keys_noKeysOrData() {
		then(CollectionUtils.valueAtPath((String[]) null, PATH_DATA)).as("Null keys").isNull();
		then(CollectionUtils.valueAtPath(new String[0], PATH_DATA)).as("Empty keys").isNull();
		then(CollectionUtils.valueAtPath(new String[] { "a" }, null)).as("Null data").isNull();
		then(CollectionUtils.valueAtPath(new String[] { "a" }, Map.of())).as("Empty data").isNull();
	}

	@Test
	public void valueAtPath_keys_type() {
		then(CollectionUtils.valueAtPath(new String[] { "a", "b", "c" }, PATH_DATA, Integer.class))
				.as("Value of matching type").isEqualTo(1);
		then(CollectionUtils.valueAtPath(new String[] { "s" }, PATH_DATA, Integer.class))
				.as("Value of other type").isNull();
		then(CollectionUtils.valueAtPath(new String[] { "x" }, 0, PATH_DATA, Integer.class))
				.as("Missing value").isNull();
	}

}
