/* ==================================================================
 * DatumSamples.java - Oct 17, 2014 12:14:16 PM
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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import net.solarnetwork.domain.SerializeIgnore;

/**
 * A collection of different types of sample data, grouped by logical sample
 * type.
 * 
 * @author matt
 * @version 2.1
 */
@JsonPropertyOrder({ "i", "a", "s", "t" })
public class DatumSamples extends DatumSupport implements MutableDatumSamplesOperations, Serializable {

	private static final long serialVersionUID = 3704506858283891128L;

	/** The instantaneous properties. */
	private Map<String, Number> instantaneous;

	/** The accumulating properties. */
	private Map<String, Number> accumulating;

	/** The status properties. */
	private Map<String, Object> status;

	/**
	 * Default constructor.
	 */
	public DatumSamples() {
		super();
	}

	/**
	 * Construct with values.
	 * 
	 * @param instantaneous
	 *        the instantaneous data
	 * @param accumulating
	 *        the accumulating data
	 * @param status
	 *        the status data
	 */
	public DatumSamples(Map<String, Number> instantaneous, Map<String, Number> accumulating,
			Map<String, Object> status) {
		super();
		this.instantaneous = instantaneous;
		this.accumulating = accumulating;
		this.status = status;
	}

	/**
	 * Copy constructor.
	 * 
	 * @param other
	 *        the samples to copy
	 * @since 1.4
	 */
	@SuppressWarnings("unchecked")
	public DatumSamples(DatumSamplesOperations other) {
		super(other);
		if ( other != null ) {
			@SuppressWarnings("rawtypes")
			Map o = other.getSampleData(DatumSamplesType.Instantaneous);
			this.instantaneous = (o != null ? new LinkedHashMap<>(o) : null);
			o = other.getSampleData(DatumSamplesType.Accumulating);
			this.accumulating = (o != null ? new LinkedHashMap<>(o) : null);
			o = other.getSampleData(DatumSamplesType.Status);
			this.status = (o != null ? new LinkedHashMap<>(o) : null);
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{");
		if ( instantaneous != null ) {
			builder.append("i=");
			builder.append(instantaneous);
			if ( accumulating != null || status != null || getTags() != null ) {
				builder.append(", ");
			}
		}
		if ( accumulating != null ) {
			builder.append("a=");
			builder.append(accumulating);
			if ( status != null || getTags() != null ) {
				builder.append(", ");
			}
		}
		if ( status != null ) {
			builder.append("s=");
			builder.append(status);
			if ( getTags() != null ) {
				builder.append(", ");
			}
		}
		if ( getTags() != null ) {
			builder.append("t=");
			builder.append(getTags());
		}
		builder.append("}");
		return builder.toString();
	}

	/**
	 * Clear all property values.
	 */
	@Override
	public void clear() {
		super.clear();
		this.accumulating = null;
		this.instantaneous = null;
		this.status = null;
	}

	/**
	 * Get a merged map of all sample data.
	 * 
	 * @return a map with all sample data combined
	 */
	@JsonIgnore
	@SerializeIgnore
	public Map<String, ?> getSampleData() {
		if ( instantaneous == null && accumulating == null && status == null ) {
			return null;
		}
		Map<String, Object> results = new LinkedHashMap<String, Object>(4);
		if ( instantaneous != null ) {
			results.putAll(instantaneous);
		}
		if ( accumulating != null ) {
			results.putAll(accumulating);
		}
		if ( status != null ) {
			results.putAll(status);
		}
		if ( getTags() != null ) {
			results.put("tags", getTags().toArray());
		}
		return results;
	}

	/**
	 * Test if there are any properties configured in the instantaneous,
	 * accumulating, status, or tag data sets.
	 * 
	 * @return {@literal true} if there is at least one value in one of the data
	 *         sets of this object
	 * @since 1.3
	 */
	@Override
	@JsonIgnore
	@SerializeIgnore
	public boolean isEmpty() {
		if ( instantaneous != null && !instantaneous.isEmpty() ) {
			return false;
		}
		if ( accumulating != null && !accumulating.isEmpty() ) {
			return false;
		}
		if ( status != null && !status.isEmpty() ) {
			return false;
		}
		if ( getTags() != null && !getTags().isEmpty() ) {
			return false;
		}
		return true;
	}

	@Override
	public Map<String, ?> getSampleData(DatumSamplesType type) {
		Map<String, ?> data;
		switch (type) {
			case Instantaneous:
				data = instantaneous;
				break;

			case Accumulating:
				data = accumulating;
				break;

			case Status:
				data = status;
				break;

			default:
				throw new IllegalArgumentException("Sample type [" + type + "] not supported");
		}
		return data;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void setSampleData(DatumSamplesType type, Map<String, ?> data) {
		switch (type) {
			case Instantaneous:
				setInstantaneous((Map) data);
				break;

			case Accumulating:
				setAccumulating((Map) data);
				break;

			case Status:
				setStatus((Map) data);
				break;

			case Tag:
				setTags(data.keySet());
				break;

			default:
				throw new IllegalArgumentException("Sample type [" + type + "] not supported");
		}
	}

	@Override
	public Integer getSampleInteger(DatumSamplesType type, String key) {
		return getMapInteger(key, getSampleData(type));
	}

	@Override
	public Long getSampleLong(DatumSamplesType type, String key) {
		return getMapLong(key, getSampleData(type));
	}

	@Override
	public Float getSampleFloat(DatumSamplesType type, String key) {
		return getMapFloat(key, getSampleData(type));
	}

	@Override
	public Double getSampleDouble(DatumSamplesType type, String key) {
		return getMapDouble(key, getSampleData(type));
	}

	@Override
	public BigDecimal getSampleBigDecimal(DatumSamplesType type, String key) {
		return getMapBigDecimal(key, getSampleData(type));
	}

	@Override
	public String getSampleString(DatumSamplesType type, String key) {
		if ( type == DatumSamplesType.Tag ) {
			Set<String> tags = getTags();
			return (tags != null && tags.contains(key) ? key : null);
		}
		return getMapString(key, getSampleData(type));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <V> V getSampleValue(DatumSamplesType type, String key) {
		if ( type == DatumSamplesType.Tag ) {
			Set<String> tags = getTags();
			return (V) (tags != null && tags.contains(key) ? key : null);
		}
		Map<String, ?> m = getSampleData(type);
		return (V) (m != null ? m.get(key) : null);
	}

	@Override
	public <V> V findSampleValue(String key) {
		V v = getSampleValue(DatumSamplesType.Instantaneous, key);
		if ( v != null ) {
			return v;
		}
		v = getSampleValue(DatumSamplesType.Accumulating, key);
		if ( v != null ) {
			return v;
		}
		return getSampleValue(DatumSamplesType.Status, key);
	}

	@Override
	public boolean hasSampleValue(String key) {
		Object o = findSampleValue(key);
		return (o != null);
	}

	@Override
	public void putSampleValue(DatumSamplesType type, String key, Object value) {
		if ( type == DatumSamplesType.Tag ) {
			if ( value == null ) {
				removeTag(key);
			} else {
				if ( !key.equals(value) ) {
					removeTag(key);
				}
				addTag(value.toString());
			}
			return;
		} else if ( type != DatumSamplesType.Status && value != null && !(value instanceof Number) ) {
			// refuse to add non-Number value to i,a maps; silently ignore
			return;
		}
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Map<String, Object> m = (Map) getSampleData(type);
		if ( m == null ) {
			if ( value == null ) {
				return;
			}
			m = new LinkedHashMap<String, Object>(4);
			setSampleData(type, m);
		}
		if ( value == null ) {
			m.remove(key);
		} else {
			m.put(key, value);
		}
	}

	@Override
	public boolean hasSampleValue(DatumSamplesType type, String key) {
		if ( type == DatumSamplesType.Tag ) {
			return hasTag(key);
		}
		Map<String, ?> data = getSampleData(type);
		return (data != null ? data.containsKey(key) : false);
	}

	/**
	 * Put a value into or remove a value from the {@link #getInstantaneous()}
	 * map, creating the map if it doesn't exist.
	 * 
	 * @param key
	 *        the key to put
	 * @param n
	 *        the value to put, or {@literal null} to remove the key
	 */
	public void putInstantaneousSampleValue(String key, Number n) {
		Map<String, Number> m = instantaneous;
		if ( m == null ) {
			if ( n == null ) {
				return;
			}
			m = new LinkedHashMap<String, Number>(4);
			instantaneous = m;
		}
		if ( n == null ) {
			m.remove(key);
		} else {
			m.put(key, n);
		}
	}

	/**
	 * Put a value into or remove a value from the {@link #getAccumulating()}
	 * map, creating the map if it doesn't exist.
	 * 
	 * @param key
	 *        the key to put
	 * @param n
	 *        the value to put, or {@literal null} to remove the key
	 */
	public void putAccumulatingSampleValue(String key, Number n) {
		Map<String, Number> m = accumulating;
		if ( m == null ) {
			if ( n == null ) {
				return;
			}
			m = new LinkedHashMap<String, Number>(4);
			accumulating = m;
		}
		if ( n == null ) {
			m.remove(key);
		} else {
			m.put(key, n);
		}
	}

	/**
	 * Put a value into or remove a value from the {@link #getStatus()} map,
	 * creating the map if it doesn't exist.
	 * 
	 * @param key
	 *        the key to put
	 * @param value
	 *        the value to put, or {@literal null} to remove the key
	 */
	public void putStatusSampleValue(String key, Object value) {
		Map<String, Object> m = status;
		if ( m == null ) {
			if ( value == null ) {
				return;
			}
			m = new LinkedHashMap<String, Object>(4);
			status = m;
		}
		if ( value == null ) {
			m.remove(key);
		} else {
			m.put(key, value);
		}
	}

	/**
	 * Get an Integer value from the {@link #getInstantaneous()} map, or
	 * {@literal null} if not available.
	 * 
	 * @param key
	 *        the key of the value to get
	 * @return the value as an Integer, or {@literal null} if not available
	 */
	public Integer getInstantaneousSampleInteger(String key) {
		return getMapInteger(key, instantaneous);
	}

	/**
	 * Get a Long value from the {@link #getInstantaneous()} map, or
	 * {@literal null} if not available.
	 * 
	 * @param key
	 *        the key of the value to get
	 * @return the value as an Long, or {@literal null} if not available
	 */
	public Long getInstantaneousSampleLong(String key) {
		return getMapLong(key, instantaneous);
	}

	/**
	 * Get a Float value from the {@link #getInstantaneous()} map, or
	 * {@literal null} if not available.
	 * 
	 * @param key
	 *        the key of the value to get
	 * @return the value as an Float, or {@literal null} if not available
	 */
	public Float getInstantaneousSampleFloat(String key) {
		return getMapFloat(key, instantaneous);
	}

	/**
	 * Get a Double value from the {@link #getInstantaneous()} map, or
	 * {@literal null} if not available.
	 * 
	 * @param key
	 *        the key of the value to get
	 * @return the value as an Double, or {@literal null} if not available
	 */
	public Double getInstantaneousSampleDouble(String key) {
		return getMapDouble(key, instantaneous);
	}

	/**
	 * Get a BigDecimal value from the {@link #getInstantaneous()} map, or
	 * {@literal null} if not available.
	 * 
	 * @param key
	 *        the key of the value to get
	 * @return the value as an BigDecimal, or {@literal null} if not available
	 */
	public BigDecimal getInstantaneousSampleBigDecimal(String key) {
		return getMapBigDecimal(key, instantaneous);
	}

	/**
	 * Get an Integer value from the {@link #getAccumulating()} map, or
	 * {@literal null} if not available.
	 * 
	 * @param key
	 *        the key of the value to get
	 * @return the value as an Integer, or {@literal null} if not available
	 */
	public Integer getAccumulatingSampleInteger(String key) {
		return getMapInteger(key, accumulating);
	}

	/**
	 * Get a Long value from the {@link #getAccumulating()} map, or
	 * {@literal null} if not available.
	 * 
	 * @param key
	 *        the key of the value to get
	 * @return the value as an Long, or {@literal null} if not available
	 */
	public Long getAccumulatingSampleLong(String key) {
		return getMapLong(key, accumulating);
	}

	/**
	 * Get a Float value from the {@link #getAccumulating()} map, or
	 * {@literal null} if not available.
	 * 
	 * @param key
	 *        the key of the value to get
	 * @return the value as an Float, or {@literal null} if not available
	 */
	public Float getAccumulatingSampleFloat(String key) {
		return getMapFloat(key, accumulating);
	}

	/**
	 * Get a Double value from the {@link #getAccumulating()} map, or
	 * {@literal null} if not available.
	 * 
	 * @param key
	 *        the key of the value to get
	 * @return the value as an Double, or {@literal null} if not available
	 */
	public Double getAccumulatingSampleDouble(String key) {
		return getMapDouble(key, accumulating);
	}

	/**
	 * Get a BigDecimal value from the {@link #getAccumulating()} map, or
	 * {@literal null} if not available.
	 * 
	 * @param key
	 *        the key of the value to get
	 * @return the value as an BigDecimal, or {@literal null} if not available
	 */
	public BigDecimal getAccumulatingSampleBigDecimal(String key) {
		return getMapBigDecimal(key, accumulating);
	}

	/**
	 * Get an Integer value from the {@link #getInstantaneous()} map, or
	 * {@literal null} if not available.
	 * 
	 * @param key
	 *        the key of the value to get
	 * @return the value as an Integer, or {@literal null} if not available
	 */
	public Integer getStatusSampleInteger(String key) {
		return getMapInteger(key, status);
	}

	/**
	 * Get a Long value from the {@link #getInstantaneous()} map, or
	 * {@literal null} if not available.
	 * 
	 * @param key
	 *        the key of the value to get
	 * @return the value as an Long, or {@literal null} if not available
	 */
	public Long getStatusSampleLong(String key) {
		return getMapLong(key, status);
	}

	/**
	 * Get a Float value from the {@link #getInstantaneous()} map, or
	 * {@literal null} if not available.
	 * 
	 * @param key
	 *        the key of the value to get
	 * @return the value as an Float, or {@literal null} if not available
	 */
	public Float getStatusSampleFloat(String key) {
		return getMapFloat(key, status);
	}

	/**
	 * Get a Double value from the {@link #getInstantaneous()} map, or
	 * {@literal null} if not available.
	 * 
	 * @param key
	 *        the key of the value to get
	 * @return the value as an Double, or {@literal null} if not available
	 */
	public Double getStatusSampleDouble(String key) {
		return getMapDouble(key, status);
	}

	/**
	 * Get a BigDecimal value from the {@link #getInstantaneous()} map, or
	 * {@literal null} if not available.
	 * 
	 * @param key
	 *        the key of the value to get
	 * @return the value as an BigDecimal, or {@literal null} if not available
	 */
	public BigDecimal getStatusSampleBigDecimal(String key) {
		return getMapBigDecimal(key, status);
	}

	/**
	 * Get a String value from the {@link #getStatus()} map, or {@literal null}
	 * if not available.
	 * 
	 * @param key
	 *        the key of the value to get
	 * @return the value as a String, or {@literal null} if not available
	 */
	public String getStatusSampleString(String key) {
		return getMapString(key, status);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((accumulating == null) ? 0 : accumulating.hashCode());
		result = prime * result + ((instantaneous == null) ? 0 : instantaneous.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
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
		DatumSamples other = (DatumSamples) obj;
		if ( accumulating == null ) {
			if ( other.accumulating != null ) {
				return false;
			}
		} else if ( !accumulating.equals(other.accumulating) ) {
			return false;
		}
		if ( instantaneous == null ) {
			if ( other.instantaneous != null ) {
				return false;
			}
		} else if ( !instantaneous.equals(other.instantaneous) ) {
			return false;
		}
		if ( status == null ) {
			if ( other.status != null ) {
				return false;
			}
		} else if ( !status.equals(other.status) ) {
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
	 * Shortcut for {@link #getInstantaneous()}.
	 * 
	 * @return map
	 */
	public Map<String, Number> getI() {
		return getInstantaneous();
	}

	/**
	 * Set the instantaneous properties.
	 * 
	 * @param map
	 *        the properties to set
	 */
	public void setI(Map<String, Number> map) {
		setInstantaneous(map);
	}

	/**
	 * Shortcut for {@link #getAccumulating()}.
	 * 
	 * @return map
	 */
	public Map<String, Number> getA() {
		return getAccumulating();
	}

	/**
	 * Set the accumulating properties.
	 * 
	 * @param map
	 *        the properties to set
	 */
	public void setA(Map<String, Number> map) {
		setAccumulating(map);
	}

	/**
	 * Shortcut for {@link #getStatus()}.
	 * 
	 * @return map
	 */
	public Map<String, Object> getS() {
		return getStatus();
	}

	/**
	 * Set the status properties.
	 * 
	 * @param map
	 *        the status properties to set
	 */
	public void setS(Map<String, Object> map) {
		setStatus(map);
	}

	/**
	 * Get a map of <em>instantaneous</em> sample values. These values measure
	 * instant readings of something.
	 * 
	 * @return map of instantaneous measurements
	 */
	@JsonIgnore
	@SerializeIgnore
	public Map<String, Number> getInstantaneous() {
		return instantaneous;
	}

	/**
	 * Set the instantaneous properties.
	 * 
	 * @param instantaneous
	 *        the properties to set
	 */
	public void setInstantaneous(Map<String, Number> instantaneous) {
		this.instantaneous = instantaneous;
	}

	/**
	 * Get a map <em>accumulating</em> sample values. These values measure an
	 * accumulating data value, whose values represent an offset from another
	 * sample on a different date.
	 * 
	 * @return map of accumulating measurements
	 */
	@JsonIgnore
	@SerializeIgnore
	public Map<String, Number> getAccumulating() {
		return accumulating;
	}

	/**
	 * Set the accumulating properties.
	 * 
	 * @param accumulating
	 *        the properties to set
	 */
	public void setAccumulating(Map<String, Number> accumulating) {
		this.accumulating = accumulating;
	}

	/**
	 * Get a map of <em>status</em> sample values. These are arbitrary values.
	 * 
	 * @return map of status messages
	 */
	@JsonIgnore
	@SerializeIgnore
	public Map<String, Object> getStatus() {
		return status;
	}

	/**
	 * Set the status properties.
	 * 
	 * @param status
	 *        the properties to set
	 */
	public void setStatus(Map<String, Object> status) {
		this.status = status;
	}

}
