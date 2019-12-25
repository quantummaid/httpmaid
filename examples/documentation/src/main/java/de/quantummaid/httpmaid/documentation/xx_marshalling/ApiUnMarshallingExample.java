package de.quantummaid.httpmaid.documentation.xx_marshalling;

import com.google.gson.Gson;
import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.marshalling.MarshallingConfigurators;

import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.http.headers.ContentType.fromString;
import static de.quantummaid.httpmaid.marshalling.MarshallingConfigurators.toMarshallContentType;
import static de.quantummaid.httpmaid.purejavaendpoint.PureJavaEndpoint.pureJavaEndpointFor;

public class ApiUnMarshallingExample {
    private static final Gson GSON = new Gson();

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        //Showcase start apiUnMarshalling
        final HttpMaid httpMaid = anHttpMaid()
                .get("/form", (request, response) -> response.setJavaResourceAsBody("form.html"))
                .post("/submit", (request, response) -> response.setBody(request.bodyString()))
                .configured(toMarshallContentType(fromString("application/json"), string -> GSON.fromJson(string, Map.class), GSON::toJson))
                .configured(MarshallingConfigurators.toMarshallByDefaultUsingTheContentType(fromString("application/json")))
                .build();
        //Showcase end apiUnMarshalling
        pureJavaEndpointFor(httpMaid).listeningOnThePort(1337);
    }
}
