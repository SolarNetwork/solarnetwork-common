/* ==================================================================
 * ErrorCodeResolver.java - 9/02/2024 10:26:16 am
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

package net.solarnetwork.common.ocpp.v201.domain;

import net.solarnetwork.ocpp.domain.ErrorCode;
import net.solarnetwork.ocpp.json.RpcError;

/**
 * {@link net.solarnetwork.ocpp.service.ErrorCodeResolver} for OCPP v2.0.
 * 
 * @author matt
 * @version 1.0
 */
public class ErrorCodeResolver implements net.solarnetwork.ocpp.service.ErrorCodeResolver {

	/**
	 * Constructor.
	 */
	public ErrorCodeResolver() {
		super();
	}

	@SuppressWarnings("deprecation")
	@Override
	public ActionErrorCode errorCodeForRpcError(RpcError rpcError) {
		switch (rpcError) {
			case ActionNotImplemented:
				return ActionErrorCode.NotImplemented;

			case ActionNotSupported:
				return ActionErrorCode.NotSupported;

			case GenericError:
			case MessageSyntaxError:
			case MessageTypeNotSupported:
				return ActionErrorCode.GenericError;

			case InternalError:
				return ActionErrorCode.InternalError;

			case PayloadOccurenceConstraintViolation:
			case PayloadOccurrenceConstraintViolation:
				return ActionErrorCode.OccurrenceConstraintViolation;

			case PayloadPropertyConstraintViolation:
				return ActionErrorCode.PropertyConstraintViolation;

			case PayloadProtocolError:
				return ActionErrorCode.ProtocolError;

			case PayloadSyntaxError:
				return ActionErrorCode.FormatViolation;

			case PayloadTypeConstraintViolation:
				return ActionErrorCode.TypeConstraintViolation;

			case SecurityError:
				return ActionErrorCode.SecurityError;

		}
		return null;
	}

	@Override
	public ErrorCode errorCodeForName(String name) {
		return ActionErrorCode.valueOf(name);
	}

}
