/* ==================================================================
 * ErrorHolder.java - 4/02/2020 5:50:50 pm
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

import java.util.Map;

/**
 * An object that holds error details.
 * 
 * @author matt
 * @version 1.0
 */
public interface ErrorHolder {

	/**
	 * Get the OCPP error code.
	 * 
	 * @return the error code, never {@literal null}
	 */
	ErrorCode getErrorCode();

	/**
	 * Get an optional description of the error.
	 * 
	 * @return the description, or {@literal null}
	 */
	String getErrorDescription();

	/**
	 * Get an optional map of error details.
	 * 
	 * @return the error details, or {@literal null}
	 */
	Map<String, ?> getErrorDetails();

}
