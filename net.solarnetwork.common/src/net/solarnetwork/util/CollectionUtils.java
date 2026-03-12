/* ==================================================================
 * CollectionUtils.java - 17/01/2020 10:20:16 am
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

package net.solarnetwork.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;

/**
 * Utility methods for dealing with collections.
 *
 * @author matt
 * @version 1.7
 * @since 1.58
 */
public final class CollectionUtils {

	private CollectionUtils() {
		// not available
	}

	/**
	 * Create integer ranges from a set of integers to produce a reduced set of
	 * ranges that cover all integers in the source set.
	 *
	 * <p>
	 * The resulting ranges are guaranteed to be equal to, <b>or a super set
	 * of</b>, the given source set. Thus the resulting ranges could include
	 * <b>more</b> values than the source set. The resulting ranges will not
	 * intersect any other but could be adjacent. They will also be ordered in
	 * ascending order.
	 * </p>
	 *
	 * <p>
	 * This can be useful when you want to coalesce many discreet ranges into a
	 * smaller number of larger ranges, such as when querying a device for data
	 * in address registers, to reduce the number of overall requests required
	 * to read the complete set of registers. Instead of reading many small
	 * ranges of registers, fewer large ranges of registers can be requested.
	 * For example, if the a set {@code s} passed to this method contained these
	 * ranges:
	 * </p>
	 *
	 * <ul>
	 * <li>0-1</li>
	 * <li>3-5</li>
	 * <li>20-28</li>
	 * <li>404-406</li>
	 * <li>412-418</li>
	 * </ul>
	 *
	 * <p>
	 * then calling this method like {@code minimizeIntRanges(s, 32)} would
	 * return a new set with these ranges:
	 * </p>
	 *
	 * <ul>
	 * <li>0-28</li>
	 * <li>404-418</li>
	 * </ul>
	 *
	 * @param set
	 *        the set of integers to reduce
	 * @param maxRangeLength
	 *        the maximum length of any combined range in the resulting set
	 * @return a new set of ranges possibly combined, or {@code set} if there
	 *         are less than two ranges to start with, or {@code null} if
	 *         {@code set} is {@code null}
	 * @throws IllegalArgumentException
	 *         if {@code maxRangeLength} is less than {@literal 1}
	 */
	public static @Nullable List<IntRange> coveringIntRanges(@Nullable SortedSet<Integer> set,
			int maxRangeLength) {
		if ( maxRangeLength < 1 ) {
			throw new IllegalArgumentException("Max range length must be greater than 0.");
		}
		if ( set == null ) {
			return null;
		}
		List<IntRange> result = new ArrayList<>();
		if ( set.isEmpty() ) {
			return Collections.emptyList();
		}
		IntRange last;
		if ( set instanceof IntOrderedIterable ) {
			final int min = set.first();
			final int[] meta = new int[] { min, min };
			((IntOrderedIterable) set).forEachOrdered(v -> {
				int d = v - meta[0];
				if ( d >= maxRangeLength ) {
					result.add(IntRange.rangeOf(meta[0], meta[1]));
					meta[0] = v;
					meta[1] = v;
				} else {
					meta[1] = v;
				}
			});
			last = IntRange.rangeOf(meta[0], meta[1]);
		} else {
			Iterator<Integer> itr = set.iterator();
			int v = itr.next();
			int a = v;
			int b = a;
			while ( itr.hasNext() ) {
				v = itr.next();
				int d = v - a;
				if ( d >= maxRangeLength ) {
					result.add(IntRange.rangeOf(a, b));
					a = v;
					b = v;
				} else {
					b = v;
				}
			}
			last = IntRange.rangeOf(a, b);
		}
		if ( result.isEmpty() || !result.get(result.size() - 1).equals(last) ) {
			result.add(last);
		}
		return result;
	}

