/* ==================================================================
 * IntShortBiConsumer.java - 19/01/2020 7:46:47 am
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

import java.util.Objects;

/**
 * Represents an operation that accepts int and short arguments and returns no
 * result.
 * 
 * @author matt
 * @version 1.0
 * @since 1.58
 */
@FunctionalInterface
public interface IntShortBiConsumer {

	/**
	 * Applies this operator to the given operands.
	 *
	 * @param a
	 *        the first input argument
	 * @param b
	 *        the second input argument
	 */
	void accept(int a, short b);

	/**
	 * Returns a composed {@code BiConsumer} that performs, in sequence, this
	 * operation followed by the {@code after} operation. If performing either
	 * operation throws an exception, it is relayed to the caller of the
	 * composed operation. If performing this operation throws an exception, the
	 * {@code after} operation will not be performed.
	 *
	 * @param after
	 *        the operation to perform after this operation
	 * @return a composed {@code BiConsumer} that performs in sequence this
	 *         operation followed by the {@code after} operation
	 * @throws NullPointerException
	 *         if {@code after} is null
	 */
	default IntShortBiConsumer andThen(IntShortBiConsumer after) {
		Objects.requireNonNull(after);

		return (l, r) -> {
			accept(l, r);
			after.accept(l, r);
		};
	}
}
