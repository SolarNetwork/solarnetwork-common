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

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.sql.DataSource;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariConfigMXBean;
import com.zaxxer.hikari.HikariDataSource;
import net.solarnetwork.util.ClassUtils;

/**
 * Managed service factory for {@link HikariDataSource} instances.
 * 
 * @author matt
 * @version 1.0
 */
public class HikariDataSourceManagedServiceFactory implements ManagedServiceFactory {

	private static final String SERVICE_PROPERTY_PREFIX = "serviceProperty.";
	private final BundleContext bundleContext;
	private final Executor executor;
	private final AtomicBoolean destroyed;

	private final ConcurrentMap<String, ManagedHikariDataSource> instances;

	private final Logger log = LoggerFactory.getLogger(getClass());

	public HikariDataSourceManagedServiceFactory(BundleContext bundleContext) {
		super();
		this.bundleContext = bundleContext;
		this.destroyed = new AtomicBoolean(false);
		this.executor = ForkJoinPool.commonPool();
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
				Properties p = new Properties();
				Hashtable<String, Object> instanceProps = new Hashtable<>();
				Enumeration<String> keys = properties.keys();
				while ( keys.hasMoreElements() ) {
					String key = keys.nextElement();
					if ( key.startsWith(SERVICE_PROPERTY_PREFIX) ) {
						instanceProps.put(key.substring(SERVICE_PROPERTY_PREFIX.length()),
								properties.get(key));
					} else {
						p.put(key, properties.get(key));
					}
				}
				HikariConfig config = new HikariConfig(p);
				HikariDataSource ds = new HikariDataSource(config);

				ServiceRegistration<javax.sql.DataSource> reg = bundleContext
						.registerService(javax.sql.DataSource.class, ds, instanceProps);
				return new ManagedHikariDataSource(ds, reg);
			} else {
				// apply updates
				HikariConfigMXBean bean = v.dataSource.getHikariConfigMXBean();
				Map<String, Object> p = new HashMap<>(8);
				Hashtable<String, Object> instanceProps = new Hashtable<>();
				Enumeration<String> keys = properties.keys();
				while ( keys.hasMoreElements() ) {
					String key = keys.nextElement();
					if ( key.startsWith(SERVICE_PROPERTY_PREFIX) ) {
						instanceProps.put(key.substring(SERVICE_PROPERTY_PREFIX.length()),
								properties.get(key));
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

		private ManagedHikariDataSource(HikariDataSource dataSource,
				ServiceRegistration<DataSource> serviceReg) {
			super();
			this.dataSource = dataSource;
			this.serviceReg = serviceReg;
		}
	}

}
