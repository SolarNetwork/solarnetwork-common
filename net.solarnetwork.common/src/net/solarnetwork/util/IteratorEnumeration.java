/* ==================================================================
 * IteratorEnumeration.java - 18/11/2017 8:06:23 AM
 * 
 * Copyright 2017 SolarNetwork.net Dev Team
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

package net.solarnetwork.util;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * Utility that adapts an {@link Iterator} into an {@link Enumeration}.
 * 
 * <p>
 * This is useful when dealing with older APIs that still rely on
 * {@link Enumeration}, such as the Servlet API.
 * </p>
 * 
 * @param <E>
 *        the element type
 * @author matt
 * @version 1.0
 * @since 1.41
 */
public class IteratorEnumeration<E> implements Enumeration<E> {

	private final Iterator<E> iterator;

	/**
	 * Constructor.
	 * 
	 * @param iterator
	 *        the iterator to delegate to
	 */
	public IteratorEnumeration(Iterator<E> iterator) {
		this.iterator = iterator;
	}

	@Override
	public E nextElement() {
		return iterator.next();
	}

	@Override
	public boolean hasMoreElements() {
		return iterator.hasNext();
	}

}
