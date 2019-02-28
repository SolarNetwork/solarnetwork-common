/* ==================================================================
 * Bitmaskable.java - 18/02/2019 10:21:15 am
 * 
 * Copyright 2019 SolarNetwork.net Dev Team
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

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * A standardized API for domain objects that can be represented in bitmask
 * form.
 * 
 * <p>
 * A bitmask is a collection of on/off flags encoded as bits within an integer
 * value.
 * </p>
 * 
 * @author matt
 * @version 1.0
 * @since 1.50
 */
public interface Bitmaskable {

	/**
	 * Get the bit offset.
	 * 
	 * @return offset, starting from {@literal 0} for the right-most bit
	 */
	int bitmaskBitOffset();

	/**
	 * Get a bitmask value for a set of {@code Bitmaskable} objects.
	 * 
	 * @param maskables
	 *        the set of {@code Bitmaskable} objects ({@literal null} allowed)
	 * @return a bitmask value of all {@link Bitmaskable#bitmaskBitOffset()}
	 *         values of the given {@code maskables}
	 * @see #setForBitmask(int)
	 */
	static int bitmaskValue(Set<? extends Bitmaskable> maskables) {
		int mask = 0;
		if ( maskables != null ) {
			for ( Bitmaskable c : maskables ) {
				mask |= (1 << (c.bitmaskBitOffset()));
			}
		}
		return mask;
	}

	/**
	 * Convert a bitmask value into a set of {@code Bitmaskable} objects.
	 * 
	 * @param mask
	 *        a bitmask value of a set of {@code Bitmaskable} objects
	 * @param clazz
	 *        the class of an enumeration of {@link Bitmaskable} objects
	 * @return an immutable set of {@link Bitmaskable} objects, never
	 *         {@literal null}
	 * @see #bitmaskValue(Set)
	 */
	static <T extends Enum<T> & Bitmaskable> Set<T> setForBitmask(int mask, Class<T> clazz) {
		Set<T> result = setForBitmask(mask, clazz.getEnumConstants());
		return (result.isEmpty() ? result : EnumSet.copyOf(result));
	}

	/**
	 * Convert a bitmask value into a set of {@code Bitmaskable} objects.
	 * 
	 * @param mask
	 *        a bitmask value of a set of {@code Bitmaskable} objects
	 * @param values
	 *        the complete set of possible {@link Bitmaskable} objects
	 * @return an immutable set of {@link Bitmaskable} objects, never
	 *         {@literal null}
	 * @see #bitmaskValue(Set)
	 */
	static <T extends Bitmaskable> Set<T> setForBitmask(int mask, T[] values) {
		if ( mask < 1 ) {
			return Collections.emptySet();
		}
		Set<T> set = new HashSet<>(16);
		for ( T c : values ) {
			int b = c.bitmaskBitOffset();
			if ( ((mask >> b) & 1) == 1 ) {
				set.add(c);
			}
		}
		return (set.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(set));
	}
}
