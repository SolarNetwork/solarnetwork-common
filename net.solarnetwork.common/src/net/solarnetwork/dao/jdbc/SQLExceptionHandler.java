/* ==================================================================
 * SQLExceptionHandler.java - 24/07/2016 3:14:47 PM
 * 
 * Copyright 2007-2016 SolarNetwork.net Dev Team
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

package net.solarnetwork.dao.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * A handler for SQL exceptions.
 * 
 * @author matt
 * @version 1.0
 */
public interface SQLExceptionHandler {

	/**
	 * Handle an exception triggered when a connection cannot be obtained.
	 * 
	 * @param e
	 *        The exception to handle.
	 */
	void handleGetConnectionException(SQLException e);

	/**
	 * Handle an exception triggered on an active Connection.
	 * 
	 * @param conn
	 *        The {@code Connection} the exception occurred on.
	 * @param e
	 *        The exception.
	 */
	void handleConnectionException(Connection conn, SQLException e);

}
