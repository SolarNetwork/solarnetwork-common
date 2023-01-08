/* ==================================================================
 * BasicPK.java - 5/06/2021 3:04:42 PM
 * 
 * Copyright 2021 SolarNetwork.net Dev Team
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
import net.solarnetwork.domain.Identity;

/**
 * A basic, immutable implementation of {@link Identity} that is also
 * {@link Serializable}.
 * 
 * @author matt
 * @version 1.0
 * @since 1.72
 */
public class BasicIdentity<PK extends Comparable<PK>> implements Identity<PK>, Serializable {

	private static final long serialVersionUID = 1468072353770355777L;

	/** The primary key. */
	private final PK id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *        the ID to use
	 */
	public BasicIdentity(PK id) {
		super();
		this.id = id;
	}

	@SuppressWarnings("unchecked")
	@Override
	public BasicIdentity<PK> clone() {
		try {
			return (BasicIdentity<PK>) super.clone();
		} catch ( CloneNotSupportedException e ) {
			// should never get here
			throw new RuntimeException(e);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	/**
	 * Test if two {@code BasicIdentity} objects have the same {@link #getId()}
	 * value.
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
		BasicIdentity<PK> other = (BasicIdentity<PK>) obj;
		if ( id == null ) {
			if ( other.id != null ) {
				return false;
			}
		} else if ( !id.equals(other.id) ) {
			return false;
		}
		return true;
	}

	/**
	 * Compare based on the {@code id}, with {@literal null} values ordered
	 * before non-{@literal null} values.
	 */
	@Override
	public int compareTo(PK o) {
		if ( id == null && o == null ) {
			return 0;
		}
		if ( id == null ) {
			return -1;
		}
		if ( o == null ) {
			return 1;
		}
		return id.compareTo(o);
	}

	@Override
	public PK getId() {
		return id;
	}

}
