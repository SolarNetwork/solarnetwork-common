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
import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.BiFunction;
import java.util.function.LongSupplier;
import org.slf4j.Logger;
import net.solarnetwork.service.Identifiable;

/**
 * General purpose counter statistic tracker for highly concurrent counts.
 *
 * This class is useful for eventually-consistent count tracking, see
 * {@link LongAdder} for more information. The {@code increment(...)} methods
 * track simple total count values, while the {@code add(...)} methods
 * additionally track average/min/max statistics.
 *
 * @author matt
 * @version 1.2
 * @since 3.10
 */
public class StatTracker implements Identifiable {

	/**
	 * A "no accumulation" instance.
	 *
	 * @since 1.1
	 */
	public static final Accumulation NO_ACCUMULATION = new AccumulationValue(0L, 0L, 0.0, 0L, 0L);

	private final ConcurrentMap<String, LongAdder> counts;
	private final ConcurrentMap<String, AccumulativeStats> accums;
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
	 *         if {@code counts} or {@code name} is {@literal null}
	 */
	public StatTracker(ConcurrentMap<String, LongAdder> counts, String name, String uid, Logger log,
			int logFrequency) {
		this(counts, new ConcurrentHashMap<>(), name, uid, log, logFrequency);
	}

	/**
	 * Constructor.
	 *
	 * @param counts
	 *        the count container
	 * @param accums
	 *        the accumulative stats container
	 * @param name
	 *        a display name
	 * @param uid
	 *        a unique identifier
	 * @param log
	 *        the logger
	 * @param logFrequency
	 *        the log frequency
	 * @throws IllegalArgumentException
	 *         if {@code counts}, {@code accums}, or {@code name} is
	 *         {@literal null}
	 * @since 1.1
	 */
	public StatTracker(ConcurrentMap<String, LongAdder> counts,
			ConcurrentMap<String, AccumulativeStats> accums, String name, String uid, Logger log,
			int logFrequency) {
		super();
		this.counts = requireNonNullArgument(counts, "counts");
		this.accums = requireNonNullArgument(accums, "accums");
		this.name = requireNonNullArgument(name, "name");
		this.uid = uid;
		this.log = log;
		this.logFrequency = logFrequency;
	}

	/**
	 * An accumulation type.
	 *
	 * @since 1.2
	 */
	public enum AccumulationType {
		/** The number of times the statistic was updated. */
		Count,

		/** The total accumulated value. */
		Total,

		/** The average (mean) accumulated value. */
		Average,

		/** The minimum seen accumulated value. */
		Minimum,

		/** The maximum seen accumulated value. */
		Maximum,
	}

	/**
	 * An API to accumulated statistics.
	 *
	 * @since 1.1
	 */
	public static interface Accumulation {

		/**
		 * Get the total count of accumulations.
		 *
		 * @return the total count
		 */
		long count();

		/**
		 * Get the total accumulated value.
		 *
		 * @return the total value
		 */
		long total();

		/**
		 * Get the average accumulation.
		 *
		 * @return the average accumulation
		 */
		double avg();

		/**
		 * Get the minimum accumulated value.
		 *
		 * @return the minimum accumulation
		 */
		long min();

		/**
		 * Get the maximum accumulated value.
		 *
		 * @return the maximum accumulation
		 */
		long max();

		/**
		 * Get the value for an accumulation type.
		 *
		 * @param type
		 *        the type to get the value for
		 * @return the value
		 * @since 1.2
		 */
		default Number valueFor(AccumulationType type) {
			switch (type) {
				case Count:
					return count();
				case Total:
					return total();
				case Average:
					return avg();
				case Minimum:
					return min();
				case Maximum:
					return max();
				default:
					return null;
			}
		}

	}

	/**
	 * A simple immutable {@link Accumulation}.
	 *
	 * @since 1.1
	 */
	public static final class AccumulationValue implements Accumulation {

		private final long count;
		private final long total;
		private final double avg;
		private final long min;
		private final long max;

		/**
		 * Constructor.
		 *
		 * @param count
		 *        the count
		 * @param total
		 *        the total
		 * @param avg
		 *        the average
		 * @param min
		 *        the minimum
		 * @param max
		 *        the maximum
		 */
		public AccumulationValue(long count, long total, double avg, long min, long max) {
			super();
			this.count = count;
			this.total = total;
			this.avg = avg;
			this.min = min;
			this.max = max;
		}

