/* ==================================================================
 * MqttMessageHandler.java - 23/11/2019 5:20:17 pm
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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * API for handling MQTT messages.
 * 
 * @author matt
 * @version 1.1
 */
public interface MqttMessageHandler {

	/**
	 * Handle a MQTT message.
	 * 
	 * @param message
	 *        the message to handle
	 */
	void onMqttMessage(MqttMessage message);

	/**
	 * Handle a MQTT message, possibly asynchronously.
	 *
	 * <p>
	 * Implement this in preference to {@link #onMqttMessage(MqttMessage)} to
	 * take the message off the connection's I/O thread. The message is
	 * self-contained, so it remains valid after this method returns.
	 * </p>
	 *
	 * <p>
	 * For a QOS 1 message the returned stage decides the acknowledgement: it is
	 * acknowledged once the stage completes normally, and <b>not</b> acknowledged
	 * if the stage completes exceptionally, leaving the broker to redeliver it.
	 * A stage that never completes is never acknowledged, so an implementation
	 * must bound its own work. At QOS 0 there is nothing to acknowledge, and at
	 * QOS 2 the acknowledgement has already been sent by the time a message is
	 * delivered here, so for those the outcome only affects logging.
	 * </p>
	 *
	 * <p>
	 * Handling messages asynchronously means they may be handled out of order,
	 * and concurrently. Implementations that require ordered, serial delivery
	 * should not implement this method.
	 * </p>
	 *
	 * <p>
	 * The default implementation delegates to
	 * {@link #onMqttMessage(MqttMessage)} and so handles the message
	 * synchronously, on the calling thread.
	 * </p>
	 *
	 * @param message
	 *        the message to handle
	 * @return a stage that completes when the message has been accepted, or
	 *         completes exceptionally if it has not; never {@literal null}
	 * @since 1.1
	 */
	default CompletionStage<?> onMqttMessageAsync(MqttMessage message) {
		try {
			onMqttMessage(message);
			return CompletableFuture.completedFuture(null);
		} catch ( RuntimeException e ) {
			return CompletableFuture.failedFuture(e);
		}
	}

}
