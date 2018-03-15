/* ==================================================================
 * ArrayUtils.java - 16/03/2018 6:48:58 AM
 * 
 * Copyright 2018 SolarNetwork.net Dev Team
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

import java.lang.reflect.Array;
import org.springframework.beans.factory.ObjectFactory;

/**
 * Utilities for dealing with arrays.
 * 
 * @author matt
 * @version 1.0
 * @since 1.42
 */
public final class ArrayUtils {

	private ArrayUtils() {
		// do not construct me
	}

	/**
	 * Adjust an array to a specific length, filling in any new elements with
	 * newly objects.
	 * 
	 * <p>
	 * This method can shorten or lengthen an array of objects. After adjusting
	 * the array length, any {@literal null} element in the array will be
	 * initialized to an object returned from {@code factory}, or if
	 * {@code factory} is not provided a new instance of {@code itemClass} via
	 * {@link Class#newInstance()}. The {@link ObjectFactory#getObject()} method
	 * (or class constructor if no {@code factory} provided) will be called for
	 * <b>each</b> {@code null} array index.
	 * </p>
	 * 
	 * <p>
	 * Note that if a size adjustment is made, a new array instance is returned
	 * from this method, with elements copied from {@code array} where
	 * appropriate. If no size adjustment is necessary, then {@code array} is
	 * returned directly.
	 * </p>
	 * 
	 * @param array
	 *        the source array, or {@literal null}
	 * @param count
	 *        the desired length of the array; if less than zero will be treated
	 *        as zero
	 * @param itemClass
	 *        the class of array items
	 * @param factory
	 *        a factory to create new array items, or {@literal null} to create
	 *        {@code itemClass} instances directly
	 * @return a copy of {@code array} with the adjusted length, or
	 *         {@code array} if no adjustment was necessary
	 */
	public static <T> T[] arrayWithLength(T[] array, int count, Class<T> itemClass,
			ObjectFactory<? extends T> factory) {
		if ( count < 0 ) {
			count = 0;
		}
		int inCount = (array == null ? -1 : array.length);
		T[] result = array;
		if ( inCount != count ) {
			@SuppressWarnings("unchecked")
			T[] newIncs = (T[]) Array.newInstance(itemClass, count);
			if ( array != null ) {
				System.arraycopy(array, 0, newIncs, 0, Math.min(count, inCount));
			}
			for ( int i = 0; i < count; i++ ) {
				if ( newIncs[i] == null ) {
					if ( factory != null ) {
						newIncs[i] = factory.getObject();
					} else {
						try {
							newIncs[i] = itemClass.newInstance();
						} catch ( InstantiationException e ) {
							throw new RuntimeException(e);
						} catch ( IllegalAccessException e ) {
							throw new RuntimeException(e);
						}
					}
				}
			}
			result = newIncs;
		}
		return result;
	}

}
