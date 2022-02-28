/* ==================================================================
 * DatumSamplesExpressionRoot.java - 14/05/2021 9:51:52 AM
 * 
 * Copyright 2021 SolarNetwork.net Dev Team
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

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * An expression root object implementation that acts like a composite map of
 * parameters, sample data, and datum properties.
 * 
 * <p>
 * The {@code Map} implementation treats the given {@code Datum},
 * {@link DatumSamplesOperations}, and parameter {@code Map} as a single
 * {@code Map}, where keys are handled by returning the first found
 * non-{@literal null} value in the following order:
 * </p>
 * 
 * <ol>
 * <li>parameter {@code Map}</li>
 * <li>the {@code DatumSamplesOperations} sample, following the
 * {@code Instantaneous}, {@code Accumulating}, and {@code Status} priority
 * defined in {@link DatumSamplesOperations#findSampleValue(String)}</li>
 * <li>if the {@code Datum} implements {@link Datum}, then call
 * {@link Datum#asSampleOperations()} and follow the
 * {@link DatumSamplesOperations#findSampleValue(String)} rules again on
 * that</li>
 * </ol>
 * 
 * @author matt
 * @version 2.1
 * @since 1.71
 */
public class DatumSamplesExpressionRoot extends AbstractMap<String, Object>
		implements DatumExpressionRoot, DatumMathFunctions {

	private final Datum datum;
	private final DatumSamplesOperations datumOps;
	private final DatumSamplesOperations sample;
	private final Map<String, ?> parameters;

	/**
	 * Constructor.
	 * 
	 * @param datum
	 *        the datum currently being populated
	 * @param sample
	 *        the samples
	 * @param parameters
	 *        the parameters
	 */
	public DatumSamplesExpressionRoot(Datum datum, DatumSamplesOperations sample,
			Map<String, ?> parameters) {
		super();
		this.datum = datum;
		this.datumOps = (datum != null ? datum.asSampleOperations() : null);
		this.sample = sample;
		this.parameters = parameters;
	}

	@Override
	public Datum getDatum() {
		return datum;
	}

	/**
	 * Get the samples.
	 * 
	 * <p>
	 * This may or may not be the same samples as returned by
	 * {@link Datum#asSampleOperations()} on the {@code Datum} returned by
	 * {@link #getDatum()}.
	 * </p>
	 * 
	 * @return the datum samples; may be {@literal null}
	 */
	public DatumSamplesOperations getSample() {
		return sample;
	}

	/**
	 * Get optional additional parameters.
	 * 
	 * @return the parameters; may be {@literal null}
	 */
	public Map<String, ?> getParameters() {
		return parameters;
	}

	/**
	 * Get the data map.
	 * 
	 * <p>
	 * This method returns this object.
	 * </p>
	 * 
	 * @return this object
	 */
	@Override
	public Map<String, ?> getData() {
		return this;
	}

	/**
	 * Get the property map.
	 * 
	 * <p>
	 * This method returns this object.
	 * </p>
	 * 
	 * @return this object
	 */
	@Override
	public Map<String, ?> getProps() {
		return this;
	}

	@Override
	public boolean containsKey(Object key) {
		return get(key) != null;
	}

	/**
	 * An alias for {@link #containsKey(Object)}
	 * 
	 * @param key
	 *        the key to search for
	 * @return {@literal true} if a property with the given key exists
	 */
	public boolean has(Object key) {
		return get(key) != null;
	}

	@Override
	public Object get(Object key) {
		if ( key == null ) {
			return null;
		}
		String k = key.toString();
		Object o = null;
		if ( parameters != null ) {
			o = parameters.get(key);
			if ( o != null ) {
				return o;
			}
		}
		if ( sample != null ) {
			o = sample.findSampleValue(k);
			if ( o != null ) {
				return o;
			}
		}
		if ( datumOps != null ) {
			o = datumOps.findSampleValue(k);
			if ( o != null ) {
				return o;
			}
		}
		return null;
	}

	@Override
	public Set<Entry<String, Object>> entrySet() {
		return new EntrySet();
	}

	private static final DatumSamplesType[] TYPES = new DatumSamplesType[] {
			DatumSamplesType.Instantaneous, DatumSamplesType.Accumulating, DatumSamplesType.Status };

	private static final DatumSamplesType[] NUMBER_TYPES = new DatumSamplesType[] {
			DatumSamplesType.Instantaneous, DatumSamplesType.Accumulating };

	private final class EntrySet extends AbstractSet<Entry<String, Object>>
			implements Set<Entry<String, Object>> {

		private final Map<String, Object> delegate;

		private EntrySet() {
			super();
			delegate = new LinkedHashMap<>();
			if ( datumOps != null ) {
				for ( DatumSamplesType type : TYPES ) {
					Map<String, ?> data = datumOps.getSampleData(type);
					if ( data != null ) {
						delegate.putAll(data);
					}
				}
			}
			if ( sample != null ) {
				for ( DatumSamplesType type : TYPES ) {
					Map<String, ?> data = sample.getSampleData(type);
					if ( data != null ) {
						delegate.putAll(data);
					}
				}
			}
			if ( parameters != null ) {
				delegate.putAll(parameters);
			}
		}

		@Override
		public Iterator<Entry<String, Object>> iterator() {
			return delegate.entrySet().iterator();
		}

		@Override
		public int size() {
			return delegate.size();
		}

	}

	private static <T> void populateMap(Map<String, T> dest, DatumSamplesOperations ops,
			DatumSamplesType[] types, Pattern pat) {
		for ( DatumSamplesType type : types ) {
			@SuppressWarnings("unchecked")
			Map<String, T> data = (Map<String, T>) ops.getSampleData(type);
			if ( data != null ) {
				for ( Entry<String, T> e : data.entrySet() ) {
					if ( pat == null || pat.matcher(e.getKey()).find() ) {
						dest.put(e.getKey(), e.getValue());
					}
				}
			}
		}
	}

	/**
	 * Group a set of properties matching a pattern into a collection.
	 * 
	 * @param pattern
	 *        the property name pattern to group
	 * @return the group of matching properties, never {@literal null}
	 * @since 2.1
	 */
	public final Collection<? extends Number> group(String pattern) {
		Pattern pat = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
		Map<String, Number> values = new LinkedHashMap<>(8);
		if ( datumOps != null ) {
			populateMap(values, datumOps, NUMBER_TYPES, pat);
		}
		if ( sample != null ) {
			populateMap(values, sample, NUMBER_TYPES, pat);
		}
		if ( parameters != null ) {
			for ( Entry<String, ?> e : parameters.entrySet() ) {
				if ( pat.matcher(e.getKey()).find() && (e.getValue() instanceof Number) ) {
					values.put(e.getKey(), (Number) e.getValue());
				}
			}
		}
		return values.values();
	}

}
