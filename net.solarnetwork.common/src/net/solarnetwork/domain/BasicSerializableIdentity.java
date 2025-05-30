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

package net.solarnetwork.domain;

import java.io.Serializable;

/**
 * A basic, immutable implementation of {@link Identity} that is also
 * {@link Serializable}.
 *
 * @param <T>
 *        the identity type
 * @param <K>
 *        the primary key type
 * @author matt
 * @version 1.0
 * @since 4.0
 */
public abstract class BasicSerializableIdentity<T extends BasicSerializableIdentity<T, K>, K extends Comparable<K> & Serializable>
		extends BasicSerializableUnique<K> implements Identity<T, K>, Serializable {

	private static final long serialVersionUID = 1468072353770355777L;

	/**
	 * Constructor.
	 *
	 * @param id
	 *        the ID to use
	 */
	public BasicSerializableIdentity(K id) {
		super(id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T clone() {
		try {
			return (T) super.clone();
		} catch ( CloneNotSupportedException e ) {
			// should never get here
			throw new IllegalStateException(e);
		}
	}

}
