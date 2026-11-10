/* ==================================================================
 * CommonJdbcUtilsTests.java - 11/11/2026 11:27:20 am
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

package net.solarnetwork.dao.jdbc.test;

import static net.solarnetwork.dao.jdbc.CommonJdbcUtils.prepareOptimizedArrayParameter;
import static net.solarnetwork.dao.jdbc.CommonSqlUtils.whereOptimizedArrayContains;
import static net.solarnetwork.test.CommonTestUtils.randomDecimal;
import static net.solarnetwork.test.CommonTestUtils.randomInt;
import static net.solarnetwork.test.CommonTestUtils.randomLong;
import static net.solarnetwork.test.CommonTestUtils.randomString;
import static org.assertj.core.api.BDDAssertions.and;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.KeyHolder;
import net.solarnetwork.dao.FilterResults;
import net.solarnetwork.dao.OptimizedQueryCriteria;
import net.solarnetwork.dao.PaginationCriteria;
import net.solarnetwork.dao.jdbc.CommonJdbcUtils;
import net.solarnetwork.dao.jdbc.CountPreparedStatementCreatorProvider;
import net.solarnetwork.domain.CodedValue;
import net.solarnetwork.domain.SortDescriptor;
import net.solarnetwork.domain.Unique;

/**
 * Test cases for the {@link CommonJdbcUtils} class.
 *
 * @author matt
 * @version 1.0
 */
