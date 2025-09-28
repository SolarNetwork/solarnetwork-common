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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.solarnetwork.domain.Bitmaskable;
import net.solarnetwork.domain.DeviceOperatingState;
import net.solarnetwork.domain.DeviceOperatingStatus;
import net.solarnetwork.domain.GenericDeviceOperatingState;
import net.solarnetwork.domain.GenericDeviceOperatingStatus;

/**
 * Test cases for the {@link DeviceOperatingStatus} class.
 *
 * @author matt
 * @version 1.0
 */
public class DeviceOperatingStatusTests {

	private ObjectMapper objectMapper;

	@Before
	public void setup() {
		objectMapper = new ObjectMapper();
		objectMapper.setDefaultPropertyInclusion(Include.NON_NULL);
	}

	@Test
	public void serializeJson() throws Exception {
		Set<GenericDeviceOperatingState> deviceStates = new LinkedHashSet<>(
				Arrays.asList(new GenericDeviceOperatingState(3), new GenericDeviceOperatingState(15)));
		DeviceOperatingStatus<GenericDeviceOperatingState> s = new DeviceOperatingStatus<>(
				DeviceOperatingState.Normal, deviceStates);
		String json = objectMapper.writeValueAsString(s);
		assertThat("JSON value", json, equalTo(
				"{\"state\":\"Normal\",\"stateCode\":1,\"deviceStatesCode\":16388,\"deviceStates\":[3,15]}"));
	}

	@Test
	public void deserializeJson() throws Exception {
		String json = "{\"state\":\"Normal\",\"stateCode\":1,\"deviceStatesCode\":16388}";
		DeviceOperatingStatus<GenericDeviceOperatingState> s = objectMapper.readValue(json,
				GenericDeviceOperatingStatus.class);

		assertThat("State", s.getState(), equalTo(DeviceOperatingState.Normal));
		assertThat("State code", s.getStateCode(), equalTo(DeviceOperatingState.Normal.getCode()));
		assertThat("Device states code", s.getDeviceStatesCode(), equalTo(16388));
		assertThat("Device states", s.getDeviceStates(),
				contains(new GenericDeviceOperatingState(3), new GenericDeviceOperatingState(15)));
	}

	@Test
	public void buildWithEnumBuilder() throws Exception {
		DeviceOperatingStatus<FooStates> s = DeviceOperatingStatus.enumBuilder(FooStates.class)
				.withStateCode(1).withDeviceStatesCode(13).build();
		assertThat("State", s.getState(), equalTo(DeviceOperatingState.Normal));
		assertThat("State code", s.getStateCode(), equalTo(DeviceOperatingState.Normal.getCode()));
		assertThat("Device states code", s.getDeviceStatesCode(), equalTo(13));
		assertThat("Device states", s.getDeviceStates(),
				contains(FooStates.Foo, FooStates.Bim, FooStates.Bam));
	}

	private enum FooStates implements Bitmaskable {

		Foo,

		Bar,

		Bim,

		Bam;

		@Override
		public int bitmaskBitOffset() {
			return this.ordinal();
		}

	}

}
