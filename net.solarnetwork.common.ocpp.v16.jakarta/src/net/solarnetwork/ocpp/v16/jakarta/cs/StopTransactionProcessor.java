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

package net.solarnetwork.ocpp.v16.jakarta.cs;

import static net.solarnetwork.util.ObjectUtils.requireNonNullArgument;
import static net.solarnetwork.util.ObjectUtils.requireNonNullProperty;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import net.solarnetwork.ocpp.domain.Action;
import net.solarnetwork.ocpp.domain.ActionMessage;
import net.solarnetwork.ocpp.domain.AuthorizationInfo;
import net.solarnetwork.ocpp.domain.ChargePointIdentity;
import net.solarnetwork.ocpp.domain.ChargeSession;
import net.solarnetwork.ocpp.domain.ChargeSessionEndInfo;
import net.solarnetwork.ocpp.domain.ChargeSessionEndReason;
import net.solarnetwork.ocpp.domain.ErrorCodeException;
import net.solarnetwork.ocpp.domain.SampledValue;
import net.solarnetwork.ocpp.service.ActionMessageResultHandler;
import net.solarnetwork.ocpp.service.AuthorizationException;
import net.solarnetwork.ocpp.service.BaseActionMessageProcessor;
import net.solarnetwork.ocpp.service.cs.ChargeSessionManager;
import net.solarnetwork.ocpp.v16.jakarta.ActionErrorCode;
import net.solarnetwork.ocpp.v16.jakarta.CentralSystemAction;
import net.solarnetwork.ocpp.xml.jakarta.support.XmlDateUtils;
import ocpp.v16.jakarta.cs.IdTagInfo;
import ocpp.v16.jakarta.cs.MeterValue;
import ocpp.v16.jakarta.cs.Reason;
import ocpp.v16.jakarta.cs.StopTransactionRequest;
import ocpp.v16.jakarta.cs.StopTransactionResponse;

/**
 * Process {@link StopTransactionRequest} action messages.
 *
 * @author matt
 * @version 2.0
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
	 *         if any parameter is {@code null}
	 */
	public StopTransactionProcessor(ChargeSessionManager chargeSessionManager) {
		super(StopTransactionRequest.class, StopTransactionResponse.class, SUPPORTED_ACTIONS);
		this.chargeSessionManager = requireNonNullArgument(chargeSessionManager, "chargeSessionManager");
	}

	@Override
	public void processActionMessage(final ActionMessage<StopTransactionRequest> message,
			final ActionMessageResultHandler<StopTransactionRequest, StopTransactionResponse> resultHandler) {
		processActionMessageWithClientIdentifier(message, resultHandler,
				ActionErrorCode.FormationViolation);
	}

	@Override
	protected void handleActionMessageWithClientIdentifier(ActionMessage<StopTransactionRequest> message,
			final ActionMessageResultHandler<StopTransactionRequest, StopTransactionResponse> resultHandler,
			final ChargePointIdentity identity, final StopTransactionRequest req) {
		if ( req.getTransactionId() < 1 ) {
			ErrorCodeException err = new ErrorCodeException(ActionErrorCode.PropertyConstraintViolation,
					"The transaction ID must be greater than 0.");
			resultHandler.handleActionMessageResult(message, null, err);
			return;
		}

		final String txId = String.valueOf(req.getTransactionId());

		try {
			ChargeSession session = chargeSessionManager.getActiveChargingSession(identity, txId);

			if ( session == null ) {
				resultHandler.handleActionMessageResult(message, new StopTransactionResponse(), null);
				return;
			}

			// @formatter:off
			ChargeSessionEndInfo info = ChargeSessionEndInfo.builder()
					.withChargePointId(identity)
					.withAuthorizationId(req.getIdTag())
					.withTransactionId(txId)
					.withMeterEnd(req.getMeterStop())
					.withTimestampEnd(XmlDateUtils.timestamp(req.getTimestamp(), Instant::now))
					.withReason(reason(req.getReason()))
					.withTransactionData(sampledValues(session.getId(), req.getTransactionData()))
					.build();
			// @formatter:on

			log.info("Received StopTransaction request: {}", info);

			AuthorizationInfo authInfo = chargeSessionManager.endChargingSession(info);

			IdTagInfo tagInfo = null;
			if ( authInfo != null ) {
				tagInfo = new IdTagInfo();
				tagInfo.setStatus(CentralSystemUtils.statusForStatus(authInfo.getStatus()));
			}
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
			log.error("Exception handling StopTransactionRequest: {}", t.toString(), t);
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

	private @Nullable List<SampledValue> sampledValues(@Nullable UUID chargeSessionId,
			@Nullable List<MeterValue> transactionData) {
		List<SampledValue> result = null;
		if ( transactionData != null && !transactionData.isEmpty() ) {
			for ( MeterValue v : transactionData ) {
				if ( v.getSampledValue() == null || v.getSampledValue().isEmpty() ) {
					continue;
				}
				final Instant ts = requireNonNullProperty(
						XmlDateUtils.timestamp(v.getTimestamp(), Instant::now), "MeterValue.timestamp");
				for ( ocpp.v16.jakarta.cs.SampledValue sv : v.getSampledValue() ) {
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
