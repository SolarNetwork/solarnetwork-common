/* ==================================================================
 * DerbyDataSourceErrorProxy.java - 24/07/2016 2:52:57 PM
 * 
 * Copyright 2007-2016 SolarNetwork.net Dev Team
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

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Collection;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.LoggerFactory;

/**
 * A {@link DataSource} proxy that catches connection errors in order to handle
 * the exceptions from {@link SQLExceptionHandlers} registered with the system.
 * 
 * @author matt
 * @version 1.0
 */
public class SQLExceptionHandlerDataSourceProxy implements DataSource {

	private final DataSource delegate;
	private final BundleContext bundleContext;

	private final org.slf4j.Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * Construct with values.
	 * 
	 * @param delegate
	 *        The {@link DataSource} to delegate to.
	 * @param bundleContext
	 *        The bundle context to use. May be <em>null</em>.
	 */
	public SQLExceptionHandlerDataSourceProxy(DataSource delegate, BundleContext bundleContext) {
		super();
		this.delegate = delegate;
		this.bundleContext = bundleContext;
	}

	/**
	 * Get the delegate DataSource.
	 * 
	 * @return The delegate.
	 */
	public DataSource getDelegate() {
		return delegate;
	}

	@Override
	public Connection getConnection() throws SQLException {
		try {
			return delegate.getConnection();
		} catch ( final SQLException e ) {
			if ( bundleContext != null ) {
				doWithHandlers(new SQLExceptionHandlerCallback() {

					@Override
					public void doWithHandler(SQLExceptionHandler handler) throws Exception {
						handler.handleGetConnectionException(e);
					}
				});
			}
			throw e;
		}
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		try {
			return delegate.getConnection(username, password);
		} catch ( final SQLException e ) {
			if ( bundleContext != null ) {
				doWithHandlers(new SQLExceptionHandlerCallback() {

					@Override
					public void doWithHandler(SQLExceptionHandler handler) throws Exception {
						handler.handleGetConnectionException(e);
					}
				});
			}
			throw e;
		}
	}

	static interface SQLExceptionHandlerCallback {

		void doWithHandler(SQLExceptionHandler handler) throws Exception;

	}

	private void doWithHandlers(SQLExceptionHandlerCallback callback) throws SQLException {
		Collection<ServiceReference<SQLExceptionHandler>> handlerRefs;
		try {
			handlerRefs = bundleContext.getServiceReferences(SQLExceptionHandler.class, null);
		} catch ( InvalidSyntaxException e ) {
			log.error("Exception getting SQLExceptionHandler references from bundle context", e);
			return;
		}
		for ( ServiceReference<SQLExceptionHandler> ref : handlerRefs ) {
			SQLExceptionHandler handler = bundleContext.getService(ref);
			if ( handler != null ) {
				try {
					callback.doWithHandler(handler);
				} catch ( SQLException e ) {
					throw e;
				} catch ( Exception e ) {
					log.error("SQLExceptionHandler threw exception", e);
				}
			}
		}
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return delegate.getLogWriter();
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return delegate.unwrap(iface);
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		delegate.setLogWriter(out);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return delegate.isWrapperFor(iface);
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		delegate.setLoginTimeout(seconds);
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		return delegate.getLoginTimeout();
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return delegate.getParentLogger();
	}

}
