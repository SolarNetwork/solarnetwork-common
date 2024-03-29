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

/**
 * API for an object that represents an aggregation of individual
 * {@link StreamDatum} within a unique stream over a specific period of time and
 * a set of property values and associated aggregate statistics.
 * 
 * @author matt
 * @version 1.0
 */
public interface AggregateStreamDatum extends StreamDatum {

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
	Instant getEndTimestamp();

	/**
	 * Get the property statistics.
	 * 
	 * @return the statistics
	 */
	DatumPropertiesStatistics getStatistics();

}
