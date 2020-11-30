/* ==================================================================
 * SimpleSortDescriptor.java - Jun 10, 2011 7:09:23 PM
 * 
 * Copyright 2007-2011 SolarNetwork.net Dev Team
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Implementation of {@link SortDescriptor}.
 * 
 * @author matt
 * @version 1.1
 */
public class SimpleSortDescriptor implements SortDescriptor {

	private final String sortKey;
	private final boolean descending;

	/**
	 * Create a list of ascending sort descriptors.
	 * 
	 * @param keys
	 *        the sort keys
	 * @return the descriptors, or {@literal null} if {@code keys} has no
	 *         elements
	 */
	public static List<SortDescriptor> sorts(String... keys) {
		if ( keys == null || keys.length < 1 ) {
			return null;
		}
		List<SortDescriptor> result = new ArrayList<>(keys.length);
		for ( String k : keys ) {
			result.add(new SimpleSortDescriptor(k));
		}
		return result;
	}

	/**
	 * Construct with a sort key.
	 * 
	 * Ascending order will be used.
	 * 
	 * @param sortKey
	 *        the sort key
	 */
	public SimpleSortDescriptor(String sortKey) {
		this(sortKey, false);
	}

	public SimpleSortDescriptor(String sortKey, boolean descending) {
		super();
		this.sortKey = sortKey;
		this.descending = descending;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SimpleSortDescriptor{");
		builder.append(sortKey);
		if ( descending ) {
			builder.append(" DESC");
		}
		builder.append("}");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		return Objects.hash(descending, sortKey);
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( !(obj instanceof SimpleSortDescriptor) ) {
			return false;
		}
		SimpleSortDescriptor other = (SimpleSortDescriptor) obj;
		return descending == other.descending && Objects.equals(sortKey, other.sortKey);
	}

	@Override
	public String getSortKey() {
		return sortKey;
	}

	@Override
	public boolean isDescending() {
		return descending;
	}

}
