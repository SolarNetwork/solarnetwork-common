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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import net.solarnetwork.service.OptionalService;
import net.solarnetwork.service.SSLService;
import net.solarnetwork.service.StaticOptionalService;

/**
 * Basic implementation of {@link MqttConnectionConfig}.
 * 
 * @author matt
 * @version 2.1
 */
public class BasicMqttConnectionConfig implements MqttConnectionConfig {

	/** The {@code reconnect} property default value. */
	public static final boolean DEFAULT_RECONNECT = true;

	/** The {@code reconnectDelaySeconds} property default value. */
	public static final int DEFAULT_RECONNECT_DELAY_SECONDS = 10;

	/** The {@code connectTimeoutSeconds} property default value. */
	public static final int DEFAULT_CONNECT_TIMEOUT_SECONDS = 10;

	/** The {@code keepAliveSeconds} property default value. */
	public static final int DEFAULT_KEEP_ALIVE_SECONDS = 60;

	/** The {@code maximumMessageSize} property default value. */
	public static final int DEFAULT_MAXIMUM_MESSAGE_SIZE = 8192;

	/** The {@code cleanSession} property default value. */
	public static final boolean DEFAULT_CLEAN_SESSION = true;

	/**
	 * The {@code version} property default value.
	 * 
	 * @since 1.1
	 */
	public static final MqttVersion DEFAULT_VERSION = MqttVersion.Mqtt311;

	private String uid;
	private URI serverUri;
	private MqttVersion version;
	private OptionalService<SSLService> optionalSslService;
	private String clientId;
	private String username;
	private String password;
	private boolean cleanSession;
	private int connectTimeoutSeconds;
	private boolean reconnect;
	private int reconnectDelaySeconds;
	private MqttMessage lastWill;
	private int maximumMessageSize;
	private int keepAliveSeconds;
	private int readTimeoutSeconds = -1;
	private int writeTimeoutSeconds = -1;
	private MqttStats stats;
	private boolean wireLoggingEnabled;
	private final BasicMutableMqttProperties properties;

	/**
	 * Default constructor.
	 */
	public BasicMqttConnectionConfig() {
		super();
		this.uid = UUID.randomUUID().toString();
		this.version = DEFAULT_VERSION;
		this.connectTimeoutSeconds = DEFAULT_CONNECT_TIMEOUT_SECONDS;
		this.reconnect = DEFAULT_RECONNECT;
		this.reconnectDelaySeconds = DEFAULT_RECONNECT_DELAY_SECONDS;
		this.keepAliveSeconds = DEFAULT_KEEP_ALIVE_SECONDS;
		this.maximumMessageSize = DEFAULT_MAXIMUM_MESSAGE_SIZE;
		this.cleanSession = DEFAULT_CLEAN_SESSION;
		this.properties = new BasicMutableMqttProperties();
	}

	/**
	 * Copy constructor.
	 * 
	 * @param other
	 *        the configuration to copy, or {@literal null}
	 */
	public BasicMqttConnectionConfig(MqttConnectionConfig other) {
		this();
		if ( other == null ) {
			return;
		}
		setUid(other.getUid());
		setServerUri(other.getServerUri());
		setVersion(other.getVersion());
		setSslService(other.getSslService());
		setClientId(other.getClientId());
		setUsername(other.getUsername());
		setPassword(other.getPassword());
		setCleanSession(other.isCleanSession());
		setConnectTimeoutSeconds(other.getConnectTimeoutSeconds());
		setReconnect(other.isReconnect());
		setReconnectDelaySeconds(other.getReconnectDelaySeconds());
		setLastWill(other.getLastWill());
		setMaximumMessageSize(other.getMaximumMessageSize());
		setKeepAliveSeconds(other.getKeepAliveSeconds());
		setStats(other.getStats());
	}

	@Override
	public String getUid() {
		return uid;
	}

	/**
	 * Set the unique ID.
	 * 
	 * @param uid
	 *        the ID
	 * @throws IllegalArgumentException
	 *         if {@code uid} is {@literal null} or empty
	 */
	public void setUid(String uid) {
		if ( uid == null || uid.isEmpty() ) {
			throw new IllegalArgumentException("The uid value must not be empty.");
		}
		this.uid = uid;
		if ( stats != null ) {
			stats.setUid(uid);
		}
	}

	@Override
	public URI getServerUri() {
		return serverUri;
	}

	/**
	 * Set the MQTT broker URI to connect to.
	 * 
	 * @param serverUri
	 *        the server URI
	 */
	public void setServerUri(URI serverUri) {
		this.serverUri = serverUri;
	}

	/**
	 * Get the MQTT broker URI to connect to, as a string.
	 * 
	 * @return the URI value, or {@literal null} if one has not been set
	 */
	public String getServerUriValue() {
		URI uri = getServerUri();
		return (uri != null ? uri.toString() : null);
	}

