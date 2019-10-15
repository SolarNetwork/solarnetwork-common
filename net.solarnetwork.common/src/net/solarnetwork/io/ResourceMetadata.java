/* ==================================================================
 * ResourceMetadata.java - 16/10/2019 6:43:39 am
 * 
 * Copyright 2019 SolarNetwork.net Dev Team
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

package net.solarnetwork.io;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.util.MimeType;

/**
 * Metadata about a resource.
 * 
 * @author matt
 * @version 1.0
 * @since 1.54
 */
public interface ResourceMetadata {

	/** The default content type value. */
	MimeType DEFAULT_CONTENT_TYPE = MimeType.valueOf("application/octet-stream");

	/** The metadata map key for the {@link #getModified()} value. */
	String CONTENT_TYPE_KEY = "Content-Type";

	/** The metadata map key for the {@link #getModified()} value. */
	String MODIFIED_KEY = "Last-Modified";

	/**
	 * Get the modification date, if known.
	 * 
	 * @return the modified date, or {@literal null} if not known
	 */
	Date getModified();

	/**
	 * Get the resource content type.
	 * 
	 * @return the content type, never {@literal null}
	 */
	default MimeType getContentType() {
		return DEFAULT_CONTENT_TYPE;
	}

	/**
	 * Get the metadata as a map of key-value pairs.
	 * 
	 * <p>
	 * Implementing classes are free to return a map with arbitrary keys.
	 * </p>
	 * 
	 * @return a map of the metadata values
	 */
	default Map<String, ?> asMap() {
		Map<String, Object> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		populateMap(map);
		return map;
	}

	/**
	 * Populate a map with all available metadata key-value pairs.
	 * 
	 * <p>
	 * Any existing values in {@code map} will be overwritten by keys from this
	 * instance.
	 * </p>
	 * 
	 * @param map
	 *        the map to populate
	 */
	default void populateMap(Map<String, Object> map) {
		Date d = getModified();
		if ( d != null ) {
			map.put(MODIFIED_KEY, d);
		}
		map.put(CONTENT_TYPE_KEY, getContentType());
	}

	/**
	 * Get the metadata as a map of key-value pairs, excluding any standard
	 * metadata properties so that only custom metadata values are included.
	 * 
	 * @return a map of the custom metadata values
	 */
	default Map<String, ?> asCustomMap() {
		Map<String, Object> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		populateMap(map);
		for ( Iterator<String> itr = map.keySet().iterator(); itr.hasNext(); ) {
			String k = itr.next();
			if ( !isCustomKey(k) ) {
				itr.remove();
			}
		}
		return map;
	}

	/**
	 * Test if a metadata map key is standard or custom.
	 * 
	 * @param key
	 *        the metadata key to test
	 * @return {@literal true} if {@code key} represents a custom metadata key,
	 *         {@literal false} if it represents a standard metadata key
	 */
	default boolean isCustomKey(String key) {
		switch (key) {
			case CONTENT_TYPE_KEY:
			case MODIFIED_KEY:
				return false;

			default:
				return true;
		}
	}

}
