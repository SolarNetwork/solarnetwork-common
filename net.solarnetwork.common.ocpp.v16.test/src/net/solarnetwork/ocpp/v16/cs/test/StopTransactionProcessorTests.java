/* ==================================================================
 * StopTransactionProcessorTests.java - 14/02/2020 4:06:01 pm
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

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.expect;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import java.time.Instant;
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
import net.solarnetwork.ocpp.service.AuthorizationException;
import net.solarnetwork.ocpp.service.cs.ChargeSessionManager;
import net.solarnetwork.ocpp.v16.cs.StopTransactionProcessor;
import ocpp.domain.ErrorHolder;
import ocpp.v16.ActionErrorCode;
import ocpp.v16.CentralSystemAction;
import ocpp.v16.cs.IdTagInfo;
import ocpp.v16.cs.Location;
import ocpp.v16.cs.Measurand;
import ocpp.v16.cs.MeterValue;
import ocpp.v16.cs.ReadingContext;
import ocpp.v16.cs.Reason;
import ocpp.v16.cs.SampledValue;
import ocpp.v16.cs.StopTransactionRequest;
import ocpp.v16.cs.UnitOfMeasure;
import ocpp.xml.support.XmlDateUtils;

/**
 * Test cases for the {@link StopTransactionProcessor} class.
 * 
 * @author matt
 * @version 1.0
 */
public class StopTransactionProcessorTests {

	private ChargeSessionManager chargeSessionManager;
	private StopTransactionProcessor processor;

