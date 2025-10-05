/* ==================================================================
 * DatumStringFunctions.java - 6/10/2025 7:52:31 am
 *
 * Copyright 2025 SolarNetwork.net Dev Team
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

import java.util.regex.Pattern;
import org.springframework.util.ConcurrentLruCache;

/**
 * API for datum-related string helper functions.
 *
 * @author matt
 * @version 1.0
 * @since 4.7
 */
public interface DatumStringFunctions {

	/**
	 * A {@link Pattern} cache of limited size.
	 *
	 * <p>
	 * The cache maximum number of elements is 100 by default, but can be
	 * overridden with the {@code net.solarentwork.util.datum.regexCacheSize}
	 * system property. For example:
	 * {@code -D net.solarentwork.util.datum.regexCacheSize=20}.
	 * </p>
	 *
	 * <p>
	 * Although this cache is public, it is meant to only be used internally by
	 * this interface.
	 * </p>
	 */
	static ConcurrentLruCache<String, Pattern> PATTERN_CACHE = new ConcurrentLruCache<>(
			Integer.parseInt(System.getProperty("net.solarentwork.util.datum.regexCacheSize", "100")),
			k -> Pattern.compile(k));

	/**
	 * Test if a string matches a regular expression.
	 *
	 * <p>
	 * This method uses the {@link #PATTERN_CACHE} to cache compiled regular
	 * expressions.
	 * </p>
	 *
	 * @param source
	 *        the string to test for a match
	 * @param regex
	 *        the regular expression to match; will match anywhere within
	 *        {@code source}, so use {@code ^} and {@code $} to anchor the match
	 *        to the beginning and end of the source string as needed
	 * @return {@code true} if {@code source} and {@code regex} are both
	 *         non-empty and the regular expression {@code regex} is found
	 *         within {@code source}
	 */
	default boolean regexMatches(String source, String regex) {
		if ( source == null || source.isEmpty() || regex == null || regex.isEmpty() ) {
			return false;
		}
		Pattern p = PATTERN_CACHE.get(regex);
		return p.matcher(source).find();
	}

	/**
	 * Replace all occurrences of a regular expression match.
	 *
	 * <p>
	 * This method uses the {@link #PATTERN_CACHE} to cache compiled regular
	 * expressions.
	 * </p>
	 *
	 * @param source
	 *        the string to find matches on
	 * @param regex
	 *        the regular expression to match; may include capture groups
	 * @param replacement
	 *        the match replacement; may include capture group back references
	 *        it the form {@code $N} where {@code N} is the 1-based capture
	 *        group number; if {@code null} an empty string will be used
	 * @return the {@code source} string after replacing all {@code regex}
	 *         matches with {@code replacement}; if {@code source} or
	 *         {@code regex} are {@code null} then {@code source} will be
	 *         returned unchanged
	 */
	default String regexReplace(String source, String regex, String replacement) {
		if ( source == null || source.isEmpty() || regex == null || regex.isEmpty() ) {
			return source;
		}
		Pattern p = PATTERN_CACHE.get(regex);
		return p.matcher(source).replaceAll(replacement != null ? replacement : "");
	}

}
