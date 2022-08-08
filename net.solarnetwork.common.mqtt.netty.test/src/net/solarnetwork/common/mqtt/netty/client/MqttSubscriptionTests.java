/* ==================================================================
 * MqttSubscriptionTests.java - 9/08/2022 10:08:33 am
 * 
 * Copyright 2022 SolarNetwork.net Dev Team
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
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import net.solarnetwork.common.mqtt.MqttMessageHandler;

/**
 * Test cases for {@link MqttSubscription}
 * 
 * @author matt
 * @version 1.0
 */
public class MqttSubscriptionTests {

	private MqttMessageHandler handler;

	@Before
	public void setup() {
		handler = EasyMock.createMock(MqttMessageHandler.class);
	}

	@After
	public void teardown() {
		EasyMock.verify(handler);
	}

	private void replayAll() {
		EasyMock.replay(handler);
	}

	@Test
	public void matchesTopic_wildcardSegment() {
		// GIVEN
		MqttSubscription sub = new MqttSubscription("foo/+/bar", handler, false);

		// WHEN
		replayAll();

		// THEN
		assertThat("Topic matches wildcard", sub.matches("foo/1/bar"), is(equalTo(true)));
		assertThat("Topic does not match wildcard", sub.matches("foo/bar"), is(equalTo(false)));
	}

	@Test
	public void matchesTopic_sharedSubscription_wildcardSegment() {
		// GIVEN
		MqttSubscription sub = new MqttSubscription("$share/GROUP1/foo/+/bar", handler, false);

		// WHEN
		replayAll();

		// THEN
		assertThat("Topic matches wildcard", sub.matches("foo/1/bar"), is(equalTo(true)));
		assertThat("Topic does not match wildcard", sub.matches("foo/bar"), is(equalTo(false)));
	}

}
