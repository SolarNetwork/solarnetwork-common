/* ==================================================================
 * FilterableDao.java - 7/02/2020 9:05:06 am
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

import java.util.List;
import net.solarnetwork.domain.SortDescriptor;
import net.solarnetwork.domain.Unique;

/**
 * API for a Data Access Object that supports filtered queries.
 *
 * @param <T>
 *        the filtered result type
 * @param <K>
 *        the entity primary key type
 * @param <F>
 *        the filter type
 * @author matt
 * @version 2.0
 * @since 1.59
 */
public interface FilterableDao<T extends Unique<K>, K extends Comparable<K>, F> {

	/**
	 * API for querying for a filtered set of results from all possible results.
	 *
	 * @param filter
	 *        the query filter
	 * @param sorts
	 *        the optional sort descriptors
	 * @param offset
	 *        an optional result offset
	 * @param max
	 *        an optional maximum number of returned results
	 * @return the results, never {@literal null}
	 */
	FilterResults<T, K> findFiltered(F filter, List<SortDescriptor> sorts, Long offset, Integer max);

	/**
	 * Short cut to query for all available results with a given filter.
	 *
	 * <p>
	 * This short cut method calls
	 * {@link #findFiltered(Object, List, Long, Integer)} with {@literal null}
	 * sorting and pagination arguments, so all results are returned in their
	 * default order.
	 * </p>
	 *
	 * @param filter
	 *        the query filter
	 * @return the results
	 * @since 1.1
	 */
	default FilterResults<T, K> findFiltered(F filter) {
		return findFiltered(filter, null, null, null);
	}

}
