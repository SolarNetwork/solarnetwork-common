/* ==================================================================
 * DatumAuxiliaryRecordSerializerTests.java - 18/07/2026 11:10:45 am
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

import static java.time.Instant.now;
import static net.solarnetwork.test.CommonTestUtils.randomLong;
import static net.solarnetwork.test.CommonTestUtils.randomString;
import static org.assertj.core.api.BDDAssertions.from;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import java.io.IOException;
import java.time.temporal.ChronoUnit;
import org.junit.Before;
import org.junit.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.solarnetwork.codec.BasicDatumAuxiliaryRecordSerializer;
import net.solarnetwork.codec.JsonUtils;
import net.solarnetwork.domain.datum.BasicDatumAuxiliaryRecord;
import net.solarnetwork.domain.datum.DatumAuxiliaryRecord;
import net.solarnetwork.domain.datum.DatumId.DatumIdent;
import net.solarnetwork.domain.datum.DatumSamples;
import net.solarnetwork.domain.datum.GeneralDatumMetadata;
import net.solarnetwork.domain.datum.ObjectDatumKind;
import net.solarnetwork.util.DateUtils;

/**
 * Test cases for the {@link BasicDatumAuxiliaryRecordSerializer} class.
 *
 * @author matt
 * @version 1.0
 */
public class BasicDatumAuxiliaryRecordSerializerTests {

	private ObjectMapper mapper;

	@Before
	public void setup() {
		mapper = JsonUtils.newDatumObjectMapper();
	}

	@Test
	public void serialize_mark() throws IOException {
		// GIVEN
		final DatumIdent ident = new DatumIdent(ObjectDatumKind.Node, randomLong(), randomString(),
				now().truncatedTo(ChronoUnit.SECONDS));
		final GeneralDatumMetadata meta = new GeneralDatumMetadata();
		meta.putInfoValue("foo", "bar");
		final BasicDatumAuxiliaryRecord aux = BasicDatumAuxiliaryRecord.createMark(ident, randomString(),
				meta);

		// WHEN
		final String result = mapper.writeValueAsString(aux);

		// THEN
		// @formatter:off
		then(result)
			.as("JSON generated")
			.isEqualToIgnoringWhitespace("""
				{
					"type":"Mark",
					"kind":"n",
					"objectId":%d,
					"sourceId":"%s",
					"timestamp":"%s",
					"notes":"%s",
					"metadata":{
						"m": {
							"foo": "bar"
						}
					}
				}
				""".formatted(
						aux.getObjectId(),
						aux.getSourceId(),
						DateUtils.ISO_DATE_TIME_ALT_UTC.format(aux.getTimestamp()),
						aux.getNotes()
				)
			)
			;
		// @formatter:on
	}

	@Test
	public void serialize_reset() throws IOException {
		// GIVEN
		final DatumIdent ident = new DatumIdent(ObjectDatumKind.Node, randomLong(), randomString(),
				now().truncatedTo(ChronoUnit.SECONDS));

		final GeneralDatumMetadata meta = new GeneralDatumMetadata();
		meta.putInfoValue("foo", "bar");

		final DatumSamples finalSamples = new DatumSamples();
		finalSamples.putInstantaneousSampleValue("a", 1);

		final DatumSamples startSamples = new DatumSamples();
		startSamples.putInstantaneousSampleValue("a", 0);

		final BasicDatumAuxiliaryRecord aux = BasicDatumAuxiliaryRecord.createReset(ident,
				randomString(), finalSamples, startSamples, meta);

		// WHEN
		final String result = mapper.writeValueAsString(aux);

		// THEN
		// @formatter:off
		then(result)
			.as("JSON generated")
			.isEqualToIgnoringWhitespace("""
				{
					"type":"Reset",
					"kind":"n",
					"objectId":%d,
					"sourceId":"%s",
					"timestamp":"%s",
					"notes":"%s",
					"metadata":{
						"m": {
							"foo": "bar"
						}
					},
					"samplesFinal":{
						"i":{
							"a":1
						}
					},
					"samplesStart":{
						"i":{
							"a":0
						}
					}
				}
				""".formatted(
						aux.getObjectId(),
						aux.getSourceId(),
						DateUtils.ISO_DATE_TIME_ALT_UTC.format(aux.getTimestamp()),
						aux.getNotes()
				)
			)
			;
		// @formatter:on
	}

	@Test
	public void roundtrip() throws IOException {
		// GIVEN
		final DatumIdent ident = new DatumIdent(ObjectDatumKind.Node, randomLong(), randomString(),
				now().truncatedTo(ChronoUnit.SECONDS));

		final GeneralDatumMetadata meta = new GeneralDatumMetadata();
		meta.putInfoValue("foo", "bar");

		final DatumSamples finalSamples = new DatumSamples();
		finalSamples.putInstantaneousSampleValue("a", 1);

		final DatumSamples startSamples = new DatumSamples();
		startSamples.putInstantaneousSampleValue("a", 0);

		final BasicDatumAuxiliaryRecord aux = BasicDatumAuxiliaryRecord.createReset(ident,
				randomString(), finalSamples, startSamples, meta);

		// WHEN
		final String json = mapper.writeValueAsString(aux);
		final DatumAuxiliaryRecord parsed = mapper.readValue(json, DatumAuxiliaryRecord.class);

		// THEN
		// @formatter:off
		then(parsed)
			.as("Roundtrip parsed back to object")
			.isEqualTo(aux)
			.asInstanceOf(type(BasicDatumAuxiliaryRecord.class))
			.returns(true, from(v -> v.isSameAs(aux)))
			;
		// @formatter:on
	}

}