@SuppressWarnings("static-access")
@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class CommonJdbcUtilsTests {

	/**
	 * A statement creator with count support, for mocking.
	 */
	public interface PreparedStatementCreatorWithCount
			extends PreparedStatementCreator, CountPreparedStatementCreatorProvider {
		// marker API for Mockito
	}

	private static final class TestCriteria implements PaginationCriteria, OptimizedQueryCriteria {

		private final @Nullable Integer max;
		private final @Nullable Long offset;
		private final boolean withoutTotalResultsCount;

		private TestCriteria(@Nullable Integer max, @Nullable Long offset,
				boolean withoutTotalResultsCount) {
			super();
			this.max = max;
			this.offset = offset;
			this.withoutTotalResultsCount = withoutTotalResultsCount;
		}

		@Override
		public @Nullable List<SortDescriptor> getSorts() {
			return null;
		}

		@Override
		public @Nullable Integer getMax() {
			return max;
		}

		@Override
		public @Nullable Long getOffset() {
			return offset;
		}

		@Override
		public boolean isWithoutTotalResultsCount() {
			return withoutTotalResultsCount;
		}

	}

	private record TestEntity(Long key) implements Unique<Long> {

		@Override
		public Long getId() {
			return key;
		}

	}

	enum TestCodedValue implements CodedValue {

		One(1),

		Two(2),

		Three(3);

		private final int code;

		private TestCodedValue(int code) {
			this.code = code;
		}

		@Override
		public int getCode() {
			return code;
		}

	}

	@Mock
	private Connection con;

	@Mock
	private PreparedStatement stmt;

	@Mock
	private Array array;

	@Mock
	private ResultSet rs;

	@Mock
	private CallableStatement call;

	@Mock
	private JdbcOperations jdbcOps;

	@Mock
	private PreparedStatementCreator sql;

	@Mock
	private PreparedStatementCreatorWithCount sqlWithCount;

	@Mock
	private PreparedStatementCreator countSql;

	@Mock
	private RowMapper<TestEntity> mapper;

	@Test
	public void prepareOptimizedArrayParameter_null() throws SQLException {
		// WHEN
		final int result = prepareOptimizedArrayParameter(con, stmt, 1, (Long[]) null);

		// THEN
		then(stmt).shouldHaveNoInteractions();
		and.then(result).as("Parameter offset unchanged").isEqualTo(1);
	}

	@Test
	public void prepareOptimizedArrayParameter_empty() throws SQLException {
		// GIVEN
		final Long[] value = new Long[0];
		final StringBuilder where = new StringBuilder();

		// WHEN
		final int whereCount = whereOptimizedArrayContains(value, "foo", where);
		final int result = prepareOptimizedArrayParameter(con, stmt, 1, value);

		// THEN
		then(stmt).shouldHaveNoInteractions();
		// @formatter:off
		and.then(whereCount)
			.as("No SQL parameter generated")
			.isZero()
			;
		and.then(result)
			.as("Parameter offset unchanged, to match generated SQL")
			.isEqualTo(1)
			;
		// @formatter:on
	}

	@Test
	public void prepareOptimizedArrayParameter_empty_setNull() throws SQLException {
		// WHEN
		final int result = prepareOptimizedArrayParameter(con, stmt, 1, "bigint", new Long[0], true);

		// THEN
		then(stmt).should().setNull(2, Types.ARRAY);
		and.then(result).as("Parameter offset incremented").isEqualTo(2);
	}

	@Test
	public void prepareOptimizedArrayParameter_single() throws SQLException {
		// GIVEN
		final Long value = randomLong();

		// WHEN
		final int result = prepareOptimizedArrayParameter(con, stmt, 1, new Long[] { value });

		// THEN
		then(stmt).should().setObject(2, value);
		and.then(result).as("Parameter offset incremented").isEqualTo(2);
	}

	@Test
	public void prepareOptimizedArrayParameter_multiple() throws SQLException {
		// GIVEN
		final Long[] value = new Long[] { randomLong(), randomLong() };
		given(con.createArrayOf(eq("bigint"), aryEq(value))).willReturn(array);

		// WHEN
		final int result = prepareOptimizedArrayParameter(con, stmt, 1, value);

		// THEN
		then(stmt).should().setArray(2, array);
		then(array).should().free();
		and.then(result).as("Parameter offset incremented").isEqualTo(2);
	}

	@Test
	public void getArray_null() throws SQLException {
		// GIVEN
		given(rs.getArray(1)).willReturn(null);

		// WHEN
		final Long[] result = CommonJdbcUtils.getArray(rs, 1);

		// THEN
		and.then(result).as("Null column value returns null").isNull();
	}

	@Test
	public void getArray() throws SQLException {
		// GIVEN
		final Long[] value = new Long[] { randomLong(), randomLong() };
		given(rs.getArray(1)).willReturn(array);
		given(array.getArray()).willReturn(value);

		// WHEN
		final Long[] result = CommonJdbcUtils.getArray(rs, 1);

		// THEN
		and.then(result).as("Array value returned").isSameAs(value);
	}

	@Test
	public void getArray_error_notNumeric() throws SQLException {
		// GIVEN
		final SQLException err = new SQLException("Test");
		given(rs.getArray(1)).willReturn(array);
		given(array.getArray()).willThrow(err);
		given(array.getBaseType()).willReturn(Types.VARCHAR);

		// THEN
		// @formatter:off
		and.thenThrownBy(() -> CommonJdbcUtils.getArray(rs, 1))
			.as("Array exception rethrown for non-numeric array")
			.isSameAs(err)
			;
		// @formatter:on
	}

	@Test
	public void getArray_numeric_nonFinite() throws SQLException {
		// GIVEN
		final BigDecimal n1 = randomDecimal();
		final BigDecimal n5 = randomDecimal();
		final ResultSet arrayRs = mock(ResultSet.class);
		given(rs.getArray(1)).willReturn(array);
		given(array.getArray()).willThrow(new SQLException("Bad value for type BigDecimal"));
		given(array.getBaseType()).willReturn(Types.NUMERIC);
		given(array.getResultSet()).willReturn(arrayRs);
		given(arrayRs.next()).willReturn(true, true, true, true, true, false);
		given(arrayRs.getInt(1)).willReturn(1, 2, 3, 4, 5);
		// @formatter:off
		given(arrayRs.getBigDecimal(2))
			.willReturn(n1)
			.willThrow(new SQLException("NaN"))
			.willThrow(new SQLException("Infinity"))
			.willThrow(new SQLException("-Infinity"))
			.willReturn(n5)
			;
		// @formatter:on
		given(arrayRs.getArray(2)).willThrow(new SQLException("Not an array"));
		given(arrayRs.getString(2)).willReturn("NaN", "Infinity", "-Infinity");

		// WHEN
		final BigDecimal[] result = CommonJdbcUtils.getArray(rs, 1);

		// THEN
		// @formatter:off
		and.then(result)
			.as("Non-finite values mapped to null")
			.containsExactly(n1, null, null, null, n5)
			;
		// @formatter:on
		then(arrayRs).should().close();
	}

	@Test
	public void getArray_numeric_indexGap() throws SQLException {
		// GIVEN
		final BigDecimal n2 = randomDecimal();
		final BigDecimal n3 = randomDecimal();
		final ResultSet arrayRs = mock(ResultSet.class);
		given(rs.getArray(1)).willReturn(array);
		given(array.getArray()).willThrow(new SQLException("Bad value for type BigDecimal"));
		given(array.getBaseType()).willReturn(Types.NUMERIC);
		given(array.getResultSet()).willReturn(arrayRs);
		given(arrayRs.next()).willReturn(true, true, false);
		given(arrayRs.getInt(1)).willReturn(2, 3);
		given(arrayRs.getBigDecimal(2)).willReturn(n2, n3);

		// WHEN
		final BigDecimal[] result = CommonJdbcUtils.getArray(rs, 1);

		// THEN
		// @formatter:off
		and.then(result)
			.as("Missing leading index filled with null")
			.containsExactly(null, n2, n3)
			;
		// @formatter:on
	}

	@Test
	public void getArray_decimal_twoDimensional() throws SQLException {
		// GIVEN
		final BigDecimal n11 = randomDecimal();
		final BigDecimal n21 = randomDecimal();
		final BigDecimal n22 = randomDecimal();
		final ResultSet arrayRs = mock(ResultSet.class);
		final Array nested1 = mock(Array.class);
		final ResultSet nested1Rs = mock(ResultSet.class);
		final Array nested2 = mock(Array.class);
		final ResultSet nested2Rs = mock(ResultSet.class);

		given(rs.getArray(1)).willReturn(array);
		given(array.getArray()).willThrow(new SQLException("Bad value for type BigDecimal"));
		given(array.getBaseType()).willReturn(Types.DECIMAL);
		given(array.getResultSet()).willReturn(arrayRs);

		// outer array rows are nested arrays
		given(arrayRs.next()).willReturn(true, true, false);
		given(arrayRs.getInt(1)).willReturn(1, 2);
		given(arrayRs.getBigDecimal(2)).willThrow(new SQLException("Not a number"));
		given(arrayRs.getArray(2)).willReturn(nested1, nested2);

		// first nested array has a non-finite value
		given(nested1.getResultSet()).willReturn(nested1Rs);
		given(nested1Rs.next()).willReturn(true, true, false);
		given(nested1Rs.getInt(1)).willReturn(1, 2);
		given(nested1Rs.getBigDecimal(2)).willReturn(n11).willThrow(new SQLException("NaN"));
		given(nested1Rs.getArray(2)).willThrow(new SQLException("Not an array"));
		given(nested1Rs.getString(2)).willReturn("NaN");

		given(nested2.getResultSet()).willReturn(nested2Rs);
		given(nested2Rs.next()).willReturn(true, true, false);
		given(nested2Rs.getInt(1)).willReturn(1, 2);
		given(nested2Rs.getBigDecimal(2)).willReturn(n21, n22);

		// WHEN
		final BigDecimal[][] result = CommonJdbcUtils.getArray(rs, 1);

		// THEN
		// @formatter:off
		and.then(result)
			.as("2D array parsed with non-finite values mapped to null")
			.isDeepEqualTo(new BigDecimal[][] {
				new BigDecimal[] { n11, null },
				new BigDecimal[] { n21, n22 },
			})
			;
		// @formatter:on
		then(arrayRs).should().close();
		then(nested1Rs).should().close();
		then(nested2Rs).should().close();
	}

	@Test
	public void getArray_numeric_unsupportedValue() throws SQLException {
		// GIVEN
		final SQLException err = new SQLException("Bad value for type BigDecimal");
		final ResultSet arrayRs = mock(ResultSet.class);
		given(rs.getArray(1)).willReturn(array);
		given(array.getArray()).willThrow(err);
		given(array.getBaseType()).willReturn(Types.NUMERIC);
		given(array.getResultSet()).willReturn(arrayRs);
		given(arrayRs.next()).willReturn(true);
		given(arrayRs.getInt(1)).willReturn(1);
		given(arrayRs.getBigDecimal(2)).willThrow(new SQLException("Not a number"));
		given(arrayRs.getArray(2)).willThrow(new SQLException("Not an array"));
		given(arrayRs.getString(2)).willReturn("foo");

		// THEN
		// @formatter:off
		and.thenThrownBy(() -> CommonJdbcUtils.getArray(rs, 1))
			.as("Original array exception rethrown when value cannot be parsed")
			.isSameAs(err)
			;
		// @formatter:on
		then(arrayRs).should().close();
	}

	@Test
	public void array() throws SQLException {
		// GIVEN
		final Long[] value = new Long[] { randomLong(), randomLong() };
		given(rs.getArray(1)).willReturn(array);
		given(array.getArray()).willReturn(value);

		// WHEN
		final Long[] result = CommonJdbcUtils.array(rs, 1);

		// THEN
		and.then(result).as("Array value returned").isSameAs(value);
	}

	@Test
	public void arrayValue() throws SQLException {
		// GIVEN
		final Long[] value = new Long[] { randomLong(), randomLong() };
		given(array.getArray()).willReturn(value);

		// WHEN
		final Long[] result = CommonJdbcUtils.arrayValue(array);

		// THEN
		and.then(result).as("Array value returned").isSameAs(value);
	}

	@Test
	public void arrayValue_null() {
		// WHEN
		final Long[] result = CommonJdbcUtils.arrayValue(null);

		// THEN
		and.then(result).as("Null input returns null").isNull();
	}

	@Test
	public void arrayValue_notArray() {
		// WHEN
		final Long[] result = CommonJdbcUtils.arrayValue(randomString());

		// THEN
		and.then(result).as("Non-array input returns null").isNull();
	}

	@Test
	public void arrayValue_error() throws SQLException {
		// GIVEN
		given(array.getArray()).willThrow(new SQLException("Test"));

		// WHEN
		final Long[] result = CommonJdbcUtils.arrayValue(array);

		// THEN
		and.then(result).as("Array exception returns null").isNull();
	}

	@Test
	public void getUuid() throws SQLException {
		// GIVEN
		final UUID value = UUID.randomUUID();
		given(rs.getObject(1)).willReturn(value);

		// WHEN
		final UUID result = CommonJdbcUtils.getUuid(rs, 1);

		// THEN
		and.then(result).as("UUID column value returned").isSameAs(value);
	}

	@Test
	public void getUuid_string() throws SQLException {
		// GIVEN
		final UUID value = UUID.randomUUID();
		given(rs.getObject(1)).willReturn(value.toString());

		// WHEN
		final UUID result = CommonJdbcUtils.getUuid(rs, 1);

		// THEN
		and.then(result).as("String column value parsed as UUID").isEqualTo(value);
	}

	@Test
	public void getUuid_null() throws SQLException {
		// GIVEN
		given(rs.getObject(1)).willReturn(null);

		// WHEN
		final UUID result = CommonJdbcUtils.getUuid(rs, 1);

		// THEN
		and.then(result).as("Null column value returns null").isNull();
	}

	@Test
	public void getUuid_null_default() throws SQLException {
		// GIVEN
		final UUID defaultValue = UUID.randomUUID();
		given(rs.getObject(1)).willReturn(null);

		// WHEN
		final UUID result = CommonJdbcUtils.getUuid(rs, 1, defaultValue);

		// THEN
		and.then(result).as("Null column value returns default").isSameAs(defaultValue);
	}

	@Test
	public void getUuid_default_notUsed() throws SQLException {
		// GIVEN
		final UUID value = UUID.randomUUID();
		given(rs.getObject(1)).willReturn(value);

		// WHEN
		final UUID result = CommonJdbcUtils.getUuid(rs, 1, UUID.randomUUID());

		// THEN
		and.then(result).as("Non-null column value returned instead of default").isSameAs(value);
	}

	@Test
	public void getUuid_invalid() throws SQLException {
		// GIVEN
		given(rs.getObject(1)).willReturn("not a UUID");

		// THEN
		// @formatter:off
		and.thenThrownBy(() -> CommonJdbcUtils.getUuid(rs, 1))
			.as("Invalid UUID string throws")
			.isInstanceOf(IllegalArgumentException.class)
			;
		// @formatter:on
	}

	@Test
	public void uuid() throws SQLException {
		// GIVEN
		final UUID value = UUID.randomUUID();
		given(rs.getObject(1)).willReturn(value);

		// WHEN
		final UUID result = CommonJdbcUtils.uuid(rs, 1);

		// THEN
		and.then(result).as("UUID column value returned").isSameAs(value);
	}

	@Test
	public void uuid_null_default() throws SQLException {
		// GIVEN
		final UUID defaultValue = UUID.randomUUID();
		given(rs.getObject(1)).willReturn(null);

		// WHEN
		final UUID result = CommonJdbcUtils.uuid(rs, 1, defaultValue);

		// THEN
		and.then(result).as("Null column value returns default").isSameAs(defaultValue);
	}

	@Test
	public void getUuid_call() throws SQLException {
		// GIVEN
		final UUID value = UUID.randomUUID();
		given(call.getObject(1)).willReturn(value);

		// WHEN
		final UUID result = CommonJdbcUtils.getUuid(call, 1);

		// THEN
		and.then(result).as("UUID parameter value returned").isSameAs(value);
	}

	@Test
	public void getUuid_call_string() throws SQLException {
		// GIVEN
		final UUID value = UUID.randomUUID();
		given(call.getObject(1)).willReturn(value.toString());

		// WHEN
		final UUID result = CommonJdbcUtils.getUuid(call, 1);

		// THEN
		and.then(result).as("String parameter value parsed as UUID").isEqualTo(value);
	}

	@Test
	public void getUuid_call_null() throws SQLException {
		// GIVEN
		given(call.getObject(1)).willReturn(null);

		// WHEN
		final UUID result = CommonJdbcUtils.getUuid(call, 1);

		// THEN
		and.then(result).as("Null parameter value returns null").isNull();
	}

	@Test
	public void getUuid_call_null_default() throws SQLException {
		// GIVEN
		final UUID defaultValue = UUID.randomUUID();
		given(call.getObject(1)).willReturn(null);

		// WHEN
		final UUID result = CommonJdbcUtils.getUuid(call, 1, defaultValue);

		// THEN
		and.then(result).as("Null parameter value returns default").isSameAs(defaultValue);
	}

	@Test
	public void executeCountQuery() throws SQLException {
		// GIVEN
		final Long count = randomLong();
		given(jdbcOps.query(same(countSql), ArgumentMatchers.<ResultSetExtractor<Long>> any()))
				.willAnswer(inv -> inv.<ResultSetExtractor<Long>> getArgument(1).extractData(rs));
		given(rs.next()).willReturn(true);
		given(rs.getLong(1)).willReturn(count);

		// WHEN
		final Long result = CommonJdbcUtils.executeCountQuery(jdbcOps, countSql);

		// THEN
		and.then(result).as("Count from first row first column").isEqualTo(count);
	}

	@Test
	public void executeCountQuery_noRow() throws SQLException {
		// GIVEN
		given(jdbcOps.query(same(countSql), ArgumentMatchers.<ResultSetExtractor<Long>> any()))
				.willAnswer(inv -> inv.<ResultSetExtractor<Long>> getArgument(1).extractData(rs));
		given(rs.next()).willReturn(false);

		// WHEN
		final Long result = CommonJdbcUtils.executeCountQuery(jdbcOps, countSql);

		// THEN
		and.then(result).as("No row returns null").isNull();
	}

	@Test
	public void executeFilterQuery_countSupport() {
		// GIVEN
		final Long count = 123L;
		final List<TestEntity> data = List.of(new TestEntity(randomLong()));

		// execute count query
		given(sqlWithCount.countPreparedStatementCreator()).willReturn(countSql);
		given(jdbcOps.query(same(countSql), ArgumentMatchers.<ResultSetExtractor<Long>> any()))
				.willReturn(count);

		// execute actual query
		given(jdbcOps.query(sqlWithCount, mapper)).willReturn(data);

		// WHEN
		final TestCriteria filter = new TestCriteria(1, 1L, false);
		final FilterResults<TestEntity, Long> results = CommonJdbcUtils.executeFilterQuery(jdbcOps,
				filter, sqlWithCount, mapper);

		// THEN
		and.then(results).as("Result returned").isNotNull();
		and.then(results.getReturnedResultCount()).as("Result count from data").isEqualTo(data.size());
		and.then(results.getStartingOffset()).as("Result offset from criteria").isEqualTo(1L);
		and.then(results.getTotalResults())
				.as("Result total available with CountPreparedStatementCreatorProvider implementation")
				.isEqualTo(count);
		and.then(results.getResults()).as("Result results is data").isSameAs(data);
	}

	@Test
	public void executeFilterQuery_noCountSupport() {
		// GIVEN
		final List<TestEntity> data = List.of(new TestEntity(randomLong()));
		given(jdbcOps.query(sql, mapper)).willReturn(data);

		// WHEN
		final TestCriteria filter = new TestCriteria(1, 1L, false);
		final FilterResults<TestEntity, Long> results = CommonJdbcUtils.executeFilterQuery(jdbcOps,
				filter, sql, mapper);

		// THEN
		and.then(results).as("Result returned").isNotNull();
		and.then(results.getReturnedResultCount()).as("Result count from data").isEqualTo(data.size());
		and.then(results.getStartingOffset()).as("Result offset from criteria").isEqualTo(1L);
		and.then(results.getTotalResults()).as(
				"Result total not available without CountPreparedStatementCreatorProvider implementation")
				.isNull();
		and.then(results.getResults()).as("Result results is data").isSameAs(data);
	}

	@Test
	public void executeFilterQuery_noOffset() {
		// GIVEN
		final List<TestEntity> data = List.of(new TestEntity(randomLong()));
		given(jdbcOps.query(sql, mapper)).willReturn(data);

		// WHEN
		final TestCriteria filter = new TestCriteria(1, null, false);
		final FilterResults<TestEntity, Long> results = CommonJdbcUtils.executeFilterQuery(jdbcOps,
				filter, sql, mapper);

		// THEN
		and.then(results).as("Result returned").isNotNull();
		and.then(results.getReturnedResultCount()).as("Result count from data").isEqualTo(data.size());
		and.then(results.getStartingOffset())
				.as("Result offset defaults to 0 when not provided by criteria").isEqualTo(0L);
		and.then(results.getTotalResults()).as(
				"Result total not available without CountPreparedStatementCreatorProvider implementation")
				.isNull();
		and.then(results.getResults()).as("Result results is data").isSameAs(data);
	}

	@Test
	public void executeFilterQuery_withoutTotalResultsCount() {
		// GIVEN
		final List<TestEntity> data = List.of(new TestEntity(randomLong()));
		given(jdbcOps.query(sqlWithCount, mapper)).willReturn(data);

		// WHEN
		final TestCriteria filter = new TestCriteria(1, 1L, true);
		final FilterResults<TestEntity, Long> results = CommonJdbcUtils.executeFilterQuery(jdbcOps,
				filter, sqlWithCount, mapper);

		// THEN
		then(sqlWithCount).should(never()).countPreparedStatementCreator();
		and.then(results).as("Result returned").isNotNull();
		and.then(results.getReturnedResultCount()).as("Result count from data").isEqualTo(data.size());
		and.then(results.getStartingOffset()).as("Result offset from criteria").isEqualTo(1L);
		and.then(results.getTotalResults())
				.as("Result total not available given withoutTotalResultsCount").isNull();
		and.then(results.getResults()).as("Result results is data").isSameAs(data);
	}

	@Test
	public void executeFilterQuery_noMax() {
		// GIVEN
		final List<TestEntity> data = List.of(new TestEntity(randomLong()),
				new TestEntity(randomLong()));
		given(jdbcOps.query(sqlWithCount, mapper)).willReturn(data);

		// WHEN
		final TestCriteria filter = new TestCriteria(null, null, false);
		final FilterResults<TestEntity, Long> results = CommonJdbcUtils.executeFilterQuery(jdbcOps,
				filter, sqlWithCount, mapper);

		// THEN
		then(sqlWithCount).should(never()).countPreparedStatementCreator();
		and.then(results).as("Result returned").isNotNull();
		and.then(results.getReturnedResultCount()).as("Result count from data").isEqualTo(data.size());
		and.then(results.getStartingOffset())
				.as("Result offset defaults to 0 when not provided by criteria").isEqualTo(0L);
		and.then(results.getTotalResults())
				.as("Result total is data count when no max provided by criteria")
				.isEqualTo((long) data.size());
		and.then(results.getResults()).as("Result results is data").isSameAs(data);
	}

	private void givenGeneratedKeys(@Nullable Map<String, Object> keys) {
		given(jdbcOps.update(same(sql), any(KeyHolder.class))).willAnswer(inv -> {
			if ( keys != null ) {
				inv.<KeyHolder> getArgument(1).getKeyList().add(keys);
			}
			return 1;
		});
	}

	@Test
	public void updateWithGeneratedLong() {
		// GIVEN
		final Long id = randomLong();
		givenGeneratedKeys(Map.of("id", id));

		// WHEN
		final Long result = CommonJdbcUtils.updateWithGeneratedLong(jdbcOps, sql, "id");

		// THEN
		and.then(result).as("Generated key returned").isEqualTo(id);
	}

	@Test
	public void updateWithGeneratedLong_integer() {
		// GIVEN
		final Integer id = randomInt();
		givenGeneratedKeys(Map.of("id", id));

		// WHEN
		final Long result = CommonJdbcUtils.updateWithGeneratedLong(jdbcOps, sql, "id");

		// THEN
		and.then(result).as("Generated number key returned as long").isEqualTo(id.longValue());
	}

	@Test
	public void updateWithGeneratedLong_noKeys() {
		// GIVEN
		givenGeneratedKeys(null);

		// THEN
		// @formatter:off
		and.thenThrownBy(() -> CommonJdbcUtils.updateWithGeneratedLong(jdbcOps, sql, "id"))
			.as("No generated keys throws")
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("Generated ID")
			;
		// @formatter:on
	}

	@Test
	public void updateWithGeneratedLong_missingColumn() {
		// GIVEN
		givenGeneratedKeys(Map.of("foo", randomLong()));

		// THEN
		// @formatter:off
		and.thenThrownBy(() -> CommonJdbcUtils.updateWithGeneratedLong(jdbcOps, sql, "id"))
			.as("Missing generated key column throws")
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("Generated ID")
			;
		// @formatter:on
	}

	@Test
	public void updateWithGeneratedLong_notNumber() {
		// GIVEN
		givenGeneratedKeys(Map.of("id", randomString()));

		// THEN
		// @formatter:off
		and.thenThrownBy(() -> CommonJdbcUtils.updateWithGeneratedLong(jdbcOps, sql, "id"))
			.as("Non-number generated key throws")
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("Generated ID")
			;
		// @formatter:on
	}

	@Test
	public void getCodedValueSet() throws SQLException {
		// GIVEN
		given(rs.getArray(1)).willReturn(array);
		given(array.getArray()).willReturn(new Integer[] { 3, 1, 99, 3 });

		// WHEN
		final Set<TestCodedValue> result = CommonJdbcUtils.getCodedValueSet(rs, 1, TestCodedValue.class);

		// THEN
		// @formatter:off
		and.then(result)
			.as("Values in array order, with unknown codes ignored and duplicates removed")
			.containsExactly(TestCodedValue.Three, TestCodedValue.One)
			;
		// @formatter:on
	}

	@Test
	public void getCodedValueSet_null() throws SQLException {
		// GIVEN
		given(rs.getArray(1)).willReturn(null);

		// WHEN
		final Set<TestCodedValue> result = CommonJdbcUtils.getCodedValueSet(rs, 1, TestCodedValue.class);

		// THEN
		and.then(result).as("Null column value returns null").isNull();
	}

	@Test
	public void getTimestampInstant() throws SQLException {
		// GIVEN
		final Instant ts = Instant.now();
		given(rs.getTimestamp(1)).willReturn(Timestamp.from(ts));

		// WHEN
		final Instant result = CommonJdbcUtils.getTimestampInstant(rs, 1);

		// THEN
		and.then(result).as("Timestamp returned as Instant").isEqualTo(ts);
	}

	@Test
	public void getTimestampInstant_null() throws SQLException {
		// GIVEN
		given(rs.getTimestamp(1)).willReturn(null);

		// WHEN
		final Instant result = CommonJdbcUtils.getTimestampInstant(rs, 1);

		// THEN
		and.then(result).as("Null column value returns null").isNull();
	}

	@Test
	public void timestampInstant() throws SQLException {
		// GIVEN
		final Instant ts = Instant.now();
		given(rs.getTimestamp(1)).willReturn(Timestamp.from(ts));

		// WHEN
		final Instant result = CommonJdbcUtils.timestampInstant(rs, 1);

		// THEN
		and.then(result).as("Timestamp returned as Instant").isEqualTo(ts);
	}

}
