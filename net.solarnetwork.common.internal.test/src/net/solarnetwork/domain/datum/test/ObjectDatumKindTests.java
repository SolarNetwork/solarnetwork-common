/* ==================================================================
 * ObjectDatumKindTests.java - 4/10/2024 7:44:08 am
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

package net.solarnetwork.domain.datum.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import java.io.IOException;
import org.junit.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.solarnetwork.domain.datum.ObjectDatumKind;

/**
 * Test cases for the {@link ObjectDatumKind} class.
 *
 * @author matt
 * @version 1.0
 */
public class ObjectDatumKindTests {

	@Test
	public void forValue_null() {
		// WHEN
		ObjectDatumKind result = ObjectDatumKind.fromValue(null);

		// THEN
		assertThat("Null resolved for null value", result, is(nullValue()));
	}

	@Test
	public void forValue_empty() {
		// WHEN
		ObjectDatumKind result = ObjectDatumKind.fromValue("");

		// THEN
		assertThat("Null resolved for empty value", result, is(nullValue()));
	}

	@Test(expected = IllegalArgumentException.class)
	public void forValue_illegal() {
		// WHEN
		ObjectDatumKind.fromValue("not valid");
	}

	@Test
	public void forValue_name() {
		for ( ObjectDatumKind t : ObjectDatumKind.values() ) {
			// WHEN
			ObjectDatumKind result = ObjectDatumKind.fromValue(t.name().toLowerCase());

			// THEN
			assertThat("Enum resolved for case-insensitive name", result, is(equalTo(t)));
		}
	}

	@Test
	public void forValue_key() {
		for ( ObjectDatumKind t : ObjectDatumKind.values() ) {
			// WHEN
			ObjectDatumKind result = ObjectDatumKind.fromValue(String.valueOf(t.getKey()).toUpperCase());

			// THEN
			assertThat("Enum resolved for case-insensitive key", result, is(equalTo(t)));
		}
	}

	@Test
	public void toJson() throws IOException {
		// GIVEN
		ObjectMapper m = new ObjectMapper();

		for ( ObjectDatumKind t : ObjectDatumKind.values() ) {

			// WHEN
			String json = m.writeValueAsString(t);

			// THEN
			assertThat("JSON value is key value", json,
					is(equalTo(String.format("\"%s\"", t.keyValue()))));
		}
	}

	@Test
	public void fromJson_key() throws IOException {
		// GIVEN
		ObjectMapper m = new ObjectMapper();

		for ( ObjectDatumKind t : ObjectDatumKind.values() ) {

			// WHEN
			ObjectDatumKind result = m.readValue(String.format("\"%s\"", t.keyValue()),
					ObjectDatumKind.class);

			// THEN
			assertThat("Parsed JSON key value", result, is(equalTo(t)));
		}
	}

	@Test
	public void fromJson_key_caseInsensitive() throws IOException {
		// GIVEN
		ObjectMapper m = new ObjectMapper();

		for ( ObjectDatumKind t : ObjectDatumKind.values() ) {

			// WHEN
			ObjectDatumKind result = m.readValue(String.format("\"%s\"", t.keyValue().toUpperCase()),
					ObjectDatumKind.class);

			// THEN
			assertThat("Parsed JSON key value", result, is(equalTo(t)));
		}
	}

	@Test
	public void fromJson_name() throws IOException {
		// GIVEN
		ObjectMapper m = new ObjectMapper();

		for ( ObjectDatumKind t : ObjectDatumKind.values() ) {

			// WHEN
			ObjectDatumKind result = m.readValue(String.format("\"%s\"", t.name()),
					ObjectDatumKind.class);

			// THEN
			assertThat("Parsed JSON key value", result, is(equalTo(t)));
		}
	}

	@Test
	public void fromJson_name_caseInsensitive() throws IOException {
		// GIVEN
		ObjectMapper m = new ObjectMapper();

		for ( ObjectDatumKind t : ObjectDatumKind.values() ) {

			// WHEN
			ObjectDatumKind result = m.readValue(String.format("\"%s\"", t.name().toUpperCase()),
					ObjectDatumKind.class);

			// THEN
			assertThat("Parsed JSON key value", result, is(equalTo(t)));
		}
	}

}
