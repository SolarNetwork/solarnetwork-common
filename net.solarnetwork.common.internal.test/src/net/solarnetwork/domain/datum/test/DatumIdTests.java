/* ==================================================================
 * DatumIdTests.java - 11/03/2026 6:32:33 pm
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

import static net.solarnetwork.test.CommonTestUtils.randomLong;
import static net.solarnetwork.test.CommonTestUtils.randomString;
import static org.assertj.core.api.BDDAssertions.from;
import static org.assertj.core.api.BDDAssertions.then;
import java.time.Instant;
import org.junit.Test;
import net.solarnetwork.domain.datum.DatumId;
import net.solarnetwork.domain.datum.DatumId.DatumIdent;
import net.solarnetwork.domain.datum.ObjectDatumKind;

/**
 * Test cases for the {@link DatumId} class.
 *
 * @author matt
 * @version 1.0
 */
public class DatumIdTests {

	@Test
	public void factory_general() {
		// GIVEN
		final Long objectId = randomLong();
		final String sourceId = randomString();
		final Instant timestamp = Instant.now();

		// WHEN
		final var pk = DatumId.datumId(null, objectId, sourceId, timestamp);

		// THEN
		// @formatter:off
		then(pk)
			.as("Identity created")
			.isExactlyInstanceOf(DatumId.class)
			.as("Given kind preserved")
			.returns(null, from(DatumId::getKind))
			.as("Given object ID preserved")
			.returns(objectId, from(DatumId::getObjectId))
			.as("Given source ID preserved")
			.returns(sourceId, from(DatumId::getSourceId))
			.as("Given timestamp preserved")
			.returns(timestamp, from(DatumId::getTimestamp))
			;
		// @formatter:on
	}

	@Test
	public void factory_general_identity() {
		// GIVEN
		final ObjectDatumKind kind = ObjectDatumKind.Node;
		final Long objectId = randomLong();
		final String sourceId = randomString();
		final Instant timestamp = Instant.now();

		// WHEN
		final var pk = DatumId.datumId(kind, objectId, sourceId, timestamp);

		// THEN
		// @formatter:off
		then(pk)
			.as("Identity created")
			.isExactlyInstanceOf(DatumIdent.class)
			.as("Given kind preserved")
			.returns(kind, from(DatumId::getKind))
			.as("Given object ID preserved")
			.returns(objectId, from(DatumId::getObjectId))
			.as("Given source ID preserved")
			.returns(sourceId, from(DatumId::getSourceId))
			.as("Given timestamp preserved")
			.returns(timestamp, from(DatumId::getTimestamp))
			;
		// @formatter:on
	}

	@Test
	public void factory_node() {
		// GIVEN
		final String sourceId = randomString();
		final Instant timestamp = Instant.now();

		// WHEN
		final var pk = DatumId.nodeId(null, sourceId, timestamp);

		// THEN
		// @formatter:off
		then(pk)
			.as("Identity created")
			.isExactlyInstanceOf(DatumId.class)
			.as("Node kind assigned")
			.returns(ObjectDatumKind.Node, from(DatumId::getKind))
			.as("Given object ID preserved")
			.returns(null, from(DatumId::getObjectId))
			.as("Given source ID preserved")
			.returns(sourceId, from(DatumId::getSourceId))
			.as("Given timestamp preserved")
			.returns(timestamp, from(DatumId::getTimestamp))
			;
		// @formatter:on
	}

	@Test
	public void factory_node_identity() {
		// GIVEN
		final Long objectId = randomLong();
		final String sourceId = randomString();
		final Instant timestamp = Instant.now();

		// WHEN
		final var pk = DatumId.nodeId(objectId, sourceId, timestamp);

		// THEN
		// @formatter:off
		then(pk)
			.as("Identity created")
			.isExactlyInstanceOf(DatumIdent.class)
			.as("Node kind assigned")
			.returns(ObjectDatumKind.Node, from(DatumId::getKind))
			.as("Given object ID preserved")
			.returns(objectId, from(DatumId::getObjectId))
			.as("Given source ID preserved")
			.returns(sourceId, from(DatumId::getSourceId))
			.as("Given timestamp preserved")
			.returns(timestamp, from(DatumId::getTimestamp))
			;
		// @formatter:on
	}

	@Test
	public void factory_location() {
		// GIVEN
		final String sourceId = randomString();
		final Instant timestamp = Instant.now();

		// WHEN
		final var pk = DatumId.locationId(null, sourceId, timestamp);

		// THEN
		// @formatter:off
		then(pk)
			.as("Identity created")
			.isExactlyInstanceOf(DatumId.class)
			.as("Location kind assigned")
			.returns(ObjectDatumKind.Location, from(DatumId::getKind))
			.as("Given object ID preserved")
			.returns(null, from(DatumId::getObjectId))
			.as("Given source ID preserved")
			.returns(sourceId, from(DatumId::getSourceId))
			.as("Given timestamp preserved")
			.returns(timestamp, from(DatumId::getTimestamp))
			;
		// @formatter:on
	}

	@Test
	public void factory_location_identity() {
		// GIVEN
		final Long objectId = randomLong();
		final String sourceId = randomString();
		final Instant timestamp = Instant.now();

		// WHEN
		final var pk = DatumId.locationId(objectId, sourceId, timestamp);

		// THEN
		// @formatter:off
		then(pk)
			.as("Identity created")
			.isExactlyInstanceOf(DatumIdent.class)
			.as("Location kind assigned")
			.returns(ObjectDatumKind.Location, from(DatumId::getKind))
			.as("Given object ID preserved")
			.returns(objectId, from(DatumId::getObjectId))
			.as("Given source ID preserved")
			.returns(sourceId, from(DatumId::getSourceId))
			.as("Given timestamp preserved")
			.returns(timestamp, from(DatumId::getTimestamp))
			;
		// @formatter:on
	}

}
