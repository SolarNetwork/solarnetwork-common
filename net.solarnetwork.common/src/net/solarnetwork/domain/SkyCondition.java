/* ==================================================================
 * SkyCondition.java - 18/02/2019 9:10:57 am
 * 
 * Copyright 2019 SolarNetwork.net Dev Team
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

package net.solarnetwork.domain;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Enumeration of standard sky condition values.
 * 
 * @author matt
 * @version 1.0
 * @since 1.50
 */
public enum SkyCondition implements Bitmaskable {

	/** Clear day. */
	Clear(1),

	/** Scattered clouds. */
	ScatteredClouds(2),

	/** Cloudy. */
	Cloudy(3),

	/** Fog. */
	Fog(4),

	/** Drizzle. */
	Drizzle(5),

	/** Scattered showers. */
	ScatteredShowers(6),

	/** Showers. */
	Showers(7),

	/** Rain. */
	Rain(8),

	/** Hail. */
	Hail(9),

	/** Scattered snow. */
	ScatteredSnow(10),

	/** Snow. */
	Snow(11),

	/** Storm. */
	Storm(12),

	/** Severe storm. */
	SevereStorm(13),

	/** Thunder. */
	Thunder(14),

	/** Windy */
	Windy(15),

	/** Hazy. */
	Hazy(16),

	/** Tornado. */
	Tornado(17),

	/** Hurricane. */
	Hurricane(18),

	/** Dusty, sand storm. */
	Dusty(19);

	private static final ConcurrentMap<Integer, Set<SkyCondition>> BITMASK_CACHE = new ConcurrentHashMap<>(
			8, 0.9f, 1);

	private final int code;

	private SkyCondition(int code) {
		this.code = code;
	}

	/**
	 * Get the code for this condition.
	 * 
	 * @return the code
	 */
	public int getCode() {
		return code;
	}

	@Override
	public int bitmaskBitOffset() {
		return code - 1;
	}

	/**
	 * Get an enum for a code value.
	 * 
	 * @param code
	 *        the code to get an enum for
	 * @return the enum with the given {@code code}, or {@literal null} if
	 *         {@code code} is {@literal 0}
	 * @throws IllegalArgumentException
	 *         if {@code code} is not supported
	 */
	public static SkyCondition forCode(int code) {
		if ( code == 0 ) {
			return null;
		}
		for ( SkyCondition c : values() ) {
			if ( code == c.code ) {
				return c;
			}
		}
		throw new IllegalArgumentException("SkyCondition code [" + code + "] not supported");
	}

	/**
	 * Get a bitmask value out of a set of conditions.
	 * 
	 * @param conditions
	 *        the set of conditions ({@literal null} allowed)
	 * @return the bitmask
	 */
	public static int bitmaskValue(Set<SkyCondition> conditions) {
		return Bitmaskable.bitmaskValue(conditions);
	}

	/**
	 * Convert a bitmask value into a set of conditions.
	 * 
	 * <p>
	 * This method maintains a cache of results to help reduce the number of
	 * runtime objects needed.
	 * </p>
	 * 
	 * @param mask
	 *        a bitmask value of a set of condition codes
	 * @return an immutable set of conditions, never {@literal null}
	 * @see #bitmaskValue(Set)
	 */
	public static Set<SkyCondition> conditionsForBitmask(int mask) {
		return BITMASK_CACHE.computeIfAbsent(mask, m -> {
			Set<SkyCondition> set = Bitmaskable.setForBitmask(m, SkyCondition.class);
			return (set.isEmpty() ? set : Collections.unmodifiableSet(EnumSet.copyOf(set)));
		});
	}

	/**
	 * Clear the internal cache used when computing {@code Set<SkyCondition>}
	 * instances from bitmask values.
	 */
	public static void clearBitmaskCache() {
		BITMASK_CACHE.clear();
	}
}
