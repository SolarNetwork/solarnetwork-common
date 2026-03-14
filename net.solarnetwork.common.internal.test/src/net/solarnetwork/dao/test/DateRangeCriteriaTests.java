/* ==================================================================
 * DateRangeCriteriaTests.java - 15/03/2026 10:52:35 am
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
import java.time.Instant;
import org.jspecify.annotations.Nullable;
import org.junit.Test;
import net.solarnetwork.dao.DateRangeCriteria;

/**
 * Test cases for the {@link DateRangeCriteria} API.
 *
 * @author matt
 * @version 1.0
 */
public class DateRangeCriteriaTests {

	private static final class TestCriteria implements DateRangeCriteria {

		private final @Nullable Instant startDate;
		private final @Nullable Instant endDate;

		private TestCriteria(@Nullable Instant startDate, @Nullable Instant endDate) {
			super();
			this.startDate = startDate;
			this.endDate = endDate;
		}

		@Override
		public @Nullable Instant getStartDate() {
			return startDate;
		}

		@Override
		public @Nullable Instant getEndDate() {
			return endDate;
		}

	}

	@Test
	public void dateRange() {
		// GIVEN
		final Instant start = Instant.now();
		final Instant end = start.plusSeconds(1);

		// WHEN
		final var criteria = new TestCriteria(start, end);

		// THEN
		// @formatter:off
		then(criteria)
			.as("Start date available")
			.returns(true, from(TestCriteria::hasStartDate))
			.as("getStartDate returns given value")
			.returns(start, from(TestCriteria::getStartDate))
			.as("startDate returns given value")
			.returns(start, from(TestCriteria::startDate))
			.as("End date available")
			.returns(true, from(TestCriteria::hasEndDate))
			.as("getEndDate returns given value")
			.returns(end, from(TestCriteria::getEndDate))
			.as("endDate returns given value")
			.returns(end, from(TestCriteria::endDate))
			.as("Date range available")
			.returns(true, from(TestCriteria::hasDateRange))
			;
		// @formatter:on
	}

	@Test
	public void onlyStartDate() {
		// GIVEN
		final Instant start = Instant.now();

		// WHEN
		final var criteria = new TestCriteria(start, null);

		// THEN
		// @formatter:off
		then(criteria)
			.as("Start date available")
			.returns(true, from(TestCriteria::hasStartDate))
			.as("getStartDate returns given value")
			.returns(start, from(TestCriteria::getStartDate))
			.as("startDate returns given value")
			.returns(start, from(TestCriteria::startDate))
			.as("End date not available")
			.returns(false, from(TestCriteria::hasEndDate))
			.as("getEndDate returns given value")
			.returns(null, from(TestCriteria::getEndDate))
			.as("endDate returns given value")
			.returns(null, from(TestCriteria::endDate))
			.as("Date range not available when end missing")
			.returns(false, from(TestCriteria::hasDateRange))
			;
		// @formatter:on
	}

	@Test
	public void onlyEndDate() {
		// GIVEN
		final Instant end = Instant.now();

		// WHEN
		final var criteria = new TestCriteria(null, end);

		// THEN
		// @formatter:off
		then(criteria)
			.as("Start date not available")
			.returns(false, from(TestCriteria::hasStartDate))
			.as("getStartDate returns given value")
			.returns(null, from(TestCriteria::getStartDate))
			.as("startDate returns given value")
			.returns(null, from(TestCriteria::startDate))
			.as("End date available")
			.returns(true, from(TestCriteria::hasEndDate))
			.as("getEndDate returns given value")
			.returns(end, from(TestCriteria::getEndDate))
			.as("endDate returns given value")
			.returns(end, from(TestCriteria::endDate))
			.as("Date range not available when start missing")
			.returns(false, from(TestCriteria::hasDateRange))
			;
		// @formatter:on
	}

}
