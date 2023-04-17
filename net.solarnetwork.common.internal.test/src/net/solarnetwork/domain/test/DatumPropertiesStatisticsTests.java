/* ==================================================================
 * DatumPropertiesStatisticsTests.java - 29/06/2022 2:13:20 pm
 * 
 * Copyright 2022 SolarNetwork.net Dev Team
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

import static net.solarnetwork.util.NumberUtils.decimalArray;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
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
	public void accessors() {
		// GIVEN
		// @formatter:off
		DatumPropertiesStatistics stats = DatumPropertiesStatistics.statisticsOf(
				new BigDecimal[][] { 
					decimalArray("10", "1.2", "3.4"),
					decimalArray("5", "0.2", "2.2") },
				new BigDecimal[][] { 
					decimalArray("123", "12345", "23456"), 
					decimalArray("234", "9876", "8765"),
					decimalArray("345" ,"1", "2") });
		// @formatter:on

		// THEN
		assertThat("Total prop length", stats.getLength(), is(equalTo(5)));

		assertThat("Instantaneous length", stats.getInstantaneousLength(), is(equalTo(2)));
		assertThat("Instantaneous 1 count", stats.getInstantaneousCount(1),
				is(equalTo(new BigDecimal("5"))));
		assertThat("Instantaneous 0 minimum", stats.getInstantaneousMinimum(0),
				is(equalTo(new BigDecimal("1.2"))));
		assertThat("Instantaneous 1 maximum", stats.getInstantaneousMaximum(1),
				is(equalTo(new BigDecimal("2.2"))));

		assertThat("Accumulating length", stats.getAccumulatingLength(), is(equalTo(3)));
		assertThat("Accumulating 1 diff", stats.getAccumulatingDifference(1),
				is(equalTo(new BigDecimal("234"))));
		assertThat("Accumulating 1 start", stats.getAccumulatingStart(1),
				is(equalTo(new BigDecimal("9876"))));
		assertThat("Accumulating 2 end", stats.getAccumulatingEnd(2), is(equalTo(new BigDecimal("2"))));
	}

}
