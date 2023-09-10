/* ==================================================================
 * TimeBasedV7UuidGeneratorTests.java - 3/08/2022 5:23:31 pm
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

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.stream.Collectors.joining;
import static net.solarnetwork.util.UuidUtils.V7_MAX_PRECISION;
import static net.solarnetwork.util.UuidUtils.V7_MICRO_COUNT_PRECISION;
import static net.solarnetwork.util.UuidUtils.extractTimestampV7;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.solarnetwork.util.TimeBasedV7UuidGenerator;
import net.solarnetwork.util.UuidUtils;

/**
 * Test cases for the {@link TimeBasedV7UuidGenerator} class.
 * 
 * @author matt
 * @version 1.0
 */
public class TimeBasedV7UuidGeneratorTests {

	private static final Logger log = LoggerFactory.getLogger(TimeBasedV7UuidGeneratorTests.class);

	private static Clock fixedClock() {
		Instant t = LocalDateTime.of(2022, 8, 3, 17, 25, 0, 123456789).toInstant(ZoneOffset.UTC);
		return Clock.fixed(t, ZoneOffset.UTC);
	}

	@Test
	public void create() {
		// GIVEN
		Clock fixed = fixedClock();
		TimeBasedV7UuidGenerator generator = new TimeBasedV7UuidGenerator(new SecureRandom(), fixed);

		// WHEN
		UUID uuid = generator.generate();
		log.info("Generated v7 UUID: {}", uuid);

		// THEN
		assertThat("UUID generated", uuid, is(notNullValue()));
		assertThat("UUID version", uuid.version(), is(equalTo(7)));
		assertThat("UUID variant", uuid.variant(), is(equalTo(2)));

		assertThat("Top 48 bits are millisesoconds", uuid.getMostSignificantBits() >> 16,
				is(equalTo(fixed.instant().toEpochMilli())));
	}

	@Test
	public void create_withMicros() {
		// GIVEN
		Clock fixed = fixedClock();
		TimeBasedV7UuidGenerator generator = new TimeBasedV7UuidGenerator(new SecureRandom(), fixed,
				V7_MICRO_COUNT_PRECISION);

		// WHEN
		UUID uuid = generator.generate();
		log.info("Generated v7 + micros UUID: {}", uuid);

		// THEN
		assertThat("UUID generated", uuid, is(notNullValue()));
		assertThat("UUID version", uuid.version(), is(equalTo(7)));
		assertThat("UUID variant", uuid.variant(), is(equalTo(2)));

		assertThat("Top 48 bits are millisesoconds", uuid.getMostSignificantBits() >> 16,
				is(equalTo(fixed.instant().toEpochMilli())));
		assertThat("Bits 2-12 of upper are microseconds", (uuid.getMostSignificantBits() & 0xFFF) >> 2,
				is(equalTo(NANOSECONDS.toMicros(fixed.instant().getNano())
						- (NANOSECONDS.toMillis(fixed.instant().getNano()) * 1000))));
	}

	@Test
	public void timeOrder() {
		// GIVEN
		TimeBasedV7UuidGenerator generator = TimeBasedV7UuidGenerator.INSTANCE;

		// WHEN
		final SecureRandom r = new SecureRandom();
		final int count = 20;
		List<UUID> uuids = new ArrayList<>(count);
		for ( int i = 0; i < count; i++ ) {
			UUID uuid = generator.generate();
			uuids.add(uuid);
			try {
				Thread.sleep(Math.abs(r.nextLong()) % 100L);
			} catch ( InterruptedException e ) {
				// ignore
			}
		}

		log.debug("Generated {} v7 UUIDs: [{}]", uuids.size(),
				uuids.stream().map(u -> String.format("%s (%s)", u, extractTimestampV7(u, 0)))
						.collect(joining("\n\t", "\n\t", "\n")));

		// THEN
		long prevTimestamp = 0;
		for ( UUID uuid : uuids ) {
			Instant curr = UuidUtils.extractTimestamp(uuid);
			assertThat("UUID time is never decreasing", curr.toEpochMilli(),
					is(greaterThanOrEqualTo(prevTimestamp)));
			prevTimestamp = curr.toEpochMilli();
		}
	}

