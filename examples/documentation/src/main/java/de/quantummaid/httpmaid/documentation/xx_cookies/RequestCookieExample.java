package de.quantummaid.httpmaid.documentation.xx_cookies;

import de.quantummaid.httpmaid.HttpMaid;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.purejavaendpoint.PureJavaEndpoint.pureJavaEndpointFor;

public class RequestCookieExample {

    public static void main(String[] args) {
        //Showcase start requestCookie
        final HttpMaid httpMaid = anHttpMaid()
                .get("/get", (request, response) -> {
                    final String myCookie = request.cookies().getCookie("myCookie");
                    response.setBody("Value was: " + myCookie);
                })
                .build();
        //Showcase end requestCookie
        pureJavaEndpointFor(httpMaid).listeningOnThePort(1337);
    }
}
