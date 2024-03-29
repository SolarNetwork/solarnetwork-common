/* ==================================================================
 * ObjectDatumStreamMetadata.java - 5/11/2020 4:01:03 pm
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

import net.solarnetwork.domain.Location;

/**
 * Object-specific (node or location) datum stream metadata.
 * 
 * @author matt
 * @version 1.0
 * @since 1.72
 */
public interface ObjectDatumStreamMetadata extends DatumStreamMetadata {

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
	 * Get the optional custom JSON metadata.
	 * 
	 * @return the JSON metadata
	 */
	String getMetaJson();

	/**
	 * Get the object datum kind.
	 * 
	 * @return the kind
	 */
	ObjectDatumKind getKind();

	/**
	 * Get the object location.
	 * 
	 * @return the location
	 */
	Location getLocation();

}
