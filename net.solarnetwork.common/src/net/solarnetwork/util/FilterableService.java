/* ==================================================================
 * FilterableService.java - Mar 25, 2014 11:26:25 AM
 * 
 * Copyright 2007-2014 SolarNetwork.net Dev Team
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

import java.util.Map;

/**
 * API for a service that supports filtering properties, to support narrowing
 * down a possible collection of services to one or more specific services
 * matching the filter.
 * 
 * @author matt
 * @version 1.2
 */
public interface FilterableService {

	/**
	 * Get the current map of property filters, with keys representing property
	 * names and value their desired associated value.
	 * 
	 * @return filters
	 */
	Map<String, ?> getPropertyFilters();

	/**
	 * Set a property filter value.
	 * 
	 * @param key
	 *        the key to add
	 * @param value
	 *        the value
	 */
	void setPropertyFilter(String key, Object value);

	/**
	 * Remove a property filter value.
	 * 
	 * @param key
	 *        the key to remove
	 * @return the removed value, or {@literal null} if no value was available
	 */
	Object removePropertyFilter(String key);

	/**
	 * Get a property filter value.
	 * 
	 * @param <T>
	 *        the expected property value type
	 * @param key
	 *        the property filter key to get the value for
	 * @return the property value, or {@literal null} if not available
	 * @since 1.2
	 */
	@SuppressWarnings("unchecked")
	default <T> T getPropertyValue(String key) {
		Map<String, ?> props = getPropertyFilters();
		return (props != null ? (T) props.get(key) : null);
	}

	/**
	 * Get a filter property value from an optional service that is also
	 * optional.
	 * 
	 * @param <T>
	 *        the filter property type
	 * @param optionalService
	 *        the optional service, or {@literal null}
	 * @param key
	 *        the desired filter property key
	 * @return the filter property value, or {@literal null}
	 * @since 1.1
	 */
	static <T> T filterPropValue(OptionalService<?> optionalService, String key) {
		FilterableService service = (optionalService instanceof FilterableService
				? (FilterableService) optionalService
				: null);
		return filterPropValue(service, key);
	}

	/**
	 * Get a filter property value.
	 * 
	 * @param <T>
	 *        the filter property type
	 * @param service
	 *        the filterable service, or {@literal null}
	 * @param key
	 *        the desired filter property key
	 * @return the filter property value, or {@literal null}
	 * @since 1.1
	 */
	@SuppressWarnings("unchecked")
	static <T> T filterPropValue(FilterableService service, String key) {
		Map<String, ?> props = (service != null ? service.getPropertyFilters() : null);
		return (T) (props != null ? props.get(key) : null);
	}

	/**
	 * Set a filter property value on an optional service that is also optional.
	 * 
	 * @param optionalService
	 *        the optional service, or {@literal null}
	 * @param key
	 *        the desired filter property key
	 * @param value
	 *        the filter property value to set
	 * @since 1.1
	 */
	static void setFilterProp(OptionalService<?> optionalService, String key, Object value) {
		FilterableService service = (optionalService instanceof FilterableService
				? (FilterableService) optionalService
				: null);
		setFilterProp(service, key, value);
	}

	/**
	 * Set a filter property value.
	 * 
	 * @param service
	 *        the filterable service, or {@literal null}
	 * @param key
	 *        the filter property key
	 * @param value
	 *        the filter property value to set
	 * @since 1.1
	 */
	static void setFilterProp(FilterableService service, String key, Object value) {
		if ( service != null ) {
			service.setPropertyFilter(key, value);
		}
	}

}
