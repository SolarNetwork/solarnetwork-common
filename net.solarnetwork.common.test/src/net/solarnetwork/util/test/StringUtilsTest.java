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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import net.solarnetwork.util.StringUtils;
import org.junit.Assert;
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

	@Test
	public void patternsForNull() {
		Pattern[] r = StringUtils.patterns(null, 0);
		assertNull("Null expressions", r);
	}

	@Test
	public void patternsForEmpty() {
		Pattern[] r = StringUtils.patterns(new String[0], 0);
		assertNull("Empty expressions", r);
	}

	@Test
	public void patternsForExpression() {
		final String[] ex = new String[] { "foo" };
		Pattern[] r = StringUtils.patterns(ex, 0);
		assertNotNull(r);
		assertEquals("Same number of patterns", ex.length, r.length);
		assertTrue("Pattern compiled without flags", r[0].matcher("foo").matches());
	}

	@Test
	public void patternsForExpressionWithFlags() {
		final String[] ex = new String[] { "FOO\n#here's a comment" };
		Pattern[] r = StringUtils.patterns(ex, Pattern.CASE_INSENSITIVE | Pattern.COMMENTS);
		assertNotNull(r);
		assertEquals("Same number of patterns", ex.length, r.length);
		assertTrue("Pattern compiled with flags", r[0].matcher("foo").matches());
	}

	@Test
	public void patternsForExpressions() {
		final String[] ex = new String[] { "foo", "bar", "bam" };
		Pattern[] r = StringUtils.patterns(ex, 0);
		assertNotNull(r);
		assertEquals("Same number of patterns", ex.length, r.length);
	}

	@Test(expected = PatternSyntaxException.class)
	public void illegalPattern() {
		StringUtils.patterns(new String[] { "[" }, 0);
	}

	@Test
	public void expressionsForNull() {
		String[] r = StringUtils.expressions(null);
		assertNull("Null patterns", r);
	}

	@Test
	public void expressionsForEmpty() {
		String[] r = StringUtils.expressions(new Pattern[0]);
		assertNull("Empty patterns", r);
	}

	@Test
	public void expressionsForPattern() {
		final Pattern[] pat = new Pattern[] { Pattern.compile("foo") };
		final String[] ex = new String[] { "foo" };
		String[] r = StringUtils.expressions(pat);
		assertNotNull(r);
		assertEquals("Same number of expresions", ex.length, r.length);
		Assert.assertArrayEquals("Expressions from patterns", ex, r);
	}

	@Test
	public void expressionsForPatterns() {
		final Pattern[] pat = new Pattern[] { Pattern.compile("foo"),
				Pattern.compile("bar\n#comment", Pattern.COMMENTS), Pattern.compile("bam") };
		final String[] ex = new String[] { "foo", "bar\n#comment", "bam" };
		String[] r = StringUtils.expressions(pat);
		assertNotNull(r);
		assertEquals("Same number of expresions", ex.length, r.length);
		Assert.assertArrayEquals("Expressions from patterns", ex, r);
	}

	@Test
	public void matchNull() {
		Matcher r = StringUtils.matches(null, "foo");
		assertNull("No match found", r);
	}

	@Test
	public void matchEmpty() {
		Matcher r = StringUtils.matches(new Pattern[0], "foo");
		assertNull("No match found", r);
	}

	@Test
	public void matchNullExpressions() {
		Matcher r = StringUtils.matches(new Pattern[] { Pattern.compile("foo") }, null);
		assertNull("No match found", r);
	}

	@Test
	public void matchEmptyExpressions() {
		Matcher r = StringUtils.matches(new Pattern[] { Pattern.compile("foo") }, "");
		assertNull("No match found", r);
	}

	@Test
	public void matchPattern() {
		final Pattern[] pat = new Pattern[] { Pattern.compile("foo") };
		Matcher r = StringUtils.matches(pat, "foo");
		assertNotNull("Match found", r);
	}

	@Test
	public void noMatchPattern() {
		final Pattern[] pat = new Pattern[] { Pattern.compile("foo") };
		Matcher r = StringUtils.matches(pat, "bar");
		assertNull("No match found", r);
	}

	@Test
	public void matchPatterns() {
		final Pattern[] pat = new Pattern[] { Pattern.compile("foo"), Pattern.compile("bar"),
				Pattern.compile("bam") };
		Matcher r;
		r = StringUtils.matches(pat, "foo");
		assertNotNull("Match first found", r);
		assertEquals("Match first", pat[0], r.pattern());
		r = StringUtils.matches(pat, "bar");
		assertNotNull("Match second found", r);
		assertEquals("Match second", pat[1], r.pattern());
		r = StringUtils.matches(pat, "bam");
		assertNotNull("Match third found", r);
		assertEquals("Match third", pat[2], r.pattern());
	}

	@Test
	public void noMatchPatterns() {
		final Pattern[] pat = new Pattern[] { Pattern.compile("foo"), Pattern.compile("bar"),
				Pattern.compile("bam") };
		Matcher r = StringUtils.matches(pat, "FOO");
		assertNull("No match found", r);
	}

}
