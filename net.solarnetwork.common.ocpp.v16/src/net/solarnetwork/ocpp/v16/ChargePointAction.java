/* ==================================================================
 * ChargePointAction.java - 3/02/2020 8:04:40 am
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

import net.solarnetwork.ocpp.domain.Action;

/**
 * OCPP 1.6 Charge Point action enumeration.
 * 
 * @author matt
 * @version 1.0
 */
public enum ChargePointAction implements Action {

	/** Cancel reservation. */
	CancelReservation,

	/** Change availability. */
	ChangeAvailability,

	/** Change configuration. */
	ChangeConfiguration,

	/** Clear cache. */
	ClearCache,

	/** Clear charging profile. */
	ClearChargingProfile,

	/** Data transfer. */
	DataTransfer,

	/** Get composite schedule. */
	GetCompositeSchedule,

	/** Get configuration. */
	GetConfiguration,

	/** Get diagnostics. */
	GetDiagnostics,

	/** Get local list version. */
	GetLocalListVersion,

	/** Remote start transaction. */
	RemoteStartTransaction,

	/** Remote stop transaction. */
	RemoteStopTransaction,

	/** Reserve now. */
	ReserveNow,

	/** Reset. */
	Reset,

	/** Send local list. */
	SendLocalList,

	/** Set charging profile. */
	SetChargingProfile,

	/** Trigger message. */
	TriggerMessage,

	/** Unlock connector. */
	UnlockConnector,

	/** Update firmware. */
	UpdateFirmware;

	@Override
	public String getName() {
		return name();
	}

}
