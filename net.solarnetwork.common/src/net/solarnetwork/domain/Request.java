/* ==================================================================
 * Request.java - Nov 20, 2012 7:04:41 AM
 * 
 * Copyright 2007-2012 SolarNetwork.net Dev Team
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

import java.util.Map;

/**
 * A request envelope object.
 * 
 * @author matt
 * @version 1.0
 */
public class Request {

	private final String username;
	private final String password;
	private final Map<String, Object> data;

	/**
	 * Constructor.
	 * 
	 * @param username
	 *        the username
	 * @param password
	 *        the password
	 * @param data
	 *        the message data
	 */
	public Request(String username, String password, Map<String, Object> data) {
		super();
		this.username = username;
		this.password = password;
		this.data = data;
	}

	/**
	 * Get the username.
	 * 
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Get the password.
	 * 
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Get the message data.
	 * 
	 * @return the data
	 */
	public Map<String, Object> getData() {
		return data;
	}

}
