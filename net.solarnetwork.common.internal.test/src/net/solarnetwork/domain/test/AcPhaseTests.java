/* ==================================================================
 * AcPhaseTests.java - 25/02/2019 7:56:58 am
 * 
 * Copyright 2019 SolarNetwork.net Dev Team
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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import net.solarnetwork.domain.AcPhase;

/**
 * Test cases for the {@link AcPhase} class.
 * 
 * @author matt
 * @version 1.0
 */
public class AcPhaseTests {

	@Test
	public void withLineKey() {
		assertThat("A", AcPhase.PhaseA.withLineKey("foo"), equalTo("foo_ab"));
		assertThat("B", AcPhase.PhaseB.withLineKey("foo"), equalTo("foo_bc"));
		assertThat("C", AcPhase.PhaseC.withLineKey("foo"), equalTo("foo_ca"));
		assertThat("Total", AcPhase.Total.withLineKey("foo"), equalTo("foo_t"));
	}

	@Test
	public void withKey() {
		assertThat("A", AcPhase.PhaseA.withKey("foo"), equalTo("foo_a"));
		assertThat("B", AcPhase.PhaseB.withKey("foo"), equalTo("foo_b"));
		assertThat("C", AcPhase.PhaseC.withKey("foo"), equalTo("foo_c"));
		assertThat("Total", AcPhase.Total.withKey("foo"), equalTo("foo_t"));
	}
}
