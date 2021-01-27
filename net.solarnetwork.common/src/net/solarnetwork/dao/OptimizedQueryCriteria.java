/* ==================================================================
 * OptimizedQueryCriteria.java - 23/10/2020 9:23:06 pm
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

/**
 * Search criteria for query optimizations to apply.
 * 
 * @author matt
 * @version 1.0
 * @since 1.67
 */
public interface OptimizedQueryCriteria {

	/**
	 * Hint that a total result count is not necessary.
	 * 
	 * <p>
	 * Setting this to {@literal true} can improve the performance of most
	 * queries, when the overall total count of results is not needed. When set,
	 * features like {@link FilterResults#getTotalResults()} will not be
	 * available in the results.
	 * </p>
	 * 
	 * @return {@literal true} to optimize query to omit a total result count
	 */
	boolean isWithoutTotalResultsCount();

}
