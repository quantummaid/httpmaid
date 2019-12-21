package de.quantummaid.httpmaid.documentation.xx_cookies;

import de.quantummaid.httpmaid.HttpMaid;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.http.headers.cookies.CookieBuilder.cookie;
import static de.quantummaid.httpmaid.purejavaendpoint.PureJavaEndpoint.pureJavaEndpointFor;
import static java.util.concurrent.TimeUnit.HOURS;

public class LifetimeCookieExample {

    public static void main(String[] args) {
        //Showcase start lifetimeCookie
        final HttpMaid httpMaid = anHttpMaid()
                .get("/setWithOptions", (request, response) -> response.setCookie(cookie("myCookie", "foo").withMaxAge(2, HOURS)))
                .build();
        //Showcase end lifetimeCookie
        pureJavaEndpointFor(httpMaid).listeningOnThePort(1337);
    }
}
