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

package net.solarnetwork.util;

/**
 * API for an "optional" service.
 * 
 * <p>
 * This API is like a simplified OSGi ServiceTracker. Calling the
 * {@link #service()} method will return the first available matching service,
 * or <em>null</em> if none available.
 * </p>
 * 
 * @param <T>
 *        the tracked service type
 * @author matt
 * @version 1.1
 */
public interface OptionalService<T> {

	/**
	 * Get the configured service, or <em>null</em> if none available.
	 * 
	 * @return the service, or <em>null</em>
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

}