	@Before
	public void setup() {
		chargeSessionManager = EasyMock.createMock(ChargeSessionManager.class);
		processor = new StopTransactionProcessor(chargeSessionManager);
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
	public void stop_ok() throws InterruptedException {
		// given
		CountDownLatch l = new CountDownLatch(1);
		ChargePointIdentity clientId = createClientId();
		ChargePoint cp = new ChargePoint(UUID.randomUUID().getMostSignificantBits(), Instant.now(),
				new ChargePointInfo(clientId.getIdentifier()));
		String idTag = UUID.randomUUID().toString().substring(0, 20);
		int transactionId = 1;

		Capture<ChargeSessionEndInfo> infoCaptor = new Capture<>();
		ChargeSession session = new ChargeSession(UUID.randomUUID(), Instant.now(), idTag, cp.getId(), 1,
				2);
		expect(chargeSessionManager.getActiveChargingSession(clientId, transactionId))
				.andReturn(session);

		AuthorizationInfo authInfo = new AuthorizationInfo(idTag, AuthorizationStatus.Accepted, null,
				null);
		expect(chargeSessionManager.endChargingSession(capture(infoCaptor))).andReturn(authInfo);

		// when
		replayAll();
		StopTransactionRequest req = new StopTransactionRequest();
		req.setIdTag(idTag);
		req.setTransactionId(transactionId);
		req.setTimestamp(XmlDateUtils.newXmlCalendar());
		req.setMeterStop(12345);
		req.setReason(Reason.LOCAL);

		MeterValue mv = new MeterValue();
		mv.setTimestamp(XmlDateUtils.newXmlCalendar(2020, 02, 14, 10, 0, 0, 0));
		SampledValue sv = new SampledValue();
		sv.setContext(ReadingContext.SAMPLE_PERIODIC);
		sv.setLocation(Location.OUTLET);
		sv.setMeasurand(Measurand.ENERGY_ACTIVE_IMPORT_REGISTER);
		sv.setUnit(UnitOfMeasure.WH);
		sv.setValue("1234");
		mv.getSampledValue().add(sv);
		sv = new SampledValue();
		sv.setContext(ReadingContext.SAMPLE_PERIODIC);
		sv.setLocation(Location.OUTLET);
		sv.setMeasurand(Measurand.POWER_ACTIVE_IMPORT);
		sv.setUnit(UnitOfMeasure.W);
		sv.setValue("3000");
		mv.getSampledValue().add(sv);
		req.getTransactionData().add(mv);

		ActionMessage<StopTransactionRequest> message = new BasicActionMessage<StopTransactionRequest>(
				clientId, CentralSystemAction.StopTransaction, req);
		processor.processActionMessage(message, (msg, res, err) -> {
			assertThat("Message passed", msg, sameInstance(message));
			assertThat("Result available", res, notNullValue());
			assertThat("No error", err, nullValue());

			IdTagInfo tagInfo = res.getIdTagInfo();
			assertThat("Result info available", tagInfo, notNullValue());
			assertThat("Result tag status", tagInfo.getStatus(),
					equalTo(ocpp.v16.cs.AuthorizationStatus.ACCEPTED));

			l.countDown();
			return true;
		});

		// then
		assertThat("Result handler invoked", l.await(1, TimeUnit.SECONDS), equalTo(true));

		ChargeSessionEndInfo info = infoCaptor.getValue();
		assertThat("Session auth ID is ID tag", info.getAuthorizationId(), equalTo(idTag));
		assertThat("Session Charge Point ID copied from req", info.getChargePointId(),
				equalTo(clientId));
		assertThat("Connector ID copied from req", info.getTransactionId(),
				equalTo(req.getTransactionId()));
		assertThat("Meter start copied from req", info.getMeterEnd(),
				equalTo((long) req.getMeterStop()));
		assertThat("Reservation ID copied from req", info.getReason(),
				equalTo(ChargeSessionEndReason.Local));
		assertThat("Timestamp copied from req", info.getTimestampEnd(),
				equalTo(XmlDateUtils.timestamp(req.getTimestamp(), null)));

		List<net.solarnetwork.ocpp.domain.SampledValue> txData = StreamSupport
				.stream(info.getTransactionData().spliterator(), false).collect(Collectors.toList());
		assertThat("2 sampeld value entities created", txData, hasSize(2));
		for ( int i = 0; i < 2; i++ ) {
			net.solarnetwork.ocpp.domain.SampledValue sve = txData.get(i);
			assertThat("Session ID populated " + i, sve.getSessionId(), equalTo(session.getId()));
			assertThat("Timestatmp " + i, sve.getTimestamp(),
					equalTo(XmlDateUtils.timestamp(mv.getTimestamp(), null)));
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
	public void stop_notAuthorized() throws InterruptedException {
		// given
		CountDownLatch l = new CountDownLatch(1);
		ChargePointIdentity chargePointId = createClientId();
		String idTag = UUID.randomUUID().toString().substring(0, 20);
		int transactionId = 1;

		expect(chargeSessionManager.getActiveChargingSession(chargePointId, transactionId))
				.andThrow(new AuthorizationException(
						new AuthorizationInfo(idTag, AuthorizationStatus.Blocked, null, null)));

		// when
		replayAll();
		StopTransactionRequest req = new StopTransactionRequest();
		req.setIdTag(idTag);
		req.setTransactionId(transactionId);
		req.setTimestamp(XmlDateUtils.newXmlCalendar());
		req.setMeterStop(12345);
		req.setReason(Reason.LOCAL);

		ActionMessage<StopTransactionRequest> message = new BasicActionMessage<StopTransactionRequest>(
				chargePointId, CentralSystemAction.StopTransaction, req);
		processor.processActionMessage(message, (msg, res, err) -> {
			assertThat("Message passed", msg, sameInstance(message));
			assertThat("Result available", res, notNullValue());
			assertThat("No error", err, nullValue());

			IdTagInfo tagInfo = res.getIdTagInfo();
			assertThat("Result info available", tagInfo, notNullValue());
			assertThat("Result tag status", tagInfo.getStatus(),
					equalTo(ocpp.v16.cs.AuthorizationStatus.BLOCKED));

			l.countDown();
			return true;
		});

		// then
		assertThat("Result handler invoked", l.await(1, TimeUnit.SECONDS), equalTo(true));
	}

	@Test
	public void stop_transactionNotFound() throws InterruptedException {
		// given
		CountDownLatch l = new CountDownLatch(1);
		ChargePointIdentity chargePointId = createClientId();
		String idTag = UUID.randomUUID().toString().substring(0, 20);
		int transactionId = 1;

		expect(chargeSessionManager.getActiveChargingSession(chargePointId, transactionId))
				.andReturn(null);

		// when
		replayAll();
		StopTransactionRequest req = new StopTransactionRequest();
		req.setIdTag(idTag);
		req.setTransactionId(transactionId);
		req.setTimestamp(XmlDateUtils.newXmlCalendar());
		req.setMeterStop(12345);
		req.setReason(Reason.LOCAL);

		ActionMessage<StopTransactionRequest> message = new BasicActionMessage<StopTransactionRequest>(
				chargePointId, CentralSystemAction.StopTransaction, req);
		processor.processActionMessage(message, (msg, res, err) -> {
			assertThat("Message passed", msg, sameInstance(message));
			assertThat("Result available", res, nullValue());
			assertThat("Error happened", err, instanceOf(ErrorHolder.class));

			ErrorHolder error = (ErrorHolder) err;
			assertThat("Is PropertyConstraintViolation error", error.getErrorCode(),
					equalTo(ActionErrorCode.PropertyConstraintViolation));

			l.countDown();
			return true;
		});

		// then
		assertThat("Result handler invoked", l.await(1, TimeUnit.SECONDS), equalTo(true));
	}

}
