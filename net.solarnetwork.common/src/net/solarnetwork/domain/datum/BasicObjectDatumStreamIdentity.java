/* ==================================================================
 * BasicObjectDatumStreamIdentity.java - 5/03/2026 8:01:00 am
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
import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * Basic immutable implementation of {@link ObjectDatumStreamIdentity}.
 *
 * @author matt
 * @version 1.0
 * @since 4.20
 */
public class BasicObjectDatumStreamIdentity implements ObjectDatumStreamIdentity, Serializable {

	@Serial
	private static final long serialVersionUID = -8696797204329088515L;

	/** The stream ID. */
	private final UUID streamId;

	/** The kind. */
	private final ObjectDatumKind kind;

	/** The object ID. */
	private final Long objectId;

	/** The source ID. */
	private final String sourceId;

	/**
	 * Constructor.
	 *
	 * @param streamId
	 *        the stream ID
	 * @param kind
	 *        the stream kind
	 * @param objectId
	 *        the object ID
	 * @param sourceId
	 *        the source ID
	 * @throws IllegalArgumentException
	 *         if any argument is {@code null}
	 */
	public BasicObjectDatumStreamIdentity(UUID streamId, ObjectDatumKind kind, Long objectId,
			String sourceId) {
		super();
		this.streamId = requireNonNullArgument(streamId, "streamId");
		this.kind = requireNonNullArgument(kind, "kind");
		this.objectId = requireNonNullArgument(objectId, "objectId");
		this.sourceId = requireNonNullArgument(sourceId, "sourceId");
	}

	/**
	 * Create a new identity instance.
	 *
	 * @param streamId
	 *        the stream ID
	 * @param kind
	 *        the stream kind
	 * @param objectId
	 *        the object ID
	 * @param sourceId
	 *        the source ID
	 * @return the new instance
	 * @throws IllegalArgumentException
	 *         if any argument is {@code null}
	 */
	public static BasicObjectDatumStreamIdentity streamIdentity(UUID streamId, ObjectDatumKind kind,
			Long objectId, String sourceId) {
		return new BasicObjectDatumStreamIdentity(streamId, kind, objectId, sourceId);
	}

	@Override
	public final UUID getStreamId() {
		return streamId;
	}

	@Override
	public final ObjectDatumKind getKind() {
		return kind;
	}

	@Override
	public final Long getObjectId() {
		return objectId;
	}

	@Override
	public final String getSourceId() {
		return sourceId;
	}

}
