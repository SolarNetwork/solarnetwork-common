/* ==================================================================
 * StringNaturalSortComparator.java - 16/11/2022 10:57:36 am
 * 
 * Copyright 2022 SolarNetwork.net Dev Team
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

package net.solarnetwork.util;

import java.util.Comparator;

/**
 * Compare strings using "natural order".
 * 
 * <p>
 * See <a href="https://en.wikipedia.org/wiki/Natural_sort_order">Natural sort
 * order</a> for more information.
 * </p>
 * 
 * @author matt
 * @version 1.0
 * @since 2.12
 */
public class StringNaturalSortComparator implements Comparator<String> {

	/** A default intstance for case-sensitive natural sort order. */
	public static final Comparator<String> CASE_SENSITIVE_NATURAL_SORT = new StringNaturalSortComparator(
			false);

	/** A default intstance for case-insensitive natural sort order. */
	public static final Comparator<String> CASE_INSENSITIVE_NATURAL_SORT = new StringNaturalSortComparator(
			true);

	private final boolean caseInsensitive;

	/**
	 * Constructor.
	 * 
	 * <p>
	 * This will preserve case.
	 * </p>
	 */
	public StringNaturalSortComparator() {
		this(false);
	}

	/**
	 * Constructor.
	 * 
	 * @param caseInsensitive
	 *        {@literal true} to ignore case
	 */
	public StringNaturalSortComparator(boolean caseInsensitive) {
		super();
		this.caseInsensitive = caseInsensitive;
	}

	@Override
	public int compare(String o1, String o2) {
		return StringUtils.naturalSortCompare(o1, o2, caseInsensitive);
	}

}
