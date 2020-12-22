# Usecases V: Dependency Injection
Until now, we assumed that all usecase classes have a public constructor with
zero arguments and HttpMaid would call this constructor when instantiating the 
usecases. Of course, this assumption is often not feasible. Serious projects
oftentimes facilitate dependency injection frameworks and/or the usecase classes
have dependencies like database objects that need to be provided in the constructor.
It is very easy to reflect these requirements in the HttpMaid configuration.
In order to configure usecase instantiation to your needs and e.g. register
the injector of your choice, the `UseCaseConfigurators.toCreateUseCaseInstancesUsing()`
configurator method exists. If for example you would like
to register a Guice injector, the configuration would look like this:
<!---[CodeSnippet] (dependencyInjectionSample)-->
```java
final HttpMaid httpMaid = anHttpMaid()
        /*...*/
        .configured(toCreateUseCaseInstancesUsing(injector::instantiate))
        .build();
```

## Start-Up Checks
HttpMaid will attempt to instantiate all use cases on start-up time to make sure that
all use cases can be instantiated. If you do not want this check to occur, you can disable
it like this:
<!---[CodeSnippet] (disableStartupChecksExample)-->
```java
final HttpMaid httpMaid = anHttpMaid()
        /*...*/
        .configured(toCreateUseCaseInstancesUsing(injector))
        .disableStartupChecks()
        .build();
```
 
