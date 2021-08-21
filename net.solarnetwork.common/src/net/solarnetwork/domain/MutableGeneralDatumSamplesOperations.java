/* ==================================================================
 * MutableGeneralDatumSamplesOperations.java - 23/03/2018 9:30:44 AM
 * 
 * Copyright 2018 SolarNetwork.net Dev Team
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

package net.solarnetwork.domain;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Extension of {@link GeneralDatumSamplesOperations} that adds mutate
 * operations.
 * 
 * @author matt
 * @version 1.1
 * @since 1.42
 */
public interface MutableGeneralDatumSamplesOperations extends GeneralDatumSamplesOperations {

	/**
	 * Add a value into or remove a value from a sample type collection,
	 * creating the collection if it doesn't already exist.
	 * 
	 * <p>
	 * To add a tag, pass the tag name for both {@code key} and {@code value}.
	 * To remove a tag, pass the tag name for {@code key} and {@literal null}
	 * for {@code value}. To replace a tag, pass the tag to remove for
	 * {@code key} and the tag to add as {@code value}.
	 * </p>
	 * 
	 * @param type
	 *        the type of sample data to get
	 * @param key
	 *        the key to put, or tag to add/remove for
	 *        {@link GeneralDatumSamplesType#Tag}
	 * @param value
	 *        the value to put, or tag to add, or {@literal null} to remove the
	 *        value; this will be cast without checking
	 */
	void putSampleValue(GeneralDatumSamplesType type, String key, Object value);

	/**
	 * Set specific sample data.
	 * 
	 * <p>
	 * In the case of {@link GeneralDatumSamplesType#Tag} the keys of
	 * {@code data} will be used as the tag values to save.
	 * </p>
	 * 
	 * @param type
	 *        the type of sample data to set
	 * @param data
	 *        the data to set; this is cast to the appropriate type without
	 *        checking
	 * @throws IllegalArgumentException
	 *         if {@code type} is not supported
	 */
	void setSampleData(GeneralDatumSamplesType type, Map<String, ?> data);

	/**
	 * Set the tags.
	 * 
	 * @param tags
	 *        the tags to set
	 * @since 1.1
	 */
	void setTags(Set<String> tags);

	/**
	 * Copy all the sample data from another samples instance.
	 * 
	 * @param other
	 *        the instance to copy the samples data from
	 * @since 1.1
	 */
	default void copyFrom(GeneralDatumSamplesOperations other) {
		if ( other == null ) {
			return;
		}
		Map<String, ?> m = other.getSampleData(GeneralDatumSamplesType.Instantaneous);
		if ( m != null ) {
			setSampleData(GeneralDatumSamplesType.Instantaneous, new LinkedHashMap<>(m));
		}
		m = other.getSampleData(GeneralDatumSamplesType.Accumulating);
		if ( m != null ) {
			setSampleData(GeneralDatumSamplesType.Accumulating, new LinkedHashMap<>(m));
		}
		m = other.getSampleData(GeneralDatumSamplesType.Status);
		if ( m != null ) {
			setSampleData(GeneralDatumSamplesType.Status, new LinkedHashMap<>(m));
		}
		if ( other.getTags() != null ) {
			setTags(new LinkedHashSet<>(other.getTags()));
		}
	}

}
