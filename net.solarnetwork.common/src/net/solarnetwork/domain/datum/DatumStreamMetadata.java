/* ==================================================================
 * DatumStreamMetadata.java - 22/10/2020 3:01:10 pm
 *
 * Copyright 2020 SolarNetwork.net Dev Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU  Public License as
 * published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Public License for more details.
 *
 * You should have received a copy of the GNU  Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * ==================================================================
 */

package net.solarnetwork.domain.datum;

import static net.solarnetwork.util.ObjectUtils.nonnull;
import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import net.solarnetwork.domain.datum.DatumPropertiesStatistics.AccumulatingStatistic;
import net.solarnetwork.domain.datum.DatumPropertiesStatistics.InstantaneousStatistic;

/**
 * Metadata about a datum stream.
 *
 * @author matt
 * @version 2.3
 * @since 2.0
 */
public interface DatumStreamMetadata {

	/**
	 * Get the stream ID.
	 *
	 * @return the stream ID
	 */
	UUID getStreamId();

	/**
	 * Get the time zone ID associated with this stream.
	 *
	 * @return the time zone ID, or {@code null} if not known
	 */
	@Nullable
	String getTimeZoneId();

	/**
	 * Get all property names included in the stream.
	 *
	 * @return the property names, or {@code null} if none exist
	 */
	default String @Nullable [] getPropertyNames() {
		final int iLen = propertyNamesLength(DatumSamplesType.Instantaneous);
		final int aLen = propertyNamesLength(DatumSamplesType.Accumulating);
		final int sLen = propertyNamesLength(DatumSamplesType.Status);
		final int len = iLen + aLen + sLen;
		if ( len < 1 ) {
			return null;
		}
		String[] result = new String[len];
		if ( iLen > 0 ) {
			System.arraycopy(namesForType(DatumSamplesType.Instantaneous), 0, result, 0, iLen);
		}
		if ( aLen > 0 ) {
			System.arraycopy(namesForType(DatumSamplesType.Accumulating), 0, result, iLen, aLen);
		}
		if ( sLen > 0 ) {
			System.arraycopy(namesForType(DatumSamplesType.Status), 0, result, iLen + aLen, sLen);
		}
		return result;
	}

	/**
	 * Get the total number of instantaneous, accumulating, and status property
	 * names.
	 *
	 * @return the total number of properties
	 * @since 2.3
	 */
	default int getPropertyNamesLength() {
		return propertyNamesLength(DatumSamplesType.Instantaneous)
				+ propertyNamesLength(DatumSamplesType.Accumulating)
				+ propertyNamesLength(DatumSamplesType.Status);
	}

	/**
	 * Get the subset of all property names that are of a specific type.
	 *
	 * @param type
	 *        the type of property to get the names for
	 * @return the property names, or {@code null} if none available or
	 *         {@code type} is not a supported type
	 */
	String @Nullable [] propertyNamesForType(DatumSamplesType type);

	/**
	 * Get the subset of all property names that are of a specific type, assumed
	 * non-{@code null}.
	 *
	 * <p>
	 * This is designed to be used when a property is known to exist, such as
	 * after testing {@link #propertyNamesLength(DatumSamplesType)}.
	 * </p>
	 *
	 * @param type
	 *        the type of property to get the names for
	 * @return the property names
	 * @throws IllegalStateException
	 *         if property names for the given type do not exist
	 * @since 2.3
	 * @see #propertyNamesForType(DatumSamplesType)
	 */
	default String[] namesForType(DatumSamplesType type) {
		return nonnull(propertyNamesForType(type), "%s property names", type);
	}

	/**
	 * Get a property names array length.
	 *
	 * @return the number of property names of the given type
	 * @since 2.3
	 */
	default int propertyNamesLength(DatumSamplesType type) {
		final String[] names = propertyNamesForType(type);
		return (names != null ? names.length : 0);
	}

	/**
	 * Get the instantaneous property names array length.
	 *
	 * @return the number of instantaneous property names
	 * @since 2.3
	 */
	default int getInstantaneousLength() {
		return propertyNamesLength(DatumSamplesType.Instantaneous);
	}

