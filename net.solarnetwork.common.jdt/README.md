# Java Compiler Service — JDT

This project contains an OSGi bundle that provides a `net.solarnetwork.util.JavaCompiler` service
implemented with the Eclipse JDT library.

## OSGi service properties

The bundle will register a `net.solarnetwork.util.JavaCompiler` service when deployed, with the
following service properties:

| Property | Description |
|:---------|:------------|
| `impl`   | Will be set to `jdt`. |
