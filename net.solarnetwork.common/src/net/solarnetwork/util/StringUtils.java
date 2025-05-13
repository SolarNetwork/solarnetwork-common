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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
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
 * @version 1.16
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
	 * A pattern to match integer number values.
	 *
	 * @since 1.11
	 */
	public static Pattern INTEGER_PATTERN = Pattern.compile("[+-]?\\d+");

	/**
	 * A pattern to match decimal number values.
	 *
	 * @since 1.11
	 */
	public static Pattern DECIMAL_PATTERN = Pattern.compile("[+-]?\\d+(\\.\\d+)?([Ee][+-]?\\d+)?");

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
	 * @see #delimitedStringFromCollection(Collection, String)
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
	 * @return the Set, or {@literal null} if {@code list} is {@literal null} or
	 *         an empty string
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
	 * @return the Set, or {@literal null} if {@code list} is {@literal null} or
	 *         an empty string
	 */
	public static Set<String> delimitedStringToSet(final String list, final String delim) {
		if ( list == null || list.length() < 1 ) {
			return null;
		}
		String[] data = list.trim().split("\\s*" + Pattern.quote(delim) + "\\s*");
		Set<String> s = new LinkedHashSet<>(data.length);
		for ( String d : data ) {
			s.add(d);
		}
		return s;
	}

	/**
	 * Get a List via a comma-delimited string value.
	 *
	 * @param list
	 *        the comma-delimited string
	 * @return the List, or {@literal null} if {@code list} is {@literal null}
	 *         or an empty string
	 * @see #delimitedStringToList(String, String)
	 * @since 1.16
	 */
	public static List<String> commaDelimitedStringToList(final String list) {
		return delimitedStringToList(list, ",");
	}

	/**
	 * Get a string List via a delimited String value.
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
	 * @return the List, or {@literal null} if {@code list} is {@literal null}
	 *         or an empty string
	 * @since 1.16
	 */
	public static List<String> delimitedStringToList(final String list, final String delim) {
		if ( list == null || list.length() < 1 ) {
			return null;
		}
		String[] data = list.trim().split("\\s*" + Pattern.quote(delim) + "\\s*");
		List<String> s = new ArrayList<>(data.length);
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
	 * @return the map, or {@literal null} if {@code mapping} is {@literal null}
	 *         or empty
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
	 * @return the map, or {@literal null} if {@code mapping} is {@literal null}
	 *         or empty
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
	 * {@code expressions} is {@literal null} or empty, the result will be
	 * {@literal null}. Pass {@literal 0} for {@code flags} if no special flags
	 * are desired.
	 *
	 * @param expressions
	 *        the array of expressions to compile into {@link Pattern} objects
	 * @param flags
	 *        the Pattern flags to use, or {@literal 0} for no flags
	 * @return the compiled regular expressions, in the same order as
	 *         {@code expressions}, or {@literal null} if no expressions
	 *         supplied
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
	 * {@code patterns} is {@literal null} or empty, the result will be
	 * {@literal null}.
	 *
	 * @param patterns
	 *        the array of Pattern objects to convert to strings (may be
	 *        {@literal null})
	 * @return the string expressions, in the same order as {@code patterns}, or
	 *         {@literal null} if no patterns supplied
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
	 * {@literal null} is returned.
	 *
	 * @param patterns
	 *        the patterns to test (may be {@literal null})
	 * @param text
	 *        the string to test (may be {@literal null})
	 * @return a {@link Matcher} that matches {@code text} or {@literal null} if
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
	 * @param <K>
	 *        the key type
	 * @param <V>
	 *        the value type
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

	/**
	 * A pattern that matches any character not allowed in
	 * {@link #simpleIdValue(String)}.
	 *
	 * @since 1.8
	 */
	public static final Pattern NOT_SIMPLE_ID_CHARACTER_PATTERN = Pattern.compile("[^a-zA-Z0-9_]+");

	/**
	 * A pattern that matches any {@literal _} at the start or end of a string.
	 *
	 * @since 1.8
	 */
	public static final Pattern UNDERSCORE_PREFIX_OR_SUFFIX = Pattern.compile("(^_+|_+$)");

	/**
	 * Generate a "simple" ID out of a string.
	 *
	 * <p>
	 * A simple ID is created by taking {@code text} and:
	 * </p>
	 *
	 * <ol>
	 * <li>leading and trailing whitespace is removed</li>
	 * <li>change to lower case</li>
	 * <li>replace any runs of characters other than {@literal a-zA-Z0-9_} with
	 * a {@literal _}</li>
	 * <li>
	 * </ol>
	 *
	 * @param text
	 *        the text to derive the simple ID from
	 * @return the simple ID, or {@literal null} if {@code text} is
	 *         {@literal null}
	 * @since 1.8
	 */
	public static String simpleIdValue(String text) {
		return simpleIdValue(text, false);
	}

	/**
	 * Generate a "simple" ID out of a string.
	 *
	 * <p>
	 * A simple ID is created by taking {@code text} and:
	 * </p>
	 *
	 * <ol>
	 * <li>leading and trailing whitespace is removed</li>
	 * <li>change to lower case (if {@code lowerCase} is {@literal true}</li>
	 * <li>replace any runs of characters other than {@literal a-zA-Z0-9_} with
	 * a {@literal _}</li>
	 * <li>
	 * </ol>
	 *
	 * @param text
	 *        the text to derive the simple ID from
	 * @param preserveRateCase
	 *        {@literal true} to lower-case the text
	 * @return the simple ID, or {@literal null} if {@code text} is
	 *         {@literal null}
	 * @since 1.14
	 */
	public static String simpleIdValue(String text, boolean preserveRateCase) {
		if ( text == null || text.isEmpty() ) {
			return text;
		}
		text = text.trim();
		if ( !preserveRateCase ) {
			text = text.toLowerCase();
		}
		String s = NOT_SIMPLE_ID_CHARACTER_PATTERN.matcher(text).replaceAll("_");
		if ( s.charAt(0) == '_' || s.charAt(s.length() - 1) == '_' ) {
			s = UNDERSCORE_PREFIX_OR_SUFFIX.matcher(s).replaceAll("");
		}
		return s;
	}

	/**
	 * Calcualte the UTF-8 byte length of a given string.
	 *
	 * <p>
	 * This is faster than converting the string to a byte array in the UTF-8
	 * encoding.
	 * </p>
	 *
	 * @param text
	 *        the text to calcualte the UTF-8 length of
	 * @return the length
	 * @since 1.10
	 */
	public static int utf8length(CharSequence text) {
		return text.length()
				+ text.codePoints().filter(cp -> cp > 0x7f).map(cp -> cp <= 0x7ff ? 1 : 2).sum();
	}

	/**
	 * Parse a number value if possible.
	 *
	 * <p>
	 * This method will return either a {@link BigInteger} or {@link BigDecimal}
	 * value.
	 * </p>
	 *
	 * @param text
	 *        the string to parse
	 * @return a number instance if {@code text} can be parsed as a number, or
	 *         {@literal null} otherwise
	 * @since 1.11
	 */
	public static Number numberValue(String text) {
		if ( text == null ) {
			return null;
		}
		try {
			if ( INTEGER_PATTERN.matcher(text).matches() ) {
				return new BigInteger(text);
			} else if ( DECIMAL_PATTERN.matcher(text).matches() ) {
				return new BigDecimal(text);
			}
		} catch ( NumberFormatException e ) {
			// don't expect to get here, but just to be sure we ignore this
		}
		return null;
	}

	/**
	 * Test if a string matches a pattern, returning the text along with any
	 * capture groups as an array if there is a match.
	 *
	 * <p>
	 * Note that {@link Matcher#find()} is used, so the pattern matches anywhere
	 * in {@code text} by default.
	 * </p>
	 *
	 * @param pattern
	 *        the pattern
	 * @param text
	 *        the string to test against {@code pattern}
	 * @return if the string does not match or either argument is
	 *         {@literal null}, {@literal null}; otherwise an array whose first
	 *         element is {@code text} and any additional elements are pattern
	 *         capture values
	 * @since 1.11
	 */
	public static String[] match(Pattern pattern, String text) {
		if ( pattern == null || text == null ) {
			return null;
		}
		Matcher m = pattern.matcher(text);
		if ( m.find() ) {
			int groupCount = m.groupCount();
			String[] result = new String[1 + groupCount];
			result[0] = text;
			for ( int i = 1; i <= groupCount; i++ ) {
				result[i] = m.group(i);
			}
			return result;
		}
		return null;
	}

	private static final byte TokenNone = 0;
	private static final byte TokenOther = 1;
	private static final byte TokenDigits = 2;
	private static final byte TokenLetters = 3;

	/**
	 * Compare two strings using "natural" sort ordering.
	 *
	 * <p>
	 * By "natural" we mean that numbers within the string are compared
	 * numerically. See
	 * <a href="https://en.wikipedia.org/wiki/Natural_sort_order">Natural sort
	 * order</a> for more information.
	 * </p>
	 *
	 * <p>
	 * Adapted from Tomáš Pažourek's
	 * <a href="https://github.com/tompazourek/NaturalSort.Extension">C#
	 * implementation</a>. The comparison of numbers that are padded reverses
	 * that implementation, however, so that less-padded values sort before
	 * more-padded values. For example {@literal 3} sorts before {@literal 003}.
	 * </p>
	 *
	 * @param a
	 *        the first string to compare
	 * @param b
	 *        the second string to compare
	 * @param ignoreCase
	 *        {@literal true} if case should be ignored
	 * @return a negative number, {@literal 0}, or a positive number if
	 *         {@code a} sorts before, equal to, or after {@code b}
	 */
	public static int naturalSortCompare(final String a, final String b, final boolean ignoreCase) {
		if ( a == b ) {
			return 0;
		} else if ( a == null ) {
			return -1;
		} else if ( b == null ) {
			return 1;
		}

		final int strLength1 = a.length();
		final int strLength2 = b.length();

		int startIndex1 = 0;
		int startIndex2 = 0;

		while ( true ) {
			// get next token from string 1
			int endIndex1 = startIndex1;
			byte token1 = TokenNone;
			while ( endIndex1 < strLength1 ) {
				byte charToken = tokenForCharacter(a.charAt(endIndex1));
				if ( token1 == TokenNone ) {
					token1 = charToken;
				} else if ( token1 != charToken ) {
					break;
				}

				endIndex1++;
			}

			// get next token from string 2
			int endIndex2 = startIndex2;
			byte token2 = TokenNone;
			while ( endIndex2 < strLength2 ) {
				byte charToken = tokenForCharacter(b.charAt(endIndex2));
				if ( token2 == TokenNone ) {
					token2 = charToken;
				} else if ( token2 != charToken ) {
					break;
				}

				endIndex2++;
			}

			// if the token kinds are different, compare just the token kind
			int tokenCompare = Byte.compare(token1, token2);
			if ( tokenCompare != 0 ) {
				return tokenCompare;
			}

			// now we know that both tokens are the same kind

			// didn't find any more tokens, return that they're equal
			if ( token1 == TokenNone ) {
				return 0;
			}

			int rangeLength1 = endIndex1 - startIndex1;
			int rangeLength2 = endIndex2 - startIndex2;

			if ( token1 == TokenDigits ) {
				// compare both tokens as numbers
				int maxLength = Math.max(rangeLength1, rangeLength2);

				// both spans will get padded by zeroes on the left to be the same length
				final char paddingChar = '0';
				int paddingLength1 = maxLength - rangeLength1;
				int paddingLength2 = maxLength - rangeLength2;

				for ( int i = 0; i < maxLength; i++ ) {
					char digit1 = i < paddingLength1 ? paddingChar
							: a.charAt(startIndex1 + i - paddingLength1);
					char digit2 = i < paddingLength2 ? paddingChar
							: b.charAt(startIndex2 + i - paddingLength2);

					int digitCompare = Character.compare(digit1, digit2);
					if ( digitCompare != 0 ) {
						return digitCompare;
					}
				}

				// if the numbers are equal, we compare how much we padded the strings, less before more
				int paddingCompare = Integer.compare(paddingLength2, paddingLength1);
				if ( paddingCompare != 0 ) {
					return paddingCompare;
				}
			} else {
				// use string comparison
				int minLength = Math.min(rangeLength1, rangeLength2);
				String s1 = a.substring(startIndex1, startIndex1 + minLength);
				String s2 = b.substring(startIndex2, startIndex2 + minLength);
				int stringCompare = ignoreCase ? s1.compareToIgnoreCase(s2) : s1.compareTo(s2);
				if ( stringCompare == 0 ) {
					stringCompare = rangeLength1 - rangeLength2;
				}

				if ( stringCompare != 0 ) {
					return stringCompare;
				}
			}

			startIndex1 = endIndex1;
			startIndex2 = endIndex2;
		}
	}

	private static byte tokenForCharacter(char c) {
		return c >= 'a'
				? c <= 'z' ? TokenLetters
						: c < 128 ? TokenOther : Character.isLetter(c) ? TokenLetters : TokenOther
				: c >= 'A' ? c <= 'Z' ? TokenLetters : TokenOther
						: c >= '0' && c <= '9' ? TokenDigits : TokenOther;
	}

	/**
	 * Parse a delimited integer range list into an {@link IntRangeSet}.
	 *
	 * <p>
	 * The {@code value} format is a comma-delimited list of integer or integer
	 * ranges. Integer ranges may be specified by using a dash delimiter. For
	 * example {@literal 4-6,10} would result in a set including 4, 5, 6, and
	 * 10.
	 * </p>
	 *
	 * @param value
	 *        a delimited list of integer ranges
	 * @return the numbers as a set, or {@literal null} if {@code value} is
	 *         {@literal null} or empty or has no valid values
	 * @since 1.13
	 */
	public static IntRangeSet commaDelimitedStringToIntRangeSet(String value) {
		if ( value == null || value.trim().isEmpty() ) {
			return null;
		}
		IntRangeSet set = new IntRangeSet();
		String[] ranges = value.trim().split("\\s*,\\s*");
		for ( String range : ranges ) {
			String[] components = range.split("\\s*-\\s*", 2);
			try {
				int a = Integer.parseInt(components[0]);
				int b = (components.length > 1 ? Integer.parseInt(components[1]) : a);
				if ( a > 0 && b > 0 ) {
					set.addRange(a, b);
				}
			} catch ( IllegalArgumentException e ) {
				// ignore and continue
			}
		}
		return (set.isEmpty() ? null : set);
	}

	/**
	 * Generate a comma-delimited integer range list from an
	 * {@link IntRangeSet}.
	 *
	 * @param set
	 *        the set to generate the delimited string value for
	 * @return the delimited string, or {@literal null} if {@code set} is
	 *         {@literal null} or empty
	 * @since 1.13
	 */
	public static String commaDelimitedStringFromIntRangeSet(IntRangeSet set) {
		if ( set == null ) {
			return null;
		}
		StringBuilder buf = new StringBuilder();
		for ( IntRange range : set.ranges() ) {
			if ( range != null ) {
				if ( buf.length() > 0 ) {
					buf.append(',');
				}
				buf.append(range.getMin());
				if ( !range.isSingleton() ) {
					buf.append("-");
					buf.append(range.getMax());
				}
			}
		}
		return (buf.length() > 0 ? buf.toString() : null);
	}

	/**
	 * Resolve a string unless it is {@code null} or empty.
	 *
	 * @param s
	 *        the string to test
	 * @return {@code s} unless it is {@code null} or empty, in which case
	 *         {@code null}
	 * @since 1.15
	 */
	public static String nonEmptyString(String s) {
		return nonEmptyString(s, null);
	}

	/**
	 * Resolve a string unless it is {@code null} or empty.
	 *
	 * @param s
	 *        the string to test
	 * @param defaultValue
	 *        the value to return if {@code s} is {@code null} or empty
	 * @return {@code s} unless it is {@code null} or empty, in which case
	 *         {@code defaultValue}
	 * @since 1.15
	 */
	public static String nonEmptyString(String s, String defaultValue) {
		return (s == null || s.isEmpty() ? defaultValue : s);
	}

}
