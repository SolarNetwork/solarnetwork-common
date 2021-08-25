/* ==================================================================
 * StatCounterTests.java - 23/08/2021 10:41:59 AM
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

package net.solarnetwork.util.test;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import java.security.SecureRandom;
import org.junit.Before;
import org.junit.Test;
import net.solarnetwork.test.StringLogger;
import net.solarnetwork.util.StatCounter;
import net.solarnetwork.util.StatCounter.Stat;

/**
 * Test cases for the {@link StatCounter} class.
 * 
 * @author matt
 * @version 1.0
 */
public class StatCounterTests {

	public enum BasicCounts implements Stat {

		Foo("Foo"),

		Bar("Bar"),

		;

		private final String description;

		private BasicCounts(String description) {
			this.description = description;
		}

		@Override
		public int getIndex() {
			return ordinal();
		}

		@Override
		public String getDescription() {
			return description;
		}

	}

	public enum ExtraCounts implements Stat {

		Bim("Bim"),

		Bam("Bam"),

		;

		private final String description;

		private ExtraCounts(String description) {
			this.description = description;
		}

		@Override
		public int getIndex() {
			return ordinal();
		}

		@Override
		public String getDescription() {
			return description;
		}

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
		StatCounter c = new StatCounter("TestStats", "test", log, 5, BasicCounts.values(), null);

		// WHEN
		long f = 0;
		long b = 0;
		for ( int i = 0; i < 10; i++ ) {
			Stat s;
			if ( rnd.nextBoolean() ) {
				f++;
				s = BasicCounts.Foo;
			} else {
				b++;
				s = BasicCounts.Bar;
			}
			c.incrementAndGet(s);
		}

		// THEN
		assertThat("Foo final value", c.get(BasicCounts.Foo), is(f));
		assertThat("Bar final value", c.get(BasicCounts.Bar), is(b));
	}

	@Test
	public void extra_increment() {
		// GIVEN
		StatCounter c = new StatCounter("TestStats", "test", log, 5, BasicCounts.values(),
				ExtraCounts.values());

		// WHEN
		long bim = 0;
		long bam = 0;
		for ( int i = 0; i < 10; i++ ) {
			Stat s;
			if ( rnd.nextBoolean() ) {
				bim++;
				s = ExtraCounts.Bim;
			} else {
				bam++;
				s = ExtraCounts.Bam;
			}
			c.incrementAndGet(s);
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
		StatCounter c = new StatCounter("TestStats", "test", log, 5, BasicCounts.values(),
				ExtraCounts.values());

		// WHEN
		long foo = 0;
		long bar = 0;
		long bim = 0;
		long bam = 0;
		for ( int i = 0; i < 10; i++ ) {
			Stat s;
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
			c.incrementAndGet(s);
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
		StatCounter c = new StatCounter("TestStats", "test", log, 5, BasicCounts.values(),
				ExtraCounts.values());

		// WHEN
		long foo = 0;
		long bar = 0;
		long bim = 0;
		long bam = 0;
		for ( int i = 0; i < 10; i++ ) {
			Stat s;
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
			c.addAndGet(s, amount);
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
		StatCounter c = new StatCounter("TestStats", "test", log, 5, BasicCounts.values(),
				ExtraCounts.values());

		// WHEN
		for ( int i = 0; i < 10; i++ ) {
			c.incrementAndGet(BasicCounts.Foo);
		}

		// THEN
		assertThat("Foo final value", c.get(BasicCounts.Foo), is(10L));
		assertThat("2 log entries added", log.getEntries(), hasSize(2));
		assertThat("Log entry 0 count", log.getEntries().get(0).toString(), endsWith("Foo: 5"));
		assertThat("Log entry 0 count", log.getEntries().get(1).toString(), endsWith("Foo: 10"));
	}

}
