/* ==================================================================
 * BasicEntity.java - 7/02/2020 8:30:47 am
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

import java.io.Serializable;
import java.time.Instant;

/**
 * Basic implementation of {@link Entity} using a comparable and serializable
 * primary key.
 * 
 * @param <K>
 *        the primary key type
 * @author matt
 * @version 1.1
 * @since 1.59
 */
public class BasicEntity<K extends Comparable<K> & Serializable> extends BasicIdentity<K>
		implements Entity<K>, Serializable {

	private static final long serialVersionUID = -2236221331020004471L;

	/** The creation date. */
	private final Instant created;

	/**
	 * Constructor.
	 */
	public BasicEntity() {
		this(null, Instant.now());
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *        the primary key
	 * @param created
	 *        the created date
	 */
	public BasicEntity(K id, Instant created) {
		super(id);
		this.created = created;
	}

	@Override
	public Instant getCreated() {
		return created;
	}

	@Override
	public BasicEntity<K> clone() {
		return (BasicEntity<K>) super.clone();
	}

}
