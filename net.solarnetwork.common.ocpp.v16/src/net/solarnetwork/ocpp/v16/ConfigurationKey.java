/* ==================================================================
 * ConfigurationKey.java - 31/01/2020 9:59:00 am
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

import static net.solarnetwork.ocpp.domain.ConfigurationType.Boolean;
import static net.solarnetwork.ocpp.domain.ConfigurationType.CSL;
import static net.solarnetwork.ocpp.domain.ConfigurationType.Integer;
import static net.solarnetwork.ocpp.v16.FeatureProfile.LocalAuthListManagement;
import static net.solarnetwork.ocpp.v16.FeatureProfile.Reservation;
import static net.solarnetwork.ocpp.v16.FeatureProfile.SmartCharging;
import net.solarnetwork.ocpp.domain.ConfigurationType;

/**
 * OCPP v1.6 configuration key enumeration.
 * 
 * @author matt
 * @version 1.0
 */
public enum ConfigurationKey implements net.solarnetwork.ocpp.domain.ConfigurationKey {

	/**
	 * If this key exists, the Charge Point supports <em>Unknown Offline
	 * Authorization</em>. If this key reports a value of {@literal true},
	 * <em>Unknown Offline Authorization</em> is enabled.
	 */
	AllowOfflineTxForUnknownId(Boolean),

	/**
	 * If this key exists, the Charge Point supports an <em>Authorization
	 * Cache</em>. If this key reports a value of {@literal true}, the
	 * <em>Authorization Cache</em> is enabled.
	 */
	AuthorizationCacheEnabled(Boolean),

	/**
	 * Whether a remote request to start a transaction in the form of a
	 * {@literal RemoteStartTransaction} message should be authorized beforehand
	 * like a local action to start a transaction.
	 */
	AuthorizeRemoteTxRequests(Boolean),

	/** Number of times to blink Charge Point lighting when signaling. */
	BlinkRepeat(Integer),

	/**
	 * Size (in seconds) of the clock-aligned data interval. This is the size
	 * (in seconds) of the set of evenly spaced aggregation intervals per day,
	 * starting at 00:00:00 (midnight).
	 */
	ClockAlignedDataInterval(Integer),

	/**
	 * Interval (in seconds) from beginning of status <em>Preparing</em> until
	 * incipient transaction is automatically canceled, due to failure of EV
	 * driver to (correctly) insert the charging cable connector(s) into the
	 * appropriate socket(s).
	 */
	ConnectionTimeOut(Integer),

	/**
	 * The phase rotation per connector in respect to the connector’s electrical
	 * meter (or if absent, the grid connection). Values are reported in CSL
	 * using the pattern {@code connectorId:phaseRotation}. If known, the Charge
	 * Point MAY also report the phase rotation between the grid connection and
	 * the main energy meter by using index number {@literal 0}. For example:
	 * {@literal 0.RST, 1.RST, 2.RTS}.
	 * 
	 * @see net.solarnetwork.ocpp.domain.PhaseRotation
	 */
	ConnectorPhaseRotation(CSL),

	/**
	 * Maximum number of items in a {@link #ConnectorPhaseRotation}
	 * Configuration Key.
	 */
	ConnectorPhaseRotationMaxLength(Integer),

	/**
	 * Maximum number of requested configuration keys in a
	 * {@literal GetConfiguration} message.
	 */
	GetConfigurationMaxKeys(Integer),

	/**
	 * Interval (in seconds) of inactivity (no OCPP exchanges) with central
	 * system after which the Charge Point should send a {@literal Heartbeat}
	 * message.
	 */
	HeartbeatInterval(Integer),

	/**
	 * Percentage as integer from 0-100 of maximum intensity at which to
	 * illuminate Charge Point lighting.
	 */
	LightIntensity(Integer),

	/**
	 * Whether the Charge Point, when offline, will start a transaction for
	 * locally-authorized identifiers.
	 */
	LocalAuthorizeOffline(Boolean),

	/**
	 * Whether the Charge Point, when online, will start a transaction for
	 * locally-authorized identifiers without waiting for or requesting an
	 * Authorize.conf from the Central System.
	 */
	LocalPreAuthorize(Boolean),

	/**
	 * Maximum energy in Wh delivered when an identifier is invalidated by the
	 * Central System after start of a transaction.
	 */
	MaxEnergyOnInvalidId(Integer),

	/**
	 * Clock-aligned measurand(s) to be included in a {@literal MeterValues}
	 * message, every {@link #ClockAlignedDataInterval} seconds.
	 */
	MeterValuesAlignedData(CSL),

	/**
	 * Maximum number of items in a {@link #MeterValuesAlignedData}
	 * Configuration Key.
	 */
	MeterValuesAlignedDataMaxLength(Integer),

	/**
	 * Sampled {@link ocpp.v16.cs.Measurand} values to be included in a
	 * {@literal MeterValues} message, every {@link #MeterValueSampleInterval}
	 * seconds. Where applicable, the {@link ocpp.v16.cs.Measurand} is combined
	 * with the optional phase, for instance: {@literal Voltage.L1}.
	 */
	MeterValuesSampledData(CSL),

	/**
	 * Maximum number of items in a {@link #MeterValuesSampledData}
	 * Configuration Key.
	 */
	MeterValuesSampledDataMaxLength(Integer),

	/**
	 * Interval (in seconds) between sampling of metering (or other) data,
	 * intended to be transmitted by {@literal MeterValues} messages. For
	 * charging session data (ConnectorId *gt; 0), samples are acquired and
	 * transmitted periodically at this interval from the start of the charging
	 * transaction. A value of {@literal 0}, by convention, is to be interpreted
	 * to mean that no sampled data should be transmitted.
	 */
	MeterValueSampleInterval(Integer),

