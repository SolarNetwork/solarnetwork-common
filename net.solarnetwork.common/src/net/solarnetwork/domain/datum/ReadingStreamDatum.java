/* ==================================================================
 * ReadingStreamDatum.java - 13/11/2026 7:46:54 am
 *
 * Copyright 2026 SolarNetwork.net Dev Team
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

/**
 * Marker API for an aggregate stream datum that represents a calculated
 * "reading" for a specific time period.
 *
 * <p>
 * The {@link #getTimestamp()} value represents the starting timestamp of the
 * reading, and {@link #getEndTimestamp()} the ending timestamp, or {@code null}
 * if the ending timestamp is implicit. The start and end timestamp values can
 * be equal, for reading calculations for a single point in time, or different
 * for calculations representing a time range.
 * </p>
 *
 * <p>
 * A reading differs from other aggregate datum in how its accumulating
 * properties are interpreted: the reading value of an accumulating property is
 * its {@link DatumPropertiesStatistics.AccumulatingStatistic#Difference}
 * statistic, rather than the property value itself.
 * </p>
 *
 * @author matt
 * @version 1.0
 * @since 4.61
 */
public interface ReadingStreamDatum extends AggregateStreamDatum {

}
