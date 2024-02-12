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

package net.solarnetwork.ocpp.v16.cs.json;

import java.io.IOException;
import javax.xml.bind.JAXBElement;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.solarnetwork.ocpp.domain.Action;
import net.solarnetwork.ocpp.domain.SchemaValidationException;
import net.solarnetwork.ocpp.v16.CentralSystemAction;
import net.solarnetwork.ocpp.v16.json.BaseActionPayloadDecoder;
import ocpp.v16.cs.AuthorizeRequest;
import ocpp.v16.cs.AuthorizeResponse;
import ocpp.v16.cs.BootNotificationRequest;
import ocpp.v16.cs.BootNotificationResponse;
import ocpp.v16.cs.CentralSystemService;
import ocpp.v16.cs.DataTransferRequest;
import ocpp.v16.cs.DataTransferResponse;
import ocpp.v16.cs.DiagnosticsStatusNotificationRequest;
import ocpp.v16.cs.DiagnosticsStatusNotificationResponse;
import ocpp.v16.cs.FirmwareStatusNotificationRequest;
import ocpp.v16.cs.FirmwareStatusNotificationResponse;
import ocpp.v16.cs.HeartbeatRequest;
import ocpp.v16.cs.HeartbeatResponse;
import ocpp.v16.cs.MeterValuesRequest;
import ocpp.v16.cs.MeterValuesResponse;
import ocpp.v16.cs.ObjectFactory;
import ocpp.v16.cs.StartTransactionRequest;
import ocpp.v16.cs.StartTransactionResponse;
import ocpp.v16.cs.StatusNotificationRequest;
import ocpp.v16.cs.StatusNotificationResponse;
import ocpp.v16.cs.StopTransactionRequest;
import ocpp.v16.cs.StopTransactionResponse;

/**
 * OCPP 1.6 implementation of
 * {@link net.solarnetwork.ocpp.json.ActionPayloadDecoder} for Central Service
 * actions.
 * 
 * @author matt
 * @version 1.0
 */
public class CentralServiceActionPayloadDecoder extends BaseActionPayloadDecoder {

	/** The JAXB object factory. */
	protected final ObjectFactory jaxbObjectFactory;

	/**
	 * Constructor.
	 * 
	 * <p>
	 * A default {@link ObjectMapper} instance will be used.
	 * </p>
	 */
	public CentralServiceActionPayloadDecoder() {
		this(defaultObjectMapper());
	}

	/**
	 * Constructor.
	 * 
	 * @param mapper
	 *        the object mapper to use
	 */
	public CentralServiceActionPayloadDecoder(ObjectMapper mapper) {
		super(mapper, ObjectFactory.class, "ocpp/v16/cs/OCPP_CentralSystemService_1.6.wsdl",
				CentralSystemService.class.getClassLoader());
		this.jaxbObjectFactory = new ObjectFactory();
	}

