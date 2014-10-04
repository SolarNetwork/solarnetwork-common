/* ==================================================================
 * GeneralNodeDatumSamples.java - Aug 22, 2014 6:26:13 AM
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

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import net.solarnetwork.util.SerializeIgnore;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonPropertyOrder;

/**
 * Metadata about general node datum streams of data.
 * 
 * @author matt
 * @version 1.0
 */
@JsonPropertyOrder({ "m", "pm", "t" })
public class GeneralDatumMetadata extends GeneralDatumSupport implements Serializable {

	private static final long serialVersionUID = -2571643375746163527L;

	private Map<String, Object> info;
	private Map<String, Map<String, Object>> propertyInfo;

	/**
	 * Default constructor.
	 */
	public GeneralDatumMetadata() {
		super();
	}

	/**
	 * Copy constructor.
	 */
	public GeneralDatumMetadata(GeneralDatumMetadata other) {
		super();
		if ( other.getTags() != null ) {
			setTags(new LinkedHashSet<String>(other.getTags()));
		}
		if ( other.info != null ) {
			info = new LinkedHashMap<String, Object>(other.info);
		}
		if ( other.propertyInfo != null ) {
			propertyInfo = new LinkedHashMap<String, Map<String, Object>>(other.propertyInfo);
		}
	}

	/**
	 * Construct with values.
	 * 
	 * @param info
	 *        the info data
	 */
	public GeneralDatumMetadata(Map<String, Object> info) {
		super();
		this.info = info;
	}

	/**
	 * Construct with values.
	 * 
	 * @param info
	 *        the info data
	 * @param propertyInfo
	 *        the property info data
	 */
	public GeneralDatumMetadata(Map<String, Object> info, Map<String, Map<String, Object>> propertyInfo) {
		super();
		this.info = info;
		this.propertyInfo = propertyInfo;
	}

	/**
	 * Put a value into the {@link #getInfo()} map, creating the map if it
	 * doesn't exist.
	 * 
	 * @param key
	 *        the key to put
	 * @param value
	 *        the value to put
	 */
	public void putInfoValue(String key, Object value) {
		Map<String, Object> m = info;
		if ( m == null ) {
			m = new LinkedHashMap<String, Object>(4);
			info = m;
		}
		m.put(key, value);
	}

	/**
	 * Get an Integer value from the {@link #getInfo()} map, or <em>null</em> if
	 * not available.
	 * 
	 * @param key
	 *        the key of the value to get
	 * @return the value as an Integer, or <em>null</em> if not available
	 */
	public Integer getInfoInteger(String key) {
		return getMapInteger(key, info);
	}

	/**
	 * Get a Long value from the {@link #getInfo()} map, or <em>null</em> if not
	 * available.
	 * 
	 * @param key
	 *        the key of the value to get
	 * @return the value as an Long, or <em>null</em> if not available
	 */
	public Long getInfoLong(String key) {
		return getMapLong(key, info);
	}

	/**
	 * Get a Float value from the {@link #getInfo()} map, or <em>null</em> if
	 * not available.
	 * 
	 * @param key
	 *        the key of the value to get
	 * @return the value as an Float, or <em>null</em> if not available
	 */
	public Float getInfoFloat(String key) {
		return getMapFloat(key, info);
	}

	/**
	 * Get a Double value from the {@link #getInfo()} map, or <em>null</em> if
	 * not available.
	 * 
	 * @param key
	 *        the key of the value to get
	 * @return the value as an Double, or <em>null</em> if not available
	 */
	public Double getInfoDouble(String key) {
		return getMapDouble(key, info);
	}

	/**
	 * Get a BigDecimal value from the {@link #getInfo()} map, or <em>null</em>
	 * if not available.
	 * 
	 * @param key
	 *        the key of the value to get
	 * @return the value as an BigDecimal, or <em>null</em> if not available
	 */
	public BigDecimal getInfoBigDecimal(String key) {
		return getMapBigDecimal(key, info);
	}

