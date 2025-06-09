/* ==================================================================
 * ObjectUtilsTests.java - 7/10/2021 10:22:46 AM
 *
 * Copyright 2021 SolarNetwork.net Dev Team
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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.junit.Assert;
import org.junit.Test;
import net.solarnetwork.util.ObjectUtils;

/**
 * Test cases for the {@link ObjectUtils} class.
 *
 * @author matt
 * @version 1.1
 */
public class ObjectUtilsTests {

	@Test
	public void requireNonNullArgument_notNull() {
		// WHEN
		String arg = "foo";
		String result = ObjectUtils.requireNonNullArgument(arg, "fooBar");

		// THEN
		assertThat("Object should be returned when not null", result, is(sameInstance(arg)));
	}

	@Test
	public void requireNonNullArgument_null() {
		// WHEN
		try {
			ObjectUtils.requireNonNullArgument(null, "fooBar");
			Assert.fail("Should have thrown IllegalArgumentException");
		} catch ( IllegalArgumentException e ) {
			assertThat("Message should include argument name", e.getMessage(),
					is("The fooBar argument must not be null."));
		}
	}

	@Test
	public void requireNonEmptyArgument_array_notEmpty() {
		// WHEN
		String[] arg = new String[] { "foo" };
		String[] result = ObjectUtils.requireNonEmptyArgument(arg, "fooBar");

		// THEN
		assertThat("Object should be returned when not empty", result, is(sameInstance(arg)));
	}

	@Test
	public void requireNonEmptyArgument_array_null() {
		// WHEN
		try {
			ObjectUtils.requireNonEmptyArgument((String[]) null, "fooBar");
			Assert.fail("Should have thrown IllegalArgumentException");
		} catch ( IllegalArgumentException e ) {
			assertThat("Message should include argument name", e.getMessage(),
					is("The fooBar argument must not be empty."));
		}
	}

	@Test
	public void requireNonEmptyArgument_array_empty() {
		// WHEN
		try {
			ObjectUtils.requireNonEmptyArgument(new String[0], "fooBar");
			Assert.fail("Should have thrown IllegalArgumentException");
		} catch ( IllegalArgumentException e ) {
			assertThat("Message should include argument name", e.getMessage(),
					is("The fooBar argument must not be empty."));
		}
	}

	@Test
	public void requireNonEmptyArgument_collection_notEmpty() {
		// WHEN
		List<String> arg = Arrays.asList("foo");
		List<String> result = ObjectUtils.requireNonEmptyArgument(arg, "fooBar");

		// THEN
		assertThat("Object should be returned when not empty", result, is(sameInstance(arg)));
	}

	@Test
	public void requireNonEmptyArgument_collection_null() {
		// WHEN
		try {
			ObjectUtils.requireNonEmptyArgument((List<String>) null, "fooBar");
			Assert.fail("Should have thrown IllegalArgumentException");
		} catch ( IllegalArgumentException e ) {
			assertThat("Message should include argument name", e.getMessage(),
					is("The fooBar argument must not be empty."));
		}
	}

	@Test
	public void requireNonEmptyArgument_collection_empty() {
		// WHEN
		try {
			ObjectUtils.requireNonEmptyArgument(Collections.emptyList(), "fooBar");
			Assert.fail("Should have thrown IllegalArgumentException");
		} catch ( IllegalArgumentException e ) {
			assertThat("Message should include argument name", e.getMessage(),
					is("The fooBar argument must not be empty."));
		}
	}

	@Test
	public void comparativelyEqual_nulls() {
		assertThat("Comparing two nulls are equal", ObjectUtils.comparativelyEqual(null, null),
				is(true));
	}

	@Test
	public void comparativelyEqual_oneNull() {
		assertThat("Comparing one null is not equal",
				ObjectUtils.comparativelyEqual(null, BigDecimal.ONE), is(false));
		assertThat("Comparing one null is not equal",
				ObjectUtils.comparativelyEqual(BigDecimal.ONE, null), is(false));
	}

	@Test
	public void comparativelyEqual_identityEquals() {
		final Long l = Long.valueOf("123456789");
		assertThat("Comparing same instance is true", ObjectUtils.comparativelyEqual(l, l), is(true));
	}

	@Test
	public void comparativelyEqual_equalsDifferentInstances() {
		final BigDecimal l = new BigDecimal("12345.6789");
		final BigDecimal r = new BigDecimal("12345.67890");
		assertThat("Decimals compare as equal", l.compareTo(r), is(equalTo(0)));
		assertThat("Comparing different instances that are comparatively equal is true",
				ObjectUtils.comparativelyEqual(l, r), is(true));
		assertThat("Comparing different BigDecimal instances with different scales is false",
				Objects.equals(l, r), is(false));
	}

	@Test
	public void comparativelyEqual_notEqual() {
		final BigDecimal l = new BigDecimal("12345.6789");
		final BigDecimal r = new BigDecimal("23456.7890");
		assertThat("Decimals compare not as equal", l.compareTo(r), is(lessThan(0)));
		assertThat("Comparing different instances that are comparatively not equal is false",
				ObjectUtils.comparativelyEqual(l, r), is(false));
	}

}
