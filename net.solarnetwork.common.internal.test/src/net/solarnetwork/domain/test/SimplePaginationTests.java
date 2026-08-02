/* ==================================================================
 * SimplePaginationTests.java - 2 Aug 2026 3:23:57 pm
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

package net.solarnetwork.domain.test;

import static org.assertj.core.api.BDDAssertions.from;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import java.util.List;
import org.junit.Test;
import net.solarnetwork.domain.SimplePagination;
import net.solarnetwork.domain.SimpleSortDescriptor;
import net.solarnetwork.domain.SortDescriptor;

/**
 * Test cases for the {@link SimplePagination} class.
 *
 * @author matt
 * @version 1.0
 */
public class SimplePaginationTests {

	@Test
	public void setOrderBy() {
		// GIVEN
		final var pagination = new SimplePagination();

		// WHEN
		pagination.setOrderBy(List.of("a", "b~", "c"));

		// THEN
		// @formatter:off
		then(pagination.getSorts())
			.as("Sort list created")
			.hasSize(3)
			.satisfies(sorts -> {
				then(sorts).element(0, type(SortDescriptor.class))
					.as("Sort key copied")
					.returns("a", from(SortDescriptor::getSortKey))
					.as("Sort direction copied")
					.returns(false, from(SortDescriptor::isDescending))
					;
				then(sorts).element(1, type(SortDescriptor.class))
					.as("Sort key copied")
					.returns("b", from(SortDescriptor::getSortKey))
					.as("Sort direction copied")
					.returns(true, from(SortDescriptor::isDescending))
					;
				then(sorts).element(2, type(SortDescriptor.class))
					.as("Sort key copied")
					.returns("c", from(SortDescriptor::getSortKey))
					.as("Sort direction copied")
					.returns(false, from(SortDescriptor::isDescending))
					;
			})
			;
		// @formatter:on
	}

	@Test
	public void getOrderBy() {
		// GIVEN
		final var pagination = new SimplePagination();

		// WHEN
		pagination.setSorts(List.of(
		// @formatter:off
				(SortDescriptor)new SimpleSortDescriptor("a", false),
				new SimpleSortDescriptor("b", true),
				new SimpleSortDescriptor("c", false)
				// @formatter:on
		));

		// THEN
		// @formatter:off
		then(pagination.getOrderBy())
			.as("Order by list created")
			.hasSize(3)
			.satisfies(orders -> {
				then(orders).element(0)
					.as("Order by key copied")
					.isEqualTo("a")
					;
				then(orders).element(1)
					.as("Order by key copied with descending suffic")
					.isEqualTo("b~")
					;
				then(orders).element(2)
					.as("Order by key copied")
					.isEqualTo("c")
					;
			})
			;
		// @formatter:on
	}
}
