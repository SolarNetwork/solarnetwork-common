/* ==================================================================
 * OcppUtilsTests.java - 9/02/2024 3:42:24 pm
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

package net.solarnetwork.ocpp.v201.util.tests;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.FileCopyUtils;
import com.networknt.schema.JsonSchemaFactory;
import net.solarnetwork.ocpp.domain.SchemaValidationException;
import net.solarnetwork.ocpp.v201.util.OcppUtils;
import ocpp.v201.ConnectorStatusEnum;
import ocpp.v201.StatusNotificationRequest;

/**
 * Test cases for the {@link OcppUtils} class.
 * 
 * @author matt
 * @version 1.0
 */
public class OcppUtilsTests {

	private JsonSchemaFactory jsonSchemaFactory;

	@Before
	public void setup() {
		jsonSchemaFactory = OcppUtils.ocppSchemaFactory_v201();
	}

	@Test
	public void requestActionClass() {
		// GIVEN
		String action = "Foo";

		// WHEN
		String result = OcppUtils.actionClassName(action, true);

		// THEN
		assertThat("Action class generated", result, is(equalTo("FooRequest")));
	}

	@Test
	public void responseActionClass() {
		// GIVEN
		String action = "Foo";

		// WHEN
		String result = OcppUtils.actionClassName(action, false);

		// THEN
		assertThat("Action class generated", result, is(equalTo("FooResponse")));
	}

	@Test
	public void parse_StatusNotificationRequest() throws IOException {
		// GIVEN
		String action = "StatusNotification";
		String json = FileCopyUtils.copyToString(new InputStreamReader(
				getClass().getResourceAsStream("test-ocpp-StatusNotificationRequest-01.json"),
				StandardCharsets.UTF_8));

		// WHEN
		Object msg = OcppUtils.parseOcppMessage(action, true, json, jsonSchemaFactory);

		// THEN
		assertThat("Message parsed and validated", msg, is(instanceOf(StatusNotificationRequest.class)));
		StatusNotificationRequest req = (StatusNotificationRequest) msg;
		assertThat("Property timestamp parsed", req.getTimestamp(),
				is(equalTo(Instant.parse("2024-02-10T10:10:10.010Z"))));
		assertThat("Property connectorStatus parsed",
				req.getConnectorStatus().equals(ConnectorStatusEnum.AVAILABLE));
		assertThat("Property evseId parsed", req.getEvseId(), is(equalTo(1)));
		assertThat("Property connectorId parsed", req.getConnectorId(), is(equalTo(2)));
	}

	@Test(expected = SchemaValidationException.class)
	public void parse_StatusNotificationRequest_invalid() throws IOException {
		// GIVEN
		String action = "StatusNotification";
		String json = FileCopyUtils.copyToString(new InputStreamReader(
				getClass().getResourceAsStream("test-ocpp-StatusNotificationRequest-02.json"),
				StandardCharsets.UTF_8));

		// WHEN
		OcppUtils.parseOcppMessage(action, true, json, jsonSchemaFactory);
	}

}
