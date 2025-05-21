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

import java.net.URI;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.Future;
import net.solarnetwork.common.mqtt.MqttMessageHandler;
import net.solarnetwork.common.mqtt.MqttProperties;
import net.solarnetwork.common.mqtt.MqttTopicAliases;

/**
 * API for a MQTT client.
 *
 * <p>
 * Unless otherwise noted, all methods should be implemented in a thread-safe
 * manner. Callback implementations for asynchronous methods must not make any
 * assumptions on which thread invokes the callback.
 * </p>
 *
 * @author matt
 * @version 1.1
 */
public interface MqttClient {

	/**
	 * Toggle wire-level logging support.
	 *
	 * <p>
	 * If enabled, then when connections are opened they will include logging
	 * support under a logger prefix {@literal net.solarnetwork.mqtt.} followed
	 * by {@literal host:port}.
	 * </p>
	 *
	 * @param wireLogging
	 *        {@literal true} to add a {@link LoggingHandler} to the channel
	 *        pipeline when it is initialized
	 */
	void setWireLogging(boolean wireLogging);

	/**
	 * Connect to the specified hostname or IP address.
	 *
	 * <p>
	 * By default uses port 1883. If you want to change the port number, see
	 * {@link #connect(String, int)}/
	 * </p>
	 *
	 * @param host
	 *        The IP address or host to connect to
	 * @return A future which will be completed when the connection is opened
	 *         and we received an CONNACK
	 */
	Future<MqttConnectResult> connect(String host);

	/**
	 * Connect to the specified hostname or IP address using the specified port.
	 *
	 * @param host
	 *        The IP address or host to connect to
	 * @param port
	 *        The tcp port to connect to
	 * @return A future which will be completed when the connection is opened
	 *         and we received an CONNACK
	 */
	Future<MqttConnectResult> connect(String host, int port);

	/**
	 * Test if the connection is available.
	 *
	 * @return boolean value indicating if channel is active
	 */
	boolean isConnected();

	/**
	 * Get the URI for the connected MQTT server, if available.
	 *
	 * @return the server URI, or {@literal null} if
	 *         {@link #connect(String, int)} has not previously been called
	 */
	URI getServerUri();

	/**
	 * Attempt reconnect to the host that was attempted with
	 * {@link #connect(String, int)} method before.
	 *
	 * @return A future which will be completed when the connection is opened
	 *         and we received an CONNACK
	 * @throws IllegalStateException
	 *         if no previous {@link #connect(String, int)} calls were attempted
	 */
	Future<MqttConnectResult> reconnect();

	/**
	 * Retrieve the Netty {@link EventLoopGroup} we are using.
	 *
	 * @return The Netty {@link EventLoopGroup} we use for the connection
	 */
	EventLoopGroup getEventLoop();

	/**
	 * Set a custom {@link EventLoopGroup}.
	 *
	 * <p>
	 * If you change the EventLoopGroup to another type, make sure to change the
	 * {@link Channel} class using
	 * {@link MqttClientConfig#setChannelClass(Class)} If you want to force the
	 * MqttClient to use another {@link EventLoopGroup}, call this function
	 * before calling {@link #connect(String, int)}.
	 * </p>
	 *
	 * @param eventLoop
	 *        The new eventloop to use
	 */
	void setEventLoop(EventLoopGroup eventLoop);

	/**
	 * Subscribe on the given topic.
	 *
	 * <p>
	 * When a message is received, MqttClient will invoke the
	 * {@link MqttMessageHandler#onMqttMessage(net.solarnetwork.common.mqtt.MqttMessage)}
	 * function of the given handler.
	 * </p>
	 *
	 * @param topic
	 *        The topic filter to subscribe to
	 * @param handler
	 *        The handler to invoke when we receive a message
	 * @return A future which will be completed when the server acknowledges our
	 *         subscribe request
	 */
	Future<Void> on(String topic, MqttMessageHandler handler);

	/**
	 * Subscribe on the given topic, with the given QOS.
	 *
	 * <p>
	 * When a message is received, MqttClient will invoke the
	 * {@link MqttMessageHandler#onMqttMessage(net.solarnetwork.common.mqtt.MqttMessage)}
	 * function of the given handler.
	 * </p>
	 *
	 * @param topic
	 *        The topic filter to subscribe to
	 * @param handler
	 *        The handler to invoke when we receive a message
	 * @param qos
	 *        The qos to request to the server
	 * @return A future which will be completed when the server acknowledges our
	 *         subscribe request
	 */
	Future<Void> on(String topic, MqttMessageHandler handler, MqttQoS qos);

	/**
	 * Subscribe on the given topic.
	 *
	 * <p>
	 * When a message is received, MqttClient will invoke the
	 * {@link MqttMessageHandler#onMqttMessage(net.solarnetwork.common.mqtt.MqttMessage)}
	 * function of the given handler. This subscription is only once. If the
	 * MqttClient has received 1 message, the subscription will be removed.
	 * </p>
	 *
	 * @param topic
	 *        The topic filter to subscribe to
	 * @param handler
	 *        The handler to invoke when we receive a message
	 * @return A future which will be completed when the server acknowledges our
	 *         subscribe request
	 */
	Future<Void> once(String topic, MqttMessageHandler handler);

