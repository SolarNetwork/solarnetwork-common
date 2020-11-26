/* ==================================================================
 * SearchFilterTests.java - Apr 22, 2014 8:30:36 AM
 * 
 * Copyright 2007-2014 SolarNetwork.net Dev Team
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

package net.solarnetwork.support.test;

import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import net.solarnetwork.support.SearchFilter;
import net.solarnetwork.support.SearchFilter.CompareOperator;
import net.solarnetwork.support.SearchFilter.LogicOperator;
import net.solarnetwork.support.SearchFilter.VisitorCallback;

/**
 * Test cases for the {@link SearchFilter} class.
 * 
 * @author matt
 * @version 1.0
 */
public class SearchFilterTests {

	private void assertComparisonSearchFilter(String prefix, SearchFilter result, String key,
			CompareOperator op, String value) {
		assertThat(prefix + " op", result.getCompareOperator(), equalTo(op));
		assertThat(prefix + " has 1 key-value pair", result.getFilter().size(), equalTo(1));
		String k = result.getFilter().keySet().iterator().next();
		assertThat(prefix + " key", k, equalTo(key));
		assertThat(prefix + " value", result.getFilter().get(k), equalTo(value));
	}

	@Test
	public void nullFilter() {
		SearchFilter f = new SearchFilter(null);
		String result = f.asLDAPSearchFilterString();
		Assert.assertEquals("", result);
	}

	@Test
	public void parse_nullFilter() {
		SearchFilter f = SearchFilter.forLDAPSearchFilterString(null);
		assertThat("Filter not parsed", f, nullValue());
	}

	@Test
	public void emptyFilter() {
		SearchFilter f = new SearchFilter(new HashMap<String, Object>());
		String result = f.asLDAPSearchFilterString();
		Assert.assertEquals("", result);
	}

	@Test
	public void parse_emptyFilter() {
		SearchFilter f = SearchFilter.forLDAPSearchFilterString("");
		assertThat("Filter not parsed", f, nullValue());
	}

	@Test
	public void defaultSingleFilter() {
		Map<String, String> m = Collections.singletonMap("foo", "bar");
		SearchFilter f = new SearchFilter(m);
		String result = f.asLDAPSearchFilterString();
		Assert.assertEquals("(foo=bar)", result);
	}

	@Test
	public void parse_defaultSingleFilter() {
		SearchFilter f = SearchFilter.forLDAPSearchFilterString("(foo=bar)");
		assertThat("Filter parsed", f, notNullValue());
		assertThat("Logic is AND", f.getLogicOperator(), equalTo(LogicOperator.AND));

		assertComparisonSearchFilter("1", f, "foo", CompareOperator.EQUAL, "bar");
	}

	@Test
	public void notEqualSingleFilter() {
		Map<String, String> m = Collections.singletonMap("foo", "bar");
		SearchFilter f = new SearchFilter(m, CompareOperator.EQUAL, LogicOperator.NOT);
		String result = f.asLDAPSearchFilterString();
		Assert.assertEquals("(!(foo=bar))", result);
	}

	@Test
	public void parse_notEqualSingleFilter() {
		SearchFilter f = SearchFilter.forLDAPSearchFilterString("(!(foo=bar))");
		assertThat("Filter parsed", f, notNullValue());
		assertThat("Logic is NOT", f.getLogicOperator(), equalTo(LogicOperator.NOT));

		Map<String, ?> filter = f.getFilter();
		assertThat("Filter has 1 nested SearchFilter objects", filter.values(),
				contains(instanceOf(SearchFilter.class)));
		List<SearchFilter> nested = filter.values().stream().map(SearchFilter.class::cast)
				.collect(Collectors.toList());

		assertComparisonSearchFilter("1", nested.get(0), "foo", CompareOperator.EQUAL, "bar");
	}

