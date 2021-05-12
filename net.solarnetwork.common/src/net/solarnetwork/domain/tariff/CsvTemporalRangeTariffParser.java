/* ==================================================================
 * CsvTemporalRangeTariffParser.java - 12/05/2021 8:46:19 PM
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

package net.solarnetwork.domain.tariff;

import static java.lang.String.format;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.time.DateTimeException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;
import net.solarnetwork.domain.tariff.Tariff.Rate;
import net.solarnetwork.util.StringUtils;

/**
 * Parse {@link TemporalRangesTariff} rows from CSV data.
 * 
 * <p>
 * The CSV resource <b>must</b> provide a header row, and the names of the
 * tariffs will be taken from there. The first 4 columns of the CSV must be:
 * </p>
 * 
 * <ol>
 * <li>month range, as month names, abbreviations, or numbers 1 - 12</li>
 * <li>day of month range, as numbers 1 - 31</li>
 * <li>day of week range, as weekday names, abbreviations, or numbers 1-7 (1 =
 * Monday)</li>
 * <li>time of date range, as hours 0-24 or ISO local times HH:MM-HH-MM</li>
 * </ol>
 * 
 * <p>
 * The remaining columns are rate values, and must be numbers that can be parsed
 * as a {@link BigDecimal}.
 * </p>
 * 
 * @author matt
 * @version 1.0
 * @since 1.71
 */
public class CsvTemporalRangeTariffParser {

	private final Locale locale;

	/**
	 * Constructor.
	 * 
	 * <p>
	 * The system locale will be used.
	 * </p>
	 */
	public CsvTemporalRangeTariffParser() {
		this(null);
	}

	/**
	 * Constructor.
	 * 
	 * @param locale
	 *        the locale to use, or {@literal null} to use the system default
	 */
	public CsvTemporalRangeTariffParser(Locale locale) {
		super();
		this.locale = (locale != null ? locale : Locale.getDefault());
	}

	/**
	 * Parse tariff rows from a reader.
	 * 
	 * @param reader
	 *        the reader
	 * @return the parsed rows, never {@literal null}
	 * @throws IOException
	 *         if any IO error occurs
	 * @throws IllegalArgumentException
	 *         if any parsing error occurs, like invalid number or range syntax
	 */
	public List<TemporalRangesTariff> parseTariffs(Reader reader) throws IOException {
		List<TemporalRangesTariff> result = new ArrayList<>();
		try (ICsvListReader csvReader = new CsvListReader(reader, CsvPreference.STANDARD_PREFERENCE)) {
			String[] headers = csvReader.getHeader(true);
			if ( headers == null || headers.length < 5 ) {
				throw new IllegalArgumentException(
						format("Not enough columns in CSV header: need at least 5 but found %d",
								(headers != null ? headers.length : 0)));
			}
			String[] ids = new String[headers.length - 4];
			for ( int i = 0, len = ids.length; i < len; i++ ) {
				ids[0] = StringUtils.simpleIdValue(headers[i + 4]);
			}
			while ( true ) {
				List<String> row = csvReader.read();
				if ( row == null ) {
					break;
				}
				if ( row.size() < 5 ) {
					throw new IllegalArgumentException(
							format("Not enough columns in CSV row %d: need at least 5 but found %d",
									csvReader.getLineNumber(), (row != null ? row.size() : 0)));
				}
				try {
					int ratesLength = Math.min(row.size() - 4, ids.length);
					List<Rate> rates = new ArrayList<>(ratesLength);
					for ( int i = 0; i < ratesLength; i++ ) {
						int j = i + 4;
						String rateString = row.get(j);
						BigDecimal rateValue = (rateString != null && !rateString.isEmpty()
								? new BigDecimal(rateString)
								: BigDecimal.ZERO);
						rates.add(new SimpleTariffRate(ids[i], headers[j], rateValue));
					}
					TemporalRangesTariff t = new TemporalRangesTariff(row.get(0), row.get(1), row.get(2),
							row.get(3), rates, locale);
					result.add(t);
				} catch ( NumberFormatException e ) {
					throw new IllegalArgumentException(
							format("Error parsing rate value in CSV row %d: %s",
									csvReader.getLineNumber(), e.getMessage()),
							e);
				} catch ( DateTimeException e ) {
					throw new IllegalArgumentException(
							format("Error parsing date range value in CSV row %d: %s",
									csvReader.getLineNumber(), e.getMessage()),
							e);
				} catch ( Exception e ) {
					throw new IllegalArgumentException(format("Error parsing CSV row %d: %s",
							csvReader.getLineNumber(), e.getMessage()), e);
				}
			}
		}
		return result;
	}

}
