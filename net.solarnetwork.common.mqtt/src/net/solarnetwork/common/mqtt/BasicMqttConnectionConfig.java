/* ==================================================================
 * BasicMqttConnectionConfig.java - 25/11/2019 7:12:42 am
 * 
 * Copyright 2019 SolarNetwork.net Dev Team
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

package net.solarnetwork.common.mqtt;

import net.solarnetwork.support.SSLService;

/**
 * Basic implementation of {@link MqttConnectionConfig}.
 * 
 * @author matt
 * @version 1.0
 */
public class BasicMqttConnectionConfig implements MqttConnectionConfig {

	/** The {@code reconnect} property default value. */
	public static final boolean DEFAULT_RECONNECT = true;

	/** The {@code reconnectDelaySeconds} property default value. */
	public static final int DEFAULT_RECONNECT_DELAY_SECONDS = 10;

	private String host;
	private int port;
	private MqttVersion version;
	private SSLService sslService;
	private String clientId;
	private String username;
	private String password;
	private boolean cleanSession;
	private boolean reconnect;
	private int reconnectDelaySeconds;
	private MqttMessage lastWill;
	private int maximumMessageSize;
	private int keepAliveSeconds;

	/**
	 * Default constructor.
	 */
	public BasicMqttConnectionConfig() {
		super();
		this.version = MqttVersion.Mqtt311;
		this.port = DEFAULT_PORT;
		this.reconnect = DEFAULT_RECONNECT;
		this.reconnectDelaySeconds = DEFAULT_RECONNECT_DELAY_SECONDS;
	}

	@Override
	public String getHost() {
		return host;
	}

	/**
	 * Set the host to connect to.
	 * 
	 * @param host
	 *        the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}

	@Override
	public int getPort() {
		return port;
	}

	/**
	 * Set the port to connect to.
	 * 
	 * @param port
	 *        the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public MqttVersion getVersion() {
		return version;
	}

	/**
	 * Set the MQTT version.
	 * 
	 * @param version
	 *        the version to set
	 * @throws IllegalArgumentException
	 *         if {@code version} is {@literal null}
	 */
	public void setVersion(MqttVersion version) {
		if ( version == null ) {
			throw new IllegalArgumentException("The version value must not be null.");
		}
		this.version = version;
	}

	@Override
	public SSLService getSslService() {
		return sslService;
	}

	/**
	 * Set the SSL service.
	 * 
	 * @param sslService
	 *        the sslService to set
	 */
	public void setSslService(SSLService sslService) {
		this.sslService = sslService;
	}

	@Override
	public String getClientId() {
		return clientId;
	}

	/**
	 * Set the client ID.
	 * 
	 * @param clientId
	 *        the clientId to set
	 */
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	@Override
	public String getUsername() {
		return username;
	}

	/**
	 * Set the username.
	 * 
	 * @param username
	 *        the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	@Override
	public String getPassword() {
		return password;
	}

	/**
	 * Set the password.
	 * 
	 * @param password
	 *        the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public boolean isCleanSession() {
		return cleanSession;
	}

	@Override
	public boolean isReconnect() {
		return reconnect;
	}

	/**
	 * Set the reconnect flag.
	 * 
	 * @param reconnect
	 *        {@literal true} to automatically reconnect
	 */
	public void setReconnect(boolean reconnect) {
		this.reconnect = reconnect;
	}

	@Override
	public int getReconnectDelaySeconds() {
		return reconnectDelaySeconds;
	}

	/**
	 * Set the reconnect delay, in seconds.
	 * 
	 * @param reconnectDelaySeconds
	 *        the seconds to delay before reconnecting
	 */
	public void setReconnectDelaySeconds(int reconnectDelaySeconds) {
		this.reconnectDelaySeconds = reconnectDelaySeconds;
	}

	/**
	 * Set the clean session flag.
	 * 
	 * @param cleanSession
	 *        the cleanSession to set
	 */
	public void setCleanSession(boolean cleanSession) {
		this.cleanSession = cleanSession;
	}

	@Override
	public MqttMessage getLastWill() {
		return lastWill;
	}

	/**
	 * Set the last will message.
	 * 
	 * @param lastWill
	 *        the lastWill to set
	 */
	public void setLastWill(MqttMessage lastWill) {
		this.lastWill = lastWill;
	}

	@Override
	public int getMaximumMessageSize() {
		return maximumMessageSize;
	}

	/**
	 * Set the maximum message size.
	 * 
	 * @param maximumMessageSize
	 *        the maximumMessageSize to set, in bytes; must be greater than
	 *        {@literal 0} and less than or equal to {@literal 256000000}
	 * @throws IllegalArgumentException
	 *         if {@code maximumMessageSize} is not valid
	 */
	public void setMaximumMessageSize(int maximumMessageSize) {
		if ( !(maximumMessageSize > 0 && maximumMessageSize <= 256_000_000) ) {
			throw new IllegalArgumentException(
					"The maximumMessageSize value must be between 1 and 256_000_000.");
		}
		this.maximumMessageSize = maximumMessageSize;
	}

	@Override
	public int getKeepAliveSeconds() {
		return keepAliveSeconds;
	}

	/**
	 * Set the keep alive seconds.
	 * 
	 * @param keepAliveSeconds
	 *        the keepAliveSeconds to set, or {@literal -1} to disable
	 * @throws IllegalArgumentException
	 *         if {@code keepAliveSecondes} is not {@literal -1} or greater than
	 *         {@literal 0}
	 */
	public void setKeepAliveSeconds(int keepAliveSeconds) {
		if ( !(keepAliveSeconds == -1 || keepAliveSeconds > 0) ) {
			throw new IllegalArgumentException("The keepAliveSeconds value must be -1 or > 0.");
		}
		this.keepAliveSeconds = keepAliveSeconds;
	}

}
