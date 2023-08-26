/* ==================================================================
 * StartTransactionProcessorTests.java - 14/02/2020 2:32:54 pm
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

package net.solarnetwork.ocpp.v16.cs.test;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.expect;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import net.solarnetwork.ocpp.domain.ActionMessage;
import net.solarnetwork.ocpp.domain.AuthorizationInfo;
import net.solarnetwork.ocpp.domain.AuthorizationStatus;
import net.solarnetwork.ocpp.domain.BasicActionMessage;
import net.solarnetwork.ocpp.domain.ChargePoint;
import net.solarnetwork.ocpp.domain.ChargePointIdentity;
import net.solarnetwork.ocpp.domain.ChargePointInfo;
import net.solarnetwork.ocpp.domain.ChargeSession;
import net.solarnetwork.ocpp.domain.ChargeSessionStartInfo;
import net.solarnetwork.ocpp.service.AuthorizationException;
import net.solarnetwork.ocpp.service.cs.ChargeSessionManager;
import net.solarnetwork.ocpp.v16.cs.StartTransactionProcessor;
import ocpp.v16.CentralSystemAction;
import ocpp.v16.cs.IdTagInfo;
import ocpp.v16.cs.StartTransactionRequest;
import ocpp.xml.support.XmlDateUtils;

/**
 * Test cases for the {@link StartTransactionProcessor} class.
 * 
 * @author matt
 * @version 1.0
 */
public class StartTransactionProcessorTests {

	private ChargeSessionManager chargeSessionManager;
	private StartTransactionProcessor processor;

	@Before
	public void setup() {
		chargeSessionManager = EasyMock.createMock(ChargeSessionManager.class);
		processor = new StartTransactionProcessor(chargeSessionManager);
	}

	@After
	public void teardown() {
		EasyMock.verify(chargeSessionManager);
	}

	private void replayAll() {
		EasyMock.replay(chargeSessionManager);
	}

	private ChargePointIdentity createClientId() {
		return new ChargePointIdentity(UUID.randomUUID().toString(), UUID.randomUUID().toString());
	}

	@Test
	public void start_ok() throws InterruptedException {
		// given
		CountDownLatch l = new CountDownLatch(1);
		ChargePointIdentity clientId = createClientId();
		ChargePoint cp = new ChargePoint(UUID.randomUUID().getMostSignificantBits(), Instant.now(),
				new ChargePointInfo(clientId.getIdentifier()));
		String idTag = UUID.randomUUID().toString().substring(0, 20);

		Capture<ChargeSessionStartInfo> infoCaptor = Capture.newInstance();
		ChargeSession session = new ChargeSession(UUID.randomUUID(), Instant.now(), idTag, cp.getId(), 1,
				2);
		expect(chargeSessionManager.startChargingSession(capture(infoCaptor))).andReturn(session);

		// when
		replayAll();
		StartTransactionRequest req = new StartTransactionRequest();
		req.setIdTag(idTag);
		req.setConnectorId(1);
		req.setTimestamp(XmlDateUtils.newXmlCalendar());
		req.setMeterStart(12345);
		ActionMessage<StartTransactionRequest> message = new BasicActionMessage<StartTransactionRequest>(
				clientId, CentralSystemAction.StartTransaction, req);
		processor.processActionMessage(message, (msg, res, err) -> {
			assertThat("Message passed", msg, sameInstance(message));
			assertThat("Result available", res, notNullValue());
			assertThat("No error", err, nullValue());

			IdTagInfo tagInfo = res.getIdTagInfo();
			assertThat("Result info available", tagInfo, notNullValue());
			assertThat("Result tag status", tagInfo.getStatus(),
					equalTo(ocpp.v16.cs.AuthorizationStatus.ACCEPTED));
			assertThat("Result transaction ID", res.getTransactionId(),
					equalTo(session.getTransactionId()));

			l.countDown();
			return true;
		});

		// then
		assertThat("Result handler invoked", l.await(1, TimeUnit.SECONDS), equalTo(true));

		ChargeSessionStartInfo info = infoCaptor.getValue();
		assertThat("Session auth ID is ID tag", info.getAuthorizationId(), equalTo(idTag));
		assertThat("Session Charge Point ID copied from req", info.getChargePointId(),
				equalTo(clientId));
		assertThat("Connector ID copied from req", info.getConnectorId(), equalTo(req.getConnectorId()));
		assertThat("Meter start copied from req", info.getMeterStart(),
				equalTo((long) req.getMeterStart()));
		assertThat("Reservation ID copied from req", info.getReservationId(),
				equalTo(req.getReservationId()));
		assertThat("Timestamp copied from req", info.getTimestampStart(),
				equalTo(XmlDateUtils.timestamp(req.getTimestamp(), null)));
	}

