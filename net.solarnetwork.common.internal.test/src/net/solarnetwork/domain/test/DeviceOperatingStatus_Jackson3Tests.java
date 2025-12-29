/* ==================================================================
 * DeviceOperatingStatusTests.java - 18/02/2019 11:26:37 am
 *
 * Copyright 2019 SolarNetwork.net Dev Team
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

package net.solarnetwork.domain.test;

import static org.assertj.core.api.BDDAssertions.from;
import static org.assertj.core.api.BDDAssertions.then;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import com.fasterxml.jackson.annotation.JsonInclude;
import net.solarnetwork.domain.DeviceOperatingState;
import net.solarnetwork.domain.DeviceOperatingStatus;
import net.solarnetwork.domain.GenericDeviceOperatingState;
import net.solarnetwork.domain.GenericDeviceOperatingStatus;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Test cases for the {@link DeviceOperatingStatus} class.
 *
 * @author matt
 * @version 1.0
 */
public class DeviceOperatingStatus_Jackson3Tests {

	private ObjectMapper objectMapper;

	@Before
	public void setup() {
		objectMapper = JsonMapper.builder()
				.changeDefaultPropertyInclusion(
						incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))
				.changeDefaultPropertyInclusion(
						incl -> incl.withContentInclusion(JsonInclude.Include.NON_NULL))
				.build();
	}

	@Test
	public void serializeJson() throws Exception {
		Set<GenericDeviceOperatingState> deviceStates = new LinkedHashSet<>(
				Arrays.asList(new GenericDeviceOperatingState(3), new GenericDeviceOperatingState(15)));
		DeviceOperatingStatus<GenericDeviceOperatingState> s = new DeviceOperatingStatus<>(
				DeviceOperatingState.Normal, deviceStates);
		String json = objectMapper.writeValueAsString(s);
		then(json).as("JSON value").isEqualTo(
				"{\"state\":\"Normal\",\"stateCode\":1,\"deviceStatesCode\":16388,\"deviceStates\":[3,15]}");
	}

	@Test
	public void deserializeJson() throws Exception {
		String json = "{\"state\":\"Normal\",\"stateCode\":1,\"deviceStatesCode\":16388}";
		DeviceOperatingStatus<GenericDeviceOperatingState> s = objectMapper.readValue(json,
				GenericDeviceOperatingStatus.class);

		// @formatter:off
		then(s)
			.as("State")
			.returns(DeviceOperatingState.Normal, from(DeviceOperatingStatus::getState))
			.as("State code")
			.returns(DeviceOperatingState.Normal.getCode(), from(DeviceOperatingStatus::getStateCode))
			.as("State code")
			.returns(16388, from(DeviceOperatingStatus::getDeviceStatesCode))
			.as("Device states")
			.returns(Set.of(new GenericDeviceOperatingState(3), new GenericDeviceOperatingState(15)), from(DeviceOperatingStatus::getDeviceStates))
			;
		// @formatter:on
	}

}
