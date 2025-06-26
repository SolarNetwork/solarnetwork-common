# SPIfall - Service Provider Void

This plugin project advertises OSGi Service Provider Registrar and Processor
support but provides no actual implementation of those concepts. The purpose
of this is to allow plugins that require such services be present at runtime
but can be configured to avoid using the Service Provider framework, such
as through system properties. Libraries like slf4j 2 and JAXB are examples
of such plugins