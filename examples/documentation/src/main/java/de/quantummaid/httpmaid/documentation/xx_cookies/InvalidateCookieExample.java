package de.quantummaid.httpmaid.documentation.xx_cookies;

import de.quantummaid.httpmaid.HttpMaid;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.purejavaendpoint.PureJavaEndpoint.pureJavaEndpointFor;

public class InvalidateCookieExample {

    public static void main(String[] args) {
        //Showcase start invalidateCookie
        final HttpMaid httpMaid = anHttpMaid()
                .get("/invalidate", (request, response) -> response.invalidateCookie("myCookie"))
                .build();
        //Showcase end invalidateCookie
        pureJavaEndpointFor(httpMaid).listeningOnThePort(1337);
    }
}
