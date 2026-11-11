/* ==================================================================
 * CommonSqlUtilsTests.java - 11/11/2026 11:46:22 am
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

import static net.solarnetwork.dao.jdbc.CommonSqlUtils.SQL_COMMENT;
import static net.solarnetwork.dao.jdbc.CommonSqlUtils.WHERE_COMPONENT_PREFIX_LENGTH;
import static net.solarnetwork.dao.jdbc.CommonSqlUtils.forUpdate;
import static net.solarnetwork.dao.jdbc.CommonSqlUtils.limitOffset;
import static net.solarnetwork.dao.jdbc.CommonSqlUtils.limitOffsetLiteral;
import static net.solarnetwork.dao.jdbc.CommonSqlUtils.orderBySorts;
import static net.solarnetwork.dao.jdbc.CommonSqlUtils.safeLikeValue;
import static net.solarnetwork.dao.jdbc.CommonSqlUtils.whereArrayColContains;
import static net.solarnetwork.dao.jdbc.CommonSqlUtils.whereDateRange;
import static net.solarnetwork.dao.jdbc.CommonSqlUtils.whereEqual;
import static net.solarnetwork.dao.jdbc.CommonSqlUtils.whereLike;
import static net.solarnetwork.dao.jdbc.CommonSqlUtils.whereOptimizedArrayContains;
import static net.solarnetwork.dao.jdbc.CommonSqlUtils.whereOptimizedLike;
import static net.solarnetwork.dao.jdbc.CommonSqlUtils.wrappedCountQuery;
import static net.solarnetwork.test.CommonTestUtils.randomLong;
import static net.solarnetwork.test.CommonTestUtils.randomString;
import static org.assertj.core.api.BDDAssertions.and;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.junit.Test;
import net.solarnetwork.dao.DateRangeCriteria;
import net.solarnetwork.dao.PaginationCriteria;
import net.solarnetwork.dao.jdbc.CommonSqlUtils;
import net.solarnetwork.domain.SimpleSortDescriptor;
import net.solarnetwork.domain.SortDescriptor;

/**
 * Test cases for the {@link CommonSqlUtils} class.
 *
 * @author matt
 * @version 1.0
 */
@SuppressWarnings("static-access")
public class CommonSqlUtilsTests {

	private record TestPagination(@Nullable Integer maxResults, @Nullable Long startOffset)
			implements PaginationCriteria {

		@Override
		public @Nullable List<SortDescriptor> getSorts() {
			return null;
		}

		@Override
		public @Nullable Integer getMax() {
			return maxResults;
		}

		@Override
		public @Nullable Long getOffset() {
			return startOffset;
		}

	}

	private record TestDateRange(@Nullable Instant start, @Nullable Instant end)
			implements DateRangeCriteria {

		@Override
		public @Nullable Instant getStartDate() {
			return start;
		}

		@Override
		public @Nullable Instant getEndDate() {
			return end;
		}

	}

	private record TestSort(@Nullable String sortKey, boolean descending) implements SortDescriptor {

		@Override
		public @Nullable String getSortKey() {
			return sortKey;
		}

		@Override
		public boolean isDescending() {
			return descending;
		}

	}

	@Test
	public void sqlComment_match() {
		for ( String line : new String[] { "-- comment", "  -- indented comment", "\t--tab" } ) {
			and.then(SQL_COMMENT.matcher(line).find()).as("Comment line [%s] matches", line).isTrue();
		}
	}

	@Test
	public void sqlComment_noMatch() {
		for ( String line : new String[] { "SELECT 1", "SELECT 1 -- trailing comment", "" } ) {
			and.then(SQL_COMMENT.matcher(line).find()).as("Non-comment line [%s] does not match", line)
					.isFalse();
		}
	}

	@Test
	public void whereComponentPrefixLength() {
		// GIVEN
		final StringBuilder buf = new StringBuilder();
		whereEqual(randomLong(), "foo", buf);

		// WHEN
		final String result = buf.substring(WHERE_COMPONENT_PREFIX_LENGTH);

		// THEN
		and.then(result).as("Leading tab and AND removed").isEqualTo(" foo = ?\n");
	}

	@Test
	public void forUpdate_noSkipLocked() {
		// GIVEN
		final StringBuilder buf = new StringBuilder("SELECT * FROM foo");

		// WHEN
		forUpdate(false, buf);

		// THEN
		and.then(buf).as("FOR UPDATE appended").hasToString("SELECT * FROM foo\nFOR UPDATE");
	}

