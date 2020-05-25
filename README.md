[![CII Best Practices](https://bestpractices.coreinfrastructure.org/projects/3875/badge)](https://bestpractices.coreinfrastructure.org/projects/3875)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=de.quantummaid%3Ahttpmaid-parent&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=de.quantummaid%3Ahttpmaid-parent)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=de.quantummaid%3Ahttpmaid-parent&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=de.quantummaid%3Ahttpmaid-parent)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=de.quantummaid%3Ahttpmaid-parent&metric=security_rating)](https://sonarcloud.io/dashboard?id=de.quantummaid%3Ahttpmaid-parent)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=de.quantummaid%3Ahttpmaid-parent&metric=alert_status)](https://sonarcloud.io/dashboard?id=de.quantummaid%3Ahttpmaid-parent)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=de.quantummaid%3Ahttpmaid-parent&metric=sqale_index)](https://sonarcloud.io/dashboard?id=de.quantummaid%3Ahttpmaid-parent)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=de.quantummaid%3Ahttpmaid-parent&metric=bugs)](https://sonarcloud.io/dashboard?id=de.quantummaid%3Ahttpmaid-parent)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=de.quantummaid%3Ahttpmaid-parent&metric=code_smells)](https://sonarcloud.io/dashboard?id=de.quantummaid%3Ahttpmaid-parent)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=de.quantummaid%3Ahttpmaid-parent&metric=coverage)](https://sonarcloud.io/dashboard?id=de.quantummaid%3Ahttpmaid-parent)
[![Last Commit](https://img.shields.io/github/last-commit/quantummaid/httpmaid)](https://github.com/quantummaid/httpmaid)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.quantummaid.httpmaid/core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.quantummaid.httpmaid/core)
[![Code Size](https://img.shields.io/github/languages/code-size/quantummaid/httpmaid)](https://github.com/quantummaid/httpmaid)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Slack](https://img.shields.io/badge/chat%20on-Slack-brightgreen)](https://quantummaid.de/community.html)
[![Gitter](https://img.shields.io/badge/chat%20on-Gitter-brightgreen)](https://gitter.im/quantum-maid-framework/community)
[![Twitter](https://img.shields.io/twitter/follow/quantummaid)](https://twitter.com/quantummaid)


<img src="httpmaid_logo.png" align="left"/>

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

## Getting started
HttpMaid is part of the QuantumMaid framework. You can find easy-to-follow and
interesting tutorials [here](https://github.com/quantummaid/quantummaid-tutorials/blob/master/README.md).

The HttpMaid documentation can be found [here](https://quantummaid.de/docs.html).

## Get in touch
Feel free to join us on [Slack](https://quantummaid.de/community.html)
or [Gitter](https://gitter.im/quantum-maid-framework/community) to ask questions, give feedback or just discuss software
architecture with the team behind HttpMaid. Also, don't forget to visit our [website](https://quantummaid.de) and follow
us on [Twitter](https://twitter.com/quantummaid)!
