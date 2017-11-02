/* ==================================================================
 * TomcatJdbcPoolDataSourceFactoryBean.java - May 30, 2011 3:57:24 PM
 * 
 * Copyright 2007-2011 SolarNetwork.net Dev Team
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
 * $Id$
 * ==================================================================
 */

package net.solarnetwork.dao.jdbc;

import java.lang.management.ManagementFactory;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Properties;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.sql.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.osgi.framework.BundleContext;
import org.osgi.service.jdbc.DataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.ObjectFactory;

/**
 * {@link javax.sql.DataSource} using a Tomcat Pool.
 * 
 * @author matt
 * @version 1.2
 */
public class TomcatJdbcPoolDataSourceFactoryBean
		implements ObjectFactory<javax.sql.DataSource>, FactoryBean<javax.sql.DataSource> {

	private DataSourceFactory dataSourceFactory;
	private Properties dataSourceProperties;
	private PoolProperties poolProperties;
	private BundleContext bundleContext;
	private boolean sqlExceptionHandlerSupport = false;
	private boolean registerJmx = false;
	private String jmxName = "SolarNetwork";

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Override
	public DataSource getObject() throws BeansException {
		try {
			DataSource ds = dataSourceFactory.createDataSource(dataSourceProperties);
			if ( bundleContext != null && isSqlExceptionHandlerSupport() ) {
				ds = new SQLExceptionHandlerDataSourceProxy(ds, bundleContext);
			}
			poolProperties.setDataSource(ds);
		} catch ( SQLException e ) {
			throw new BeanInstantiationException(DataSource.class, "SQL exception", e);
		}
		org.apache.tomcat.jdbc.pool.DataSource ds = new org.apache.tomcat.jdbc.pool.DataSource(
				poolProperties);
		if ( registerJmx ) {
			MBeanServer server = ManagementFactory.getPlatformMBeanServer();
			Hashtable<String, String> props = new Hashtable<String, String>();
			props.put("type", "DataSource");
			props.put("name", jmxName);
			try {
				ObjectName name = ObjectName.getInstance("net.solarnetwork.dao.jdbc", props);
				server.registerMBean(ds.getPool().getJmxPool(), name);
			} catch ( MalformedObjectNameException e ) {
				log.error("Error registering JDBC connection pool with JMX: {}", e.getMessage());
			} catch ( InstanceAlreadyExistsException e ) {
				log.error("Error registering JDBC connection pool with JMX: {}", e.getMessage());
			} catch ( MBeanRegistrationException e ) {
				log.error("Error registering JDBC connection pool with JMX: {}", e.getMessage());
			} catch ( NotCompliantMBeanException e ) {
				log.error("Error registering JDBC connection pool with JMX: {}", e.getMessage());
			}
		}
		return ds;
	}

	@Override
	public Class<?> getObjectType() {
		return javax.sql.DataSource.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	public DataSourceFactory getDataSourceFactory() {
		return dataSourceFactory;
	}

	public void setDataSourceFactory(DataSourceFactory dataSourceFactory) {
		this.dataSourceFactory = dataSourceFactory;
	}

	public PoolProperties getPoolProperties() {
		return poolProperties;
	}

	public void setPoolProperties(PoolProperties poolProperties) {
		this.poolProperties = poolProperties;
	}

	public Properties getDataSourceProperties() {
		return dataSourceProperties;
	}

	public void setDataSourceProperties(Properties dataSourceProperties) {
		this.dataSourceProperties = dataSourceProperties;
	}

	public BundleContext getBundleContext() {
		return bundleContext;
	}

	public void setBundleContext(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}

	public boolean isSqlExceptionHandlerSupport() {
		return sqlExceptionHandlerSupport;
	}

	public void setSqlExceptionHandlerSupport(boolean sqlExceptionHandlerSupport) {
		this.sqlExceptionHandlerSupport = sqlExceptionHandlerSupport;
	}

	/**
	 * Get the flag if the pool should be registered with the platform JMX
	 * server.
	 * 
	 * @return boolean
	 */
	public boolean isRegisterJmx() {
		return registerJmx;
	}

	/**
	 * Control if the pool should be registered with the platform MBean server.
	 * 
	 * @param registerJmx
	 *        {@literal true} to register with the platform MBean server;
	 *        defaults to {@literal false}
	 */
	public void setRegisterJmx(boolean registerJmx) {
		this.registerJmx = registerJmx;
	}

	/**
	 * Get the JMX name to use when registering with the platform JMX server.
	 * 
	 * @return the JMX name to use
	 */
	public String getJmxName() {
		return jmxName;
	}

	/**
	 * Set the JMX name to use when registering with the platform JMX server.
	 * 
	 * @param jmxName
	 *        the JMX name to use
	 */
	public void setJmxName(String jmxName) {
		this.jmxName = jmxName;
	}

}
