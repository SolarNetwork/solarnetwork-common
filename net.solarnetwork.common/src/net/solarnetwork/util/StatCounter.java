/* ==================================================================
 * StatCounter.java - 23/08/2021 10:10:28 AM
 * 
 * Copyright 2021 SolarNetwork.net Dev Team
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

import java.util.concurrent.atomic.AtomicLongArray;
import org.slf4j.Logger;

/**
 * Count statistic helper.
 * 
 * <p>
 * This class keeps track of an atomic array of {@code long} count values. It is
 * designed to help provide runtime operational information in a structured way,
 * so services can define a set of named counters they wish to track and expose
 * to users.
 * </p>
 * 
 * <p>
 * An {@code enum} that implements {@link StatCounter.Stat} can be used to make
 * the counter indexes have meaningful names in code.
 * </p>
 * 
 * <p>
 * This class supports a "base" set of statistics and a "non-base" set of
 * statistics. This design supports a common set of statistics that might be
 * applicable across several different services, along with another unique set
 * of statistics specific to the service. For example, all MQTT clients might
 * track a "base" set of message count statistics while one client service might
 * track specific message types differently than another client service.
 * </p>
 * 
 * @author matt
 * @version 1.0
 * @since 1.78
 */
public class StatCounter {

	/**
	 * A statistic API.
	 */
	public interface Stat {

		/**
		 * Get the statistic index.
		 * 
		 * @return the statistic index
		 */
		int getIndex();

		/**
		 * Get a description of the statistic.
		 * 
		 * @return the description
		 */
		String getDescription();
	}

	private final Logger log;
	private final String name;
	private final Stat[] baseStats;
	private final Stat[] stats;

	private final AtomicLongArray counts;
	private int logFrequency;
	private String uid;

	/**
	 * Constructor.
	 * 
	 * @param name
	 *        the name to use (appears on logs)
	 * @param uid
	 *        the UID to use (appears on logs)
	 * @param log
	 *        the Logger to use, or {@literal null} for no logging
	 * @param logFrequency
	 *        a frequency at which to log INFO level statistic messages
	 * @param baseStats
	 *        the "base" statistics to track; can <b>not</b> be {@literal null}
	 * @throws IllegalArgumentException
	 *         if any required argument is {@literal null}, or {@code stats} is
	 *         provided and the component type is assignable from the
	 *         {@code baseStats} component type
	 */
	public StatCounter(String name, String uid, Logger log, int logFrequency, Stat[] baseStats) {
		this(name, uid, log, logFrequency, baseStats, null);
	}

	/**
	 * Constructor without logging capability.
	 * 
	 * @param name
	 *        the name to use (appears on logs)
	 * @param uid
	 *        the UID to use (appears on logs)
	 * @param baseStats
	 *        the "base" statistics to track; can <b>not</b> be {@literal null}
	 * @param stats
	 *        the non-base statistics to track; can be {@literal null}
	 * @throws IllegalArgumentException
	 *         if any required argument is {@literal null}, or {@code stats} is
	 *         provided and the component type is assignable from the
	 *         {@code baseStats} component type
	 */
	public StatCounter(String name, String uid, Stat[] baseStats, Stat[] stats) {
		this(name, uid, null, 0, baseStats, stats);
	}

