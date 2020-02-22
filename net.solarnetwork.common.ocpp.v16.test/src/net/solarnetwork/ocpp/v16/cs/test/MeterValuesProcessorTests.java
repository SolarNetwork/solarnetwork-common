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

package net.solarnetwork.ocpp.v16.cs.test;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.expect;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
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
import net.solarnetwork.ocpp.domain.BasicActionMessage;
import net.solarnetwork.ocpp.domain.ChargeSession;
import net.solarnetwork.ocpp.service.cs.ChargeSessionManager;
import net.solarnetwork.ocpp.v16.cs.MeterValuesProcessor;
import ocpp.v16.CentralSystemAction;
import ocpp.v16.cs.Location;
import ocpp.v16.cs.Measurand;
import ocpp.v16.cs.MeterValue;
import ocpp.v16.cs.MeterValuesRequest;
import ocpp.v16.cs.ReadingContext;
import ocpp.v16.cs.SampledValue;
import ocpp.v16.cs.UnitOfMeasure;
import ocpp.xml.support.XmlDateUtils;

/**
 * Test cases for the {@link MeterValuesProcessor} class.
 * 
 * @author matt
 * @version 1.0
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

	@Test
	public void process_ok() throws InterruptedException {
		// given
		CountDownLatch l = new CountDownLatch(1);
		String chargePointId = UUID.randomUUID().toString();
		String idTag = UUID.randomUUID().toString().substring(0, 20);
		int transactionId = 1;

		ChargeSession session = new ChargeSession(UUID.randomUUID(), Instant.now(), idTag, chargePointId,
				1, transactionId);
		expect(chargeSessionManager.getActiveChargingSession(chargePointId, transactionId))
				.andReturn(session);

		Capture<Iterable<net.solarnetwork.ocpp.domain.SampledValue>> readingsCaptor = new Capture<>();
		chargeSessionManager.addChargingSessionReadings(capture(readingsCaptor));

		// when
		replayAll();
		MeterValuesRequest req = new MeterValuesRequest();
		req.setConnectorId(session.getConnectorId());
		req.setTransactionId(session.getTransactionId());

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
		req.getMeterValue().add(mv);

		ActionMessage<MeterValuesRequest> message = new BasicActionMessage<MeterValuesRequest>(
				chargePointId, CentralSystemAction.MeterValues, req);
		processor.processActionMessage(message, (msg, res, err) -> {
			assertThat("Message passed", msg, sameInstance(message));
			assertThat("Result available", res, notNullValue());
			assertThat("No error", err, nullValue());

			l.countDown();
			return true;
		});

		// then
		assertThat("Result handler invoked", l.await(1, TimeUnit.SECONDS), equalTo(true));

		List<net.solarnetwork.ocpp.domain.SampledValue> txData = StreamSupport
				.stream(readingsCaptor.getValue().spliterator(), false).collect(Collectors.toList());
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

}
