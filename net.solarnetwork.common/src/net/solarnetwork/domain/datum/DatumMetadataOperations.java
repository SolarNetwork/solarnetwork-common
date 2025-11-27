/* ==================================================================
 * DatumMetadataOperations.java - 1/03/2022 9:35:13 AM
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
import java.math.BigInteger;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.util.StringUtils;
import net.solarnetwork.domain.Differentiable;
import net.solarnetwork.util.CollectionUtils;

/**
 * API for read-only datum metadata operations.
 *
 * @author matt
 * @version 1.1
 * @since 2.3
 */
public interface DatumMetadataOperations extends Differentiable<DatumMetadataOperations> {

	/**
	 * Get a set of all available info keys.
	 *
	 * @return the set of keys, never {@literal null}
	 */
	default Set<String> getInfoKeys() {
		Map<String, ?> info = getInfo();
		return (info != null ? info.keySet() : Collections.emptySet());
	}

	/**
	 * Get a general information metadata map.
	 *
	 * @return the map of general information, or {@literal null}
	 */
	Map<String, ?> getInfo();

	/**
	 * Get the information metadata for a given key.
	 *
	 * @param key
	 *        the info key to get the associated metadata value for
	 * @return the value, or {@literal null}
	 */
	default Object getInfo(String key) {
		Map<String, ?> info = getInfo();
		return (info != null ? info.get(key) : null);
	}

	/**
	 * Test if a given info key is available.
	 *
	 * @param key
	 *        the info key to look for
	 * @return {@literal true} if info for the given key has been set on this
	 *         instance
	 */
	default boolean hasInfo(String key) {
		Map<String, ?> info = getInfo();
		return (info != null ? info.containsKey(key) : false);
	}

	/**
	 * Get a set of all available property info keys.
	 *
	 * @return the set of property info keys, never {@literal null}
	 */
	Set<String> getPropertyInfoKeys();

	/**
	 * Get the property information metadata map for a given key.
	 *
	 * @param key
	 *        the property key to get the metadata for
	 * @return the property metadata, or {@literal null}
	 */
	Map<String, ?> getPropertyInfo(String key);

	/**
	 * Test if a given property info key is available.
	 *
	 * @param key
	 *        the property info key to look for
	 * @return {@literal true} if property info for the given key has been set
	 *         on this instance
	 */
	default boolean hasPropertyInfo(String key) {
		Map<String, ?> info = getPropertyInfo(key);
		return (info != null);
	}

	/**
	 * Test if a given property info key is available.
	 *
	 * @param property
	 *        the property name
	 * @param key
	 *        the key of the value to get
	 * @return {@literal true} if info for the given property key has been set
	 *         on this instance
	 */
	default boolean hasInfo(String property, String key) {
		Map<String, ?> info = getPropertyInfo(property);
		return (info != null ? info.containsKey(key) : false);
	}

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

	/**
	 * Test if this metadata instance has no properties set.
	 *
	 * @return {@literal true} if there are no properties configured
	 */
	default boolean isEmpty() {
		Set<String> keys = getInfoKeys();
		if ( keys != null && !keys.isEmpty() ) {
			return false;
		}
		keys = getPropertyInfoKeys();
		if ( keys != null ) {
			for ( String key : keys ) {
				Map<String, ?> propInfo = getPropertyInfo(key);
				if ( propInfo != null && !propInfo.isEmpty() ) {
					return false;
				}
			}
		}
		keys = getTags();
		return (keys == null || keys.isEmpty());
	}

