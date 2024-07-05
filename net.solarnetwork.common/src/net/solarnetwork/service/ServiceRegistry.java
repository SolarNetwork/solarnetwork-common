/* ==================================================================
 * ServiceRegistry.java - 6/07/2024 7:27:43 am
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

import java.util.Collection;

/**
 * API for a dynamic runtime service registry.
 *
 * @author matt
 * @version 1.0
 * @since 3.15
 */
public interface ServiceRegistry {

	/**
	 * Get a collection of available services, optionally matching a filter or
	 * predicate.
	 *
	 * @param <S>
	 *        the desired service type
	 * @param clazz
	 *        the service class
	 * @param filter
	 *        an optional LDAP-style search filter
	 * @return the resolved services, never {@literal null}
	 * @throws IllegalArgumentException
	 *         if {@code clazz} is {@literal null} or {@code filter} has an
	 *         invalid syntax
	 */
	Collection<?> services(String filter);

	/**
	 * Get a collection of available services, optionally matching a filter or
	 * predicate.
	 *
	 * @param <S>
	 *        the desired service type
	 * @param clazz
	 *        the service class
	 * @param filter
	 *        an optional LDAP-style search filter
	 * @return the resolved services, never {@literal null}
	 * @throws IllegalArgumentException
	 *         if {@code clazz} is {@literal null} or {@code filter} has an
	 *         invalid syntax
	 */
	<S> Collection<S> services(Class<S> clazz, String filter);
}
