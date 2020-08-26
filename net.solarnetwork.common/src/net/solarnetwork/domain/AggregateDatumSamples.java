/* ==================================================================
 * AggregateDatumSamples.java - 27/08/2020 10:38:08 AM
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

package net.solarnetwork.domain;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.solarnetwork.util.NumberUtils;

/**
 * An aggregation of datum sample values.
 * 
 * @author matt
 * @version 1.0
 * @since 1.65
 */
public class AggregateDatumSamples extends GeneralDatumSupport {

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
	public void addSample(GeneralDatumSamples sample) {
		if ( sample == null ) {
			return;
		}
		count++;
		Map<String, Number> m = sample.getInstantaneous();
		if ( m != null ) {
			for ( Map.Entry<String, Number> me : m.entrySet() ) {
				putInstantaneousSampleValue(me.getKey(), me.getValue());
			}
		}
		m = sample.getAccumulating();
		if ( m != null ) {
			for ( Map.Entry<String, Number> me : m.entrySet() ) {
				putAccumulatingSampleValue(me.getKey(), me.getValue());
			}
		}
		Map<String, Object> s = sample.getStatus();
		if ( s != null ) {
			for ( Map.Entry<String, Object> me : s.entrySet() ) {
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
	 * Get the count of samples added via
	 * {@link #addSample(GeneralDatumSamples)}.
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
