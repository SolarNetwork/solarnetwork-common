# SolarNetwork log4j2 support

This plugin provides log4j2 extensions.

# EventAdmin Appender

A `EventAdminAppender` log4j2 Appender is included, that publishes logs to an OSGi
`org.osgi.service.event.EventAdmin` service. Other plugins can then subscribe to the log events to
perform actions on them.

The `EventAdmin` topic log messages are published to is `net/solarnetwork/Log`. The following event
properties are provided:

| Property | Type | Description |
|:---------|:-----|:------------|
| `ts`       | Long     | The log timestamp, as a millisecond epoch. |
| `level`    | String   | The log level name, e.g. `TRACE`, `DEBUG`, `INFO`, `WARN`, `ERROR`, or `FATAL`. |
| `priority` | Integer  | The log level priority (lower values have more priority), e.g. `600`, `500`, `400`, `300`, `200`, or `100`. |
| `name`     | String   | The log name. |
| `msg`      | String   | The log message . |
| `exMsg`    | String   | An exception message, if an exception was included. |
| `exSt`     | String[] | An array of exception stack trace element values, if an exception was included. |

## Configuration

Once deployed, the appender can be configured in the `log4j.xml` configuration file like this
(showing the bare minimum):

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="WARN" packages="net.solarnetwork.common.log4j2.appender">
  <Appenders>
    <EventAdminAppender name="EventAdmin"/>
  </Appenders>
  <Loggers>
    <Root level="info">
      <AppenderRef ref="EventAdmin"/>
    </Root>
  </Loggers>
</Configuration>
```

Note the following:

 1. the `packages="net.solarnetwork.common.log4j2.appender"` added to `<Configuration>`
 2. the `<EventAdminAppender name="EventAdmin"/>` added within `<Appenders>`
 3. the `<AppenderRef ref="EventAdmin"/>` added within `<Root>`
 
This will route log messages to `EventAdmin`. From there, other plugins can do something useful with
the log events.
