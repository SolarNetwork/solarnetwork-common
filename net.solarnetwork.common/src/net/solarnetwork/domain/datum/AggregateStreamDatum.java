/* ==================================================================
 * AggregateStreamDatum.java - 29/06/2022 10:12:19 am
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

package net.solarnetwork.domain.datum;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.jspecify.annotations.Nullable;
import net.solarnetwork.util.DateUtils;

/**
 * API for an object that represents an aggregation of individual
 * {@link StreamDatum} within a unique stream over a specific period of time and
 * a set of property values and associated aggregate statistics.
 *
 * @author matt
 * @version 1.1
 */
public interface AggregateStreamDatum extends StreamDatum {

	/**
	 * A property name to use for an end timestamp.
	 *
	 * @since 1.1
	 */
	public static final String END_DATE_PROPERTY_NAME = "endDate";

	/**
	 * A property name to use for a local timestamp.
	 *
	 * @since 1.1
	 */
	public static final String LOCAL_DATE_PROPERTY_NAME = "localDate";

	/**
	 * A property name to use for a local end timestamp.
	 *
	 * @since 1.1
	 */
	public static final String LOCAL_END_DATE_PROPERTY_NAME = "localEndDate";

	/**
	 * Get the associated timestamp for the end of the aggregate period covered
	 * by this datum (exclusive).
	 *
	 * <p>
	 * The {@link #getTimestamp()} value represents the start of the aggregate
	 * period covered by this datum (inclusive).
	 * </p>
	 *
	 * @return the end timestamp for this datum
	 */
	@Nullable
	Instant getEndTimestamp();

	/**
	 * Get the property statistics.
	 *
	 * @return the statistics
	 */
	DatumPropertiesStatistics getStatistics();

	@Override
	default GeneralDatum toGeneralDatum(ObjectDatumStreamMetadata meta) {
		return toGeneralDatum(meta, Aggregation.None, null);
	}

	/**
	 * Convert this stream datum into a general datum.
	 *
	 * @param meta
	 *        the stream metadata associated with this stream datum
	 * @param aggregation
	 *        the aggregation level represented by this stream datum
	 * @return the general datum
	 * @since 1.1
	 * @see #toGeneralDatum(ObjectDatumStreamMetadata, Aggregation,
	 *      DatumReadingType)
	 */
	default GeneralDatum toGeneralDatum(ObjectDatumStreamMetadata meta, Aggregation aggregation) {
		return toGeneralDatum(meta, aggregation, null);
	}

	/**
	 * Convert this stream datum into a general datum.
	 *
	 * <p>
	 * A {@code localDate} status sample value will be added with the local
	 * version of this datum's timestamp. Properties for all instantaneous
	 * statistics will also be added.
	 * </p>
	 *
	 * <p>
	 * If {@code readingType} is non-{@code null}, then accumulating statistics
	 * will be added to the result, and if {@link #getEndTimestamp()} is
	 * non-{@code null} then {@code endDate} and {@code localEndDate} status
	 * properties will also be added.
	 * </p>
	 *
	 * @param meta
	 *        the stream metadata associated with this stream datum
	 * @param aggregation
	 *        the aggregation level represented by this stream datum
	 * @param readingType
	 *        the reading type represented by this stream datum
	 * @return the general datum
	 * @since 1.1
	 */
	default GeneralDatum toGeneralDatum(ObjectDatumStreamMetadata meta, Aggregation aggregation,
			final @Nullable DatumReadingType readingType) {
		final GeneralDatum result = StreamDatum.super.toGeneralDatum(meta);
		final DatumSamples samples = result.getSamples();
		final ZoneId zone = switch (aggregation) {
			// the XofY aggregates implicitly use UTC for their dates
			case DayOfWeek, DayOfYear, HourOfDay, HourOfYear, SeasonalDayOfWeek, SeasonalHourOfDay, WeekOfYear -> ZoneOffset.UTC;
			default -> meta.getTimeZoneId() != null ? ZoneId.of(meta.getTimeZoneId()) : ZoneOffset.UTC;
		};

		samples.putStatusSampleValue(LOCAL_DATE_PROPERTY_NAME,
				DateUtils.ISO_DATE_TIME_ALT_UTC.format(getTimestamp().atZone(zone).toLocalDateTime()));

		meta.populateInstantaneousStatistics(samples, getStatistics());
		if ( readingType != null ) {
			meta.populateAccumulatingStatistics(samples, getStatistics());
			if ( getEndTimestamp() != null ) {
				samples.putStatusSampleValue(END_DATE_PROPERTY_NAME,
						DateUtils.ISO_DATE_TIME_ALT_UTC.format(getEndTimestamp()));
				samples.putStatusSampleValue(LOCAL_END_DATE_PROPERTY_NAME,
						DateUtils.ISO_DATE_TIME_ALT_UTC
								.format(getEndTimestamp().atZone(zone).toLocalDateTime()));
			}
		}

		return result;
	}

}
