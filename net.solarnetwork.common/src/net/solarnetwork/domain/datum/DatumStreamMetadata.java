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

import java.math.BigDecimal;
import java.util.UUID;
import net.solarnetwork.domain.datum.DatumPropertiesStatistics.AccumulatingStatistic;
import net.solarnetwork.domain.datum.DatumPropertiesStatistics.InstantaneousStatistic;

/**
 * Metadata about a datum stream.
 *
 * @author matt
 * @version 2.2
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
	String getTimeZoneId();

	/**
	 * Get all property names included in the stream.
	 *
	 * @return the property names
	 */
	String[] getPropertyNames();

	/**
	 * Get the subset of all property names that are of a specific type.
	 *
	 * @param type
	 *        the type of property to get the names for
	 * @return the property names, or {@code null} if none available or
	 *         {@code type} is {@link DatumSamplesType#Tag}
	 */
	String[] propertyNamesForType(DatumSamplesType type);

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
	default Object value(DatumProperties props, DatumSamplesType type, int propertyIndex) {
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
	default BigDecimal stat(DatumPropertiesStatistics stats, InstantaneousStatistic type,
			int propertyIndex) {
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
	default BigDecimal stat(DatumPropertiesStatistics stats, AccumulatingStatistic type,
			int propertyIndex) {
		if ( stats == null || type == null ) {
			return null;
		}
		return stats.stat(type, propertyIndex);
	}

}
