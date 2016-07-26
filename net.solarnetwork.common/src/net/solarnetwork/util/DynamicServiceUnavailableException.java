/* ==================================================================
 * DynamicServiceUnavailableException.java - 8/06/2015 3:20:17 pm
 * 
 * Copyright 2007-2015 SolarNetwork.net Dev Team
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
 * Exception thrown when a dynamic service is not available.
 * 
 * @author matt
 * @version 1.0
 */
public class DynamicServiceUnavailableException extends RuntimeException {

	private static final long serialVersionUID = -6082514393080966631L;

	public DynamicServiceUnavailableException() {
		super();
	}

	public DynamicServiceUnavailableException(String message, Throwable cause) {
		super(message, cause);
	}

	public DynamicServiceUnavailableException(String message) {
		super(message);
	}

	public DynamicServiceUnavailableException(Throwable cause) {
		super(cause);
	}

}
