/* ==================================================================
 * HikariDataSourceManagedServiceFactory.java - 26/07/2019 11:35:12 am
 * 
 * Copyright 2019 SolarNetwork.net Dev Team
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

package net.solarnetwork.common.jdbc.pool.hikari;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.sql.DataSource;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;
import org.osgi.service.jdbc.DataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariConfigMXBean;
import com.zaxxer.hikari.HikariDataSource;
import net.solarnetwork.common.osgi.service.DynamicServiceTracker;
import net.solarnetwork.dao.jdbc.DataSourcePingTest;
import net.solarnetwork.dao.jdbc.SQLExceptionHandler;
import net.solarnetwork.dao.jdbc.SQLExceptionHandlerDataSourceProxy;
import net.solarnetwork.service.PingTest;
import net.solarnetwork.util.ClassUtils;
import net.solarnetwork.util.SearchFilter;
import net.solarnetwork.util.SearchFilter.LogicOperator;
import net.solarnetwork.util.StringUtils;

/**
 * Managed service factory for {@link HikariDataSource} instances.
 * 
 * @author matt
 * @version 1.1
 */
public class HikariDataSourceManagedServiceFactory implements ManagedServiceFactory {

	/** Configuration property prefix for service instance properties. */
	public static final String SERVICE_PROPERTY_PREFIX = "serviceProperty.";

	/** Configuration property prefix for data source properties. */
	public static final String DATA_SOURCE_PROPERTY_PREFIX = "dataSource.";

	/**
	 * Configuration property for the {@link DataSourceFactory} filter to use.
	 */
	public static final String DATA_SOURCE_FACTORY_FILTER_PROPERTY = "dataSourceFactory.filter";

	/**
	 * Configuration property for the SQL to use to register a
	 * {@link DataSourcePingTest} with.
	 */
	public static final String DATA_SOURCE_PING_TEST_QUERY_PROPERTY = "pingTest.query";

	/**
	 * Configuration property for the data source to use a
	 * {@link net.solarnetwork.dao.jdbc.SQLExceptionHandlerDataSourceProxy}.
	 * 
	 * @since 1.1
	 */
	public static final String EXCEPTION_HANDLER_SUPPORT_PROPERTY = "factory.exceptionHandlerSupport";

	/** The {@code ignoredPropertyPrefixes} property default value. */
	public static final Set<String> DEFAULT_IGNORED_PROPERTY_PREFIXES = Collections.unmodifiableSet(
			new LinkedHashSet<>(Arrays.asList("factory.", "felix.", "service.", "uid")));

	private final BundleContext bundleContext;
	private final Executor executor;
	private final AtomicBoolean destroyed;
	private Set<String> ignoredPropertyPrefixes = DEFAULT_IGNORED_PROPERTY_PREFIXES;

	private final ConcurrentMap<String, ManagedHikariDataSource> instances;

