# Endpoints
In order to actually serve a configured HttpMaid instance, you have to start an endpoint. The endpoint you choose
depends on your specific deployment requirements.
## Pure Java
The most lightweight endpoint is called `PureJavaEndpoint`. It relies entirely on native Java and does not need any dependencies
other than HttpMaid core.
<!---[CodeSnippet] (javaEndpoint)-->
```java
PureJavaEndpoint.pureJavaEndpointFor(httpMaid).listeningOnThePort(port);
```
It does not support websockets.
## Jetty
Another option is the Jetty endpoint. To use it, add this dependency to your project:
<!---[CodeSnippet] (jettydependency)-->
```xml
<dependency>
    <groupId>de.quantummaid.httpmaid.integrations</groupId>
    <artifactId>httpmaid-jetty</artifactId>
    <version>0.9.118</version>
</dependency>
```
It can be used like this:
<!---[CodeSnippet] (jettyEndpoint)-->
```java
JettyEndpoint.jettyEndpointFor(httpMaid).listeningOnThePort(port);
```

If you need support for websockets, you this dependency instead:

<!---[CodeSnippet] (jettywebsocketsdependency)-->
```xml
<dependency>
    <groupId>de.quantummaid.httpmaid.integrations</groupId>
    <artifactId>httpmaid-jetty-websockets</artifactId>
    <version>0.9.118</version>
</dependency>
```
You can use it like this:
<!---[CodeSnippet] (jettyWebsocketEndpoint)-->
```java
JettyWebsocketEndpoint.jettyWebsocketEndpoint(httpMaid, port);
```

## AWS Lambda with API Gateway
HttpMaid can be deployed as a [serverless AWS Lambda function](https://aws.amazon.com/lambda/) and handle
[AWS API Gateway](https://aws.amazon.com/api-gateway/) HTTP events.
In order to do this, you need to add this dependency to your project:
<!---[CodeSnippet] (awsdependency)-->
```xml
<dependency>
    <groupId>de.quantummaid.httpmaid.integrations</groupId>
    <artifactId>httpmaid-awslambda</artifactId>
    <version>0.9.118</version>
</dependency>
```
Afterwards, you can create a class to delegate AWS Lambda events to HttpMaid:

<!---[CodeSnippet] (lambdaFunctionSample)-->
```java
import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.awslambda.AwsLambdaEndpoint;

import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.awslambda.AwsLambdaEndpoint.awsLambdaEndpointFor;

public final class MyLambdaFunction {
    private static final HttpMaid HTTP_MAID = anHttpMaid()
            // ...
            .build();
    private static final AwsLambdaEndpoint ENDPOINT = awsLambdaEndpointFor(HTTP_MAID);

    public Map<String, Object> handleRequest(final Map<String, Object> event) {
        return ENDPOINT.delegate(event);
    }
}
```
You can now package your application as a JAR file, upload it to AWS Lambda and configure the `MyLambdaFunction` as an entry point.
When you have done this, you can create an API in AWS API Gateway and integrate it with this Lambda function. HttpMaid
automatically supports [all possible types of API Gateway](https://docs.aws.amazon.com/apigateway/latest/developerguide/http-api-vs-rest.html).
If you want to learn more, please check out our [AWS Lambda tutorial](https://github.com/quantummaid/quantummaid-tutorials/blob/master/aws-lambda/README.md)
with in-depth explanations and step-by-step instructions for building and deployment. 

## JEE / Servlet
If you intend to host your application using standard Java servlet technology, you can go with the servlet endpoint.
Just add the following dependency:
<!---[CodeSnippet] (servletdependency)-->
```xml
<dependency>
    <groupId>de.quantummaid.httpmaid.integrations</groupId>
    <artifactId>httpmaid-servlet</artifactId>
    <version>0.9.118</version>
</dependency>
```

Using it depends on how your servlet is loaded.
If you want to provide the servlet instance programmatically to your servlet engine,
you can create a new servlet like this:

<!---[CodeSnippet] (servletSample)-->
```java
final HttpServlet servlet = ServletEndpoint.servletEndpointFor(httpMaid);
```

If you instead need to provide your servlet engine with a class that it can construct by itself,
you can extend the `ServletEndpoint` and provide the HttpMaid instance via the super constructor:

<!---[CodeSnippet] (servletStaticSample)-->
```java
import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.servlet.ServletEndpoint;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;

public class MyServletEndpoint extends ServletEndpoint {
    private static final HttpMaid HTTP_MAID = anHttpMaid()
            // ...
            .build();

    public MyServletEndpoint() {
        super(HTTP_MAID);
    }
}
```
