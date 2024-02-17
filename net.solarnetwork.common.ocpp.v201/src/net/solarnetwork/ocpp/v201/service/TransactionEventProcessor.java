/* ==================================================================
 * TransactionEventProcessor.java - 16/02/2024 4:57:22 pm
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

import static net.solarnetwork.ocpp.domain.UnitOfMeasure.kWh;
import static net.solarnetwork.util.ObjectUtils.requireNonNullArgument;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.solarnetwork.ocpp.domain.ActionMessage;
import net.solarnetwork.ocpp.domain.AuthorizationInfo;
import net.solarnetwork.ocpp.domain.ChargePointIdentity;
import net.solarnetwork.ocpp.domain.ChargeSession;
import net.solarnetwork.ocpp.domain.ChargeSessionEndInfo;
import net.solarnetwork.ocpp.domain.ChargeSessionStartInfo;
import net.solarnetwork.ocpp.domain.ErrorCodeException;
import net.solarnetwork.ocpp.service.ActionMessageResultHandler;
import net.solarnetwork.ocpp.service.AuthorizationException;
import net.solarnetwork.ocpp.service.BaseActionMessageProcessor;
import net.solarnetwork.ocpp.service.cs.ChargeSessionManager;
import net.solarnetwork.ocpp.v201.domain.Action;
import net.solarnetwork.ocpp.v201.domain.ActionErrorCode;
import net.solarnetwork.ocpp.v201.util.OcppUtils;
import ocpp.v201.AuthorizationStatusEnum;
import ocpp.v201.EVSE;
import ocpp.v201.IdToken;
import ocpp.v201.IdTokenInfo;
import ocpp.v201.LocationEnum;
import ocpp.v201.MeasurandEnum;
import ocpp.v201.MeterValue;
import ocpp.v201.ReadingContextEnum;
import ocpp.v201.SampledValue;
import ocpp.v201.Transaction;
import ocpp.v201.TransactionEventEnum;
import ocpp.v201.TransactionEventRequest;
import ocpp.v201.TransactionEventResponse;
import ocpp.v201.UnitOfMeasure;

/**
 * Process {@link TransactionEventRequest} action messages.
 * 
 * @author matt
 * @version 1.0
 */
