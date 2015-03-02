/* ==================================================================
 * StaticOptionalServiceCollection.java - Dec 12, 2014 11:55:24 AM
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

import java.util.Collection;

/**
 * Implementation of {@link OptionalServiceCollection} using a static collection
 * of service instances.
 * 
 * <p>
 * This can be useful when the {@link OptionalServiceCollection} API is
 * required, but the service is known and available statically.
 * </p>
 * 
 * @author matt
 * @version 1.0
 */
public class StaticOptionalServiceCollection<T> implements OptionalServiceCollection<T> {

	private final Collection<T> services;

	/**
	 * Construct with the static services.
	 * 
	 * @param services
	 *        the services
	 */
	public StaticOptionalServiceCollection(Collection<T> services) {
		super();
		this.services = services;
	}

	@Override
	public Iterable<T> services() {
		return services;
	}

}
