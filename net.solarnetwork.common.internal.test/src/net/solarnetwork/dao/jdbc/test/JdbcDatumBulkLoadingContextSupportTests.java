/* ==================================================================
 * JdbcDatumBulkLoadingContextSupportTests.java - 6/05/2026 3:37:47 pm
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

import static net.solarnetwork.test.CommonTestUtils.RNG;
import static net.solarnetwork.test.CommonTestUtils.randomInt;
import static net.solarnetwork.test.EasyMockUtils.assertWith;
import static org.assertj.core.api.BDDAssertions.from;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.InstanceOfAssertFactories.map;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.easymock.EasyMock;
import org.jspecify.annotations.Nullable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.SimpleTransactionStatus;
import net.solarnetwork.dao.BasicBulkLoadingOptions;
import net.solarnetwork.dao.BulkLoadingDao.LoadingExceptionHandler;
import net.solarnetwork.dao.BulkLoadingDao.LoadingOptions;
import net.solarnetwork.dao.BulkLoadingDao.LoadingTransactionMode;
import net.solarnetwork.dao.jdbc.JdbcDatumBulkLoadingContextSupport;
import net.solarnetwork.domain.datum.Datum;
import net.solarnetwork.domain.datum.DatumSamples;
import net.solarnetwork.domain.datum.GeneralDatum;
import net.solarnetwork.util.StringLongMapping;

/**
 * Test cases for the {@link JdbcDatumBulkLoadingContextSupport} class.
 *
 * @author matt
 * @version 1.0
 */
public class JdbcDatumBulkLoadingContextSupportTests {

	private static final String BULK_LOAD_SQL = "{call load_data(?)}";

	private PlatformTransactionManager txManager;
	private DataSource dataSource;
	private Connection jdbcConnection;
	private CallableStatement jdbcStatement;

	private static class TestContext extends JdbcDatumBulkLoadingContextSupport<Datum> {

		private List<Datum> loaded = new ArrayList<>(8);
		private List<Map<String, ? extends Number>> batchLoadedBySource = new ArrayList<>(2);

		private TestContext(@Nullable PlatformTransactionManager txManager, DataSource dataSource,
				String sql, LoadingOptions options,
				@Nullable LoadingExceptionHandler<Datum> exceptionHandler) {
			super(txManager, dataSource, sql, options, exceptionHandler);
			setCountTrackerProvider(StringLongMapping::new);
		}

		@Override
		protected boolean doLoadDatum(Datum entity, PreparedStatement stmt, long index)
				throws SQLException {
			loaded.add(entity);
			return true;
		}

		@Override
		public void commit() {
			super.commit();
			batchLoadedBySource.add(committedCountsPerSource());
		}

	}

	@Before
	public void setup() {
		txManager = EasyMock.createMock(PlatformTransactionManager.class);
		dataSource = EasyMock.createMock(DataSource.class);
		jdbcConnection = EasyMock.createMock(Connection.class);
		jdbcStatement = EasyMock.createMock(CallableStatement.class);
	}

	@After
	public void teardown() {
		EasyMock.verify(txManager, dataSource, jdbcConnection, jdbcStatement);
	}

	private void replayAll() {
		EasyMock.replay(txManager, dataSource, jdbcConnection, jdbcStatement);
	}

