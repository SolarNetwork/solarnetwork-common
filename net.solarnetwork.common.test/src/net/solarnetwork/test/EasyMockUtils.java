/* ==================================================================
 * EasyMockUtils.java - 25/01/2018 8:06:06 AM
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

import org.easymock.EasyMock;

/**
 * Utilities to help with EasyMock.
 * 
 * @author matt
 * @version 1.0
 */
public class EasyMockUtils {

	/**
	 * Verify an argument matches an {@link Assertion}.
	 * 
	 * @param <E>
	 *        the argument type
	 * @param assertion
	 *        the assertion to use
	 * @return {@literal null}
	 */
	public static <E> E assertWith(Assertion<E> assertion) {
		EasyMock.reportMatcher(new EasyMockArgumentAssertionMatcher<E>(assertion));
		return null;
	}
}