	@Test
	public void forUpdate_skipLocked() {
		// GIVEN
		final StringBuilder buf = new StringBuilder("SELECT * FROM foo");

		// WHEN
		forUpdate(true, buf);

		// THEN
		// @formatter:off
		and.then(buf)
			.as("FOR UPDATE SKIP LOCKED appended")
			.hasToString("SELECT * FROM foo\nFOR UPDATE SKIP LOCKED")
			;
		// @formatter:on
	}

	@Test
	public void forUpdate_table() {
		// GIVEN
		final StringBuilder buf = new StringBuilder();

		// WHEN
		forUpdate(false, new String[] { "foo" }, buf);

		// THEN
		and.then(buf).as("FOR UPDATE OF table appended").hasToString("\nFOR UPDATE OF foo");
	}

	@Test
	public void forUpdate_tables_skipLocked() {
		// GIVEN
		final StringBuilder buf = new StringBuilder();

		// WHEN
		forUpdate(true, new String[] { "foo", "bar" }, buf);

		// THEN
		// @formatter:off
		and.then(buf)
			.as("FOR UPDATE OF comma-delimited tables appended, with SKIP LOCKED")
			.hasToString("\nFOR UPDATE OF foo, bar SKIP LOCKED")
			;
		// @formatter:on
	}

	@Test
	public void forUpdate_tables_empty() {
		// GIVEN
		final StringBuilder buf = new StringBuilder();

		// WHEN
		forUpdate(false, new String[0], buf);

		// THEN
		and.then(buf).as("Empty table names ignored").hasToString("\nFOR UPDATE");
	}

	@Test
	public void limitOffset_null() {
		// GIVEN
		final StringBuilder buf = new StringBuilder();

		// WHEN
		final int result = limitOffset(null, buf);

		// THEN
		and.then(result).as("No parameters generated").isZero();
		and.then(buf).as("Nothing appended").isEmpty();
	}

	@Test
	public void limitOffset_maxAndOffset() {
		// GIVEN
		final StringBuilder buf = new StringBuilder();

		// WHEN
		final int result = limitOffset(new TestPagination(10, 20L), buf);

		// THEN
		and.then(result).as("Limit and offset parameters generated").isEqualTo(2);
		and.then(buf).as("LIMIT and OFFSET appended").hasToString("\nLIMIT ? OFFSET ?");
	}

	@Test
	public void limitOffset_max() {
		// GIVEN
		final StringBuilder buf = new StringBuilder();

		// WHEN
		final int result = limitOffset(new TestPagination(10, null), buf);

		// THEN
		and.then(result).as("Limit parameter generated").isEqualTo(1);
		and.then(buf).as("LIMIT appended").hasToString("\nLIMIT ?");
	}

	@Test
	public void limitOffset_offset() {
		// GIVEN
		final StringBuilder buf = new StringBuilder();

		// WHEN
		final int result = limitOffset(new TestPagination(null, 20L), buf);

		// THEN
		and.then(result).as("Offset parameter generated").isEqualTo(1);
		and.then(buf).as("OFFSET appended on new line").hasToString("\nOFFSET ?");
	}

	@Test
	public void limitOffset_notPositive() {
		// GIVEN
		final StringBuilder buf = new StringBuilder();

		// WHEN
		final int result = limitOffset(new TestPagination(0, 0L), buf);

		// THEN
		and.then(result).as("No parameters generated for non-positive values").isZero();
		and.then(buf).as("Nothing appended").isEmpty();
	}

	@Test
	public void wrappedCountQuery_select() {
		// WHEN
		final String result = wrappedCountQuery("SELECT * FROM foo");

		// THEN
		// @formatter:off
		and.then(result)
			.as("Query wrapped in count query")
			.isEqualTo("SELECT COUNT(*) FROM (SELECT * FROM foo) AS q")
			;
		// @formatter:on
	}

	@Test
	public void limitOffsetLiteral_null() {
		// GIVEN
		final StringBuilder buf = new StringBuilder();

		// WHEN
		limitOffsetLiteral(null, buf);

		// THEN
		and.then(buf).as("Nothing appended").isEmpty();
	}

	@Test
	public void limitOffsetLiteral_maxAndOffset() {
		// GIVEN
		final StringBuilder buf = new StringBuilder();

		// WHEN
		limitOffsetLiteral(new TestPagination(10, 20L), buf);

		// THEN
		and.then(buf).as("Literal LIMIT and OFFSET appended").hasToString("\nLIMIT 10 OFFSET 20");
	}

