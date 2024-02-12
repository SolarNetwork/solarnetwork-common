/* ==================================================================
 * StartTransactionProcessor.java - 14/02/2020 1:46:00 pm
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

import java.time.Instant;
import java.util.Collections;
import java.util.Set;
import net.solarnetwork.ocpp.domain.Action;
import net.solarnetwork.ocpp.domain.ActionMessage;
import net.solarnetwork.ocpp.domain.ChargePointIdentity;
import net.solarnetwork.ocpp.domain.ChargeSession;
import net.solarnetwork.ocpp.domain.ChargeSessionStartInfo;
import net.solarnetwork.ocpp.domain.ErrorCodeException;
import net.solarnetwork.ocpp.service.ActionMessageResultHandler;
import net.solarnetwork.ocpp.service.AuthorizationException;
import net.solarnetwork.ocpp.service.BaseActionMessageProcessor;
import net.solarnetwork.ocpp.service.cs.ChargeSessionManager;
import net.solarnetwork.ocpp.v16.ActionErrorCode;
import net.solarnetwork.ocpp.v16.CentralSystemAction;
import net.solarnetwork.ocpp.xml.support.XmlDateUtils;
import ocpp.v16.cs.AuthorizationStatus;
import ocpp.v16.cs.IdTagInfo;
import ocpp.v16.cs.StartTransactionRequest;
import ocpp.v16.cs.StartTransactionResponse;

/**
 * Process {@link StartTransactionRequest} action messages.
 * 
 * <p>
 * If an authorization exception occurs the resulting
 * {@link StartTransactionResponse} will have its {@code transactionId} set to
 * {@link AuthorizationException#getTransactionId()} if available, otherwise
 * {@literal 0}.
 * </p>
 * 
 * @author matt
 * @version 1.2
 */
public class StartTransactionProcessor
		extends BaseActionMessageProcessor<StartTransactionRequest, StartTransactionResponse> {

	/** The supported actions of this processor. */
	public static final Set<Action> SUPPORTED_ACTIONS = Collections
			.singleton(CentralSystemAction.StartTransaction);

	private final ChargeSessionManager chargeSessionManager;

	/**
	 * Constructor.
	 * 
	 * @param chargeSessionManager
	 *        the session manager
	 * @throws IllegalArgumentException
	 *         if any parameter is {@literal null}
	 */
	public StartTransactionProcessor(ChargeSessionManager chargeSessionManager) {
		super(StartTransactionRequest.class, StartTransactionResponse.class, SUPPORTED_ACTIONS);
		if ( chargeSessionManager == null ) {
			throw new IllegalArgumentException("The chargeSessionManager parameter must not be null.");
		}
		this.chargeSessionManager = chargeSessionManager;
	}

	@Override
	public void processActionMessage(ActionMessage<StartTransactionRequest> message,
			ActionMessageResultHandler<StartTransactionRequest, StartTransactionResponse> resultHandler) {
		final ChargePointIdentity chargePointId = message.getClientId();
		final StartTransactionRequest req = message.getMessage();
		if ( req == null || chargePointId == null ) {
			ErrorCodeException err = new ErrorCodeException(ActionErrorCode.FormationViolation,
					"Missing StartTransactionRequest message.");
			resultHandler.handleActionMessageResult(message, null, err);
			return;
		}

		// @formatter:off
		ChargeSessionStartInfo info = ChargeSessionStartInfo.builder()
				.withChargePointId(chargePointId)
				.withAuthorizationId(req.getIdTag())
				.withConnectorId(req.getConnectorId())
				.withMeterStart(req.getMeterStart())
				.withTimestampStart(XmlDateUtils.timestamp(req.getTimestamp(), Instant::now))
				.withReservationId(req.getReservationId())
				.build();
		// @formatter:on

		log.info("Received StartTransaction request: {}", info);

		try {
			ChargeSession session = chargeSessionManager.startChargingSession(info);
			IdTagInfo tagInfo = new IdTagInfo();
			tagInfo.setStatus(AuthorizationStatus.ACCEPTED);
			StartTransactionResponse res = new StartTransactionResponse();
			res.setIdTagInfo(tagInfo);
			res.setTransactionId(session.getTransactionId());
			resultHandler.handleActionMessageResult(message, res, null);
		} catch ( AuthorizationException e ) {
			IdTagInfo tagInfo = new IdTagInfo();
			tagInfo.setStatus(CentralSystemUtils.statusForStatus(e.getInfo().getStatus()));
			StartTransactionResponse res = new StartTransactionResponse();
			res.setIdTagInfo(tagInfo);
			res.setTransactionId(e.getTransactionId() != null ? e.getTransactionId() : 0);
			resultHandler.handleActionMessageResult(message, res, null);
		} catch ( Throwable t ) {
			ErrorCodeException err = new ErrorCodeException(ActionErrorCode.InternalError,
					"Internal error: " + t.getMessage());
			resultHandler.handleActionMessageResult(message, null, err);
		}
	}

}