	/**
	 * Subscribe on the given topic, with the given QOS.
	 *
	 * <p>
	 * When a message is received, MqttClient will invoke the
	 * {@link MqttMessageHandler#onMqttMessage(net.solarnetwork.common.mqtt.MqttMessage)}
	 * function of the given handler. This subscription is only once. If the
	 * MqttClient has received 1 message, the subscription will be removed.
	 * </p>
	 *
	 * @param topic
	 *        The topic filter to subscribe to
	 * @param handler
	 *        The handler to invoke when we receive a message
	 * @param qos
	 *        The qos to request to the server
	 * @return A future which will be completed when the server acknowledges our
	 *         subscribe request
	 */
	Future<Void> once(String topic, MqttMessageHandler handler, MqttQoS qos);

	/**
	 * Remove the subscription for the given topic and handler.
	 *
	 * <p>
	 * If you want to unsubscribe from all handlers known for this topic, use
	 * {@link #off(String)}.
	 * </p>
	 *
	 * @param topic
	 *        The topic to unsubscribe for
	 * @param handler
	 *        The handler to unsubscribe
	 * @return A future which will be completed when the server acknowledges our
	 *         unsubscribe request
	 */
	Future<Void> off(String topic, MqttMessageHandler handler);

	/**
	 * Remove all subscriptions for the given topic.
	 *
	 * <p>
	 * If you want to specify which handler to unsubscribe, use
	 * {@link #off(String, MqttMessageHandler)}.
	 * </p>
	 *
	 * @param topic
	 *        The topic to unsubscribe for
	 * @return A future which will be completed when the server acknowledges our
	 *         unsubscribe request
	 */
	Future<Void> off(String topic);

	/**
	 * Publish a message to the given payload.
	 *
	 * @param topic
	 *        The topic to publish to
	 * @param payload
	 *        The payload to send
	 * @return A future which will be completed when the message is sent out of
	 *         the MqttClient
	 */
	Future<Void> publish(String topic, ByteBuf payload);

	/**
	 * Publish a message to the given payload, using the given QOS.
	 *
	 * @param topic
	 *        The topic to publish to
	 * @param payload
	 *        The payload to send
	 * @param qos
	 *        The qos to use while publishing
	 * @return A future which will be completed when the message is delivered to
	 *         the server
	 */
	Future<Void> publish(String topic, ByteBuf payload, MqttQoS qos);

	/**
	 * Publish a message to the given payload, using optional retain flag.
	 *
	 * @param topic
	 *        The topic to publish to
	 * @param payload
	 *        The payload to send
	 * @param retain
	 *        true if you want to retain the message on the server, false
	 *        otherwise
	 * @return A future which will be completed when the message is sent out of
	 *         the MqttClient
	 */
	Future<Void> publish(String topic, ByteBuf payload, boolean retain);

	/**
	 * Publish a message to the given payload, using the given QOS and optional
	 * retain flag.
	 *
	 * @param topic
	 *        The topic to publish to
	 * @param payload
	 *        The payload to send
	 * @param qos
	 *        The qos to use while publishing
	 * @param retain
	 *        true if you want to retain the message on the server, false
	 *        otherwise
	 * @return A future which will be completed when the message is delivered to
	 *         the server
	 */
	Future<Void> publish(String topic, ByteBuf payload, MqttQoS qos, boolean retain);

	/**
	 * Publish a message to the given payload, using the given QOS and optional
	 * retain flag.
	 *
	 * @param topic
	 *        The topic to publish to
	 * @param payload
	 *        The payload to send
	 * @param qos
	 *        The qos to use while publishing
	 * @param retain
	 *        true if you want to retain the message on the server, false
	 *        otherwise
	 * @param properties
	 *        properties, or {@literal null}
	 * @return A future which will be completed when the message is delivered to
	 *         the server
	 */
	Future<Void> publish(String topic, ByteBuf payload, MqttQoS qos, boolean retain,
			MqttProperties properties);

	/**
	 * Retrieve the MqttClient configuration.
	 *
	 * @return The {@link MqttClientConfig} instance we use
	 */
	MqttClientConfig getClientConfig();

	/**
	 * Construct the MqttClientImpl with additional config.
	 *
	 * <p>
	 * This config can also be changed using the {@link #getClientConfig()}
	 * function.
	 * </p>
	 *
	 * @param config
	 *        The config object to use while looking for settings
	 * @param defaultHandler
	 *        The handler for incoming messages that do not match any topic
	 *        subscriptions
	 * @return the client, never {@literal null}
	 */
	static MqttClient create(MqttClientConfig config, MqttMessageHandler defaultHandler) {
		return new MqttClientImpl(config, defaultHandler);
	}

	/**
	 * Send disconnect and close channel.
	 *
	 * @return A future which will be completed when the channel has been
	 *         closed.
	 */
	java.util.concurrent.Future<?> disconnect();

	/**
	 * Get disconnected flag.
	 *
	 * @return {@literal true} if {@link #disconnect()} has been called to close
	 *         the connection
	 * @since 1.1
	 */
	boolean isDisconnected();

	/**
	 * Sets the {@link MqttClientCallback} object for this MqttClient.
	 *
	 * @param callback
	 *        The callback to be set
	 */
	void setCallback(MqttClientCallback callback);

	/**
	 * Get the topic aliases.
	 *
	 * @return the topic aliases, never {@literal null}
	 * @since 1.1
	 */
	MqttTopicAliases getTopicAliases();

}
