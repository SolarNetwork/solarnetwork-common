/* ==================================================================
 * UnionIterator.java - Mar 28, 2013 6:19:58 AM
 * 
 * Copyright 2007-2013 SolarNetwork.net Dev Team
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Joins multiple Iterator instances into a single Iterator.
 * 
 * @param <E>
 *        the element type
 * @author matt
 * @version 1.1
 */
public class UnionIterator<E> implements Iterator<E> {

	private final Collection<Iterator<E>> iterators;

	/**
	 * Construct from a collection of iterators.
	 * 
	 * @param iterators
	 *        the iterators to merge
	 */
	public UnionIterator(Collection<Iterator<E>> iterators) {
		assert iterators != null;
		this.iterators = new ArrayList<>(iterators);
	}

	@Override
	public boolean hasNext() {
		Iterator<Iterator<E>> itr;
		for ( itr = iterators.iterator(); itr.hasNext(); ) {
			Iterator<E> e = itr.next();
			if ( e.hasNext() ) {
				return true;
			}
			finishedIterator(e);
			itr.remove();
		}
		return false;
	}

	/**
	 * Callback when iteration has finished on a delegate interator.
	 * <p>
	 * This can be used by extending classes to clean up resources on the
	 * delegate iterators.
	 * </p>
	 * 
	 * @param itr
	 *        the delegate iterator that has finished
	 */
	protected void finishedIterator(Iterator<E> itr) {
		// extending classes can override
	}

	@Override
	public E next() {
		assert iterators.size() > 0;
		return iterators.iterator().next().next();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
