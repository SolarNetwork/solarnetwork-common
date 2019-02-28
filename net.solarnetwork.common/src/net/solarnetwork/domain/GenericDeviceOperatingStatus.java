/* ==================================================================
 * GenericDeviceOperatingStatus.java - 18/02/2019 11:42:44 am
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

import java.util.Set;

/**
 * Concrete implementation of {@link DeviceOperatingStatus} using
 * {@link GenericDeviceOperatingState} device states.
 * 
 * @author matt
 * @version 1.0
 */
public class GenericDeviceOperatingStatus extends DeviceOperatingStatus<GenericDeviceOperatingState> {

	/**
	 * Constructor.
	 * 
	 * @param state
	 *        the state
	 */
	public GenericDeviceOperatingStatus(DeviceOperatingState state) {
		super(state);
	}

	/**
	 * Constructor.
	 * 
	 * @param state
	 * @param deviceStates
	 */
	public GenericDeviceOperatingStatus(DeviceOperatingState state,
			Set<GenericDeviceOperatingState> deviceStates) {
		super(state, deviceStates);
	}

}
