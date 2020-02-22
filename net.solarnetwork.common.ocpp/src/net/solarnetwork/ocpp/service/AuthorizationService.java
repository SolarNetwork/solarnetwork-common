/* ==================================================================
 * AuthorizationService.java - 6/02/2020 7:16:07 pm
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

import net.solarnetwork.domain.Identifiable;
import net.solarnetwork.ocpp.domain.AuthorizationInfo;

/**
 * API for authorizing an ID tag.
 * 
 * <p>
 * This API can be used by a Charge Point to authorize an identifier value, for
 * example against a local authorization list or by making a call to a remote
 * service such as an OCPP Central System. This API can also be used by a
 * Central System to authorize a request from a Charge Point.
 * </p>
 * 
 * @author matt
 * @version 1.0
 */
public interface AuthorizationService extends Identifiable {

	/**
	 * Request authorization of a specific charge point ID tag value.
	 * 
	 * @param clientId
	 *        the ID of the client making the request, such as a Charge Point ID
	 * @param identifier
	 *        the identifier to authorize, e.g. RFID value
	 * @return the authorization result, never {@literal null}
	 */
	AuthorizationInfo authorize(String clientId, String identifier);

}
