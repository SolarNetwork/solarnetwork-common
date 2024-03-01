/* ==================================================================
 * BootNotificationProcessor.java - 6/02/2020 5:08:50 pm
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

package net.solarnetwork.ocpp.v201.service;

import static net.solarnetwork.util.ObjectUtils.requireNonNullArgument;
import java.time.Clock;
import java.util.Collections;
import java.util.Set;
import net.solarnetwork.ocpp.domain.ActionMessage;
import net.solarnetwork.ocpp.domain.ChargePoint;
import net.solarnetwork.ocpp.domain.ChargePointInfo;
import net.solarnetwork.ocpp.domain.ErrorCodeException;
import net.solarnetwork.ocpp.service.ActionMessageResultHandler;
import net.solarnetwork.ocpp.service.BaseActionMessageProcessor;
import net.solarnetwork.ocpp.service.cs.ChargePointManager;
import net.solarnetwork.ocpp.v201.domain.Action;
import net.solarnetwork.ocpp.v201.domain.ActionErrorCode;
import ocpp.v201.BootNotificationRequest;
import ocpp.v201.BootNotificationResponse;
import ocpp.v201.ChargingStation;
import ocpp.v201.Modem;
import ocpp.v201.RegistrationStatusEnum;

/**
 * Process {@link BootNotificationRequest} action messages.
 * 
 * @author matt
 * @version 1.0
 */
public class BootNotificationProcessor
		extends BaseActionMessageProcessor<BootNotificationRequest, BootNotificationResponse> {

	/** The supported actions of this processor. */
	public static final Set<net.solarnetwork.ocpp.domain.Action> SUPPORTED_ACTIONS = Collections
			.singleton(Action.BootNotification);

	/** The default {@code heartbeatIntervalSeconds} value. */
	public static final int DEFAULT_HEARTBEAT_INTERVAL_SECONDS = 300;

	private final Clock clock;
	private final ChargePointManager chargePointManager;
	private int heartbeatIntervalSeconds = DEFAULT_HEARTBEAT_INTERVAL_SECONDS;

	/**
	 * Constructor.
	 * 
	 * @param clock
	 *        the clock to use
	 * @param chargePointManager
	 *        the {@link ChargePointManager} to notify
	 * @throws IllegalArgumentException
	 *         if any argument is {@literal null}
	 */
	public BootNotificationProcessor(Clock clock, ChargePointManager chargePointManager) {
		super(BootNotificationRequest.class, BootNotificationResponse.class, SUPPORTED_ACTIONS);
		if ( chargePointManager == null ) {
			throw new IllegalArgumentException("The chargePointManager parameter must not be null.");
		}
		this.clock = requireNonNullArgument(clock, "clock");
		this.chargePointManager = requireNonNullArgument(chargePointManager, "chargePointManager");
	}

	@Override
	public void processActionMessage(ActionMessage<BootNotificationRequest> message,
			ActionMessageResultHandler<BootNotificationRequest, BootNotificationResponse> resultHandler) {
		BootNotificationRequest req = message.getMessage();
		if ( req == null ) {
			ErrorCodeException err = new ErrorCodeException(ActionErrorCode.FormatViolation,
					"Missing BootNotificationRequest message.");
			resultHandler.handleActionMessageResult(message, null, err);
			return;
		}

		ChargePointInfo info = new ChargePointInfo(message.getClientId().getIdentifier());
		if ( req.getChargingStation() != null ) {
			ChargingStation station = req.getChargingStation();
			info.setChargePointVendor(station.getVendorName());
			info.setChargePointModel(station.getModel());
			info.setChargePointSerialNumber(station.getSerialNumber());
			info.setFirmwareVersion(station.getFirmwareVersion());
			if ( station.getModem() != null ) {
				Modem modem = station.getModem();
				info.setIccid(modem.getIccid());
				info.setImsi(modem.getImsi());
			}
		}

		try {
			ChargePoint cp = chargePointManager.registerChargePoint(message.getClientId(), info);

			BootNotificationResponse res = new BootNotificationResponse();
			res.setCurrentTime(clock.instant());
			if ( cp.getRegistrationStatus() != null ) {
				switch (cp.getRegistrationStatus()) {
					case Accepted:
						res.setStatus(RegistrationStatusEnum.ACCEPTED);
						break;

					case Rejected:
						res.setStatus(RegistrationStatusEnum.REJECTED);
						break;

					default:
						res.setStatus(RegistrationStatusEnum.PENDING);
						break;
				}
			} else {
				res.setStatus(RegistrationStatusEnum.PENDING);
			}
			res.setInterval(getHeartbeatIntervalSeconds());
			resultHandler.handleActionMessageResult(message, res, null);
		} catch ( Throwable t ) {
			ErrorCodeException err = new ErrorCodeException(ActionErrorCode.InternalError,
					"Internal error: " + t.getMessage());
			resultHandler.handleActionMessageResult(message, null, err);
		}
	}

	/**
	 * Get the heartbeat interval.
	 * 
	 * @return the interval, in seconds
	 */
	public int getHeartbeatIntervalSeconds() {
		return heartbeatIntervalSeconds;
	}

	/**
	 * Set the heartbeat interval.
	 * 
	 * @param heartbeatIntervalSeconds
	 *        the interval to set, in seconds
	 */
	public void setHeartbeatIntervalSeconds(int heartbeatIntervalSeconds) {
		this.heartbeatIntervalSeconds = heartbeatIntervalSeconds;
	}

}
