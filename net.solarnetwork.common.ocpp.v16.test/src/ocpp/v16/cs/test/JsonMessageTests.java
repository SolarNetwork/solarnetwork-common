/* ==================================================================
 * JsonMessageTests.java - 30/01/2020 4:45:29 pm
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

package ocpp.v16.cs.test;

import static net.solarnetwork.ocpp.xml.support.XmlDateUtils.newXmlCalendar;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import ocpp.v16.cs.BootNotificationRequest;
import ocpp.v16.cs.Location;
import ocpp.v16.cs.Measurand;
import ocpp.v16.cs.MeterValue;
import ocpp.v16.cs.ReadingContext;
import ocpp.v16.cs.Reason;
import ocpp.v16.cs.SampledValue;
import ocpp.v16.cs.StopTransactionRequest;
import ocpp.v16.cs.UnitOfMeasure;
import ocpp.v16.cs.ValueFormat;

/**
 * Test cases for mapping JAXB XML classes into JSON and vice versa.
 * 
 * @author matt
 * @version 1.0
 */
public class JsonMessageTests {

	private static final Logger log = LoggerFactory.getLogger(JsonMessageTests.class);

	private ObjectMapper createObjectMapperJaxbWriteJson() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JaxbAnnotationModule());
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		mapper.setDateFormat(new StdDateFormat().withColonInTimeZone(true));
		return mapper;
	}

	@Test
	public void read_bootNotificationReq() throws IOException {
		ObjectMapper mapper = createObjectMapperJaxbWriteJson();
		BootNotificationRequest req = mapper.readValue(
				"{\"chargePointModel\":\"One\",\"chargePointSerialNumber\":\"ABC123\",\"chargeBoxSerialNumber\":\"123abc\",\"firmwareVersion\":\"1.2.3\"}",
				BootNotificationRequest.class);
		assertThat("Charge box serial number", req.getChargeBoxSerialNumber(), equalTo("123abc"));
		assertThat("Charge point model", req.getChargePointModel(), equalTo("One"));
		assertThat("Charge point serial number", req.getChargePointSerialNumber(), equalTo("ABC123"));
		assertThat("Firmware version", req.getFirmwareVersion(), equalTo("1.2.3"));
	}

	@Test
	public void write_bootNotificationReq() throws IOException {
		ObjectMapper mapper = createObjectMapperJaxbWriteJson();

		BootNotificationRequest req = new BootNotificationRequest();
		req.setChargeBoxSerialNumber("123abc");
		req.setChargePointModel("One");
		req.setChargePointSerialNumber("ABC123");
		req.setFirmwareVersion("1.2.3");
		String json = mapper.writeValueAsString(req);
		assertThat("Message", json, equalTo(
				"{\"chargePointModel\":\"One\",\"chargePointSerialNumber\":\"ABC123\",\"chargeBoxSerialNumber\":\"123abc\",\"firmwareVersion\":\"1.2.3\"}"));
	}

	@Test
	public void write_stopTxReq() throws IOException {
		final ObjectMapper mapper = createObjectMapperJaxbWriteJson();
		final long reqTimestamp = 1580426430000L;

		StopTransactionRequest req = new StopTransactionRequest();
		req.setIdTag("abc123");
		req.setMeterStop(12345);
		req.setTimestamp(newXmlCalendar(reqTimestamp));
		req.setTransactionId(321);
		req.setReason(Reason.LOCAL);

		for ( int i = 0; i < 3; i++ ) {
			MeterValue mv = new MeterValue();
			mv.setTimestamp(newXmlCalendar(reqTimestamp));

			SampledValue energy = new SampledValue();
			SampledValue power = new SampledValue();
			switch (i) {
				case 0:
					energy.setContext(ReadingContext.TRANSACTION_BEGIN);
					power.setContext(ReadingContext.TRANSACTION_BEGIN);
					break;

				case 2:
					energy.setContext(ReadingContext.TRANSACTION_END);
					power.setContext(ReadingContext.TRANSACTION_END);
					break;

				default:
					energy.setContext(ReadingContext.SAMPLE_PERIODIC);
					power.setContext(ReadingContext.SAMPLE_PERIODIC);
			}
			energy.setFormat(ValueFormat.RAW);
			energy.setLocation(Location.INLET);
			energy.setMeasurand(Measurand.ENERGY_ACTIVE_IMPORT_REGISTER);
			energy.setUnit(UnitOfMeasure.K_WH);
			energy.setValue(String.valueOf(i));
			mv.getSampledValue().add(energy);

			power.setFormat(ValueFormat.RAW);
			power.setLocation(Location.INLET);
			power.setMeasurand(Measurand.POWER_ACTIVE_IMPORT);
			power.setUnit(UnitOfMeasure.W);
			power.setValue("500");
			mv.getSampledValue().add(power);

			req.getTransactionData().add(mv);
		}

		String json = mapper.writeValueAsString(req);
		log.debug("Got JSON: {}", json);
		assertThat("Message", json, equalTo(
				"{\"transactionId\":321,\"idTag\":\"abc123\",\"timestamp\":\"2020-01-30T23:20:30.000+00:00\",\"meterStop\":12345,\"reason\":\"Local\",\"transactionData\":[{\"timestamp\":\"2020-01-30T23:20:30.000+00:00\",\"sampledValue\":[{\"value\":\"0\",\"context\":\"Transaction.Begin\",\"format\":\"Raw\",\"measurand\":\"Energy.Active.Import.Register\",\"location\":\"Inlet\",\"unit\":\"kWh\"},{\"value\":\"500\",\"context\":\"Transaction.Begin\",\"format\":\"Raw\",\"measurand\":\"Power.Active.Import\",\"location\":\"Inlet\",\"unit\":\"W\"}]},{\"timestamp\":\"2020-01-30T23:20:30.000+00:00\",\"sampledValue\":[{\"value\":\"1\",\"context\":\"Sample.Periodic\",\"format\":\"Raw\",\"measurand\":\"Energy.Active.Import.Register\",\"location\":\"Inlet\",\"unit\":\"kWh\"},{\"value\":\"500\",\"context\":\"Sample.Periodic\",\"format\":\"Raw\",\"measurand\":\"Power.Active.Import\",\"location\":\"Inlet\",\"unit\":\"W\"}]},{\"timestamp\":\"2020-01-30T23:20:30.000+00:00\",\"sampledValue\":[{\"value\":\"2\",\"context\":\"Transaction.End\",\"format\":\"Raw\",\"measurand\":\"Energy.Active.Import.Register\",\"location\":\"Inlet\",\"unit\":\"kWh\"},{\"value\":\"500\",\"context\":\"Transaction.End\",\"format\":\"Raw\",\"measurand\":\"Power.Active.Import\",\"location\":\"Inlet\",\"unit\":\"W\"}]}]}"));
	}

}
