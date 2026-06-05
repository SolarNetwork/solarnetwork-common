/* ==================================================================
 * DatumStreamIdentity.java - 5/06/2026 9:38:32 pm
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
import net.solarnetwork.domain.datum.DatumId.DatumIdent;

/**
 * Identity for a stream of datum (over time).
 *
 * @author matt
 * @version 1.1
 */
public interface DatumStreamIdentity {

	/**
	 * Get the object datum kind.
	 *
	 * @return the kind
	 */
	ObjectDatumKind getKind();

	/**
	 * Get the object ID.
	 *
	 * @return the object ID
	 */
	Long getObjectId();

	/**
	 * Get the source ID.
	 *
	 * @return the source ID
	 */
	String getSourceId();

	/**
	 * Get a datum identity for a specific timestamp.
	 *
	 * @param timestamp
	 *        the timestamp
	 * @return the datum identity
	 * @since 1.1
	 */
	default DatumIdentity datumIdentity(Instant timestamp) {
		return new DatumIdent(getKind(), getObjectId(), getSourceId(), timestamp);
	}

}
