/* ==================================================================
 * ChargePointManager.java - 6/02/2020 7:38:10 pm
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

package net.solarnetwork.ocpp.service.cs;

import net.solarnetwork.ocpp.domain.ChargePoint;
import net.solarnetwork.ocpp.domain.ChargePointIdentity;
import net.solarnetwork.ocpp.domain.ChargePointInfo;
import net.solarnetwork.ocpp.domain.RegistrationStatus;
import net.solarnetwork.ocpp.domain.StatusNotification;
import net.solarnetwork.service.Identifiable;

/**
 * This API represents the set of functionality required by an OCPP Central
 * System to manage a set of Charge Point clients.
 * 
 * @author matt
 * @version 2.0
 */
public interface ChargePointManager extends Identifiable {

	/**
	 * Register (or re-register) a Charge Point.
	 * 
	 * <p>
	 * This method can be called by a Charge Point that wants to self-register,
	 * for example via a {@literal BootNotification} request, or by an
	 * administration tool to create a new Charge Point entity.
	 * </p>
	 * 
	 * @param identity
	 *        the client ID making the request
	 * @param info
	 *        the details to register
	 * @return the resulting charge point, never {@literal null}
	 */
	ChargePoint registerChargePoint(ChargePointIdentity identity, ChargePointInfo info);

	/**
	 * Test if a Charge Point's registration has been accepted.
	 * 
	 * @param chargePointId
	 *        the Charge Point ID
	 * @return {@literal true} if the Charge Point has previously been
	 *         registered, is not disabled, and has a status of
	 *         {@link RegistrationStatus#Accepted}
	 */
	boolean isChargePointRegistrationAccepted(long chargePointId);

	/**
	 * Update the status of a Charge Point or specific connector.
	 * 
	 * @param identity
	 *        the charge point to update
	 * @param info
	 *        the status update info
	 */
	void updateChargePointStatus(ChargePointIdentity identity, StatusNotification info);

}