	@Test
	public void limitOffsetLiteral_max() {
		// GIVEN
		final StringBuilder buf = new StringBuilder();

		// WHEN
		limitOffsetLiteral(new TestPagination(10, null), buf);

		// THEN
		and.then(buf).as("Literal LIMIT appended").hasToString("\nLIMIT 10");
	}

	@Test
	public void limitOffsetLiteral_offset() {
		// GIVEN
		final StringBuilder buf = new StringBuilder();

		// WHEN
		limitOffsetLiteral(new TestPagination(null, 20L), buf);

		// THEN
		and.then(buf).as("Literal OFFSET appended without max").hasToString("\nOFFSET 20");
	}

	@Test
	public void limitOffsetLiteral_notPositiveMax_offset() {
		// GIVEN
		final StringBuilder buf = new StringBuilder();

		// WHEN
		limitOffsetLiteral(new TestPagination(0, 20L), buf);

		// THEN
		and.then(buf).as("Literal OFFSET appended on new line when max not positive")
				.hasToString("\nOFFSET 20");
	}

	@Test
	public void limitOffsetLiteral_max_negativeOffset() {
		// GIVEN
		final StringBuilder buf = new StringBuilder();

		// WHEN
		limitOffsetLiteral(new TestPagination(10, -5L), buf);

		// THEN
		and.then(buf).as("Literal OFFSET omitted when not positive").hasToString("\nLIMIT 10");
	}

	@Test
	public void limitOffsetLiteral_notPositive() {
		// GIVEN
		final StringBuilder buf = new StringBuilder();

		// WHEN
		limitOffsetLiteral(new TestPagination(0, 0L), buf);

		// THEN
		and.then(buf).as("Nothing appended for non-positive values").isEmpty();
	}

	@Test
	public void whereArrayColContains_null() {
		// GIVEN
		final StringBuilder buf = new StringBuilder();

		// WHEN
		final int result = whereArrayColContains(null, "foo", buf);

		// THEN
		and.then(result).as("No parameters generated").isZero();
		and.then(buf).as("Nothing appended").isEmpty();
	}

	@Test
	public void whereArrayColContains_empty() {
		// GIVEN
		final StringBuilder buf = new StringBuilder();

		// WHEN
		final int result = whereArrayColContains(new Long[0], "foo", buf);

		// THEN
		and.then(result).as("No parameters generated").isZero();
		and.then(buf).as("Nothing appended").isEmpty();
	}

	@Test
	public void whereArrayColContains_values() {
		// GIVEN
		final StringBuilder buf = new StringBuilder();

		// WHEN
		final int result = whereArrayColContains(new Long[] { randomLong(), randomLong() }, "foo", buf);

		// THEN
		and.then(result).as("Array parameter generated").isEqualTo(1);
		and.then(buf).as("Array contains clause appended").hasToString("\tAND foo @> ?\n");
	}

	@Test
	public void whereOptimizedArrayContains_null() {
		// GIVEN
		final StringBuilder buf = new StringBuilder();

		// WHEN
		final int result = whereOptimizedArrayContains(null, "foo", buf);

		// THEN
		and.then(result).as("No parameters generated").isZero();
		and.then(buf).as("Nothing appended").isEmpty();
	}

	@Test
	public void whereOptimizedArrayContains_empty() {
		// GIVEN
		final StringBuilder buf = new StringBuilder();

		// WHEN
		final int result = whereOptimizedArrayContains(new Long[0], "foo", buf);

		// THEN
		and.then(result).as("No parameters generated").isZero();
		and.then(buf).as("Nothing appended").isEmpty();
	}

	@Test
	public void whereOptimizedArrayContains_single() {
		// GIVEN
		final StringBuilder buf = new StringBuilder();

		// WHEN
		final int result = whereOptimizedArrayContains(new Long[] { randomLong() }, "foo", buf);

		// THEN
		and.then(result).as("Parameter generated").isEqualTo(1);
		and.then(buf).as("Equality clause appended for single value").hasToString("\tAND foo = ?\n");
	}

	@Test
	public void whereOptimizedArrayContains_multiple() {
		// GIVEN
		final StringBuilder buf = new StringBuilder();

		// WHEN
		final int result = whereOptimizedArrayContains(new Long[] { randomLong(), randomLong() }, "foo",
				buf);

		// THEN
		and.then(result).as("Array parameter generated").isEqualTo(1);
		// @formatter:off
		and.then(buf)
			.as("ANY clause appended for multiple values")
			.hasToString("\tAND foo = ANY(?)\n")
			;
		// @formatter:on
	}

