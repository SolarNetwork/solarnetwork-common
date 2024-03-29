/* ==================================================================
 * ServiceTracker.java - Dec 3, 2012 7:02:45 AM
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

package net.solarnetwork.service;

import java.util.Map;

/**
 * API for an "optional" service.
 * 
 * <p>
 * This API is like a simplified OSGi ServiceTracker. Calling the
 * {@link #service()} method will return the first available matching service,
 * or {@literal null} if none available.
 * </p>
 * 
 * @param <T>
 *        the tracked service type
 * @author matt
 * @version 2.0
 */
public interface OptionalService<T> {

	/**
	 * A convenient configurable optional service.
	 * 
	 * @param <T>
	 *        the tracked service type
	 */
	interface OptionalFilterableService<T> extends OptionalService<T>, FilterableService {
		// no additional methods
	}

	/**
	 * Get the configured service, or {@literal null} if none available.
	 * 
	 * @return the service, or {@literal null}
	 */
	T service();

	/**
	 * Resolve an optional service.
	 * 
	 * <p>
	 * This method is a convenient way to deal with a possibly null
	 * {@code OptionalService}.
	 * </p>
	 * 
	 * @param <T>
	 *        the service type
	 * @param optional
	 *        the optional service, or {@literal null}
	 * @return the resolved service, or {@literal null}
	 * @since 1.1
	 */
	static <T> T service(OptionalService<T> optional) {
		return service(optional, null);
	}

	/**
	 * Resolve an optional service with a fallback.
	 * 
	 * <p>
	 * This method is a convenient way to deal with a possibly null
	 * {@code OptionalService}.
	 * </p>
	 * 
	 * @param <T>
	 *        the service type
	 * @param optional
	 *        the optional service, or {@literal null}
	 * @param fallback
	 *        the result to return if {@code optional} is {@literal null} or its
	 *        resolved service is {@literal null}
	 * @return the resolved service, or {@code fallback}
	 * @since 1.1
	 */
	static <T> T service(OptionalService<T> optional, T fallback) {
		T service = (optional != null ? optional.service() : null);
		return (service != null ? service : fallback);
	}

	/**
	 * Resolve a required optional service, throwing an exception if not
	 * available.
	 * 
	 * @param <T>
	 *        the service type
	 * @param optional
	 *        the optional service, or {@literal null}
	 * @return the resolved service, never {@literal null}
	 * @throws OptionalServiceNotAvailableException
	 *         if the service can not be resolved
	 * @since 2.0
	 */
	static <T> T requiredService(OptionalService<T> optional) {
		return requiredService(optional, null);
	}

	/**
	 * Resolve a required optional service, throwing an exception if not
	 * available.
	 * 
	 * @param <T>
	 *        the service type
	 * @param optional
	 *        the optional service, or {@literal null}
	 * @param description
	 *        a description to use if the service can not be resolved, or
	 *        {@literal null}
	 * @return the resolved service, never {@literal null}
	 * @throws OptionalServiceNotAvailableException
	 *         if the service can not be resolved
	 * @since 2.0
	 */
	static <T> T requiredService(OptionalService<T> optional, String description) {
		T service = null;
		Exception t = null;
		try {
			service = service(optional);
			if ( service != null ) {
				return service;
			}
		} catch ( Exception e ) {
			t = e;
		}
		StringBuilder msg = new StringBuilder("Service");
		if ( description != null ) {
			msg.append(" [").append(description).append(']');
		}
		if ( optional instanceof FilterableService ) {
			FilterableService f = (FilterableService) optional;
			Map<String, ?> filters = f.getPropertyFilters();
			if ( filters != null && !filters.isEmpty() ) {
				msg.append(" matching filter ").append(filters);
			}
		}
		msg.append(" not");
		if ( optional == null ) {
			msg.append(" configured");
		} else {
			msg.append(" available");
		}
		msg.append('.');
		throw new OptionalServiceNotAvailableException(msg.toString(), t);
	}

}
