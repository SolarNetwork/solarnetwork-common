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
import javax.sql.DataSource;

/**
 * A handler for SQL exceptions.
 * 
 * @author matt
 * @version 1.1
 */
public interface SQLExceptionHandler {

	/**
	 * Handle an exception triggered when a connection cannot be obtained.
	 * 
	 * @param e
	 *        the exception to handle
	 */
	void handleGetConnectionException(SQLException e);

	/**
	 * Handle an exception triggered when a connection cannot be obtained from a
	 * {@code DataSource}.
	 * 
	 * <p>
	 * This default implementation simply calls
	 * {@link #handleGetConnectionException(SQLException)}.
	 * </p>
	 * 
	 * @param dataSource
	 *        the data source
	 * @param e
	 *        the exception to handle
	 * @since 1.1
	 */
	default void handleGetConnectionException(DataSource dataSource, SQLException e) {
		handleGetConnectionException(e);
	}

	/**
	 * Handle an exception triggered on an active {@code Connection}.
	 * 
	 * @param conn
	 *        the {@code Connection} the exception occurred on
	 * @param e
	 *        the exception to handle
	 */
	void handleConnectionException(Connection conn, SQLException e);

	/**
	 * Handle an exception triggered on an active {@code Connection}.
	 * 
	 * <p>
	 * This default implementation simply calls
	 * {@link #handleConnectionException(Connection, SQLException)}.
	 * </p>
	 * 
	 * @param dataSource
	 *        the data source
	 * @param conn
	 *        the {@code Connection} the exception occurred on
	 * @param e
	 *        the exception to handle
	 * @since 1.1
	 */
	default void handleConnectionException(DataSource dataSource, Connection conn, SQLException e) {
		handleConnectionException(conn, e);
	}

}
