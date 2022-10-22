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
import java.util.LinkedHashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import net.solarnetwork.service.IdentifiableConfiguration;
import net.solarnetwork.util.CollectionUtils;
import net.solarnetwork.util.StringUtils;

/**
 * Basic implementation of {@link IdentifiableConfiguration}.
 * 
 * @author matt
 * @version 1.2
 * @since 1.42
 */
@JsonPropertyOrder({ "name", "serviceIdentifier", "serviceProperties" })
public class BasicIdentifiableConfiguration implements IdentifiableConfiguration, Serializable {

	private static final long serialVersionUID = 8272531095755880837L;

	private String name;
	private String serviceIdentifier;
	private Map<String, Object> serviceProps;

	/**
	 * Default constructor.
	 */
	public BasicIdentifiableConfiguration() {
		super();
	}

	/**
	 * Copy constructor.
	 * 
	 * @param other
	 *        the configuration to copy
	 */
	public BasicIdentifiableConfiguration(IdentifiableConfiguration other) {
		super();
		if ( other == null ) {
			return;
		}
		setName(other.getName());
		setServiceIdentifier(other.getServiceIdentifier());
		if ( other.getServiceProperties() != null ) {
			Map<String, Object> sprops = new LinkedHashMap<String, Object>(other.getServiceProperties());
			setServiceProps(sprops);
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getSimpleName());
		builder.append("{");
		if ( name != null ) {
			builder.append("name=");
			builder.append(name);
			builder.append(", ");
		}
		if ( serviceIdentifier != null ) {
			builder.append("serviceIdentifier=");
			builder.append(serviceIdentifier);
			builder.append(", ");
		}
		if ( serviceProps != null ) {
			builder.append("serviceProps=");
			Map<String, Object> maskedServiceProps = StringUtils.sha256MaskedMap(serviceProps,
					CollectionUtils.sensitiveNamesToMask(serviceProps.keySet()));
			builder.append(maskedServiceProps);
		}
		builder.append("}");
		return builder.toString();
	}

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

	/**
	 * Get the service properties.
	 * 
	 * @return the properties
	 */
	@JsonGetter("serviceProperties")
	public Map<String, Object> getServiceProps() {
		return serviceProps;
	}

	/**
	 * Set the service properties.
	 * 
	 * @param serviceProps
	 *        the properties to set
	 */
	@JsonSetter("serviceProperties")
	public void setServiceProps(Map<String, Object> serviceProps) {
		this.serviceProps = serviceProps;
	}

}
