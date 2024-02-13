/* ==================================================================
 * ErrorCodeResolver.java - 3/04/2020 7:34:15 am
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

package net.solarnetwork.ocpp.v16;

import net.solarnetwork.ocpp.domain.ErrorCode;
import net.solarnetwork.ocpp.json.RpcError;

/**
 * {@link net.solarnetwork.ocpp.service.ErrorCodeResolver} for OCPP v1.6.
 * 
 * @author matt
 * @version 1.1
 * @since 1.1
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
				return ActionErrorCode.OccurenceConstraintViolation;

			case PayloadPropertyConstraintViolation:
				return ActionErrorCode.PropertyConstraintViolation;

			case PayloadProtocolError:
				return ActionErrorCode.ProtocolError;

			case PayloadSyntaxError:
				return ActionErrorCode.FormationViolation;

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
