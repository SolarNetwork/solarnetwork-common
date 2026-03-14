/* ==================================================================
 * LocalDateRangeCriteriaTests.java - 15/03/2026 10:52:35 am
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
import java.time.LocalDateTime;
import org.jspecify.annotations.Nullable;
import org.junit.Test;
import net.solarnetwork.dao.LocalDateRangeCriteria;

/**
 * Test cases for the {@link LocalDateRangeCriteria} API.
 *
 * @author matt
 * @version 1.0
 */
public class LocalDateRangeCriteriaTests {

	private static final class TestCriteria implements LocalDateRangeCriteria {

		private final @Nullable LocalDateTime localStartDate;
		private final @Nullable LocalDateTime localEndDate;

		private TestCriteria(@Nullable LocalDateTime localStartDate,
				@Nullable LocalDateTime localEndDate) {
			super();
			this.localStartDate = localStartDate;
			this.localEndDate = localEndDate;
		}

		@Override
		public @Nullable LocalDateTime getLocalStartDate() {
			return localStartDate;
		}

		@Override
		public @Nullable LocalDateTime getLocalEndDate() {
			return localEndDate;
		}

	}

	@Test
	public void dateRange() {
		// GIVEN
		final LocalDateTime start = LocalDateTime.now();
		final LocalDateTime end = start.plusSeconds(1);

		// WHEN
		final var criteria = new TestCriteria(start, end);

		// THEN
		// @formatter:off
		then(criteria)
			.as("Local start date available")
			.returns(true, from(TestCriteria::hasLocalStartDate))
			.as("getLocalStartDate returns given value")
			.returns(start, from(TestCriteria::getLocalStartDate))
			.as("localStartDate returns given value")
			.returns(start, from(TestCriteria::localStartDate))
			.as("Local end date available")
			.returns(true, from(TestCriteria::hasLocalEndDate))
			.as("getLocalEndDate returns given value")
			.returns(end, from(TestCriteria::getLocalEndDate))
			.as("localEndDate returns given value")
			.returns(end, from(TestCriteria::localEndDate))
			.as("Local date range available")
			.returns(true, from(TestCriteria::hasLocalDateRange))
			;
		// @formatter:on
	}

	@Test
	public void onlyLocalStartDate() {
		// GIVEN
		final LocalDateTime start = LocalDateTime.now();

		// WHEN
		final var criteria = new TestCriteria(start, null);

		// THEN
		// @formatter:off
		then(criteria)
			.as("Local start date available")
			.returns(true, from(TestCriteria::hasLocalStartDate))
			.as("getLocalStartDate returns given value")
			.returns(start, from(TestCriteria::getLocalStartDate))
			.as("localStartDate returns given value")
			.returns(start, from(TestCriteria::localStartDate))
			.as("Local end date not available")
			.returns(false, from(TestCriteria::hasLocalEndDate))
			.as("getLocalEndDate returns given value")
			.returns(null, from(TestCriteria::getLocalEndDate))
			.as("localEndDate returns given value")
			.returns(null, from(TestCriteria::localEndDate))
			.as("Local date range not available when local end missing")
			.returns(false, from(TestCriteria::hasLocalDateRange))
			;
		// @formatter:on
	}

	@Test
	public void onlyEndDate() {
		// GIVEN
		final LocalDateTime end = LocalDateTime.now();

		// WHEN
		final var criteria = new TestCriteria(null, end);

		// THEN
		// @formatter:off
		then(criteria)
			.as("Local start date available")
			.returns(false, from(TestCriteria::hasLocalStartDate))
			.as("getLocalStartDate returns given value")
			.returns(null, from(TestCriteria::getLocalStartDate))
			.as("localStartDate returns given value")
			.returns(null, from(TestCriteria::localStartDate))
			.as("Local end date available")
			.returns(true, from(TestCriteria::hasLocalEndDate))
			.as("getLocalEndDate returns given value")
			.returns(end, from(TestCriteria::getLocalEndDate))
			.as("localEndDate returns given value")
			.returns(end, from(TestCriteria::localEndDate))
			.as("Local date range not available when local start missing")
			.returns(false, from(TestCriteria::hasLocalDateRange))
			;
		// @formatter:on
	}

}