	/**
	 * Get a String value from the {@link #getInfo()} map, or <em>null</em> if
	 * not available.
	 * 
	 * @param key
	 *        the key of the value to get
	 * @return the value as a String, or <em>null</em> if not available
	 */
	public String getInfoString(String key) {
		return getMapString(key, info);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((info == null) ? 0 : info.hashCode());
		result = prime * result + ((propertyInfo == null) ? 0 : propertyInfo.hashCode());
		result = prime * result + ((getTags() == null) ? 0 : getTags().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( obj == null ) {
			return false;
		}
		if ( getClass() != obj.getClass() ) {
			return false;
		}
		GeneralDatumMetadata other = (GeneralDatumMetadata) obj;
		if ( info == null ) {
			if ( other.info != null ) {
				return false;
			}
		} else if ( !info.equals(other.info) ) {
			return false;
		}
		if ( propertyInfo == null ) {
			if ( other.propertyInfo != null ) {
				return false;
			}
		} else if ( !propertyInfo.equals(other.propertyInfo) ) {
			return false;
		}
		if ( getTags() == null ) {
			if ( other.getTags() != null ) {
				return false;
			}
		} else if ( !getTags().equals(other.getTags()) ) {
			return false;
		}
		return true;
	}

	/**
	 * Get a map of <em>info</em> values. These are arbitrary values.
	 * 
	 * @return map of info values
	 */
	@JsonIgnore
	@SerializeIgnore
	public Map<String, Object> getInfo() {
		return info;
	}

	public void setInfo(Map<String, Object> info) {
		this.info = info;
	}

	/**
	 * Shortcut for {@link #getInfo()}.
	 * 
	 * @return map
	 */
	public Map<String, Object> getM() {
		return getInfo();
	}

	/**
	 * Shortcut for {@link #setInfo(Map)}.
	 * 
	 * @param map
	 *        the Map to set
	 */
	public void setM(Map<String, Object> map) {
		setInfo(map);
	}

	/**
	 * Get a map of <em>property info</em> maps. Each top-level key represents a
	 * property name and the associated map the metadata for that property.
	 * 
	 * @return map
	 */
	@JsonIgnore
	@SerializeIgnore
	public Map<String, Map<String, Object>> getPropertyInfo() {
		return propertyInfo;
	}

	public void setPropertyInfo(Map<String, Map<String, Object>> propertyInfo) {
		this.propertyInfo = propertyInfo;
	}

	/**
	 * Shortcut for {@link #getPropertyInfo()}.
	 * 
	 * @return the map
	 */
	public Map<String, Map<String, Object>> getPm() {
		return getPropertyInfo();
	}

	/**
	 * Shortcut for {@link GeneralDatumMetadata#setPropertyInfo(Map)}.
	 * 
	 * @param map
	 *        the map to set
	 */
	public void setPm(Map<String, Map<String, Object>> map) {
		setPropertyInfo(map);
	}

	/**
	 * Put a value into the {@link #getPropertyInfo()} map, creating the map if
	 * it doesn't exist.
	 * 
	 * @param property
	 *        the property name
	 * @param key
	 *        the key to put
	 * @param value
	 *        the value to put
	 */
	public void putInfoValue(String property, String key, Object value) {
		Map<String, Map<String, Object>> pm = propertyInfo;
		if ( pm == null ) {
			pm = new LinkedHashMap<String, Map<String, Object>>(4);
			propertyInfo = pm;
		}
		Map<String, Object> m = pm.get(property);
		if ( m == null ) {
			m = new LinkedHashMap<String, Object>(4);
			pm.put(property, m);
		}
		m.put(key, value);
	}

	/**
	 * Get an Integer value from the {@link #getPropertyInfo()} map, or
	 * <em>null</em> if not available.
	 * 
	 * @param property
	 *        the property name
	 * @param key
	 *        the key of the value to get
	 * @return the value as an Integer, or <em>null</em> if not available
	 */
	public Integer getInfoInteger(String property, String key) {
		return getMapInteger(key, (propertyInfo == null ? null : propertyInfo.get(property)));
	}

	/**
	 * Get a Long value from the {@link #getPropertyInfo()} map, or
	 * <em>null</em> if not available.
	 * 
	 * @param property
	 *        the property name
	 * @param key
	 *        the key of the value to get
	 * @return the value as an Long, or <em>null</em> if not available
	 */
	public Long getInfoLong(String property, String key) {
		return getMapLong(key, (propertyInfo == null ? null : propertyInfo.get(property)));
	}

	/**
	 * Get a Float value from the {@link #getPropertyInfo()} map, or
	 * <em>null</em> if not available.
	 * 
	 * @param property
	 *        the property name
	 * @param key
	 *        the key of the value to get
	 * @return the value as an Float, or <em>null</em> if not available
	 */
	public Float getInfoFloat(String property, String key) {
		return getMapFloat(key, (propertyInfo == null ? null : propertyInfo.get(property)));
	}

	/**
	 * Get a Double value from the {@link #getPropertyInfo()} map, or
	 * <em>null</em> if not available.
	 * 
	 * @param property
	 *        the property name
	 * @param key
	 *        the key of the value to get
	 * @return the value as an Double, or <em>null</em> if not available
	 */
	public Double getInfoDouble(String property, String key) {
		return getMapDouble(key, (propertyInfo == null ? null : propertyInfo.get(property)));
	}

	/**
	 * Get a BigDecimal value from the {@link #getPropertyInfo()} map, or
	 * <em>null</em> if not available.
	 * 
	 * @param property
	 *        the property name
	 * @param key
	 *        the key of the value to get
	 * @return the value as an BigDecimal, or <em>null</em> if not available
	 */
	public BigDecimal getInfoBigDecimal(String property, String key) {
		return getMapBigDecimal(key, (propertyInfo == null ? null : propertyInfo.get(property)));
	}

	/**
	 * Get a String value from the {@link #getPropertyInfo()} map, or
	 * <em>null</em> if not available.
	 * 
	 * @param property
	 *        the property name
	 * @param key
	 *        the key of the value to get
	 * @return the value as a String, or <em>null</em> if not available
	 */
	public String getInfoString(String property, String key) {
		return getMapString(key, (propertyInfo == null ? null : propertyInfo.get(property)));
	}
}
