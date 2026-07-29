/* ==================================================================
 * AggregateStreamDatumTests.java - 29/07/2026 10:22:38 am
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

import static java.util.Map.entry;
import static net.solarnetwork.domain.datum.AggregateStreamDatum.END_DATE_PROPERTY_NAME;
import static net.solarnetwork.domain.datum.AggregateStreamDatum.LOCAL_DATE_PROPERTY_NAME;
import static net.solarnetwork.domain.datum.AggregateStreamDatum.LOCAL_END_DATE_PROPERTY_NAME;
import static net.solarnetwork.domain.datum.DatumPropertiesStatistics.AccumulatingStatistic.End;
import static net.solarnetwork.domain.datum.DatumPropertiesStatistics.AccumulatingStatistic.Start;
import static net.solarnetwork.domain.datum.DatumPropertiesStatistics.InstantaneousStatistic.Maximum;
import static net.solarnetwork.domain.datum.DatumPropertiesStatistics.InstantaneousStatistic.Minimum;
import static net.solarnetwork.domain.datum.DatumSamplesType.Accumulating;
import static net.solarnetwork.domain.datum.DatumSamplesType.Instantaneous;
import static net.solarnetwork.test.CommonTestUtils.randomDecimal;
import static net.solarnetwork.test.CommonTestUtils.randomLong;
import static net.solarnetwork.test.CommonTestUtils.randomString;
import static net.solarnetwork.util.DateUtils.ISO_DATE_TIME_ALT_UTC;
import static org.assertj.core.api.BDDAssertions.from;
import static org.assertj.core.api.BDDAssertions.then;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.junit.Test;
import net.solarnetwork.domain.datum.AggregateStreamDatum;
import net.solarnetwork.domain.datum.Aggregation;
import net.solarnetwork.domain.datum.BasicObjectDatumStreamMetadata;
import net.solarnetwork.domain.datum.DatumProperties;
import net.solarnetwork.domain.datum.DatumPropertiesStatistics;
import net.solarnetwork.domain.datum.DatumReadingType;
import net.solarnetwork.domain.datum.DatumSamples;
import net.solarnetwork.domain.datum.DatumSamplesType;
import net.solarnetwork.domain.datum.DatumStreamId.DatumStreamIdent;
import net.solarnetwork.domain.datum.GeneralDatum;
import net.solarnetwork.domain.datum.ObjectDatumKind;

/**
 * Test cases for the {@link AggregateStreamDatum} interface.
 *
 * @author matt
 * @version 1.0
 */
public class AggregateStreamDatumTests {

	private static final class TestAggregateStreamDatum implements AggregateStreamDatum {

		private final UUID streamId;
		private final Instant timestamp;
		private final DatumProperties properties;
		private final @Nullable Instant endTimestamp;
		private final DatumPropertiesStatistics statistics;

		private TestAggregateStreamDatum(UUID streamId, Instant timestamp, DatumProperties properties,
				@Nullable Instant endTimestamp, DatumPropertiesStatistics statistics) {
			super();
			this.streamId = streamId;
			this.timestamp = timestamp;
			this.properties = properties;
			this.endTimestamp = endTimestamp;
			this.statistics = statistics;
		}

		@Override
		public UUID getStreamId() {
			return streamId;
		}

		@Override
		public Instant getTimestamp() {
			return timestamp;
		}

		@Override
		public DatumProperties getProperties() {
			return properties;
		}

		@Override
		public @Nullable Instant getEndTimestamp() {
			return endTimestamp;
		}

		@Override
		public DatumPropertiesStatistics getStatistics() {
			return statistics;
		}

	}

	private BasicObjectDatumStreamMetadata testMeta() {
		final var streamId = UUID.randomUUID();

		final var iProps = new String[] { randomString(), randomString(), randomString() };
		final var aProps = new String[] { randomString(), randomString() };
		final var sProps = new String[] { randomString() };

		final var streamIdent = new DatumStreamIdent(ObjectDatumKind.Node, randomLong(), randomString());
		return new BasicObjectDatumStreamMetadata(streamId, "America/Los_Angeles", streamIdent, iProps,
				aProps, sProps, null);
	}

