/* ==================================================================
 * Assertion.java - 25/01/2018 8:03:31 AM
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

package net.solarnetwork.test;

/**
 * API for a testing assertion.
 * 
 * <p>
 * This API has been designed to work with frameworks like EasyMock.
 * </p>
 * 
 * @param <E>
 *        the argument type
 * @author matt
 * @version 1.0
 */
public interface Assertion<E> {

	/**
	 * Verify an object, throwing an exception if the argument fails
	 * verification.
	 * 
	 * @param argument
	 *        the argument to check
	 * @throws Throwable
	 *         if an exception occurs or validation fails
	 */
	void check(E argument) throws Throwable;

}
