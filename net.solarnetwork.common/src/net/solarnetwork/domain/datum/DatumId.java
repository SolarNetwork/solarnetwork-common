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

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
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
	private final ObjectDatumKind kind;

	/** The object ID. */
	private final Long objectId;

	/** The source ID. */
	private final String sourceId;

	/** The timestamp. */
	private final Instant timestamp;

	/**
	 * Create a new node datum stream ID.
	 * 
	 * @param nodeId
	 *        the node ID
	 * @param sourceId
	 *        the source ID
	 * @param timestamp
	 *        the timestamp
	 * @return the key
	 */
	public static DatumId nodeId(Long nodeId, String sourceId, Instant timestamp) {
		return new DatumId(ObjectDatumKind.Node, nodeId, sourceId, timestamp);
	}

	/**
	 * Create a new location datum stream ID.
	 * 
	 * @param locationId
	 *        the node ID
	 * @param sourceId
	 *        the source ID
	 * @param timestamp
	 *        the timestamp
	 * @return the key
	 */
	public static DatumId locationId(Long locationId, String sourceId, Instant timestamp) {
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
	public DatumId(ObjectDatumKind kind, Long objectId, String sourceId, Instant timestamp) {
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

	@Override
	public int compareTo(DatumId o) {
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
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( !(obj instanceof DatumId) ) {
			return false;
		}
		DatumId other = (DatumId) obj;
		return Objects.equals(kind, other.kind) && Objects.equals(objectId, other.objectId)
				&& Objects.equals(sourceId, other.sourceId)
				&& Objects.equals(timestamp, other.timestamp);
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

	/**
	 * Get the timestamp.
	 * 
	 * @return the timestamp
	 */
	public Instant getTimestamp() {
		return timestamp;
	}

}
