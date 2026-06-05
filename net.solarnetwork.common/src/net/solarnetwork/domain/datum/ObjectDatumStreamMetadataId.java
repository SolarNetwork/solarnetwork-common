/* ==================================================================
 * DatumStreamMetadataId.java - 22/11/2020 9:50:39 pm
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
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import net.solarnetwork.domain.datum.DatumStreamId.DatumStreamIdent;

/**
 * A general datum stream metadata identifier.
 *
 * @author matt
 * @version 1.4
 * @since 1.72
 */
public class ObjectDatumStreamMetadataId implements Cloneable, Serializable, DatumStreamIdentity,
		Comparable<ObjectDatumStreamMetadataId> {

	@Serial
	private static final long serialVersionUID = -5784786087066166834L;

	/** The stream ID. */
	private final DatumStreamIdent streamId;

	/**
	 * Constructor.
	 *
	 * @param kind
	 *        the object kind
	 * @param objectId
	 *        the object ID
	 * @param sourceId
	 *        the source ID
	 * @throws IllegalArgumentException
	 *         if any argument is {@code null}
	 */
	public ObjectDatumStreamMetadataId(ObjectDatumKind kind, Long objectId, String sourceId) {
		super();
		this.streamId = new DatumStreamIdent(requireNonNullArgument(kind, "kind"),
				requireNonNullArgument(objectId, "objectId"),
				requireNonNullArgument(sourceId, "sourceId"));
	}

	/**
	 * Constructor.
	 *
	 * @param identity
	 *        the identity ID
	 * @throws IllegalArgumentException
	 *         if any argument is {@code null}
	 * @since 1.4
	 */
	public ObjectDatumStreamMetadataId(DatumStreamIdentity identity) {
		super();
		this.streamId = (requireNonNullArgument(identity, "identity") instanceof DatumStreamIdent ident
				? ident
				: new DatumStreamIdent(identity.getKind(), identity.getObjectId(),
						identity.getSourceId()));
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DatumStreamMetadataId{");
		builder.append("kind=");
		builder.append(streamId.getKind());
		builder.append(", objectId=");
		builder.append(streamId.getObjectId());
		builder.append(", sourceId=");
		builder.append(streamId.getSourceId());
		builder.append("}");
		return builder.toString();
	}

	@Override
	public ObjectDatumStreamMetadataId clone() {
		try {
			return (ObjectDatumStreamMetadataId) super.clone();
		} catch ( CloneNotSupportedException e ) {
			// should not get here
			throw new RuntimeException(e);
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(streamId);
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( !(obj instanceof ObjectDatumStreamMetadataId) ) {
			return false;
		}
		ObjectDatumStreamMetadataId other = (ObjectDatumStreamMetadataId) obj;
		return Objects.equals(streamId, other.streamId);
	}

	@SuppressWarnings("BoxedPrimitiveEquality")
	@Override
	public int compareTo(@Nullable ObjectDatumStreamMetadataId o) {
		if ( this == o ) {
			return 0;
		}
		if ( o == null ) {
			return -1;
		}
		return streamId.compareTo(o.streamId);
	}

	/**
	 * Test if this ID is fully specified.
	 *
	 * @param expectedKind
	 *        the kind to match
	 * @return {@literal true} if {@code expectedKind} is the same as this
	 *         object's {@code kind} and {@code objectId}, {@code sourceId} are
	 *         all non-null and non-empty
	 */
	public boolean isValidDatumStreamMetadataId(ObjectDatumKind expectedKind) {
		return (streamId.hasIdentity() && expectedKind == streamId.getKind());
	}

	/**
	 * Get the kind.
	 *
	 * @return the kind
	 */
	@Override
	public ObjectDatumKind getKind() {
		return streamId.toIdentity().getKind();
	}

	/**
	 * Get the object ID.
	 *
	 * @return the object ID
	 */
	@Override
	public Long getObjectId() {
		return streamId.toIdentity().getObjectId();
	}

	/**
	 * Get the source ID.
	 *
	 * @return the source ID
	 */
	@Override
	public String getSourceId() {
		return streamId.toIdentity().getSourceId();
	}

}
