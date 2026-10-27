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
import org.jspecify.annotations.Nullable;
import net.solarnetwork.util.StringUtils;

/**
 * Tariff utilities.
 *
 * @author matt
 * @version 1.2
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
	 *        a {@code String} or {@link Reader} of CSV data, or the CSV rows as
	 *        a {@code String[][]} or a {@code List} of {@code List} (like a
	 *        JSON array of arrays in metadata)
	 * @return the parsed schedule, or {@code null} if {@code scheduleData} is
	 *         not one of the supported types, or its rows have no header row of
	 *         at least 5 columns
	 * @throws IOException
	 *         if any parsing error occurs
	 * @see CsvTemporalRangeTariffParser
	 */
	public static @Nullable TariffSchedule parseCsvTemporalRangeSchedule(final @Nullable Locale locale,
			final boolean preserveRateCase, final boolean firstMatchOnly,
			final @Nullable TemporalTariffEvaluator evaluator, Object scheduleData) throws IOException {
		return parseCsvTemporalRangeSchedule(locale, preserveRateCase, firstMatchOnly, false, evaluator,
				scheduleData);
	}

	/**
	 * Parse a CSV temporal range tariff schedule, optionally rejecting ranges
	 * that would otherwise be reordered.
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
	 * @param strict
	 *        {@literal true} to reject descending ranges, and empty or
	 *        out-of-day time ranges, instead of reordering them
	 * @param evaluator
	 *        an optional evaluator to use; if not provided then
	 *        {@link net.solarnetwork.domain.tariff.SimpleTemporalRangesTariffEvaluator}
	 *        will be used
	 * @param scheduleData
	 *        a {@code String} or {@link Reader} of CSV data, or the CSV rows as
	 *        a {@code String[][]} or a {@code List} of {@code List} (like a
	 *        JSON array of arrays in metadata)
	 * @return the parsed schedule, or {@code null} if {@code scheduleData} is
	 *         not one of the supported types, or its rows have no header row of
	 *         at least 5 columns
	 * @throws IOException
	 *         if any parsing error occurs
	 * @see CsvTemporalRangeTariffParser
	 * @since 1.2
	 */
	public static @Nullable TariffSchedule parseCsvTemporalRangeSchedule(final @Nullable Locale locale,
			final boolean preserveRateCase, final boolean firstMatchOnly, final boolean strict,
			final @Nullable TemporalTariffEvaluator evaluator, Object scheduleData) throws IOException {
		List<ChronoFieldsTariff> tariffs;
		if ( scheduleData instanceof String || scheduleData instanceof Reader ) {
			// parse as CSV
			tariffs = new CsvTemporalRangeTariffParser(locale, preserveRateCase, strict)
					.parseTariffs(scheduleData instanceof Reader ? (Reader) scheduleData
							: new StringReader(scheduleData.toString()));
		} else if ( scheduleData instanceof String[][] data ) {
			tariffs = new ArrayList<>();
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
						if ( id == null ) {
							continue;
						}
						rates.add(new SimpleTariffRate(id, name, new BigDecimal(row[j])));
					}
					TemporalRangeSetsTariff tariff = new TemporalRangeSetsTariff(row[0], row[1], row[2],
							wholeHourRange(row[3]), rates, locale, strict);
					tariffs.add(tariff);
				}
			}
		} else if ( scheduleData instanceof List<?> data ) {
			// assume List of List here (should be List<List<String>>)
			tariffs = new ArrayList<>();
			if ( !data.isEmpty() ) {
				final List<?> headers = data.get(0) instanceof List<?> l ? l : null;
				if ( headers == null || headers.size() < 5 ) {
					return null;
				}
				for ( int i = 1, rowCount = data.size(); i < rowCount; i++ ) {
					final List<?> row = data.get(i) instanceof List<?> l ? l : null;
					if ( row == null || row.size() < 5 ) {
						continue;
					}
					// like CsvTemporalRangeTariffParser, ignore values without a header and empty rates
					final int colCount = Math.min(row.size(), headers.size());
					final List<Tariff.Rate> rates = new ArrayList<>(colCount - 4);
					for ( int j = 4; j < colCount; j++ ) {
						final String name = stringValue(headers.get(j));
						final String id = (name != null
								? StringUtils.simpleIdValue(name, preserveRateCase)
								: null);
						final String val = stringValue(row.get(j));
						if ( name == null || id == null || val == null || val.isEmpty() ) {
							continue;
						}
						rates.add(new SimpleTariffRate(id, name, new BigDecimal(val)));
					}
					tariffs.add(new TemporalRangeSetsTariff(stringValue(row.get(0)),
							stringValue(row.get(1)), stringValue(row.get(2)),
							wholeHourRange(stringValue(row.get(3))), rates, locale, strict));
				}
			}
		} else {
			return null;
		}
		SimpleTemporalTariffSchedule s = new SimpleTemporalTariffSchedule(tariffs, evaluator);
		s.setFirstMatchOnly(firstMatchOnly);
		return s;
	}

	private static @Nullable String stringValue(@Nullable Object value) {
		// trim like the CSV parser does
		return (value != null ? value.toString().trim() : null);
	}

	/**
	 * Widen a whole hour time-of-day range, like {@code 8}, to that hour, like
	 * {@code 8-9}.
	 *
	 * @param range
	 *        the time-of-day range
	 * @return the range, widened if it is a single whole hour from 0 to 23
	 * @since 1.1
	 */
	public static @Nullable String wholeHourRange(@Nullable String range) {
		if ( range != null && range.indexOf('-') < 0 && range.indexOf(':') < 0 ) {
			try {
				int hod = Integer.parseInt(range);
				if ( hod < 24 ) {
					return hod + "-" + (hod + 1);
				}
			} catch ( NumberFormatException e ) {
				// ignore and continue
			}
		}
		return range;
	}

}
