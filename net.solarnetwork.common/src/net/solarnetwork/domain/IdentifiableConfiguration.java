/* ==================================================================
 * IdentifiableConfiguration.java - 21/03/2018 11:29:48 AM
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

package net.solarnetwork.domain;

import java.util.Map;

/**
 * API for a user-supplied set of configuration to use with some
 * {@link Identifiable} service.
 * 
 * @author matt
 * @version 1.0
 * @since 1.42
 */
public interface IdentifiableConfiguration {

	/**
	 * Get a name for this configuration.
	 * 
	 * <p>
	 * This is expected to be a user-supplied name.
	 * </p>
	 * 
	 * @return a configuration name
	 */
	String getName();

	/**
	 * Get the unique identifier for the service this configuration is
	 * associated with.
	 * 
	 * <p>
	 * This value will correspond to some {@link Identifiable#getUid()} value.
	 * </p>
	 * 
	 * @return the service type identifier
	 */
	String getServiceIdentifier();

	/**
	 * Get a map of properties to pass to the service in order to perform
	 * actions.
	 * 
	 * <p>
	 * It is expected this map would contain user-supplied runtime configuration
	 * such as credentials to use, host name, etc.
	 * </p>
	 * 
	 * @return the runtime properties to pass to the service
	 */
	Map<String, ?> getServiceProperties();

}