	private final Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * Constructor.
	 * 
	 * <p>
	 * The {@link ForkJoinPool#commonPool()} will be used as the executor.
	 * </p>
	 * 
	 * @param bundleContext
	 *        the bundle context
	 */
	public HikariDataSourceManagedServiceFactory(BundleContext bundleContext) {
		this(bundleContext, ForkJoinPool.commonPool());
	}

	/**
	 * Constructor.
	 * 
	 * @param bundleContext
	 *        the bundle context
	 * @param executor
	 *        the executor to use
	 */
	public HikariDataSourceManagedServiceFactory(BundleContext bundleContext, Executor executor) {
		super();
		this.bundleContext = bundleContext;
		this.executor = executor;
		this.destroyed = new AtomicBoolean(false);
		this.instances = new ConcurrentHashMap<>(4, 0.9f, 1);
	}

	@Override
	public String getName() {
		return "HikariCP DataSource factory";
	}

	@Override
	public void updated(String pid, @SuppressWarnings("rawtypes") Dictionary properties)
			throws ConfigurationException {
		if ( destroyed.get() ) {
			return;
		}
		executor.execute(new Runnable() {

			@SuppressWarnings("unchecked")
			@Override
			public void run() {
				try {
					doUpdate(pid, properties);
				} catch ( Throwable t ) {
					log.error("Error applying managed HikariCP DataSource {} properties {}: {} ", pid,
							properties, t.toString(), t);
				}
			}
		});
	}

	@Override
	public void deleted(String pid) {
		if ( destroyed.compareAndSet(false, true) ) {
			executor.execute(new Runnable() {

				@Override
				public void run() {
					try {
						doDelete(pid);
					} catch ( Throwable t ) {
						log.warn("Error deleting managed HikariCP DataSource {}: {} ", pid, t.toString(),
								t);
					}
				}
			});
		}
	}

	private void doUpdate(String pid, Dictionary<String, ?> properties) {
		if ( destroyed.get() ) {
			return;
		}
		instances.compute(pid, (k, v) -> {
			if ( v == null ) {
				// create new
				Properties poolProps = new Properties();
				Hashtable<String, Object> serviceProps = new Hashtable<>();
				Properties dataSourceProps = new Properties();
				Enumeration<String> keys = properties.keys();
				String pingTestQuery = null;
				String dataSourceFactoryFilter = null;
				boolean exceptionHandlerSupport = false;
				while ( keys.hasMoreElements() ) {
					String key = keys.nextElement();
					if ( key.equals(DATA_SOURCE_FACTORY_FILTER_PROPERTY) ) {
						dataSourceFactoryFilter = (String) properties.get(key);
					} else if ( key.equals(DATA_SOURCE_PING_TEST_QUERY_PROPERTY) ) {
						pingTestQuery = (String) properties.get(key);
					} else if ( key.startsWith(DATA_SOURCE_PROPERTY_PREFIX) ) {
						dataSourceProps.put(key.substring(DATA_SOURCE_PROPERTY_PREFIX.length()),
								properties.get(key));
					} else if ( key.startsWith(SERVICE_PROPERTY_PREFIX) ) {
						String propKey = key.substring(SERVICE_PROPERTY_PREFIX.length());
						Object propVal = servicePropertyValue(propKey, properties.get(key));
						serviceProps.put(propKey, propVal);
					} else if ( key.equals(EXCEPTION_HANDLER_SUPPORT_PROPERTY) ) {
						Object propVal = properties.get(key);
						if ( propVal != null ) {
							exceptionHandlerSupport = StringUtils.parseBoolean(propVal.toString());
						}
					} else if ( ignoredPropertyPrefixes != null
							&& ignoredPropertyPrefixes.stream().anyMatch(s -> key.startsWith(s)) ) {
						// ignore this prop
						log.debug("Ignoring DataSource property {}", key);
					} else {
						poolProps.put(key, properties.get(key));
					}
				}

				String jdbcUrl = (String) dataSourceProps.get("url");
				ManagedHikariDataSource mds = new ManagedHikariDataSource(pid, dataSourceFactoryFilter,
						jdbcUrl, pingTestQuery, serviceProps, dataSourceProps, poolProps,
						exceptionHandlerSupport);
				mds.register();
				return mds;
			} else {
				synchronized ( v ) {
					// apply updates
					HikariConfigMXBean bean = v.poolDataSource.getHikariConfigMXBean();
					Map<String, Object> p = new HashMap<>(8);
					Hashtable<String, Object> serviceProps = new Hashtable<>();
					Enumeration<String> keys = properties.keys();
					while ( keys.hasMoreElements() ) {
						String key = keys.nextElement();
						if ( key.equals(DATA_SOURCE_FACTORY_FILTER_PROPERTY) ) {
							// TODO: handle change?
						} else if ( key.equals(DATA_SOURCE_PING_TEST_QUERY_PROPERTY) ) {
							// TODO: handle change?
						} else if ( key.startsWith(SERVICE_PROPERTY_PREFIX) ) {
							serviceProps.put(key.substring(SERVICE_PROPERTY_PREFIX.length()),
									properties.get(key));
						} else if ( key.startsWith(DATA_SOURCE_PROPERTY_PREFIX) ) {
							// TODO: handle change?
						} else if ( ignoredPropertyPrefixes != null
								&& ignoredPropertyPrefixes.stream().anyMatch(s -> key.startsWith(s)) ) {
							// ignore this prop
							log.debug("Ignoring DataSource property {}", key);
						} else {
							p.put(key, properties.get(key));
						}
					}
					if ( !serviceProps.isEmpty() ) {
						v.poolDataSourceReg.setProperties(serviceProps);
					}
					ClassUtils.setBeanProperties(bean, p, true);
				}
				return v;
			}
		});
	}

	/**
	 * Map well-known service keys to appropriate instance values, such as
	 * {@code Integer}.
	 * 
	 * @param propKey
	 *        the service property key; must not be {@literal null}
	 * @param object
	 *        the property value
	 * @return the property value to use
	 */
	private Object servicePropertyValue(String propKey, Object object) {
		if ( Constants.SERVICE_RANKING.equals(propKey) ) {
			if ( object instanceof Integer ) {
				return object;
			}
			try {
				return Integer.valueOf(object.toString());
			} catch ( NumberFormatException e ) {
				// ignore and continue
			}
		}
		return object;
	}

	private void doDelete(String pid) {
		instances.computeIfPresent(pid, (k, v) -> {
			v.unregister();
			return null;
		});
	}

	private final class ManagedHikariDataSource implements ServiceListener {

		private final String pid;
		private final String dataSourceFactoryFilter;
		private final String jdbcUrl;
		private final String pingTestQuery;
		private final Dictionary<String, ?> serviceProps;
		private final Properties dataSourceProps;
		private final Properties poolProps;
		private final boolean exceptionHandlerSupport;

		private DataSource dataSource;
		private HikariDataSource poolDataSource;
		private ServiceRegistration<DataSource> poolDataSourceReg;
		private ServiceRegistration<PingTest> pingTestReg;
		private boolean dataSourceFactoryListening;

		private ManagedHikariDataSource(String pid, String dataSourceFactoryFilter, String jdbcUrl,
				String pingTestQuery, Dictionary<String, ?> serviceProps, Properties dataSourceProps,
				Properties poolProps, boolean exceptionHandlerSupport) {
			super();
			this.pid = pid;
			this.dataSourceFactoryFilter = dataSourceFactoryFilter;
			this.jdbcUrl = jdbcUrl;
			this.pingTestQuery = pingTestQuery;
			this.serviceProps = serviceProps;
			this.dataSourceProps = dataSourceProps;
			this.poolProps = poolProps;
			this.exceptionHandlerSupport = exceptionHandlerSupport;
			this.dataSourceFactoryListening = false;
		}

		private boolean isRegistered() {
			return poolDataSourceReg != null;
		}

		@Override
		public void serviceChanged(ServiceEvent event) {
			if ( event.getType() == ServiceEvent.REGISTERED && !isRegistered() ) {
				Object service = bundleContext.getService(event.getServiceReference());
				if ( service instanceof DataSourceFactory ) {
					log.info("DataSourceFactory discovered for managed DataSource {} with props {}",
							jdbcUrl, serviceProps);
					executor.execute(new Runnable() {

						@Override
						public void run() {
							if ( destroyed.get() ) {
								return;
							}
							createDataSource((DataSourceFactory) service);
						}
					});
				}
			}
		}

		private synchronized void createDataSource(DataSourceFactory dataSourceFactory) {
			try {
				DataSource ds = dataSourceFactory.createDataSource(dataSourceProps);
				if ( exceptionHandlerSupport ) {
					DynamicServiceTracker<SQLExceptionHandler> handlers = new DynamicServiceTracker<>(
							bundleContext, SQLExceptionHandler.class);
					ds = new SQLExceptionHandlerDataSourceProxy(ds, handlers);
				}
				this.dataSource = ds;
				register();
			} catch ( SQLException e ) {
				log.error("Error creating managed DataSource {} from DataSourceFactory {}: {}", pid,
						dataSourceFactoryFilter, e.getMessage(), e);
			}
		}

		/**
		 * Create the pooled DataSource and register it with the OSGi runtime.
		 * 
		 * <p>
		 * If {@code dataSourceFactoryFilter} is configured, this will obtain a
		 * DataSource from the available factory to use with the registered
		 * pooled DataSource, or if a factory isn't available wait for one to
		 * become available before registering the pooled DataSource.
		 * </p>
		 */
		private synchronized void register() {
			if ( isRegistered() ) {
				return;
			}

			if ( dataSource == null && dataSourceFactoryFilter != null ) {
				createDataSourceOrListenForFactory();
				return;
			}

			log.info("Creating DataSource to [{}] with service props {}", jdbcUrl, serviceProps);
			HikariConfig poolConfig = new HikariConfig(poolProps);
			if ( dataSource != null ) {
				poolConfig.setDataSource(dataSource);
			}
			HikariDataSource ds = new HikariDataSource(poolConfig);

			log.info("Registering pooled JDBC DataSource for {} with props {} and pool settings {}",
					jdbcUrl, serviceProps, poolProps);
			ServiceRegistration<DataSource> reg = bundleContext.registerService(DataSource.class, ds,
					serviceProps);

			ServiceRegistration<PingTest> pingReg = null;
			if ( pingTestQuery != null ) {
				String pingTestId = pingTestId();
				DataSourcePingTest pingTest = new DataSourcePingTest(ds, pingTestQuery, pingTestId);
				log.info("Registering PingTest for pooled JDBC DataSource {} with props {}", jdbcUrl,
						serviceProps);
				pingReg = bundleContext.registerService(PingTest.class, pingTest, null);
			}

			this.poolDataSource = ds;
			this.poolDataSourceReg = reg;
			this.pingTestReg = pingReg;

			if ( dataSourceFactoryListening ) {
				bundleContext.removeServiceListener(this);
				dataSourceFactoryListening = false;
			}
		}

		private String pingTestId() {
			String id = pid;
			if ( serviceProps != null ) {
				Map<String, Object> m = new LinkedHashMap<>(serviceProps.size());
				for ( Enumeration<String> keys = serviceProps.keys(); keys.hasMoreElements(); ) {
					String key = keys.nextElement();
					m.put(key, serviceProps.get(key));
				}
				id = new SearchFilter(m, LogicOperator.AND).asLDAPSearchFilterString();
			}
			return String.format("%s-%s", DataSourcePingTest.class.getName(), id);
		}

		private void createDataSourceOrListenForFactory() {
			if ( dataSourceFactoryFilter == null ) {
				return;
			}
			// first try to get immediately
			DataSourceFactory dsFactory = null;
			Collection<ServiceReference<DataSourceFactory>> dsFactoryRefs;
			try {
				dsFactoryRefs = bundleContext.getServiceReferences(DataSourceFactory.class,
						dataSourceFactoryFilter);
				Iterator<ServiceReference<DataSourceFactory>> itr = (dsFactoryRefs != null
						? dsFactoryRefs.iterator()
						: null);
				ServiceReference<DataSourceFactory> dsFactoryRef = itr.next();
				dsFactory = bundleContext.getService(dsFactoryRef);
				if ( dsFactory == null ) {
					throw new NoSuchElementException();
				}
			} catch ( NoSuchElementException e ) {
				log.debug(
						"No DataSourceFactory available for service filter {}; will wait for one to be registered",
						dataSourceFactoryFilter);
			} catch ( InvalidSyntaxException e ) {
				throw new RuntimeException("DataSourceFactory service filter invalid: " + e.getMessage(),
						e);
			}

			if ( dsFactory != null ) {
				// we've got a factory, so get the DataSource and register immediately
				createDataSource(dsFactory);
			}

			if ( dataSource == null && !dataSourceFactoryListening ) {
				// no DataSource available yet; register as a listener for when it becomes available
				String filter = "(&(" + Constants.OBJECTCLASS + "=" + DataSourceFactory.class.getName()
						+ ")" + dataSourceFactoryFilter + ")";
				log.info("Registering listener for DataSourceFactory {}", filter);
				try {
					bundleContext.addServiceListener(this, filter);
					dataSourceFactoryListening = true;
				} catch ( InvalidSyntaxException e ) {
					log.error(
							"Invalid DataSourceFactory service filter {} for managed DataSource {} with props: {}",
							dataSourceFactoryFilter, jdbcUrl, serviceProps, e.toString(), e);
				}
			}
		}

		private synchronized void unregister() {
			if ( pingTestReg != null ) {
				log.info("Unregistering PingTest for pooled JDBC DataSource {} with props {}", jdbcUrl,
						serviceProps);
				try {
					pingTestReg.unregister();
				} catch ( IllegalStateException e ) {
					// shouldn't be here, just ignore
				} catch ( Throwable t ) {
					log.warn("Error unregistering PingTest for DataSource {}: {}", pid, t.toString(), t);
				} finally {
					pingTestReg = null;
				}
			}
			if ( poolDataSourceReg != null ) {
				log.info("Unregistering pooled JDBC DataSource for {} with props {}", jdbcUrl,
						serviceProps);
				try {
					poolDataSourceReg.unregister();
				} catch ( IllegalStateException e ) {
					// shouldn't be here, just ignore
				} catch ( Throwable t ) {
					log.warn("Error unregistering HikariCP DataSource {}: {}", pid, t.toString(), t);
				} finally {
					poolDataSourceReg = null;
				}
			}
			if ( poolDataSource != null ) {
				try {
					poolDataSource.close();
				} catch ( Throwable t2 ) {
					log.warn("Error closing HikariCP DataSource {}: {}", pid, t2.toString(), t2);
				} finally {
					poolDataSource = null;
				}
			}
		}

	}

	/**
	 * Configure a set of configuration property prefixes to ignore.
	 * 
	 * @param ignoredPropertyPrefixes
	 *        the prefixes to ignore
	 */
	public void setIgnoredPropertyPrefixes(Set<String> ignoredPropertyPrefixes) {
		this.ignoredPropertyPrefixes = ignoredPropertyPrefixes;
	}

}
