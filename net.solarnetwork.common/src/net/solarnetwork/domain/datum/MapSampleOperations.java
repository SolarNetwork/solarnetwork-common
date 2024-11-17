/* ==================================================================
 * MapSampleOperations.java - 5/03/2022 12:33:51 PM
 *
 * Copyright 2022 SolarNetwork.net Dev Team
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

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.solarnetwork.util.CollectionUtils;
import net.solarnetwork.util.ObjectUtils;

/**
 * {@link MutableDatumSamplesOperations} that delegates all operations to a
 * simple Map.
 *
 * <p>
 * All the methods of this API ignore any {@link DatumSamplesType} argument, and
 * simply get/set values in a single {@link Map} passed to the constructor. A
 * {@link DatumSamplesOperations} delegate can also be provided, in which case
 * all get methods will delegate to that instance, falling back to the internal
 * {@link Map} if the value is not found.
 * </p>
 *
 * @author matt
 * @version 1.1
 * @since 2.3
 */
public class MapSampleOperations implements MutableDatumSamplesOperations {

	private final Map<String, Object> parameters;
	private final DatumSamplesOperations datum;

	/**
	 * Constructor.
	 *
	 * @param parameters
	 *        the parameters
	 * @throws IllegalArgumentException
	 *         if any argument is {@literal null}
	 */
	public MapSampleOperations(Map<String, Object> parameters) {
		this(parameters, null);
	}

	/**
	 * Constructor.
	 *
	 * @param parameters
	 *        the parameters
	 * @param datum
	 *        the optional sample operations to delegate get operations to
	 * @throws IllegalArgumentException
	 *         if {@code parameters} is {@literal null}
	 */
	public MapSampleOperations(Map<String, Object> parameters, DatumSamplesOperations datum) {
		super();
		this.parameters = ObjectUtils.requireNonNullArgument(parameters, "parameters");
		this.datum = datum;
	}

	@Override
	public Map<String, ?> getSampleData(DatumSamplesType type) {
		Map<String, Object> result = parameters;
		Map<String, ?> datumProps = datum.getSampleData(type);
		if ( datumProps != null ) {
			result = new LinkedHashMap<>(result);
			result.putAll(datumProps);
		}
		return result;
	}

	@Override
	public Integer getSampleInteger(DatumSamplesType type, String key) {
		Integer result = null;
		if ( datum != null ) {
			result = datum.getSampleInteger(type, key);
		}
		return (result != null ? result : CollectionUtils.getMapInteger(key, parameters));
	}

	@Override
	public Long getSampleLong(DatumSamplesType type, String key) {
		Long result = null;
		if ( datum != null ) {
			result = datum.getSampleLong(type, key);
		}
		return (result != null ? result : CollectionUtils.getMapLong(key, parameters));
	}

	@Override
	public Float getSampleFloat(DatumSamplesType type, String key) {
		Float result = null;
		if ( datum != null ) {
			result = datum.getSampleFloat(type, key);
		}
		return (result != null ? result : CollectionUtils.getMapFloat(key, parameters));
	}

	@Override
	public Double getSampleDouble(DatumSamplesType type, String key) {
		Double result = null;
		if ( datum != null ) {
			result = datum.getSampleDouble(type, key);
		}
		return (result != null ? result : CollectionUtils.getMapDouble(key, parameters));
	}

	@Override
	public BigDecimal getSampleBigDecimal(DatumSamplesType type, String key) {
		BigDecimal result = null;
		if ( datum != null ) {
			result = datum.getSampleBigDecimal(type, key);
		}
		return (result != null ? result : CollectionUtils.getMapBigDecimal(key, parameters));
	}

	@Override
	public String getSampleString(DatumSamplesType type, String key) {
		String result = null;
		if ( datum != null ) {
			result = datum.getSampleString(type, key);
		}
		return (result != null ? result : CollectionUtils.getMapString(key, parameters));
	}

	@Override
	public <V> V getSampleValue(DatumSamplesType type, String key) {
		if ( datum != null ) {
			return datum.getSampleValue(type, key);
		}
		return findSampleValue(key);
	}

	@Override
	public boolean hasSampleValue(DatumSamplesType type, String key) {
		if ( datum != null ) {
			return datum.hasSampleValue(type, key);
		}
		return hasSampleValue(key);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V> V findSampleValue(String key) {
		V result = null;
		if ( datum != null ) {
			result = datum.findSampleValue(key);
		}
		return (result != null ? result : (V) parameters.get(key));
	}

	@Override
	public boolean hasSampleValue(String key) {
		return (findSampleValue(key) != null);
	}

	@Override
	public void clear() {
		parameters.clear();
	}

	@Override
	public void putSampleValue(DatumSamplesType type, String key, Object value) {
		parameters.put(key, value);
	}

	/**
	 * Set the sample data map.
	 *
	 * <p>
	 * <b>Note</b> this method does <b>not</b> set {@code data} as the map used
	 * internally by this class. Instead the internal map is cleared and all
	 * values in {@code data} are copied into it. This differs from the contract
	 * of {@link MutableDatumSamplesOperations} but is by design and simply a
	 * compromise required by this class.
	 * </p>
	 *
	 * {@inheritDoc}
	 */
	@Override
	public void setSampleData(DatumSamplesType type, Map<String, ?> data) {
		// we don't allow changing the parameters instance, but we do copy the values
		if ( data != null ) {
			parameters.clear();
			parameters.putAll(data);
		}
	}

	/**
	 * Set the tags.
	 *
	 * <p>
	 * <b>Note</b> this method will always return {@literal null} as tags are
	 * not supported.
	 * </p>
	 *
	 * {@inheritDoc}
	 */
	@Override
	public Set<String> getTags() {
		return null;
	}

	/**
	 * Set the tags.
	 *
	 * <p>
	 * <b>Note</b> this method does nothing as tags are not supported.
	 * </p>
	 *
	 * {@inheritDoc}
	 */
	@Override
	public void setTags(Set<String> tags) {
		// unsupported
	}

}
