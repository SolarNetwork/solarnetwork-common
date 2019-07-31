/* ==================================================================
 * HikariDataSourceManagedServiceFactoryTests.java - 31/07/2019 10:59:33 am
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

package net.solarnetwork.common.jdbc.pool.hikari.test;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import javax.sql.DataSource;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.jdbc.DataSourceFactory;
import net.solarnetwork.common.jdbc.pool.hikari.HikariDataSourceManagedServiceFactory;
import net.solarnetwork.test.CallingThreadExecutorService;

/**
 * Test cases for the {@link HikariDataSourceManagedServiceFactory} class.
 * 
 * @author matt
 * @version 1.0
 */
public class HikariDataSourceManagedServiceFactoryTests {

	private BundleContext bundleContext;
	private ServiceRegistration<DataSource> dataSourceReg;
	private ServiceReference<DataSourceFactory> dataSourceFactoryRef;
	private DataSourceFactory dataSourceFactory;
	private HikariDataSourceManagedServiceFactory factory;

	@SuppressWarnings("unchecked")
	@Before
	public void setup() {
		bundleContext = EasyMock.createMock(BundleContext.class);
		dataSourceReg = EasyMock.createMock(ServiceRegistration.class);
		dataSourceFactoryRef = EasyMock.createMock(ServiceReference.class);
		dataSourceFactory = EasyMock.createMock(DataSourceFactory.class);
		factory = new HikariDataSourceManagedServiceFactory(bundleContext,
				new CallingThreadExecutorService());

	}

	@After
	public void teardown() {
		EasyMock.verify(bundleContext, dataSourceReg, dataSourceFactory);
	}

	private void replayAll() {
		EasyMock.replay(bundleContext, dataSourceReg, dataSourceFactory);
	}

	@Test
	public void configurationUpdated_newServiceWithServiceProps() throws Exception {
		// given
		final String uuid = UUID.randomUUID().toString();
		final String pid = net.solarnetwork.common.jdbc.pool.hikari.Activator.SERVICE_PID + "-" + uuid;
		final String dsFactoryFilter = "(osgi.jdbc.driver.class=org.apache.derby.jdbc.EmbeddedDriver)";
		final String jdbcUrlAttributes = "create=true";
		final String jdbcUrl = "jdbc:derby:memory:" + uuid + ";" + jdbcUrlAttributes;

		Map<String, Object> props = new LinkedHashMap<>(8);
		props.put(HikariDataSourceManagedServiceFactory.DATA_SOURCE_FACTORY_FILTER_PROPERTY,
				dsFactoryFilter);
		props.put("dataSource.jdbc.url", jdbcUrl);
		props.put("serviceProperty.db", "test");
		props.put("serviceProperty.foo", "bar");

		Capture<DataSource> dataSourceCaptor = new Capture<>();
		Capture<Dictionary<String, ?>> servicePropCaptor = new Capture<>();
		Capture<Properties> dataSourcePropCaptor = new Capture<>();

		expect(bundleContext.getServiceReferences(DataSourceFactory.class, dsFactoryFilter))
				.andReturn(Collections.singleton(dataSourceFactoryRef));

		expect(bundleContext.getService(dataSourceFactoryRef)).andReturn(dataSourceFactory);

		EmbeddedDataSource ds = new EmbeddedDataSource();
		ds.setDatabaseName("memory:" + uuid);
		ds.setConnectionAttributes(jdbcUrlAttributes);
		expect(dataSourceFactory.createDataSource(capture(dataSourcePropCaptor))).andReturn(ds);

		expect(bundleContext.registerService(eq(DataSource.class), capture(dataSourceCaptor),
				capture(servicePropCaptor))).andReturn(dataSourceReg);

		// when
		replayAll();
		factory.updated(pid, new Hashtable<String, Object>(props));

		// then
	}

}
