/* ==================================================================
 * BasicLongEntity.java - 7/02/2020 9:00:34 am
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
 * Basic implementation of {@link Entity} with long primary key type.
 *
 * @author matt
 * @version 1.1
 * @since 1.59
 */
public abstract class BasicLongEntity extends BasicEntity<Long> {

	private static final long serialVersionUID = 307950311993178478L;

	/**
	 * Constructor.
	 */
	public BasicLongEntity() {
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
	public BasicLongEntity(Long id, Instant created) {
		super(id, created);
	}

	@Override
	public BasicLongEntity clone() {
		return (BasicLongEntity) super.clone();
	}

}
