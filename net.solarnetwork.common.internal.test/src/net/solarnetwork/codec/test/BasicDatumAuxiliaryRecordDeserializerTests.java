/* ==================================================================
 * BasicDatumAuxiliaryRecordDeserializerTests.java - 18/07/2026 10:58:25 am
 *
 * Copyright 2026 SolarNetwork.net Dev Team
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

package net.solarnetwork.codec.test;

import static org.assertj.core.api.BDDAssertions.from;
import static org.assertj.core.api.BDDAssertions.then;
import java.io.IOException;
import java.time.Instant;
import org.junit.Before;
import org.junit.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import net.solarnetwork.codec.BasicDatumAuxiliaryRecordDeserializer;
import net.solarnetwork.domain.datum.DatumAuxiliaryRecord;
import net.solarnetwork.domain.datum.DatumAuxiliaryType;
import net.solarnetwork.domain.datum.DatumSamples;
import net.solarnetwork.domain.datum.GeneralDatumMetadata;
import net.solarnetwork.domain.datum.ObjectDatumKind;

/**
 * Test cases for the {@link BasicDatumAuxiliaryRecordDeserializer} class.
 *
 * @author matt
 * @version 1.0
 */
public class BasicDatumAuxiliaryRecordDeserializerTests {

	private ObjectMapper mapper;

	private ObjectMapper createObjectMapper() {
		ObjectMapper m = new ObjectMapper();
		SimpleModule mod = new SimpleModule("Test");
		mod.addDeserializer(DatumAuxiliaryRecord.class, BasicDatumAuxiliaryRecordDeserializer.INSTANCE);
		m.registerModule(mod);
		return m;
	}

	@Before
	public void setup() {
		mapper = createObjectMapper();
	}

	@Test
	public void deserialize_mark() throws IOException {
		// GIVEN
		// @formatter:off
		final String json = """
				{
					"type":"Mark",
					"kind":"n",
					"objectId":123,
					"sourceId":"a",
					"timestamp":"2026-07-17 20:36:09Z",
					"notes":"b",
					"metadata":{
						"m": {
							"foo": "bar"
						}
					}
				}
				""";
		// @formatter:on

		// WHEN
		final DatumAuxiliaryRecord result = mapper.readValue(json, DatumAuxiliaryRecord.class);

		// THEN
		final GeneralDatumMetadata expectedMeta = new GeneralDatumMetadata();
		expectedMeta.putInfoValue("foo", "bar");

		// @formatter:off
		then(result)
			.as("JSON parsed")
			.isNotNull()
			.as("Type parsed")
			.returns(DatumAuxiliaryType.Mark, from(DatumAuxiliaryRecord::getType))
			.as("Kind parsed")
			.returns(ObjectDatumKind.Node, from(DatumAuxiliaryRecord::getKind))
			.as("Object ID parsed")
			.returns(123L, from(DatumAuxiliaryRecord::getObjectId))
			.as("Source ID parsed")
			.returns("a", from(DatumAuxiliaryRecord::getSourceId))
			.as("Timestamp parsed")
			.returns(Instant.parse("2026-07-17T20:36:09Z"), from(DatumAuxiliaryRecord::getTimestamp))
			.as("Notes parsed")
			.returns("b", from(DatumAuxiliaryRecord::getNotes))
			.as("Metadata parsed")
			.returns(expectedMeta, from(DatumAuxiliaryRecord::getMetadata))
			;
		// @formatter:on
	}

	@Test
	public void deserialize_reset() throws IOException {
		// GIVEN
		// @formatter:off
		final String json = """
				{
					"type":"Reset",
					"kind":"n",
					"objectId":123,
					"sourceId":"a",
					"timestamp":"2026-07-17 20:38:44Z",
					"notes":"b",
					"metadata":{
						"m": {
							"foo": "bar"
						}
					},
					"samplesFinal":{"i":{"a":1}},
					"samplesStart":{"i":{"a":0}}
				}
				""";
		// @formatter:on

		// WHEN
		final DatumAuxiliaryRecord result = mapper.readValue(json, DatumAuxiliaryRecord.class);

		// THEN
		final GeneralDatumMetadata expectedMeta = new GeneralDatumMetadata();
		expectedMeta.putInfoValue("foo", "bar");

		final DatumSamples expectedFinal = new DatumSamples();
		expectedFinal.putInstantaneousSampleValue("a", 1);

		final DatumSamples expectedStart = new DatumSamples();
		expectedStart.putInstantaneousSampleValue("a", 0);

		// @formatter:off
		then(result)
			.as("JSON parsed")
			.isNotNull()
			.as("Type parsed")
			.returns(DatumAuxiliaryType.Reset, from(DatumAuxiliaryRecord::getType))
			.as("Kind parsed")
			.returns(ObjectDatumKind.Node, from(DatumAuxiliaryRecord::getKind))
			.as("Object ID parsed")
			.returns(123L, from(DatumAuxiliaryRecord::getObjectId))
			.as("Source ID parsed")
			.returns("a", from(DatumAuxiliaryRecord::getSourceId))
			.as("Timestamp parsed")
			.returns(Instant.parse("2026-07-17T20:38:44Z"), from(DatumAuxiliaryRecord::getTimestamp))
			.as("Notes parsed")
			.returns("b", from(DatumAuxiliaryRecord::getNotes))
			.as("Metadata parsed")
			.returns(expectedMeta, from(DatumAuxiliaryRecord::getMetadata))
			.as("Samples final parsed")
			.returns(expectedFinal, from(DatumAuxiliaryRecord::getSamplesFinal))
			.as("Samples start parsed")
			.returns(expectedStart, from(DatumAuxiliaryRecord::getSamplesStart))
			;
		// @formatter:on
	}

}
