/* ==================================================================
 * RpcError.java - 3/04/2020 7:17:19 am
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
 * Standardized RPC protocol error types.
 * 
 * @author matt
 * @version 1.1
 * @since 1.1
 */
public enum RpcError {

	/** Requested Action is not known by receiver. */
	ActionNotImplemented,

	/** Requested Action is recognized but not supported by the receiver. */
	ActionNotSupported,

	/**
	 * An internal error occurred and the receiver was not able to process the
	 * request.
	 */
	InternalError,

	/** The message type is not supported by the receiver. */
	MessageTypeNotSupported,

	/** Content of the request is not valid. */
	MessageSyntaxError,

	/**
	 * During the processing of Action a security issue occurred preventing
	 * receiver from completing the Action successfully.
	 */
	SecurityError,

	/**
	 * Payload is syntactically correct but at least one field contains an
	 * invalid value.
	 */
	PayloadPropertyConstraintViolation,

	/**
	 * Payload for Action is syntactically correct but at least one of the
	 * fields violates occurrence constraints.
	 * 
	 * @deprecated use {@link #PayloadOccurrenceConstraintViolation}
	 */
	@Deprecated
	PayloadOccurenceConstraintViolation,

	/**
	 * Payload for Action is syntactically correct but at least one of the
	 * fields violates occurrence constraints.
	 * 
	 * @since 1.1
	 */
	PayloadOccurrenceConstraintViolation,

	/**
	 * Payload for Action is syntactically correct but at least one of the
	 * fields violates data type constraints (e.g. “somestring”: 12).
	 */
	PayloadTypeConstraintViolation,

	/**
	 * Payload for Action is incomplete in some way other than
	 * {@code PropertyConstraintViolation} or
	 * {@code  OccurenceConstraintViolation}.
	 */
	PayloadProtocolError,

	/** Payload is syntactically incorrect. */
	PayloadSyntaxError,

	/** Any other error not covered by the previous ones. */
	GenericError;

}
