/* ==================================================================
 * NettyMqttConnection.java - 25/11/2019 7:27:40 am
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

package net.solarnetwork.common.mqtt.netty;

import java.io.IOException;
import java.nio.charset.Charset;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;
import org.thingsboard.mqtt.MqttClient;
import org.thingsboard.mqtt.MqttClientConfig;
import org.thingsboard.mqtt.MqttHandler;
import org.thingsboard.mqtt.MqttLastWill;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttVersion;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import net.solarnetwork.common.mqtt.BasicMqttConnectionConfig;
import net.solarnetwork.common.mqtt.MqttConnection;
import net.solarnetwork.common.mqtt.MqttConnectionConfig;
import net.solarnetwork.common.mqtt.MqttMessage;
import net.solarnetwork.support.CertificateException;
import net.solarnetwork.support.SSLService;

/**
 * Netty based implementation of {@link MqttConnection}.
 * 
 * @author matt
 * @version 1.0
 */
public class NettyMqttConnection implements MqttConnection, MqttHandler {

	private final BasicMqttConnectionConfig connectionConfig;

	private boolean closed;
	private MqttClient client;

	/**
	 * Constructor.
	 */
	public NettyMqttConnection() {
		super();
		this.closed = false;
		this.connectionConfig = new BasicMqttConnectionConfig();
	}

	@Override
	public synchronized void close() throws IOException {
		if ( closed ) {
			return;
		}
		closed = true;
		if ( client != null ) {
			try {
				client.disconnect();
			} finally {
				client = null;
			}
		}
	}

	@Override
	public synchronized void open() throws IOException {
		if ( !closed || client != null ) {
			return;
		}
		MqttClientConfig config = createClientConfig();
		if ( config != null ) {
			client = MqttClient.create(config, this);
		}
	}

	private MqttClientConfig createClientConfig() {
		final MqttConnectionConfig connConfig = getConnectionConfig();
		if ( connConfig == null ) {
			return null;
		}
		final MqttClientConfig config;
		if ( connConfig.getSslService() != null ) {
			config = new MqttClientConfig(createSslContext(connConfig.getSslService()));
		} else {
			config = new MqttClientConfig();
		}
		config.setCleanSession(connConfig.isCleanSession());
		config.setClientId(config.getClientId());
		if ( connConfig.getLastWill() != null ) {
			MqttMessage msg = connConfig.getLastWill();
			MqttLastWill lwt = new MqttLastWill(msg.getTopic(),
					new String(msg.getPayload(), Charset.forName("UTF-8")), msg.isRetained(),
					MqttQoS.valueOf(msg.getQosLevel().getValue()));
			config.setLastWill(lwt);
		}
		config.setMaxBytesInMessage(connConfig.getMaximumMessageSize());
		config.setPassword(connConfig.getPassword());
		switch (connConfig.getVersion()) {
			case Mqtt31:
				config.setProtocolVersion(MqttVersion.MQTT_3_1);
				break;

			default:
				config.setProtocolVersion(MqttVersion.MQTT_3_1_1);
		}
		config.setReconnect(connConfig.isReconnect());
		config.setReconnectDelay(connConfig.getReconnectDelaySeconds());
		config.setTimeoutSeconds(connConfig.getKeepAliveSeconds());
		config.setUsername(connConfig.getUsername());
		return config;
	}

	private SslContext createSslContext(SSLService sslService) {
		TrustManagerFactory tmf = sslService.getTrustManagerFactory();
		KeyManagerFactory kmf = sslService.getKeyManagerFactory();
		try {
			return SslContextBuilder.forClient().keyManager(kmf).trustManager(tmf).build();
		} catch ( SSLException e ) {
			throw new CertificateException(
					"Error configuring SSL for MQTT connection: " + e.getMessage(), e);
		}
	}

	@Override
	public boolean isEstablished() {
		final MqttClient client;
		synchronized ( this ) {
			if ( closed ) {
				return false;
			}
			client = client();
		}
		if ( client == null ) {
			return false;
		}
		synchronized ( client ) {
			return client.isConnected();
		}
	}

	@Override
	public synchronized boolean isClosed() {
		return closed;
	}

	private MqttClient client() {
		return client;
	}

	@Override
	public void onMessage(String topic, ByteBuf payload) {
		// TODO Auto-generated method stub

	}

	/**
	 * Get the connection configuration.
	 * 
	 * @return the configuration, never {@literal null}
	 */
	public BasicMqttConnectionConfig getConnectionConfig() {
		return connectionConfig;
	}

}
