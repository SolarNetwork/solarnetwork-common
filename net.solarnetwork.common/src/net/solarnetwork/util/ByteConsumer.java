/* ==================================================================
 * ByteConsumer.java - 25/01/2020 11:27:36 am
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
import java.util.function.Consumer;

/**
 * Represents an operation that accepts a single {@code byte}-valued argument
 * and returns no result. This is the primitive type specialization of
 * {@link Consumer} for {@code byte}. Unlike most other functional interfaces,
 * {@code ByteConsumer} is expected to operate via side-effects.
 * 
 * @author matt
 * @version 1.0
 * @since 1.58
 */
@FunctionalInterface
public interface ByteConsumer {

	/**
	 * Performs this operation on the given argument.
	 *
	 * @param value
	 *        the input argument
	 */
	void accept(byte value);

	/**
	 * Returns a composed {@code ByteConsumer} that performs, in sequence, this
	 * operation followed by the {@code after} operation. If performing either
	 * operation throws an exception, it is relayed to the caller of the
	 * composed operation. If performing this operation throws an exception, the
	 * {@code after} operation will not be performed.
	 *
	 * @param after
	 *        the operation to perform after this operation
	 * @return a composed {@code ByteConsumer} that performs in sequence this
	 *         operation followed by the {@code after} operation
	 * @throws NullPointerException
	 *         if {@code after} is null
	 */
	default ByteConsumer andThen(ByteConsumer after) {
		Objects.requireNonNull(after);
		return (byte t) -> {
			accept(t);
			after.accept(t);
		};
	}

}
