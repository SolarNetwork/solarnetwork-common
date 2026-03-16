/* ==================================================================
 * UniqueTests.java - 17/03/2026 7:32:25 am
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

import static net.solarnetwork.test.CommonTestUtils.randomInt;
import static org.assertj.core.api.BDDAssertions.from;
import static org.assertj.core.api.BDDAssertions.then;
import org.jspecify.annotations.Nullable;
import org.junit.Test;
import net.solarnetwork.domain.Unique;

/**
 * Test cases for the {@link Unique} API.
 *
 * @author matt
 * @version 1.0
 */
public class UniqueTests {

	private static final class TestUnique implements Unique<Integer> {

		private final Integer id;

		private TestUnique(@Nullable Integer id) {
			super();
			this.id = id;
		}

		@Override
		public @Nullable Integer getId() {
			return id;
		}

	}

	@Test
	public void id() {
		// GIVEN
		final var id = randomInt();

		// WHEN
		final var unq = new TestUnique(id);

		// THEN
		// @formatter:off
		then(unq)
			.as("ID returned from getter")
			.returns(id, from(TestUnique::getId))
			.as("Non-null ID means hasId")
			.returns(true, from(TestUnique::hasId))
			.as("ID returned from access")
			.returns(id, from(TestUnique::id))
			;
		// @formatter:on
	}

	@Test
	public void id_null() {
		// GIVEN
		final Integer id = null;

		// WHEN
		final var unq = new TestUnique(id);

		// THEN
		// @formatter:off
		then(unq)
			.as("Null returned from getter")
			.returns(null, from(TestUnique::getId))
			.as("Null ID means not hasId")
			.returns(false, from(TestUnique::hasId))
			.as("Null returned from access")
			.returns(null, from(TestUnique::id))
			;
		// @formatter:on
	}

}
