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
import java.util.Objects;

/**
 * Simple pagination characteristics.
 *
 * <p>
 * This is designed to support search queries and filters.
 * </p>
 *
 * @author matt
 * @version 1.2
 */
public class SimplePagination implements Cloneable {

	private List<SortDescriptor> sorts;
	private Long offset;
	private Integer max;

	/**
	 * Constructor.
	 */
	public SimplePagination() {
		super();
	}

	/**
	 * Test if the arguments match the values in this instance.
	 *
	 * @param sorts
	 *        the sorts
	 * @param offset
	 *        the offset
	 * @param max
	 *        the max
	 * @return {@literal true} if all the arguments match the associated
	 *         properties in this instance
	 * @deprecated use {@link #matches(List, Long, Integer)}
	 */
	@Deprecated
	public boolean matches(List<SortDescriptor> sorts, Integer offset, Integer max) {
		Long offsetLong = offset != null ? offset.longValue() : null;
		return matches(sorts, offsetLong, max);
	}

	/**
	 * Test if the arguments match the values in this instance.
	 *
	 * @param sorts
	 *        the sorts
	 * @param offset
	 *        the offset
	 * @param max
	 *        the max
	 * @return {@literal true} if all the arguments match the associated
	 *         properties in this instance
	 * @since 1.2
	 */
	public boolean matches(List<SortDescriptor> sorts, Long offset, Integer max) {
		// @formatter:off
		return Objects.equals(this.sorts, sorts)
				&& Objects.equals(this.offset, offset)
				&& Objects.equals(this.max, max);
		// @formatter:on
	}

	@Override
	public SimplePagination clone() {
		try {
			return (SimplePagination) super.clone();
		} catch ( CloneNotSupportedException e ) {
			// cannot happen
			throw new RuntimeException(e);
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(max, offset, sorts);
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( !(obj instanceof SimplePagination) ) {
			return false;
		}
		SimplePagination other = (SimplePagination) obj;
		return Objects.equals(max, other.max) && Objects.equals(offset, other.offset)
				&& Objects.equals(sorts, other.sorts);
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
	public Long getOffset() {
		return offset;
	}

	/**
	 * Set the desired starting offset.
	 *
	 * @param offset
	 *        the offset to set
	 */
	public void setOffset(Long offset) {
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
