/* ==================================================================
 * ConfigurationType.java - 31/01/2020 10:02:59 am
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

package net.solarnetwork.ocpp.domain;

/**
 * An enumeration of data types for configuration key values.
 * 
 * @author matt
 * @version 1.0
 */
public enum ConfigurationType {

	/** Logical boolean. */
	Boolean,

	/** A comma separated list of string. */
	CSL,

	/** An integer number. */
	Integer,

	/** An arbitrary string. */
	String;

}
