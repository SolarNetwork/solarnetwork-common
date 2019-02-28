/* ==================================================================
 * SkyConditions.java - 18/02/2019 9:14:44 am
 * 
 * Copyright 2019 SolarNetwork.net Dev Team
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

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * A set of sky conditions.
 * 
 * @author matt
 * @version 1.0
 * @since 1.50
 */
public class SkyConditions {

	private final boolean night;
	private final Set<SkyCondition> conditions;

	/**
	 * Constructor for day conditions.
	 * 
	 * @param conditions
	 *        the daytime conditions
	 */
	public SkyConditions(Set<SkyCondition> conditions) {
		this(conditions, false);
	}

	/**
	 * Constructor.
	 * 
	 * @param conditions
	 *        the conditions
	 * @param night
	 *        {@literal true} for night-time, {@literal false} for daytime
	 */
	public SkyConditions(Set<SkyCondition> conditions, boolean night) {
		super();
		this.conditions = (conditions != null ? EnumSet.copyOf(conditions) : Collections.emptySet());
		this.night = night;
	}

	/**
	 * Get the night-time flag.
	 * 
	 * @return {@literal true} for night-time conditions
	 */
	public boolean isNight() {
		return night;
	}

	/**
	 * Get the set of conditions.
	 * 
	 * @return the conditions
	 */
	public Set<SkyCondition> getConditions() {
		return conditions;
	}

}
