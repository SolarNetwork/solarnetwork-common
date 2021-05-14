/* ==================================================================
 * DatumSamplesExpressionRoot.java - 14/05/2021 9:51:52 AM
 * 
 * Copyright 2021 SolarNetwork.net Dev Team
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

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.solarnetwork.domain.datum.Datum;
import net.solarnetwork.domain.datum.GeneralDatum;

/**
 * An expression root object implementation that acts like a composite map of
 * parameters, sample data, and datum properties.
 * 
 * <p>
 * The {@code Map} implementation treats the given {@code Datum},
 * {@link GeneralDatumSamplesOperations}, and parameter {@code Map} as a single
 * {@code Map}, where keys are handled by returning the first found
 * non-{@literal null} value in the following order:
 * </p>
 * 
 * <ol>
 * <li>parameter {@code Map}</li>
 * <li>the {@code GeneralDatumSamplesOperations} sample, following the
 * {@code Instantaneous}, {@code Accumulating}, and {@code Status} priority
 * defined in {@link GeneralDatumSamplesOperations#findSampleValue(String)}</li>
 * <li>if the {@code Datum} implements {@link GeneralDatum}, then call
 * {@link GeneralDatum#asSampleOperations()} and follow the
 * {@link GeneralDatumSamplesOperations#findSampleValue(String)} rules again on
 * that</li>
 * </ol>
 * 
 * @author matt
 * @version 1.0
 * @since 1.71
 */
public class DatumSamplesExpressionRoot extends AbstractMap<String, Object>
		implements DatumExpressionRoot, Map<String, Object> {

	private final Datum datum;
	private final GeneralDatumSamplesOperations datumOps;
	private final GeneralDatumSamplesOperations sample;
	private final Map<String, ?> parameters;

	/**
	 * Constructor.
	 * 
	 * @param datum
	 *        the datum currently being populated
	 * @param
	 */
	public DatumSamplesExpressionRoot(Datum datum, GeneralDatumSamplesOperations sample,
			Map<String, ?> parameters) {
		super();
		this.datum = datum;
		if ( datum instanceof GeneralDatum ) {
			this.datumOps = ((GeneralDatum) datum).asSampleOperations();
		} else {
			this.datumOps = null;
		}
		this.sample = sample;
		this.parameters = parameters;
	}

	@Override
	public Datum getDatum() {
		return datum;
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

	private static final GeneralDatumSamplesType[] TYPES = new GeneralDatumSamplesType[] {
			GeneralDatumSamplesType.Instantaneous, GeneralDatumSamplesType.Accumulating,
			GeneralDatumSamplesType.Status };

	private final class EntrySet extends AbstractSet<Entry<String, Object>>
			implements Set<Entry<String, Object>> {

		private final Map<String, Object> delegate;

		private EntrySet() {
			super();
			delegate = new LinkedHashMap<>();
			if ( datumOps != null ) {
				for ( GeneralDatumSamplesType type : TYPES ) {
					Map<String, ?> data = datumOps.getSampleData(type);
					if ( data != null ) {
						delegate.putAll(data);
					}
				}
			}
			if ( sample != null ) {
				for ( GeneralDatumSamplesType type : TYPES ) {
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

}
