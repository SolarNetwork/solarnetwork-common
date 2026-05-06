/* ==================================================================
 * JdbcDatumBulkLoadingContextSupport.java - 6/05/2026 12:29:03 pm
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

import static net.solarnetwork.dao.BulkLoadingDao.LoadingTransactionMode.TransactionCheckpoints;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.function.Supplier;
import javax.sql.DataSource;
import org.jspecify.annotations.Nullable;
import org.springframework.transaction.PlatformTransactionManager;
import net.solarnetwork.dao.BulkLoadingDao.LoadingExceptionHandler;
import net.solarnetwork.dao.BulkLoadingDao.LoadingOptions;
import net.solarnetwork.domain.datum.Datum;
import net.solarnetwork.util.CountTracker;

/**
 * Extension of {@link JdbcBulkLoadingContextSupport} with additional support
 * for {@link Datum} loading.
 *
 * @author matt
 * @version 1.0
 * @since 4.36
 */
public abstract class JdbcDatumBulkLoadingContextSupport<T extends Datum>
		extends JdbcBulkLoadingContextSupport<T> {

	private @Nullable Supplier<CountTracker> countTrackerProvider;
	private @Nullable CountTracker loadedCountsBySource;
	private @Nullable CountTracker committedCountsBySource;

	/**
	 * Constructor.
	 *
	 * @param txManager
	 *        the transaction manager
	 * @param dataSource
	 *        the data source
	 * @param sql
	 *        the SQL statement (or JDBC SQL call) to execute
	 * @param options
	 *        the options
	 * @param exceptionHandler
	 *        an exception handler
	 * @throws IllegalArgumentException
	 *         if {@code dataSource} or {@code sql} or {@code options} is
	 *         {@code null}, or {@code txManager} is {@code null} and the
	 *         {@code options.transactionMode} is not {@code NoTransaction}
	 */
	public JdbcDatumBulkLoadingContextSupport(@Nullable PlatformTransactionManager txManager,
			DataSource dataSource, String sql, LoadingOptions options,
			@Nullable LoadingExceptionHandler<T> exceptionHandler) {
		super(txManager, dataSource, sql, options, exceptionHandler);
	}

	@Override
	protected final boolean doLoad(T entity, PreparedStatement stmt, long index) throws SQLException {
		boolean result = doLoadDatum(entity, stmt, index);
		if ( result ) {
			if ( loadedCountsBySource != null ) {
				loadedCountsBySource.incrementCount(entity.getSourceId());
			} else if ( countTrackerProvider != null ) {
				loadedCountsBySource = countTrackerProvider.get();
				loadedCountsBySource.putCount(entity.getSourceId(), 1L);
				if ( transaction == null ) {
					committedCountsBySource = loadedCountsBySource;
				}
			}
		}
		return result;
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
	protected abstract boolean doLoadDatum(T entity, PreparedStatement stmt, long index)
			throws SQLException;

	@Override
	public Map<String, ? extends Number> loadedCountsPerSource() {
		final CountTracker tracker = loadedCountsBySource;
		return (tracker != null ? tracker.toMap() : Map.of());
	}

	@Override
	public Map<String, ? extends Number> committedCountsPerSource() {
		final CountTracker tracker = committedCountsBySource;
		return (tracker != null ? tracker.toMap() : Map.of());
	}

	@Override
	public void commit() {
		super.commit();
		if ( loadedCountsBySource != null ) {
			committedCountsBySource = loadedCountsBySource.clone();
		}
	}

	@Override
	public void createCheckpoint() {
		if ( options.getTransactionMode() == TransactionCheckpoints && transaction != null
				&& !transaction.isCompleted() ) {
			if ( loadedCountsBySource != null ) {
				committedCountsBySource = loadedCountsBySource.clone();
			}
		}
		super.createCheckpoint();
	}

	@Override
	public void rollback() {
		if ( loadedCountsBySource != null && ((hasCheckpoint() && transaction != null)
				|| (batchTransaction() != null && !batchTransaction().isCompleted())
				|| transaction != null && !transaction.isCompleted()) ) {
			loadedCountsBySource = committedCountsBySource;
		}
		super.rollback();
	}

	/**
	 * Get a provider of {@link CountTracker} instances.
	 *
	 * @return the provider, or {@code null}
	 */
	public @Nullable Supplier<CountTracker> getCountTrackerProvider() {
		return countTrackerProvider;
	}

	/**
	 * Set a provider of {@link CountTracker} instances.
	 *
	 * @param countTrackerProvider
	 *        the provider to set, or {@code null}
	 */
	public void setCountTrackerProvider(@Nullable Supplier<CountTracker> countTrackerProvider) {
		this.countTrackerProvider = countTrackerProvider;
	}

}
