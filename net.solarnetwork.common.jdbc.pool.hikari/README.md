# HikariCP pooled JDBC DataSource

This project provides an OSGi `org.osgi.service.cm.ManagedServiceFactory` for configuring
HikariCP-based pooled JDBC `javax.sql.DataSource` services that wrap a non-pooled DataSource
obtained from a `org.osgi.service.jdbc.DataSourceFactory` service. Basically this provides a way to
configure access to JDBC databases in OSGi.

# Install

Drop the `net.solarnetwork.common.jdbc.pool.hikari-X.jar` artifact into your OSGi runtime.
It will automatically register a `org.osgi.service.cm.ManagedServiceFactory` using the PID
**net.solarnetwork.jdbc.pool.hikari**. If you combine this with something like the 
Apache Felix FileInstall bundle, then a JDBC DataSource can be configured by creating
a managed service factory configuration file with the properties outlined below.

# Configuration

Each service factory configuration supports the following overall settings:

| Property           | Description |
|:-------------------|:------------|
| `service.factoryPid` | When used with bundles like Felix FileInstall, this must be set to `net.solarnetwork.jdbc.pool.hikari`. |
| `dataSourceFactory.filter` | An OSGi filter that will resolve the `org.osgi.service.jdbc.DataSourceFactory` to use. For example, `(osgi.jdbc.driver.class=org.postgresql.Driver)` would work with the Postgres JDBC driver. |
| `dataSource.*`| JDBC DataSource property values can be configured via properties that start with `dataSource.`; the DataSource property key will be the property key minus that prefix and the DataSource property value the configured property value. Typically DataSource properties for `url`, `user`, and `password` should be configured. For example, `dataSource.url = jdbc:postgresql://dbserver/mydb` would configure the JDBC URL for a Postgres database `mydb` on `dbserver`. |
| `pingTest.query` | A SQL query that that will cause a `net.solarnetwork.dao.jdbc.DataSourcePingTest` to be registered that uses this query to validate the JDBC pool is healthy. The query must return a `java.sql.Date`. For example, `SELECT CURRENT_DATE`. |
| `serviceProperty.*` | OSGi service property values can be configured via properties that start with `serviceProperty.`; the service property key will be the property key value minus that prefix and the the service property value the configured property value. For example, `serviceProperty.db = myapp` would register a service property `db` with a value `myapp`. |
| `*`     | All other properties will be configured on the [Hikari pool configuration itself](https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby). For example, `minimumIdle = 1` or `maximumPoolSize = 20`. |

To put it all together, here's an example configuration for a Postgres database:

```
service.factoryPid = net.solarnetwork.jdbc.pool.hikari

dataSourceFactory.filter = (osgi.jdbc.driver.class=org.postgresql.Driver)

dataSource.url = jdbc:postgresql://localhost:5432/solarnetwork
dataSource.user = solarnet
dataSource.password = solarnet

pingTest.query = SELECT CURRENT_DATE

serviceProperty.db = central

minimumIdle = 1
maximumPoolSize = 20
```
