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
import static java.time.format.TextStyle.SHORT;
import static net.solarnetwork.util.DateUtils.formatRange;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.CsvListWriter;
import org.supercsv.io.ICsvListReader;
import org.supercsv.io.ICsvListWriter;
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
 * <li>time of day range, as hours 0-24 or ISO local times HH:MM-HH-MM; if a
 * singleton hour value is provided it will be converted to a range of one hour
 * starting at that hour, for example {@code 12} becomes {@code 12-13}.</li>
 * </ol>
 *
 * <p>
 * The remaining columns are rate values, and must be numbers that can be parsed
 * as a {@link BigDecimal}.
 * </p>
 *
 * @author matt
 * @version 1.1
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
				ids[i] = StringUtils.simpleIdValue(headers[i + 4]);
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
						if ( rateString != null && !rateString.isEmpty() ) {
							BigDecimal rateValue = new BigDecimal(rateString);
							rates.add(new SimpleTariffRate(ids[i], headers[j], rateValue));
						}
					}
					// look for MOD singleton hour; convert to hour range if found
					String modRange = row.get(3);
					if ( modRange.indexOf('-') < 0 && modRange.indexOf(':') < 0 ) {
						try {
							int hod = Integer.parseInt(modRange);
							if ( hod < 24 ) {
								modRange = hod + "-" + (hod + 1);
							}
						} catch ( NumberFormatException e ) {
							// ignore and continue
						}
					}
					TemporalRangesTariff t = new TemporalRangesTariff(row.get(0), row.get(1), row.get(2),
							modRange, rates, locale);
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

	/**
	 * Encode a list of tariffs as CSV data.
	 *
	 * <p>
	 * The range columns are formatted using abbreviations if possible. The time
	 * range column will be formatted using integer hours if possible, otherwise
	 * {@literal HH:MM} syntax.
	 * </p>
	 *
	 * @param tariffs
	 *        the tariffs to encode
	 * @param writer
	 *        the writer to write the CSV data to
	 * @throws IOException
	 *         if any IO error occurs
	 * @throws IllegalArgumentException
	 *         if any formatting error occurs
	 */
	public void formatCsv(List<TemporalRangesTariff> tariffs, Writer writer) throws IOException {
		if ( tariffs == null || tariffs.isEmpty() ) {
			return;
		}
		List<String> rateNames = extractRateDescriptions(tariffs);
		String[] headers = new String[4 + rateNames.size()];
		headers[0] = "Month";
		headers[1] = "Day";
		headers[2] = "Weekday";
		headers[3] = "Time";
		for ( int i = 0, len = rateNames.size(); i < len; i++ ) {
			headers[i + 4] = rateNames.get(i);
		}
		try (ICsvListWriter csvWriter = new CsvListWriter(writer, CsvPreference.STANDARD_PREFERENCE)) {
			csvWriter.writeHeader(headers);
			try {
				// change rate headers to IDs for faster lookup while processing rows
				for ( int i = 4; i < headers.length; i++ ) {
					headers[i] = StringUtils.simpleIdValue(headers[i]);
				}

				for ( TemporalRangesTariff tariff : tariffs ) {
					encodeToCsv(tariff, headers, csvWriter);
				}
			} catch ( DateTimeException e ) {
				throw new IllegalArgumentException(
						format("Error formatting date range value in CSV row %d: %s",
								csvWriter.getLineNumber(), e.getMessage()),
						e);
			} catch ( Exception e ) {
				throw new IllegalArgumentException(format("Error formatting CSV row %d: %s",
						csvWriter.getLineNumber(), e.getMessage()), e);
			}
		}
	}

	private void encodeToCsv(TemporalRangesTariff tariff, String[] headers, ICsvListWriter csvWriter)
			throws IOException {
		String[] row = new String[headers.length];
		row[0] = formatRange(ChronoField.MONTH_OF_YEAR, tariff.getMonthRange(), locale, SHORT);
		row[1] = formatRange(ChronoField.DAY_OF_MONTH, tariff.getDayOfMonthRange(), locale, SHORT);
		row[2] = formatRange(ChronoField.DAY_OF_WEEK, tariff.getDayOfWeekRange(), locale, SHORT);
		row[3] = formatRange(ChronoField.MINUTE_OF_DAY, tariff.getMinuteOfDayRange(), locale, SHORT);
		for ( int i = 4; i < headers.length; i++ ) {
			Rate r = tariff.getRates().get(headers[i]);
			row[i] = (r != null ? r.getAmount().toPlainString() : null);
		}
		csvWriter.write(row);
	}

	private List<String> extractRateDescriptions(List<TemporalRangesTariff> tariffs) {
		return new ArrayList<>(tariffs.stream().flatMap(t -> t.getRates().values().stream())
				.map(Rate::getDescription).collect(Collectors.toCollection(LinkedHashSet::new)));
	}

}
