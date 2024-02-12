/* ==================================================================
 * ActionErrorCode.java - 9/02/2024 10:19:20 am
 * 
 * Copyright 2024 SolarNetwork.net Dev Team
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

package net.solarnetwork.ocpp.v201.domain;

/**
 * OCPP v2.0 action error code enumeration.
 * 
 * @author matt
 * @version 1.0
 */
public enum ActionErrorCode implements net.solarnetwork.ocpp.domain.ErrorCode {

	/** Payload for Action is syntactically incorrect. */
	FormatViolation,

	/**
	 * Any other error not covered by the more specific error codes in this
	 * enumeration.
	 */
	GenericError,

	/**
	 * An internal error occurred and the receiver was not able to process the
	 * requested Action successfully.
	 */
	InternalError,

	/**
	 * A message with an Message Type Number received that is not supported by
	 * this implementation.
	 */
	MessageTypeNotSupported,

	/** Requested Action is not known by receiver. */
	NotImplemented,

	/** Requested Action is recognized but not supported by the receiver. */
	NotSupported,

	/**
	 * Payload for Action is syntactically correct but at least one of the
	 * fields violates occurrence constraints.
	 */
	OccurrenceConstraintViolation,

	/**
	 * Payload is syntactically correct but at least one field contains an
	 * invalid value.
	 */
	PropertyConstraintViolation,

	/** Payload for Action is not conform the PDU structure. */
	ProtocolError,

	/**
	 * Content of the call is not a valid RPC Request, for example: MessageId
	 * could not be read.
	 */
	RpcFrameworkError,

	/**
	 * During the processing of Action a security issue occurred preventing
	 * receiver from completing the Action successfully.
	 */
	SecurityError,

	/**
	 * Payload for Action is syntactically correct but at least one of the
	 * fields violates data type constraints (e.g. "somestring": 12).
	 */
	TypeConstraintViolation,

	;

	@Override
	public String getName() {
		return name();
	}

}
