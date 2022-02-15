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

import java.math.RoundingMode;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.solarnetwork.domain.DatumExpressionRoot;
import net.solarnetwork.util.NumberUtils;

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
		implements DatumExpressionRoot, Map<String, Object> {

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
		if ( datum instanceof Datum ) {
			this.datumOps = datum.asSampleOperations();
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

	private static final DatumSamplesType[] TYPES = new DatumSamplesType[] {
			DatumSamplesType.Instantaneous, DatumSamplesType.Accumulating, DatumSamplesType.Status };

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

	/**
	 * Return the minimum between two number values.
	 * 
	 * @param n1
	 *        the first number
	 * @param n2
	 *        the second number
	 * @return the minimum number, or {@literal null} if both arguments are
	 *         {@literal null}
	 * @since 2.1
	 */
	public static final Number min(Number n1, Number n2) {
		return NumberUtils.min(n1, n2);
	}

	/**
	 * Return the maximum between two number values.
	 * 
	 * @param n1
	 *        the first number
	 * @param n2
	 *        the second number
	 * @return the maximum number, or {@literal null} if both arguments are
	 *         {@literal null}
	 * @since 2.1
	 */
	public static final Number max(Number n1, Number n2) {
		return NumberUtils.max(n1, n2);
	}

	/**
	 * Round positive numbers away from zero and negative numbers towards zero,
	 * to the nearest integer multiple of a specific significance.
	 * 
	 * @param n
	 *        the number to round
	 * @param significance
	 *        the multiple factor to round to
	 * @return the rounded number, or {@literal null} if {@code n} or
	 *         {@code significance} are {@literal null}
	 * @since 2.1
	 */
	public static final Number ceil(Number n, Number significance) {
		return NumberUtils.ceil(n, significance);
	}

	/**
	 * Round positive numbers towards zero and negative numbers away from zero,
	 * to the nearest integer multiple of a specific significance.
	 * 
	 * @param n
	 *        the number to round
	 * @param significance
	 *        the multiple factor to round to
	 * @return the rounded number, or {@literal null} if {@code n} or
	 *         {@code significance} are {@literal null}
	 * @since 2.1
	 */
	public static final Number floor(Number n, Number significance) {
		return NumberUtils.floor(n, significance);
	}

	/**
	 * Round a number towards zero to the nearest integer multiple of a specific
	 * significance.
	 * 
	 * @param n
	 *        the number to round
	 * @param significance
	 *        the multiple factor to round to
	 * @return the rounded number, or {@literal null} if {@code n} or
	 *         {@code significance} are {@literal null}
	 * @since 2.1
	 */
	public static final Number up(Number n, Number significance) {
		return NumberUtils.up(n, significance);
	}

	/**
	 * Round a number towards zero to the nearest integer multiple of a specific
	 * significance.
	 * 
	 * <p>
	 * This method rounds using the {@link RoundingMode#DOWN} mode.
	 * </p>
	 * 
	 * @param n
	 *        the number to round
	 * @param significance
	 *        the multiple factor to round to
	 * @return the rounded number, or {@literal null} if {@code n} or
	 *         {@code significance} are {@literal null}
	 * @since 2.1
	 */
	public static final Number down(Number n, Number significance) {
		return NumberUtils.down(n, significance);
	}

	/**
	 * Round a number to the nearest integer multiple of a specific
	 * significance.
	 * 
	 * @param n
	 *        the number to round
	 * @param significance
	 *        the multiple factor to round to
	 * @return the rounded number, or {@literal null} if {@code n} or
	 *         {@code significance} are {@literal null}
	 * @since 2.1
	 */
	public static final Number mround(Number n, Number significance) {
		return NumberUtils.mround(n, significance);
	}

	/**
	 * Round a number to a maximum number of decimal digits using the
	 * {@link RoundingMode#HALF_UP} mode.
	 * 
	 * @param n
	 *        the number to round
	 * @param digits
	 *        the maximum number of decimal digits
	 * @return the rounded number, or {@literal null} if {@code n} or
	 *         {@code digits} is {@literal null}
	 * @since 2.1
	 */
	public static final Number round(Number n, Number digits) {
		return NumberUtils.round(n, digits);
	}

	/**
	 * Round a number away from zero to a maximum number of decimal digits.
	 * 
	 * @param n
	 *        the number to round
	 * @param digits
	 *        the maximum number of decimal digits
	 * @return the rounded number, or {@literal null} if {@code n} or
	 *         {@code digits} is {@literal null}
	 * @since 2.1
	 */
	public static final Number roundUp(Number n, Number digits) {
		return NumberUtils.roundUp(n, digits);
	}

	/**
	 * Round a number towards zero to a maximum number of decimal digits.
	 * 
	 * @param n
	 *        the number to round
	 * @param digits
	 *        the maximum number of decimal digits
	 * @return the rounded number, or {@literal null} if {@code n} or
	 *         {@code digits} is {@literal null}
	 * @since 2.1
	 */
	public static final Number roundDown(Number n, Number digits) {
		return NumberUtils.roundDown(n, digits);
	}

}
