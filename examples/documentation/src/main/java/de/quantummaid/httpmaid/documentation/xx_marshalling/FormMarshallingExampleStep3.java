package de.quantummaid.httpmaid.documentation.xx_marshalling;

import de.quantummaid.httpmaid.HttpMaid;

import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.marshalling.MarshallingConfigurators.toUnmarshallFormUrlEncodedRequests;
import static de.quantummaid.httpmaid.purejavaendpoint.PureJavaEndpoint.pureJavaEndpointFor;

public class FormMarshallingExampleStep3 {

    public static void main(String[] args) {
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
        pureJavaEndpointFor(httpMaid).listeningOnThePort(1337);
    }
}