	/**
	 * Convert a dictionary to a map.
	 *
	 * <p>
	 * This creates a new {@link Map} and copies all the key-value pairs from
	 * the given {@link Dictionary} into it.
	 * </p>
	 *
	 * @param <K>
	 *        the key type
	 * @param <V>
	 *        the value type
	 * @param dict
	 *        the dictionary to convert
	 * @return the new map instance, or {@code null} if {@code dict} is
	 *         {@code null}
	 */
	public static <K, V> @Nullable Map<K, V> mapForDictionary(@Nullable Dictionary<K, V> dict) {
		if ( dict == null ) {
			return null;
		}
		Map<K, V> result = new HashMap<>(dict.size());
		Enumeration<K> keyEnum = dict.keys();
		while ( keyEnum.hasMoreElements() ) {
			K key = keyEnum.nextElement();
			if ( key != null ) {
				result.put(key, dict.get(key));
			}
		}
		return result;
	}

	/**
	 * Convert a map to a dictionary.
	 *
	 * <p>
	 * This creates a new {@link Dictionary} and copies all the key-value pairs
	 * from the given {@link Map} into it.
	 * </p>
	 *
	 * @param <K>
	 *        the key type
	 * @param <V>
	 *        the value type
	 * @param map
	 *        the map to convert
	 * @return the new dictionary instance, or {@code null} if {@code map} is
	 *         {@code null}
	 */
	public static <K, V> @Nullable Dictionary<K, V> dictionaryForMap(@Nullable Map<K, V> map) {
		if ( map == null ) {
			return null;
		}
		return new Hashtable<>(map);
	}

