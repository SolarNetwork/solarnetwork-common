/* ==================================================================
 * ActionMessageResultHandler.java - 4/02/2020 4:16:28 pm
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

import net.solarnetwork.ocpp.domain.ActionMessage;

/**
 * API for handling the result of an {@link ActionMessage}.
 * 
 * <p>
 * This API is not specific to any OCPP protocol version, so that services can
 * be designed that support multiple versions.
 * </p>
 * 
 * @param <T>
 *        the message type
 * @param <R>
 *        the result type
 * @author matt
 * @version 1.0
 */
@FunctionalInterface
public interface ActionMessageResultHandler<T, R> {

	/**
	 * Handle an {@link ActionMessage} result.
	 * 
	 * @param message
	 *        the source message the result is for
	 * @param result
	 *        the successful result, or {@literal null} if no result is
	 *        available
	 * @param error
	 *        the error result, or {@literal null} if no error occurred; can
	 *        implement {@link net.solarnetwork.ocpp.domain.ErrorHolder} to pass specific details
	 * @return {@literal true} if the result was handled, {@literal false}
	 *         otherwise
	 */
	boolean handleActionMessageResult(ActionMessage<T> message, R result, Throwable error);

}
