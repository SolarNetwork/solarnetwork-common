/* ==================================================================
 * FilterResults.java - 7/02/2020 9:08:37 am
 *
 * Copyright 2020 SolarNetwork.net Dev Team
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

package net.solarnetwork.dao;

import net.solarnetwork.domain.Identity;

/**
 * A filtered query results object.
 *
 * <p>
 * This object extends {@link Iterable} but also exposes a JavaBean getter
 * property {@link #getResults()} to ease the marshaling of the results into
 * other forms.
 * </p>
 *
 * @param <T>
 *        the filtered result type
 * @param <K>
 *        the filtered result identity type
 * @author matt
 * @version 2.0
 * @since 1.59
 */
public interface FilterResults<T extends Identity<T, K>, K extends Comparable<K>> extends Iterable<T> {

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
	Long getTotalResults();

	/**
	 * Get the starting offset of the returned results, within all available
	 * results.
	 *
	 * @return the starting offset
	 */
	long getStartingOffset();

	/**
	 * Get the number of results that matched the query.
	 *
	 * @return the number of returned results
	 */
	int getReturnedResultCount();

}
