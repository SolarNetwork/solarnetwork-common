/* ==================================================================
 * BasicCallErrorMessage.java - 31/01/2020 9:37:32 am
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
 * Basic implementation of {@link CallErrorMessage}.
 * 
 * @author matt
 * @version 1.0
 */
public class BasicCallErrorMessage extends BaseMessage implements CallErrorMessage {

	private final String messageId;
	private final ErrorCode errorCode;
	private final String errorDescription;
	private final Map<String, ?> errorDetails;

	/**
	 * Constructor.
	 * 
	 * @param messageId
	 *        a unique ID for this message; must not be {@literal null}
	 * @param errorCode
	 *        the error code; must not be {@literal null}
	 * @throws IllegalArgumentException
	 *         if {@code messageId} or {@code errorCode} are {@literal null}
	 */
	public BasicCallErrorMessage(String messageId, ErrorCode errorCode) {
		this(messageId, errorCode, null, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param messageId
	 *        a unique ID for this message; must not be {@literal null}
	 * @param errorCode
	 *        the error code; must not be {@literal null}
	 * @param errorDescription
	 *        an optional description of the error
	 * @param errorDetails
	 *        optional implementation-specific error detail properties
	 * @throws IllegalArgumentException
	 *         if {@code messageId} or {@code errorCode} are {@literal null}
	 */
	public BasicCallErrorMessage(String messageId, ErrorCode errorCode, String errorDescription,
			Map<String, ?> errorDetails) {
		super();
		if ( messageId == null || messageId.isEmpty() ) {
			throw new IllegalArgumentException("The messageId parameter must be provided.");
		}
		if ( errorCode == null ) {
			throw new IllegalArgumentException("The errorCode parameter must be provided.");
		}
		this.messageId = messageId;
		this.errorCode = errorCode;
		this.errorDescription = errorDescription;
		this.errorDetails = errorDetails;
	}

	@Override
	public String getMessageId() {
		return messageId;
	}

	@Override
	public ErrorCode getErrorCode() {
		return errorCode;
	}

	@Override
	public String getErrorDescription() {
		return errorDescription;
	}

	@Override
	public Map<String, ?> getErrorDetails() {
		return errorDetails;
	}

}
