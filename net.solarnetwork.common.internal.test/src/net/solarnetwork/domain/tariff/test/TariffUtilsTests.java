/* ==================================================================
 * TariffUtilsTests.java - 24/07/2024 4:42:35 pm
 *
 * Copyright 2024 SolarNetwork.net Dev Team
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

import static net.solarnetwork.domain.tariff.test.CsvTemporalRangeTariffParserTests.assertTemporalRangeTariff;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.junit.Test;
import org.springframework.util.FileCopyUtils;
import net.solarnetwork.domain.tariff.SimpleTemporalTariffSchedule;
import net.solarnetwork.domain.tariff.Tariff;
import net.solarnetwork.domain.tariff.TariffSchedule;
import net.solarnetwork.domain.tariff.TariffUtils;

/**
 * Test cases for the {@link TariffUtils} class.
 *
 * @author matt
 * @version 1.0
 */
public class TariffUtilsTests {

	private String stringResource(String resource) {
		try {
			return FileCopyUtils.copyToString(
					new InputStreamReader(getClass().getResourceAsStream(resource), "UTF-8"));
		} catch ( Exception e ) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void parseCsvTemporalRangeSchedule_unsupported() throws IOException {
		// GIVEN
		final Object csv = new Object();

		// WHEN
		TariffSchedule result = TariffUtils.parseCsvTemporalRangeSchedule(Locale.US, false, false, null,
				csv);

		// THEN
		assertThat("Null returned for unsupported input data", result, is(nullValue()));
	}

	@Test
	public void parseCsvTemporalRangeSchedule_string() throws IOException {
		// GIVEN
		final String csv = stringResource("test-tariffs-01.csv");

		// WHEN
		TariffSchedule result = TariffUtils.parseCsvTemporalRangeSchedule(Locale.US, false, false, null,
				csv);

		// THEN
		assertThat("SimpleTemporalTariffSchedule provided", result,
				is(instanceOf(SimpleTemporalTariffSchedule.class)));
		List<Tariff> tariffs = new ArrayList<>();
		tariffs.addAll(result.rules());
		assertThat("All rules parsed", tariffs, hasSize(4));
		assertTemporalRangeTariff("Row 1", tariffs.get(0), "January-December", null, "Mon-Fri", "0-8",
				"10.48");
		assertTemporalRangeTariff("Row 2", tariffs.get(1), "January-December", null, "Mon-Fri", "8-24",
				"11.00");
		assertTemporalRangeTariff("Row 3", tariffs.get(2), "January-December", null, "Sat-Sun", "0-8",
				"9.19");
		assertTemporalRangeTariff("Row 4", tariffs.get(3), "January-December", null, "Sat-Sun", "8-24",
				"11.21");
	}

	@Test
	public void parseCsvTemporalRangeSchedule_reader() throws IOException {
		try (Reader csv = new InputStreamReader(getClass().getResourceAsStream("test-tariffs-01.csv"),
				"UTF-8")) {

			// WHEN
			TariffSchedule result = TariffUtils.parseCsvTemporalRangeSchedule(Locale.US, false, false,
					null, csv);

			// THEN
			assertThat("SimpleTemporalTariffSchedule provided", result,
					is(instanceOf(SimpleTemporalTariffSchedule.class)));
			List<Tariff> tariffs = new ArrayList<>();
			tariffs.addAll(result.rules());
			assertThat("All rules parsed", tariffs, hasSize(4));
			assertTemporalRangeTariff("Row 1", tariffs.get(0), "January-December", null, "Mon-Fri",
					"0-8", "10.48");
			assertTemporalRangeTariff("Row 2", tariffs.get(1), "January-December", null, "Mon-Fri",
					"8-24", "11.00");
			assertTemporalRangeTariff("Row 3", tariffs.get(2), "January-December", null, "Sat-Sun",
					"0-8", "9.19");
			assertTemporalRangeTariff("Row 4", tariffs.get(3), "January-December", null, "Sat-Sun",
					"8-24", "11.21");
		}
	}

	@Test
	public void parseCsvTemporalRangeSchedule_array() throws IOException {
		// GIVEN
		final String[][] csv = new String[][] {
				new String[] { "Month", "Day Range", "Day of Week Range", "Hour of Day Range", "E" },
				new String[] { "January-December", null, "Mon-Fri", "0-8", "10.48" },
				new String[] { "January-December", null, "Sat-Sun", "0-8", "9.19" } };

		// WHEN
		TariffSchedule result = TariffUtils.parseCsvTemporalRangeSchedule(Locale.US, false, false, null,
				csv);

		// THEN
		assertThat("SimpleTemporalTariffSchedule provided", result,
				is(instanceOf(SimpleTemporalTariffSchedule.class)));
		List<Tariff> tariffs = new ArrayList<>();
		tariffs.addAll(result.rules());
		assertThat("All rules parsed", tariffs, hasSize(2));
		assertTemporalRangeTariff("Row 1", tariffs.get(0), "January-December", null, "Mon-Fri", "0-8",
				"10.48");
		assertTemporalRangeTariff("Row 3", tariffs.get(1), "January-December", null, "Sat-Sun", "0-8",
				"9.19");
	}

}
