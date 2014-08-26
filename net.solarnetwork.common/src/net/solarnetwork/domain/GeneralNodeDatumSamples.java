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
import java.util.Map;
import net.solarnetwork.util.SerializeIgnore;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonPropertyOrder;

/**
 * A collection of different types of sample data, grouped by logical sample
 * type.
 * 
 * @author matt
 * @version 1.0
 */
@JsonPropertyOrder({ "i", "a", "s" })
public class GeneralNodeDatumSamples implements Serializable {

	private static final long serialVersionUID = -4820458070622781600L;

	private Map<String, Number> instantaneous;
	private Map<String, Number> accumulating;
	private Map<String, String> status;

	/**
	 * Default constructor.
	 */
	public GeneralNodeDatumSamples() {
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
	public GeneralNodeDatumSamples(Map<String, Number> instantaneous, Map<String, Number> accumulating,
			Map<String, String> status) {
		super();
		this.instantaneous = instantaneous;
		this.accumulating = accumulating;
		this.status = status;
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
		return results;
	}

	/**
	 * Put a value into the {@link #getInstantaneous()} map, creating the map if
	 * it doesn't exist.
	 * 
	 * @param key
	 *        the key to put
	 * @param n
	 *        the value to put
	 */
	public void putInstantaneousSampleValue(String key, Number n) {
		Map<String, Number> m = instantaneous;
		if ( m == null ) {
			m = new LinkedHashMap<String, Number>(4);
			instantaneous = m;
		}
		m.put(key, n);
	}

	/**
	 * Put a value into the {@link #getAccumulating()} map, creating the map if
	 * it doesn't exist.
	 * 
	 * @param key
	 *        the key to put
	 * @param n
	 *        the value to put
	 */
	public void putAccumulatingSampleValue(String key, Number n) {
		Map<String, Number> m = accumulating;
		if ( m == null ) {
			m = new LinkedHashMap<String, Number>(4);
			accumulating = m;
		}
		m.put(key, n);
	}

	/**
	 * Put a value into the {@link #getStatus()} map, creating the map if it
	 * doesn't exist.
	 * 
	 * @param key
	 *        the key to put
	 * @param value
	 *        the value to put
	 */
	public void putStatusSampleValue(String key, String value) {
		Map<String, String> m = status;
		if ( m == null ) {
			m = new LinkedHashMap<String, String>(4);
			status = m;
		}
		m.put(key, value);
	}

	/**
	 * Get an Integer value from the {@link #getInstantaneous()} map, or
	 * <em>null</em> if not available.
	 * 
	 * @param key
	 *        the key of the value to get
	 * @return the value as an Integer, or <em>null</em> if not available
	 */
	public Integer getInstantaneousSampleInteger(String key) {
		return getSampleInteger(key, instantaneous);
	}

	/**
	 * Get a Long value from the {@link #getInstantaneous()} map, or
	 * <em>null</em> if not available.
	 * 
	 * @param key
	 *        the key of the value to get
	 * @return the value as an Long, or <em>null</em> if not available
	 */
	public Long getInstantaneousSampleLong(String key) {
		return getSampleLong(key, instantaneous);
	}

	/**
	 * Get a Float value from the {@link #getInstantaneous()} map, or
	 * <em>null</em> if not available.
	 * 
	 * @param key
	 *        the key of the value to get
	 * @return the value as an Float, or <em>null</em> if not available
	 */
	public Float getInstantaneousSampleFloat(String key) {
		return getSampleFloat(key, instantaneous);
	}

	/**
	 * Get a Double value from the {@link #getInstantaneous()} map, or
	 * <em>null</em> if not available.
	 * 
	 * @param key
	 *        the key of the value to get
	 * @return the value as an Double, or <em>null</em> if not available
	 */
	public Double getInstantaneousSampleDouble(String key) {
		return getSampleDouble(key, instantaneous);
	}

	/**
	 * Get a BigDecimal value from the {@link #getInstantaneous()} map, or
	 * <em>null</em> if not available.
	 * 
	 * @param key
	 *        the key of the value to get
	 * @return the value as an BigDecimal, or <em>null</em> if not available
	 */
	public BigDecimal getInstantaneousSampleBigDecimal(String key) {
		return getSampleBigDecimal(key, instantaneous);
	}

	/**
	 * Get an Integer value from the {@link #getAccumulating()} map, or
	 * <em>null</em> if not available.
	 * 
	 * @param key
	 *        the key of the value to get
	 * @return the value as an Integer, or <em>null</em> if not available
	 */
	public Integer getAccumulatingSampleInteger(String key) {
		return getSampleInteger(key, accumulating);
	}

	/**
	 * Get a Long value from the {@link #getAccumulating()} map, or
	 * <em>null</em> if not available.
	 * 
	 * @param key
	 *        the key of the value to get
	 * @return the value as an Long, or <em>null</em> if not available
	 */
	public Long getAccumulatingSampleLong(String key) {
		return getSampleLong(key, accumulating);
	}

	/**
	 * Get a Float value from the {@link #getAccumulating()} map, or
	 * <em>null</em> if not available.
	 * 
	 * @param key
	 *        the key of the value to get
	 * @return the value as an Float, or <em>null</em> if not available
	 */
	public Float getAccumulatingSampleFloat(String key) {
		return getSampleFloat(key, accumulating);
	}

	/**
	 * Get a Double value from the {@link #getAccumulating()} map, or
	 * <em>null</em> if not available.
	 * 
	 * @param key
	 *        the key of the value to get
	 * @return the value as an Double, or <em>null</em> if not available
	 */
	public Double getAccumulatingSampleDouble(String key) {
		return getSampleDouble(key, accumulating);
	}

	/**
	 * Get a BigDecimal value from the {@link #getAccumulating()} map, or
	 * <em>null</em> if not available.
	 * 
	 * @param key
	 *        the key of the value to get
	 * @return the value as an BigDecimal, or <em>null</em> if not available
	 */
	public BigDecimal getAccumulatingSampleBigDecimal(String key) {
		return getSampleBigDecimal(key, accumulating);
	}

	/**
	 * Get a String value from the {@link #getSample()} map, or <em>null</em> if
	 * not available.
	 * 
	 * @param key
	 *        the key of the value to get
	 * @return the value as a String, or <em>null</em> if not available
	 */
	public String getStatusSampleString(String key) {
		return getSampleString(key, status);
	}

	private String getSampleString(String key, Map<String, ?> map) {
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

	private Integer getSampleInteger(String key, Map<String, ?> map) {
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

	private Long getSampleLong(String key, Map<String, ?> map) {
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

	private Float getSampleFloat(String key, Map<String, ?> map) {
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

	private Double getSampleDouble(String key, Map<String, ?> map) {
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

	private BigDecimal getSampleBigDecimal(String key, Map<String, ?> map) {
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((accumulating == null) ? 0 : accumulating.hashCode());
		result = prime * result + ((instantaneous == null) ? 0 : instantaneous.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
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
		GeneralNodeDatumSamples other = (GeneralNodeDatumSamples) obj;
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

	public void setA(Map<String, Number> map) {
		setAccumulating(map);
	}

	/**
	 * Shortcut for {@link #getStatus()}.
	 * 
	 * @return map
	 */
	public Map<String, String> getS() {
		return getStatus();
	}

	public void setS(Map<String, String> map) {
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

	public void setAccumulating(Map<String, Number> accumulating) {
		this.accumulating = accumulating;
	}

	/**
	 * Get a map of <em>status</em> sample values. These are arbitrary strings.
	 * 
	 * @return map of status messages
	 */
	@JsonIgnore
	@SerializeIgnore
	public Map<String, String> getStatus() {
		return status;
	}

	public void setStatus(Map<String, String> status) {
		this.status = status;
	}

}
