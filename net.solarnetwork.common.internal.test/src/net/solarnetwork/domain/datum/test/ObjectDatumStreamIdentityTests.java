/* ==================================================================
 * ObjectDatumStreamIdentityTests.java - 12/03/2026 6:02:59 am
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

package net.solarnetwork.domain.datum.test;

import static java.util.UUID.randomUUID;
import static net.solarnetwork.test.CommonTestUtils.randomLong;
import static net.solarnetwork.test.CommonTestUtils.randomString;
import static org.assertj.core.api.BDDAssertions.from;
import static org.assertj.core.api.BDDAssertions.then;
import java.time.Instant;
import java.util.UUID;
import org.junit.Test;
import net.solarnetwork.domain.datum.DatumId.DatumIdent;
import net.solarnetwork.domain.datum.ObjectDatumKind;
import net.solarnetwork.domain.datum.ObjectDatumStreamIdentity;

/**
 * Test cases for the {@link ObjectDatumStreamIdentity} API.
 *
 * @author matt
 * @version 1.0
 */
public class ObjectDatumStreamIdentityTests {

	private static class TestIdentity implements ObjectDatumStreamIdentity {

		private final UUID streamId;
		private final ObjectDatumKind kind;
		private final Long objectId;
		private final String sourceId;

		private TestIdentity(ObjectDatumKind kind) {
			this(randomUUID(), kind, randomLong(), randomString());
		}

		private TestIdentity(UUID streamId, ObjectDatumKind kind, Long objectId, String sourceId) {
			super();
			this.streamId = streamId;
			this.kind = kind;
			this.objectId = objectId;
			this.sourceId = sourceId;
		}

		@Override
		public UUID getStreamId() {
			return streamId;
		}

		@Override
		public final ObjectDatumKind getKind() {
			return kind;
		}

		@Override
		public final Long getObjectId() {
			return objectId;
		}

		@Override
		public final String getSourceId() {
			return sourceId;
		}

	}

	@Test
	public void datumIdent() {
		// GIVEN
		final UUID streamId = randomUUID();
		final ObjectDatumKind kind = ObjectDatumKind.Node;
		final Long objectId = randomLong();
		final String sourceId = randomString();
		final Instant timestamp = Instant.now();

		// WHEN
		final var ident = new TestIdentity(streamId, kind, objectId, sourceId);
		final DatumIdent result = ident.datumIdent(timestamp);

		// THEN
		// @formatter:off
		then(result)
			.as("Non-null result provided")
			.isNotNull()
			.as("Given kind returned")
			.returns(kind, from(DatumIdent::getKind))
			.as("Given object ID returned")
			.returns(objectId, from(DatumIdent::getObjectId))
			.as("Given sourceId returned")
			.returns(sourceId, from(DatumIdent::getSourceId))
			.as("Given timestamp returned")
			.returns(timestamp, from(DatumIdent::getTimestamp))
			;
		// @formatter:on
	}

}
