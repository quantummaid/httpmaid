# Quickstart

This guide assumes you are using Maven, but it will work with Gradle etc. accordingly.
Add the following dependency to your `pom.xml` file:
<!---[CodeSnippet] (httpmaiddependency)-->
```xml
<dependency>
    <groupId>de.quantummaid.httpmaid</groupId>
    <artifactId>core</artifactId>
    <version>0.9.101</version>
</dependency>
```

The most basic usage of HttpMaid is to start a server with a single route `/hello` 
and let it answer with a formal greeting `"Hi."`.
The following code shows the necessary Java code:
<!---[CodeSnippet] (quickstart)-->
```java
final HttpMaid httpMaid = anHttpMaid()
        .get("/hello", (request, response) -> response.setBody("Hi."))
        .build();
final PureJavaEndpoint endpoint = pureJavaEndpointFor(httpMaid).listeningOnThePort(1337);
```
You can navigate your browser to http://localhost:1337/hello 
or you can execute the following curl command:
```bash
$ curl http://localhost:1337/hello
```
In both cases you will be greeted with a pleasant message: `"Hi."`

## Explanation
The example configured a single route:
<!---[CodeSnippet] (quickstartPart1)-->
```java
final HttpMaid httpMaid = anHttpMaid()
        .get("/hello", (request, response) -> response.setBody("Hi."))
        .build();
```

This will respond to all GET requests to the `/hello` route with the string `"Hi!"`

HttpMaid by itself is infrastructure agnostic. When you want to actually serve
requests, you have to combine it with an endpoint. There are endpoints for
different ways to serve http requests, like using a standalone Jetty instance
or integrating into a servlet engine. In our example, we will facilitate the
so-called `PureJavaEndpoint`, which is implemented on top of the native Java http
server and has therefore a minimal dependency footprint:
<!---[CodeSnippet] (quickstartPart2)-->
```java
final PureJavaEndpoint endpoint = pureJavaEndpointFor(httpMaid).listeningOnThePort(1337);
```

You can now point a web browser of your choice to http://localhost:1337/hello and
see the expected `"Hi!"` message.

When you are serving http requests, you can easily clean up all resources like so:
<!---[CodeSnippet] (quickstartPart3)-->
```java
httpMaid.close();
```

This will close all resources, including the endpoint.

