package de.quantummaid.httpmaid.documentation.xx_marshalling;

import de.quantummaid.httpmaid.HttpMaid;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.purejavaendpoint.PureJavaEndpoint.pureJavaEndpointFor;

public class ApiUnMarshallingExample {

    public static void main(String[] args) {
        //Showcase start apiUnMarshalling
        final HttpMaid httpMaid = anHttpMaid()
                .get("/form", (request, response) -> response.setJavaResourceAsBody("form.html"))
                .post("/submit", (request, response) -> response.setBody(request.bodyString()))
                /*.configured(toMarshallBodiesBy() //TODO
                        .unmarshallingContentTypeInRequests(fromString("application/json")).with(body -> GSON.fromJson(body, Map.class))
                        .marshallingContentTypeInResponses(fromString("application/json")).with(map -> GSON.toJson(map))
                        .usingTheDefaultContentType(fromString("application/json")))

                 */
                .build();
        //Showcase end apiUnMarshalling
        pureJavaEndpointFor(httpMaid).listeningOnThePort(1337);
    }
}
