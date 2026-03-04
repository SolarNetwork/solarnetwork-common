/* ==================================================================
 * BasicDatumStreamMetadataIdentityTests.java - 5/03/2026 8:07:47 am
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
import static net.solarnetwork.domain.datum.ObjectDatumKind.Node;
import static net.solarnetwork.test.CommonTestUtils.randomLong;
import static net.solarnetwork.test.CommonTestUtils.randomString;
import static org.assertj.core.api.BDDAssertions.from;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;
import org.junit.Test;
import net.solarnetwork.domain.datum.BasicObjectDatumStreamIdentity;
import net.solarnetwork.domain.datum.ObjectDatumKind;

/**
 * Test cases for the {@link BasicDatumStreamMetadataIdentity} class.
 *
 * @author matt
 * @version 1.0
 */
public class BasicDatumStreamMetadataIdentityTests {

	@Test
	public void construct() {
		// GIVEN
		final var streamId = randomUUID();
		final var kind = ObjectDatumKind.Node;
		final var objectId = randomLong();
		final var sourceId = randomString();

		// WHEN
		final var ident = new BasicObjectDatumStreamIdentity(streamId, kind, objectId, sourceId);

		// THEN
		// @formatter:off
		then(ident)
			.as("Given stream ID provided")
			.returns(streamId, from(BasicObjectDatumStreamIdentity::getStreamId))
			.as("Given kind provided")
			.returns(kind, from(BasicObjectDatumStreamIdentity::getKind))
			.as("Given object ID provided")
			.returns(objectId, from(BasicObjectDatumStreamIdentity::getObjectId))
			.as("Given source ID provided")
			.returns(sourceId, from(BasicObjectDatumStreamIdentity::getSourceId))
			;
		// @formatter:on
	}

	@Test
	public void construct_streamIdentity() {
		// GIVEN
		final var streamId = randomUUID();
		final var kind = ObjectDatumKind.Node;
		final var objectId = randomLong();
		final var sourceId = randomString();

		// WHEN
		final var ident = BasicObjectDatumStreamIdentity.streamIdentity(streamId, kind, objectId,
				sourceId);

		// THEN
		// @formatter:off
		then(ident)
			.as("Given stream ID provided")
			.returns(streamId, from(BasicObjectDatumStreamIdentity::getStreamId))
			.as("Given kind provided")
			.returns(kind, from(BasicObjectDatumStreamIdentity::getKind))
			.as("Given object ID provided")
			.returns(objectId, from(BasicObjectDatumStreamIdentity::getObjectId))
			.as("Given source ID provided")
			.returns(sourceId, from(BasicObjectDatumStreamIdentity::getSourceId))
			;
		// @formatter:on
	}

	@Test
	public void construct_null_streamId() {
		thenThrownBy(() -> {
			new BasicObjectDatumStreamIdentity(null, Node, randomLong(), randomString());
		}, "Null stream ID not allowed").isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void construct_null_kind() {
		thenThrownBy(() -> {
			new BasicObjectDatumStreamIdentity(randomUUID(), null, randomLong(), randomString());
		}, "Null kind not allowed").isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void construct_null_objectId() {
		thenThrownBy(() -> {
			new BasicObjectDatumStreamIdentity(randomUUID(), Node, null, randomString());
		}, "Null object ID not allowed").isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void construct_null_sourceId() {
		thenThrownBy(() -> {
			new BasicObjectDatumStreamIdentity(randomUUID(), Node, randomLong(), null);
		}, "Null source ID not allowed").isInstanceOf(IllegalArgumentException.class);
	}

}
