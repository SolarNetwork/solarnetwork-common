/* ==================================================================
 * AggregateDatumSamples.java - 27/08/2020 10:38:08 AM
 * 
 * Copyright 2020 SolarNetwork.net Dev Team
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

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.solarnetwork.util.NumberUtils;

/**
 * An aggregation of datum sample values.
 * 
 * @author matt
 * @version 2.0
 * @since 1.65
 */
public class AggregateDatumSamples extends DatumSupport {

	private static final long serialVersionUID = -5462422979612352107L;

	private int count = 0;
	private Map<String, AggregateDatumProperty> instantaneous;
	private Map<String, AggregateDatumProperty> accumulating;
	private Map<String, Object> status;

	private void addAggregatePropertyValue(Map<String, AggregateDatumProperty> m, String key, Number n) {
		if ( n == null ) {
			m.remove(key);
		} else {
			m.compute(key, (k, v) -> {
				BigDecimal d = NumberUtils.bigDecimalForNumber(n);
				if ( v != null ) {
					v.accumulate(d);
					return v;
				}
				return new AggregateDatumProperty(d);
			});
		}
	}

	/**
	 * Add all properties of a sample.
	 * 
	 * @param sample
	 *        the sample whose properties should be accumulated into this
	 *        aggregate
	 */
	public void addSample(DatumSamplesOperations sample) {
		if ( sample == null ) {
			return;
		}
		count++;
		Map<String, ?> m = sample.getSampleData(DatumSamplesType.Instantaneous);
		if ( m != null ) {
			for ( Map.Entry<String, ?> me : m.entrySet() ) {
				putInstantaneousSampleValue(me.getKey(), (Number) me.getValue());
			}
		}
		m = sample.getSampleData(DatumSamplesType.Accumulating);
		if ( m != null ) {
			for ( Map.Entry<String, ?> me : m.entrySet() ) {
				putAccumulatingSampleValue(me.getKey(), (Number) me.getValue());
			}
		}
		m = sample.getSampleData(DatumSamplesType.Accumulating);
		if ( m != null ) {
			for ( Map.Entry<String, ?> me : m.entrySet() ) {
				putStatusSampleValue(me.getKey(), me.getValue());
			}
		}
		Set<String> t = sample.getTags();
		if ( t != null ) {
			for ( String v : t ) {
				addTag(v);
			}
		}
	}

	/**
	 * Generate a new samples instance as an average of the added samples.
	 * 
	 * @param decimalScale
	 *        the average decimal scale
	 * @param minPropertyFormat
	 *        an optional string template to generate a "minimum" property name
	 *        with; will be passed a single string parameter
	 * @param maxPropertyFormat
	 *        an optional string template to generate a "maximum" property name
	 *        with; will be passed a single string parameter
	 * @return the new samples instance, never {@literal null}
	 * @since 1.1
	 */
	public DatumSamples average(int decimalScale, String minPropertyFormat, String maxPropertyFormat) {
		DatumSamples out = new DatumSamples();
		Map<String, AggregateDatumProperty> i = getInstantaneous();
		if ( i != null ) {
			for ( Map.Entry<String, AggregateDatumProperty> me : i.entrySet() ) {
				out.putInstantaneousSampleValue(me.getKey(), me.getValue().average(decimalScale));
				if ( minPropertyFormat != null && !minPropertyFormat.isEmpty() ) {
					out.putInstantaneousSampleValue(String.format(minPropertyFormat, me.getKey()),
							me.getValue().getMin());
				}
				if ( maxPropertyFormat != null && !maxPropertyFormat.isEmpty() ) {
					out.putInstantaneousSampleValue(String.format(maxPropertyFormat, me.getKey()),
							me.getValue().getMax());
				}
			}
		}
		Map<String, AggregateDatumProperty> a = getAccumulating();
		if ( a != null ) {
			for ( Map.Entry<String, AggregateDatumProperty> me : a.entrySet() ) {
				out.putAccumulatingSampleValue(me.getKey(), me.getValue().last());
			}
		}
		Map<String, Object> s = getStatus();
		if ( s != null ) {
			out.setStatus(s);
		}
		Set<String> t = getTags();
		if ( t != null ) {
			out.setTags(t);
		}
		return out;
	}

	/**
	 * Get the count of samples added via {@link #addSample(DatumSamples)}.
	 * 
	 * @return the count
	 */
	public int addedSampleCount() {
		return count;
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
		Map<String, AggregateDatumProperty> m = instantaneous;
		if ( m == null ) {
			if ( n == null ) {
				return;
			}
			m = new LinkedHashMap<String, AggregateDatumProperty>(4);
			instantaneous = m;
		}
		addAggregatePropertyValue(m, key, n);
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
		Map<String, AggregateDatumProperty> m = accumulating;
		if ( m == null ) {
			if ( n == null ) {
				return;
			}
			m = new LinkedHashMap<String, AggregateDatumProperty>(4);
			accumulating = m;
		}
		addAggregatePropertyValue(m, key, n);
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
	 * Get the instantaneous properties.
	 * 
	 * @return the instantaneous properties
	 */
	public Map<String, AggregateDatumProperty> getInstantaneous() {
		return instantaneous;
	}

	/**
	 * Get the accumulating properties.
	 * 
	 * @return the accumulating properties
	 */
	public Map<String, AggregateDatumProperty> getAccumulating() {
		return accumulating;
	}

	/**
	 * Get the status properties.
	 * 
	 * @return the status properties
	 */
	public Map<String, Object> getStatus() {
		return status;
	}

}
