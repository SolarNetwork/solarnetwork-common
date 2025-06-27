/* ==================================================================
 * BasicSerializableIdentity.java - 11/04/2018 6:59:05 AM
 *
 * Copyright 2018 SolarNetwork.net Dev Team
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

/**
 * A basic, immutable implementation of {@link Identity}.
 *
 * @param <K>
 *        the primary key type
 * @author matt
 * @version 2.0
 * @since 1.43
 */
public abstract class BasicIdentity<K extends Comparable<K>> extends BasicUnique<K>
		implements Identity<K> {

	/**
	 * Constructor.
	 *
	 * @param id
	 *        the ID to use
	 */
	public BasicIdentity(K id) {
		super(id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public BasicIdentity<K> clone() {
		try {
			return (BasicIdentity<K>) super.clone();
		} catch ( CloneNotSupportedException e ) {
			// should never get here
			throw new IllegalStateException(e);
		}
	}

}
