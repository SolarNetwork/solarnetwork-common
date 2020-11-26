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

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
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

}
