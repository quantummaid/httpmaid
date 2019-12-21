package de.quantummaid.httpmaid.documentation.xx_marshalling;

import de.quantummaid.httpmaid.HttpMaid;

import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.marshalling.MarshallingConfigurators.toUnmarshallFormUrlEncodedRequests;

public class FormMarshallingExampleStep3 {

    public static void main(String[] args) {
        //Showcase start formMarshallingStep3
        final HttpMaid httpMaid = anHttpMaid()
                //.get("/form", theResource("form.html")) TODO: theResource gibts ned
                .post("/submit", (request, response) -> {
                    final Map<String, Object> bodyMap = request.bodyMap();
                    final String name = (String) bodyMap.get("name");
                    final String profession = (String) bodyMap.get("profession");
                    response.setBody("Hello " + name + " and good luck as a " + profession + "!");
                })
                .configured(toUnmarshallFormUrlEncodedRequests())
                .build();
        //Showcase end formMarshallingStep3
    }
}
