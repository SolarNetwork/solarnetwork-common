/* ==================================================================
 * DifferentiableTests.java - 2/09/2022 10:14:17 am
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

package net.solarnetwork.domain.test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static net.solarnetwork.domain.Differentiable.differ;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import java.util.Collection;
import org.junit.Test;
import net.solarnetwork.domain.Differentiable;

/**
 * Test cases for the {@link Differentiable} API.
 * 
 * @author matt
 * @version 1.0
 */
public class DifferentiableTests {

	private static final class DiffTest implements Differentiable<DiffTest> {

		private final int n;

		public DiffTest(int n) {
			super();
			this.n = n;
		}

		@Override
		public boolean differsFrom(DiffTest other) {
			return n != other.n;
		}

	}

	private static DiffTest n(int n) {
		return new DiffTest(n);
	}

	@Test
	public void differ_bothNull() {
		assertThat("Two null objects do not differ", differ((DiffTest) null, null), is(equalTo(false)));
	}

	@Test
	public void differ_leftNull() {
		assertThat("Left null object differ", differ(null, n(1)), is(equalTo(true)));
	}

	@Test
	public void differ_rightNull() {
		assertThat("Left null object differ", differ(n(1), null), is(equalTo(true)));
	}

	@Test
	public void differ_same() {
		assertThat("Objects do not differ", differ(n(1), n(1)), is(equalTo(false)));
	}

	@Test
	public void differ_different() {
		assertThat("Objects do not differ", differ(n(1), n(2)), is(equalTo(true)));
	}

	@Test
	public void differ_collections_bothNull() {
		assertThat("Two null collections do not differ", differ((Collection<DiffTest>) null, null),
				is(equalTo(false)));
	}

	@Test
	public void differ_collections_leftNull() {
		assertThat("Left null collection differ", differ(null, emptyList()), is(equalTo(true)));
	}

	@Test
	public void differ_collections_rightNull() {
		assertThat("Left null collection differ", differ(emptyList(), null), is(equalTo(true)));
	}

	@Test
	public void differ_collections_differentSizeCollections() {
		assertThat("Different size collections differ", differ(emptyList(), asList(n(1))),
				is(equalTo(true)));
	}

	@Test
	public void differ_collections_singleton_different() {
		assertThat("Singlton collections differ", differ(asList(n(1)), asList(n(2))), is(equalTo(true)));
	}

	@Test
	public void differ_collections_singleton_same() {
		assertThat("Singlton collections do not differ", differ(asList(n(1)), asList(n(1))),
				is(equalTo(false)));
	}

	@Test
	public void differ_collections_different() {
		assertThat("Collections differ", differ(asList(n(1), n(2), n(3)), asList(n(1), n(3), n(4))),
				is(equalTo(true)));
	}

	@Test
	public void differ_collections_same() {
		assertThat("Collections do not differ",
				differ(asList(n(1), n(3), n(5)), asList(n(1), n(3), n(5))), is(equalTo(false)));
	}

}
