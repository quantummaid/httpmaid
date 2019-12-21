package de.quantummaid.httpmaid.documentation.xx_marshalling;

import de.quantummaid.httpmaid.HttpMaid;

import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.http.headers.HeaderValueWithComment.fromString;

public class ApiUnMarshallingExample {

    public static void main(String[] args) {
        //Showcase start apiUnMarshalling
        final HttpMaid httpMaid = anHttpMaid()
                //.get("/form", theResource("form.html")) TODO: theResource gibts ned
                .post("/submit", (request, response) -> response.setBody(request.bodyString()))
                /*.configured(toMarshallBodiesBy() //TODO
                        .unmarshallingContentTypeInRequests(fromString("application/json")).with(body -> GSON.fromJson(body, Map.class))
                        .marshallingContentTypeInResponses(fromString("application/json")).with(map -> GSON.toJson(map))
                        .usingTheDefaultContentType(fromString("application/json")))

                 */
                .build();
        //Showcase end apiUnMarshalling
    }
}
