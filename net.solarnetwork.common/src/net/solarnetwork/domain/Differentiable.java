/* ==================================================================
 * Differentiable.java - 14/02/2020 9:14:20 am
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

import java.util.Collection;
import java.util.Iterator;

/**
 * Common API for an object that can be compared to another for differences.
 * 
 * <p>
 * The actual meaning of <i>different</i> is implementation specific. For
 * example an entity might implement this API in order to tell if a modified
 * copy of an entity with the same identity as a persisted instance have
 * different property values, and thus the copy should be persisted to save the
 * updated property values.
 * </p>
 * 
 * @author matt
 * @version 1.1
 * @since 1.59
 */
public interface Differentiable<T> {

	/**
	 * Test if this object differs from another.
	 * 
	 * @param other
	 *        the other object to compare to
	 * @return {@literal true} if the object differs from this object
	 */
	boolean differsFrom(T other);

	/**
	 * Test if two {@link Differentiable} objects differ.
	 * 
	 * <p>
	 * If both objects are {@literal null} this method returns {@literal false}.
	 * If one object is {@literal null} this methdo returns {@literal true}.
	 * Otherwise this method returns the result of {@code l.differsFrom(r)}.
	 * </p>
	 * 
	 * @param <T>
	 *        the Differentiable type
	 * @param l
	 *        the first object
	 * @param r
	 *        the second object
	 * @return {@literal true} if there are any difference between the objects
	 * @since 1.1
	 */
	static <T extends Differentiable<T>> boolean differ(T l, T r) {
		if ( l == r ) {
			return false;
		} else if ( l == null || r == null ) {
			return true;
		} else {
			return l.differsFrom(r);
		}
	}

	/**
	 * Test if two collections of {@link Differentiable} objects have any
	 * difference.
	 * 
	 * <p>
	 * This compares the objects in each collection in iteration order. If any
	 * pair of objects differ, this method returns {@literal true}. If the
	 * collections are of different sizes, this method returns true. If both
	 * collections are {@literal null} this method returns {@literal false}.
	 * </p>
	 * 
	 * @param <T>
	 *        the Differentiable type
	 * @param l
	 *        the first collection
	 * @param r
	 *        the second collection
	 * @return {@literal true} if there are any differences between the
	 *         collections
	 * @since 1.1
	 */
	static <T extends Differentiable<T>> boolean differ(Collection<T> l, Collection<T> r) {
		if ( l == r ) {
			return false;
		} else if ( l == null || r == null ) {
			return true;
		} else if ( l.size() != r.size() ) {
			return true;
		}
		for ( Iterator<T> li = l.iterator(), ri = r.iterator(); li.hasNext(); ) {
			T le = li.next();
			T re = ri.next();
			if ( le == re ) {
				continue;
			} else if ( le == null || re == null || le.differsFrom(re) ) {
				return true;
			}
		}
		return false;
	}

}
