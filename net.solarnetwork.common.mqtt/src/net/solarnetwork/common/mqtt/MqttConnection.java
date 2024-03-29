/* ==================================================================
 * MqttConnection.java - 24/11/2019 12:29:14 pm
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

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Future;

/**
 * API for a connection to a MQTT broker.
 * 
 * @author matt
 * @version 1.0
 */
public interface MqttConnection extends Closeable {

	/**
	 * Open the connection, if it is not already open.
	 * 
	 * <p>
	 * The connection must be opened before calling any of the other methods in
	 * this API. The {@link #close()} method must be called when the connection
	 * is longer needed.
	 * </p>
	 * 
	 * @throws IOException
	 *         if the connection cannot be opened
	 * @return future with results of connection
	 */
	Future<MqttConnectReturnCode> open() throws IOException;

	/**
	 * Test if the connection has been established.
	 * 
	 * @return {@literal true} if the connection has been established,
	 *         {@literal false} if the connection has never been opened or has
	 *         been closed
	 */
	boolean isEstablished();

	/**
	 * Test if {@link #close()} has been called.
	 * 
	 * <p>
	 * This method does not necessarily verify if the physical connection has
	 * been terminated, it is merely an indication if {@link #close()} has been
	 * invoked.
	 * </p>
	 * 
	 * @return {@literal true} if {@link #close()} has been invoked on this
	 *         connection
	 */
	boolean isClosed();

	/**
	 * Publish a message.
	 * 
	 * <p>
	 * If MQTT 5 or higher is used, then topic aliases will be automatically
	 * applied as long as the connected broker supports them.
	 * </p>
	 * 
	 * @param message
	 *        the message to publish
	 * @return a future that completes when the message has been published
	 */
	Future<?> publish(MqttMessage message);

	/**
	 * Subscribe to a topic.
	 * 
	 * @param topic
	 *        the topic to subscribe to
	 * @param qosLevel
	 *        the desired quality of service of the subscription
	 * @param handler
	 *        the message handler; if {@literal null} then the configured
	 *        {@link #setMessageHandler(MqttMessageHandler)} will be used
	 * @return a future that completes when the subscription has been
	 *         acknowledged by the MQTT server
	 */
	Future<?> subscribe(String topic, MqttQos qosLevel, MqttMessageHandler handler);

	/**
	 * Unsubscribe from a topic.
	 * 
	 * @param topic
	 *        the topic to unsubscribe from
	 * @param handler
	 *        the message handler to unsubscribe, or {@literal null} for the
	 *        configured {@link #setMessageHandler(MqttMessageHandler)}
	 * @return a future that completes when the unsubscription has been
	 *         acknowledged by the MQTT server
	 */
	Future<?> unsubscribe(String topic, MqttMessageHandler handler);

	/**
	 * Configure a connection-wide message handler, to receive all MQTT messages
	 * not associated with a more specific subscription handler.
	 * 
	 * @param handler
	 *        the handler
	 */
	void setMessageHandler(MqttMessageHandler handler);

	/**
	 * Configure a connection observer, to monitor the state of the connection.
	 * 
	 * @param observer
	 *        the observer
	 */
	void setConnectionObserver(MqttConnectionObserver observer);

}
