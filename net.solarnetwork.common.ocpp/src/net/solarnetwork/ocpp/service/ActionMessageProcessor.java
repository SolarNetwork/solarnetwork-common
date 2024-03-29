/* ==================================================================
 * ActionMessageProcessor.java - 4/02/2020 4:08:35 pm
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
import net.solarnetwork.ocpp.domain.Action;
import net.solarnetwork.ocpp.domain.ActionMessage;

/**
 * API for processing {@link ActionMessage} objects.
 * 
 * @param <T>
 *        the message type
 * @param <R>
 *        the result type
 * @author matt
 * @version 1.0
 */
public interface ActionMessageProcessor<T, R> {

	/**
	 * Get the set of supported actions.
	 * 
	 * @return the set of supported actions; never {@literal null}
	 */
	Set<Action> getSupportedActions();

	/**
	 * Test if a specific message is supported by this processor.
	 * 
	 * @param message
	 *        the message
	 * @return {@literal true} if
	 *         {@link #processActionMessage(ActionMessage, ActionMessageResultHandler)}
	 *         can handle the given message
	 */
	boolean isMessageSupported(ActionMessage<?> message);

	/**
	 * Process an {@link ActionMessage} and provide the result to an
	 * {@link ActionMessageResultHandler}.
	 * 
	 * @param message
	 *        the message to process, never {@literal null}
	 * @param resultHandler
	 *        the handler to provider the results to
	 */
	void processActionMessage(ActionMessage<T> message, ActionMessageResultHandler<T, R> resultHandler);

}