		@Override
		public long count() {
			return count;
		}

		@Override
		public long total() {
			return total;
		}

		@Override
		public double avg() {
			return avg;
		}

		@Override
		public long min() {
			return min;
		}

		@Override
		public long max() {
			return max;
		}

	}

	/**
	 * Accumulative statistics.
	 *
	 * @since 1.1
	 */
	public static final class AccumulativeStats implements Accumulation {

		private final LongAdder count;
		private final LongAdder total;
		private final LongAccumulator min;
		private final LongAccumulator max;

		private AccumulativeStats(long value) {
			this.count = new LongAdder();
			this.count.increment();
			this.total = new LongAdder();
			this.total.add(value);
			this.min = new LongAccumulator(Math::min, value);
			this.max = new LongAccumulator(Math::max, value);
		}

		private void add(long value) {
			count.increment();
			total.add(value);
			min.accumulate(value);
			max.accumulate(value);
		}

		private Accumulation snapshot() {
			return new AccumulationValue(count(), total(), avg(), min(), max());
		}

		@Override
		public long count() {
			return count.longValue();
		}

		@Override
		public long total() {
			return total.longValue();
		}

		@Override
		public double avg() {
			return (double) total() / (double) count();
		}

		@Override
		public long min() {
			return min.longValue();
		}

		@Override
		public long max() {
			return max.longValue();
		}

	}

