/* ==================================================================
 * DatumStreamMetadataTests.java - 29/07/2026 10:49:22 am
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

import static java.util.UUID.randomUUID;
import static net.solarnetwork.test.CommonTestUtils.randomString;
import static org.assertj.core.api.BDDAssertions.then;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.junit.Test;
import net.solarnetwork.domain.datum.DatumSamplesType;
import net.solarnetwork.domain.datum.DatumStreamMetadata;

/**
 * Test cases for the {@link DatumStreamMetadata} interface.
 *
 * @author matt
 * @version 1.0
 */
public class DatumStreamMetadataTests {

	private static final class TestMetadata implements DatumStreamMetadata {

		private final UUID streamId;
		private final @Nullable String timeZoneId;
		private final String @Nullable [] instantaneousProperties;
		private final String @Nullable [] accumulatingProperties;
		private final String @Nullable [] statusProperties;

		private TestMetadata(UUID streamId, @Nullable String timeZoneId,
				String @Nullable [] instantaneousProperties, String @Nullable [] accumulatingProperties,
				String @Nullable [] statusProperties) {
			super();
			this.streamId = streamId;
			this.timeZoneId = timeZoneId;
			this.instantaneousProperties = instantaneousProperties;
			this.accumulatingProperties = accumulatingProperties;
			this.statusProperties = statusProperties;
		}

		@Override
		public UUID getStreamId() {
			return streamId;
		}

		@Override
		public @Nullable String getTimeZoneId() {
			return timeZoneId;
		}

		@Override
		public String @Nullable [] propertyNamesForType(DatumSamplesType type) {
			if ( type == null ) {
				return null;
			}
			return switch (type) {
				case Instantaneous -> instantaneousProperties;
				case Accumulating -> accumulatingProperties;
				case Status -> statusProperties;
				default -> null;
			};
		}

	}

	@Test
	public void propertyNames() {
		// GIVEN
		final var iProps = new String[] { randomString(), randomString(), randomString() };
		final var aProps = new String[] { randomString(), randomString() };
		final var sProps = new String[] { randomString() };

		final var meta = new TestMetadata(randomUUID(), "UTC", iProps, aProps, sProps);

		// THEN
		// @formatter:off
		then(meta.getPropertyNames())
			.as("Property name array generated from all types")
			.containsExactly(iProps[0], iProps[1], iProps[2], aProps[0], aProps[1], sProps[0])
			;
		then(meta.getInstantaneousLength())
			.as("Instantaneous property names length returned")
			.isEqualTo(3)
			;
		then(meta.getAccumulatingLength())
			.as("Accumualting property names length returned")
			.isEqualTo(2)
			;
		then(meta.getStatusLength())
			.as("Status property names length returned")
			.isEqualTo(1)
			;
		for ( int i = 0; i < iProps.length; i++ ) {
			then(meta.propertyName(DatumSamplesType.Instantaneous, i))
				.as("Instantaneous property %d name resolved", i)
				.isEqualTo(iProps[i])
				;
		}
		then(meta.propertyName(DatumSamplesType.Instantaneous, iProps.length))
			.as("Out-of-bounds instantaneous property resolves as null")
			.isNull()
			;
		for ( int i = 0; i < aProps.length; i++ ) {
			then(meta.propertyName(DatumSamplesType.Accumulating, i))
				.as("Accumulting property %d name resolved", i)
				.isEqualTo(aProps[i])
				;
		}
		then(meta.propertyName(DatumSamplesType.Accumulating, aProps.length))
			.as("Out-of-bounds accumulating property resolves as null")
			.isNull()
			;
		for ( int i = 0; i < sProps.length; i++ ) {
			then(meta.propertyName(DatumSamplesType.Status, i))
				.as("Status property name %d resolved", i)
				.isEqualTo(sProps[i])
				;
		}
		then(meta.propertyName(DatumSamplesType.Status, sProps.length))
			.as("Out-of-bounds status property resolves as null")
			.isNull()
			;
		// @formatter:on
	}

	@Test
	public void propertyNames_noInstantaneous() {
		// GIVEN
		final var aProps = new String[] { randomString(), randomString() };
		final var sProps = new String[] { randomString() };

		final var meta = new TestMetadata(randomUUID(), "UTC", null, aProps, sProps);

		// THEN
		// @formatter:off
		then(meta.getPropertyNames())
			.as("Property name array generated from all types")
			.containsExactly(aProps[0], aProps[1], sProps[0])
			;
		then(meta.getInstantaneousLength())
			.as("Instantaneous property names length returned")
			.isEqualTo(0)
			;
		then(meta.getAccumulatingLength())
			.as("Accumualting property names length returned")
			.isEqualTo(2)
			;
		then(meta.getStatusLength())
			.as("Status property names length returned")
			.isEqualTo(1)
			;
		// @formatter:on
	}

	@Test
	public void propertyNames_noAccumulating() {
		// GIVEN
		final var iProps = new String[] { randomString(), randomString(), randomString() };
		final var sProps = new String[] { randomString() };

		final var meta = new TestMetadata(randomUUID(), "UTC", iProps, null, sProps);

		// THEN
		// @formatter:off
		then(meta.getPropertyNames())
			.as("Property name array generated from all types")
			.containsExactly(iProps[0], iProps[1], iProps[2], sProps[0])
			;
		then(meta.getInstantaneousLength())
			.as("Instantaneous property names length returned")
			.isEqualTo(3)
			;
		then(meta.getAccumulatingLength())
			.as("Accumualting property names length returned")
			.isEqualTo(0)
			;
		then(meta.getStatusLength())
			.as("Status property names length returned")
			.isEqualTo(1)
			;
		// @formatter:on
	}

	@Test
	public void propertyNames_noStatus() {
		// GIVEN
		final var iProps = new String[] { randomString(), randomString(), randomString() };
		final var aProps = new String[] { randomString(), randomString() };

		final var meta = new TestMetadata(randomUUID(), "UTC", iProps, aProps, null);

		// THEN
		// @formatter:off
		then(meta.getPropertyNames())
			.as("Property name array generated from all types")
			.containsExactly(iProps[0], iProps[1], iProps[2], aProps[0], aProps[1])
			;
		then(meta.getInstantaneousLength())
			.as("Instantaneous property names length returned")
			.isEqualTo(3)
			;
		then(meta.getAccumulatingLength())
			.as("Accumualting property names length returned")
			.isEqualTo(2)
			;
		then(meta.getStatusLength())
			.as("Status property names length returned")
			.isEqualTo(0)
			;
		// @formatter:on
	}

	@Test
	public void propertyNames_empty() {
		// GIVEN
		final var meta = new TestMetadata(randomUUID(), "UTC", null, null, null);

		// THEN
		// @formatter:off
		then(meta.getPropertyNames())
			.as("Property name array null if no properties available")
			.isNull()
			;
		then(meta.getInstantaneousLength())
			.as("Instantaneous property names length returned")
			.isEqualTo(0)
			;
		then(meta.getAccumulatingLength())
			.as("Accumualting property names length returned")
			.isEqualTo(0)
			;
		then(meta.getStatusLength())
			.as("Status property names length returned")
			.isEqualTo(0)
			;
		// @formatter:on
	}

}
