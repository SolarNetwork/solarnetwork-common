/* ==================================================================
 * ChargePointActionMessage.java - 2/04/2020 4:17:56 pm
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

package net.solarnetwork.ocpp.v16.cp;

import java.util.UUID;
import com.fasterxml.jackson.databind.JsonNode;
import net.solarnetwork.ocpp.domain.ActionMessage;
import net.solarnetwork.ocpp.domain.ChargePointIdentity;
import ocpp.domain.Action;
import ocpp.v16.ChargePointAction;

/**
 * {@link ActionMessage} implementation for charge point actions processed as
 * JSON messages.
 * 
 * @author matt
 * @version 1.0
 */
public class ChargePointActionMessage implements ActionMessage<JsonNode> {

	private final ChargePointIdentity clientId;
	private final String messageId;
	private final Action action;
	private final JsonNode message;

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
	public ChargePointActionMessage(ChargePointIdentity clientId, ChargePointAction action,
			JsonNode message) {
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
	public ChargePointActionMessage(ChargePointIdentity clientId, String messageId,
			ChargePointAction action, JsonNode message) {
		super();
		this.clientId = clientId;
		this.messageId = messageId;
		this.action = action;
		this.message = message;
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
	public JsonNode getMessage() {
		return message;
	}

}
