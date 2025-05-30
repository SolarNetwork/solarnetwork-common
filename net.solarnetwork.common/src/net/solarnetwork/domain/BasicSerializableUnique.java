/* ==================================================================
 * BasicUnique.java - 30/05/2025 11:34:40 am
 *
 * Copyright 2025 SolarNetwork.net Dev Team
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

package net.solarnetwork.domain;

import java.io.Serializable;
import java.util.Objects;

/**
 * Basic immuntable implementation of {@link Unique}.
 *
 * @author matt
 * @version 1.0
 */
public class BasicSerializableUnique<K> implements Unique<K>, Serializable {

	private static final long serialVersionUID = -3308792199560978726L;

	private final K id;

	/**
	 * Constructor.
	 *
	 * @param id
	 *        the ID to use
	 */
	public BasicSerializableUnique(K id) {
		super();
		this.id = id;
	}

	@Override
	public final K getId() {
		return id;
	}

	/**
	 * Compute a hash code based on the {@link #getId()} value.
	 *
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	/**
	 * Test if two {@code BasicUnique} objects have the same {@link #getId()}
	 * value.
	 *
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( obj == null ) {
			return false;
		}
		if ( getClass() != obj.getClass() ) {
			return false;
		}
		@SuppressWarnings("unchecked")
		Unique<K> other = (Unique<K>) obj;
		return Objects.equals(id, other.getId());
	}

}
