/* ==================================================================
 * NodeControlInfo.java - Sep 28, 2011 4:08:29 PM
 * 
 * Copyright 2007-2011 SolarNetwork.net Dev Team
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
 * API for a user-manageable node component.
 * 
 * @author matt
 * @version 1.0
 */
public interface NodeControlInfo {

	/**
	 * Get the control ID.
	 * 
	 * @return the control ID
	 */
	String getControlId();

	/**
	 * Get an optional control property name.
	 * 
	 * @return the control property name, or {@literal null}
	 */
	String getPropertyName();

	/**
	 * Get the control property type.
	 * 
	 * @return the property type
	 */
	NodeControlPropertyType getType();

	/**
	 * Get the control value.
	 * 
	 * @return the value
	 */
	String getValue();

	/**
	 * Get a read-only flag.
	 * 
	 * @return the read-only flag
	 */
	Boolean getReadonly();

	/**
	 * Get an optional unit of measure for the control value.
	 * 
	 * @return the unit of measure, or {@literal null}
	 */
	String getUnit();

}
