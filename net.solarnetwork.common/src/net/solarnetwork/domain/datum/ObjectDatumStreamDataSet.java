/* ==================================================================
 * ObjectDatumStreamDataSet.java - 30/04/2022 8:00:23 am
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

/**
 * A set of {@link StreamDatum} with associated metadata.
 * 
 * @param <T>
 *        the stream datum type
 * @author matt
 * @version 1.0
 * @since 2.4
 */
public interface ObjectDatumStreamDataSet<T extends StreamDatum>
		extends ObjectDatumStreamMetadataProvider, Iterable<T> {

	/**
	 * Get the actual results.
	 * 
	 * <p>
	 * These are the same results returned by {@link Iterable#iterator()}.
	 * </p>
	 * 
	 * @return the results, never {@literal null}
	 */
	Iterable<T> getResults();

	/**
	 * Get a total number of available results, if known.
	 * 
	 * <p>
	 * If this result represents partial results and the total count of
	 * available results is known, this value represents that total count of
	 * available results.
	 * </p>
	 * 
	 * @return total available results, or {@literal null} if not known
	 */
	Long getTotalResultCount();

	/**
	 * Get the starting offset of the returned results.
	 * 
	 * @return the starting offset, or {@literal null} if not known
	 */
	Integer getStartingOffset();

	/**
	 * Get the number of results that matched the query.
	 * 
	 * @return the number of returned results, or {@literal null} if not known
	 */
	Integer getReturnedResultCount();

}
