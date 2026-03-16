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

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;
import net.solarnetwork.domain.Differentiable;
import net.solarnetwork.util.CollectionUtils;
import net.solarnetwork.util.StringUtils;

/**
 * API for accessing general datum sample property values.
 *
 * @author matt
 * @version 1.3
 * @since 2.0
 */
public interface DatumSamplesOperations extends Differentiable<DatumSamplesOperations> {

	/**
	 * A set of {@link DatumSamplesType} that use {@code Map<String, ?>}-like
	 * storage.
	 *
	 * @since 1.2
	 */
	Set<DatumSamplesType> KEYED_TYPES = unmodifiableSet(new LinkedHashSet<>(asList(
			DatumSamplesType.Instantaneous, DatumSamplesType.Accumulating, DatumSamplesType.Status)));

	/**
	 * A set of {@link DatumSamplesType} that use
	 * {@code Map<String, Number>}-like storage.
	 *
	 * @since 1.2
	 */
	Set<DatumSamplesType> KEYED_NUMBER_TYPES = unmodifiableSet(
			new LinkedHashSet<>(asList(DatumSamplesType.Instantaneous, DatumSamplesType.Accumulating)));

	/**
	 * Get specific sample data.
	 *
	 * @param type
	 *        the type of sample data to get
	 * @return a map with the specific sample data, or {@code null}
	 * @throws IllegalArgumentException
	 *         if {@code type} is not supported
	 */
	@Nullable
	Map<String, ?> getSampleData(DatumSamplesType type);

	/**
	 * Get an Integer value from a sample map, or {@code null} if not available.
	 *
	 * @param type
	 *        the type of sample data to get
	 * @param key
	 *        the key of the value to get
	 * @return the value as an Integer, or {@code null} if not available
	 */
	@Nullable
	Integer getSampleInteger(DatumSamplesType type, String key);

	/**
	 * Get a Long value from a sample map, or {@code null} if not available.
	 *
	 * @param type
	 *        the type of sample data to get
	 * @param key
	 *        the key of the value to get
	 * @return the value as an Long, or {@code null} if not available
	 */
	@Nullable
	Long getSampleLong(DatumSamplesType type, String key);

	/**
	 * Get a Float value from a sample map, or {@code null} if not available.
	 *
	 * @param type
	 *        the type of sample data to get
	 * @param key
	 *        the key of the value to get
	 * @return the value as an Float, or {@code null} if not available
	 */
	@Nullable
	Float getSampleFloat(DatumSamplesType type, String key);

	/**
	 * Get a Double value from a sample map, or {@code null} if not available.
	 *
	 * @param type
	 *        the type of sample data to get
	 * @param key
	 *        the key of the value to get
	 * @return the value as an Double, or {@code null} if not available
	 */
	@Nullable
	Double getSampleDouble(DatumSamplesType type, String key);

	/**
	 * Get a BigDecimal value from a sample map, or {@code null} if not
	 * available.
	 *
	 * @param type
	 *        the type of sample data to get
	 * @param key
	 *        the key of the value to get
	 * @return the value as an BigDecimal, or {@code null} if not available
	 */
	@Nullable
	BigDecimal getSampleBigDecimal(DatumSamplesType type, String key);

	/**
	 * Get a String value from a sample map, or {@code null} if not available.
	 *
	 * <p>
	 * If {@code type} is {@link DatumSamplesType#Tag}, then this method will
	 * return {@code key} if a tag by that name exists and otherwise it will
	 * return {@code null}.
	 * </p>
	 *
	 * @param type
	 *        the type of sample data to get
	 * @param key
	 *        the key of the value, or tag name, to get
	 * @return the value as an String, or {@code null} if not available
	 */
	@Nullable
	String getSampleString(DatumSamplesType type, String key);

	/**
	 * Get a sample value.
	 *
	 * <p>
	 * If {@code type} is {@link DatumSamplesType#Tag}, then this method will
	 * return {@code key} if a tag by that name exists and otherwise it will
	 * return {@code null}.
	 * </p>
	 *
	 * @param <V>
	 *        the expected value type
	 * @param type
	 *        the type of sample data to get
	 * @param key
	 *        the key of the value, or tag name, to get
	 * @return the value cast as a {@code V}, or {@code null} if not available
	 * @throws ClassCastException
	 *         if the sample value cannot be cast to {@code V}
	 */
	<V> @Nullable V getSampleValue(DatumSamplesType type, String key);