	@Test
	public void start_notAuthorized() throws InterruptedException {
		// given
		CountDownLatch l = new CountDownLatch(1);
		ChargePointIdentity chargePointId = createClientId();
		String idTag = UUID.randomUUID().toString().substring(0, 20);

		expect(chargeSessionManager.startChargingSession(anyObject()))
				.andThrow(new AuthorizationException(
						new AuthorizationInfo(idTag, AuthorizationStatus.Invalid, null, null)));

		// when
		replayAll();
		StartTransactionRequest req = new StartTransactionRequest();
		req.setIdTag(idTag);
		req.setConnectorId(1);
		req.setTimestamp(XmlDateUtils.newXmlCalendar());
		req.setMeterStart(12345);
		ActionMessage<StartTransactionRequest> message = new BasicActionMessage<StartTransactionRequest>(
				chargePointId, CentralSystemAction.StartTransaction, req);
		processor.processActionMessage(message, (msg, res, err) -> {
			assertThat("Message passed", msg, sameInstance(message));
			assertThat("Result available", res, notNullValue());
			assertThat("No error", err, nullValue());

			IdTagInfo tagInfo = res.getIdTagInfo();
			assertThat("Result info available", tagInfo, notNullValue());
			assertThat("Result tag status", tagInfo.getStatus(),
					equalTo(ocpp.v16.cs.AuthorizationStatus.INVALID));
			assertThat("Result transaction ID", res.getTransactionId(), equalTo(0));

			l.countDown();
			return true;
		});

		// then
		assertThat("Result handler invoked", l.await(1, TimeUnit.SECONDS), equalTo(true));
	}

	@Test
	public void start_notAuthorized_explicitTransactionId() throws InterruptedException {
		// given
		CountDownLatch l = new CountDownLatch(1);
		ChargePointIdentity chargePointId = createClientId();
		String idTag = UUID.randomUUID().toString().substring(0, 20);

		Integer txId = new SecureRandom().nextInt(60_000) + 1;

		expect(chargeSessionManager.startChargingSession(anyObject()))
				.andThrow(new AuthorizationException(
						new AuthorizationInfo(idTag, AuthorizationStatus.Invalid, null, null), txId));

		// when
		replayAll();
		StartTransactionRequest req = new StartTransactionRequest();
		req.setIdTag(idTag);
		req.setConnectorId(1);
		req.setTimestamp(XmlDateUtils.newXmlCalendar());
		req.setMeterStart(12345);
		ActionMessage<StartTransactionRequest> message = new BasicActionMessage<StartTransactionRequest>(
				chargePointId, CentralSystemAction.StartTransaction, req);
		processor.processActionMessage(message, (msg, res, err) -> {
			assertThat("Message passed", msg, sameInstance(message));
			assertThat("Result available", res, notNullValue());
			assertThat("No error", err, nullValue());

			IdTagInfo tagInfo = res.getIdTagInfo();
			assertThat("Result info available", tagInfo, notNullValue());
			assertThat("Result tag status", tagInfo.getStatus(),
					equalTo(ocpp.v16.cs.AuthorizationStatus.INVALID));
			assertThat("Result transaction ID", res.getTransactionId(), equalTo(txId));

			l.countDown();
			return true;
		});

		// then
		assertThat("Result handler invoked", l.await(1, TimeUnit.SECONDS), equalTo(true));
	}

}
