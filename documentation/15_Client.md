# HttpMaid Client

Additionally to HttpMaid's main server functionality, it can also be used as a http client.
It should be noted that this client arose a from a necessity in test code and was never
intended to be used in production code.
In access it, you need to include the client integration.
<!---[Dependency](groupId=de.quantummaid.httpmaid.integrations artifactId=httpmaid-client version)-->
```xml
<dependency>
    <groupId>de.quantummaid.httpmaid.integrations</groupId>
    <artifactId>httpmaid-client</artifactId>
    <version>0.9.144</version>
</dependency>
```
## Configuring a client
Before a client can be used, it needs to be configured. Example:
<!---[CodeSnippet] (clientExample)-->
```java
final HttpMaidClient httpMaidClient = aHttpMaidClientForTheHost("localhost")
        .withThePort(8080)
        .viaHttp()
        .build();
```


The first three configuration options deal with connecting the client to a server.
In the example, the client is configured to send all requests to port `8080` on the `example.org` host. The
protocol to use is `https` (the alternative being `http`).

There are additional methods to configure the client:

- `withBasePath()` - configures the so-called base path. A base path is a path that gets prefixed to all requests. Given for example
                     a client configured to use the base path `/rest/api` - when a request to e.g. `/contacts` gets
                     issued, the client will extend the path to `/rest/api/contacts` by prefixing with the base path.

- `withDefaultResponseMapping()` - configures how the client can map the requests' responses. When issuing requests,
the client is always explictly given a class type that the user wants the response to be mapped to. There are default implementations
for `String` and `SimpleHttpResponseObject` (which will let you access all metadata). If you intend to map the response to types other than
that, you need to provide mappings for them.

- `withResponseMapping()` - same as `withDefaultResponseMapping()`, but configures a response mapping only for a specific type.

## Using a client
A configured client can be used to issue http requests to the configured server. Examples:
<!---[CodeSnippet] (clientUsageExamples)-->
```java
final SimpleHttpResponseObject response = httpMaidClient.issue(aGetRequestToThePath("/foo"));

final String stringResponse = httpMaidClient.issue(aGetRequestToThePath("/foo").mappedToString());

final SimpleHttpResponseObject httpResponseObject = httpMaidClient.issue(
        aPostRequestToThePath("/upload")
                .withAMultipartBodyWithTheParts(
                        aPartWithTheControlName("file")
                                .withTheFileName("file.txt")
                                .withTheContent(myStream)
                )
);
```


## Using HttpMaid client and server together
Sometimes you would use a HttpMaid client to connect to a HttpMaid server running in the same JVM.
This could happen e.g. in an integration test scenario. In order to speed up the execution time
of your tests, the possibility exists to connect the client directly to your server, bypassing
all "real" http handling. You can create such a client like this:
<!---[CodeSnippet] (clientToSameHttpMaidInstanceExample)-->
```java
final HttpMaidClient connectedHttpMaidClient = aHttpMaidClientBypassingRequestsDirectlyTo(httpMaid).build();
```

