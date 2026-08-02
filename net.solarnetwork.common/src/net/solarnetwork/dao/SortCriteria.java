/* ==================================================================
 * SortCriteria.java - 27/10/2020 3:26:39 pm
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
import org.jspecify.annotations.Nullable;
import net.solarnetwork.domain.SortDescriptor;

/**
 * API for sorting criteria.
 *
 * @author matt
 * @version 1.1
 * @since 1.67
 */
public interface SortCriteria {

	/**
	 * Get the sort orderings.
	 *
	 * @return the sorts
	 */
	@Nullable
	List<SortDescriptor> getSorts();

	/**
	 * Test if any sort descriptors are available.
	 *
	 * @return {@literal true} if at least one sort descriptor is available
	 * @since 1.1
	 */
	default boolean hasSorts() {
		List<SortDescriptor> sorts = getSorts();
		return (sorts != null && !sorts.isEmpty());
	}

	/**
	 * Get the sort orderings.
	 *
	 * <p>
	 * This method is designed to be used after a call to {@link #hasSorts()}
	 * returns {@code true}, to avoid nullness warnings.
	 * </p>
	 *
	 * @return the sorts (presumed non-null)
	 * @since 1.1
	 */
	@SuppressWarnings("NullAway")
	default List<SortDescriptor> sorts() {
		return getSorts();
	}

}
