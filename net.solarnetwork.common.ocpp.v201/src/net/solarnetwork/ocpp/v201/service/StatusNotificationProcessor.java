/* ==================================================================
 * StatusNotificationProcessor.java - 17/02/2024 6:25:13 pm
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

package net.solarnetwork.ocpp.v201.service;

import java.util.Collections;
import java.util.Set;
import net.solarnetwork.ocpp.domain.ActionMessage;
import net.solarnetwork.ocpp.domain.ChargePointIdentity;
import net.solarnetwork.ocpp.domain.ChargePointStatus;
import net.solarnetwork.ocpp.domain.ErrorCodeException;
import net.solarnetwork.ocpp.domain.StatusNotification;
import net.solarnetwork.ocpp.service.ActionMessageResultHandler;
import net.solarnetwork.ocpp.service.BaseActionMessageProcessor;
import net.solarnetwork.ocpp.service.cs.ChargePointManager;
import net.solarnetwork.ocpp.v201.domain.Action;
import net.solarnetwork.ocpp.v201.domain.ActionErrorCode;
import net.solarnetwork.security.SecurityException;
import ocpp.v201.StatusNotificationRequest;
import ocpp.v201.StatusNotificationResponse;

/**
 * Process {@link StatusNotificationRequest} action messages.
 *
 * @author matt
 * @version 1.0
 * @since 1.1
 */
public class StatusNotificationProcessor
		extends BaseActionMessageProcessor<StatusNotificationRequest, StatusNotificationResponse> {

	/** The supported actions of this processor. */
	public static final Set<net.solarnetwork.ocpp.domain.Action> SUPPORTED_ACTIONS = Collections
			.singleton(Action.StatusNotification);

	private final ChargePointManager chargePointManager;

	/**
	 * Constructor.
	 *
	 * @param chargePointManager
	 *        the charge point manager
	 * @throws IllegalArgumentException
	 *         if {@code chargePointConnectorDao} is {@literal null}
	 */
	public StatusNotificationProcessor(ChargePointManager chargePointManager) {
		super(StatusNotificationRequest.class, StatusNotificationResponse.class, SUPPORTED_ACTIONS);
		if ( chargePointManager == null ) {
			throw new IllegalArgumentException("The chargePointManager parameter must not be null.");
		}
		this.chargePointManager = chargePointManager;
	}

	@Override
	public void processActionMessage(ActionMessage<StatusNotificationRequest> message,
			ActionMessageResultHandler<StatusNotificationRequest, StatusNotificationResponse> resultHandler) {
		final ChargePointIdentity identity = message.getClientId();
		final StatusNotificationRequest req = message.getMessage();
		if ( req == null || identity == null ) {
			ErrorCodeException err = new ErrorCodeException(ActionErrorCode.FormatViolation,
					"Missing StatusNotificationRequest message.");
			resultHandler.handleActionMessageResult(message, null, err);
			return;
		}

		// @formatter:off
		StatusNotification info = StatusNotification.builder()
				.withEvseId(req.getEvseId())
				.withConnectorId(req.getConnectorId())
				.withStatus(statusValue(req))
				.withTimestamp(req.getTimestamp())
				.build();
		// @formatter:on

		try {
			chargePointManager.updateChargePointStatus(identity, info);
			resultHandler.handleActionMessageResult(message, new StatusNotificationResponse(), null);
		} catch ( SecurityException e ) {
			ErrorCodeException err = new ErrorCodeException(ActionErrorCode.SecurityError,
					e.getMessage());
			resultHandler.handleActionMessageResult(message, null, err);
		} catch ( Throwable t ) {
			ErrorCodeException err = new ErrorCodeException(ActionErrorCode.InternalError,
					"Internal error: " + t.getMessage());
			resultHandler.handleActionMessageResult(message, null, err);
		}
	}

	private ChargePointStatus statusValue(StatusNotificationRequest req) {
		if ( req != null && req.getConnectorStatus() != null ) {
			try {
				return ChargePointStatus.valueOf(req.getConnectorStatus().value());
			} catch ( IllegalArgumentException e ) {
				// ignore
			}
		}
		return ChargePointStatus.Unknown;
	}

}
