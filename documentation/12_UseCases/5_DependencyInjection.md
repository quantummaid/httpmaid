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
 

## Support for third-party dependency injection
HttpMaid offers integrations for the following dependency injection frameworks:

### Guice
HttpMaid supports dependency injection using the [Guice framework](https://github.com/google/guice).
In order to use it, the following dependency needs to be added to the project:

<!---[CodeSnippet] (guicedependency)-->
```xml
<dependency>
    <groupId>de.quantummaid.httpmaid.integrations</groupId>
    <artifactId>httpmaid-guice</artifactId>
    <version>1.0.48</version>
</dependency>
```

The `GuiceConfigurators` class provides several configurator methods:

#### toCreateUseCaseInstancesUsingGuice(Module... modules)
Configures HttpMaid to create a new Guice injector and use it to instantiate usecases.
It will add all provided modules to the injector.
The injector will look at all registered usecase classes and try to find a single public constructor.
If it does not find a single public constructor, it will fail to initialize.

#### toCreateUseCaseInstancesUsingGuice(Injector injector)
Configures HttpMaid to use the provided Guice injector to instantiate usecases.
It will not alter the injector in any way.


### Dagger
HttpMaid supports dependency injection using the [Dagger framework](https://dagger.dev/).
In order to use it, the following dependency needs to be added to the project:

<!---[CodeSnippet] (daggerdependency)-->
```xml
<dependency>
    <groupId>de.quantummaid.httpmaid.integrations</groupId>
    <artifactId>httpmaid-dagger</artifactId>
    <version>1.0.48</version>
</dependency>
```

The `DaggerConfigurators` class provides several configurator methods:

#### toCreateUseCaseInstancesUsingDagger()
Configures HttpMaid to create a look for generated Dagger factories and use them to instantiate usecases.

#### toCreateUseCaseInstancesUsingDagger(NamePattern factoryClassNamePattern, String factoryConstructorName, NamePattern factoryMethodNamePattern)
Configures HttpMaid to create a look for generated Dagger factories and use them to instantiate usecases.
The provided parameters can be used to configure the expected names of the Dagger factory classes, the expected name of the factory method
to instantiate the Dagger factory and the expected name of the factory method used to actually instantiate the usecase. 

#### toCreateUseCaseInstancesUsingDagger(DaggerFactoryFinder factoryFinder)
Configures HttpMaid to create a look for generated Dagger factories and use them to instantiate usecases.
The way Dagger factories are discovered can be configured by implementing the `DaggerFactoryFinder` parameter.

<!---[Nav]-->
[&larr;](4_Enriching.md)&nbsp;&nbsp;&nbsp;[Overview](../../README.md)&nbsp;&nbsp;&nbsp;[&rarr;](../13_CORS.md)