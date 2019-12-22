package de.quantummaid.httpmaid.documentation.xx_marshalling;

import de.quantummaid.httpmaid.HttpMaid;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.purejavaendpoint.PureJavaEndpoint.pureJavaEndpointFor;

public class FormMarshallingExampleStep1 {

    public static void main(String[] args) {
        //Showcase start formMarshallingStep1
        final HttpMaid httpMaid = anHttpMaid()
                .get("/form", (request, response) -> response.setJavaResourceAsBody("form.html"))
                .post("/submit", (request, response) -> response.setBody(request.bodyString()))
                .build();
        //Showcase end formMarshallingStep1
        pureJavaEndpointFor(httpMaid).listeningOnThePort(1337);
    }
}
