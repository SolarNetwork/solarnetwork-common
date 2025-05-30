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
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import net.solarnetwork.domain.Unique;

/**
 * Basic implementation of {@link FilterResults}.
 *
 * @param <T>
 *        the match type
 * @param <K>
 *        the primary key type
 * @author matt
 * @version 2.0
 * @since 1.59
 */
@JsonPropertyOrder({ "totalResults", "startingOffset", "returnedResultCount", "results" })
public class BasicFilterResults<T extends Unique<K>, K extends Comparable<K>>
		implements FilterResults<T, K> {

	private final Iterable<T> results;
	private final Long totalResults;
	private final long startingOffset;
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
	public BasicFilterResults(Iterable<T> results, Long totalResults, int startingOffset,
			int returnedResultCount) {
		this(results, totalResults, (long) startingOffset, returnedResultCount);
	}

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
	 * @since 1.3
	 */
	public BasicFilterResults(Iterable<T> results, Long totalResults, long startingOffset,
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
	public BasicFilterResults(Iterable<T> results) {
		this(results, null, 0, iterableCount(results));
	}

	/**
	 * Create a {@link FilterResults} instance.
	 *
	 * @param <T>
	 *        the result type
	 * @param <K>
	 *        the result key type
	 * @param data
	 *        the result data
	 * @param criteria
	 *        the criteria used to produce the results
	 * @param totalResults
	 *        the total result count, if available
	 * @param returnedResults
	 *        the returned results count
	 * @return the results instance
	 * @since 1.1
	 */
	public static <T extends Unique<K>, K extends Comparable<K>> FilterResults<T, K> filterResults(
			Iterable<T> data, PaginationCriteria criteria, Long totalResults, int returnedResults) {
		long offset = 0;
		if ( criteria != null && criteria.getMax() != null ) {
			offset = criteria.getOffset() != null ? criteria.getOffset() : 0;
		}
		return new BasicFilterResults<>(data, totalResults, offset, returnedResults);
	}

	private static int iterableCount(Iterable<?> iterable) {
		if ( iterable instanceof Collection<?> ) {
			return ((Collection<?>) iterable).size();
		}
		return (int) StreamSupport.stream(iterable.spliterator(), false).count();
	}

	@Override
	public Iterator<T> iterator() {
		if ( results == null ) {
			Set<T> emptyResult = Collections.emptySet();
			return emptyResult.iterator();
		}
		return results.iterator();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BasicFilterResults{");
		if ( totalResults != null ) {
			builder.append("totalCount=");
			builder.append(totalResults);
			builder.append(", ");
		}
		builder.append("offset=");
		builder.append(startingOffset);
		builder.append(", count=");
		builder.append(returnedResultCount);
		builder.append("}");
		return builder.toString();
	}

	@Override
	public Iterable<T> getResults() {
		return results;
	}

	@Override
	public Long getTotalResults() {
		return totalResults;
	}

	@Override
	public long getStartingOffset() {
		return startingOffset;
	}

	@Override
	public int getReturnedResultCount() {
		return returnedResultCount;
	}

}
