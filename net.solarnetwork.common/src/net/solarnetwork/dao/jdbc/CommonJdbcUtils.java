/* ==================================================================
 * CommonJdbcUtils.java - 11 Nov 2026 10:58:56 am
 *
 * Copyright 2026 SolarNetwork.net Dev Team
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

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import net.solarnetwork.codec.jackson.JsonUtils;
import net.solarnetwork.dao.BasicFilterResults;
import net.solarnetwork.dao.DateRangeCriteria;
import net.solarnetwork.dao.FilterResults;
import net.solarnetwork.dao.OptimizedQueryCriteria;
import net.solarnetwork.dao.PaginationCriteria;
import net.solarnetwork.domain.CodedValue;
import net.solarnetwork.domain.Unique;
import net.solarnetwork.util.ObjectUtils;

/**
 * Common JDBC utilities for SolarNetwork.
 *
 * @author matt
 * @version 1.0
 * @since 4.60
 */
public final class CommonJdbcUtils {

	private CommonJdbcUtils() {
		// not available
	}

	/**
	 * Create a {@link PreparedStatement} with settings appropriate for updating
	 * or not.
	 *
	 * @param con
	 *        the connection
	 * @param sql
	 *        the SQL statement
	 * @param forUpdate
	 *        {@literal true} to use a scroll-insensitive, updatable
	 *        {@link ResultSet}, or {@literal false} for a forward-only,
	 *        read-only one
	 * @return the prepared statement
	 * @throws SQLException
	 *         if any SQL error occurs
	 */
	public static PreparedStatement createPreparedStatement(Connection con, String sql,
			boolean forUpdate) throws SQLException {
		PreparedStatement stmt;
		if ( forUpdate ) {
			stmt = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_UPDATABLE);
		} else {
			stmt = con.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY,
					ResultSet.CLOSE_CURSORS_AT_COMMIT);
		}
		return stmt;
	}

	/**
	 * Prepare a SQL statement {@code INTEGER} array parameter, optimized to a
	 * non-array parameter if the array holds a single object.
	 *
	 * <p>
	 * The parameter will <b>not</b> be set if {@code value} is {@code null}.
	 * </p>
	 *
	 * @param con
	 *        the JDBC connection
	 * @param stmt
	 *        the JDBC statement
	 * @param parameterOffset
	 *        the zero-based starting JDBC statement parameter offset
	 * @param value
	 *        the array value
	 * @return the new JDBC statement parameter offset
	 * @throws SQLException
	 *         if any SQL error occurs
	 */
	public static int prepareOptimizedArrayParameter(Connection con, PreparedStatement stmt,
			int parameterOffset, Integer @Nullable [] value) throws SQLException {
		return prepareOptimizedArrayParameter(con, stmt, parameterOffset, "integer", value);
	}

	/**
	 * Prepare a SQL statement {@code BIGINT} array parameter, optimized to a
	 * non-array parameter if the array holds a single object.
	 *
	 * <p>
	 * The parameter will <b>not</b> be set if {@code value} is {@code null}.
	 * </p>
	 *
	 * @param con
	 *        the JDBC connection
	 * @param stmt
	 *        the JDBC statement
	 * @param parameterOffset
	 *        the zero-based starting JDBC statement parameter offset
	 * @param value
	 *        the array value
	 * @return the new JDBC statement parameter offset
	 * @throws SQLException
	 *         if any SQL error occurs
	 */
	public static int prepareOptimizedArrayParameter(Connection con, PreparedStatement stmt,
			int parameterOffset, Long @Nullable [] value) throws SQLException {
		return prepareOptimizedArrayParameter(con, stmt, parameterOffset, "bigint", value);
	}

	/**
	 * Prepare a SQL statement {@code BIGINT} array parameter, optimized to a
	 * non-array parameter if the array holds a single object.
	 *
	 * @param con
	 *        the JDBC connection
	 * @param stmt
	 *        the JDBC statement
	 * @param parameterOffset
	 *        the zero-based starting JDBC statement parameter offset
	 * @param value
	 *        the array value
	 * @param setNull
	 *        {@literal true} to set a NULL parameter if {@code value} is
	 *        {@code null}, or {@literal false} to skip the parameter
	 * @return the new JDBC statement parameter offset
	 * @throws SQLException
	 *         if any SQL error occurs
	 */
	public static int prepareOptimizedArrayParameter(Connection con, PreparedStatement stmt,
			int parameterOffset, Long @Nullable [] value, boolean setNull) throws SQLException {
		return prepareOptimizedArrayParameter(con, stmt, parameterOffset, "bigint", value, setNull);
	}

	/**
	 * Prepare a SQL statement {@code TEXT} array parameter, optimized to a
	 * non-array parameter if the array holds a single object.
	 *
	 * <p>
	 * The parameter will <b>not</b> be set if {@code value} is {@code null}.
	 * </p>
	 *
	 * @param con
	 *        the JDBC connection
	 * @param stmt
	 *        the JDBC statement
	 * @param parameterOffset
	 *        the zero-based starting JDBC statement parameter offset
	 * @param value
	 *        the array value
	 * @return the new JDBC statement parameter offset
	 * @throws SQLException
	 *         if any SQL error occurs
	 */
	public static int prepareOptimizedArrayParameter(Connection con, PreparedStatement stmt,
			int parameterOffset, String @Nullable [] value) throws SQLException {
		return prepareOptimizedArrayParameter(con, stmt, parameterOffset, "text", value);
	}

	/**
	 * Prepare a SQL statement {@code TEXT} array parameter, optimized to a
	 * non-array parameter if the array holds a single object.
	 *
	 * @param con
	 *        the JDBC connection
	 * @param stmt
	 *        the JDBC statement
	 * @param parameterOffset
	 *        the zero-based starting JDBC statement parameter offset
	 * @param value
	 *        the array value
	 * @param setNull
	 *        {@literal true} to set a NULL parameter if {@code value} is
	 *        {@code null}, or {@literal false} to skip the parameter
	 * @return the new JDBC statement parameter offset
	 * @throws SQLException
	 *         if any SQL error occurs
	 */
	public static int prepareOptimizedArrayParameter(Connection con, PreparedStatement stmt,
			int parameterOffset, String @Nullable [] value, boolean setNull) throws SQLException {
		return prepareOptimizedArrayParameter(con, stmt, parameterOffset, "text", value, setNull);
	}

	/**
	 * Prepare a SQL statement {@code UUID} array parameter, optimized to a
	 * non-array parameter if the array holds a single object.
	 *
	 * <p>
	 * The parameter will <b>not</b> be set if {@code value} is {@code null}.
	 * </p>
	 *
	 * @param con
	 *        the JDBC connection
	 * @param stmt
	 *        the JDBC statement
	 * @param parameterOffset
	 *        the zero-based starting JDBC statement parameter offset
	 * @param value
	 *        the array value
	 * @return the new JDBC statement parameter offset
	 * @throws SQLException
	 *         if any SQL error occurs
	 */
	public static int prepareOptimizedArrayParameter(Connection con, PreparedStatement stmt,
			int parameterOffset, UUID @Nullable [] value) throws SQLException {
		return prepareOptimizedArrayParameter(con, stmt, parameterOffset, "uuid", value);
	}

	/**
	 * Prepare a SQL statement {@code UUID} array parameter, optimized to a
	 * non-array parameter if the array holds a single object.
	 *
	 * @param con
	 *        the JDBC connection
	 * @param stmt
	 *        the JDBC statement
	 * @param parameterOffset
	 *        the zero-based starting JDBC statement parameter offset
	 * @param value
	 *        the array value
	 * @param setNull
	 *        {@literal true} to set a NULL parameter if {@code value} is
	 *        {@code null}, or {@literal false} to skip the parameter
	 * @return the new JDBC statement parameter offset
	 * @throws SQLException
	 *         if any SQL error occurs
	 */
	public static int prepareOptimizedArrayParameter(Connection con, PreparedStatement stmt,
			int parameterOffset, UUID @Nullable [] value, boolean setNull) throws SQLException {
		return prepareOptimizedArrayParameter(con, stmt, parameterOffset, "uuid", value, setNull);
	}

	/**
	 * Prepare a SQL statement array parameter, optimized to a non-array
	 * parameter if the array holds a single object.
	 *
	 * <p>
	 * The parameter will <b>not</b> be set if {@code value} is {@code null}.
	 * </p>
	 *
	 * @param con
	 *        the JDBC connection
	 * @param stmt
	 *        the JDBC statement
	 * @param parameterOffset
	 *        the zero-based starting JDBC statement parameter offset
	 * @param arrayType
	 *        the SQL array type to use
	 * @param value
	 *        the array value
	 * @return the new JDBC statement parameter offset
	 * @throws SQLException
	 *         if any SQL error occurs
	 */
	public static int prepareOptimizedArrayParameter(Connection con, PreparedStatement stmt,
			int parameterOffset, String arrayType, Object @Nullable [] value) throws SQLException {
		return prepareOptimizedArrayParameter(con, stmt, parameterOffset, arrayType, value, false);
	}

	/**
	 * Prepare a SQL statement array parameter, optimized to a non-array
	 * parameter if the array holds a single object.
	 *
	 * @param con
	 *        the JDBC connection
	 * @param stmt
	 *        the JDBC statement
	 * @param parameterOffset
	 *        the zero-based starting JDBC statement parameter offset
	 * @param arrayType
	 *        the SQL array type to use
	 * @param value
	 *        the array value
	 * @param setNull
	 *        {@literal true} to set a NULL parameter if {@code value} is
	 *        {@code null} or empty, or {@literal false} to skip the parameter
	 * @return the new JDBC statement parameter offset
	 * @throws SQLException
	 *         if any SQL error occurs
	 */
	public static int prepareOptimizedArrayParameter(Connection con, PreparedStatement stmt,
			int parameterOffset, String arrayType, Object @Nullable [] value, boolean setNull)
			throws SQLException {
		if ( value != null && value.length > 0 ) {
			if ( value.length > 1 ) {
				Array array = con.createArrayOf(arrayType, value);
				stmt.setArray(++parameterOffset, array);
				array.free();
			} else {
				stmt.setObject(++parameterOffset, value[0]);
			}
		} else if ( setNull ) {
			stmt.setNull(++parameterOffset, Types.ARRAY);
		}
		return parameterOffset;
	}

	/**
	 * Prepare a SQL statement {@code INTEGER} array parameter.
	 *
	 * <p>
	 * The parameter will <b>not</b> be set if {@code value} is {@code null}.
	 * </p>
	 *
	 * @param con
	 *        the JDBC connection
	 * @param stmt
	 *        the JDBC statement
	 * @param parameterOffset
	 *        the zero-based starting JDBC statement parameter offset
	 * @param value
	 *        the array value
	 * @return the new JDBC statement parameter offset
	 * @throws SQLException
	 *         if any SQL error occurs
	 */
	public static int prepareArrayParameter(Connection con, PreparedStatement stmt, int parameterOffset,
			Integer @Nullable [] value) throws SQLException {
		return prepareArrayParameter(con, stmt, parameterOffset, value, false);
	}

	/**
	 * Prepare a SQL statement {@code INTEGER} array parameter.
	 *
	 * @param con
	 *        the JDBC connection
	 * @param stmt
	 *        the JDBC statement
	 * @param parameterOffset
	 *        the zero-based starting JDBC statement parameter offset
	 * @param value
	 *        the array value
	 * @param setNull
	 *        {@literal true} to set a NULL parameter if {@code value} is
	 *        {@code null}, or {@literal false} to skip the parameter
	 * @return the new JDBC statement parameter offset
	 * @throws SQLException
	 *         if any SQL error occurs
	 */
	public static int prepareArrayParameter(Connection con, PreparedStatement stmt, int parameterOffset,
			Integer @Nullable [] value, boolean setNull) throws SQLException {
		return prepareArrayParameter(con, stmt, parameterOffset, "integer", value, setNull);
	}

	/**
	 * Prepare a SQL statement {@code BIGINT} array parameter.
	 *
	 * <p>
	 * The parameter will <b>not</b> be set if {@code value} is {@code null}.
	 * </p>
	 *
	 * @param con
	 *        the JDBC connection
	 * @param stmt
	 *        the JDBC statement
	 * @param parameterOffset
	 *        the zero-based starting JDBC statement parameter offset
	 * @param value
	 *        the array value
	 * @return the new JDBC statement parameter offset
	 * @throws SQLException
	 *         if any SQL error occurs
	 */
	public static int prepareArrayParameter(Connection con, PreparedStatement stmt, int parameterOffset,
			Long @Nullable [] value) throws SQLException {
		return prepareArrayParameter(con, stmt, parameterOffset, value, false);
	}

	/**
	 * Prepare a SQL statement {@code BIGINT} array parameter.
	 *
	 * @param con
	 *        the JDBC connection
	 * @param stmt
	 *        the JDBC statement
	 * @param parameterOffset
	 *        the zero-based starting JDBC statement parameter offset
	 * @param value
	 *        the array value
	 * @param setNull
	 *        {@literal true} to set a NULL parameter if {@code value} is
	 *        {@code null}, or {@literal false} to skip the parameter
	 * @return the new JDBC statement parameter offset
	 * @throws SQLException
	 *         if any SQL error occurs
	 */
	public static int prepareArrayParameter(Connection con, PreparedStatement stmt, int parameterOffset,
			Long @Nullable [] value, boolean setNull) throws SQLException {
		return prepareArrayParameter(con, stmt, parameterOffset, "bigint", value, setNull);
	}

	/**
	 * Prepare a SQL statement {@code TEXT} array parameter.
	 *
	 * <p>
	 * The parameter will <b>not</b> be set if {@code value} is {@code null}.
	 * </p>
	 *
	 * @param con
	 *        the JDBC connection
	 * @param stmt
	 *        the JDBC statement
	 * @param parameterOffset
	 *        the zero-based starting JDBC statement parameter offset
	 * @param value
	 *        the array value
	 * @return the new JDBC statement parameter offset
	 * @throws SQLException
	 *         if any SQL error occurs
	 */
	public static int prepareArrayParameter(Connection con, PreparedStatement stmt, int parameterOffset,
			@Nullable String @Nullable [] value) throws SQLException {
		return prepareArrayParameter(con, stmt, parameterOffset, "text", value);
	}

	/**
	 * Prepare a SQL statement {@code TEXT} array parameter.
	 *
	 * @param con
	 *        the JDBC connection
	 * @param stmt
	 *        the JDBC statement
	 * @param parameterOffset
	 *        the zero-based starting JDBC statement parameter offset
	 * @param value
	 *        the array value
	 * @param setNull
	 *        {@literal true} to set a NULL parameter if {@code value} is
	 *        {@code null}, or {@literal false} to skip the parameter
	 * @return the new JDBC statement parameter offset
	 * @throws SQLException
	 *         if any SQL error occurs
	 */
	public static int prepareArrayParameter(Connection con, PreparedStatement stmt, int parameterOffset,
			@Nullable String @Nullable [] value, boolean setNull) throws SQLException {
		return prepareArrayParameter(con, stmt, parameterOffset, "text", value, setNull);
	}

	/**
	 * Prepare a SQL statement {@code UUID} array parameter.
	 *
	 * <p>
	 * The parameter will <b>not</b> be set if {@code value} is {@code null}.
	 * </p>
	 *
	 * @param con
	 *        the JDBC connection
	 * @param stmt
	 *        the JDBC statement
	 * @param parameterOffset
	 *        the zero-based starting JDBC statement parameter offset
	 * @param value
	 *        the array value
	 * @return the new JDBC statement parameter offset
	 * @throws SQLException
	 *         if any SQL error occurs
	 */
	public static int prepareArrayParameter(Connection con, PreparedStatement stmt, int parameterOffset,
			UUID @Nullable [] value) throws SQLException {
		return prepareArrayParameter(con, stmt, parameterOffset, "uuid", value);
	}

	/**
	 * Prepare a SQL statement {@code UUID} array parameter.
	 *
	 * @param con
	 *        the JDBC connection
	 * @param stmt
	 *        the JDBC statement
	 * @param parameterOffset
	 *        the zero-based starting JDBC statement parameter offset
	 * @param value
	 *        the array value
	 * @param setNull
	 *        {@literal true} to set a NULL parameter if {@code value} is
	 *        {@code null}, or {@literal false} to skip the parameter
	 * @return the new JDBC statement parameter offset
	 * @throws SQLException
	 *         if any SQL error occurs
	 */
	public static int prepareArrayParameter(Connection con, PreparedStatement stmt, int parameterOffset,
			UUID @Nullable [] value, boolean setNull) throws SQLException {
		return prepareArrayParameter(con, stmt, parameterOffset, "uuid", value, setNull);
	}

	/**
	 * Prepare a SQL statement array parameter.
	 *
	 * <p>
	 * The parameter will <b>not</b> be set if {@code value} is {@code null}.
	 * </p>
	 *
	 * @param con
	 *        the JDBC connection
	 * @param stmt
	 *        the JDBC statement
	 * @param parameterOffset
	 *        the zero-based starting JDBC statement parameter offset
	 * @param arrayType
	 *        the SQL array type to use
	 * @param value
	 *        the array value
	 * @return the new JDBC statement parameter offset
	 * @throws SQLException
	 *         if any SQL error occurs
	 */
	public static int prepareArrayParameter(Connection con, PreparedStatement stmt, int parameterOffset,
			String arrayType, @Nullable Object @Nullable [] value) throws SQLException {
		return prepareArrayParameter(con, stmt, parameterOffset, arrayType, value, false);
	}

	/**
	 * Prepare a SQL statement {@code BigDecimal} array parameter.
	 *
	 * <p>
	 * The parameter will <b>not</b> be set if {@code value} is {@code null}.
	 * </p>
	 *
	 * @param con
	 *        the JDBC connection
	 * @param stmt
	 *        the JDBC statement
	 * @param parameterOffset
	 *        the zero-based starting JDBC statement parameter offset
	 * @param value
	 *        the array value
	 * @return the new JDBC statement parameter offset
	 * @throws SQLException
	 *         if any SQL error occurs
	 */
	public static int prepareArrayParameter(Connection con, PreparedStatement stmt, int parameterOffset,
			@Nullable BigDecimal @Nullable [] value) throws SQLException {
		return prepareArrayParameter(con, stmt, parameterOffset, "NUMERIC", value);
	}

	/**
	 * Prepare a SQL statement {@code BigDecimal} array parameter.
	 *
	 * @param con
	 *        the JDBC connection
	 * @param stmt
	 *        the JDBC statement
	 * @param parameterOffset
	 *        the zero-based starting JDBC statement parameter offset
	 * @param value
	 *        the array value
	 * @param setNull
	 *        {@literal true} to set a NULL parameter if {@code value} is
	 *        {@code null}, or {@literal false} to skip the parameter
	 * @return the new JDBC statement parameter offset
	 * @throws SQLException
	 *         if any SQL error occurs
	 */
	public static int prepareArrayParameter(Connection con, PreparedStatement stmt, int parameterOffset,
			@Nullable BigDecimal @Nullable [] value, boolean setNull) throws SQLException {
		return prepareArrayParameter(con, stmt, parameterOffset, "NUMERIC", value, setNull);
	}

	/**
	 * Prepare a SQL statement array parameter.
	 *
	 * @param con
	 *        the JDBC connection
	 * @param stmt
	 *        the JDBC statement
	 * @param parameterOffset
	 *        the zero-based starting JDBC statement parameter offset
	 * @param arrayType
	 *        the SQL array type to use
	 * @param value
	 *        the array value
	 * @param setNull
	 *        {@literal true} to set a NULL parameter if {@code value} is
	 *        {@code null}, or {@literal false} to skip the parameter
	 * @return the new JDBC statement parameter offset
	 * @throws SQLException
	 *         if any SQL error occurs
	 */
	public static int prepareArrayParameter(Connection con, PreparedStatement stmt, int parameterOffset,
			String arrayType, @Nullable Object @Nullable [] value, boolean setNull) throws SQLException {
		if ( value != null ) {
			Array array = con.createArrayOf(arrayType, value);
			stmt.setArray(++parameterOffset, array);
			array.free();
		} else if ( setNull ) {
			stmt.setNull(++parameterOffset, Types.ARRAY);
		}
		return parameterOffset;
	}

	/**
	 * Prepare a SQL query limit/offset.
	 *
	 * @param filter
	 *        the search criteria
	 * @param stmt
	 *        the JDBC statement
	 * @param parameterOffset
	 *        the zero-based starting JDBC statement parameter offset
	 * @return the new JDBC statement parameter offset
	 * @throws SQLException
	 *         if any SQL error occurs
	 * @see CommonSqlUtils#limitOffset(PaginationCriteria, StringBuilder)
	 */
	public static int prepareLimitOffset(@Nullable PaginationCriteria filter, PreparedStatement stmt,
			int parameterOffset) throws SQLException {
		if ( filter != null && filter.getMax() != null ) {
			int max = filter.getMax();
			if ( max > 0 ) {
				stmt.setInt(++parameterOffset, max);
			}
		}
		if ( filter != null && filter.getOffset() != null ) {
			long offset = filter.getOffset();
			if ( offset > 0 ) {
				stmt.setLong(++parameterOffset, offset);
			}
		}
		return parameterOffset;
	}

	/**
	 * Prepare a SQL query date range filter.
	 *
	 * @param filter
	 *        the search criteria
	 * @param stmt
	 *        the JDBC statement
	 * @param parameterOffset
	 *        the zero-based starting JDBC statement parameter offset
	 * @return the new JDBC statement parameter offset
	 * @throws SQLException
	 *         if any SQL error occurs
	 */
	public static int prepareDateRange(DateRangeCriteria filter, PreparedStatement stmt,
			int parameterOffset) throws SQLException {
		if ( filter.getStartDate() != null ) {
			stmt.setTimestamp(++parameterOffset, Timestamp.from(filter.getStartDate()));
		}
		if ( filter.getEndDate() != null ) {
			stmt.setTimestamp(++parameterOffset, Timestamp.from(filter.getEndDate()));
		}
		return parameterOffset;
	}

	/**
	 * Prepare a SQL query JSON string parameter.
	 *
	 * @param data
	 *        the JSON data; will be serialized via
	 *        {@link JsonUtils#getJSONString(Object, String)}
	 * @param stmt
	 *        the JDBC statement
	 * @param parameterOffset
	 *        the zero-based starting JDBC statement parameter offset
	 * @param setNull
	 *        {@literal true} to set a NULL parameter if {@code data} serializes
	 *        to {@code null}, or {@literal false} to skip the parameter
	 * @return the new JDBC statement parameter offset
	 * @throws SQLException
	 *         if any SQL error occurs
	 */
	public static int prepareJsonString(@Nullable Object data, PreparedStatement stmt,
			int parameterOffset, boolean setNull) throws SQLException {
		String props = JsonUtils.getJSONString(data, null);
		if ( props != null ) {
			stmt.setString(++parameterOffset, props);
		} else if ( setNull ) {
			stmt.setNull(++parameterOffset, Types.VARCHAR);
		}
		return parameterOffset;
	}

	/**
	 * Prepare a SQL query {@code CodedValue} parameter as a {@code INTEGER}.
	 *
	 * @param stmt
	 *        the JDBC statement
	 * @param parameterOffset
	 *        the zero-based starting JDBC statement parameter offset
	 * @param value
	 *        the value to set
	 * @param defaultValue
	 *        the value to set if {@code value} is {@code null}
	 * @param setNull
	 *        {@literal true} to set a NULL parameter if {@code value} is
	 *        {@code null} and {@literal defaultValue} is {@code null}, or
	 *        {@literal false} to skip the parameter
	 * @return the new JDBC statement parameter offset
	 * @throws SQLException
	 *         if any SQL error occurs
	 */
	public static int prepareCodedValue(PreparedStatement stmt, int parameterOffset,
			@Nullable CodedValue value, @Nullable CodedValue defaultValue, boolean setNull)
			throws SQLException {
		if ( value != null ) {
			stmt.setInt(++parameterOffset, value.getCode());
		} else if ( defaultValue != null ) {
			stmt.setInt(++parameterOffset, defaultValue.getCode());
		} else if ( setNull ) {
			stmt.setNull(++parameterOffset, Types.INTEGER);
		}
		return parameterOffset;
	}

	/**
	 * Prepare a SQL query {@code CodedValue} parameter as a {@code CHARACTER}.
	 *
	 * @param stmt
	 *        the JDBC statement
	 * @param parameterOffset
	 *        the zero-based starting JDBC statement parameter offset
	 * @param value
	 *        the value to set
	 * @param defaultValue
	 *        the value to set if {@code value} is {@code null}
	 * @param setNull
	 *        {@literal true} to set a NULL parameter if {@code value} is
	 *        {@code null} and {@literal defaultValue} is {@code null}, or
	 *        {@literal false} to skip the parameter
	 * @return the new JDBC statement parameter offset
	 * @throws SQLException
	 *         if any SQL error occurs
	 */
	public static int prepareCodedValueChar(PreparedStatement stmt, int parameterOffset,
			@Nullable CodedValue value, @Nullable CodedValue defaultValue, boolean setNull)
			throws SQLException {
		if ( value != null ) {
			stmt.setObject(++parameterOffset, (char) value.getCode());
		} else if ( defaultValue != null ) {
			stmt.setObject(++parameterOffset, (char) defaultValue.getCode());
		} else if ( setNull ) {
			stmt.setNull(++parameterOffset, Types.CHAR);
		}
		return parameterOffset;
	}

	/**
	 * Prepare a SQL query {@code CodedValue} parameter as a {@code INTEGER}.
	 *
	 * @param stmt
	 *        the JDBC statement
	 * @param parameterOffset
	 *        the zero-based starting JDBC statement parameter offset
	 * @param values
	 *        the values to set
	 * @param setNull
	 *        {@literal true} to set a NULL parameter if {@code values} is
	 *        {@code null} and {@literal defaultValue} is {@code null}, or
	 *        {@literal false} to skip the parameter
	 * @return the new JDBC statement parameter offset
	 * @throws SQLException
	 *         if any SQL error occurs
	 */
	public static int prepareCodedValuesArray(Connection con, PreparedStatement stmt,
			int parameterOffset, @Nullable Collection<? extends CodedValue> values, boolean setNull)
			throws SQLException {
		Integer[] codes = null;
		if ( values != null ) {
			codes = values.stream().map(CodedValue::getCode).toArray(Integer[]::new);
		}
		return prepareArrayParameter(con, stmt, parameterOffset, codes, setNull);
	}

	/**
	 * Prepare a SQL query parameter.
	 *
	 * @param stmt
	 *        the JDBC statement
	 * @param parameterOffset
	 *        the zero-based starting JDBC statement parameter offset
	 * @param sqlType
	 *        the SQL type, as in {@link java.sql.Types}
	 * @param value
	 *        the object to set
	 * @param setNull
	 *        {@literal true} to set a NULL parameter if {@code value} is
	 *        {@code null}, or {@literal false} to skip the parameter
	 * @return the new JDBC statement parameter offset
	 * @throws SQLException
	 *         if any SQL error occurs
	 */
	public static int prepareObjectParameter(PreparedStatement stmt, int parameterOffset, int sqlType,
			@Nullable Object value, boolean setNull) throws SQLException {
		if ( value != null ) {
			stmt.setObject(++parameterOffset, value, sqlType);
		} else if ( setNull ) {
			stmt.setNull(++parameterOffset, sqlType);
		}
		return parameterOffset;
	}

	/**
	 * Prepare a SQL query string parameter.
	 *
	 * <p>
	 * The SQL type {@link Types#VARCHAR} will be used.
	 * </p>
	 *
	 * @param stmt
	 *        the JDBC statement
	 * @param parameterOffset
	 *        the zero-based starting JDBC statement parameter offset
	 * @param value
	 *        the value to set
	 * @param setNull
	 *        {@literal true} to set a NULL parameter if {@code value} is
	 *        {@code null}, or {@literal false} to skip the parameter
	 * @return the new JDBC statement parameter offset
	 * @throws SQLException
	 *         if any SQL error occurs
	 */
	public static int prepareParameter(PreparedStatement stmt, int parameterOffset,
			@Nullable String value, boolean setNull) throws SQLException {
		return prepareObjectParameter(stmt, parameterOffset, Types.VARCHAR, value, setNull);
	}

	/**
	 * Prepare a SQL query string parameter.
	 *
	 * <p>
	 * The SQL type {@link Types#VARCHAR} will be used.
	 * </p>
	 *
	 * <p>
	 * The parameter will <b>not</b> be set if {@code value} is {@code null}.
	 * </p>
	 *
	 * @param stmt
	 *        the JDBC statement
	 * @param parameterOffset
	 *        the zero-based starting JDBC statement parameter offset
	 * @param value
	 *        the value to set
	 * @return the new JDBC statement parameter offset
	 * @throws SQLException
	 *         if any SQL error occurs
	 */
	public static int prepareParameter(PreparedStatement stmt, int parameterOffset,
			@Nullable String value) throws SQLException {
		return prepareParameter(stmt, parameterOffset, value, false);
	}

	/**
	 * Prepare a SQL query boolean parameter.
	 *
	 * <p>
	 * The SQL type {@link Types#BOOLEAN} will be used.
	 * </p>
	 *
	 * @param stmt
	 *        the JDBC statement
	 * @param parameterOffset
	 *        the zero-based starting JDBC statement parameter offset
	 * @param value
	 *        the value to set
	 * @param setNull
	 *        {@literal true} to set a NULL parameter if {@code value} is
	 *        {@code null}, or {@literal false} to skip the parameter
	 * @return the new JDBC statement parameter offset
	 * @throws SQLException
	 *         if any SQL error occurs
	 */
	public static int prepareParameter(PreparedStatement stmt, int parameterOffset,
			@Nullable Boolean value, boolean setNull) throws SQLException {
		return prepareObjectParameter(stmt, parameterOffset, Types.BOOLEAN, value, setNull);
	}

	/**
	 * Prepare a SQL query boolean parameter.
	 *
	 * <p>
	 * The SQL type {@link Types#BOOLEAN} will be used.
	 * </p>
	 *
	 * <p>
	 * The parameter will <b>not</b> be set if {@code value} is {@code null}.
	 * </p>
	 *
	 * @param stmt
	 *        the JDBC statement
	 * @param parameterOffset
	 *        the zero-based starting JDBC statement parameter offset
	 * @param value
	 *        the value to set
	 * @return the new JDBC statement parameter offset
	 * @throws SQLException
	 *         if any SQL error occurs
	 */
	public static int prepareParameter(PreparedStatement stmt, int parameterOffset,
			@Nullable Boolean value) throws SQLException {
		return prepareParameter(stmt, parameterOffset, value, false);
	}

	/**
	 * Prepare a SQL query integer parameter.
	 *
	 * <p>
	 * The SQL type {@link Types#INTEGER} will be used.
	 * </p>
	 *
	 * @param stmt
	 *        the JDBC statement
	 * @param parameterOffset
	 *        the zero-based starting JDBC statement parameter offset
	 * @param value
	 *        the value to set
	 * @param setNull
	 *        {@literal true} to set a NULL parameter if {@code value} is
	 *        {@code null}, or {@literal false} to skip the parameter
	 * @return the new JDBC statement parameter offset
	 * @throws SQLException
	 *         if any SQL error occurs
	 */
	public static int prepareParameter(PreparedStatement stmt, int parameterOffset,
			@Nullable Integer value, boolean setNull) throws SQLException {
		return prepareObjectParameter(stmt, parameterOffset, Types.INTEGER, value, setNull);
	}

	/**
	 * Prepare a SQL query integer parameter.
	 *
	 * <p>
	 * The SQL type {@link Types#INTEGER} will be used.
	 * </p>
	 *
	 * <p>
	 * The parameter will <b>not</b> be set if {@code value} is {@code null}.
	 * </p>
	 *
	 * @param stmt
	 *        the JDBC statement
	 * @param parameterOffset
	 *        the zero-based starting JDBC statement parameter offset
	 * @param value
	 *        the value to set
	 * @return the new JDBC statement parameter offset
	 * @throws SQLException
	 *         if any SQL error occurs
	 */
	public static int prepareParameter(PreparedStatement stmt, int parameterOffset,
			@Nullable Integer value) throws SQLException {
		return prepareParameter(stmt, parameterOffset, value, false);
	}

	/**
	 * Prepare a SQL query long parameter.
	 *
	 * <p>
	 * The SQL type {@link Types#BIGINT} will be used.
	 * </p>
	 *
	 * @param stmt
	 *        the JDBC statement
	 * @param parameterOffset
	 *        the zero-based starting JDBC statement parameter offset
	 * @param value
	 *        the value to set
	 * @param setNull
	 *        {@literal true} to set a NULL parameter if {@code value} is
	 *        {@code null}, or {@literal false} to skip the parameter
	 * @return the new JDBC statement parameter offset
	 * @throws SQLException
	 *         if any SQL error occurs
	 */
	public static int prepareParameter(PreparedStatement stmt, int parameterOffset, @Nullable Long value,
			boolean setNull) throws SQLException {
		return prepareObjectParameter(stmt, parameterOffset, Types.BIGINT, value, setNull);
	}

	/**
	 * Prepare a SQL query long parameter.
	 *
	 * <p>
	 * The SQL type {@link Types#BIGINT} will be used.
	 * </p>
	 *
	 * <p>
	 * The parameter will <b>not</b> be set if {@code value} is {@code null}.
	 * </p>
	 *
	 * @param stmt
	 *        the JDBC statement
	 * @param parameterOffset
	 *        the zero-based starting JDBC statement parameter offset
	 * @param value
	 *        the value to set
	 * @return the new JDBC statement parameter offset
	 * @throws SQLException
	 *         if any SQL error occurs
	 */
	public static int prepareParameter(PreparedStatement stmt, int parameterOffset, @Nullable Long value)
			throws SQLException {
		return prepareParameter(stmt, parameterOffset, value, false);
	}

	/**
	 * Prepare a SQL query UUID parameter.
	 *
	 * <p>
	 * The SQL type {@link Types#OTHER} will be used.
	 * </p>
	 *
	 * @param stmt
	 *        the JDBC statement
	 * @param parameterOffset
	 *        the zero-based starting JDBC statement parameter offset
	 * @param value
	 *        the value to set
	 * @param setNull
	 *        {@literal true} to set a NULL parameter if {@code value} is
	 *        {@code null}, or {@literal false} to skip the parameter
	 * @return the new JDBC statement parameter offset
	 * @throws SQLException
	 *         if any SQL error occurs
	 */
	public static int prepareParameter(PreparedStatement stmt, int parameterOffset, @Nullable UUID value,
			boolean setNull) throws SQLException {
		return prepareObjectParameter(stmt, parameterOffset, Types.OTHER, value, setNull);
	}

	/**
	 * Prepare a SQL query UUID parameter.
	 *
	 * <p>
	 * The SQL type {@link Types#OTHER} will be used.
	 * </p>
	 *
	 * <p>
	 * The parameter will <b>not</b> be set if {@code value} is {@code null}.
	 * </p>
	 *
	 * @param stmt
	 *        the JDBC statement
	 * @param parameterOffset
	 *        the zero-based starting JDBC statement parameter offset
	 * @param value
	 *        the value to set
	 * @return the new JDBC statement parameter offset
	 * @throws SQLException
	 *         if any SQL error occurs
	 */
	public static int prepareParameter(PreparedStatement stmt, int parameterOffset, @Nullable UUID value)
			throws SQLException {
		return prepareParameter(stmt, parameterOffset, value, false);
	}

	/**
	 * Prepare a SQL query decimal parameter.
	 *
	 * <p>
	 * The SQL type {@link Types#NUMERIC} will be used.
	 * </p>
	 *
	 * @param stmt
	 *        the JDBC statement
	 * @param parameterOffset
	 *        the zero-based starting JDBC statement parameter offset
	 * @param value
	 *        the value to set
	 * @param setNull
	 *        {@literal true} to set a NULL parameter if {@code value} is
	 *        {@code null}, or {@literal false} to skip the parameter
	 * @return the new JDBC statement parameter offset
	 * @throws SQLException
	 *         if any SQL error occurs
	 */
	public static int prepareParameter(PreparedStatement stmt, int parameterOffset,
			@Nullable BigDecimal value, boolean setNull) throws SQLException {
		return prepareObjectParameter(stmt, parameterOffset, Types.NUMERIC, value, setNull);
	}

	/**
	 * Prepare a SQL query decimal parameter.
	 *
	 * <p>
	 * The SQL type {@link Types#NUMERIC} will be used.
	 * </p>
	 *
	 * <p>
	 * The parameter will <b>not</b> be set if {@code value} is {@code null}.
	 * </p>
	 *
	 * @param stmt
	 *        the JDBC statement
	 * @param parameterOffset
	 *        the zero-based starting JDBC statement parameter offset
	 * @param value
	 *        the value to set
	 * @return the new JDBC statement parameter offset
	 * @throws SQLException
	 *         if any SQL error occurs
	 */
	public static int prepareParameter(PreparedStatement stmt, int parameterOffset,
			@Nullable BigDecimal value) throws SQLException {
		return prepareParameter(stmt, parameterOffset, value, false);
	}

	/**
	 * Prepare a SQL query LIKE substring match parameter.
	 *
	 * <p>
	 * The SQL type {@link Types#VARCHAR} will be used.
	 * </p>
	 *
	 * @param stmt
	 *        the JDBC statement
	 * @param parameterOffset
	 *        the zero-based starting JDBC statement parameter offset
	 * @param value
	 *        the value to set; if non-empty then it will be wrapped with
	 *        {@code "%"} characters for a full substring match
	 * @param setNull
	 *        {@literal true} to set a NULL parameter if {@code value} is
	 *        {@code null}, or {@literal false} to skip the parameter
	 * @return the new JDBC statement parameter offset
	 * @throws SQLException
	 *         if any SQL error occurs
	 */
	public static int prepareLikeSubstringParameter(PreparedStatement stmt, int parameterOffset,
			@Nullable String value, boolean setNull) throws SQLException {
		if ( value != null && !value.isEmpty() ) {
			value = '%' + CommonSqlUtils.safeLikeValue(value) + '%';
		}
		return prepareObjectParameter(stmt, parameterOffset, Types.VARCHAR, value, setNull);
	}

	/**
	 * Prepare a SQL query LIKE substring match parameter.
	 *
	 * <p>
	 * The SQL type {@link Types#VARCHAR} will be used.
	 * </p>
	 *
	 * <p>
	 * The parameter will <b>not</b> be set if {@code value} is {@code null}.
	 * </p>
	 *
	 * @param stmt
	 *        the JDBC statement
	 * @param parameterOffset
	 *        the zero-based starting JDBC statement parameter offset
	 * @param value
	 *        the value to set; if non-empty then it will be wrapped with
	 *        {@code "%"} characters for a full substring match
	 * @return the new JDBC statement parameter offset
	 * @throws SQLException
	 *         if any SQL error occurs
	 */
	public static int prepareLikeSubstringParameter(PreparedStatement stmt, int parameterOffset,
			@Nullable String value) throws SQLException {
		return prepareLikeSubstringParameter(stmt, parameterOffset, value, false);
	}

	/**
	 * Prepare a SQL query LIKE substring match array parameter.
	 *
	 * @param stmt
	 *        the JDBC statement
	 * @param parameterOffset
	 *        the zero-based starting JDBC statement parameter offset
	 * @param value
	 *        the array value
	 * @param setNull
	 *        {@literal true} to set a NULL parameter if {@code value} is
	 *        {@code null}, or {@literal false} to skip the parameter
	 * @return the new JDBC statement parameter offset
	 * @throws SQLException
	 *         if any SQL error occurs
	 */
	public static int prepareLikeSubstringParameter(PreparedStatement stmt, int parameterOffset,
			@Nullable String @Nullable [] value, boolean setNull) throws SQLException {
		@Nullable
		String[] newValue = value;
		if ( value != null ) {
			newValue = new String[value.length];
			for ( int i = 0; i < newValue.length; i++ ) {
				if ( value[i] == null ) {
					continue;
				}
				newValue[i] = '%' + CommonSqlUtils.safeLikeValue(value[i]) + '%';
			}
		}
		return prepareArrayParameter(stmt.getConnection(), stmt, parameterOffset, "text", newValue,
				setNull);
	}

	/**
	 * Prepare a SQL query LIKE substring match array parameter.
	 *
	 * <p>
	 * The parameter will <b>not</b> be set if {@code value} is {@code null}.
	 * </p>
	 *
	 * @param stmt
	 *        the JDBC statement
	 * @param parameterOffset
	 *        the zero-based starting JDBC statement parameter offset
	 * @param value
	 *        the array value
	 * @return the new JDBC statement parameter offset
	 * @throws SQLException
	 *         if any SQL error occurs
	 */
	public static int prepareLikeSubstringParameter(PreparedStatement stmt, int parameterOffset,
			@Nullable String @Nullable [] value) throws SQLException {
		return prepareLikeSubstringParameter(stmt, parameterOffset, value, false);
	}

	/**
	 * Prepare a SQL query LIKE substring match array parameter.
	 *
	 * @param stmt
	 *        the JDBC statement
	 * @param parameterOffset
	 *        the zero-based starting JDBC statement parameter offset
	 * @param value
	 *        the array value
	 * @param setNull
	 *        {@literal true} to set a NULL parameter if {@code value} is
	 *        {@code null}, or {@literal false} to skip the parameter
	 * @return the new JDBC statement parameter offset
	 * @throws SQLException
	 *         if any SQL error occurs
	 * @see CommonSqlUtils#whereOptimizedLike(Object[], String, StringBuilder)
	 */
	public static int prepareOptimizedLikeSubstringParameter(PreparedStatement stmt, int parameterOffset,
			@Nullable String @Nullable [] value, boolean setNull) throws SQLException {
		if ( value != null && value.length == 1 ) {
			return prepareLikeSubstringParameter(stmt, parameterOffset, value[0], setNull);
		}
		return prepareLikeSubstringParameter(stmt, parameterOffset, value, setNull);
	}

	/**
	 * Prepare an optimized SQL query LIKE substring match array parameter.
	 *
	 * <p>
	 * The parameter will <b>not</b> be set if {@code value} is {@code null}.
	 * </p>
	 *
	 * @param stmt
	 *        the JDBC statement
	 * @param parameterOffset
	 *        the zero-based starting JDBC statement parameter offset
	 * @param value
	 *        the array value
	 * @return the new JDBC statement parameter offset
	 * @throws SQLException
	 *         if any SQL error occurs
	 * @see CommonSqlUtils#whereOptimizedLike(Object[], String, StringBuilder)
	 */
	public static int prepareOptimizedLikeSubstringParameter(PreparedStatement stmt, int parameterOffset,
			@Nullable String @Nullable [] value) throws SQLException {
		return prepareOptimizedLikeSubstringParameter(stmt, parameterOffset, value, false);
	}

	/**
	 * Get an array result column value.
	 *
	 * <p>
	 * For {@link BigDecimal} values specially handling is included for
	 * non-finite values returned from the database. If an exception is thrown
	 * by {@link Array#getArray()} and the {@link Array#getBaseType()} is
	 * {@code NUMERIC} or {@code DECIMAL}, then special processing happens to
	 * try to extract 1- or 2-dimensional arrays of {@link BigDecimal}, by
	 * iterating over the {@link Array#getResultSet()}. If an individual
	 * {@code ResultSet} row value throws an exception when attempting to read
	 * the value as a {@link BigDecimal}, it will be inspected as a nested array
	 * value, and processed as a nested array. If the row value is not an array,
	 * it is inspected as a string value, and if equal to {@code "Infinity"} or
	 * {@code "-Infinity"} or {@code "NaN"} then {@code null} will be used
	 * instead, and processing will continue.
	 * </p>
	 *
	 * @param <T>
	 *        the expected array type
	 * @param rs
	 *        the result set
	 * @param colNum
	 *        the column number
	 * @return the array
	 * @throws SQLException
	 *         if any SQL error occurs
	 * @throws ClassCastException
	 *         if a casting error occurs
	 */
	@SuppressWarnings({ "unchecked", "TypeParameterUnusedInFormals" })
	public static <T> @Nullable T getArray(ResultSet rs, int colNum) throws SQLException {
		Array a = rs.getArray(colNum);
		if ( a == null ) {
			return null;
		}
		try {
			return (T) a.getArray();
		} catch ( SQLException e ) {
			try {
				int type = a.getBaseType();
				if ( type == Types.NUMERIC || type == Types.DECIMAL ) {
					// if failed reading as BigDecimal, assume we have non-finite values and try to re-map those into null
					List<Object> result = parseBigDecimalArray(a);
					boolean twoDee = false;
					for ( Object o : result ) {
						if ( o != null ) {
							if ( o.getClass().isArray() ) {
								twoDee = true;
							}
							break;
						}
					}
					return (T) result.toArray(twoDee ? BigDecimal[][]::new : BigDecimal[]::new);
				}
			} catch ( SQLException e2 ) {
				// give up
			}
			throw e;
		}
	}

	/**
	 * Get an array result column value.
	 *
	 * @param <T>
	 *        the expected array type
	 * @param rs
	 *        the result set
	 * @param colNum
	 *        the column number
	 * @return the array
	 * @throws SQLException
	 *         if any SQL error occurs
	 * @throws ClassCastException
	 *         if a casting error occurs
	 * @see #getArray(ResultSet, int)
	 */
	@SuppressWarnings({ "NullAway", "TypeParameterUnusedInFormals" })
	public static <T> T array(ResultSet rs, int colNum) throws SQLException {
		return getArray(rs, colNum);
	}

	private static List<Object> parseBigDecimalArray(Array a) throws SQLException {
		List<Object> result = new ArrayList<>();

		try (ResultSet arrayResultSet = a.getResultSet()) {
			while ( arrayResultSet.next() ) {
				int idx = arrayResultSet.getInt(1) - 1;
				while ( result.size() < idx ) {
					result.add(null);
				}
				try {
					result.add(arrayResultSet.getBigDecimal(2));
				} catch ( SQLException e ) {
					// try as 2d-array
					try {
						Array a2 = arrayResultSet.getArray(2);
						List<Object> nested = parseBigDecimalArray(a2);
						result.add(nested.toArray(BigDecimal[]::new));
					} catch ( SQLException e2 ) {
						// try as a string instead
						String s = arrayResultSet.getString(2);
						if ( "Infinity".equals(s) || "-Infinity".equals(s) || "NaN".equals(s) ) {
							result.add(null);
						} else {
							throw e;
						}
					}
				}
			}
		}

		return result;
	}

	/**
	 * Get an array value from a SQL array instance.
	 *
	 * @param <T>
	 *        the expected array type, e.g. {@code Long[].class}
	 * @param o
	 *        the {@link Array} instance
	 * @return the array value, or {@code null} if {@code o} is {@code null} or
	 *         not a {@link Array}
	 * @throws ClassCastException
	 *         if a casting error occurs
	 */
	@SuppressWarnings({ "unchecked", "TypeParameterUnusedInFormals" })
	public static <T> @Nullable T arrayValue(@Nullable Object o) {
		if ( o instanceof Array a ) {
			try {
				return (T) a.getArray();
			} catch ( SQLException e ) {
				// ignore
			}
		}
		return null;
	}

	/**
	 * Get a UUID column value.
	 *
	 * <p>
	 * This method can be more efficient than calling
	 * {@link ResultSet#getString(int)} if the JDBC driver returns a UUID
	 * instance natively. Otherwise, this method will call {@code toString()} on
	 * the column value and parse that as a UUID.
	 * </p>
	 *
	 * @param rs
	 *        the result set to read from
	 * @param column
	 *        the column number to get as a UUID
	 * @return the UUID, or {@code null} if the column value is null
	 * @throws SQLException
	 *         if an error occurs
	 * @throws IllegalArgumentException
	 *         if the column value is non-null but does not conform to the
	 *         string representation as described in {@link UUID#toString()}
	 */
	public static @Nullable UUID getUuid(ResultSet rs, int column) throws SQLException {
		return getUuid(rs, column, null);
	}

	/**
	 * Get a UUID column value.
	 *
	 * @param rs
	 *        the result set to read from
	 * @param column
	 *        the column number to get as a UUID
	 * @return the UUID (presumed null)
	 * @throws SQLException
	 *         if an error occurs
	 * @throws IllegalArgumentException
	 *         if the column value is non-null but does not conform to the
	 *         string representation as described in {@link UUID#toString()}
	 * @see #getUuid(ResultSet, int)
	 */
	@SuppressWarnings("NullAway")
	public static UUID uuid(ResultSet rs, int column) throws SQLException {
		return uuid(rs, column, null);
	}

	/**
	 * Get a UUID column value.
	 *
	 * <p>
	 * This method can be more efficient than calling
	 * {@link ResultSet#getString(int)} if the JDBC driver returns a UUID
	 * instance natively. Otherwise, this method will call {@code toString()} on
	 * the column value and parse that as a UUID.
	 * </p>
	 *
	 * @param rs
	 *        the result set to read from
	 * @param column
	 *        the column number to get as a UUID
	 * @param defaultValue
	 *        the default value to use if the UUID column value is {@code null}
	 * @return the UUID, or {@code defaultValue} if the column value is null
	 * @throws SQLException
	 *         if an error occurs
	 * @throws IllegalArgumentException
	 *         if the column value is non-null but does not conform to the
	 *         string representation as described in {@link UUID#toString()}
	 */
	public static @Nullable UUID getUuid(ResultSet rs, int column, @Nullable UUID defaultValue)
			throws SQLException {
		Object sid = rs.getObject(column);
		return (sid instanceof UUID uuid ? uuid
				: sid != null ? UUID.fromString(sid.toString()) : defaultValue);
	}

	/**
	 * Get a UUID column value.
	 *
	 * @param rs
	 *        the result set to read from
	 * @param column
	 *        the column number to get as a UUID
	 * @param defaultValue
	 *        the default value to use if the UUID column value is {@code null}
	 * @return the UUID, or {@code defaultValue} if the column value is
	 *         {@code null}
	 * @throws SQLException
	 *         if an error occurs
	 * @throws IllegalArgumentException
	 *         if the column value is non-null but does not conform to the
	 *         string representation as described in {@link UUID#toString()}
	 * @see #getUuid(ResultSet, int, UUID)
	 */
	@SuppressWarnings("NullAway")
	public static UUID uuid(ResultSet rs, int column, UUID defaultValue) throws SQLException {
		return getUuid(rs, column, defaultValue);
	}

	/**
	 * Get a UUID statement value.
	 *
	 * @param call
	 *        the call to read from
	 * @param parameterIndex
	 *        the statement parameter to get as a UUID, starting from {@code 1}
	 * @return the UUID, or {@code null}
	 * @throws SQLException
	 *         if an error occurs
	 * @throws IllegalArgumentException
	 *         if the column value is non-null but does not conform to the
	 *         string representation as described in {@link UUID#toString()}
	 */
	public static @Nullable UUID getUuid(CallableStatement call, int parameterIndex)
			throws SQLException {
		return getUuid(call, parameterIndex, null);
	}

	/**
	 * Get a UUID statement value.
	 *
	 * @param call
	 *        the call to read from
	 * @param parameterIndex
	 *        the statement parameter to get as a UUID, starting from {@code 1}
	 * @param defaultValue
	 *        the default value to use of the UUID parameter value is
	 *        {@code null}
	 * @return the UUID, or {@code defaultValue} if the parameter value is
	 *         {@code null}
	 * @throws SQLException
	 *         if an error occurs
	 * @throws IllegalArgumentException
	 *         if the column value is non-null but does not conform to the
	 *         string representation as described in {@link UUID#toString()}
	 */
	public static @Nullable UUID getUuid(CallableStatement call, int parameterIndex,
			@Nullable UUID defaultValue) throws SQLException {
		Object sid = call.getObject(parameterIndex);
		return (sid instanceof UUID uuid ? uuid
				: sid != null ? UUID.fromString(sid.toString()) : defaultValue);
	}

	/**
	 * Execute a query for a count result.
	 *
	 * @param jdbcTemplate
	 *        the JDBC template to use
	 * @param creator
	 *        the statement creator; if implements
	 *        {@link CountPreparedStatementCreatorProvider} then
	 *        {@link CountPreparedStatementCreatorProvider#countPreparedStatementCreator()}
	 *        will be used
	 * @return the result, or {@code null} if no result count is available
	 */
	public static @Nullable Long executeCountQuery(JdbcOperations jdbcTemplate,
			PreparedStatementCreator creator) {
		return jdbcTemplate.query(creator, rs -> rs.next() ? rs.getLong(1) : null);
	}

	/**
	 * Standardized utility to execute a filter based query.
	 *
	 * <p>
	 * If {@code filter} has no maximum result count, the query is assumed to
	 * return all results starting from the filter's offset, so the total result
	 * count is the offset plus the number of returned results. If no results
	 * are returned for a positive offset, the total result count is not known
	 * and will be {@code null}.
	 * </p>
	 *
	 * @param <M>
	 *        the filter result type
	 * @param <K>
	 *        the filter result key type
	 * @param jdbcTemplate
	 *        the JDBC template to use
	 * @param filter
	 *        the pagination criteria
	 * @param sql
	 *        the SQL to execute
	 * @param mapper
	 *        the row mapper to use
	 * @return the results, never {@code null}
	 */
	public static <M extends Unique<K>, K extends Comparable<K>> FilterResults<M, K> executeFilterQuery(
			JdbcOperations jdbcTemplate, PaginationCriteria filter, PreparedStatementCreator sql,
			RowMapper<M> mapper) {
		Long totalCount = null;
		if ( filter.getMax() != null && sql instanceof CountPreparedStatementCreatorProvider c
				&& !(filter instanceof OptimizedQueryCriteria oqc
						&& oqc.isWithoutTotalResultsCount()) ) {
			totalCount = executeCountQuery(jdbcTemplate, c.countPreparedStatementCreator());
		}

		List<M> results = jdbcTemplate.query(sql, mapper);

		long offset = (filter.getOffset() != null ? filter.getOffset() : 0L);
		if ( filter.getMax() == null ) {
			if ( offset < 1 ) {
				totalCount = (long) results.size();
			} else if ( !results.isEmpty() ) {
				totalCount = offset + results.size();
			}
		}

		return new BasicFilterResults<>(results, totalCount, offset, results.size());
	}

	/**
	 * Perform an update operation and extract a generated {@code Long} key.
	 *
	 * @param jdbcTemplate
	 *        the JDBC template to use
	 * @param sql
	 *        the SQL to execute
	 * @param keyColumnName
	 *        the name of the generated key column to extract
	 * @return the generated key value
	 * @throws IllegalStateException
	 *         if the key is not returned returned or is not a {@code Long}
	 *         instance
	 */
	public static Long updateWithGeneratedLong(JdbcOperations jdbcTemplate, PreparedStatementCreator sql,
			String keyColumnName) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(sql, keyHolder);
		Map<String, Object> keys = keyHolder.getKeys();
		Object id = keys != null ? keys.get(keyColumnName) : null;
		return ObjectUtils.nonnull(
				id instanceof Long n ? n : id instanceof Number n ? n.longValue() : null,
				"Generated ID");
	}

	/**
	 * Get a set of enumerated coded values from a JDBC array.
	 *
	 * @param <T>
	 *        the coded value type
	 * @param rs
	 *        the result set
	 * @param colNum
	 *        the column number
	 * @param clazz
	 *        the enum class
	 * @return the coded values, in array order without duplicates, or
	 *         {@code null} if the column value is {@code null}; {@code null}
	 *         array elements and unknown codes are ignored
	 * @throws SQLException
	 *         if any SQL error occurs
	 */
	public static <T extends Enum<T> & CodedValue> @Nullable Set<T> getCodedValueSet(ResultSet rs,
			int colNum, Class<T> clazz) throws SQLException {
		@Nullable
		Number @Nullable [] codes = getArray(rs, colNum);
		if ( codes == null ) {
			return null;
		}
		Set<T> result = new LinkedHashSet<>(codes.length);
		for ( Number code : codes ) {
			if ( code == null ) {
				continue;
			}
			int c = code.intValue();
			for ( T e : clazz.getEnumConstants() ) {
				if ( c == e.getCode() ) {
					result.add(e);
					break;
				}
			}
		}
		return result;
	}

	/**
	 * Get a Timestamp column value as an Instant.
	 *
	 * @param rs
	 *        the result set to read from
	 * @param column
	 *        the column number to get as an Instant
	 * @return the instant, or {@code null} if the column value is null
	 * @throws SQLException
	 *         if an error occurs
	 */
	public static @Nullable Instant getTimestampInstant(ResultSet rs, int column) throws SQLException {
		Timestamp ts = rs.getTimestamp(column);
		return (ts != null ? ts.toInstant() : null);
	}

	/**
	 * Get a Timestamp column value as an Instant.
	 *
	 * @param rs
	 *        the result set to read from
	 * @param column
	 *        the column number to get as an Instant
	 * @return the instant (presumed non-null)
	 * @throws SQLException
	 *         if an error occurs
	 */
	@SuppressWarnings("NullAway")
	public static Instant timestampInstant(ResultSet rs, int column) throws SQLException {
		return getTimestampInstant(rs, column);
	}

}
