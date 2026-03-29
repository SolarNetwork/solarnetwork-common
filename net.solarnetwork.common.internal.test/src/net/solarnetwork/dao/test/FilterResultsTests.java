/* ==================================================================
 * FilterResultsTests.java - 30/03/2026 12:46:49 pm
 *
 * Copyright 2026 SolarNetwork.net Dev Team
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

package net.solarnetwork.dao.test;

import static net.solarnetwork.test.CommonTestUtils.randomLong;
import static org.assertj.core.api.BDDAssertions.then;
import java.util.Iterator;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.junit.Test;
import net.solarnetwork.dao.FilterResults;
import net.solarnetwork.domain.BasicUnique;

/**
 * Test cases for the {@link FilterResults} API.
 *
 * @author matt
 * @version 1.0
 */
public class FilterResultsTests {

	private static class TestFilterResults implements FilterResults<BasicUnique<Long>, Long> {

		private List<BasicUnique<Long>> data;

		private TestFilterResults(List<BasicUnique<Long>> data) {
			super();
			this.data = data;
		}

		@Override
		public Iterator<BasicUnique<Long>> iterator() {
			return data.iterator();
		}

		@Override
		public Iterable<BasicUnique<Long>> getResults() {
			return data;
		}

		@Override
		public @Nullable Long getTotalResults() {
			return null;
		}

		@Override
		public long getStartingOffset() {
			return 0;
		}

		@Override
		public int getReturnedResultCount() {
			return (data != null ? data.size() : 0);
		}

	}

	@Test
	public void firstResult() {
		// GIVEN
		final var item = new BasicUnique<>(randomLong());
		final var obj = new TestFilterResults(List.of(item));

		// WHEN
		final var result = obj.firstResult();

		// THEN
		// @formatter:off
		then(result)
			.as("First data element returned")
			.isSameAs(item)
			;
		// @formatter:on
	}

	@Test
	public void firstResult_empty() {
		// GIVEN
		final var obj = new TestFilterResults(List.of());

		// WHEN
		final var result = obj.firstResult();

		// THEN
		// @formatter:off
		then(result)
			.as("Null returned from empty data")
			.isNull()
			;
		// @formatter:on
	}

	@Test
	public void firstResult_null() {
		// GIVEN
		final var obj = new TestFilterResults(null);

		// WHEN
		final var result = obj.firstResult();

		// THEN
		// @formatter:off
		then(result)
			.as("Null returned from null data")
			.isNull()
			;
		// @formatter:on
	}

}
