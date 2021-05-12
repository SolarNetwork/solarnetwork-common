/* ==================================================================
 * CsvTemporalRangeTariffParserTests.java - 12/05/2021 9:36:03 PM
 * 
 * Copyright 2021 SolarNetwork.net Dev Team
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

package net.solarnetwork.domain.tariff.test;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import org.junit.Test;
import net.solarnetwork.domain.tariff.CsvTemporalRangeTariffParser;
import net.solarnetwork.domain.tariff.TemporalRangesTariff;

/**
 * Test cases for the {@link CsvTemporalRangeTariffParser} class.
 * 
 * @author matt
 * @version 1.0
 */
public class CsvTemporalRangeTariffParserTests {

	@Test
	public void parse_example() throws IOException {
		// GIVEN
		CsvTemporalRangeTariffParser p = new CsvTemporalRangeTariffParser();

		// WHEN
		try (InputStreamReader r = new InputStreamReader(
				getClass().getResourceAsStream("test-tariffs.csv"), "UTF-8")) {
			List<TemporalRangesTariff> tariffs = p.parseTariffs(r);

			assertThat("Tariffs parsed", tariffs, hasSize(4));
			// TODO more checks
		}
	}

}
