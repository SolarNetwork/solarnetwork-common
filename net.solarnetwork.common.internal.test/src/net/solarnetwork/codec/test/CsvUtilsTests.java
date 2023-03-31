/* ==================================================================
 * CsvUtilsTests.java - 31/03/2023 3:18:52 pm
 * 
 * Copyright 2023 SolarNetwork.net Dev Team
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

package net.solarnetwork.codec.test;

import static net.solarnetwork.util.IntRange.rangeOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import org.junit.Test;
import net.solarnetwork.codec.CsvUtils;
import net.solarnetwork.util.IntRangeSet;

/**
 * Test cases for the {@link CsvUtils} class.
 * 
 * @author matt
 * @version 1.0
 */
public class CsvUtilsTests {

	@Test
	public void parseColumnReference_num() {
		assertThat("Number column ref parsed", CsvUtils.parseColumnReference("1"), is(equalTo(1)));
	}

	@Test
	public void parseColumnReference_zero() {
		assertThat("Zero column ref parsed", CsvUtils.parseColumnReference("0"), is(equalTo(0)));
	}

	@Test
	public void parseColumnReference_neg() {
		assertThat("Negative number column ref parsed", CsvUtils.parseColumnReference("-1"),
				is(equalTo(-1)));
	}

	@Test
	public void parseColumnReference_char() {
		assertThat("Character column ref parsed", CsvUtils.parseColumnReference("A"), is(equalTo(1)));
	}

	@Test
	public void parseColumnReference_char_double() {
		assertThat("Double-character column ref parsed", CsvUtils.parseColumnReference("AA"),
				is(equalTo(27)));
	}

	@Test
	public void parseColumnReference_char_lowerCase() {
		assertThat("Double-character column ref parsed", CsvUtils.parseColumnReference("a"),
				is(equalTo(1)));
	}

	@Test(expected = IllegalArgumentException.class)
	public void parseColumnReference_char_invalid() {
		CsvUtils.parseColumnReference("!");
	}

	@Test(expected = IllegalArgumentException.class)
	public void parseColumnReference_char_null() {
		CsvUtils.parseColumnReference(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void parseColumnReference_char_empty() {
		CsvUtils.parseColumnReference("");
	}

	@Test(expected = IllegalArgumentException.class)
	public void parseColumnReference_char_whitespace() {
		CsvUtils.parseColumnReference(" ");
	}

	@Test
	public void parseColumnReferences_single_nums() {
		assertThat("Single range ref parsed", CsvUtils.parseColumnsReference("1-3"),
				is(equalTo(new IntRangeSet(rangeOf(1, 3)))));
	}

	@Test
	public void parseColumnReferences_single_chars() {
		assertThat("Single range ref parsed", CsvUtils.parseColumnsReference("A-C"),
				is(equalTo(new IntRangeSet(rangeOf(1, 3)))));
	}

	@Test
	public void parseColumnReferences_single_chars_double() {
		assertThat("Single range ref parsed", CsvUtils.parseColumnsReference("AA-CC"),
				is(equalTo(new IntRangeSet(rangeOf(27, 81)))));
	}

	@Test
	public void parseColumnReferences_single_mixed() {
		assertThat("Single range ref parsed", CsvUtils.parseColumnsReference("3-F"),
				is(equalTo(new IntRangeSet(rangeOf(3, 6)))));
	}

	@Test
	public void parseColumnReferences_multi_nums() {
		assertThat("Multi range ref parsed", CsvUtils.parseColumnsReference("1-3,9,11-15"),
				is(equalTo(new IntRangeSet(rangeOf(1, 3), rangeOf(9), rangeOf(11, 15)))));
	}

	@Test
	public void parseColumnReferences_multi_chars() {
		assertThat("Multi range ref parsed", CsvUtils.parseColumnsReference("A-C,I,K-O"),
				is(equalTo(new IntRangeSet(rangeOf(1, 3), rangeOf(9), rangeOf(11, 15)))));
	}

	@Test
	public void parseColumnReferences_null() {
		assertThat("Null input returns null", CsvUtils.parseColumnsReference(null), is(nullValue()));
	}

	@Test
	public void parseColumnReferences_empty() {
		assertThat("Empty input returns null", CsvUtils.parseColumnsReference(""), is(nullValue()));
	}

	@Test
	public void parseColumnReferences_whitespace() {
		assertThat("Whitespace input returns null", CsvUtils.parseColumnsReference(" "),
				is(nullValue()));
	}

}
