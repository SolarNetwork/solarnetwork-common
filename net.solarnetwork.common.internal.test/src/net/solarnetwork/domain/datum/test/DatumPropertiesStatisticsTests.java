/* ==================================================================
 * DatumPropertiesStatisticsTests.java - 6/03/2026 5:40:31 pm
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

import static net.solarnetwork.util.NumberUtils.decimalArray;
import static org.assertj.core.api.BDDAssertions.from;
import static org.assertj.core.api.BDDAssertions.then;
import java.math.BigDecimal;
import org.junit.Test;
import net.solarnetwork.domain.datum.DatumPropertiesStatistics;

/**
 * Test cases for the {@link DatumPropertiesStatistics} class.
 *
 * @author matt
 * @version 1.0
 */
public class DatumPropertiesStatisticsTests {

	@Test
	public void empty() {
		// WHEN
		final var s = DatumPropertiesStatistics.emptyStatistics();

		// @formatter:off
		then(s)
			.as("Instance created")
			.isNotNull()
			.as("Is empty")
			.returns(true, from(DatumPropertiesStatistics::isEmpty))
			.as("Length is 0")
			.returns(0, from(DatumPropertiesStatistics::getLength))
			;
		// @formatter:on
	}

	@Test
	public void nonEmpty() {
		// WHEN
		final var s = DatumPropertiesStatistics.emptyStatistics();
		s.setInstantaneous(new BigDecimal[][] { decimalArray("1", "2", "3") });

		// @formatter:off
		then(s)
			.as("Instance created")
			.isNotNull()
			.as("Is not empty")
			.returns(false, from(DatumPropertiesStatistics::isEmpty))
			.as("Length is 1")
			.returns(1, from(DatumPropertiesStatistics::getLength))
			;
		// @formatter:on
	}

}
