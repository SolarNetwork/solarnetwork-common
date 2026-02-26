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

import static net.solarnetwork.util.ObjectUtils.requireNonNullArgument;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jspecify.annotations.Nullable;
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
	 * Constructor for outbound message response.
	 *
	 * @param message
	 *        the message
	 * @param handler
	 *        the handler
	 * @throws IllegalArgumentException
	 *         if any argument is {@code null}
	 */
	public PendingActionMessage(ActionMessage<Object> message,
			ActionMessageResultHandler<Object, Object> handler) {
		this(message, handler, true);
	}

	/**
	 * Constructor for inbound message response.
	 *
	 * <p>
	 * A no-operation handler will be set.
	 * </p>
	 *
	 * @param message
	 *        the message
	 * @throws IllegalArgumentException
	 *         if any argument is {@code null}
	 */
	public PendingActionMessage(ActionMessage<Object> message) {
		this(message, PendingActionMessage::noop, false);
	}

	private PendingActionMessage(ActionMessage<Object> message,
			ActionMessageResultHandler<Object, Object> handler, boolean outbound) {
		super();
		this.message = requireNonNullArgument(message, "message");
		this.handler = requireNonNullArgument(handler, "handler");
		this.date = System.currentTimeMillis();
		this.outbound = outbound;
		this.processed = new AtomicBoolean(false);
	}

	private static boolean noop(ActionMessage<Object> message, @Nullable Object result,
			@Nullable Throwable error) {
		// ignore
		return true;
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

	/**
	 * Get the message date.
	 *
	 * @return the date
	 */
	public final long getDate() {
		return date;
	}

	/**
	 * Get the outbound flag.
	 *
	 * @return the outbound flag
	 */
	public final boolean isOutbound() {
		return outbound;
	}

	/**
	 * Get the message.
	 *
	 * @return the message; never {@code null}
	 */
	public final ActionMessage<Object> getMessage() {
		return message;
	}

	/**
	 * Get the result handler.
	 *
	 * @return the handler; never {@code null}
	 */
	public final ActionMessageResultHandler<Object, Object> getHandler() {
		return handler;
	}

}
