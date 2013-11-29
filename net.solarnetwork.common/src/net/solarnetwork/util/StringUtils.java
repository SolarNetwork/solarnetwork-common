/* ==================================================================
 * StringUtils.java - Nov 1, 2012 1:55:45 PM
 * 
 * Copyright 2007-2012 SolarNetwork.net Dev Team
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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Common string helper utilities.
 * 
 * @author matt
 * @version 1.2
 */
public final class StringUtils {

	private StringUtils() {
		// don't construct me
	}

	/**
	 * Get a comma-delimited string from a collection of objects.
	 * 
	 * @param set
	 *        the set
	 * @return the comma-delimited string
	 * @see #delimitedStringFromCollection(Set, String)
	 */
	public static String commaDelimitedStringFromCollection(final Collection<?> set) {
		return delimitedStringFromCollection(set, ",");
	}

	/**
	 * Get a delimited string from a collection of objects.
	 * 
	 * <p>
	 * This will call the {@link Object#toString()} method on each object in the
	 * set, using the natural iteration ordering of the set.No attempt to escape
	 * delimiters within the set's values is done.
	 * </p>
	 * 
	 * @param set
	 *        the set
	 * @param delim
	 *        the delimiter
	 * @return the delimited string
	 */
	public static String delimitedStringFromCollection(final Collection<?> set, String delim) {
		if ( set == null ) {
			return null;
		}
		if ( delim == null ) {
			delim = "";
		}
		StringBuilder buf = new StringBuilder();
		for ( Object o : set ) {
			if ( buf.length() > 0 ) {
				buf.append(delim);
			}
			if ( o != null ) {
				buf.append(o.toString());
			}
		}
		return buf.toString();
	}

	/**
	 * Get a delimited string from a map of objects.
	 * 
	 * <p>
	 * This will call {@link #delimitedStringFromMap(Map, String, String)} using
	 * a {@code =} key value delimieter and a {@code ,} pair delimiter.
	 * </p>
	 * 
	 * @param map
	 *        the map
	 * @return the string
	 */
	public static String delimitedStringFromMap(final Map<?, ?> map) {
		return delimitedStringFromMap(map, "=", ",");
	}

	/**
	 * Get a delimited string from a map of objects.
	 * 
	 * <p>
	 * This will call the {@link Object#toString()} method on each key and value
	 * in the map, using the natural iteration ordering of the map. No attempt
	 * to escape delimiters within the map's values is done.
	 * </p>
	 * 
	 * @param map
	 *        the map
	 * @param keyValueDelim
	 *        the delimited to use between keys and values
	 * @param pairDelim
	 *        the delimiter to use betwen key/value pairs
	 * @return the string
	 */
	public static String delimitedStringFromMap(final Map<?, ?> map, String keyValueDelim,
			String pairDelim) {
		if ( map == null ) {
			return null;
		}
		StringBuilder buf = new StringBuilder();
		for ( Map.Entry<?, ?> me : map.entrySet() ) {
			if ( buf.length() > 0 ) {
				buf.append(pairDelim);
			}
			if ( me.getKey() != null ) {
				buf.append(me.getKey().toString());
			}
			buf.append(keyValueDelim);
			if ( me.getValue() != null ) {
				buf.append(me.getValue().toString());
			}
		}
		return buf.toString();
	}

	/**
	 * Get a Set via a comma-delimited string value.
	 * 
	 * @param list
	 *        the comma-delimited string
	 * @return the Set, or <em>null</em> if {@code list} is <em>null</em> or an
	 *         empty string
	 * @see #delimitedStringToSet(String, String)
	 */
	public static Set<String> commaDelimitedStringToSet(final String list) {
		return delimitedStringToSet(list, ",");
	}

	/**
	 * Get a string Set via a delimited String value.
	 * 
	 * <p>
	 * The format of the {@code list} String should be a delimited list of
	 * values. Whitespace is permitted around the delimiter, and will be
	 * stripped from the values. Whitespace is also trimmed from the start and
	 * end of the input string.
	 * </p>
	 * 
	 * @param list
	 *        the delimited text
	 * @param delim
	 *        the delimiter to split the list with
	 * @return the Set, or <em>null</em> if {@code list} is <em>null</em> or an
	 *         empty string
	 */
	public static Set<String> delimitedStringToSet(final String list, final String delim) {
		if ( list == null || list.length() < 1 ) {
			return null;
		}
		String[] data = list.trim().split("\\s*" + Pattern.quote(delim) + "\\s*");
		Set<String> s = new LinkedHashSet<String>(data.length);
		for ( String d : data ) {
			s.add(d);
		}
		return s;
	}

	/**
	 * Get string Map via a comma-delimited String value.
	 * 
	 * <p>
	 * The format of the {@code mapping} String should be:
	 * </p>
	 * 
	 * <pre>
	 * key=val[,key=val,...]
	 * </pre>
	 * 
	 * @param mapping
	 *        the delimited text
	 * @see #delimitedStringToMap(String, String, String)
	 */
	public static Map<String, String> commaDelimitedStringToMap(final String mapping) {
		return delimitedStringToMap(mapping, ",", "=");
	}

	/**
	 * Get a string Map via a delimited String value.
	 * 
	 * <p>
	 * The format of the {@code mapping} String should be:
	 * </p>
	 * 
	 * <pre>
	 * key=val[,key=val,...]
	 * </pre>
	 * 
	 * <p>
	 * The record and field delimiters are passed as parameters to this method.
	 * Whitespace is permitted around all delimiters, and will be stripped from
	 * the keys and values. Whitespace is also trimmed from the start and end of
	 * the input string.
	 * </p>
	 * 
	 * @param mapping
	 *        the delimited text
	 * @param recordDelim
	 *        the key+value record delimiter
	 * @param fieldDelim
	 *        the key+value delimiter
	 */
	public static Map<String, String> delimitedStringToMap(final String mapping,
			final String recordDelim, final String fieldDelim) {
		if ( mapping == null || mapping.length() < 1 ) {
			return null;
		}
		final String[] pairs = mapping.trim().split("\\s*" + Pattern.quote(recordDelim) + "\\s*");
		final Map<String, String> map = new LinkedHashMap<String, String>();
		final Pattern fieldSplit = Pattern.compile("\\s*" + Pattern.quote(fieldDelim) + "\\s*");
		for ( String pair : pairs ) {
			String[] kv = fieldSplit.split(pair);
			if ( kv == null || kv.length != 2 ) {
				continue;
			}
			map.put(kv[0], kv[1]);
		}
		return map;
	}

}
