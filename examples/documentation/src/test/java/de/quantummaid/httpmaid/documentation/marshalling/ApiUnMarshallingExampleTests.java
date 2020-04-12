package de.quantummaid.httpmaid.documentation.marshalling;

import com.google.gson.Gson;
import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.client.HttpClientRequest;
import de.quantummaid.httpmaid.documentation.support.Deployer;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.http.headers.ContentType.fromString;
import static de.quantummaid.httpmaid.marshalling.MarshallingConfigurators.toMarshallByDefaultUsingTheContentType;
import static de.quantummaid.httpmaid.marshalling.MarshallingConfigurators.toMarshallContentType;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

public final class ApiUnMarshallingExampleTests {
    private static final Gson GSON = new Gson();

    @Test
    public void apiUnMarshallingExample() {
        //Showcase start apiUnMarshalling
        final HttpMaid httpMaid = anHttpMaid()
                .get("/form", (request, response) -> response.setJavaResourceAsBody("form.html"))
                .post("/submit", (request, response) -> response.setBody(request.bodyString()))
                .configured(toMarshallContentType(fromString("application/json"), string -> GSON.fromJson(string, Map.class), GSON::toJson))
                .configured(toMarshallByDefaultUsingTheContentType(fromString("application/json")))
                .build();
        //Showcase end apiUnMarshalling

        Deployer.test(httpMaid, client -> {
            final String form = client.issue(HttpClientRequest.aGetRequestToThePath("/form").mappedToString());
            assertThat(form, containsString("Profession:"));
        });
    }
}
