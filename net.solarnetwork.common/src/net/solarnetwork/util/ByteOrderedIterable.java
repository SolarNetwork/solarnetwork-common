/* ==================================================================
 * ByteOrderedIterable.java - 25/01/2020 11:30:00 am
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

package net.solarnetwork.util;

/**
 * API for a collection that supports ordered iteration over byte values.
 * 
 * @author matt
 * @version 1.0
 * @since 1.58
 */
public interface ByteOrderedIterable {

	/**
	 * Iterate over all primitive values in this collection.
	 * 
	 * <p>
	 * This method of iteration can be more efficient than iterating via the
	 * {@link java.util.Iterator} API because no unboxing of primitives is
	 * necessary.
	 * </p>
	 * 
	 * @param action
	 *        the consumer to handle the values
	 */
	void forEachOrdered(ByteConsumer action);

	/**
	 * Iterate over a range of values in this collection.
	 * 
	 * <p>
	 * This method of iteration can be more efficient than iterating via the
	 * {@link java.util.Iterator} API because no unboxing of primitives is
	 * necessary.
	 * </p>
	 * 
	 * @param min
	 *        the minimum key value (inclusive)
	 * @param max
	 *        the maximum key value (exclusive)
	 * @param action
	 *        the consumer to handle the values
	 */
	void forEachOrdered(int min, int max, ByteConsumer action);

}
