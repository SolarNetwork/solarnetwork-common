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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import net.solarnetwork.util.ObjectUtils;

/**
 * Test cases for the {@link ObjectUtils} class.
 * 
 * @author matt
 * @version 1.0
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

}
