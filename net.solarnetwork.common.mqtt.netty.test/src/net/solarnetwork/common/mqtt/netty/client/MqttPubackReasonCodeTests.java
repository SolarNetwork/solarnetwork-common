/* ==================================================================
 * MqttPubackReasonCodeTests.java - 28/09/2026 6:45:00 pm
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

package net.solarnetwork.common.mqtt.netty.client;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import org.junit.Test;

/**
 * Test cases for the {@link MqttPubackReasonCode} class.
 *
 * @author matt
 * @version 1.0
 */
public class MqttPubackReasonCodeTests {

	@Test
	public void isError_success() {
		assertThat("Success is not an error", MqttPubackReasonCode.isError((byte) 0x00), is(false));
	}

	@Test
	public void isError_noSubscribers() {
		// MQTT 5 reason codes below 0x80 are success codes: a message accepted by the server
		// that matched no subscription is still delivered successfully
		assertThat("No matching subscribers is not an error",
				MqttPubackReasonCode.isError(MqttPubackReasonCode.NoSubscribers.getCode()), is(false));
	}

	@Test
	public void isError_allNonErrorCodes() {
		for ( int code = 0x00; code < 0x80; code++ ) {
			assertThat("Code " + code + " is not an error", MqttPubackReasonCode.isError((byte) code),
					is(false));
		}
	}

	@Test
	public void isError_allErrorCodes() {
		for ( int code = 0x80; code < 0x100; code++ ) {
			assertThat("Code " + code + " is an error", MqttPubackReasonCode.isError((byte) code),
					is(true));
		}
	}

	@Test
	public void forCode_noSubscribers() {
		assertThat("No subscribers decoded", MqttPubackReasonCode.forCode((byte) 0x10),
				is(equalTo(MqttPubackReasonCode.NoSubscribers)));
	}

	@Test
	public void forCode_notAuthorized() {
		assertThat("Not authorized decoded", MqttPubackReasonCode.forCode((byte) 0x87),
				is(equalTo(MqttPubackReasonCode.NotAuthorized)));
	}
}
