/* ==================================================================
 * HeartbeatProcessorTests.java - 5/02/2020 2:57:02 pm
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

package net.solarnetwork.ocpp.v201.service.tests;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import net.solarnetwork.ocpp.domain.BasicActionMessage;
import net.solarnetwork.ocpp.domain.ChargePointIdentity;
import net.solarnetwork.ocpp.v201.domain.Action;
import net.solarnetwork.ocpp.v201.service.HeartbeatProcessor;
import ocpp.v201.HeartbeatRequest;

/**
 * Test cases for the {@link HeartbeatProcessor} class.
 * 
 * @author matt
 * @version 1.0
 */
public class HeartbeatProcessorTests {

	private ChargePointIdentity createClientId(String identifier) {
		return new ChargePointIdentity(identifier, UUID.randomUUID().toString());
	}

	@Test
	public void ok() throws InterruptedException {
		// GIVEN
		final Clock fixed = Clock.fixed(Instant.now(), ZoneOffset.UTC);
		final HeartbeatProcessor p = new HeartbeatProcessor(fixed);
		final CountDownLatch l = new CountDownLatch(1);

		// WHEN
		HeartbeatRequest req = new HeartbeatRequest();
		BasicActionMessage<HeartbeatRequest> act = new BasicActionMessage<>(createClientId("foo"),
				Action.Heartbeat, req);
		p.processActionMessage(act, (msg, res, err) -> {
			assertThat("Message preserved", msg, is(sameInstance(act)));
			assertThat("Error not present", err, is(nullValue()));
			assertThat("Response provided", res, is(notNullValue()));
			assertThat("Response currentTime is from fixed clock", res.getCurrentTime(),
					is(equalTo(fixed.instant())));
			l.countDown();
			return true;
		});

		// THEN
		assertThat("Result handler invoked", l.await(1, TimeUnit.SECONDS), equalTo(true));
	}

	@Test
	public void nullRequest() throws InterruptedException {
		// GIVEN
		final Clock fixed = Clock.fixed(Instant.now(), ZoneOffset.UTC);
		final HeartbeatProcessor p = new HeartbeatProcessor(fixed);
		final CountDownLatch l = new CountDownLatch(1);

		// when
		HeartbeatRequest req = new HeartbeatRequest();
		BasicActionMessage<HeartbeatRequest> act = new BasicActionMessage<>(createClientId("foo"),
				Action.Heartbeat, req);
		p.processActionMessage(act, (msg, res, err) -> {
			assertThat("Message preserved", msg, is(sameInstance(act)));
			assertThat("Error not present", err, is(nullValue()));
			assertThat("Response provided", res, is(notNullValue()));
			assertThat("Response currentTime is from fixed clock", res.getCurrentTime(),
					is(equalTo(fixed.instant())));
			l.countDown();
			return true;
		});

		// then
		assertThat("Result handler invoked", l.await(1, TimeUnit.SECONDS), equalTo(true));
	}

	@Test(expected = NullPointerException.class)
	public void nullHandler() {
		new HeartbeatProcessor(Clock.systemDefaultZone()).processActionMessage(null, null);
	}

}
