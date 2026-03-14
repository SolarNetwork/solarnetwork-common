/* ==================================================================
 * SortCriteriaTests.java - 15/03/2026 11:12:24 am
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

import static org.assertj.core.api.BDDAssertions.from;
import static org.assertj.core.api.BDDAssertions.then;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.junit.Test;
import net.solarnetwork.dao.SortCriteria;
import net.solarnetwork.domain.SimpleSortDescriptor;
import net.solarnetwork.domain.SortDescriptor;

/**
 * Test cases for the {@link SortCriteria} API.
 *
 * @author matt
 * @version 1.0
 */
public class SortCriteriaTests {

	private static final class TestCriteria implements SortCriteria {

		private final @Nullable List<SortDescriptor> sorts;

		private TestCriteria(@Nullable List<SortDescriptor> sorts) {
			super();
			this.sorts = sorts;
		}

		@Override
		public @Nullable List<SortDescriptor> getSorts() {
			return sorts;
		}

	}

	@Test
	public void sorts() {
		// GIVEN
		final List<SortDescriptor> sorts = SimpleSortDescriptor.sorts("a", "b");

		// WHEN
		final var criteria = new TestCriteria(sorts);

		// THEN
		// @formatter:off
		then(criteria)
			.as("Sorts available")
			.returns(true, from(TestCriteria::hasSorts))
			.as("getSorts returns given value")
			.returns(sorts, from(TestCriteria::getSorts))
			.as("sorts returns given value")
			.returns(sorts, from(TestCriteria::sorts))
			;
		// @formatter:on
	}

	@Test
	public void noSorts() {
		// GIVEN

		// WHEN
		final var criteria = new TestCriteria(null);

		// THEN
		// @formatter:off
		then(criteria)
			.as("Sorts available")
			.returns(false, from(TestCriteria::hasSorts))
			.as("getSorts returns given value")
			.returns(null, from(TestCriteria::getSorts))
			.as("sorts returns given value")
			.returns(null, from(TestCriteria::sorts))
			;
		// @formatter:on
	}

	@Test
	public void emptySorts() {
		// GIVEN
		final List<SortDescriptor> sorts = List.of();

		// WHEN
		final var criteria = new TestCriteria(sorts);

		// THEN
		// @formatter:off
		then(criteria)
			.as("Sorts available")
			.returns(false, from(TestCriteria::hasSorts))
			.as("getSorts returns given value")
			.returns(sorts, from(TestCriteria::getSorts))
			.as("sorts returns given value")
			.returns(sorts, from(TestCriteria::sorts))
			;
		// @formatter:on
	}

}
