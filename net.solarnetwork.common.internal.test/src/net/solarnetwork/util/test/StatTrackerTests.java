/* ==================================================================
 * StatTrackerTests.java - 21/05/2024 9:53:50 am
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

package net.solarnetwork.util.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import java.security.SecureRandom;
import java.util.NavigableMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import net.solarnetwork.test.StringLogger;
import net.solarnetwork.util.StatTracker;
import net.solarnetwork.util.StatTracker.Accumulation;
import net.solarnetwork.util.StatTracker.AccumulationValue;

/**
 * Test cases for the {@link StatTracker} class.
 *
 * @author matt
 * @version 1.1
 */
public class StatTrackerTests {

	public enum BasicCounts {

		Foo,

		Bar,

		;

	}

	public enum ExtraCounts {

		Bim,

		Bam,

		;

	}

	private static final SecureRandom rnd = new SecureRandom();

	private StringLogger log;

	@Before
	public void setup() {
		log = new StringLogger();
	}

	@Test
	public void basic_increment() {
		// GIVEN
		StatTracker c = new StatTracker("TestStats", "test", log, 5);

		// WHEN
		long f = 0;
		long b = 0;
		for ( int i = 0; i < 10; i++ ) {
			BasicCounts s;
			if ( rnd.nextBoolean() ) {
				f++;
				s = BasicCounts.Foo;
			} else {
				b++;
				s = BasicCounts.Bar;
			}
			c.increment(s);
		}

		// THEN
		assertThat("Foo final value", c.get(BasicCounts.Foo), is(f));
		assertThat("Bar final value", c.get(BasicCounts.Bar), is(b));
	}

	@Test
	public void extra_increment() {
		// GIVEN
		StatTracker c = new StatTracker("TestStats", "test", log, 5);

		// WHEN
		long bim = 0;
		long bam = 0;
		for ( int i = 0; i < 10; i++ ) {
			Enum<?> s;
			if ( rnd.nextBoolean() ) {
				bim++;
				s = ExtraCounts.Bim;
			} else {
				bam++;
				s = ExtraCounts.Bam;
			}
			c.increment(s);
		}

		// THEN
		assertThat("Foo final value", c.get(BasicCounts.Foo), is(0L));
		assertThat("Bar final value", c.get(BasicCounts.Bar), is(0L));
		assertThat("Bim final value", c.get(ExtraCounts.Bim), is(bim));
		assertThat("Bam final value", c.get(ExtraCounts.Bam), is(bam));
	}

	@Test
	public void mixed_increment() {
		// GIVEN
		StatTracker c = new StatTracker("TestStats", "test", log, 5);

		// WHEN
		long foo = 0;
		long bar = 0;
		long bim = 0;
		long bam = 0;
		for ( int i = 0; i < 10; i++ ) {
			Enum<?> s;
			if ( rnd.nextBoolean() ) {
				if ( rnd.nextBoolean() ) {
					foo++;
					s = BasicCounts.Foo;
				} else {
					bar++;
					s = BasicCounts.Bar;
				}
			} else {
				if ( rnd.nextBoolean() ) {
					bim++;
					s = ExtraCounts.Bim;
				} else {
					bam++;
					s = ExtraCounts.Bam;

				}
			}
			c.increment(s);
		}

		// THEN
		assertThat("Foo final value", c.get(BasicCounts.Foo), is(foo));
		assertThat("Bar final value", c.get(BasicCounts.Bar), is(bar));
		assertThat("Bim final value", c.get(ExtraCounts.Bim), is(bim));
		assertThat("Bam final value", c.get(ExtraCounts.Bam), is(bam));
	}

	@Test
	public void mixed_add() {
		// GIVEN
		StatTracker c = new StatTracker("TestStats", "test", log, 5);

		// WHEN
		long foo = 0;
		long bar = 0;
		long bim = 0;
		long bam = 0;
		for ( int i = 0; i < 10; i++ ) {
			Enum<?> s;
			int amount = rnd.nextInt(100);
			if ( rnd.nextBoolean() ) {
				if ( rnd.nextBoolean() ) {
					foo += amount;
					s = BasicCounts.Foo;
				} else {
					bar += amount;
					s = BasicCounts.Bar;
				}
			} else {
				if ( rnd.nextBoolean() ) {
					bim += amount;
					s = ExtraCounts.Bim;
				} else {
					bam += amount;
					s = ExtraCounts.Bam;

				}
			}
			c.add(s, amount);
		}

		// THEN
		assertThat("Foo final value", c.get(BasicCounts.Foo), is(foo));
		assertThat("Bar final value", c.get(BasicCounts.Bar), is(bar));
		assertThat("Bim final value", c.get(ExtraCounts.Bim), is(bim));
		assertThat("Bam final value", c.get(ExtraCounts.Bam), is(bam));
	}

