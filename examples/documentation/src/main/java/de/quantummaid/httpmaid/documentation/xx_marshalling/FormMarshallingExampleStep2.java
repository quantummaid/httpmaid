package de.quantummaid.httpmaid.documentation.xx_marshalling;

import de.quantummaid.httpmaid.HttpMaid;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.marshalling.MarshallingConfigurators.toUnmarshallFormUrlEncodedRequests;

public class FormMarshallingExampleStep2 {

    public static void main(String[] args) {
        //Showcase start formMarshallingStep2
        final HttpMaid httpMaid = anHttpMaid()
                //.get("/form", theResource("form.html")) //TODO theResource gibts ned
                .post("/submit", (request, response) -> response.setBody(request.bodyString()))
                .configured(toUnmarshallFormUrlEncodedRequests())
                .build();
        //Showcase end formMarshallingStep2
    }
}
