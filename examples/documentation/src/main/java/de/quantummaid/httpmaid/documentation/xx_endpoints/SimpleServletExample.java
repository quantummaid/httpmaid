package de.quantummaid.httpmaid.documentation.xx_endpoints;

import de.quantummaid.httpmaid.HttpMaid;

import javax.servlet.http.HttpServlet;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.servlet.ServletEndpoint.servletEndpointFor;
import static de.quantummaid.httpmaid.servletwithwebsockets.WebSocketAwareHttpMaidServlet.webSocketAwareHttpMaidServlet;

public class SimpleServletExample {

    public static void main(String[] args) {

        final HttpMaid httpMaid = anHttpMaid()
                .build();

        //Showcase start servletSample
        final HttpServlet servlet = servletEndpointFor(httpMaid);
        //Showcase end servletSample

        //Showcase start websocketServletSample
        final HttpServlet servletWithWebsockets = webSocketAwareHttpMaidServlet(httpMaid);
        //Showcase end websocketServletSample
    }
}
