/* ==================================================================
 * StatusNotificationTests.java - 17/02/2024 6:33:07 pm
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

package net.solarnetwork.ocpp.v201.service.tests;

import static java.util.UUID.randomUUID;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.eq;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import net.solarnetwork.ocpp.domain.ActionMessage;
import net.solarnetwork.ocpp.domain.BasicActionMessage;
import net.solarnetwork.ocpp.domain.ChargePointIdentity;
import net.solarnetwork.ocpp.domain.StatusNotification;
import net.solarnetwork.ocpp.service.cs.ChargePointManager;
import net.solarnetwork.ocpp.v201.domain.Action;
import net.solarnetwork.ocpp.v201.service.StatusNotificationProcessor;
import ocpp.v201.ConnectorStatusEnum;
import ocpp.v201.StatusNotificationRequest;

/**
 * Test cases for the {@link StatusNotificationProcessor} class.
 *
 * @author matt
 * @version 1.0
 * @since 1.1
 */
public class StatusNotificationProcessorTests {

	private ChargePointManager chargePointManager;
	private StatusNotificationProcessor processor;

	@Before
	public void setup() {
		chargePointManager = EasyMock.createMock(ChargePointManager.class);
		processor = new StatusNotificationProcessor(chargePointManager);
	}

	@After
	public void teardown() {
		EasyMock.verify(chargePointManager);
	}

	private void replayAll() {
		EasyMock.replay(chargePointManager);
	}

	private ChargePointIdentity createClientId() {
		return new ChargePointIdentity(randomUUID().toString(), randomUUID().toString());
	}

	@Test
	public void saveStatus() throws Exception {
		// GIVEN
		CountDownLatch l = new CountDownLatch(1);
		ChargePointIdentity identity = createClientId();

		Capture<StatusNotification> notifCaptor = Capture.newInstance();
		chargePointManager.updateChargePointStatus(eq(identity), capture(notifCaptor));

		// WHEN
		replayAll();

		StatusNotificationRequest req = new StatusNotificationRequest(Instant.now(),
				ConnectorStatusEnum.OCCUPIED, 1, 2);
		ActionMessage<StatusNotificationRequest> message = new BasicActionMessage<>(identity,
				Action.StatusNotification, req);
		processor.processActionMessage(message, (msg, res, err) -> {
			assertThat("Message passed", msg, sameInstance(message));
			assertThat("Result available", res, notNullValue());
			assertThat("No error", err, nullValue());
			l.countDown();
			return true;
		});

		// then
		assertThat("Result handler invoked", l.await(1, TimeUnit.SECONDS), equalTo(true));
		StatusNotification notif = notifCaptor.getValue();
		assertThat("EVSE ID matches request", notif.getEvseId(), equalTo(req.getEvseId()));
		assertThat("Connector ID matches request", notif.getConnectorId(),
				equalTo(req.getConnectorId()));
		assertThat("Status matches request", notif.getStatus(),
				equalTo(net.solarnetwork.ocpp.domain.ChargePointStatus.Occupied));
	}

}
