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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;
import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.DataSource;
import javax.sql.PooledConnection;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.LoggerFactory;

/**
 * A {@link DataSource} proxy that catches connection errors in order to handle
 * the exceptions from {@link SQLExceptionHandler} instances registered with the
 * system.
 * 
 * @author matt
 * @version 1.0
 */
public class SQLExceptionHandlerDataSourceProxy implements DataSource, ConnectionEventListener {

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
			Connection conn = delegate.getConnection();
			return getWrappedConnection(conn);
		} catch ( final SQLException e ) {
			handleSQLException(null, e);
			throw e;
		}
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		try {
			Connection conn = delegate.getConnection(username, password);
			return getWrappedConnection(conn);
		} catch ( final SQLException e ) {
			handleSQLException(null, e);
			throw e;
		}
	}

	private Connection getWrappedConnection(Connection conn) {
		// if we have a PooledConnection we can tap into that...
		if ( conn instanceof PooledConnection ) {
			PooledConnection pooledConn = (PooledConnection) conn;
			pooledConn.addConnectionEventListener(this);
		} else {
			// not a PooledConnection, so use a Proxy to catch exceptions
			conn = wrapJdbcObjectWithProxy(conn);
		}
		return conn;
	}

	private void collectAllInterfaces(Class<?> clazz, Set<Class<?>> interfaces) {
		for ( Class<?> i : clazz.getInterfaces() ) {
			interfaces.add(i);
		}
		if ( clazz.getSuperclass() != null ) {
			collectAllInterfaces(clazz.getSuperclass(), interfaces);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T wrapJdbcObjectWithProxy(T delegate) {
		Set<Class<?>> allInterfaces = new LinkedHashSet<Class<?>>();
		collectAllInterfaces(delegate.getClass(), allInterfaces);
		Class<?>[] interfaces = allInterfaces.toArray(new Class<?>[allInterfaces.size()]);
		Object proxy = Proxy.newProxyInstance(delegate.getClass().getClassLoader(), interfaces,
				new JDBCDelegatingHandler(delegate));
		return (T) proxy;
	}

	/**
	 * An {@link InvocationHandler} for JDBC objects that looks for thrown
	 * {@link SQLException} exceptions to pass to the registered
	 * {@link SQLExceptionHandler} instacnes.
	 */
	private class JDBCDelegatingHandler implements InvocationHandler {

		private final Object delegate;

		/**
		 * Delegate all method calls to another JDBC object.
		 * 
		 * @param delegate
		 *        the delegate
		 */
		public JDBCDelegatingHandler(Object delegate) {
			super();
			this.delegate = delegate;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			Method delegateMethod = delegate.getClass().getMethod(method.getName(),
					method.getParameterTypes());
			try {
				Object res = delegateMethod.invoke(delegate, args);
				if ( res instanceof Statement ) {
					// to catch SQLExceptions thrown by statements, wrap those
					res = wrapJdbcObjectWithProxy(res);
				}
				return res;
			} catch ( InvocationTargetException e ) {
				Throwable t = e.getCause();
				if ( t instanceof SQLException ) {
					Connection conn = null;
					if ( delegate instanceof Connection ) {
						conn = (Connection) delegate;
					} else if ( delegate instanceof Statement ) {
						conn = ((Statement) delegate).getConnection();
					}
					handleSQLException(conn, (SQLException) t);
				}
				throw e.getCause();
			}
		}

	}

	@Override
	public void connectionClosed(ConnectionEvent event) {
		// nothing to do
	}

	@Override
	public void connectionErrorOccurred(ConnectionEvent event) {
		SQLException ex = event.getSQLException();
		try {
			handleSQLException((Connection) event.getSource(), ex);
		} catch ( SQLException e ) {
			log.warn("SQLException handling exception {}", ex, e);
		}
	}

	private void handleSQLException(final Connection conn, final SQLException e) throws SQLException {
		if ( e == null ) {
			return;
		}
		if ( bundleContext != null ) {
			doWithHandlers(new SQLExceptionHandlerCallback() {

				@Override
				public void doWithHandler(SQLExceptionHandler handler) throws Exception {
					if ( conn == null ) {
						handler.handleGetConnectionException(e);
					} else {
						handler.handleConnectionException(conn, e);
					}
				}
			});
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
