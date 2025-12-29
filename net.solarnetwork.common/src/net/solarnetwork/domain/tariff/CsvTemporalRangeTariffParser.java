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
import de.siegmar.fastcsv.reader.CommentStrategy;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRecord;
import de.siegmar.fastcsv.reader.CsvRecordHandler;
import de.siegmar.fastcsv.reader.FieldModifiers;
import de.siegmar.fastcsv.writer.CsvWriter;
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
 * @version 1.3
 * @since 1.71
 */
public class CsvTemporalRangeTariffParser {

	private final Locale locale;
	private final boolean preserveRateCase;

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
		this(locale, false);
	}

	/**
	 * Constructor.
	 *
	 * @param locale
	 *        the locale to use, or {@literal null} to use the system default
	 * @param preserveRateCase
	 *        {@literal true} to preserve the case of rate names
	 * @since 1.2
	 */
	public CsvTemporalRangeTariffParser(Locale locale, boolean preserveRateCase) {
		super();
		this.locale = (locale != null ? locale : Locale.getDefault());
		this.preserveRateCase = preserveRateCase;
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
	public List<ChronoFieldsTariff> parseTariffs(Reader reader) throws IOException {
		List<ChronoFieldsTariff> result = new ArrayList<>();
		try (CsvReader<CsvRecord> csvReader = CsvReader.builder().allowMissingFields(true)
				.allowExtraFields(true).commentStrategy(CommentStrategy.SKIP)
				.build(CsvRecordHandler.builder().fieldModifier(FieldModifiers.TRIM).build(), reader)) {
			List<String> headers = null;
			String[] ids = null;
			for ( CsvRecord row : csvReader ) {
				if ( headers == null ) {
					headers = row.getFields();
					if ( headers.size() < 5 ) {
						throw new IllegalArgumentException(
								format("Not enough columns in CSV header: need at least 5 but found %d",
										(headers != null ? headers.size() : 0)));
					}
					ids = new String[headers.size() - 4];
					for ( int i = 0, len = ids.length; i < len; i++ ) {
						ids[i] = StringUtils.simpleIdValue(headers.get(i + 4), preserveRateCase);
					}
					continue;
				}

				if ( row.getFieldCount() < 5 ) {
					throw new IllegalArgumentException(
							format("Not enough columns in CSV row %d: need at least 5 but found %d",
									row.getStartingLineNumber(), row.getFieldCount()));
				}
				try {
					int ratesLength = Math.min(row.getFieldCount() - 4, ids.length);
					List<Rate> rates = new ArrayList<>(ratesLength);
					for ( int i = 0; i < ratesLength; i++ ) {
						int j = i + 4;
						String rateString = row.getField(j);
						if ( rateString != null && !rateString.isEmpty() ) {
							BigDecimal rateValue = new BigDecimal(rateString);
							rates.add(new SimpleTariffRate(ids[i], headers.get(j), rateValue));
						}
					}
					// look for MOD singleton hour; convert to hour range if found
					String modRange = row.getField(3);
					if ( modRange != null && modRange.indexOf('-') < 0 && modRange.indexOf(':') < 0 ) {
						try {
							int hod = Integer.parseInt(modRange);
							if ( hod < 24 ) {
								modRange = hod + "-" + (hod + 1);
							}
						} catch ( NumberFormatException e ) {
							// ignore and continue
						}
					}
					TemporalRangeSetsTariff t = new TemporalRangeSetsTariff(row.getField(0),
							row.getField(1), row.getField(2), modRange, rates, locale);
					result.add(t);
				} catch ( NumberFormatException e ) {
					throw new IllegalArgumentException(
							format("Error parsing rate value in CSV row %d: %s",
									row.getStartingLineNumber(), e.getMessage()),
							e);
				} catch ( DateTimeException e ) {
					throw new IllegalArgumentException(
							format("Error parsing date range value in CSV row %d: %s",
									row.getStartingLineNumber(), e.getMessage()),
							e);
				} catch ( Exception e ) {
					throw new IllegalArgumentException(format("Error parsing CSV row %d: %s",
							row.getStartingLineNumber(), e.getMessage()), e);
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
	public void formatCsv(List<ChronoFieldsTariff> tariffs, Writer writer) throws IOException {
		if ( tariffs == null || tariffs.isEmpty() ) {
			return;
		}
		List<String> rateNames = extractRateDescriptions(tariffs);
		String[] header = new String[4 + rateNames.size()];
		header[0] = "Month";
		header[1] = "Day";
		header[2] = "Weekday";
		header[3] = "Time";
		for ( int i = 0, len = rateNames.size(); i < len; i++ ) {
			header[i + 4] = rateNames.get(i);
		}
		int rowNumber = 1;
		try (CsvWriter csvWriter = CsvWriter.builder().build(writer)) {
			csvWriter.writeRecord(header);
			try {
				// change rate headers to IDs for faster lookup while processing rows
				for ( int i = 4; i < header.length; i++ ) {
					header[i] = StringUtils.simpleIdValue(header[i]);
				}

				for ( ChronoFieldsTariff tariff : tariffs ) {
					rowNumber++;
					encodeToCsv(tariff, header, csvWriter);
				}
			} catch ( DateTimeException e ) {
				throw new IllegalArgumentException(
						format("Error formatting date range value in CSV row %d: %s", rowNumber,
								e.getMessage()),
						e);
			} catch ( Exception e ) {
				throw new IllegalArgumentException(
						format("Error formatting CSV row %d: %s", rowNumber, e.getMessage()), e);
			}
		}
	}

	private void encodeToCsv(ChronoFieldsTariff tariff, String[] headers, CsvWriter csvWriter)
			throws IOException {
		String[] row = new String[headers.length];
		row[0] = formatRange(ChronoField.MONTH_OF_YEAR,
				tariff.rangeForChronoField(ChronoField.MONTH_OF_YEAR), locale, SHORT);
		row[1] = formatRange(ChronoField.DAY_OF_MONTH,
				tariff.rangeForChronoField(ChronoField.DAY_OF_MONTH), locale, SHORT);
		row[2] = formatRange(ChronoField.DAY_OF_WEEK,
				tariff.rangeForChronoField(ChronoField.DAY_OF_WEEK), locale, SHORT);
		row[3] = formatRange(ChronoField.MINUTE_OF_DAY,
				tariff.rangeForChronoField(ChronoField.MINUTE_OF_DAY), locale, SHORT);
		for ( int i = 4; i < headers.length; i++ ) {
			Rate r = tariff.getRates().get(headers[i]);
			row[i] = (r != null ? r.getAmount().toPlainString() : null);
		}
		csvWriter.writeRecord(row);
	}

	private List<String> extractRateDescriptions(List<ChronoFieldsTariff> tariffs) {
		return new ArrayList<>(tariffs.stream().flatMap(t -> t.getRates().values().stream())
				.map(Rate::getDescription).collect(Collectors.toCollection(LinkedHashSet::new)));
	}

	/**
	 * Get the configured locale.
	 *
	 * @return the locale
	 * @since 1.2
	 */
	public final Locale getLocale() {
		return locale;
	}

	/**
	 * Get the "preserve rate case" mode.
	 *
	 * @return {@literal true} to preserve the case of rate names
	 * @since 1.2
	 */
	public final boolean isPreserveRateCase() {
		return preserveRateCase;
	}

}
