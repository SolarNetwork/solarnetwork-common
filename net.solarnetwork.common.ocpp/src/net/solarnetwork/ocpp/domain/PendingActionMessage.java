/* ==================================================================
 * PendingActionMessage.java - 11/02/2020 7:18:33 am
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

package net.solarnetwork.ocpp.domain;

import java.util.concurrent.atomic.AtomicBoolean;
import net.solarnetwork.ocpp.service.ActionMessageResultHandler;

/**
 * A "pending" action message, which is waiting for a result message to be
 * received.
 * 
 * @author matt
 * @version 1.0
 */
public class PendingActionMessage {

	private final long date;
	private final boolean outbound;
	private final ActionMessage<Object> message;
	private final ActionMessageResultHandler<Object, Object> handler;
	private final AtomicBoolean processed;

	/**
	 * Constructor for inbound message response.
	 * 
	 * @param message
	 *        the message
	 * @param handler
	 *        the handler
	 * @throws IllegalArgumentException
	 *         if {@code message} or {@code handler} are {@literal null}
	 */
	public PendingActionMessage(ActionMessage<Object> message,
			ActionMessageResultHandler<Object, Object> handler) {
		super();
		if ( message == null ) {
			throw new IllegalArgumentException("The message parameter must not be null.");
		}
		if ( handler == null ) {
			throw new IllegalArgumentException("The handler parameter must not be null.");
		}
		this.date = System.currentTimeMillis();
		this.outbound = true;
		this.message = message;
		this.handler = handler;
		this.processed = new AtomicBoolean(false);
	}

	/**
	 * Constructor for inbound message response.
	 * 
	 * @param message
	 *        the message
	 * @throws IllegalArgumentException
	 *         if {@code message} is {@literal null}
	 */
	public PendingActionMessage(ActionMessage<Object> message) {
		super();
		this.date = System.currentTimeMillis();
		this.outbound = false;
		if ( message == null ) {
			throw new IllegalArgumentException("The message parameter must not be null.");
		}
		this.message = message;
		this.handler = null;
		this.processed = new AtomicBoolean(false);
	}

	/**
	 * Get the message date.
	 * 
	 * @return the date
	 */
	public long getDate() {
		return date;
	}

	/**
	 * Get the outbound flag.
	 * 
	 * @return the outbound flag
	 */
	public boolean isOutbound() {
		return outbound;
	}

	/**
	 * Get the message.
	 * 
	 * @return the message; never {@literal null}
	 */
	public ActionMessage<Object> getMessage() {
		return message;
	}

	/**
	 * Get the result handler.
	 * 
	 * @return the handler; never {@literal null}
	 */
	public ActionMessageResultHandler<Object, Object> getHandler() {
		return handler;
	}

	/**
	 * Call to process the pending action.
	 * 
	 * @return {@literal true} if the action can be processed, {@literal false}
	 *         if this method has already been called
	 */
	public boolean doProcess() {
		return processed.compareAndSet(false, true);
	}

}
