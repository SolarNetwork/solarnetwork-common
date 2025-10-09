/* ==================================================================
 * ChargePointActionPayloadDecoder.java - 3/02/2020 6:16:07 am
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

package net.solarnetwork.ocpp.v16.jakarta.cp.json;

import java.io.IOException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.xml.bind.JAXBElement;
import net.solarnetwork.ocpp.domain.Action;
import net.solarnetwork.ocpp.domain.SchemaValidationException;
import net.solarnetwork.ocpp.v16.jakarta.ChargePointAction;
import net.solarnetwork.ocpp.v16.jakarta.json.BaseActionPayloadDecoder;
import ocpp.v16.jakarta.cp.CancelReservationRequest;
import ocpp.v16.jakarta.cp.CancelReservationResponse;
import ocpp.v16.jakarta.cp.ChangeAvailabilityRequest;
import ocpp.v16.jakarta.cp.ChangeAvailabilityResponse;
import ocpp.v16.jakarta.cp.ChangeConfigurationRequest;
import ocpp.v16.jakarta.cp.ChangeConfigurationResponse;
import ocpp.v16.jakarta.cp.ChargePointService;
import ocpp.v16.jakarta.cp.ClearCacheRequest;
import ocpp.v16.jakarta.cp.ClearCacheResponse;
import ocpp.v16.jakarta.cp.ClearChargingProfileRequest;
import ocpp.v16.jakarta.cp.ClearChargingProfileResponse;
import ocpp.v16.jakarta.cp.DataTransferRequest;
import ocpp.v16.jakarta.cp.DataTransferResponse;
import ocpp.v16.jakarta.cp.GetCompositeScheduleRequest;
import ocpp.v16.jakarta.cp.GetCompositeScheduleResponse;
import ocpp.v16.jakarta.cp.GetConfigurationRequest;
import ocpp.v16.jakarta.cp.GetConfigurationResponse;
import ocpp.v16.jakarta.cp.GetDiagnosticsRequest;
import ocpp.v16.jakarta.cp.GetDiagnosticsResponse;
import ocpp.v16.jakarta.cp.GetLocalListVersionRequest;
import ocpp.v16.jakarta.cp.GetLocalListVersionResponse;
import ocpp.v16.jakarta.cp.ObjectFactory;
import ocpp.v16.jakarta.cp.RemoteStartTransactionRequest;
import ocpp.v16.jakarta.cp.RemoteStartTransactionResponse;
import ocpp.v16.jakarta.cp.RemoteStopTransactionRequest;
import ocpp.v16.jakarta.cp.RemoteStopTransactionResponse;
import ocpp.v16.jakarta.cp.ReserveNowRequest;
import ocpp.v16.jakarta.cp.ReserveNowResponse;
import ocpp.v16.jakarta.cp.ResetRequest;
import ocpp.v16.jakarta.cp.ResetResponse;
import ocpp.v16.jakarta.cp.SendLocalListRequest;
import ocpp.v16.jakarta.cp.SendLocalListResponse;
import ocpp.v16.jakarta.cp.SetChargingProfileRequest;
import ocpp.v16.jakarta.cp.SetChargingProfileResponse;
import ocpp.v16.jakarta.cp.TriggerMessageRequest;
import ocpp.v16.jakarta.cp.TriggerMessageResponse;
import ocpp.v16.jakarta.cp.UnlockConnectorRequest;
import ocpp.v16.jakarta.cp.UnlockConnectorResponse;
import ocpp.v16.jakarta.cp.UpdateFirmwareRequest;
import ocpp.v16.jakarta.cp.UpdateFirmwareResponse;

/**
 * OCPP 1.6 implementation of
 * {@link net.solarnetwork.ocpp.json.ActionPayloadDecoder} for Charge Point
 * actions.
 *
 * @author matt
 * @version 1.1
 */
public class ChargePointActionPayloadDecoder extends BaseActionPayloadDecoder {

	/** The JAXB object factory. */
	protected final ObjectFactory jaxbObjectFactory;

	/**
	 * Constructor.
	 *
	 * <p>
	 * A default {@link ObjectMapper} instance will be used.
	 * </p>
	 */
	public ChargePointActionPayloadDecoder() {
		this(defaultObjectMapper());
	}

	/**
	 * Constructor.
	 *
	 * @param mapper
	 *        the object mapper to use
	 */
	public ChargePointActionPayloadDecoder(ObjectMapper mapper) {
		super(mapper, ObjectFactory.class, "ocpp/v16/jakarta/cp/OCPP_ChargePointService_1.6.wsdl",
				ChargePointService.class.getClassLoader());
		this.jaxbObjectFactory = new ObjectFactory();
	}

