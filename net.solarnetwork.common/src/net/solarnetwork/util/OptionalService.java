/* ==================================================================
 * ServiceTracker.java - Dec 3, 2012 7:02:45 AM
 * 
 * Copyright 2007-2012 SolarNetwork.net Dev Team
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

/**
 * API for an "optional" service.
 * 
 * <p>
 * This API is like a simplified OSGi ServiceTracker. Calling the
 * {@link #service()} method will return the first available matching service,
 * or <em>null</em> if none available.
 * </p>
 * 
 * @param <T>
 *        the tracked service type
 * @author matt
 * @version 1.0
 */
public interface OptionalService<T> {

	/**
	 * Get the configured service, or <em>null</em> if none available.
	 * 
	 * @return the service, or <em>null</em>
	 */
	T service();

}
