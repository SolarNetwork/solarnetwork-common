/* ==================================================================
 * PaginationCriteria.java - 27/10/2020 3:09:26 pm
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

import org.jspecify.annotations.Nullable;

/**
 * API for page-based search criteria.
 *
 * @author matt
 * @version 1.2
 * @since 1.67
 */
public interface PaginationCriteria extends SortCriteria {

	/**
	 * Get the desired starting offset.
	 *
	 * @return the offset, or {@code null}
	 */
	@Nullable
	Long getOffset();

	/**
	 * Test if an offset value is available.
	 *
	 * @return {@code true} if {@link #getOffset()} is not {@code null}
	 * @since 1.2
	 */
	default boolean hasOffset() {
		return (getOffset() != null);
	}

	/**
	 * Get the desired starting offset.
	 *
	 * <p>
	 * This method is designed to be used after a call to {@link #hasOffset()}
	 * returns {@code true}, to avoid nullness warnings.
	 * </p>
	 *
	 * @return the offset (presumed non-null)
	 * @since 1.2
	 */
	@SuppressWarnings("NullAway")
	default Long offset() {
		return getOffset();
	}

	/**
	 * Get the maximum desired results.
	 *
	 * @return the max, or {@code null} for all results
	 */
	@Nullable
	Integer getMax();

	/**
	 * Test if a max value is available.
	 *
	 * @return {@code true} if {@link #getMax()} is not {@code null}
	 * @since 1.2
	 */
	default boolean hasMax() {
		return (getMax() != null);
	}

	/**
	 * Get the desired starting offset.
	 *
	 * <p>
	 * This method is designed to be used after a call to {@link #hasMax()}
	 * returns {@code true}, to avoid nullness warnings.
	 * </p>
	 *
	 * @return the max (presumed non-null)
	 * @since 1.2
	 */
	@SuppressWarnings("NullAway")
	default Integer max() {
		return getMax();
	}

}
