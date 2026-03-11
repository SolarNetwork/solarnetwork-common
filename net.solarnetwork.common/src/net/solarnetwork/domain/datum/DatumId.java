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
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import net.solarnetwork.domain.BaseId;

/**
 * Primary key for a datum based on kind/object/source/timestamp values.
 *
 * @author matt
 * @version 2.0
 * @since 1.71
 */
public class DatumId extends BaseId implements Serializable, Cloneable, Comparable<DatumId> {

	private static final long serialVersionUID = 6891814538805568843L;

	/** The object kind. */
	private final @Nullable ObjectDatumKind kind;

	/** The object ID. */
	private final @Nullable Long objectId;

	/** The source ID. */
	private final @Nullable String sourceId;

	/** The timestamp. */
	private final @Nullable Instant timestamp;

	/**
	 * Fully-specified version of {@code DatumId}.
	 *
	 * <p>
	 * All properties will be non-{@code null} in instances of this class.
	 * </p>
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
			super(requireNonNullArgument(kind, "kind"), requireNonNullArgument(objectId, "objectId"),
					requireNonNullArgument(sourceId, "sourceId"),
					requireNonNullArgument(timestamp, "timestamp"));
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
	 * Constructor.
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
		this.kind = kind;
		this.objectId = objectId;
		this.sourceId = sourceId;
		this.timestamp = timestamp;
	}

	@Override
	public DatumId clone() {
		return (DatumId) super.clone();
	}

	@Override
	protected void populateIdValue(StringBuilder buf) {
		buf.append("k=");
		if ( kind != null ) {
			buf.append(kind.getKey());
		}
		buf.append("o=");
		if ( objectId != null ) {
			buf.append(objectId);
		}
		buf.append("s=");
		if ( sourceId != null ) {
			buf.append(sourceId);
		}
		buf.append(";t=");
		if ( timestamp != null ) {
			buf.append(timestamp.getEpochSecond()).append('.').append(timestamp.getNano());
		}
	}

	@Override
	protected void populateStringValue(StringBuilder buf) {
		if ( kind != null ) {
			if ( buf.length() > 0 ) {
				buf.append(", ");
			}
			buf.append("kind=");
			buf.append(kind);
		}
		if ( objectId != null ) {
			if ( buf.length() > 0 ) {
				buf.append(", ");
			}
			buf.append("objectId=");
			buf.append(objectId);
		}
		if ( sourceId != null ) {
			if ( buf.length() > 0 ) {
				buf.append(", ");
			}
			buf.append("sourceId=");
			buf.append(sourceId);
		}
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
		int result = 0;
		if ( kind != o.kind ) {
			if ( kind == null ) {
				return 1;
			} else if ( o.kind == null ) {
				return -1;
			}
			result = kind.compareTo(o.kind);
			if ( result != 0 ) {
				return result;
			}
		}
		if ( objectId != o.objectId ) {
			if ( objectId == null ) {
				return 1;
			} else if ( o.objectId == null ) {
				return -1;
			}
			result = objectId.compareTo(o.objectId);
			if ( result != 0 ) {
				return result;
			}
		}
		if ( sourceId != o.sourceId ) {
			if ( sourceId == null ) {
				return 1;
			} else if ( o.sourceId == null ) {
				return -1;
			}
			result = sourceId.compareTo(o.sourceId);
			if ( result != 0 ) {
				return result;
			}
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
		return Objects.hash(kind, objectId, sourceId, timestamp);
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( !(obj instanceof DatumId other) ) {
			return false;
		}
		return Objects.equals(kind, other.kind) && Objects.equals(objectId, other.objectId)
				&& Objects.equals(sourceId, other.sourceId)
				&& Objects.equals(timestamp, other.timestamp);
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
		final ObjectDatumKind kind = this.kind;
		final Long objectId = this.objectId;
		final String sourceId = this.sourceId;
		final Instant timestamp = this.timestamp;
		if ( kind != null && objectId != null && sourceId != null && timestamp != null ) {
			return new DatumIdent(kind, objectId, sourceId, timestamp);
		}
		throw new IllegalStateException("Datum identity not available.");
	}

	/**
	 * Get the kind.
	 *
	 * @return the kind
	 */
	public @Nullable ObjectDatumKind getKind() {
		return kind;
	}

	/**
	 * Get the object ID.
	 *
	 * @return the object ID
	 */
	public @Nullable Long getObjectId() {
		return objectId;
	}

	/**
	 * Get the source ID.
	 *
	 * @return the source ID
	 */
	public @Nullable String getSourceId() {
		return sourceId;
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
