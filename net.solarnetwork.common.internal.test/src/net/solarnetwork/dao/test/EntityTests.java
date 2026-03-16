/* ==================================================================
 * EntityTests.java - 17/03/2026 7:38:05 am
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

import static java.time.Instant.now;
import static net.solarnetwork.test.CommonTestUtils.randomInt;
import static org.assertj.core.api.BDDAssertions.from;
import static org.assertj.core.api.BDDAssertions.then;
import java.time.Instant;
import org.jspecify.annotations.Nullable;
import org.junit.Test;
import net.solarnetwork.dao.Entity;

/**
 * Test cases for the {@link Entity} API.
 *
 * @author matt
 * @version 1.0
 */
public class EntityTests {

	private static final class TestEntity implements Entity<Integer> {

		private final @Nullable Integer id;
		private final @Nullable Instant created;

		private TestEntity(@Nullable Integer id, @Nullable Instant created) {
			super();
			this.id = id;
			this.created = created;
		}

		@Override
		public @Nullable Integer getId() {
			return id;
		}

		@Override
		public @Nullable Instant getCreated() {
			return created;
		}

		@Override
		protected TestEntity clone() {
			try {
				return (TestEntity) super.clone();
			} catch ( CloneNotSupportedException e ) {
				throw new IllegalStateException(e);
			}
		}

	}

	@Test
	public void entity() {
		// GIVEN
		final Integer id = randomInt();
		final Instant created = now();

		// WHEN
		final var unq = new TestEntity(id, created);

		// THEN
		// @formatter:off
		then(unq)
			.as("ID returned from getter")
			.returns(id, from(TestEntity::getId))
			.as("Non-null ID means hasId")
			.returns(true, from(TestEntity::hasId))
			.as("ID returned from access")
			.returns(id, from(TestEntity::id))
			.as("Created returned from getter")
			.returns(created, from(TestEntity::getCreated))
			.as("Non-null created means hasCreated")
			.returns(true, from(TestEntity::hasCreated))
			.as("Created returned from access")
			.returns(created, from(TestEntity::created))
			;
		// @formatter:on
	}

	@Test
	public void entity_null() {
		// GIVEN
		final Integer id = null;
		final Instant created = null;

		// WHEN
		final var unq = new TestEntity(id, created);

		// THEN
		// @formatter:off
		then(unq)
			.as("ID returned from getter")
			.returns(null, from(TestEntity::getId))
			.as("Null ID means not hasId")
			.returns(false, from(TestEntity::hasId))
			.as("ID returned from access")
			.returns(null, from(TestEntity::id))
			.as("Created returned from getter")
			.returns(null, from(TestEntity::getCreated))
			.as("Null created means not hasCreated")
			.returns(false, from(TestEntity::hasCreated))
			.as("Created returned from access")
			.returns(null, from(TestEntity::created))
			;
		// @formatter:on
	}

}