	@Test
	public void load_batch() throws Exception {
		// GIVEN
		final var opts = new BasicBulkLoadingOptions(null, 4, LoadingTransactionMode.BatchTransactions,
				null);

		final int datumCount = 6;
		final List<Datum> data = new ArrayList<>(datumCount);
		final Instant start = Instant.now().truncatedTo(ChronoUnit.SECONDS);
		final Map<String, Long> sourceCounts = new HashMap<>(datumCount);
		for ( int i = 0; i < datumCount; i++ ) {
			var d = new GeneralDatum(RNG.nextLong(2) + 1L, String.valueOf((char) ('a' + RNG.nextInt(2))),
					start.plusSeconds(i), new DatumSamples(Map.of("watts", randomInt()), null, null));
			data.add(d);
			sourceCounts.compute(d.getSourceId(), (k, v) -> (v != null ? v : 0L) + 1L);
		}

		// start batch transaction, should be called 2x
		final var txDef = new DefaultTransactionDefinition(PROPAGATION_REQUIRES_NEW);
		expect(txManager.getTransaction(txDef)).andAnswer(() -> new SimpleTransactionStatus()).times(2);

		// get the DB connection, disable auto-commit
		expect(dataSource.getConnection()).andReturn(jdbcConnection).times(2);
		jdbcConnection.setAutoCommit(false);
		expectLastCall().times(2);

		// prepare the load call
		expect(jdbcConnection.prepareCall(BULK_LOAD_SQL)).andReturn(jdbcStatement).times(2);

		// commit batch transactions
		txManager.commit(assertWith(txStatus -> {
			if ( txStatus instanceof SimpleTransactionStatus txs ) {
				txs.setCompleted();
			}
		}));
		expectLastCall().times(2);

		// close statements
		expect(jdbcStatement.isClosed()).andReturn(false).times(2);
		jdbcStatement.close();
		expectLastCall().times(2);

		// close batch connections
		jdbcConnection.close();
		expectLastCall().times(2);

		// WHEN
		replayAll();
		try (TestContext ctx = new TestContext(txManager, dataSource, BULK_LOAD_SQL, opts, null)) {
			for ( Datum d : data ) {
				ctx.load(d);
			}
			ctx.commit();

			// THEN
			// @formatter:off
			then(ctx)
				.as("Context tracked loaded count equal to given datum")
				.returns((long)data.size(), from(TestContext::getLoadedCount))
				.as("Loaded given datum")
				.returns(data, from(c -> c.loaded))
				;

			then(ctx.loadedCountsPerSource())
				.asInstanceOf(map(String.class, Long.class))
				.as("Datum loaded by source tracked")
				.containsExactlyInAnyOrderEntriesOf(sourceCounts)
				;


			then(ctx.committedCountsPerSource())
				.asInstanceOf(map(String.class, Long.class))
				.as("Datum committed by source tracked")
				.containsExactlyInAnyOrderEntriesOf(sourceCounts)
				;

			then(ctx.batchLoadedBySource)
				.as("Two batch transactions committed")
				.hasSize(2)
				.last(map(String.class, Long.class))
				.containsExactlyInAnyOrderEntriesOf(sourceCounts)
				;
			// @formatter:on
		}
	}

	@Test
	public void load_singleTransaction() throws Exception {
		// GIVEN
		final var opts = new BasicBulkLoadingOptions(null, null,
				LoadingTransactionMode.SingleTransaction, null);

		final int datumCount = 6;
		final List<Datum> data = new ArrayList<>(datumCount);
		final Instant start = Instant.now().truncatedTo(ChronoUnit.SECONDS);
		final Map<String, Long> sourceCounts = new HashMap<>(datumCount);
		for ( int i = 0; i < datumCount; i++ ) {
			var d = new GeneralDatum(RNG.nextLong(1) + 1L, String.valueOf((char) ('a' + RNG.nextInt(1))),
					start.plusSeconds(i), new DatumSamples(Map.of("watts", randomInt()), null, null));
			data.add(d);
			sourceCounts.compute(d.getSourceId(), (k, v) -> (v != null ? v : 0L) + 1L);
		}

		// start batch transaction, should be called once
		final var txDef = new DefaultTransactionDefinition(PROPAGATION_REQUIRES_NEW);
		expect(txManager.getTransaction(txDef)).andAnswer(() -> new SimpleTransactionStatus());

		// get the DB connection, disable auto-commit
		expect(dataSource.getConnection()).andReturn(jdbcConnection);
		jdbcConnection.setAutoCommit(false);
		expectLastCall();

		// prepare the load call
		expect(jdbcConnection.prepareCall(BULK_LOAD_SQL)).andReturn(jdbcStatement);

		// commit batch transactions
		txManager.commit(assertWith(txStatus -> {
			if ( txStatus instanceof SimpleTransactionStatus txs ) {
				txs.setCompleted();
			}
		}));

		// close statements
		expect(jdbcStatement.isClosed()).andReturn(false);
		jdbcStatement.close();
		expectLastCall();

		// close batch connections
		jdbcConnection.close();
		expectLastCall();

		// WHEN
		replayAll();
		try (TestContext ctx = new TestContext(txManager, dataSource, BULK_LOAD_SQL, opts, null)) {
			for ( Datum d : data ) {
				ctx.load(d);
			}
			ctx.commit();

			// THEN
			// @formatter:off
			then(ctx)
				.as("Context tracked loaded count equal to given datum")
				.returns((long)data.size(), from(TestContext::getLoadedCount))
				.as("Loaded given datum")
				.returns(data, from(c -> c.loaded))
				;

			then(ctx.loadedCountsPerSource())
				.asInstanceOf(map(String.class, Long.class))
				.as("Datum loaded by source tracked")
				.containsExactlyInAnyOrderEntriesOf(sourceCounts)
				;


			then(ctx.committedCountsPerSource())
				.asInstanceOf(map(String.class, Long.class))
				.as("Datum committed by source tracked")
				.containsExactlyInAnyOrderEntriesOf(sourceCounts)
				;

			then(ctx.batchLoadedBySource)
				.as("One transaction committed")
				.hasSize(1)
				.last(map(String.class, Long.class))
				.containsExactlyInAnyOrderEntriesOf(sourceCounts)
				;
			// @formatter:on
		}
	}

