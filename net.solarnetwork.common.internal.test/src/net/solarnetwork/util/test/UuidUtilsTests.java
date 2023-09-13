/* ==================================================================
 * UuidUtilsTests.java - 12/09/2022 12:44:28 pm
 * 
 * Copyright 2022 SolarNetwork.net Dev Team
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

package net.solarnetwork.util.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.Test;
import net.solarnetwork.util.UuidUtils;

/**
 * Test cases for the {@link UuidUtils} class.
 * 
 * @author matt
 * @version 1.0
 */
public class UuidUtilsTests {

	private Instant testInstant() {
		return LocalDateTime.of(2022, 8, 3, 17, 25, 0, 123456789).toInstant(ZoneOffset.UTC);
	}

	@Test
	public void extractTimestamp_v7() {
		// GIVEN
		UUID uuid = UUID.fromString("018264bd-3e5b-7723-b2fe-4435fd443b4e");

		// WHEN
		Instant ts = UuidUtils.extractTimestamp(uuid);

		// THEN
		assertThat("Timestamp is expected", ts,
				is(equalTo(testInstant().truncatedTo(ChronoUnit.MILLIS))));

	}

	@Test
	public void extractTimestamp_v7_withMicros() {
		// GIVEN
		UUID uuid = UUID.fromString("018264bd-3e5b-7720-b7c6-6d0bd3434a5e");

		// WHEN
		Instant ts = UuidUtils.extractTimestamp(uuid, UuidUtils.V7_MICRO_COUNT_PRECISION);

		// THEN
		assertThat("Timestamp is expected", ts,
				is(equalTo(testInstant().truncatedTo(ChronoUnit.MICROS))));

	}

	@Test
	public void extractTimestamp_v7_with12bit() {
		// GIVEN
		UUID uuid = UUID.fromString("018264bd-3e5b-774f-be79-e3e20f112c44");

		// WHEN
		Instant ts = UuidUtils.extractTimestamp(uuid, 12);

		// THEN
		// calculate fractional value, capped to 10-bit precision
		double f = ((long) (0.456789 * (1 << 12)) / (double) (1 << 12));
		long n = (long) (f * 1_000_000);
		assertThat("Timestamp is expected", ts,
				is(equalTo(testInstant().truncatedTo(ChronoUnit.MILLIS).plusNanos(n))));
	}

	@Test
	public void extractTimestamp_v7_with10bit() {
		// GIVEN
		UUID uuid = UUID.fromString("018264bd-3e5b-774c-baa6-990185e5c63b");

		// WHEN
		Instant ts = UuidUtils.extractTimestamp(uuid, 10);

		// THEN
		// calculate fractional value, capped to 10-bit precision
		double f = ((long) (0.456789 * (1 << 10)) / (double) (1 << 10));
		long n = (long) (f * 1_000_000);
		assertThat("Timestamp is expected", ts,
				is(equalTo(testInstant().truncatedTo(ChronoUnit.MILLIS).plusNanos(n))));
	}

	@Test
	public void extractTimestamp_v7_with8bit() {
		// GIVEN
		UUID uuid = UUID.fromString("018264bd-3e5b-7743-b2e1-27eea30afb54");

		// WHEN
		Instant ts = UuidUtils.extractTimestamp(uuid, 8);

		// THEN
		// calculate fractional value, capped to 8-bit precision
		double f = ((long) (0.456789 * (1 << 8)) / (double) (1 << 8));
		long n = (long) (f * 1_000_000);
		assertThat("Timestamp is expected", ts,
				is(equalTo(testInstant().truncatedTo(ChronoUnit.MILLIS).plusNanos(n))));
	}

	@Test
	public void createBoundary() {
		// WHEN
		UUID uuid = UuidUtils.createUuidV7Boundary(testInstant());

		// THEN
		String upper = Long.toUnsignedString(uuid.getMostSignificantBits(), 16);
		String lower = Long.toUnsignedString(uuid.getLeastSignificantBits(), 16);
		assertThat("Upper bits are timestamp, version, and zeros", upper,
				is(equalTo("18264bd3e5b7000")));
		assertThat("Lower bits are all zero, except UUID variant", lower,
				is(equalTo("8000000000000000")));
		Instant ts = UuidUtils.extractTimestamp(uuid);
		assertThat("Timestamp ms boundary created", ts,
				is(equalTo(testInstant().truncatedTo(ChronoUnit.MILLIS))));
	}

}
