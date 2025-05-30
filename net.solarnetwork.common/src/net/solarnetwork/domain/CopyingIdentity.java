/* ==================================================================
 * CopyingEntity.java - 13/08/2022 7:25:31 am
 *
 * Copyright 2022 SolarNetwork.net Dev Team
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
 * API for an identity with copying support.
 *
 * @param <T>
 *        the identity type
 * @param <K>
 *        the primary key type
 * @author matt
 * @version 2.0
 * @since 2.9
 */
public interface CopyingIdentity<T extends Identity<T, K>, K extends Comparable<K>>
		extends Identity<T, K> {

	/**
	 * Create a new copy of this identity with a given ID.
	 *
	 * @param id
	 *        the ID to use in the copy
	 * @return the new copy
	 */
	T copyWithId(K id);

	/**
	 * Copy the properties of this identity into another entity.
	 *
	 * @param other
	 *        the identity to copy the properties from this instance
	 */
	void copyTo(T other);

}