	/**
	 * Get the accumulating property names array length.
	 *
	 * @return the number of accumulating property names
	 * @since 2.3
	 */
	default int getAccumulatingLength() {
		return propertyNamesLength(DatumSamplesType.Accumulating);
	}

	/**
	 * Get the status property names array length.
	 *
	 * @return the number of status property names
	 * @since 2.3
	 */
	default int getStatusLength() {
		return propertyNamesLength(DatumSamplesType.Status);
	}

	/**
	 * Get a property name.
	 *
	 * @param type
	 *        the property type
	 * @param propertyIndex
	 *        the property index of the given type to retrieve
	 * @return the property name, or {@code null} if no such name exists
	 * @since 2.3
	 * @see #name(DatumSamplesType, int)
	 */
	default @Nullable String propertyName(DatumSamplesType type, int propertyIndex) {
		String[] names = propertyNamesForType(type);
		if ( names != null && propertyIndex < names.length ) {
			return names[propertyIndex];
		}
		return null;
	}

	/**
	 * Get a property name, assumed non-{@code null}.
	 *
	 * <p>
	 * This is designed to be used when a property is known to exist, such as
	 * after testing {@link #propertyNamesLength(DatumSamplesType)}.
	 * </p>
	 *
	 * @param type
	 *        the property type
	 * @param propertyIndex
	 *        the property index of the given type to retrieve
	 * @return the property name
	 * @throws IllegalStateException
	 *         if a property name for the given type and index does not exist
	 * @since 2.3
	 * @see #propertyName(DatumSamplesType, int)
	 */
	default String name(DatumSamplesType type, int propertyIndex) {
		String[] names = propertyNamesForType(type);
		if ( names != null && propertyIndex >= 0 && propertyIndex < names.length ) {
			return names[propertyIndex];
		}
		throw new IllegalStateException(
				"Property %d of type %s not available.".formatted(propertyIndex, type));
	}

