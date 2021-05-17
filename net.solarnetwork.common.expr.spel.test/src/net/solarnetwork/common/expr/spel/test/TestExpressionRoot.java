/* ==================================================================
 * TestExpressionRoot.java - 13/05/2021 9:03:39 PM
 * 
 * Copyright 2021 SolarNetwork.net Dev Team
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

package net.solarnetwork.common.expr.spel.test;

import java.util.Map;

/**
 * Test expression root.
 * 
 * @author matt
 * @version 1.0
 */
public class TestExpressionRoot {

	private final Map<String, ?> data;
	private final int foo;

	/**
	 * Constructor.
	 * 
	 * @param data
	 *        the data
	 * @param foo
	 *        the foo
	 */
	public TestExpressionRoot(Map<String, ?> data, int foo) {
		super();
		this.data = data;
		this.foo = foo;
	}

	/**
	 * Get the data.
	 * 
	 * @return the data
	 */
	public Map<String, ?> getData() {
		return data;
	}

	/**
	 * Get the foo.
	 * 
	 * @return the foo
	 */
	public int getFoo() {
		return foo;
	}

}