	@Override
	public <T> T decodeActionPayload(final Action action, final boolean forResult,
			final JsonNode payload) throws IOException {
		// in OCPP spec, JSON null OR empty object payload means "no payload"
		if ( payload.isNull() || (payload.isObject() && payload.isEmpty()) ) {
			return null;
		}
		final CentralSystemAction a = action instanceof CentralSystemAction
				? (CentralSystemAction) action
				: CentralSystemAction.valueOf(action.getName());
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
	private <T> Class<T> messageClassForAction(final CentralSystemAction action,
			final boolean forResult) {
		Class<T> clazz = null;
		switch (action) {
			case Authorize:
				clazz = (Class<T>) (forResult ? AuthorizeResponse.class : AuthorizeRequest.class);
				break;

			case BootNotification:
				clazz = (Class<T>) (forResult ? BootNotificationResponse.class
						: BootNotificationRequest.class);
				break;

			case DataTransfer:
				clazz = (Class<T>) (forResult ? DataTransferResponse.class : DataTransferRequest.class);
				break;

			case DiagnosticsStatusNotification:
				clazz = (Class<T>) (forResult ? DiagnosticsStatusNotificationResponse.class
						: DiagnosticsStatusNotificationRequest.class);
				break;

			case FirmwareStatusNotification:
				clazz = (Class<T>) (forResult ? FirmwareStatusNotificationResponse.class
						: FirmwareStatusNotificationRequest.class);
				break;

			case Heartbeat:
				clazz = (Class<T>) (forResult ? HeartbeatResponse.class : HeartbeatRequest.class);
				break;

			case MeterValues:
				clazz = (Class<T>) (forResult ? MeterValuesResponse.class : MeterValuesRequest.class);
				break;

			case StartTransaction:
				clazz = (Class<T>) (forResult ? StartTransactionResponse.class
						: StartTransactionRequest.class);
				break;

			case StatusNotification:
				clazz = (Class<T>) (forResult ? StatusNotificationResponse.class
						: StatusNotificationRequest.class);
				break;

			case StopTransaction:
				clazz = (Class<T>) (forResult ? StopTransactionResponse.class
						: StopTransactionRequest.class);
				break;

			default:
				throw new UnsupportedOperationException(
						"Action " + action.getName() + " not supported.");
		}
		return clazz;
	}

	private <T> void validateActionMessage(final CentralSystemAction action, final boolean forResult,
			T obj) {
		JAXBElement<?> el = null;
		switch (action) {
			case Authorize:
				el = forResult ? jaxbObjectFactory.createAuthorizeResponse((AuthorizeResponse) obj)
						: jaxbObjectFactory.createAuthorizeRequest((AuthorizeRequest) obj);
				break;

			case BootNotification:
				el = forResult
						? jaxbObjectFactory
								.createBootNotificationResponse((BootNotificationResponse) obj)
						: jaxbObjectFactory.createBootNotificationRequest((BootNotificationRequest) obj);
				break;

			case DataTransfer:
				el = forResult ? jaxbObjectFactory.createDataTransferResponse((DataTransferResponse) obj)
						: jaxbObjectFactory.createDataTransferRequest((DataTransferRequest) obj);
				break;

			case DiagnosticsStatusNotification:
				el = forResult
						? jaxbObjectFactory.createDiagnosticsStatusNotificationResponse(
								(DiagnosticsStatusNotificationResponse) obj)
						: jaxbObjectFactory.createDiagnosticsStatusNotificationRequest(
								(DiagnosticsStatusNotificationRequest) obj);
				break;

			case FirmwareStatusNotification:
				el = forResult
						? jaxbObjectFactory.createFirmwareStatusNotificationResponse(
								(FirmwareStatusNotificationResponse) obj)
						: jaxbObjectFactory.createFirmwareStatusNotificationRequest(
								(FirmwareStatusNotificationRequest) obj);
				break;

			case Heartbeat:
				el = forResult ? jaxbObjectFactory.createHeartbeatResponse((HeartbeatResponse) obj)
						: jaxbObjectFactory.createHeartbeatRequest((HeartbeatRequest) obj);
				break;

			case MeterValues:
				el = forResult ? jaxbObjectFactory.createMeterValuesResponse((MeterValuesResponse) obj)
						: jaxbObjectFactory.createMeterValuesRequest((MeterValuesRequest) obj);
				break;

			case StartTransaction:
				el = forResult
						? jaxbObjectFactory
								.createStartTransactionResponse((StartTransactionResponse) obj)
						: jaxbObjectFactory.createStartTransactionRequest((StartTransactionRequest) obj);
				break;

			case StatusNotification:
				el = forResult
						? jaxbObjectFactory
								.createStatusNotificationResponse((StatusNotificationResponse) obj)
						: jaxbObjectFactory
								.createStatusNotificationRequest((StatusNotificationRequest) obj);
				break;

			case StopTransaction:
				el = forResult
						? jaxbObjectFactory.createStopTransactionResponse((StopTransactionResponse) obj)
						: jaxbObjectFactory.createStopTransactionRequest((StopTransactionRequest) obj);
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