	@Test
	public void timeOrder_withMicros() {
		// GIVEN
		TimeBasedV7UuidGenerator generator = TimeBasedV7UuidGenerator.INSTANCE_MICROS;

		// WHEN
		final SecureRandom r = new SecureRandom();
		final int count = 20;
		List<UUID> uuids = new ArrayList<>(count);
		for ( int i = 0; i < count; i++ ) {
			UUID uuid = generator.generate();
			uuids.add(uuid);
			if ( r.nextBoolean() ) {
				try {
					Thread.sleep(Math.abs(r.nextInt(100)));
				} catch ( InterruptedException e ) {
					// ignore
				}
			}
		}

		log.debug("Generated {} v7 + micros UUIDs: [{}]", uuids.size(), uuids.stream()
				.map(u -> String.format("%s (%s)", u, extractTimestampV7(u, V7_MICRO_COUNT_PRECISION)))
				.collect(joining("\n\t", "\n\t", "\n")));

		// THEN
		long prevTimestamp = 0;
		for ( UUID uuid : uuids ) {
			Instant curr = UuidUtils.extractTimestampV7(uuid, V7_MICRO_COUNT_PRECISION);
			assertThat("UUID time is never decreasing", curr.toEpochMilli(),
					is(greaterThanOrEqualTo(prevTimestamp)));
			prevTimestamp = curr.toEpochMilli();
		}
	}

	@Test
	public void create_12bit() {
		// GIVEN
		Clock fixed = fixedClock();
		TimeBasedV7UuidGenerator generator = new TimeBasedV7UuidGenerator(new SecureRandom(), fixed,
				V7_MAX_PRECISION);

		// WHEN
		UUID uuid = generator.generate();
		log.info("Generated v7 + 12-bit precision UUID: {}", uuid);

		// THEN
		assertThat("UUID generated", uuid, is(notNullValue()));
		assertThat("UUID version", uuid.version(), is(equalTo(7)));
		assertThat("UUID variant", uuid.variant(), is(equalTo(2)));

		assertThat("Top 48 bits are millisesoconds", uuid.getMostSignificantBits() >> 16,
				is(equalTo(fixed.instant().toEpochMilli())));
		assertThat("Bits 0-12 of upper are fractional milliseconds truncated to 12 bit precision",
				(uuid.getMostSignificantBits() & 0xFFF), is(equalTo(0x74FL)));
	}

	@Test
	public void create_10bit() {
		// GIVEN
		Clock fixed = fixedClock();
		TimeBasedV7UuidGenerator generator = new TimeBasedV7UuidGenerator(new SecureRandom(), fixed, 10);

		// WHEN
		UUID uuid = generator.generate();
		log.info("Generated v7 + 10-bit precision UUID: {}", uuid);

		// THEN
		assertThat("UUID generated", uuid, is(notNullValue()));
		assertThat("UUID version", uuid.version(), is(equalTo(7)));
		assertThat("UUID variant", uuid.variant(), is(equalTo(2)));

		assertThat("Top 48 bits are millisesoconds", uuid.getMostSignificantBits() >> 16,
				is(equalTo(fixed.instant().toEpochMilli())));
		long msFrac = 0x1D3; // 0.456789 * (1 << 10)
		assertThat("Bits 2-12 of upper are fractional milliseconds truncated to 10 bit precision",
				((uuid.getMostSignificantBits() & 0xFFC) >> 2), is(equalTo(msFrac)));
	}

	@Test
	public void create_8bit() {
		// GIVEN
		Clock fixed = fixedClock();
		TimeBasedV7UuidGenerator generator = new TimeBasedV7UuidGenerator(new SecureRandom(), fixed, 8);

		// WHEN
		UUID uuid = generator.generate();
		log.info("Generated v7 + 8-bit precision UUID: {}", uuid);

		// THEN
		assertThat("UUID generated", uuid, is(notNullValue()));
		assertThat("UUID version", uuid.version(), is(equalTo(7)));
		assertThat("UUID variant", uuid.variant(), is(equalTo(2)));

		assertThat("Top 48 bits are millisesoconds", uuid.getMostSignificantBits() >> 16,
				is(equalTo(fixed.instant().toEpochMilli())));
		long msFrac = 0x74; // 0.456789 * (1 << 8)
		assertThat("Bits 4-12 of upper are fractional milliseconds truncated to 8 bit precision",
				((uuid.getMostSignificantBits() & 0xFF0) >> 4), is(equalTo(msFrac)));
	}

}
