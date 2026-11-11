/* ==================================================================
 * CommonSqlUtils.java - 6/10/2021 10:04:35 AM
 *
 * Copyright 2021 SolarNetwork.net Dev Team
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

import static net.solarnetwork.util.StringUtils.nonEmptyString;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Map;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;
import net.solarnetwork.dao.DateRangeCriteria;
import net.solarnetwork.dao.PaginationCriteria;
import net.solarnetwork.domain.SortDescriptor;

/**
 * Common SQL utilities for SolarNetwork.
 *
 * @author matt
 * @version 1.0
 * @since 4.60
 */
public final class CommonSqlUtils {

	private CommonSqlUtils() {
		// not available
	}

	/**
	 * Regular expression for a line starting with a {@literal --} SQL style
	 * comment character.
	 */
	public static final Pattern SQL_COMMENT = Pattern.compile("^\\s*--");

	/**
	 * The number of characters to drop from the start of a leading where
	 * clause.
	 */
	public static final int WHERE_COMPONENT_PREFIX_LENGTH = 4;

	/**
	 * Generate SQL {@literal FOR UPDATE SKIP LOCKED} criteria to support
	 * locking.
	 *
	 * @param skipLocked
	 *        {@literal true} to include the {@literal SKIP LOCKED} clause
	 * @param buf
	 *        the buffer to append the SQL to
	 */
	public static void forUpdate(boolean skipLocked, StringBuilder buf) {
		forUpdate(skipLocked, null, buf);
	}

	/**
	 * Generate SQL {@literal FOR UPDATE SKIP LOCKED} criteria to support
	 * locking.
	 *
	 * @param skipLocked
	 *        {@literal true} to include the {@literal SKIP LOCKED} clause
	 * @param tableNames
	 *        explicit table names to lock, or {@code null} for default (all
	 *        referenced tables)
	 * @param buf
	 *        the buffer to append the SQL to
	 */
	public static void forUpdate(boolean skipLocked, String @Nullable [] tableNames, StringBuilder buf) {
		buf.append("\nFOR UPDATE");
		if ( tableNames != null && tableNames.length > 0 ) {
			buf.append(" OF");
			int i = 0;
			for ( String tableName : tableNames ) {
				if ( i++ > 0 ) {
					buf.append(',');
				}
				buf.append(' ');
				buf.append(tableName);
			}
		}
		if ( skipLocked ) {
			buf.append(" SKIP LOCKED");
		}
	}

	/**
	 * Generate SQL {@literal LIMIT x OFFSET y} criteria to support pagination.
	 *
	 * <p>
	 * The buffer is populated with a pattern of {@literal \nLIMIT ? OFFSET ?}.
	 * </p>
	 *
	 * @param filter
	 *        the search criteria
	 * @param buf
	 *        the buffer to append the SQL to
	 * @return the number of JDBC query parameters generated
	 */
	public static int limitOffset(@Nullable PaginationCriteria filter, StringBuilder buf) {
		int result = 0;
		if ( filter != null && filter.getMax() != null ) {
			int max = filter.getMax();
			if ( max > 0 ) {
				buf.append("\nLIMIT ?");
				result++;
			}
		}
		if ( filter != null && filter.getOffset() != null ) {
			long offset = filter.getOffset();
			if ( offset > 0 ) {
				if ( result < 1 ) {
					buf.append('\n');
				} else {
					buf.append(' ');
				}
				buf.append("OFFSET ?");
				result++;
			}
		}
		return result;
	}

	/**
	 * Wrap a SQL query with a {@literal SELECT COUNT(*)} clause.
	 *
	 * @param sql
	 *        the SQL query to wrap
	 * @return the wrapped query
	 */
	public static String wrappedCountQuery(String sql) {
		return "SELECT COUNT(*) FROM (" + sql + ") AS q";
	}

