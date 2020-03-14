/* ==================================================================
 * BasicFilterResults.java - 7/02/2020 9:16:50 am
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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.StreamSupport;
import net.solarnetwork.domain.Identity;

/**
 * Basic implementation of {@link FilterResults}.
 * 
 * @author matt
 * @version 1.0
 * @since 1.59
 */
public class BasicFilterResults<M extends Identity<K>, K> implements FilterResults<M, K> {

	private final Iterable<M> results;
	private final Long totalResults;
	private final int startingOffset;
	private final int returnedResultCount;

	/**
	 * Constructor.
	 * 
	 * @param results
	 *        the results iterable
	 * @param totalResults
	 *        the total available results, or {@literal null}
	 * @param startingOffset
	 *        the starting offset
	 * @param returnedResultCount
	 *        the count of objects in {@code results}
	 */
	public BasicFilterResults(Iterable<M> results, Long totalResults, int startingOffset,
			int returnedResultCount) {
		super();
		this.results = results;
		this.totalResults = totalResults;
		this.startingOffset = startingOffset;
		this.returnedResultCount = returnedResultCount;
	}

	/**
	 * Constructor.
	 * 
	 * <p>
	 * This total results count will be set to {@literal null}, the starting
	 * offset to {@literal 0}, and the returned result count will be derived
	 * from the number of items in {@code results}.
	 * </p>
	 * 
	 * @param results
	 *        the results iterable
	 */
	public BasicFilterResults(Iterable<M> results) {
		this(results, null, 0, iterableCount(results));
	}

	private static int iterableCount(Iterable<?> iterable) {
		if ( iterable instanceof Collection<?> ) {
			return ((Collection<?>) iterable).size();
		}
		return (int) StreamSupport.stream(iterable.spliterator(), false).count();
	}

	@Override
	public Iterator<M> iterator() {
		if ( results == null ) {
			Set<M> emptyResult = Collections.emptySet();
			return emptyResult.iterator();
		}
		return results.iterator();
	}

	@Override
	public Iterable<M> getResults() {
		return results;
	}

	@Override
	public Long getTotalResults() {
		return totalResults;
	}

	@Override
	public int getStartingOffset() {
		return startingOffset;
	}

	@Override
	public int getReturnedResultCount() {
		return returnedResultCount;
	}

}