	private DatumProperties testDatumProps() {
		final var iData = new BigDecimal[] { randomDecimal(), randomDecimal(), randomDecimal() };
		final var aData = new BigDecimal[] { randomDecimal(), randomDecimal() };
		final var sData = new String[] { randomString() };

		final var properties = new DatumProperties();
		properties.setInstantaneous(iData);
		properties.setAccumulating(aData);
		properties.setStatus(sData);
		properties.setTags(new String[] { randomString(), randomString() });

		return properties;
	}

	private DatumPropertiesStatistics testStats() {
		// @formatter:off
		return DatumPropertiesStatistics.statisticsOf(new BigDecimal[][] {
			new BigDecimal[] { randomDecimal(), randomDecimal(), randomDecimal() },
			new BigDecimal[] { randomDecimal(), randomDecimal(), randomDecimal() },
			new BigDecimal[] { randomDecimal(), randomDecimal(), randomDecimal() }
		}, new BigDecimal[][] {
			new BigDecimal[] { randomDecimal(), randomDecimal(), randomDecimal() },
			new BigDecimal[] { randomDecimal(), randomDecimal(), randomDecimal() },
		});
		// @formatter:on
	}

	@Test
	public void toGeneralDatum() {
		// GIVEN
		final var meta = testMeta();
		final var props = testDatumProps();
		final var stats = testStats();
		final var ts = Instant.now().truncatedTo(ChronoUnit.SECONDS);

		final var datum = new TestAggregateStreamDatum(meta.getStreamId(), ts, props, ts.plusSeconds(1),
				stats);

		// WHEN
		final GeneralDatum result = datum.toGeneralDatum(meta);

		// THEN
		// @formatter:off
		then(result)
			.as("Stream identity kind copied")
			.returns(meta.getKind(), from(GeneralDatum::getKind))
			.as("Stream identity object ID copied")
			.returns(meta.getObjectId(), from(GeneralDatum::getObjectId))
			.as("Stream identity source ID copied")
			.returns(meta.getSourceId(), from(GeneralDatum::getSourceId))
			.as("Stream datum timestamp copied")
			.returns(datum.getTimestamp(), from(GeneralDatum::getTimestamp))
			.extracting(GeneralDatum::getSamples)
			.as("Stream datum properties copied")
			.satisfies(ds -> {
				then(ds.getInstantaneous())
					.as("Instantaneous data copied")
					.containsExactlyInAnyOrderEntriesOf(Map.of(
						meta.name(Instantaneous, 0), props.instantaneousValue(0),
						Minimum.name(meta.name(Instantaneous, 0)), stats.getInstantaneousMinimum(0),
						Maximum.name(meta.name(Instantaneous, 0)), stats.getInstantaneousMaximum(0),
						meta.name(Instantaneous, 1), props.instantaneousValue(1),
						Minimum.name(meta.name(Instantaneous, 1)), stats.getInstantaneousMinimum(1),
						Maximum.name(meta.name(Instantaneous, 1)), stats.getInstantaneousMaximum(1),
						meta.name(Instantaneous, 2), props.instantaneousValue(2),
						Minimum.name(meta.name(Instantaneous, 2)), stats.getInstantaneousMinimum(2),
						Maximum.name(meta.name(Instantaneous, 2)), stats.getInstantaneousMaximum(2)
					))
					;
				then(ds.getAccumulating())
					.as("Accumulating data copied")
					.containsExactlyInAnyOrderEntriesOf(Map.of(
						meta.name(Accumulating, 0), props.accumulatingValue(0),
						meta.name(Accumulating, 1), props.accumulatingValue(1)
					))
					;
				then(ds.getStatus())
					.as("Status data copied")
					.containsExactlyInAnyOrderEntriesOf(Map.of(
						meta.name(DatumSamplesType.Status, 0), props.statusValue(0),
						LOCAL_DATE_PROPERTY_NAME, ISO_DATE_TIME_ALT_UTC.format(
								datum.getTimestamp().atZone(ZoneId.of(meta.getTimeZoneId())).toLocalDateTime())
					))
					;
			})
			.as("Tags copied")
			.returns(Set.of(props.getTags()), from(DatumSamples::getTags))
			;
		// @formatter:on
	}

