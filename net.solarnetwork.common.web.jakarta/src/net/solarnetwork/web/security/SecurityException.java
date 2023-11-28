/* ==================================================================
 * SecurityException.java - Dec 18, 2009 4:31:14 PM
 * 
 * Copyright 2007-2009 SolarNetwork.net Dev Team
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

package net.solarnetwork.web.security;

/**
 * Exception for security errors.
 * 
 * @author matt
 * @version 1.0
 * @since 1.11
 */
public class SecurityException extends RuntimeException {

	private static final long serialVersionUID = 4715317846353024503L;

	/**
	 * Constructor.
	 */
	public SecurityException() {
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param msg
	 *        the message
	 * @param t
	 *        the cause
	 */
	public SecurityException(String msg, Throwable t) {
		super(msg, t);
	}

	/**
	 * Constructor.
	 * 
	 * @param msg
	 *        the message
	 */
	public SecurityException(String msg) {
		super(msg);
	}

	/**
	 * Constructor.
	 * 
	 * @param t
	 *        the cause
	 */
	public SecurityException(Throwable t) {
		super(t);
	}

}
