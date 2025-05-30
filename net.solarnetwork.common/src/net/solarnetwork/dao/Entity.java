/* ==================================================================
 * Entity.java - 7/02/2020 8:09:45 am
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

import java.time.Instant;
import net.solarnetwork.domain.Identity;

/**
 * Base API for a persistable domain object.
 *
 * @param <K>
 *        the primary key type
 * @author matt
 * @version 2.0
 * @since 1.59
 */
public interface Entity<T extends Entity<T, K>, K extends Comparable<K>> extends Identity<T, K> {

	/**
	 * Get the date this entity was created.
	 *
	 * @return the created date
	 */
	public Instant getCreated();

}