	@Override
	public <T> T decodeActionPayload(final Action action, final boolean forResult,
			final JsonNode payload) throws IOException {
		// in OCPP spec, JSON null OR empty object payload means "no payload"
		if ( payload == null || payload.isNull() || (payload.isObject() && payload.isEmpty()) ) {
			return null;
		}
		final ChargePointAction a = action instanceof ChargePointAction ? (ChargePointAction) action
				: ChargePointAction.valueOf(action.getName());
		Class<T> clazz = messageClassForAction(a, forResult);
		T result;
		try {
			result = mapper.treeToValue(payload, clazz);
		} catch ( JsonMappingException e ) {
			throw new SchemaValidationException(payload, e.getMessage(), e);
		}
		validateActionMessage(a, forResult, result);
		return result;

	}

	@SuppressWarnings("unchecked")
	private <T> Class<T> messageClassForAction(final ChargePointAction action, final boolean forResult) {
		Class<T> clazz = null;
		switch (action) {
			case CancelReservation:
				clazz = (Class<T>) (forResult ? CancelReservationResponse.class
						: CancelReservationRequest.class);
				break;

			case ChangeAvailability:
				clazz = (Class<T>) (forResult ? ChangeAvailabilityResponse.class
						: ChangeAvailabilityRequest.class);
				break;

			case ChangeConfiguration:
				clazz = (Class<T>) (forResult ? ChangeConfigurationResponse.class
						: ChangeConfigurationRequest.class);
				break;

			case ClearCache:
				clazz = (Class<T>) (forResult ? ClearCacheResponse.class : ClearCacheRequest.class);
				break;

			case ClearChargingProfile:
				clazz = (Class<T>) (forResult ? ClearChargingProfileResponse.class
						: ClearChargingProfileRequest.class);
				break;

			case DataTransfer:
				clazz = (Class<T>) (forResult ? DataTransferResponse.class : DataTransferRequest.class);
				break;

			case GetCompositeSchedule:
				clazz = (Class<T>) (forResult ? GetCompositeScheduleResponse.class
						: GetCompositeScheduleRequest.class);
				break;

			case GetConfiguration:
				clazz = (Class<T>) (forResult ? GetConfigurationResponse.class
						: GetConfigurationRequest.class);
				break;

			case GetDiagnostics:
				clazz = (Class<T>) (forResult ? GetDiagnosticsResponse.class
						: GetDiagnosticsRequest.class);
				break;

			case GetLocalListVersion:
				clazz = (Class<T>) (forResult ? GetLocalListVersionResponse.class
						: GetLocalListVersionRequest.class);
				break;

			case RemoteStartTransaction:
				clazz = (Class<T>) (forResult ? RemoteStartTransactionResponse.class
						: RemoteStartTransactionRequest.class);
				break;

			case RemoteStopTransaction:
				clazz = (Class<T>) (forResult ? RemoteStopTransactionResponse.class
						: RemoteStopTransactionRequest.class);
				break;

			case ReserveNow:
				clazz = (Class<T>) (forResult ? ReserveNowResponse.class : ReserveNowRequest.class);
				break;

			case Reset:
				clazz = (Class<T>) (forResult ? ResetResponse.class : ResetRequest.class);
				break;

			case SendLocalList:
				clazz = (Class<T>) (forResult ? SendLocalListResponse.class
						: SendLocalListRequest.class);
				break;

			case SetChargingProfile:
				clazz = (Class<T>) (forResult ? SetChargingProfileResponse.class
						: SetChargingProfileRequest.class);
				break;

			case TriggerMessage:
				clazz = (Class<T>) (forResult ? TriggerMessageResponse.class
						: TriggerMessageRequest.class);
				break;

			case UnlockConnector:
				clazz = (Class<T>) (forResult ? UnlockConnectorResponse.class
						: UnlockConnectorRequest.class);
				break;

			case UpdateFirmware:
				clazz = (Class<T>) (forResult ? UpdateFirmwareResponse.class
						: UpdateFirmwareRequest.class);
				break;

			default:
				throw new UnsupportedOperationException(
						"Action " + action.getName() + " not supported.");
		}
		return clazz;
	}

