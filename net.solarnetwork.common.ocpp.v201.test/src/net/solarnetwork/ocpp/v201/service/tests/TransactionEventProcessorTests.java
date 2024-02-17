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

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
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
import net.solarnetwork.ocpp.domain.ChargeSessionEndInfo;
import net.solarnetwork.ocpp.domain.ChargeSessionEndReason;
import net.solarnetwork.ocpp.domain.ChargeSessionStartInfo;
import net.solarnetwork.ocpp.service.AuthorizationException;
import net.solarnetwork.ocpp.service.cs.ChargeSessionManager;
import net.solarnetwork.ocpp.v201.domain.Action;
import net.solarnetwork.ocpp.v201.service.TransactionEventProcessor;
import ocpp.v201.AuthorizationStatusEnum;
import ocpp.v201.EVSE;
import ocpp.v201.IdToken;
import ocpp.v201.IdTokenEnum;
import ocpp.v201.IdTokenInfo;
import ocpp.v201.LocationEnum;
import ocpp.v201.MeasurandEnum;
import ocpp.v201.MeterValue;
import ocpp.v201.ReadingContextEnum;
import ocpp.v201.ReasonEnum;
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

		ActionMessage<TransactionEventRequest> message = new BasicActionMessage<>(clientId,
				Action.TransactionEvent, req);
		processor.processActionMessage(message, (msg, res, err) -> {
			assertThat("Message passed", msg, sameInstance(message));
			assertThat("Result available", res, notNullValue());
			assertThat("No error", err, nullValue());

			IdTokenInfo tagInfo = res.getIdTokenInfo();
			assertThat("Result info available", tagInfo, notNullValue());
			assertThat("Result tag status", tagInfo.getStatus(),
					equalTo(AuthorizationStatusEnum.ACCEPTED));

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

		ActionMessage<TransactionEventRequest> message = new BasicActionMessage<>(chargePointId,
				Action.TransactionEvent, req);
		processor.processActionMessage(message, (msg, res, err) -> {
			assertThat("Message passed", msg, sameInstance(message));
			assertThat("Result available", res, notNullValue());
			assertThat("No error", err, nullValue());

			IdTokenInfo tagInfo = res.getIdTokenInfo();
			assertThat("Result info available", tagInfo, notNullValue());
			assertThat("Result tag status", tagInfo.getStatus(),
					equalTo(AuthorizationStatusEnum.INVALID));

			l.countDown();
			return true;
		});

		// THEN
		assertThat("Result handler invoked", l.await(1, TimeUnit.SECONDS), equalTo(true));
	}

	@Test
	public void update_ok() throws InterruptedException {
		// GIVEN
		CountDownLatch l = new CountDownLatch(1);
		ChargePointIdentity clientId = createClientId();
		String idTag = UUID.randomUUID().toString().substring(0, 20);
		String txId = UUID.randomUUID().toString();
		int evseId = 1;
		int connectorId = 2;

		Capture<Iterable<net.solarnetwork.ocpp.domain.SampledValue>> readingsCaptor = Capture
				.newInstance();
		chargeSessionManager.addChargingSessionReadings(eq(clientId), eq(evseId), eq(connectorId),
				capture(readingsCaptor));

		// WHEN
		replayAll();

		SampledValue sv1 = new SampledValue();
		sv1.setContext(ReadingContextEnum.SAMPLE_PERIODIC);
		sv1.setLocation(LocationEnum.OUTLET);
		sv1.setMeasurand(MeasurandEnum.ENERGY_ACTIVE_IMPORT_REGISTER);
		UnitOfMeasure wh = new UnitOfMeasure();
		wh.setUnit("Wh");
		sv1.setUnitOfMeasure(wh);
		sv1.setValue(1234.0);

		SampledValue sv2 = new SampledValue();
		sv2.setContext(ReadingContextEnum.SAMPLE_PERIODIC);
		sv2.setLocation(LocationEnum.OUTLET);
		sv2.setMeasurand(MeasurandEnum.POWER_ACTIVE_IMPORT);
		UnitOfMeasure w = new UnitOfMeasure();
		w.setUnit("W");
		sv2.setUnitOfMeasure(w);
		sv2.setValue(3000.0);

		MeterValue mv = new MeterValue(asList(sv1, sv2),
				ZonedDateTime.of(2020, 02, 14, 10, 0, 0, 0, ZoneOffset.UTC).toInstant());

		EVSE evse = new EVSE(evseId);
		evse.setConnectorId(connectorId);
		Transaction tx = new Transaction(txId);
		IdToken idToken = new IdToken(idTag, IdTokenEnum.LOCAL);
		TransactionEventRequest req = new TransactionEventRequest(TransactionEventEnum.UPDATED,
				Instant.now(), TriggerReasonEnum.TRIGGER, 0, tx);
		req.setIdToken(idToken);
		req.setEvse(evse);
		req.setMeterValue(singletonList(mv));

		ActionMessage<TransactionEventRequest> message = new BasicActionMessage<>(clientId,
				Action.TransactionEvent, req);
		processor.processActionMessage(message, (msg, res, err) -> {
			assertThat("Message passed", msg, sameInstance(message));
			assertThat("Result available", res, notNullValue());
			assertThat("No error", err, nullValue());

			IdTokenInfo tagInfo = res.getIdTokenInfo();
			assertThat("Result info available", tagInfo, notNullValue());
			assertThat("Result tag status", tagInfo.getStatus(),
					equalTo(AuthorizationStatusEnum.ACCEPTED));

			l.countDown();
			return true;
		});

		// THEN
		assertThat("Result handler invoked", l.await(1, TimeUnit.SECONDS), equalTo(true));

		List<net.solarnetwork.ocpp.domain.SampledValue> txData = StreamSupport
				.stream(readingsCaptor.getValue().spliterator(), false).collect(Collectors.toList());
		assertThat("2 sampeld value entities created", txData, hasSize(2));
		for ( int i = 0; i < 2; i++ ) {
			net.solarnetwork.ocpp.domain.SampledValue sve = txData.get(i);
			assertThat("Session ID not available " + i, sve.getSessionId(), is(nullValue()));
			assertThat("Timestatmp " + i, sve.getTimestamp(), equalTo(mv.getTimestamp()));
			assertThat("Reading context translated " + i, sve.getContext(),
					equalTo(net.solarnetwork.ocpp.domain.ReadingContext.SamplePeriodic));
			assertThat("Location translated " + i, sve.getLocation(),
					equalTo(net.solarnetwork.ocpp.domain.Location.Outlet));
			if ( i == 0 ) {
				assertThat("Measurand translated 0", sve.getMeasurand(),
						equalTo(net.solarnetwork.ocpp.domain.Measurand.EnergyActiveImportRegister));
				assertThat("Unit trarnslated 0 ", sve.getUnit(),
						equalTo(net.solarnetwork.ocpp.domain.UnitOfMeasure.Wh));
				assertThat("Value copied 0", sve.getValue(), equalTo("1234"));
			} else {
				assertThat("Measurand translated 1", sve.getMeasurand(),
						equalTo(net.solarnetwork.ocpp.domain.Measurand.PowerActiveImport));
				assertThat("Unit trarnslated 1 ", sve.getUnit(),
						equalTo(net.solarnetwork.ocpp.domain.UnitOfMeasure.W));
				assertThat("Value copied 1", sve.getValue(), equalTo("3000"));
			}
		}
	}

	@Test
	public void stop_ok() throws InterruptedException {
		// GIVEN
		CountDownLatch l = new CountDownLatch(1);
		ChargePointIdentity clientId = createClientId();
		ChargePoint cp = new ChargePoint(UUID.randomUUID().getMostSignificantBits(), Instant.now(),
				new ChargePointInfo(clientId.getIdentifier()));
		String idTag = UUID.randomUUID().toString().substring(0, 20);
		String txId = UUID.randomUUID().toString();

		Capture<ChargeSessionEndInfo> infoCaptor = Capture.newInstance();
		ChargeSession session = new ChargeSession(UUID.randomUUID(), Instant.now(), idTag, cp.getId(), 1,
				txId);
		expect(chargeSessionManager.getActiveChargingSession(clientId, txId)).andReturn(session);

		AuthorizationInfo authInfo = new AuthorizationInfo(idTag, AuthorizationStatus.Accepted, null,
				null);
		expect(chargeSessionManager.endChargingSession(capture(infoCaptor))).andReturn(authInfo);

		// WHEN
		replayAll();
		EVSE evse = new EVSE(1);
		evse.setConnectorId(1);
		Transaction tx = new Transaction(txId);
		tx.setStoppedReason(ReasonEnum.EV_DISCONNECTED);
		IdToken idToken = new IdToken(idTag, IdTokenEnum.LOCAL);
		TransactionEventRequest req = new TransactionEventRequest(TransactionEventEnum.ENDED,
				Instant.now(), TriggerReasonEnum.EV_DEPARTED, 0, tx);
		req.setIdToken(idToken);
		req.setEvse(evse);

		UnitOfMeasure wh = new UnitOfMeasure();
		wh.setUnit("Wh");

		SampledValue sv1 = new SampledValue(12345.0);
		sv1.setContext(ReadingContextEnum.TRANSACTION_END);
		sv1.setLocation(LocationEnum.OUTLET);
		sv1.setMeasurand(MeasurandEnum.ENERGY_ACTIVE_IMPORT_REGISTER);
		sv1.setUnitOfMeasure(wh);

		UnitOfMeasure w = new UnitOfMeasure();
		w.setUnit("W");

		SampledValue sv2 = new SampledValue(3000.0);
		sv2.setContext(ReadingContextEnum.TRANSACTION_END);
		sv2.setLocation(LocationEnum.OUTLET);
		sv2.setMeasurand(MeasurandEnum.POWER_ACTIVE_IMPORT);
		sv2.setUnitOfMeasure(w);

		MeterValue mv = new MeterValue(asList(sv1, sv2), req.getTimestamp());
		req.setMeterValue(singletonList(mv));

		ActionMessage<TransactionEventRequest> message = new BasicActionMessage<>(clientId,
				Action.TransactionEvent, req);
		processor.processActionMessage(message, (msg, res, err) -> {
			assertThat("Message passed", msg, sameInstance(message));
			assertThat("Result available", res, notNullValue());
			assertThat("No error", err, nullValue());

			IdTokenInfo tagInfo = res.getIdTokenInfo();
			assertThat("Result info available", tagInfo, notNullValue());
			assertThat("Result tag status", tagInfo.getStatus(),
					equalTo(AuthorizationStatusEnum.ACCEPTED));

			l.countDown();
			return true;
		});

		// THEN
		assertThat("Result handler invoked", l.await(1, TimeUnit.SECONDS), equalTo(true));

		ChargeSessionEndInfo info = infoCaptor.getValue();
		assertThat("Session auth ID is ID tag", info.getAuthorizationId(), equalTo(idTag));
		assertThat("Session Charge Point ID copied from req", info.getChargePointId(),
				equalTo(clientId));
		assertThat("Connector ID copied from req", info.getTransactionId(), equalTo(txId));
		assertThat("Meter start copied from req", info.getMeterEnd(),
				equalTo(sv1.getValue().longValue()));
		assertThat("Reason copied from req", info.getReason(),
				equalTo(ChargeSessionEndReason.EVDisconnected));
		assertThat("Timestamp copied from req", info.getTimestampEnd(), equalTo(req.getTimestamp()));

		List<net.solarnetwork.ocpp.domain.SampledValue> txData = StreamSupport
				.stream(info.getTransactionData().spliterator(), false).collect(Collectors.toList());
		assertThat("2 sampled value entities created", txData, hasSize(2));
		for ( int i = 0; i < 2; i++ ) {
			net.solarnetwork.ocpp.domain.SampledValue sve = txData.get(i);
			assertThat("Session ID populated " + i, sve.getSessionId(), equalTo(session.getId()));
			assertThat("Timestatmp " + i, sve.getTimestamp(), equalTo(req.getTimestamp()));
			assertThat("Reading context translated " + i, sve.getContext(),
					equalTo(net.solarnetwork.ocpp.domain.ReadingContext.TransactionEnd));
			assertThat("Location translated " + i, sve.getLocation(),
					equalTo(net.solarnetwork.ocpp.domain.Location.Outlet));
			if ( i == 0 ) {
				assertThat("Measurand translated 0", sve.getMeasurand(),
						equalTo(net.solarnetwork.ocpp.domain.Measurand.EnergyActiveImportRegister));
				assertThat("Unit trarnslated 0 ", sve.getUnit(),
						equalTo(net.solarnetwork.ocpp.domain.UnitOfMeasure.Wh));
				assertThat("Value copied 0", sve.getValue(), equalTo("12345"));
			} else {
				assertThat("Measurand translated 1", sve.getMeasurand(),
						equalTo(net.solarnetwork.ocpp.domain.Measurand.PowerActiveImport));
				assertThat("Unit trarnslated 1 ", sve.getUnit(),
						equalTo(net.solarnetwork.ocpp.domain.UnitOfMeasure.W));
				assertThat("Value copied 1", sve.getValue(), equalTo("3000"));
			}
		}
	}

	@Test
	public void stop_notAuthorized() throws InterruptedException {
		// GIVEN
		CountDownLatch l = new CountDownLatch(1);
		ChargePointIdentity chargePointId = createClientId();
		String idTag = UUID.randomUUID().toString().substring(0, 20);
		String txId = UUID.randomUUID().toString();

		expect(chargeSessionManager.getActiveChargingSession(chargePointId, txId))
				.andThrow(new AuthorizationException(
						new AuthorizationInfo(idTag, AuthorizationStatus.Blocked, null, null)));

		// WHEN
		replayAll();
		EVSE evse = new EVSE(1);
		evse.setConnectorId(1);
		Transaction tx = new Transaction(txId);
		IdToken idToken = new IdToken(idTag, IdTokenEnum.LOCAL);
		TransactionEventRequest req = new TransactionEventRequest(TransactionEventEnum.ENDED,
				Instant.now(), TriggerReasonEnum.EV_DEPARTED, 0, tx);
		req.setIdToken(idToken);
		req.setEvse(evse);

		ActionMessage<TransactionEventRequest> message = new BasicActionMessage<>(chargePointId,
				Action.TransactionEvent, req);
		processor.processActionMessage(message, (msg, res, err) -> {
			assertThat("Message passed", msg, sameInstance(message));
			assertThat("Result available", res, notNullValue());
			assertThat("No error", err, nullValue());

			IdTokenInfo tagInfo = res.getIdTokenInfo();
			assertThat("Result info available", tagInfo, notNullValue());
			assertThat("Result tag status", tagInfo.getStatus(),
					equalTo(ocpp.v201.AuthorizationStatusEnum.BLOCKED));

			l.countDown();
			return true;
		});

		// THEN
		assertThat("Result handler invoked", l.await(1, TimeUnit.SECONDS), equalTo(true));
	}

	@Test
	public void stop_transactionNotFound() throws InterruptedException {
		// GIVEN
		CountDownLatch l = new CountDownLatch(1);
		ChargePointIdentity chargePointId = createClientId();
		String idTag = UUID.randomUUID().toString().substring(0, 20);
		String txId = UUID.randomUUID().toString();

		expect(chargeSessionManager.getActiveChargingSession(chargePointId, txId)).andReturn(null);

		// WHEN
		replayAll();
		EVSE evse = new EVSE(1);
		evse.setConnectorId(1);
		Transaction tx = new Transaction(txId);
		IdToken idToken = new IdToken(idTag, IdTokenEnum.LOCAL);
		TransactionEventRequest req = new TransactionEventRequest(TransactionEventEnum.ENDED,
				Instant.now(), TriggerReasonEnum.EV_DEPARTED, 0, tx);
		req.setIdToken(idToken);
		req.setEvse(evse);

		ActionMessage<TransactionEventRequest> message = new BasicActionMessage<>(chargePointId,
				Action.TransactionEvent, req);
		processor.processActionMessage(message, (msg, res, err) -> {
			assertThat("Message passed", msg, sameInstance(message));
			assertThat("Result available", res, notNullValue());
			assertThat("No error", err, nullValue());

			IdTokenInfo tagInfo = res.getIdTokenInfo();
			assertThat("Result info not available", tagInfo, nullValue());

			l.countDown();
			return true;
		});

		// then
		assertThat("Result handler invoked", l.await(1, TimeUnit.SECONDS), equalTo(true));
	}

	@Test
	public void stop_noInfoResult() throws InterruptedException {
		// GIVEN
		CountDownLatch l = new CountDownLatch(1);
		ChargePointIdentity clientId = createClientId();
		ChargePoint cp = new ChargePoint(UUID.randomUUID().getMostSignificantBits(), Instant.now(),
				new ChargePointInfo(clientId.getIdentifier()));
		String idTag = UUID.randomUUID().toString().substring(0, 20);
		String txId = UUID.randomUUID().toString();

		Capture<ChargeSessionEndInfo> infoCaptor = Capture.newInstance();
		ChargeSession session = new ChargeSession(UUID.randomUUID(), Instant.now(), idTag, cp.getId(), 1,
				txId);
		expect(chargeSessionManager.getActiveChargingSession(clientId, txId)).andReturn(session);

		expect(chargeSessionManager.endChargingSession(capture(infoCaptor))).andReturn(null);

		// WHEN
		replayAll();
		EVSE evse = new EVSE(1);
		evse.setConnectorId(1);
		Transaction tx = new Transaction(txId);
		tx.setStoppedReason(ReasonEnum.EV_DISCONNECTED);
		IdToken idToken = new IdToken(idTag, IdTokenEnum.LOCAL);
		TransactionEventRequest req = new TransactionEventRequest(TransactionEventEnum.ENDED,
				Instant.now(), TriggerReasonEnum.EV_DEPARTED, 0, tx);
		req.setIdToken(idToken);
		req.setEvse(evse);

		UnitOfMeasure wh = new UnitOfMeasure();
		wh.setUnit("Wh");

		SampledValue sv1 = new SampledValue(12345.0);
		sv1.setContext(ReadingContextEnum.TRANSACTION_END);
		sv1.setLocation(LocationEnum.OUTLET);
		sv1.setMeasurand(MeasurandEnum.ENERGY_ACTIVE_IMPORT_REGISTER);
		sv1.setUnitOfMeasure(wh);

		MeterValue mv = new MeterValue(singletonList(sv1), req.getTimestamp());
		req.setMeterValue(singletonList(mv));

		ActionMessage<TransactionEventRequest> message = new BasicActionMessage<>(clientId,
				Action.TransactionEvent, req);
		processor.processActionMessage(message, (msg, res, err) -> {
			assertThat("Message passed", msg, sameInstance(message));
			assertThat("Result available", res, notNullValue());
			assertThat("No error", err, nullValue());

			IdTokenInfo tagInfo = res.getIdTokenInfo();
			assertThat("Result info not available", tagInfo, nullValue());

			l.countDown();
			return true;
		});

		// then
		assertThat("Result handler invoked", l.await(1, TimeUnit.SECONDS), equalTo(true));

		ChargeSessionEndInfo info = infoCaptor.getValue();
		assertThat("Session auth ID is ID tag", info.getAuthorizationId(), equalTo(idTag));
		assertThat("Session Charge Point ID copied from req", info.getChargePointId(),
				equalTo(clientId));
		assertThat("Connector ID copied from req", info.getTransactionId(), equalTo(txId));
		assertThat("Meter start copied from req", info.getMeterEnd(),
				equalTo(sv1.getValue().longValue()));
		assertThat("Reason ID copied from req", info.getReason(),
				equalTo(ChargeSessionEndReason.EVDisconnected));
		assertThat("Timestamp copied from req", info.getTimestampEnd(), equalTo(req.getTimestamp()));
	}

}
