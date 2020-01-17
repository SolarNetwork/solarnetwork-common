/* ==================================================================
 * CollectionUtilsTests.java - 17/01/2020 10:41:39 am
 * 
 * Copyright 2020 SolarNetwork.net Dev Team
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

import static net.solarnetwork.util.IntRange.rangeOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import java.util.List;
import org.junit.Test;
import net.solarnetwork.util.CollectionUtils;
import net.solarnetwork.util.IntRange;
import net.solarnetwork.util.IntRangeSet;

/**
 * Test cases for the {@link CollectionUtils} class.
 * 
 * @author matt
 * @version 1.0
 */
public class CollectionUtilsTests {

	@Test
	public void coverintgIntRanges_empty() {
		List<IntRange> result = CollectionUtils.coveringIntRanges(new IntRangeSet(), 64);
		assertThat("Resulting ranges", result, hasSize(0));
	}

	@Test
	public void coveringIntRanges_reduced() {
		IntRangeSet set = new IntRangeSet(rangeOf(0, 1), rangeOf(3, 5), rangeOf(100, 101));
		List<IntRange> result = CollectionUtils.coveringIntRanges(set, 64);
		assertThat("Resulting ranges", result, contains(rangeOf(0, 5), rangeOf(100, 101)));
	}

	@Test
	public void coveringIntRanges_reduced2() {
		IntRangeSet set = new IntRangeSet(rangeOf(1), rangeOf(3, 5), rangeOf(7), rangeOf(9), rangeOf(32),
				rangeOf(60, 72), rangeOf(100, 101));
		List<IntRange> result = CollectionUtils.coveringIntRanges(set, 64);
		assertThat("Resulting ranges", result, contains(rangeOf(1, 64), rangeOf(65, 101)));
	}

	@Test
	public void coveringIntRanges_toSingletons() {
		IntRangeSet set = new IntRangeSet(rangeOf(0, 1), rangeOf(100, 101));
		List<IntRange> result = CollectionUtils.coveringIntRanges(set, 1);
		assertThat("Resulting ranges", result,
				contains(rangeOf(0), rangeOf(1), rangeOf(100), rangeOf(101)));
	}

	@Test
	public void coveringIntRanges_nochange() {
		IntRangeSet set = new IntRangeSet(rangeOf(0, 5), rangeOf(100, 101));
		List<IntRange> result = CollectionUtils.coveringIntRanges(set, 64);
		assertThat("Resulting ranges", result, contains(rangeOf(0, 5), rangeOf(100, 101)));
	}

	@Test
	public void coveringIntRanges_toOneRange() {
		IntRangeSet set = new IntRangeSet(rangeOf(1, 2),
				rangeOf(Integer.MAX_VALUE - 1, Integer.MAX_VALUE));
		List<IntRange> result = CollectionUtils.coveringIntRanges(set, Integer.MAX_VALUE);
		assertThat("Resulting ranges", result, contains(rangeOf(1, Integer.MAX_VALUE)));
	}

	@Test
	public void coveringIntRanges_javaDocExample() {
		IntRangeSet set = new IntRangeSet(rangeOf(0, 1), rangeOf(3, 5), rangeOf(20, 28),
				rangeOf(404, 406), rangeOf(412, 418));
		List<IntRange> result = CollectionUtils.coveringIntRanges(set, 64);
		assertThat("Resulting ranges", result, contains(rangeOf(0, 28), rangeOf(404, 418)));
	}

	@Test(expected = IllegalArgumentException.class)
	public void coveringIntRanges_zeroMax() {
		CollectionUtils.coveringIntRanges(new IntRangeSet(), 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void coveringIntRanges_negativeMax() {
		CollectionUtils.coveringIntRanges(new IntRangeSet(), -1);
	}

}
