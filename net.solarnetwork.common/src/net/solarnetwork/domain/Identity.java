/* ==================================================================
 * Identity.java - Aug 8, 2010 7:42:21 PM
 * 
 * Copyright 2007-2010 SolarNetwork.net Dev Team
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
 * $Revision$
 * ==================================================================
 */

package net.solarnetwork.domain;

import java.util.Comparator;

/**
 * Common API for identity information in SolarNetwork participating services.
 * 
 * @param <PK>
 *        the primary data type that uniquely identifies the object
 * @version 1.2
 * @author matt
 * @since 1.43
 */
public interface Identity<PK> extends Comparable<PK> {

	/**
	 * Get the primary identifier of the object
	 * 
	 * @return the primary identifier
	 */
	PK getId();

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
	 * @return {@literal true} if this object has a valid identifier
	 * @since 1.1
	 */
	default boolean hasId() {
		return getId() != null;
	}

	/**
	 * Sort instances by their ID values.
	 * 
	 * @param <T>
	 *        the {@code Identity} type
	 * @param <PK>
	 *        the {@code Identity} ID type
	 * @return the comparator
	 * @since 1.2
	 */
	static <T extends Identity<PK>, PK extends Comparable<PK>> Comparator<T> sortByIdentity() {
		return (l, r) -> l.compareTo(r.getId());
	}

}