	@Test
	public void load_noTransaction() throws Exception {
		// GIVEN
		final var opts = new BasicBulkLoadingOptions(null, null, LoadingTransactionMode.NoTransaction,
				null);

		final int datumCount = 6;
		final List<Datum> data = new ArrayList<>(datumCount);
		final Instant start = Instant.now().truncatedTo(ChronoUnit.SECONDS);
		final Map<String, Long> sourceCounts = new HashMap<>(datumCount);
		for ( int i = 0; i < datumCount; i++ ) {
			var d = new GeneralDatum(RNG.nextLong(1) + 1L, String.valueOf((char) ('a' + RNG.nextInt(1))),
					start.plusSeconds(i), new DatumSamples(Map.of("watts", randomInt()), null, null));
			data.add(d);
			sourceCounts.compute(d.getSourceId(), (k, v) -> (v != null ? v : 0L) + 1L);
		}

		// get the DB connection, disable auto-commit
		expect(dataSource.getConnection()).andReturn(jdbcConnection);
		jdbcConnection.setAutoCommit(true);
		expectLastCall();

		// prepare the load call
		expect(jdbcConnection.prepareCall(BULK_LOAD_SQL)).andReturn(jdbcStatement);

		// close statements
		expect(jdbcStatement.isClosed()).andReturn(false);
		jdbcStatement.close();
		expectLastCall();

		// close batch connections
		jdbcConnection.close();
		expectLastCall();

		// WHEN
		replayAll();
		try (TestContext ctx = new TestContext(txManager, dataSource, BULK_LOAD_SQL, opts, null)) {
			for ( Datum d : data ) {
				ctx.load(d);
			}
			ctx.commit();

			// THEN
			// @formatter:off
			then(ctx)
				.as("Context tracked loaded count equal to given datum")
				.returns((long)data.size(), from(TestContext::getLoadedCount))
				.as("Loaded given datum")
				.returns(data, from(c -> c.loaded))
				;

			then(ctx.loadedCountsPerSource())
				.asInstanceOf(map(String.class, Long.class))
				.as("Datum loaded by source tracked")
				.containsExactlyInAnyOrderEntriesOf(sourceCounts)
				;


			then(ctx.committedCountsPerSource())
				.asInstanceOf(map(String.class, Long.class))
				.as("Datum committed by source tracked")
				.containsExactlyInAnyOrderEntriesOf(sourceCounts)
				;

			then(ctx.batchLoadedBySource)
				.as("One transaction committed")
				.hasSize(1)
				.last(map(String.class, Long.class))
				.containsExactlyInAnyOrderEntriesOf(sourceCounts)
				;
			// @formatter:on
		}
	}
}