	@Test
	public void whereEqual_null() {
		// GIVEN
		final StringBuilder buf = new StringBuilder();

		// WHEN
		final int result = whereEqual(null, "foo", buf);

		// THEN
		and.then(result).as("No parameters generated").isZero();
		and.then(buf).as("Nothing appended").isEmpty();
	}

	@Test
	public void whereEqual_value() {
		// GIVEN
		final StringBuilder buf = new StringBuilder();

		// WHEN
		final int result = whereEqual(randomLong(), "foo", buf);

		// THEN
		and.then(result).as("Parameter generated").isEqualTo(1);
		and.then(buf).as("Equality clause appended").hasToString("\tAND foo = ?\n");
	}

	@Test
	public void whereLike_null() {
		// GIVEN
		final StringBuilder buf = new StringBuilder();

		// WHEN
		final int result = whereLike(null, "foo", buf);

		// THEN
		and.then(result).as("No parameters generated").isZero();
		and.then(buf).as("Nothing appended").isEmpty();
	}

	@Test
	public void whereLike_value() {
		// GIVEN
		final StringBuilder buf = new StringBuilder();

		// WHEN
		final int result = whereLike(randomString(), "foo", buf);

		// THEN
		and.then(result).as("Parameter generated").isEqualTo(1);
		and.then(buf).as("LIKE clause appended").hasToString("\tAND foo LIKE ?\n");
	}

	@Test
	public void whereOptimizedLike_null() {
		// GIVEN
		final StringBuilder buf = new StringBuilder();

		// WHEN
		final int result = whereOptimizedLike(null, "foo", buf);

		// THEN
		and.then(result).as("No parameters generated").isZero();
		and.then(buf).as("Nothing appended").isEmpty();
	}

	@Test
	public void whereOptimizedLike_empty() {
		// GIVEN
		final StringBuilder buf = new StringBuilder();

		// WHEN
		final int result = whereOptimizedLike(new String[0], "foo", buf);

		// THEN
		and.then(result).as("No parameters generated").isZero();
		and.then(buf).as("Nothing appended").isEmpty();
	}

	@Test
	public void whereOptimizedLike_single() {
		// GIVEN
		final StringBuilder buf = new StringBuilder();

		// WHEN
		final int result = whereOptimizedLike(new String[] { randomString() }, "foo", buf);

		// THEN
		and.then(result).as("Parameter generated").isEqualTo(1);
		and.then(buf).as("LIKE clause appended for single value").hasToString("\tAND foo LIKE ?\n");
	}

	@Test
	public void whereOptimizedLike_multiple() {
		// GIVEN
		final StringBuilder buf = new StringBuilder();

		// WHEN
		final int result = whereOptimizedLike(new String[] { randomString(), randomString() }, "foo",
				buf);

		// THEN
		and.then(result).as("Array parameter generated").isEqualTo(1);
		// @formatter:off
		and.then(buf)
			.as("LIKE ANY clause appended for multiple values")
			.hasToString("\tAND foo LIKE ANY(?)\n")
			;
		// @formatter:on
	}

	@Test
	public void whereDateRange_none() {
		// GIVEN
		final StringBuilder buf = new StringBuilder();

		// WHEN
		final int result = whereDateRange(new TestDateRange(null, null), "ts", buf);

		// THEN
		and.then(result).as("No parameters generated").isZero();
		and.then(buf).as("Nothing appended").isEmpty();
	}

	@Test
	public void whereDateRange_startAndEnd() {
		// GIVEN
		final StringBuilder buf = new StringBuilder();
		final Instant now = Instant.now();

		// WHEN
		final int result = whereDateRange(new TestDateRange(now, now.plusSeconds(1)), "ts", buf);

		// THEN
		and.then(result).as("Start and end parameters generated").isEqualTo(2);
		// @formatter:off
		and.then(buf)
			.as("Inclusive start and exclusive end clauses appended")
			.hasToString("\tAND ts >= ?\n\tAND ts < ?\n")
			;
		// @formatter:on
	}

	@Test
	public void whereDateRange_start() {
		// GIVEN
		final StringBuilder buf = new StringBuilder();

		// WHEN
		final int result = whereDateRange(new TestDateRange(Instant.now(), null), "ts", buf);

		// THEN
		and.then(result).as("Start parameter generated").isEqualTo(1);
		and.then(buf).as("Inclusive start clause appended").hasToString("\tAND ts >= ?\n");
	}

