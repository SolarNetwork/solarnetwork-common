/* ==================================================================
 * DatumIdentity.java - 11/03/2026 3:02:59 pm
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

import java.time.Instant;

/**
 * Primary key for a datum based on kind/object/source/timestamp values.
 *
 * @author matt
 * @version 1.1
 * @since 4.26
 */
public interface DatumIdentity extends DatumStreamIdentity {

	/**
	 * Get the kind.
	 *
	 * @return the kind
	 */
	@Override
	ObjectDatumKind getKind();

	/**
	 * Get the object ID.
	 *
	 * @return the object ID
	 */
	@Override
	Long getObjectId();

	/**
	 * Get the source ID.
	 *
	 * @return the source ID
	 */
	@Override
	String getSourceId();

	/**
	 * Get the timestamp.
	 *
	 * @return the timestamp
	 */
	Instant getTimestamp();

	/**
	 * Get a stand-alone datum stream identity from this datum.
	 *
	 * <p>
	 * The returned instance will <b>not</b> also implement
	 * {@code DatumIdentity} (and thus cannot be the same instance as this one).
	 * </p>
	 *
	 * @return a distinct stream identity
	 */
	default DatumStreamIdentity streamIdentity() {
		return new DatumStreamId.DatumStreamIdent(getKind(), getObjectId(), getSourceId());
	}

}
