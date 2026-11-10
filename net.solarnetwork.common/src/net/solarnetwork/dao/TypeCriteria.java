/* ==================================================================
 * TypeCriteria.java - 11 Nov 2026 10:08:20 am
 *
 * Copyright 2026 SolarNetwork.net Dev Team
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
 * Search criteria for type related data.
 *
 * @author matt
 * @version 1.0
 * @since 4.60
 */
public interface TypeCriteria {

	/**
	 * Get the first datum class type.
	 *
	 * <p>
	 * This returns the first available type from the {@link #getTypes()} array,
	 * or {@code null} if not available.
	 * </p>
	 *
	 * @return the first type, or {@code null} if not available
	 */
	default @Nullable String getType() {
		final String[] types = getTypes();
		return (types != null && types.length > 0 ? types[0] : null);
	}

	/**
	 * Get an array of datum class types.
	 *
	 * @return array of types (may be {@code null})
	 */
	String @Nullable [] getTypes();

	/**
	 * Test if the filter has a type criteria specified.
	 *
	 * @return {@literal true} if {@link #getType()} is non-null and not empty
	 */
	default boolean hasTypeCriteria() {
		return getType() != null && !getType().isEmpty();
	}

	/**
	 * Get the datum class types.
	 *
	 * <p>
	 * This method is designed to be used after a call to
	 * {@link #hasTypeCriteria()} returns {@code true}, to avoid nullness
	 * warnings.
	 * </p>
	 *
	 * @return the types (presumed non-null)
	 */
	@SuppressWarnings("NullAway")
	default String[] types() {
		return getTypes();
	}

}