	/**
	 * The minimum duration (in seconds) that a Charge Point or Connector status
	 * is stable before a {@literal StatusNotification} message is sent to the
	 * Central System.
	 */
	MinimumStatusDuration(Integer),

	/** The number of physical charging connectors of this Charge Point. */
	NumberOfConnectors(Integer),

	/** Number of times to retry an unsuccessful reset of the Charge Point. */
	ResetRetries(Integer),

	/**
	 * When set to true, the Charge Point SHALL administratively stop the
	 * transaction when the cable is unplugged from the EV.
	 */
	StopTransactionOnEVSideDisconnect(Boolean),

	/**
	 * Whether the Charge Point will stop an ongoing transaction when it
	 * receives a non-{@literal Accepted} authorization status in a
	 * {@literal StartTransaction} message response for this transaction.
	 */
	StopTransactionOnInvalidId(Boolean),

	/**
	 * Clock-aligned periodic {@link ocpp.v16.cs.Measurand} values to be
	 * included in the {@literal TransactionData} element of
	 * {@literal StopTransaction} or {@literal MeterValues} message for every
	 * {@link #ClockAlignedDataInterval} of the Transaction.
	 */
	StopTxnAlignedData(CSL),

	/**
	 * Maximum number of items in a {@link #StopTxnAlignedData} Configuration
	 * Key.
	 */
	StopTxnAlignedDataMaxLength(Integer),

	/**
	 * Sampled {@link ocpp.v16.cs.Measurand} values to be included in the
	 * {@literal TransactionData} element of {@literal StopTransaction}
	 * messages, every {@link #MeterValueSampleInterval} seconds from the start
	 * of the charging session.
	 */
	StopTxnSampledData(CSL),

	/**
	 * Maximum number of items in a {@link #StopTxnSampledData} Configuration
	 * Key.
	 */
	StopTxnSampledDataMaxLength(Integer),

	/**
	 * A list of supported {@link net.solarnetwork.ocpp.v16.FeatureProfile}
	 * values.
	 */
	SupportedFeatureProfiles(CSL),

	/**
	 * Maximum number of items in a {@link #SupportedFeatureProfiles}
	 * Configuration Key.
	 */
	SupportedFeatureProfilesMaxLength(Integer),

	/**
	 * How often the Charge Point should try to submit a transaction-related
	 * message when the Central System fails to process it.
	 */
	TransactionMessageAttempts(Integer),

	/**
	 * How long (in seconds) the Charge Point should wait before resubmitting a
	 * transaction-related message that the Central System failed to process.
	 */
	TransactionMessageRetryInterval(Integer),

	/**
	 * When set to {@literal true}, the Charge Point SHALL unlock the cable on
	 * Charge Point side when the cable is unplugged at the EV.
	 */
	UnlockConnectorOnEVSideDisconnect(Boolean),

	/**
	 * For OCPP-J, a value of {@literal 0} disables client side websocket Ping /
	 * Pong feature. In this case there is either no ping / pong or the server
	 * initiates the ping and client responds with Pong. Positive values are
	 * interpreted as number of seconds between pings.
	 */
	WebSocketPingInterval(Integer),

	/*-------------------------------------------------------------------------
	 * Local Auth List Management Profile
	 */

	/** Whether the Local Authorization List is enabled. */
	LocalAuthListEnabled(LocalAuthListManagement, Boolean),

	/**
	 * Maximum number of identifications that can be stored in the Local
	 * Authorization List.
	 */
	LocalAuthListMaxLength(LocalAuthListManagement, Integer),

	/**
	 * Maximum number of identifications that can be send in a single
	 * {@literal SendLocalList} message.
	 */
	SendLocalListMaxLength(LocalAuthListManagement, Integer),

	/*-------------------------------------------------------------------------
	 * Reservation Profile
	 */

	/**
	 * If this configuration key is present and set to {@literal true}: Charge
	 * Point support reservations on connector 0.
	 */
	ReserveConnectorZeroSupported(Reservation, Boolean),

	/*-------------------------------------------------------------------------
	 * Smart Charging Profile
	 */

	/**
	 * Max StackLevel of a ChargingProfile. The number defined also indicates
	 * the max allowed number of installed charging schedules per Charging
	 * Profile Purposes.
	 */
	ChargeProfileMaxStackLevel(SmartCharging, Integer),

	/**
	 * A list of supported quantities for use in a ChargingSchedule. Allowed
	 * values: 'Current' and 'Power'.
	 */
	ChargingScheduleAllowedChargingRateUnit(SmartCharging, CSL),

	/** Maximum number of periods that may be defined per ChargingSchedule. */
	ChargingScheduleMaxPeriods(SmartCharging, Integer),

	/**
	 * If defined and true, this Charge Point support switching from 3 to 1
	 * phase during a Transaction.
	 */
	ConnectorSwitch3to1PhaseSupported(SmartCharging, Boolean),

	/** Maximum number of Charging profiles installed at a time. */
	MaxChargingProfilesInstalled(SmartCharging, Integer);

	private final FeatureProfile profile;
	private final ConfigurationType type;

	private ConfigurationKey(ConfigurationType type) {
		this(FeatureProfile.Core, type);
	}

	private ConfigurationKey(FeatureProfile profile, ConfigurationType type) {
		this.profile = profile;
		this.type = type;
	}

	/**
	 * Get the feature profile associated with this key.
	 * 
	 * @return the feature profile
	 */
	public FeatureProfile getProfile() {
		return profile;
	}

	@Override
	public ConfigurationType getType() {
		return type;
	}

	@Override
	public String getName() {
		return name();
	}
}
