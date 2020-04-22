# HttpMaid
HttpMaid directly publishes your business logic as an HTTP endpoint.
It's non-invasive, secure and flexible.

Let's see an example:


```
final HttpMaid httpMaid = HttpMaid.anHttpMaid()
        .get("/api/hello", (request, response) -> {
            httpResponse.setBody("Hello World!");
            httpResponse.setStatus(200);
        })
        .build();
```

Once your usecase is more complicated than just saying hello, you want to focus on implementing it
instead of dealing with protocol details.

Let's say we have the usecase of sending an email:

```
public class SendEmail {

    public Receipt sendEmail(final Email email) {
        final String trackingId = send(email.sender, email.receiver, email.subject, email.body);
        final String timestamp = String.valueOf(Instant.now().toEpochMilli());

        return new Receipt(trackingId, timestamp);
    }
}
```

Now we can expose this usecase using HttpMaid:

```
final HttpMaid httpMaid = HttpMaid.anHttpMaid()
        .post("/api/sendEmail", SendEmail.class)
        .build();
```
It's that simple - and stays that simple, even when things get more complicated.
Look [here](https://github.com/quantummaid/quantummaid-tutorials/blob/master/basic-tutorial/README.md) for a complete tutorial.

## What is HttpMaid doing for you?

> Good architecture is less about the decisions you make and more about the decisions you defer making.

HttpMaid allows you to write your usecases decoupled from the underlying hazards of an HTTP/REST infrastructure.
Stop debating tiresome questions like:
 
- "*Should it be a `PUT` or a `POST`*?"
- "*Is the username coming from the request body, the JWT token or a plain text header value?*"
- "*Are we talking Json, YAML, XML or a custom (binary?) content type?*"

You can't possibly know the answer until you've faced the customer. And then she might just change
her mind.

## Other features

Besides allowing you to easily export usecases, HttpMaid offers the following features:

* dependency injection with built-in support for [Guice](https://github.com/google/guice) and [Dagger](https://dagger.dev/)
* seamless endpoint integrations such as 
    - AWS Lambda
    - Jetty
    - Servlet
* authentication and authorization using JWT
* predefined CORS configurations
* multipart request handling

## Why another HTTP framework?

> The goal of refactoring is to actively counteract the natural increase in the degree of chaos.

We did not find any framework that would allow us to develop a web application and claim in good conscience
that our business logic does not depend on the underlying HTTP server, persistence layer or (de-)serialization mechanism
(also referred to as *infrastructure code* in Domain-Driven Design).
