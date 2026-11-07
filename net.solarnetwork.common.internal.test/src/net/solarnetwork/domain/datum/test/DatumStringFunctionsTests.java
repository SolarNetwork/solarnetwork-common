/* ==================================================================
 * DatumStringFunctionsTests.java - 6/10/2025 8:21:25 am
 *
 * Copyright 2025 SolarNetwork.net Dev Team
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

package net.solarnetwork.domain.datum.test;

import static net.solarnetwork.test.CommonTestUtils.randomInt;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.InstanceOfAssertFactories.DOUBLE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import java.io.IOException;
import java.io.InputStream;
import org.junit.Test;
import org.springframework.util.ConcurrentLruCache;
import net.solarnetwork.domain.datum.DatumStringFunctions;

/**
 * Test cases for the {@link DatumStringFunctions} interface.
 *
 * @author matt
 * @version 1.2
 */
public class DatumStringFunctionsTests implements DatumStringFunctions {

	@Test
	public void regexMatches_nullSource() {
		assertThat("Null source returns false", regexMatches(null, "1"), is(false));
	}

	@Test
	public void regexMatches_emptySource() {
		assertThat("Empty source returns false", regexMatches("", "1"), is(false));
	}

	@Test
	public void regexMatches_nullRegex() {
		assertThat("Null regex returns false", regexMatches("123", null), is(false));
	}

	@Test
	public void regexMatches_emptyRegex() {
		assertThat("Empty regex returns false", regexMatches("123", ""), is(false));
	}

	@Test
	public void regexMatches_match() {
		assertThat("Matching regex returns true", regexMatches("123", "\\d"), is(true));
	}

	@Test
	public void regexMatches_noMatch() {
		assertThat("Non-matching regex returns false", regexMatches("123", "[a-z]"), is(false));
	}

	@Test
	public void regexReplace_nullSource() {
		assertThat("Null source returns null", regexReplace(null, "1", "a"), is(nullValue()));
	}

	@Test
	public void regexReplace_emptySource() {
		assertThat("Empty source returns source", regexReplace("", "1", "a"), is(equalTo("")));
	}

	@Test
	public void regexReplace_nullRegex() {
		// GIVEN
		final String source = "123";

		// THEN
		assertThat("Null regex returns null", regexReplace(source, null, "a"), is(equalTo(source)));
	}

	@Test
	public void regexReplace_emptyRegex() {
		// GIVEN
		final String source = "123";

		// THEN
		assertThat("Null regex returns null", regexReplace(source, "", "a"), is(equalTo(source)));
	}

	@Test
	public void regexReplace_noMatch() {
		// GIVEN
		final String source = "/123/abc";

		assertThat("Non-matching regex returns source", regexReplace(source, "^.*/(\\d+)$", "$1"),
				is(equalTo(source)));
	}

	@Test
	public void regexReplace_match() {
		assertThat("Replaced by regex", regexReplace("/abc/123", "^.*/(\\d+)$", "$1"),
				is(equalTo("123")));
	}

	@Test
	public void regexReplace_matchAll() {
		assertThat("Replaced by regex (all matches)", regexReplace("/123/123/123", "123", "abc"),
				is(equalTo("/abc/abc/abc")));
	}

	@Test
	public void regexReplace_fromCache() {
		// GIVEN
		final String regex = "^.*/(\\d+)$";

		// WHEN
		for ( int i = 0; i < 5; i++ ) {
			regexReplace("/abc/123", regex, "$1");
		}

		// THEN
		assertThat("Regex was cached", PATTERN_CACHE.contains(regex), is(true));
	}

	@Test
	public void regexReplace_cacheOverflow() {
		// GIVEN
		final int cacheCapacity = 100;

		// WHEN
		for ( int i = 0, max = cacheCapacity + 1; i < max; i++ ) {
			regexReplace("/abc/123", String.valueOf(i), "");
			try {
				Thread.sleep(10);
			} catch ( InterruptedException e ) {
				// continue
			}
		}

		// THEN
		assertThat("LRU regex was evicted", PATTERN_CACHE.contains("0"), is(false));
		for ( int i = 1, max = cacheCapacity + 1; i < max; i++ ) {
			assertThat("MRU regex was cached", PATTERN_CACHE.contains(String.valueOf(i)), is(true));
		}
		assertThat("Cache is at capacity", PATTERN_CACHE.size(), is(equalTo(cacheCapacity)));
	}

