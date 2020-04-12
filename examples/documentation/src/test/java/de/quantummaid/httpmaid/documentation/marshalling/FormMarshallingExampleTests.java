package de.quantummaid.httpmaid.documentation.marshalling;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.client.HttpClientRequest;
import de.quantummaid.httpmaid.documentation.support.Deployer;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.client.HttpClientRequest.aPostRequestToThePath;
import static de.quantummaid.httpmaid.marshalling.MarshallingConfigurators.toUnmarshallFormUrlEncodedRequests;
import static de.quantummaid.httpmaid.purejavaendpoint.PureJavaEndpoint.pureJavaEndpointFor;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

public class FormMarshallingExampleTests {

    @Test
    public void formMarshallingExampleStep1() {
        //Showcase start formMarshallingStep1
        final HttpMaid httpMaid = anHttpMaid()
                .get("/form", (request, response) -> response.setJavaResourceAsBody("form.html"))
                .post("/submit", (request, response) -> response.setBody(request.bodyString()))
                .build();
        //Showcase end formMarshallingStep1

        Deployer.test(httpMaid, client -> {
            final String form = client.issue(HttpClientRequest.aGetRequestToThePath("/form").mappedToString());
            assertThat(form, containsString("Profession:"));
            Deployer.assertPost("/submit", "foo", "foo", client);
        });
    }

    @Test
    public void formMarshallingExampleStep2() {
        //Showcase start formMarshallingStep2
        final HttpMaid httpMaid = anHttpMaid()
                .get("/form", (request, response) -> response.setJavaResourceAsBody("form.html"))
                .post("/submit", (request, response) -> response.setBody(request.bodyString()))
                .configured(toUnmarshallFormUrlEncodedRequests())
                .build();
        //Showcase end formMarshallingStep2

        Deployer.test(httpMaid, client -> {
            final String form = client.issue(HttpClientRequest.aGetRequestToThePath("/form").mappedToString());
            assertThat(form, containsString("Profession:"));
            Deployer.assertPost("/submit", "foo", "foo", client);
        });
    }

    @Test
    public void formMarshallingExampleStep3() {
        //Showcase start formMarshallingStep3
        final HttpMaid httpMaid = anHttpMaid()
                .get("/form", (request, response) -> response.setJavaResourceAsBody("form.html"))
                .post("/submit", (request, response) -> {
                    final Map<String, Object> bodyMap = request.bodyMap();
                    final String name = (String) bodyMap.get("name");
                    final String profession = (String) bodyMap.get("profession");
                    response.setBody("Hello " + name + " and good luck as a " + profession + "!");
                })
                .configured(toUnmarshallFormUrlEncodedRequests())
                .build();
        //Showcase end formMarshallingStep3

        Deployer.test(httpMaid, client -> {
            final String form = client.issue(HttpClientRequest.aGetRequestToThePath("/form").mappedToString());
            assertThat(form, containsString("Profession:"));
            Deployer.assertPost("/submit", "name=Bob&profession=Developer", "Hello Bob and good luck as a Developer!", client);
        });
    }
}
