/* ==================================================================
 * CountTracker.java - 6/05/2026 11:38:15 am
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

package net.solarnetwork.util;

import java.util.Map;

/**
 * API for something that tracks a set of counts by a string key.
 *
 * @author matt
 * @version 1.0
 * @since 4.36
 */
public interface CountTracker {

	/**
	 * Sets a new value for the given key.
	 *
	 * @param key
	 *        the key
	 * @param value
	 *        the value to associate with key
	 */
	void putCount(String key, long value);

	/**
	 * Increment the count of a given key, setting to {@code 1} if the key is
	 * not already known.
	 *
	 * @param key
	 *        the key to increment the associated value of
	 */
	default void incrementCount(String key) {
		addCount(key, 1L);
	}

	/**
	 * Add to the count of a given key, setting to {@code amount} if the key is
	 * not already known.
	 *
	 * @param key
	 *        the key to increment the associated value of
	 * @param amount
	 *        the amount to add, or set if {@code key} is not already known
	 */
	void addCount(String key, long amount);

	/**
	 * Get the count for a given key.
	 *
	 * @param key
	 *        the key to get the count for
	 * @param notFoundValue
	 *        the value to return if {@code key} is not known
	 * @return the count
	 */
	long getCount(String key, long notFoundValue);

	/**
	 * Get the count for a given key, defaulting to {@code 0} if {@code key} is
	 * not known.
	 *
	 * @param key
	 *        the key to get the count for
	 * @return the count
	 */
	default long getCount(String key) {
		return getCount(key, 0);
	}

	/**
	 * Get a {@link Map} of all count values.
	 *
	 * @return the map
	 */
	Map<String, Long> toMap();

	/**
	 * Create a copy of this tracker.
	 *
	 * @return the copy
	 */
	CountTracker clone();

}
