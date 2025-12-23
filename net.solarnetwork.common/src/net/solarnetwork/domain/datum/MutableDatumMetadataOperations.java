/* ==================================================================
 * MutableDatumMetadataOperations.java - 1/03/2022 10:27:47 AM
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

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Extension of {@link DatumSamplesOperations} that adds mutate operations.
 *
 * @author matt
 * @version 1.1
 * @since 2.3
 */
public interface MutableDatumMetadataOperations extends DatumMetadataOperations {

	/**
	 * Remove all property values and tags.
	 */
	void clear();

	/**
	 * Put a value into or remove a value from the {@link #getInfo()} map,
	 * creating the map if it doesn't exist.
	 *
	 * @param key
	 *        the key to put
	 * @param value
	 *        the value to put, or {@literal null} to remove the key
	 */
	void putInfoValue(String key, Object value);

	/**
	 * Set the complete info metadata map.
	 *
	 * @param info
	 *        the info to set, or {@literal null}
	 */
	void setInfo(Map<String, Object> info);

	/**
	 * Put a value into or remove a value from the
	 * {@link #getPropertyInfo(String)} map, creating the map if it doesn't
	 * exist.
	 *
	 * @param property
	 *        the property name
	 * @param key
	 *        the key to put
	 * @param value
	 *        the value to put, or {@literal null} to remove the key
	 */
	void putInfoValue(String property, String key, Object value);

	/**
	 * Set the complete property info map for a given property key.
	 *
	 * @param key
	 *        the property key
	 * @param info
	 *        the info metadata to set, or {@literal null}
	 */
	void setInfo(String key, Map<String, Object> info);

	/**
	 * Set the tags.
	 *
	 * @param tags
	 *        the tags to set
	 */
	void setTags(Set<String> tags);

	/**
	 * Add a tag.
	 *
	 * @param tag
	 *        the tag value to add
	 * @return {@literal true} if the tag was not already present
	 */
	default boolean addTag(String tag) {
		Set<String> tags = getTags();
		if ( tags == null ) {
			tags = new LinkedHashSet<>(2);
			setTags(tags);
		}
		return tags.add(tag);
	}

	/**
	 * Remove one or more tags.
	 *
	 * @param tags
	 *        the tags to remove
	 * @return {@literal true} if any of the given tags were removed
	 */
	default boolean removeTag(String... tags) {
		Set<String> tagSet = getTags();
		if ( tagSet == null || tagSet.isEmpty() ) {
			return false;
		}
		boolean changed = false;
		for ( String tag : tags ) {
			if ( tagSet.remove(tag) ) {
				changed = true;
			}
		}
		return changed;
	}

	/**
	 * Merge the values from another datum metadata instance into this one.
	 *
	 * @param meta
	 *        the metadata to merge into this object
	 * @param replace
	 *        if {@literal true} then replace values in this object with
	 *        equivalent ones in the provided object, otherwise keep the values
	 *        from this object
	 */
	void merge(final DatumMetadataOperations meta, final boolean replace);

	/**
	 * Populate metadata based on a key and value.
	 *
	 * <p>
	 * The {@code key} will be treated as a general metadata key, unless it
	 * starts with a {@code /} character in which case a path is assumed. Values
	 * that can be coerced to number types will be.
	 * </p>
	 *
	 * @param key
	 *        the key to set
	 * @param value
	 *        the value to set
	 * @since 1.1
	 */
	void populate(String key, final Object value);

}