	@Test
	public void whereDateRange_end() {
		// GIVEN
		final StringBuilder buf = new StringBuilder();

		// WHEN
		final int result = whereDateRange(new TestDateRange(null, Instant.now()), "ts", buf);

		// THEN
		and.then(result).as("End parameter generated").isEqualTo(1);
		and.then(buf).as("Exclusive end clause appended").hasToString("\tAND ts < ?\n");
	}

	@Test
	public void orderBySorts_nullSorts() {
		// GIVEN
		final StringBuilder buf = new StringBuilder();

		// WHEN
		final int result = orderBySorts(null, Map.of("a", "t.a"), buf);

		// THEN
		and.then(result).as("No joining characters added").isZero();
		and.then(buf).as("Nothing appended").isEmpty();
	}

	@Test
	public void orderBySorts_nullMapping() {
		// GIVEN
		final StringBuilder buf = new StringBuilder();

		// WHEN
		final int result = orderBySorts(List.of(new SimpleSortDescriptor("a")), null, buf);

		// THEN
		and.then(result).as("No joining characters added").isZero();
		and.then(buf).as("Nothing appended").isEmpty();
	}

	@Test
	public void orderBySorts_emptyMapping() {
		// GIVEN
		final StringBuilder buf = new StringBuilder();

		// WHEN
		final int result = orderBySorts(List.of(new SimpleSortDescriptor("a")), Map.of(), buf);

		// THEN
		and.then(result).as("No joining characters added").isZero();
		and.then(buf).as("Nothing appended").isEmpty();
	}

	@Test
	public void orderBySorts_mapped() {
		// GIVEN
		final StringBuilder buf = new StringBuilder();
		final List<SortDescriptor> sorts = List.of(new SimpleSortDescriptor("a"),
				new SimpleSortDescriptor("b", true));

		// WHEN
		final int result = orderBySorts(sorts, Map.of("a", "t.a", "b", "t.b"), buf);

		// THEN
		and.then(result).as("Joining characters added").isEqualTo(2);
		// @formatter:off
		and.then(buf)
			.as("Mapped sorts appended in order, with descending flag")
			.hasToString(", t.a, t.b DESC")
			;
		// @formatter:on
	}

	@Test
	public void orderBySorts_unmappedSkipped() {
		// GIVEN
		final StringBuilder buf = new StringBuilder();
		final List<SortDescriptor> sorts = List.of(new SimpleSortDescriptor("x"),
				new SimpleSortDescriptor("a", true));

		// WHEN
		final int result = orderBySorts(sorts, Map.of("a", "t.a"), buf);

		// THEN
		and.then(result).as("Joining characters added").isEqualTo(2);
		and.then(buf).as("Unmapped sort skipped").hasToString(", t.a DESC");
	}

	@Test
	public void orderBySorts_noneMapped() {
		// GIVEN
		final StringBuilder buf = new StringBuilder();
		final List<SortDescriptor> sorts = List.of(new SimpleSortDescriptor("x"));

		// WHEN
		final int result = orderBySorts(sorts, Map.of("a", "t.a"), buf);

		// THEN
		and.then(result).as("No joining characters added").isZero();
		and.then(buf).as("Nothing appended").isEmpty();
	}

	@Test
	public void orderBySorts_nullSortKey() {
		// GIVEN
		final StringBuilder buf = new StringBuilder();
		final List<SortDescriptor> sorts = List.of(new TestSort(null, false), new TestSort("a", true));

		// WHEN
		final int result = orderBySorts(sorts, Map.of("a", "t.a"), buf);

		// THEN
		and.then(result).as("Joining characters added").isEqualTo(2);
		and.then(buf).as("Sort without key skipped").hasToString(", t.a DESC");
	}

	@Test
	public void safeLikeValue_null() {
		// WHEN
		final String result = safeLikeValue(null);

		// THEN
		and.then(result).as("Null input returns null").isNull();
	}

	@Test
	public void safeLikeValue_noReserved() {
		// WHEN
		final String result = safeLikeValue("foo bar");

		// THEN
		and.then(result).as("Value without reserved characters unchanged").isEqualTo("foo bar");
	}

	@Test
	public void safeLikeValue_reserved() {
		// WHEN
		final String result = safeLikeValue("a_b%c");

		// THEN
		and.then(result).as("Reserved characters escaped").isEqualTo("a\\_b\\%c");
	}

	@Test
	public void safeLikeValue_backslash() {
		// WHEN
		final String result = safeLikeValue("a\\b_c");

		// THEN
		and.then(result).as("Backslash escaped along with other reserved characters")
				.isEqualTo("a\\\\b\\_c");
	}

}
