/* ==================================================================
 * StatTracker.java - 21/05/2024 9:08:38 am
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

package net.solarnetwork.util;

import static net.solarnetwork.util.ObjectUtils.requireNonNullArgument;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.LongSupplier;
import org.slf4j.Logger;
import net.solarnetwork.service.Identifiable;

/**
 * General purpose counter statistic tracker for highly concurrent counts.
 *
 * This class is useful for eventually-consistent count tracking, see
 * {@link LongAdder} for more information.
 *
 * @author matt
 * @version 1.0
 * @since 3.10
 */
public class StatTracker implements Identifiable {

	private final ConcurrentMap<String, LongAdder> counts;
	private final Logger log;
	private final String name;
	private int logFrequency;
	private String uid;

	/**
	 * Constructor.
	 *
	 * @param name
	 *        a display name
	 * @param uid
	 *        a unique identifier
	 * @param log
	 *        the logger
	 * @param logFrequency
	 *        the log frequency
	 * @throws IllegalArgumentException
	 *         if {@code name} is {@literal null}
	 */
	public StatTracker(String name, String uid, Logger log, int logFrequency) {
		this(new ConcurrentHashMap<>(), name, uid, log, logFrequency);
	}

	/**
	 * Constructor.
	 *
	 * @param counts
	 *        the count container
	 * @param name
	 *        a display name
	 * @param uid
	 *        a unique identifier
	 * @param log
	 *        the logger
	 * @param logFrequency
	 *        the log frequency
	 * @throws IllegalArgumentException
	 *         if {@code counts}, {@code watermarks}, or {@code name} is
	 *         {@literal null}
	 */
	public StatTracker(ConcurrentMap<String, LongAdder> counts, String name, String uid, Logger log,
			int logFrequency) {
		super();
		this.counts = requireNonNullArgument(counts, "counts");
		this.name = requireNonNullArgument(name, "name");
		this.uid = uid;
		this.log = log;
		this.logFrequency = logFrequency;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(name);
		buf.append(" stats {\n");
		for ( Map.Entry<String, LongAdder> e : counts.entrySet() ) {
			buf.append(String.format("%30s: %d\n", e.getKey(), e.getValue().longValue()));
		}
		buf.append("}");
		return buf.toString();
	}

	/**
	 * Get a sorted snapshot of all counts.
	 *
	 * @return a snapshot of counts; the returned map holds a copy of the
	 *         current counts
	 */
	public NavigableMap<String, Long> allCounts() {
		final TreeMap<String, Long> m = new TreeMap<>();
		for ( Map.Entry<String, LongAdder> e : counts.entrySet() ) {
			m.put(e.getKey(), e.getValue().longValue());
		}
		return m;
	}

	/**
	 * Get a current count value.
	 *
	 * @param key
	 *        the statistic to get the count for
	 * @return the current count value, or {@literal 0} if not defined
	 */
	public long get(String key) {
		final LongAdder c = counts.get(key);
		return (c != null ? c.longValue() : 0L);
	}

	/**
	 * Get a current count value.
	 *
	 * @param key
	 *        the statistic to get the count for (the {@code name} value will be
	 *        used)
	 * @return the current count value, or {@literal 0} if not defined
	 */
	public long get(Enum<?> key) {
		final LongAdder c = counts.get(key.name());
		return (c != null ? c.longValue() : 0L);
	}

	private void log(String key, LongSupplier c, boolean quiet) {
		if ( quiet || !log.isInfoEnabled() ) {
			return;
		}
		long count = c.getAsLong();
		if ( (count % logFrequency) != 0 ) {
			return;
		}

		final String uid = getUid();
		if ( uid != null && !uid.isEmpty() ) {
			log.info("{} {} {}: {}", name, uid, key, count);
		} else {
			log.info("{} {}: {}", name, key, count);
		}
	}

	/**
	 * Increment the current count value.
	 *
	 * @param key
	 *        the count to increment
	 */
	public final void increment(String key) {
		add(key, 1L, false);
	}

	/**
	 * Increment the current count value.
	 *
	 * @param key
	 *        the count to increment (the {@code name} value will be used)
	 */
	public final void increment(Enum<?> key) {
		add(key.name(), 1L, false);
	}

	/**
	 * Increment the current count value.
	 *
	 * @param key
	 *        the count to increment
	 * @param quiet
	 *        {@literal true} to ignore logging
	 */
	public final void increment(String key, boolean quiet) {
		add(key, 1L, quiet);
	}

	/**
	 * Increment the current count value.
	 *
	 * @param key
	 *        the count to increment (the {@code name} value will be used)
	 * @param quiet
	 *        {@literal true} to ignore logging
	 */
	public final void increment(Enum<?> key, boolean quiet) {
		add(key.name(), 1L, quiet);
	}

	/**
	 * Add to the current count value.
	 *
	 * @param key
	 *        the count to add to
	 * @param count
	 *        the amount to add
	 */
	public final void add(String key, long count) {
		add(key, count, false);
	}

	/**
	 * Add to the current count value.
	 *
	 * @param key
	 *        the count to add to (the {@code name} value will be used)
	 * @param count
	 *        the amount to add
	 */
	public final void add(Enum<?> key, long count) {
		add(key.name(), count, false);
	}

	/**
	 * Add to the current count value.
	 *
	 * @param key
	 *        the count to add to and get
	 * @param count
	 *        the amount to add
	 * @param quiet
	 *        {@literal true} to ignore logging
	 */
	public final void add(String key, long count, boolean quiet) {
		final LongAdder c = counts.computeIfAbsent(key, k -> new LongAdder());
		c.add(count);
		log(key, c::longValue, quiet);
	}

	/**
	 * Add to the current count value.
	 *
	 * @param key
	 *        the count to add to (the {@code name} value will be used)
	 * @param count
	 *        the amount to add
	 * @param quiet
	 *        {@literal true} to ignore logging
	 */
	public final void add(Enum<?> key, long count, boolean quiet) {
		add(key.name(), count, quiet);

	}

	@Override
	public String getGroupUid() {
		return null;
	}

	@Override
	public final String getDisplayName() {
		return name;
	}

	/**
	 * Get the log frequency.
	 *
	 * @return the log frequency
	 */
	public final int getLogFrequency() {
		return logFrequency;
	}

	/**
	 * Set the log frequency.
	 *
	 * @param logFrequency
	 *        the frequency
	 */
	public final void setLogFrequency(int logFrequency) {
		this.logFrequency = logFrequency;
	}

	@Override
	public final String getUid() {
		return uid;
	}

	/**
	 * Set the unique ID.
	 *
	 * @param uid
	 *        the unique ID, or {@literal null} for none
	 */
	public final void setUid(String uid) {
		this.uid = uid;
	}

}
