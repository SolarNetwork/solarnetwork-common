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

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import net.solarnetwork.domain.KeyValuePair;

/**
 * Common string helper utilities.
 * 
 * @author matt
 * @version 1.7
 */
public final class StringUtils {

	private StringUtils() {
		// don't construct me
	}

	/**
	 * Pattern to capture template variable names of the form
	 * <code>{name}</code>.
	 */
	public static final Pattern NAMES_PATTERN = Pattern.compile("\\{([^/]+?)\\}");

	/**
	 * Replace variables in a string template with corresponding values.
	 * 
	 * <p>
	 * Template variables are encoded like <code>{name:default}</code> where the
	 * {@code :default} part is optional. The {@code name} value is treated as a
	 * key in the provided {@code variables} map, and any corresponding value
	 * found is turned into a string and replaces the template variable in the
	 * resulting string. The optional {@code default} value, if provided, will
	 * be used as the variable value if {@code name} is not found in
	 * {@code variables}.
	 * </p>
	 * 
	 * <p>
	 * Adapted from the {@code org.springframework.web.util.UriComponents}
	 * class, mimicking URI path variable substitutions.
	 * </p>
	 * 
	 * @param source
	 *        the template string to replace variables in
	 * @param variables
	 *        the variables
	 * @return the string with variables replaced, or {@literal null} if
	 *         {@code source} is {@literal null}
	 * @since 1.4
	 */
	public static String expandTemplateString(String source, Map<String, ?> variables) {
		if ( source == null ) {
			return null;
		}
		if ( source.indexOf('{') == -1 ) {
			return source;
		}
		if ( source.indexOf(':') != -1 ) {
			source = sanitizeVariableTemplate(source);
		}
		if ( variables == null ) {
			variables = Collections.emptyMap();
		}
		Matcher matcher = NAMES_PATTERN.matcher(source);
		StringBuffer sb = new StringBuffer();
		while ( matcher.find() ) {
			String match = matcher.group(1);
			Object variableValue = getVariableValue(match, variables);
			String variableValueString = getVariableValueAsString(variableValue);
			String replacement = Matcher.quoteReplacement(variableValueString);
			matcher.appendReplacement(sb, replacement);
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	/**
	 * Remove nested "{}" such as in template variables with regular
	 * expressions.
	 */
	private static String sanitizeVariableTemplate(String source) {
		int level = 0;
		StringBuilder sb = new StringBuilder();
		for ( char c : source.toCharArray() ) {
			if ( c == '{' ) {
				level++;
			}
			if ( c == '}' ) {
				level--;
			}
			if ( level > 1 || (level == 1 && c == '}') ) {
				continue;
			}
			sb.append(c);
		}
		return sb.toString();
	}

	private static Object getVariableValue(String match, Map<String, ?> variables) {
		int colonIdx = match.indexOf(':');
		String name = (colonIdx != -1 ? match.substring(0, colonIdx) : match);
		String fallback = (colonIdx != -1 ? match.substring(colonIdx + 1) : null);
		Object val = variables.get(name);
		if ( val == null ) {
			val = fallback;
		}
		return val;
	}

	private static String getVariableValueAsString(Object variableValue) {
		return (variableValue != null ? variableValue.toString() : "");
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
	 * a {@code =} key value delimiter and a {@code ,} pair delimiter.
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
	 * end of the input string. The list order is preserved in the iteration
	 * order of the returned Set.
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
			String[] kv = fieldSplit.split(pair, 2);
			if ( kv == null || kv.length != 2 ) {
				continue;
			}
			map.put(kv[0], kv[1]);
		}
		return map;
	}

	/**
	 * Create an array of regular expressions from strings. If
	 * {@code expressions} is <em>null</em> or empty, the result will be
	 * <em>null</em>. Pass {@bold 0} for {@code flags} if no special flags are
	 * desired.
	 * 
	 * @param expressions
	 *        the array of expressions to compile into {@link Pattern} objects
	 * @param flags
	 *        the Pattern flags to use, or {@bold 0} for no flags
	 * @return the compiled regular expressions, in the same order as
	 *         {@code expressions}, or <em>null</em> if no expressions supplied
	 * @throws PatternSyntaxException
	 *         If an expression's syntax is invalid
	 */
	public static Pattern[] patterns(final String[] expressions, int flags) {
		Pattern[] result = null;
		if ( expressions != null && expressions.length > 0 ) {
			result = new Pattern[expressions.length];
			for ( int i = 0, len = expressions.length; i < len; i++ ) {
				result[i] = (flags == 0 ? Pattern.compile(expressions[i])
						: Pattern.compile(expressions[i], flags));
			}
		}
		return result;
	}

	/**
	 * Create an array of expression strings from Pattern objects. If
	 * {@code patterns} is <em>null</em> or empty, the result will be
	 * <em>null</em>.
	 * 
	 * @param patterns
	 *        the array of Pattern objects to convert to strings (may be
	 *        <em>null</em>)
	 * @return the string expressions, in the same order as {@code patterns}, or
	 *         <em>null</em> if no patterns supplied
	 */
	public static String[] expressions(final Pattern[] patterns) {
		String[] results = null;
		if ( patterns != null && patterns.length > 0 ) {
			results = new String[patterns.length];
			for ( int i = 0, len = patterns.length; i < len; i++ ) {
				results[i] = patterns[i].pattern();
			}
		}
		return results;
	}

	/**
	 * Test if a string matches any one of a list of patterns. The
	 * {@code patterns} list will be tested one at a time, in array order. The
	 * first result that matches will be returned. If no match is found,
	 * <em>null</em> is returned.
	 * 
	 * @param patterns
	 *        the patterns to test (may be <em>null</em>)
	 * @param text
	 *        the string to test (may be <em>null</em>)
	 * @return a {@link Matcher} that matches {@code text} or <em>null</em> if
	 *         no match was found
	 */
	public static Matcher matches(final Pattern[] patterns, String text) {
		if ( patterns == null || patterns.length < 0 || text == null ) {
			return null;
		}
		for ( Pattern pattern : patterns ) {
			Matcher m = pattern.matcher(text);
			if ( m.matches() ) {
				return m;
			}
		}
		return null;
	}

	/**
	 * Get a boolean value from a String.
	 * 
	 * <p>
	 * This method is more generous than {@link Boolean#parseBoolean(String)}.
	 * The following values are considered {@literal true}, all ignoring case:
	 * </p>
	 * 
	 * <ul>
	 * <li>{@literal 1}</li>
	 * <li>{@literal t}</li>
	 * <li>{@literal true}</li>
	 * <li>{@literal y}</li>
	 * <li>{@literal yes}</li>
	 * </ul>
	 * 
	 * <p>
	 * All other values (or a missing value) is considered {@literal false}.
	 * </p>
	 * 
	 * @param s
	 *        the string to parse as a boolean
	 * @return the parsed boolean result
	 * @since 1.6
	 */
	public static boolean parseBoolean(String s) {
		boolean result = false;
		if ( s != null ) {
			s = s.trim();
			if ( s.length() < 5 ) {
				s = s.toLowerCase();
				if ( s.equals("true") || s.equals("yes") || s.equals("y") || s.equals("t")
						|| s.equals("1") ) {
					result = true;
				}
			}
		}
		return result;
	}

	private static final SecureRandom rng = new SecureRandom();

	/**
	 * Compute a Base64-encoded SHA-256 digest of a string value with a random
	 * salt.
	 * 
	 * @param propertyValue
	 *        the current property value
	 * @return a Base64 encoded SHA-256 digest with a <code>{SSHA-256}</code>
	 *         prefix
	 * @since 1.7
	 */
	public static final String sha256Base64Value(String propertyValue) {
		byte[] salt = new byte[8];
		rng.nextBytes(salt);
		return sha256Base64Value(propertyValue, salt);
	}

	/**
	 * Compute a Base64-encoded SHA-256 digest of a string value with optional
	 * salt.
	 * 
	 * <p>
	 * When salt is provided, the digest is computed from
	 * {@literal propertyValue + salt} and then the returned Base64 value
	 * contains {@literal digest + salt}. The length of the salt can be
	 * determined after decoding the Base64 value, as
	 * {@literal decodedLength - 32}.
	 * </p>
	 * 
	 * @param propertyValue
	 *        the current property value
	 * @param salt
	 *        the optional salt to add
	 * @return a Base64 encoded SHA-256 digest with a <code>{SSHA-256}</code>
	 *         prefix (if salt provided) or <code>{SHA-256}</code> if no salt
	 *         provided
	 * @since 1.7
	 */
	public static final String sha256Base64Value(String propertyValue, byte[] salt) {
		byte[] plain;
		try {
			plain = (propertyValue != null ? propertyValue.getBytes("UTF-8") : new byte[0]);
		} catch ( UnsupportedEncodingException e ) {
			throw new RuntimeException(e);
		}
		if ( salt != null && salt.length > 0 ) {
			byte[] tmp = new byte[plain.length + salt.length];
			System.arraycopy(plain, 0, tmp, 0, plain.length);
			System.arraycopy(salt, 0, tmp, tmp.length - salt.length, salt.length);
			plain = tmp;
		}
		byte[] cipher = DigestUtils.sha256(plain);
		if ( salt != null && salt.length > 0 ) {
			byte[] tmp = new byte[cipher.length + salt.length];
			System.arraycopy(cipher, 0, tmp, 0, cipher.length);
			System.arraycopy(salt, 0, tmp, tmp.length - salt.length, salt.length);
			cipher = tmp;
		}
		try {
			return (salt != null && salt.length > 0 ? "{SSHA-256}" : "{SHA-256}")
					+ new String(Base64.encodeBase64(cipher, false), "US-ASCII");
		} catch ( UnsupportedEncodingException e ) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * A pattern for matching <code>{type-len}digest</code> style digest
	 * strings.
	 * 
	 * @since 1.7
	 */
	public static final Pattern DIGEST_PREFIX_PATTERN = Pattern
			.compile("\\{(\\w+)-?((?<=-)\\d+)?\\}(.*)");

	/**
	 * Decode a digest string in the form <code>{key}value</code> into a
	 * key-value pair composed of digest (key) and salt (value) hex-encoded
	 * values.
	 * 
	 * <p>
	 * The returned pair's {@code value} will be {@literal null} if no salt was
	 * included in the digest. Both values will be returned as hex-encoded
	 * strings.
	 * </p>
	 * 
	 * @param digest
	 *        a Base64-encoded digest string, in the form returned by
	 *        {@link #sha256Base64Value(String)}
	 * @return a key/value pair of the
	 * @since 1.7
	 */
	public static final KeyValuePair decodeBase64DigestComponents(String digest) {
		if ( digest == null ) {
			return null;
		}
		Matcher m = DIGEST_PREFIX_PATTERN.matcher(digest);
		if ( !m.matches() ) {
			return null;
		}
		String type = m.group(1).toUpperCase();
		String len = m.group(2);
		int algLen = 0;
		if ( len != null ) {
			algLen = Integer.parseInt(len);
		}
		byte[] salt = null;
		byte[] data = Base64.decodeBase64(m.group(3));

		int digestByteLen = 0;
		if ( type.endsWith("SHA") ) {
			if ( algLen < 2 ) {
				digestByteLen = 20;
			} else {
				digestByteLen = algLen / 8;
			}
		} else if ( type.endsWith("MD5") ) {
			digestByteLen = 16;
		}

		if ( digestByteLen < data.length ) {
			byte[] tmp = new byte[digestByteLen];
			System.arraycopy(data, 0, tmp, 0, digestByteLen);
			salt = new byte[data.length - digestByteLen];
			System.arraycopy(data, digestByteLen, salt, 0, salt.length);
			data = tmp;
		}

		return new KeyValuePair(Hex.encodeHexString(data),
				salt != null ? Hex.encodeHexString(salt) : null);
	}

	/**
	 * "Mask" a set of map values by replacing them with SHA-256 digest values.
	 * 
	 * <p>
	 * This method will return a new map instance, unless no values need masking
	 * in which case {@code map} itself will be returned. For any key in
	 * {@code maskKeys} found in {@code map}, the returned map's value will be
	 * the SHA-256 digest value computed from the string form of the value
	 * passed to {@link #sha256Base64Value(String)}.
	 * </p>
	 * 
	 * @param map
	 *        the map of values to mask
	 * @param maskKeys
	 *        the set of map keys whose values should be masked
	 * @return either a new map instance with one or more values masked, or
	 *         {@code map} when no values need masking
	 * @see #sha256Base64Value(String)
	 * @since 1.7
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <K, V> Map<K, V> sha256MaskedMap(Map<K, V> map, Set<K> maskKeys) {
		Map<K, V> res = map;
		if ( map != null && maskKeys != null && !map.isEmpty() && !maskKeys.isEmpty() ) {
			for ( K propName : maskKeys ) {
				if ( map.containsKey(propName) ) {
					Map<K, V> maskedMap = new LinkedHashMap<K, V>(map.size());
					for ( Map.Entry<K, V> me : map.entrySet() ) {
						K key = me.getKey();
						V val = me.getValue();
						if ( val != null && maskKeys.contains(key.toString()) ) {
							String maskedVal = StringUtils.sha256Base64Value(val.toString());
							((Map) maskedMap).put(key, maskedVal);
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
}
