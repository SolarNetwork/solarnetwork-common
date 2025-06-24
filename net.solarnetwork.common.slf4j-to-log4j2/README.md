 # Slf4j 2 to Log4j 2
 
 This project is a fragment bundle on `slf4j.api` that imports
 the `org.apache.logging.slf4j` package, so service loading can be avoided
 by adding the following system property to the runtime:
 
 
 ```
-Dslf4j.provider=org.apache.logging.slf4j.SLF4JServiceProvider
```
