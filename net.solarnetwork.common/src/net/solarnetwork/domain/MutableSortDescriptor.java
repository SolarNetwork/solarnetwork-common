/* ==================================================================
 * MutableSortDescriptor.java - Dec 3, 2013 6:58:21 AM
 * 
 * Copyright 2007-2013 SolarNetwork.net Dev Team
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
 * Mutable implementation of {@link SortDescriptor}.
 * 
 * The {@code descending} property defaults to {@literal false}.
 * 
 * @author matt
 * @version 1.1
 */
public class MutableSortDescriptor implements SortDescriptor {

	private String sortKey;
	private boolean descending;

	/**
	 * Construct with ascending order.
	 */
	public MutableSortDescriptor() {
		super();
		this.descending = false;
	}

	/**
	 * Construct with a sort key.
	 * 
	 * Ascending order will be used.
	 * 
	 * @param sortKey
	 *        the sort key
	 */
	public MutableSortDescriptor(String sortKey) {
		this(sortKey, false);
	}

	/**
	 * Construct with a sort key and order.
	 * 
	 * @param sortKey
	 *        the sort key
	 * @param descending
	 *        {@code true} to sort in descending order, {@code false} for
	 *        ascending
	 */
	public MutableSortDescriptor(String sortKey, boolean descending) {
		super();
		this.sortKey = sortKey;
		this.descending = descending;
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
		if ( !(obj instanceof MutableSortDescriptor) ) {
			return false;
		}
		MutableSortDescriptor other = (MutableSortDescriptor) obj;
		return descending == other.descending && Objects.equals(sortKey, other.sortKey);
	}

	@Override
	public String getSortKey() {
		return sortKey;
	}

	/**
	 * Set the sort key.
	 * 
	 * @param sortKey
	 *        the key to set
	 */
	public void setSortKey(String sortKey) {
		this.sortKey = sortKey;
	}

	@Override
	public boolean isDescending() {
		return descending;
	}

	/**
	 * Set the descending flag.
	 * 
	 * @param descending
	 *        the flag to set
	 */
	public void setDescending(boolean descending) {
		this.descending = descending;
	}

}
