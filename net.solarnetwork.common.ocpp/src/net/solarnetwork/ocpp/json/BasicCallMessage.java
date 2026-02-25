/* ==================================================================
 * BasicCallMessage.java - 31/01/2020 8:09:22 am
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

package net.solarnetwork.ocpp.json;

import static net.solarnetwork.util.ObjectUtils.requireNonEmptyArgument;
import static net.solarnetwork.util.ObjectUtils.requireNonNullArgument;
import org.jspecify.annotations.Nullable;
import net.solarnetwork.ocpp.domain.Action;

/**
 * A basic implementation of {@link CallMessage}.
 *
 * @author matt
 * @version 1.0
 */
public class BasicCallMessage extends BaseMessage implements CallMessage {

	private final String clientId;
	private final String messageId;
	private final Action action;
	private final @Nullable Object payload;

	/**
	 * Constructor.
	 *
	 * @param clientId
	 *        the ID of the client that initiated this message; must not be
	 *        {@code null}
	 * @param messageId
	 *        a unique ID for this message; must not be {@code null}
	 * @param action
	 *        the message action; must not be {@code null}
	 * @throws IllegalArgumentException
	 *         if {@code messageId} or {@code action} are {@code null}
	 */
	public BasicCallMessage(String clientId, String messageId, Action action) {
		this(clientId, messageId, action, null);
	}

	/**
	 * Constructor.
	 *
	 * @param clientId
	 *        the ID of the client that initiated this message; must not be
	 *        {@code null}
	 * @param messageId
	 *        a unique ID for this message; must not be {@code null}
	 * @param action
	 *        the message action; must not be {@code null}
	 * @param payload
	 *        the optional message content
	 * @throws IllegalArgumentException
	 *         if {@code clientId}, {@code messageId}, or {@code action} are
	 *         {@code null}
	 */
	public BasicCallMessage(String clientId, String messageId, Action action, @Nullable Object payload) {
		super();
		this.clientId = requireNonEmptyArgument(clientId, "clientId");
		this.messageId = requireNonEmptyArgument(messageId, "messageId");
		this.action = requireNonNullArgument(action, "action");
		this.payload = payload;
	}

	@Override
	public final String getClientId() {
		return clientId;
	}

	@Override
	public final String getMessageId() {
		return messageId;
	}

	@Override
	public final Action getAction() {
		return action;
	}

	@Override
	public final @Nullable Object getPayload() {
		return payload;
	}

}
