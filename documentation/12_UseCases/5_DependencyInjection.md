# Usecases V: Dependency Injection

HttpMaid uses InjectMaid for dependency injection.

## Start-Up Checks
HttpMaid will attempt to instantiate all use cases on start-up time to make sure that
all use cases can be instantiated. If you do not want this check to occur, you can disable
it like this:
<!---[CodeSnippet] (disableStartupChecksExample)-->
```java
final HttpMaid httpMaid = anHttpMaid()
        /*...*/
        .disableStartupChecks()
        .build();
```
 
