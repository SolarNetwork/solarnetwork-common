/* ==================================================================
 * DatumStreamId.java - 5/06/2026 9:41:53 pm
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

package net.solarnetwork.domain.datum;

import static net.solarnetwork.util.ObjectUtils.requireNonNullArgument;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import net.solarnetwork.domain.BaseId;

/**
 * Primary key for a datum stream based on kind/object/source values.
 *
 * @author matt
 * @version 1.0
 * @since 4.40
 */
public class DatumStreamId extends BaseId implements Serializable, Cloneable, Comparable<DatumStreamId> {

	private static final long serialVersionUID = -5315103337212345628L;

	/** The object kind. */
	private final @Nullable ObjectDatumKind kind;

	/** The object ID. */
	private final @Nullable Long objectId;

	/** The source ID. */
	private final @Nullable String sourceId;

	/**
	 * Fully-specified version of {@code DatumStreamId}.
	 *
	 * <p>
	 * All properties will be non-{@code null} in instances of this class.
	 * </p>
	 */
	public static final class DatumStreamIdent extends DatumStreamId implements DatumStreamIdentity {

		private static final long serialVersionUID = -7031626044806453831L;

		/**
		 * Fully-specified datum identifier.
		 *
		 * @param kind
		 *        the kind
		 * @param objectId
		 *        the object ID
		 * @param sourceId
		 *        the source ID
		 * @throws IllegalArgumentException
		 *         if any argument is {@code null}
		 */
		public DatumStreamIdent(@Nullable ObjectDatumKind kind, @Nullable Long objectId,
				@Nullable String sourceId) {
			super(requireNonNullArgument(kind, "kind"), requireNonNullArgument(objectId, "objectId"),
					requireNonNullArgument(sourceId, "sourceId"));
		}

		@Override
		public DatumStreamIdentity toIdentity() {
			return this;
		}

	}

	/**
	 * Create a new node datum stream ID.
	 *
	 * <p>
	 * If all arguments are non-null then a {@link DatumStreamIdent} will be
	 * returned, so the {@link DatumStreamIdentity} API is available.
	 * </p>
	 *
	 * @param nodeId
	 *        the node ID
	 * @param sourceId
	 *        the source ID
	 * @return the key
	 */
	public static DatumStreamId nodeStreamId(@Nullable Long nodeId, @Nullable String sourceId) {
		if ( nodeId != null && sourceId != null ) {
			return new DatumStreamIdent(ObjectDatumKind.Node, nodeId, sourceId);
		}
		return new DatumStreamId(ObjectDatumKind.Node, nodeId, sourceId);
	}

	/**
	 * Create a new location datum stream ID.
	 *
	 * <p>
	 * If all arguments are non-null then a {@link DatumStreamIdent} will be
	 * returned, so the {@link DatumStreamIdentity} API is available.
	 * </p>
	 *
	 * @param locationId
	 *        the node ID
	 * @param sourceId
	 *        the source ID
	 * @return the key
	 */
	public static DatumStreamId locationStreamId(@Nullable Long locationId, @Nullable String sourceId) {
		if ( locationId != null && sourceId != null ) {
			return new DatumStreamIdent(ObjectDatumKind.Location, locationId, sourceId);
		}
		return new DatumStreamId(ObjectDatumKind.Location, locationId, sourceId);
	}

	/**
	 * Create a new datum stream ID.
	 *
	 * <p>
	 * If all arguments are non-null then a {@link DatumStreamIdent} will be
	 * returned, so the {@link DatumStreamIdentity} API is available.
	 * </p>
	 *
	 * @param kind
	 *        the kind
	 * @param objectId
	 *        the object ID
	 * @param sourceId
	 *        the source ID
	 * @return the key
	 */
	public static DatumStreamId datumStreamId(@Nullable ObjectDatumKind kind, @Nullable Long objectId,
			@Nullable String sourceId) {
		if ( kind != null && objectId != null && sourceId != null ) {
			return new DatumStreamIdent(kind, objectId, sourceId);
		}
		return new DatumStreamId(kind, objectId, sourceId);
	}

	/**
	 * Constructor.
	 *
	 * <p>
	 * Note that the
	 * {@link DatumStreamId#datumId(ObjectDatumKind, Long, String, Instant)}
	 * method should be used in preference to direct construction, so that
	 * {@link DatumStreamIdent} instances can be created when possible.
	 * </p>
	 *
	 * @param kind
	 *        the kind
	 * @param objectId
	 *        the stream object ID
	 * @param sourceId
	 *        ID the stream source ID
	 */
	public DatumStreamId(@Nullable ObjectDatumKind kind, @Nullable Long objectId,
			@Nullable String sourceId) {
		super();
		this.kind = kind;
		this.objectId = objectId;
		this.sourceId = sourceId;
	}

	@Override
	public DatumStreamId clone() {
		return (DatumStreamId) super.clone();
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
	}

	@SuppressWarnings("BoxedPrimitiveEquality")
	@Override
	public int compareTo(@Nullable DatumStreamId o) {
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
		if ( sourceId == o.sourceId ) {
			return 0;
		} else if ( sourceId == null ) {
			return 1;
		} else if ( o.sourceId == null ) {
			return -1;
		}
		return sourceId.compareTo(o.sourceId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(kind, objectId, sourceId);
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( !(obj instanceof DatumStreamId other) ) {
			return false;
		}
		return Objects.equals(kind, other.kind) && Objects.equals(objectId, other.objectId)
				&& Objects.equals(sourceId, other.sourceId);
	}

	/**
	 * Get a {@link DatumStreamIdentity} from this instance.
	 *
	 * @return the identity
	 * @throws IllegalStateException
	 *         if this instance is not fully specified as a
	 *         {@link DatumStreamIdentity}
	 */
	public DatumStreamIdentity toIdentity() {
		final ObjectDatumKind kind = this.kind;
		final Long objectId = this.objectId;
		final String sourceId = this.sourceId;
		if ( kind != null && objectId != null && sourceId != null ) {
			return new DatumStreamIdent(kind, objectId, sourceId);
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

}
