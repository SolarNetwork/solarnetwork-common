/* ==================================================================
 * CallErrorMessage.java - 31/01/2020 7:55:45 am
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

import java.util.Map;
import net.solarnetwork.ocpp.domain.ErrorCode;

/**
 * A call error result message.
 * 
 * <p>
 * This type of message represents an error response to a {@link CallMessage}
 * initiated by a client.
 * </p>
 * 
 * @author matt
 * @version 1.0
 */
public interface CallErrorMessage extends Message {

	@Override
	default MessageType getMessageType() {
		return MessageType.CallError;
	}

	/**
	 * Get the unique ID of the {@link CallMessage} this is a response to.
	 * 
	 * @return the unique ID, never {@literal null}
	 */
	String getMessageId();

	/**
	 * Get the error code.
	 * 
	 * @return the error code, never {@literal null}
	 */
	ErrorCode getErrorCode();

	/**
	 * Get an optional error description.
	 * 
	 * @return a description of the error, if available
	 */
	String getErrorDescription();

	/**
	 * Optional implementation-specific details about the error.
	 * 
	 * @return a map of error detail properties
	 */
	Map<String, ?> getErrorDetails();

}
