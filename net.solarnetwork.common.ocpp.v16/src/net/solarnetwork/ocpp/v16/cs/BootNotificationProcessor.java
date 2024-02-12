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

package net.solarnetwork.ocpp.v16.cs;

import java.util.Collections;
import java.util.Set;
import net.solarnetwork.ocpp.domain.Action;
import net.solarnetwork.ocpp.domain.ActionMessage;
import net.solarnetwork.ocpp.domain.ChargePoint;
import net.solarnetwork.ocpp.domain.ChargePointInfo;
import net.solarnetwork.ocpp.domain.ErrorCodeException;
import net.solarnetwork.ocpp.service.ActionMessageResultHandler;
import net.solarnetwork.ocpp.service.BaseActionMessageProcessor;
import net.solarnetwork.ocpp.service.cs.ChargePointManager;
import net.solarnetwork.ocpp.v16.ActionErrorCode;
import net.solarnetwork.ocpp.v16.CentralSystemAction;
import net.solarnetwork.ocpp.xml.support.XmlDateUtils;
import ocpp.v16.cs.BootNotificationRequest;
import ocpp.v16.cs.BootNotificationResponse;
import ocpp.v16.cs.RegistrationStatus;

/**
 * Process {@link BootNotificationRequest} action messages.
 * 
 * @author matt
 * @version 1.0
 */
public class BootNotificationProcessor
		extends BaseActionMessageProcessor<BootNotificationRequest, BootNotificationResponse> {

	/** The supported actions of this processor. */
	public static final Set<Action> SUPPORTED_ACTIONS = Collections
			.singleton(CentralSystemAction.BootNotification);

	/** The default {@code heartbeatIntervalSeconds} value. */
	public static final int DEFAULT_HEARTBEAT_INTERVAL_SECONDS = 300;

	private final ChargePointManager chargePointManager;
	private int heartbeatIntervalSeconds = DEFAULT_HEARTBEAT_INTERVAL_SECONDS;

	/**
	 * Constructor.
	 * 
	 * @param chargePointManager
	 *        the {@link ChargePointManager} to notify
	 * @throws IllegalArgumentException
	 *         if {@code chargePointManager} is {@literal null}
	 */
	public BootNotificationProcessor(ChargePointManager chargePointManager) {
		super(BootNotificationRequest.class, BootNotificationResponse.class, SUPPORTED_ACTIONS);
		if ( chargePointManager == null ) {
			throw new IllegalArgumentException("The chargePointManager parameter must not be null.");
		}
		this.chargePointManager = chargePointManager;
	}

	@Override
	public void processActionMessage(ActionMessage<BootNotificationRequest> message,
			ActionMessageResultHandler<BootNotificationRequest, BootNotificationResponse> resultHandler) {
		BootNotificationRequest req = message.getMessage();
		if ( req == null ) {
			ErrorCodeException err = new ErrorCodeException(ActionErrorCode.FormationViolation,
					"Missing BootNotificationRequest message.");
			resultHandler.handleActionMessageResult(message, null, err);
			return;
		}

		ChargePointInfo info = new ChargePointInfo(message.getClientId().getIdentifier());
		info.setChargePointVendor(req.getChargePointVendor());
		info.setChargePointModel(req.getChargePointModel());
		info.setChargePointSerialNumber(req.getChargePointSerialNumber());
		info.setChargeBoxSerialNumber(req.getChargeBoxSerialNumber());
		info.setFirmwareVersion(req.getFirmwareVersion());
		info.setIccid(req.getIccid());
		info.setImsi(req.getImsi());
		info.setMeterType(req.getMeterType());
		info.setMeterSerialNumber(req.getMeterSerialNumber());

		try {
			ChargePoint cp = chargePointManager.registerChargePoint(message.getClientId(), info);

			BootNotificationResponse res = new BootNotificationResponse();
			res.setCurrentTime(XmlDateUtils.newXmlCalendar());
			if ( cp.getRegistrationStatus() != null ) {
				switch (cp.getRegistrationStatus()) {
					case Accepted:
						res.setStatus(RegistrationStatus.ACCEPTED);
						break;

					case Rejected:
						res.setStatus(RegistrationStatus.REJECTED);
						break;

					default:
						res.setStatus(RegistrationStatus.PENDING);
						break;
				}
			} else {
				res.setStatus(RegistrationStatus.PENDING);
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
