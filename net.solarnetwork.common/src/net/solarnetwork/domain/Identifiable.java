/* ==================================================================
 * Identifiable.java - 31/10/2017 6:30:03 AM
 * 
 * Copyright 2017 SolarNetwork.net Dev Team
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
 * API for a standardized way of identifying services, to support configuring
 * links to specific instances of a service at runtime. Many managed services in
 * SolarNode allow any number of them to be deployed.
 * 
 * @author matt
 * @version 1.1
 */
public interface Identifiable {

	/**
	 * A string UID property, for use in events and other data structures.
	 * 
	 * @since 1.1
	 */
	String UID_PROPERTY = "uid";

	/**
	 * A string group UID property, for use in events and other data structures.
	 *
	 * @since 1.1
	 */
	String GROUP_UID_PROPERTY = "groupUid";

	/**
	 * Get a unique identifier for this service.
	 * 
	 * <p>
	 * This should be meaningful to the service implementation, and should be
	 * minimally unique between instances of the same service interface.
	 * </p>
	 * 
	 * @return unique identifier (should never be {@literal null})
	 */
	String getUid();

	/**
	 * Get a grouping identifier for this service.
	 * 
	 * <p>
	 * This should be meaningful to the service implementation.
	 * </p>
	 * 
	 * @return a group identifier, or {@literal null} if not part of any group
	 */
	String getGroupUid();

	/**
	 * Get a friendly display name for this service.
	 * 
	 * @return a display name
	 */
	String getDisplayName();

}
