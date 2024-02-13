/* ==================================================================
 * ChargePointActionPayloadDecoder.java - 3/02/2020 6:11:54 am
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

import java.io.IOException;
import com.fasterxml.jackson.databind.JsonNode;
import net.solarnetwork.ocpp.domain.Action;

/**
 * A service that can decode the JSON payload of an action message into a domain
 * object.
 * 
 * @author matt
 * @version 1.0
 */
public interface ActionPayloadDecoder {

	/**
	 * Decode the payload of an action message.
	 * 
	 * @param <T>
	 *        the expected type
	 * @param action
	 *        the action
	 * @param forResult
	 *        {@literal true} if the payload is the {@code action} result;
	 *        {@literal false} if it is the {@code action} request
	 * @param payload
	 *        the payload JSON
	 * @return the payload object
	 * @throws IOException
	 *         if there is any error decoding the JSON payload
	 * @throws UnsupportedOperationException
	 *         if {@code action} is not supported
	 */
	<T> T decodeActionPayload(Action action, boolean forResult, JsonNode payload) throws IOException;

}
