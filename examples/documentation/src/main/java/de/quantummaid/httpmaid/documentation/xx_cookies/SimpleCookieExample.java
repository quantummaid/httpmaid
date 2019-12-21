package de.quantummaid.httpmaid.documentation.xx_cookies;

import de.quantummaid.httpmaid.HttpMaid;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.purejavaendpoint.PureJavaEndpoint.pureJavaEndpointFor;

public class SimpleCookieExample {

    public static void main(String[] args) {
        //Showcase start simpleCookie
        final HttpMaid httpMaid = anHttpMaid()
                .get("/set", (request, response) -> response.setCookie("myCookie", "foo"))
                .build();
        //Showcase end simpleCookie
        pureJavaEndpointFor(httpMaid).listeningOnThePort(1337);
    }
}
