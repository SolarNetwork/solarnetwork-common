/* ==================================================================
 * Identity.java - Aug 8, 2010 7:42:21 PM
 *
 * Copyright 2007-2010 SolarNetwork.net Dev Team
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
 * $Revision$
 * ==================================================================
 */

package net.solarnetwork.domain;

/**
 * Common API for identity information in SolarNetwork participating services.
 *
 * @param <K>
 *        the primary data type that uniquely identifies the object
 * @version 2.0
 * @author matt
 * @since 1.43
 */
public interface Identity<K extends Comparable<K>>
		extends Unique<K>, Comparable<Identity<K>>, Cloneable {

	/**
	 * Compare based on the {@code getId()} value only, with {@literal null}
	 * values ordered before non-{@literal null} values.
	 *
	 * {@inheritDoc}
	 */
	@Override
	default int compareTo(Identity<K> o) {
		final K id = getId();
		final K otherId = (o != null ? o.getId() : null);
		if ( id == null && otherId == null ) {
			return 0;
		}
		if ( id == null ) {
			return -1;
		}
		if ( otherId == null ) {
			return 1;
		}
		return id.compareTo(otherId);
	}

}
