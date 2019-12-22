package de.quantummaid.httpmaid.documentation.xx_marshalling;

import de.quantummaid.httpmaid.HttpMaid;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.marshalling.MarshallingConfigurators.toUnmarshallFormUrlEncodedRequests;
import static de.quantummaid.httpmaid.purejavaendpoint.PureJavaEndpoint.pureJavaEndpointFor;

public class FormMarshallingExampleStep2 {

    public static void main(String[] args) {
        //Showcase start formMarshallingStep2
        final HttpMaid httpMaid = anHttpMaid()
                .get("/form", (request, response) -> response.setJavaResourceAsBody("form.html"))
                .post("/submit", (request, response) -> response.setBody(request.bodyString()))
                .configured(toUnmarshallFormUrlEncodedRequests())
                .build();
        //Showcase end formMarshallingStep2
        pureJavaEndpointFor(httpMaid).listeningOnThePort(1337);
    }
}
