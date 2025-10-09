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

package net.solarnetwork.ocpp.v16.jakarta.cp.json.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import java.io.IOException;
import java.math.BigDecimal;
import org.junit.Before;
import org.junit.Test;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.solarnetwork.ocpp.domain.SchemaValidationException;
import net.solarnetwork.ocpp.v16.jakarta.ChargePointAction;
import net.solarnetwork.ocpp.v16.jakarta.cp.json.ChargePointActionPayloadDecoder;
import net.solarnetwork.ocpp.v16.jakarta.json.BaseActionPayloadDecoder;
import ocpp.v16.jakarta.cp.CancelReservationRequest;
import ocpp.v16.jakarta.cp.CancelReservationResponse;
import ocpp.v16.jakarta.cp.CancelReservationStatus;
import ocpp.v16.jakarta.cp.ChargingProfile;
import ocpp.v16.jakarta.cp.ChargingProfileKindType;
import ocpp.v16.jakarta.cp.ChargingProfilePurposeType;
import ocpp.v16.jakarta.cp.ChargingRateUnitType;
import ocpp.v16.jakarta.cp.ChargingSchedule;
import ocpp.v16.jakarta.cp.ChargingSchedulePeriod;
import ocpp.v16.jakarta.cp.GetConfigurationRequest;
import ocpp.v16.jakarta.cp.GetConfigurationResponse;
import ocpp.v16.jakarta.cp.SetChargingProfileRequest;

/**
 * Test cases for the {@link ChargePointActionPayloadDecoder} class.
 *
 * @author matt
 * @version 1.2
 */
public class ChargePointActionPayloadDecoderTests {

	private ObjectMapper mapper;
	private ChargePointActionPayloadDecoder decoder;

	private ObjectMapper createObjectMapper() {
		return BaseActionPayloadDecoder.defaultObjectMapper();
	}

	private JsonNode treeForJson(String json) {
		try {
			return mapper.readTree(json);
		} catch ( IOException e ) {
			throw new RuntimeException(e);
		}
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
	public void decodeNull() throws IOException {
		CancelReservationRequest result = decoder
				.decodeActionPayload(ChargePointAction.CancelReservation, false, null);
		assertThat("Null returned on null input", result, is(nullValue()));
	}

	@Test
	public void decodeJsonNull() throws IOException {
		JsonNode json = treeForJson("null");
		CancelReservationRequest result = decoder
				.decodeActionPayload(ChargePointAction.CancelReservation, false, json);
		assertThat("Null returned on JSON null input", result, is(nullValue()));
	}

	@Test
	public void decodeJsonEmptyObject() throws IOException {
		JsonNode json = treeForJson("{}");
		CancelReservationRequest result = decoder
				.decodeActionPayload(ChargePointAction.CancelReservation, false, json);
		assertThat("Null returned on JSON empty object input", result, is(nullValue()));
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

	@Test
	public void decodeSetChargingProfileRequest() throws Exception {
		JsonNode json = treeForResource("setchargingprofile-req-01.json");
		SetChargingProfileRequest result = decoder
				.decodeActionPayload(ChargePointAction.SetChargingProfile, false, json);
		assertThat("Result decoded", result, notNullValue());

		assertThat("Connector ID decoded", result.getConnectorId(), is(equalTo(1)));
		assertThat("CsChargingProfiles decoded", result.getCsChargingProfiles(), is(notNullValue()));

		ChargingProfile cp = result.getCsChargingProfiles();
		assertThat("Charging profile ID value", cp.getChargingProfileId(), is(equalTo(11)));
		assertThat("Transaction ID value", cp.getTransactionId(), is(equalTo(224296)));
		assertThat("Charging profile purpose value", cp.getChargingProfilePurpose(),
				is(equalTo(ChargingProfilePurposeType.TX_PROFILE)));
		assertThat("Charging profile kind value", cp.getChargingProfileKind(),
				is(equalTo(ChargingProfileKindType.RELATIVE)));
		assertThat("Charging schedule decoded", cp.getChargingSchedule(), is(notNullValue()));

		ChargingSchedule sched = cp.getChargingSchedule();
		assertThat("Charging rate unit value", sched.getChargingRateUnit(),
				is(equalTo(ChargingRateUnitType.W)));
		assertThat("Charging schedule period decoded", sched.getChargingSchedulePeriod(), hasSize(1));
		assertThat("No duration", sched.getDuration(), is(nullValue()));

		ChargingSchedulePeriod per = sched.getChargingSchedulePeriod().get(0);
		assertThat("Start period value", per.getStartPeriod(), is(equalTo(0)));
		assertThat("Limit value", per.getLimit().compareTo(new BigDecimal("4000")), is(equalTo(0)));
	}

	// TODO: other actions

}
