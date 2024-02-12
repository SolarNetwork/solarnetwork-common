/* ==================================================================
 * ChargePointActionPayloadDecoderTests.java - 3/02/2020 9:20:32 am
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

package net.solarnetwork.ocpp.v16.cp.json.test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.solarnetwork.ocpp.domain.SchemaValidationException;
import net.solarnetwork.ocpp.v16.ChargePointAction;
import net.solarnetwork.ocpp.v16.cp.json.ChargePointActionPayloadDecoder;
import net.solarnetwork.ocpp.v16.json.BaseActionPayloadDecoder;
import ocpp.v16.cp.CancelReservationRequest;
import ocpp.v16.cp.CancelReservationResponse;
import ocpp.v16.cp.CancelReservationStatus;
import ocpp.v16.cp.GetConfigurationRequest;
import ocpp.v16.cp.GetConfigurationResponse;

/**
 * Test cases for the {@link ChargePointActionPayloadDecoder} class.
 * 
 * @author matt
 * @version 1.0
 */
public class ChargePointActionPayloadDecoderTests {

	private ObjectMapper mapper;
	private ChargePointActionPayloadDecoder decoder;

	private ObjectMapper createObjectMapper() {
		return BaseActionPayloadDecoder.defaultObjectMapper();
	}

	private JsonNode treeForResource(String resource) {
		try {
			return mapper.readTree(getClass().getResourceAsStream(resource));
		} catch ( IOException e ) {
			throw new RuntimeException(e);
		}
	}

	@Before
	public void setup() {
		mapper = createObjectMapper();
		decoder = new ChargePointActionPayloadDecoder();
	}

	@Test
	public void decodeCancelReservationRequest() throws IOException {
		JsonNode json = treeForResource("cancelreservation-req-01.json");
		CancelReservationRequest result = decoder
				.decodeActionPayload(ChargePointAction.CancelReservation, false, json);
		assertThat("Result decoded", result, notNullValue());
		assertThat("IdTag", result.getReservationId(), equalTo(123456));
	}

	@Test(expected = SchemaValidationException.class)
	public void decodeCancelReservationRequest_invalid() throws IOException {
		JsonNode json = treeForResource("cancelreservation-req-02.json");
		decoder.decodeActionPayload(ChargePointAction.CancelReservation, false, json);
	}

	@Test
	public void decodeCancelReservationResponse() throws IOException {
		JsonNode json = treeForResource("cancelreservation-res-01.json");
		CancelReservationResponse result = decoder
				.decodeActionPayload(ChargePointAction.CancelReservation, true, json);
		assertThat("Result decoded", result, notNullValue());
		assertThat("Status", result.getStatus(), equalTo(CancelReservationStatus.ACCEPTED));
	}

	@Test(expected = SchemaValidationException.class)
	public void decodeCancelReservationResponse_invalid() throws IOException {
		JsonNode json = treeForResource("cancelreservation-res-02.json");
		decoder.decodeActionPayload(ChargePointAction.CancelReservation, true, json);
	}

	@Test
	public void encodeGetConfigurationRequest_nokeys() throws IOException {
		// GIVEN
		GetConfigurationRequest req = new GetConfigurationRequest();

		// WHEN
		String json = mapper.writeValueAsString(req);

		// THEN
		assertThat("Request encoded", json, equalTo("{}"));
	}

	@Test
	public void decodeGetConfigurationResponse() throws IOException {
		JsonNode json = treeForResource("getconfiguration-res-01.json");
		GetConfigurationResponse result = decoder.decodeActionPayload(ChargePointAction.GetConfiguration,
				true, json);
		assertThat("Result decoded", result, notNullValue());

		assertThat("Configuration list decoded", result.getConfigurationKey(), hasSize(1));
		assertThat("Configuration 1 key", result.getConfigurationKey().get(0).getKey(), equalTo("foo"));
		assertThat("Configuration 1 value", result.getConfigurationKey().get(0).getValue(),
				equalTo("bar"));
		assertThat("Configuration 1 readonly", result.getConfigurationKey().get(0).isReadonly(),
				equalTo(true));
	}

	// TODO: other actions

}