	@Test
	public void numberValue_null() {
		then(numberValue(null)).as("Null input returns null").isNull();
	}

	@Test
	public void numberValue_number() {
		// GIVEN
		final Number input = randomInt();

		// THEN
		then(numberValue(input)).as("Number input returns same instance").isSameAs(input);
	}

	@Test
	public void numberValue_nan() {
		// GIVEN
		final String input = "not a number";

		// THEN
		// @formatter:off
		then(numberValue(input))
			.as("Non-number input returns null")
			.isNull()
			;
		// @formatter:on
	}

	@Test
	public void numberValue_decimalString() {
		// GIVEN
		final String input = "4567890.125";

		// THEN
		// @formatter:off
		then(numberValue(input))
			.as("String decimal input returns number")
			.isInstanceOf(Double.class)
			.asInstanceOf(DOUBLE)
			.isEqualByComparingTo(4567890.125)
			;
		// @formatter:on
	}

	@Test
	public void numberValue_intString() {
		// GIVEN
		final String input = "123";

		// THEN
		// @formatter:off
		then(numberValue(input))
			.as("String integer input returns number")
			.isEqualTo(123)
			;
		// @formatter:on
	}

	@Test
	public void numberValue_paddedString() {
		// GIVEN
		final String input = "  12 ";

		// THEN
		// @formatter:off
		then(numberValue(input))
			.as("String input with leading and trailing whitespace returns number")
			.isEqualTo(12)
			;
		// @formatter:on
	}

	@Test
	public void patternCache_sizeProperty() throws Exception {
		// WHEN
		ConcurrentLruCache<?, ?> cache = newPatternCache("20");

		// THEN
		// @formatter:off
		then(cache.capacity())
			.as("Cache size from system property")
			.isEqualTo(20)
			;
		// @formatter:on
	}

	@Test
	public void patternCache_sizeProperty_invalid() throws Exception {
		for ( String size : new String[] { "", "abc", "-1" } ) {
			// WHEN
			ConcurrentLruCache<?, ?> cache = newPatternCache(size);

			// THEN
			// @formatter:off
			then(cache.capacity())
				.as("Default cache size used for invalid system property [%s]", size)
				.isEqualTo(100)
				;
			// @formatter:on
		}
	}

	/**
	 * Load a new copy of {@link DatumStringFunctions}, so its
	 * {@link DatumStringFunctions#PATTERN_CACHE} is created with a given cache
	 * size system property value.
	 */
	private static ConcurrentLruCache<?, ?> newPatternCache(String size) throws Exception {
		final String prop = "net.solarnetwork.domain.datum.regexCacheSize";
		final String orig = System.getProperty(prop);
		System.setProperty(prop, size);
		try {
			Class<?> c = Class.forName(DatumStringFunctions.class.getName(), true,
					new ChildFirstClassLoader(DatumStringFunctions.class.getName()));
			return (ConcurrentLruCache<?, ?>) c.getField("PATTERN_CACHE").get(null);
		} finally {
			if ( orig != null ) {
				System.setProperty(prop, orig);
			} else {
				System.clearProperty(prop);
			}
		}
	}

	/**
	 * A class loader that defines one class itself, so that class is
	 * initialized again, and delegates all other classes to its parent.
	 */
	private static final class ChildFirstClassLoader extends ClassLoader {

		private final String className;

		private ChildFirstClassLoader(String className) {
			super(DatumStringFunctionsTests.class.getClassLoader());
			this.className = className;
		}

		@Override
		protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
			if ( !name.equals(className) ) {
				return super.loadClass(name, resolve);
			}
			synchronized ( getClassLoadingLock(name) ) {
				Class<?> c = findLoadedClass(name);
				if ( c == null ) {
					try (InputStream in = getParent()
							.getResourceAsStream(name.replace('.', '/') + ".class")) {
						byte[] data = in.readAllBytes();
						c = defineClass(name, data, 0, data.length);
					} catch ( IOException e ) {
						throw new ClassNotFoundException(name, e);
					}
				}
				return c;
			}
		}

	}

}
