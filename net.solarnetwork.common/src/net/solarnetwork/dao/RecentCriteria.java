/* ==================================================================
 * RecentCriteria.java - 23/10/2020 9:24:58 pm
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

package net.solarnetwork.dao;

/**
 * Search criteria for recent data results.
 * 
 * @author matt
 * @version 1.0
 * @since 1.67
 */
public interface RecentCriteria {

	/**
	 * Flag to indicate that only the most recently available data should be
	 * returned.
	 * 
	 * @return the most recent only
	 */
	boolean isMostRecent();

}
