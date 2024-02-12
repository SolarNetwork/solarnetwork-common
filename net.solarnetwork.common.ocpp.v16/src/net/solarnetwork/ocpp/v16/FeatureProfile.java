/* ==================================================================
 * FeatureProfile.java - 31/01/2020 11:13:02 am
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

package net.solarnetwork.ocpp.v16;

/**
 * OCPP 1.6 enumeration of feature profiles.
 * 
 * @author matt
 * @version 1.0
 */
public enum FeatureProfile {

	/** Core. */
	Core,

	/** Firmware management. */
	FirmwareManagement,

	/** Local auth list management. */
	LocalAuthListManagement,

	/** Reservation. */
	Reservation,

	/** Remote trigger. */
	RemoteTrigger,

	/** Smart charging. */
	SmartCharging;

}
