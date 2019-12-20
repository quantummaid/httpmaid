# Endpoints
In order to actually serve a configured HttpMaid instance, you have to start an endpoint. The endpoint you choose
depends on your specific deployment requirements.
## Pure Java
The most lightweight endpoint is called `PureJavaEndpoint`. It relies entirely on native Java and does not need any dependencies
other than HttpMaid core.
```java
pureJavaEndpointFor(httpMaid).listeningOnThePort(1337);
```
It does not support websockets.
## Jetty
Another option is the Jetty endpoint:
```java
jettyEndpointFor(httpMaid).listeningOnThePort(1337);
```
If you intend to use websockets, you can go with the Jetty-for-websockets variant:
```java
jettyEndpointWithWebSocketsSupportFor(httpMaid).listeningOnThePort(1337);
```
## Servlet
If you intend to host your application using standard Java servlet technology, you can go with the servlet endpoint.
Using it depends on how your servlet is loaded.
If you want to provide the servlet instance programmatically to your servlet engine, just create a new servlet like this:
```java
final HttpServlet servlet = servletEndpointFor(httpMaid);
```

If instead you need to provide your servlet engine with a class that it can construct by itself,
you can extend the `ServletEndpoint` and provide the HttpMaid instance via the super constructor:
```java
public final class MyServlet extends ServletEndpoint {
    
    private static final HttpMaid httpMaid = aLowLevelHttpMaid() ...
    
    public MyServlet() {
        super(httpMaid);
    }
}
```

There exists a version of it supporting websockets. It can be used in the same manner as the normal version:
```java
final HttpServlet servlet = webSocketAwareHttpMaidServlet(httpMaid);
```
or
```java
public final class MyServlet extends WebSocketAwareHttpMaidServlet {
    
    private static final HttpMaid httpMaid = aLowLevelHttpMaid() ...
    
    public MyServlet() {
        super(httpMaid);
    }
}
```