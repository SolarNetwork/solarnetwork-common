/* ==================================================================
 * ErrorCodeResolver.java - 3/04/2020 7:31:17 am
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

package net.solarnetwork.ocpp.service;

import net.solarnetwork.ocpp.domain.ErrorCode;
import net.solarnetwork.ocpp.json.RpcError;

/**
 * API to resolve error codes from other error types.
 * 
 * @author matt
 * @version 1.0
 * @since 1.1
 */
public interface ErrorCodeResolver {

	/**
	 * Resolve an {@link ErrorCode} for a {@link RpcError}.
	 * 
	 * @param rpcError
	 *        the RPC error
	 * @return the error code, never {@literal null}
	 */
	ErrorCode errorCodeForRpcError(RpcError rpcError);

	/**
	 * Resolve an {@link ErrorCode} from an error name.
	 * 
	 * @param name
	 *        the name to resolve
	 * @return the error, never {@literal null}
	 * @throws IllegalArgumentException
	 *         if {@code name} is not a valid error value
	 */
	ErrorCode errorCodeForName(String name);

}
