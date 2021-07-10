/* ==================================================================
 * DeviceInfo.java - 9/07/2021 6:22:33 AM
 * 
 * Copyright 2021 SolarNetwork.net Dev Team
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

import java.time.LocalDate;

/**
 * Static information about a device, such as manufacturer name, model number,
 * and so on.
 * 
 * <p>
 * All properties are considered optional, and methods may return
 * {@literal null} if a property is not known.
 * </p>
 * 
 * @author matt
 * @version 1.0
 * @since 1.75
 */
public interface DeviceInfo {

	/** A property metadata key to use for device info data. */
	static final String DEVICE_INFO_METADATA_KEY = "deviceInfo";

	/**
	 * Get the name of the device.
	 * 
	 * @return the name
	 */
	String getName();

	/**
	 * Get the name of the manufacturer.
	 * 
	 * @return the manufacturer
	 */
	String getManufacturer();

	/**
	 * Get the model name.
	 * 
	 * @return the model name
	 */
	String getModelName();

	/**
	 * Get the model revision.
	 * 
	 * @return the version
	 */
	String getVersion();

	/**
	 * Get the device serial number.
	 * 
	 * @return the device serial number
	 */
	String getSerialNumber();

	/**
	 * Get the device manufacture date.
	 * 
	 * @return the manufacture date
	 */
	LocalDate getManufactureDate();

	/**
	 * Get a deployment-specific location identifier, such as IP address or
	 * Modbus unit ID.
	 * 
	 * @return the device address
	 */
	String getDeviceAddress();

}
