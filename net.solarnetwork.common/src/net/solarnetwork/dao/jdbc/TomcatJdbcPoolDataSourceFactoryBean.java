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

import java.sql.SQLException;
import java.util.Properties;
import javax.sql.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.osgi.framework.BundleContext;
import org.osgi.service.jdbc.DataSourceFactory;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.ObjectFactory;

/**
 * {@link javax.sql.DataSource} using a Tomcat Pool.
 * 
 * @author matt
 * @version 1.1
 */
public class TomcatJdbcPoolDataSourceFactoryBean
		implements ObjectFactory<javax.sql.DataSource>, FactoryBean<javax.sql.DataSource> {

	private DataSourceFactory dataSourceFactory;
	private Properties dataSourceProperties;
	private PoolProperties poolProperties;
	private BundleContext bundleContext;
	private boolean sqlExceptionHandlerSupport = false;

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
		DataSource ds = new org.apache.tomcat.jdbc.pool.DataSource(poolProperties);
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

}
