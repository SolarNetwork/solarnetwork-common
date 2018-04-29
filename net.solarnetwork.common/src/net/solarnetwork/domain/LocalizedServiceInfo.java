/* ==================================================================
 * LocalizedServiceInfo.java - 11/04/2018 4:06:25 PM
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
 * API for information about a service that has been localized.
 * 
 * <p>
 * This API does not provide a way to localize the information. Rather, it is a
 * marker for an instance that has already been localized. This is designed to
 * support APIs that can localize objects based on a requested locale.
 * </p>
 * 
 * @author matt
 * @version 1.0
 * @since 1.43
 */
public interface LocalizedServiceInfo extends Identity<String> {

	/**
	 * Get the locale used for this information.
	 * 
	 * @return the locale
	 */
	String getLocale();

	/**
	 * Get a localized name for the service.
	 * 
	 * @return the service name
	 */
	String getLocalizedName();

	/**
	 * Get a localized description of the service.
	 * 
	 * @return the service description
	 */
	String getLocalizedDescription();

	/**
	 * Get a map of other localized information.
	 * 
	 * <p>
	 * The keys in the returned map are service-dependent and defined in advance
	 * so they are well-known. The values represent localized messages
	 * associated with those well-known keys.
	 * </p>
	 * 
	 * @return a map of messages, never {@literal null}
	 */
	Map<String, String> getLocalizedInfoMessages();

}
