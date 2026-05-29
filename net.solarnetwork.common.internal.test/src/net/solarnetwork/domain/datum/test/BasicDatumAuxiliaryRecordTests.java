/* ==================================================================
 * BasicDatumAuxiliaryRecordTests.java - 30/05/2026 8:22:02 am
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

import static java.time.Instant.now;
import static net.solarnetwork.domain.datum.ObjectDatumKind.Location;
import static net.solarnetwork.domain.datum.ObjectDatumKind.Node;
import static net.solarnetwork.test.CommonTestUtils.randomLong;
import static net.solarnetwork.test.CommonTestUtils.randomString;
import static org.assertj.core.api.BDDAssertions.from;
import static org.assertj.core.api.BDDAssertions.then;
import java.util.Map;
import org.junit.Test;
import net.solarnetwork.domain.datum.BasicDatumAuxiliaryRecord;
import net.solarnetwork.domain.datum.DatumId.DatumIdent;
import net.solarnetwork.domain.datum.DatumSamples;
import net.solarnetwork.domain.datum.GeneralDatumMetadata;

/**
 * Test cases for the {@link BasicDatumAuxiliaryRecord} class.
 *
 * @author matt
 * @version 1.0
 */
public class BasicDatumAuxiliaryRecordTests {

	@Test
	public void createMark() {
		// GIVEN
		final DatumIdent ident = new DatumIdent(Node, randomLong(), randomString(), now());
		final String notes = randomString();
		final GeneralDatumMetadata meta = new GeneralDatumMetadata();

		// WHEN
		final var result = BasicDatumAuxiliaryRecord.createMark(ident, notes, meta);

		// THEN
		// @formatter:off
		then(result)
			.as("Record created")
			.isNotNull()
			.as("Datum ident from constructor arg preserved")
			.returns(ident, from(BasicDatumAuxiliaryRecord::datumIdent))
			.as("Object kind from ident returned")
			.returns(ident.getKind(), from(BasicDatumAuxiliaryRecord::getKind))
			.as("Object ID from ident returned")
			.returns(ident.getObjectId(), from(BasicDatumAuxiliaryRecord::getObjectId))
			.as("Source ID from ident returned")
			.returns(ident.getSourceId(), from(BasicDatumAuxiliaryRecord::getSourceId))
			.as("Timestamp from ident returned")
			.returns(ident.getTimestamp(), from(BasicDatumAuxiliaryRecord::getTimestamp))
			.as("Notes from constructor arg preserved")
			.returns(notes, from(BasicDatumAuxiliaryRecord::getNotes))
			.as("Metadata from constructor arg preserved")
			.returns(meta, from(BasicDatumAuxiliaryRecord::getMetadata))
			.as("Final samples null")
			.returns(null, from(BasicDatumAuxiliaryRecord::getSamplesFinal))
			.as("Start samples null")
			.returns(null, from(BasicDatumAuxiliaryRecord::getSamplesStart))
			;
		// @formatter:on
	}

	@Test
	public void createReset() {
		// GIVEN
		final var ident = new DatumIdent(Node, randomLong(), randomString(), now());
		final var notes = randomString();
		final var finalSamples = new DatumSamples(Map.of("a", 1), null, null);
		final var startSamples = new DatumSamples(Map.of("a", 0), null, null);
		final var meta = new GeneralDatumMetadata();

		// WHEN
		final var result = BasicDatumAuxiliaryRecord.createReset(ident, notes, finalSamples,
				startSamples, meta);

		// THEN
		// @formatter:off
		then(result)
			.as("Record created")
			.isNotNull()
			.as("Datum ident from constructor arg preserved")
			.returns(ident, from(BasicDatumAuxiliaryRecord::datumIdent))
			.as("Object kind from ident returned")
			.returns(ident.getKind(), from(BasicDatumAuxiliaryRecord::getKind))
			.as("Object ID from ident returned")
			.returns(ident.getObjectId(), from(BasicDatumAuxiliaryRecord::getObjectId))
			.as("Source ID from ident returned")
			.returns(ident.getSourceId(), from(BasicDatumAuxiliaryRecord::getSourceId))
			.as("Timestamp from ident returned")
			.returns(ident.getTimestamp(), from(BasicDatumAuxiliaryRecord::getTimestamp))
			.as("Notes from constructor arg preserved")
			.returns(notes, from(BasicDatumAuxiliaryRecord::getNotes))
			.as("Metadata from constructor arg preserved")
			.returns(meta, from(BasicDatumAuxiliaryRecord::getMetadata))
			.as("Final samples from constructor arg preserved")
			.returns(finalSamples, from(BasicDatumAuxiliaryRecord::getSamplesFinal))
			.as("Start samples from constructor arg preserved")
			.returns(startSamples, from(BasicDatumAuxiliaryRecord::getSamplesStart))
			;
		// @formatter:on
	}

