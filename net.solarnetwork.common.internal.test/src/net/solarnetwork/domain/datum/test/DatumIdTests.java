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
import net.solarnetwork.domain.datum.DatumIdentity;
import net.solarnetwork.domain.datum.DatumStreamId.DatumStreamIdent;
import net.solarnetwork.domain.datum.DatumStreamIdentity;
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
			.as("Identity not fully formed")
			.returns(false, from(DatumId::hasIdentity))
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
			.as("Identity fully formed")
			.returns(true, from(DatumId::hasIdentity))
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
			.as("Identity not fully formed")
			.returns(false, from(DatumId::hasIdentity))
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
			.as("Identity fully formed")
			.returns(true, from(DatumId::hasIdentity))
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
			.as("Identity not fully formed")
			.returns(false, from(DatumId::hasIdentity))
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
			.as("Identity fully formed")
			.returns(true, from(DatumId::hasIdentity))
			;
		// @formatter:on
	}

	@Test
	public void sort_byKind() {
		// GIVEN
		final Long objectId = randomLong();
		final String sourceId = randomString();
		final Instant timestamp = Instant.now();
		final DatumId left = DatumId.locationId(objectId, sourceId, timestamp);
		final DatumId right = DatumId.nodeId(objectId, sourceId, timestamp);
		final DatumId left2 = DatumId.locationId(Long.valueOf(objectId.longValue()), sourceId,
				timestamp);

		// THEN
		then(left.compareTo(right)).as("Kind compares in enum order").isGreaterThan(0);
		then(right.compareTo(left)).as("Kind compares in enum order").isLessThan(0);
		then(left.compareTo(left2)).as("Equality compared").isZero();
		then(left).as("Inequality preserved").isNotEqualTo(right);
		then(left).as("Equality preserved").isEqualTo(left2);
	}

	@Test
	public void sort_byObject() {
		// GIVEN
		final Long objectId = randomLong();
		final Long objectId2 = objectId + 1L;
		final String sourceId = "a";
		final Instant timestamp = Instant.now();
		final DatumId left = DatumId.nodeId(objectId, sourceId, timestamp);
		final DatumId right = DatumId.nodeId(objectId2, sourceId, timestamp);

		// THEN
		then(left.compareTo(right)).as("Object compares second").isLessThan(0);
		then(right.compareTo(left)).as("Object compares second").isGreaterThan(0);
	}

	@Test
	public void sort_bySource() {
		// GIVEN
		final Long objectId = randomLong();
		final String sourceId = "a";
		final String sourceId2 = "b";
		final Instant timestamp = Instant.now();
		final DatumId left = DatumId.nodeId(objectId, sourceId, timestamp);
		final DatumId right = DatumId.nodeId(objectId, sourceId2, timestamp);

		// THEN
		then(left.compareTo(right)).as("Source compares last").isLessThan(0);
		then(right.compareTo(left)).as("Source compares last").isGreaterThan(0);
	}

	@Test
	public void sort_bySource_naturally() {
		// GIVEN
		final Long objectId = randomLong();
		final String sourceId = "a/2";
		final String sourceId2 = "a/100";
		final Instant timestamp = Instant.now();
		final DatumId left = DatumId.nodeId(objectId, sourceId, timestamp);
		final DatumId right = DatumId.nodeId(objectId, sourceId2, timestamp);

		// THEN
		then(left.compareTo(right)).as("Source compares with natrual sort").isLessThan(0);
		then(right.compareTo(left)).as("Source compares with natrual sort").isGreaterThan(0);
	}

	@Test
	public void sort_byTimestamp() {
		// GIVEN
		final Long objectId = randomLong();
		final String sourceId = "a";
		final Instant timestamp = Instant.now();
		final Instant timestamp2 = timestamp.plusSeconds(1);
		final DatumId left = DatumId.nodeId(objectId, sourceId, timestamp);
		final DatumId right = DatumId.nodeId(objectId, sourceId, timestamp2);

		// THEN
		then(left.compareTo(right)).as("Source compares last").isLessThan(0);
		then(right.compareTo(left)).as("Source compares last").isGreaterThan(0);
	}

	@Test(expected = IllegalStateException.class)
	public void toIdentity_invalid() {
		// GIVEN
		final DatumId id = new DatumId(ObjectDatumKind.Node, null, null, null);

		id.toIdentity();
	}

	@Test
	public void toIdentity() {
		// GIVEN
		final DatumId id = new DatumId(ObjectDatumKind.Node, randomLong(), randomString(),
				Instant.now());

		// WHEN
		final DatumIdentity ident = id.toIdentity();

		// THEN
		// @formatter:off
		then(ident)
			.as("DatumIdent instance returned")
			.isExactlyInstanceOf(DatumIdent.class)
			.as("Given kind preserved")
			.returns(id.getKind(), from(DatumIdentity::getKind))
			.as("Given object ID preserved")
			.returns(id.getObjectId(), from(DatumIdentity::getObjectId))
			.as("Given source ID preserved")
			.returns(id.getSourceId(), from(DatumIdentity::getSourceId))
			.as("Given timestamp preserved")
			.returns(id.getTimestamp(), from(DatumIdentity::getTimestamp))
			;
		// @formatter:on
	}

	@Test
	public void toIdentity_sameInstance() {
		// GIVEN
		final DatumIdent id = new DatumIdent(ObjectDatumKind.Node, randomLong(), randomString(),
				Instant.now());

		// WHEN
		final DatumIdentity ident = id.toIdentity();

		// THEN
		// @formatter:off
		then(ident)
			.as("Same DatumIdent instance returned")
			.isSameAs(id)
			;
		// @formatter:on
	}

	@Test(expected = IllegalStateException.class)
	public void streamIdentity_invalid() {
		// GIVEN
		final DatumId id = new DatumId(ObjectDatumKind.Node, null, null, null);

		id.streamIdentity();
	}

	@Test
	public void streamIdentity() {
		// GIVEN
		final DatumId id = new DatumId(ObjectDatumKind.Node, randomLong(), randomString(),
				Instant.now());

		// WHEN
		final DatumStreamIdentity ident = id.streamIdentity();

		// THEN
		// @formatter:off
		then(ident)
			.as("DatumIdent instance returned")
			.isExactlyInstanceOf(DatumStreamIdent.class)
			.as("Given kind preserved")
			.returns(id.getKind(), from(DatumStreamIdentity::getKind))
			.as("Given object ID preserved")
			.returns(id.getObjectId(), from(DatumStreamIdentity::getObjectId))
			.as("Given source ID preserved")
			.returns(id.getSourceId(), from(DatumStreamIdentity::getSourceId))
			;
		// @formatter:on
	}

	@Test
	public void streamIdentity_sameInstance() {
		// GIVEN
		final DatumStreamIdent streamId = new DatumStreamIdent(ObjectDatumKind.Node, randomLong(),
				randomString());
		final DatumIdent id = new DatumIdent(streamId, Instant.now());

		// WHEN
		final DatumStreamIdentity ident = id.streamIdentity();

		// THEN
		// @formatter:off
		then(ident)
			.as("Same DatumStreamIdent instance returned")
			.isSameAs(streamId)
			;
		// @formatter:on
	}

}
