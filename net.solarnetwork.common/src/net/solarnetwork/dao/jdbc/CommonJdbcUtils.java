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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Collection;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import net.solarnetwork.codec.jackson.JsonUtils;
import net.solarnetwork.dao.DateRangeCriteria;
import net.solarnetwork.dao.PaginationCriteria;
import net.solarnetwork.domain.CodedValue;

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
		return prepareOptimizedArrayParameter(con, stmt, parameterOffset, "text", value, setNull);
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

}
