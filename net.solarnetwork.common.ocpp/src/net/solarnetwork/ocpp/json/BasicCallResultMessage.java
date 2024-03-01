/* ==================================================================
 * BasicCallResultMessage.java - 31/01/2020 8:24:10 am
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

/**
 * Basic implementation of {@link CallResultMessage}.
 * 
 * @author matt
 * @version 1.0
 */
public class BasicCallResultMessage extends BaseMessage implements CallResultMessage {

	private final String messageId;
	private final Object payload;

	/**
	 * Constructor.
	 * 
	 * @param messageId
	 *        a unique ID for this message; must not be {@literal null}
	 * @throws IllegalArgumentException
	 *         if {@code messageId} is {@literal null}
	 */
	public BasicCallResultMessage(String messageId) {
		this(messageId, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param messageId
	 *        a unique ID for this message; must not be {@literal null}
	 * @param payload
	 *        the optional message content
	 * @throws IllegalArgumentException
	 *         if {@code messageId} is {@literal null}
	 */
	public BasicCallResultMessage(String messageId, Object payload) {
		super();
		if ( messageId == null || messageId.isEmpty() ) {
			throw new IllegalArgumentException("The messageId parameter must be provided.");
		}
		this.messageId = messageId;
		this.payload = payload;
	}

	@Override
	public String getMessageId() {
		return messageId;
	}

	@Override
	public Object getPayload() {
		return payload;
	}

}
