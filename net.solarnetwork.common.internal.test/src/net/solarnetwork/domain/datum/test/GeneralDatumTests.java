/* ==================================================================
 * GeneralDatumTests.java - 8/03/2026 10:05:26 am
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
import static net.solarnetwork.domain.datum.ObjectDatumKind.Node;
import static net.solarnetwork.test.CommonTestUtils.randomLong;
import static net.solarnetwork.test.CommonTestUtils.randomString;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;
import net.solarnetwork.domain.datum.DatumId;
import net.solarnetwork.domain.datum.DatumSamples;
import net.solarnetwork.domain.datum.GeneralDatum;

/**
 * Test cases for the {@link GeneralDatum} class.
 *
 * @author matt
 * @version 1.0
 */
public class GeneralDatumTests {

	@Test
	public void datumId_provided() {
		// GIVEN
		final var pk = new DatumId(Node, randomLong(), randomString(), now());
		final var d = new GeneralDatum(pk, new DatumSamples());

		// WHEN
		final var result = d.datumId();

		then(result).as("Given primary key instance returned").isSameAs(pk);
		then(d.getId()).as("Given primary key instance returned").isSameAs(pk);
	}

	@Test
	public void datumId_created() {
		// GIVEN
		final var pk = new DatumId(Node, randomLong(), randomString(), now());
		final var d = GeneralDatum.nodeDatum(pk.getObjectId(), pk.getSourceId(), pk.getTimestamp(),
				new DatumSamples());

		// WHEN
		final var result = d.datumId();

		then(result).as("Primary key matches given properties").isEqualTo(pk);
		then(d.getId()).as("Same primary key instance resturned").isSameAs(result);
	}

}