public class TransactionEventProcessor

		extends BaseActionMessageProcessor<TransactionEventRequest, TransactionEventResponse> {

	/** The supported actions of this processor. */
	public static final Set<net.solarnetwork.ocpp.domain.Action> SUPPORTED_ACTIONS = Collections
			.singleton(Action.TransactionEvent);

	private final ChargeSessionManager chargeSessionManager;

	/**
	 * Constructor.
	 * 
	 * @param chargeSessionManager
	 *        the session manager
	 * @throws IllegalArgumentException
	 *         if any parameter is {@literal null}
	 */
	public TransactionEventProcessor(ChargeSessionManager chargeSessionManager) {
		super(TransactionEventRequest.class, TransactionEventResponse.class, SUPPORTED_ACTIONS);
		this.chargeSessionManager = requireNonNullArgument(chargeSessionManager, "chargeSessionManager");
	}

	@Override
	public void processActionMessage(final ActionMessage<TransactionEventRequest> message,
			final ActionMessageResultHandler<TransactionEventRequest, TransactionEventResponse> resultHandler) {
		final ChargePointIdentity chargePointId = message.getClientId();
		final TransactionEventRequest req = message.getMessage();
		if ( req == null || chargePointId == null ) {
			ErrorCodeException err = new ErrorCodeException(ActionErrorCode.FormatViolation,
					"Missing StartTransactionRequest message.");
			resultHandler.handleActionMessageResult(message, null, err);
			return;
		}

		final TransactionEventEnum eventType = req.getEventType();
		try {

			switch (eventType) {
				case STARTED:
					processStartTransaction(message, resultHandler, chargePointId, req);
					break;

				case UPDATED:
					processUpdateTransaction(message, resultHandler, chargePointId, req);
					break;

				case ENDED:
					processEndTransaction(message, resultHandler, chargePointId, req);
					break;
			}
		} catch ( AuthorizationException e ) {
			IdTokenInfo tagInfo = new IdTokenInfo(OcppUtils.statusForStatus(e.getInfo().getStatus()));
			TransactionEventResponse res = new TransactionEventResponse();
			res.setIdTokenInfo(tagInfo);
			resultHandler.handleActionMessageResult(message, res, null);
		} catch ( Throwable t ) {
			ErrorCodeException err = new ErrorCodeException(ActionErrorCode.InternalError,
					"Internal error: " + t.getMessage());
			resultHandler.handleActionMessageResult(message, null, err);
		}
	}

	/**
	 * Process the start transaction event.
	 * 
	 * @param message
	 *        the message to process, never {@literal null}
	 * @param resultHandler
	 *        the handler to provider the results to
	 * @param chargePointId
	 *        the charge point ID
	 * @param request
	 *        the request
	 */
	protected void processStartTransaction(final ActionMessage<TransactionEventRequest> message,
			final ActionMessageResultHandler<TransactionEventRequest, TransactionEventResponse> resultHandler,
			final ChargePointIdentity chargePointId, final TransactionEventRequest request) {
		final IdToken idToken = request.getIdToken();

		// try to extract a "meter start" value like provided in OCPP 1.6, by looking for
		// an ENERGY_ACTIVE_IMPORT_REGISTER sample value
		final long meterStartValue = findEnergyReadingSampledValue(request.getMeterValue(),
				ReadingContextEnum.TRANSACTION_BEGIN);

		// @formatter:off
		final ChargeSessionStartInfo.Builder startInfoDetails = ChargeSessionStartInfo.builder()
				.withChargePointId(chargePointId)
				.withAuthorizationId(idToken.getIdToken())
				.withTimestampStart(request.getTimestamp())
				.withReservationId(request.getReservationId())
				.withMeterStart(meterStartValue)
				;
		// @formatter:on

		final EVSE evse = request.getEvse();
		if ( evse != null ) {
			if ( evse.getId() != null ) {
				startInfoDetails.withEvseId(evse.getId());
			}
			if ( evse.getConnectorId() != null ) {
				startInfoDetails.withConnectorId(evse.getConnectorId());
			}
		}

		final Transaction tx = request.getTransactionInfo();
		if ( tx != null ) {
			startInfoDetails.withTransactionId(tx.getTransactionId());
		}

		final ChargeSessionStartInfo startInfo = startInfoDetails.build();

		log.info("Received transaction start request: {}", startInfo);

		ChargeSession session = chargeSessionManager.startChargingSession(startInfo);

		log.info("Charge session {} started for tx {}", session.getId(), session.getTransactionId());

		IdTokenInfo tagInfo = new IdTokenInfo(AuthorizationStatusEnum.ACCEPTED);
		TransactionEventResponse res = new TransactionEventResponse();
		res.setIdTokenInfo(tagInfo);
		resultHandler.handleActionMessageResult(message, res, null);
	}

	/**
	 * Process the update transaction event.
	 * 
	 * @param message
	 *        the message to process, never {@literal null}
	 * @param resultHandler
	 *        the handler to provider the results to
	 * @param chargePointId
	 *        the charge point ID
	 * @param request
	 *        the request
	 */
	protected void processUpdateTransaction(final ActionMessage<TransactionEventRequest> message,
			final ActionMessageResultHandler<TransactionEventRequest, TransactionEventResponse> resultHandler,
			final ChargePointIdentity chargePointId, final TransactionEventRequest request) {
		final EVSE evse = request.getEvse();

		List<MeterValue> values = request.getMeterValue();
		List<net.solarnetwork.ocpp.domain.SampledValue> newReadings = new ArrayList<>();
		if ( values != null && !values.isEmpty() ) {
			for ( MeterValue mv : values ) {
				mv.getSampledValue().stream().map(v -> {
					return OcppUtils.sampledValue(null, mv.getTimestamp(), v);
				}).forEach(newReadings::add);
			}
		}
		if ( !newReadings.isEmpty() ) {
			log.info("Saving charge point {} EVSE {} connector {} readings: {}", chargePointId,
					evse.getId(), evse.getConnectorId(), newReadings);
			chargeSessionManager.addChargingSessionReadings(chargePointId, evse.getId(),
					evse.getConnectorId(), newReadings);
		}

		IdTokenInfo tagInfo = new IdTokenInfo(AuthorizationStatusEnum.ACCEPTED);
		TransactionEventResponse res = new TransactionEventResponse();
		res.setIdTokenInfo(tagInfo);
		resultHandler.handleActionMessageResult(message, res, null);
	}

	/**
	 * Process the end transaction event.
	 * 
	 * @param message
	 *        the message to process, never {@literal null}
	 * @param resultHandler
	 *        the handler to provider the results to
	 * @param chargePointId
	 *        the charge point ID
	 * @param request
	 *        the request
	 */
	protected void processEndTransaction(final ActionMessage<TransactionEventRequest> message,
			final ActionMessageResultHandler<TransactionEventRequest, TransactionEventResponse> resultHandler,
			final ChargePointIdentity chargePointId, final TransactionEventRequest request) {
		final Transaction tx = request.getTransactionInfo();

		final ChargeSession session = chargeSessionManager.getActiveChargingSession(chargePointId,
				tx.getTransactionId());
		if ( session == null ) {
			resultHandler.handleActionMessageResult(message, new TransactionEventResponse(), null);
			return;
		}

		// try to extract a "meter stop" value like provided in OCPP 1.6, by looking for
		// an ENERGY_ACTIVE_IMPORT_REGISTER sample value
		final long meterEndValue = findEnergyReadingSampledValue(request.getMeterValue(),
				ReadingContextEnum.TRANSACTION_END);

		final IdToken idToken = request.getIdToken();

		// @formatter:off
		ChargeSessionEndInfo info = ChargeSessionEndInfo.builder()
				.withChargePointId(chargePointId)
				.withAuthorizationId(idToken.getIdToken())
				.withTransactionId(tx.getTransactionId())
				.withMeterEnd(meterEndValue)
				.withTimestampEnd(request.getTimestamp())
				.withReason(OcppUtils.reason(tx.getStoppedReason()))
				.withTransactionData(sampledValues(session.getId(), request.getMeterValue()))
				.build();
		// @formatter:on

		log.info("Received transaction end request: {}", info);

		AuthorizationInfo authInfo = chargeSessionManager.endChargingSession(info);

		IdTokenInfo tokenInfo = null;
		if ( authInfo != null ) {
			tokenInfo = new IdTokenInfo(OcppUtils.statusForStatus(authInfo.getStatus()));
		}
		TransactionEventResponse res = new TransactionEventResponse();
		res.setIdTokenInfo(tokenInfo);
		resultHandler.handleActionMessageResult(message, res, null);
	}

	private long findEnergyReadingSampledValue(List<MeterValue> meters, ReadingContextEnum context) {
		if ( meters == null ) {
			return 0;
		}
		SampledValue sv = meters.stream()
				.flatMap(m -> m.getSampledValue() != null ? m.getSampledValue().stream()
						: Collections.<SampledValue> emptyList().stream())
				.filter(s -> s.getValue() != null
						&& s.getMeasurand() == MeasurandEnum.ENERGY_ACTIVE_IMPORT_REGISTER
						&& (s.getLocation() == null || s.getLocation() == LocationEnum.OUTLET)
						&& (s.getContext() == null || s.getContext() == context
								|| s.getContext() == ReadingContextEnum.SAMPLE_PERIODIC)
						&& (s.getPhase() == null))
				.findFirst().orElse(null);
		if ( sv != null ) {
			BigDecimal d = BigDecimal.valueOf(sv.getValue());
			UnitOfMeasure unit = sv.getUnitOfMeasure();
			if ( unit != null ) {
				if ( unit.getMultiplier() != null ) {
					d = d.scaleByPowerOfTen(unit.getMultiplier());
				}
				if ( kWh.name().equalsIgnoreCase(unit.getUnit()) ) {
					d = d.scaleByPowerOfTen(3);
				}
			}
			return d.longValue();
		}
		return 0;
	}

	private List<net.solarnetwork.ocpp.domain.SampledValue> sampledValues(UUID chargeSessionId,
			List<MeterValue> transactionData) {
		List<net.solarnetwork.ocpp.domain.SampledValue> result = null;
		if ( transactionData != null && !transactionData.isEmpty() ) {
			for ( MeterValue v : transactionData ) {
				if ( v.getSampledValue() == null || v.getSampledValue().isEmpty() ) {
					continue;
				}
				for ( SampledValue sv : v.getSampledValue() ) {
					if ( result == null ) {
						result = new ArrayList<>(16);
					}
					result.add(OcppUtils.sampledValue(chargeSessionId, v.getTimestamp(), sv));
				}
			}
		}
		return result;
	}

}
