/* ==================================================================
 * TransactionEventProcessorTests.java - 16/02/2024 5:49:45 pm
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

import static java.util.Collections.singletonList;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.expect;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
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
import net.solarnetwork.ocpp.v201.domain.Action;
import net.solarnetwork.ocpp.v201.service.TransactionEventProcessor;
import ocpp.v201.EVSE;
import ocpp.v201.IdToken;
import ocpp.v201.IdTokenEnum;
import ocpp.v201.IdTokenInfo;
import ocpp.v201.LocationEnum;
import ocpp.v201.MeasurandEnum;
import ocpp.v201.MeterValue;
import ocpp.v201.ReadingContextEnum;
import ocpp.v201.SampledValue;
import ocpp.v201.Transaction;
import ocpp.v201.TransactionEventEnum;
import ocpp.v201.TransactionEventRequest;
import ocpp.v201.TriggerReasonEnum;
import ocpp.v201.UnitOfMeasure;

/**
 * Test cases for the {@link TransactionEventProcessor} class.
 * 
 * @author matt
 * @version 1.0
 */
public class TransactionEventProcessorTests {

	private ChargeSessionManager chargeSessionManager;
	private TransactionEventProcessor processor;

	@Before
	public void setup() {
		chargeSessionManager = EasyMock.createMock(ChargeSessionManager.class);
		processor = new TransactionEventProcessor(chargeSessionManager);
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
		// GIVEN
		CountDownLatch l = new CountDownLatch(1);
		ChargePointIdentity clientId = createClientId();
		ChargePoint cp = new ChargePoint(UUID.randomUUID().getMostSignificantBits(), Instant.now(),
				new ChargePointInfo(clientId.getIdentifier()));
		String idTag = UUID.randomUUID().toString().substring(0, 20);
		String txId = UUID.randomUUID().toString();

		Capture<ChargeSessionStartInfo> infoCaptor = Capture.newInstance();
		ChargeSession session = new ChargeSession(UUID.randomUUID(), Instant.now(), idTag, cp.getId(), 1,
				txId);
		expect(chargeSessionManager.startChargingSession(capture(infoCaptor))).andReturn(session);

		// WHEN
		replayAll();
		EVSE evse = new EVSE(1);
		evse.setConnectorId(1);
		Transaction tx = new Transaction(txId);
		IdToken idToken = new IdToken(idTag, IdTokenEnum.LOCAL);
		TransactionEventRequest req = new TransactionEventRequest(TransactionEventEnum.STARTED,
				Instant.now(), TriggerReasonEnum.AUTHORIZED, 0, tx);
		req.setIdToken(idToken);
		req.setEvse(evse);

		UnitOfMeasure wh = new UnitOfMeasure();
		wh.setUnit("Wh");

		SampledValue meterStart = new SampledValue(12345.0);
		meterStart.setContext(ReadingContextEnum.TRANSACTION_BEGIN);
		meterStart.setLocation(LocationEnum.OUTLET);
		meterStart.setMeasurand(MeasurandEnum.ENERGY_ACTIVE_IMPORT_REGISTER);
		meterStart.setUnitOfMeasure(wh);

		req.setMeterValue(singletonList(new MeterValue(singletonList(meterStart), req.getTimestamp())));

		ActionMessage<TransactionEventRequest> message = new BasicActionMessage<TransactionEventRequest>(
				clientId, Action.TransactionEvent, req);
		processor.processActionMessage(message, (msg, res, err) -> {
			assertThat("Message passed", msg, sameInstance(message));
			assertThat("Result available", res, notNullValue());
			assertThat("No error", err, nullValue());

			IdTokenInfo tagInfo = res.getIdTokenInfo();
			assertThat("Result info available", tagInfo, notNullValue());
			assertThat("Result tag status", tagInfo.getStatus(),
					equalTo(ocpp.v201.AuthorizationStatusEnum.ACCEPTED));

			l.countDown();
			return true;
		});

		// THEN
		assertThat("Result handler invoked", l.await(1, TimeUnit.SECONDS), equalTo(true));

		ChargeSessionStartInfo info = infoCaptor.getValue();
		assertThat("Session auth ID is ID tag", info.getAuthorizationId(), equalTo(idTag));
		assertThat("Session Charge Point ID copied from req", info.getChargePointId(),
				equalTo(clientId));
		assertThat("EVSE ID copied from req", info.getEvseId(), equalTo(evse.getId()));
		assertThat("Connector ID copied from req", info.getConnectorId(),
				equalTo(evse.getConnectorId()));
		assertThat("Meter start copied from req", info.getMeterStart(),
				equalTo(meterStart.getValue().longValue()));
		assertThat("Reservation ID copied from req", info.getReservationId(),
				equalTo(req.getReservationId()));
		assertThat("Timestamp copied from req", info.getTimestampStart(), equalTo(req.getTimestamp()));
	}

	@Test
	public void start_notAuthorized() throws InterruptedException {
		// GIVEN
		CountDownLatch l = new CountDownLatch(1);
		ChargePointIdentity chargePointId = createClientId();
		String idTag = UUID.randomUUID().toString().substring(0, 20);
		String txId = UUID.randomUUID().toString();

		expect(chargeSessionManager.startChargingSession(anyObject()))
				.andThrow(new AuthorizationException(
						new AuthorizationInfo(idTag, AuthorizationStatus.Invalid, null, null)));

		// when
		replayAll();
		EVSE evse = new EVSE(1);
		evse.setConnectorId(1);
		Transaction tx = new Transaction(txId);
		IdToken idToken = new IdToken(idTag, IdTokenEnum.LOCAL);
		TransactionEventRequest req = new TransactionEventRequest(TransactionEventEnum.STARTED,
				Instant.now(), TriggerReasonEnum.AUTHORIZED, 0, tx);
		req.setIdToken(idToken);
		req.setEvse(evse);

		ActionMessage<TransactionEventRequest> message = new BasicActionMessage<TransactionEventRequest>(
				chargePointId, Action.TransactionEvent, req);
		processor.processActionMessage(message, (msg, res, err) -> {
			assertThat("Message passed", msg, sameInstance(message));
			assertThat("Result available", res, notNullValue());
			assertThat("No error", err, nullValue());

			IdTokenInfo tagInfo = res.getIdTokenInfo();
			assertThat("Result info available", tagInfo, notNullValue());
			assertThat("Result tag status", tagInfo.getStatus(),
					equalTo(ocpp.v201.AuthorizationStatusEnum.INVALID));

			l.countDown();
			return true;
		});

		// then
		assertThat("Result handler invoked", l.await(1, TimeUnit.SECONDS), equalTo(true));
	}

}
