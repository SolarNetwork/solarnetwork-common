/* ==================================================================
 * ChargePointBroker.java - 11/02/2020 5:50:50 am
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

import java.util.Set;
import net.solarnetwork.ocpp.domain.ActionMessage;
import net.solarnetwork.ocpp.domain.ChargePointIdentity;
import ocpp.domain.Action;

/**
 * API for sending messages to Charge Points.
 * 
 * @author matt
 * @version 1.0
 */
public interface ChargePointBroker {

	/**
	 * Get a complete set of Charge Point identifiers that are available, or
	 * otherwise know to this broker.
	 * 
	 * @return the set of available charge point identifiers, never
	 *         {@literal null}
	 */
	Set<ChargePointIdentity> availableChargePointsIds();

	/**
	 * Test if a Charge Point is available, or otherwise known to this broker.
	 * 
	 * @param identity
	 *        the Charge Point ID to query
	 * @return {@literal true} if this broker is aware of the given
	 *         {@code clientId} and should be able to send messages to it via
	 *         {@link #sendMessageToChargePoint(ActionMessage, ActionMessageResultHandler)}
	 */
	boolean isChargePointAvailable(ChargePointIdentity identity);

	/**
	 * Test if an {@link ActionMessage} is supported by this broker.
	 * 
	 * <p>
	 * A message is supported if the message {@link Action} is supported by this
	 * broker and the message {@code clientId} is available.
	 * </p>
	 * 
	 * @param message
	 *        the message to test
	 * @return {@literal true} if the message is supported
	 */
	boolean isMessageSupported(ActionMessage<?> message);

	/**
	 * Send an {@link ActionMessage} to a Charge Point and provide the result to
	 * an {@link ActionMessageResultHandler}.
	 * 
	 * @param <T>
	 *        the message type
	 * @param <R>
	 *        the result type
	 * @param message
	 *        the message to process, never {@literal null}
	 * @param resultHandler
	 *        the handler to provider the results to
	 * @return {@literal true} if the message client ID is known to the broker
	 *         and the message has been or will be sent
	 */
	<T, R> boolean sendMessageToChargePoint(ActionMessage<T> message,
			ActionMessageResultHandler<T, R> resultHandler);

}