	/**
	 * A function that can be used with
	 * {@link StatTracker#allStatistics(BiFunction)} to map accumulation value
	 * keys.
	 *
	 * <p>
	 * This function simply concatenates the key and type values.
	 * </p>
	 *
	 * @since 1.2
	 */
	public static final BiFunction<String, AccumulationType, String> DEFAULT_KEY_MAPPER = (key,
			type) -> {
		return key + type.name();
	};

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(name);
		buf.append(" stats {\n");
		for ( Map.Entry<String, LongAdder> e : counts.entrySet() ) {
			buf.append(String.format("%30s: %d\n", e.getKey(), e.getValue().longValue()));
		}
		for ( Map.Entry<String, AccumulativeStats> e : accums.entrySet() ) {
			AccumulativeStats a = e.getValue();
			buf.append(String.format("%30s: %d; avg %.1f/%d; min %d; max %d\n", e.getKey(), a.total(),
					a.avg(), a.count(), a.min(), a.max()));
		}
		buf.append("}");
		return buf.toString();
	}

	/**
	 * Get a sorted snapshot of all counts.
	 *
	 * @return a snapshot of counts; the returned map holds a copy of the
	 *         current counts; for accumulated values the total accumulation
	 *         will be returned
	 */
	public NavigableMap<String, Long> allCounts() {
		final NavigableMap<String, Long> m = new TreeMap<>();
		for ( Map.Entry<String, LongAdder> e : counts.entrySet() ) {
			m.put(e.getKey(), e.getValue().longValue());
		}
		for ( Map.Entry<String, AccumulativeStats> e : accums.entrySet() ) {
			m.put(e.getKey(), e.getValue().total());
		}
		return m;
	}

	/**
	 * Get a sorted snapshot of all accumulations.
	 *
	 * @return a snapshot of accumulations; the returned map holds a copy of the
	 *         current accumulations
	 * @since 1.1
	 */
	public NavigableMap<String, Accumulation> allAccumulations() {
		final NavigableMap<String, Accumulation> m = new TreeMap<>();
		for ( Map.Entry<String, AccumulativeStats> e : accums.entrySet() ) {
			m.put(e.getKey(), e.getValue().snapshot());
		}
		return m;
	}

	/**
	 * Get a sorted snapshot of all count and accumulation values.
	 *
	 * <p>
	 * The {@link #DEFAULT_KEY_MAPPER} will be used.
	 * </p>
	 *
	 * @return a snapshot of counts; the returned map holds a copy of the
	 *         current counts
	 * @since 1.2
	 */
	public NavigableMap<String, Number> allStatistics() {
		return allStatistics(DEFAULT_KEY_MAPPER);
	}

	/**
	 * Get a sorted snapshot of all count and accumulation values.
	 *
	 * @param keyMapper
	 *        mapping function to transform the accumulative values into result
	 *        map keys
	 * @return a snapshot of counts; the returned map holds a copy of the
	 *         current counts
	 * @since 1.2
	 */
	public NavigableMap<String, Number> allStatistics(
			BiFunction<String, AccumulationType, String> keyMapper) {
		final NavigableMap<String, Number> m = new TreeMap<>();
		for ( Map.Entry<String, LongAdder> e : counts.entrySet() ) {
			m.put(e.getKey(), e.getValue().longValue());
		}
		for ( Map.Entry<String, AccumulativeStats> e : accums.entrySet() ) {
			for ( AccumulationType type : AccumulationType.values() ) {
				m.put(keyMapper.apply(e.getKey(), type), e.getValue().valueFor(type));
			}
		}
		return m;
	}

	/**
	 * Get a current count value.
	 *
	 * @param key
	 *        the statistic to get the count for; if an accumulation for the key
	 *        exists its total value will be included
	 * @return the current count value, or {@literal 0} if not defined
	 */
	public long get(String key) {
		long result = 0L;
		final LongAdder c = counts.get(key);
		if ( c != null ) {
			result = c.longValue();
		}
		final AccumulativeStats s = accums.get(key);
		if ( s != null ) {
			result += s.total();
		}
		return result;
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
		return get(key.name());
	}

	/**
	 * Get a current accumulation value.
	 *
	 * @param key
	 *        the statistic to get the count for
	 * @return the current count value, or {@link #NO_ACCUMULATION} if not
	 *         defined
	 * @since 1.1
	 */
	public Accumulation getAccumulation(Enum<?> key) {
		return getAccumulation(key.name());
	}

	/**
	 * Get a current accumulation value.
	 *
	 * @param key
	 *        the statistic to get the count for
	 * @return the current count value, or {@link #NO_ACCUMULATION} if not
	 *         defined
	 * @since 1.1
	 */
	public Accumulation getAccumulation(String key) {
		final AccumulativeStats s = accums.get(key);
		if ( s != null ) {
			return s.snapshot();
		}
		return NO_ACCUMULATION;
	}

	private void log(final String key, final LongSupplier c, final boolean quiet) {
		if ( quiet || log == null || !log.isInfoEnabled() ) {
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

	private void log(final String key, final AccumulativeStats a, final boolean quiet) {
		if ( quiet || log == null || !log.isInfoEnabled() ) {
			return;
		}
		long count = a.count();
		if ( (count % logFrequency) != 0 ) {
			return;
		}

		final String uid = getUid();
		if ( uid != null && !uid.isEmpty() ) {
			log.info("{} {} {}: {}; avg {}; range {} - {}", name, uid, key, count, a.avg(), a.min(),
					a.max());
		} else {
			log.info("{} {}: {}; avg {}; range {} - {}", name, key, count, a.avg(), a.min(), a.max());
		}
	}

	/**
	 * Increment the current count value.
	 *
	 * @param key
	 *        the count to increment
	 */
	public final void increment(String key) {
		increment(key, false);
	}

	/**
	 * Increment the current count value.
	 *
	 * @param key
	 *        the count to increment (the {@code name} value will be used)
	 */
	public final void increment(Enum<?> key) {
		increment(key.name(), false);
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
		final LongAdder c = counts.computeIfAbsent(key, k -> new LongAdder());
		c.increment();
		log(key, c::longValue, quiet);
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
		increment(key.name(), quiet);
	}

	/**
	 * Add to the current value.
	 *
	 * @param key
	 *        the count to add to
	 * @param value
	 *        the amount to add
	 */
	public final void add(String key, long value) {
		add(key, value, false);
	}

	/**
	 * Add to the current value.
	 *
	 * @param key
	 *        the count to add to (the {@code name} value will be used)
	 * @param value
	 *        the amount to add
	 */
	public final void add(Enum<?> key, long value) {
		add(key.name(), value, false);
	}

	/**
	 * Add to the current value.
	 *
	 * @param key
	 *        the count to add to and get
	 * @param value
	 *        the amount to add
	 * @param quiet
	 *        {@literal true} to ignore logging
	 */
	public final void add(String key, long value, boolean quiet) {
		final AccumulativeStats a = accums.compute(key, (k, v) -> {
			if ( v == null ) {
				v = new AccumulativeStats(value);
			} else {
				v.add(value);
			}
			return v;
		});
		log(key, a, quiet);
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

	/**
	 * Remove all statistics.
	 */
	public void reset() {
		counts.clear();
		accums.clear();
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
