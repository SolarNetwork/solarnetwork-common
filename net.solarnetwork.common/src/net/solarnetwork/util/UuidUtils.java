/* ==================================================================
 * UuidUtils.java - 4/08/2022 9:44:31 am
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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Utility functions for UUIDs.
 * 
 * @author matt
 * @version 1.1
 * @since 3.5
 */
public final class UuidUtils {

	/** A special V7 precision flag for a microsecond counter from 0-999. */
	public static final int V7_MICRO_COUNT_PRECISION = -10;

	/** The V7 maximum additional time precision bits (12). */
	public static final int V7_MAX_PRECISION = 12;

	private UuidUtils() {
		// not available
	}

	/**
	 * Extract the timestamp out of a UUID.
	 * 
	 * <p>
	 * Only UUID versions 1 and 7 are supported.
	 * </p>
	 * 
	 * @param uuid
	 *        the UUID to extract the timestamp from
	 * @return the timestamp, or {@literal null} if unable to extract a
	 *         timestamp
	 */
	public static Instant extractTimestamp(UUID uuid) {
		return extractTimestamp(uuid, 0);
	}

	/**
	 * Extract the timestamp out of a UUID.
	 * 
	 * <p>
	 * Only UUID versions 1 and 7 are supported.
	 * </p>
	 * 
	 * @param uuid
	 *        the UUID to extract the timestamp from
	 * @param additionalPrecisionBits
	 *        if a value between {@code 2} and {@code 12} then for version 7
	 *        UUIDs assume that up to 12 bits starting at bit 66 represent a
	 *        fractional milliseconds value of the timestamp; a special value of
	 *        {@link #V7_MICRO_COUNT_PRECISION} can be passed to represent a
	 *        10-bit microsecond counter value, e.g. 0-999; pass {@code 0} to
	 *        assume no additional precision
	 * @return the timestamp, or {@literal null} if unable to extract a
	 *         timestamp
	 */
	public static Instant extractTimestamp(UUID uuid, int additionalPrecisionBits) {
		if ( uuid.version() == 7 ) {
			return extractTimestampV7(uuid, additionalPrecisionBits);
		} else if ( uuid.version() == 1 ) {
			return Instant.ofEpochMilli(uuid.timestamp());
		}
		return null;
	}

	/**
	 * Extract the timestamp out of a version 7 UUID.
	 * 
	 * @param uuid
	 *        the UUID to extract the timestamp from
	 * @param additionalPrecisionBits
	 *        if a value between {@code 2} and {@code 12} then assume that up to
	 *        12 bits starting at bit 66 represent a fractional milliseconds
	 *        value of the timestamp; a special value of
	 *        {@link #V7_MICRO_COUNT_PRECISION} can be passed to represent a
	 *        10-bit microsecond counter value, e.g. 0-999; pass {@code 0} to
	 *        assume no additional precision
	 * @return the timestamp, or {@literal null} if unable to extract a
	 *         timestamp
	 */
	public static Instant extractTimestampV7(UUID uuid, int additionalPrecisionBits) {
		if ( uuid.version() == 7 ) {
			// timestamp is highest 48 bits of UUID
			Instant inst = Instant.ofEpochMilli((uuid.getMostSignificantBits() >> 16) & 0xFFFFFFFFFFFFL);
			if ( additionalPrecisionBits == V7_MICRO_COUNT_PRECISION ) {
				inst = inst.plus((uuid.getMostSignificantBits() & 0xFFF) >> 2, ChronoUnit.MICROS);
			} else if ( additionalPrecisionBits > 1 && additionalPrecisionBits <= 12 ) {
				double fracMillis = (double) ((uuid.getMostSignificantBits() & 0xFFF) >> (12
						- additionalPrecisionBits)) / (double) (1 << additionalPrecisionBits);
				inst = inst.plusNanos((long) (fracMillis * 1_000_000));
			}
			return inst;
		}
		return null;
	}

	/**
	 * Generate a UUID v7 "boundary" value that encodes a given timestamp.
	 * 
	 * @param ts
	 *        the timestamp to encode
	 * @return the UUID
	 */
	public static UUID createUuidV7Boundary(Instant ts) {
		long now = ts.toEpochMilli();

		// @formatter:off
		long lower = 0x8000000000000000L;
		long upper = (
				((now & 0xFFFFFFFFFFFFL) << 16) // truncate epoch to 48 bits
				| 0x7000L // variant
				);
		// @formatter:on
		return new UUID(upper, lower);
	}
}
