/* ==================================================================
 * TextResourceCache.java - 8/12/2020 4:16:44 pm
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

package net.solarnetwork.service.support;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import net.solarnetwork.util.ByteUtils;
import net.solarnetwork.util.ClassUtils;
import net.solarnetwork.util.StringUtils;

/**
 * A simple text resource cache.
 * 
 * <p>
 * All loaded resources are cached in memory.
 * </p>
 * 
 * @author matt
 * @version 1.0
 * @since 1.67
 */
public class TextResourceCache {

	/** A global, thread-safe, shared instance. */
	public static final TextResourceCache INSTANCE = new TextResourceCache(
			new ConcurrentHashMap<>(8, 0.9f, 1));

	private final Map<String, String> cache;

	/**
	 * Constructor.
	 * 
	 * <p>
	 * This uses a {@link HashMap} for the cache, which is <b>not</b> thread
	 * safe.
	 * </p>
	 */
	public TextResourceCache() {
		this(new HashMap<>(8));
	}

	/**
	 * Constructor.
	 * 
	 * @param cache
	 *        the cache to use; for thread safety use something like a
	 *        {@link java.util.concurrent.ConcurrentMap}
	 */
	public TextResourceCache(Map<String, String> cache) {
		super();
		if ( cache == null ) {
			throw new IllegalArgumentException("The cache argument must not be null.");
		}
		this.cache = cache;
	}

	/**
	 * Get a text resource as a string.
	 * 
	 * @param resourceName
	 *        the resource to load
	 * @param clazz
	 *        the Class to load the resource from
	 * @return the text
	 * @throws RuntimeException
	 *         if the resource cannot be loaded
	 */
	public String getResourceAsString(String resourceName, Class<?> clazz) {
		return getResourceAsString(resourceName, clazz, null, null);
	}

	/**
	 * Get a text resource as a string.
	 * 
	 * @param resourceName
	 *        the resource to load
	 * @param clazz
	 *        the Class to load the resource from
	 * @param templateVariables
	 *        optional template variables to substitute in the loaded resource
	 * @return the text
	 * @throws RuntimeException
	 *         if the resource cannot be loaded
	 */
	public String getResourceAsString(String resourceName, Class<?> clazz,
			Map<String, ?> templateVariables) {
		return getResourceAsString(resourceName, clazz, null, templateVariables);
	}

	/**
	 * Get a text resource as a string.
	 * 
	 * @param resourceName
	 *        the resource to load
	 * @param clazz
	 *        the Class to load the resource from
	 * @param skip
	 *        an optional pattern that will be used to match against lines;
	 *        matches will be left out of the string used to match
	 * @return the text
	 * @throws RuntimeException
	 *         if the resource cannot be loaded
	 */
	public String getResourceAsString(String resourceName, Class<?> clazz, Pattern skip) {
		return getResourceAsString(resourceName, clazz, skip, null);
	}

	/**
	 * Get a text resource as a string.
	 * 
	 * <p>
	 * Note that the final, template resolved text is cached in memory.
	 * </p>
	 * 
	 * @param resourceName
	 *        the resource to load
	 * @param clazz
	 *        the Class to load the resource from
	 * @param skip
	 *        an optional pattern that will be used to match against lines;
	 *        matches will be left out of the string used to match
	 * @param templateVariables
	 *        optional template variables to substitute in the loaded resource
	 * @return the text
	 * @throws RuntimeException
	 *         if the resource cannot be loaded
	 * @see ClassUtils#getResourceAsString(String, Class, Pattern)
	 * @see StringUtils#expandTemplateString(String, Map)
	 */
	public String getResourceAsString(String resourceName, Class<?> clazz, Pattern skip,
			Map<String, ?> templateVariables) {
		String key;
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			digest.update(clazz.getName().getBytes());
			digest.update((byte) '.');
			digest.update(resourceName.getBytes());
			if ( templateVariables != null ) {
				digest.update((byte) '?');
				int count = 0;
				for ( Map.Entry<String, ?> me : templateVariables.entrySet() ) {
					if ( count > 0 ) {
						digest.update((byte) '&');
					}
					digest.update(me.getKey().getBytes());
					digest.update((byte) '=');
					if ( me.getValue() != null ) {
						digest.update(me.getValue().toString().getBytes());
					}
				}
			}
			key = ByteUtils.encodeHexString(digest.digest(), 0, digest.getDigestLength(), false);
		} catch ( NoSuchAlgorithmException e ) {
			throw new RuntimeException("SHA-1 MessageDigest not available.");
		}
		return cache.computeIfAbsent(key, k -> {
			String s = ClassUtils.getResourceAsString(resourceName, clazz, skip);
			if ( templateVariables != null ) {
				s = StringUtils.expandTemplateString(s, templateVariables);
			}
			return s;
		});
	}

}
