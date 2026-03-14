/* ==================================================================
 * PagincationCriteriaTests.java - 15/03/2026 11:06:56 am
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

import static net.solarnetwork.test.CommonTestUtils.randomInt;
import static net.solarnetwork.test.CommonTestUtils.randomLong;
import static org.assertj.core.api.BDDAssertions.from;
import static org.assertj.core.api.BDDAssertions.then;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.junit.Test;
import net.solarnetwork.dao.PaginationCriteria;
import net.solarnetwork.domain.SortDescriptor;

/**
 * Test cases for the {@link PaginationCriteria} class.
 *
 * @author matt
 * @version 1.0
 */
public class PagincationCriteriaTests {

	private static final class TestCriteria implements PaginationCriteria {

		private final @Nullable Long offset;
		private final @Nullable Integer max;

		private TestCriteria(@Nullable Long offset, @Nullable Integer max) {
			super();
			this.offset = offset;
			this.max = max;
		}

		@Override
		public @Nullable List<SortDescriptor> getSorts() {
			return null;
		}

		@Override
		public @Nullable Long getOffset() {
			return offset;
		}

		@Override
		public @Nullable Integer getMax() {
			return max;
		}

	}

	@Test
	public void pagination() {
		// GIVEN
		final Long offset = randomLong();
		final Integer max = randomInt();

		// WHEN
		final var criteria = new TestCriteria(offset, max);

		// THEN
		// @formatter:off
		then(criteria)
			.as("Offset available")
			.returns(true, from(TestCriteria::hasOffset))
			.as("getOffset returns given value")
			.returns(offset, from(TestCriteria::getOffset))
			.as("offset returns given value")
			.returns(offset, from(TestCriteria::offset))
			.as("Max available")
			.returns(true, from(TestCriteria::hasMax))
			.as("getMax returns given value")
			.returns(max, from(TestCriteria::getMax))
			.as("max returns given value")
			.returns(max, from(TestCriteria::max))
			;
		// @formatter:on
	}

	@Test
	public void pagination_noOffset() {
		// GIVEN
		final Integer max = randomInt();

		// WHEN
		final var criteria = new TestCriteria(null, max);

		// THEN
		// @formatter:off
		then(criteria)
			.as("Offset not available")
			.returns(false, from(TestCriteria::hasOffset))
			.as("getOffset returns given value")
			.returns(null, from(TestCriteria::getOffset))
			.as("offset returns given value")
			.returns(null, from(TestCriteria::offset))
			.as("Max available")
			.returns(true, from(TestCriteria::hasMax))
			.as("getMax returns given value")
			.returns(max, from(TestCriteria::getMax))
			.as("max returns given value")
			.returns(max, from(TestCriteria::max))
			;
		// @formatter:on
	}

	@Test
	public void pagination_noMax() {
		// GIVEN
		final Long offset = randomLong();

		// WHEN
		final var criteria = new TestCriteria(offset, null);

		// THEN
		// @formatter:off
		then(criteria)
			.as("Offset available")
			.returns(true, from(TestCriteria::hasOffset))
			.as("getOffset returns given value")
			.returns(offset, from(TestCriteria::getOffset))
			.as("offset returns given value")
			.returns(offset, from(TestCriteria::offset))
			.as("Max not available")
			.returns(false, from(TestCriteria::hasMax))
			.as("getMax returns given value")
			.returns(null, from(TestCriteria::getMax))
			.as("max returns given value")
			.returns(null, from(TestCriteria::max))
			;
		// @formatter:on
	}

}
