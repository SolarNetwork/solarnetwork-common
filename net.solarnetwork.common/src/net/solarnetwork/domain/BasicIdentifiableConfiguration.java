/* ==================================================================
 * BasicIdentifiableConfiguration.java - 21/03/2018 11:32:38 AM
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

import java.io.Serializable;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * Basic implementation of {@link IdentifiableConfiguration}.
 * 
 * @author matt
 * @version 1.0
 * @since 1.42
 */
@JsonPropertyOrder({ "name", "serviceIdentifier", "compressionType", "serviceProperties" })
public class BasicIdentifiableConfiguration implements IdentifiableConfiguration, Serializable {

	private static final long serialVersionUID = 8272531095755880837L;

	private String name;
	private String serviceIdentifier;
	private Map<String, Object> serviceProps;

	/**
	 * Get a name for this configuration
	 * 
	 * <p>
	 * This is meant to be configurable by end users.
	 * </p>
	 * 
	 * @return the configuration name
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Set the configuration name.
	 * 
	 * @param name
	 *        the name to use
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get the unique identifier for the service this configuration is
	 * associated with.
	 * 
	 * @return the identifier of the service
	 */
	@Override
	public String getServiceIdentifier() {
		return serviceIdentifier;
	}

	/**
	 * Set the unique identifier for the service this configuration is
	 * associated with.
	 * 
	 * @param serviceIdentifier
	 *        the identifier of the service to use
	 */
	public void setServiceIdentifier(String serviceIdentifier) {
		this.serviceIdentifier = serviceIdentifier;
	}

	@Override
	@JsonIgnore
	public Map<String, ?> getServiceProperties() {
		return getServiceProps();
	}

	@JsonGetter("serviceProperties")
	public Map<String, Object> getServiceProps() {
		return serviceProps;
	}

	@JsonSetter("serviceProperties")
	public void setServiceProps(Map<String, Object> serviceProps) {
		this.serviceProps = serviceProps;
	}

}