	/**
	 * Get the index of a specific property name.
	 *
	 * @param type
	 *        the type of property to get the index for
	 * @param name
	 *        the property name to search for
	 * @return the index, or {@literal -1} if not available
	 * @since 2.1
	 */
	default int propertyIndex(DatumSamplesType type, String name) {
		String[] names = propertyNamesForType(type);
		if ( names != null ) {
			for ( int i = 0, len = names.length; i < len; i++ ) {
				if ( name.equals(names[i]) ) {
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * Extract a datum property value for a property index.
	 *
	 * @param props
	 *        the properties to extract the value from
	 * @param type
	 *        the desired property kind
	 * @param propertyIndex
	 *        the property index of the desired value
	 * @return the property value, or {@code nul}
	 * @since 2.2
	 */
	default @Nullable Object value(@Nullable DatumProperties props, @Nullable DatumSamplesType type,
			int propertyIndex) {
		if ( props == null || type == null ) {
			return null;
		}
		return props.value(type, propertyIndex);
	}

	/**
	 * Extract an instantaneous datum property statistic value for a property
	 * index.
	 *
	 * @param stats
	 *        the statistics to extract the value from
	 * @param type
	 *        the desired statistic kind
	 * @param propertyIndex
	 *        the property index of the desired value
	 * @return the statistic value, or {@code nul}
	 * @since 2.2
	 */
	default @Nullable BigDecimal stat(@Nullable DatumPropertiesStatistics stats,
			@Nullable InstantaneousStatistic type, int propertyIndex) {
		if ( stats == null || type == null ) {
			return null;
		}
		return stats.stat(type, propertyIndex);
	}

	/**
	 * Extract an accumulating datum property statistic value for a property
	 * index.
	 *
	 * @param stats
	 *        the statistics to extract the value from
	 * @param type
	 *        the desired statistic kind
	 * @param propertyIndex
	 *        the property index of the desired value
	 * @return the statistic value, or {@code nul}
	 * @since 2.2
	 */
	default @Nullable BigDecimal stat(@Nullable DatumPropertiesStatistics stats,
			@Nullable AccumulatingStatistic type, int propertyIndex) {
		if ( stats == null || type == null ) {
			return null;
		}
		return stats.stat(type, propertyIndex);
	}

	/**
	 * Create a {@link DatumSamples} instance from {@link DatumProperties} and
	 * this metadata.
	 *
	 * @param props
	 *        the properties
	 * @return the new samples instance
	 * @since 2.3
	 */
	default DatumSamples datumSamples(final @Nullable DatumProperties props) {
		final var samples = new DatumSamples();

		if ( props != null ) {
			for ( DatumSamplesType type : DatumSamplesType.values() ) {
				String[] names = propertyNamesForType(type);
				if ( names != null ) {
					for ( int i = 0; i < names.length; i++ ) {
						samples.putSampleValue(type, names[i], props.value(type, i));
					}
				}
			}

			String[] tags = props.getTags();
			if ( tags != null ) {
				samples.setTags(Set.of(tags));
			}
		}

		return samples;
	}

	/**
	 * Populate a {@link DatumSamples} instance with instantaneous property
	 * statistics.
	 *
	 * <p>
	 * This will populate {@code _min} and {@code _max} instantaneous properties
	 * for all available instantaneous property statistic values.
	 * </p>
	 *
	 * @param s
	 *        the samples instance to populate
	 * @param stats
	 *        the statistics to copy
	 * @since 2.3
	 */
	default void populateInstantaneousStatistics(final @Nullable DatumSamples s,
			final @Nullable DatumPropertiesStatistics stats) {
		if ( s == null || stats == null ) {
			return;
		}
		final int statCount = stats.getInstantaneousLength();
		if ( statCount < 1 ) {
			return;
		}
		String[] propNames = propertyNamesForType(DatumSamplesType.Instantaneous);
		if ( propNames == null ) {
			return;
		}
		final int len = Math.min(statCount, propNames.length);
		for ( int i = 0; i < len; i++ ) {
			BigDecimal min = stat(stats, InstantaneousStatistic.Minimum, i);
			BigDecimal max = stat(stats, InstantaneousStatistic.Maximum, i);
			if ( min != null && max != null ) {
				s.putSampleValue(DatumSamplesType.Instantaneous,
						InstantaneousStatistic.Minimum.name(propNames[i]), min);
				s.putSampleValue(DatumSamplesType.Instantaneous,
						InstantaneousStatistic.Maximum.name(propNames[i]), max);
			}
		}
	}

	/**
	 * Populate a {@link DatumSamples} instance with accumulating property
	 * statistics.
	 *
	 * <p>
	 * This will populate {@code _start} and {@code _end} instantaneous
	 * properties for all available accumulating property statistic values, and
	 * an accumulating property value for the difference statistic value.
	 * </p>
	 *
	 * @param s
	 *        the samples instance to populate
	 * @param stats
	 *        the statistics to copy
	 * @since 2.3
	 */
	default void populateAccumulatingStatistics(final @Nullable DatumSamples s,
			final @Nullable DatumPropertiesStatistics stats) {
		if ( s == null || stats == null ) {
			return;
		}
		final int statCount = stats.getAccumulatingLength();
		if ( statCount < 1 ) {
			return;
		}
		final String[] propNames = propertyNamesForType(DatumSamplesType.Accumulating);
		if ( propNames == null ) {
			return;
		}
		final int len = Math.min(statCount, propNames.length);
		for ( int i = 0; i < len; i++ ) {
			BigDecimal diff = stat(stats, AccumulatingStatistic.Difference, i);
			BigDecimal start = stat(stats, AccumulatingStatistic.Start, i);
			BigDecimal end = stat(stats, AccumulatingStatistic.End, i);

			if ( diff != null ) {
				s.putSampleValue(DatumSamplesType.Accumulating, propNames[i], diff);
			}
			if ( start != null ) {
				s.putSampleValue(DatumSamplesType.Instantaneous,
						AccumulatingStatistic.Start.name(propNames[i]), start);
			}
			if ( end != null ) {
				s.putSampleValue(DatumSamplesType.Instantaneous,
						AccumulatingStatistic.End.name(propNames[i]), end);
			}
		}
	}

}
