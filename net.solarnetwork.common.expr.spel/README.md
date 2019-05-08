# Spring Expression Service

## Compiler mode

The expression compile mode can be controlled via the `spring.expression.compiler.mode` system property,
set to one of the following:

| Value       | Description |
|-------------|-------------|
| `OFF`       | Do not compile expressions. |
| `IMMEDIATE` | Compile expressions right away. |
| `MIXED`     | Automatically compile expressions after some number of executions, falling back to no compilation if compilation fails. |