	@Test
	public void equals() {
		// GIVEN
		final var aux1 = BasicDatumAuxiliaryRecord.createMark(
				new DatumIdent(Node, randomLong(), randomString(), now()), null,
				new GeneralDatumMetadata());
		final var aux2 = BasicDatumAuxiliaryRecord.createMark(
				new DatumIdent(Node, aux1.getObjectId(), aux1.getSourceId(), aux1.getTimestamp()), null,
				new GeneralDatumMetadata());
		final var aux3 = BasicDatumAuxiliaryRecord.createMark(new DatumIdent(Node, aux1.getObjectId(),
				aux1.getSourceId(), aux1.getTimestamp().plusSeconds(1)), null,
				new GeneralDatumMetadata());
		final var aux4 = BasicDatumAuxiliaryRecord.createReset(
				new DatumIdent(Node, aux1.getObjectId(), aux1.getSourceId(), aux1.getTimestamp()), null,
				new DatumSamples(), new DatumSamples(), new GeneralDatumMetadata());

		// THEN
		// @formatter:off
		then(aux1)
			.as("Aux1 == Aux2 because ident + type are equal")
			.isEqualTo(aux2)
			.as("Compare returns 0 for equality")
			.returns(0, from(r -> r.compareTo(aux2)))
			;
		then(aux1)
			.as("Aux1 != Aux3 because timestamp differs")
			.isNotEqualTo(aux3)
			.as("Compare returns -1 because ts1 < ts3")
			.returns(-1, from(r -> r.compareTo(aux3)))
			;
		then(aux1)
			.as("Aux1 != Aux4 because type differs")
			.isNotEqualTo(aux4)
			.as("Compare returns 1 because type1 > type4")
			.returns(1, from(r -> r.compareTo(aux4)))
			;
		// @formatter:on
	}

	@Test
	public void compare_types() {
		// GIVEN
		final var aux1 = BasicDatumAuxiliaryRecord.createMark(
				new DatumIdent(Node, randomLong(), randomString(), now()), null,
				new GeneralDatumMetadata());
		final var aux2 = BasicDatumAuxiliaryRecord.createReset(
				new DatumIdent(Node, aux1.getObjectId(), aux1.getSourceId(), aux1.getTimestamp()), null,
				new DatumSamples(), new DatumSamples(), new GeneralDatumMetadata());

		// THEN
		// @formatter:off
		then(aux1.compareTo(aux2))
			.as("Aux1 > Aux2 because Mark > Reset")
			.isEqualTo(1)
			;
		then(aux2.compareTo(aux1))
			.as("Aux2 < Aux1 because Reset < Mark")
			.isEqualTo(-1)
			;
		// @formatter:on
	}

	@Test
	public void compare_timestamp() {
		// GIVEN
		final var aux1 = BasicDatumAuxiliaryRecord.createMark(
				new DatumIdent(Node, randomLong(), randomString(), now()), null,
				new GeneralDatumMetadata());
		final var aux2 = BasicDatumAuxiliaryRecord.createMark(new DatumIdent(aux1.getKind(),
				aux1.getObjectId(), aux1.getSourceId(), aux1.getTimestamp().plusSeconds(1)), null,
				new GeneralDatumMetadata());

		// THEN
		// @formatter:off
		then(aux1.compareTo(aux2))
			.as("Aux1 < Aux2 because ts1 < ts2")
			.isEqualTo(-1)
			;
		then(aux2.compareTo(aux1))
			.as("Aux2 > Aux1 because ts2 > ts1")
			.isEqualTo(1)
			;
		// @formatter:on
	}

	@Test
	public void compare_sourceId() {
		// GIVEN
		final var aux1 = BasicDatumAuxiliaryRecord.createMark(
				new DatumIdent(Node, randomLong(), "a", now()), null, new GeneralDatumMetadata());
		final var aux2 = BasicDatumAuxiliaryRecord.createMark(
				new DatumIdent(aux1.getKind(), aux1.getObjectId(), "b", aux1.getTimestamp()), null,
				new GeneralDatumMetadata());

		// THEN
		// @formatter:off
		then(aux1.compareTo(aux2))
			.as("Aux1 < Aux2 because s1 < s2")
			.isEqualTo(-1)
			;
		then(aux2.compareTo(aux1))
			.as("Aux2 > Aux1 because s2 > s1")
			.isEqualTo(1)
			;
		// @formatter:on
	}

	@Test
	public void compare_objectId() {
		// GIVEN
		final var aux1 = BasicDatumAuxiliaryRecord.createMark(
				new DatumIdent(Node, randomLong(), randomString(), now()), null,
				new GeneralDatumMetadata());
		final var aux2 = BasicDatumAuxiliaryRecord.createMark(new DatumIdent(aux1.getKind(),
				aux1.getObjectId() + 1, aux1.getSourceId(), aux1.getTimestamp()), null,
				new GeneralDatumMetadata());

		// THEN
		// @formatter:off
		then(aux1.compareTo(aux2))
			.as("Aux1 < Aux2 because id1 < id2")
			.isEqualTo(-1)
			;
		then(aux2.compareTo(aux1))
			.as("Aux2 > Aux1 because id2 > id1")
			.isEqualTo(1)
			;
		// @formatter:on
	}

	@Test
	public void compare_kind() {
		// GIVEN
		final var aux1 = BasicDatumAuxiliaryRecord.createMark(
				new DatumIdent(Node, randomLong(), randomString(), now()), null,
				new GeneralDatumMetadata());
		final var aux2 = BasicDatumAuxiliaryRecord.createMark(
				new DatumIdent(Location, aux1.getObjectId(), aux1.getSourceId(), aux1.getTimestamp()),
				null, new GeneralDatumMetadata());

		// THEN
		// @formatter:off
		then(aux1.compareTo(aux2))
			.as("Aux1 < Aux2 because k1 < k2")
			.isEqualTo(-1)
			;
		then(aux2.compareTo(aux1))
			.as("Aux2 > Aux1 because k2 > k1")
			.isEqualTo(1)
			;
		// @formatter:on
	}

}
