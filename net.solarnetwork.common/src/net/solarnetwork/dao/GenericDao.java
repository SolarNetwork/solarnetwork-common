/* ==================================================================
 * GenericDao.java - 7/02/2020 8:08:30 am
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

package net.solarnetwork.dao;

import java.util.Collection;
import java.util.List;
import net.solarnetwork.domain.SortDescriptor;

/**
 * A simple Data Access Object (DAO) API for managing persistent entity objects.
 * 
 * @param <T>
 *        the entity type managed by this DAO
 * @param <K>
 *        the entity primary key type
 * @author matt
 * @version 1.0
 * @since 1.59
 */
public interface GenericDao<T extends Entity<K>, K> {

	/**
	 * Get the entity class supported by this DAO.
	 * 
	 * @return class
	 */
	Class<? extends T> getObjectType();

	/**
	 * Persist an entity, creating or updating as appropriate.
	 * 
	 * @param entity
	 *        the domain object so store
	 * @return the primary key of the stored object
	 */
	K save(T entity);

	/**
	 * Get a persisted entity by its primary key.
	 * 
	 * @param id
	 *        the primary key to retrieve
	 * @return the domain object, or {@literal null} if not available
	 */
	T get(K id);

	/**
	 * Get all persisted entities, optionally sorted in some way.
	 * 
	 * <p>
	 * The {@code sortDescriptors} parameter can be {@literal null}, in which
	 * case the sort order is not defined and implementation specific.
	 * </p>
	 * 
	 * @param sorts
	 *        list of sort descriptors to sort the results by
	 * @return list of all persisted entities, or empty list if none available
	 */
	Collection<T> getAll(List<SortDescriptor> sorts);

	/**
	 * Remove a persisted entity.
	 * 
	 * @param entity
	 *        the entity to delete
	 */
	void delete(T entity);

}
