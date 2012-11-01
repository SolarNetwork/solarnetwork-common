/* ==================================================================
 * StringUtilsTest.java - Nov 1, 2012 2:13:33 PM
 * 
 * Copyright 2007-2012 SolarNetwork.net Dev Team
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
 * $Id$
 * ==================================================================
 */

package net.solarnetwork.util.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Set;

import net.solarnetwork.util.StringUtils;

import org.junit.Test;

/**
 * Unit test for the StringUtils class.
 * 
 * @author matt
 * @version $Revision$
 */
public class StringUtilsTest {

	@Test
	public void commaDelimitedStringToSetNullInput() {
		assertNull(StringUtils.commaDelimitedStringToSet(null));
	}

	@Test
	public void commaDelimitedStringToSetEmptyInput() {
		assertNull(StringUtils.commaDelimitedStringToSet(""));
	}

	@Test
	public void commaDelimitedStringToSetBlankInput() {
		Set<String> result = StringUtils.commaDelimitedStringToSet("   ");
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals("", result.iterator().next()); // empty because of trim
	}
	
	private void verifySetAB(Set<String> result) {
		assertNotNull(result);
		assertEquals(2, result.size());
		assertTrue("Must contain element 'a'", result.contains("a"));
		assertTrue("Must contain element 'b'", result.contains("b"));
	}

	@Test
	public void commaDelimitedStringToSetOneDelimiter() {
		Set<String> result = StringUtils.commaDelimitedStringToSet("a,b");
		verifySetAB(result);
	}

	@Test
	public void commaDelimitedStringToSetOneDelimiterWithWhitespace() {
		Set<String> result = StringUtils.commaDelimitedStringToSet("a, b");
		verifySetAB(result);
	}

	@Test
	public void commaDelimitedStringToSetOneDelimiterWithWhitespaceAround() {
		Set<String> result = StringUtils.commaDelimitedStringToSet("a , b");
		verifySetAB(result);
	}

	@Test
	public void commaDelimitedStringToSetOneDelimiterWithWhitespaceBefore() {
		Set<String> result = StringUtils.commaDelimitedStringToSet(" a , b");
		verifySetAB(result);
	}

	@Test
	public void commaDelimitedStringToSetOneDelimiterWithWhitespaceAfter() {
		Set<String> result = StringUtils.commaDelimitedStringToSet("a , b ");
		verifySetAB(result);
	}

	@Test
	public void commaDelimitedStringToSetOneDelimiterWithWhitespaceEverywhere() {
		Set<String> result = StringUtils.commaDelimitedStringToSet(" a , b ");
		verifySetAB(result);
	}

	@Test
	public void commaDelimitedStringToMapNullInput() {
		assertNull(StringUtils.commaDelimitedStringToMap(null));
	}

	@Test
	public void commaDelimitedStringToMapEmptyInput() {
		assertNull(StringUtils.commaDelimitedStringToMap(""));
	}

	@Test
	public void commaDelimitedStringToMapBlankInput() {
		Map<String, String> result = StringUtils.commaDelimitedStringToMap("   ");
		assertNotNull(result);
		assertEquals(0, result.size());
	}
	
	private void verifyMapAB(Map<String, String> result) {
		assertNotNull(result);
		assertEquals(2, result.size());
		assertTrue("Must contain key 'a'", result.containsKey("a"));
		assertTrue("Must contain key 'b'", result.containsKey("b"));
		assertEquals("Must contain value 'A'", "A", result.get("a"));
		assertEquals("Must contain value 'B'", "B", result.get("b"));
	}

	@Test
	public void commaDelimitedStringToMapOneDelimiter() {
		Map<String, String> result = StringUtils.commaDelimitedStringToMap("a=A,b=B");
		verifyMapAB(result);
	}

	@Test
	public void commaDelimitedStringToMapOneDelimiterWithWhitespaceAllAround() {
		Map<String, String> result = StringUtils.commaDelimitedStringToMap(" a = A , b = B ");
		verifyMapAB(result);
	}

}
