/* ==================================================================
 * ChargeSessionManager.java - 14/02/2020 1:44:37 pm
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

import java.util.Collection;
import java.util.UUID;
import net.solarnetwork.ocpp.domain.AuthorizationInfo;
import net.solarnetwork.ocpp.domain.ChargePointIdentity;
import net.solarnetwork.ocpp.domain.ChargeSession;
import net.solarnetwork.ocpp.domain.ChargeSessionEndInfo;
import net.solarnetwork.ocpp.domain.ChargeSessionStartInfo;
import net.solarnetwork.ocpp.domain.SampledValue;
import net.solarnetwork.ocpp.service.AuthorizationException;
import net.solarnetwork.service.Identifiable;

/**
 * This API represents the set of functionality required by an OCPP Central
 * System to manage charging sessions (OCPP transactions) for Charge Point
 * clients.
 * 
 * @author matt
 * @version 3.0
 */
public interface ChargeSessionManager extends Identifiable {

	/**
	 * Start a charging session.
	 * 
	 * <p>
	 * If no {@code transactionId} is provided, this method must generate a new
	 * transaction ID value that is an unsigned 16-bit integer value in the
	 * range 1-65535, returned as a base-10 string.
	 * </p>
	 * 
	 * @param info
	 *        the start charging session info
	 * @return the new charge session
	 * @throws AuthorizationException
	 *         if any authorization error occurs
	 */
	ChargeSession startChargingSession(ChargeSessionStartInfo info) throws AuthorizationException;

	/**
	 * Get an active charging session for a transaction ID.
	 * 
	 * <p>
	 * An <em>active</em> charging session is one that has not ended yet.
	 * </p>
	 * 
	 * @param chargePointId
	 *        the charge point ID
	 * @param transactionId
	 *        the transaction ID
	 * @return the charge session
	 * @throws AuthorizationException
	 *         if any no active charge session is available for the given
	 *         criteria
	 * @since 3.0
	 */
	ChargeSession getActiveChargingSession(ChargePointIdentity chargePointId, String transactionId)
			throws AuthorizationException;

	/**
	 * Get active charging sessions, optionally limited to a specific charge
	 * point ID.
	 * 
	 * <p>
	 * An <em>active</em> charging session is one that has not ended yet.
	 * </p>
	 * 
	 * @param chargePointId
	 *        the charge point identifier to get sessions for, or
	 *        {@literal null} for all sessions for all charge points
	 * @return the active sessions, never {@literal null}
	 */
	Collection<ChargeSession> getActiveChargingSessions(ChargePointIdentity chargePointId);

	/**
	 * Get all available charge session readings.
	 * 
	 * @param sessionId
	 *        the charge session ID
	 * @return the readings, never {@literal null}
	 */
	Collection<SampledValue> getChargingSessionReadings(UUID sessionId);

	/**
	 * Add charge session readings.
	 * 
	 * @param chargePointId
	 *        the charge point identifier to get sessions for, or
	 *        {@literal null} for all sessions for all charge points
	 * @param evseId
	 *        the EVSE ID, or {@literal null} to use the active charge session's
	 *        information
	 * @param connectorId
	 *        the connector ID, or {@literal null} to use the active charge
	 *        session's information
	 * @param readings
	 *        the readings to add
	 * @since 2.3
	 */
	void addChargingSessionReadings(ChargePointIdentity chargePointId, Integer evseId,
			Integer connectorId, Iterable<SampledValue> readings);

	/**
	 * End a charging session.
	 * 
	 * @param info
	 *        the end charging session info
	 * @return info if needed, otherwise {@literal null}
	 */
	AuthorizationInfo endChargingSession(ChargeSessionEndInfo info);

}
