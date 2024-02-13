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
	private final Object payload;

	/**
	 * Constructor.
	 * 
	 * @param clientId
	 *        the ID of the client that initiated this message; must not be
	 *        {@literal null}
	 * @param messageId
	 *        a unique ID for this message; must not be {@literal null}
	 * @param action
	 *        the message action; must not be {@literal null}
	 * @throws IllegalArgumentException
	 *         if {@code messageId} or {@code action} are {@literal null}
	 */
	public BasicCallMessage(String clientId, String messageId, Action action) {
		this(clientId, messageId, action, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param clientId
	 *        the ID of the client that initiated this message; must not be
	 *        {@literal null}
	 * @param messageId
	 *        a unique ID for this message; must not be {@literal null}
	 * @param action
	 *        the message action; must not be {@literal null}
	 * @param payload
	 *        the optional message content
	 * @throws IllegalArgumentException
	 *         if {@code messageId} or {@code action} are {@literal null}
	 */
	public BasicCallMessage(String clientId, String messageId, Action action, Object payload) {
		super();
		if ( clientId == null || clientId.isEmpty() ) {
			throw new IllegalArgumentException("The clientId parameter must be provided.");
		}
		if ( messageId == null || messageId.isEmpty() ) {
			throw new IllegalArgumentException("The messageId parameter must be provided.");
		}
		if ( action == null ) {
			throw new IllegalArgumentException("The action parameter must be provided.");
		}
		this.clientId = clientId;
		this.messageId = messageId;
		this.action = action;
		this.payload = payload;
	}

	@Override
	public String getClientId() {
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
	public Object getPayload() {
		return payload;
	}

}
