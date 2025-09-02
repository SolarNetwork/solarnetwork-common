/* ==================================================================
 * BasicIdentityLocation.java - 2/09/2025 5:52:23 pm
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

import java.util.Objects;

/**
 * Location with an identity.
 *
 * @author matt
 * @version 1.0
 * @since 4.4
 */
public class BasicIdentityLocation extends BasicLocation implements Identity<Long> {

	private static final long serialVersionUID = 3445970896204685365L;

	private final Long id;

	/**
	 * Constructor.
	 *
	 * @param id
	 *        the ID
	 * @param loc
	 *        the location to copy the values from
	 */
	public BasicIdentityLocation(Long id, Location loc) {
		super(loc);
		this.id = id;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public BasicIdentityLocation clone() {
		return (BasicIdentityLocation) super.clone();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(id);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( !super.equals(obj) ) {
			return false;
		}
		if ( !(obj instanceof BasicIdentityLocation) ) {
			return false;
		}
		BasicIdentityLocation other = (BasicIdentityLocation) obj;
		return Objects.equals(id, other.id);
	}

}
