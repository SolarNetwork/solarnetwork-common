/* ==================================================================
 * StreamDatumTests.java - 6/03/2026 11:59:13 am
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

import static net.solarnetwork.domain.datum.StreamDatum.UNASSIGNED_STREAM_ID;
import static net.solarnetwork.test.CommonTestUtils.randomDecimal;
import static net.solarnetwork.test.CommonTestUtils.randomLong;
import static net.solarnetwork.test.CommonTestUtils.randomString;
import static org.assertj.core.api.BDDAssertions.from;
import static org.assertj.core.api.BDDAssertions.then;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.Test;
import net.solarnetwork.domain.datum.BasicObjectDatumStreamMetadata;
import net.solarnetwork.domain.datum.DatumProperties;
import net.solarnetwork.domain.datum.DatumSamples;
import net.solarnetwork.domain.datum.DatumStreamId.DatumStreamIdent;
import net.solarnetwork.domain.datum.GeneralDatum;
import net.solarnetwork.domain.datum.ObjectDatumKind;
import net.solarnetwork.domain.datum.StreamDatum;

/**
 * Test cases for the {@link StreamDatum} API.
 *
 * @author matt
 * @version 1.1
 */
public class StreamDatumTests {

	private static class TestStreamDatum implements StreamDatum {

		private final UUID streamId;
		private final Instant timestamp;
		private final DatumProperties properties;

		private TestStreamDatum(UUID streamId, Instant timestamp, DatumProperties properties) {
			super();
			this.streamId = streamId;
			this.timestamp = timestamp;
			this.properties = properties;
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

	}

	@Test
	public void accessors() {
		// GIVEN
		final var streamId = UUID.randomUUID();
		final var timestamp = Instant.now();
		final var properties = new DatumProperties();

		// WHEN
		final var datum = new TestStreamDatum(streamId, timestamp, properties);

		// THEN
		// @formatter:off
		then(datum)
			.as("Getter returns given stream ID")
			.returns(streamId, from(TestStreamDatum::getStreamId))
			.as("Accessor returns given stream ID")
			.returns(streamId, from(TestStreamDatum::streamId))
			.as("Stream ID is assigned")
			.returns(true, from(TestStreamDatum::streamIdIsAssigned))
			.as("Getter returns given timestamp")
			.returns(timestamp, from(TestStreamDatum::getTimestamp))
			.extracting(TestStreamDatum::getProperties)
			.as("Getter returns given properties")
			.isSameAs(properties)
			;
		// @formatter:on
	}

	@Test
	public void streamIdIsAssigned_unassigned() {
		// WHEN
		final var datum = new TestStreamDatum(UNASSIGNED_STREAM_ID, null, null);

		// THEN
		// @formatter:off
		then(datum)
			.as("Getter returns given stream ID")
			.returns(UNASSIGNED_STREAM_ID, from(TestStreamDatum::getStreamId))
			.as("Accessor returns null for unassigned ID")
			.returns(null, from(TestStreamDatum::streamId))
			.as("Stream ID is not assigned")
			.returns(false, from(TestStreamDatum::streamIdIsAssigned))
			;
		// @formatter:on
	}

	@Test
	public void toGeneralDatum() {
		// GIVEN
		final var streamId = UUID.randomUUID();
		final var timestamp = Instant.now().truncatedTo(ChronoUnit.SECONDS);

		final var iProps = new String[] { randomString(), randomString(), randomString() };
		final var aProps = new String[] { randomString(), randomString() };
		final var sProps = new String[] { randomString() };

		final var streamIdent = new DatumStreamIdent(ObjectDatumKind.Node, randomLong(), randomString());
		final var meta = new BasicObjectDatumStreamMetadata(streamId, "America/Los_Angeles", streamIdent,
				iProps, aProps, sProps, null);

		final var iData = new BigDecimal[] { randomDecimal(), randomDecimal(), randomDecimal() };
		final var aData = new BigDecimal[] { randomDecimal(), randomDecimal() };
		final var sData = new String[] { randomString() };

		final var properties = new DatumProperties();
		properties.setInstantaneous(iData);
		properties.setAccumulating(aData);
		properties.setStatus(sData);
		properties.setTags(new String[] { randomString(), randomString() });

		final var datum = new TestStreamDatum(streamId, timestamp, properties);

		// WHEN
		final GeneralDatum result = datum.toGeneralDatum(meta);

		// THEN
		// @formatter:off
		then(result)
			.as("Stream identity kind copied")
			.returns(streamIdent.getKind(), from(GeneralDatum::getKind))
			.as("Stream identity object ID copied")
			.returns(streamIdent.getObjectId(), from(GeneralDatum::getObjectId))
			.as("Stream identity source ID copied")
			.returns(streamIdent.getSourceId(), from(GeneralDatum::getSourceId))
			.as("Stream datum timestamp copied")
			.returns(datum.getTimestamp(), from(GeneralDatum::getTimestamp))
			.extracting(GeneralDatum::getSamples)
			.satisfies(ds -> {
				then(ds.getInstantaneous())
					.as("Instantaneous data copied")
					.containsExactlyInAnyOrderEntriesOf(Map.of(
						iProps[0], iData[0],
						iProps[1], iData[1],
						iProps[2], iData[2]
					))
					;
				then(ds.getAccumulating())
					.as("Accumulating data copied")
					.containsExactlyInAnyOrderEntriesOf(Map.of(
						aProps[0], aData[0],
						aProps[1], aData[1]
					))
					;
				then(ds.getStatus())
					.as("Status data copied")
					.containsExactlyInAnyOrderEntriesOf(Map.of(
						sProps[0], sData[0]
					))
					;
			})
			.as("Tags copied")
			.returns(Set.of(properties.getTags()), from(DatumSamples::getTags))
			;
		// @formatter:on
	}

}