	@Test
	public void logging() {
		// GIVEN
		StatTracker c = new StatTracker("TestStats", "test", log, 5);

		// WHEN
		for ( int i = 0; i < 10; i++ ) {
			c.increment(BasicCounts.Foo);
		}

		// THEN
		assertThat("Foo final value", c.get(BasicCounts.Foo), is(10L));
		assertThat("2 log entries added", log.getEntries(), hasSize(2));
		assertThat("Log entry 0 count", log.getEntries().get(0).toString(), endsWith("Foo: 5"));
		assertThat("Log entry 0 count", log.getEntries().get(1).toString(), endsWith("Foo: 10"));
	}

	@Test
	public void concurrent() throws Exception {
		// GIVEN
		final ExecutorService executor = Executors.newWorkStealingPool();
		final int taskCount = 10_000;
		final StatTracker c = new StatTracker("TestStats", "test", log, 10);

		// WHEN
		try {
			for ( int i = 0; i < taskCount; i++ ) {
				executor.submit(() -> {
					Enum<?> s;
					if ( rnd.nextBoolean() ) {
						if ( rnd.nextBoolean() ) {
							s = BasicCounts.Foo;
						} else {
							s = BasicCounts.Bar;
						}
					} else {
						if ( rnd.nextBoolean() ) {
							s = ExtraCounts.Bim;
						} else {
							s = ExtraCounts.Bam;
						}
					}
					c.increment(s);
				});
			}
		} finally {
			executor.shutdown();
			executor.awaitTermination(10, TimeUnit.SECONDS);
		}

		// THEN
		long total = c.get(BasicCounts.Foo) + c.get(BasicCounts.Bar) + c.get(ExtraCounts.Bim)
				+ c.get(ExtraCounts.Bam);
		assertThat("All counts captured", total, is(equalTo((long) taskCount)));
	}

	@Test
	public void stringValue() {
		// GIVEN
		StatTracker c = new StatTracker("TestStats", "test", log, 5);

		// WHEN
		long count = rnd.nextInt(90) + 10;
		c.add(BasicCounts.Foo, count);

		// THEN
		// @formatter:off
		assertThat("String value", c.toString(), is(equalTo(String.format(
				  "TestStats stats {\n"
				+ "                           Foo: %d; avg %.1f/%d; min %d; max %d\n"
				+ "}"
				, count, (double)count, 1, count, count))));
		// @formatter:on
	}

	@Test
	public void stringValue_incrementOnly() {
		// GIVEN
		StatTracker c = new StatTracker("TestStats", "test", log, 5);

		// WHEN
		long count = rnd.nextInt(90) + 10;
		for ( int i = 0; i < count; i++ ) {
			c.increment(BasicCounts.Foo);
		}

		// THEN
		// @formatter:off
		assertThat("String value", c.toString(), is(equalTo(String.format(
				  "TestStats stats {\n"
				+ "                           Foo: %d\n"
				+ "}"
				, count))));
		// @formatter:on
	}

	@Test
	public void allCounts() {
		// GIVEN
		StatTracker c = new StatTracker("TestStats", "test", log, 5);
		c.add(BasicCounts.Foo, 1);
		c.add(BasicCounts.Bar, 2);
		c.add(ExtraCounts.Bim, 3);
		c.add(ExtraCounts.Bam, 4);

		// WHEN
		NavigableMap<String, Long> m = c.allCounts();

		// THEN
		assertThat("All counts present, ordered", m.keySet(), contains("Bam", "Bar", "Bim", "Foo"));
		assertThat("Foo count", m.get(BasicCounts.Foo.name()), is(equalTo(1L)));
		assertThat("Bar count", m.get(BasicCounts.Bar.name()), is(equalTo(2L)));
		assertThat("Bim count", m.get(ExtraCounts.Bim.name()), is(equalTo(3L)));
		assertThat("Bam count", m.get(ExtraCounts.Bam.name()), is(equalTo(4L)));
	}

	@Test
	public void allCounts_naturalOrder() {
		// GIVEN
		StatTracker c = new StatTracker("TestStats", "test", log, 5);
		c.increment("foo");
		c.increment("Foo 10");
		c.increment("Foo 5");
		c.increment("Foo 100");
		c.increment("bim");

		// WHEN
		NavigableMap<String, Long> m = c.allCounts();

		// THEN
		assertThat("All counts present, ordered", m.keySet(),
				contains("bim", "foo", "Foo 5", "Foo 10", "Foo 100"));
	}

	private void assertAccumulationEquals(String msg, Accumulation actual, Accumulation expected) {
		assertThat(msg + " provided", actual, is(notNullValue()));
		assertThat(msg + " total", actual.total(), is(equalTo(expected.total())));
		assertThat(msg + " count", actual.count(), is(equalTo(expected.count())));
		assertThat(msg + " avg", actual.avg(), is(equalTo(expected.avg())));
		assertThat(msg + " min", actual.min(), is(equalTo(expected.min())));
		assertThat(msg + " max", actual.max(), is(equalTo(expected.max())));
	}

	@Test
	public void getAccumulation_none() {
		// GIVEN
		StatTracker c = new StatTracker("TestStats", "test", log, 5);

		// WHEN
		Accumulation a = c.getAccumulation(BasicCounts.Foo);

		// THEN
		assertThat("Constant instance returned", a, is(sameInstance(StatTracker.NO_ACCUMULATION)));
	}

