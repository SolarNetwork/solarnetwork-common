/* ==================================================================
 * BasicObjectDatumStreamMetadata.java - 5/11/2020 4:03:50 pm
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

import static net.solarnetwork.util.ObjectUtils.requireNonNullArgument;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import net.solarnetwork.domain.BasicLocation;
import net.solarnetwork.domain.Location;
import net.solarnetwork.domain.datum.DatumStreamId.DatumStreamIdent;

/**
 * Basic implementation of {@link ObjectDatumStreamMetadata}.
 *
 * @author matt
 * @version 1.3
 * @since 1.72
 */
public class BasicObjectDatumStreamMetadata extends BasicDatumStreamMetadata
		implements ObjectDatumStreamMetadata {

	private static final long serialVersionUID = -4093896601567626604L;

	/** The stream identity. */
	private final DatumStreamIdentity identity;

	/** The location. */
	private final @Nullable BasicLocation location;

	/** The JSON metadata. */
	private final @Nullable String metaJson;

	/**
	 * Create a new metadata instance with no property names.
	 *
	 * @param streamId
	 *        the stream ID
	 * @param timeZoneId
	 *        the time zone ID
	 * @param kind
	 *        the object kind
	 * @param objectId
	 *        the object ID
	 * @param sourceId
	 *        the source ID
	 * @return the new instance
	 */
	public static BasicObjectDatumStreamMetadata emptyMeta(UUID streamId, @Nullable String timeZoneId,
			ObjectDatumKind kind, Long objectId, String sourceId) {
		return new BasicObjectDatumStreamMetadata(streamId, timeZoneId, kind, objectId, sourceId, null,
				null, null, null);
	}

	/**
	 * Create a new metadata instance with no property names.
	 *
	 * @param streamId
	 *        the stream ID
	 * @param timeZoneId
	 *        the time zone ID
	 * @param identity
	 *        the identity
	 * @return the new instance
	 */
	public static BasicObjectDatumStreamMetadata emptyMeta(UUID streamId, @Nullable String timeZoneId,
			DatumStreamIdentity identity) {
		return new BasicObjectDatumStreamMetadata(streamId, timeZoneId, identity, null, null, null, null,
				null);
	}

	/**
	 * Constructor.
	 *
	 * <p>
	 * All arguments except {@code streamId}, {@code objectId}, and
	 * {@code sourceId} are allowed to be {@code null}. If any array is empty,
	 * it will be treated as if it were {@code null}.
	 * </p>
	 *
	 * @param streamId
	 *        the stream ID
	 * @param timeZoneId
	 *        the time zone ID
	 * @param kind
	 *        the object kind
	 * @param objectId
	 *        the object ID
	 * @param sourceId
	 *        the source ID
	 * @param instantaneousProperties
	 *        the instantaneous property names
	 * @param accumulatingProperties
	 *        the accumulating property names
	 * @param statusProperties
	 *        the status property names
	 * @throws IllegalArgumentException
	 *         if {@code streamId} or {@code objectId} or {@code sourceId} is
	 *         {@code null}
	 */
	public BasicObjectDatumStreamMetadata(UUID streamId, @Nullable String timeZoneId,
			ObjectDatumKind kind, Long objectId, String sourceId,
			String @Nullable [] instantaneousProperties, String @Nullable [] accumulatingProperties,
			String @Nullable [] statusProperties) {
		this(streamId, timeZoneId, kind, objectId, sourceId, null, instantaneousProperties,
				accumulatingProperties, statusProperties, null);
	}

	/**
	 * Constructor.
	 *
	 * <p>
	 * All arguments except {@code streamId}, {@code objectId}, and
	 * {@code sourceId} are allowed to be {@code null}. If any array is empty,
	 * it will be treated as if it were {@code null}.
	 * </p>
	 *
	 * @param streamId
	 *        the stream ID
	 * @param timeZoneId
	 *        the time zone ID
	 * @param kind
	 *        the object kind
	 * @param objectId
	 *        the object ID
	 * @param sourceId
	 *        the source ID
	 * @param instantaneousProperties
	 *        the instantaneous property names
	 * @param accumulatingProperties
	 *        the accumulating property names
	 * @param statusProperties
	 *        the status property names
	 * @param metaJson
	 *        the JSON metadata
	 * @throws IllegalArgumentException
	 *         if {@code streamId} or {@code objectId} or {@code sourceId} is
	 *         {@code null}
	 */
	public BasicObjectDatumStreamMetadata(UUID streamId, @Nullable String timeZoneId,
			ObjectDatumKind kind, Long objectId, String sourceId,
			String @Nullable [] instantaneousProperties, String @Nullable [] accumulatingProperties,
			String @Nullable [] statusProperties, @Nullable String metaJson) {
		this(streamId, timeZoneId, kind, objectId, sourceId, null, instantaneousProperties,
				accumulatingProperties, statusProperties, metaJson);
	}

	/**
	 * Constructor.
	 *
	 * <p>
	 * All arguments except {@code streamId}, {@code objectId}, and
	 * {@code sourceId} are allowed to be {@code null}. If any array is empty,
	 * it will be treated as if it were {@code null}.
	 * </p>
	 *
	 * @param streamId
	 *        the stream ID
	 * @param timeZoneId
	 *        the time zone ID
	 * @param kind
	 *        the object kind
	 * @param objectId
	 *        the object ID
	 * @param sourceId
	 *        the source ID
	 * @param location
	 *        the location
	 * @param instantaneousProperties
	 *        the instantaneous property names
	 * @param accumulatingProperties
	 *        the accumulating property names
	 * @param statusProperties
	 *        the status property names
	 * @param metaJson
	 *        the JSON metadata
	 * @throws IllegalArgumentException
	 *         if {@code streamId}, {@code kind}, {@code objectId}, or
	 *         {@code sourceId} is {@code null}
	 */
	public BasicObjectDatumStreamMetadata(UUID streamId, @Nullable String timeZoneId,
			ObjectDatumKind kind, Long objectId, String sourceId, @Nullable Location location,
			String @Nullable [] instantaneousProperties, String @Nullable [] accumulatingProperties,
			String @Nullable [] statusProperties, @Nullable String metaJson) {
		this(streamId, timeZoneId, new DatumStreamIdent(kind, objectId, sourceId), location,
				instantaneousProperties, accumulatingProperties, statusProperties, metaJson);
	}

	/**
	 * Constructor.
	 *
	 * <p>
	 * All arguments except {@code streamId}, {@code objectId}, and
	 * {@code sourceId} are allowed to be {@code null}. If any array is empty,
	 * it will be treated as if it were {@code null}.
	 * </p>
	 *
	 * @param streamId
	 *        the stream ID
	 * @param timeZoneId
	 *        the time zone ID
	 * @param identity
	 *        the stream identity
	 * @param instantaneousProperties
	 *        the instantaneous property names
	 * @param accumulatingProperties
	 *        the accumulating property names
	 * @param statusProperties
	 *        the status property names
	 * @param metaJson
	 *        the JSON metadata
	 * @throws IllegalArgumentException
	 *         if {@code streamId} or {@code objectId} or {@code sourceId} is
	 *         {@code null}
	 * @since 1.3
	 */
	public BasicObjectDatumStreamMetadata(UUID streamId, @Nullable String timeZoneId,
			DatumStreamIdentity identity, String @Nullable [] instantaneousProperties,
			String @Nullable [] accumulatingProperties, String @Nullable [] statusProperties,
			@Nullable String metaJson) {
		this(streamId, timeZoneId, identity, null, instantaneousProperties, accumulatingProperties,
				statusProperties, metaJson);
	}

	/**
	 * Constructor.
	 *
	 * <p>
	 * All arguments except {@code streamId}, {@code objectId}, and
	 * {@code sourceId} are allowed to be {@code null}. If any array is empty,
	 * it will be treated as if it were {@code null}.
	 * </p>
	 *
	 * @param streamId
	 *        the stream ID
	 * @param timeZoneId
	 *        the time zone ID
	 * @param identity
	 *        the stream identity
	 * @param location
	 *        the location
	 * @param instantaneousProperties
	 *        the instantaneous property names
	 * @param accumulatingProperties
	 *        the accumulating property names
	 * @param statusProperties
	 *        the status property names
	 * @param metaJson
	 *        the JSON metadata
	 * @throws IllegalArgumentException
	 *         if {@code streamId} or {@code objectId} or {@code sourceId} is
	 *         {@code null}
	 * @since 1.2
	 */
	public BasicObjectDatumStreamMetadata(UUID streamId, @Nullable String timeZoneId,
			DatumStreamIdentity identity, @Nullable Location location,
			String @Nullable [] instantaneousProperties, String @Nullable [] accumulatingProperties,
			String @Nullable [] statusProperties, @Nullable String metaJson) {
		super(streamId, timeZoneId, instantaneousProperties, accumulatingProperties, statusProperties);
		this.identity = requireNonNullArgument(identity, "identity");
		this.location = BasicLocation.locationValue(location);
		this.metaJson = metaJson;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BasicObjectDatumStreamMetadata{");
		builder.append("streamId=");
		builder.append(getStreamId());
		builder.append(", kind=");
		builder.append(identity.getKind());
		builder.append(", objectId=");
		builder.append(identity.getObjectId());
		builder.append(", sourceId=");
		builder.append(identity.getSourceId());
		if ( getPropertyNames() != null ) {
			builder.append(", propertyNames=");
			builder.append(Arrays.toString(getPropertyNames()));
		}
		builder.append("}");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(identity.getKind(), location, metaJson,
				identity.getObjectId(), identity.getSourceId());
		return result;
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( !super.equals(obj) ) {
			return false;
		}
		if ( !(obj instanceof BasicObjectDatumStreamMetadata other) ) {
			return false;
		}
		return Objects.equals(identity, other.identity) && Objects.equals(location, other.location)
				&& Objects.equals(metaJson, other.metaJson);
	}

	@Override
	public Long getObjectId() {
		return identity.getObjectId();
	}

	@Override
	public String getSourceId() {
		return identity.getSourceId();
	}

	@Override
	public @Nullable String getMetaJson() {
		return metaJson;
	}

	@Override
	public ObjectDatumKind getKind() {
		return identity.getKind();
	}

	@Override
	public @Nullable BasicLocation getLocation() {
		return location;
	}

}