	/**
	 * Constructor.
	 * 
	 * @param name
	 *        the name to use (appears on logs)
	 * @param uid
	 *        the UID to use (appears on logs)
	 * @param log
	 *        the Logger to use, or {@literal null} for no logging
	 * @param logFrequency
	 *        a frequency at which to log INFO level statistic messages
	 * @param baseStats
	 *        the "base" statistics to track; can <b>not</b> be {@literal null}
	 * @param stats
	 *        the non-base statistics to track; can be {@literal null}
	 * @throws IllegalArgumentException
	 *         if any required argument is {@literal null}, or {@code stats} is
	 *         provided and the component type is assignable from the
	 *         {@code baseStats} component type
	 */
	public StatCounter(String name, String uid, Logger log, int logFrequency, Stat[] baseStats,
			Stat[] stats) {
		super();
		if ( name == null ) {
			throw new IllegalArgumentException("The name argument must not be null.");
		}
		this.name = name;
		if ( uid == null ) {
			throw new IllegalArgumentException("The uid argument must not be null.");
		}
		this.uid = uid;
		this.log = log;
		this.logFrequency = logFrequency;
		if ( baseStats == null ) {
			throw new IllegalArgumentException("The baseStats argument must not be null.");
		}
		this.baseStats = baseStats;
		if ( stats != null && stats.getClass().getComponentType()
				.isAssignableFrom(baseStats.getClass().getComponentType()) ) {
			throw new IllegalArgumentException(
					"The stats type cannot be assignable from the baseStats type.");
		}
		this.stats = stats;
		this.counts = new AtomicLongArray(baseStats.length + (stats != null ? stats.length : 0));
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(name);
		buf.append(" stats {\n");
		Stat[] s = (stats != null ? stats : baseStats);
		for ( Stat c : s ) {
			buf.append(String.format("%30s: %d\n", c.getDescription(), get(c)));
		}
		buf.append("}");
		return buf.toString();
	}

	/**
	 * Set the log frequency.
	 * 
	 * @param logFrequency
	 *        the frequency
	 */
	public void setLogFrequency(int logFrequency) {
		this.logFrequency = logFrequency;
	}

	/**
	 * Set the unique ID.
	 * 
	 * @param uid
	 *        the unique ID
	 */
	public void setUid(String uid) {
		this.uid = uid;
	}

	private int countStatIndex(Stat stat) {
		if ( stat == null ) {
			throw new IllegalArgumentException("The stat argument must not be null.");
		}
		int idx = stat.getIndex();
		Class<?> clazz = stat.getClass();
		if ( stats != null && clazz.isAssignableFrom(stats.getClass().getComponentType()) ) {
			idx += baseStats.length;
		} else if ( !clazz.isAssignableFrom(baseStats.getClass().getComponentType()) ) {
			throw new IllegalArgumentException(
					"The stat argument type is not assigable to either baseStats or stats.");

		}
		return idx;
	}

	/**
	 * Get a current count value.
	 * 
	 * @param stat
	 *        the statistic to get the count for
	 * @return the current count value
	 * @throws IllegalArgumentException
	 *         if {@code stat} is {@literal null} or not the same type as the
	 *         configured {@code baseStats} or {@code stats} component type
	 */
	public long get(Stat stat) {
		return counts.get(countStatIndex(stat));
	}

	/**
	 * Increment and get the current count value.
	 * 
	 * @param stat
	 *        the count to increment and get
	 * @return the incremented count value
	 * @throws IllegalArgumentException
	 *         if {@code stat} is {@literal null} or not the same type as the
	 *         configured {@code baseStats} or {@code stats} component type
	 */
	public long incrementAndGet(Stat stat) {
		long c = counts.incrementAndGet(countStatIndex(stat));
		if ( log.isInfoEnabled() && ((c % logFrequency) == 0) ) {
			log.info("{} {} {}: {}", name, uid, stat.getDescription(), c);
		}
		return c;
	}

	/**
	 * Add to and get the current count value.
	 * 
	 * @param stat
	 *        the count to add to and get
	 * @param count
	 *        the amount to add
	 * @return the added count value
	 * @throws IllegalArgumentException
	 *         if {@code stat} is {@literal null} or not the same type as the
	 *         configured {@code baseStats} or {@code stats} component type
	 */
	public long addAndGet(Stat stat, long count) {
		long c = counts.addAndGet(countStatIndex(stat), count);
		if ( log.isInfoEnabled() && ((c % logFrequency) == 0) ) {
			log.info("{} {} {}: {}", uid, name, stat.getDescription(), c);
		}
		return c;
	}

}
