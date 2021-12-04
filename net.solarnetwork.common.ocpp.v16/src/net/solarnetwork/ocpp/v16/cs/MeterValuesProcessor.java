/* ==================================================================
 * MeterValuesProcessor.java - 15/02/2020 8:49:26 am
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
import net.solarnetwork.codec.JsonUtils;
import net.solarnetwork.ocpp.domain.ActionMessage;
import net.solarnetwork.ocpp.domain.ChargePointIdentity;
import net.solarnetwork.ocpp.domain.ChargeSession;
import net.solarnetwork.ocpp.domain.SampledValue;
import net.solarnetwork.ocpp.service.ActionMessageResultHandler;
import net.solarnetwork.ocpp.service.BaseActionMessageProcessor;
import net.solarnetwork.ocpp.service.cs.ChargeSessionManager;
import ocpp.domain.Action;
import ocpp.domain.ErrorCodeException;
import ocpp.v16.ActionErrorCode;
import ocpp.v16.CentralSystemAction;
import ocpp.v16.cs.MeterValue;
import ocpp.v16.cs.MeterValuesRequest;
import ocpp.v16.cs.MeterValuesResponse;
import ocpp.xml.support.XmlDateUtils;

/**
 * Process {@link MeterValuesRequest} action messages.
 * 
 * @author matt
 * @version 1.1
 */
public class MeterValuesProcessor
		extends BaseActionMessageProcessor<MeterValuesRequest, MeterValuesResponse> {

	/** The supported actions of this processor. */
	public static final Set<Action> SUPPORTED_ACTIONS = Collections
			.singleton(CentralSystemAction.MeterValues);

	private final ChargeSessionManager chargeSessionManager;

	/**
	 * Constructor.
	 * 
	 * @param chargeSessionManager
	 *        the session manager
	 * @throws IllegalArgumentException
	 *         if any parameter is {@literal null}
	 */
	public MeterValuesProcessor(ChargeSessionManager chargeSessionManager) {
		super(MeterValuesRequest.class, MeterValuesResponse.class, SUPPORTED_ACTIONS);
		if ( chargeSessionManager == null ) {
			throw new IllegalArgumentException("The chargeSessionManager parameter must not be null.");
		}
		this.chargeSessionManager = chargeSessionManager;
	}

	@Override
	public void processActionMessage(ActionMessage<MeterValuesRequest> message,
			ActionMessageResultHandler<MeterValuesRequest, MeterValuesResponse> resultHandler) {
		final ChargePointIdentity chargePointId = message.getClientId();
		final MeterValuesRequest req = message.getMessage();
		if ( req == null || chargePointId == null ) {
			ErrorCodeException err = new ErrorCodeException(ActionErrorCode.FormationViolation,
					"Missing MeterValuesRequest message.");
			resultHandler.handleActionMessageResult(message, null, err);
			return;
		}

		if ( log.isInfoEnabled() ) {
			log.info("Received MeterValues req: {}", JsonUtils.getJSONString(req, "{}"));
		}

		try {
			ChargeSession session = chargeSessionManager.getActiveChargingSession(chargePointId,
					req.getTransactionId());

			if ( session == null ) {
				ErrorCodeException err = new ErrorCodeException(ActionErrorCode.GenericError,
						"Charge session not found for given IDs.");
				resultHandler.handleActionMessageResult(message, null, err);
				return;
			}

			List<MeterValue> values = req.getMeterValue();
			List<SampledValue> newReadings = new ArrayList<>();
			if ( values != null && !values.isEmpty() ) {
				for ( MeterValue mv : values ) {
					Instant ts = XmlDateUtils.timestamp(mv.getTimestamp(), Instant::now);
					mv.getSampledValue().stream().map(v -> {
						return CentralSystemUtils.sampledValue(session.getId(), ts, v);
					}).forEach(newReadings::add);
				}
			}
			if ( !newReadings.isEmpty() ) {
				log.info("Saving charge readings for session {}: {}", session, newReadings);
				chargeSessionManager.addChargingSessionReadings(newReadings);
			}

			resultHandler.handleActionMessageResult(message, new MeterValuesResponse(), null);
		} catch ( Throwable t ) {
			ErrorCodeException err = new ErrorCodeException(ActionErrorCode.InternalError,
					"Internal error: " + t.getMessage());
			resultHandler.handleActionMessageResult(message, null, err);
		}
	}

}
