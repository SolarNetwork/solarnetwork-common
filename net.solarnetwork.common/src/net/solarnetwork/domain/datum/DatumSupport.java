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

/**
 * Supporting abstract class for general node datum related objects.
 * 
 * @author matt
 * @version 2.0
 */
public abstract class DatumSupport implements Serializable {

	private static final long serialVersionUID = -4264640101068495508L;

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
		if ( map == null ) {
			return null;
		}
		Object s = map.get(key);
		if ( s == null ) {
			return null;
		}
		if ( s instanceof String ) {
			return (String) s;
		}
		return s.toString();
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
		if ( map == null ) {
			return null;
		}
		Object n = map.get(key);
		if ( n == null ) {
			return null;
		}
		if ( n instanceof Integer ) {
			return (Integer) n;
		}
		if ( n instanceof Number ) {
			return ((Number) n).intValue();
		}
		try {
			return Integer.valueOf(n.toString());
		} catch ( NumberFormatException e ) {
			return null;
		}
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
		if ( map == null ) {
			return null;
		}
		Object n = map.get(key);
		if ( n == null ) {
			return null;
		}
		if ( n instanceof Long ) {
			return (Long) n;
		}
		if ( n instanceof Number ) {
			return ((Number) n).longValue();
		}
		try {
			return Long.valueOf(n.toString());
		} catch ( NumberFormatException e ) {
			return null;
		}
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
		if ( map == null ) {
			return null;
		}
		Object n = map.get(key);
		if ( n == null ) {
			return null;
		}
		if ( n instanceof Float ) {
			return (Float) n;
		}
		if ( n instanceof Number ) {
			return ((Number) n).floatValue();
		}
		try {
			return Float.valueOf(n.toString());
		} catch ( NumberFormatException e ) {
			return null;
		}
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
		if ( map == null ) {
			return null;
		}
		Object n = map.get(key);
		if ( n == null ) {
			return null;
		}
		if ( n instanceof Double ) {
			return (Double) n;
		}
		if ( n instanceof Number ) {
			return ((Number) n).doubleValue();
		}
		try {
			return Double.valueOf(n.toString());
		} catch ( NumberFormatException e ) {
			return null;
		}
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
		if ( map == null ) {
			return null;
		}
		Object n = map.get(key);
		if ( n == null ) {
			return null;
		}
		if ( n instanceof BigDecimal ) {
			return (BigDecimal) n;
		}
		try {
			return new BigDecimal(n.toString());
		} catch ( NumberFormatException e ) {
			return null;
		}
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

	public void setT(Set<String> set) {
		setTags(set);
	}

	/**
	 * Return <em>true</em> if {@code tags} contains {@code tag}.
	 * 
	 * @param tag
	 *        the tag value to test for existence
	 * @return boolean
	 */
	public boolean hasTag(String tag) {
		return (tags != null && tags.contains(tag));
	}

	/**
	 * Add a tag value.
	 * 
	 * @param tag
	 *        the tag value to add
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
	 */
	public boolean removeTag(String tag) {
		if ( tag == null ) {
			return false;
		}
		Set<String> set = tags;
		return (set != null ? set.remove(tag) : false);
	}

}
