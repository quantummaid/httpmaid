# Usecases I: Basics

This chapter assumes you understand basic http and HttpMaid concepts,
especially the mechanics of marshalling and unmarshalling.

So far, we introduced you how to handling requests in a very low-level fashion
using the `HttpHandler` interface in the examples.
This way, you can directly access request and response features like headers and query
parameters.
In real projects, you would call domain logic from the handlers in order to do something
productively and keep an architectural boundary between infrastructure code (HttpMaid)
and business / domain logic. Depending on which design "philosophy" you follow, you might
call the entry points into your domain logic services, usecases, or even something else.
In order to maintain consistency, throughout HttpMaid we will call this concept a *usecase*.

Let's design a simple usecase:
<!---[CodeSnippet] (pingUseCase)-->
```java
public final class PingUseCase {

    public void ping() {
        System.out.println("Ping!");
    }
}
```

As you can see, the `PingUseCase` is a POJO class with a single public method `ping()`.
Note how the class does not contain a single dependency on any infrastructure (read: HttpMaid) code.
If we want to serve this usecase via HttpMaid using the constructs we know so far,
we will probably end up with something like this:
<!---[CodeSnippet] (pingUseCaseLowLevel)-->
```java
final HttpMaid httpMaid = anHttpMaid()
        .get("/ping", (request, response) -> {
            final PingUseCase pingUseCase = new PingUseCase();
            pingUseCase.ping();
        })
        .build();
```

We registered a handler that instantiates the usecase and then calls it.
Since more complex projects might contain hundreds of usecases, registering usecases
to HttpMaid this way leads to a lot of boilerplate code
and heavily violates the *Don't repeat yourself* design principle.

# Serving usecases directly
HttpMaid mitigates these problems by offering to directly serve usecases.
Instead of calling the domain logic from handlers, you register the usecase
classes directly to HttpMaid.

<!--
In real projects, you would have to map these features to actual domain logic.
With increasing project size and complexity, managing requests and mapping them to domain
logic becomes unfeasable.
HttpMaid catches these architectural requirements by offering to serve so-called usecases.
Instead of calling the domain logic from handlers, you register it to HttpMaid
on a much higher level in the form of usecases. These are classes with one single public method
that will reflect one single feature of your application. For example:
-->

In order to do this, you need to add the `httpmaid-usecases` dependency to your project:
<!---[Dependency](groupId=de.quantummaid.httpmaid.integrations artifactId=httpmaid-usecases version)-->
```xml
<dependency>
    <groupId>de.quantummaid.httpmaid.integrations</groupId>
    <artifactId>httpmaid-usecases</artifactId>
    <version>0.9.90</version>
</dependency>
```


HttpMaid will automatically discover the dependency and support usecases.
Afterwards, you can change the configuration and shrink it
down significantly:
<!---[CodeSnippet] (pingUseCaseCorrectExample)-->
```java
final HttpMaid httpMaid = anHttpMaid()
        .get("/ping", PingUseCase.class)
        .build();
```

HttpMaid will now direct `POST` requests on the `/ping` route to the `ping()` 
usecase method. You can try this by running your application. Once you 
browse to http://localhost:1337/ping, you should see
the `Ping!` message pop up on the console.

