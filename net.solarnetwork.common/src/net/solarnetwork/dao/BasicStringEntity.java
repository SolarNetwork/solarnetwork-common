/* ==================================================================
 * BasicStringEntity.java - 7/02/2020 8:59:16 am
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

/**
 * Basic implementation of {@link Entity} with string primary key type.
 *
 * @param <T>
 *        the entity type
 * @author matt
 * @version 2.0
 * @since 1.59
 */
public class BasicStringEntity<T extends BasicStringEntity<T>> extends BasicEntity<T, String> {

	private static final long serialVersionUID = -8170743004788998114L;

	/**
	 * Constructor.
	 */
	public BasicStringEntity() {
		super();
	}

	/**
	 * Constructor.
	 *
	 * @param id
	 *        the primary key
	 * @param created
	 *        the created date
	 */
	public BasicStringEntity(String id, Instant created) {
		super(id, created);
	}

}