	/**
	 * Generate SQL {@literal LIMIT x OFFSET y} criteria to support pagination
	 * where the limit and offset are generated as literal values.
	 *
	 * <p>
	 * The buffer is populated with a pattern of {@literal \nLIMIT x OFFSET y}.
	 * </p>
	 *
	 * @param filter
	 *        the search criteria
	 * @param buf
	 *        the buffer to append the SQL to
	 */
	public static void limitOffsetLiteral(@Nullable PaginationCriteria filter, StringBuilder buf) {
		if ( filter == null ) {
			return;
		}
		boolean limit = false;
		Integer max = filter.getMax();
		if ( max != null && max > 0 ) {
			buf.append("\nLIMIT ").append(max);
			limit = true;
		}
		Long offset = filter.getOffset();
		if ( offset != null && offset > 0 ) {
			buf.append(limit ? ' ' : '\n').append("OFFSET ").append(offset);
		}
	}

	/**
	 * Generate SQL {@literal WHERE} criteria for an array column containment
	 * clause.
	 *
	 * @param array
	 *        the array value to match
	 * @param colName
	 *        the array SQL column name
	 * @param buf
	 *        the buffer to append the SQL to
	 * @return the number of JDBC query parameters generated
	 * @see CommonJdbcUtils#prepareArrayParameter(Connection, PreparedStatement,
	 *      int, String, Object[])
	 */
	public static int whereArrayColContains(Object @Nullable [] array, String colName,
			StringBuilder buf) {
		if ( array != null && array.length > 0 ) {
			buf.append("\tAND ").append(colName).append(" @> ?\n");
			return 1;
		}
		return 0;
	}

	/**
	 * Generate SQL {@literal WHERE} criteria for an array containment clause.
	 *
	 * <p>
	 * If {@code array} contains exactly one value, the generated SQL will use a
	 * simple equality comparison. Otherwise, an {@code ANY()} comparison will
	 * be generated.
	 * </p>
	 *
	 * @param array
	 *        the array value to match
	 * @param colName
	 *        the array SQL column name
	 * @param buf
	 *        the buffer to append the SQL to
	 * @return the number of JDBC query parameters generated
	 * @see CommonJdbcUtils#prepareOptimizedArrayParameter(Connection,
	 *      PreparedStatement, int, String, Object[])
	 */
	public static int whereOptimizedArrayContains(Object @Nullable [] array, String colName,
			StringBuilder buf) {
		int paramCount = 0;
		if ( array != null && array.length > 0 ) {
			buf.append("\tAND ").append(colName).append(" = ");
			if ( array.length > 1 ) {
				buf.append("ANY(?)");
			} else {
				buf.append("?");
			}
			buf.append("\n");
			paramCount = 1;
		}
		return paramCount;
	}

	/**
	 * Generate SQL {@literal WHERE} criteria for a simple equality comparison.
	 *
	 * @param object
	 *        the column value to match
	 * @param colName
	 *        the SQL column name
	 * @param buf
	 *        the buffer to append the SQL to
	 * @return the number of JDBC query parameters generated
	 */
	public static int whereEqual(@Nullable Object object, String colName, StringBuilder buf) {
		int paramCount = 0;
		if ( object != null ) {
			buf.append("\tAND ").append(colName).append(" = ?\n");
			paramCount = 1;
		}
		return paramCount;
	}

	/**
	 * Generate SQL {@literal WHERE} criteria for a {@literal LIKE} comparison.
	 *
	 * @param object
	 *        the column value to match
	 * @param colName
	 *        the SQL column name
	 * @param buf
	 *        the buffer to append the SQL to
	 * @return the number of JDBC query parameters generated
	 */
	public static int whereLike(@Nullable Object object, String colName, StringBuilder buf) {
		int paramCount = 0;
		if ( object != null ) {
			buf.append("\tAND ").append(colName).append(" LIKE ?\n");
			paramCount = 1;
		}
		return paramCount;
	}

