/* ==================================================================
 * FallbackOptionalService.java - 9/03/2020 1:40:34 pm
 * 
 * Copyright 2020 SolarNetwork.net Dev Team
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

import java.util.Collection;

/**
 * Implementation of {@link OptionalService} using a collection of delegate
 * optional services, returning the first available service when asked.
 * 
 * <p>
 * This can be useful when the an optional service should be used in preference
 * to a completely independently configured default service.
 * </p>
 * 
 * @param <T>
 *        the service type
 * @author matt
 * @version 1.0
 * @since 1.59
 */
public class FallbackOptionalService<T> implements OptionalService<T> {

	private final Collection<OptionalService<T>> services;

	/**
	 * Constructor.
	 * 
	 * @param services
	 *        the services to use
	 */
	public FallbackOptionalService(Collection<OptionalService<T>> services) {
		super();
		if ( services == null ) {
			throw new IllegalArgumentException("The services parameter must not be null.");
		}
		this.services = services;
	}

	@Override
	public T service() {
		for ( OptionalService<T> service : services ) {
			T s = service.service();
			if ( s != null ) {
				return s;
			}
		}
		return null;
	}

}