	@Test
	public void defaultDoubleFilter() {
		Map<String, Object> m = new LinkedHashMap<String, Object>();
		m.put("foo", "bar");
		m.put("bar", 1);
		SearchFilter f = new SearchFilter(m);
		String result = f.asLDAPSearchFilterString();
		Assert.assertEquals("(&(foo=bar)(bar=1))", result);
	}

	@Test
	public void notEqualDoubleFilter() {
		Map<String, Object> m = new LinkedHashMap<String, Object>();
		m.put("foo", "bar");
		m.put("bar", 1);
		SearchFilter f = new SearchFilter(m, CompareOperator.EQUAL, LogicOperator.NOT);
		String result = f.asLDAPSearchFilterString();
		Assert.assertEquals("(!(&(foo=bar)(bar=1)))", result);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void parse_notEqualDoubleFilter() {
		SearchFilter f = SearchFilter.forLDAPSearchFilterString("(!(&(foo=bar)(bar=1)))");
		assertThat("Filter parsed", f, notNullValue());
		assertThat("Logic is NOT", f.getLogicOperator(), equalTo(LogicOperator.NOT));

		Map<String, ?> filter = f.getFilter();
		assertThat("Filter has 1 nested SearchFilter objects", filter.values(),
				contains(instanceOf(SearchFilter.class)));
		List<SearchFilter> nested = filter.values().stream().map(SearchFilter.class::cast)
				.collect(Collectors.toList());

		assertThat("Nested logic is AND", nested.get(0).getLogicOperator(), equalTo(LogicOperator.AND));
		assertThat("Nested filter has 2 nested SearchFilter objects", nested.get(0).getFilter().values(),
				contains(instanceOf(SearchFilter.class), instanceOf(SearchFilter.class)));
		List<SearchFilter> nested1 = nested.get(0).getFilter().values().stream()
				.map(SearchFilter.class::cast).collect(Collectors.toList());

		assertComparisonSearchFilter("1", nested1.get(0), "foo", CompareOperator.EQUAL, "bar");
		assertComparisonSearchFilter("2", nested1.get(1), "bar", CompareOperator.EQUAL, "1");
	}

	@Test
	public void orEqualDoubleFilter() {
		Map<String, Object> m = new LinkedHashMap<String, Object>();
		m.put("foo", "bar");
		m.put("bar", 1);
		SearchFilter f = new SearchFilter(m, CompareOperator.EQUAL, LogicOperator.OR);
		String result = f.asLDAPSearchFilterString();
		Assert.assertEquals("(|(foo=bar)(bar=1))", result);
	}

	@Test
	public void orSubstringDoubleFilter() {
		Map<String, Object> m = new LinkedHashMap<String, Object>();
		m.put("foo", "bar");
		m.put("bar", 1);
		SearchFilter f = new SearchFilter(m, CompareOperator.SUBSTRING, LogicOperator.OR);
		String result = f.asLDAPSearchFilterString();
		Assert.assertEquals("(|(foo=*bar*)(bar=*1*))", result);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void parse_orSubstringDoubleFilter() {
		SearchFilter f = SearchFilter.forLDAPSearchFilterString("(|(foo=*bar*)(bar=*1*))");
		assertThat("Filter parsed", f, notNullValue());
		assertThat("Logic is OR", f.getLogicOperator(), equalTo(LogicOperator.OR));
		Map<String, ?> filter = f.getFilter();
		assertThat("Filter has 2 nested SearchFilter objects", filter.values(),
				contains(instanceOf(SearchFilter.class), instanceOf(SearchFilter.class)));
		List<SearchFilter> nested = filter.values().stream().map(SearchFilter.class::cast)
				.collect(Collectors.toList());
		assertComparisonSearchFilter("1", nested.get(0), "foo", CompareOperator.SUBSTRING, "bar");
		assertComparisonSearchFilter("2", nested.get(1), "bar", CompareOperator.SUBSTRING, "1");
	}

	@Test
	public void nestedSingleFilter() {
		Map<String, String> m1 = Collections.singletonMap("bar", "one");
		SearchFilter f1 = new SearchFilter(m1);
		Map<String, Object> m = new LinkedHashMap<String, Object>();
		m.put("foo", f1);
		SearchFilter f = new SearchFilter(m);
		String result = f.asLDAPSearchFilterString();
		Assert.assertEquals("(bar=one)", result);
	}

	@Test
	public void parse_nestedSingleFilter() {
		SearchFilter f = SearchFilter.forLDAPSearchFilterString("(bar=one)");
		assertThat("Filter parsed", f, notNullValue());
		assertThat("Logic is AND", f.getLogicOperator(), equalTo(LogicOperator.AND));
		assertComparisonSearchFilter("1", f, "bar", CompareOperator.EQUAL, "one");
	}

	@Test
	public void nestedDoubleFilter() {
		Map<String, Object> m1 = new LinkedHashMap<String, Object>();
		m1.put("foo", "bar");
		m1.put("bar", 1);
		SearchFilter f1 = new SearchFilter(m1);
		Map<String, Object> m = new LinkedHashMap<String, Object>();
		m.put("foo", f1);
		SearchFilter f = new SearchFilter(m);
		String result = f.asLDAPSearchFilterString();
		Assert.assertEquals("(&(foo=bar)(bar=1))", result);
	}

	@Test
	public void mixedNestedDoubleFilter() {
		Map<String, Object> m1 = new LinkedHashMap<String, Object>();
		m1.put("foo", "bar");
		m1.put("bar", 1);
		SearchFilter f1 = new SearchFilter(m1);
		Map<String, Object> m = new LinkedHashMap<String, Object>();
		m.put("foo", f1);
		m.put("wiz", "pop");
		SearchFilter f = new SearchFilter(m);
		String result = f.asLDAPSearchFilterString();
		Assert.assertEquals("(&(&(foo=bar)(bar=1))(wiz=pop))", result);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void parse_mixedNestedDoubleFilter() {
		SearchFilter f = SearchFilter.forLDAPSearchFilterString("(&(&(foo=bar)(bar=1))(wiz=pop))");
		assertThat("Filter parsed", f, notNullValue());

		assertThat("Top logic is AND", f.getLogicOperator(), equalTo(LogicOperator.AND));

		Map<String, ?> filter = f.getFilter();
		assertThat("Filter has 2 nested SearchFilter objects", filter.values(),
				contains(instanceOf(SearchFilter.class), instanceOf(SearchFilter.class)));
		List<SearchFilter> nested = filter.values().stream().map(SearchFilter.class::cast)
				.collect(Collectors.toList());

		assertThat("Nested 1 logic is AND", nested.get(0).getLogicOperator(),
				equalTo(LogicOperator.AND));

		Map<String, ?> filter1 = nested.get(0).getFilter();
		assertThat("Filter 1 has 2 nested SearchFilter objects", filter1.values(),
				contains(instanceOf(SearchFilter.class), instanceOf(SearchFilter.class)));
		List<SearchFilter> nested1 = filter1.values().stream().map(SearchFilter.class::cast)
				.collect(Collectors.toList());
		assertComparisonSearchFilter("1", nested1.get(0), "foo", CompareOperator.EQUAL, "bar");
		assertComparisonSearchFilter("2", nested1.get(1), "bar", CompareOperator.EQUAL, "1");

		assertComparisonSearchFilter("3", nested.get(1), "wiz", CompareOperator.EQUAL, "pop");
	}

	@Test
	public void mixedNestedSingleFilter() {
		Map<String, String> m1 = Collections.singletonMap("foo", "bar");
		SearchFilter f1 = new SearchFilter(m1);
		Map<String, Object> m = new LinkedHashMap<String, Object>();
		m.put("foo", f1);
		m.put("wiz", "pop");
		SearchFilter f = new SearchFilter(m);
		String result = f.asLDAPSearchFilterString();
		Assert.assertEquals("(&(foo=bar)(wiz=pop))", result);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void parse_mixedNestedSingleFilter() {
		SearchFilter f = SearchFilter.forLDAPSearchFilterString("(&(foo=bar)(wiz=pop))");
		assertThat("Filter parsed", f, notNullValue());
		assertThat("Logic is AND", f.getLogicOperator(), equalTo(LogicOperator.AND));
		Map<String, ?> filter = f.getFilter();
		assertThat("Filter has 2 nested SearchFilter objects", filter.values(),
				contains(instanceOf(SearchFilter.class), instanceOf(SearchFilter.class)));
		List<SearchFilter> nested = filter.values().stream().map(SearchFilter.class::cast)
				.collect(Collectors.toList());
		assertComparisonSearchFilter("1", nested.get(0), "foo", CompareOperator.EQUAL, "bar");
		assertComparisonSearchFilter("2", nested.get(1), "wiz", CompareOperator.EQUAL, "pop");
	}

	private static class CollectingVisitor implements VisitorCallback {

		private final List<SearchFilter[]> captured = new ArrayList<>();

		@Override
		public boolean visit(SearchFilter node, SearchFilter parentNode) {
			captured.add(new SearchFilter[] { node, parentNode });
			return true;
		}

	}

	@SuppressWarnings("unchecked")
	@Test
	public void walk_simple() {
		// GIVEN
		SearchFilter f = SearchFilter.forLDAPSearchFilterString("(foo=bar)");

		// WHEN
		CollectingVisitor visitor = new CollectingVisitor();
		f.walk(visitor);

		// THEN
		assertThat("Callback count", visitor.captured, hasSize(1));
		assertThat("Callback values", visitor.captured.get(0),
				arrayContaining(sameInstance(f), nullValue()));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void walk_complex() {
		// GIVEN
		SearchFilter f = SearchFilter.forLDAPSearchFilterString(
				"'(& (/m/foo=bar) (| (/pm/bam/pop~=whiz) (/pm/boo/boo>0) (! (/pm/bam/ding<=9))))");

		// WHEN
		CollectingVisitor visitor = new CollectingVisitor();
		f.walk(visitor);

		// THEN
		assertThat("Callback count", visitor.captured, hasSize(7));
		assertThat("Callback 0 is root", visitor.captured.get(0),
				arrayContaining(sameInstance(f), nullValue()));

		assertThat("Callback 1 node", visitor.captured.get(1)[0].toString(), equalTo("(/m/foo=bar)"));
		assertThat("Callback 1 parent is root", visitor.captured.get(1)[1], sameInstance(f));

		assertThat("Callback 2 node", visitor.captured.get(2)[0].getLogicOperator(),
				equalTo(LogicOperator.OR));
		assertThat("Callback 2 parent is root", visitor.captured.get(2)[1], sameInstance(f));

		assertThat("Callback 3 node", visitor.captured.get(3)[0].toString(),
				equalTo("(/pm/bam/pop~=whiz)"));
		assertThat("Callback 3 parent is OR", visitor.captured.get(3)[1],
				sameInstance(visitor.captured.get(2)[0]));

		assertThat("Callback 4 node", visitor.captured.get(4)[0].toString(), equalTo("(/pm/boo/boo>0)"));
		assertThat("Callback 4 parent is OR", visitor.captured.get(4)[1],
				sameInstance(visitor.captured.get(2)[0]));

		assertThat("Callback 5 node", visitor.captured.get(5)[0].getLogicOperator(),
				equalTo(LogicOperator.NOT));
		assertThat("Callback 5 parent is OR", visitor.captured.get(5)[1],
				sameInstance(visitor.captured.get(2)[0]));

		assertThat("Callback 6 node", visitor.captured.get(6)[0].toString(),
				equalTo("(/pm/bam/ding<=9)"));
		assertThat("Callback 6 parent is NOT", visitor.captured.get(6)[1],
				sameInstance(visitor.captured.get(5)[0]));

	}

}
