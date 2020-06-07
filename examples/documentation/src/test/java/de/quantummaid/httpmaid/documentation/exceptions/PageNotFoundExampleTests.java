package de.quantummaid.httpmaid.documentation.exceptions;

import de.quantummaid.httpmaid.Configurators;
import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.client.HttpClientRequestBuilder;
import de.quantummaid.httpmaid.client.SimpleHttpResponseObject;
import de.quantummaid.httpmaid.documentation.support.Deployer;
import org.junit.jupiter.api.Test;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.client.HttpClientRequest.aGetRequestToThePath;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class PageNotFoundExampleTests {

    @Test
    public void pageNotFoundExample() {
        //Showcase start pageNotFoundExample
        final HttpMaid httpMaid = anHttpMaid()
                .post("/hello", (request, response) -> response.setBody("Hello!"))
                .configured(Configurators.toHandlePageNotFoundUsing((request, response) -> response.setStatus(403)))
                .build();
        //Showcase end pageNotFoundExample

        Deployer.test(httpMaid, client -> {
            final HttpClientRequestBuilder<SimpleHttpResponseObject> request = aGetRequestToThePath("/notexisting");
            final SimpleHttpResponseObject response = client.issue(request);
            final int statusCode = response.getStatusCode();
            assertThat(statusCode, is(403));
            final String body = response.getBody();
            assertThat(body, is(""));
        });
    }
}
