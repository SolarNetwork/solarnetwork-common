/* ==================================================================
 * SortDescriptors.java - Apr 29, 2011 3:40:16 PM
 * 
 * Copyright 2007-2011 SolarNetwork.net Dev Team
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

/**
 * An API for specifying a collection ordering.
 * 
 * @author matt
 * @version 1.0
 */
public interface SortDescriptor {

	/**
	 * Get the name of the value to sort by.
	 * 
	 * <p>
	 * How this value is interpreted is implementation dependent.
	 * </p>
	 * 
	 * @return the sort key
	 */
	String getSortKey();

	/**
	 * Return {@literal true} if the sort should be in descending order, otherwise
	 * the short should be in ascending order.
	 * 
	 * @return {@literal true} if the sort should be in descending order
	 */
	boolean isDescending();

}
