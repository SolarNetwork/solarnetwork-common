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
import static org.easymock.EasyMock.isNull;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
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
import org.easymock.CaptureType;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.jdbc.DataSourceFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import com.zaxxer.hikari.HikariDataSource;
import net.solarnetwork.common.jdbc.pool.hikari.HikariDataSourceManagedServiceFactory;
import net.solarnetwork.dao.jdbc.DataSourcePingTest;
import net.solarnetwork.service.PingTest;
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
	private ServiceRegistration<PingTest> pingTestReg;

	private String factoryPid;

	@SuppressWarnings("unchecked")
	@Before
	public void setup() {
		bundleContext = EasyMock.createMock(BundleContext.class);
		dataSourceReg = EasyMock.createMock(ServiceRegistration.class);
		dataSourceFactoryRef = EasyMock.createMock(ServiceReference.class);
		dataSourceFactory = EasyMock.createMock(DataSourceFactory.class);
		pingTestReg = EasyMock.createMock(ServiceRegistration.class);
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

	private void resetAll() {
		EasyMock.reset(bundleContext, dataSourceReg, dataSourceFactory);
	}

	@Test
	public void configurationUpdated_regisgerService() throws Exception {
		// given
		final String uuid = UUID.randomUUID().toString();
		final String pid = net.solarnetwork.common.jdbc.pool.hikari.Activator.SERVICE_PID + "-" + uuid;
		final String dsFactoryFilter = "(osgi.jdbc.driver.class=org.apache.derby.jdbc.EmbeddedDriver)";
		final String jdbcUrlAttributes = "create=true";
		final String jdbcUrl = "jdbc:derby:memory:" + uuid + ";" + jdbcUrlAttributes;

		Map<String, Object> props = new LinkedHashMap<>(8);
		props.put(HikariDataSourceManagedServiceFactory.DATA_SOURCE_FACTORY_FILTER_PROPERTY,
				dsFactoryFilter);
		props.put("dataSource.url", jdbcUrl);
		props.put("dataSource.user", "user");
		props.put("dataSource.password", "password");
		props.put("serviceProperty.db", "test");
		props.put("serviceProperty.foo", "bar");

		Capture<Properties> dataSourcePropCaptor = new Capture<>();
		Capture<DataSource> dataSourceCaptor = new Capture<>();
		Capture<Dictionary<String, ?>> servicePropCaptor = new Capture<>();

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
		Properties dataSourceProps = dataSourcePropCaptor.getValue();
		assertThat("DataSource URL", dataSourceProps, hasEntry("url", jdbcUrl));
		assertThat("DataSource user", dataSourceProps, hasEntry("user", "user"));
		assertThat("DataSource password", dataSourceProps, hasEntry("password", "password"));

		DataSource dataSource = dataSourceCaptor.getValue();
		assertThat("Registered DataSource is Hikari pool", dataSource,
				instanceOf(HikariDataSource.class));
		assertThat("Hikari DataSource is from DataSourceFactory",
				((HikariDataSource) dataSource).getDataSource(), sameInstance(ds));

		Dictionary<String, ?> serviceProps = servicePropCaptor.getValue();
		assertThat("Service propery values", serviceProps.get("db"), equalTo("test"));
		assertThat("Service propery values", serviceProps.get("foo"), equalTo("bar"));

		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		java.sql.Date result = jdbcTemplate.queryForObject("VALUES CURRENT_DATE", java.sql.Date.class);
		assertThat("Date returned from JDBC", result, notNullValue());

		// save PID for other tests
		factoryPid = pid;
	}

	@Test
	public void configurationUpdated_regisgerServiceWithRanking() throws Exception {
		// given
		final String uuid = UUID.randomUUID().toString();
		final String pid = net.solarnetwork.common.jdbc.pool.hikari.Activator.SERVICE_PID + "-" + uuid;
		final String dsFactoryFilter = "(osgi.jdbc.driver.class=org.apache.derby.jdbc.EmbeddedDriver)";
		final String jdbcUrlAttributes = "create=true";
		final String jdbcUrl = "jdbc:derby:memory:" + uuid + ";" + jdbcUrlAttributes;

		Map<String, Object> props = new LinkedHashMap<>(8);
		props.put(HikariDataSourceManagedServiceFactory.DATA_SOURCE_FACTORY_FILTER_PROPERTY,
				dsFactoryFilter);
		props.put("dataSource.url", jdbcUrl);
		props.put("dataSource.user", "user");
		props.put("dataSource.password", "password");
		props.put("serviceProperty.db", "test");
		props.put("serviceProperty.foo", "bar");
		props.put("serviceProperty.service.ranking", "-10");

		Capture<Properties> dataSourcePropCaptor = new Capture<>();
		Capture<DataSource> dataSourceCaptor = new Capture<>();
		Capture<Dictionary<String, ?>> servicePropCaptor = new Capture<>();

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
		Properties dataSourceProps = dataSourcePropCaptor.getValue();
		assertThat("DataSource URL", dataSourceProps, hasEntry("url", jdbcUrl));
		assertThat("DataSource user", dataSourceProps, hasEntry("user", "user"));
		assertThat("DataSource password", dataSourceProps, hasEntry("password", "password"));

		DataSource dataSource = dataSourceCaptor.getValue();
		assertThat("Registered DataSource is Hikari pool", dataSource,
				instanceOf(HikariDataSource.class));
		assertThat("Hikari DataSource is from DataSourceFactory",
				((HikariDataSource) dataSource).getDataSource(), sameInstance(ds));

		Dictionary<String, ?> serviceProps = servicePropCaptor.getValue();
		assertThat("Service propery value db", serviceProps.get("db"), equalTo("test"));
		assertThat("Service propery value foo", serviceProps.get("foo"), equalTo("bar"));
		assertThat("Service propery value service.rank", serviceProps.get("service.ranking"),
				equalTo(-10));

		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		java.sql.Date result = jdbcTemplate.queryForObject("VALUES CURRENT_DATE", java.sql.Date.class);
		assertThat("Date returned from JDBC", result, notNullValue());

		// save PID for other tests
		factoryPid = pid;
	}

	@Test
	public void configurationUpdated_regisgerServiceWithPingTest() throws Exception {
		// given
		final String uuid = UUID.randomUUID().toString();
		final String pid = net.solarnetwork.common.jdbc.pool.hikari.Activator.SERVICE_PID + "-" + uuid;
		final String dsFactoryFilter = "(osgi.jdbc.driver.class=org.apache.derby.jdbc.EmbeddedDriver)";
		final String jdbcUrlAttributes = "create=true";
		final String jdbcUrl = "jdbc:derby:memory:" + uuid + ";" + jdbcUrlAttributes;
		final String pingTestQuery = "VALUES CURRENT_DATE";

		Map<String, Object> props = new LinkedHashMap<>(8);
		props.put(HikariDataSourceManagedServiceFactory.DATA_SOURCE_FACTORY_FILTER_PROPERTY,
				dsFactoryFilter);
		props.put(HikariDataSourceManagedServiceFactory.DATA_SOURCE_PING_TEST_QUERY_PROPERTY,
				pingTestQuery);
		props.put("dataSource.url", jdbcUrl);
		props.put("dataSource.user", "user");
		props.put("dataSource.password", "password");
		props.put("serviceProperty.db", "test");
		props.put("serviceProperty.foo", "bar");

		Capture<Properties> dataSourcePropCaptor = new Capture<>();
		Capture<DataSource> dataSourceCaptor = new Capture<>();
		Capture<Dictionary<String, ?>> servicePropCaptor = new Capture<>();
		Capture<PingTest> pingTestCaptor = new Capture<>();

		expect(bundleContext.getServiceReferences(DataSourceFactory.class, dsFactoryFilter))
				.andReturn(Collections.singleton(dataSourceFactoryRef));

		expect(bundleContext.getService(dataSourceFactoryRef)).andReturn(dataSourceFactory);

		EmbeddedDataSource ds = new EmbeddedDataSource();
		ds.setDatabaseName("memory:" + uuid);
		ds.setConnectionAttributes(jdbcUrlAttributes);
		expect(dataSourceFactory.createDataSource(capture(dataSourcePropCaptor))).andReturn(ds);

		expect(bundleContext.registerService(eq(DataSource.class), capture(dataSourceCaptor),
				capture(servicePropCaptor))).andReturn(dataSourceReg);

		expect(bundleContext.registerService(eq(PingTest.class), capture(pingTestCaptor), isNull()))
				.andReturn(pingTestReg);

		// when
		replayAll();
		factory.updated(pid, new Hashtable<String, Object>(props));

		// then
		Properties dataSourceProps = dataSourcePropCaptor.getValue();
		assertThat("DataSource URL", dataSourceProps, hasEntry("url", jdbcUrl));
		assertThat("DataSource user", dataSourceProps, hasEntry("user", "user"));
		assertThat("DataSource password", dataSourceProps, hasEntry("password", "password"));

		DataSource dataSource = dataSourceCaptor.getValue();
		assertThat("Registered DataSource is Hikari pool", dataSource,
				instanceOf(HikariDataSource.class));
		assertThat("Hikari DataSource is from DataSourceFactory",
				((HikariDataSource) dataSource).getDataSource(), sameInstance(ds));

		Dictionary<String, ?> serviceProps = servicePropCaptor.getValue();
		assertThat("Service propery values", serviceProps.get("db"), equalTo("test"));
		assertThat("Service propery values", serviceProps.get("foo"), equalTo("bar"));

		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		java.sql.Date result = jdbcTemplate.queryForObject(pingTestQuery, java.sql.Date.class);
		assertThat("Date returned from JDBC", result, notNullValue());

		PingTest pingTest = pingTestCaptor.getValue();
		assertThat("Registered PingTest is DataSourcePingTest", pingTest,
				instanceOf(DataSourcePingTest.class));
		DataSourcePingTest dsPingTest = (DataSourcePingTest) pingTest;
		assertThat("PingTest query", dsPingTest.getQuery(), equalTo(pingTestQuery));

		// save PID for other tests
		factoryPid = pid;
	}

	@Test
	public void configurationUpdated_regisgerServiceViaDelayedListener() throws Exception {
		// given
		final String uuid = UUID.randomUUID().toString();
		final String pid = net.solarnetwork.common.jdbc.pool.hikari.Activator.SERVICE_PID + "-" + uuid;
		final String dsFactoryFilter = "(osgi.jdbc.driver.class=org.apache.derby.jdbc.EmbeddedDriver)";
		final String jdbcUrlAttributes = "create=true";
		final String jdbcUrl = "jdbc:derby:memory:" + uuid + ";" + jdbcUrlAttributes;

		Map<String, Object> props = new LinkedHashMap<>(8);
		props.put(HikariDataSourceManagedServiceFactory.DATA_SOURCE_FACTORY_FILTER_PROPERTY,
				dsFactoryFilter);
		props.put("dataSource.url", jdbcUrl);
		props.put("dataSource.user", "user");
		props.put("dataSource.password", "password");
		props.put("serviceProperty.db", "test");
		props.put("serviceProperty.foo", "bar");

		Capture<ServiceListener> dataSourceFactoryListenerCaptor = new Capture<>(CaptureType.ALL);
		Capture<Properties> dataSourcePropCaptor = new Capture<>();
		Capture<DataSource> dataSourceCaptor = new Capture<>();
		Capture<Dictionary<String, ?>> servicePropCaptor = new Capture<>();

		// no DataSourceFactory registered at first
		expect(bundleContext.getServiceReferences(DataSourceFactory.class, dsFactoryFilter))
				.andReturn(Collections.emptySet());

		// so register ServiceListener to wait for event
		bundleContext.addServiceListener(capture(dataSourceFactoryListenerCaptor),
				eq("(&(objectClass=org.osgi.service.jdbc.DataSourceFactory)" + dsFactoryFilter + ")"));

		// after REGISTERED event is received, the process continues
		expect(bundleContext.getService(dataSourceFactoryRef)).andReturn(dataSourceFactory);

		EmbeddedDataSource ds = new EmbeddedDataSource();
		ds.setDatabaseName("memory:" + uuid);
		ds.setConnectionAttributes(jdbcUrlAttributes);
		expect(dataSourceFactory.createDataSource(capture(dataSourcePropCaptor))).andReturn(ds);

		expect(bundleContext.registerService(eq(DataSource.class), capture(dataSourceCaptor),
				capture(servicePropCaptor))).andReturn(dataSourceReg);

		// finally, clean up and remove listener
		bundleContext.removeServiceListener(capture(dataSourceFactoryListenerCaptor));

		// when
		replayAll();
		factory.updated(pid, new Hashtable<String, Object>(props));

		// followed by a service event
		ServiceListener dataSourceFactoryListener = dataSourceFactoryListenerCaptor.getValues().get(0);
		assertThat("ServiceListener for DataSourceFactory available", dataSourceFactoryListener,
				notNullValue());
		ServiceEvent event = new ServiceEvent(ServiceEvent.REGISTERED, dataSourceFactoryRef);
		dataSourceFactoryListener.serviceChanged(event);

		// then
		Properties dataSourceProps = dataSourcePropCaptor.getValue();
		assertThat("DataSource URL", dataSourceProps, hasEntry("url", jdbcUrl));
		assertThat("DataSource user", dataSourceProps, hasEntry("user", "user"));
		assertThat("DataSource password", dataSourceProps, hasEntry("password", "password"));

		DataSource dataSource = dataSourceCaptor.getValue();
		assertThat("Registered DataSource is Hikari pool", dataSource,
				instanceOf(HikariDataSource.class));
		assertThat("Hikari DataSource is from DataSourceFactory",
				((HikariDataSource) dataSource).getDataSource(), sameInstance(ds));

		Dictionary<String, ?> serviceProps = servicePropCaptor.getValue();
		assertThat("Service propery values", serviceProps.get("db"), equalTo("test"));
		assertThat("Service propery values", serviceProps.get("foo"), equalTo("bar"));

		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		java.sql.Date result = jdbcTemplate.queryForObject("VALUES CURRENT_DATE", java.sql.Date.class);
		assertThat("Date returned from JDBC", result, notNullValue());

		assertThat("2nd ServiceListener captured from removal",
				dataSourceFactoryListenerCaptor.getValues().size(), equalTo(2));
		assertThat("Same ServiceListener removed as added",
				dataSourceFactoryListenerCaptor.getValues().get(1),
				sameInstance(dataSourceFactoryListenerCaptor.getValues().get(0)));

		// save PID for other tests
		factoryPid = pid;
	}

	@Test
	public void configurationUpdated_unregisgerService() throws Exception {
		// given
		configurationUpdated_regisgerService();
		resetAll();

		dataSourceReg.unregister();

		// when
		replayAll();
		factory.deleted(factoryPid);

		// then
	}

}
