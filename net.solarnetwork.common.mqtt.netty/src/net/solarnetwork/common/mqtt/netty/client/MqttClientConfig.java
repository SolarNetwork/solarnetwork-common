/**
 * Copyright 2019 SolarNetwork.net Dev Team
 * Copyright © 2016-2019 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.solarnetwork.common.mqtt.netty.client;

import java.util.Random;
import io.netty.channel.Channel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.mqtt.MqttProperties;
import io.netty.handler.codec.mqtt.MqttProperties.IntegerProperty;
import io.netty.handler.codec.mqtt.MqttProperties.MqttProperty;
import io.netty.handler.codec.mqtt.MqttVersion;
import io.netty.handler.ssl.SslContext;

/**
 * MQTT client configuration.
 * 
 * @author matt
 * @version 1.2
 */
public final class MqttClientConfig {

	private final SslContext sslContext;
	private final String randomClientId;
	private final MqttProperties connectionProperties = new MqttProperties();

	private String clientId;
	private int timeoutSeconds = 60;
	private int readTimeoutSeconds = -1;
	private int writeTimeoutSeconds = -1;
	private MqttVersion protocolVersion = MqttVersion.MQTT_3_1;
	private String username = null;
	private String password = null;
	private boolean cleanSession = true;
	private MqttLastWill lastWill;
	private Class<? extends Channel> channelClass = NioSocketChannel.class;

	private boolean reconnect = true;
	private long reconnectDelay = 1L;
	private int maxBytesInMessage = 8092;

	public MqttClientConfig() {
		this(null);
	}

	public MqttClientConfig(SslContext sslContext) {
		this.sslContext = sslContext;
		Random random = new Random();
		String id = "netty-mqtt/";
		String[] options = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".split("");
		for ( int i = 0; i < 8; i++ ) {
			id += options[random.nextInt(options.length)];
		}
		this.clientId = id;
		this.randomClientId = id;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		if ( clientId == null ) {
			this.clientId = randomClientId;
		} else {
			this.clientId = clientId;
		}
	}

	public int getTimeoutSeconds() {
		return timeoutSeconds;
	}

	public void setTimeoutSeconds(int timeoutSeconds) {
		if ( timeoutSeconds != -1 && timeoutSeconds <= 0 ) {
			throw new IllegalArgumentException("timeoutSeconds must be > 0 or -1");
		}
		this.timeoutSeconds = timeoutSeconds;
	}

	/**
	 * Get a read-specific timeout.
	 * 
	 * @return the seconds to use for a read-specific timeout, or {@literal 0}
	 *         to disable or {@literal -1} to use the
	 *         {@link #getTimeoutSeconds()} value; defaults to {@literal -1}
	 */
	public int getReadTimeoutSeconds() {
		return readTimeoutSeconds;
	}

	/**
	 * Set the read-specific timeout.
	 * 
	 * @param readTimeoutSeconds
	 *        the timeout to set, or {@literal 0} to disable or {@literal -1} to
	 *        use the {@link #getTimeoutSeconds()} value
	 */
	public void setReadTimeoutSeconds(int readTimeoutSeconds) {
		this.readTimeoutSeconds = readTimeoutSeconds;
	}

	/**
	 * Get a write-specific timeout.
	 * 
	 * @return the seconds to use for a write-specific timeout, or {@literal 0}
	 *         to disable or {@literal -1} to use the
	 *         {@link #getTimeoutSeconds()} value; defaults to {@literal -1}
	 */
	public int getWriteTimeoutSeconds() {
		return writeTimeoutSeconds;
	}

	/**
	 * Set the write-specific timeout.
	 * 
	 * @param writeTimeoutSeconds
	 *        the timeout to set, or {@literal 0} to disable or {@literal -1} to
	 *        use the {@link #getTimeoutSeconds()} value
	 */
	public void setWriteTimeoutSeconds(int writeTimeoutSeconds) {
		this.writeTimeoutSeconds = writeTimeoutSeconds;
	}