	@Test
	public void toGeneralDatum_reading() {
		// GIVEN
		final var meta = testMeta();
		final var props = testDatumProps();
		final var stats = testStats();
		final var ts = Instant.now().truncatedTo(ChronoUnit.SECONDS);

		final var datum = new TestAggregateStreamDatum(meta.getStreamId(), ts, props, ts.plusSeconds(1),
				stats);

		// WHEN
		final GeneralDatum result = datum.toGeneralDatum(meta, Aggregation.None,
				DatumReadingType.Difference);

		// THEN
		// @formatter:off
		then(result)
			.as("Stream identity kind copied")
			.returns(meta.getKind(), from(GeneralDatum::getKind))
			.as("Stream identity object ID copied")
			.returns(meta.getObjectId(), from(GeneralDatum::getObjectId))
			.as("Stream identity source ID copied")
			.returns(meta.getSourceId(), from(GeneralDatum::getSourceId))
			.as("Stream datum timestamp copied")
			.returns(datum.getTimestamp(), from(GeneralDatum::getTimestamp))
			.extracting(GeneralDatum::getSamples)
			.satisfies(ds -> {
				then(ds.getInstantaneous())
					.as("Instantaneous data copied")
					.containsExactlyInAnyOrderEntriesOf(Map.ofEntries(
						entry(meta.name(Instantaneous, 0), props.instantaneousValue(0)),
						entry(Minimum.name(meta.name(Instantaneous, 0)), stats.getInstantaneousMinimum(0)),
						entry(Maximum.name(meta.name(Instantaneous, 0)), stats.getInstantaneousMaximum(0)),
						entry(meta.name(Instantaneous, 1), props.instantaneousValue(1)),
						entry(Minimum.name(meta.name(Instantaneous, 1)), stats.getInstantaneousMinimum(1)),
						entry(Maximum.name(meta.name(Instantaneous, 1)), stats.getInstantaneousMaximum(1)),
						entry(meta.name(Instantaneous, 2), props.instantaneousValue(2)),
						entry(Minimum.name(meta.name(Instantaneous, 2)), stats.getInstantaneousMinimum(2)),
						entry(Maximum.name(meta.name(Instantaneous, 2)), stats.getInstantaneousMaximum(2)),

						// reading instant stats added
						entry(Start.name(meta.name(Accumulating, 0)), stats.getAccumulatingStart(0)),
						entry(End.name(meta.name(Accumulating, 0)), stats.getAccumulatingEnd(0)),
						entry(Start.name(meta.name(Accumulating, 1)), stats.getAccumulatingStart(1)),
						entry(End.name(meta.name(Accumulating,1)), stats.getAccumulatingEnd(1))
					))
					;
				then(ds.getAccumulating())
					.as("Accumulating data copied")
					.containsExactlyInAnyOrderEntriesOf(Map.of(
						// reading diff provided here
						meta.name(Accumulating, 0), stats.getAccumulatingDifference(0),
						meta.name(Accumulating, 1), stats.getAccumulatingDifference(1)
					))
					;
				then(ds.getStatus())
					.as("Status data copied")
					.containsExactlyInAnyOrderEntriesOf(Map.of(
						meta.name(DatumSamplesType.Status, 0), props.statusValue(0),
						LOCAL_DATE_PROPERTY_NAME, ISO_DATE_TIME_ALT_UTC.format(
								datum.getTimestamp().atZone(ZoneId.of(meta.getTimeZoneId())).toLocalDateTime()),
						END_DATE_PROPERTY_NAME, ISO_DATE_TIME_ALT_UTC.format(datum.getEndTimestamp()),
						LOCAL_END_DATE_PROPERTY_NAME, ISO_DATE_TIME_ALT_UTC.format(
								datum.getEndTimestamp().atZone(ZoneId.of(meta.getTimeZoneId())).toLocalDateTime())
					))
					;
			})
			.as("Tags copied")
			.returns(Set.of(props.getTags()), from(DatumSamples::getTags))
			;
		// @formatter:on
	}

}
