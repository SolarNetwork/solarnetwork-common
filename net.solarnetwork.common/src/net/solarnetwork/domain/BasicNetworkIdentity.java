/* ==================================================================
 * BasicNetworkIdentity.java - Sep 13, 2011 8:16:37 PM
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

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Basic implementation of {@link NetworkIdentity}.
 *
 * @author matt
 * @version 1.3
 */
@JsonPropertyOrder({ "host", "port", "forceTLS", "networkServiceURLs", "identityKey", "termsOfService" })
@JsonIgnoreProperties({ "solarQueryServiceURL", "solarUserServiceURL", "solarInMqttServiceURL" })
public class BasicNetworkIdentity implements NetworkIdentity, Serializable {

	private static final long serialVersionUID = 1734756599885882478L;

	/** The identify key. */
	private String identityKey;

	/** The terms of service. */
	private String termsOfService;

	/** The host. */
	private String host;

	/** The port. */
	private Integer port;

	/** The "force TLS" flag. */
	private boolean forceTLS;

	/** Map of network service URLs. */
	private Map<String, String> networkServiceURLs;

	/**
	 * Default constructor.
	 */
	public BasicNetworkIdentity() {
		super();
	}

	/**
	 * Construct with an identity key.
	 *
	 * @param identityKey
	 *        the identity key
	 * @param termsOfService
	 *        the TOS
	 * @param host
	 *        the service host
	 * @param port
	 *        the service port
	 * @param forceTLS
	 *        {@literal true} if TLS should be used on a port other than
	 *        {@literal 443}
	 */
	public BasicNetworkIdentity(String identityKey, String termsOfService, String host, Integer port,
			Boolean forceTLS) {
		super();
		setIdentityKey(identityKey);
		setTermsOfService(termsOfService);
		setHost(host);
		setPort(port);
		setForceTLS(forceTLS);
	}

	@Override
	public String getIdentityKey() {
		return identityKey;
	}

	@Override
	public String getTermsOfService() {
		return termsOfService;
	}

	@Override
	public String getHost() {
		return host;
	}

	@Override
	public Integer getPort() {
		return port;
	}

	@Override
	public boolean isForceTLS() {
		return forceTLS;
	}

	/**
	 * Set the identity key.
	 *
	 * @param identityKey
	 *        the key to set
	 */
	public void setIdentityKey(String identityKey) {
		this.identityKey = identityKey;
	}

	/**
	 * Set the terms of service.
	 *
	 * @param termsOfService
	 *        the terms to set
	 */
	public void setTermsOfService(String termsOfService) {
		this.termsOfService = termsOfService;
	}

	/**
	 * Set the host.
	 *
	 * @param host
	 *        the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * Set the port.
	 *
	 * @param port
	 *        the port to set
	 */
	public void setPort(Integer port) {
		this.port = port;
	}

	/**
	 * Set the force TLS flag.
	 *
	 * @param forceTLS
	 *        the flag to set
	 */
	public void setForceTLS(boolean forceTLS) {
		this.forceTLS = forceTLS;
	}

	@Override
	public Map<String, String> getNetworkServiceURLs() {
		return networkServiceURLs;
	}

	/**
	 * Set the network service URLs.
	 *
	 * @param networkServiceURLs
	 *        the service URLs to set
	 */
	public void setNetworkServiceURLs(Map<String, String> networkServiceURLs) {
		this.networkServiceURLs = networkServiceURLs;
	}

	private void putServiceURL(String key, String url) {
		if ( key == null ) {
			return;
		}
		if ( url == null ) {
			if ( networkServiceURLs == null ) {
				return;
			}
			networkServiceURLs.remove(key);
		} else {
			Map<String, String> map = networkServiceURLs;
			if ( map == null ) {
				map = new LinkedHashMap<String, String>(2);
				networkServiceURLs = map;
			}
			map.put(key, url);
		}
	}

	private String getServiceURL(String key) {
		return (networkServiceURLs == null ? null : networkServiceURLs.get(key));
	}

	/**
	 * Put a {@code networkServiceURLs} value for the
	 * {@link NetworkIdentity#SOLARUSER_NETWORK_SERVICE_KEY} key.
	 *
	 * @param url
	 *        The URL to set, or {@literal null} to remove.
	 */
	public void setSolarUserServiceURL(String url) {
		putServiceURL(SOLARUSER_NETWORK_SERVICE_KEY, url);
	}

	/**
	 * Get the {@link NetworkIdentity#SOLARUSER_NETWORK_SERVICE_KEY}
	 * {@code networkServiceURLs} value.
	 *
	 * @return the URL
	 */
	public String getSolarUserServiceURL() {
		return getServiceURL(SOLARUSER_NETWORK_SERVICE_KEY);
	}

	/**
	 * Put a {@code networkServiceURLs} value for the
	 * {@link NetworkIdentity#SOLARQUERY_NETWORK_SERVICE_KEY} key.
	 *
	 * @param url
	 *        The URL to set, or {@literal null} to remove.
	 */
	public void setSolarQueryServiceURL(String url) {
		putServiceURL(SOLARQUERY_NETWORK_SERVICE_KEY, url);
	}

	/**
	 * Get the {@link NetworkIdentity#SOLARQUERY_NETWORK_SERVICE_KEY}
	 * {@code networkServiceURLs} value.
	 *
	 * @return the URL
	 */
	public String getSolarQueryServiceURL() {
		return getServiceURL(SOLARQUERY_NETWORK_SERVICE_KEY);
	}

	/**
	 * Put a {@code networkServiceURLs} value for the
	 * {@link NetworkIdentity#SOLARIN_MQTT_NETWORK_SERVICE_KEY} key.
	 *
	 * @param url
	 *        The URL to set, or {@literal null} to remove.
	 */
	public void setSolarInMqttServiceURL(String url) {
		putServiceURL(SOLARIN_MQTT_NETWORK_SERVICE_KEY, url);
	}

	/**
	 * Get the {@link NetworkIdentity#SOLARIN_MQTT_NETWORK_SERVICE_KEY}
	 * {@code networkServiceURLs} value.
	 *
	 * @return the URL
	 */
	public String getSolarInMqttServiceURL() {
		return getServiceURL(SOLARIN_MQTT_NETWORK_SERVICE_KEY);
	}

}
