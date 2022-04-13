/* ==================================================================
 * UnionIteratorTests.java - 13/04/2022 5:19:02 PM
 * 
 * Copyright 2022 SolarNetwork.net Dev Team
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

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import java.util.List;
import org.junit.Test;
import net.solarnetwork.util.UnionIterator;

/**
 * Test cases for the {@link UnionIterator}.
 * 
 * @author matt
 * @version 1.0
 */
public class UnionIteratorTests {

	@Test
	public void singleInput() {
		// GIVEN
		List<Integer> i1 = asList(1, 2, 3);

		// WHEN
		UnionIterator<Integer> itr = new UnionIterator<>(asList(i1.iterator()));

		// THEN
		Iterable<Integer> iterable = () -> itr;
		assertThat("Union of one iterator returns results",
				stream(iterable.spliterator(), false).collect(toList()), contains(1, 2, 3));
	}

	@Test
	public void doubleInput() {
		// GIVEN
		List<Integer> i1 = asList(1, 2, 3);
		List<Integer> i2 = asList(4, 5, 6);

		// WHEN
		UnionIterator<Integer> itr = new UnionIterator<>(asList(i1.iterator(), i2.iterator()));

		// THEN
		Iterable<Integer> iterable = () -> itr;
		assertThat("Union of two iterator returns combined results",
				stream(iterable.spliterator(), false).collect(toList()), contains(1, 2, 3, 4, 5, 6));
	}

	@Test
	public void inputsWithNull() {
		// GIVEN
		List<Integer> i1 = asList(1, null, 3);
		List<Integer> i2 = asList(4, null, 6);

		// WHEN
		UnionIterator<Integer> itr = new UnionIterator<>(asList(i1.iterator(), i2.iterator()));

		// THEN
		Iterable<Integer> iterable = () -> itr;
		assertThat("Union of two iterator returns combined results",
				stream(iterable.spliterator(), false).collect(toList()),
				contains(1, null, 3, 4, null, 6));
	}

}
