/* ==================================================================
 * ChargePointTests.java - 12/02/2026 10:06:45 am
 *
 * Copyright 2026 SolarNetwork.net Dev Team
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

package net.solarnetwork.ocpp.domain.test;

import static org.assertj.core.api.BDDAssertions.from;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;
import net.solarnetwork.ocpp.domain.ChargePoint;
import net.solarnetwork.ocpp.domain.ChargePointInfo;
import net.solarnetwork.ocpp.domain.RegistrationStatus;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Test cases for the {@link ChargePoint} class.
 *
 * @author matt
 * @version 1.0
 */
public class ChargePointTests {

	private ObjectMapper createObjectMapper() {
		return JsonMapper.builder().build();
	}

	@Test
	public void parseJson() {
		// GIVEN

		final ObjectMapper mapper = createObjectMapper();

		// WHEN
		ChargePoint result = mapper.readValue("""
				{
				  "enabled": true,
				  "registrationStatus": "Pending",
				  "connectorCount": 2,
				  "info": {
				    "id": "CP0001",
				    "chargePointModel": "SolarNode",
				    "chargePointVendor": "SolarNetwork",
				    "chargePointSerialNumber": "ABC123XYZ",
				    "chargeBoxSerialNumber": "xyz321abc",
				    "firmwareVersion": "1.2.3",
				    "iccid": "abc123",
				    "imsi": "def234",
				    "meterType": "efg345",
				    "meterSerialNumber": "fgh456"
				  }
				}
				""", ChargePoint.class);

		// THEN
		// @formatter:off
		then(result)
			.as("Entity parsed")
			.isNotNull()
			.returns(null, from(ChargePoint::getId))
			.returns(true, from(ChargePoint::isEnabled))
			.returns(RegistrationStatus.Pending, from(ChargePoint::getRegistrationStatus))
			.returns(2, from(ChargePoint::getConnectorCount))
			.extracting(ChargePoint::getInfo)
			.as("Info parsed")
			.isNotNull()
			.returns("CP0001", from(ChargePointInfo::getId))
			.returns("SolarNode", from(ChargePointInfo::getChargePointModel))
			.returns("SolarNetwork", from(ChargePointInfo::getChargePointVendor))
			.returns("ABC123XYZ", from(ChargePointInfo::getChargePointSerialNumber))
			.returns("xyz321abc", from(ChargePointInfo::getChargeBoxSerialNumber))
			.returns("1.2.3", from(ChargePointInfo::getFirmwareVersion))
			.returns("abc123", from(ChargePointInfo::getIccid))
			.returns("def234", from(ChargePointInfo::getImsi))
			.returns("efg345", from(ChargePointInfo::getMeterType))
			.returns("fgh456", from(ChargePointInfo::getMeterSerialNumber))
			;
		// @formatter:on

	}

}
