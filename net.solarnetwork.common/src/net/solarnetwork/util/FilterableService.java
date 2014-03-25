/* ==================================================================
 * FilterableService.java - Mar 25, 2014 11:26:25 AM
 * 
 * Copyright 2007-2014 SolarNetwork.net Dev Team
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

import java.util.Map;

/**
 * FIXME
 * 
 * <p>
 * TODO
 * </p>
 * 
 * <p>
 * The configurable properties of this class are:
 * </p>
 * 
 * <dl class="class-properties">
 * <dt></dt>
 * <dd></dd>
 * </dl>
 * 
 * @author matt
 * @version 1.0
 */
public interface FilterableService {

	/**
	 * Get the current map of property filters, with keys representing property
	 * names and value their desired associated value.
	 * 
	 * @return filters
	 */
	Map<String, ?> getPropertyFilters();

	/**
	 * Set a property filter value.
	 * 
	 * @param key
	 *        the key to add
	 * @param value
	 *        the value
	 */
	void setPropertyFilter(String key, Object value);

	/**
	 * Remove a property filter value.
	 * 
	 * @param key
	 *        the key to remove
	 * @return the removed value, or <em>null</em> if no value was available
	 */
	Object removePropertyFilter(String key);

}
