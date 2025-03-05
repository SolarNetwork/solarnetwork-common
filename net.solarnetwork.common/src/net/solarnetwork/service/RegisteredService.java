/* ==================================================================
 * RegisteredService.java - 6/07/2024 7:27:43 am
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

/**
 * A reference to a registered service.
 *
 * @param <S>
 *        the service type
 * @see ServiceRegistry
 */
public interface RegisteredService<S> {

	/**
	 * Get a copy of the properties of the service referenced by this
	 * {@code ServiceReference} object.
	 *
	 * @return A copy of the properties of the service referenced by this
	 *         {@code ServiceReference} object
	 */
	public Map<String, Object> properties();

}