	@Test
	public void getAccumulation() {
		// GIVEN
		StatTracker c = new StatTracker("TestStats", "test", log, 5);
		c.add(BasicCounts.Foo, 1L);
		c.add(BasicCounts.Foo, 2L);
		c.add(BasicCounts.Foo, 3L);
		c.add(BasicCounts.Foo, 4L);

		// WHEN
		Accumulation a = c.getAccumulation(BasicCounts.Foo);

		// THEN
		assertAccumulationEquals("Foo count", a, new AccumulationValue(4L, 10L, 2.5, 1L, 4L));
	}

	@Test
	public void allAccumulations() {
		// GIVEN
		StatTracker c = new StatTracker("TestStats", "test", log, 5);
		c.add(BasicCounts.Foo, 1L);
		c.add(BasicCounts.Bar, 2L);
		c.add(ExtraCounts.Bim, 3L);
		c.add(ExtraCounts.Bam, 4L);

		// WHEN
		NavigableMap<String, Accumulation> m = c.allAccumulations();

		// THEN
		assertThat("All counts present, ordered", m.keySet(), contains("Bam", "Bar", "Bim", "Foo"));
		assertAccumulationEquals("Foo count", m.get(BasicCounts.Foo.name()),
				new AccumulationValue(1L, 1L, 1.0, 1L, 1L));
		assertAccumulationEquals("Bar count", m.get(BasicCounts.Bar.name()),
				new AccumulationValue(1L, 2L, 2.0, 2L, 2L));
		assertAccumulationEquals("Bim count", m.get(ExtraCounts.Bim.name()),
				new AccumulationValue(1L, 3L, 3.0, 3L, 3L));
		assertAccumulationEquals("Bam count", m.get(ExtraCounts.Bam.name()),
				new AccumulationValue(1L, 4L, 4.0, 4L, 4L));
	}

	@Test
	public void allAccumulations_naturalOrder() {
		// GIVEN
		StatTracker c = new StatTracker("TestStats", "test", log, 5);
		c.add("foo", 1L);
		c.add("Foo 10", 2L);
		c.add("Foo 5", 3L);
		c.add("Foo 100", 4L);
		c.add("bim", 5L);

		// WHEN
		NavigableMap<String, Accumulation> m = c.allAccumulations();

		// THEN
		assertThat("All accumulations present, ordered", m.keySet(),
				contains("bim", "foo", "Foo 5", "Foo 10", "Foo 100"));
	}

	@Test
	public void allStatistics() {
		// GIVEN
		StatTracker c = new StatTracker("TestStats", "test", log, 5);
		c.increment(BasicCounts.Foo);
		c.increment(BasicCounts.Foo);
		c.add(BasicCounts.Bar, 1L);
		c.add(ExtraCounts.Bim, 2L);
		c.add(ExtraCounts.Bim, 4L);

		// WHEN
		NavigableMap<String, Number> m = c.allStatistics();

		// THEN
		assertThat("All counts present, ordered", m.keySet(),
				contains("BarAverage", "BarCount", "BarMaximum", "BarMinimum", "BarTotal", "BimAverage",
						"BimCount", "BimMaximum", "BimMinimum", "BimTotal", "Foo"));
		assertThat("Bar average", m, hasEntry("BarAverage", 1.0));
		assertThat("Bar count", m, hasEntry("BarCount", 1L));
		assertThat("Bar maximum", m, hasEntry("BarMaximum", 1L));
		assertThat("Bar minimum", m, hasEntry("BarMinimum", 1L));
		assertThat("Bar total", m, hasEntry("BarTotal", 1L));

		assertThat("Bim average", m, hasEntry("BimAverage", 3.0));
		assertThat("Bim count", m, hasEntry("BimCount", 2L));
		assertThat("Bim maximum", m, hasEntry("BimMaximum", 4L));
		assertThat("Bim minimum", m, hasEntry("BimMinimum", 2L));
		assertThat("Bim total", m, hasEntry("BimTotal", 6L));

		assertThat("Foo", m, hasEntry("Foo", 2L));
	}

	@Test
	public void allStatistics_naturalOrder() {
		// GIVEN
		StatTracker c = new StatTracker("TestStats", "test", log, 5);
		c.increment("foo");
		c.add("Foo 10", 2L);
		c.add("Foo 5", 3L);
		c.add("Foo 100", 4L);
		c.add("bim", 5L);

		// WHEN
		NavigableMap<String, Number> m = c.allStatistics();

		// THEN
		// @formatter:off
		assertThat("All statistics present, ordered", m.keySet(), contains(
				"bimAverage",
				"bimCount",
				"bimMaximum",
				"bimMinimum",
				"bimTotal",

				"foo",

				"Foo 5Average",
				"Foo 5Count",
				"Foo 5Maximum",
				"Foo 5Minimum",
				"Foo 5Total",

				"Foo 10Average",
				"Foo 10Count",
				"Foo 10Maximum",
				"Foo 10Minimum",
				"Foo 10Total",

				"Foo 100Average",
				"Foo 100Count",
				"Foo 100Maximum",
				"Foo 100Minimum",
				"Foo 100Total"
				));
		// @formatter:on
	}

}
