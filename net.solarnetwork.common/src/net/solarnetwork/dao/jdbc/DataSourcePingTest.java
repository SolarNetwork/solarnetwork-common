/* ==================================================================
 * DataSourcePingTest.java - 25/05/2015 11:30:49 am
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

package net.solarnetwork.dao.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import javax.sql.DataSource;
import net.solarnetwork.domain.PingTest;
import net.solarnetwork.domain.PingTestResult;

/**
 * {@link PingTest} to verify a {@link DataSource} connection is available.
 * 
 * <p>
 * This test expects the configured {@code query} to return a
 * {@link java.sql.Timestamp} as the first column of the query result.
 * </p>
 * 
 * @author matt
 * @version 1.01
 * @since 1.52
 */
public class DataSourcePingTest implements PingTest {

	private final DataSource dataSource;
	private final String query;
	private final String id;

	/**
	 * Constructor.
	 * 
	 * <p>
	 * The test ID will be set to the name of this class.
	 * </p>
	 * 
	 * @param dataSource
	 *        the data source
	 * @param query
	 *        the query to execute
	 */
	public DataSourcePingTest(DataSource dataSource, String query) {
		super();
		this.dataSource = dataSource;
		this.query = query;
		this.id = getClass().getName();
	}

	/**
	 * Constructor.
	 * 
	 * @param dataSource
	 *        the data source
	 * @param query
	 *        the query to execute
	 * @param id
	 *        the test ID
	 * @since 1.1
	 */
	public DataSourcePingTest(DataSource dataSource, String query, String id) {
		super();
		this.dataSource = dataSource;
		this.query = query;
		this.id = id;
	}

	@Override
	public String getPingTestId() {
		return id;
	}

	@Override
	public String getPingTestName() {
		return "JDBC Pool DataSource Connection";
	}

	@Override
	public long getPingTestMaximumExecutionMilliseconds() {
		return 500;
	}

	@Override
	public PingTest.Result performPingTest() throws Exception {
		if ( dataSource == null ) {
			return new PingTestResult(false, "No DataSource configured.");
		}
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Timestamp ts = null;
		try {
			conn = dataSource.getConnection();
			conn.setAutoCommit(true);
			stmt = conn.prepareStatement(query);
			rs = stmt.executeQuery();
			while ( rs.next() ) {
				ts = rs.getTimestamp(1);
				break;
			}
		} finally {
			if ( rs != null ) {
				rs.close();
			}
			if ( stmt != null ) {
				stmt.close();
			}
			if ( conn != null ) {
				conn.close();
			}
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS'Z'");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		return new PingTestResult(ts != null, ts != null ? sdf.format(ts) : "No timestamp available.");
	}

	/**
	 * Get the ping test SQL query.
	 * 
	 * @return the SQL query
	 */
	public String getQuery() {
		return query;
	}

}
