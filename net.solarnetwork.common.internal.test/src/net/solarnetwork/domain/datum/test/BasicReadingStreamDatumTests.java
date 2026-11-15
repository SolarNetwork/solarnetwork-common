/* ==================================================================
 * BasicReadingStreamDatumTests.java - 15/11/2026 10:41:07 am
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

package net.solarnetwork.domain.datum.test;

import static net.solarnetwork.domain.datum.DatumProperties.propertiesOf;
import static net.solarnetwork.domain.datum.DatumPropertiesStatistics.statisticsOf;
import static net.solarnetwork.util.NumberUtils.decimalArray;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenIllegalArgumentException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.Test;
import net.solarnetwork.domain.datum.AggregateStreamDatum;
import net.solarnetwork.domain.datum.BasicAggregateStreamDatum;
import net.solarnetwork.domain.datum.BasicReadingStreamDatum;
import net.solarnetwork.domain.datum.DatumProperties;
import net.solarnetwork.domain.datum.DatumPropertiesStatistics;
import net.solarnetwork.domain.datum.ReadingStreamDatum;

/**
 * Test cases for the {@link BasicReadingStreamDatum} class.
 *
 * @author matt
 * @version 1.0
 */
public class BasicReadingStreamDatumTests {

	private static DatumProperties props() {
		return propertiesOf(decimalArray("1.23"), decimalArray("30"), new String[] { "ok" },
				new String[] { "t" });
	}

	private static DatumPropertiesStatistics stats() {
		// @formatter:off
		return statisticsOf(new BigDecimal[][] {
					decimalArray("6", "1.0", "2.0")
				}, new BigDecimal[][] {
					decimalArray("30", "100", "130")
				});
		// @formatter:on
	}

	@Test
	public void construct() {
		// GIVEN
		final UUID streamId = UUID.randomUUID();
		final Instant start = Instant.now().truncatedTo(ChronoUnit.HOURS);
		final Instant end = start.plus(1, ChronoUnit.HOURS);
		final DatumProperties p = props();
		final DatumPropertiesStatistics s = stats();

		// WHEN
		BasicReadingStreamDatum d = new BasicReadingStreamDatum(streamId, start, p, end, s);

		// THEN
		// @formatter:off
		then(d)
			.as("Is a reading")
			.isInstanceOf(ReadingStreamDatum.class)
			.as("Is an aggregate")
			.isInstanceOf(AggregateStreamDatum.class)
			;
		then(d.getStreamId()).as("Stream ID").isEqualTo(streamId);
		then(d.getTimestamp()).as("Start timestamp").isEqualTo(start);
		then(d.getEndTimestamp()).as("End timestamp").isEqualTo(end);
		then(d.getProperties()).as("Properties").isSameAs(p);
		then(d.getStatistics()).as("Statistics").isSameAs(s);
		// @formatter:on
	}

	@Test
	public void construct_implicitEnd() {
		// GIVEN
		final Instant start = Instant.now().truncatedTo(ChronoUnit.HOURS);

		// WHEN
		BasicReadingStreamDatum d = new BasicReadingStreamDatum(UUID.randomUUID(), start, props(),
				null, stats());

		// THEN
		then(d.getEndTimestamp()).as("Implicit end timestamp is null").isNull();
	}

	@SuppressWarnings("NullAway")
	@Test
	public void construct_nullStatistics() {
		thenIllegalArgumentException().as("Statistics required").isThrownBy(
				() -> new BasicReadingStreamDatum(UUID.randomUUID(), Instant.now(), props(), null, null));
	}

	@Test
	public void cloneDatum() {
		// GIVEN
		final Instant start = Instant.now().truncatedTo(ChronoUnit.HOURS);
		BasicReadingStreamDatum d = new BasicReadingStreamDatum(UUID.randomUUID(), start, props(),
				start.plus(1, ChronoUnit.HOURS), stats());

		// WHEN
		BasicReadingStreamDatum c = d.clone();

		// THEN
		// @formatter:off
		then(c)
			.as("Clone is a new instance")
			.isNotSameAs(d)
			.as("Clone is equal")
			.isEqualTo(d)
			.as("Clone has the same hash")
			.hasSameHashCodeAs(d)
			;
		// @formatter:on
	}

	@Test
	public void equality() {
		// GIVEN
		final UUID streamId = UUID.randomUUID();
		final Instant start = Instant.now().truncatedTo(ChronoUnit.HOURS);
		final Instant end = start.plus(1, ChronoUnit.HOURS);

		// WHEN
		BasicReadingStreamDatum d1 = new BasicReadingStreamDatum(streamId, start, props(), end,
				stats());
		BasicReadingStreamDatum d2 = new BasicReadingStreamDatum(streamId, start, props(), end,
				stats());
		BasicReadingStreamDatum d3 = new BasicReadingStreamDatum(streamId, start, props(), null,
				stats());

		// THEN
		// @formatter:off
		then(d1)
			.as("Same values are equal")
			.isEqualTo(d2)
			.as("Same values have same hash")
			.hasSameHashCodeAs(d2)
			.as("Different end timestamp not equal")
			.isNotEqualTo(d3)
			;
		// @formatter:on
	}

	@Test
	public void equality_aggregate() {
		// GIVEN
		final UUID streamId = UUID.randomUUID();
		final Instant start = Instant.now().truncatedTo(ChronoUnit.HOURS);

		// WHEN
		BasicReadingStreamDatum r = new BasicReadingStreamDatum(streamId, start, props(), null,
				stats());
		BasicAggregateStreamDatum a = new BasicAggregateStreamDatum(streamId, start, props(), null,
				stats());

		// THEN
		then(r).as("Reading equals aggregate with same values").isEqualTo(a);
		then(a).as("Aggregate equals reading with same values").isEqualTo(r);
	}

	@Test
	public void stringValue() {
		// GIVEN
		final UUID streamId = UUID.randomUUID();
		final Instant start = Instant.parse("2026-11-15T01:00:00Z");
		final Instant end = Instant.parse("2026-11-15T02:00:00Z");
		BasicReadingStreamDatum d = new BasicReadingStreamDatum(streamId, start, props(), end,
				stats());

		// WHEN
		String s = d.toString();

		// THEN
		then(s).as("String form").isEqualTo(("BasicReadingStreamDatum{streamId=%s, "
				+ "ts=2026-11-15T01:00:00Z, endTs=2026-11-15T02:00:00Z, i=[1.23], a=[30], s=[ok], "
				+ "t=[t], si=[[6, 1.0, 2.0]], sa=[[30, 100, 130]]}").formatted(streamId));
	}

}
