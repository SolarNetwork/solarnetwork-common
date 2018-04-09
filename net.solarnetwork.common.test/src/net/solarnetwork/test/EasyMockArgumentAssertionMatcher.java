/* ==================================================================
 * EasyMockArgumentAssertionMatcher.java - 25/01/2018 8:07:44 AM
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

import org.easymock.IArgumentMatcher;

/**
 * EasyMock argument matcher that uses an {@link Assertion} to perform matching.
 * 
 * @author matt
 * @version 1.0
 */
public class EasyMockArgumentAssertionMatcher<E> implements IArgumentMatcher {

	private final Assertion<E> assertion;
	private Throwable throwable;

	public EasyMockArgumentAssertionMatcher(Assertion<E> assertion) {
		this.assertion = assertion;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean matches(Object actual) {
		try {
			assertion.check((E) actual);
			return true;
		} catch ( Throwable e ) {
			throwable = e;
			return false;
		}
	}

	@Override
	public void appendTo(StringBuffer buffer) {
		if ( throwable != null ) {
			buffer.append(throwable.getMessage());
		}
	}
}
