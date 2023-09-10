/* ==================================================================
 * TimeBasedUuidGenerator.java - 2/08/2022 5:32:23 pm
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

package net.solarnetwork.util;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static net.solarnetwork.util.ObjectUtils.requireNonNullArgument;
import static net.solarnetwork.util.UuidUtils.V7_MAX_PRECISION;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * UUID generator using time-based v7 UUIDs.
 * 
 * <p>
 * The UUID v7 specification defines the timestamp precision as milliseconds,
 * with an allowance for up to 12 more bits of time precision. The
 * {@code additionalPrecisionBits} property of this class allows between 2 and
 * 12 bits of additional time precision, as well as a microsecond "counter" mode
 * that uses 10 bits.
 * </p>
 * 
 * <p>
 * The UUID v7 scheme follows this bit pattern:
 * </p>
 * 
 * <pre>{@code
 *  0                   1                   2                   3
 *  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                           unix_ts_ms                          |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |          unix_ts_ms           |  ver  |       rand_a          |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |var|                        rand_b                             |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                            rand_b                             |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * }</pre>
 * 
 * <p>
 * Using 12 bits of additional time precision follows this bit pattern, where
 * {@code ms_frac} is an integer millisecond integer fraction out of 4096
 * (2^12):
 * </p>
 * 
 * <pre>{@code
 *  0                   1                   2                   3
 *  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                           unix_ts_ms                          |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |          unix_ts_ms           |  ver  |       ms_frac         |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |var|                        rand_b                             |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                            rand_b                             |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * }</pre>
 * 
 * <p>
 * Lower additional bit precisions shrink the {@code ms_frac} field size to the
 * left and fill the remaining bits on the right with random values.
 * </p>
 * 
 * <p>
 * The "microsecond counter" mode of this class follows this bit pattern:
 * </p>
 * 
 * <pre>{@code
 *  0                   1                   2                   3
 *  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                           unix_ts_ms                          |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |          unix_ts_ms           |  ver  |       micros      |rnd|
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |var|                        rand_b                             |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                            rand_b                             |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * }</pre>
 * 
 * <p>
 * See <a href=
 * "https://datatracker.ietf.org/doc/html/draft-ietf-uuidrev-rfc4122bis#name-uuid-version-7">the
 * IETF working draft</a> for more details.
 * </p>
 * 
 * @author matt
 * @version 1.0
 * @since 3.5
 */
public class TimeBasedV7UuidGenerator implements UuidGenerator, UuidTimestampDecoder {

	/**
	 * A default instance.
	 */
	public static final TimeBasedV7UuidGenerator INSTANCE;
	static {
		Clock c = Clock.tick(Clock.systemUTC(), Duration.ofMillis(1));
		TimeBasedV7UuidGenerator g;
		try {
			g = new TimeBasedV7UuidGenerator(SecureRandom.getInstanceStrong(), c);
		} catch ( NoSuchAlgorithmException e ) {
			g = new TimeBasedV7UuidGenerator(new SecureRandom(), c);
		}
		INSTANCE = g;
	}

	/**
	 * A default instance with microsecond counter time precision.
	 */
	public static final TimeBasedV7UuidGenerator INSTANCE_MICROS;
	static {
		Clock c = Clock.tick(Clock.systemUTC(), Duration.of(1, ChronoUnit.MICROS));
		TimeBasedV7UuidGenerator g;
		try {
			g = new TimeBasedV7UuidGenerator(SecureRandom.getInstanceStrong(), c,
					UuidUtils.V7_MICRO_COUNT_PRECISION);
		} catch ( NoSuchAlgorithmException e ) {
			g = new TimeBasedV7UuidGenerator(new SecureRandom(), c, UuidUtils.V7_MICRO_COUNT_PRECISION);
		}
		INSTANCE_MICROS = g;
	}

