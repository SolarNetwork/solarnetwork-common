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

import java.io.Serializable;
import java.util.Objects;

/**
 * A general datum stream metadata identifier.
 * 
 * @author matt
 * @version 1.0
 * @since 1.72
 */
public class ObjectDatumStreamMetadataId implements Cloneable, Serializable {

	private static final long serialVersionUID = -5784786087066166834L;

	/** The kind. */
	private final ObjectDatumKind kind;

	/** The object ID. */
	private final Long objectId;

	/** The source ID. */
	private final String sourceId;

	/**
	 * Constructor.
	 * 
	 * @param kind
	 *        the object kind
	 * @param objectId
	 *        the object ID
	 * @param sourceId
	 *        the source ID
	 */
	public ObjectDatumStreamMetadataId(ObjectDatumKind kind, Long objectId, String sourceId) {
		super();
		this.kind = kind;
		this.objectId = objectId;
		this.sourceId = sourceId;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DatumStreamMetadataId{");
		builder.append("kind=");
		builder.append(kind);
		builder.append(", objectId=");
		builder.append(objectId);
		builder.append(", sourceId=");
		builder.append(sourceId);
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
		return Objects.hash(kind, objectId, sourceId);
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
		return kind == other.kind && Objects.equals(objectId, other.objectId)
				&& Objects.equals(sourceId, other.sourceId);
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
		return (expectedKind == kind && objectId != null && sourceId != null && !sourceId.isEmpty());
	}

	/**
	 * Get the kind.
	 * 
	 * @return the kind
	 */
	public ObjectDatumKind getKind() {
		return kind;
	}

	/**
	 * Get the object ID.
	 * 
	 * @return the object ID
	 */
	public Long getObjectId() {
		return objectId;
	}

	/**
	 * Get the source ID.
	 * 
	 * @return the source ID
	 */
	public String getSourceId() {
		return sourceId;
	}

}
