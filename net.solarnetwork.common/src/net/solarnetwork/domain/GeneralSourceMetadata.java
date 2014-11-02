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

package net.solarnetwork.domain;

import java.util.Map;
import java.util.Set;
import net.solarnetwork.util.SerializeIgnore;
import org.joda.time.DateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

/**
 * Metadata about a source.
 * 
 * @author matt
 * @version 1.0
 */
@JsonPropertyOrder({ "created", "updated", "sourceId" })
public class GeneralSourceMetadata {

	private String sourceId;
	private DateTime created;
	private DateTime updated;
	private GeneralDatumMetadata meta;

	public String getSourceId() {
		return sourceId;
	}

	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	public DateTime getCreated() {
		return created;
	}

	public void setCreated(DateTime created) {
		this.created = created;
	}

	public DateTime getUpdated() {
		return updated;
	}

	public void setUpdated(DateTime updated) {
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

	@JsonIgnore
	@SerializeIgnore
	public GeneralDatumMetadata getMeta() {
		return meta;
	}

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
	 * @param map
	 *        the map to set
	 */
	public void setT(Set<String> set) {
		if ( meta == null ) {
			meta = new GeneralDatumMetadata();
		}
		meta.setTags(set);
	}

}