	/**
	 * A default instance with 12-bit additional time precision.
	 */
	public static final TimeBasedV7UuidGenerator INSTANCE_12BIT;
	static {
		Clock c = Clock.systemUTC();
		TimeBasedV7UuidGenerator g;
		try {
			g = new TimeBasedV7UuidGenerator(SecureRandom.getInstanceStrong(), c, 12);
		} catch ( NoSuchAlgorithmException e ) {
			g = new TimeBasedV7UuidGenerator(new SecureRandom(), c, 12);
		}
		INSTANCE_12BIT = g;
	}

	private final SecureRandom rand;
	private final Clock clock;
	private final int additionalPrecisionBits;

	/**
	 * Constructor.
	 * 
	 * <p>
	 * Uses millisecond time precision.
	 * </p>
	 * 
	 * @param rand
	 *        the random number generator to use
	 * @param clock
	 *        the clock
	 */
	public TimeBasedV7UuidGenerator(SecureRandom rand, Clock clock) {
		this(rand, clock, 0);
	}

	/**
	 * Constructor.
	 * 
	 * @param rand
	 *        the random number generator to use
	 * @param clock
	 *        the clock
	 * @param additionalPrecisionBits
	 *        if a value between {@code 2} and {@code 12} then for version 7
	 *        UUIDs assume that up to 12 bits starting at bit 66 represent a
	 *        fractional milliseconds value of the timestamp; a special value of
	 *        {@link UuidUtils#V7_MICRO_COUNT_PRECISION} can be passed to
	 *        represent a 10-bit microsecond counter value, e.g. 0-999; pass
	 *        {@code 0} to assume no additional precision
	 */
	public TimeBasedV7UuidGenerator(SecureRandom rand, Clock clock, int additionalPrecisionBits) {
		super();
		this.rand = requireNonNullArgument(rand, "rand");
		this.clock = requireNonNullArgument(clock, "clock");
		this.additionalPrecisionBits = additionalPrecisionBits;
	}

	@Override
	public UUID generate() {
		final Instant now = clock.instant();

		long lower = 0xB000000000000000L | (rand.nextLong() & 0x3FFFFFFFFFFFFFFFL);
		byte[] r = new byte[2];
		if ( additionalPrecisionBits != 12 ) {
			rand.nextBytes(r);
		}
		// @formatter:off
		long upper = (
				((now.toEpochMilli() & 0xFFFFFFFFFFFFL) << 16) // truncate epoch to 48 bits
				| 0x7000L // variant
				| ((r[0] & 0x0F) << 8) // rand_a 12 bits
				| (r[1] & 0xFF)
				);
		// @formatter:on
		if ( additionalPrecisionBits == UuidUtils.V7_MICRO_COUNT_PRECISION ) {
			// use 10 bits for microseconds; preserve last 2 random bits
			int micros = ((now.getNano() / 1_000) - ((now.getNano() / 1_000_000) * 1_000));
			upper = (upper & 0xFFFFFFFF_FFFFF003L) | (micros << 2);
		} else if ( additionalPrecisionBits > 2 && additionalPrecisionBits <= V7_MAX_PRECISION ) {
			// calculate fractional milliseconds from nanoseconds
			long m = NANOSECONDS.toMillis(now.getNano());
			double f = (now.getNano() - MILLISECONDS.toNanos(m)) / 1_000_000.0;

			// map fractional milliseconds into precision bits integer
			int i = (int) (f * (1 << additionalPrecisionBits));

			// merge fractional milliseconds onto upper bits
			long mask = 0xFFFFFFFF_FFFFF000L;
			if ( additionalPrecisionBits < V7_MAX_PRECISION ) {
				// add active bits to mask for right-most bits not part of time precision
				long s = ~((~1) << (V7_MAX_PRECISION - additionalPrecisionBits - 1));
				mask |= s;
			}

			upper = (upper & mask) | (i << (V7_MAX_PRECISION - additionalPrecisionBits));
		}
		return new UUID(upper, lower);
	}

	@Override
	public Instant decodeTimestamp(UUID uuid) {
		return UuidUtils.extractTimestampV7(uuid, additionalPrecisionBits);
	}

	@Override
	public UUID createTimestampBoundary(Instant ts) {
		return UuidUtils.createUuidV7Boundary(ts);
	}

}
