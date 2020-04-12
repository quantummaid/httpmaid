package de.quantummaid.httpmaid.documentation.cookies;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.documentation.support.Deployer;
import de.quantummaid.httpmaid.documentation.support.browser.Browser;
import org.junit.jupiter.api.Test;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.documentation.support.browser.SimulatedBrowser.simulatedBrowser;
import static de.quantummaid.httpmaid.http.headers.cookies.CookieBuilder.cookie;
import static java.util.concurrent.TimeUnit.HOURS;

public class CookieExampleTests {

    @Test
    public void simpleCookieExample() {
        //Showcase start simpleCookie
        final HttpMaid httpMaid = anHttpMaid()
                .get("/set", (request, response) -> response.setCookie("myCookie", "foo"))
                .build();
        //Showcase end simpleCookie

        Deployer.test(httpMaid, client -> {
            final Browser browser = simulatedBrowser(client);
            browser.request("/set");
            browser.assertCookieExists("myCookie=\"foo\"");
        });
    }

    @Test
    public void lifetimeCookieExample() {
        //Showcase start lifetimeCookie
        final HttpMaid httpMaid = anHttpMaid()
                .get("/setWithOptions", (request, response) -> response.setCookie(cookie("myCookie", "foo").withMaxAge(2, HOURS)))
                .build();
        //Showcase end lifetimeCookie

        Deployer.test(httpMaid, client -> {
            final Browser browser = simulatedBrowser(client);
            browser.request("/setWithOptions");
            browser.assertCookieExists("myCookie=\"foo\"; Max-Age=7200");
        });
    }

    @Test
    public void requestCookieExample() {
        //Showcase start requestCookie
        final HttpMaid httpMaid = anHttpMaid()
                .get("/get", (request, response) -> {
                    final String myCookie = request.cookies().getCookie("myCookie");
                    response.setBody("Value was: " + myCookie);
                })
                .build();
        //Showcase end requestCookie

        Deployer.test(httpMaid, client -> {
            final Browser browser = simulatedBrowser(client);
            browser.injectCookie("myCookie", "foobar");
            browser.request("/get");
            browser.assertCurrentPageContains("Value was: foobar");
        });
    }

    @Test
    public void invalidateCookieExample() {
        //Showcase start invalidateCookie
        final HttpMaid httpMaid = anHttpMaid()
                .get("/invalidate", (request, response) -> response.invalidateCookie("myCookie"))
                .build();
        //Showcase end invalidateCookie

        Deployer.test(httpMaid, client -> {
            final Browser browser = simulatedBrowser(client);
            browser.request("/invalidate");
            browser.assertCookieExists("myCookie=\"\"; Expires=Thu, 01 Jan 1970 00:00:00 GMT");
        });
    }
}
