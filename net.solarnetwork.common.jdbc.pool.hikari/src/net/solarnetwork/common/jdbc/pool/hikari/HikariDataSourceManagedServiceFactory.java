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
import org.osgi.framework.InvalidSyntaxException;
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
import net.solarnetwork.dao.jdbc.DataSourcePingTest;
import net.solarnetwork.domain.PingTest;
import net.solarnetwork.util.ClassUtils;

/**
 * Managed service factory for {@link HikariDataSource} instances.
 * 
 * @author matt
 * @version 1.0
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

	public static final Set<String> DEFAULT_IGNORED_PROPERTY_PREFIXES = Collections
			.unmodifiableSet(new LinkedHashSet<>(Arrays.asList("felix.", "service.", "uid")));

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
				Hashtable<String, Object> instanceProps = new Hashtable<>();
				Properties dsProps = new Properties();
				Enumeration<String> keys = properties.keys();
				DataSourceFactory dsFactory = null;
				String pingTestQuery = null;
				while ( keys.hasMoreElements() ) {
					String key = keys.nextElement();
					if ( key.equals(DATA_SOURCE_FACTORY_FILTER_PROPERTY) ) {
						final String dsFactoryFilter = (String) properties.get(key);
						Collection<ServiceReference<DataSourceFactory>> dsFactoryRefs;
						try {
							dsFactoryRefs = bundleContext.getServiceReferences(DataSourceFactory.class,
									dsFactoryFilter);
							Iterator<ServiceReference<DataSourceFactory>> itr = (dsFactoryRefs != null
									? dsFactoryRefs.iterator()
									: null);
							ServiceReference<DataSourceFactory> dsFactoryRef = itr.next();
							dsFactory = bundleContext.getService(dsFactoryRef);
							if ( dsFactory == null ) {
								throw new NoSuchElementException();
							}
						} catch ( NoSuchElementException e ) {
							throw new RuntimeException(
									"No DataSourceFactory available for service filter "
											+ dsFactoryFilter);
						} catch ( InvalidSyntaxException e ) {
							throw new RuntimeException(
									"DataSourceFactory service filter invalid: " + e.getMessage(), e);
						}
					} else if ( key.equals(DATA_SOURCE_PING_TEST_QUERY_PROPERTY) ) {
						pingTestQuery = (String) properties.get(key);
					} else if ( key.startsWith(DATA_SOURCE_PROPERTY_PREFIX) ) {
						dsProps.put(key.substring(DATA_SOURCE_PROPERTY_PREFIX.length()),
								properties.get(key));
					} else if ( key.startsWith(SERVICE_PROPERTY_PREFIX) ) {
						instanceProps.put(key.substring(SERVICE_PROPERTY_PREFIX.length()),
								properties.get(key));
					} else if ( ignoredPropertyPrefixes != null
							&& ignoredPropertyPrefixes.stream().anyMatch(s -> key.startsWith(s)) ) {
						// ignore this prop
						log.debug("Ignoring DataSource property {}", key);
					} else {
						poolProps.put(key, properties.get(key));
					}
				}
				HikariConfig config = new HikariConfig(poolProps);
				if ( dsFactory != null ) {
					try {
						config.setDataSource(dsFactory.createDataSource(dsProps));
					} catch ( SQLException e ) {
						throw new RuntimeException(
								"Error creating managed DataSource from DataSourceFactory: "
										+ e.getMessage(),
								e);
					}
				}

				String jdbcUrl = (String) dsProps.get("url");
				HikariDataSource ds = new HikariDataSource(config);

				log.info("Registering pooled JDBC DataSource for {} with props {} and pool settings {}",
						jdbcUrl, instanceProps, poolProps);
				ServiceRegistration<DataSource> reg = bundleContext.registerService(DataSource.class, ds,
						instanceProps);

				ServiceRegistration<PingTest> pingTestReg = null;
				if ( pingTestQuery != null ) {
					DataSourcePingTest pingTest = new DataSourcePingTest(ds, pingTestQuery);
					pingTestReg = bundleContext.registerService(PingTest.class, pingTest, null);
				}

				return new ManagedHikariDataSource(ds, reg, jdbcUrl, instanceProps, pingTestReg);
			} else {
				// apply updates
				HikariConfigMXBean bean = v.dataSource.getHikariConfigMXBean();
				Map<String, Object> p = new HashMap<>(8);
				Hashtable<String, Object> instanceProps = new Hashtable<>();
				Enumeration<String> keys = properties.keys();
				while ( keys.hasMoreElements() ) {
					String key = keys.nextElement();
					if ( key.equals(DATA_SOURCE_FACTORY_FILTER_PROPERTY) ) {
						// TODO: handle DS change?
					} else if ( key.startsWith(SERVICE_PROPERTY_PREFIX) ) {
						instanceProps.put(key.substring(SERVICE_PROPERTY_PREFIX.length()),
								properties.get(key));
					} else if ( key.startsWith(DATA_SOURCE_PROPERTY_PREFIX) ) {
						// TODO: handle DS prop change?
					} else if ( ignoredPropertyPrefixes != null
							&& ignoredPropertyPrefixes.stream().anyMatch(s -> key.startsWith(s)) ) {
						// ignore this prop
						log.debug("Ignoring DataSource property {}", key);
					} else {
						p.put(key, properties.get(key));
					}
				}
				if ( !instanceProps.isEmpty() ) {
					v.serviceReg.setProperties(instanceProps);
				}
				ClassUtils.setBeanProperties(bean, p, true);
				return v;
			}
		});
	}

	private void doDelete(String pid) {
		instances.computeIfPresent(pid, (k, v) -> {
			log.info("Unregistering pooled JDBC DataSource for {} with props {}", v.jdbcUrl,
					v.serviceProps);
			if ( v.pingTestReg != null ) {
				try {
					v.pingTestReg.unregister();
				} catch ( IllegalStateException e ) {
					// shouldn't be here, just ignore
				} catch ( Throwable t ) {
					log.warn("Error unregistering PingTest for DataSource {}: {}", pid, t.toString(), t);
				}
			}
			try {
				v.serviceReg.unregister();
			} catch ( IllegalStateException e ) {
				// shouldn't be here, just ignore
			} catch ( Throwable t ) {
				log.warn("Error unregistering HikariCP DataSource {}: {}", pid, t.toString(), t);
			} finally {
				try {
					v.dataSource.close();
				} catch ( Throwable t2 ) {
					log.warn("Error closing HikariCP DataSource {}: {}", pid, t2.toString(), t2);
				}
			}
			return null;
		});
	}

	private static final class ManagedHikariDataSource {

		private final HikariDataSource dataSource;
		private final ServiceRegistration<DataSource> serviceReg;
		private final String jdbcUrl;
		private final Map<String, ?> serviceProps;
		private final ServiceRegistration<PingTest> pingTestReg;

		private ManagedHikariDataSource(HikariDataSource dataSource,
				ServiceRegistration<DataSource> serviceReg, String jdbcUrl, Map<String, ?> serviceProps,
				ServiceRegistration<PingTest> pingTestReg) {
			super();
			this.dataSource = dataSource;
			this.serviceReg = serviceReg;
			this.jdbcUrl = jdbcUrl;
			this.serviceProps = serviceProps;
			this.pingTestReg = pingTestReg;
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
