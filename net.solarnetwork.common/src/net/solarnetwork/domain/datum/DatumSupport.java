/* ==================================================================
 * DatumSupport.java - Oct 3, 2014 7:41:01 AM
 * 
 * Copyright 2007-2014 SolarNetwork.net Dev Team
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU  Public License as 
 * published by the Free Software Foundation; either version 2 of 
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 *  Public License for more details.
 * 
 * You should have received a copy of the GNU  Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
 * 02111-1307 USA
 * ==================================================================
 */

package net.solarnetwork.domain.datum;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;
import net.solarnetwork.domain.SerializeIgnore;
import net.solarnetwork.util.CollectionUtils;

/**
 * Supporting abstract class for general node datum related objects.
 * 
 * @author matt
 * @version 2.0
 */
public abstract class DatumSupport implements Serializable {

	private static final long serialVersionUID = -4264640101068495508L;

	/** The tags. */
	private Set<String> tags;

	/**
	 * Constructor.
	 */
	public DatumSupport() {
		super();
	}

	/**
	 * Copy constructor.
	 * 
	 * @param other
	 *        the other instance to copy
	 * @since 1.1
	 */
	public DatumSupport(DatumSamplesOperations other) {
		super();
		if ( other != null ) {
			this.tags = (other.getTags() != null ? new LinkedHashSet<>(other.getTags()) : null);
		}
	}

	/**
	 * Remove all data values.
	 */
	public void clear() {
		tags = null;
	}

	/**
	 * Get a String value out of a Map. If the key exists but is not a String,
	 * {@link Object#toString()} will be called on that object.
	 * 
	 * @param key
	 *        the key of the object to get
	 * @param map
	 *        the map to inspect, {@literal null} is allowed
	 * @return the value, or {@literal null} if not found
	 */
	protected String getMapString(String key, Map<String, ?> map) {
		return CollectionUtils.getMapString(key, map);
	}

	/**
	 * Get an Integer value out of a Map. If the key exists, is not an Integer,
	 * but is a Number, {@link Number#intValue()} will be called on that object.
	 * 
	 * @param key
	 *        the key of the object to get
	 * @param map
	 *        the map to inspect, {@literal null} is allowed
	 * @return the value, or {@literal null} if not found
	 */
	protected Integer getMapInteger(String key, Map<String, ?> map) {
		return CollectionUtils.getMapInteger(key, map);
	}

	/**
	 * Get a Long value out of a Map. If the key exists, is not a Long, but is a
	 * Number, {@link Number#longValue()} will be called on that object.
	 * 
	 * @param key
	 *        the key of the object to get
	 * @param map
	 *        the map to inspect, {@literal null} is allowed
	 * @return the value, or {@literal null} if not found
	 */
	protected Long getMapLong(String key, Map<String, ?> map) {
		return CollectionUtils.getMapLong(key, map);
	}

	/**
	 * Get a Float value out of a Map. If the key exists, is not a Float, but is
	 * a Number, {@link Number#floatValue()} will be called on that object.
	 * 
	 * @param key
	 *        the key of the object to get
	 * @param map
	 *        the map to inspect, {@literal null} is allowed
	 * @return the value, or {@literal null} if not found
	 */
	protected Float getMapFloat(String key, Map<String, ?> map) {
		return CollectionUtils.getMapFloat(key, map);
	}

	/**
	 * Get a Double value out of a Map. If the key exists, is not a Double, but
	 * is a Number, {@link Number#doubleValue()} will be called on that object.
	 * 
	 * @param key
	 *        the key of the object to get
	 * @param map
	 *        the map to inspect, {@literal null} is allowed
	 * @return the value, or {@literal null} if not found
	 */
	protected Double getMapDouble(String key, Map<String, ?> map) {
		return CollectionUtils.getMapDouble(key, map);
	}

	/**
	 * Get a BigDecimal value out of a Map. If the key exists but is not a
	 * BigDecimal, {@link Object#toString()} will be called on that object and
	 * {@link BigDecimal#BigDecimal(String)} will be returned.
	 * 
	 * @param key
	 *        the key of the object to get
	 * @param map
	 *        the map to inspect, {@literal null} is allowed
	 * @return the value, or {@literal null} if not found
	 */
	protected BigDecimal getMapBigDecimal(String key, Map<String, ?> map) {
		return CollectionUtils.getMapBigDecimal(key, map);
	}

	/**
	 * Get an array of <em>tags</em>.
	 * 
	 * @return array of tags
	 */
	@JsonIgnore
	@SerializeIgnore
	public Set<String> getTags() {
		return tags;
	}

	/**
	 * Set the tags.
	 * 
	 * @param tags
	 *        the tags to set
	 */
	public void setTags(Set<String> tags) {
		this.tags = tags;
	}

	/**
	 * Shortcut for {@link #getTags()}.
	 * 
	 * @return map
	 */
	public Set<String> getT() {
		return getTags();
	}

	/**
	 * Set the tags.
	 * 
	 * @param set
	 *        the tags to set
	 */
	public void setT(Set<String> set) {
		setTags(set);
	}

	/**
	 * Return {@literal true} if {@code tags} contains {@code tag}.
	 * 
	 * @param tag
	 *        the tag value to test for existence
	 * @return {@literal true} if the tag is present
	 */
	public boolean hasTag(String tag) {
		return (tags != null && tags.contains(tag));
	}

	/**
	 * Add a tag value.
	 * 
	 * @param tag
	 *        the tag value to add
	 * @return {@literal true} if the tag was added, or {@literal false} if the
	 *         tag was already present
	 */
	public boolean addTag(String tag) {
		if ( tag == null ) {
			return false;
		}
		Set<String> set = tags;
		if ( set == null ) {
			set = new LinkedHashSet<String>(2);
			tags = set;
		}
		return set.add(tag);
	}

	/**
	 * Remove a tag value.
	 * 
	 * @param tag
	 *        the tag value to add
	 * @return {@literal true} if the tag was removed, or {@literal false} if
	 *         the tag was not present
	 */
	public boolean removeTag(String tag) {
		if ( tag == null ) {
			return false;
		}
		Set<String> set = tags;
		return (set != null ? set.remove(tag) : false);
	}

}
