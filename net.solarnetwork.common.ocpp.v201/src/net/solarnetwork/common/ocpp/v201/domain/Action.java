/* ==================================================================
 * Action.java - 9/02/2024 2:21:42 pm
 *
 * Copyright 2024 SolarNetwork.net Dev Team
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

package net.solarnetwork.common.ocpp.v201.domain;

/**
 * OCPP 2 action enumeration.
 *
 * @author matt
 * @version 1.0
 */
public enum Action implements net.solarnetwork.ocpp.domain.Action {

	/** Authorize a charge session. */
	Authorize,

	/** Notify that a charger has started. */
	BootNotification,

	/** Cancel reservation. */
	CancelReservation,

	/** Provide a signed certificate. */
	CertificateSigned,

	/** Change availability. */
	ChangeAvailability,

	/** Clear cache. */
	ClearCache,

	/** Clear charging profile. */
	ClearChargingProfile,

	/** Clear display message. */
	ClearDisplayMessage,

	/** Clear variable monitoring. */
	ClearVariableMonitoring,

	/** Clear charging limit. */
	ClearedChargingLimit,

	/** Cost updated. */
	CostUpdated,

	/** Csutomer information. */
	CustomerInformation,

	/** Data transfer. */
	DataTransfer,

	/** Delete certificate. */
	DeleteCertificate,

	/** Firmware status notification. */
	FirmwareStatusNotification,

	/** Get 15118 EV certificate. */
	Get15118EVCertificate,

	/** Get base report. */
	GetBaseReport,

	/** Get certificate status. */
	GetCertificateStatus,

	/** Get charging profiles. */
	GetChargingProfiles,

	/** Get composite schedule. */
	GetCompositeSchedule,

	/** Get display messages. */
	GetDisplayMessages,

	/** Get installed certificate IDs. */
	GetInstalledCertificateIds,

	/** Get local list version. */
	GetLocalListVersion,

	/** Get log. */
	GetLog,

	/** Get monitoring report. */
	GetMonitoringReport,

	/** Get report. */
	GetReport,

	/** Get transaction status. */
	GetTransactionStatus,

	/** Get variables. */
	GetVariables,

	/** Heartbeat. */
	Heartbeat,

	/** Install certificate. */
	InstallCertificate,

	/** Log status notification. */
	LogStatusNotification,

	/** Meter values. */
	MeterValues,

	/** Notify charging limit. */
	NotifyChargingLimit,

	/** Notify customer information. */
	NotifyCustomerInformation,

	/** Notify display message. */
	NotifyDisplayMessages,

	/** Notify EV charging needs. */
	NotifyEVChargingNeeds,

	/** Notify EV charging schedule. */
	NotifyEVChargingSchedule,

	/** Notify event. */
	NotifyEvent,

	/** Notify monitoring report. */
	NotifyMonitoringReport,

	/** Notify report. */
	NotifyReport,

	/** Publish firmware. */
	PublishFirmware,

	/** Publish firmware status notification. */
	PublishFirmwareStatusNotification,

	/** Report charging profiles. */
	ReportChargingProfiles,

	/** Request start transaction. */
	RequestStartTransaction,

	/** Request stop transaction. */
	RequestStopTransaction,

	/** Reservation status update. */
	ReservationStatusUpdate,

	/** Reserve now. */
	ReserveNow,

	/** Reset. */
	Reset,

	/** Security event notification. */
	SecurityEventNotification,

	/** Send local list. */
	SendLocalList,

	/** Set charging profile. */
	SetChargingProfile,

	/** Set display message. */
	SetDisplayMessage,

	/** Set monioring base. */
	SetMonitoringBase,

	/** Set monitoring level. */
	SetMonitoringLevel,

	/** Set network profile. */
	SetNetworkProfile,

	/** Set variable monitoring. */
	SetVariableMonitoring,

	/** Set variables. */
	SetVariables,

	/** Sign certificate. */
	SignCertificate,

	/** Status notification. */
	StatusNotification,

	/** Transaction event. */
	TransactionEvent,

	/** Trigger message. */
	TriggerMessage,

	/** Unlock connector. */
	UnlockConnector,

	/** Unpublish firmware. */
	UnpublishFirmware,

	/** Update firmware. */
	UpdateFirmware,

	;

	@Override
	public String getName() {
		return name();
	}

}
