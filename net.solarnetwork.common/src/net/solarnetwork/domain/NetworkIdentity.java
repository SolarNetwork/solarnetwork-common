/* ==================================================================
 * NetworkIdentity.java - Sep 13, 2011 8:11:59 PM
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

import java.util.Map;

/**
 * Information that identifies the central SolarNet network.
 * 
 * @author matt
 * @version 1.2
 */
public interface NetworkIdentity {

	/**
	 * The {@link #getNetworkServiceURLs()} key for the SolarUser service.
	 * 
	 * @since 1.1
	 */
	final String SOLARUSER_NETWORK_SERVICE_KEY = "solaruser";

	/**
	 * The {@link #getNetworkServiceURLs()} key for the SolarQuery service.
	 * 
	 * @since 1.1
	 */
	final String SOLARQUERY_NETWORK_SERVICE_KEY = "solarquery";

	/**
	 * The {@link #getNetworkServiceURLs()} key for the SolarIn MQTT service.
	 * 
	 * @since 1.2
	 */
	final String SOLARIN_MQTT_NETWORK_SERVICE_KEY = "solarin-mqtt";

	/**
	 * Get the service host name.
	 * 
	 * @return map of service host name
	 */
	String getHost();

	/**
	 * The host port to use.
	 * 
	 * @return the port
	 */
	Integer getPort();

	/**
	 * Flag indicating if TLS must be used.
	 * 
	 * @return boolean
	 */
	boolean isForceTLS();

	/**
	 * Get a universally unique key that identifies this service.
	 * 
	 * @return unique key
	 */
	String getIdentityKey();

	/**
	 * Get the terms of service for this service.
	 * 
	 * @return the terms of service
	 */
	String getTermsOfService();

	/**
	 * Get a mapping of pre-defined network service URLs, to be used by clients
	 * to provide links to the SolarNetwork they are associated with.
	 * 
	 * @return a mapping of keys to string URLs
	 * @since 1.1
	 */
	Map<String, String> getNetworkServiceURLs();

}
