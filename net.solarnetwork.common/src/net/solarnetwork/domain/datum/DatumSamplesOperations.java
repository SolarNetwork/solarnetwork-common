/* ==================================================================
 * DatumSamplesOperations.java - 23/03/2018 9:24:15 AM
 * 
 * Copyright 2018 SolarNetwork.net Dev Team
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
import java.util.Map;
import java.util.Set;
import net.solarnetwork.domain.Differentiable;

/**
 * API for accessing general datum sample property values.
 * 
 * @author matt
 * @version 1.0
 * @since 2.0
 */
public interface DatumSamplesOperations extends Differentiable<DatumSamplesOperations> {

	/**
	 * Get specific sample data.
	 * 
	 * @param type
	 *        the type of sample data to get
	 * @return a map with the specific sample data, or {@literal null}
	 * @throws IllegalArgumentException
	 *         if {@code type} is not supported
	 */
	Map<String, ?> getSampleData(DatumSamplesType type);

	/**
	 * Get an Integer value from a sample map, or {@literal null} if not
	 * available.
	 * 
	 * @param type
	 *        the type of sample data to get
	 * @param key
	 *        the key of the value to get
	 * @return the value as an Integer, or {@literal null} if not available
	 */
	Integer getSampleInteger(DatumSamplesType type, String key);

	/**
	 * Get a Long value from a sample map, or {@literal null} if not available.
	 * 
	 * @param type
	 *        the type of sample data to get
	 * @param key
	 *        the key of the value to get
	 * @return the value as an Long, or {@literal null} if not available
	 */
	Long getSampleLong(DatumSamplesType type, String key);

	/**
	 * Get a Float value from a sample map, or {@literal null} if not available.
	 * 
	 * @param type
	 *        the type of sample data to get
	 * @param key
	 *        the key of the value to get
	 * @return the value as an Float, or {@literal null} if not available
	 */
	Float getSampleFloat(DatumSamplesType type, String key);

	/**
	 * Get a Double value from a sample map, or {@literal null} if not
	 * available.
	 * 
	 * @param type
	 *        the type of sample data to get
	 * @param key
	 *        the key of the value to get
	 * @return the value as an Double, or {@literal null} if not available
	 */
	Double getSampleDouble(DatumSamplesType type, String key);

	/**
	 * Get a BigDecimal value from a sample map, or {@literal null} if not
	 * available.
	 * 
	 * @param type
	 *        the type of sample data to get
	 * @param key
	 *        the key of the value to get
	 * @return the value as an BigDecimal, or {@literal null} if not available
	 */
	BigDecimal getSampleBigDecimal(DatumSamplesType type, String key);

	/**
	 * Get a String value from a sample map, or {@literal null} if not
	 * available.
	 * 
	 * <p>
	 * If {@code type} is {@link DatumSamplesType#Tag}, then this method will
	 * return {@code key} if a tag by that name exists and otherwise it will
	 * return {@literal null}.
	 * </p>
	 * 
	 * @param type
	 *        the type of sample data to get
	 * @param key
	 *        the key of the value, or tag name, to get
	 * @return the value as an String, or {@literal null} if not available
	 */
	String getSampleString(DatumSamplesType type, String key);

	/**
	 * Get a sample value.
	 * 
	 * <p>
	 * If {@code type} is {@link DatumSamplesType#Tag}, then this method will
	 * return {@code key} if a tag by that name exists and otherwise it will
	 * return {@literal null}.
	 * </p>
	 * 
	 * @param <V>
	 *        the expected value type
	 * @param type
	 *        the type of sample data to get
	 * @param key
	 *        the key of the value, or tag name, to get
	 * @return the value cast as a {@code V}, or {@literal null} if not
	 *         available
	 */
	<V> V getSampleValue(DatumSamplesType type, String key);

	/**
	 * Test is a sample value is present for a given key.
	 * 
	 * <p>
	 * Tags can be tested for as well by passing {@link DatumSamplesType#Tag}
	 * and the tag name as {@code key}.
	 * </p>
	 * 
	 * @param type
	 *        the type of sample data to test
	 * @param key
	 *        the key of the value, or name of the tag, to look for
	 * @return {@literal true} if a value is present for the given key
	 */
	boolean hasSampleValue(DatumSamplesType type, String key);

	/**
	 * Find a sample value.
	 * 
	 * <p>
	 * This will search {@code Instantaneous}, {@code Accumulating}, and
	 * {@code Status} data types, in that order, and return the first non-null
	 * value found.
	 * </p>
	 * 
	 * @param <V>
	 *        the expected value type
	 * @param key
	 *        the key of the value, or tag name, to get
	 * @return the value cast as a {@code V}, or {@literal null} if not
	 *         available
	 */
	<V> V findSampleValue(String key);

	/**
	 * Test is a sample value is present for a given key.
	 * 
	 * <p>
	 * This will search {@code Instantaneous}, {@code Accumulating}, and
	 * {@code Status} data types, in that order, and return the first non-null
	 * value found.
	 * </p>
	 * 
	 * @param key
	 *        the key of the value, or name of the tag, to look for
	 * @return {@literal true} if a value is present for the given key
	 */
	boolean hasSampleValue(String key);

	/**
	 * Get the sample tags.
	 * 
	 * @return the tags, or {@literal null}
	 */
	Set<String> getTags();

	/**
	 * Test if a given tag is set.
	 * 
	 * @param tag
	 *        the tag to look for
	 * @return {@literal true} if the given tag has been set on this instance
	 */
	default boolean hasTag(String tag) {
		Set<String> tags = getTags();
		return (tags != null && tags.contains(tag));
	}

	default boolean isEmpty() {
		for ( DatumSamplesType t : new DatumSamplesType[] { DatumSamplesType.Accumulating,
				DatumSamplesType.Instantaneous, DatumSamplesType.Status } ) {
			Map<String, ?> d = getSampleData(t);
			if ( d != null && !d.isEmpty() ) {
				return false;
			}
		}
		Set<String> tags = getTags();
		return (tags == null || tags.isEmpty());
	}

	@Override
	default boolean differsFrom(DatumSamplesOperations other) {
		if ( other == null ) {
			return true;
		} else if ( this == other ) {
			return false;
		}

		for ( DatumSamplesType t : new DatumSamplesType[] { DatumSamplesType.Accumulating,
				DatumSamplesType.Instantaneous, DatumSamplesType.Status } ) {
			Map<String, ?> d1 = getSampleData(t);
			Map<String, ?> d2 = other.getSampleData(t);
			if ( d1 == null ) {
				if ( d2 != null ) {
					return true;
				}
			} else if ( !d1.equals(d2) ) {
				return true;
			}
		}
		if ( getTags() == null ) {
			if ( other.getTags() != null ) {
				return true;
			}
		} else if ( !getTags().equals(other.getTags()) ) {
			return true;
		}

		return false;
	}
}
