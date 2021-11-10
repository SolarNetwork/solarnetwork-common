/* ==================================================================
 * StaticOptionalService.java - Mar 28, 2013 12:45:36 PM
 * 
 * Copyright 2007-2013 SolarNetwork.net Dev Team
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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Implementation of {@link OptionalService.OptionalFilterableService} using a
 * static service instance.
 * 
 * <p>
 * This can be useful when the {@link OptionalService.OptionalFilterableService}
 * API is required, but the service is known and available statically.
 * </p>
 * 
 * @param <T>
 *        the service type
 * @author matt
 * @version 1.2
 */
public class StaticOptionalService<T> implements OptionalService.OptionalFilterableService<T> {

	private final T service;
	private final Map<String, Object> propertyFilters;

	public StaticOptionalService(T service) {
		super();
		this.service = service;
		propertyFilters = new LinkedHashMap<String, Object>(4);
	}

	@Override
	public T service() {
		return service;
	}

	@Override
	public Map<String, ?> getPropertyFilters() {
		return propertyFilters;
	}

	@Override
	public void setPropertyFilter(String key, Object value) {
		propertyFilters.put(key, value);
	}

	@Override
	public Object removePropertyFilter(String key) {
		return propertyFilters.get(key);
	}

}
