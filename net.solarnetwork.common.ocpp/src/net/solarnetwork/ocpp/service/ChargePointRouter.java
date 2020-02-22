/* ==================================================================
 * ChargePointRouter.java - 11/02/2020 10:45:21 am
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

/**
 * API for a service that can find a broker for a Charge Point based on Charge
 * Point IDs.
 * 
 * @author matt
 * @version 1.0
 */
public interface ChargePointRouter {

	/**
	 * Get a complete set of Charge Point identifiers that are available, or
	 * otherwise know to all available brokers.
	 * 
	 * @return the set of available charge point identifiers, never
	 *         {@literal null}
	 */
	Set<String> availableChargePointsIds();

	/**
	 * Get a {@link ChargePointBroker} for a specific Charge Point ID, if
	 * available.
	 * 
	 * @param clientId
	 *        the ID of the Charge Point
	 * @return the broker, or {@literal null} if not available
	 */
	ChargePointBroker brokerForChargePoint(String clientId);

}
