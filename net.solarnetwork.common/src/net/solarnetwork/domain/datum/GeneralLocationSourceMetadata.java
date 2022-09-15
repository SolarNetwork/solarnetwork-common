/* ==================================================================
 * GeneralLocationSourceMetadata.java - Oct 21, 2014 1:37:21 PM
 * 
 * Copyright 2007-2014 SolarNetwork.net Dev Team
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.solarnetwork.domain.Location;
import net.solarnetwork.domain.SimpleLocation;

/**
 * Metadata about a source associated with a location.
 * 
 * @author matt
 * @version 1.1
 */
@JsonPropertyOrder({ "created", "updated", "locationId", "sourceId", "location" })
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeneralLocationSourceMetadata extends GeneralSourceMetadata {

	private Long locationId;

	private Location location;

	/**
	 * Get the location ID.
	 * 
	 * @return the location ID
	 */
	public Long getLocationId() {
		return locationId;
	}

	/**
	 * Set the location ID.
	 * 
	 * @param locationId
	 *        the location ID to set
	 */
	public void setLocationId(Long locationId) {
		this.locationId = locationId;
	}

	/**
	 * Get the location.
	 * 
	 * @return the location
	 */
	public Location getLocation() {
		return location;
	}

	/**
	 * Set the location.
	 * 
	 * @param location
	 *        the location to set
	 */
	@JsonDeserialize(as = SimpleLocation.class)
	public void setLocation(Location location) {
		this.location = location;
	}

}