	/**
	 * Generate SQL {@literal WHERE} criteria for a {@literal LIKE} comparison
	 * against one or more values.
	 *
	 * <p>
	 * If {@code array} contains exactly one value, the generated SQL will use a
	 * simple {@code LIKE} comparison. Otherwise, an {@code LIKE ANY()}
	 * comparison will be generated.
	 * </p>
	 *
	 * @param array
	 *        the array value to match
	 * @param colName
	 *        the array SQL column name
	 * @param buf
	 *        the buffer to append the SQL to
	 * @return the number of JDBC query parameters generated
	 */
	public static int whereOptimizedLike(Object @Nullable [] array, String colName, StringBuilder buf) {
		int paramCount = 0;
		if ( array != null && array.length > 0 ) {
			buf.append("\tAND ").append(colName).append(" LIKE ");
			if ( array.length > 1 ) {
				buf.append("ANY(?)");
			} else {
				buf.append("?");
			}
			buf.append("\n");
			paramCount = 1;
		}
		return paramCount;
	}

	/**
	 * Generate SQL {@literal WHERE} criteria for a date range.
	 *
	 * <p>
	 * The buffer is populated with a pattern of {@literal \tAND c = ?\n} for
	 * each clause. The leading tab and {@literal AND} and space characters are
	 * <b>not</b> stripped.
	 * </p>
	 *
	 * @param filter
	 *        the search criteria
	 * @param colName
	 *        the date column name to use, e.g. {@code solardatum.ts}
	 * @param buf
	 *        the buffer to append the SQL to
	 * @return the number of JDBC query parameters generated
	 */
	public static int whereDateRange(DateRangeCriteria filter, String colName, StringBuilder buf) {
		int paramCount = 0;
		if ( filter.getStartDate() != null ) {
			buf.append("\tAND ").append(colName).append(" >= ?\n");
			paramCount += 1;
		}
		if ( filter.getEndDate() != null ) {
			buf.append("\tAND ").append(colName).append(" < ?\n");
			paramCount += 1;
		}
		return paramCount;
	}

	/**
	 * Generate SQL {@literal ORDER BY} criteria for a set of
	 * {@link SortDescriptor}.
	 *
	 * <p>
	 * The buffer is populated with a pattern of {@literal , key} for each key.
	 * The leading comma and space characters are <b>not</b> stripped, but the
	 * returned value indicates the number of characters to trim from the
	 * results if needed.
	 * </p>
	 *
	 * @param sorts
	 *        the sorts
	 * @param sortKeyMapping
	 *        the mapping of sort keys to SQL sort names
	 * @param buf
	 *        the buffer to append the SQL to
	 * @return the number of leading "joining" characters added to {@code buf};
	 *         will either be {@literal 0} or {@literal 2}
	 */
	public static int orderBySorts(@Nullable Iterable<SortDescriptor> sorts,
			@Nullable Map<String, String> sortKeyMapping, StringBuilder buf) {
		if ( sorts == null || sortKeyMapping == null || sortKeyMapping.isEmpty() ) {
			return 0;
		}
		boolean appended = false;
		for ( SortDescriptor sort : sorts ) {
			String sortKey = nonEmptyString(sort.getSortKey());
			if ( sortKey == null ) {
				continue;
			}
			String sqlName = nonEmptyString(sortKeyMapping.get(sortKey));
			if ( sqlName != null ) {
				appended = true;
				buf.append(", ").append(sqlName);
				if ( sort.isDescending() ) {
					buf.append(" DESC");
				}
			}
		}
		return (appended ? 2 : 0);
	}

	/** Pattern for matching SQL LIKE pattern reserved characters. */
	public static final Pattern LIKE_PATTERN_RESERVED = Pattern.compile("([_%\\\\])");

	/**
	 * Escape reserved SQL LIKE pattern characters.
	 *
	 * @param value
	 *        the value to escape
	 * @return the escaped value, or {@code null} if {@code value} is
	 *         {@code null}
	 */
	public static @Nullable String safeLikeValue(@Nullable String value) {
		if ( value == null ) {
			return null;
		}
		return LIKE_PATTERN_RESERVED.matcher(value).replaceAll("\\\\$1");
	}

}
