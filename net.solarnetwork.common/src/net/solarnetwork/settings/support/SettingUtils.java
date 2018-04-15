/* ==================================================================
 * SettingUtils.java - 16/04/2018 9:32:39 AM
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

package net.solarnetwork.settings.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.solarnetwork.settings.GroupSettingSpecifier;
import net.solarnetwork.settings.SettingSpecifier;
import net.solarnetwork.settings.TextFieldSettingSpecifier;

/**
 * Helper utilities for settings.
 * 
 * @author matt
 * @version 1.0
 * @since 1.43
 */
public final class SettingUtils {

	private SettingUtils() {
		// Do not construct me.
	}

	/**
	 * API to map a list element into a set of {@link SettingSpecifier} objects.
	 * 
	 * @param <T>
	 *        The collection type.
	 */
	public interface KeyedListCallback<T> {

		/**
		 * Map a single list element value into one or more
		 * {@link SettingSpecifier} objects.
		 * 
		 * @param value
		 *        The list element value.
		 * @param index
		 *        The list element index.
		 * @param key
		 *        An indexed key prefix to use for the grouped settings.
		 * @return The settings.
		 */
		public Collection<SettingSpecifier> mapListSettingKey(T value, int index, String key);

	}

	/**
	 * Get a dynamic list {@link GroupSettingSpecifier}.
	 * 
	 * @param collection
	 *        The collection to turn into settings.
	 * @param mapper
	 *        A helper to map individual elements into settings.
	 * @return The resulting {@link GroupSettingSpecifier}.
	 */
	public static <T> BasicGroupSettingSpecifier dynamicListSettingSpecifier(String key,
			Collection<T> collection, KeyedListCallback<T> mapper) {
		List<SettingSpecifier> listStringGroupSettings;
		if ( collection == null ) {
			listStringGroupSettings = Collections.emptyList();
		} else {
			final int len = collection.size();
			listStringGroupSettings = new ArrayList<SettingSpecifier>(len);
			int i = 0;
			for ( T value : collection ) {
				Collection<SettingSpecifier> res = mapper.mapListSettingKey(value, i,
						key + "[" + i + "]");
				i++;
				if ( res != null ) {
					listStringGroupSettings.addAll(res);
				}
			}
		}
		return new BasicGroupSettingSpecifier(key, listStringGroupSettings, true);
	}

	/**
	 * Get a set of setting keys that require secure handling.
	 * 
	 * <p>
	 * This method considers the following settings for secure handling :
	 * </p>
	 * 
	 * <ol>
	 * <li>{@link TextFieldSettingSpecifier#isSecureTextEntry()} that returns
	 * {@literal true}</li>
	 * </ol>
	 * 
	 * <p>
	 * The returned set maintains the same iteration order as {@code settings}.
	 * </p>
	 * 
	 * @param settings
	 *        the settings to check ({@literal null} allowed)
	 * @return the set of secure entry keys, never {@literal null}
	 */
	public static Set<String> secureKeys(List<SettingSpecifier> settings) {
		if ( settings == null || settings.isEmpty() ) {
			return Collections.emptySet();
		}
		Set<String> secureProps = null;
		for ( SettingSpecifier setting : settings ) {
			if ( setting instanceof TextFieldSettingSpecifier ) {
				TextFieldSettingSpecifier text = (TextFieldSettingSpecifier) setting;
				if ( text.isSecureTextEntry() ) {
					String key = text.getKey();
					if ( secureProps == null ) {
						secureProps = new LinkedHashSet<String>(4);
					}
					secureProps.add(key);
				}
			}
		}
		return (secureProps != null ? secureProps : Collections.<String> emptySet());
	}

}
