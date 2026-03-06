/* ==================================================================
 * StreamDatum.java - 22/10/2020 1:59:49 pm
 *
 * Copyright 2020 SolarNetwork.net Dev Team
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

package net.solarnetwork.domain.datum;

import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * API for an object that exists within a unique stream at a specific point in
 * time and a set of property values.
 *
 * @author matt
 * @version 1.1
 * @since 1.72
 */
public interface StreamDatum {

	/**
	 * An "unassigned" stream ID value.
	 *
	 * @since 1.1
	 */
	UUID UNASSIGNED_STREAM_ID = UUID.fromString("00000000-0000-4000-8000-000000000000");

	/**
	 * Get the unique ID of the stream this datum is a part of.
	 *
	 * @return the stream ID
	 */
	UUID getStreamId();

	/**
	 * Get the stream ID, if it is an assigned value.
	 *
	 * @return the stream ID, or {@code null} if the configured stream ID is
	 *         equal to {@link #UNASSIGNED_STREAM_ID}
	 * @since 1.1
	 */
	default @Nullable UUID streamId() {
		final UUID streamId = getStreamId();
		return (isStreamIdAssigned(streamId) ? streamId : null);
	}

	/**
	 * Get the associated timestamp of this datum.
	 *
	 * <p>
	 * This value represents the point in time the properties associated with
	 * this datum were observed, collected, inferred, predicted, etc.
	 * </p>
	 *
	 * @return the timestamp for this datum
	 */
	Instant getTimestamp();

	/**
	 * Get the properties associated with this datum.
	 *
	 * @return the properties
	 */
	DatumProperties getProperties();

	/**
	 * Test if the stream ID is assigned.
	 *
	 * @return {@code true} if {@link #getStreamId()} is not {@code null} and
	 *         not equal to {@link #UNASSIGNED_STREAM_ID}
	 * @see StreamDatum#isStreamIdAssigned(UUID)
	 * @since 1.1
	 */
	default boolean streamIdIsAssigned() {
		return isStreamIdAssigned(getStreamId());
	}

	/**
	 * Test if a stream ID is assigned.
	 *
	 * @param streamId
	 *        the stream ID to test
	 * @return {@code true} if {@code streamId} is not {@code null} and not
	 *         equal to {@link #UNASSIGNED_STREAM_ID}
	 * @since 1.1
	 */
	static boolean isStreamIdAssigned(final @Nullable UUID streamId) {
		return (streamId != null && !UNASSIGNED_STREAM_ID.equals(streamId));
	}

}
