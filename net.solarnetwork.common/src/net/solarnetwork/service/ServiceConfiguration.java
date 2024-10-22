/* ==================================================================
 * ServiceConfiguration.java - 17/10/2024 7:50:34 am
 *
 * Copyright 2024 SolarNetwork.net Dev Team
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

package net.solarnetwork.service;

import java.util.Map;
import net.solarnetwork.util.NumberUtils;

/**
 * API for a user-supplied set of configuration to use with some service.
 *
 * @author matt
 * @version 1.0
 * @since 3.24
 */
public interface ServiceConfiguration {

	/**
	 * Get a map of properties to pass to the service in order to perform
	 * actions.
	 *
	 * <p>
	 * It is expected this map would contain user-supplied runtime configuration
	 * such as credentials to use, host name, etc.
	 * </p>
	 *
	 * @return the runtime properties to pass to the service
	 */
	Map<String, ?> getServiceProperties();

	/**
	 * Check if a service property value is present.
	 *
	 * @param key
	 *        the service property key to check has a value
	 * @return {@literal true} if the service property value exists and if a
	 *         string value is not empty
	 */
	default boolean hasServiceProperty(String key) {
		assert key != null;
		Map<String, ?> props = getServiceProperties();
		Object val = (props != null ? props.get(key) : null);
		if ( val == null ) {
			return false;
		}
		if ( val instanceof String && ((String) val).isEmpty() ) {
			return false;
		}
		return true;
	}

	/**
	 * Check if a service property value is present and of a given type.
	 *
	 * @param <T>
	 *        the expected type of the value
	 * @param key
	 *        the service property key to check has a value
	 * @param type
	 *        the type of the value
	 * @return {@literal true} if the service property value exists and can be
	 *         returned as the given type and if a string value is not empty, or
	 *         {@literal null} if not available or cannot be converted to the
	 *         given type
	 */
	default <T> boolean hasServiceProperty(String key, Class<T> type) {
		assert key != null && type != null;
		return serviceProperty(key, type) != null;
	}

	/**
	 * Get a service property value.
	 *
	 * @param <T>
	 *        the expected type of the value
	 * @param key
	 *        the service property key to get the value for
	 * @param type
	 *        the type of the value
	 * @return the service property value, or {@literal null} if not available
	 *         or cannot be converted to the given type or if a string value is
	 *         empty
	 */
	@SuppressWarnings("unchecked")
	default <T> T serviceProperty(String key, Class<T> type) {
		assert key != null && type != null;
		Map<String, ?> props = getServiceProperties();
		Object val = (props != null ? props.get(key) : null);
		if ( val == null ) {
			return null;
		}
		if ( String.class.isAssignableFrom(type) ) {
			String s = val.toString();
			return (T) (s.isEmpty() ? null : s);
		} else if ( type.isAssignableFrom(val.getClass()) ) {
			return (T) val;
		} else if ( Number.class.isAssignableFrom(type) ) {
			try {
				if ( val instanceof Number ) {
					return (T) NumberUtils.convertNumber((Number) val, (Class<? extends Number>) type);
				}
				return (T) NumberUtils.parseNumber(val.toString(), (Class<? extends Number>) type);
			} catch ( IllegalArgumentException e ) {
				// ignore and return null
			}
		}
		return null;
	}

}