	public MqttVersion getProtocolVersion() {
		return protocolVersion;
	}

	public void setProtocolVersion(MqttVersion protocolVersion) {
		if ( protocolVersion == null ) {
			throw new NullPointerException("protocolVersion");
		}
		this.protocolVersion = protocolVersion;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isCleanSession() {
		return cleanSession;
	}

	public void setCleanSession(boolean cleanSession) {
		this.cleanSession = cleanSession;
	}

	public MqttLastWill getLastWill() {
		return lastWill;
	}

	public void setLastWill(MqttLastWill lastWill) {
		this.lastWill = lastWill;
	}

	public Class<? extends Channel> getChannelClass() {
		return channelClass;
	}

	public void setChannelClass(Class<? extends Channel> channelClass) {
		this.channelClass = channelClass;
	}

	public SslContext getSslContext() {
		return sslContext;
	}

	public boolean isReconnect() {
		return reconnect;
	}

	public void setReconnect(boolean reconnect) {
		this.reconnect = reconnect;
	}

	public long getReconnectDelay() {
		return reconnectDelay;
	}

	/**
	 * Sets the reconnect delay in seconds. Defaults to 1 second.
	 * 
	 * @param reconnectDelay
	 *        the reconnection delay, in seconds
	 * @throws IllegalArgumentException
	 *         if reconnectDelay is smaller than 1.
	 */
	public void setReconnectDelay(long reconnectDelay) {
		if ( reconnectDelay <= 0 ) {
			throw new IllegalArgumentException("reconnectDelay must be > 0");
		}
		this.reconnectDelay = reconnectDelay;
	}

	public int getMaxBytesInMessage() {
		return maxBytesInMessage;
	}

	/**
	 * Sets the maximum number of bytes in the message for the
	 * {@link io.netty.handler.codec.mqtt.MqttDecoder}. Default value is 8092 as
	 * specified by Netty. The absolute maximum size is 256MB as set by the MQTT
	 * spec.
	 *
	 * @param maxBytesInMessage
	 *        the maximum number of bytes to allow
	 * @throws IllegalArgumentException
	 *         if maxBytesInMessage is smaller than 1 or greater than
	 *         256_000_000.
	 */
	public void setMaxBytesInMessage(int maxBytesInMessage) {
		if ( maxBytesInMessage <= 0 || maxBytesInMessage > 256_000_000 ) {
			throw new IllegalArgumentException("maxBytesInMessage must be > 0 or < 256_000_000");
		}
		this.maxBytesInMessage = maxBytesInMessage;
	}

	/**
	 * Get the MQTT connection properties.
	 * 
	 * @return the propertes, never {@literal null}
	 * @since 1.1
	 */
	public MqttProperties getConnectionProperties() {
		return connectionProperties;
	}

	/**
	 * Convenience method to set the {@code TOPIC_ALIAS_MAXIMUM} connection
	 * property.
	 * 
	 * @param max
	 *        the maximum number of topic aliases to allow on the connection
	 * @since 1.1
	 */
	public void setMaximumTopicAliases(int max) {
		connectionProperties.add(
				new IntegerProperty(MqttProperties.MqttPropertyType.TOPIC_ALIAS_MAXIMUM.value(), max));
	}

	/**
	 * Convenience method to get the {@code TOPIC_ALIAS_MAXIMUM} connection
	 * property.
	 * 
	 * @return the maximum number of topic aliases to allow on the connection
	 * @since 1.1
	 */
	public int getMaximumTopicAliases() {
		@SuppressWarnings("rawtypes")
		MqttProperty prop = connectionProperties
				.getProperty(MqttProperties.MqttPropertyType.TOPIC_ALIAS_MAXIMUM.value());
		return (prop instanceof IntegerProperty ? ((IntegerProperty) prop).value() : 0);
	}

}
