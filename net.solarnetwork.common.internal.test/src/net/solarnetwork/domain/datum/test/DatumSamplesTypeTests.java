/* ==================================================================
 * DatumSamplesTypeTests.java - 29/09/2024 3:24:51 pm
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
import net.solarnetwork.domain.datum.DatumSamplesType;

/**
 * Test cases for the {@link DatumSamplesType} class.
 *
 * @author matt
 * @version 1.0
 */
public class DatumSamplesTypeTests {

	@Test
	public void forValue_null() {
		// WHEN
		DatumSamplesType result = DatumSamplesType.fromValue(null);

		// THEN
		assertThat("Null resolved for null value", result, is(nullValue()));
	}

	@Test
	public void forValue_empty() {
		// WHEN
		DatumSamplesType result = DatumSamplesType.fromValue("");

		// THEN
		assertThat("Null resolved for empty value", result, is(nullValue()));
	}

	@Test(expected = IllegalArgumentException.class)
	public void forValue_illegal() {
		// WHEN
		DatumSamplesType.fromValue("not valid");
	}

	@Test
	public void forValue_name() {
		for ( DatumSamplesType t : DatumSamplesType.values() ) {
			// WHEN
			DatumSamplesType result = DatumSamplesType.fromValue(t.name().toLowerCase());

			// THEN
			assertThat("Enum resolved for case-insensitive name", result, is(equalTo(t)));
		}
	}

	@Test
	public void forValue_key() {
		for ( DatumSamplesType t : DatumSamplesType.values() ) {
			// WHEN
			DatumSamplesType result = DatumSamplesType
					.fromValue(String.valueOf(t.toKey()).toUpperCase());

			// THEN
			assertThat("Enum resolved for case-insensitive key", result, is(equalTo(t)));
		}
	}

	@Test
	public void toJson() throws IOException {
		// GIVEN
		ObjectMapper m = new ObjectMapper();

		for ( DatumSamplesType t : DatumSamplesType.values() ) {

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

		for ( DatumSamplesType t : DatumSamplesType.values() ) {

			// WHEN
			DatumSamplesType result = m.readValue(String.format("\"%s\"", t.keyValue()),
					DatumSamplesType.class);

			// THEN
			assertThat("Parsed JSON key value", result, is(equalTo(t)));
		}
	}

	@Test
	public void fromJson_key_caseInsensitive() throws IOException {
		// GIVEN
		ObjectMapper m = new ObjectMapper();

		for ( DatumSamplesType t : DatumSamplesType.values() ) {

			// WHEN
			DatumSamplesType result = m.readValue(String.format("\"%s\"", t.keyValue().toUpperCase()),
					DatumSamplesType.class);

			// THEN
			assertThat("Parsed JSON key value", result, is(equalTo(t)));
		}
	}

	@Test
	public void fromJson_name() throws IOException {
		// GIVEN
		ObjectMapper m = new ObjectMapper();

		for ( DatumSamplesType t : DatumSamplesType.values() ) {

			// WHEN
			DatumSamplesType result = m.readValue(String.format("\"%s\"", t.name()),
					DatumSamplesType.class);

			// THEN
			assertThat("Parsed JSON key value", result, is(equalTo(t)));
		}
	}

	@Test
	public void fromJson_name_caseInsensitive() throws IOException {
		// GIVEN
		ObjectMapper m = new ObjectMapper();

		for ( DatumSamplesType t : DatumSamplesType.values() ) {

			// WHEN
			DatumSamplesType result = m.readValue(String.format("\"%s\"", t.name().toUpperCase()),
					DatumSamplesType.class);

			// THEN
			assertThat("Parsed JSON key value", result, is(equalTo(t)));
		}
	}

}
