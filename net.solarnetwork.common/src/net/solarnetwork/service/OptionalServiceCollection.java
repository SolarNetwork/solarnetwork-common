/* ==================================================================
 * OptionalServiceCollection.java - Mar 24, 2014 1:41:25 PM
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

package net.solarnetwork.service;

/**
 * API for a collection of "optional" services. This API is like a simplified
 * OSGi ServiceTracker for a collection of services. Calling the
 * {@link #services()} method will return the all available matching services.
 * 
 * @param <T>
 *        the tracked service type
 * @author matt
 * @version 1.1
 * @see OptionalService
 */
public interface OptionalServiceCollection<T> {

	/**
	 * A convenient configurable optional service collection.
	 * 
	 * @param <T>
	 *        the tracked service type
	 */
	interface OptionalFilterableServiceCollection<T>
			extends OptionalServiceCollection<T>, FilterableService {
		// no additional methods
	}

	/**
	 * Get the collection of configured services.
	 * 
	 * @return the services, never {@literal null} but could be empty
	 */
	Iterable<T> services();

	/**
	 * Resolve an optional service.
	 * 
	 * <p>
	 * This method is a convenient way to deal with a possibly null
	 * {@code OptionalServiceCollection}.
	 * </p>
	 * 
	 * @param <T>
	 *        the service type
	 * @param optional
	 *        the optional service collection, or {@literal null}
	 * @return the resolved services, or {@literal null}
	 * @since 1.1
	 */
	static <T> Iterable<T> services(OptionalServiceCollection<T> optional) {
		return services(optional, null);
	}

	/**
	 * Resolve an optional service with a fallback.
	 * 
	 * <p>
	 * This method is a convenient way to deal with a possibly null
	 * {@code OptionalServiceCollection}.
	 * </p>
	 * 
	 * @param <T>
	 *        the service type
	 * @param optional
	 *        the optional service collection, or {@literal null}
	 * @param fallback
	 *        the result to return if {@code optional} is {@literal null} or its
	 *        resolved service collection is {@literal null}
	 * @return the resolved services, or {@code fallback}
	 * @since 1.1
	 */
	static <T> Iterable<T> services(OptionalServiceCollection<T> optional, Iterable<T> fallback) {
		Iterable<T> service = (optional != null ? optional.services() : null);
		return (service != null ? service : fallback);
	}

}