	/**
	 * Get a sample value.
	 *
	 * <p>
	 * This method is a nullness-check shortcut, for example to be used after
	 * {@link #hasSampleValue(DatumSamplesType, String)} returns {@code true}.
	 * </p>
	 *
	 * @param <V>
	 *        the expected value type
	 * @param type
	 *        the type of sample data to get
	 * @param key
	 *        the key of the value, or tag name, to get
	 * @return the value cast as a {@code V}, or {@code null} if not available
	 * @throws ClassCastException
	 *         if the sample value cannot be cast to {@code V}
	 * @see #getSampleValue(DatumSamplesType, String)
	 * @since 1.3
	 */
	@SuppressWarnings("NullAway")
	default <V> V sampleValue(DatumSamplesType type, String key) {
		return getSampleValue(type, key);
	}

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
	 * @return the value cast as a {@code V}, or {@code null} if not available
	 */
	<V> @Nullable V findSampleValue(String key);

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
	 * @return the tags, or {@code null}
	 */
	@Nullable
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

	/**
	 * Test if there are any properties available.
	 *
	 * @return {@literal true} if there is at least one non-{@code null}
	 *         property or tag available
	 */
	default boolean isEmpty() {
		for ( DatumSamplesType t : KEYED_TYPES ) {
			Map<String, ?> d = getSampleData(t);
			if ( d != null && !d.isEmpty() ) {
				return false;
			}
		}
		Set<String> tags = getTags();
		return (tags == null || tags.isEmpty());
	}

	@Override
	default boolean differsFrom(@Nullable DatumSamplesOperations other) {
		if ( other == null ) {
			return true;
		} else if ( this == other ) {
			return false;
		}

		for ( DatumSamplesType t : KEYED_TYPES ) {
			Map<String, ?> d1 = getSampleData(t);
			Map<String, ?> d2 = other.getSampleData(t);
			if ( d1 == null ) {
				if ( d2 != null && !d2.isEmpty() ) {
					return true;
				}
			} else if ( d2 == null ) {
				if ( d1 != null && !d1.isEmpty() ) {
					return true;
				}
			} else if ( !d1.equals(d2) ) {
				return true;
			}
		}
		if ( getTags() == null ) {
			if ( other.getTags() != null && !other.getTags().isEmpty() ) {
				return true;
			}
		} else if ( other.getTags() == null ) {
			if ( getTags() != null && !getTags().isEmpty() ) {
				return true;
			}
		} else if ( !getTags().equals(other.getTags()) ) {
			return true;
		}

		return false;
	}

	/**
	 * Test if another operations has any different {@code Number} properties
	 * defined or any property values available in both it an this object are
	 * different.
	 *
	 * <p>
	 * This will use {@link StringUtils#numberValue(String)} to parse string
	 * values.
	 * </p>
	 *
	 * @param other
	 *        the other object to compare to
	 * @return {@literal true} if the object differs from this object
	 * @see #differsNumericallyFrom(DatumSamplesOperations, Function, Function)
	 * @since 1.2
	 */
	default boolean differsNumericallyFrom(@Nullable DatumSamplesOperations other) {
		return differsNumericallyFrom(other, StringUtils::numberValue, null);
	}

	/**
	 * Test if another operations has any different numeric properties defined
	 * or any property values available in both it an this object are different.
	 *
	 * <p>
	 * First, if {@code parser} is provided then non-numeric property values are
	 * converted to strings and passed to this function, with the function
	 * result being used for the property comparison.
	 * </p>
	 *
	 * <p>
	 * Then, if {@code mapper} is provided then numeric property values are
	 * passed to this function, with the function result being used for the
	 * property comparison.
	 * </p>
	 *
	 * <p>
	 * Finally, comparison values that are the same type and implement
	 * {@link Comparable} are compared using
	 * {@link Comparable#compareTo(Object)}. Non-{@code Comparable} property
	 * values are compared using {@link Object#equals(Object)}.
	 * </p>
	 *
	 * @param other
	 *        the other object to compare to
	 * @param parser
	 *        an optional function to map string values to numbers
	 * @param mapper
	 *        an optional function to map number values to other forms, for
	 *        example {@code NumberUtils::bigDecimalForNumber}
	 * @return {@literal true} if the object differs from this object
	 * @since 1.2
	 */
	@SuppressWarnings("unchecked")
	default boolean differsNumericallyFrom(@Nullable DatumSamplesOperations other,
			@Nullable Function<String, @Nullable Number> parser,
			@Nullable Function<Number, @Nullable Number> mapper) {
		if ( other == null ) {
			return true;
		} else if ( this == other ) {
			return false;
		}
		for ( DatumSamplesType propType : (parser != null ? KEYED_TYPES : KEYED_NUMBER_TYPES) ) {
			Map<String, Object> m1 = (Map<String, Object>) this.getSampleData(propType);
			Map<String, Object> m2 = (Map<String, Object>) other.getSampleData(propType);
			if ( CollectionUtils.differsNumerically(m1, m2, parser, mapper) ) {
				return true;
			}
		}
		return false;
	}
}
