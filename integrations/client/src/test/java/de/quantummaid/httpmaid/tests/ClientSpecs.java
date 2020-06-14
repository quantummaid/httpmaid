package de.quantummaid.httpmaid.tests;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.client.HttpMaidClient;
import de.quantummaid.httpmaid.tests.givenwhenthen.FreePortPool;
import de.quantummaid.httpmaid.tests.givenwhenthen.Given;
import org.junit.jupiter.api.Test;

import java.util.List;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.client.HttpClientRequest.*;
import static de.quantummaid.httpmaid.client.HttpMaidClient.aHttpMaidClientForTheHost;
import static de.quantummaid.httpmaid.endpoint.purejavaendpoint.PureJavaEndpoint.pureJavaEndpointFor;
import static de.quantummaid.httpmaid.tests.givenwhenthen.Given.givenTheHttpMaidServer;
import static java.lang.String.join;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class ClientSpecs {

    @Test
    public void clientCanIssueAGetRequest() {
        givenTheHttpMaidServer(
                anHttpMaid()
                        .get("/test", (request, response) -> response.setBody("foo"))
                        .build()
        )
                .when().aRequestIsMade(aGetRequestToThePath("/test"))
                .theResponseBodyWas("foo");
    }

    @Test
    public void clientCanIssueAPostRequest() {
        givenTheHttpMaidServer(
                anHttpMaid()
                        .post("/test", (request, response) -> response.setBody("foo"))
                        .build()
        )
                .when().aRequestIsMade(aPostRequestToThePath("/test"))
                .theResponseBodyWas("foo");
    }

    @Test
    public void clientCanIssueAPutRequest() {
        givenTheHttpMaidServer(
                anHttpMaid()
                        .put("/test", (request, response) -> response.setBody("foo"))
                        .build()
        )
                .when().aRequestIsMade(aPutRequestToThePath("/test"))
                .theResponseBodyWas("foo");
    }

    @Test
    public void clientCanIssueADeleteRequest() {
        givenTheHttpMaidServer(
                anHttpMaid()
                        .delete("/test", (request, response) -> response.setBody("foo"))
                        .build()
        )
                .when().aRequestIsMade(aDeleteRequestToThePath("/test"))
                .theResponseBodyWas("foo");
    }

    @Test
    public void clientCanExplicitlyAddQueryParameters() {
        givenTheHttpMaidServer(
                anHttpMaid()
                        .get("/test", (request, response) -> {
                            final String queryParameter = request.queryParameters().getQueryParameter("foo");
                            response.setBody(queryParameter);
                        })
                        .build()
        )
                .when().aRequestIsMade(aGetRequestToThePath("/test").withQueryParameter("foo", "bar"))
                .theResponseBodyWas("bar");
    }

    @Test
    public void clientCanImplicitlyAddQueryParameters() {
        givenTheHttpMaidServer(
                anHttpMaid()
                        .get("/test", (request, response) -> {
                            final String queryParameter = request.queryParameters().getQueryParameter("foo");
                            response.setBody(queryParameter);
                        })
                        .build()
        )
                .when().aRequestIsMade(aGetRequestToThePath("/test?foo=bar"))
                .theResponseBodyWas("bar");
    }

    @Test
    public void clientCanExplicitlyAddAnEmptyQueryParameter() {
        givenTheHttpMaidServer(
                anHttpMaid()
                        .get("/test", (request, response) -> {
                            final String queryParameter = request.queryParameters().getQueryParameter("foo");
                            response.setBody(queryParameter);
                        })
                        .build()
        )
                .when().aRequestIsMade(aGetRequestToThePath("/test").withQueryParameter("foo"))
                .theResponseBodyWas("");
    }

    @Test
    public void clientCanImplicitlyAddAnEmptyQueryParameter() {
        givenTheHttpMaidServer(
                anHttpMaid()
                        .get("/test", (request, response) -> {
                            final String queryParameter = request.queryParameters().getQueryParameter("foo");
                            response.setBody(queryParameter);
                        })
                        .build()
        )
                .when().aRequestIsMade(aGetRequestToThePath("/test?foo="))
                .theResponseBodyWas("");
    }

    @Test
    public void clientCanSendSingleHeader() {
        givenTheHttpMaidServer(
                anHttpMaid()
                        .get("/", (request, response) -> {
                            final String header = request.headers().header("X-My-Header");
                            response.setBody(header);
                        })
                        .build()
        )
                .when().aRequestIsMade(aGetRequestToThePath("/").withHeader("X-My-Header", "foo"))
                .theResponseStatusCodeWas(200)
                .theResponseBodyWas("foo");
    }

    @Test
    public void clientCanSendHeaderMultipleTimesWithDistinctValues() {
        givenTheHttpMaidServer(
                anHttpMaid()
                        .get("/", (request, response) -> {
                            final List<String> headers = request.headers().allValuesFor("X-My-Header");
                            response.setBody(join(" / ", headers));
                        })
                        .build()
        )
                .when().aRequestIsMade(aGetRequestToThePath("/")
                .withHeader("X-My-Header", "foo").withHeader("X-My-Header", "bar"))
                .theResponseBodyWas("foo / bar");
    }

    @Test
    public void clientResponseHasAUserFriendlyDescription() {
        givenTheHttpMaidServer(
                anHttpMaid()
                        .get("/test", (request, response) -> {
                            response.setStatus(203);
                            response.setBody("Foobar");
                            response.addHeader("HeaderName", "HeaderValue");
                        })
                        .build()
        )
                .when().aRequestIsMade(aGetRequestToThePath("/test"))
                .theResponseDescriptionContains(" HTTP Response ")
                .theResponseDescriptionContains(" Status Code ")
                .theResponseDescriptionContains(" 203 ")
                .theResponseDescriptionContains(" Headers ")
                .theResponseDescriptionContains(" headername = [HeaderValue] ")
                .theResponseDescriptionContains(" Body ")
                .theResponseDescriptionContains(" Foobar ");
    }

    @Test
    public void emptyBodyValuesWithMapMaidDoNotCauseProblems() {
        Given.givenAnHttpServer()
                .when().aRequestIsMadeWithAMapMaidClient(aPostRequestToThePath("/test"))
                .theServerReceivedARequestToThePath("/test");
    }

    @Test
    public void emptyReturnValuesAreNotNull() {
        try (final HttpMaid httpMaid = HttpMaid.anHttpMaid()
                .get("/", (request, response) -> response.setBody(""))
                .build()) {
            final int port = FreePortPool.freePort();
            pureJavaEndpointFor(httpMaid).listeningOnThePort(port);

            final HttpMaidClient client = aHttpMaidClientForTheHost("localhost")
                    .withThePort(port)
                    .viaHttp()
                    .build();
            final String response = client.issue(aGetRequestToThePath("/").mappedToString());
            assertThat(response, is(""));
        }
    }
}
