/* ==================================================================
 * StringUtilsTests.java - Nov 1, 2012 2:13:33 PM
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
 */

package net.solarnetwork.util.test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Assert;
import org.junit.Test;
import net.solarnetwork.domain.KeyValuePair;
import net.solarnetwork.util.StringUtils;

/**
 * Unit test for the StringUtils class.
 * 
 * @author matt
 * @version 1.2
 */
public class StringUtilsTests {

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
	public void commaDelimitedStringToMapWithNestedEquals() {
		Map<String, String> result = StringUtils
				.commaDelimitedStringToMap("foo = bar=bam, bar = bim=bam");
		assertEquals(result.get("foo"), "bar=bam");
		assertEquals(result.get("bar"), "bim=bam");
	}

	@Test
	public void commaDelimitedStringToMapWithSingleNestedEquals() {
		Map<String, String> result = StringUtils.commaDelimitedStringToMap("foo = bar=bam");
		assertEquals(result.get("foo"), "bar=bam");
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

	@Test
	public void expandTemplateNull() {
		String result = StringUtils.expandTemplateString(null, null);
		assertThat(result, nullValue());
	}

	@Test
	public void expandTemplateSimple() {
		String result = StringUtils.expandTemplateString("Hello {name}",
				Collections.singletonMap("name", "world"));
		assertThat(result, equalTo("Hello world"));
	}

	@Test
	public void expandTemplateMissingKey() {
		String result = StringUtils.expandTemplateString("Hello {name}", null);
		assertThat(result, equalTo("Hello "));
	}

	@Test
	public void expandTemplateMulti() {
		Map<String, Object> vars = new HashMap<String, Object>();
		vars.put("greeting", "Hello");
		vars.put("name", "world");
		String result = StringUtils.expandTemplateString("{greeting} {name}", vars);
		assertThat(result, equalTo("Hello world"));
	}

	@Test
	public void expandTemplateWithDefaults() {
		String result = StringUtils.expandTemplateString("{greeting:Hello} {name:universe}",
				Collections.singletonMap("name", "world"));
		assertThat(result, equalTo("Hello world"));
	}

	@Test
	public void sha256PropertyNoSalt() {
		String result = StringUtils.sha256Base64Value("password", null);
		assertThat(result, equalTo("{SHA-256}XohImNooBHFR0OVvjcYpJ3NgPQ1qq73WKhHvch0VQtg="));
	}

	@Test
	public void sha256PropertyFixedSalt() throws Exception {
		byte[] salt = Hex.decodeHex("6ae3b4c425b8d0b6");
		String result = StringUtils.sha256Base64Value("password", salt);
		assertThat(result,
				equalTo("{SSHA-256}BDHkL7DnK8AOtcT4+GRI++kjOER7Zmr5YcVflkwQ/bhq47TEJbjQtg=="));
	}

	@Test
	public void sha256PropertyRandomSalt() throws Exception {
		String result = StringUtils.sha256Base64Value("password");
		assertThat(result, startsWith("{SSHA-256}"));
		assertThat("Value not same as unsalted", result,
				not(equalTo("{SHA256}XohImNooBHFR0OVvjcYpJ3NgPQ1qq73WKhHvch0VQtg=")));
	}

	@Test
	public void decodeBase64Sha256DigestNoSalt() {
		KeyValuePair pair = StringUtils
				.decodeBase64DigestComponents("{SHA-256}XohImNooBHFR0OVvjcYpJ3NgPQ1qq73WKhHvch0VQtg=");
		assertThat("Digest as hex", pair.getKey(),
				equalTo("5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8"));
		assertThat("No salt", pair.getValue(), nullValue());
	}

	@Test
	public void decodeBase64Sha256DigestWithSalt() throws Exception {
		String expectedSalt = "6ae3b4c425b8d0b6";
		KeyValuePair pair = StringUtils.decodeBase64DigestComponents(
				"{SSHA-256}BDHkL7DnK8AOtcT4+GRI++kjOER7Zmr5YcVflkwQ/bhq47TEJbjQtg==");
		assertThat("Salt as hex", pair.getValue(), equalTo(expectedSalt));

		byte[] plain = new byte[16];
		System.arraycopy("password".getBytes("UTF-8"), 0, plain, 0, 8);
		System.arraycopy(Hex.decodeHex(expectedSalt), 0, plain, 8, 8);
		assertThat("Digest as hex", pair.getKey(), equalTo(DigestUtils.sha256Hex(plain)));
	}

	@Test
	public void maskedMapNullArguments() {
		Map<String, Object> masked = StringUtils.sha256MaskedMap(null, null);
		assertThat("Null arguments results in null", masked, nullValue());
	}

	@Test
	public void maskedMapNullMapArgument() {
		Set<String> secureKeys = Collections.singleton("a");
		Map<String, Object> masked = StringUtils.sha256MaskedMap(null, secureKeys);
		assertThat("Null map argument results in null", masked, nullValue());
	}

	@Test
	public void maskedMapNullKeysArgument() {
		Map<String, String> map = Collections.singletonMap("a", "b");
		Map<String, String> masked = StringUtils.sha256MaskedMap(map, null);
		assertThat("Null keys argument results in map argument", masked, sameInstance(map));
	}

	@Test
	public void maskedMapNoChange() {
		Map<String, String> map = Collections.singletonMap("a", "b");
		Set<String> secureKeys = Collections.singleton("c");
		Map<String, String> masked = StringUtils.sha256MaskedMap(map, secureKeys);
		assertThat("No change results in map argument", masked, sameInstance(map));
	}

	@Test
	public void maskedMapSingleChange() {
		Map<String, String> map = new LinkedHashMap<String, String>();
		map.put("a", "b");
		map.put("c", "d");
		Set<String> secureKeys = Collections.singleton("c");
		Map<String, String> masked = StringUtils.sha256MaskedMap(map, secureKeys);
		assertThat("Change results in new map instance", masked, not(sameInstance(map)));
		assertThat("Change results in map of same size", masked.keySet(), hasSize(map.size()));
		assertThat("Unchanged key", masked, hasEntry("a", "b"));
		assertThat("Masked key", masked.get("c"), startsWith("{SSHA-256}"));
	}

	@Test
	public void maskedMapMultiChange() {
		Map<String, String> map = new LinkedHashMap<String, String>();
		map.put("a", "b");
		map.put("c", "d");
		Set<String> secureKeys = new HashSet<String>(Arrays.asList("a", "c"));
		Map<String, String> masked = StringUtils.sha256MaskedMap(map, secureKeys);
		assertThat("Change results in new map instance", masked, not(sameInstance(map)));
		assertThat("Change results in map of same size", masked.keySet(), hasSize(map.size()));
		assertThat("Masked key", masked.get("a"), startsWith("{SSHA-256}"));
		assertThat("Masked key", masked.get("c"), startsWith("{SSHA-256}"));
	}

	@Test
	public void simpleIdValue_null() {
		String id = StringUtils.simpleIdValue(null);
		assertThat("Null ID generated", id, nullValue());
	}

	@Test
	public void simpleIdValue_empty() {
		String id = StringUtils.simpleIdValue("");
		assertThat("Empty ID generated", id, equalTo(""));
	}

	@Test
	public void simpleIdValue_basic() {
		String id = StringUtils.simpleIdValue("This Is A Title");
		assertThat("Empty ID generated", id, equalTo("this_is_a_title"));
	}

	@Test
	public void simpleIdValue_noChange() {
		String id = StringUtils.simpleIdValue("this_is_a_title");
		assertThat("Empty ID generated", id, equalTo("this_is_a_title"));
	}

	@Test
	public void simpleIdValue_trim() {
		String id = StringUtils.simpleIdValue(" This Is A Title ");
		assertThat("Empty ID generated", id, equalTo("this_is_a_title"));
	}

	@Test
	public void simpleIdValue_coalesce() {
		String id = StringUtils.simpleIdValue("Hello, world");
		assertThat("Empty ID generated", id, equalTo("hello_world"));
	}

	@Test
	public void simpleIdValue_remove_prefix() {
		String id = StringUtils.simpleIdValue("! Hello");
		assertThat("Empty ID generated", id, equalTo("hello"));
	}

	@Test
	public void simpleIdValue_remove_suffix() {
		String id = StringUtils.simpleIdValue("Hello!!");
		assertThat("Empty ID generated", id, equalTo("hello"));
	}

	@Test
	public void simpleIdValue_remove_prefixAndsuffix() {
		String id = StringUtils.simpleIdValue("!!Hello!!");
		assertThat("Empty ID generated", id, equalTo("hello"));
	}

	@Test
	public void simpleIdValue_complex() {
		String id = StringUtils.simpleIdValue("!! OMG, is this like, SOO **complex**, or what?!");
		assertThat("Empty ID generated", id, equalTo("omg_is_this_like_soo_complex_or_what"));
	}

}
