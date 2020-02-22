/* ==================================================================
 * StopTransactionProcessor.java - 14/02/2020 4:06:24 pm
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.solarnetwork.ocpp.domain.ActionMessage;
import net.solarnetwork.ocpp.domain.AuthorizationInfo;
import net.solarnetwork.ocpp.domain.ChargeSession;
import net.solarnetwork.ocpp.domain.ChargeSessionEndInfo;
import net.solarnetwork.ocpp.domain.ChargeSessionEndReason;
import net.solarnetwork.ocpp.domain.SampledValue;
import net.solarnetwork.ocpp.service.ActionMessageResultHandler;
import net.solarnetwork.ocpp.service.AuthorizationException;
import net.solarnetwork.ocpp.service.BaseActionMessageProcessor;
import net.solarnetwork.ocpp.service.cs.ChargeSessionManager;
import ocpp.domain.Action;
import ocpp.domain.ErrorCodeException;
import ocpp.v16.ActionErrorCode;
import ocpp.v16.CentralSystemAction;
import ocpp.v16.cs.IdTagInfo;
import ocpp.v16.cs.MeterValue;
import ocpp.v16.cs.Reason;
import ocpp.v16.cs.StopTransactionRequest;
import ocpp.v16.cs.StopTransactionResponse;
import ocpp.xml.support.XmlDateUtils;

/**
 * Process {@link StopTransactionRequest} action messages.
 * 
 * @author matt
 * @version 1.0
 */
public class StopTransactionProcessor
		extends BaseActionMessageProcessor<StopTransactionRequest, StopTransactionResponse> {

	/** The supported actions of this processor. */
	public static final Set<Action> SUPPORTED_ACTIONS = Collections
			.singleton(CentralSystemAction.StopTransaction);

	private final ChargeSessionManager chargeSessionManager;

	/**
	 * Constructor.
	 * 
	 * @param chargeSessionManager
	 *        the session manager
	 * @throws IllegalArgumentException
	 *         if any parameter is {@literal null}
	 */
	public StopTransactionProcessor(ChargeSessionManager chargeSessionManager) {
		super(StopTransactionRequest.class, StopTransactionResponse.class, SUPPORTED_ACTIONS);
		if ( chargeSessionManager == null ) {
			throw new IllegalArgumentException("The chargeSessionManager parameter must not be null.");
		}
		this.chargeSessionManager = chargeSessionManager;
	}

	@Override
	public void processActionMessage(ActionMessage<StopTransactionRequest> message,
			ActionMessageResultHandler<StopTransactionRequest, StopTransactionResponse> resultHandler) {
		final String chargePointId = message.getClientId();
		final StopTransactionRequest req = message.getMessage();
		if ( req == null || chargePointId == null ) {
			ErrorCodeException err = new ErrorCodeException(ActionErrorCode.FormationViolation,
					"Missing StopTransactionRequest message.");
			resultHandler.handleActionMessageResult(message, null, err);
			return;
		}

		try {
			ChargeSession session = chargeSessionManager.getActiveChargingSession(chargePointId,
					req.getTransactionId());

			// @formatter:off
			ChargeSessionEndInfo info = ChargeSessionEndInfo.builder()
					.withChargePointId(chargePointId)
					.withAuthorizationId(req.getIdTag())
					.withTransactionId(req.getTransactionId())
					.withMeterEnd(req.getMeterStop())
					.withTimestampEnd(XmlDateUtils.timestamp(req.getTimestamp(), Instant::now))
					.withReason(reason(req.getReason()))
					.withTransactionData(sampledValues(session.getId(), req.getTransactionData()))
					.build();
			// @formatter:on

			log.info("Received StopTransaction request: {}", info);

			AuthorizationInfo authInfo = chargeSessionManager.endChargingSession(info);

			IdTagInfo tagInfo = new IdTagInfo();
			tagInfo.setStatus(CentralSystemUtils.statusForStatus(authInfo.getStatus()));
			StopTransactionResponse res = new StopTransactionResponse();
			res.setIdTagInfo(tagInfo);
			resultHandler.handleActionMessageResult(message, res, null);
		} catch ( AuthorizationException e ) {
			IdTagInfo tagInfo = new IdTagInfo();
			tagInfo.setStatus(CentralSystemUtils.statusForStatus(e.getInfo().getStatus()));
			StopTransactionResponse res = new StopTransactionResponse();
			res.setIdTagInfo(tagInfo);
			resultHandler.handleActionMessageResult(message, res, null);
		} catch ( Throwable t ) {
			ErrorCodeException err = new ErrorCodeException(ActionErrorCode.InternalError,
					"Internal error: " + t.getMessage());
			resultHandler.handleActionMessageResult(message, null, err);
		}
	}

	private static ChargeSessionEndReason reason(Reason reason) {
		if ( reason == null ) {
			return ChargeSessionEndReason.Local;
		}
		try {
			return ChargeSessionEndReason.valueOf(reason.value());
		} catch ( IllegalArgumentException e ) {
			return ChargeSessionEndReason.Unknown;
		}
	}

	private List<SampledValue> sampledValues(UUID chargeSessionId, List<MeterValue> transactionData) {
		List<SampledValue> result = null;
		if ( transactionData != null && !transactionData.isEmpty() ) {
			for ( MeterValue v : transactionData ) {
				if ( v.getSampledValue() == null || v.getSampledValue().isEmpty() ) {
					continue;
				}
				Instant ts = XmlDateUtils.timestamp(v.getTimestamp(), Instant::now);
				for ( ocpp.v16.cs.SampledValue sv : v.getSampledValue() ) {
					if ( result == null ) {
						result = new ArrayList<>(16);
					}
					result.add(CentralSystemUtils.sampledValue(chargeSessionId, ts, sv));
				}
			}
		}
		return result;
	}

}
