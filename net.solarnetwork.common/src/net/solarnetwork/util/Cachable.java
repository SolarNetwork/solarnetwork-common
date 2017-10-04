/* ===================================================================
 * Cachable.java
 * 
 * Created Sep 13, 2008 1:23:05 PM
 * 
 * Copyright (c) 2008 Solarnetwork.net Dev Team.
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
 * ===================================================================
 */

package net.solarnetwork.util;

/**
 * API for items that can be cached.
 *
 * @author matt.magoffin
 * @version 1.0
 */
public interface Cachable {

	/**
	 * Get a unique cache key that identifies this cachable item.
	 * 
	 * @return the cache key
	 */
	String getCacheKey();

	/**
	 * Get a suggested time-to-live, in seconds.
	 * 
	 * @return TTL in seconds, or {@literal null} if should use a default value
	 */
	Long getTtl();

	/**
	 * Get a suggested time-to-idle, in seconds.
	 * 
	 * @return TTI in seconds, or {@literal null} if should use a default value
	 */
	Long getTti();

}
