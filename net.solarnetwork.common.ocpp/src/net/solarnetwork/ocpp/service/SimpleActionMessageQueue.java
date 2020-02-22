/* ==================================================================
 * SimpleActionMessageQueue.java - 12/02/2020 10:26:16 am
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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import net.solarnetwork.ocpp.domain.PendingActionMessage;

/**
 * FIXME
 * 
 * <p>
 * TODO
 * </p>
 * 
 * @author matt
 * @version 1.0
 */
public class SimpleActionMessageQueue implements ActionMessageQueue {

	private final Map<String, Deque<PendingActionMessage>> pendingMessages;

	/**
	 * Constructor.
	 */
	public SimpleActionMessageQueue() {
		this(new ConcurrentHashMap<>(8, 0.7f, 2));
	}

	/**
	 * Constructor.
	 * 
	 * @param pendingMessages
	 *        the pending message queue map
	 */
	public SimpleActionMessageQueue(Map<String, Deque<PendingActionMessage>> pendingMessages) {
		super();
		this.pendingMessages = pendingMessages;
	}

	@Override
	public Deque<PendingActionMessage> pendingMessageQueue(String clientId) {
		return pendingMessages.computeIfAbsent(clientId, k -> new ArrayDeque<>(8));
	}

	@Override
	public void addPendingMessage(PendingActionMessage msg, Consumer<Deque<PendingActionMessage>> fn) {
		String clientId = msg.getMessage().getClientId();
		Deque<PendingActionMessage> q = pendingMessageQueue(clientId);
		synchronized ( q ) {
			// enqueue the call
			q.add(msg);
			if ( fn != null && q.peek() == msg ) {
				fn.accept(q);
			}
		}
	}

	@Override
	public PendingActionMessage pollPendingMessage(String clientId) {
		PendingActionMessage msg = null;
		Deque<PendingActionMessage> q = pendingMessages.get(clientId);
		if ( q != null ) {
			synchronized ( q ) {
				msg = q.pollFirst();
			}
		}
		return msg;
	}

	@Override
	public PendingActionMessage pollPendingMessage(final String clientId, final String messageId) {
		PendingActionMessage msg = null;
		Deque<PendingActionMessage> q = pendingMessages.get(clientId);
		if ( q != null ) {
			synchronized ( q ) {
				for ( Iterator<PendingActionMessage> itr = q.descendingIterator(); itr.hasNext(); ) {
					PendingActionMessage oneMsg = itr.next();
					if ( oneMsg.getMessage().getMessageId().equals(messageId) ) {
						msg = oneMsg;
						itr.remove();
						break;
					}
				}
			}
		}
		return msg;
	}

	@Override
	public Iterable<Entry<String, Deque<PendingActionMessage>>> allQueues() {
		return pendingMessages.entrySet();
	}

}
