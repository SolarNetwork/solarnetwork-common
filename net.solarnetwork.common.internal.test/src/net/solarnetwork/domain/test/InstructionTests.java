/* ==================================================================
 * InstructionTests.java - 24/06/2026 8:00:35 am
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

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.within;
import org.junit.Test;
import net.solarnetwork.domain.Instruction;

/**
 * Test cases for the {@link Instruction} interface.
 *
 * @author matt
 * @version 1.0
 */
public class InstructionTests {

	@Test
	public void localId_seed() {
		// GIVEN
		final long now = System.currentTimeMillis();

		// WHEN
		final Long result = Instruction.localId();

		// THEN
		// @formatter:off
		then(result)
			.as("Local ID generated")
			.isNotNull()
			.as("Was seeded with current time")
			.isCloseTo(now, within(1000L))
			;
		// @formatter:on
	}

	@Test
	public void localId_sequential() {
		// GIVEN
		final long start = Instruction.localId();

		// WHEN
		for ( long i = 1; i <= 100; i++ ) {
			final long result = Instruction.localId();
			then(result).as("Local ID is incremented").isEqualTo(start + i);
		}
	}

}
