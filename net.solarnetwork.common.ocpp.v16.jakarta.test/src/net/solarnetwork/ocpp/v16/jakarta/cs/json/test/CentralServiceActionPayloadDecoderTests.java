/* ==================================================================
 * CentralServiceActionPayloadDecoderTests.java - 3/02/2020 7:53:09 am
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

package net.solarnetwork.ocpp.v16.jakarta.cs.json.test;

import static net.solarnetwork.ocpp.xml.jakarta.support.XmlDateUtils.newXmlCalendar;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import net.solarnetwork.ocpp.domain.SchemaValidationException;
import net.solarnetwork.ocpp.v16.jakarta.CentralSystemAction;
import net.solarnetwork.ocpp.v16.jakarta.cs.json.CentralServiceActionPayloadDecoder;
import net.solarnetwork.ocpp.v16.jakarta.json.BaseActionPayloadDecoder;
import ocpp.v16.jakarta.cp.CancelReservationRequest;
import ocpp.v16.jakarta.cs.AuthorizationStatus;
import ocpp.v16.jakarta.cs.AuthorizeRequest;
import ocpp.v16.jakarta.cs.AuthorizeResponse;
import ocpp.v16.jakarta.cs.BootNotificationRequest;
import ocpp.v16.jakarta.cs.BootNotificationResponse;
import ocpp.v16.jakarta.cs.RegistrationStatus;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Test cases for the {@link CentralServiceActionPayloadDecoder} class.
 *
 * @author matt
 * @version 2.0
 */
public class CentralServiceActionPayloadDecoderTests {

	private ObjectMapper mapper;
	private CentralServiceActionPayloadDecoder decoder;

	private ObjectMapper createObjectMapper() {
		return BaseActionPayloadDecoder.defaultObjectMapper();
	}

	private JsonNode treeForJson(String json) {
		return mapper.readTree(json);
	}

	private JsonNode treeForResource(String resource) {
		return mapper.readTree(getClass().getResourceAsStream(resource));
	}

	@Before
	public void setup() {
		mapper = createObjectMapper();
		decoder = new CentralServiceActionPayloadDecoder();
	}

	@Test
	public void decodeNull() throws IOException {
		CancelReservationRequest result = decoder.decodeActionPayload(CentralSystemAction.Authorize,
				false, null);
		assertThat("Null returned on null input", result, is(nullValue()));
	}

	@Test
	public void decodeJsonNull() throws IOException {
		JsonNode json = treeForJson("null");
		CancelReservationRequest result = decoder.decodeActionPayload(CentralSystemAction.Authorize,
				false, json);
		assertThat("Null returned on JSON null input", result, is(nullValue()));
	}

	@Test
	public void decodeJsonEmptyObject() throws IOException {
		JsonNode json = treeForJson("{}");
		CancelReservationRequest result = decoder.decodeActionPayload(CentralSystemAction.Authorize,
				false, json);
		assertThat("Null returned on JSON empty object input", result, is(nullValue()));
	}

	@Test
	public void decodeAuthorizeRequest() throws IOException {
		JsonNode json = treeForResource("authorize-req-01.json");
		AuthorizeRequest result = decoder.decodeActionPayload(CentralSystemAction.Authorize, false,
				json);
		assertThat("Result decoded", result, notNullValue());
		assertThat("IdTag", result.getIdTag(), equalTo("abc123"));
	}

	@Test(expected = SchemaValidationException.class)
	public void decodeAuthorizeRequest_invalid() throws IOException {
		JsonNode json = treeForResource("authorize-req-02.json");
		decoder.decodeActionPayload(CentralSystemAction.Authorize, false, json);
	}

	@Test
	public void decodeAuthorizeResponse() throws IOException {
		JsonNode json = treeForResource("authorize-res-01.json");
		AuthorizeResponse result = decoder.decodeActionPayload(CentralSystemAction.Authorize, true,
				json);
		assertThat("Result decoded", result, notNullValue());
		assertThat("IdTagInfo available", result.getIdTagInfo(), notNullValue());
		assertThat("IdTagInfo status", result.getIdTagInfo().getStatus(),
				equalTo(AuthorizationStatus.ACCEPTED));
		assertThat("IdTagInfo expiryDate", result.getIdTagInfo().getExpiryDate(),
				equalTo(newXmlCalendar(2020, 2, 1, 12, 34, 56, 789)));
		assertThat("IdTagInfo parentIdTag", result.getIdTagInfo().getParentIdTag(), equalTo("def123"));
	}

	@Test(expected = SchemaValidationException.class)
	public void decodeAuthorizeResponse_invalid() throws IOException {
		JsonNode json = treeForResource("authorize-res-02.json");
		decoder.decodeActionPayload(CentralSystemAction.Authorize, true, json);
	}

	@Test
	public void decodeBootNotificationRequest() throws IOException {
		JsonNode json = treeForResource("bootnotification-req-01.json");
		BootNotificationRequest result = decoder
				.decodeActionPayload(CentralSystemAction.BootNotification, false, json);
		assertThat("Result decoded", result, notNullValue());
		assertThat("Charge point vendor", result.getChargePointVendor(), equalTo("ACME"));
		assertThat("Charge point model", result.getChargePointModel(), equalTo("Super Duper"));
		assertThat("Charge point serial number", result.getChargePointSerialNumber(),
				equalTo("1234567890"));
		assertThat("Charge box serial number", result.getChargeBoxSerialNumber(), equalTo("0987654321"));
		assertThat("Firmware version", result.getFirmwareVersion(), equalTo("1.2.3"));
		assertThat("ICCID", result.getIccid(), equalTo("ABC-123-456"));
		assertThat("IMSI", result.getImsi(), equalTo("DEF-321-654"));
		assertThat("Meter type", result.getMeterType(), equalTo("Good One"));
		assertThat("Meter serial number", result.getMeterSerialNumber(), equalTo("ABCDEFGHIJKL"));
	}

	@Test
	public void decodeBootNotificationResponse() throws IOException {
		JsonNode json = treeForResource("bootnotification-res-01.json");
		BootNotificationResponse result = decoder
				.decodeActionPayload(CentralSystemAction.BootNotification, true, json);
		assertThat("Result decoded", result, notNullValue());
		assertThat("Status", result.getStatus(), equalTo(RegistrationStatus.ACCEPTED));
		assertThat("Current time", result.getCurrentTime(),
				equalTo(newXmlCalendar(2020, 2, 1, 23, 45, 67, 890)));
		assertThat("Interval", result.getInterval(), equalTo(600));
	}

	// TODO: other actions
}
