package de.quantummaid.httpmaid.documentation.xx_marshalling;

import de.quantummaid.httpmaid.HttpMaid;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;

public class FormMarshallingExampleStep1 {

    public static void main(String[] args) {
        //Showcase start formMarshallingStep1
        final HttpMaid httpMaid = anHttpMaid()
                //.get("/form", theResource("form.html")) TODO: theResource gibts ned
                .post("/submit", (request, response) -> response.setBody(request.bodyString()))
                .build();
        //Showcase end formMarshallingStep1
    }
}
