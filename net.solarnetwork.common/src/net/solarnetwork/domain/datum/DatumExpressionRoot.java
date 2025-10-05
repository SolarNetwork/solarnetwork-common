/* ==================================================================
 * DatumExpressionRoot.java - 9/05/2021 11:18:13 AM
 *
 * Copyright 2021 SolarNetwork.net Dev Team
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
import java.util.Map;

/**
 * API for a datum-based expression root.
 *
 * @author matt
 * @version 1.3
 * @since 2.0
 */
public interface DatumExpressionRoot
		extends DatumDateFunctions, DatumMathFunctions, DatumStringFunctions, Map<String, Object> {

	/**
	 * Get the datum.
	 *
	 * @return the datum; may be {@literal null}
	 */
	Datum getDatum();

	/**
	 * Get the datum properties.
	 *
	 * @return the datum properties
	 */
	Map<String, ?> getProps();

	/**
	 * Get additional data.
	 *
	 * @return the data
	 */
	Map<String, ?> getData();

	/**
	 * Get a location datum's object ID.
	 *
	 * @return the datum object ID, or {@literal null} if the datum is not a
	 *         location kind or does not have a location ID
	 * @since 1.2
	 */
	default Long getLocId() {
		Datum datum = getDatum();
		return (datum != null && datum.getKind() == ObjectDatumKind.Location ? datum.getObjectId()
				: null);
	}

	/**
	 * Get a node datum's object ID.
	 *
	 * @return the datum object ID, or {@literal null} if the datum is not a
	 *         node kind or does not have a node ID
	 * @since 1.2
	 */
	default Long getNodeId() {
		Datum datum = getDatum();
		return (datum != null && datum.getKind() == ObjectDatumKind.Node ? datum.getObjectId() : null);
	}

	/**
	 * Get a datum's source ID.
	 *
	 * @return the datum source ID, or {@literal null} if the datum does not
	 *         have a source ID
	 * @since 1.2
	 */
	default String getSourceId() {
		Datum datum = getDatum();
		return (datum != null ? datum.getSourceId() : null);
	}

	/**
	 * Get a datum's timestamp.
	 *
	 * @return the datum timestamp, or {@literal null} if the datum does not
	 *         have a timestamp
	 * @since 1.2
	 */
	default Instant getTimestamp() {
		Datum datum = getDatum();
		return (datum != null ? datum.getTimestamp() : null);
	}

}
