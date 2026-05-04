/* ==================================================================
 * JdbcBulkLoadingContextSupportTests.java - 4/05/2026 10:19:36 am
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

import static net.solarnetwork.test.EasyMockUtils.assertWith;
import static org.assertj.core.api.BDDAssertions.from;
import static org.assertj.core.api.BDDAssertions.then;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
import net.solarnetwork.dao.jdbc.JdbcBulkLoadingContextSupport;

/**
 * Test cases for the {@link JdbcBulkLoadingContextSupport} class.
 *
 * @author matt
 * @version 1.0
 */
public class JdbcBulkLoadingContextSupportTests {

	private static final String BULK_LOAD_SQL = "{call load_data(?)}";

	private PlatformTransactionManager txManager;
	private DataSource dataSource;
	private Connection jdbcConnection;
	private CallableStatement jdbcStatement;

	private static class TestContext extends JdbcBulkLoadingContextSupport<Integer> {

		private List<Integer> loaded = new ArrayList<>(8);

		private TestContext(@Nullable PlatformTransactionManager txManager, DataSource dataSource,
				String sql, LoadingOptions options,
				@Nullable LoadingExceptionHandler<Integer> exceptionHandler) {
			super(txManager, dataSource, sql, options, exceptionHandler);
		}

		@Override
		protected boolean doLoad(Integer entity, PreparedStatement stmt, long index)
				throws SQLException {
			loaded.add(entity);
			return true;
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
		final var data = List.of(1, 2, 3, 4, 5, 6);

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
			for ( Integer d : data ) {
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
			// @formatter:on
		}
	}

	@Test
	public void load_singleTransaction() throws Exception {
		// GIVEN
		final var opts = new BasicBulkLoadingOptions(null, null,
				LoadingTransactionMode.SingleTransaction, null);
		final var data = List.of(1, 2, 3, 4, 5, 6);

		// start batch transaction, should be called 2x
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
			for ( Integer d : data ) {
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
			// @formatter:on
		}
	}

}
