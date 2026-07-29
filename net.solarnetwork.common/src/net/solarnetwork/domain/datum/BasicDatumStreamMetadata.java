/* ==================================================================
 * BasicDatumStreamMetadata.java - 22/10/2020 3:07:55 pm
 *
 * Copyright 2020 SolarNetwork.net Dev Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU  Public License as
 * published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Public License for more details.
 *
 * You should have received a copy of the GNU  Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * ==================================================================
 */

package net.solarnetwork.domain.datum;

import static net.solarnetwork.util.ObjectUtils.requireNonNullArgument;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Implementation of {@link DatumStreamMetadata}.
 *
 * @author matt
 * @version 2.1
 * @since 1.72
 */
public class BasicDatumStreamMetadata implements DatumStreamMetadata, Serializable {

	private static final long serialVersionUID = -6339837836211488890L;

	/** The stream ID. */
	private final UUID streamId;

	/** The time zone ID. */
	private final @Nullable String timeZoneId;

	/** The instantaneous property names. */
	private final String @Nullable [] instantaneousProperties;

	/** The accumulating property names. */
	private final String @Nullable [] accumulatingProperties;

	/** The status property names. */
	private final String @Nullable [] statusProperties;

	/**
	 * Constructor.
	 *
	 * <p>
	 * All arguments except {@code streamId} are allowed to be {@code null}. If
	 * any array is empty, it will be treated as if it were {@code null}.
	 * </p>
	 *
	 * @param streamId
	 *        the stream ID
	 * @param timeZoneId
	 *        the time zone ID
	 * @param instantaneousProperties
	 *        the instantaneous property names
	 * @param accumulatingProperties
	 *        the accumulating property names
	 * @param statusProperties
	 *        the status property names
	 * @throws IllegalArgumentException
	 *         if {@code streamId} is {@code null}
	 */
	public BasicDatumStreamMetadata(UUID streamId, @Nullable String timeZoneId,
			String @Nullable [] instantaneousProperties, String @Nullable [] accumulatingProperties,
			String @Nullable [] statusProperties) {
		super();
		this.streamId = requireNonNullArgument(streamId, "streamId");
		this.timeZoneId = timeZoneId;
		this.instantaneousProperties = instantaneousProperties != null
				&& instantaneousProperties.length > 0 ? instantaneousProperties : null;
		this.accumulatingProperties = accumulatingProperties != null && accumulatingProperties.length > 0
				? accumulatingProperties
				: null;
		this.statusProperties = statusProperties != null && statusProperties.length > 0
				? statusProperties
				: null;
	}

	/**
	 * Constructor.
	 *
	 * <p>
	 * All arguments except {@code streamId} are allowed to be {@code null}. The
	 * other arguments are {@code Object} to work around MyBatis mapping issues.
	 * If any array is empty, it will be treated as if it were {@code null}.
	 * </p>
	 *
	 * @param streamId
	 *        the stream ID
	 * @param timeZoneId
	 *        the time zone ID
	 * @param instantaneousProperties
	 *        the instantaneous property names; must be a {@code String[]}
	 * @param accumulatingProperties
	 *        the accumulating property names; must be a {@code String[]}
	 * @param statusProperties
	 *        the status property names; must be a {@code String[]}
	 * @throws IllegalArgumentException
	 *         if {@code streamId} is {@code null}
	 */
	public BasicDatumStreamMetadata(UUID streamId, @Nullable String timeZoneId,
			@Nullable Object instantaneousProperties, @Nullable Object accumulatingProperties,
			@Nullable Object statusProperties) {
		this(streamId, timeZoneId, (String @Nullable []) instantaneousProperties,
				(String @Nullable []) accumulatingProperties, (String @Nullable []) statusProperties);
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
	public String @Nullable [] propertyNamesForType(@Nullable DatumSamplesType type) {
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(accumulatingProperties);
		result = prime * result + Arrays.hashCode(instantaneousProperties);
		result = prime * result + Arrays.hashCode(statusProperties);
		result = prime * result + Objects.hash(streamId, timeZoneId);
		return result;
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( !(obj instanceof BasicDatumStreamMetadata other) ) {
			return false;
		}
		return Arrays.equals(accumulatingProperties, other.accumulatingProperties)
				&& Arrays.equals(instantaneousProperties, other.instantaneousProperties)
				&& Arrays.equals(statusProperties, other.statusProperties)
				&& Objects.equals(streamId, other.streamId)
				&& Objects.equals(timeZoneId, other.timeZoneId);
	}

}
