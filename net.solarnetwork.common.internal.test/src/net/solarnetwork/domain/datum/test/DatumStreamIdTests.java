/* ==================================================================
 * DatumStreamIdTests.java - 11/03/2026 6:32:33 pm
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
import org.junit.Test;
import net.solarnetwork.domain.datum.DatumStreamId;
import net.solarnetwork.domain.datum.DatumStreamId.DatumStreamIdent;
import net.solarnetwork.domain.datum.ObjectDatumKind;

/**
 * Test cases for the {@link DatumStreamId} class.
 *
 * @author matt
 * @version 1.0
 */
public class DatumStreamIdTests {

	@Test
	public void factory_general() {
		// GIVEN
		final Long objectId = randomLong();
		final String sourceId = randomString();

		// WHEN
		final var pk = DatumStreamId.datumStreamId(null, objectId, sourceId);

		// THEN
		// @formatter:off
		then(pk)
			.as("Identity created")
			.isExactlyInstanceOf(DatumStreamId.class)
			.as("Given kind preserved")
			.returns(null, from(DatumStreamId::getKind))
			.as("Given object ID preserved")
			.returns(objectId, from(DatumStreamId::getObjectId))
			.as("Given source ID preserved")
			.returns(sourceId, from(DatumStreamId::getSourceId))
			;
		// @formatter:on
	}

	@Test
	public void factory_general_identity() {
		// GIVEN
		final ObjectDatumKind kind = ObjectDatumKind.Node;
		final Long objectId = randomLong();
		final String sourceId = randomString();

		// WHEN
		final var pk = DatumStreamId.datumStreamId(kind, objectId, sourceId);

		// THEN
		// @formatter:off
		then(pk)
			.as("Identity created")
			.isExactlyInstanceOf(DatumStreamIdent.class)
			.as("Given kind preserved")
			.returns(kind, from(DatumStreamId::getKind))
			.as("Given object ID preserved")
			.returns(objectId, from(DatumStreamId::getObjectId))
			.as("Given source ID preserved")
			.returns(sourceId, from(DatumStreamId::getSourceId))
			;
		// @formatter:on
	}

	@Test
	public void factory_node() {
		// GIVEN
		final String sourceId = randomString();

		// WHEN
		final var pk = DatumStreamId.nodeStreamId(null, sourceId);

		// THEN
		// @formatter:off
		then(pk)
			.as("Identity created")
			.isExactlyInstanceOf(DatumStreamId.class)
			.as("Node kind assigned")
			.returns(ObjectDatumKind.Node, from(DatumStreamId::getKind))
			.as("Given object ID preserved")
			.returns(null, from(DatumStreamId::getObjectId))
			.as("Given source ID preserved")
			.returns(sourceId, from(DatumStreamId::getSourceId))
			;
		// @formatter:on
	}

	@Test
	public void factory_node_identity() {
		// GIVEN
		final Long objectId = randomLong();
		final String sourceId = randomString();

		// WHEN
		final var pk = DatumStreamId.nodeStreamId(objectId, sourceId);

		// THEN
		// @formatter:off
		then(pk)
			.as("Identity created")
			.isExactlyInstanceOf(DatumStreamIdent.class)
			.as("Node kind assigned")
			.returns(ObjectDatumKind.Node, from(DatumStreamId::getKind))
			.as("Given object ID preserved")
			.returns(objectId, from(DatumStreamId::getObjectId))
			.as("Given source ID preserved")
			.returns(sourceId, from(DatumStreamId::getSourceId))
			;
		// @formatter:on
	}

	@Test
	public void factory_location() {
		// GIVEN
		final String sourceId = randomString();

		// WHEN
		final var pk = DatumStreamId.locationStreamId(null, sourceId);

		// THEN
		// @formatter:off
		then(pk)
			.as("Identity created")
			.isExactlyInstanceOf(DatumStreamId.class)
			.as("Location kind assigned")
			.returns(ObjectDatumKind.Location, from(DatumStreamId::getKind))
			.as("Given object ID preserved")
			.returns(null, from(DatumStreamId::getObjectId))
			.as("Given source ID preserved")
			.returns(sourceId, from(DatumStreamId::getSourceId))
			;
		// @formatter:on
	}

	@Test
	public void factory_location_identity() {
		// GIVEN
		final Long objectId = randomLong();
		final String sourceId = randomString();

		// WHEN
		final var pk = DatumStreamId.locationStreamId(objectId, sourceId);

		// THEN
		// @formatter:off
		then(pk)
			.as("Identity created")
			.isExactlyInstanceOf(DatumStreamIdent.class)
			.as("Location kind assigned")
			.returns(ObjectDatumKind.Location, from(DatumStreamId::getKind))
			.as("Given object ID preserved")
			.returns(objectId, from(DatumStreamId::getObjectId))
			.as("Given source ID preserved")
			.returns(sourceId, from(DatumStreamId::getSourceId))
			;
		// @formatter:on
	}

	@Test
	public void sort_byKind() {
		// GIVEN
		final Long objectId = randomLong();
		final String sourceId = randomString();
		final DatumStreamId left = DatumStreamId.locationStreamId(objectId, sourceId);
		final DatumStreamId right = DatumStreamId.nodeStreamId(objectId, sourceId);
		final DatumStreamId left2 = DatumStreamId.locationStreamId(Long.valueOf(objectId.longValue()),
				sourceId);

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
		final DatumStreamId left = DatumStreamId.nodeStreamId(objectId, sourceId);
		final DatumStreamId right = DatumStreamId.nodeStreamId(objectId2, sourceId);

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
		final DatumStreamId left = DatumStreamId.nodeStreamId(objectId, sourceId);
		final DatumStreamId right = DatumStreamId.nodeStreamId(objectId, sourceId2);

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
		final DatumStreamId left = DatumStreamId.nodeStreamId(objectId, sourceId);
		final DatumStreamId right = DatumStreamId.nodeStreamId(objectId, sourceId2);

		// THEN
		then(left.compareTo(right)).as("Source compares with natrual sort").isLessThan(0);
		then(right.compareTo(left)).as("Source compares with natrual sort").isGreaterThan(0);
	}

}
