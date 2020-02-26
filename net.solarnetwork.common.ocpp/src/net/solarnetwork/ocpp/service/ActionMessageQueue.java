/* ==================================================================
 * ActionMessageQueue.java - 12/02/2020 10:25:44 am
 * 
 * Copyright 2020 SolarNetwork.net Dev Team
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

package net.solarnetwork.ocpp.service;

import java.util.Deque;
import java.util.Map.Entry;
import java.util.function.Consumer;
import net.solarnetwork.ocpp.domain.ActionMessage;
import net.solarnetwork.ocpp.domain.ChargePointIdentity;
import net.solarnetwork.ocpp.domain.PendingActionMessage;

/**
 * API for thread-safe management of action message queues partitioned by client
 * identifiers.
 * 
 * <p>
 * The operations in this API are all thread-safe. Any method that gives access
 * a {@link Deque}, like
 * {@link #addPendingMessage(PendingActionMessage, Consumer)} or
 * {@link #allQueues()} <b>should</b> synchronize on those instances for thread
 * safety.
 * </p>
 * 
 * @author matt
 * @version 1.0
 */
public interface ActionMessageQueue {

	/**
	 * Get the pending message queue for a specific client.
	 * 
	 * @param clientId
	 *        the client ID
	 * @return the queue, never {@literal null}
	 */
	Deque<PendingActionMessage> pendingMessageQueue(ChargePointIdentity clientId);

	/**
	 * Add a message to the pending message queue.
	 * 
	 * <p>
	 * The pending message's {@link ActionMessage#getClientId()} will determine
	 * the client queue the message is added to.
	 * </p>
	 * 
	 * @param msg
	 *        the message to add
	 */
	default void addPendingMessage(PendingActionMessage msg) {
		addPendingMessage(msg, null);
	}

	/**
	 * Add a message to the pending message queue.
	 * 
	 * <p>
	 * The pending message's {@link ActionMessage#getClientId()} will determine
	 * the client queue the message is added to.
	 * </p>
	 * 
	 * @param msg
	 *        the message to add
	 * @param fn
	 *        an optional function to apply synchronously
	 */
	void addPendingMessage(PendingActionMessage msg, Consumer<Deque<PendingActionMessage>> fn);

	/**
	 * Find and remove the first available message from the pending message
	 * queue.
	 * 
	 * @param clientId
	 *        the ID of the client
	 * @return the found message, or {@literal null} if no messages available
	 */
	PendingActionMessage pollPendingMessage(final ChargePointIdentity clientId);

	/**
	 * Find and remove a message from the pending message queue, based on its
	 * message ID.
	 * 
	 * <p>
	 * This method will search the pending messages queue and return the first
	 * message found with the matching message ID, after first removing that
	 * message from the queue.
	 * </p>
	 * 
	 * @param clientId
	 *        the ID of the client
	 * @param messageId
	 *        the ID to find
	 * @return the found message, or {@literal null} if not found
	 */
	PendingActionMessage pollPendingMessage(final ChargePointIdentity clientId, final String messageId);

	/**
	 * Get an iterable for all available queues.
	 * 
	 * @return the iterable, never {@literal null}
	 */
	Iterable<Entry<ChargePointIdentity, Deque<PendingActionMessage>>> allQueues();
}
