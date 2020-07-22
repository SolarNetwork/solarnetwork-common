/* ==================================================================
 * SimplePagination.java - 23/07/2020 6:52:01 AM
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

package net.solarnetwork.domain;

import java.util.List;

/**
 * Simple pagination characteristics.
 * 
 * <p>
 * This is designed to support search queries and filters.
 * </p>
 * 
 * @author matt
 * @version 1.0
 */
public class SimplePagination {

	private List<SortDescriptor> sorts;
	private Integer offset;
	private Integer max;

	/**
	 * 
	 */
	public SimplePagination() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Get the sort orderings.
	 * 
	 * @return the sorts
	 */
	public List<SortDescriptor> getSorts() {
		return sorts;
	}

	/**
	 * Set the sort orderings.
	 * 
	 * @param sorts
	 *        the sorts to set
	 */
	public void setSorts(List<SortDescriptor> sorts) {
		this.sorts = sorts;
	}

	/**
	 * Get the desired starting offset.
	 * 
	 * @return the offset, or {@literal null}
	 */
	public Integer getOffset() {
		return offset;
	}

	/**
	 * Set the desired starting offset.
	 * 
	 * @param offset
	 *        the offset to set
	 */
	public void setOffset(Integer offset) {
		this.offset = offset;
	}

	/**
	 * Get the maximum desired results.
	 * 
	 * @return the max, or {@literal null} for all results
	 */
	public Integer getMax() {
		return max;
	}

	/**
	 * Set the maximum results.
	 * 
	 * @param max
	 *        the max to set
	 */
	public void setMax(Integer max) {
		this.max = max;
	}

}
