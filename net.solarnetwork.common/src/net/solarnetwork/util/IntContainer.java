/* ==================================================================
 * IntContainer.java - 26/07/2024 3:12:42 pm
 *
 * Copyright 2024 SolarNetwork.net Dev Team
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
 * API for an object that contains some collection of ints.
 *
 * @author matt
 * @version 1.0
 * @since 3.16
 */
public interface IntContainer {

	/**
	 * Test if a value is present in this container.
	 *
	 * @param value
	 *        the value to test
	 * @return {@literal true} if {@code value} is present in this instance
	 */
	boolean contains(int value);

	/**
	 * Get the minimum value in this container.
	 *
	 * @return the minimum value contained in this instance, or {@literal null}
	 *         if the container is empty
	 */
	Integer min();

	/**
	 * Get the maximum value in this container.
	 *
	 * @return the minimum value contained in this instance, or {@literal null}
	 *         if the container is empty
	 */
	Integer max();

}
