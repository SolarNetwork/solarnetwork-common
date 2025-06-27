/* ==================================================================
 * Unique.java - 30/05/2025 11:27:21 am
 *
 * Copyright 2025 SolarNetwork.net Dev Team
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

/**
 * Common API for uniquely identifiable information.
 *
 * @param <K>
 *        the unique identifier type
 * @author matt
 * @version 1.0
 * @since 4.0
 */
public interface Unique<K> {

	/**
	 * Get the primary identifier of the object.
	 *
	 * @return the primary identifier
	 */
	K getId();

	/**
	 * Test if this object has a valid identifier.
	 *
	 * <p>
	 * This method must only return {@literal true} if the object returned from
	 * {@link #getId()} is a valid identifier for objects of this type.
	 * </p>
	 *
	 * <p>
	 * This implementation simply tests if {@link #getId()} is not
	 * {@literal null}. Extending classes, such as those with composite keys
	 * where nested properties must be defined for the key to be valid, can
	 * override this implementation as needed.
	 * </p>
	 *
	 * @return {@code true} if this object has a valid identifier
	 */
	default boolean hasId() {
		return getId() != null;
	}

}
