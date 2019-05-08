# Spring Expression Service Changelog

## 2019-05-09 - v1.0.1

 * Change expression compile mode from `IMMEDIATE` to the system default, which is controlled via
   the `spring.expression.compiler.mode` system property. Compilation was seen to be working in a
   SolarNode OSGi environment, but upon second invocation of a compiled expression a
   `java.lang.VerifyError: Expecting to find object/array on stack` exception was thrown.