	@Override
	default boolean differsFrom(DatumMetadataOperations other) {
		if ( other == null ) {
			return true;
		} else if ( this == other ) {
			return false;
		}

		Map<String, ?> i1 = getInfo();
		Map<String, ?> i2 = other.getInfo();
		if ( i1 == null ) {
			if ( i2 != null ) {
				return true;
			}
		} else if ( !i1.equals(i2) ) {
			return true;
		}

		Set<String> keys = getPropertyInfoKeys();
		if ( keys != null && !keys.isEmpty() ) {
			for ( String key : keys ) {
				i1 = getPropertyInfo(key);
				i2 = other.getPropertyInfo(key);
				if ( i1 == null ) {
					if ( i2 != null ) {
						return true;
					}
				} else if ( !i1.equals(i2) ) {
					return true;
				}
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

	/**
	 * Test if metadata at a given path is available.
	 *
	 * @param path
	 *        the path of the metadata object to get
	 * @return {@literal true} if metadata for the given path is available on
	 *         this instance
	 */
	default boolean hasMetadataAtPath(String path) {
		return (metadataAtPath(path) != null);
	}

	/**
	 * Get a metadata value at a given path.
	 *
	 * @param path
	 *        the path of the metadata object to get
	 * @return the metadata value, or {@literal null} if none exists at the
	 *         given path
	 * @see #metadataAtPath(String, Class)
	 */
	Object metadataAtPath(String path);

	/**
	 * Get a metadata value of a given type at a given path.
	 *
	 * <p>
	 * The {@code path} syntax is that of URL paths, using a {@literal /}
	 * delimiter between nested metadata objects. The top-level path component
	 * must be one of {@literal /m} for {@link #getInfo()} data, {@literal /pm}
	 * for {@link #getPropertyInfo(String)} data, or {@literal /t} for
	 * {@link #getTags()} data.
	 * </p>
	 *
	 * <p>
	 * For example, the path {@literal /m/foo} would return the value associated
	 * with the "foo" key in the {@code Map} returned from {@link #getInfo()}.
	 * The path {@literal /pm/foo/bar} would return the "bar" key in the
	 * {@code Map} associated with the "foo" key in the {@code Map} returned
	 * from {@link #getPropertyInfo(String)}.
	 * </p>
	 *
	 * <p>
	 * For tags, using the {@literal /t} path will return the complete
	 * {@code Set} of tags returned by {@link #getTags()}. If the path has
	 * another component, then the next component value will be returned if a
	 * tag matching that component value exists. For example the path
	 * {@literal /t/foo} would return {@literal foo} if {@link #getTags()}
	 * contains {@literal foo}, otherwise {@literal null}.
	 * </p>
	 *
	 * @param <T>
	 *        the expected return type
	 * @param path
	 *        the path of the metadata object to get
	 * @param clazz
	 *        the expected class of the return type
	 * @return the metadata, or {@literal null} if none exists at the given path
	 *         or is not of type {@code T}
	 */
	<T> T metadataAtPath(String path, Class<T> clazz);

	/**
	 * Get a Number value from the {@link #getInfo()} map, or {@literal null} if
	 * not available.
	 *
	 * @param key
	 *        the key of the value to get
	 * @return the value as a Short, or {@literal null} if not available
	 */
	default Number getInfoNumber(String key) {
		Map<String, ?> info = getInfo();
		Object v = (info != null ? info.get(key) : null);
		if ( v == null ) {
			return null;
		} else if ( v instanceof Number ) {
			return (Number) v;
		}
		try {
			return new BigDecimal(v.toString());
		} catch ( NumberFormatException e ) {
			return null;
		}
	}

	/**
	 * Get a Short value from the {@link #getInfo()} map, or {@literal null} if
	 * not available.
	 *
	 * @param key
	 *        the key of the value to get
	 * @return the value as a Short, or {@literal null} if not available
	 */
	default Short getInfoShort(String key) {
		return CollectionUtils.getMapShort(key, getInfo());
	}

	/**
	 * Get an Integer value from the {@link #getInfo()} map, or {@literal null}
	 * if not available.
	 *
	 * @param key
	 *        the key of the value to get
	 * @return the value as an Integer, or {@literal null} if not available
	 */
	default Integer getInfoInteger(String key) {
		return CollectionUtils.getMapInteger(key, getInfo());
	}

	/**
	 * Get a Long value from the {@link #getInfo()} map, or {@literal null} if
	 * not available.
	 *
	 * @param key
	 *        the key of the value to get
	 * @return the value as an Long, or {@literal null} if not available
	 */
	default Long getInfoLong(String key) {
		return CollectionUtils.getMapLong(key, getInfo());
	}

	/**
	 * Get a Float value from the {@link #getInfo()} map, or {@literal null} if
	 * not available.
	 *
	 * @param key
	 *        the key of the value to get
	 * @return the value as an Float, or {@literal null} if not available
	 */
	default Float getInfoFloat(String key) {
		return CollectionUtils.getMapFloat(key, getInfo());
	}

	/**
	 * Get a Double value from the {@link #getInfo()} map, or {@literal null} if
	 * not available.
	 *
	 * @param key
	 *        the key of the value to get
	 * @return the value as an Double, or {@literal null} if not available
	 */
	default Double getInfoDouble(String key) {
		return CollectionUtils.getMapDouble(key, getInfo());
	}

	/**
	 * Get a BigDecimal value from the {@link #getInfo()} map, or
	 * {@literal null} if not available.
	 *
	 * @param key
	 *        the key of the value to get
	 * @return the value as an BigDecimal, or {@literal null} if not available
	 */
	default BigDecimal getInfoBigDecimal(String key) {
		return CollectionUtils.getMapBigDecimal(key, getInfo());
	}

	/**
	 * Get a BigInteger value from the {@link #getInfo()} map, or
	 * {@literal null} if not available.
	 *
	 * @param key
	 *        the key of the value to get
	 * @return the value as an BigInteger, or {@literal null} if not available
	 */
	default BigInteger getInfoBigInteger(String key) {
		return CollectionUtils.getMapBigInteger(key, getInfo());
	}

	/**
	 * Get a String value from the {@link #getInfo()} map, or {@literal null} if
	 * not available.
	 *
	 * @param key
	 *        the key of the value to get
	 * @return the value as a String, or {@literal null} if not available
	 */
	default String getInfoString(String key) {
		return CollectionUtils.getMapString(key, getInfo());
	}

	/**
	 * Get a Number value from the {@link #getPropertyInfo(String)} map, or
	 * {@literal null} if not available.
	 *
	 * @param property
	 *        the property name
	 * @param key
	 *        the key of the value to get
	 * @return the value as a Number, or {@literal null} if not available
	 */
	default Number getInfoNumber(String property, String key) {
		Map<String, ?> info = getPropertyInfo(property);
		Object v = (info != null ? info.get(key) : null);
		if ( v == null ) {
			return null;
		} else if ( v instanceof Number ) {
			return (Number) v;
		}
		try {
			return new BigDecimal(v.toString());
		} catch ( NumberFormatException e ) {
			return null;
		}
	}

	/**
	 * Get a Short value from the {@link #getPropertyInfo(String)} map, or
	 * {@literal null} if not available.
	 *
	 * @param property
	 *        the property name
	 * @param key
	 *        the key of the value to get
	 * @return the value as a Short, or {@literal null} if not available
	 */
	default Short getInfoShort(String property, String key) {
		return CollectionUtils.getMapShort(key, getPropertyInfo(property));
	}

	/**
	 * Get an Integer value from the {@link #getPropertyInfo(String)} map, or
	 * {@literal null} if not available.
	 *
	 * @param property
	 *        the property name
	 * @param key
	 *        the key of the value to get
	 * @return the value as an Integer, or {@literal null} if not available
	 */
	default Integer getInfoInteger(String property, String key) {
		return CollectionUtils.getMapInteger(key, getPropertyInfo(property));
	}

	/**
	 * Get a Long value from the {@link #getPropertyInfo(String)} map, or
	 * {@literal null} if not available.
	 *
	 * @param property
	 *        the property name
	 * @param key
	 *        the key of the value to get
	 * @return the value as an Long, or {@literal null} if not available
	 */
	default Long getInfoLong(String property, String key) {
		return CollectionUtils.getMapLong(key, getPropertyInfo(property));
	}

	/**
	 * Get a Float value from the {@link #getPropertyInfo(String)} map, or
	 * {@literal null} if not available.
	 *
	 * @param property
	 *        the property name
	 * @param key
	 *        the key of the value to get
	 * @return the value as an Float, or {@literal null} if not available
	 */
	default Float getInfoFloat(String property, String key) {
		return CollectionUtils.getMapFloat(key, getPropertyInfo(property));
	}

	/**
	 * Get a Double value from the {@link #getPropertyInfo(String)} map, or
	 * {@literal null} if not available.
	 *
	 * @param property
	 *        the property name
	 * @param key
	 *        the key of the value to get
	 * @return the value as an Double, or {@literal null} if not available
	 */
	default Double getInfoDouble(String property, String key) {
		return CollectionUtils.getMapDouble(key, getPropertyInfo(property));
	}

	/**
	 * Get a BigDecimal value from the {@link #getPropertyInfo(String)} map, or
	 * {@literal null} if not available.
	 *
	 * @param property
	 *        the property name
	 * @param key
	 *        the key of the value to get
	 * @return the value as an BigDecimal, or {@literal null} if not available
	 */
	default BigDecimal getInfoBigDecimal(String property, String key) {
		return CollectionUtils.getMapBigDecimal(key, getPropertyInfo(property));
	}

	/**
	 * Get a BigInteger value from the {@link #getPropertyInfo(String)} map, or
	 * {@literal null} if not available.
	 *
	 * @param property
	 *        the property name
	 * @param key
	 *        the key of the value to get
	 * @return the value as an BigInteger, or {@literal null} if not available
	 */
	default BigInteger getInfoBigInteger(String property, String key) {
		return CollectionUtils.getMapBigInteger(key, getPropertyInfo(property));
	}

	/**
	 * Get a String value from the {@link #getPropertyInfo(String)} map, or
	 * {@literal null} if not available.
	 *
	 * @param property
	 *        the property name
	 * @param key
	 *        the key of the value to get
	 * @return the value as a String, or {@literal null} if not available
	 */
	default String getInfoString(String property, String key) {
		return CollectionUtils.getMapString(key, getPropertyInfo(property));
	}

	/**
	 * Resolve a locale from a metadata path using the {@code ENGLISH} locale.
	 *
	 * @param path
	 *        the path to resolve the locale for
	 * @return a resolved locale, never {@code null}; defaults to English if no
	 *         other locale can be resolved (that includes a non-empty country
	 *         or language)
	 * @since 1.1
	 * @see #resolveLocale(String, Locale)
	 */
	default Locale resolveLocale(final String path) {
		return resolveLocale(path, Locale.ENGLISH);
	}

	/**
	 * Resolve a locale from a metadata path.
	 *
	 * <p>
	 * This method performs a depth-first search for a locale. At each path
	 * level, it will look first for a sibling property named the same as the
	 * current path component with {@code -locale} added. If that does not
	 * resolve a valid locale, then a sibling property named simply
	 * {@code locale} will be tried. If both of these paths fail to resolve a
	 * locale, then the last path component is removed, and the search begins
	 * again at this next-higher path.
	 * </p>
	 *
	 * <p>
	 * A last-ditch attempt is also tried for the {@code /m/locale} path, if no
	 * locale could be resolved otherwise.
	 * </p>
	 *
	 * <p>
	 * If no locale can be found after all these attempts, then {@code ENGLISH}
	 * will be returned.
	 * </p>
	 *
	 * <p>
	 * For example, imagine metadata that looks like this:
	 * </p>
	 *
	 * <pre>{@code {
	 *   "m": {
	 *     "a": 1,
	 *     "a-locale": "fr",
	 *     "locale": "en-US",
	 *     "z": 99
	 *   },
	 *   "pm": {
	 *     "level1": {
	 *       "locale": "en-CA",
	 *       "level2a": {
	 *         "b": 2
	 *         "b-locale": "en-GB"
	 *         "c": 3
	 *       },
	 *       "level2b": {
	 *         "d": 4
	 *       },
	 *       "level2b-locale": "de-DE"
	 *     }
	 *   }
	 * }}</pre>
	 *
	 * <p>
	 * The following locales would be resolved:
	 * </p>
	 *
	 * <table>
	 * <caption> Locale resolution examples </caption> <thead>
	 * <tr>
	 * <th>Path</th>
	 * <th>Resolved Locale</th>
	 * <th>Description</th>
	 * </tr>
	 * </thead> <tbody>
	 * <tr>
	 * <td>/pm/level1/level2a/b</td>
	 * <td>en-GB</td>
	 * <td>direct sibling</td>
	 * </tr>
	 * <tr>
	 * <td>/pm/level1/level2a/c</td>
	 * <td>en-CA</td>
	 * <td>parent default</td>
	 * </tr>
	 * <tr>
	 * <td>/pm/level1/level2b/d</td>
	 * <td>de-DE</td>
	 * <td>parent explicit</td>
	 * </tr>
	 * <tr>
	 * <td>/pm/level1/level2a</td>
	 * <td>en-CA</td>
	 * <td>sibling default</td>
	 * </tr>
	 * <tr>
	 * <td>/pm/level1</td>
	 * <td>en-US</td>
	 * <td>global default</td>
	 * </tr>
	 * <tr>
	 * <td>/m/a</td>
	 * <td>fr</td>
	 * <td>sibling explicit</td>
	 * </tr>
	 * <tr>
	 * <td>/m/z</td>
	 * <td>en-US</td>
	 * <td>sibling default</td>
	 * </tr>
	 * </tbody>
	 * </table>
	 *
	 * @param path
	 *        the path to resolve the locale for
	 * @param fallback
	 *        the value to return if no locale can be resolved from the metadata
	 * @return a resolved locale; defaults to {@code fallback} if no other
	 *         locale can be resolved (that includes a non-empty country or
	 *         language)
	 * @since 1.1
	 */
	default Locale resolveLocale(final String path, final Locale fallback) {
		if ( isEmpty() || path == null || path.isEmpty() ) {
			return fallback;
		}
		Locale l = null;
		String[] components = StringUtils.delimitedListToStringArray(path, "/");
		for ( int idx = components.length; idx > 0; idx-- ) {
			String[] cmp = new String[idx];
			System.arraycopy(components, 0, cmp, 0, cmp.length);
			cmp[idx - 1] = cmp[idx - 1] + "-locale";
			String localePath = StringUtils.arrayToDelimitedString(cmp, "/");
			String localeValue = metadataAtPath(localePath, String.class);
			l = parseLocale(localeValue);
			if ( l != null ) {
				return l;
			}
			cmp[idx - 1] = "locale";
			localePath = StringUtils.arrayToDelimitedString(cmp, "/");
			localeValue = metadataAtPath(localePath, String.class);
			l = parseLocale(localeValue);
			if ( l != null ) {
				return l;
			}
		}
		l = parseLocale(getInfoString("locale"));
		return (l != null ? l : fallback);
	}

	private static Locale parseLocale(final String localeValue) {
		if ( localeValue != null && !localeValue.isEmpty() ) {
			try {
				Locale l = Locale.forLanguageTag(localeValue);
				if ( !l.getCountry().isEmpty() || !l.getLanguage().isEmpty() ) {
					return l;
				}
			} catch ( Exception e ) {
				// ignore and continue
			}
		}
		return null;
	}

}
