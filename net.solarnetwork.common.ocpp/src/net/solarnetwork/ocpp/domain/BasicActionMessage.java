/* ==================================================================
 * BasicActionMessage.java - 4/02/2020 5:56:42 pm
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

import java.util.UUID;

/**
 * Basic implementation of {@link ActionMessage}.
 * 
 * @param <T>
 *        the message type
 * @author matt
 * @version 1.1
 */
public class BasicActionMessage<T> implements ActionMessage<T> {

	private final ChargePointIdentity clientId;
	private final String messageId;
	private final Action action;
	private final T message;

	/**
	 * Constructor.
	 * 
	 * <p>
	 * A new unique random UUID will be used for the message ID value.
	 * </p>
	 * 
	 * @param clientId
	 *        the client ID
	 * @param action
	 *        the action
	 * @param message
	 *        the message
	 */
	public BasicActionMessage(ChargePointIdentity clientId, Action action, T message) {
		this(clientId, UUID.randomUUID().toString(), action, message);
	}

	/**
	 * Constructor.
	 * 
	 * @param clientId
	 *        the client ID
	 * @param messageId
	 *        the message ID
	 * @param action
	 *        the action
	 * @param message
	 *        the message
	 */
	public BasicActionMessage(ChargePointIdentity clientId, String messageId, Action action, T message) {
		super();
		this.clientId = clientId;
		this.messageId = messageId;
		this.action = action;
		this.message = message;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BasicActionMessage{");
		if ( clientId != null ) {
			builder.append("clientId=");
			builder.append(clientId);
			builder.append(", ");
		}
		if ( messageId != null ) {
			builder.append("messageId=");
			builder.append(messageId);
			builder.append(", ");
		}
		if ( action != null ) {
			builder.append("action=");
			builder.append(action);
			builder.append(", ");
		}
		if ( message != null ) {
			builder.append("message=");
			builder.append(message);
		}
		builder.append("}");
		return builder.toString();
	}

	@Override
	public ChargePointIdentity getClientId() {
		return clientId;
	}

	@Override
	public String getMessageId() {
		return messageId;
	}

	@Override
	public Action getAction() {
		return action;
	}

	@Override
	public T getMessage() {
		return message;
	}

}
