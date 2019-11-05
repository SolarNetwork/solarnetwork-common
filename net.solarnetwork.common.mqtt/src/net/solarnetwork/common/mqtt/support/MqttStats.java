/* ==================================================================
 * MqttStats.java - 11/06/2018 7:43:25 PM
 * 
 * Copyright 2018 SolarNetwork.net Dev Team
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

package net.solarnetwork.common.mqtt.support;

import java.util.concurrent.atomic.AtomicLongArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Statistics for MQTT processing.
 * 
 * @author matt
 * @version 1.0
 * @since 1.1
 */
public class MqttStats {

	/**
	 * A MQTT statistic API.
	 */
	public interface MqttStat {

		int getIndex();

		String getDescription();
	}

	/** Basic counted fields. */
	public enum BasicCounts implements MqttStat {

		ConnectionAttempts(0, "connection attempts"),

		ConnectionSuccess(1, "connections made"),

		ConnectionFail(2, "connections failed"),

		ConnectionLost(3, "connections lost"),

		MessagesReceived(4, "messages received"),

		MessagesDelivered(5, "messages delivered"),

		MessagesDeliveredFail(6, "failed message deliveries");

		private final int index;
		private final String description;

		private BasicCounts(int index, String description) {
			this.index = index;
			this.description = description;
		}

		@Override
		public int getIndex() {
			return index;
		}

		@Override
		public String getDescription() {
			return description;
		}

	}

	private static final Logger log = LoggerFactory.getLogger(MqttStats.class);

	private final AtomicLongArray counts;
	private final MqttStat[] countStats;
	private int logFrequency;
	private String uid;

	/**
	 * Constructor.
	 * 
	 * @param uid
	 *        the UID
	 * @param logFrequency
	 *        a frequency at which to log INFO level statistic messages
	 */
	public MqttStats(String uid, int logFrequency) {
		this(uid, logFrequency, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param uid
	 *        the UID
	 * @param logFrequency
	 *        a frequency at which to log INFO level statistic messages
	 * @param countStats
	 *        the number of statistics to track (on top of the
	 *        {@link BasicCounts}
	 */
	public MqttStats(String uid, int logFrequency, MqttStat[] countStats) {
		super();
		this.uid = uid;
		this.logFrequency = logFrequency;
		this.countStats = countStats;
		this.counts = new AtomicLongArray(
				BasicCounts.values().length + (countStats != null ? countStats.length : 0));
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

	private int countStatIndex(MqttStat stat) {
		int idx = stat.getIndex();
		if ( !(stat instanceof BasicCounts) ) {
			idx += BasicCounts.values().length;
		}
		return idx;
	}

	/**
	 * Get a current count value.
	 * 
	 * @param count
	 *        the count to get
	 * @return the current count value
	 */
	public long get(MqttStat stat) {
		return counts.get(countStatIndex(stat));
	}

	/**
	 * Increment and get the current count value.
	 * 
	 * @param stat
	 *        the count to increment and get
	 * @return the incremented count value
	 */
	public long incrementAndGet(MqttStat stat) {
		long c = counts.incrementAndGet(countStatIndex(stat));
		if ( log.isInfoEnabled() && ((c % logFrequency) == 0) ) {
			log.info("MQTT {} {}: {}", uid, stat.getDescription(), c);
		}
		return c;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder("MqttStats{\n");
		for ( MqttStat c : countStats ) {
			buf.append(String.format("%30s: %d\n", c.getDescription(), get(c)));
		}
		buf.append("}");
		return buf.toString();
	}

}
