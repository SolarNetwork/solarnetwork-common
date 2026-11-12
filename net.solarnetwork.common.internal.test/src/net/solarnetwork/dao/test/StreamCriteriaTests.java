/* ==================================================================
 * StreamCriteriaTests.java - 13/11/2026 7:53:29 am
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
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.junit.Test;
import net.solarnetwork.dao.StreamCriteria;

/**
 * Test cases for the {@link StreamCriteria} API.
 *
 * @author matt
 * @version 1.0
 */
public class StreamCriteriaTests {

	private static final class TestCriteria implements StreamCriteria {

		private final UUID @Nullable [] streamIds;

		private TestCriteria(UUID @Nullable [] streamIds) {
			super();
			this.streamIds = streamIds;
		}

		@Override
		public UUID @Nullable [] getStreamIds() {
			return streamIds;
		}

	}

	@Test
	public void streamIds() {
		// GIVEN
		final UUID streamId1 = UUID.randomUUID();
		final UUID streamId2 = UUID.randomUUID();

		// WHEN
		final var criteria = new TestCriteria(new UUID[] { streamId1, streamId2 });

		// THEN
		// @formatter:off
		then(criteria)
			.as("getStreamId returns first stream ID")
			.returns(streamId1, from(TestCriteria::getStreamId))
			.as("Stream criteria available")
			.returns(true, from(TestCriteria::hasStreamCriteria))
			;
		// @formatter:on
	}

	@Test
	public void streamIds_null() {
		// WHEN
		final var criteria = new TestCriteria(null);

		// THEN
		// @formatter:off
		then(criteria)
			.as("getStreamId returns null when no stream IDs")
			.returns(null, from(TestCriteria::getStreamId))
			.as("Stream criteria not available when no stream IDs")
			.returns(false, from(TestCriteria::hasStreamCriteria))
			;
		// @formatter:on
	}

	@Test
	public void streamIds_empty() {
		// WHEN
		final var criteria = new TestCriteria(new UUID[0]);

		// THEN
		// @formatter:off
		then(criteria)
			.as("getStreamId returns null when stream IDs empty")
			.returns(null, from(TestCriteria::getStreamId))
			.as("Stream criteria not available when stream IDs empty")
			.returns(false, from(TestCriteria::hasStreamCriteria))
			;
		// @formatter:on
	}

	@Test
	public void streamIds_firstNull() {
		// WHEN
		final var criteria = new TestCriteria(new UUID[] { null, UUID.randomUUID() });

		// THEN
		// @formatter:off
		then(criteria)
			.as("getStreamId returns first element, even when null")
			.returns(null, from(TestCriteria::getStreamId))
			.as("Stream criteria not available when first stream ID is null")
			.returns(false, from(TestCriteria::hasStreamCriteria))
			;
		// @formatter:on
	}

}
