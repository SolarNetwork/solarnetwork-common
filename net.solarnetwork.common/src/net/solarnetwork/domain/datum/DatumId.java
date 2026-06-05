/* ==================================================================
 * DatumStreamId.java - 21/11/2020 9:40:24 pm
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
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import net.solarnetwork.domain.BaseId;
import net.solarnetwork.domain.datum.DatumStreamId.DatumStreamIdent;

/**
 * Primary key for a datum based on kind/object/source/timestamp values.
 *
 * @author matt
 * @version 2.2
 * @since 1.71
 */
public sealed class DatumId extends BaseId implements Serializable, Cloneable, Comparable<DatumId>
		permits DatumId.DatumIdent {

	@Serial
	private static final long serialVersionUID = 6891814538805568843L;

	/** The stream ID. */
	protected final DatumStreamId streamId;

	/** The timestamp. */
	private final @Nullable Instant timestamp;

	/**
	 * Fully-specified version of {@code DatumId}.
	 *
	 * <p>
	 * All properties will be non-{@code null} in instances of this class.
	 * </p>
	 *
	 * @since 2.1
	 */
	public static final class DatumIdent extends DatumId implements DatumIdentity {

		private static final long serialVersionUID = 73513046710808672L;

		/**
		 * Fully-specified datum identifier.
		 *
		 * @param kind
		 *        the kind
		 * @param objectId
		 *        the object ID
		 * @param sourceId
		 *        the source ID
		 * @param timestamp
		 *        the timestamp
		 * @throws IllegalArgumentException
		 *         if any argument is {@code null}
		 */
		public DatumIdent(@Nullable ObjectDatumKind kind, @Nullable Long objectId,
				@Nullable String sourceId, @Nullable Instant timestamp) {
			this(new DatumStreamIdent(requireNonNullArgument(kind, "kind"),
					requireNonNullArgument(objectId, "objectId"),
					requireNonNullArgument(sourceId, "sourceId")),
					requireNonNullArgument(timestamp, "timestamp"));
		}

		/**
		 * Fully-specified datum identifier.
		 *
		 * @param streamId
		 *        the stream ID
		 * @param timestamp
		 *        the timestamp
		 * @throws IllegalArgumentException
		 *         if any argument is {@code null}
		 */
		public DatumIdent(DatumStreamId streamId, @Nullable Instant timestamp) {
			super(requireNonNullArgument(streamId, "streamId"),
					requireNonNullArgument(timestamp, "timestamp"));
			// verify that streamId is also DatumStreamIdentity
			streamId.toIdentity();
		}

		@Override
		public DatumStreamIdentity streamIdentity() {
			return streamId.toIdentity();
		}

		@Override
		public boolean hasIdentity() {
			return true;
		}

		@Override
		public DatumIdentity toIdentity() {
			return this;
		}

	}

	/**
	 * Create a new node datum stream ID.
	 *
	 * <p>
	 * If all arguments are non-null then a {@link DatumIdent} will be returned,
	 * so the {@link DatumIdentity} API is available.
	 * </p>
	 *
	 * @param nodeId
	 *        the node ID
	 * @param sourceId
	 *        the source ID
	 * @param timestamp
	 *        the timestamp
	 * @return the key
	 */
	public static DatumId nodeId(@Nullable Long nodeId, @Nullable String sourceId,
			@Nullable Instant timestamp) {
		if ( nodeId != null && sourceId != null && timestamp != null ) {
			return new DatumIdent(ObjectDatumKind.Node, nodeId, sourceId, timestamp);
		}
		return new DatumId(ObjectDatumKind.Node, nodeId, sourceId, timestamp);
	}

	/**
	 * Create a new location datum stream ID.
	 *
	 * <p>
	 * If all arguments are non-null then a {@link DatumIdent} will be returned,
	 * so the {@link DatumIdentity} API is available.
	 * </p>
	 *
	 * @param locationId
	 *        the node ID
	 * @param sourceId
	 *        the source ID
	 * @param timestamp
	 *        the timestamp
	 * @return the key
	 */
	public static DatumId locationId(@Nullable Long locationId, @Nullable String sourceId,
			@Nullable Instant timestamp) {
		if ( locationId != null && sourceId != null && timestamp != null ) {
			return new DatumIdent(ObjectDatumKind.Location, locationId, sourceId, timestamp);
		}
		return new DatumId(ObjectDatumKind.Location, locationId, sourceId, timestamp);
	}

	/**
	 * Create a new datum stream ID.
	 *
	 * <p>
	 * If all arguments are non-null then a {@link DatumIdent} will be returned,
	 * so the {@link DatumIdentity} API is available.
	 * </p>
	 *
	 * @param kind
	 *        the kind
	 * @param objectId
	 *        the object ID
	 * @param sourceId
	 *        the source ID
	 * @param timestamp
	 *        the timestamp
	 * @return the key
	 * @since 2.1
	 */
	public static DatumId datumId(@Nullable ObjectDatumKind kind, @Nullable Long objectId,
			@Nullable String sourceId, @Nullable Instant timestamp) {
		return datumId(DatumStreamId.datumStreamId(kind, objectId, sourceId), timestamp);
	}

	/**
	 * Create a new datum stream ID.
	 *
	 * <p>
	 * If the {@code streamId} properties are all non-null and {@code timestamp}
	 * is non-null then a {@link DatumIdent} will be returned, so the
	 * {@link DatumIdentity} API is available.
	 * </p>
	 *
	 * @param streamId
	 *        the stream ID
	 * @param timestamp
	 *        the timestamp
	 * @return the key
	 * @since 2.2
	 */
	public static DatumId datumId(DatumStreamId streamId, @Nullable Instant timestamp) {
		if ( streamId.getKind() != null && streamId.getObjectId() != null
				&& streamId.getSourceId() != null && timestamp != null ) {
			return new DatumIdent(streamId instanceof DatumStreamIdentity ? streamId
					: new DatumStreamIdent(streamId.getKind(), streamId.getObjectId(),
							streamId.getSourceId()),
					timestamp);
		}
		return new DatumId(streamId, timestamp);
	}

	/**
	 * Constructor.
	 *
	 * <p>
	 * Note that the
	 * {@link DatumId#datumId(ObjectDatumKind, Long, String, Instant)} method
	 * should be used in preference to direct construction, so that
	 * {@link DatumIdent} instances can be created when possible.
	 * </p>
	 *
	 * @param kind
	 *        the kind
	 * @param objectId
	 *        the stream object ID
	 * @param sourceId
	 *        ID the stream source ID
	 * @param timestamp
	 *        the time stamp
	 */
	public DatumId(@Nullable ObjectDatumKind kind, @Nullable Long objectId, @Nullable String sourceId,
			@Nullable Instant timestamp) {
		super();
		this.streamId = DatumStreamId.datumStreamId(kind, objectId, sourceId);
		this.timestamp = timestamp;
	}

	/**
	 * Constructor.
	 *
	 * <p>
	 * Note that the {@link DatumId#datumId(DatumStreamId, Instant)} method
	 * should be used in preference to direct construction, so that
	 * {@link DatumIdent} instances can be created when possible.
	 * </p>
	 *
	 * @param streamId
	 *        the stream ID
	 * @param timestamp
	 *        the time stamp
	 * @throws IllegalArgumentException
	 *         if {@code streamId} is {@code null}
	 * @since 2.2
	 */
	public DatumId(DatumStreamId streamId, @Nullable Instant timestamp) {
		super();
		this.streamId = requireNonNullArgument(streamId, "streamId");
		this.timestamp = timestamp;
	}

	@Override
	public DatumId clone() {
		return (DatumId) super.clone();
	}

	@Override
	protected void populateIdValue(StringBuilder buf) {
		streamId.populateIdValue(buf);
		buf.append(";t=");
		if ( timestamp != null ) {
			buf.append(timestamp.getEpochSecond()).append('.').append(timestamp.getNano());
		}
	}

	@Override
	protected void populateStringValue(StringBuilder buf) {
		streamId.populateStringValue(buf);
		if ( timestamp != null ) {
			if ( buf.length() > 0 ) {
				buf.append(", ");
			}
			buf.append("timestamp=");
			buf.append(timestamp);
		}
	}

	@SuppressWarnings("BoxedPrimitiveEquality")
	@Override
	public int compareTo(@Nullable DatumId o) {
		if ( this == o ) {
			return 0;
		}
		if ( o == null ) {
			return -1;
		}
		int result = streamId.compareTo(o.streamId);
		if ( result != 0 ) {
			return result;
		}
		if ( timestamp == o.timestamp ) {
			return 0;
		} else if ( timestamp == null ) {
			return 1;
		} else if ( o.timestamp == null ) {
			return -1;
		}
		return timestamp.compareTo(o.timestamp);
	}

	@Override
	public int hashCode() {
		return Objects.hash(streamId, timestamp);
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( !(obj instanceof DatumId other) ) {
			return false;
		}
		return Objects.equals(streamId, other.streamId) && Objects.equals(timestamp, other.timestamp);
	}

	/**
	 * Get a {@link DatumStreamIdentity} from this instance.
	 *
	 * @return the stream identity
	 * @throws IllegalStateException
	 *         if this instance is not fully specified as a
	 *         {@link DatumIdentity}
	 * @since 2.2
	 */
	public DatumStreamIdentity streamIdentity() {
		return streamId.toIdentity();
	}

	/**
	 * Test if this ID is fully specified.
	 *
	 * @return {@literal true} if {@code kind}, {@code objectId},
	 *         {@code sourceId}, and {@code timestamp} are all non-null and
	 *         non-empty
	 * @since 2.2
	 */
	public boolean hasIdentity() {
		return (streamId.hasIdentity() && timestamp != null);
	}

	/**
	 * Get a {@link DatumIdentity} from this instance.
	 *
	 * @return the identity
	 * @throws IllegalStateException
	 *         if this instance is not fully specified as a
	 *         {@link DatumIdentity}
	 */
	public DatumIdentity toIdentity() {
		final DatumStreamIdentity streamIdent = streamId.toIdentity();
		final Instant timestamp = this.timestamp;
		if ( timestamp != null ) {
			return new DatumIdent(streamIdent instanceof DatumStreamIdent dsi ? dsi
					: new DatumStreamIdent(streamIdent.getKind(), streamIdent.getObjectId(),
							streamIdent.getSourceId()),
					timestamp);
		}
		throw new IllegalStateException("Datum identity not available.");
	}

	/**
	 * Get the kind.
	 *
	 * @return the kind
	 */
	public @Nullable ObjectDatumKind getKind() {
		return streamId.getKind();
	}

	/**
	 * Get the object ID.
	 *
	 * @return the object ID
	 */
	public @Nullable Long getObjectId() {
		return streamId.getObjectId();
	}

	/**
	 * Get the source ID.
	 *
	 * @return the source ID
	 */
	public @Nullable String getSourceId() {
		return streamId.getSourceId();
	}

	/**
	 * Get the timestamp.
	 *
	 * @return the timestamp
	 */
	public @Nullable Instant getTimestamp() {
		return timestamp;
	}

}
