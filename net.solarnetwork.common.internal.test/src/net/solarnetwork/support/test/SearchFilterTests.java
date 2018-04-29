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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import net.solarnetwork.support.SearchFilter;
import net.solarnetwork.support.SearchFilter.CompareOperator;
import net.solarnetwork.support.SearchFilter.LogicOperator;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test cases for the {@link SearchFilter} class.
 * 
 * @author matt
 * @version 1.0
 */
public class SearchFilterTests {

	@Test
	public void nullFilter() {
		SearchFilter f = new SearchFilter(null);
		String result = f.asLDAPSearchFilterString();
		Assert.assertEquals("", result);
	}

	@Test
	public void emptyFilter() {
		SearchFilter f = new SearchFilter(new HashMap<String, Object>());
		String result = f.asLDAPSearchFilterString();
		Assert.assertEquals("", result);
	}

	@Test
	public void defaultSingleFilter() {
		Map<String, String> m = Collections.singletonMap("foo", "bar");
		SearchFilter f = new SearchFilter(m);
		String result = f.asLDAPSearchFilterString();
		Assert.assertEquals("(foo=bar)", result);
	}

	@Test
	public void notEqualSingleFilter() {
		Map<String, String> m = Collections.singletonMap("foo", "bar");
		SearchFilter f = new SearchFilter(m, CompareOperator.EQUAL, LogicOperator.NOT);
		String result = f.asLDAPSearchFilterString();
		Assert.assertEquals("(!(foo=bar))", result);
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

}
