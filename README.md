[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.quantummaid.httpmaid/core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.quantummaid.httpmaid/core)
[![CII Best Practices](https://bestpractices.coreinfrastructure.org/projects/2892/badge)](https://bestpractices.coreinfrastructure.org/projects/2892)

<img src="httpmaid_logo.png" align="left"/>

# HttpMaid

HttpMaid is an http framework that allows you to "just publish my business logic as HTTP endpoint".
It's non-invasive, flexible and ultra-extendable.

<br/>
<br/>
<br/>
<br/>
<br/>

Let's see some low-level example:

```
final HttpMaid httpMaid = HttpMaid.anHttpMaid()
        .get("/api/hello", new HttpHandler() {
            @Override
            public void handle(final HttpRequest request, final HttpResponse httpResponse) {
                httpResponse.setBody("Hello World!");
                httpResponse.setStatus(200);
            }
        })
        .build();
```

Treat the HttpMaid instance as a description of your endpoints: we have here a request handler, for the path `api/hello`, 
with the request method `GET`, which handles the request by setting the response to the String `Hello World!` and the 
status to 200. Pretty descriptive, right?

This way of saying hello gives you full control over the HTTP protocol. Once your UseCase is more complicated than just 
saying hello, you want to focus on implementing it instead of dealing with protocol details.

Let's say we have a UseCase of sending an email:

```
public class SendEmail {
    private final EmailService emailService;

    //    @Inject if you so wish
    public SendEmail(final EmailService emailService) {
        this.emailService = emailService;
    }

    public Receipt sendEmail(final Email email) {
        final String trackingId = emailService.send(email.sender, email.receiver, email.subject, email.body);
        final String timestamp = String.valueOf(Instant.now().toEpochMilli());

        return new Receipt(trackingId, timestamp);
    }
}
```

Now we can expose this UseCase using HttpMaid:

```
final HttpMaid useCaseDrivenHttpMaid = HttpMaid.anHttpMaid()
        .post("/api/sendEmail", SendEmail.class)
        .configured(toUseMapMaid(MAP_MAID))
        .configured(toCreateUseCaseInstancesUsing(INJECTOR::getInstance))
        .build();
```

Want to extract the sender from the authorization, the receiver and subject from path and 
the email contents from the request body?

```java
final HttpMaid useCaseDrivenHttpMaid = HttpMaid.anHttpMaid()
        .post("/api/sendEmail/<receiver>/<subject>", SendEmail.class)
        .configured(toUseMapMaid(MAP_MAID))
        .configured(toCreateUseCaseInstancesUsing(INJECTOR::getInstance))
        .configured(toEnrichTheIntermediateMapWithAllPathParameters())
        .configured(toAuthenticateRequestsUsing(request -> {
            final Optional<String> jwtToken = request.headers().getHeader("Authorization");
            final Optional<String> userEmail = TOKEN_SERVICE.decrypt(jwtToken);
            return userEmail; 
        }).afterBodyProcessing())
        .configured(toEnrichTheIntermediateMapUsing((map, request) -> {
            final Optional<String> userEmail = request.authenticationInformationAs(String.class);
            userEmail.ifPresent(email -> {
                map.put("sender", email);
            });
        }))
        .build();
```

Want to use the very same UseCase to handle SendEmail events coming from an SNS topic? Refer to the 
"Event Driven HttpMaid" (TODO) part of the README to see what modifications should be done to the httpMaid object to 
achieve that. 

## What is HttpMaid doing for you?

> A good architecture is less about the decisions you make and more about the decisions you defer making.

HttpMaid allows you to write your UseCases decoupled from the underlying hazards of an Http/Rest infrastructure.

Debating questions like:
 
- "Should it be a PUT or a POST?"
- "Is the Username coming from the request body, the JWT token or a plain text header value?"
- "Are we talking JSON, YAML, XML or a custom (binary?) ContentType?"

is tiresome because you can't possibly know the answer until you've faced the customer. Furthermore, he might just change
his mind.   

And that's what HttMaid is doing for you.

## Other Features

Besides providing you with the described interface to build http request handlers, expose UseCases or handle events, 
HttpMaid offers following features:

* Several endpoint integrations such as 
        - AWS Lambda
        - Jetty
        - Spark
        - Servlet
* Integration with (de)serialization framework MapMaid
* Websockets
* Predefined CORS configurations
* Predefined MultiPart support 

## Why another HTTP framework?

_The goal of refactoring is to actively counteract the natural increase in the degree of chaos_ 

We did not find any framework that would allow us to develop a web application and claim in good conscience that its 
business logic does not depend on the underlying HTTP server, persistence layer or (de)serialization mechanism (also
referred to as "infrastructure code" in DDD).

## Getting started

 Add the dependency:

```
<dependency>
    <groupId>de.quantummaid.httpmaid</groupId>
    <artifactId>core</artifactId>
    <version>${httmaid.version}</version>
</dependency>
```

Configure HttpMaid with an HttpHandler and expose as a PureJavaEndpoint

```
public class Application {
    public static void main(String[] args) {
        final HttpMaid httpMaid = HttpMaid.anHttpMaid()
                .get("/api/hello", new HttpHandler() {
                    @Override
                    public void handle(final HttpRequest request, final HttpResponse httpResponse) {
                        httpResponse.setBody("Hello World!");
                        httpResponse.setStatus(200);
                    }
                })
                .build();

        PureJavaEndpoint.pureJavaEndpointFor(httpMaid).listeningOnThePort(1337);
    }
}
```

Run the example and try

```
    curl http://localhost:1337/api/hello
```

## Changing the endpoint

Since HttpMaid separates the _how_ is from the _what_, you can focus on defining _what_ your http endpoints should do and decide on _how_ to serve them best separately, based on the requirements of your infrastructure.
 
To expose the same httpMaid instance using a Jetty endpoint, include the following dependency:

```
<dependency>
    <groupId>de.quantummaid.httpmaid.integrations</groupId>
    <artifactId>httpmaid-jetty</artifactId>
    <version>${httpmaid.version}</version>
</dependency>
```

And replace the `PureJavaEndpoint` line with:

```
    JettyEndpoint.jettyEndpointFor(httpMaid).listeningOnThePort(1337);
```

Restart the application and enjoy the benefits of Jetty.

## Documentation
<!---[TOC](./docs)-->
1. [Introduction](docs/01_Introduction.md)
2. [Quick start](docs/02_QuickStart.md)
3. [Routing](docs/03_Routing.md)
4. [Handling requests](docs/04_HandlingRequests.md)
5. [Cookies](docs/05_Cookies.md)
6. [Serving files](docs/06_ServingFiles.md)
7. [Configuring and logging](docs/07_ConfiguringAndLogging.md)
8. [Exceptions](docs/08_Exceptions.md)
9. [Authentication and authorization](docs/09_AuthenticationAndAuthorization.md)
10. [Multipart uploads](docs/10_MultipartUploads.md)
11. Marshalling
    1. [Marshalling forms](docs/11_Marshalling/1_MarshallingForms.md)
    2. [Marshalling a p is](docs/11_Marshalling/2_MarshallingAPIs.md)
12. Use cases
    1. [Basics](docs/12_UseCases/1_Basics.md)
    2. [Object mapping](docs/12_UseCases/2_ObjectMapping.md)
    3. [Validation](docs/12_UseCases/3_Validation.md)
    4. [Advanced topics](docs/12_UseCases/4_AdvancedTopics.md)
13. [C o r s](docs/13_CORS.md)
14. [Endpoints](docs/14_Endpoints.md)
15. [Client](docs/15_Client.md)
16. [Dependency management](docs/16_DependencyManagement.md)
17. [F a q](docs/17_Faq.md)
<!---EndOfToc-->