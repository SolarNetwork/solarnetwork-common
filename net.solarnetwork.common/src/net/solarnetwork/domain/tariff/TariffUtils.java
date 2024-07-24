/* ==================================================================
 * TariffUtils.java - 24/07/2024 4:32:33 pm
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

package net.solarnetwork.domain.tariff;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.solarnetwork.util.StringUtils;

/**
 * Tariff utilities.
 *
 * @author matt
 * @version 1.0
 * @since 3.16
 */
public final class TariffUtils {

	private TariffUtils() {
		// not available
	}

	/**
	 * Parse a CSV temporal range tariff schedule.
	 *
	 * <p>
	 * This method can parse either CSV data in the form of a string or
	 * {@link Reader}, or a {@code String[][]} of data structured in the same
	 * rows and columns as the CSV form.
	 *
	 * @param locale
	 *        the locale to use when parsing the CSV; if not provided the
	 *        default system locale will be used
	 * @param preserveRateCase
	 *        {@literal true} to preserve the case of rate names parsed from the
	 *        CSV header row
	 * @param firstMatchOnly
	 *        {@literal true} if only the first tariff rule to match should be
	 *        returned
	 * @param evaluator
	 *        an optional evaluator to use; if not provided then
	 *        {@link net.solarnetwork.domain.tariff.SimpleTemporalRangesTariffEvaluator}
	 *        will be used
	 * @param scheduleData
	 *        a {@code String}, {@link Reader}, or {@code String[]} instance of
	 *        CSV data
	 * @return the parsed schedule, or {@literal null} if {@code scheduleData}
	 *         is not a {@code String}, {@link Reader}, or {@code String[]}
	 *         instance
	 * @throws IOException
	 *         if any parsing error occurs
	 * @see CsvTemporalRangeTariffParser
	 */
	public static TariffSchedule parseCsvTemporalRangeSchedule(final Locale locale,
			final boolean preserveRateCase, final boolean firstMatchOnly,
			final TemporalRangesTariffEvaluator evaluator, Object scheduleData) throws IOException {
		List<TemporalRangesTariff> tariffs;
		if ( scheduleData instanceof String || scheduleData instanceof Reader ) {
			// parse as CSV
			tariffs = new CsvTemporalRangeTariffParser(locale, preserveRateCase)
					.parseTariffs(scheduleData instanceof Reader ? (Reader) scheduleData
							: new StringReader(scheduleData.toString()));
		} else if ( scheduleData instanceof String[][] ) {
			tariffs = new ArrayList<>();
			String[][] data = (String[][]) scheduleData;
			if ( data.length > 1 ) {
				String[] headers = data[0];
				if ( headers.length < 5 ) {
					return null;
				}
				for ( int i = 1; i < data.length; i++ ) {
					String[] row = data[i];
					if ( row.length < 5 ) {
						continue;
					}
					List<Tariff.Rate> rates = new ArrayList<>(row.length - 4);
					for ( int j = 4; j < row.length; j++ ) {
						String name = headers[j];
						String id = StringUtils.simpleIdValue(name, preserveRateCase);
						rates.add(new SimpleTariffRate(id, name, new BigDecimal(row[j])));
					}
					TemporalRangesTariff tariff = new TemporalRangesTariff(row[0], row[1], row[2],
							row[3], rates, locale);
					tariffs.add(tariff);
				}
			}
		} else {
			return null;
		}
		SimpleTemporalTariffSchedule s = new SimpleTemporalTariffSchedule(tariffs, evaluator);
		s.setFirstMatchOnly(firstMatchOnly);
		return s;
	}

}
