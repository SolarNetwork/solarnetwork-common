/* ==================================================================
 * BulkLoadingContextSupport.java - 2/12/2020 12:16:37 pm
 * 
 * Copyright 2020 SolarNetwork.net Dev Team
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

import static net.solarnetwork.dao.BulkLoadingDao.LoadingTransactionMode.BatchTransactions;
import static net.solarnetwork.dao.BulkLoadingDao.LoadingTransactionMode.NoTransaction;
import static net.solarnetwork.dao.BulkLoadingDao.LoadingTransactionMode.SingleTransaction;
import static net.solarnetwork.dao.BulkLoadingDao.LoadingTransactionMode.TransactionCheckpoints;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.SqlProvider;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import net.solarnetwork.dao.BulkLoadingDao;

/**
 * Base implementation of {@link BulkLoadingDao.LoadingContext} for JDBC stored
 * procedure based implementations.
 * 
 * <P>
 * This class handles the low-level transaction semantics for the load
 * operation, and relies on extending classes to implement the
 * {@link #doLoad(Object, PreparedStatement, long)} method to save each entity
 * into the backend database.
 * </p>
 * 
 * @param <T>
 *        the entity type
 * @author matt
 * @version 1.0
 */
public abstract class JdbcBulkLoadingContextSupport<T>
		implements BulkLoadingDao.LoadingContext<T>, SqlProvider {

	/** The default batch size. */
	public static final int DEFAULT_BATCH_SIZE = 100;

	/** A class-level logger. */
	protected final Logger log = LoggerFactory.getLogger(getClass());

	private final PlatformTransactionManager txManager;
	private final DataSource dataSource;
	private final String sql;
	private final BulkLoadingDao.LoadingOptions options;
	private final BulkLoadingDao.LoadingExceptionHandler<T> exceptionHandler;

	private final TransactionStatus transaction;
	private final int batchSize;

	private long numLoaded;
	private long numCommitted;
	private Connection con;
	private PreparedStatement stmt;
	private TransactionStatus batchTransaction;
	private CountAwareCheckpoint transactionCheckpoint;
	private T lastLoadedEntity;

	/**
	 * Constructor.
	 * 
	 * @param txManager
	 *        the transaction manager
	 * @param dataSource
	 *        the data source
	 * @param sql
	 *        the SQL statement (or JDBC SQL call) to execute
	 */
	public JdbcBulkLoadingContextSupport(PlatformTransactionManager txManager, DataSource dataSource,
			String sql, BulkLoadingDao.LoadingOptions options,
			BulkLoadingDao.LoadingExceptionHandler<T> exceptionHandler) {
		super();
		this.txManager = txManager;
		this.dataSource = dataSource;
		this.sql = sql;
		if ( options == null ) {
			throw new IllegalArgumentException("The LoadingOptions argument cannot be null");
		}
		this.options = options;
		this.exceptionHandler = exceptionHandler;

		if ( options.getTransactionMode() == SingleTransaction
				|| options.getTransactionMode() == TransactionCheckpoints ) {
			log.debug("Starting new bulk load [{}] overall transaction", options.getName());
			DefaultTransactionDefinition txDef = new DefaultTransactionDefinition();
			txDef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
			transaction = txManager.getTransaction(txDef);
		} else {
			transaction = null;
		}
		if ( options.getBatchSize() != null && options.getBatchSize() > 0 ) {
			this.batchSize = options.getBatchSize();
		} else {
			this.batchSize = DEFAULT_BATCH_SIZE;
		}

		this.numLoaded = 0;
	}

	private static class CountAwareCheckpoint {

		private final long count;
		private final Object savepoint;

		private CountAwareCheckpoint(Object savepoint, long count) {
			super();
			this.savepoint = savepoint;
			this.count = count;
		}
	}

	@Override
	public BulkLoadingDao.LoadingOptions getOptions() {
		return options;
	}

	@Override
	public long getLoadedCount() {
		return numLoaded;
	}

	@Override
	public long getCommittedCount() {
		return numCommitted;
	}

	/**
	 * Get the JDBC connection.
	 * 
	 * @return the connection
	 * @throws SQLException
	 *         if any SQL error occurs
	 */
	protected Connection getConnection() throws SQLException {
		Connection c = this.con;
		if ( c != null ) {
			return c;
		}
		c = DataSourceUtils.getConnection(dataSource);
		c.setAutoCommit(options.getTransactionMode() == NoTransaction);
		this.con = c;
		return c;
	}

	private PreparedStatement getPreparedStatement() throws SQLException {
		if ( this.stmt != null ) {
			return this.stmt;
		}
		PreparedStatement ps = createJdbcStatement(getConnection());
		this.stmt = ps;
		return ps;

	}

	@Override
	public T getLastLoadedEntity() {
		return lastLoadedEntity;
	}

	@Override
	public final void load(T entity) {
		lastLoadedEntity = entity;
		try {
			if ( options.getTransactionMode() == BatchTransactions ) {
				if ( numLoaded % batchSize == 0 ) {
					if ( batchTransaction != null ) {
						commit();
					}
					log.debug("Starting new bulk load [{}] batch transaction @ row {}",
							options.getName(), numLoaded);
					DefaultTransactionDefinition txDef = new DefaultTransactionDefinition();
					txDef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
					batchTransaction = txManager.getTransaction(txDef);
				}
			}
			if ( doLoad(entity, getPreparedStatement(), numLoaded) ) {
				numLoaded++;
			}
		} catch ( Exception e ) {
			if ( exceptionHandler != null ) {
				exceptionHandler.handleLoadingException(e, this);
			}
		}
	}

	/**
	 * Load a single entity.
	 * 
	 * <p>
	 * Extending classes must implement this method to perform the actual saving
	 * of the entity to JDBC.
	 * </p>
	 * 
	 * @param entity
	 *        the entity to load
	 * @param stmt
	 *        the statement to use
	 * @param index
	 *        the loading index
	 * @return {@literal true} if loaded
	 * @throws SQLException
	 *         if any SQL error occurs
	 */
	protected abstract boolean doLoad(T entity, PreparedStatement stmt, long index) throws SQLException;

	@Override
	public void createCheckpoint() {
		if ( options.getTransactionMode() == TransactionCheckpoints && transaction != null
				&& !transaction.isCompleted() ) {
			Object checkpoint = transaction.createSavepoint();
			if ( transactionCheckpoint != null ) {
				transaction.releaseSavepoint(transactionCheckpoint.savepoint);
			}
			transactionCheckpoint = new CountAwareCheckpoint(checkpoint, numLoaded);
			numCommitted = numLoaded;
		}
	}

	@Override
	public void commit() {
		if ( batchTransaction != null ) {
			log.debug("Committing bulk load [{}] batch transaction @ row {}", options.getName(),
					numLoaded);
			txManager.commit(batchTransaction);
			batchTransaction = null;
		} else if ( transaction != null && !transaction.isCompleted() ) {
			log.debug("Committing bulk load [{}] overall transaction @ row {}", options.getName(),
					numLoaded);
			txManager.commit(transaction);
		}
		numCommitted = numLoaded;
		close();
		stmt = null;
		con = null;
	}

	@Override
	public void rollback() {
		if ( transactionCheckpoint != null && transaction != null ) {
			transaction.rollbackToSavepoint(transactionCheckpoint.savepoint);
			transaction.releaseSavepoint(transactionCheckpoint.savepoint);
			numLoaded = transactionCheckpoint.count;
			transactionCheckpoint = null;
		} else if ( batchTransaction != null && !batchTransaction.isCompleted() ) {
			batchTransaction.setRollbackOnly();
			txManager.rollback(batchTransaction);
			numLoaded = numCommitted;
			batchTransaction = null;
		} else if ( transaction != null && !transaction.isCompleted() ) {
			txManager.rollback(transaction);
			numLoaded = numCommitted;
		}
	}

	@Override
	public void close() {
		if ( stmt != null ) {
			try {
				if ( !stmt.isClosed() ) {
					stmt.close();
				}
			} catch ( SQLException e ) {
				log.warn("Error closing bulk loading statement", e);
			}
		}
		try {
			if ( batchTransaction != null && !batchTransaction.isCompleted() ) {
				txManager.rollback(batchTransaction);
			} else if ( transaction != null && !transaction.isCompleted() ) {
				txManager.rollback(transaction);
			}
		} catch ( Exception e ) {
			log.warn("Error rolling back transaction", e);
		}
		if ( con != null ) {
			try {
				if ( !con.isClosed() ) {
					con.close();
				}
			} catch ( SQLException e ) {
				log.warn("Error closing bulk loading connection", e);
			} finally {
				DataSourceUtils.releaseConnection(con, dataSource);
			}
		}
	}

	/**
	 * Create the JDBC statement to use.
	 * 
	 * <p>
	 * This implementation invokes {@link Connection#prepareCall(String)},
	 * passing {@link #getSql()}. Extending classes can override to customize
	 * this behavior.
	 * </p>
	 * 
	 * @param con
	 *        the JDBC connection
	 * @return the statement
	 * @throws SQLException
	 *         if any SQL error occurs
	 */
	protected PreparedStatement createJdbcStatement(Connection con) throws SQLException {
		return con.prepareCall(getSql());
	}

	/**
	 * Get the transaction manager.
	 * 
	 * @return the manager
	 */
	public PlatformTransactionManager getTransactionManager() {
		return txManager;
	}

	/**
	 * Get the JDBC data source.
	 * 
	 * @return the dataSource
	 */
	public DataSource getDataSource() {
		return dataSource;
	}

	/**
	 * Get the JDBC statement to use for bulk loading.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public String getSql() {
		return sql;
	}

	/**
	 * Get the exception handler.
	 * 
	 * @return the exceptionHandler
	 */
	public BulkLoadingDao.LoadingExceptionHandler<T> getExceptionHandler() {
		return exceptionHandler;
	}

}