	private <T> void validateActionMessage(final ChargePointAction action, final boolean forResult,
			T obj) {
		JAXBElement<?> el = null;
		switch (action) {
			case CancelReservation:
				el = forResult
						? jaxbObjectFactory
								.createCancelReservationResponse((CancelReservationResponse) obj)
						: jaxbObjectFactory
								.createCancelReservationRequest((CancelReservationRequest) obj);
				break;

			case ChangeAvailability:
				el = forResult
						? jaxbObjectFactory
								.createChangeAvailabilityResponse((ChangeAvailabilityResponse) obj)
						: jaxbObjectFactory
								.createChangeAvailabilityRequest((ChangeAvailabilityRequest) obj);
				break;

			case ChangeConfiguration:
				el = forResult
						? jaxbObjectFactory
								.createChangeConfigurationResponse((ChangeConfigurationResponse) obj)
						: jaxbObjectFactory
								.createChangeConfigurationRequest((ChangeConfigurationRequest) obj);
				break;

			case ClearCache:
				el = forResult ? jaxbObjectFactory.createClearCacheResponse((ClearCacheResponse) obj)
						: jaxbObjectFactory.createClearCacheRequest((ClearCacheRequest) obj);
				break;

			case ClearChargingProfile:
				el = forResult
						? jaxbObjectFactory
								.createClearChargingProfileResponse((ClearChargingProfileResponse) obj)
						: jaxbObjectFactory
								.createClearChargingProfileRequest((ClearChargingProfileRequest) obj);
				break;

			case DataTransfer:
				el = forResult ? jaxbObjectFactory.createDataTransferResponse((DataTransferResponse) obj)
						: jaxbObjectFactory.createDataTransferRequest((DataTransferRequest) obj);
				break;

			case GetCompositeSchedule:
				el = forResult
						? jaxbObjectFactory
								.createGetCompositeScheduleResponse((GetCompositeScheduleResponse) obj)
						: jaxbObjectFactory
								.createGetCompositeScheduleRequest((GetCompositeScheduleRequest) obj);
				break;

			case GetConfiguration:
				el = forResult
						? jaxbObjectFactory
								.createGetConfigurationResponse((GetConfigurationResponse) obj)
						: jaxbObjectFactory.createGetConfigurationRequest((GetConfigurationRequest) obj);
				break;

			case GetDiagnostics:
				el = forResult
						? jaxbObjectFactory.createGetDiagnosticsResponse((GetDiagnosticsResponse) obj)
						: jaxbObjectFactory.createGetDiagnosticsRequest((GetDiagnosticsRequest) obj);
				break;

			case GetLocalListVersion:
				el = forResult
						? jaxbObjectFactory
								.createGetLocalListVersionResponse((GetLocalListVersionResponse) obj)
						: jaxbObjectFactory
								.createGetLocalListVersionRequest((GetLocalListVersionRequest) obj);
				break;

			case RemoteStartTransaction:
				el = forResult
						? jaxbObjectFactory.createRemoteStartTransactionResponse(
								(RemoteStartTransactionResponse) obj)
						: jaxbObjectFactory.createRemoteStartTransactionRequest(
								(RemoteStartTransactionRequest) obj);
				break;

			case RemoteStopTransaction:
				el = forResult
						? jaxbObjectFactory
								.createRemoteStopTransactionResponse((RemoteStopTransactionResponse) obj)
						: jaxbObjectFactory
								.createRemoteStopTransactionRequest((RemoteStopTransactionRequest) obj);
				break;

			case ReserveNow:
				el = forResult ? jaxbObjectFactory.createReserveNowResponse((ReserveNowResponse) obj)
						: jaxbObjectFactory.createReserveNowRequest((ReserveNowRequest) obj);
				break;

			case Reset:
				el = forResult ? jaxbObjectFactory.createResetResponse((ResetResponse) obj)
						: jaxbObjectFactory.createResetRequest((ResetRequest) obj);
				break;

			case SendLocalList:
				el = forResult
						? jaxbObjectFactory.createSendLocalListResponse((SendLocalListResponse) obj)
						: jaxbObjectFactory.createSendLocalListRequest((SendLocalListRequest) obj);
				break;

			case SetChargingProfile:
				el = forResult
						? jaxbObjectFactory
								.createSetChargingProfileResponse((SetChargingProfileResponse) obj)
						: jaxbObjectFactory
								.createSetChargingProfileRequest((SetChargingProfileRequest) obj);
				break;

			case TriggerMessage:
				el = forResult
						? jaxbObjectFactory.createTriggerMessageResponse((TriggerMessageResponse) obj)
						: jaxbObjectFactory.createTriggerMessageRequest((TriggerMessageRequest) obj);
				break;

			case UnlockConnector:
				el = forResult
						? jaxbObjectFactory.createUnlockConnectorResponse((UnlockConnectorResponse) obj)
						: jaxbObjectFactory.createUnlockConnectorRequest((UnlockConnectorRequest) obj);
				break;

			case UpdateFirmware:
				el = forResult
						? jaxbObjectFactory.createUpdateFirmwareResponse((UpdateFirmwareResponse) obj)
						: jaxbObjectFactory.createUpdateFirmwareRequest((UpdateFirmwareRequest) obj);
				break;

			default:
				throw new UnsupportedOperationException(
						"Action " + action.getName() + " not supported.");
		}
		if ( el != null ) {
			schemaHelper.validate(jaxbContext, el);
		}
	}

}
