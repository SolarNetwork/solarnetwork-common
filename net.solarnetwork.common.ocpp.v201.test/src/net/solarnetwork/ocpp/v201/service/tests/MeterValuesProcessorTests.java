/* ==================================================================
 * MeterValuesProcessorTests.java - 15/02/2020 9:15:12 am
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

import static java.util.Arrays.asList;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.isNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
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
import net.solarnetwork.ocpp.domain.BasicActionMessage;
import net.solarnetwork.ocpp.domain.ChargePointIdentity;
import net.solarnetwork.ocpp.service.cs.ChargeSessionManager;
import net.solarnetwork.ocpp.v201.domain.Action;
import net.solarnetwork.ocpp.v201.service.MeterValuesProcessor;
import ocpp.v201.LocationEnum;
import ocpp.v201.MeasurandEnum;
import ocpp.v201.MeterValue;
import ocpp.v201.MeterValuesRequest;
import ocpp.v201.ReadingContextEnum;
import ocpp.v201.SampledValue;
import ocpp.v201.UnitOfMeasure;

/**
 * Test cases for the {@link MeterValuesProcessor} class.
 * 
 * @author matt
 * @version 1.2
 */
public class MeterValuesProcessorTests {

	private ChargeSessionManager chargeSessionManager;
	private MeterValuesProcessor processor;

	@Before
	public void setup() {
		chargeSessionManager = EasyMock.createMock(ChargeSessionManager.class);
		processor = new MeterValuesProcessor(chargeSessionManager);
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
	public void process_ok() throws InterruptedException {
		// GIVEN
		CountDownLatch l = new CountDownLatch(1);
		ChargePointIdentity clientId = createClientId();
		int evseId = 1;

		Capture<Iterable<net.solarnetwork.ocpp.domain.SampledValue>> readingsCaptor = Capture
				.newInstance();
		chargeSessionManager.addChargingSessionReadings(eq(clientId), eq(evseId), isNull(),
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
		MeterValuesRequest req = new MeterValuesRequest(evseId, asList(mv));
		req.setEvseId(evseId);

		ActionMessage<MeterValuesRequest> message = new BasicActionMessage<MeterValuesRequest>(clientId,
				Action.MeterValues, req);
		processor.processActionMessage(message, (msg, res, err) -> {
			assertThat("Message passed", msg, sameInstance(message));
			assertThat("Result available", res, notNullValue());
			assertThat("No error", err, nullValue());

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
	public void process_noSession() throws InterruptedException {
		// GIVEN
		CountDownLatch l = new CountDownLatch(1);
		ChargePointIdentity clientId = createClientId();
		final int evseId = 1;

		Capture<Iterable<net.solarnetwork.ocpp.domain.SampledValue>> readingsCaptor = Capture
				.newInstance();
		chargeSessionManager.addChargingSessionReadings(eq(clientId), eq(evseId), isNull(),
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
		MeterValuesRequest req = new MeterValuesRequest(evseId, asList(mv));
		req.setEvseId(evseId);

		ActionMessage<MeterValuesRequest> message = new BasicActionMessage<MeterValuesRequest>(clientId,
				Action.MeterValues, req);
		processor.processActionMessage(message, (msg, res, err) -> {
			assertThat("Message passed", msg, sameInstance(message));
			assertThat("Result available", res, notNullValue());
			assertThat("No error", err, nullValue());

			l.countDown();
			return true;
		});

		// THEN
		assertThat("Result handler invoked", l.await(1, TimeUnit.SECONDS), equalTo(true));

		List<net.solarnetwork.ocpp.domain.SampledValue> txData = StreamSupport
				.stream(readingsCaptor.getValue().spliterator(), false).collect(Collectors.toList());
		assertThat("2 sampled value entities created", txData, hasSize(2));
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
				assertThat("Unit translated 0 ", sve.getUnit(),
						equalTo(net.solarnetwork.ocpp.domain.UnitOfMeasure.Wh));
				assertThat("Value copied 0", sve.getValue(), equalTo("1234"));
			} else {
				assertThat("Measurand translated 1", sve.getMeasurand(),
						equalTo(net.solarnetwork.ocpp.domain.Measurand.PowerActiveImport));
				assertThat("Unit translated 1 ", sve.getUnit(),
						equalTo(net.solarnetwork.ocpp.domain.UnitOfMeasure.W));
				assertThat("Value copied 1", sve.getValue(), equalTo("3000"));
			}
		}
	}

}
