/* ==================================================================
 * GeneralSourceMetadata.java - Oct 21, 2014 1:39:26 PM
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

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import net.solarnetwork.domain.SerializeIgnore;

/**
 * Metadata about a source.
 * 
 * @author matt
 * @version 2.0
 */
@JsonPropertyOrder({ "created", "updated", "sourceId" })
public class GeneralSourceMetadata {

	private String sourceId;
	private Instant created;
	private Instant updated;
	private GeneralDatumMetadata meta;

	/**
	 * Get the source ID.
	 * 
	 * @return the source ID
	 */
	public String getSourceId() {
		return sourceId;
	}

	/**
	 * Set the source ID.
	 * 
	 * @param sourceId
	 *        the source ID to set
	 */
	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	/**
	 * Get the created date.
	 * 
	 * @return the created date
	 */
	public Instant getCreated() {
		return created;
	}

	/**
	 * Set the created date.
	 * 
	 * @param created
	 *        the date to set
	 */
	public void setCreated(Instant created) {
		this.created = created;
	}

	/**
	 * Get the updated date.
	 * 
	 * @return the updated date
	 */
	public Instant getUpdated() {
		return updated;
	}

	/**
	 * Set the updated date.
	 * 
	 * @param updated
	 *        the date to set
	 */
	public void setUpdated(Instant updated) {
		this.updated = updated;
	}

	/**
	 * Alternative for {@link #getMeta()}. This method exists so that we can
	 * configure {@code @JsonUnwrapped} on our {@link GeneralDatumMetadata} but
	 * still support setting it in a normal, wrapped fashion via
	 * {@link #setMeta(GeneralDatumMetadata)}.
	 * 
	 * @return GeneralDatumMetadata
	 */
	@JsonUnwrapped
	public GeneralDatumMetadata getMetadata() {
		return getMeta();
	}

	/**
	 * Get the metadata.
	 * 
	 * @return the metadata
	 */
	@JsonIgnore
	@SerializeIgnore
	public GeneralDatumMetadata getMeta() {
		return meta;
	}

	/**
	 * Set the metadata.
	 * 
	 * @param meta
	 *        the metadata to set
	 */
	@JsonProperty
	public void setMeta(GeneralDatumMetadata meta) {
		this.meta = meta;
	}

	/**
	 * Shortcut for {@link GeneralDatumMetadata#setInfo(Map)}.
	 * 
	 * @param map
	 *        the Map to set
	 */
	public void setM(Map<String, Object> map) {
		if ( meta == null ) {
			meta = new GeneralDatumMetadata();
		}
		meta.setInfo(map);
	}

	/**
	 * Shortcut for {@link GeneralDatumMetadata#setPropertyInfo(Map)}.
	 * 
	 * @param map
	 *        the map to set
	 */
	public void setPm(Map<String, Map<String, Object>> map) {
		if ( meta == null ) {
			meta = new GeneralDatumMetadata();
		}
		meta.setPropertyInfo(map);
	}

	/**
	 * Shortcut for {@link GeneralDatumMetadata#setTags(Set)}.
	 * 
	 * @param set
	 *        the set to use
	 */
	public void setT(Set<String> set) {
		if ( meta == null ) {
			meta = new GeneralDatumMetadata();
		}
		meta.setTags(set);
	}

}
