 # Log4j 2 to Log4j 2 Core
 
 This project is a fragment bundle on `org.apache.logging.log4j.api` that imports
 the `org.apache.logging.log4j.core.impl` package, so service loading can be avoided
 by adding the following system property to the runtime:
 
 ```
-Dlog4j2.provider=org.apache.logging.log4j.core.impl.Log4jProvider
```
