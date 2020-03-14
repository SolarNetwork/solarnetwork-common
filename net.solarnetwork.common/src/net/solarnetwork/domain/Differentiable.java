/* ==================================================================
 * Differentiable.java - 14/02/2020 9:14:20 am
 * 
 * Copyright 2020 SolarNetwork.net Dev Team
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
 * Common API for an object that can be compared to another for differences.
 * 
 * <p>
 * The actual meaning of <i>different</i> is implementation specific. For
 * example an entity might implement this API in order to tell if a modified
 * copy of an entity with the same identity as a persisted instance have
 * different property values, and thus the copy should be persisted to save the
 * updated property values.
 * </p>
 * 
 * @author matt
 * @version 1.0
 * @since 1.59
 */
public interface Differentiable<T> {

	boolean differsFrom(T other);

}