	/**
	 * Set the MQTT broker URI to connect to, as a string.
	 * 
	 * @param serverUri
	 *        the URI value
	 * @throws IllegalArgumentException
	 *         if {@code serverUri} is not a valid URI
	 */
	public void setServerUriValue(String serverUri) {
		try {
			setServerUri(new URI(serverUri));
		} catch ( URISyntaxException e ) {
			throw new IllegalArgumentException(
					"Invalid MQTT server URI [" + serverUri + "]: " + e.getMessage());
		}
	}

	@Override
	public boolean isUseSsl() {
		URI uri = getServerUri();
		String scheme = (uri != null ? uri.getScheme() : "mqtt");
		int port = (uri != null ? uri.getPort() : -1);
		boolean useSsl = (port == 8883 || "mqtts".equalsIgnoreCase(scheme)
				|| "ssl".equalsIgnoreCase(scheme));
		return useSsl;
	}

	/**
	 * Get the MQTT broker host name to connect to.
	 * 
	 * @return the host name, or {@literal null}
	 */
	public String getHost() {
		URI uri = getServerUri();
		return (uri != null ? uri.getHost() : null);
	}

	/**
	 * Get the MQTT broker port to connect to.
	 * 
	 * @return the port
	 */
	public int getPort() {
		URI uri = getServerUri();
		int port = (uri != null ? uri.getPort() : -1);
		if ( port == -1 ) {
			port = (isUseSsl() ? DEFAULT_PORT_SSL : DEFAULT_PORT);
		}
		return port;
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
		return (optionalSslService != null ? optionalSslService.service() : null);
	}

	/**
	 * Set the SSL service.
	 * 
	 * <p>
	 * Internally this calls
	 * {@link BasicMqttConnectionConfig#setOptionalSslService(OptionalService)}
	 * with a static service reference.
	 * </p>
	 * 
	 * @param sslService
	 *        the sslService to set
	 */
	public void setSslService(SSLService sslService) {
		setOptionalSslService(new StaticOptionalService<>(sslService));
	}

	/**
	 * Get the optional {@link SSLService}.
	 * 
	 * @return the optional service
	 */
	public OptionalService<SSLService> getOptionalSslService() {
		return optionalSslService;
	}

	/**
	 * Set the optional {@link SSLService}.
	 * 
	 * @param optionalSslService
	 *        the optional service
	 */
	public void setOptionalSslService(OptionalService<SSLService> optionalSslService) {
		this.optionalSslService = optionalSslService;
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
	public int getConnectTimeoutSeconds() {
		return connectTimeoutSeconds;
	}

	/**
	 * Set a connection timeout, in seconds.
	 * 
	 * @param connectTimeoutSeconds
	 *        the timeout to set
	 * @throws IllegalArgumentException
	 *         if {@code connectTimeoutSeconds} is not greater than {@literal 0}
	 */
	public void setConnectTimeoutSeconds(int connectTimeoutSeconds) {
		if ( connectTimeoutSeconds < 1 ) {
			throw new IllegalArgumentException("The connectTimeoutSeconds value must be > 0.");
		}
		this.connectTimeoutSeconds = connectTimeoutSeconds;
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

	@Override
	public MqttStats getStats() {
		return stats;
	}

	/**
	 * Set the MQTT statistics object to use.
	 * 
	 * <p>
	 * The UID of {@code stats} will be set to this object's UID.
	 * </p>
	 * 
	 * @param stats
	 *        the statistics object
	 */
	public void setStats(MqttStats stats) {
		this.stats = stats;
		if ( stats != null && uid != null ) {
			stats.setUid(uid);
		}
	}

	@Override
	public BasicMutableMqttProperties getProperties() {
		return properties;
	}

	@Override
	public boolean isWireLoggingEnabled() {
		return wireLoggingEnabled;
	}

	@Override
	public void setWireLoggingEnabled(boolean wireLoggingEnabled) {
		this.wireLoggingEnabled = wireLoggingEnabled;
	}

	@Override
	public int getReadTimeoutSeconds() {
		return readTimeoutSeconds;
	}

	/**
	 * Set the read-specific timeout.
	 * 
	 * @param readTimeoutSeconds
	 *        the timeout to set, or {@literal 0} to disable or {@literal -1} to
	 *        use the {@link #getTimeoutSeconds()} value
	 * @since 2.1
	 */
	public void setReadTimeoutSeconds(int readTimeoutSeconds) {
		this.readTimeoutSeconds = readTimeoutSeconds;
	}

	@Override
	public int getWriteTimeoutSeconds() {
		return writeTimeoutSeconds;
	}

	/**
	 * Set the write-specific timeout.
	 * 
	 * @param writeTimeoutSeconds
	 *        the timeout to set, or {@literal 0} to disable or {@literal -1} to
	 *        use the {@link #getTimeoutSeconds()} value
	 * @since 2.1
	 */
	public void setWriteTimeoutSeconds(int writeTimeoutSeconds) {
		this.writeTimeoutSeconds = writeTimeoutSeconds;
	}

}
