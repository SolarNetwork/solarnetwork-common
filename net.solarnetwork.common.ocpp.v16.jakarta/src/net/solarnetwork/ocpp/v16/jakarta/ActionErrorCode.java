/* ==================================================================
 * ActionErrorCode.java - 31/01/2020 7:59:13 am
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

package net.solarnetwork.ocpp.v16.jakarta;

/**
 * OCPP v1.6 action error code enumeration.
 * 
 * @author matt
 * @version 1.0
 */
public enum ActionErrorCode implements net.solarnetwork.ocpp.domain.ErrorCode {

	/** Requested Action is not known by receiver. */
	NotImplemented,

	/** Requested Action is recognized but not supported by the receiver. */
	NotSupported,

	/**
	 * An internal error occurred and the receiver was not able to process the
	 * requested Action successfully.
	 */
	InternalError,

	/** Payload for Action is incomplete. */
	ProtocolError,

	/**
	 * During the processing of Action a security issue occurred preventing
	 * receiver from completing the Action successfully.
	 */
	SecurityError,

	/**
	 * Payload for Action is syntactically incorrect or not conform the PDU
	 * structure for Action.
	 */
	FormationViolation,

	/**
	 * Payload is syntactically correct but at least one field contains an
	 * invalid value.
	 */
	PropertyConstraintViolation,

	/**
	 * Payload for Action is syntactically correct but at least one of the
	 * fields violates occurrence constraints.
	 */
	OccurenceConstraintViolation,

	/**
	 * Payload for Action is syntactically correct but at least one of the
	 * fields violates data type constraints (e.g. “somestring”: 12).
	 */
	TypeConstraintViolation,

	/** Any other error not covered by the previous ones. */
	GenericError;

	@Override
	public String getName() {
		return name();
	}
}