	/**
	 * Get a String value out of a Map.
	 *
	 * <p>
	 * If the key exists but is not a String, {@link Object#toString()} will be
	 * called on that object.
	 * </p>
	 *
	 * @param key
	 *        the key of the object to get
	 * @param map
	 *        the map to inspect, {@code null} is allowed
	 * @return the value, or {@code null} if not found
	 * @since 1.2
	 */
	public static @Nullable String getMapString(String key, @Nullable Map<String, ?> map) {
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

	/**
	 * Get a Short value out of a Map.
	 *
	 * <p>
	 * If the key exists, is not an Short, but is a Number,
	 * {@link Number#shortValue()} will be called on that object.
	 * </p>
	 *
	 * @param key
	 *        the key of the object to get
	 * @param map
	 *        the map to inspect, {@code null} is allowed
	 * @return the value, or {@code null} if not found or not compatible with
	 *         Short
	 * @since 1.2
	 */
	public static @Nullable Short getMapShort(String key, @Nullable Map<String, ?> map) {
		if ( map == null ) {
			return null;
		}
		Object n = map.get(key);
		if ( n == null ) {
			return null;
		}
		if ( n instanceof Short ) {
			return (Short) n;
		}
		if ( n instanceof Number ) {
			return ((Number) n).shortValue();
		}
		try {
			return Short.valueOf(n.toString());
		} catch ( NumberFormatException e ) {
			return null;
		}
	}

	/**
	 * Get an Integer value out of a Map.
	 *
	 * <p>
	 * If the key exists, is not an Integer, but is a Number,
	 * {@link Number#intValue()} will be called on that object.
	 * </p>
	 *
	 * @param key
	 *        the key of the object to get
	 * @param map
	 *        the map to inspect, {@code null} is allowed
	 * @return the value, or {@code null} if not found or not compatible with
	 *         Integer
	 * @since 1.2
	 */
	public static @Nullable Integer getMapInteger(String key, @Nullable Map<String, ?> map) {
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

	/**
	 * Get a Long value out of a Map.
	 *
	 * <p>
	 * If the key exists, is not a Long, but is a Number,
	 * {@link Number#longValue()} will be called on that object.
	 * </p>
	 *
	 * @param key
	 *        the key of the object to get
	 * @param map
	 *        the map to inspect, {@code null} is allowed
	 * @return the value, or {@code null} if not found or not compatible with
	 *         Long
	 * @since 1.2
	 */
	public static @Nullable Long getMapLong(String key, @Nullable Map<String, ?> map) {
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

	/**
	 * Get a Float value out of a Map.
	 *
	 * <p>
	 * If the key exists, is not a Float, but is a Number,
	 * {@link Number#floatValue()} will be called on that object.
	 * </p>
	 *
	 * @param key
	 *        the key of the object to get
	 * @param map
	 *        the map to inspect, {@code null} is allowed
	 * @return the value, or {@code null} if not found or not compatible with
	 *         Float
	 * @since 1.2
	 */
	public static @Nullable Float getMapFloat(String key, @Nullable Map<String, ?> map) {
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

	/**
	 * Get a Double value out of a Map
	 *
	 * <p>
	 * If the key exists, is not a Double, but is a Number,
	 * {@link Number#doubleValue()} will be called on that object.
	 * </p>
	 *
	 * @param key
	 *        the key of the object to get
	 * @param map
	 *        the map to inspect, {@code null} is allowed
	 * @return the value, or {@code null} if not found or not compatible with
	 *         Double
	 * @since 1.2
	 */
	public static @Nullable Double getMapDouble(String key, @Nullable Map<String, ?> map) {
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

	/**
	 * Get a BigDecimal value out of a Map.
	 *
	 * <p>
	 * If the key exists but is not a BigDecimal, {@link Object#toString()} will
	 * be called on that object and {@link BigDecimal#BigDecimal(String)} will
	 * be returned.
	 * </p>
	 *
	 * @param key
	 *        the key of the object to get
	 * @param map
	 *        the map to inspect, {@code null} is allowed
	 * @return the value, or {@code null} if not found or not compatible with
	 *         BigDecimal
	 * @since 1.2
	 */
	public static @Nullable BigDecimal getMapBigDecimal(String key, @Nullable Map<String, ?> map) {
		if ( map == null ) {
			return null;
		}
		Object n = map.get(key);
		if ( n == null ) {
			return null;
		}
		if ( n instanceof Number ) {
			return NumberUtils.bigDecimalForNumber((Number) n);
		}
		try {
			return new BigDecimal(n.toString());
		} catch ( NumberFormatException e ) {
			return null;
		}
	}

	/**
	 * Get a BigDecimal value out of a Map.
	 *
	 * <p>
	 * If the key exists but is not a BigDecimal, {@link Object#toString()} will
	 * be called on that object and {@link BigDecimal#BigDecimal(String)} will
	 * be returned.
	 * </p>
	 *
	 * @param key
	 *        the key of the object to get
	 * @param map
	 *        the map to inspect, {@code null} is allowed
	 * @return the value, or {@code null} if not found or not compatible with
	 *         BigInteger
	 * @since 1.2
	 */
	public static @Nullable BigInteger getMapBigInteger(String key, @Nullable Map<String, ?> map) {
		if ( map == null ) {
			return null;
		}
		Object n = map.get(key);
		if ( n == null ) {
			return null;
		}
		if ( n instanceof Number ) {
			return NumberUtils.bigIntegerForNumber((Number) n);
		}
		try {
			return new BigInteger(n.toString());
		} catch ( NumberFormatException e ) {
			return null;
		}
	}

	/**
	 * Get a service property value.
	 *
	 * @param <T>
	 *        the expected type of the value
	 * @param key
	 *        the map property key to get the value for
	 * @param type
	 *        the type of the value
	 * @param defaultResult
	 *        the default instant to return if the property is not available
	 * @param map
	 *        the map to inspect, {@code null} is allowed
	 * @return the property value, or {@code null} if not available or cannot be
	 *         converted to the given type or if a string value is empty
	 * @since 1.7
	 */
	@SuppressWarnings("unchecked")
	public static <T> @Nullable T mapProperty(final @Nullable String key, final Class<T> type,
			@Nullable T defaultResult, final @Nullable Map<String, ?> map) {
		if ( key == null || map == null ) {
			return defaultResult;
		}
		assert key != null && type != null;
		Object val = (map != null ? map.get(key) : null);
		if ( val == null ) {
			return defaultResult;
		}
		if ( String.class.isAssignableFrom(type) ) {
			String s = val.toString();
			return (T) (s.isEmpty() ? null : s);
		} else if ( type.isAssignableFrom(val.getClass()) ) {
			return (T) val;
		} else if ( Number.class.isAssignableFrom(type) ) {
			try {
				if ( val instanceof Number n ) {
					return (T) NumberUtils.convertNumber(n, (Class<? extends Number>) type);
				}
				return (T) NumberUtils.parseNumber(val.toString(), (Class<? extends Number>) type);
			} catch ( IllegalArgumentException e ) {
				// ignore and return null
			}
		}
		return defaultResult;
	}

	/**
	 * Resolve a string map from a service property value.
	 *
	 * <p>
	 * String property values are parsed using
	 * {@link StringUtils#commaDelimitedStringToMap(String)}.
	 * </p>
	 *
	 * @param key
	 *        the map property key to extract
	 * @param map
	 *        the map to inspect, {@code null} is allowed
	 * @return the mapping, or {@code null}
	 * @since 1.7
	 */
	@SuppressWarnings("unchecked")
	public static @Nullable Map<String, String> mapPropertyStringMap(final @Nullable String key,
			final @Nullable Map<String, ?> map) {
		if ( key == null || map == null ) {
			return null;
		}
		final Object propVal = map.get(key);
		final Map<String, String> result;
		if ( propVal instanceof Map<?, ?> ) {
			result = (Map<String, String>) propVal;
		} else if ( propVal != null ) {
			result = StringUtils.commaDelimitedStringToMap(propVal.toString());
		} else {
			result = null;
		}
		return result;
	}

	/**
	 * Resolve a list from a service property value.
	 *
	 * <p>
	 * The property value can be a {@code List}, array, or a comma-delimited
	 * single value.
	 * </p>
	 *
	 * @param key
	 *        the map property key to extract
	 * @param map
	 *        the map to inspect, {@code null} is allowed
	 * @return the list, or {@code null}
	 * @since 1.7
	 */
	@SuppressWarnings("unchecked")
	public static @Nullable List<String> mapPropertyStringList(final @Nullable String key,
			final @Nullable Map<String, ?> map) {
		if ( key == null || map == null ) {
			return null;
		}
		final Object propVal = map.get(key);
		if ( propVal == null ) {
			return null;
		}
		final List<String> result;
		if ( propVal instanceof List<?> l && !l.isEmpty() ) {
			if ( l.get(0) instanceof String ) {
				// assume all values are strings
				result = (List<String>) l;
			} else {
				result = new ArrayList<>(l.size());
				for ( Object o : l ) {
					if ( o != null ) {
						result.add(o.toString());
					}
				}
			}
		} else if ( propVal instanceof String[] a ) {
			result = Arrays.asList(a);
		} else if ( propVal instanceof Object[] a ) {
			result = new ArrayList<>(a.length);
			for ( Object o : a ) {
				if ( o != null ) {
					result.add(o.toString());
				}
			}
		} else {
			result = StringUtils.commaDelimitedStringToList(propVal.toString());
		}
		return result;
	}

	/**
	 * Resolve an {@link Instant} from a setting on a configuration.
	 *
	 * <p>
	 * The property value can be a number value, which will be treated as
	 * milliseconds since the epoch, or an ISO timestamp suitable for passing to
	 * {@link Instant#parse(CharSequence)} (for example {@code PT2H} for "2
	 * hours").
	 * </p>
	 *
	 * @param key
	 *        the map property key to extract
	 * @param defaultResult
	 *        the default instant to return if the property is not available
	 * @param map
	 *        the map to inspect, {@code null} is allowed
	 * @return the instant, or {@code defaultResult} if a timestamp is not
	 *         available
	 * @since 1.7
	 */
	public static @Nullable Instant mapPropertyTimestamp(final @Nullable String key,
			final @Nullable Instant defaultResult, final @Nullable Map<String, ?> map) {
		if ( key == null || map == null ) {
			return defaultResult;
		}
		final Object propVal = map.get(key);
		if ( propVal instanceof Instant d ) {
			return d;
		} else if ( propVal instanceof Number n ) {
			return Instant.ofEpochMilli(n.longValue());
		} else if ( propVal != null ) {
			String s = propVal.toString();
			try {
				return Instant.parse(s);
			} catch ( DateTimeParseException e ) {
				// not parsable...
			}
		}
		return defaultResult;
	}

	/**
	 * Resolve a {@link Number} from a setting on a configuration.
	 *
	 * <p>
	 * String values will be parsed witH
	 * {@link StringUtils#numberValue(String)}, and narrowed to 32-bit (if
	 * possible) using {@link NumberUtils#narrow32(Number)}.
	 * </p>
	 *
	 * @param key
	 *        the map property key to extract
	 * @param defaultResult
	 *        the default number to return if the property is not available
	 * @param map
	 *        the map to inspect, {@code null} is allowed
	 * @return the number, or {@code defaultResult} if a number is not available
	 * @since 1.7
	 */
	public static @Nullable Number mapPropertyNumber(final @Nullable String key,
			final @Nullable Number defaultResult, final @Nullable Map<String, ?> map) {
		return mapPropertyNumber(key, defaultResult, StringUtils::numberValue, NumberUtils::narrow32,
				map);
	}

	/**
	 * Resolve a {@link Number} from a setting on a configuration.
	 *
	 * <p>
	 * The property value can be a number or string value.
	 * </p>
	 *
	 * <p>
	 * First, if {@code parser} is provided then non-numeric property values are
	 * converted to strings and passed to this function.
	 * </p>
	 *
	 * <p>
	 * Then, if {@code mapper} is provided then numeric property values are
	 * passed to this function, with the function result being returned.
	 * </p>
	 *
	 * @param key
	 *        the map property key to extract
	 * @param defaultResult
	 *        the default number to return if the property is not available
	 * @param parser
	 *        an optional function to map string values to numbers
	 * @param mapper
	 *        an optional function to map number values to other forms, for
	 *        example {@code NumberUtils::bigDecimalForNumber}
	 * @param map
	 *        the map to inspect, {@code null} is allowed
	 * @return the number, or {@code defaultResult} if a number is not available
	 * @since 1.7
	 */
	public static @Nullable Number mapPropertyNumber(final @Nullable String key,
			final @Nullable Number defaultResult, @Nullable Function<String, Number> parser,
			@Nullable Function<Number, Number> mapper, final @Nullable Map<String, ?> map) {
		if ( key == null || map == null ) {
			return defaultResult;
		}
		final Object propVal = map.get(key);
		if ( propVal == null ) {
			return defaultResult;
		}

		Number numVal = (propVal instanceof Number n ? n : null);
		if ( numVal == null && parser != null && propVal != null ) {
			numVal = parser.apply(propVal.toString());
		}

		if ( mapper != null ) {
			numVal = mapper.apply(numVal);
		}

		return (numVal != null ? numVal : defaultResult);
	}

	/**
	 * Resolve a {@link Duration} from a setting on a configuration.
	 *
	 * <p>
	 * The property value can be a number value, which will be treated as
	 * seconds, or an ISO duration suitable for passing to
	 * {@link Duration#parse(CharSequence)} (for example {@code PT2H} for "2
	 * hours").
	 * </p>
	 *
	 * @param key
	 *        the map property key to extract
	 * @param defaultResult
	 *        the default duration to return if the property is not available
	 * @param map
	 *        the map to inspect, {@code null} is allowed
	 * @return the duration, or {@code defaultResult} if a duration is not
	 *         available
	 * @since 1.7
	 */
	public static @Nullable Duration mapPropertyDuration(final String key,
			final @Nullable Duration defaultResult, final @Nullable Map<String, ?> map) {
		if ( key == null || map == null ) {
			return null;
		}
		final Object propVal = map.get(key);
		if ( propVal instanceof Duration d ) {
			return d;
		} else if ( propVal instanceof Number n ) {
			return Duration.ofSeconds(n.longValue());
		} else if ( propVal != null ) {
			String s = propVal.toString();
			Number n = NumberUtils.parseNumber(s);
			if ( n != null ) {
				return Duration.ofSeconds(n.longValue());
			}
			try {
				return Duration.parse(s);
			} catch ( DateTimeParseException e ) {
				// not parsable...
			}
		}
		return defaultResult;
	}

	/**
	 * A regular expression to match sensitive key names.
	 *
	 * @since 1.2
	 * @see #sensitiveNamesToMask(Set)
	 */
	public static final Pattern SENSITIVE_NAME_PATTERN = Pattern.compile("(?:secret|pass)",
			Pattern.CASE_INSENSITIVE);

	/**
	 * Extract a set of values from a set that have sensitive-sounding names.
	 *
	 * <p>
	 * The point of this method is to identify keys from a map that appear to
	 * have associated "sensitive" values, such as passwords, with the aim of
	 * then not printing those values somewhere, such as the application log.
	 * </p>
	 *
	 * <p>
	 * This method calls {@link #valuesMatching(Set, Pattern)}, passing in the
	 * {@link #SENSITIVE_NAME_PATTERN} pattern.
	 * </p>
	 *
	 * @param set
	 *        the names to examine, {@code null} is allowed
	 * @return the sensitive looking names, never {@code null}
	 * @since 1.2
	 * @see StringUtils#sha256MaskedMap(Map, Set)
	 */
	public static Set<String> sensitiveNamesToMask(@Nullable Set<String> set) {
		return valuesMatching(set, SENSITIVE_NAME_PATTERN);
	}

	/**
	 * Extract a set of values from a set that match a regular expression.
	 *
	 * @param set
	 *        the names to examine, {@code null} is allowed
	 * @param pattern
	 *        the regular expression whose matches should be returned
	 * @return the matching names, never {@code null}
	 * @since 1.2
	 */
	public static Set<String> valuesMatching(@Nullable Set<String> set, Pattern pattern) {
		if ( set == null || set.isEmpty() ) {
			return Collections.emptySet();
		}
		Set<String> result = null;
		for ( String n : set ) {
			if ( n == null || n.isEmpty() ) {
				continue;
			}
			if ( pattern.matcher(n).find() ) {
				if ( result == null ) {
					result = new HashSet<>(4, 0.9f);
				}
				result.add(n);
			}
		}
		return (result == null ? Collections.emptySet() : result);
	}

	/**
	 * Get a filtered subset of a "super" collection.
	 *
	 * @param <C>
	 *        the collection type
	 * @param <T>
	 *        the collection item type
	 * @param superSet
	 *        the "super" collection that defines all possible collection item
	 *        values
	 * @param subSet
	 *        the "sub" collection that defines a subset of possible collection
	 *        item values, or a {@code null} or empty set for all values
	 * @param supplier
	 *        a supplier for a new result collection
	 * @return a filtered sub-collection, or {@code superSet} if {@code subSet}
	 *         has no values in common with {@code superSet} or {@code superSet}
	 *         is {@code null} or empty
	 * @since 1.3
	 */
	public static <C extends Collection<T>, T> @Nullable C filteredSubset(@Nullable C superSet,
			@Nullable C subSet, Supplier<C> supplier) {
		if ( subSet != null && !subSet.isEmpty() && superSet != null && !superSet.isEmpty() ) {
			if ( superSet.containsAll(subSet) ) {
				// given sub-set contains only items from super-set, so return it directly
				return subSet;
			}
			C result = supplier.get();
			for ( T item : subSet ) {
				if ( superSet.contains(item) ) {
					result.add(item);
				}
			}
			if ( !result.isEmpty() ) {
				return result;
			}
		}
		return superSet;
	}

	/**
	 * Sort a collection.
	 *
	 * @param <T>
	 *        the collection type
	 * @param collection
	 *        the collection
	 * @param propNames
	 *        an optional list of element property names to sort by; if not
	 *        provided then the elements themselves will be compared
	 * @return the sorted list; if {@code collection} has less than 2 elements,
	 *         it will be returned directly
	 * @see #sort(Collection, boolean, String...)
	 * @since 1.4
	 */
	public static <T> @Nullable Collection<T> sort(Collection<T> collection,
			String @Nullable... propNames) {
		return sort(collection, false, propNames);
	}

	/**
	 * Sort a collection.
	 *
	 * <p>
	 * If {@code propNames} are not provided, the elements of the collection
	 * must implement {@code Comparable} and they will be sorted accordingly.
	 * Otherwise each {@code propNames} value will be extracted from each
	 * element and they must implement {@code Comparable}. The elements can be
	 * either a {@code Map<String, ?>}, in which case {@code propNames} are
	 * treated as map keys, or arbitrary {@code Object} instances, in which case
	 * {@code propNames} are treated as JavaBean "getter" names.
	 * </p>
	 *
	 * <p>
	 * If the collection fails to sort in any way, the {@code collection} value
	 * will be returned as-is.
	 * </p>
	 *
	 * @param <T>
	 *        the collection type
	 * @param collection
	 *        the collection
	 * @param reverse
	 *        {@literal true} to sort in reverse ordering
	 * @param propNames
	 *        an optional list of element property names to sort by; if not
	 *        provided then the elements themselves will be compared
	 * @return the sorted list; if {@code collection} has less than 2 elements,
	 *         it will be returned directly
	 * @since 1.4
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> @Nullable Collection<T> sort(@Nullable Collection<T> collection, boolean reverse,
			String @Nullable... propNames) {
		if ( collection == null || collection.size() < 2 ) {
			return collection;
		}
		List<T> result = new ArrayList<>(collection);
		try {
			Comparator<T> cmp = null;
			if ( propNames == null || propNames.length < 1 ) {
				// natural order
				if ( reverse ) {
					cmp = Collections.reverseOrder();
				}
			} else {
				cmp = (l, r) -> {
					Function<String, Object> lGetter;
					Function<String, Object> rGetter;
					if ( l instanceof Map ) {
						lGetter = ((Map) l)::get;
					} else {
						PropertyAccessor la = PropertyAccessorFactory.forBeanPropertyAccess(l);
						lGetter = la::getPropertyValue;
					}
					if ( r instanceof Map ) {
						rGetter = ((Map) r)::get;
					} else {
						PropertyAccessor ra = PropertyAccessorFactory.forBeanPropertyAccess(r);
						rGetter = ra::getPropertyValue;
					}
					for ( String propName : propNames ) {
						Object lp = lGetter.apply(propName);
						Object rp = rGetter.apply(propName);
						if ( lp != rp ) {
							if ( lp == null ) {
								return -1;
							} else if ( rp == null ) {
								return 1;
							}
							Comparable lc = (Comparable) lp;
							int order = lc.compareTo(rp);
							if ( order != 0 ) {
								return order;
							}
						}
					}
					return 0;
				};
				if ( reverse ) {
					cmp = Collections.reverseOrder(cmp);
				}
			}
			Collections.sort(result, cmp);
		} catch ( Exception e ) {
			// ignore and abort
			return collection;
		}
		return result;
	}

	/**
	 * Transform a set of map values associated with a set of key values.
	 *
	 * <p>
	 * This method will return a new map instance, unless no values need
	 * transforming in which case {@code map} itself will be returned. For any
	 * key in {@code transformKeys} found in {@code map}, the returned map's
	 * value will be transformed by applying the given {@code transformer}
	 * function.
	 * </p>
	 *
	 * @param <K>
	 *        the key type
	 * @param <V>
	 *        the value type
	 * @param map
	 *        the map with values to transform
	 * @param transformKeys
	 *        the set of map keys whose values should be transformed
	 * @param transformer
	 *        the function to transform map values
	 * @return either a new map instance with one or more values transformed, or
	 *         {@code map} when no values need transforming
	 * @since 1.5
	 */
	public static <K, V> @Nullable Map<K, V> transformMap(@Nullable Map<K, V> map,
			@Nullable Set<K> transformKeys, Function<V, V> transformer) {
		assert transformer != null;
		Map<K, V> res = map;
		if ( map != null && transformKeys != null && !map.isEmpty() && !transformKeys.isEmpty() ) {
			for ( K propName : transformKeys ) {
				if ( map.containsKey(propName) ) {
					Map<K, V> maskedMap = new LinkedHashMap<K, V>(map.size());
					for ( Map.Entry<K, V> me : map.entrySet() ) {
						K key = me.getKey();
						V val = me.getValue();
						if ( val != null && transformKeys.contains(key.toString()) ) {
							V encVal = transformer.apply(val);
							maskedMap.put(key, encVal);
						} else {
							maskedMap.put(key, val);
						}
					}
					res = maskedMap;
					break;
				}
			}
		}
		return res;
	}

	/**
	 * Test if two maps have any different numeric properties defined or any
	 * property values available in both are different.
	 *
	 * <p>
	 * This will use {@link StringUtils#numberValue(String)} to parse string
	 * values.
	 * </p>
	 *
	 * @param <K>
	 *        the key type
	 * @param <V>
	 *        the value type
	 * @param m1
	 *        the first map
	 * @param m2
	 *        the second map
	 * @return {@literal true} if the values differ between the two maps
	 * @see #differsNumerically(Map, Map, Function, Function)
	 * @since 1.6
	 */
	public static <K, V> boolean differsNumerically(@Nullable Map<K, V> m1, @Nullable Map<K, V> m2) {
		return differsNumerically(m1, m2, StringUtils::numberValue, null);
	}

	/**
	 * Test if two maps have any different numeric properties defined or any
	 * property values available in both are different.
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
	 * @param <K>
	 *        the key type
	 * @param <V>
	 *        the value type
	 * @param m1
	 *        the first map
	 * @param m2
	 *        the second map
	 * @param parser
	 *        an optional function to map string values to numbers
	 * @param mapper
	 *        an optional function to map number values to other forms, for
	 *        example {@code NumberUtils::bigDecimalForNumber}
	 * @return {@literal true} if the values differ between the two maps
	 * @since 1.6
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> boolean differsNumerically(@Nullable Map<K, V> m1, @Nullable Map<K, V> m2,
			@Nullable Function<String, Number> parser, @Nullable Function<Number, Number> mapper) {
		if ( m1 == null ) {
			m1 = Collections.emptyMap();
		}
		if ( m2 == null ) {
			m2 = Collections.emptyMap();
		}
		if ( m1.isEmpty() && m2.isEmpty() ) {
			return false;
		}
		if ( (m1.size() != m2.size()) || !m1.keySet().equals(m2.keySet()) ) {
			// differ in size, or have different keys
			return true;
		}
		// compare all props as BigDecimal
		for ( K propName : m1.keySet() ) {
			final Object o1 = m1.get(propName);
			final Object o2 = m2.get(propName);

			Number n1 = (o1 instanceof Number n ? n : null);
			if ( n1 == null && parser != null && o1 != null ) {
				n1 = parser.apply(o1.toString());
			}

			Number n2 = (o2 instanceof Number n ? n : null);
			if ( n2 == null && parser != null && o2 != null ) {
				n2 = parser.apply(o2.toString());
			}

			if ( mapper != null ) {
				n1 = mapper.apply(n1);
				n2 = mapper.apply(n2);
			}

			if ( n1 == n2 ) {
				// both numbers null or same instance; check non-numbers values if possible
				if ( o1 != null && o2 != null && !o1.equals(o2) ) {
					// non-number values differ
					return true;
				}
				continue;
			} else if ( n1 == null || n2 == null ) {
				// one null but not the other
				return true;
			}
			// try Comparable first
			if ( n1.getClass().equals(n2.getClass()) && n1 instanceof Comparable c1
					&& n2 instanceof Comparable c2 ) {
				if ( c1.compareTo(c2) != 0 ) {
					return true;
				}
				// Comparable equal
				continue;
			}
			// fall back to equality
			if ( !n1.equals(n2) ) {
				return true;
			}
		}
		return false;
	}

}
